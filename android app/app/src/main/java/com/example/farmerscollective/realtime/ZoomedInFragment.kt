package com.example.farmerscollective.realtime

import android.app.AlertDialog
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.farmerscollective.R
import com.example.farmerscollective.data.OdkSubmission
import com.example.farmerscollective.databinding.FragmentZoomedInBinding
import com.example.farmerscollective.prediction.CropPastPredictedViewModel
import com.example.farmerscollective.prediction.CropPredictedViewModel
import com.example.farmerscollective.utils.Utils
import com.example.farmerscollective.utils.Utils.Companion.adjustAxis
import com.example.farmerscollective.utils.Utils.Companion.dates
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.LegendEntry
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.EntryXComparator
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.min

// ZoomedIn Fragment
// This fragment holds a graph, which can be accessed via Zoom In buttons available below all graphs
// This provides a zoomed in view of a graph in landscape mode
class ZoomedInFragment : Fragment() {

    // Initialize viewModels and binding
    private lateinit var binding: FragmentZoomedInBinding
    private val intPriceViewModel by activityViewModels<IntPriceViewModel>()
    private val odkViewModel by activityViewModels<OdkViewModel>()
    private val cropViewModel by activityViewModels<CropPricesViewModel>()
    private val pastPredictedViewModel by activityViewModels<CropPastPredictedViewModel>()
    private val predictedViewModel by activityViewModels<CropPredictedViewModel>()
    private val args by navArgs<ZoomedInFragmentArgs>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        // Inflate the layout for this fragment (fragment_zoomed_in.xml)
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_zoomed_in, container, false)

        // Shared Preferences
        val sharedPref =
            requireContext().getSharedPreferences(
                "prefs",
                Context.MODE_PRIVATE
            )

        // Variables to hold pastPredicted and predicted data
        val pastPredictedData = ArrayList<ILineDataSet>()
        val predictedData = ArrayList<ILineDataSet>()

        // Setting functions for UI components using viewbinding
        with(binding) {
            zoomChart.clear()

            // Setting onClick listener to navigate up back to previous fragment
            zoom.setOnClickListener {
                requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
                findNavController().navigateUp()
            }

            // Setup a graph depending on what argument was passed while navigating to ZoomedIn Fragment
            // barChart holds a bar chart, and zoomChart holds a lineChart
            // Visibility of both charts is set accordingly as required (one is not present when the other is visible)
            when (args.chart) {
                0 ->
                    cropViewModel.dataByYear.observe(viewLifecycleOwner) {
                        barChart.visibility = View.GONE
                        zoomChart.visibility = View.VISIBLE

                        zoomChart.clear()

                        // Add data for realtime prices along with xAxis labels
                        zoomChart.xAxis.valueFormatter = IndexAxisValueFormatter(Utils.dates)
                        zoomChart.data = LineData(it)

                        // Add onChartGesture listener
                        zoomChart.onChartGestureListener =
                            Utils.Companion.CustomChartListener(
                                requireContext(), zoomChart,
                                Utils.dates
                            )

                        // Compress settings
                        if (!sharedPref.getBoolean("compress", false)) {
                            zoomChart.setVisibleXRangeMaximum(10.0f)
                        }

                        with(sharedPref.edit()) {
                            putBoolean("cropGraph", true)
                            apply()
                        }

                        // Update zoomChart with updated values
                        adjustAxis(zoomChart)
                        zoomChart.invalidate()

                        // Dark Mode configuration for text
                        when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                            Configuration.UI_MODE_NIGHT_NO -> {
                                binding.zoomedInFragment.setBackgroundColor(Color.WHITE)
                                zoomChart.xAxis.textColor = Color.BLACK
                                zoomChart.legend.textColor = Color.BLACK
                                zoomChart.data.setValueTextColor(Color.BLACK)
                                zoomChart.axisRight.textColor = Color.BLACK
                                zoomChart.axisLeft.textColor = Color.BLACK
                            }
                            Configuration.UI_MODE_NIGHT_YES -> {
                                binding.zoomedInFragment.setBackgroundColor(Color.BLACK)
                                zoomChart.xAxis.textColor = Color.WHITE
                                zoomChart.legend.textColor = Color.WHITE
                                zoomChart.data.setValueTextColor(Color.WHITE)
                                zoomChart.axisRight.textColor = Color.WHITE
                                zoomChart.axisLeft.textColor = Color.WHITE
                            }
                        }

                    }

                1 ->
                    cropViewModel.dataByMandi.observe(viewLifecycleOwner) {
                        barChart.visibility = View.GONE
                        zoomChart.visibility = View.VISIBLE

                        // Initial variables
                        val axis = zoomChart.axisRight
                        val axis2 = zoomChart.axisLeft
                        var min: Float = Float.MAX_VALUE

                        // Set LimitLine for Minimum Support Price
                        val mspLine = LimitLine(
                            Utils.MSP[cropViewModel.year.value]!!,
                            "Minimum Support Price"
                        )
                        if (min > mspLine.limit - 200f) {
                            min = mspLine.limit - 300f
                        }
                        for (item in it) {
                            if (min > item.yMin - 200f) {
                                min = item.yMin - 300f
                            }
                        }
                        mspLine.lineColor = Color.GRAY
                        mspLine.lineWidth = 2f
                        mspLine.textColor = Color.BLACK
                        mspLine.textSize = 8f

                        // Add limit line to graph
                        axis.removeAllLimitLines()
                        axis.axisMinimum = min
                        axis2.axisMinimum = min
                        axis.addLimitLine(mspLine)

                        // Add data for realtime prices along with xAxis labels
                        zoomChart.xAxis.valueFormatter = IndexAxisValueFormatter(Utils.dates)
                        zoomChart.data = LineData(it)

                        // Add onChartGesture listener
                        zoomChart.onChartGestureListener =
                            Utils.Companion.CustomChartListener(
                                requireContext(), zoomChart,
                                Utils.dates
                            )

                        // Compress settings
                        if (!sharedPref.getBoolean("compress", false)) {
                            zoomChart.setVisibleXRangeMaximum(10.0f)
                        }

                        with(sharedPref.edit()) {
                            putBoolean("cropGraph", false)
                            apply()
                        }

                        zoomChart.moveViewToX(Utils.dates.size - 30f)
                        // Update zoomChart with updated values
                        zoomChart.invalidate()

                        // Dark Mode configuration for text
                        when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                            Configuration.UI_MODE_NIGHT_NO -> {
                                mspLine.textColor = Color.BLACK
                                binding.zoomedInFragment.setBackgroundColor(Color.WHITE)
                                zoomChart.xAxis.textColor = Color.BLACK
                                zoomChart.legend.textColor = Color.BLACK
                                zoomChart.data.setValueTextColor(Color.BLACK)
                                zoomChart.axisRight.textColor = Color.BLACK
                                zoomChart.axisLeft.textColor = Color.BLACK
                            }
                            Configuration.UI_MODE_NIGHT_YES -> {
                                mspLine.textColor = Color.WHITE
                                binding.zoomedInFragment.setBackgroundColor(Color.BLACK)
                                zoomChart.xAxis.textColor = Color.WHITE
                                zoomChart.legend.textColor = Color.WHITE
                                zoomChart.data.setValueTextColor(Color.WHITE)
                                zoomChart.axisRight.textColor = Color.WHITE
                                zoomChart.axisLeft.textColor = Color.WHITE
                            }
                        }

                    }

                2 ->
                    intPriceViewModel.prices.observe(viewLifecycleOwner) {
                        barChart.visibility = View.GONE
                        zoomChart.visibility = View.VISIBLE

                        zoomChart.clear()
                        Log.e("Tag", it.toString())

                        // Initial variables
                        val list = it
                        val axis = ArrayList<String>()
                        val entries: ArrayList<ILineDataSet> = ArrayList()
                        val values = ArrayList<Entry>()

                        // Max Price and Min Price for graph
                        var maxPrice: Float = 0f
                        var minPrice: Float = 35000f

                        // Add axis labels to axis list and obtain maximum and minimum price
                        if (list != null) {
                            var pos = 0
                            for (i in list) {
                                axis.add(i.date.substring(5, 10))
                                values.add(Entry(pos.toFloat(), i.price))
                                if (i.price > maxPrice - 100f) {
                                    maxPrice = i.price + 200f
                                }
                                if (i.price < minPrice + 100f) {
                                    minPrice = i.price - 200f
                                }
                                pos += 1
                            }
                        }

                        // onChartGestureListener from Utils
                        zoomChart.onChartGestureListener =
                            Utils.Companion.CustomChartListener(requireContext(), zoomChart, axis)


                        // Create dataset and set values
                        val dataSet = LineDataSet(values, Utils.internationalPricesCrops[intPriceViewModel.crop.value!!])
                        dataSet.color = Color.parseColor("#000000")
                        dataSet.setDrawCircles(false)
                        entries.add(dataSet)

                        // Set custom specifications for zoomChart as needed
                        zoomChart.data = LineData(entries)
                        zoomChart.description.isEnabled = false
                        zoomChart.axisRight.isEnabled = true
                        zoomChart.axisLeft.isEnabled = false
                        zoomChart.axisRight.axisMaximum = maxPrice
                        zoomChart.axisRight.axisMinimum = minPrice
                        zoomChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
                        zoomChart.xAxis.granularity = 1f
                        zoomChart.xAxis.valueFormatter = IndexAxisValueFormatter(axis)
                        zoomChart.legend.isWordWrapEnabled = true
                        zoomChart.axisRight.setDrawGridLines(false)
                        // Compress settings
                        if (it.isNotEmpty()) {
                            if (!sharedPref.getBoolean("compress", false)) {
                                zoomChart.moveViewToX(entries[0].xMax)
                                zoomChart.setVisibleXRangeMaximum(10.0f)
                            } else {
                                zoomChart.moveViewToX(entries[0].xMax)
                                zoomChart.setVisibleXRangeMaximum(365.0f)
                            }
                        }
                        // Update zoomChart with new values
                        zoomChart.invalidate()
                    }
                3 -> odkViewModel.list.observe(viewLifecycleOwner) {
                    barChart.visibility = View.VISIBLE
                    zoomChart.visibility = View.GONE

                    Log.d(this.toString(), it.keys.toString())
                    // Date Time Formatter
                    val formatter: DateTimeFormatter =
                        DateTimeFormatter.ofPattern("MM-dd")
                    // Initial values
                    val list = it
                    val entries1: ArrayList<BarEntry> = ArrayList()
                    val entries2: ArrayList<BarEntry> = ArrayList()
                    val entries3: ArrayList<BarEntry> = ArrayList()
                    val entries4: ArrayList<BarEntry> = ArrayList()
                    val barColors1: ArrayList<Int> = ArrayList()
                    val barColors2: ArrayList<Int> = ArrayList()
                    val barColors3: ArrayList<Int> = ArrayList()
                    val barColors4: ArrayList<Int> = ArrayList()
                    val colorList: ArrayList<LegendEntry> = ArrayList()
                    val traderList: ArrayList<String> = ArrayList()
                    val axis = ArrayList<String?>()
                    val subs = mutableMapOf<Int, List<OdkSubmission?>>()

                    // Minimium Price
                    var minPrice = 3500f
                    var xMax = 0f

                    // Add axis labels
                    if (list != null) {
                        for (i in list) {
                            axis.add(i.key.format(formatter))
                        }
                    }

                    // Entries are split into 4 parts, each having the nth best price for a given crop and day, where n lies between 1 and 4
                    // To accomodate for days that have less than 4 prices, we add -1000 for the other prices, so as to maintain continuity
                    // First all entry lists are filled with -1000, if a value to replace exists then -1000 is removed and other value is added
                    if (list != null) {
                        var count = 0f
                        for (i in list) {
                            xMax += 1f
                            var pos = count

                            // Obtain highest 4 values per day
                            val v = i.value.reversed().sortedBy { it!!.price }
                                .subList(0, min(4, i.value.size))

                            // Fill all entries with -1000
                            entries1.add(BarEntry(pos, -1000f))
                            entries2.add(BarEntry(pos + 0.20f, -1000f))
                            entries3.add(BarEntry(pos + 0.40f, -1000f))
                            entries4.add(BarEntry(pos + 0.60f, -1000f))
                            barColors1.add(0)
                            barColors2.add(0)
                            barColors3.add(0)
                            barColors4.add(0)
                            for (j in v) {
                                // While adding a new ODKSubmission into its respective list, we also add a LegendEntry and specific color
                                // To handle cases where trader is not added, we have added an extra option "Not filled"
                                // Hence the if-else statement to handle this case
                                if (j != null) {
                                    val legendEntry: LegendEntry
                                    if (j.localTraderId == -1) {
                                        legendEntry = LegendEntry(
                                            "Not filled",
                                            Legend.LegendForm.SQUARE,
                                            10.0f,
                                            10.0f,
                                            null,
                                            Color.parseColor(
                                                Utils.traderColors[j.localTraderId.plus(1)]
                                            )
                                        )
                                        if (v.indexOf(j) == 0) {
                                            barColors1.removeAt(barColors1.size - 1)
                                            barColors1.add(
                                                Color.parseColor(
                                                    Utils.traderColors[j.localTraderId.plus(1)]
                                                )
                                            )
                                        } else if (v.indexOf(j) == 1) {
                                            barColors2.removeAt(barColors2.size - 1)
                                            barColors2.add(
                                                Color.parseColor(
                                                    Utils.traderColors[j.localTraderId.plus(1)]
                                                )
                                            )
                                        } else if (v.indexOf(j) == 2) {
                                            barColors3.removeAt(barColors3.size - 1)
                                            barColors3.add(
                                                Color.parseColor(
                                                    Utils.traderColors[j.localTraderId.plus(1)]
                                                )
                                            )
                                        } else if (v.indexOf(j) == 3) {
                                            barColors4.removeAt(barColors4.size - 1)
                                            barColors4.add(
                                                Color.parseColor(
                                                    Utils.traderColors[j.localTraderId.plus(1)]
                                                )
                                            )
                                        }
                                        if (!traderList.contains("Not filled")) {
                                            colorList.add(legendEntry)
                                            traderList.add("Not filled")
                                        }
                                    } else {
                                        legendEntry = LegendEntry(
                                            Utils.traders[j.localTraderId?.minus(1)!!],
                                            Legend.LegendForm.SQUARE,
                                            10.0f,
                                            10.0f,
                                            null,
                                            Color.parseColor(
                                                Utils.traderColors[j.localTraderId]
                                            )
                                        )
                                        if (v.indexOf(j) == 0) {
                                            barColors1.removeAt(barColors1.size - 1)
                                            barColors1.add(
                                                Color.parseColor(
                                                    Utils.traderColors[j.localTraderId]
                                                )
                                            )
                                        } else if (v.indexOf(j) == 1) {
                                            barColors2.removeAt(barColors2.size - 1)
                                            barColors2.add(
                                                Color.parseColor(
                                                    Utils.traderColors[j.localTraderId]
                                                )
                                            )
                                        } else if (v.indexOf(j) == 2) {
                                            barColors3.removeAt(barColors3.size - 1)
                                            barColors3.add(
                                                Color.parseColor(
                                                    Utils.traderColors[j.localTraderId]
                                                )
                                            )
                                        } else if (v.indexOf(j) == 3) {
                                            barColors4.removeAt(barColors4.size - 1)
                                            barColors4.add(
                                                Color.parseColor(
                                                    Utils.traderColors[j.localTraderId]
                                                )
                                            )
                                        }
                                        if (!traderList.contains(Utils.traders[j.localTraderId.minus(1)])) {
                                            colorList.add(legendEntry)
                                            traderList.add(Utils.traders[j.localTraderId.minus(1)])
                                        }
                                    }
                                }
                                // Add odkSubmission to respective entries list
                                val barEntry = BarEntry(pos, j!!.price.toFloat())
                                if (j.price.toFloat() < minPrice) {
                                    minPrice = j.price.toFloat() - 200f
                                }
                                if (v.indexOf(j) == 0) {
                                    entries1.removeAt(entries1.size - 1)
                                    entries1.add(barEntry)
                                } else if (v.indexOf(j) == 1) {
                                    entries2.removeAt(entries2.size - 1)
                                    entries2.add(barEntry)
                                } else if (v.indexOf(j) == 2) {
                                    entries3.removeAt(entries3.size - 1)
                                    entries3.add(barEntry)
                                } else if (v.indexOf(j) == 3) {
                                    entries4.removeAt(entries4.size - 1)
                                    entries4.add(barEntry)
                                }
                                subs[pos.toInt()] = v
                                pos += 0.20f
                            }
                            count += 1f
                        }
                    }
                    Log.e("entries1", entries1.toString())
                    Log.e("entries2", entries2.toString())
                    Log.e("entries3", entries3.toString())
                    Log.e("entries4", entries4.toString())

                    // Create datasets and specify colors
                    val barDataSet1 = BarDataSet(entries1, "")
                    val barDataSet2 = BarDataSet(entries2, "")
                    val barDataSet3 = BarDataSet(entries3, "")
                    val barDataSet4 = BarDataSet(entries4, "")
                    barDataSet1.colors = barColors1
                    barDataSet2.colors = barColors2
                    barDataSet3.colors = barColors3
                    barDataSet4.colors = barColors4

                    // Create BarDataSet for multiple bar graphs
                    val data = BarData(barDataSet1, barDataSet2, barDataSet3, barDataSet4)

                    // Set custom specifications for barChart as needed
                    data.barWidth = 0.20f
                    barChart.description.isEnabled = false
                    barChart.axisRight.isEnabled = true
                    barChart.axisLeft.isEnabled = false
                    barChart.legend.setCustom(colorList)
                    barChart.legend.isWordWrapEnabled = true
                    barChart.axisRight.setDrawGridLines(false)
                    barChart.axisLeft.setDrawGridLines(false)
                    barChart.xAxis.setDrawGridLines(true)
                    barChart.data = data
                    barChart.axisRight.axisMinimum = minPrice
                    barChart.axisLeft.axisMinimum = minPrice
                    barChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
                    barChart.xAxis.granularity = 1f
                    barChart.xAxis.valueFormatter = IndexAxisValueFormatter(axis)
                    barChart.isHighlightPerDragEnabled = false
                    barChart.groupBars(0f, 0.12f, 0.02f)
                    barChart.xAxis.setCenterAxisLabels(true)
                    barChart.moveViewToX(xMax)
                    barChart.setFitBars(false)
                    // Compress settings
                    if (!sharedPref.getBoolean("compress", false)) {
                        barChart.setVisibleXRangeMaximum(8.0f)
                    } else {
                        barChart.setVisibleXRangeMaximum(365.0f)
                    }
                    // Add onChartValueSelected listener to show dialog for selected ODK Submission
                    barChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                        override fun onValueSelected(e: Entry, h: Highlight?) {
                            val x = e.x.toString()
                            val selectedXAxisCount = x.substringBefore(".")
                            val selectedXAxisPos = x.substringAfter(".").substring(0, 2)

                            // Build dialog for fragment
                            val dataDialogBuilder: AlertDialog.Builder? =
                                activity?.let { fragmentActivity ->
                                    AlertDialog.Builder(fragmentActivity)
                                }
                            // Fill dialog with required fields and show
                            if (subs[selectedXAxisCount.toInt()]?.size!! > selectedXAxisPos.toInt() / 25) {
                                val selectedOdkSubmission =
                                    subs[selectedXAxisCount.toInt()]?.get(selectedXAxisPos.toInt() / 25)
                                val traderName =
                                    if (selectedOdkSubmission?.localTraderId!! == -1) "Not filled" else Utils.traders[selectedOdkSubmission.localTraderId - 1]
                                val mandalId =
                                    if (selectedOdkSubmission.mandalId == "") "Not filled" else selectedOdkSubmission.mandalId
                                dataDialogBuilder?.setMessage("Trader Name: ${traderName}\nMandal: ${mandalId}\nPrice: Rs ${selectedOdkSubmission.price}\nFilled by: ${selectedOdkSubmission.personFillingId}\nFilled on: ${selectedOdkSubmission.date}")!!
                                    .setCancelable(false)
                                    .setPositiveButton("Dismiss") { dialog, _ ->
                                        barChart.highlightValues(null)
                                        dialog.dismiss()
                                    }
                                val dataDialog: AlertDialog = dataDialogBuilder.create()
                                dataDialog.setTitle("ODK Data")
                                dataDialog.show()
                            }
                        }

                        override fun onNothingSelected() {
                            //pass
                        }
                    })
                    // Update barChart with updated values
                    barChart.invalidate()
                }
                4 ->
                    pastPredictedViewModel.graph.observe(viewLifecycleOwner) {
                        barChart.visibility = View.GONE
                        zoomChart.visibility = View.VISIBLE

                        zoomChart.clear()
                        pastPredictedData.clear()

                        // Add and sort days to list
                        val dates = ArrayList<String>()

                        dates.addAll(it[0].keys)

                        dates.sortWith { date1, date2 ->
                            val d1 = LocalDate.parse(date1)
                            val d2 = LocalDate.parse(date2)

                            d1.compareTo(d2)
                        }

                        // values1 -> Predicted values
                        // values2 -> Actual values
                        val values1 = ArrayList<Entry>()
                        val values2 = ArrayList<Entry>()
                        val values3 = ArrayList<Entry>()

                        // Add values to values1 and values2 accordingly
                        for (date in dates) {
                            val i = dates.indexOf(date)
                            if (it[0].containsKey(date)) values1.add(
                                Entry(
                                    i.toFloat(),
                                    it[0][date]!!
                                )
                            )
                            if (it[1].containsKey(date)) values2.add(
                                Entry(
                                    i.toFloat(),
                                    it[1][date]!!
                                )
                            )
                        }

                        val hls = pastPredictedViewModel.recomm.value!!

                        for (pred in hls) {
                            val date = pred[0]

                            val i = dates.indexOf(date)
                            values3.add(Entry(i.toFloat(), it[0][date]!!))
                        }

                        Collections.sort(values3, EntryXComparator())

                        // Create datasets
                        val dataset1 = LineDataSet(values1, "Predicted")
                        val dataset2 = LineDataSet(values2, "Actual")
                        val dataset3 = LineDataSet(values3, "")

                        // Change required properties of datasets
                        dataset1.setDrawCircles(false)
                        dataset1.color = Color.parseColor("#00FF00")

                        pastPredictedData.add(dataset1)

                        dataset2.setDrawCircles(false)
                        dataset2.color = Color.parseColor("#FFA500")

                        pastPredictedData.add(dataset2)

                        dataset3.color = Color.TRANSPARENT
                        dataset3.setDrawValues(false)
                        dataset3.circleRadius = 5f
                        dataset3.circleHoleRadius = 3f
                        dataset3.setCircleColor(Color.parseColor("#0000FF"))
                        pastPredictedData.add(dataset3)

                        // Format labels for xAxis as needed
                        zoomChart.xAxis.valueFormatter =
                            IndexAxisValueFormatter(ArrayList(dates.map { date ->
                                //2022-07-25
                                date.substring(8) + date.substring(4, 8) + date.substring(2, 4)
                            }))
                        // enable scaling and dragging

                        // Add graph data to zoomChart
                        zoomChart.data = LineData(pastPredictedData)
                        zoomChart.onChartGestureListener =
                            Utils.Companion.CustomChartListener(requireContext(), zoomChart, dates)

                        // Updated zoomChart with updated values
                        zoomChart.invalidate()
                    }
                5 ->
                    predictedViewModel.graph.observe(viewLifecycleOwner) {
                        barChart.visibility = View.GONE
                        zoomChart.visibility = View.VISIBLE

                        // Check if data is set as Weekly or Daily
                        val isWeekly = sharedPref.getBoolean("isWeekly", false)

                        // Update data in viewModel accordingly
                        if (isWeekly && predictedViewModel.dailyOrWeekly.value == "Daily") {
                            predictedViewModel.changeSelection("Weekly")
                        } else if (!isWeekly && predictedViewModel.dailyOrWeekly.value == "Weekly") {
                            predictedViewModel.changeSelection("Daily")
                        }

                        zoomChart.clear()
                        predictedData.clear()

                        // Add and sort days in a list
                        val dates = java.util.ArrayList<String>()

                        dates.addAll(it.keys)

                        dates.sortWith { date1, date2 ->
                            val d1 = LocalDate.parse(date1)
                            val d2 = LocalDate.parse(date2)

                            d1.compareTo(d2)
                        }

                        // Set list of predicted/real days to be selected depending on daily/weekly selection
                        val pred_dates = if (predictedViewModel.dailyOrWeekly.value == "Weekly") {
                            dates.subList(dates.size - 12, dates.size)
                        } else {
                            dates.subList(dates.size - 30, dates.size)
                        }

                        val real_dates = if (predictedViewModel.dailyOrWeekly.value == "Weekly") {
                            dates.subList(0, dates.size - 12)
                        } else {
                            dates.subList(0, dates.size - 30)
                        }

                        // values1 -> Actual values
                        // values2 -> Predicted values
                        val values1 = java.util.ArrayList<Entry>()
                        val values2 = java.util.ArrayList<Entry>()
                        val values3 = java.util.ArrayList<Entry>()

                        // Add actual and predicted values to respective lists
                        for (date in real_dates) {
                            val i = dates.indexOf(date)
                            values1.add(Entry(i.toFloat(), it[date]!!))
                        }

                        for (date in pred_dates) {
                            val i = dates.indexOf(date)
                            values2.add(Entry(i.toFloat(), it[date]!!))
                        }

                        val hls = predictedViewModel.data.value!!

                        for (pred in hls) {
                            val date = pred.date

                            Log.e("date", date)
                            Log.e("dates", dates.toString())

                            val i = dates.indexOf(date)
                            if (it[date] != null) {
                                values3.add(Entry(i.toFloat(), it[date]!!))
                            }
                        }

                        Log.d("ccc", values3.toString())
                        Collections.sort(values3, EntryXComparator())

                        // Create dataset
                        val dataset1 = LineDataSet(values1, "Nagpur")
                        val dataset2 = LineDataSet(values2, "Predicted")
                        val dataset3 = LineDataSet(values3, "")

                        // Set required attributes for datasets
                        dataset1.setDrawCircles(false)
                        dataset1.color = Color.parseColor("#FF0000")
                        predictedData.add(dataset1)

                        dataset2.setDrawCircles(false)
                        dataset2.color = Color.parseColor("#0000FF")
                        predictedData.add(dataset2)

                        dataset3.color = Color.TRANSPARENT
                        dataset3.setDrawValues(false)
                        dataset3.circleRadius = 5f
                        dataset3.circleHoleRadius = 3f
                        dataset3.setCircleColor(Color.parseColor("#0000FF"))
                        predictedData.add(dataset3)

                        // Set axis labels on xAxis according to given format
                        zoomChart.xAxis.valueFormatter = IndexAxisValueFormatter(
                            java.util.ArrayList(
                                dates.map { date ->
                                    //2022-07-25
                                    date.substring(8) + date.substring(4, 8) + date.substring(2, 4)
                                })
                        )

                        // Add data to graph
                        zoomChart.data = LineData(predictedData)

                        // Format graph as needed
                        zoomChart.onChartGestureListener =
                            Utils.Companion.CustomChartListener(requireContext(), zoomChart, dates)
                        zoomChart.setVisibleXRangeMaximum(30.0f)
                        zoomChart.moveViewToX((dates.size - 45).toFloat())
                        if (predictedViewModel.dailyOrWeekly.value == "Weekly") {
                            zoomChart.moveViewToX((dates.size - 15).toFloat())
                            zoomChart.setVisibleXRangeMaximum(365.0f)
                        }

                        // Update zoomChart with new updated values
                        zoomChart.invalidate()
                    }
            }
        }
        // Returning binding.root to update the layout with above code
        return binding.root
    }

}