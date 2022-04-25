package com.icct.icctlms.Authentication.Teacher

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.icct.icctlms.Authentication.ForgotPassword
import com.icct.icctlms.R
import com.icct.icctlms.Welcome
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_login.forgot
import kotlinx.android.synthetic.main.activity_login.log_email
import kotlinx.android.synthetic.main.activity_login.log_password
import kotlinx.android.synthetic.main.activity_login.switchRemember
import kotlinx.android.synthetic.main.activity_login.txt_register
import kotlinx.android.synthetic.main.activity_teacher_login.*

class TeacherLogin : AppCompatActivity() {
    private lateinit var databaseTeachers : DatabaseReference
    private lateinit var dialog: Dialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teacher_login)

        showSaveInput()

        //remember save input
        switchRemember.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                saveCheckState()
            }
        }

        //forgot password
        forgot.setOnClickListener{
            val intent = Intent(this, ForgotPassword::class.java)
            Intent.FLAG_ACTIVITY_CLEAR_TASK
            Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }

        //login button
        teacher_log_button.setOnClickListener{
            progressDialogShow()
            val email: String = prof_log_email.text.toString().trim { it <= ' '}
            val password: String = prof_log_password.text.toString().trim { it <= ' '}

            val saveLogin = getSharedPreferences("SAVE_LOGIN_TEACHER", Context.MODE_PRIVATE)
            val editor = saveLogin.edit()
            editor.putString("email", email)
            editor.apply()


            when {
                TextUtils.isEmpty(prof_log_email.text.toString().trim { it <= ' '}) -> {
                    Toast.makeText(this,
                        "Please enter your email.",
                        Toast.LENGTH_SHORT).show()
                    progressDialogHide()
                }
                TextUtils.isEmpty(prof_log_password.text.toString().trim { it <= ' '}) -> {
                    Toast.makeText(this,
                        "Please enter your password.",
                        Toast.LENGTH_SHORT).show()
                    progressDialogHide()
                }



                else ->
                    //sign in with email and pass
                    FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful){
                                val user = FirebaseAuth.getInstance().currentUser
                                val uid = FirebaseAuth.getInstance().currentUser?.uid.toString()
                                var type: String = ""

                                databaseTeachers = FirebaseDatabase.getInstance().getReference("Teachers")
                                databaseTeachers.child(uid).get().addOnSuccessListener{
                                    if (it.exists()){
                                        type = it.child("type").value.toString()
                                    }
                                    if (type != "Teacher"){
                                        MaterialAlertDialogBuilder(this)
                                            .setMessage("You are not registered as teacher.")
                                            .setNegativeButton("Okay"){_, _ ->
                                            }
                                            .show()

                                    }else{
                                        if (user?.isEmailVerified == true){
                                            progressDialogHide()
                                            Intent.FLAG_ACTIVITY_CLEAR_TASK
                                            Intent.FLAG_ACTIVITY_CLEAR_TOP
                                            val intent = Intent(this, TeacherMainActivity::class.java)
                                            Toast.makeText(this, "Account is verified, login successfully.", Toast.LENGTH_SHORT).show()
                                            startActivity(intent)
                                            finish()

                                        }else{
                                            //send email verification
                                            user?.sendEmailVerification()
                                            MaterialAlertDialogBuilder(this)
                                                .setTitle("For security purpose")
                                                .setMessage("Please check your email and re-login your account after getting verified. Thank you!")
                                                .setPositiveButton("Okay"){dialog, which ->
                                                    Toast.makeText(this, "Okay", Toast.LENGTH_SHORT).show()

                                                }.setNegativeButton("Cancel"){dialog, which ->
                                                    Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
                                                }.setCancelable(false)
                                                .show()
                                            Toast.makeText(this, "Verification email sent successfully!", Toast.LENGTH_SHORT).show()
                                            progressDialogHide()
                                        }
                                    }
                                }
                                progressDialogHide()

                                //if user is email verified




                            }else {
                                Toast.makeText(
                                    this@TeacherLogin,
                                    task.exception!!.message.toString(),
                                    Toast.LENGTH_SHORT
                                ).show()
                                progressDialogHide()
                            }
                        }
            }



        }


        txt_register.setOnClickListener{
            val intent = Intent(this, TeacherRegister::class.java)
            Intent.FLAG_ACTIVITY_CLEAR_TASK
            Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }



    }

    private fun progressDialogShow(){
        dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_layout)
        dialog.setTitle("Loading please wait")
        dialog.setCancelable(false)
        dialog.show()
    }
    private fun progressDialogHide(){
        dialog.hide()
    }

    private fun showSaveInput() {
        val sharedPreferences = getSharedPreferences("SAVE_STATE_TEACHER", MODE_PRIVATE)
        val email = sharedPreferences.getString("email", "")
        val name = sharedPreferences.getString("name", "")

        prof_log_email.setText("$email")
    }

    private fun saveCheckState() {
        val sharedPreferences = getSharedPreferences("SAVE_STATE_TEACHER", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val email = prof_log_email.text.toString()
        editor.putString("email", email)
        editor.apply()
    }

    override fun onBackPressed() {
        val intent = Intent(this, Welcome::class.java)
        Intent.FLAG_ACTIVITY_CLEAR_TASK
        Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
        super.onBackPressed()

    }
    }
