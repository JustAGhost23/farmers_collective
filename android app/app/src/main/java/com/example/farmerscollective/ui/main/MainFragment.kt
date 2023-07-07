package com.example.farmerscollective.ui.main

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.example.farmerscollective.R
import com.example.farmerscollective.databinding.MainFragmentBinding

// Main Fragment
class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    private lateinit var viewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Use viewbinding to bind the Main Fragment layout(main_fragment.xml) to Main Fragment.
        val binding = DataBindingUtil.inflate<MainFragmentBinding>(
            inflater, R.layout.main_fragment, container, false
        )

        // Test to check if daily prediction data is availale (not important)
        val test = activity?.getSharedPreferences("prefs", Context.MODE_PRIVATE)
            ?.getBoolean("isDailyDataAvailable", false)
        Log.d("TESTING", test.toString())

        // Setting navigation for buttons in Main Fragment
        // Navigation is allowed only if daily and weekly recommendation data has been obtained successfully
        with(binding) {

            // Setting onClick listener to navigate to realtime Crop Prices Fragment
            btn1.setOnClickListener {
                val clickDaily = activity?.getSharedPreferences("prefs", Context.MODE_PRIVATE)
                    ?.getBoolean("isDailyDataAvailable", false)
                val clickWeekly = activity?.getSharedPreferences("prefs", Context.MODE_PRIVATE)
                    ?.getBoolean("isWeeklyDataAvailable", false)
                Log.d("testingDaily", clickDaily.toString())
                Log.d("testingWeekly", clickWeekly.toString())
                if (clickDaily == true && clickWeekly == true) it.findNavController()
                    .navigate(R.id.action_mainFragment_to_cropPricesFragment)
                else Toast.makeText(activity, "Please wait! Still loading data", Toast.LENGTH_SHORT)
                    .show()
            }

            // Setting onClick listener to navigate to Crop Predictions Fragment
            btn2.setOnClickListener {
                val clickDaily = activity?.getSharedPreferences("prefs", Context.MODE_PRIVATE)
                    ?.getBoolean("isDailyDataAvailable", false)
                val clickWeekly = activity?.getSharedPreferences("prefs", Context.MODE_PRIVATE)
                    ?.getBoolean("isWeeklyDataAvailable", false)
                Log.d("testingDaily", clickDaily.toString())
                Log.d("testingWeekly", clickWeekly.toString())
                if (clickDaily == true && clickWeekly == true) it.findNavController()
                    .navigate(R.id.action_mainFragment_to_cropPredictedFragment2)
                else Toast.makeText(activity, "Please wait! Still loading data", Toast.LENGTH_SHORT)
                    .show()
            }

            // Setting onClick listener to navigate to Crop Past Predictions Fragment
            btn3.setOnClickListener {
                val clickDaily = activity?.getSharedPreferences("prefs", Context.MODE_PRIVATE)
                    ?.getBoolean("isDailyDataAvailable", false)
                val clickWeekly = activity?.getSharedPreferences("prefs", Context.MODE_PRIVATE)
                    ?.getBoolean("isWeeklyDataAvailable", false)
                Log.d("testingDaily", clickDaily.toString())
                Log.d("testingWeekly", clickWeekly.toString())
                if (clickDaily == true && clickWeekly == true) it.findNavController()
                    .navigate(R.id.action_mainFragment_to_cropPastPredictedFragment)
                else Toast.makeText(activity, "Please wait! Still loading data", Toast.LENGTH_SHORT)
                    .show()
            }

            // Setting onClick listener to navigate to About Fragment
            btn4.setOnClickListener {
                it.findNavController().navigate(R.id.action_mainFragment_to_aboutFragment)
            }

            // Setting onClick listener to navigate to ODK Submission Fragment
            btn5.setOnClickListener {
                val clickDaily = activity?.getSharedPreferences("prefs", Context.MODE_PRIVATE)
                    ?.getBoolean("isDailyDataAvailable", false)
                val clickWeekly = activity?.getSharedPreferences("prefs", Context.MODE_PRIVATE)
                    ?.getBoolean("isWeeklyDataAvailable", false)
                Log.d("testingDaily", clickDaily.toString())
                Log.d("testingWeekly", clickWeekly.toString())
                if (clickDaily == true && clickWeekly == true) it.findNavController()
                    .navigate(R.id.action_mainFragment_to_odkFragment)
                else Toast.makeText(activity, "Please wait! Still loading data", Toast.LENGTH_SHORT)
                    .show()
            }

            // Setting onClick listener to navigate to International Crop Prices Fragment
            btn6.setOnClickListener {
                val clickDaily = activity?.getSharedPreferences("prefs", Context.MODE_PRIVATE)
                    ?.getBoolean("isDailyDataAvailable", false)
                val clickWeekly = activity?.getSharedPreferences("prefs", Context.MODE_PRIVATE)
                    ?.getBoolean("isWeeklyDataAvailable", false)
                Log.d("testingDaily", clickDaily.toString())
                Log.d("testingWeekly", clickWeekly.toString())
                if (clickDaily == true && clickWeekly == true) it.findNavController()
                    .navigate(R.id.action_mainFragment_to_internationalPricesFragment)
                else Toast.makeText(activity, "Please wait! Still loading data", Toast.LENGTH_SHORT)
                    .show()
            }

        }

        // Returning binding.root to update the layout with above code
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        // TODO: Use the ViewModel
    }

}