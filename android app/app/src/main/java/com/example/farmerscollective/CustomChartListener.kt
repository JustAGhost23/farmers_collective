package com.example.farmerscollective

import android.content.Context
import android.view.MotionEvent
import android.widget.Toast
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.listener.ChartTouchListener
import com.github.mikephil.charting.listener.OnChartGestureListener

class CustomChartListener(val context: Context, val chart: LineChart, val dates: ArrayList<String>): OnChartGestureListener {
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