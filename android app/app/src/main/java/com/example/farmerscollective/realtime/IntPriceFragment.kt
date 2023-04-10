package com.example.farmerscollective.realtime

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import com.example.farmerscollective.R
import com.example.farmerscollective.databinding.FragmentIntPriceBinding

class IntPriceFragment : Fragment() {

    companion object {
        fun newInstance() = IntPriceFragment()
    }

    private val viewModel by viewModels<IntPriceViewModel>()
    private lateinit var binding: FragmentIntPriceBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_int_price, container, false)

        with(binding) {
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, resources.getStringArray(R.array.countries))
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            country.adapter = adapter
            country.setSelection(viewModel.country.value!!)

            country.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    viewModel.changeCountry(p2)
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {

                }
            }

            viewModel.prices.observe(viewLifecycleOwner) {

            }
        }

        return binding.root
    }


}