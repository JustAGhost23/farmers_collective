package com.example.farmerscollective.utils

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import com.example.farmerscollective.prediction.CropPastPredictedViewModel
import com.google.firebase.analytics.FirebaseAnalytics
import java.time.LocalDate

class DatePickerFragment(private val viewModel: CropPastPredictedViewModel) : DialogFragment(),
    DatePickerDialog.OnDateSetListener {

    private var dialog: DatePickerDialog? = null
    private var analytics: FirebaseAnalytics? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current date as the default date in the picker

        if (dialog == null) {
            val start = LocalDate.now().minusDays(30)
            val year = start.year
            val month = start.monthValue - 1
            val day = start.dayOfMonth

            // Create a new instance of DatePickerDialog and return it
            dialog = DatePickerDialog(requireContext(), this, year, month, day)
        }

        if (analytics == null) {
            analytics = FirebaseAnalytics.getInstance(requireContext())
        }

        return dialog!!
    }

    override fun onDateSet(p0: DatePicker?, p1: Int, p2: Int, p3: Int) {
        dialog!!.updateDate(p1, p2, p3)
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "date-select-123")
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "${p3}-${p2}-${p1}")
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "date")
        bundle.putString("type", "select-predict-date")
        analytics?.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
        viewModel.changeDate(LocalDate.of(p1, p2 + 1, p3))
    }

}