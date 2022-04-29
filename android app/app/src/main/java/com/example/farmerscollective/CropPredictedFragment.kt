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
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.ChartTouchListener
import com.github.mikephil.charting.listener.OnChartGestureListener
import com.github.mikephil.charting.listener.OnChartValueSelectedListener

class CropPredictedFragment : Fragment(), OnChartGestureListener {

    companion object {
        fun newInstance() = CropPredictedFragment()
    }

    private lateinit var viewModel: CropPredictedViewModel
    val args: CropPredictedFragmentArgs by navArgs()
    private lateinit var text: TextView
    private lateinit var icon: ImageView
    private lateinit var chart: LineChart
    private lateinit var dates: ArrayList<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.crop_predicted_fragment, container, false)
        val map = listOf("rice" to R.drawable.rice, "wheat" to R.drawable.wheat,
            "corn" to R.drawable.corn, "jute" to R.drawable.jute)

        viewModel = ViewModelProvider(this)[CropPredictedViewModel::class.java]
        val data = ArrayList<ILineDataSet>()
        val values1 = ArrayList<Entry>()
        dates = ArrayList()
        val prices = ArrayList<Float>()

        text = view.findViewById(R.id.crop_predict)
        icon = view.findViewById(R.id.crop_icon)
        chart = view.findViewById(R.id.chart)

        text.text = args.crop.capitalize()
        map.forEach {
            if(it.first == args.crop) icon.setImageResource(it.second)
        }

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

        return view
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