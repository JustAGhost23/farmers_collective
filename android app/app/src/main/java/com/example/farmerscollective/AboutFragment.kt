package com.example.farmerscollective

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController

// About Fragment
class AboutFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_about, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Add a onClickListener on the header to navigate up to previous fragment
        val header: View = view.findViewById(R.id.about_view2)
        header.setOnClickListener {
            it.findNavController().navigateUp()
        }
    }

}