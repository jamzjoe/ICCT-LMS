package com.icct.icctlms

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.icct.icctlms.Authentication.Teacher.TeacherMainActivity

@SuppressLint("CustomSplashScreen")
class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        currentUser()
Handler(Looper.myLooper()!!).postDelayed({

    finish()
}, 2000)
    }

    private fun currentUser() {
        val uid =  Firebase.auth.currentUser?.uid.toString()
        val user = Firebase.auth.currentUser

        val teacher = FirebaseDatabase.getInstance().getReference("Teachers")
        val student = FirebaseDatabase.getInstance().getReference("Students")

        if(user != null){
            teacher.child(uid).get().addOnSuccessListener {
                if (it.exists()){
                    val type = it.child("type").value.toString()
                    if(type == "Teacher"){
                        val intent = Intent(this, TeacherMainActivity::class.java)
                        startActivity(intent)
                    }else{
                        val intent = Intent(this, Welcome::class.java)
                        startActivity(intent)
                    }
                }else{
                    student.child(uid).get().addOnSuccessListener {
                        if (it.exists()){
                            val type = it.child("type").value.toString()
                            if (type == "Student"){
                                val intent = Intent(this, MainActivity::class.java)
                                startActivity(intent)
                            }else{
                                val intent = Intent(this, Welcome::class.java)
                                startActivity(intent)
                            }
                        }
                    }
                }
            }
        }else{
            val intent = Intent(this, Welcome::class.java)
            startActivity(intent)
        }


    }
}