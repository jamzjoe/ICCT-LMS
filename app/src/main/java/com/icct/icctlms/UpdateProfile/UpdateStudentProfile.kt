package com.icct.icctlms.UpdateProfile

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.icct.icctlms.Authentication.ForgotPassword
import com.icct.icctlms.Authentication.Login
import com.icct.icctlms.Authentication.Register
import com.icct.icctlms.MainActivity
import com.icct.icctlms.R
import com.icct.icctlms.databinding.ActivityUpdateStudentProfileBinding
import kotlinx.android.synthetic.main.activity_update_student_profile.*
import kotlinx.android.synthetic.main.nav_header.*
import java.io.File

class UpdateStudentProfile : AppCompatActivity() {
        private lateinit var database: DatabaseReference
        private lateinit var storageReference: StorageReference
        private lateinit var uri: Uri
        private lateinit var auth: FirebaseAuth
        private lateinit var dialog : Dialog
        private lateinit var uid: String
    private lateinit var binding: ActivityUpdateStudentProfileBinding

    companion object {
        val IMAGE_REQUEST_CODE = 1_000;
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateStudentProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        retrieveUser()

        showProfile()

        //firebase auth
        auth = FirebaseAuth.getInstance()
        uid = auth.currentUser?.uid.toString()

        binding.uploadProfilePic.setOnClickListener{
        pickImageFromGallery()
        }
        binding.submit.setOnClickListener{
        uploadImage()


}
        binding.resetPass.setOnClickListener{
            val intent = Intent(this, ForgotPassword::class.java)
            startActivity(intent)
        }

//button hide to Un hide
        binding.updateInfo.setOnClickListener{
            newNameContainer.visibility = View.VISIBLE
            newMailContainer.visibility = View.VISIBLE
            newSchoolContainer.visibility = View.VISIBLE
            update_info.visibility = View.GONE
            submit_info.visibility = View.VISIBLE
            skip_info.visibility = View.VISIBLE
        }
        binding.skipInfo.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        binding.submitInfo.setOnClickListener{
            val name = binding.newName.text.toString()
            val email = binding.newMail.text.toString()
            val school = binding.newSchool.text.toString()
            database = FirebaseDatabase.getInstance().getReference("Students")
            val user = mapOf(
                "name" to  name,
                "email" to email,
                "school" to school
            )
            database.child(uid).updateChildren(user).addOnSuccessListener {

                Toast.makeText(this, "Updated successfully!", Toast.LENGTH_SHORT).show()
                MaterialAlertDialogBuilder(this)
                    .setTitle("Log out")
                    .setCancelable(false)
                    .setMessage("Are you sure you want to log-out to view your updates?")
                    .setPositiveButton("Yes, Logout"){dialog, which ->
                        FirebaseAuth.getInstance().signOut()
                        val intent = Intent(this@UpdateStudentProfile, Login::class.java)
                        startActivity(intent)
                    }
                    .setNegativeButton("No"){dialog, which ->
                        Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
                    }.show()
            }.addOnFailureListener{
                Toast.makeText(this, "Failed to update, please try again", Toast.LENGTH_SHORT).show()
            }
        }



    }

    private fun retrieveUser() {
        //database read user info
        auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid
        database = FirebaseDatabase.getInstance().getReference("Students")
        if (uid != null) {
            database.child(uid).get().addOnSuccessListener {
                if (it.exists()) {
                    val name = it.child("name").value
                    val email = it.child("email").value
                    val school = it.child("school").value


                    new_name.setText(name.toString())
                    new_mail.setText(email.toString())
                    new_school.setText(school.toString())

                }

            }
        }
    }

    private fun showProfile() {
            auth = FirebaseAuth.getInstance()
            uid = auth.currentUser?.uid.toString()
            storageReference = FirebaseStorage.getInstance().getReference("Students/$uid")
            val localFile = File.createTempFile("tempImage", "jpg")
            storageReference.getFile(localFile).addOnSuccessListener {
                val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                upload_profile_pic.setImageBitmap(bitmap)

            }
        }




    private fun uploadImage() {

        progressDialogShow()

        storageReference = FirebaseStorage.getInstance().getReference("Students/$uid")
        storageReference.putFile(uri).
        addOnSuccessListener {

            binding.submit.visibility = View.GONE
            tv_profile.text = "Successfully Uploaded"
            Toast.makeText(this@UpdateStudentProfile, "Successfully uploaded!!", Toast.LENGTH_SHORT).show()
            showProfile()
            progressDialogHide()
        }.addOnFailureListener{
            progressDialogHide()
            Toast.makeText(this@UpdateStudentProfile, "Failed to Upload.", Toast.LENGTH_SHORT).show()
        }.addOnCanceledListener {
            Toast.makeText(this@UpdateStudentProfile, "No information updated.", Toast.LENGTH_SHORT).show()
        }

    }
    private fun progressDialogShow(){
        dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_layout)
        dialog.setTitle("Uploading please wait")
        dialog.setCancelable(false)
        dialog.show()
    }
    private fun progressDialogHide(){
        dialog.hide()
    }
    private fun pickImageFromGallery() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Note: ")
            .setCancelable(false)
            .setMessage("Please make sure your image size is not too large or follow this image pixel 200x200.")
            .setPositiveButton("Ok"){dialog, which ->
                Toast.makeText(this, "Okay", Toast.LENGTH_SHORT).show()
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                startActivityForResult(intent, IMAGE_REQUEST_CODE)
            }.setNegativeButton("Cancel"){dialog, which ->
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
            }.show()


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_REQUEST_CODE && resultCode == RESULT_OK) {
            uri = data?.data!!
            binding.uploadProfilePic.setImageURI(uri)
            binding.tvProfile.text = "Click submit to upload."
            binding.submit.visibility = View.VISIBLE
        }
    }
}