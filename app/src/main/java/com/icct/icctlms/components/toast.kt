package com.icct.icctlms.components

import android.app.Activity
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

class toast {
    fun message(message: String, context : Activity){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
    fun messageFragment(message: String, context: FragmentActivity){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}