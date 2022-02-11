package com.example.farmerscollective

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.NavArgs
import androidx.navigation.fragment.navArgs

class CropCompetitorFragment : Fragment() {

    companion object {
        fun newInstance() = CropCompetitorFragment()
    }

    private lateinit var viewModel: CropCompetitorViewModel
    val args: CropCompetitorFragmentArgs by navArgs()
    private lateinit var text: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.crop_competitor_fragment, container, false)
        text = view.findViewById(R.id.cropData)

        text.text = args.crop
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(CropCompetitorViewModel::class.java)
        // TODO: Use the ViewModel
    }

}