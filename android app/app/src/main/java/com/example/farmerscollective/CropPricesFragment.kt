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


class CropPricesFragment : Fragment(), OnChartGestureListener {

    companion object {
        fun newInstance() = CropPricesFragment()
    }

    private lateinit var yearChart: LineChart
    private lateinit var mandiChart: LineChart
    private lateinit var spin: Spinner
    private lateinit var spin2: Spinner
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

        spin = view.findViewById(R.id.spinner)
        spin2 = view.findViewById(R.id.spinner2)

        group = view.findViewById(R.id.chipGroup)
        group2 = view.findViewById(R.id.chipGroup2)

        val mandiColors = mapOf("TELANGANA_ADILABAD_Price" to "#FF0000", "RAJASTHAN_KOTA_Price" to "#00FF00",
            "RAJASTHAN_BHAWANI MANDI_Price" to "#0000FF", "MADHYA PRADESH_SONKATCH_Price" to "#FFFF00",
            "MADHYA PRADESH_RATLAM_Price" to "#00FFFF", "MADHYA PRADESH_MAHIDPUR_Price" to "#FF00FF",
            "MADHYA PRADESH_GANJBASODA_Price" to "#000000")

        val yearColors = mutableMapOf<Int, String>()

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

        val arr = (current - 2)..current

        for(item in arr) {

            val text = "${item}-${(item + 1) % 100}"
            var color = "#000000"

            val replace = color.toCharArray()
            replace[(current - item) * 2 + 1] = 'F'
            replace[(current - item) * 2 + 2] = 'F'
            color = String(replace)

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

        yearColors[-1] = "#000000"

        val text = "2014-present (mean data)"

        val chip = Chip(context)
        chip.text = text
        chip.isCheckable = true

        chip.setOnCheckedChangeListener { button, b ->
            viewModel.selectYear(-1, b)
        }

        group.addView(chip)

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


        yearChart.setDrawGridBackground(false)
        yearChart.description.isEnabled = false
        yearChart.setDrawBorders(false)
//        chart.legend.isEnabled = false
        yearChart.legend.isWordWrapEnabled = true


        yearChart.axisLeft.isEnabled = false
        yearChart.axisRight.setDrawAxisLine(false)
        yearChart.axisRight.setDrawGridLines(false)
        yearChart.xAxis.setDrawAxisLine(true)
        yearChart.xAxis.setDrawGridLines(false)
        yearChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        yearChart.xAxis.granularity = 1f
        yearChart.isDragEnabled = true
        yearChart.setScaleEnabled(true)
        yearChart.isDoubleTapToZoomEnabled = false
        // if disabled, scaling can be done on x- and y-axis separately
        yearChart.setPinchZoom(false)

        mandiChart.setDrawGridBackground(false)
        mandiChart.description.isEnabled = false
        mandiChart.setDrawBorders(false)
//        chart.legend.isEnabled = false
        mandiChart.legend.isWordWrapEnabled = true


        mandiChart.axisLeft.isEnabled = false
        mandiChart.axisRight.setDrawAxisLine(false)
        mandiChart.axisRight.setDrawGridLines(false)
        mandiChart.xAxis.setDrawAxisLine(true)
        mandiChart.xAxis.setDrawGridLines(false)
        mandiChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        mandiChart.xAxis.granularity = 1f
        mandiChart.isDragEnabled = true
        mandiChart.setScaleEnabled(true)
        mandiChart.isDoubleTapToZoomEnabled = false
        // if disabled, scaling can be done on x- and y-axis separately
        mandiChart.setPinchZoom(false)

        viewModel.dataByYear.observe(viewLifecycleOwner, {
            Log.d("observer", "dataByYear")

            yearChart.clear()
            dataByYear.clear()

            val start = LocalDate.of(2001, 7, 1)
            val end = LocalDate.of(2002, 6, 30)

            if(it.isNotEmpty()) {
                val dates = ArrayList<String>()

                Stream.iterate(start, { d ->
                    d.plusDays(1)
                })
                    .limit(start.until(end, ChronoUnit.DAYS))
                    .forEach { date ->
                        dates.add(date.toString().substring(5))
                    }


                for(year in it) {

                    val values1 = ArrayList<Entry>()

                    for(date in dates) {
                        val i = dates.indexOf(date)

                        if(year.value.containsKey(date))
                            values1.add(Entry(i.toFloat(), year.value[date]!!))
                    }

                    val dataset1 = if(year.key == -1) LineDataSet(values1, "Mean values for each day") else LineDataSet(values1, year.key.toString())
                    dataset1.setDrawCircles(false)
                    dataset1.color = Color.parseColor(yearColors[year.key])

                    dataByYear.add(dataset1)

                }



                yearChart.xAxis.valueFormatter = IndexAxisValueFormatter(dates)
                // enable scaling and dragging


                yearChart.data = LineData(dataByYear)
            }

            yearChart.onChartGestureListener = this

            yearChart.invalidate()

        })

        viewModel.dataByMandi.observe(viewLifecycleOwner, {

            mandiChart.clear()
            dataByMandi.clear()

            if(it.isNotEmpty()) {
                val dates = ArrayList<String>()

                val start = if (LocalDate.now().isBefore(LocalDate.of(LocalDate.now().year, 7, 1)))
                    LocalDate.of(LocalDate.now().year - 1, 1, 1)
                else LocalDate.of(LocalDate.now().year, 1, 1)

                val end = LocalDate.now()

                Stream.iterate(start, { d ->
                    d.plusDays(1)
                })
                    .limit(start.until(end, ChronoUnit.DAYS))
                    .forEach { date ->
                        dates.add(date.toString())

                    }

                Log.d("debugging", dates.toString())


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

                    dataByMandi.add(dataset1)

                }

                mandiChart.xAxis.valueFormatter = IndexAxisValueFormatter(dates)
                // enable scaling and dragging


                mandiChart.data = LineData(dataByMandi)
            }

            mandiChart.onChartGestureListener = this

            mandiChart.invalidate()
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