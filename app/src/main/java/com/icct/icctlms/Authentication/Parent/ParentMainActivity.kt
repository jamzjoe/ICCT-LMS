package com.icct.icctlms.Authentication.Parent

import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.icct.icctlms.R
import kotlinx.android.synthetic.main.activity_parent_main.*
import kotlinx.android.synthetic.main.activity_update_student_profile.*
import java.io.File

class ParentMainActivity : AppCompatActivity() {
    private lateinit var storageReference : StorageReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_parent_main)


        val name = intent.getStringExtra("name")
        val school = intent.getStringExtra("school")

        showProfile()
        parent_name.text = name
        parent_school.text = school
        show_grades_btn.setOnClickListener{
        }
    }

    private fun showProfile() {
        val uid = intent.getStringExtra("uid")
        storageReference = FirebaseStorage.getInstance().getReference("Students/$uid")
        val localFile = File.createTempFile("tempImage", "jpg")
        storageReference.getFile(localFile).addOnSuccessListener {
            val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
            parent_profile.setImageBitmap(bitmap)

        }
    }
}