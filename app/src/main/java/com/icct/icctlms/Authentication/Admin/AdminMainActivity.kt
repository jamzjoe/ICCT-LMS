package com.icct.icctlms.Authentication.Admin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.icct.icctlms.R
import com.icct.icctlms.newsAndUpdates.CreateNewsAndUpdates
import kotlinx.android.synthetic.main.activity_admin_main.*

class AdminMainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_main)

        createAnnouncement()
    }

    private fun createAnnouncement() {
        card_announce.setOnClickListener{
            startActivity(Intent(this, CreateNewsAndUpdates::class.java))
            finish()
        }
    }
}