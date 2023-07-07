package com.example.farmerscollective.utils

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.farmerscollective.R

class ChartRangeDialog : DialogFragment() {
    internal lateinit var listener: DialogListener

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    interface DialogListener {
        fun onDialogPositiveClick(dialog: DialogFragment, selected: Int)
    }

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = context as DialogListener
        } catch (e: ClassCastException) {
            // The activity doesn't implement the interface, throw exception
            throw ClassCastException(
                (context.toString() +
                        " must implement NoticeDialogListener")
            )
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            // Build the dialog and set up the button click handlers
            val builder = AlertDialog.Builder(it)
            var selected = 0

            val sharedPref =
                requireContext().getSharedPreferences(
                    "prefs",
                    Context.MODE_PRIVATE
                )

            // Creates a dialog for selecting the range of x-axis
            builder
                .setTitle("Select X-axis range")
                .setSingleChoiceItems(
                    R.array.ranges, if (sharedPref.getBoolean("compress", false)) 1 else 0
                ) { _, i ->
                    selected = i
                }
                .setPositiveButton(
                    "set"
                ) { dialog, id ->
                    // Send the positive button event back to the host activity
                    listener.onDialogPositiveClick(this, selected)
                }

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}