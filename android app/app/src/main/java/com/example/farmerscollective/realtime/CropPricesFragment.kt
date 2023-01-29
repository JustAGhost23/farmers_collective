package com.example.farmerscollective.realtime

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.graphics.drawable.ShapeDrawable
import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.allViews
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.anychart.data.Table
import com.example.farmerscollective.R
import com.example.farmerscollective.databinding.CropPricesFragmentBinding
import com.example.farmerscollective.utils.FirstDrawListener
import com.example.farmerscollective.utils.Utils
import com.example.farmerscollective.utils.Utils.Companion.dates
import com.example.farmerscollective.utils.Utils.Companion.ready
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.chip.Chip
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.time.LocalDate
import java.util.stream.Stream


class CropPricesFragment : Fragment() {

    private val viewModel by activityViewModels<CropPricesViewModel>()
    private lateinit var binding: CropPricesFragmentBinding
    private lateinit var loadTrace: Trace
    private lateinit var analytics: FirebaseAnalytics

    override fun onAttach(context: Context) {
        super.onAttach(context)
        loadTrace = FirebasePerformance.startTrace("CropPricesFragment-LoadTime")
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
        binding = DataBindingUtil.inflate(inflater, R.layout.crop_prices_fragment, container, false)
        analytics = FirebaseAnalytics.getInstance(requireContext())

        val sharedPref =
            requireContext().getSharedPreferences(
                "prefs",
                Context.MODE_PRIVATE
            )

        with(binding) {

            yearZoom.setOnClickListener {
                val action = CropPricesFragmentDirections.actionCropPricesFragmentToZoomedInFragment(0)
                findNavController().navigate(action)
            }

            mandiZoom.setOnClickListener {
                val action = CropPricesFragmentDirections.actionCropPricesFragmentToZoomedInFragment(1)
                findNavController().navigate(action)
            }

            priceView2.setOnClickListener {
                findNavController().navigateUp()
            }

            val adap1 = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, resources.getStringArray(
                R.array.mandi
            ))
            adap1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            mandiSelector.adapter = adap1

            mandiSelector.setSelection(adap1.getPosition("MAHARASHTRA_NAGPUR_Price"))

            mandiSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    val bundle = Bundle()
                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, LocalDate.now().toEpochDay().toString())
                    bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, resources.getStringArray(R.array.mandi)[p2])
                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "text")
                    bundle.putString("type", "single-select-mandi")
                    analytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
                    viewModel.changeMandi(resources.getStringArray(R.array.mandi)[p2])
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    //do nothing
                }
            }
            val adap2 = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, resources.getStringArray(
                R.array.commodity
            ))
            adap2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            commSelector.adapter = adap2

            val current = if (LocalDate.now().isBefore(LocalDate.of(LocalDate.now().year, 6, 30)))
                LocalDate.now().year - 1
            else LocalDate.now().year

            val arr = (current-7..current).map {
                "${it}-${(it + 1) % 100}"
            }

            val adap3 = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, arr)
            adap3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            yearSelector.adapter = adap3
            yearSelector.setSelection(adap3.getPosition("${current}-${(current + 1) % 100}"))

            yearSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    val bundle = Bundle()
                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, p2.toString())
                    bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, arr.toList()[p2].substring(0, 4))
                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "text")
                    bundle.putString("type", "single-select-year")
                    analytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
                    viewModel.changeYear(arr.toList()[p2].substring(0, 4).toInt())
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    //do nothing
                }
            }

            arr.forEachIndexed { i, item ->
                val year = item.substring(0, 4).toInt()

                val chip = Chip(context)
                chip.text = item
                chip.isCheckable = true

                if(viewModel.checkYear(year)) {
                    chip.isChecked = true
                }

                chip.setOnCheckedChangeListener { button, b ->

                    if(b) {
                        val bundle = Bundle()
                        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, i.toString())
                        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, year.toString().plus("-chip"))
                        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "text")
                        bundle.putString("type", "multi-select-year")
                        analytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)

                    }

                    viewModel.selectYear(year, b)
                }

                chipGroup.addView(chip)
            }


            val mandis = resources.getStringArray(R.array.mandi)

            for(mandi in mandis) {

                val mChip = Chip(context)
                mChip.text = mandi
                mChip.isCheckable = true

                if(viewModel.checkMandi(mandi)) {
                    mChip.isChecked = true
                }

                mChip.setOnCheckedChangeListener { button, b ->

                    if(b) {
                        val bundle = Bundle()
                        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, mandis.indexOf(mandi).toString())
                        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, mandi.toString().plus("-chip"))
                        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "text")
                        bundle.putString("type", "multi-select-mandi")
                        analytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
                    }

                    viewModel.selectMandi(mandi, b)
                }

                chipGroup2.addView(mChip)
            }


            ready(yearChart)
            ready(mandiChart)

            viewModel.trends.observe(viewLifecycleOwner) {
//                var s = ""
//                    s += this@CropPricesFragment.getString(R.string.trends,
//                    year,
//                    (year + 1) % 100,
//                    it[year]!![0].first,
//                    it[year]!![0].second,
//                    it[year]!![1].first,
//                    it[year]!![1].second
//                    )
                Log.d("trends", " has been changed")
                if(it.isNotEmpty()) {
                    tableTrends.visibility = View.VISIBLE
                }
                else {
                    tableTrends.visibility = View.GONE
                }
                tableTrends.removeViews(1, tableTrends.childCount - 1)
                for(year in it.keys.sorted()) {
                    val row: TableRow = TableRow(context)
                    val t1: TextView = TextView(context)
                    val t2: TextView = TextView(context)
                    val t3: TextView = TextView(context)
                    val t4: TextView = TextView(context)
                    row.removeViews(0, row.childCount)
                    row.background = ResourcesCompat.getDrawable(resources, R.drawable.table_row_bg, null)
                    t1.text = "Highest\nLowest"
                    t1.setTypeface(null, Typeface.BOLD)
                    t1.textSize = 16f
                    t1.gravity = 1 //CENTER_HORIZONTAL
                    t1.background = ResourcesCompat.getDrawable(resources, R.drawable.table_cell_bg, null)
                    t2.text = "${year}-${(year+1)%100}\n"
                    t2.textSize = 16f
                    t2.gravity = 1 //CENTER_HORIZONTAL
                    t2.background = ResourcesCompat.getDrawable(resources, R.drawable.table_cell_bg, null)
                    t3.text = "${it[year]!![0].first}\n${it[year]!![1].first}"
                    t3.textSize = 16f
                    t3.gravity = 1 //CENTER_HORIZONTAL
                    t3.background = ResourcesCompat.getDrawable(resources, R.drawable.table_cell_bg, null)
                    t4.text = "${it[year]!![0].second}\n${it[year]!![1].second}"
                    t4.textSize = 16f
                    t4.gravity = 1 //CENTER_HORIZONTAL
                    t4.background = ResourcesCompat.getDrawable(resources, R.drawable.table_cell_bg, null)
                    row.addView(t1)
                    row.addView(t2)
                    row.addView(t3)
                    row.addView(t4)
                    tableTrends.addView(row)
                }
//                trends.text = s
            }


            viewModel.dataByYear.observe(viewLifecycleOwner) {

                yearChart.clear()
                yearChart.xAxis.valueFormatter = IndexAxisValueFormatter(dates)
                yearChart.data = LineData(it)

                yearChart.onChartGestureListener =
                    Utils.Companion.CustomChartListener(requireContext(), yearChart, dates)

                if(!sharedPref.getBoolean("compress", false)) {
                    yearChart.setVisibleXRangeMaximum(10.0f)
                }

                when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                    Configuration.UI_MODE_NIGHT_NO -> {
                        yearChart.xAxis.textColor = Color.BLACK
                        yearChart.legend.textColor = Color.BLACK
                        yearChart.data.setValueTextColor(Color.BLACK)
                        yearChart.axisRight.textColor = Color.BLACK
                        yearChart.axisLeft.textColor = Color.BLACK
                    }
                    Configuration.UI_MODE_NIGHT_YES -> {
                        yearChart.xAxis.textColor = Color.WHITE
                        yearChart.legend.textColor = Color.WHITE
                        yearChart.data.setValueTextColor(Color.WHITE)
                        yearChart.axisRight.textColor = Color.WHITE
                        yearChart.axisLeft.textColor = Color.WHITE
                    }
                }

                yearChart.moveViewToX(dates.size - 30f)
                yearChart.invalidate()

            }

            viewModel.dataByMandi.observe(viewLifecycleOwner) {

                mandiChart.clear()
                val axis = mandiChart.axisRight
                val axis2 = mandiChart.axisLeft

                val mspLine = LimitLine(Utils.MSP[2015 + yearSelector.selectedItemPosition]!!, "Minimum Support Price")
                mspLine.lineWidth = 2f
                mspLine.textSize = 8f

                axis.removeAllLimitLines()
                axis.axisMinimum = 0f
                axis2.axisMinimum = 0f
                axis.addLimitLine(mspLine)

                mandiChart.xAxis.valueFormatter = IndexAxisValueFormatter(dates)
                mandiChart.data = LineData(it)

                mandiChart.onChartGestureListener =
                    Utils.Companion.CustomChartListener(requireContext(), mandiChart, dates)

                if(!sharedPref.getBoolean("compress", false)) {
                    mandiChart.setVisibleXRangeMaximum(10.0f)
                }

                when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                    Configuration.UI_MODE_NIGHT_NO -> {
                        mandiChart.xAxis.textColor = Color.BLACK
                        mandiChart.legend.textColor = Color.WHITE
                        mandiChart.data.setValueTextColor(Color.BLACK)
                        axis.textColor = Color.BLACK
                        axis2.textColor = Color.BLACK
                        mspLine.lineColor = Color.BLACK
                        mspLine.textColor = Color.BLACK
                    }
                    Configuration.UI_MODE_NIGHT_YES -> {
                        mandiChart.xAxis.textColor = Color.WHITE
                        mandiChart.legend.textColor = Color.WHITE
                        mandiChart.data.setValueTextColor(Color.WHITE)
                        axis.textColor = Color.WHITE
                        axis2.textColor = Color.WHITE
                        mspLine.lineColor = Color.WHITE
                        mspLine.textColor = Color.WHITE
                    }
                }

                mandiChart.moveViewToX(dates.size - 30f)
                mandiChart.invalidate()

            }

//            sharedPref.registerOnSharedPreferenceChangeListener { sharedPreferences, s ->
//                if(s != "compress") return@registerOnSharedPreferenceChangeListener
//
//                if(sharedPreferences!!.getBoolean("compress", false)) {
//                    mandiChart.setVisibleXRangeMaximum(365.0f)
//                    yearChart.setVisibleXRangeMaximum(365.0f)
//
//                    mandiChart.fitScreen()
//                    yearChart.fitScreen()
//                }
//
//                else {
//                    mandiChart.setVisibleXRangeMaximum(10.0f)
//                    yearChart.setVisibleXRangeMaximum(10.0f)
//
//                    mandiChart.moveViewToX(0.0f)
//                    yearChart.moveViewToX(0.0f)
//                }
//
//
//            }


            yearChartShare.setOnClickListener {
                val icon: Bitmap = yearChart.chartBitmap
                val share = Intent(Intent.ACTION_SEND)
                share.type = "image/png"

                try {
                    val file = File(requireContext().cacheDir, "temp.png")
                    val fOut = FileOutputStream(file)
                    icon.compress(CompressFormat.PNG, 100, fOut)
                    fOut.flush()
                    fOut.close()
                    file.setReadable(true, false)
                    share.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(requireContext(), requireContext().packageName + ".provider", file))
                    share.putExtra(Intent.EXTRA_TEXT, mandiSelector.selectedItem.toString())

                    val bundle = Bundle()
                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, mandiSelector.selectedItem.toString())
                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "image")
                    analytics.logEvent(FirebaseAnalytics.Event.SHARE, bundle)

                    startActivity(share)
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(requireContext(), "Error occurred, please try later", Toast.LENGTH_SHORT).show()
                }

            }

            mandiChartShare.setOnClickListener {
                val icon: Bitmap = mandiChart.chartBitmap
                val share = Intent(Intent.ACTION_SEND)
                share.type = "image/png"

                try {
                    val file = File(requireContext().cacheDir, "temp.png")
                    val fOut = FileOutputStream(file)
                    icon.compress(CompressFormat.PNG, 100, fOut)
                    fOut.flush()
                    fOut.close()
                    file.setReadable(true, false)
                    share.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(requireContext(), requireContext().packageName + ".provider", file))
                    share.putExtra(Intent.EXTRA_TEXT, "Prices in ${yearSelector.selectedItem}")

                    val bundle = Bundle()
                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, yearSelector.selectedItem.toString())
                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "image")
                    analytics.logEvent(FirebaseAnalytics.Event.SHARE, bundle)

                    startActivity(share)
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(requireContext(), "Error occurred, please try later", Toast.LENGTH_SHORT).show()
                }

            }
        }

        return binding.root
    }

}