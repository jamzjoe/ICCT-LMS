package com.icct.icctlms.tools

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.icct.icctlms.R

class AlertDialog {
    fun showDialog(context: Context, title: String?, message: String, positiveText: String, unit: Unit?){
        MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(positiveText){_,_ ->

                run { unit }
            }
            .setIcon(R.drawable.logo_plain)
            .show()
    }
}