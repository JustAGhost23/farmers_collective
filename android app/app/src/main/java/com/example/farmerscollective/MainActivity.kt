package com.example.farmerscollective

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.work.*
import com.example.farmerscollective.workers.DailyWorker
import com.example.farmerscollective.workers.OneTimeWorker
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {

    private lateinit var controller: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        controller = Navigation.findNavController(this, R.id.nav_host_fragment_container)

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

    }


    fun toHome(view: View) {
        controller.navigate(R.id.mainFragment)
    }




}