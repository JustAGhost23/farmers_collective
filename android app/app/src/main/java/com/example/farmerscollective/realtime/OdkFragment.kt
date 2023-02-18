package com.example.farmerscollective.realtime

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.farmerscollective.R
import com.example.farmerscollective.databinding.FragmentOdkBinding
import com.example.farmerscollective.utils.Utils.Companion.traders
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.analytics.FirebaseAnalytics
import java.time.LocalDate


class OdkFragment : Fragment() {

    private val viewModel by viewModels<OdkViewModel>()
    private lateinit var binding: FragmentOdkBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_odk, container, false)

        with(binding) {

            val spinAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, resources.getStringArray(R.array.odk_filter))
            spinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = spinAdapter
            spinner.setSelection(viewModel.filter.value!!)

            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    viewModel.filter(p2)
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    //pass
                }
            }

            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, resources.getStringArray(R.array.view))
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            viewSpinner.adapter = adapter
            viewSpinner.setSelection(viewModel.view.value!!)

            viewSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    viewModel.view(p2)
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    //pass
                }
            }

            viewModel.list.observe(viewLifecycleOwner) {
                Log.d(this.toString(), it.keys.toString())
                val adapter = OdkAdapter(it.keys.toTypedArray(), object: OdkAdapter.Listener {
                    override fun onClick(date: LocalDate) {
                        viewModel.selectSubmission(date)

                        viewSpinner.visibility = View.GONE
                        spinner.visibility = View.GONE
                        list.visibility = View.GONE

                        barChart.visibility = View.VISIBLE
                        button.visibility = View.VISIBLE
                    }
                })

                list.adapter = adapter
                list.layoutManager = LinearLayoutManager(requireContext())

            }

            viewModel.dateSelect.observe(viewLifecycleOwner) {

                val valueList = it
                val entries: ArrayList<BarEntry> = ArrayList()
                val axis = ArrayList<String>()

                for(i in it) {
                    if(i!!.localTraderId!! != -1) axis.add(traders[i.localTraderId!! - 1])
                }

                Log.d(this.toString(), axis.toString())

                //fit the data into a bar

                //fit the data into a bar
                for (i in 0 until valueList.size) {
                    val barEntry = BarEntry(i.toFloat(), valueList[i]!!.price.toFloat())
                    entries.add(barEntry)
                }

                val barDataSet = BarDataSet(entries, "")

                val data = BarData(barDataSet)
                barChart.data = data
                barChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
                barChart.xAxis.granularity = 1f
                barChart.xAxis.valueFormatter = IndexAxisValueFormatter(axis)
                barChart.invalidate()
            }

            button.setOnClickListener {
                viewSpinner.visibility = View.VISIBLE
                spinner.visibility = View.VISIBLE
                list.visibility = View.VISIBLE

                barChart.visibility = View.GONE
                button.visibility = View.GONE

            }

        }

        return binding.root
    }

}