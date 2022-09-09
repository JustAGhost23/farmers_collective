package com.example.farmerscollective.prediction

import android.graphics.Color
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Guideline
import androidx.core.text.bold
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.example.farmerscollective.utils.Utils.Companion.ready
import com.example.farmerscollective.utils.DatePickerFragment
import com.example.farmerscollective.R
import com.example.farmerscollective.databinding.CropPastPredictedFragmentBinding
import com.example.farmerscollective.databinding.PastRecommBinding
import com.example.farmerscollective.utils.Utils
import com.example.farmerscollective.utils.Utils.Companion.roundToString
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.EntryXComparator
import java.text.NumberFormat
import java.time.LocalDate
import java.util.*
import kotlin.collections.ArrayList


class CropPastPredictedFragment : Fragment() {

    private lateinit var viewModel: CropPastPredictedViewModel
    private var dialog: DialogFragment? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val binding = DataBindingUtil.inflate<CropPastPredictedFragmentBinding>(inflater, R.layout.crop_past_predicted_fragment, container, false)
        viewModel = ViewModelProvider(this)[CropPastPredictedViewModel::class.java]

        binding.viewmodel = viewModel
        binding.frag = this
        binding.lifecycleOwner = this

        with (binding) {

            viewModel.recomm.observe(viewLifecycleOwner) {

                Log.v("ok", "amhere")

                pastRecomm.removeAllViews()
                val format = NumberFormat.getPercentInstance()
                format.minimumFractionDigits = 0

                for (item in it) {
                    val row = DataBindingUtil.inflate<PastRecommBinding>(
                        layoutInflater,
                        R.layout.past_recomm,
                        pastRecomm,
                        false
                    )

                    with(row) {

                        val l = item[1].toFloat()
                        val g = item[2].toFloat()
                        val profit = item[5]

                        date.text = SpannableStringBuilder()
                            .append(item[0])
                            .append("\n")
                            .bold {
                                append(format.format(g))
                            }

                        val left: Guideline = prediction.findViewById(R.id.left)
                        val right: Guideline = prediction.findViewById(R.id.right)

                        val param1 = left.layoutParams as ConstraintLayout.LayoutParams
                        param1.guidePercent = (1f - l) / 2
                        left.layoutParams = param1

                        val param2 = right.layoutParams as ConstraintLayout.LayoutParams
                        param2.guidePercent = (1f + g) / 2
                        right.layoutParams = param2

                        val loss = prediction.findViewById<TextView>(R.id.loss_val)
                        val gain = prediction.findViewById<TextView>(R.id.gain_val)

                        loss.text = roundToString(item[3].toFloat())
                        gain.text = roundToString(item[4].toFloat())

                        actual.text = profit

                        if (profit != "N/A") {
                            actual.text = roundToString(item[5].toFloat())

                            if (profit.toFloat() > 0) actual.setTextColor(Color.parseColor("#00DD00"))
                            else actual.setTextColor(Color.parseColor("#DD0000"))
                        }
                    }


                    pastRecomm.addView(row.root)

                }


            }

            val data = ArrayList<ILineDataSet>()

            ready(pastPredictChart)
            pastPredictChart.legend.isEnabled = false

            viewModel.graph.observe(viewLifecycleOwner) {

                Log.v("ok", "amhere")

                pastPredictChart.clear()
                data.clear()

                val dates = ArrayList<String>()

                dates.addAll(it[0].keys)

                dates.sortWith { date1, date2 ->
                    val d1 = LocalDate.parse(date1)
                    val d2 = LocalDate.parse(date2)

                    d1.compareTo(d2)
                }

                Log.d("k", dates.toString())

                val values1 = ArrayList<Entry>()
                val values2 = ArrayList<Entry>()
                val values3 = ArrayList<Entry>()

                for (date in dates) {
                    val i = dates.indexOf(date)
                    if (it[0].containsKey(date)) values1.add(Entry(i.toFloat(), it[0][date]!!))
                    if (it[1].containsKey(date)) values2.add(Entry(i.toFloat(), it[1][date]!!))
                }

                val hls = viewModel.recomm.value!!

                for(pred in hls) {
                    val date = pred[0]

                    val i = dates.indexOf(date)
                    values3.add(Entry(i.toFloat(), it[0][date]!!))
                }

                Collections.sort(values3, EntryXComparator())

                val dataset1 = LineDataSet(values1, "Predicted")
                val dataset2 = LineDataSet(values2, "Actual")
                val dataset3 = LineDataSet(values3, "")

                dataset1.setDrawCircles(false)
                dataset1.color = Color.parseColor("#0000FF")

                data.add(dataset1)

                dataset2.setDrawCircles(false)
                dataset2.color = Color.parseColor("#FF0000")

                data.add(dataset2)

                dataset3.color = Color.TRANSPARENT
                dataset3.setDrawValues(false)
                dataset3.circleRadius = 5f
                dataset3.circleHoleRadius = 3f
                dataset3.setCircleColor(Color.parseColor("#0000FF"))
                data.add(dataset3)

                pastPredictChart.xAxis.valueFormatter =
                    IndexAxisValueFormatter(ArrayList(dates.map { date ->
                        //2022-07-25
                        date.substring(8) + date.substring(4, 8) + date.substring(2, 4)
                    }))
                // enable scaling and dragging

                pastPredictChart.data = LineData(data)
                pastPredictChart.onChartGestureListener = Utils.Companion.CustomChartListener(requireContext(), pastPredictChart, dates)
                pastPredictChart.invalidate()

            }
        }

        return binding.root
    }

    fun showDatePickerDialog() {
        if(dialog == null) dialog = DatePickerFragment(viewModel)

        dialog!!.show(parentFragmentManager, "datePicker")
    }

}