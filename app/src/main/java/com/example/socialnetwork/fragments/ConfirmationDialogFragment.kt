package com.example.socialnetwork.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.socialnetwork.R

class ConfirmationDialogFragment : DialogFragment() {

    interface ConfirmationDialogListener {
        fun onDialogPositiveClick()
        fun onDialogNegativeClick()
    }

    var listener: ConfirmationDialogListener? = null
    private var message: String? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it, R.style.CustomDialogTheme)
            builder.setMessage(message)
                .setPositiveButton(R.string.confirm) { _, _ ->
                    listener?.onDialogPositiveClick()
                }
                .setNegativeButton(R.string.cancel) { _, _ ->
                    listener?.onDialogNegativeClick()
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    fun setMessage(message: String) {
        this.message = message
    }
}
