package com.example.farmerscollective

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.viewModels
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.listener.ChartTouchListener
import com.github.mikephil.charting.listener.OnChartGestureListener
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.stream.Stream

import kotlin.random.Random


class CropPricesFragment : Fragment() {

    companion object {
        fun newInstance() = CropPricesFragment()
    }

    private lateinit var yearChart: LineChart
    private lateinit var mandiChart: LineChart
    private lateinit var spin: Spinner
    private lateinit var spin2: Spinner
    private lateinit var spin3: Spinner
    private lateinit var group: ChipGroup
    private lateinit var group2: ChipGroup
    private val viewModel by viewModels<CropPricesViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.crop_prices_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dataByYear = ArrayList<ILineDataSet>()
        val dataByMandi = ArrayList<ILineDataSet>()

        yearChart = view.findViewById(R.id.chart)
        mandiChart = view.findViewById(R.id.chart2)

        spin = view.findViewById(R.id.mandiSelector)
        spin2 = view.findViewById(R.id.commSelector)
        spin3 = view.findViewById(R.id.yearSelector)

        group = view.findViewById(R.id.chipGroup)
        group2 = view.findViewById(R.id.chipGroup2)

        val mandiColors = mapOf("TELANGANA_ADILABAD_Price" to "#FF0000", "RAJASTHAN_KOTA_Price" to "#00FF00",
            "RAJASTHAN_BHAWANI MANDI_Price" to "#0000FF", "MADHYA PRADESH_SONKATCH_Price" to "#FFFF00",
            "MADHYA PRADESH_RATLAM_Price" to "#00FFFF", "MADHYA PRADESH_MAHIDPUR_Price" to "#FF00FF",
            "MADHYA PRADESH_GANJBASODA_Price" to "#000000")

        val yearColors = mutableMapOf<Int, Int>()

        val adap1 = ArrayAdapter(context!!, android.R.layout.simple_spinner_item, resources.getStringArray(R.array.mandi))
        adap1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spin.adapter = adap1

        spin.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                viewModel.changeMandi(resources.getStringArray(R.array.mandi)[p2])
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                //do nothing
            }
        }
        val adap2 = ArrayAdapter(context!!, android.R.layout.simple_spinner_item, resources.getStringArray(R.array.commodity))
        adap2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spin2.adapter = adap2

        val current = if (LocalDate.now().isBefore(LocalDate.of(LocalDate.now().year, 6, 30)))
            LocalDate.now().year - 1
        else LocalDate.now().year

        val arr = 2014..current

        val adap3 = ArrayAdapter(context!!, android.R.layout.simple_spinner_item, arr.toList())
        adap3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spin3.adapter = adap3
        spin3.setSelection(adap3.getPosition(current))

        spin3.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                viewModel.changeYear(arr.toList()[p2])
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                //do nothing
            }
        }

        for(item in arr) {

            val text = "${item}-${(item + 1) % 100}"
            val color = Color.rgb(Random.nextInt(200), Random.nextInt(200), Random.nextInt(200))
            yearColors[item] = color

            val chip = Chip(context)
            chip.text = text
            chip.isCheckable = true
//                val chipDrawable = ChipDrawable.createFromAttributes(
//                    context!!,
//                    null,
//                    0,
//                    R.style.Widget_Material3_Chip_Filter
//                )
//                chip.setChipDrawable(chipDrawable)
            if(item == current) chip.isChecked = true

            chip.setOnCheckedChangeListener { button, b ->
                viewModel.selectYear(item, b)
            }

            group.addView(chip)

        }

        val mandis = resources.getStringArray(R.array.mandi)

        for(mandi in mandis) {

            val mChip = Chip(context)
            mChip.text = mandi
            mChip.isCheckable = true
//                val chipDrawable = ChipDrawable.createFromAttributes(
//                    context!!,
//                    null,
//                    0,
//                    R.style.Widget_Material3_Chip_Filter
//                )
//                chip.setChipDrawable(chipDrawable)
            if(mandi == "TELANGANA_ADILABAD_Price") mChip.isChecked = true

            mChip.setOnCheckedChangeListener { button, b ->
                viewModel.selectMandi(mandi, b)
            }

            group2.addView(mChip)
        }


        ready(yearChart)
        ready(mandiChart)

        val dates = ArrayList<String>()

        val start = LocalDate.of(2001, 7, 1)
        val end = LocalDate.of(2002, 6, 30)

        Stream.iterate(start, { d ->
            d.plusDays(1)
        })
            .limit(start.until(end, ChronoUnit.DAYS))
            .forEach { date ->
                val dt = date.toString()
                dates.add(dt.substring(8) + dt.substring(4, 7))
            }


        viewModel.dataByYear.observe(viewLifecycleOwner, {
            Log.d("observer", "dataByYear")

            yearChart.clear()
            dataByYear.clear()

            if(it.isNotEmpty()) {

                for(year in it) {

                    val values1 = ArrayList<Entry>()

                    for(date in dates) {
                        val i = dates.indexOf(date)

                        if(year.value.containsKey(date) && !year.value[date]!!.equals(0f))
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

            yearChart.onChartGestureListener = CustomChartListener(context!!, yearChart, dates)
            yearChart.setVisibleXRangeMaximum(10.0f)

            yearChart.invalidate()

        })

        viewModel.dataByMandi.observe(viewLifecycleOwner, {

            mandiChart.clear()
            dataByMandi.clear()

            if(it.isNotEmpty()) {

                for(mandi in it) {

                    val values1 = ArrayList<Entry>()

                    for(date in dates) {
                        val i = dates.indexOf(date)

                        if(mandi.value.containsKey(date))
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

            mandiChart.onChartGestureListener = CustomChartListener(context!!, mandiChart, dates)
            mandiChart.setVisibleXRangeMaximum(10.0f)

            mandiChart.invalidate()
        })
    }
    
    private fun ready(chart: LineChart) {
        chart.setDrawGridBackground(false)
        chart.description.isEnabled = false
        chart.setDrawBorders(false)
//        chart.legend.isEnabled = false
        chart.legend.isWordWrapEnabled = true
        chart.axisLeft.isEnabled = false
        chart.axisRight.setDrawAxisLine(false)
        chart.axisRight.setDrawGridLines(false)
        chart.xAxis.setDrawAxisLine(true)
        chart.xAxis.setDrawGridLines(false)
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chart.xAxis.granularity = 1f
        chart.isDragEnabled = true
        chart.setScaleEnabled(true)
        chart.isDoubleTapToZoomEnabled = false
        chart.setPinchZoom(false)
    }

}