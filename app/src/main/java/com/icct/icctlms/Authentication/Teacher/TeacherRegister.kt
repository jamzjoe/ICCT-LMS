package com.icct.icctlms.Authentication.Teacher

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.icct.icctlms.Authentication.Login
import com.icct.icctlms.R
import com.icct.icctlms.Welcome
import com.icct.icctlms.data.StudentProfile
import com.icct.icctlms.data.TeacherProfile
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.activity_register.checkTerms
import kotlinx.android.synthetic.main.activity_register.reg_button
import kotlinx.android.synthetic.main.activity_register.reg_confirm
import kotlinx.android.synthetic.main.activity_register.reg_email
import kotlinx.android.synthetic.main.activity_register.reg_name
import kotlinx.android.synthetic.main.activity_register.reg_pass
import kotlinx.android.synthetic.main.activity_register.txt_login
import kotlinx.android.synthetic.main.activity_teacher_register.*


class TeacherRegister : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var dialog: Dialog
    private lateinit var colors : Array<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teacher_register)

        txt_login.setOnClickListener {
            val intent = Intent(this, TeacherLogin::class.java)
            Intent.FLAG_ACTIVITY_CLEAR_TASK
            Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }


        //chooseSchool
        val school = arrayOf(
            "ICCT San Mateo Campus",
            "ICCT Cainta Main Campus",
            "ICCT Sumulong Campus",
            "ICCT Antipolo Campus",
            "ICCT Angono Campus",
            "ICCT Binangonan Campus",
            "ICCT Cogeo Campus",
            "ICCT Taytay Campus")

        colors = arrayOf(
            "#ca9bf7",
            "#cb99c9",
            "#c1c6fc",
            "#89cff0",
            "#77dd77",
            "#ffb7ce",
            "#ff9899",
            "#ff694f",
            "#ff6961",
            "#fdfd96"
        )
        var selectedSchool = school[selectedItemIndex]

        MaterialAlertDialogBuilder(this)
            .setTitle("Choose your campus")
            .setCancelable(false)
            .setSingleChoiceItems(school, selectedItemIndex) { dialog, which ->
                selectedItemIndex = which
                selectedSchool = school[which]
            }.setPositiveButton("Ok"){dialog, which ->
                Toast.makeText(this, "$selectedSchool Selected", Toast.LENGTH_SHORT).show()
            }.setNegativeButton("Cancel"){dialog, which ->
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
            }.show()
        val finalSchool: String = selectedSchool


        //register
        reg_button.setOnClickListener{
            progressDialogShow()
            if(checkTerms.isChecked){
                when {
                    TextUtils.isEmpty(prof_reg_email.text.toString().trim { it <= ' '}) -> {
                        Toast.makeText(
                            this@TeacherRegister,
                            "Please enter your email.",
                            Toast.LENGTH_SHORT
                        ).show()
                        progressDialogHide()
                    }
                    TextUtils.isEmpty(reg_name.text.toString().trim { it <= ' '}) -> {
                        Toast.makeText(
                            this@TeacherRegister,
                            "Please enter your full_name.",
                            Toast.LENGTH_SHORT
                        ).show()
                        progressDialogHide()
                    }
                    TextUtils.isEmpty(reg_pass.text.toString().trim { it <= ' '}) -> {
                        Toast.makeText(
                            this@TeacherRegister,
                            "Please enter your password.",
                            Toast.LENGTH_SHORT
                        ).show()
                        progressDialogHide()
                    }
                    TextUtils.isEmpty(reg_confirm.text.toString().trim { it <= ' '}) -> {
                        Toast.makeText(
                            this@TeacherRegister,
                            "Please confirm your password.",
                            Toast.LENGTH_SHORT
                        ).show()
                        progressDialogHide()
                    }
                    else -> {
                        val name: String = reg_name.text.toString().trim { it <= ' '}
                        val email: String = prof_reg_email.text.toString().trim { it <= ' '}
                        val password: String = reg_pass.text.toString().trim { it <= ' '}
                        val confirm: String = reg_confirm.text.toString().trim() { it <= ' '}

                        if(password == confirm){



                            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    //if task is successful
                                    if(task.isSuccessful){

                                        Toast.makeText(
                                            this@TeacherRegister,
                                            "Account created successfully.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        progressDialogHide()


                                        //firebase store user info


                                        auth = FirebaseAuth.getInstance()
                                        val uid = auth.currentUser?.uid
                                        databaseReference = FirebaseDatabase.getInstance().getReference("Teachers")
                                        val name = reg_name.text.toString()
                                        val email = prof_reg_email.text.toString()
                                        val account_id = uid.toString()
                                        val type = "Teacher"


                                        val user = TeacherProfile(name, email, finalSchool, account_id, type, colors[random(0, colors.size-1)])

                                        if (uid!=null){
                                            databaseReference.child(uid).setValue(user).addOnCompleteListener{
                                                if (it.isSuccessful){
                                                    val intent = Intent(this, TeacherLogin::class.java)
                                                    startActivity(intent)
                                                    finish()
                                                }
                                            }
                                        }
                                    }else {
                                        progressDialogHide()
                                        //show error message
                                        Toast.makeText(
                                            this@TeacherRegister,
                                            task.exception!!.message.toString(),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                        }else{
                            Toast.makeText(
                                this,
                                "Password not match, try again!",
                                Toast.LENGTH_SHORT
                            ).show()
                            progressDialogHide()}
                        //Create an instance and register user with email and password

                    }
                }
            }else{
                progressDialogHide()
                Toast.makeText(
                    this,
                    "Please make sure to check the terms and conditions.",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }
    }
    override fun onBackPressed() {
        val intent = Intent(this, TeacherLogin::class.java)
        Intent.FLAG_ACTIVITY_CLEAR_TASK
        Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
        super.onBackPressed()

    }

    private fun progressDialogShow(){
        dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_layout)
        dialog.setTitle("Loading please wait")
        dialog.setCancelable(false)
        dialog.show()
    }


    fun random(start: Int, end: Int): Int {
        require(start <= end) { "Illegal Argument" }
        return (start..end).random()
    }

    private fun progressDialogHide(){
        dialog.hide()
    }


    var selectedItemIndex = 0

}