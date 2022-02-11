package com.example.farmerscollective

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.NavDirections
import androidx.navigation.findNavController


class CompetitorFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_competitor, container, false)
        val btns = mutableListOf<Button>()
        val map = listOf("rice" to R.id.rice, "wheat" to R.id.wheat, "corn" to R.id.corn, "jute" to R.id.jute)

        map.forEach {
            btns.add(view.findViewById(it.second))
        }

        val nav: (String) -> NavDirections = {
            CompetitorFragmentDirections.actionCompetitorFragmentToCropCompetitorFragment(it)
        }

        btns.forEachIndexed { i, btn ->
            btn.setOnClickListener {
                view.findNavController().navigate(nav(map[i].first))
            }
        }

        return view
    }


}