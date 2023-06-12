package com.example.farmerscollective.realtime

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
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
import com.example.farmerscollective.utils.Utils
import com.example.farmerscollective.utils.Utils.Companion.adjustAxis
import com.example.farmerscollective.utils.Utils.Companion.internationalPricesCrops
import com.example.farmerscollective.utils.Utils.Companion.ready
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet

class IntPriceFragment : Fragment() {

    companion object {
        fun newInstance() = IntPriceFragment()
    }

    private val viewModel by viewModels<IntPriceViewModel>()
    private lateinit var binding: FragmentIntPriceBinding

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_int_price, container, false)

        with(binding) {
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, resources.getStringArray(R.array.internationalCropName))
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            cropSpinner.adapter = adapter
            cropSpinner.setSelection(viewModel.crop.value!!)

            cropSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    viewModel.changeCropId(p2)
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {

                }
            }

            viewModel.prices.observe(viewLifecycleOwner) {
                lineChart.clear()
                Log.e("Tag", it.toString())
                if(it.isNotEmpty()) {
                    binding.xAxis.text =
                        "Submission Dates (${it.first().date} - ${
                            it.last().date
                        })"
                }
                else {
                    binding.xAxis.text = "Submission Dates"
                }

                val list = it
                val axis = ArrayList<String>()
                val entries: ArrayList<ILineDataSet> = ArrayList()
                val values = ArrayList<Entry>()

                var maxPrice: Float = 0f
                var minPrice: Float = 35000f


                lineChart.onChartGestureListener =
                    Utils.Companion.CustomChartListener(requireContext(), lineChart, axis)

                if(list != null) {
                    var pos = 0
                    for(i in list) {
                        axis.add(i.date)
                        values.add(Entry(pos.toFloat(), i.price))
                        if(i.price > maxPrice) {
                            maxPrice = i.price + 400f
                        }
                        if(i.price < minPrice) {
                            minPrice = i.price - 400f
                        }
                        pos += 1
                    }
                }
                val dataSet = LineDataSet(values, internationalPricesCrops[viewModel.crop.value!!])
                dataSet.setDrawCircles(true)
                entries.add(dataSet)

                lineChart.data = LineData(entries)
                lineChart.axisRight.isEnabled = false
                lineChart.axisLeft.axisMaximum = maxPrice
                lineChart.axisLeft.axisMinimum = minPrice
                lineChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
                lineChart.xAxis.granularity = 1f
                lineChart.xAxis.valueFormatter = IndexAxisValueFormatter(axis)
                lineChart.legend.isWordWrapEnabled = true
                lineChart.axisLeft.setDrawGridLines(false)
                lineChart.xAxis.setDrawGridLines(false)
                adjustAxis(lineChart)
                lineChart.invalidate()
            }
        }

        return binding.root
    }


}