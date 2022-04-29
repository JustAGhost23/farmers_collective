package com.example.farmerscollective.ui.main

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.findNavController
import com.example.farmerscollective.R

class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    private lateinit var viewModel: MainViewModel
    private lateinit var btn1: Button
    private lateinit var btn2: Button
    private lateinit var btn3: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.main_fragment, container, false)

        btn1 = view.findViewById(R.id.btn1)
        btn2 = view.findViewById(R.id.btn2)
        btn3 = view.findViewById(R.id.btn3)

        btn1.setOnClickListener {
            view.findNavController().navigate(R.id.action_mainFragment_to_cropPricesFragment)
        }

        btn2.setOnClickListener {
            view.findNavController().navigate(R.id.action_mainFragment_to_cropPredictedFragment2)
        }

        btn3.setOnClickListener {
            view.findNavController().navigate(R.id.action_mainFragment_to_submitFragment)
        }

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        // TODO: Use the ViewModel
    }

}