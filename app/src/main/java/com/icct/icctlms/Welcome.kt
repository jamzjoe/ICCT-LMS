package com.icct.icctlms

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.FirebaseDatabase
import com.icct.icctlms.Authentication.Admin.AdminMainActivity
import com.icct.icctlms.Authentication.Login
import com.icct.icctlms.Authentication.Parent.Parent
import com.icct.icctlms.Authentication.Teacher.TeacherLogin
import kotlinx.android.synthetic.main.activity_welcome.*

class Welcome : AppCompatActivity() {
    private lateinit var adminLoginLayout : View
    private lateinit var adminUsername : TextInputEditText
    private lateinit var adminPassword : TextInputEditText
    private lateinit var adminSubmit : MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        val layoutInflater = layoutInflater
        adminLoginLayout = layoutInflater.inflate(R.layout.alert_admin__login, null)
        adminUsername = adminLoginLayout.findViewById(R.id.admin_username)
        adminPassword = adminLoginLayout.findViewById(R.id.admin_password)
        adminSubmit = adminLoginLayout.findViewById(R.id.admin_submit)



        student.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }
        teacher.setOnClickListener {
            val intent = Intent(this, TeacherLogin::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()

        }
        parent_btn.setOnClickListener {
            val intent = Intent(this, Parent::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }
        admin_btn.setOnClickListener{
            adminGrant()
        }


    }

    private fun adminGrant() {
        val createAdmin = FirebaseDatabase.getInstance().getReference("Admin")
        createAdmin.get().addOnSuccessListener {
            if (it.exists()){
                val username = it.child("Username").value.toString()
                val password = it.child("Password").value.toString()
                MaterialAlertDialogBuilder(this)
                    .setTitle("Admin Access")
                    .setView(adminLoginLayout)
                    .show()

                adminSubmit.setOnClickListener{
                    when{
                        TextUtils.isEmpty(adminUsername.text.toString().trim { it <= ' '}) -> {
                            Toast.makeText(this, "Please input username!", Toast.LENGTH_SHORT).show()
                        }
                        TextUtils.isEmpty(adminPassword.text.toString().trim {it <= ' '}) -> {
                            Toast.makeText(this, "Please input password!", Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            val inputUsername = adminUsername.text.toString().trim()
                            val inputPassword = adminPassword.text.toString().trim()
                            when {
                                username != inputUsername -> {
                                    Toast.makeText(this, "Error, admin username doesn't match!", Toast.LENGTH_SHORT).show()
                                }
                                password != inputPassword -> {
                                    Toast.makeText(this, "Error, admin password doesn't match!", Toast.LENGTH_SHORT).show()
                                }
                                else -> {
                                    Toast.makeText(this, "Admin access granted.", Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this, AdminMainActivity::class.java))
                                    finish()
                                }
                            }
                        }
                    }
                }


            }
        }
    }


    override fun onBackPressed() {
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        super.onBackPressed()
        finish()


    }
}

