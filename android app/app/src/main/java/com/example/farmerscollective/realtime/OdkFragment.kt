package com.example.farmerscollective.realtime

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.farmerscollective.R
import com.example.farmerscollective.data.OdkSubmission
import com.example.farmerscollective.databinding.FragmentOdkBinding
import com.example.farmerscollective.utils.Utils.Companion.traders
import com.example.farmerscollective.utils.Utils.Companion.traderColors
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.LegendEntry
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.google.firebase.analytics.FirebaseAnalytics
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.min


class OdkFragment : Fragment() {

    private val viewModel by activityViewModels<OdkViewModel>()
    private lateinit var binding: FragmentOdkBinding
    private lateinit var analytics: FirebaseAnalytics

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_odk, container, false)
        analytics = FirebaseAnalytics.getInstance(requireContext())

        val sharedPref =
            requireContext().getSharedPreferences(
                "prefs",
                Context.MODE_PRIVATE
            )

        with(binding) {

            barZoom.setOnClickListener {
                val action = OdkFragmentDirections.actionOdkFragmentToZoomedInFragment(3)
                findNavController().navigate(action)
            }

//            val spinAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, resources.getStringArray(R.array.odk_filter))
//            spinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//            filterSpinner.adapter = spinAdapter
//            filterSpinner.setSelection(viewModel.filter.value!!)
//
//            filterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
//                    viewModel.filter(p2)
//                }
//
//                override fun onNothingSelected(p0: AdapterView<*>?) {
//                    //pass
//                }
//            }

            val cropNameAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, resources.getStringArray(R.array.cropName))
            cropNameAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            cropSpinner.adapter = cropNameAdapter
            cropSpinner.setSelection(viewModel.crop.value!!)

            cropSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    viewModel.chooseCrop(p2)
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    //pass
                }
            }

            val current = if (LocalDate.now().isBefore(LocalDate.of(LocalDate.now().year, 6, 30)))
                LocalDate.now().year - 1
            else LocalDate.now().year

            val initial = 2022

            val arr = (initial..current).map {
                "${it}-${(it + 1) % 100}"
            }

            val yearAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, arr)
            yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            yearSpinner.adapter = yearAdapter
            yearSpinner.setSelection(yearAdapter.getPosition("${current}-${(current + 1) % 100}"))

            yearSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    viewModel.changeYear(arr.toList()[p2].substring(0, 4).toInt())
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    //do nothing
                }
            }

            viewModel.list.observe(viewLifecycleOwner) {
                Log.d(this.toString(), it.keys.toString())
                val formatter: DateTimeFormatter =
                    DateTimeFormatter.ofPattern("MM-dd")
//                if(it.isNotEmpty()) {
//                    binding.xAxis.text =
//                        "Submission Dates (${it.keys.first().format(formatter)} - ${
//                            it.keys.last().format(formatter)
//                        })"
//                }
//                else {
//                    binding.xAxis.text = "Submission Dates"
//                }

                val list = it
                val entries1: ArrayList<BarEntry> = ArrayList()
                val entries2: ArrayList<BarEntry> = ArrayList()
                val entries3: ArrayList<BarEntry> = ArrayList()
                val entries4: ArrayList<BarEntry> = ArrayList()
                val barColors1: ArrayList<Int> = ArrayList()
                val barColors2: ArrayList<Int> = ArrayList()
                val barColors3: ArrayList<Int> = ArrayList()
                val barColors4: ArrayList<Int> = ArrayList()
                val colorList: ArrayList<LegendEntry> = ArrayList()
                val traderList: ArrayList<String> = ArrayList()
                val axis = ArrayList<String?>()
                val subs = mutableMapOf<Int, List<OdkSubmission?>>()

                var minPrice = 3500f
                var xMax = 0f

//                var i = 1
//                for(color in traderColors) {
//                    val legendEntry: LegendEntry
//                    if(i != 1) {
//                        legendEntry = LegendEntry(
//                            traders[i - 2],
//                            Legend.LegendForm.SQUARE,
//                            10.0f,
//                            10.0f,
//                            null,
//                            Color.parseColor(color)
//                        )
//                    }
//                    else {
//                        legendEntry = LegendEntry(
//                            "Not filled",
//                            Legend.LegendForm.SQUARE,
//                            10.0f,
//                            10.0f,
//                            null,
//                            Color.parseColor(color)
//                        )
//                    }
//                    colorList.add(legendEntry)
//                    i += 1
//                }

                if (list != null) {
                    for(i in list) {
//                        axis.add("")
                        axis.add(i.key.format(formatter))
//                        for(j in 0 until 3) axis.add("")
//                        val count = i.value.size
//                        if(count % 2 == 1) {
//                            for (j in 0 until count / 2) {
//                                axis.add("          ")
//                            }
//                            axis.add(i.key.format(formatter))
//                            for (j in 0 until count / 2) {
//                                axis.add("          ")
//                            }
//                        }
//                        else {
//                            for (j in 0 until (count / 2) - 1) {
//                                axis.add("          ")
//                            }
//                            axis.add(i.key.format(formatter))
//                            for (j in 0 until count / 2) {
//                                axis.add("          ")
//                            }
//                        }
//                        axis.add("          ")
                    }
                }
                Log.e("AXIS", axis.toString())

                if (list != null) {
                    var count = 0f
                    for (i in list) {
                        xMax += 1f
                        var pos = count
                        val v = i.value.reversed().sortedBy { it!!.price }.subList(0, min(4, i.value.size))
                        entries1.add(BarEntry(pos, -100f))
                        entries2.add(BarEntry(pos + 0.20f, -100f))
                        entries3.add(BarEntry(pos + 0.40f, -100f))
                        entries4.add(BarEntry(pos + 0.60f, -100f))
                        barColors1.add(0)
                        barColors2.add(0)
                        barColors3.add(0)
                        barColors4.add(0)
                        for(j in v) {
                            if (j != null) {
                                val legendEntry: LegendEntry
                                if(j.localTraderId == -1) {
                                    legendEntry = LegendEntry(
                                    "Not filled",
                                    Legend.LegendForm.SQUARE,
                                    10.0f,
                                    10.0f,
                                    null,
                                    Color.parseColor(
                                        traderColors[j.localTraderId.plus(1)]
                                    )
                                )
                                    if(v.indexOf(j) == 0) {
                                        barColors1.removeAt(barColors1.size - 1)
                                        barColors1.add(
                                            Color.parseColor(
                                                traderColors[j.localTraderId.plus(1)]
                                            )
                                        )
                                    }
                                    else if(v.indexOf(j) == 1) {
                                        barColors2.removeAt(barColors2.size - 1)
                                        barColors2.add(
                                            Color.parseColor(
                                                traderColors[j.localTraderId.plus(1)]
                                            )
                                        )
                                    }
                                    else if(v.indexOf(j) == 2) {
                                        barColors3.removeAt(barColors3.size - 1)
                                        barColors3.add(
                                            Color.parseColor(
                                                traderColors[j.localTraderId.plus(1)]
                                            )
                                        )
                                    }
                                    else if(v.indexOf(j) == 3) {
                                        barColors4.removeAt(barColors4.size - 1)
                                        barColors4.add(
                                            Color.parseColor(
                                                traderColors[j.localTraderId.plus(1)]
                                            )
                                        )
                                    }
                                    if(!traderList.contains("Not filled")) {
                                        colorList.add(legendEntry)
                                        traderList.add("Not filled")
                                    }
                                }
                                else {
                                    legendEntry = LegendEntry(
                                        traders[j.localTraderId?.minus(1)!!],
                                        Legend.LegendForm.SQUARE,
                                        10.0f,
                                        10.0f,
                                        null,
                                        Color.parseColor(
                                            traderColors[j.localTraderId]
                                        )
                                    )
                                    if(v.indexOf(j) == 0) {
                                        barColors1.removeAt(barColors1.size - 1)
                                        barColors1.add(
                                            Color.parseColor(
                                                traderColors[j.localTraderId]
                                            )
                                        )
                                    }
                                    else if(v.indexOf(j) == 1) {
                                        barColors2.removeAt(barColors2.size - 1)
                                        barColors2.add(
                                            Color.parseColor(
                                                traderColors[j.localTraderId]
                                            )
                                        )
                                    }
                                    else if(v.indexOf(j) == 2) {
                                        barColors3.removeAt(barColors3.size - 1)
                                        barColors3.add(
                                            Color.parseColor(
                                                traderColors[j.localTraderId]
                                            )
                                        )
                                    }
                                    else if(v.indexOf(j) == 3) {
                                        barColors4.removeAt(barColors4.size - 1)
                                        barColors4.add(
                                            Color.parseColor(
                                                traderColors[j.localTraderId]
                                            )
                                        )
                                    }
                                    if(!traderList.contains(traders[j.localTraderId.minus(1)])) {
                                        colorList.add(legendEntry)
                                        traderList.add(traders[j.localTraderId.minus(1)])
                                    }
                                }
                            }
                            val barEntry = BarEntry(pos, j!!.price.toFloat())
                            if(j.price.toFloat() < minPrice) {
                                minPrice = j.price.toFloat() - 200f
                            }
                            if(v.indexOf(j) == 0) {
                                entries1.removeAt(entries1.size - 1)
                                entries1.add(barEntry)
                            }
                            else if(v.indexOf(j) == 1) {
                                entries2.removeAt(entries2.size - 1)
                                entries2.add(barEntry)
                            }
                            else if(v.indexOf(j) == 2) {
                                entries3.removeAt(entries3.size - 1)
                                entries3.add(barEntry)
                            }
                            else if(v.indexOf(j) == 3) {
                                entries4.removeAt(entries4.size - 1)
                                entries4.add(barEntry)
                            }
                            subs[pos.toInt()] = v
                            pos += 0.20f
                        }
                        count += 1f
                    }
                }
                Log.e("entries1", entries1.toString())
                Log.e("entries2", entries2.toString())
                Log.e("entries3", entries3.toString())
                Log.e("entries4", entries4.toString())

                val barDataSet1 = BarDataSet(entries1, "")
                val barDataSet2 = BarDataSet(entries2, "")
                val barDataSet3 = BarDataSet(entries3, "")
                val barDataSet4 = BarDataSet(entries4, "")
                barDataSet1.colors = barColors1
                barDataSet2.colors = barColors2
                barDataSet3.colors = barColors3
                barDataSet4.colors = barColors4

                val data = BarData(barDataSet1, barDataSet2, barDataSet3, barDataSet4)
                data.barWidth = 0.20f
                barChart.description.isEnabled = false
                barChart.axisRight.isEnabled = true
                barChart.axisLeft.isEnabled = false
                barChart.legend.setCustom(colorList)
                barChart.legend.isWordWrapEnabled = true
                barChart.axisRight.setDrawGridLines(false)
                barChart.axisLeft.setDrawGridLines(false)
                barChart.xAxis.setDrawGridLines(true)
                barChart.data = data
                barChart.axisRight.axisMinimum = minPrice
                barChart.axisLeft.axisMinimum = minPrice
                barChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
                barChart.xAxis.granularity = 1f
                barChart.xAxis.valueFormatter = IndexAxisValueFormatter(axis)
                barChart.isHighlightPerDragEnabled = false
                barChart.groupBars(0f, 0.12f, 0.02f)
                barChart.xAxis.setCenterAxisLabels(true)
                barChart.moveViewToX(xMax)
                barChart.setFitBars(false)
                if(!sharedPref.getBoolean("compress", false)) {
                    barChart.setVisibleXRangeMaximum(8.0f)
                }
                else {
                    barChart.setVisibleXRangeMaximum(365.0f)
                }
                barChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener
                {
                    override fun onValueSelected(e: Entry, h: Highlight?) {
                        val x = e.x.toString()
//                        val y = e.y.toString()
                        val selectedXAxisCount = x.substringBefore(".")
                        val selectedXAxisPos = x.substringAfter(".").substring(0, 2)
                        val dataDialogBuilder: AlertDialog.Builder? = activity?.let { fragmentActivity ->
                            AlertDialog.Builder(fragmentActivity)
                        }
                        if(subs[selectedXAxisCount.toInt()]?.size!! > selectedXAxisPos.toInt() / 25) {
                            val selectedOdkSubmission =
                                subs[selectedXAxisCount.toInt()]?.get(selectedXAxisPos.toInt() / 25)
                            val traderName =
                                if (selectedOdkSubmission?.localTraderId!! == -1) "Not filled" else traders[selectedOdkSubmission.localTraderId - 1]
                            val mandalId =
                                if (selectedOdkSubmission.mandalId == "") "Not filled" else selectedOdkSubmission.mandalId
                            dataDialogBuilder?.setMessage("Trader Name: ${traderName}\nMandal: ${mandalId}\nPrice: Rs ${selectedOdkSubmission.price}\nFilled by: ${selectedOdkSubmission.personFillingId}\nFilled on: ${selectedOdkSubmission.date}")!!
                                .setCancelable(false)
                                .setPositiveButton("Dismiss") { dialog, _ ->
                                    barChart.highlightValues(null)
                                    dialog.dismiss()
                                }
                            val dataDialog: AlertDialog = dataDialogBuilder.create()
                            dataDialog.setTitle("ODK Data")
                            dataDialog.show()
                        }
                    }

                    override fun onNothingSelected() {
                        //pass
                    }
                })
                barChart.invalidate()
            }

            barChartShare.setOnClickListener {
                val icon: Bitmap = barChart.chartBitmap
                val share = Intent(Intent.ACTION_SEND)
                share.type = "image/png"

                try {
                    val file = File(requireContext().cacheDir, "temp.png")
                    val fOut = FileOutputStream(file)
                    icon.compress(Bitmap.CompressFormat.PNG, 100, fOut)
                    fOut.flush()
                    fOut.close()
                    file.setReadable(true, false)
                    share.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(requireContext(), requireContext().packageName + ".provider", file))
                    share.putExtra(Intent.EXTRA_TEXT, cropSpinner.selectedItem.toString() + " ODK prices in " + yearSpinner.selectedItem.toString())

                    val bundle = Bundle()
                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, cropSpinner.selectedItem.toString() + " ODK prices in " + yearSpinner.selectedItem.toString())
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