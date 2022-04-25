package com.icct.icctlms.messages

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.view.isEmpty
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.icct.icctlms.MainActivity
import com.icct.icctlms.R
import com.icct.icctlms.adapter.StudentMessageAdapter
import com.icct.icctlms.components.toast
import com.icct.icctlms.data.MessageData
import com.icct.icctlms.data.UserData
import kotlinx.android.synthetic.main.activity_student_message_room.*
import kotlinx.android.synthetic.main.room_members_item.*
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class StudentMessageRoom : AppCompatActivity() {
    private lateinit var view : RelativeLayout
    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var chatArrayList: ArrayList<MessageData>
    private lateinit var uid : String
    private lateinit var hour : String
    private lateinit var finalHour : String
    private lateinit var today : Calendar
    private lateinit var date : String
    private lateinit var sortKey : String
    private lateinit var now : LocalDateTime
    private lateinit var teacherName : String
    private lateinit var teacherUID : String
    private lateinit var roomID : String
    private lateinit var adapter : StudentMessageAdapter
    private lateinit var mManager : LinearLayoutManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_message_room)

        val tName = intent.getStringExtra("teacher_name")
        val tUID = intent.getStringExtra("teacher_uid")
        roomID = intent.getStringExtra("room_id").toString()
        teacherName = tName.toString()
        teacherUID = tUID.toString()

        teacher_top_name.text = teacherName
        uid = Firebase.auth.currentUser?.uid.toString()
        chatRecyclerView = findViewById(R.id.chat_recycler_view)
        chatRecyclerView.setHasFixedSize(true)
        mManager = LinearLayoutManager(this)
        chatRecyclerView.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }


        chatArrayList = arrayListOf()
        sendMessage()
        executeMessage()


        //convert hour to text
        now = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDateTime.now()
        } else {
            TODO("VERSION.SDK_INT < O")
        }
        hour = LocalTime.now().toString()
        val time = LocalTime.now()
        finalHour = time.format(DateTimeFormatter.ofPattern("hh:mm a"))

        //convert month to text
        today = Calendar.getInstance()
        val day = today.get(Calendar.DAY_OF_MONTH)
        val monthList = arrayOf("January", "February",
            "March", "April", "May", "June", "July",
            "August", "September", "October", "November",
            "December")
        val month = monthList[today.get(Calendar.MONTH)]
        val trimMonth = month.subSequence(0, 3)
        date = "$trimMonth $day at $finalHour"
        back_message.setOnClickListener{
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }



    private fun executeMessage() {
        val getMessageDataBase = FirebaseDatabase.getInstance().getReference("Chat")
            .child("StudentSend")
                .child(uid)
                    .child(teacherUID)
        getMessageDataBase.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    chatArrayList.clear()


                    for (postSnapShot in snapshot.children){
                        val members = postSnapShot.getValue(MessageData::class.java)
                        chatArrayList.add(members!!)
                    }
                    adapter = StudentMessageAdapter(chatArrayList)
                    chatArrayList.sortBy {
                        it.sortKey
                    }
                    chatRecyclerView.adapter = adapter
                    chatRecyclerView.scrollToPosition(adapter.itemCount-1)

                    val count = adapter.itemCount
                    if (count > 0){
                        empty_message.visibility = View.GONE
                    }


                    //adapter click listener
                    adapter.setOnItemClickListener(object : StudentMessageAdapter.onItemClickListener{
                        override fun onItemClick(position: Int) {
                            //onclick
                        }

                    })

                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun sendMessage() {
        send_message.setOnClickListener{
            val message = message_et.text.toString().trim()
//convert hour to text
            val ngayon = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                LocalDateTime.now()
            } else {
                TODO("VERSION.SDK_INT < O")
            }
            //use this key to sort arraylist
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                sortKey = ngayon.toMillis().toString()
            }
            val getUserInfo = FirebaseDatabase.getInstance().getReference("Students").child(uid)
            getUserInfo.get().addOnSuccessListener {
                if (it.exists()){
                    val color = it.child("colors").value.toString()
                    val name = it.child("name").value.toString()

                    val data = MessageData(
                        type = "sender",
                        dateTime = date,
                        senderName = name,
                        receiverName = "",
                        sortKey = sortKey,
                        message = message,
                        uid = uid,
                        teacherUID = teacherUID,
                        roomID = roomID,
                        colors = color
                    )
                    val sentToDatabase = FirebaseDatabase.getInstance().getReference("Chat")
                        .child("StudentSend")
                        .child(uid)
                        .child(teacherUID)
                    sentToDatabase.child(randomCode()).setValue(data).addOnSuccessListener {
                        Toast.makeText(this, "Sent successfully!", Toast.LENGTH_SHORT).show()
                        message_et.text?.clear()
                        val teacherData = MessageData(
                            type = "receiver",
                            dateTime = date,
                            senderName = "",
                            receiverName = name,
                            sortKey = sortKey,
                            message = message,
                            uid = uid,
                            teacherUID = teacherUID,
                            roomID = roomID,
                            colors = color
                        )
                        val createTeacherView = FirebaseDatabase.getInstance().getReference("Chat")
                            .child("TeacherReceived")
                            .child(teacherUID)
                            .child(uid)
                        createTeacherView.child(randomCode()).setValue(teacherData).addOnSuccessListener {
                            val getStudentInfo = FirebaseDatabase.getInstance().getReference("Students")
                            getStudentInfo.child(uid).get().addOnSuccessListener {
                                if (it.exists()){
                                    val userID = it.child("account_id").value.toString()
                                    val email = it.child("email").value.toString()
                                    val name = it.child("name").value.toString()
                                    val school = it.child("school").value.toString()
                                    val type = "Student"

                                    val userInfo = UserData(
                                        account_id = userID,
                                        email = email,
                                        name = name,
                                        school = school,
                                        type = type
                                    )


                                    val createTeacherRecipientList = FirebaseDatabase.getInstance().getReference("Chat")
                                        .child("TeacherRecipientList")
                                        .child(teacherUID)
                                    createTeacherRecipientList.setValue(userInfo)
                                }
                            }
                        }

                    }
                }
            }
        }
    }

    private fun randomCode(): String = List(6) {
        (('a'..'z') + ('A'..'Z') + ('0'..'9')).random()
    }.joinToString("")

    @RequiresApi(Build.VERSION_CODES.O)
    fun LocalDateTime.toMillis(zone: ZoneId = ZoneId.systemDefault()) = atZone(zone)?.toInstant()?.toEpochMilli()
}