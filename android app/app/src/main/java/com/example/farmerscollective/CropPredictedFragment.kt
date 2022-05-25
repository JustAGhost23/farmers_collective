package com.example.farmerscollective

import android.graphics.Color
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet

class CropPredictedFragment : Fragment() {


    private lateinit var viewModel: CropPredictedViewModel
    private lateinit var chart: LineChart
    private lateinit var recomm: ListView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.crop_predicted_fragment, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        viewModel = ViewModelProvider(this).get(CropPredictedViewModel::class.java)
        chart = view.findViewById(R.id.predict_chart)
        recomm = view.findViewById(R.id.recomm)

        val data = ArrayList<ILineDataSet>()
        val values1 = ArrayList<Entry>()



        chart.setDrawGridBackground(false)
        chart.description.isEnabled = false
        chart.setDrawBorders(false)

        chart.axisRight.isEnabled = false
        chart.axisLeft.setDrawAxisLine(false)
        chart.axisLeft.setDrawGridLines(false)
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


            val len = it.size
            for(i in 0 until len) {
                values1.add(Entry(i.toFloat(), it[i].gain))
            }

            Log.v("Predict", it.toString())

            val dataset1 = LineDataSet(values1, "realtime")
            dataset1.setDrawCircles(false)
            dataset1.color = Color.rgb(255, 0, 0)


            data.add(dataset1)

            chart.xAxis.valueFormatter = object : ValueFormatter() {
                override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                    return it[value.toInt()].date
                }


            }
            // enable scaling and dragging


            chart.data = LineData(data)
            chart.invalidate()

            recomm.adapter = CustomAdapter(ArrayList(it.reversed().subList(1, 4)), context!!)

        })

    }

}