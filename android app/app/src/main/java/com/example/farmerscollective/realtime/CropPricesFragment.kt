package com.example.farmerscollective.realtime

import android.graphics.Color
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
import com.example.farmerscollective.databinding.CropPricesFragmentBinding
import com.example.farmerscollective.utils.Utils
import com.example.farmerscollective.utils.Utils.Companion.ready
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.chip.Chip
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.stream.Stream

class CropPricesFragment : Fragment() {

    private val viewModel by viewModels<CropPricesViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = DataBindingUtil.inflate<CropPricesFragmentBinding>(inflater, R.layout.crop_prices_fragment, container, false)

        val dataByYear = ArrayList<ILineDataSet>()
        val dataByMandi = ArrayList<ILineDataSet>()
        val colors = listOf("#FF0000", "#00FF00", "#0000FF", "#FFFF00", "#00FFFF", "#FF00FF", "#000000", "#DDFFDD")

        val mandiColors = mutableMapOf<String, String>()

        val yearColors = mutableMapOf<Int, Int>()
        resources.getStringArray(R.array.mandi).forEachIndexed { i, str ->
            mandiColors[str] = colors[i]
        }

        with(binding) {

            val adap1 = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, resources.getStringArray(
                R.array.mandi
            ))
            adap1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            mandiSelector.adapter = adap1

            mandiSelector.setSelection(adap1.getPosition("MAHARASHTRA_NAGPUR_Price"))

            mandiSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    viewModel.changeMandi(resources.getStringArray(R.array.mandi)[p2])
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    //do nothing
                }
            }
            val adap2 = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, resources.getStringArray(
                R.array.commodity
            ))
            adap2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            commSelector.adapter = adap2

            val current = if (LocalDate.now().isBefore(LocalDate.of(LocalDate.now().year, 6, 30)))
                LocalDate.now().year - 1
            else LocalDate.now().year

            val arr = current-7..current

            val adap3 = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, arr.toList())
            adap3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            yearSelector.adapter = adap3
            yearSelector.setSelection(adap3.getPosition(current))

            yearSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    viewModel.changeYear(arr.toList()[p2])
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    //do nothing
                }
            }

            arr.forEachIndexed { i, item ->
                val text = "${item}-${(item + 1) % 100}"
                yearColors[item] = Color.parseColor(colors[i])

                val chip = Chip(context)
                chip.text = text
                chip.isCheckable = true
//                val chipDrawable = ChipDrawable.createFromAttributes(
//                    requireContext(),
//                    null,
//                    0,
//                    R.style.Widget_Material3_Chip_Filter
//                )
//                chip.setChipDrawable(chipDrawable)
                if(item == current) chip.isChecked = true

                chip.setOnCheckedChangeListener { button, b ->
                    viewModel.selectYear(item, b)
                }

                chipGroup.addView(chip)
            }


            val mandis = resources.getStringArray(R.array.mandi)

            for(mandi in mandis) {

                val mChip = Chip(context)
                mChip.text = mandi
                mChip.isCheckable = true

                if(mandi == "MAHARASHTRA_NAGPUR_Price") mChip.isChecked = true

                mChip.setOnCheckedChangeListener { button, b ->
                    viewModel.selectMandi(mandi, b)
                }

                chipGroup2.addView(mChip)
            }


            ready(yearChart)
            ready(mandiChart)

            val dates = ArrayList<String>()

            val start = LocalDate.of(2001, 7, 1)
            val end = LocalDate.of(2002, 6, 30)

            Stream.iterate(start) { d ->
                d.plusDays(1)
            }
                .limit(start.until(end, ChronoUnit.DAYS))
                .forEach { date ->
                    val dt = date.toString()
                    dates.add(dt.substring(8) + dt.substring(4, 7))
                }


            viewModel.dataByYear.observe(viewLifecycleOwner) {
                Log.d("observer", "dataByYear")

                yearChart.clear()
                dataByYear.clear()

                if (it.isNotEmpty()) {

                    for (year in it) {

                        val values1 = ArrayList<Entry>()

                        for (date in dates) {
                            val i = dates.indexOf(date)

                            if (year.value.containsKey(date) && !year.value[date]!!.equals(0f))
                                values1.add(Entry(i.toFloat(), year.value[date]!!))
                        }

                        val dataset1 = LineDataSet(values1, year.key.toString())
                        dataset1.setDrawCircles(false)
                        dataset1.color = yearColors[year.key]!!
                        dataset1.lineWidth = 2f

                        dataByYear.add(dataset1)

                    }

                    yearChart.xAxis.valueFormatter = IndexAxisValueFormatter(dates)
                    // enable scaling and dragging
                    yearChart.data = LineData(dataByYear)
                }

                yearChart.onChartGestureListener =
                    Utils.Companion.CustomChartListener(requireContext(), yearChart, dates)
                yearChart.setVisibleXRangeMaximum(10.0f)

                yearChart.invalidate()

            }

            viewModel.dataByMandi.observe(viewLifecycleOwner) {

                mandiChart.clear()
                dataByMandi.clear()

                if (it.isNotEmpty()) {

                    for (mandi in it) {

                        val values1 = ArrayList<Entry>()

                        for (date in dates) {
                            val i = dates.indexOf(date)

                            if (mandi.value.containsKey(date))
                                values1.add(Entry(i.toFloat(), mandi.value[date]!!))
                        }

                        val dataset1 = LineDataSet(values1, mandi.key)
                        dataset1.setDrawCircles(false)
                        dataset1.color = Color.parseColor(mandiColors[mandi.key])
                        dataset1.lineWidth = 2f

                        dataByMandi.add(dataset1)

                    }

                    mandiChart.xAxis.valueFormatter = IndexAxisValueFormatter(dates)
                    // enable scaling and dragging
                    mandiChart.data = LineData(dataByMandi)
                }

                mandiChart.onChartGestureListener =
                    Utils.Companion.CustomChartListener(requireContext(), mandiChart, dates)
                mandiChart.setVisibleXRangeMaximum(10.0f)

                mandiChart.invalidate()
            }
        }

        return binding.root
    }
    


}