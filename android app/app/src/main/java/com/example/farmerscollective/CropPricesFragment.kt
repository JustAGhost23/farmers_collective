package com.example.farmerscollective

import android.graphics.Color
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
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
import java.time.Month
import java.time.temporal.ChronoUnit
import java.util.stream.Collectors
import java.util.stream.Stream

import com.google.android.material.chip.ChipDrawable




class CropPricesFragment : Fragment(), OnChartGestureListener {

    companion object {
        fun newInstance() = CropPricesFragment()
    }

    private lateinit var chart: LineChart
    private lateinit var spin2: Spinner
    private lateinit var group: ChipGroup
    private val viewModel by viewModels<CropPricesViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.crop_prices_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val data = ArrayList<ILineDataSet>()
        val start = LocalDate.of(2022, 1, 1)
        val colors = mapOf("TELANGANA_ADILABAD_Price" to "#FF0000", "RAJASTHAN_KOTA_Price" to "#00FF00",
        "RAJASTHAN_BHAWANI MANDI_Price" to "#0000FF", "MADHYA PRADESH_SONKATCH_Price" to "#FFFF00",
        "MADHYA PRADESH_RATLAM_Price" to "#00FFFF", "MADHYA PRADESH_MAHIDPUR_Price" to "#FF00FF",
        "MADHYA PRADESH_GANJBASODA_Price" to "#000000")


        chart = view.findViewById(R.id.chart)
        spin2 = view.findViewById(R.id.spinner2)
        group = view.findViewById(R.id.chipGroup)

        val adap2 = ArrayAdapter(context!!, android.R.layout.simple_spinner_item, resources.getStringArray(R.array.commodity))
        adap2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spin2.adapter = adap2

        val arr = resources.getStringArray(R.array.mandi)


        for(item in arr) {

            if(!item.equals("TELANGANA_ADILABAD_Price")) {
                val res = Regex("_.+_").find(item)
                val text = res!!.value.substring(1, res.value.length - 1)

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
                chip.setOnCheckedChangeListener { button, b ->
                    viewModel.makeSelection(item, b)
                }

                group.addView(chip)
            }

        }



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
        // if disabled, scaling can be done on x- and y-axis separately
        chart.setPinchZoom(true)

        viewModel.data.observe(viewLifecycleOwner, {

            chart.clear()
            data.clear()

            val dates = ArrayList<String>()
            val end = LocalDate.parse(it["TELANGANA_ADILABAD_Price"]!!.keys.last())

            Stream.iterate(start, { d ->
                d.plusDays(1)
            })
                .limit(start.until(end, ChronoUnit.DAYS))
                .forEach { date ->
                    dates.add(date.toString())
                }


            for(mandi in it) {

                val values1 = ArrayList<Entry>()

                for(date in mandi.value.keys) {
                    val i = dates.indexOf(date)
                    values1.add(Entry(i.toFloat(), mandi.value[date]!!))
                }

                val dataset1 = LineDataSet(values1, mandi.key)
                dataset1.setDrawCircles(false)
                dataset1.color = Color.parseColor(colors[mandi.key])

                data.add(dataset1)

            }



            chart.xAxis.valueFormatter = IndexAxisValueFormatter(dates)
            // enable scaling and dragging


            chart.data = LineData(data)
            chart.onChartGestureListener = this

            chart.invalidate()

        })
    }

    override fun onChartGestureStart(
        me: MotionEvent?,
        lastPerformedGesture: ChartTouchListener.ChartGesture?
    ) {
        Log.v("LongPress", "Start")
    }

    override fun onChartGestureEnd(
        me: MotionEvent?,
             lastPerformedGesture: ChartTouchListener.ChartGesture?
    ) {
        Log.v("LongPress", "End")
    }

    override fun onChartLongPressed(me: MotionEvent?) {
        //pass
    }

    override fun onChartDoubleTapped(me: MotionEvent?) {
        //why you always
//        Log.v("LongPress", "Double tap")
//        val x = chart.getHighlightByTouchPoint(me!!.x, me.y).x.toInt()
//        if(x < dates.size) {
//            val y = chart.getHighlightByTouchPoint(me.x, me.y).y
//            val date = dates[x]
//            Toast.makeText(activity, "$date $y", Toast.LENGTH_SHORT).show()
//        }
    }

    override fun onChartSingleTapped(me: MotionEvent?) {
        //in the mood
    }

    override fun onChartFling(
        me1: MotionEvent?,
        me2: MotionEvent?,
        velocityX: Float,
        velocityY: Float
    ) {
        //flippin out
    }

    override fun onChartScale(me: MotionEvent?, scaleX: Float, scaleY: Float) {
        //actin brand new
    }

    override fun onChartTranslate(me: MotionEvent?, dX: Float, dY: Float) {
        //i aint tryna tell you what to do
    }
}