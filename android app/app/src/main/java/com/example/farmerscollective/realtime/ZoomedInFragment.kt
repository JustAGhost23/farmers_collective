package com.example.farmerscollective.realtime

import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.farmerscollective.R
import com.example.farmerscollective.databinding.FragmentZoomedInBinding
import com.example.farmerscollective.utils.Utils
import com.example.farmerscollective.utils.Utils.Companion.adjustAxis
import com.example.farmerscollective.utils.Utils.Companion.dates
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.time.LocalDate


class ZoomedInFragment : Fragment() {

    private lateinit var binding: FragmentZoomedInBinding
    private val viewModel by activityViewModels<CropPricesViewModel>()
    private val args by navArgs<ZoomedInFragmentArgs>()



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_zoomed_in, container, false)
        val sharedPref =
            requireContext().getSharedPreferences(
                "prefs",
                Context.MODE_PRIVATE
            )

        with(binding) {
            zoomChart.clear()
            zoom.setOnClickListener {
                requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
                findNavController().navigateUp()
            }

            when(args.chart) {
                0 ->
                    viewModel.dataByYear.observe(viewLifecycleOwner) {
                        zoomChart.clear()
                        zoomChart.xAxis.valueFormatter = IndexAxisValueFormatter(Utils.dates)
                        zoomChart.data = LineData(it)

                        zoomChart.onChartGestureListener =
                            Utils.Companion.CustomChartListener(requireContext(), zoomChart,
                                Utils.dates
                            )

                        if(!sharedPref.getBoolean("compress", false)) {
                            zoomChart.setVisibleXRangeMaximum(10.0f)
                        }

                        adjustAxis(zoomChart)
                        zoomChart.invalidate()

                        when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                            Configuration.UI_MODE_NIGHT_NO -> {
                                binding.zoomedInFragment.setBackgroundColor(Color.WHITE)
                                zoomChart.xAxis.textColor = Color.BLACK
                                zoomChart.legend.textColor = Color.BLACK
                                zoomChart.data.setValueTextColor(Color.BLACK)
                                zoomChart.axisRight.textColor = Color.BLACK
                                zoomChart.axisLeft.textColor = Color.BLACK
                            }
                            Configuration.UI_MODE_NIGHT_YES -> {
                                binding.zoomedInFragment.setBackgroundColor(Color.BLACK)
                                zoomChart.xAxis.textColor = Color.WHITE
                                zoomChart.legend.textColor = Color.WHITE
                                zoomChart.data.setValueTextColor(Color.WHITE)
                                zoomChart.axisRight.textColor = Color.WHITE
                                zoomChart.axisLeft.textColor = Color.WHITE
                            }
                        }

                    }
                
                1 ->
                    viewModel.dataByMandi.observe(viewLifecycleOwner) {

                        val axis = zoomChart.axisRight
                        val axis2 = zoomChart.axisLeft

                        val mspLine = LimitLine(Utils.MSP[viewModel.year.value]!!, "Minimum Support Price")
                        mspLine.lineColor = Color.GRAY
                        mspLine.lineWidth = 2f
                        mspLine.textColor = Color.BLACK
                        mspLine.textSize = 8f

                        axis.removeAllLimitLines()
                        axis.axisMinimum = 0f
                        axis2.axisMinimum = 0f
                        axis.addLimitLine(mspLine)

                        zoomChart.xAxis.valueFormatter = IndexAxisValueFormatter(Utils.dates)
                        zoomChart.data = LineData(it)

                        zoomChart.onChartGestureListener =
                            Utils.Companion.CustomChartListener(requireContext(), zoomChart,
                                Utils.dates
                            )

                        if(!sharedPref.getBoolean("compress", false)) {
                            zoomChart.setVisibleXRangeMaximum(10.0f)
                        }

                        zoomChart.moveViewToX(Utils.dates.size - 30f)
                        zoomChart.invalidate()

                        when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                            Configuration.UI_MODE_NIGHT_NO -> {
                                mspLine.textColor = Color.BLACK
                                binding.zoomedInFragment.setBackgroundColor(Color.WHITE)
                                zoomChart.xAxis.textColor = Color.BLACK
                                zoomChart.legend.textColor = Color.BLACK
                                zoomChart.data.setValueTextColor(Color.BLACK)
                                zoomChart.axisRight.textColor = Color.BLACK
                                zoomChart.axisLeft.textColor = Color.BLACK
                            }
                            Configuration.UI_MODE_NIGHT_YES -> {
                                mspLine.textColor = Color.WHITE
                                binding.zoomedInFragment.setBackgroundColor(Color.BLACK)
                                zoomChart.xAxis.textColor = Color.WHITE
                                zoomChart.legend.textColor = Color.WHITE
                                zoomChart.data.setValueTextColor(Color.WHITE)
                                zoomChart.axisRight.textColor = Color.WHITE
                                zoomChart.axisLeft.textColor = Color.WHITE
                            }
                        }

                    }



            }
        }
        return binding.root
    }

}