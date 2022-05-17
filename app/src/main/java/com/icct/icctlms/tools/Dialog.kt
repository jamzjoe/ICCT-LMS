package com.icct.icctlms.tools

import android.app.Dialog
import android.content.Context
import com.icct.icctlms.R

class Dialog {
    private lateinit var dialog : Dialog

     fun progressDialogShow(context : Context, message : String){
        dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_layout)
        dialog.setTitle(message)
        dialog.setCancelable(false)
        dialog.show()
    }
    fun progressDialogHide(){
        dialog.hide()
    }
}