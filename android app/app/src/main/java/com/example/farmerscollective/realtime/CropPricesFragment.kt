package com.example.farmerscollective.realtime

import android.content.Context
import android.graphics.Color
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
import androidx.navigation.findNavController
import com.example.farmerscollective.R
import com.example.farmerscollective.databinding.CropPricesFragmentBinding
import com.example.farmerscollective.utils.FirstDrawListener
import com.example.farmerscollective.utils.Utils
import com.example.farmerscollective.utils.Utils.Companion.ready
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.EntryXComparator
import com.google.android.material.chip.Chip
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.stream.Stream


class CropPricesFragment : Fragment() {

    private val viewModel by viewModels<CropPricesViewModel>()
    private lateinit var binding: CropPricesFragmentBinding
    private lateinit var loadTrace: Trace
    private lateinit var analytics: FirebaseAnalytics

    override fun onAttach(context: Context) {
        super.onAttach(context)
        loadTrace = FirebasePerformance.startTrace("CropPricesFragment-LoadTime")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        FirstDrawListener.registerFirstDrawListener(view, object : FirstDrawListener.OnFirstDrawCallback {
            override fun onDrawingStart() {
                // In practice you can also record this event separately
            }

            override fun onDrawingFinish() {
                // This is when the Fragment UI is completely drawn on the screen
                loadTrace.stop()
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.crop_prices_fragment, container, false)
        analytics = FirebaseAnalytics.getInstance(requireContext())

        val dataByYear = ArrayList<ILineDataSet>()
        val dataByMandi = ArrayList<ILineDataSet>()
        val colors = listOf("#FF0000", "#00FF00", "#0000FF", "#FFFF00", "#00FFFF", "#FF00FF", "#000000", "#DDFFDD")

        val mandiColors = mutableMapOf<String, Int>()

        val yearColors = mutableMapOf<Int, Int>()
        resources.getStringArray(R.array.mandi).forEachIndexed { i, str ->
            mandiColors[str] = Color.parseColor(colors[i])
        }

        val sharedPref =
            requireContext().getSharedPreferences(
                "prefs",
                Context.MODE_PRIVATE
            )

        with(binding) {

            priceView2.setOnClickListener {
                it.findNavController().navigateUp()
            }

            val adap1 = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, resources.getStringArray(
                R.array.mandi
            ))
            adap1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            mandiSelector.adapter = adap1

            mandiSelector.setSelection(adap1.getPosition("MAHARASHTRA_NAGPUR_Price"))

            mandiSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    val bundle = Bundle()
                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, LocalDate.now().toEpochDay().toString())
                    bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, resources.getStringArray(R.array.mandi)[p2])
                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "text")
                    bundle.putString("type", "single-select-mandi")
                    analytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
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

            val arr = (current-7..current).map {
                "${it}-${(it + 1) % 100}"
            }

            val adap3 = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, arr)
            adap3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            yearSelector.adapter = adap3
            yearSelector.setSelection(adap3.getPosition("${current}-${(current + 1) % 100}"))

            yearSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    val bundle = Bundle()
                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, p2.toString())
                    bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, arr.toList()[p2].substring(0, 4))
                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "text")
                    bundle.putString("type", "single-select-year")
                    analytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
                    viewModel.changeYear(arr.toList()[p2].substring(0, 4).toInt())
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    //do nothing
                }
            }

            arr.forEachIndexed { i, item ->
                val year = item.substring(0, 4).toInt()
                yearColors[year] = Color.parseColor(colors[i])

                val chip = Chip(context)
                chip.text = item
                chip.isCheckable = true
//                val chipDrawable = ChipDrawable.createFromAttributes(
//                    requireContext(),
//                    null,
//                    0,
//                    R.style.Widget_Material3_Chip_Filter
//                )
//                chip.setChipDrawable(chipDrawable)
                if(year == current) chip.isChecked = true

                chip.setOnCheckedChangeListener { button, b ->

                    if(b) {
                        val bundle = Bundle()
                        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, i.toString())
                        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, year.toString().plus("-chip"))
                        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "text")
                        bundle.putString("type", "multi-select-year")
                        analytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)

                    }

                    viewModel.selectYear(year, b)
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

                    if(b) {
                        val bundle = Bundle()
                        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, mandis.indexOf(mandi).toString())
                        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, mandi.toString().plus("-chip"))
                        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "text")
                        bundle.putString("type", "multi-select-mandi")
                        analytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
                    }

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
                        val maxMap = mutableMapOf<Int, Pair<String, Float>>()

                        for (date in dates) {
                            val i = dates.indexOf(date)

                            if (year.value.containsKey(date) && !year.value[date]!!.equals(0f)) {
                                values1.add(Entry(i.toFloat(), year.value[date]!!))

                                val m = date.substring(3).toInt()
                                if(maxMap.containsKey(m) && maxMap[m]!!.second < year.value[date]!!)
                                    maxMap[m] = Pair(date, year.value[date]!!)
                                else if(!maxMap.containsKey(m)) maxMap[m] = Pair(date, year.value[date]!!)
                            }

                        }

                        val values2 = ArrayList<Entry>()

                        for(pair in maxMap.values) {
                            values2.add(Entry(dates.indexOf(pair.first).toFloat(), pair.second))
                        }

                        val dataset1 = LineDataSet(values1, year.key.toString())
                        dataset1.setDrawCircles(false)
                        dataset1.color = yearColors[year.key]!!
                        dataset1.lineWidth = 2f

                        val dataset2 = LineDataSet(values2, "")
                        dataset2.color = Color.TRANSPARENT
                        dataset2.setDrawValues(false)
                        dataset2.circleRadius = 5f
                        dataset2.circleHoleRadius = 3f
                        dataset2.setCircleColor(yearColors[year.key]!!)

                        dataByYear.add(dataset1)
                        dataByYear.add(dataset2)

                    }



                    yearChart.xAxis.valueFormatter = IndexAxisValueFormatter(dates)
                    // enable scaling and dragging
                    yearChart.data = LineData(dataByYear)
                }

                yearChart.onChartGestureListener =
                    Utils.Companion.CustomChartListener(requireContext(), yearChart, dates)

                if(!sharedPref.getBoolean("compress", false)) {
                    yearChart.setVisibleXRangeMaximum(10.0f)
                    yearChart.moveViewToX(0.0f)
                }

                yearChart.invalidate()

            }

            viewModel.dataByMandi.observe(viewLifecycleOwner) {

                mandiChart.clear()
                dataByMandi.clear()

                val axis = mandiChart.axisRight
                val axis2 = mandiChart.axisLeft

                val mspLine = LimitLine(Utils.MSP[2015 + yearSelector.selectedItemPosition]!!, "Minimum Support Price")
                mspLine.lineColor = Color.GRAY
                mspLine.lineWidth = 2f
                mspLine.textColor = Color.BLACK
                mspLine.textSize = 8f

                axis.removeAllLimitLines()
                axis.axisMinimum = 0f
                axis2.axisMinimum = 0f
                axis.addLimitLine(mspLine)

                if (it.isNotEmpty()) {

                    for (mandi in it) {



                        val values1 = ArrayList<Entry>()
                        val maxList = MutableList(12) { Pair("", 0.0f) }

                        for (date in dates) {
                            val i = dates.indexOf(date)

                            if (mandi.value.containsKey(date) && mandi.value[date] != 0f) {
                                values1.add(Entry(i.toFloat(), mandi.value[date]!!))

                                val m = date.substring(3).toInt() - 1
                                if(maxList[m].second < mandi.value[date]!!) maxList[m] = Pair(date, mandi.value[date]!!)

                            }

                        }

                        val values2 = ArrayList<Entry>()

                        for(pair in maxList) {
                            if(pair.second != 0f) values2.add(Entry(dates.indexOf(pair.first).toFloat(), pair.second))
                        }

                        Collections.sort(values1, EntryXComparator())
                        Collections.sort(values2, EntryXComparator())

                        val dataset1 = LineDataSet(values1, mandi.key)
                        dataset1.setDrawCircles(false)
                        dataset1.color = mandiColors[mandi.key]!!
                        dataset1.lineWidth = 2f

                        dataByMandi.add(dataset1)

                        val dataset2 = LineDataSet(values2, "")
                        dataset2.color = Color.TRANSPARENT
                        dataset2.setDrawValues(false)
                        dataset2.circleRadius = 5f
                        dataset2.circleHoleRadius = 3f
                        dataset2.setCircleColor(mandiColors[mandi.key]!!)

                        dataByMandi.add(dataset2)

                    }

                    mandiChart.xAxis.valueFormatter = IndexAxisValueFormatter(dates)
                    // enable scaling and dragging
                    mandiChart.data = LineData(dataByMandi)
                }

                mandiChart.onChartGestureListener =
                    Utils.Companion.CustomChartListener(requireContext(), mandiChart, dates)

                if(!sharedPref.getBoolean("compress", false)) {
                    mandiChart.setVisibleXRangeMaximum(10.0f)
                    mandiChart.moveViewToX(0.0f)
                }

                mandiChart.invalidate()
            }

//            sharedPref.registerOnSharedPreferenceChangeListener { sharedPreferences, s ->
//                if(s != "compress") return@registerOnSharedPreferenceChangeListener
//
//                if(sharedPreferences!!.getBoolean("compress", false)) {
//                    mandiChart.setVisibleXRangeMaximum(365.0f)
//                    yearChart.setVisibleXRangeMaximum(365.0f)
//
//                    mandiChart.fitScreen()
//                    yearChart.fitScreen()
//                }
//
//                else {
//                    mandiChart.setVisibleXRangeMaximum(10.0f)
//                    yearChart.setVisibleXRangeMaximum(10.0f)
//
//                    mandiChart.moveViewToX(0.0f)
//                    yearChart.moveViewToX(0.0f)
//                }
//
//
//            }
        }

        return binding.root
    }



}