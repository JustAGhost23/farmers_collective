package com.example.farmerscollective

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.Navigation
import com.example.farmerscollective.ui.main.MainFragment

class MainActivity : AppCompatActivity() {

    private lateinit var controller: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        controller = Navigation.findNavController(this, R.id.nav_host_fragment_container)
    }

    fun toHome(view: View) {
        controller.navigate(R.id.mainFragment)
    }
}