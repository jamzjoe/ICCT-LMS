package com.icct.icctlms.Authentication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.icct.icctlms.R
import kotlinx.android.synthetic.main.activity_forgot_password.*

class ForgotPassword : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

    submit_button.setOnClickListener{
        val email : String = forgot_et.text.toString().trim{ it <= ' '}

        if(email.isEmpty()){
            Toast.makeText(this, "Please enter registered email address!", Toast.LENGTH_SHORT).show()
        }else{
            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener{task->
                    if (task.isSuccessful){
                        Toast.makeText(this, "Email sent successfully to reset your password. Please check your email.", Toast.LENGTH_SHORT).show()
                    finish()
                    }else{
                        Toast.makeText(this,    task.exception!!.message.toString(), Toast.LENGTH_SHORT).show()
                    }

                }
        }
    }
    }
}