package com.example.farmerscollective.prediction

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
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
import androidx.core.content.FileProvider
import androidx.core.text.bold
import androidx.core.view.marginLeft
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.farmerscollective.utils.Utils.Companion.ready
import com.example.farmerscollective.utils.DatePickerFragment
import com.example.farmerscollective.R
import com.example.farmerscollective.databinding.CropPastPredictedFragmentBinding
import com.example.farmerscollective.databinding.PastRecommBinding
import com.example.farmerscollective.realtime.IntPriceFragmentDirections
import com.example.farmerscollective.utils.FirstDrawListener
import com.example.farmerscollective.utils.Utils
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
import kotlin.collections.ArrayList

// Crop Past Predicted Fragment
class CropPastPredictedFragment : Fragment() {

    // Late initialized variables
    private lateinit var viewModel: CropPastPredictedViewModel
    private var dialog: DialogFragment? = null
    private lateinit var loadTrace: Trace

    override fun onAttach(context: Context) {
        super.onAttach(context)
        loadTrace = FirebasePerformance.startTrace("CropPastPredictedFragment-LoadTime")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Register how long it takes for the fragment UI to load up
        FirstDrawListener.registerFirstDrawListener(
            view,
            object : FirstDrawListener.OnFirstDrawCallback {
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
        // Use viewbinding to bind the Crop Past Predicted Fragment layout(crop_past_predicted_fragment.xml) to Crop Past Predicted Fragment.
        val binding = DataBindingUtil.inflate<CropPastPredictedFragmentBinding>(
            inflater,
            R.layout.crop_past_predicted_fragment,
            container,
            false
        )
        // Obtain viewModel instance and attach it to viewbinding
        viewModel = ViewModelProvider(this)[CropPastPredictedViewModel::class.java]

        binding.viewmodel = viewModel
        binding.frag = this
        binding.lifecycleOwner = this

        val scale = resources.displayMetrics.density

        // Setting functions for UI components using viewbinding
        with(binding) {
            pastPredictChart.tag = "3"

            // Setting onClick listener to navigate back up to previous fragment
            pastPredictView2.setOnClickListener {
                it.findNavController().navigateUp()
            }

            // Setting onClick listener to navigate to ZoomedInFragment
            pastRecommZoom.setOnClickListener {
                val action =
                    CropPastPredictedFragmentDirections.actionCropPastPredictedFragmentToZoomedInFragment(
                        4
                    )
                findNavController().navigate(action)
            }

            // Observing list of recommendations from viewModel
            viewModel.recomm.observe(viewLifecycleOwner) {

                Log.v("ok", "amhere")

                pastRecomm.removeAllViews()
                val format = NumberFormat.getPercentInstance()
                format.minimumFractionDigits = 0

                for (item in it) {
                    // Use viewbinding to bind row to Past Recommendation layout(past_recomm.xml)
                    val row = DataBindingUtil.inflate<PastRecommBinding>(
                        layoutInflater,
                        R.layout.past_recomm,
                        pastRecomm,
                        false
                    )

                    with(row) {

                        // l -> loss
                        // g -> gain
                        val l = item[1].toFloat()
                        val g = item[2].toFloat()
                        val profit = item[5]

                        // Date
                        date.text = SpannableStringBuilder()
                            .append(item[0])
                            .append("\n")
                            .bold {
                                append(format.format(g))
                            }
                        date.setPadding((32 * scale + 0.5f).toInt(), 0, 0, 0)

                        // Guidelines for UI
                        val left: Guideline = prediction.findViewById(R.id.left)
                        val right: Guideline = prediction.findViewById(R.id.right)

                        // Creating the row layout and adding it programmatically for each row
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
                        loss.setTextColor(Color.DKGRAY)
                        gain.setTextColor(Color.DKGRAY)

                        actual.text = profit

                        // Set text color for profit
                        if (profit != "N/A") {
                            actual.text = roundToString(item[5].toFloat())

                            if (profit.toFloat() > 0) actual.setTextColor(Color.parseColor("#00DD00"))
                            else actual.setTextColor(Color.parseColor("#DD0000"))
                        }

                        // Dark Mode configuration for text
                        when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                            Configuration.UI_MODE_NIGHT_NO -> {
                                date.setTextColor(Color.BLACK)
                            }
                            Configuration.UI_MODE_NIGHT_YES -> {
                                date.setTextColor(Color.WHITE)
                            }
                        }
                    }
                    // Add view to Past Recommendations binding
                    pastRecomm.addView(row.root)
                }
            }

            val data = ArrayList<ILineDataSet>()

            // Util function
            ready(pastPredictChart)
            pastPredictChart.legend.isEnabled = false

            // Observing list of prices needed for past prediction graph from viewModel
            viewModel.graph.observe(viewLifecycleOwner) {

                pastPredictChart.clear()
                data.clear()

                // Add and sort dates to list
                val dates = ArrayList<String>()

                dates.addAll(it[0].keys)

                dates.sortWith { date1, date2 ->
                    val d1 = LocalDate.parse(date1)
                    val d2 = LocalDate.parse(date2)

                    d1.compareTo(d2)
                }

                Log.d("dates", dates.toString())

                // values1 -> Predicted prices
                // values2 -> Actual prices
                val values1 = ArrayList<Entry>()
                val values2 = ArrayList<Entry>()
                val values3 = ArrayList<Entry>()

                // Add prices to values1 and values2
                for (date in dates) {
                    val i = dates.indexOf(date)
                    if (it[0].containsKey(date)) values1.add(Entry(i.toFloat(), it[0][date]!!))
                    if (it[1].containsKey(date)) values2.add(Entry(i.toFloat(), it[1][date]!!))
                }

                val hls = viewModel.recomm.value!!

                for (pred in hls) {
                    val date = pred[0]

                    val i = dates.indexOf(date)
                    values3.add(Entry(i.toFloat(), it[0][date]!!))
                }

                Collections.sort(values3, EntryXComparator())

                // Create datasets for graph
                val dataset1 = LineDataSet(values1, "Predicted")
                val dataset2 = LineDataSet(values2, "Actual")
                val dataset3 = LineDataSet(values3, "")

                // Adding color and other UI formatting settings to datasets
                dataset1.setDrawCircles(false)
                dataset1.color = Color.parseColor("#00FF00")

                data.add(dataset1)

                dataset2.setDrawCircles(false)
                dataset2.color = Color.parseColor("#FFA500")

                data.add(dataset2)

                dataset3.color = Color.TRANSPARENT
                dataset3.setDrawValues(false)
                dataset3.circleRadius = 5f
                dataset3.circleHoleRadius = 3f
                dataset3.setCircleColor(Color.parseColor("#0000FF"))
                data.add(dataset3)

                // Setting format for dates on xAxis
                pastPredictChart.xAxis.valueFormatter =
                    IndexAxisValueFormatter(ArrayList(dates.map { date ->
                        //2022-07-25
                        date.substring(8) + date.substring(4, 8) + date.substring(2, 4)
                    }))
                // enable scaling and dragging

                // Set data for pastPredictChart
                pastPredictChart.data = LineData(data)
                pastPredictChart.onChartGestureListener =
                    Utils.Companion.CustomChartListener(requireContext(), pastPredictChart, dates)

                // Dark Mode configuration for text
                when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                    Configuration.UI_MODE_NIGHT_NO -> {
                        pastPredictChart.xAxis.textColor = Color.BLACK
                        pastPredictChart.data.setValueTextColor(Color.BLACK)
                        pastPredictChart.axisRight.textColor = Color.BLACK
                        pastPredictChart.axisLeft.textColor = Color.BLACK
                    }
                    Configuration.UI_MODE_NIGHT_YES -> {
                        pastPredictChart.xAxis.textColor = Color.WHITE
                        pastPredictChart.data.setValueTextColor(Color.WHITE)
                        pastPredictChart.axisRight.textColor = Color.WHITE
                        pastPredictChart.axisLeft.textColor = Color.WHITE
                    }
                }

                // Update pastPredictChart with changes made above
                pastPredictChart.invalidate()

            }

            // Setting onClick listener to share a picture of the graph
            pastRecommShare.setOnClickListener {
                // Firebase analytics
                val analytics = FirebaseAnalytics.getInstance(requireContext())
                val icon: Bitmap = pastPredictChart.chartBitmap
                val share = Intent(Intent.ACTION_SEND)
                share.type = "image/png"
                val list = viewModel.recomm.value!!

                // Create bitmap and push to file
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
                    var str = "Predicted dates for ${viewModel.date.value!!} were: \n\n"
                    for (pred in list) {
                        str += "${pred[0]}: Actual profit if sold Rs. ${pred[5]}\n"
                    }

                    // Create intent containing image to be shared
                    share.putExtra(Intent.EXTRA_TEXT, str)

                    // Log event on Firebase Analytics
                    val bundle = Bundle()
                    bundle.putString(
                        FirebaseAnalytics.Param.ITEM_ID,
                        "predict ${viewModel.date.value!!}"
                    )
                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "image")
                    analytics.logEvent(FirebaseAnalytics.Event.SHARE, bundle)

                    // Share intent
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

        // Returning binding.root to update the layout with above code
        return binding.root
    }

    // Function to obtain a Date Picker Dialog
    fun showDatePickerDialog() {
        if (dialog == null) dialog = DatePickerFragment(viewModel)

        dialog!!.show(parentFragmentManager, "datePicker")
    }

}