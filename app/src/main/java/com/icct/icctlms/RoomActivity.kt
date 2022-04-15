package com.icct.icctlms

import android.app.AlertDialog
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.icct.icctlms.Authentication.Teacher.RoomMemberRequest
import com.icct.icctlms.Authentication.Teacher.TeacherMainActivity
import com.icct.icctlms.data.LinkData
import com.icct.icctlms.data.State
import com.icct.icctlms.room.TeachersRoomFolders
import com.icct.icctlms.room.TeachersRoomMembers
import com.icct.icctlms.room.TeachersRoomPost
import kotlinx.android.synthetic.main.activity_create_teacher_post.*
import kotlinx.android.synthetic.main.activity_room.*
import kotlinx.android.synthetic.main.activity_room.room_top_nav
import kotlinx.android.synthetic.main.settings_post_layout.*
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class RoomActivity : AppCompatActivity() {
    private lateinit var databaseReference: DatabaseReference
    private lateinit var roomID : String
    private lateinit var roomType : String
    private val roomPost = TeachersRoomPost()
    private val roomMembers = TeachersRoomMembers()
    private val roomFolder = TeachersRoomFolders()
    private lateinit var uid : String
    private lateinit var link : String
    private lateinit var finalDate : String
    private lateinit var sortKey : String
    private lateinit var finalHour : String
    private lateinit var hour : String
    private lateinit var today : Calendar
    private lateinit var date : String
    private lateinit var roomName : String
    private lateinit var day : String
    private lateinit var settings : View
    private lateinit var switch : ToggleButton
    private lateinit var memberRequest : TextView
    private lateinit var memberCountText : TextView
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room)

        val now = LocalDateTime.now()
        today = Calendar.getInstance()
        sortKey = now.toMillis().toString()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            date = SimpleDateFormat("MMMM d, y").format(today.time).toString()
        }
        day = SimpleDateFormat("EEEE").format(today.time).toString()
        finalDate = "$day, $date"
        val time = LocalTime.now()
        val pattern = "hh:mm a"
        hour = LocalTime.now().toString()
        finalHour = time.format(DateTimeFormatter.ofPattern(pattern))


        val inflater = layoutInflater
        settings = inflater.inflate(R.layout.settings_post_layout, null)
        switch = settings.findViewById<ToggleButton>(R.id.settings_switch)
        memberRequest = settings.findViewById(R.id.member_request_btn)
        memberCountText = settings.findViewById(R.id.member_count_txt)



        uid = FirebaseAuth.getInstance().currentUser?.uid.toString()
        roomID = intent.getStringExtra("roomID").toString()
        roomType = intent.getStringExtra("roomType").toString()
        roomName = intent.getStringExtra("roomName").toString()

        showCount()
        showHelp()
        navClick()
        replaceFragment(roomPost)
        replaceText()
        transferData()
        addLinkButton()
        clickLink()
        clickBannerToCopyRoomID()
        setSettings()
        readState()
        deleteRoom()

    }

    private fun showCount() {
        if(roomType == "Class"){

            val membersCount = FirebaseDatabase.getInstance().getReference("Public Class").child(roomID)
            membersCount.child("Request").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        val count = snapshot.childrenCount

                        memberCountText.text = count.toString()
                        if(count > 0){
                            alert_.visibility = View.VISIBLE
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
        }else{

            val membersCount = FirebaseDatabase.getInstance().getReference("Public Group").child(roomID)
            membersCount.child("Request").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        val count = snapshot.childrenCount

                        memberCountText.text = count.toString()
                        if(count > 0){
                            alert_.visibility = View.VISIBLE
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
        }
        }


    private fun deleteRoom() {

    }

    private fun firstState() {
        var data = State()
        if (roomType == "Class"){
            data = State("false")
            val databaseReference =
                FirebaseDatabase.getInstance().getReference("Public Class")
                    .child(roomID)
            databaseReference.child("Privacy").setValue(data)
        }else if (roomType == "Group"){
            data = State("false")
            val databaseReference =
                FirebaseDatabase.getInstance().getReference("Public Group")
                    .child(roomID)
            databaseReference.child("Privacy").setValue(data)
        }
    }

    private fun trueState() {
        var data = State()
        if (roomType == "Class"){
            data = State("true")
            val databaseReference =
                FirebaseDatabase.getInstance().getReference("Public Class")
                    .child(roomID)
            databaseReference.child("Privacy").setValue(data)
        }else if (roomType == "Group"){
            data = State("true")
            val databaseReference =
                FirebaseDatabase.getInstance().getReference("Public Group")
                    .child(roomID)
            databaseReference.child("Privacy").setValue(data)
        }
    }

    private fun readState() {
        if (roomType == "Class"){
            databaseReference = FirebaseDatabase.getInstance().getReference("Public Class").child(roomID)
            databaseReference.child("Privacy").get().addOnSuccessListener {
                if (it.exists()){
                    val privacy = it.child("privacy").value.toString()
                    if (privacy == "true"){
                        switch.isChecked = true
                    }
                }else{
                    switch.isChecked = false
                }
            }
        }else if (roomType == "Group"){
            databaseReference = FirebaseDatabase.getInstance().getReference("Public Group").child(roomID)
            databaseReference.child("Privacy").get().addOnSuccessListener {
                if (it.exists()){
                    val privacy = it.child("privacy").value.toString()
                    if (privacy == "true"){
                        switch.isChecked = true
                    }
                }else{
                    switch.isChecked = false
                }
            }
        }
    }


    private fun setSettings() {
       memberRequest.setOnClickListener{
           val intent = Intent(this, RoomMemberRequest::class.java)
           intent.putExtra("member_room_id", roomID)
           intent.putExtra("member_room_type", roomType)
            startActivity(intent)
        }
        settings_post.setOnClickListener {
            val alertDialogBuilder: MaterialAlertDialogBuilder = MaterialAlertDialogBuilder(this)
            alertDialogBuilder.setView(settings)
            alertDialogBuilder.setOnCancelListener{
                val parent : ViewGroup = settings.parent as ViewGroup
                parent.removeView(settings)
            }
            alertDialogBuilder.setPositiveButton("Done") { _, _ ->
                var data = State()
                if (roomType == "Class") {
                    val parent : ViewGroup = settings.parent as ViewGroup
                    parent.removeView(settings)
                        if (switch.isChecked) {
                            trueState()
                        } else {
                            firstState()
                        }


                } else if (roomType == "Group") {
                    val parent : ViewGroup = settings.parent as ViewGroup
                    parent.removeView(settings)
                        if (switch.isChecked) {
                            trueState()
                        } else{
                            firstState()
                        }


                }
            }
            alertDialogBuilder.setNeutralButton("DELETE THIS ROOM"){_,_ ->
                val parent : ViewGroup = settings.parent as ViewGroup
                parent.removeView(settings)
                MaterialAlertDialogBuilder(this)
                    .setMessage("Are you sure you want to delete this room?")
                    .setPositiveButton("CONFIRM") { _, _ ->
                       if (roomType == "Class"){
                           val deleteClassSelf =
                               FirebaseDatabase.getInstance().getReference("Class").child(uid)
                           deleteClassSelf.child(roomID).removeValue().addOnSuccessListener {
                               val deleteClassGlobal =
                                   FirebaseDatabase.getInstance().getReference("Public Class")
                               deleteClassGlobal.child(roomID).removeValue().addOnSuccessListener {
                                   val deleteClassPost =
                                       FirebaseDatabase.getInstance().getReference("Class Post")
                                   deleteClassPost.child("Room ID: $roomID").removeValue()
                                       .addOnSuccessListener {
                                           Toast.makeText(this, "Deleted successfully.", Toast.LENGTH_SHORT).show()
                                           startActivity(Intent(this, TeacherMainActivity::class.java))
                                       }
                               }
                           }
                       }else{
                           val deleteClassSelf =
                               FirebaseDatabase.getInstance().getReference("Group").child(uid)
                           deleteClassSelf.child(roomID).removeValue().addOnSuccessListener {
                               val deleteClassGlobal =
                                   FirebaseDatabase.getInstance().getReference("Public Group")
                               deleteClassGlobal.child(roomID).removeValue().addOnSuccessListener {
                                   val deleteClassPost =
                                       FirebaseDatabase.getInstance().getReference("Group Post")
                                   deleteClassPost.child("Room ID: $roomID").removeValue()
                                       .addOnSuccessListener {
                                           Toast.makeText(this, "Deleted successfully.", Toast.LENGTH_SHORT).show()
                                           startActivity(Intent(this, TeacherMainActivity::class.java))
                                       }
                               }
                           }
                       }
                    }
                    .setNegativeButton("CANCEL"){_,_ ->
                        val parent : ViewGroup = settings.parent as ViewGroup
                        parent.removeView(settings)
                    }.show()
            }
            alertDialogBuilder.show()
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


    private fun clickLink() {
        teacher_btn_att.setOnClickListener{
            if (roomType == "Group"){
                databaseReference = FirebaseDatabase.getInstance().getReference("Public Group").child(roomID)
                databaseReference.child("Links").get().addOnSuccessListener {
                    if (it.exists()){
                        link = it.child("attendanceLink").value.toString()
                        if (link.isEmpty()){
                            Toast.makeText(this, "No link attach, add link first.", Toast.LENGTH_SHORT).show()
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
                        showDialog("", "Before you go any further, copy and paste your attendance link into the add button.")
                    }
                }.addOnFailureListener{

                }
            }else if (roomType == "Class"){
                databaseReference = FirebaseDatabase.getInstance().getReference("Public Class").child(roomID)
                databaseReference.child("Links").get().addOnSuccessListener {
                    if (it.exists()){
                        link = it.child("attendanceLink").value.toString()
                        if (link.isEmpty()){
                            Toast.makeText(this, "No link attach, add link first.", Toast.LENGTH_SHORT).show()
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
                        showDialog("", "Before you go any further, copy and paste your attendance link into the add button.")
                    }
                }.addOnFailureListener{

                }
            }
        }
        teacher_btn_zoom.setOnClickListener{
            if (roomType == "Group"){
                databaseReference = FirebaseDatabase.getInstance().getReference("Public Group").child(roomID)
                databaseReference.child("Links").get().addOnSuccessListener {
                    if (it.exists()){
                        link = it.child("zoomLink").value.toString()
                        if (link.isEmpty()){
                            Toast.makeText(this, "No link attach, add link first.", Toast.LENGTH_SHORT).show()
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
                        showDialog("", "Before you go any further, copy and paste your zoom link into the add button.")
                    }
                }.addOnFailureListener{

                }
            }else if (roomType == "Class"){
                databaseReference = FirebaseDatabase.getInstance().getReference("Public Class").child(roomID)
                databaseReference.child("Links").get().addOnSuccessListener {
                    if (it.exists()){
                        link = it.child("zoomLink").value.toString()
                        if (link.isEmpty()){
                            Toast.makeText(this, "No link attach, add link first.", Toast.LENGTH_SHORT).show()
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
                        showDialog("", "Before you go any further, copy and paste your zoom link into the add button.")
                    }
                }.addOnFailureListener{

                }
            }
        }
    }

    private fun addLinkButton() {
        elips.setOnClickListener{
            pasteLinks()
        }
    }

    private fun pasteLinks() {
        val inflater = layoutInflater
        val addLinkDialog = inflater.inflate(R.layout.alert_dialog_add_link, null)
        val zoomLink = addLinkDialog.findViewById<EditText>(R.id.link_zoom)
        val attLink = addLinkDialog.findViewById<EditText>(R.id.link_attendance)
        MaterialAlertDialogBuilder(this)
            .setTitle("Paste link")
            .setView(addLinkDialog)
            .setPositiveButton("OK"){_,_ ->
                val zoom = zoomLink.text.toString()
                val attendance = attLink.text.toString()
                val data = LinkData(zoom, attendance)
                if(TextUtils.isEmpty(zoomLink.text.toString().trim()) || TextUtils.isEmpty(attLink.text.toString().trim()) ){
                    showDialog("", "Please add some links.")
                }else{
                    if(roomType == "Group"){
                        databaseReference = FirebaseDatabase.getInstance().getReference("Public Group").child(roomID)
                        databaseReference.child("Links").setValue(data).addOnCompleteListener{
                            showDialog("", "Links added successfully to this room.")
                        }
                    }else if(roomType == "Class"){
                        databaseReference = FirebaseDatabase.getInstance().getReference("Public Class").child(roomID)
                        databaseReference.child("Links").setValue(data).addOnCompleteListener{
                            showDialog("", "Links added successfully to this room.")
                        }
                    }
                }




            }.show()
    }

    private fun showHelp() {
        teacher_help_button.setOnClickListener{
            showDialog("Need help?", "Click the add button and paste your link to add virtual meeting and attendance links.\n" +
                    "\nTo delete a post, swipe it away from the display."
            )
        }
    }

    private fun transferData() {
        val bundle = Bundle()
        bundle.putString("teacher_roomID_", roomID)
        bundle.putString("teacher_room_type_", roomType)
        bundle.putString("teacher_room_name_", roomName)
        roomPost.arguments = bundle
        roomMembers.arguments = bundle
    }

    private fun replaceText() {
        val databaseClass = FirebaseDatabase.getInstance().getReference("Public Class")
        databaseReference = FirebaseDatabase.getInstance().getReference("Public Group")
        databaseReference.child(roomID).get().addOnSuccessListener { it ->
            if (it.exists()) {
                val roomNameDataBase = it.child("subjectTitle").value.toString()
                val roomSectionDatabase = it.child("section").value.toString()
                teachers_room_name.text = roomNameDataBase
                teachers_section_txt.text = roomSectionDatabase
                txt_roomType.text = roomType
                roomID_txt.text = roomID
            } else {
                databaseClass.child(roomID).get().addOnSuccessListener {
                    if (it.exists()) {
                        val roomNameDataBase = it.child("subjectTitle").value.toString()
                        val roomSectionDatabase = it.child("section").value.toString()
                        teachers_room_name.text = roomNameDataBase
                        teachers_section_txt.text = roomSectionDatabase
                        txt_roomType.text = roomType
                        roomID_txt.text = roomID
                    }
                }
            }
        }
    }

    private fun showDialog(dialogTitle : String, message: String){
        MaterialAlertDialogBuilder(this)
            .setTitle(dialogTitle)
            .setMessage(message)
            .setPositiveButton("OK"){_,_ ->

            }.show()
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
    @RequiresApi(Build.VERSION_CODES.O)
    fun LocalDateTime.toMillis(zone: ZoneId = ZoneId.systemDefault()) = atZone(zone)?.toInstant()?.toEpochMilli()

    private fun replaceFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.room_fragment_container, fragment)
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        transaction.commit()
    }
}