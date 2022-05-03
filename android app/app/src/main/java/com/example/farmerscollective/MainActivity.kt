package com.example.farmerscollective

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.Navigation
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.farmerscollective.ui.main.MainFragment
import com.example.farmerscollective.workers.PredictWorker
import com.example.farmerscollective.workers.PriceWorker
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var controller: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        controller = Navigation.findNavController(this, R.id.nav_host_fragment_container)

        val worker1 = PeriodicWorkRequestBuilder<PriceWorker>(1, TimeUnit.DAYS).build()
        val worker2 = PeriodicWorkRequestBuilder<PredictWorker>(5, TimeUnit.DAYS).build()

        WorkManager
            .getInstance(applicationContext)
            .enqueueUniquePeriodicWork("price", ExistingPeriodicWorkPolicy.KEEP, worker1)

        WorkManager
            .getInstance(applicationContext)
            .enqueueUniquePeriodicWork("predict", ExistingPeriodicWorkPolicy.KEEP, worker2)


    }

    fun toHome(view: View) {
        controller.navigate(R.id.mainFragment)
    }
}