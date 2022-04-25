package com.icct.icctlms.Authentication.Teacher

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.opengl.Visibility
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Message
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.icct.icctlms.R
import com.icct.icctlms.adapter.SendToClassAdapter
import com.icct.icctlms.adapter.SendToGroupListAdapter
import com.icct.icctlms.data.CreateClassData
import com.icct.icctlms.data.GroupListData
import com.icct.icctlms.data.RoomPostData
import kotlinx.android.synthetic.main.activity_create_teacher_post.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_teacher_home.*
import java.io.File
import java.text.SimpleDateFormat
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*
import kotlin.collections.ArrayList

class CreateTeacherPost : AppCompatActivity() {
    private lateinit var databaseReference: DatabaseReference
    private lateinit var storageReference: StorageReference
    private lateinit var dialog : Dialog
    private lateinit var classArrayList : ArrayList<CreateClassData>
    private lateinit var groupArrayList: ArrayList<GroupListData>
    private lateinit var groupRecyclerView: RecyclerView
    private lateinit var classRecyclerView: RecyclerView
    private lateinit var uid : String
    private lateinit var finalDate : String
    private lateinit var sortKey : String
    private lateinit var postData : String
    private lateinit var finalHour : String
    private lateinit var hour : String
    private lateinit var today : Calendar
    private lateinit var date : String
    private lateinit var day : String
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_teacher_post)
        //initialized group list
        groupRecyclerView = findViewById(R.id.send_group_list)
        groupRecyclerView.setHasFixedSize(true)
        groupRecyclerView.layoutManager = LinearLayoutManager(applicationContext)
        groupArrayList = arrayListOf<GroupListData>()

        //initialized class list
        classRecyclerView = findViewById(R.id.send_class_list)
        classRecyclerView.setHasFixedSize(true)
        classRecyclerView.layoutManager = LinearLayoutManager(applicationContext)
        classArrayList = arrayListOf()
        val now = LocalDateTime.now()
        today = Calendar.getInstance()
        sortKey = now.toMillis().toString()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            date = SimpleDateFormat("MMMM d, Y").format(today.time).toString()
        }
        day = SimpleDateFormat("EEEE").format(today.time).toString()
        finalDate = "$day, $date"
        val time = LocalTime.now()
        val pattern = "hh:mm a"
        hour = LocalTime.now().toString()
        finalHour = time.format(DateTimeFormatter.ofPattern(pattern))



        uid = FirebaseAuth.getInstance().currentUser?.uid.toString()

        showGroupList()
        showClassList()

        showName()
        showProfile()

        send.setOnClickListener{
            postSomething()
        }

        editBehaviour()


    }

    private fun editBehaviour() {
        edit_send.setOnClickListener{
            recycler_holder.visibility = View.VISIBLE
            send_to_back.visibility = View.GONE
            txt_send.text = "Send to"
            post_holder.visibility = View.GONE
        }
        send_to_back.setOnClickListener{
            MaterialAlertDialogBuilder(this)
                .setTitle("Delete draft?")
                .setMessage("Are you sure you want to leave?")
                .setPositiveButton("Okay"){_,_ ->
                    val intent = Intent(this, TeacherMainActivity::class.java)
                    finish()
                    startActivity(intent)
                }.setNegativeButton("Cancel"){_,_ ->}.show()
        }
    }

    private fun showClassList() {
        databaseReference = FirebaseDatabase.getInstance().getReference("Class").child(uid)
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()){
                    classArrayList.clear()

                    for (classSnapShot in snapshot.children){
                        snapshot.child(uid)
                        val subjectTitle = classSnapShot.getValue(CreateClassData::class.java)
                        classArrayList.add(subjectTitle!!)
                    }
                    val adapter = SendToClassAdapter(classArrayList)
                    classRecyclerView.adapter = adapter

                    adapter.setOnItemClickListener(object : SendToClassAdapter.onItemClickListener{

                        @SuppressLint("SetTextI18n")
                        override fun onItemClick(position: Int) {
                            val roomName = classArrayList[position].subjectTitle.toString()
                            val roomSection = classArrayList[position].section.toString()
                            val roomID = classArrayList[position].roomID.toString()
                            val roomType =  "Class"
                            post_holder.visibility = View.VISIBLE
                            recycler_holder.visibility = View.GONE
                            cardView.visibility = View.VISIBLE
                            room_name.text = "$roomName $roomSection"
                            roomIDFetch.text = roomID
                            roomTypeFetch.text = roomType
                            txt_send.text = "New Post"
                            send_to_back.visibility = View.VISIBLE
                            send_to_back.setImageResource(R.drawable.ic_ex)

                        }



                    })




                }
            }


            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun showGroupList() {
        databaseReference = FirebaseDatabase.getInstance().getReference("Group").child(uid)
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()){
                    groupArrayList.clear()

                    for (classSnapShot in snapshot.children){
                        snapshot.child(uid)
                        val subjectTitle = classSnapShot.getValue(GroupListData::class.java)
                        groupArrayList.add(subjectTitle!!)
                    }
                    val adapter = SendToGroupListAdapter(groupArrayList)
                    groupRecyclerView.adapter = adapter

                    adapter.setOnItemClickListener(object : SendToGroupListAdapter.onItemClickListener{

                        @SuppressLint("SetTextI18n")
                        override fun onItemClick(position: Int) {
                            val roomName = groupArrayList[position].subjectTitle.toString()
                            val roomSection = groupArrayList[position].section.toString()
                            val roomID = groupArrayList[position].roomID.toString()
                            val roomType =  "Group"
                            recycler_holder.visibility = View.GONE
                            post_holder.visibility = View.VISIBLE
                            cardView.visibility = View.VISIBLE
                            room_name.text = "$roomName $roomSection"
                            roomIDFetch.text = roomID
                            roomTypeFetch.text = roomType
                            txt_send.text = "New Post"
                            send_to_back.visibility = View.VISIBLE
                            send_to_back.setImageResource(R.drawable.ic_ex)


                        }



                    })




                }
            }


            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    override fun onBackPressed() {
        val intent = Intent(this, TeacherMainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        finish()
        startActivity(intent)
        super.onBackPressed()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun postSomething() {
        val roomId = roomIDFetch.text.toString()
        val roomType = roomTypeFetch.text.toString()
        val roomName = room_name.text.toString()

        if(roomType == "Group"){
            progressDialogShow()
            val databaseUserStudent = FirebaseDatabase.getInstance().getReference("Teachers")
            databaseUserStudent.child(uid).get().addOnSuccessListener {
                if (it.exists()){
                    postData = randomID()
                    val message = et.text.toString()
                    val userName = it.child("name").value.toString()
                    val data = RoomPostData(roomId, userName, sortKey, postData, roomName, finalDate, finalHour, "Teacher", message)
                    val databaseRoomPost = FirebaseDatabase.getInstance().getReference("Group Post")
                    databaseRoomPost.child("Room ID: $roomId").child(postData).setValue(data).addOnSuccessListener {
                        progressDialogHide()
                        showPop("Posted successfully!")

                    }
                }
            }
        }else if(roomType == "Class"){
            progressDialogShow()
            val databaseUserStudent = FirebaseDatabase.getInstance().getReference("Teachers")
            databaseUserStudent.child(uid).get().addOnSuccessListener {
                if (it.exists()){
                    postData = randomID()
                    val message = et.text.toString()
                    val userName = it.child("name").value.toString()
                    val data = RoomPostData(roomId, userName, sortKey, postData, userName, finalDate, finalHour, "Teacher", message)
                    val databaseRoomPost = FirebaseDatabase.getInstance().getReference("Class Post")
                    databaseRoomPost.child("Room ID: $roomId").child(postData).setValue(data).addOnSuccessListener {
                        progressDialogHide()
                        showPop("Posted successfully!")

                    }
                }
            }
        }



    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun LocalDateTime.toMillis(zone: ZoneId = ZoneId.systemDefault()) = atZone(zone)?.toInstant()?.toEpochMilli()


    private fun showPop(message : String){
        MaterialAlertDialogBuilder(this)
            .setMessage(message).setPositiveButton("OK"){_,_ ->
                val intent = Intent(this, TeacherMainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
                finish()
                startActivity(intent)
            }.show()
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


    private fun showName() {
        val auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid.toString()
databaseReference = FirebaseDatabase.getInstance().getReference("Teachers")
        databaseReference.child(uid).get().addOnSuccessListener {
            if (it.exists()){
                val name = it.child("name").value.toString()

                new_post_name.text = name
            }
        }
    }




    private fun showProfile() {
        val auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid.toString()

        storageReference = FirebaseStorage.getInstance().getReference("Teachers/$uid")
        val localFile = File.createTempFile("tempImage", "jpg")
        storageReference.getFile(localFile).addOnSuccessListener {
            val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
            new_post_profile.setImageBitmap(bitmap)

        }
    }

    private fun randomID(): String = List(16) {
        (('a'..'z') + ('A'..'Z') + ('0'..'9')).random()
    }.joinToString("")
}