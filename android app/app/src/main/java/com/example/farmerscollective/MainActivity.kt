package com.example.farmerscollective

import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.*
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.DialogFragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.work.*
import com.example.farmerscollective.utils.ChartRangeDialog
import com.example.farmerscollective.workers.DailyWorker
import com.example.farmerscollective.workers.OneTimeWorker
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.analytics.FirebaseAnalytics
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity(), ChartRangeDialog.DialogListener {

    private lateinit var controller: NavController
    private lateinit var refresh: FloatingActionButton
    private lateinit var settings: FloatingActionButton
    private lateinit var textView: TextView

    override fun onSupportNavigateUp(): Boolean {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
        return super.onSupportNavigateUp()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        controller = Navigation.findNavController(this, R.id.nav_host_fragment_container)
        refresh = findViewById(R.id.refresh)
        settings = findViewById(R.id.settings)
        textView = findViewById(R.id.textView)

        if(resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            textView.setSingleLine()
            settings.updateLayoutParams<ConstraintLayout.LayoutParams> {
                endToStart = refresh.id
                topToTop = R.id.container
            }
            (settings.layoutParams as ConstraintLayout.LayoutParams).apply {
                val scale = applicationContext.resources.displayMetrics.density
                setMargins(0, (16 * scale + 0.5f).toInt(), (16 * scale + 0.5f).toInt(), 0)
            }
        }
        if(resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            textView.setLines(2)
            settings.updateLayoutParams<ConstraintLayout.LayoutParams> {
                topToBottom = refresh.id
                endToEnd = R.id.container
            }
            (settings.layoutParams as ConstraintLayout.LayoutParams).apply {
                topMargin = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 16F, resources
                        .displayMetrics
                ).toInt()
                marginEnd = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 16F, resources
                        .displayMetrics
                ).toInt()
            }
        }

        when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_NO -> {
                textView.setTextColor(Color.BLACK)
            }
            Configuration.UI_MODE_NIGHT_YES -> {
                textView.setTextColor(Color.WHITE)
            }
        }


        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val worker1 = OneTimeWorkRequestBuilder<OneTimeWorker>()
            .setConstraints(constraints)
            .build()

        val worker2 = PeriodicWorkRequestBuilder<DailyWorker>(24, TimeUnit.HOURS)
            .setConstraints(constraints)
            .setInitialDelay(24, TimeUnit.HOURS)
            .build()

        val sharedPref =
            applicationContext.getSharedPreferences(
                "prefs",
                Context.MODE_PRIVATE
            )

        if(!sharedPref.getBoolean("isDataAvailable", false))
            WorkManager
                .getInstance(applicationContext)
                .enqueueUniqueWork("one-time", ExistingWorkPolicy.KEEP, worker1)

        WorkManager
            .getInstance(applicationContext)
            .enqueueUniquePeriodicWork("refresh", ExistingPeriodicWorkPolicy.KEEP, worker2)

        refresh.setOnClickListener {

            if(!sharedPref.getBoolean("isDataAvailable", false)) {
                Toast.makeText(this, "Please wait! Still loading data", Toast.LENGTH_SHORT).show()
            }

            else {
                WorkManager
                    .getInstance(applicationContext)
                    .enqueueUniqueWork("one-time", ExistingWorkPolicy.KEEP, worker1)

                with(sharedPref.edit()) {
                    putBoolean("isDataAvailable", false)
                    apply()
                }

                Toast.makeText(this, "Refreshing data, please wait...", Toast.LENGTH_SHORT).show()

                toHome(refresh)
            }


        }

        settings.setOnClickListener {
            val dialog = ChartRangeDialog()
            dialog.show(supportFragmentManager, "chart range")
        }

    }


    fun toHome(view: View) {
        controller.navigate(R.id.mainFragment)
    }

    override fun onDialogPositiveClick(dialog: DialogFragment, selected: Int) {
        val sharedPref =
            applicationContext.getSharedPreferences(
                "prefs",
                Context.MODE_PRIVATE
            )

        with(sharedPref.edit()) {
            putBoolean("compress", selected == 1)
            apply()
        }

        val id = controller.currentDestination?.id
        controller.popBackStack(id!!,true)
        controller.navigate(id)

        val analytics = FirebaseAnalytics.getInstance(this)
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "compress: ${(selected == 1)}")
        analytics.logEvent("GraphSetting", bundle)
    }


}