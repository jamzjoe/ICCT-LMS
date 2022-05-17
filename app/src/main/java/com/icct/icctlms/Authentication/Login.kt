package com.icct.icctlms.Authentication

import android.app.ActivityOptions
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
import com.icct.icctlms.MainActivity
import com.icct.icctlms.Welcome
import com.icct.icctlms.databinding.ActivityLoginBinding
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_update_student_profile.*

class Login : AppCompatActivity() {
    private lateinit var databaseStudents: DatabaseReference
    private val dialog = com.icct.icctlms.tools.Dialog()
    private lateinit var binding : ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        showSaveInput()

        switchRemember.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                saveCheckState()
            }
        }

        //forgot password
        forgot.setOnClickListener{
            val intent = Intent(this, ForgotPassword::class.java)
            startActivity(intent)
        }

        //login button
        binding.logButton.setOnClickListener{
            dialog.progressDialogShow(this, "Authenticating...")
            val email: String = binding.logEmail.text.toString().trim { it <= ' '}
            val password: String = binding.logPassword.text.toString().trim { it <= ' '}

            val saveLogin = getSharedPreferences("SAVE_LOGIN", Context.MODE_PRIVATE)
            val editor = saveLogin.edit()
            editor.putString("email", email)
                editor.apply()


            when {
                TextUtils.isEmpty(binding.logEmail.text.toString().trim { it <= ' '}) -> {
                    Toast.makeText(this,
                        "Please enter your email.",
                        Toast.LENGTH_SHORT).show()
                    dialog.progressDialogHide()
                }
                TextUtils.isEmpty(binding.logPassword.text.toString().trim { it <= ' '}) -> {
                    Toast.makeText(this,
                        "Please enter your password.",
                        Toast.LENGTH_SHORT).show()
                    dialog.progressDialogHide()
                }



                else ->
                    //sign in with email and pass
                    FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful){
                                val user = FirebaseAuth.getInstance().currentUser
                                val uid = FirebaseAuth.getInstance().currentUser?.uid.toString()
                                var type = ""

                                databaseStudents = FirebaseDatabase.getInstance().getReference("Students")
                                databaseStudents.child(uid).get().addOnSuccessListener{
                                    if (it.exists()){
                                        type = it.child("type").value.toString()
                                    }
                                    if (type != "Student"){
                                        MaterialAlertDialogBuilder(this)
                                            .setMessage("You are not registered as student")
                                            .setNegativeButton("Okay"){_, _ ->
                                            }
                                            .show()

                                    }else{
                                        if (user?.isEmailVerified == true){
                                            dialog.progressDialogHide()
                                            val intent = Intent(this, MainActivity::class.java)
                                            Toast.makeText(this, "Account is verified, login successfully.", Toast.LENGTH_SHORT).show()
                                            startActivity(intent)
                                            finish()

                                        }else{
                                            //send email verification
                                            user?.sendEmailVerification()
                                            MaterialAlertDialogBuilder(this)
                                                .setTitle("For security purpose")
                                                .setMessage("Please check your email and re-login your account after getting verified. Thank you!")
                                                .setPositiveButton("Okay"){_, _ ->
                                                    Toast.makeText(this, "Okay", Toast.LENGTH_SHORT).show()

                                                }.setNegativeButton("Cancel"){_, _ ->
                                                    Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
                                                }.setCancelable(false)
                                                .show()
                                            Toast.makeText(this, "Verification email sent successfully!", Toast.LENGTH_SHORT).show()
                                            dialog.progressDialogHide()
                                        }
                                    }
                                }
                                dialog.progressDialogHide()

                                //if user is email verified




                            }else {
                                Toast.makeText(
                                    this@Login,
                                    task.exception!!.message.toString(),
                                    Toast.LENGTH_SHORT
                                ).show()
                                dialog.progressDialogHide()
                            }
                        }
            }



        }


        txt_register.setOnClickListener{
            val intent = Intent(this, Register::class.java)

            startActivity(intent)
            finish()
        }



    }

    private fun showSaveInput() {
        val sharedPreferences = getSharedPreferences("SAVE_STATE", MODE_PRIVATE)
        val email = sharedPreferences.getString("email", "")

        binding.logEmail.setText("$email")
    }

    private fun saveCheckState() {
        val sharedPreferences = getSharedPreferences("SAVE_STATE", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val email = binding.logEmail.text.toString()
        editor.putString("email", email)
        editor.apply()
    }





    override fun onBackPressed() {
        val intent = Intent(this, Welcome::class.java)
        startActivity(intent)
        finish()
        super.onBackPressed()

    }





}