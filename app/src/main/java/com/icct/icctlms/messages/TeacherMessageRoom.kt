package com.icct.icctlms.messages

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.icct.icctlms.Authentication.Teacher.TeacherMainActivity
import com.icct.icctlms.MainActivity
import com.icct.icctlms.R
import com.icct.icctlms.adapter.StudentMessageAdapter
import com.icct.icctlms.data.MessageData
import com.icct.icctlms.data.UserData
import kotlinx.android.synthetic.main.activity_student_message_room.*
import kotlinx.android.synthetic.main.activity_teacher_message_room.*
import kotlinx.android.synthetic.main.post_list.*
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class TeacherMessageRoom : AppCompatActivity() {
    private lateinit var studentUID : String
    private lateinit var studentName : String
    private lateinit var uid : String
    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var chatArrayList: ArrayList<MessageData>
    private lateinit var hour : String
    private lateinit var finalHour : String
    private lateinit var today : Calendar
    private lateinit var date : String
    private lateinit var sortKey : String
    private lateinit var now : LocalDateTime
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teacher_message_room)
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

        uid = Firebase.auth.currentUser?.uid.toString()
        studentName = intent.getStringExtra("student_name").toString()
        studentUID = intent.getStringExtra("student_uid").toString()

        chatRecyclerView = findViewById(R.id.teacher_chat_recycler_view)
        chatRecyclerView.setHasFixedSize(true)
        chatRecyclerView.layoutManager = LinearLayoutManager(this)


        student_top_name.text = studentName
        chatArrayList = arrayListOf()
        sendMessage()
        executeMessage()
        val back = findViewById<ImageView>(R.id.back_message)
        back.setOnClickListener{
            startActivity(Intent(this, TeacherMainActivity::class.java))
            finish()
        }
    }

    private fun executeMessage() {
        val getTeacherReceive = FirebaseDatabase.getInstance().getReference("Chat").child("TeacherReceived")
            .child(uid)
            .child(studentUID)
        getTeacherReceive.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    chatArrayList.clear()


                    for (postSnapShot in snapshot.children){
                        val members = postSnapShot.getValue(MessageData::class.java)
                        chatArrayList.add(members!!)
                    }
                    val adapter = StudentMessageAdapter(chatArrayList)
                    chatArrayList.sortBy {
                        it.sortKey
                    }
                    chatRecyclerView.adapter = adapter

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
        teacher_send_message.setOnClickListener{
            val message = teacher_message_et.text.toString().trim()
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
            val data = MessageData(
                type = "receiver",
                dateTime = date,
                senderName = "Joe Cristian",
                receiverName = "",
                sortKey = sortKey,
                message = message,
                uid = studentUID,
                teacherUID = uid,
                roomID = ""
            )
            val sentToDatabase = FirebaseDatabase.getInstance().getReference("Chat")
                .child("StudentSend")
                .child(studentUID)
                .child(uid)
            sentToDatabase.child(randomCode()).setValue(data).addOnSuccessListener {
                Toast.makeText(this, "Sent successfully!", Toast.LENGTH_SHORT).show()
                executeMessage()
                teacher_message_et.text?.clear()
                val teacherData = MessageData(
                    type = "sender",
                    dateTime = date,
                    senderName = "Joe Cristian",
                    receiverName = "",
                    sortKey = sortKey,
                    message = message,
                    uid = studentUID,
                    uid,
                    roomID = ""
                )
                val createTeacherView = FirebaseDatabase.getInstance().getReference("Chat")
                    .child("TeacherReceived")
                    .child(uid)
                    .child(studentUID)
                createTeacherView.child(randomCode()).setValue(teacherData).addOnSuccessListener {
                    val getStudentInfo = FirebaseDatabase.getInstance().getReference("Students")
                        .child(studentUID).get().addOnSuccessListener {
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