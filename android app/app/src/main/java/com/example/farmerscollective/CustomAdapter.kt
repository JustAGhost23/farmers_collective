package com.example.farmerscollective

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import com.example.farmerscollective.data.Prediction
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet

class CustomAdapter(data: ArrayList<Prediction>, context: Context) : ArrayAdapter<Prediction>(context, R.layout.recomm_item, data) {

    private class ViewHolder {
        lateinit var date: TextView
        lateinit var loss: View
        lateinit var gain: View
        lateinit var result: View
        lateinit var parent: ConstraintLayout
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val result: View
        val viewHolder: ViewHolder
        val pred = getItem(position)

        if(convertView == null)  {
            viewHolder = ViewHolder()
            result = LayoutInflater.from(context).inflate(R.layout.recomm_item, parent, false)

            viewHolder.parent = result.findViewById(R.id.parent)
            viewHolder.date = result.findViewById(R.id.date)
            viewHolder.loss = result.findViewById(R.id.loss)
            viewHolder.gain = result.findViewById(R.id.gain)
            viewHolder.result = result.findViewById(R.id.result)


            result.tag = viewHolder
        }

        else {
            viewHolder = convertView.tag as ViewHolder
            result = convertView
        }

        viewHolder.date.text = pred!!.date
        val l = pred.loss * (1 - pred.confidence)
        val g = pred.gain * pred.confidence

        val constraints = ConstraintSet()
        constraints.clone(viewHolder.parent)
        constraints.setHorizontalBias(R.id.result, g / (l + g))
        constraints.applyTo(viewHolder.parent)

        return result
    }
}