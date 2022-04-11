package com.icct.icctlms.Authentication.Admin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.icct.icctlms.R
import kotlinx.android.synthetic.main.activity_admin.*

class Admin : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)
        log_button.setOnClickListener{
            Toast.makeText(this, "This features is not yet implemented. Stay updated. Thank you!", Toast.LENGTH_SHORT).show()
        }
    }
}