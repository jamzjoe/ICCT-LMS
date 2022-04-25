package com.icct.icctlms

import android.app.Dialog
import android.app.ProgressDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.webkit.URLUtil
import android.widget.PopupMenu
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.icct.icctlms.room.*
import kotlinx.android.synthetic.main.activity_room.*
import kotlinx.android.synthetic.main.activity_room.room_top_nav
import kotlinx.android.synthetic.main.activity_student_room.*
import kotlinx.android.synthetic.main.activity_student_room.card_holder
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter


class StudentRoomActivity : AppCompatActivity() {
    private lateinit var databaseReference : DatabaseReference
    private lateinit var link : String
    private val roomPost = StudentRoomPost()
    private val roomMembers = StudentRoomMembers()
    private val roomFolder = StudentRoomFolders()
    private var roomName = String()
    private var uid = String()
    private var roomID = String()
    private var roomCode = String()
    private var section = String()
    private var type = String()
    private var name = String()
    private var sortKey = String()
    private lateinit var pattern : String
    private lateinit var finalDate : String
    private lateinit var finalHour : String
    private lateinit var hour : String
    private lateinit var roomType : String
    private lateinit var dialog : Dialog

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_room)
        roomName = intent.getStringExtra("student_room_name").toString()
        uid = FirebaseAuth.getInstance().currentUser?.uid.toString()
        roomID = intent.getStringExtra("student_roomID").toString()
        roomCode = intent.getStringExtra("student_roomCode").toString()
        section = intent.getStringExtra("student_section").toString()
        type = intent.getStringExtra("student_type").toString()
        name = intent.getStringExtra("student_name").toString()
        roomType = intent.getStringExtra("student_room_type").toString()

        pattern = "hh:mm a"
        val now = LocalDateTime.now()
        sortKey = now.toMillis().toString()
        finalDate = LocalDate.now().toString()
        hour = LocalTime.now().toString()
        finalHour = hour.format(DateTimeFormatter.ofPattern(pattern))

        announce()
        replaceFragment(roomPost)
        navClick()
        attendanceLink()
        zoomLink()
        replaceText()
        showMessage(roomID)
        transferDataToFragments()
        needHelp()
        clickBannerToCopyRoomID()
        settingsOnClick()


    }

    private fun announce() {
    }

    private fun settingsOnClick() {
        student_settings.setOnClickListener{
            showPopUpSettings(student_settings)
        }
    }

    private fun showPopUpSettings(view : View) {
val popUp = PopupMenu(this, view)
        popUp.inflate(R.menu.settings_menu)

        popUp.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item: MenuItem? ->

            when (item!!.itemId) {
                R.id.leave_room -> leaveFunction()
            }

            true
        })

        popUp.show()
    }

    private fun leaveFunction() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Leave room?")
            .setMessage("Are you sure you want to leave in this room?")
            .setPositiveButton("SURE"){_,_ ->
                //database delete
                //start intent
                val deleteClassSelf = FirebaseDatabase.getInstance().getReference("JoinClass").child(uid)
                deleteClassSelf.child(roomID).removeValue().addOnSuccessListener {
                    Toast.makeText(this, "Leave successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                    startActivity(Intent(this, MainActivity::class.java))
                }

            }
            .setNegativeButton("CANCEL"){_,_ ->

            }
            .show()
    }

    private fun needHelp() {
        student_help_button.setOnClickListener{
            showDialog("Need help?", "To delete a post, simply slide it to the right.\n" +
                    "If there isn't a link associated to the button, ask your teacher.")
        }
    }


    private fun attendanceLink() {
       student_btn_att.setOnClickListener{
           if (roomType == "Class"){
               databaseReference = FirebaseDatabase.getInstance().getReference("Public Class").child(roomID)
               databaseReference.child("Links").get().addOnSuccessListener {
                   if (it.exists()){
                       link = it.child("attendanceLink").value.toString()
                       if (link.isEmpty()){
                           showDialog("Reminder", "Please wait till your instructor adds a link to this room if there isn't one already attached.")
                       }else{
                           MaterialAlertDialogBuilder(this)
                               .setTitle("NOTE")
                               .setMessage("Are you certain you want to continue to the attendance sheet?")
                               .setPositiveButton("OK"){_,_ ->
                                   if (URLUtil.isValidUrl(link.trim())){
                                       val i = Intent(Intent.ACTION_VIEW)
                                       i.data = Uri.parse(link)
                                       startActivity(i)
                                   }else{
                                       showDialog("", "$link is not a valid URL")
                                   }
                               }.setNegativeButton("Cancel"){_,_ ->

                               }.show()
                       }
                   }else{
                       showDialog("Reminder", "Please wait till your instructor adds a link to this room if there isn't one already attached.")
                   }
               }.addOnFailureListener{

               }
           }else if (roomType == "Group"){
               val databaseGroup = FirebaseDatabase.getInstance().getReference("Public Group").child(roomID)
               databaseGroup.child("Links").get().addOnSuccessListener {
                   if (it.exists()){
                       link = it.child("attendanceLink").value.toString()
                       if (link.isEmpty()){
                           showDialog("Reminder", "Please wait till your instructor adds a link to this room if there isn't one already attached.")
                       }else{
                           MaterialAlertDialogBuilder(this)
                               .setTitle("NOTE")
                               .setMessage("Are you certain you want to continue to the attendance sheet?")
                               .setPositiveButton("OK"){_,_ ->
                                   if (URLUtil.isValidUrl(link.trim())){
                                       val i = Intent(Intent.ACTION_VIEW)
                                       i.data = Uri.parse(link)
                                       startActivity(i)
                                   }else{
                                       showDialog("", "$link is not a valid URL")
                                   }
                               }.setNegativeButton("Cancel"){_,_ ->

                               }.show()
                       }
                   }else{
                       showDialog("Reminder", "Please wait till your instructor adds a link to this room if there isn't one already attached.")
                   }
               }.addOnFailureListener{

               }
           }
           }








    }
    private fun clickBannerToCopyRoomID() {
        card_holder.setOnClickListener{
            val clipboardManager = this.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("text", roomID)
            clipboardManager.setPrimaryClip(clipData)
            Toast.makeText(this, "Security code copied successfully.", Toast.LENGTH_SHORT).show()
        }
    }


    private fun showMessage(message: String){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun zoomLink() {
        student_btn_zoom.setOnClickListener{
if (roomType == "Class"){
    databaseReference = FirebaseDatabase.getInstance().getReference("Public Class").child(roomID)
    databaseReference.child("Links").get().addOnSuccessListener {
        if (it.exists()){
            link = it.child("zoomLink").value.toString()
            if (link.isEmpty()){
                showDialog("Reminder", "Please wait till your instructor adds a link to this room if there isn't one already attached.")
            }else{
                MaterialAlertDialogBuilder(this)
                    .setTitle("NOTE")
                    .setMessage("Are you certain you want to continue to the virtual meeting?")
                    .setPositiveButton("OK"){_,_ ->
                        if (URLUtil.isValidUrl(link.trim())){
                            val i = Intent(Intent.ACTION_VIEW)
                            i.data = Uri.parse(link)
                            startActivity(i)
                        }else{
                            showDialog("", "$link is not a valid URL")
                        }
                    }.setNegativeButton("Cancel"){_,_ ->

                    }.show()
            }
        }else{
            showDialog("Reminder", "Please wait till your instructor adds a link to this room if there isn't one already attached.")
        }
    }.addOnFailureListener{

    }
}else if (roomType == "Group"){
    databaseReference = FirebaseDatabase.getInstance().getReference("Public Group").child(roomID)
    databaseReference.child("Links").get().addOnSuccessListener {
        if (it.exists()){
            link = it.child("zoomLink").value.toString()
            if (link.isEmpty()){
                showDialog("Reminder", "Please wait till your instructor adds a link to this room if there isn't one already attached.")
            }else{
                MaterialAlertDialogBuilder(this)
                    .setTitle("NOTE")
                    .setMessage("Are you certain you want to continue to the virtual meeting?")
                    .setPositiveButton("OK"){_,_ ->
                        if (URLUtil.isValidUrl(link.trim())){
                            val i = Intent(Intent.ACTION_VIEW)
                            i.data = Uri.parse(link)
                            startActivity(i)
                        }else{
                            showDialog("", "$link is not a valid URL")
                        }
                    }.setNegativeButton("Cancel"){_,_ ->

                    }.show()
            }
        }else{
            showDialog("Reminder", "Please wait till your instructor adds a link to this room if there isn't one already attached.")
        }
    }.addOnFailureListener{

    }
}



        }
    }

    private fun navClick() {
        room_top_nav.setOnItemSelectedListener {
            when(it.itemId){
                R.id.room_post -> replaceFragment(roomPost)
                R.id.room_members -> replaceFragment(roomMembers)
                R.id.room_folders -> replaceFragment(roomFolder)
            }
            true
        }
    }
    private fun replaceText() {
        val databaseClass = FirebaseDatabase.getInstance().getReference("Public Class")
        databaseReference = FirebaseDatabase.getInstance().getReference("Public Group")
        databaseReference.child(roomID).get().addOnSuccessListener { it ->
            if (it.exists()) {
                val roomNameDataBase = it.child("subjectTitle").value.toString()
                val roomSectionDatabase = it.child("section").value.toString()
                student_room_name.text = roomNameDataBase
                student_section_txt.text = roomSectionDatabase
            } else {
                databaseClass.child(roomID).get().addOnSuccessListener {
                    if (it.exists()) {
                        val roomNameDataBase = it.child("subjectTitle").value.toString()
                        val roomSectionDatabase = it.child("section").value.toString()
                        student_room_name.text = roomNameDataBase
                        student_section_txt.text = roomSectionDatabase
                    }
                }
            }
        }
    }

    private fun transferDataToFragments() {
        //transfer data to fragments
        val bundle = Bundle()
        bundle.putString("student_name_", roomName)
        bundle.putString("student_uid_", uid)
        bundle.putString("student_roomID_", roomID)
        bundle.putString("student_roomCode_", roomCode)
        bundle.putString("student_section_", section)
        bundle.putString("student_type_", type)
        bundle.putString("student_person_name", name)
        bundle.putString("student_room_type_", roomType)
        roomPost.arguments = bundle
        roomMembers.arguments = bundle
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun LocalDateTime.toMillis(zone: ZoneId = ZoneId.systemDefault()) = atZone(zone)?.toInstant()?.toEpochMilli()


    private fun replaceFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.room_fragment_container, fragment)
        transaction.commit()
    }

    private fun progressDialogShow(){
        dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_layout)
        dialog.setTitle("Loading please wait")
        dialog.setCancelable(false)
        dialog.show()
    }
    private fun hide(){
        dialog.hide()
    }

    private fun showDialog(dialogTitle : String, message: String){
        MaterialAlertDialogBuilder(this)
            .setTitle(dialogTitle)
            .setMessage(message)
            .setPositiveButton("OK"){_,_ ->

            }.show()
    }
}