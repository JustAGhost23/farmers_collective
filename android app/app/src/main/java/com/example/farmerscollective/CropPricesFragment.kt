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
import android.widget.Toast
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

class CropPricesFragment : Fragment(), OnChartGestureListener {

    companion object {
        fun newInstance() = CropPricesFragment()
    }

    private lateinit var chart: LineChart
    private lateinit var dates: ArrayList<String>
    private val viewModel by viewModels<CropPricesViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.crop_prices_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val data = ArrayList<ILineDataSet>()
        val values1 = ArrayList<Entry>()
        dates = ArrayList()
        val prices = ArrayList<Float>()

        chart = view.findViewById(R.id.chart)


        chart.setDrawGridBackground(false)
        chart.description.isEnabled = false
        chart.setDrawBorders(false)

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
        chart.setPinchZoom(false)

        viewModel.data.observe(viewLifecycleOwner, {
            dates.addAll(it.keys)
            prices.addAll(it.values)

            val len = dates.size
            for(i in 1..len) {
                values1.add(Entry(i.toFloat(), prices[i - 1]))
            }

            val dataset1 = LineDataSet(values1, "realtime")
            dataset1.setDrawCircles(false)
            dataset1.color = Color.rgb(255, 0, 0)

//        val dataset2 = LineDataSet(values2, "realtime")
//        dataset2.setDrawCircles(false)
//        dataset2.color = Color.rgb(0, 0, 255)

            data.add(dataset1)
//        data.add(dataset2)


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
        Log.v("LongPress", "Double tap")
        val x = chart.getHighlightByTouchPoint(me!!.x, me.y).x.toInt()
        if(x < dates.size) {
            val y = chart.getHighlightByTouchPoint(me.x, me.y).y
            val date = dates[x]
            Toast.makeText(activity, "$date $y", Toast.LENGTH_SHORT).show()
        }
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