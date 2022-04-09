package com.example.farmerscollective

import android.graphics.Color
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.fragment.navArgs
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

class CropPredictedFragment : Fragment() {

    companion object {
        fun newInstance() = CropPredictedFragment()
    }

    private lateinit var viewModel: CropPredictedViewModel
    val args: CropPredictedFragmentArgs by navArgs()
    private lateinit var text: TextView
    private lateinit var icon: ImageView
    private lateinit var chart: LineChart

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.crop_predicted_fragment, container, false)
        val map = listOf("rice" to R.drawable.rice, "wheat" to R.drawable.wheat,
            "corn" to R.drawable.corn, "jute" to R.drawable.jute)

        viewModel = ViewModelProvider(this)[CropPredictedViewModel::class.java]

        text = view.findViewById(R.id.crop_predict)
        icon = view.findViewById(R.id.crop_icon)
        chart = view.findViewById(R.id.chart)

        text.text = args.crop.capitalize()
        map.forEach {
            if(it.first == args.crop) icon.setImageResource(it.second)
        }

        val data = ArrayList<ILineDataSet>()
        val values1 = ArrayList<Entry>()
        val values2 = ArrayList<Entry>()

        val csv = activity!!.assets.open("RAJASTHAN_KOTA_Price.csv")
        val dates = ArrayList<String>()
        val prices = ArrayList<Float>()

        csvReader().open(csv) {
            readAllAsSequence().forEach { line ->
                if(line[0] != "DATE" && line[1].isNotEmpty()) {
                    dates.add(line[0])
                    prices.add(line[1].toFloat())
                }
            }
        }

        val len = dates.size
        for(i in len-30..len) {
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
        chart.xAxis.valueFormatter = IndexAxisValueFormatter(dates)
        // enable scaling and dragging
        chart.isDragEnabled = true
        chart.setScaleEnabled(true)

        // if disabled, scaling can be done on x- and y-axis separately
        chart.setPinchZoom(false)

        chart.data = LineData(data)
        chart.invalidate()


        return view
    }

}