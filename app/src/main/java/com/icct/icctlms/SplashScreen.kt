package com.icct.icctlms

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.icct.icctlms.Authentication.Teacher.TeacherMainActivity

@SuppressLint("CustomSplashScreen")
class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

Handler(Looper.myLooper()!!).postDelayed({

}, 2000)


        checkConnectivity()
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

    private fun checkConnectivity() {
        val manager = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = manager.activeNetworkInfo

        if (null == activeNetwork) {
            val dialogBuilder = AlertDialog.Builder(this)
            // set message of alert dialog
            dialogBuilder.setMessage("Make sure that WI-FI or mobile data is turned on, then try again.")
                // if the dialog is cancelable
                .setCancelable(false)
                // positive button text and action
                .setPositiveButton("Retry", DialogInterface.OnClickListener { dialog, id ->
                    recreate()
                })
                // negative button text and action
                .setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, id ->
                    finish()
                })

            // create dialog box
            val alert = dialogBuilder.create()
            // set title for alert dialog box
            alert.setTitle("No Internet Connection")
            alert.setIcon(R.drawable.logo_plain)
            // show alert dialog
            alert.show()
        }else{
            currentUser()
        }
    }
}