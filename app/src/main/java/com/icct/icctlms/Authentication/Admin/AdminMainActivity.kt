package com.icct.icctlms.Authentication.Admin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.icct.icctlms.R
import com.icct.icctlms.newsAndUpdates.CreateNewsAndUpdates
import kotlinx.android.synthetic.main.activity_admin_main.*

class AdminMainActivity : AppCompatActivity() {

    private var backPressed  = 0L
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_main)

        createAnnouncement()
    }

    private fun createAnnouncement() {
        card_announce.setOnClickListener{
            startActivity(Intent(this, CreateNewsAndUpdates::class.java))
        }
    }

    override fun onBackPressed() {
        if (backPressed + 2000 > System.currentTimeMillis()){
            MaterialAlertDialogBuilder(this)
                .setMessage("Click okay to logged out.")
                .setNegativeButton("Cancel"){_, _ ->
                }
                .setCancelable(false)
                .setPositiveButton("Okay"){_,_ ->
                    super.onBackPressed()
                }.show()
        }else{
            Toast.makeText(this, "Press again to logged out.", Toast.LENGTH_SHORT).show()
        }
        backPressed = System.currentTimeMillis()

    }
}