package com.example.farmerscollective.utils

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.view.MotionEvent
import android.widget.Toast
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.listener.ChartTouchListener
import com.github.mikephil.charting.listener.OnChartGestureListener
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.stream.Stream
import kotlin.math.pow
import kotlin.math.roundToInt

class Utils {

    companion object {
        val MSP = mapOf(Pair(2015, 2600f), Pair(2016, 2775f), Pair(2017, 3050f), Pair(2018, 3399f), Pair(2019, 3710f), Pair(2020, 3880f), Pair(2021, 3950f), Pair(2022, 4300f))

        val dates = ArrayList<String>()
        val yearColors = mutableMapOf<Int, Int>()
        val colors = listOf("#FF0000", "#00FF00", "#0000FF", "#FFFF00", "#00FFFF", "#FF00FF", "#FBCEB1", "#DDFFDD")

        init {

            val start = LocalDate.of(2001, 7, 1)
            val end = LocalDate.of(2002, 6, 30)

            Stream.iterate(start) { d ->
                d.plusDays(1)
            }
                .limit(start.until(end, ChronoUnit.DAYS))
                .forEach { date ->
                    val dt = date.toString()
                    dates.add(dt.substring(8) + dt.substring(4, 7))
                }

            val current = if (LocalDate.now().isBefore(LocalDate.of(LocalDate.now().year, 6, 30)))
                LocalDate.now().year - 1
            else LocalDate.now().year

            (current - 7..current).forEachIndexed { i, item ->
                yearColors[item] = Color.parseColor(colors[i])
            }
        }

        fun ready(chart: LineChart) {

            chart.setDrawGridBackground(false)
            chart.description.isEnabled = false
            chart.setDrawBorders(false)
            chart.legend.isWordWrapEnabled = true
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
            chart.setPinchZoom(false)

        }


        fun roundToString(value: Float, places: Int = 1) = ((value * 10.0.pow(places.toDouble())).roundToInt() / 10.0.pow(places.toDouble())).toString()

        class CustomChartListener(val context: Context, val chart: LineChart, val dates: ArrayList<String>):
            OnChartGestureListener {
            private var mToast: Toast? = null

            override fun onChartSingleTapped(me: MotionEvent?) {

                val x = chart.getHighlightByTouchPoint(me!!.x, me.y).x.toInt()
                if(x < dates.size) {
                    val y = chart.getHighlightByTouchPoint(me.x, me.y).y
                    val date = dates[x]

                    mToast?.cancel()
                    mToast = Toast.makeText(context, "$date $y", Toast.LENGTH_SHORT)
                    mToast?.show()
                }

            }

            override fun onChartGestureStart(
                me: MotionEvent?,
                lastPerformedGesture: ChartTouchListener.ChartGesture?
            ) {

            }

            override fun onChartGestureEnd(
                me: MotionEvent?,
                lastPerformedGesture: ChartTouchListener.ChartGesture?
            ) {

            }

            override fun onChartLongPressed(me: MotionEvent?) {

            }

            override fun onChartTranslate(me: MotionEvent?, dX: Float, dY: Float) {

            }

            override fun onChartDoubleTapped(me: MotionEvent?) {

            }

            override fun onChartFling(
                me1: MotionEvent?,
                me2: MotionEvent?,
                velocityX: Float,
                velocityY: Float
            ) {

            }

            override fun onChartScale(me: MotionEvent?, scaleX: Float, scaleY: Float) {

            }
        }
    }
}