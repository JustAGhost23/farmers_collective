package com.example.farmerscollective.prediction

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Guideline
import androidx.core.content.FileProvider
import androidx.core.text.bold
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.example.farmerscollective.R
import com.example.farmerscollective.databinding.CropPredictedFragmentBinding
import com.example.farmerscollective.utils.FirstDrawListener
import com.example.farmerscollective.utils.Utils
import com.example.farmerscollective.utils.Utils.Companion.ready
import com.example.farmerscollective.utils.Utils.Companion.roundToString
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.EntryXComparator
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.NumberFormat
import java.time.LocalDate
import java.util.*

class CropPredictedFragment : Fragment() {


    private lateinit var viewModel: CropPredictedViewModel
    private lateinit var binding: CropPredictedFragmentBinding
    private lateinit var loadTrace: Trace
    private lateinit var analytics: FirebaseAnalytics

    override fun onAttach(context: Context) {
        super.onAttach(context)
        loadTrace = FirebasePerformance.startTrace("CropPredictedFragment-LoadTime")
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
        binding = CropPredictedFragmentBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this)[CropPredictedViewModel::class.java]
        binding.viewmodel = viewModel
        analytics = FirebaseAnalytics.getInstance(requireContext())

        with(binding) {

            val adap1 = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, resources.getStringArray(
                R.array.dailyOrWeekly
            ))
            adap1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            dailyOrWeeklySelector.adapter = adap1

            dailyOrWeeklySelector.setSelection(adap1.getPosition("Daily"))

            dailyOrWeeklySelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    viewModel.changeSelection(resources.getStringArray(R.array.dailyOrWeekly)[p2])
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    //do nothing
                }
            }

            predictChart.tag = "2"
            predictView2.setOnClickListener {
                it.findNavController().navigateUp()
            }

            val data = ArrayList<ILineDataSet>()

            ready(predictChart)
            predictChart.legend.isEnabled = false

            viewModel.today.observe(viewLifecycleOwner) {
                val todayPrice = SpannableStringBuilder().append(String.format("Price for today is Rs %.1f\n", it)).bold { append("Top 3 days to sell:") }
                binding.textView3.text = todayPrice
            }

            viewModel.data.observe(viewLifecycleOwner) {

                val format = NumberFormat.getPercentInstance()
                format.minimumFractionDigits = 0
                recomm.removeAllViews()

                for (item in it) {
                    val row = LayoutInflater.from(context).inflate(R.layout.recomm, recomm, false)

                    val g = item.confidence

                    val prediction: ConstraintLayout = row.findViewById(R.id.prediction)
                    val date: TextView = row.findViewById(R.id.date)

                    date.text = SpannableStringBuilder()
                        .append(item.date)
                        .append("\n")
                        .bold {
                            append(format.format(g))
                        }

                    val left: Guideline = prediction.findViewById(R.id.left)
                    val right: Guideline = prediction.findViewById(R.id.right)

                    val param1 = left.layoutParams as ConstraintLayout.LayoutParams
                    param1.guidePercent = g / 2
                    left.layoutParams = param1

                    val param2 = right.layoutParams as ConstraintLayout.LayoutParams
                    param2.guidePercent = (1f + g) / 2
                    right.layoutParams = param2

                    val loss = prediction.findViewById<TextView>(R.id.loss_val)
                    val gain = prediction.findViewById<TextView>(R.id.gain_val)

                    loss.text = roundToString(item.loss)
                    gain.text = roundToString(item.gain)
                    loss.setTextColor(Color.DKGRAY)
                    gain.setTextColor(Color.DKGRAY)

                    when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                        Configuration.UI_MODE_NIGHT_NO -> {
                            date.setTextColor(Color.BLACK)
                        }
                        Configuration.UI_MODE_NIGHT_YES -> {
                            date.setTextColor(Color.WHITE)
                        }
                    }

                    recomm.addView(row)

                }


            }

            viewModel.graph.observe(viewLifecycleOwner) {
                predictChart.clear()
                data.clear()

                val dates = ArrayList<String>()

                dates.addAll(it.keys)

                dates.sortWith { date1, date2 ->
                    val d1 = LocalDate.parse(date1)
                    val d2 = LocalDate.parse(date2)

                    d1.compareTo(d2)
                }

                var pred_dates = dates.subList(dates.size - 30, dates.size)
                var real_dates = dates.subList(0, dates.size - 30)

                if(viewModel.dailyOrWeekly.value == "Weekly") {
                    pred_dates = dates.subList(dates.size - 12, dates.size)
                    real_dates = dates.subList(0, dates.size - 12)
                }

                Log.d("k", dates.toString())

                val values1 = ArrayList<Entry>()
                val values2 = ArrayList<Entry>()
                val values3 = ArrayList<Entry>()

                for (date in real_dates) {
                    val i = dates.indexOf(date)
                    values1.add(Entry(i.toFloat(), it[date]!!))
                }

                for (date in pred_dates) {
                    val i = dates.indexOf(date)
                    values2.add(Entry(i.toFloat(), it[date]!!))
                }

                val hls = viewModel.data.value!!

                for(pred in hls) {
                    val date = pred.date

                    val i = dates.indexOf(date)
                    if(it[date] != null) {
                        values3.add(Entry(i.toFloat(), it[date]!!))
                    }
                }

                Collections.sort(values3, EntryXComparator())

                val dataset1 = LineDataSet(values1, "Nagpur")
                val dataset2 = LineDataSet(values2, "Predicted")
                val dataset3 = LineDataSet(values3, "")

                dataset1.setDrawCircles(false)
                dataset1.color = Color.parseColor("#FF0000")
                data.add(dataset1)

                dataset2.setDrawCircles(false)
                dataset2.color = Color.parseColor("#0000FF")
                data.add(dataset2)

                dataset3.color = Color.TRANSPARENT
                dataset3.setDrawValues(false)
                dataset3.circleRadius = 5f
                dataset3.circleHoleRadius = 3f
                dataset3.setCircleColor(Color.parseColor("#0000FF"))
                data.add(dataset3)

                predictChart.xAxis.valueFormatter = IndexAxisValueFormatter(ArrayList(dates.map { date ->
                    //2022-07-25
                    date.substring(8) + date.substring(4, 8) + date.substring(2, 4)
                }))

                predictChart.data = LineData(data)
                predictChart.onChartGestureListener = Utils.Companion.CustomChartListener(requireContext(), predictChart, dates)
                predictChart.setVisibleXRangeMaximum(30.0f)
                predictChart.moveViewToX((dates.size - 45).toFloat())
                if(viewModel.dailyOrWeekly.value == "Weekly") {
                    predictChart.moveViewToX((dates.size - 15).toFloat())
                    predictChart.setVisibleXRangeMaximum(365.0f)
                }

                when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                    Configuration.UI_MODE_NIGHT_NO -> {
                        predictChart.xAxis.textColor = Color.BLACK
                        predictChart.data.setValueTextColor(Color.BLACK)
                        predictChart.axisRight.textColor = Color.BLACK
                        predictChart.axisLeft.textColor = Color.BLACK
                    }
                    Configuration.UI_MODE_NIGHT_YES -> {
                        predictChart.xAxis.textColor = Color.WHITE
                        predictChart.data.setValueTextColor(Color.WHITE)
                        predictChart.axisRight.textColor = Color.WHITE
                        predictChart.axisLeft.textColor = Color.WHITE
                    }
                }

                predictChart.invalidate()
            }

            recommShare.setOnClickListener {
                val icon: Bitmap = predictChart.chartBitmap
                val share = Intent(Intent.ACTION_SEND)
                share.type = "image/png"
                val list = viewModel.data.value!!

                try {
                    val file = File(requireContext().cacheDir, "temp.png")
                    val fOut = FileOutputStream(file)
                    icon.compress(Bitmap.CompressFormat.PNG, 100, fOut)
                    fOut.flush()
                    fOut.close()
                    file.setReadable(true, false)
                    share.putExtra(
                        Intent.EXTRA_STREAM,
                        FileProvider.getUriForFile(
                            requireContext(),
                            requireContext().packageName + ".provider",
                            file
                        )
                    )
                    var str = "Predictions for next 30 days shown.\nTop 3 recommended days are: \n\n"
                    for(pred in list) {
                        str += "${pred.date}: ${pred.confidence * 100}% chance, expected gain Rs. ${pred.gain}\n"
                    }

                    share.putExtra(Intent.EXTRA_TEXT, str)

                    val bundle = Bundle()
                    bundle.putString(
                        FirebaseAnalytics.Param.ITEM_ID,
                        "predict"
                    )
                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "image")
                    analytics.logEvent(FirebaseAnalytics.Event.SHARE, bundle)

                    startActivity(share)
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(
                        requireContext(),
                        "Error occurred, please try later",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        return binding.root
    }

}