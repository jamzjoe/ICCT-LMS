package com.icct.icctlms.Authentication.Teacher

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.icct.icctlms.R
import com.icct.icctlms.UpdateProfile.UpdateTeachersProfile
import com.icct.icctlms.Welcome
import com.icct.icctlms.newsAndUpdates.CreateNewsAndUpdates
import com.icct.icctlms.teacherfragments.*
import kotlinx.android.synthetic.main.activity_teacher_main.*
import kotlinx.android.synthetic.main.teacher_nav_header.*
import java.io.File

class TeacherMainActivity : AppCompatActivity() {

    private val homeFragment = TeacherHome()
    private val classFragment = TeacherClass()
    private val messageFragment = TeacherMessage()
    private val notificationFragment = TeacherNotification()
    private val plannerFragment = TeacherPlanner()

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var uid: String
    private lateinit var storageReference: StorageReference
    private var backPressed  = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teacher_main)
        //fragments
        replaceFragment(homeFragment)


        teacher_searchBar.setOnClickListener{
            teacher_searchBar.isIconified = false
        }
        showTopProfile()





        //uid copy


        showProfile()
        //database read user info
        auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid
        database = FirebaseDatabase.getInstance().getReference("Teachers")
        if (uid != null) {
            database.child(uid).get().addOnSuccessListener {
                if (it.exists()) {
                    val name = it.child("name").value
                    val email = it.child("email").value
                    val school = it.child("school").value
                    val accountID = it.child("account_id").value
                    val type = it.child("type").value

                    teacher_prof_name.text = name.toString()
                    teacher_prof_email.text = email.toString()
                    teacher_prof_school.text = "$type from $school"


                    teacher_btn_copy.setOnClickListener{
                        val copyThis = accountID.toString()

                        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clipData = ClipData.newPlainText("text", copyThis)
                        clipboardManager.setPrimaryClip(clipData)
                        Toast.makeText(this, "Account ID copied successfully.", Toast.LENGTH_SHORT).show()
                    }

                    teacher_prof_edit.setOnClickListener{
                        val intent = Intent(this, UpdateTeachersProfile::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                        startActivity(intent)
                    }
                }

            }
        }



        val drawerToggle: DrawerLayout = findViewById(R.id.teacher_drawer_layout)
        val navView: NavigationView = findViewById(R.id.teacher_nav_view)

        toggle = ActionBarDrawerToggle(this, drawerToggle, R.string.open, R.string.close)
        drawerToggle.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)



        //top nav
        teacher_top_nav.setOnNavigationItemSelectedListener {
            when (it.itemId){
                R.id.teacher_top_dashboard -> replaceFragment(homeFragment)
                R.id.teacher_top_notif -> replaceFragment(notificationFragment)
                R.id.teacher_top_messages -> replaceFragment(messageFragment)
                R.id.teacher_top_myclass -> replaceFragment(classFragment)
                R.id.teacher_top_planner -> replaceFragment(plannerFragment)

            }
            true
        }
        //drawer
        navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.teacher_nav_logout -> logOut()
                R.id.teacher_settings -> settings()
                R.id.teacher_delete_account -> deleteAccount()
                R.id.announcement -> openNews()
            }
            true
        }

        teacher_hamburger.setOnClickListener{
            drawerToggle.openDrawer(GravityCompat.START)
        }
    }

    private fun openNews() {
        startActivity(Intent(this, CreateNewsAndUpdates::class.java))
    }


    private fun deleteAccount() {
        MaterialAlertDialogBuilder(this)
            .setMessage("Are you sure you want to delete this account?")
            .setPositiveButton("Yes, sure"){ _, _ ->
                val user = FirebaseAuth.getInstance().currentUser
                val uid = FirebaseAuth.getInstance().currentUser?.uid.toString()
                user?.delete()?.addOnCompleteListener{ task ->
                    if (task.isSuccessful){
                        database = FirebaseDatabase.getInstance().getReference("Teachers")
                        database.child(uid).removeValue().addOnCompleteListener{
                            Toast.makeText(this, "Database deleted", Toast.LENGTH_SHORT).show()
                        }
                        Toast.makeText(this, "Account deleted successfully", Toast.LENGTH_SHORT).show()
                    }
                }
                val intent = Intent(this, TeacherLogin::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Cancel"){_, _ ->
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun helpCenter() {
        Toast.makeText(this, "This features is on development. Stay updated.", Toast.LENGTH_SHORT).show()
    }

    private fun whatsNew() {
        Toast.makeText(this, "This features is on development. Stay updated.", Toast.LENGTH_SHORT).show()
    }

    private fun settings() {
        Toast.makeText(this, "This features is on development. Stay updated.", Toast.LENGTH_SHORT).show()
    }





    private fun replaceFragment(fragment: Fragment){
        if (fragment != null){
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.teacher_fragment_container, fragment)
            transaction.commit()
        }

    }
    private fun goToBackpack() {
        Toast.makeText(this, "This features is not yet implemented. Thank you.", Toast.LENGTH_SHORT).show()
    }



    private fun showProfile() {
        auth = FirebaseAuth.getInstance()
        uid = auth.currentUser?.uid.toString()
        storageReference = FirebaseStorage.getInstance().getReference("Teachers/$uid")
        val localFile = File.createTempFile("tempImage", "jpg")
        storageReference.getFile(localFile).addOnSuccessListener {
            val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
            teacher_profile_pic.setImageBitmap(bitmap)



        }
    }

    private fun showTopProfile() {
        auth = FirebaseAuth.getInstance()
        uid = auth.currentUser?.uid.toString()
        storageReference = FirebaseStorage.getInstance().getReference("Teachers/$uid")
        val localFile = File.createTempFile("tempImage", "jpg")
        storageReference.getFile(localFile).addOnSuccessListener {
            val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
            teacher_top_profile_pic.setImageBitmap(bitmap)



        }

        teacher_top_profile_pic.setOnClickListener{
            val intent = Intent(this, UpdateTeachersProfile::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }
    }

    override fun onResume() {
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        super.onResume()
    }


    private fun logOut() {
        MaterialAlertDialogBuilder(this)
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Okay"){_,_ ->
                Firebase.auth.signOut()
                startActivity(Intent(this, Welcome::class.java))
                finish()
            }.setNegativeButton("Cancel"){_,_ ->

            }.show()
    }


    override fun onBackPressed() {
        if (backPressed + 2000 > System.currentTimeMillis()){
            MaterialAlertDialogBuilder(this)
                .setMessage("Click okay to exit")
                .setNegativeButton("Cancel"){_, _ ->
                }
                .setCancelable(false)
                .setPositiveButton("Okay"){_,_ ->
                    super.onBackPressed()
                }.show()
        }else{
            Toast.makeText(this, "Press again to exit.", Toast.LENGTH_SHORT).show()
        }
        backPressed = System.currentTimeMillis()

    }

}





