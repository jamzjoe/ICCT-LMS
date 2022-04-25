package com.icct.icctlms

import android.app.Dialog
import android.content.*
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.icct.icctlms.Authentication.Login
import com.icct.icctlms.UpdateProfile.UpdateStudentProfile
import com.icct.icctlms.fragments.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.nav_header.*
import java.io.File
import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import java.io.ByteArrayOutputStream
import android.content.SharedPreferences
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_teacher_main.*


class MainActivity : AppCompatActivity() {


    private val homeFragment = Home()
    private val classFragment = Class()
    private val messageFragment = Message()
    private val notificationFragment = Notification()
    private val plannerFragment = Planner()
    private lateinit var dialog: Dialog
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var uid: String
    private lateinit var storageReference: StorageReference
    private var backPressed  = 0L
    private lateinit var nav : BottomNavigationView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        uid = Firebase.auth.currentUser?.uid.toString()
        //fragments
        nav = findViewById(R.id.top_nav)
        badgeNumber()
        replaceFragment(homeFragment)

        searchBar.setOnClickListener{
            searchBar.isIconified = false
        }
        showTopProfile()
        back.setOnClickListener{
            top_logo.visibility = View.VISIBLE
            back.visibility = View.GONE
            search_holder.visibility = View.GONE
        }




        //uid copy


        showProfile()
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
                    val accountID = it.child("account_id").value
                    val type = it.child("type").value

                    prof_name.text = name.toString()
                    prof_email.text = email.toString()
                    prof_school.text = "$type from $school"


                    btn_copy.setOnClickListener{
                        val copyThis = accountID.toString()

                        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clipData = ClipData.newPlainText("text", copyThis)
                        clipboardManager.setPrimaryClip(clipData)
                        Toast.makeText(this, "Account ID copied successfully.", Toast.LENGTH_SHORT).show()
                    }

                    prof_edit.setOnClickListener{
                        val intent = Intent(this, UpdateStudentProfile::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                        startActivity(intent)
                    }
                }

            }
        }



        val drawerToggle: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)

        toggle = ActionBarDrawerToggle(this, drawerToggle, R.string.open, R.string.close)
        drawerToggle.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)



        //top nav
        top_nav.setOnItemSelectedListener {
            when (it.itemId){
                R.id.top_dashboard -> replaceFragment(homeFragment)
                R.id.top_notif -> replaceFragment(notificationFragment)
                R.id.top_messages -> replaceFragment(messageFragment)
                R.id.top_myclass -> replaceFragment(classFragment)
                R.id.top_planner -> replaceFragment(plannerFragment)

            }
            true
        }
        //drawer
        navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_backpack -> goToBackpack()
                R.id.nav_logout -> logOut()
                R.id.whats_new -> whatsNew()
                R.id.help_center -> helpCenter()
                R.id.settings -> settings()
                R.id.delete_account -> deleteAccount()
            }
            true
        }

        hamburger.setOnClickListener{
            drawerToggle.openDrawer(GravityCompat.START)
        }
    }

    private fun badgeNumber() {
        val notificationReference = FirebaseDatabase.getInstance().getReference("Notifications").child("Student").child(uid.toString())
        notificationReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val count = snapshot.childrenCount
                    val teacherNotifBadge = nav.getOrCreateBadge(R.id.top_notif)
                    teacherNotifBadge.isVisible = true
                    badgeNumber()

                }else{
                    val badge = nav.getBadge(R.id.top_notif)
                    badge?.clearNumber()
                    badge?.isVisible = false
                    badgeNumber()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }


    private fun deleteAccount() {
        MaterialAlertDialogBuilder(this)
            .setMessage("Are you sure you want to delete this account?")
            .setPositiveButton("Yes, sure"){ _, _ ->
                val user = FirebaseAuth.getInstance().currentUser
                val uid = FirebaseAuth.getInstance().currentUser?.uid.toString()
                user?.delete()?.addOnCompleteListener{ task ->
                    if (task.isSuccessful){
                        database = FirebaseDatabase.getInstance().getReference("Students")
                        database.child(uid).removeValue().addOnCompleteListener{
                            Toast.makeText(this, "Database deleted",Toast.LENGTH_SHORT).show()
                            finish()
                            Toast.makeText(this, "Account deleted successfully", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                val intent = Intent(this, Login::class.java)
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
        startActivity(Intent(this, SchoolAnnouncement::class.java))
    }

    private fun settings() {
        Toast.makeText(this, "This features is on development. Stay updated.", Toast.LENGTH_SHORT).show()
    }





    private fun replaceFragment(fragment: Fragment){
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.commit()

    }
    private fun goToBackpack() {
        Toast.makeText(this, "This features is not yet implemented. Thank you.", Toast.LENGTH_SHORT).show()
    }



    private fun showProfile() {

        auth = FirebaseAuth.getInstance()
        uid = auth.currentUser?.uid.toString()
        storageReference = FirebaseStorage.getInstance().getReference("Students/$uid")
        val localFile = File.createTempFile("tempImage", "jpg")
        storageReference.getFile(localFile).addOnSuccessListener {
            val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
            profile_pic.setImageBitmap(bitmap)



        }
    }

    private fun showTopProfile() {
        auth = FirebaseAuth.getInstance()
        uid = auth.currentUser?.uid.toString()
        storageReference = FirebaseStorage.getInstance().getReference("Students/$uid")
        val localFile = File.createTempFile("tempImage", "jpg")
        storageReference.getFile(localFile).addOnSuccessListener {
            val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
            top_profile_pic.setImageBitmap(bitmap)


        }

        top_profile_pic.setOnClickListener{
            val intent = Intent(this, UpdateStudentProfile::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }
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

}