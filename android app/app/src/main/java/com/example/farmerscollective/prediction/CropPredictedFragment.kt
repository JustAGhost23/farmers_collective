package com.example.farmerscollective.prediction

import android.graphics.Color
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Guideline
import androidx.core.text.bold
import com.example.farmerscollective.utils.Utils.Companion.ready
import com.example.farmerscollective.R
import com.example.farmerscollective.databinding.CropPredictedFragmentBinding
import com.example.farmerscollective.utils.Utils
import com.example.farmerscollective.utils.Utils.Companion.roundToString
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import java.time.LocalDate
import kotlin.math.round
import kotlin.math.roundToInt

class CropPredictedFragment : Fragment() {


    private lateinit var viewModel: CropPredictedViewModel
    private lateinit var recomm: LinearLayout
    private lateinit var binding: CropPredictedFragmentBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = CropPredictedFragmentBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this)[CropPredictedViewModel::class.java]
        binding.viewmodel = viewModel

        with(binding) {

            val data = ArrayList<ILineDataSet>()

            ready(predictChart)
            predictChart.legend.isEnabled = false

            viewModel.data.observe(viewLifecycleOwner) {

//            recomm.adapter = CustomAdapter(ArrayList(it.reversed().subList(1, 4)), context!!)

                val list = it.reversed().subList(1, 4)

                for (item in list) {
                    val row = LayoutInflater.from(context).inflate(R.layout.recomm, recomm, false)

                    val g = item.confidence

                    val prediction: ConstraintLayout = row.findViewById(R.id.prediction)
                    val date: TextView = row.findViewById(R.id.date)

                    date.text = SpannableStringBuilder()
                        .append(item.date)
                        .append("\n")
                        .bold {
                            append(roundToString(g, 2))
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

                val pred_dates = dates.subList(dates.size - 30, dates.size)
                val real_dates = dates.subList(0, dates.size - 30)

                Log.d("k", dates.toString())

                val values1 = ArrayList<Entry>()
                val values2 = ArrayList<Entry>()

                for (date in real_dates) {
                    val i = dates.indexOf(date)
                    values1.add(Entry(i.toFloat(), it[date]!!))
                }

                for (date in pred_dates) {
                    val i = dates.indexOf(date)
                    values2.add(Entry(i.toFloat(), it[date]!!))
                }

                val dataset1 = LineDataSet(values1, "Nagpur")
                val dataset2 = LineDataSet(values2, "Predicted")

                dataset1.setDrawCircles(false)
                dataset1.color = Color.parseColor("#FF0000")

                data.add(dataset1)

                dataset2.setDrawCircles(false)
                dataset2.color = Color.parseColor("#0000FF")

                data.add(dataset2)

                predictChart.xAxis.valueFormatter = IndexAxisValueFormatter(ArrayList(dates.map { date ->
                    //2022-07-25
                    date.substring(8) + date.substring(4, 8) + date.substring(2, 4)
                }))
                // enable scaling and dragging

                predictChart.data = LineData(data)
                predictChart.onChartGestureListener = Utils.Companion.CustomChartListener(requireContext(), predictChart, dates)
                predictChart.setVisibleXRangeMaximum(30.0f)
                predictChart.moveViewToX((dates.size - 45).toFloat())


                predictChart.invalidate()
            }
        }

        return binding.root
    }

}