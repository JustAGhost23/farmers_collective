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

class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    private lateinit var viewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = DataBindingUtil.inflate<MainFragmentBinding>(
            inflater, R.layout.main_fragment, container, false)

        val test = activity?.getSharedPreferences("prefs", Context.MODE_PRIVATE)?.getBoolean("isDailyDataAvailable", false)
        Log.d("TESTING", test.toString())

        with(binding) {

            btn1.setOnClickListener {
                val clickDaily = activity?.getSharedPreferences("prefs", Context.MODE_PRIVATE)?.getBoolean("isDailyDataAvailable", false)
                val clickWeekly = activity?.getSharedPreferences("prefs", Context.MODE_PRIVATE)?.getBoolean("isWeeklyDataAvailable", false)
                Log.d("TESTING", clickDaily.toString())
                if(clickDaily == true && clickWeekly == true) it.findNavController().navigate(R.id.action_mainFragment_to_cropPricesFragment)
                else Toast.makeText(activity, "Please wait! Still loading data", Toast.LENGTH_SHORT).show()
            }

            btn2.setOnClickListener {
                val clickDaily = activity?.getSharedPreferences("prefs", Context.MODE_PRIVATE)?.getBoolean("isDailyDataAvailable", false)
                val clickWeekly = activity?.getSharedPreferences("prefs", Context.MODE_PRIVATE)?.getBoolean("isWeeklyDataAvailable", false)
                Log.d("TESTING", clickDaily.toString())
                if(clickDaily == true && clickWeekly == true) it.findNavController().navigate(R.id.action_mainFragment_to_cropPredictedFragment2)
                else Toast.makeText(activity, "Please wait! Still loading data", Toast.LENGTH_SHORT).show()
            }

            btn3.setOnClickListener {
                val clickDaily = activity?.getSharedPreferences("prefs", Context.MODE_PRIVATE)?.getBoolean("isDailyDataAvailable", false)
                val clickWeekly = activity?.getSharedPreferences("prefs", Context.MODE_PRIVATE)?.getBoolean("isWeeklyDataAvailable", false)
                Log.d("TESTING", clickDaily.toString())
                if(clickDaily == true && clickWeekly == true) it.findNavController().navigate(R.id.action_mainFragment_to_cropPastPredictedFragment)
                else Toast.makeText(activity, "Please wait! Still loading data", Toast.LENGTH_SHORT).show()
            }

            btn4.setOnClickListener {
                it.findNavController().navigate(R.id.action_mainFragment_to_aboutFragment)
            }

            btn5.setOnClickListener {
                val clickDaily = activity?.getSharedPreferences("prefs", Context.MODE_PRIVATE)?.getBoolean("isDailyDataAvailable", false)
                val clickWeekly = activity?.getSharedPreferences("prefs", Context.MODE_PRIVATE)?.getBoolean("isWeeklyDataAvailable", false)
                Log.d("TESTING", clickDaily.toString())
                if(clickDaily == true && clickWeekly == true) it.findNavController().navigate(R.id.action_mainFragment_to_odkFragment)
                else Toast.makeText(activity, "Please wait! Still loading data", Toast.LENGTH_SHORT).show()
            }

        }


        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        // TODO: Use the ViewModel
    }

}