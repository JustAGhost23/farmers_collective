package com.example.farmerscollective

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.work.*
import com.example.farmerscollective.utils.ChartRangeDialog
import com.example.farmerscollective.workers.DailyWorker
import com.example.farmerscollective.workers.OneTimeWorker
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity(), ChartRangeDialog.DialogListener {

    private lateinit var controller: NavController
    private lateinit var refresh: FloatingActionButton
    private lateinit var settings: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        controller = Navigation.findNavController(this, R.id.nav_host_fragment_container)
        refresh = findViewById(R.id.refresh)
        settings = findViewById(R.id.settings)


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
    }


}