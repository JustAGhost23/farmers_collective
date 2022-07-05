package com.example.farmerscollective

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.work.*
import com.example.farmerscollective.workers.DataWorker
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

        val worker = PeriodicWorkRequestBuilder<DataWorker>(1, TimeUnit.DAYS)
            .setConstraints(constraints)
            .build()



        WorkManager
            .getInstance(applicationContext)
            .enqueueUniquePeriodicWork("data", ExistingPeriodicWorkPolicy.KEEP, worker)

    }

    fun toHome(view: View) {
        controller.navigate(R.id.mainFragment)
    }
}