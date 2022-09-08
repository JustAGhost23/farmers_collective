package com.example.farmerscollective.utils

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import com.example.farmerscollective.prediction.CropPastPredictedViewModel
import java.time.LocalDate
import java.util.*

class DatePickerFragment(private val viewModel: CropPastPredictedViewModel) : DialogFragment(), DatePickerDialog.OnDateSetListener {

    private var dialog: DatePickerDialog? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current date as the default date in the picker

        if(dialog == null) {
            val start = LocalDate.now().minusDays(30)
            val year = start.year
            val month = start.monthValue - 1
            val day = start.dayOfMonth

            // Create a new instance of DatePickerDialog and return it
            dialog = DatePickerDialog(requireContext(), this, year, month, day)
        }

        return dialog!!
    }

    override fun onDateSet(p0: DatePicker?, p1: Int, p2: Int, p3: Int) {
        dialog!!.updateDate(p1, p2, p3)
        viewModel.changeDate(LocalDate.of(p1, p2 + 1, p3))
    }

}