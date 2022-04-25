package com.icct.icctlms.Authentication.Parent

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.icct.icctlms.R
import kotlinx.android.synthetic.main.activity_parent.*

class Parent : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_parent)


        database = FirebaseDatabase.getInstance().getReference("Students")

        show_student.setOnClickListener{
            val uid = student_account_id.text.toString()
            database.child(uid).get().addOnSuccessListener {
                if (it.exists()){
                    val name = it.child("name").value.toString()
                    val school = it.child("school").value.toString()
                    val intent = Intent(this, ParentMainActivity::class.java)

                    intent.putExtra("uid", uid)
                    intent.putExtra("name", name)
                    intent.putExtra("school", school)
                    startActivity(intent)
                }else{
                    MaterialAlertDialogBuilder(this)
                        .setMessage("This uid is not found or not registered.")
                        .setNegativeButton("Okay"){_, _ ->
                        }.show()
                }


            }

        }
    }
}