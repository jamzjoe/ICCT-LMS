package com.icct.icctlms.Authentication.Teacher

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.icct.icctlms.R
import com.icct.icctlms.RoomActivity
import com.icct.icctlms.adapter.MembersAdapter
import com.icct.icctlms.data.CountData
import com.icct.icctlms.data.RoomMembersData
import com.icct.icctlms.data.TeacherPostData
import com.icct.icctlms.database.Notification
import kotlinx.android.synthetic.main.activity_room_member_request.*
import kotlinx.android.synthetic.main.activity_welcome.*
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class RoomMemberRequest : AppCompatActivity() {
    private lateinit var roomID : String
    private lateinit var roomType : String
    private lateinit var databaseClass : DatabaseReference
    private lateinit var databaseGroup : DatabaseReference
    private lateinit var recyclerView: RecyclerView
    private lateinit var membersArrayList: ArrayList<RoomMembersData>
    private lateinit var roomName: String
    private lateinit var hour : String
    private lateinit var finalHour : String
    private lateinit var today : Calendar
    private lateinit var date : String
    private var count : Int = 0
    private lateinit var sortKey : String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room_member_request)
        recyclerView = findViewById(R.id.room_request_list)
        recyclerView.setHasFixedSize(
            true
        )
        recyclerView.layoutManager = LinearLayoutManager(this.applicationContext)
        membersArrayList = arrayListOf()
        roomID = intent.getStringExtra("member_room_id").toString()
        roomType = intent.getStringExtra("member_room_type").toString()
        roomName = intent.getStringExtra("room_name").toString()

        databaseGroup = FirebaseDatabase.getInstance().getReference("Public Group").child(roomID).child("Request")
        databaseClass = FirebaseDatabase.getInstance().getReference("Public Class").child(roomID).child("Request")
        if(roomType == "Group"){
            executeGroupMembers()
        }else if(roomType == "Class"){
            executeClassMembers()
        }

        //convert hour to text
        val now = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
        date = "Accepted in $trimMonth $day at $finalHour"

        //use this key to sort arraylist
        sortKey = now.toMillis().toString()
    }

    private fun executeClassMembers() {
        databaseClass.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    membersArrayList.clear()


                    for (postSnapShot in snapshot.children){
                        val members = postSnapShot.getValue(RoomMembersData::class.java)
                        membersArrayList.add(members!!)

                    }




                    val adapter = MembersAdapter(membersArrayList)
                    membersArrayList.sortByDescending {
                        it.name
                    }
                    recyclerView.adapter = adapter

                    count = adapter.itemCount
                    noData(count)
                    val data = CountData(count.toString())
                    val membersCount = FirebaseDatabase.getInstance().getReference("Public Class").child(roomID)
                    membersCount.child("Population").setValue(data)


                    //adapter click listener
                    adapter.setOnItemClickListener(object : MembersAdapter.onItemClickListener{
                        override fun onItemClick(position: Int) {
                            val studentName = membersArrayList[position].name
                            val type = membersArrayList[position].type
                            val studentUID = membersArrayList[position].uid.toString()
                            MaterialAlertDialogBuilder(this@RoomMemberRequest)
                                .setTitle("Request")
                                .setMessage("Are you sure you want to add $studentName in this room?")
                                .setPositiveButton("ACCEPT"){_,_ ->
                                    val isAccept = "true"
                                    val data = RoomMembersData(studentName, type, studentUID, isAccept)
                                    val addMember = FirebaseDatabase.getInstance().getReference("Public Class").child(roomID).child("Members")
                                    addMember.child(studentUID).setValue(data).addOnSuccessListener {
                                        val accept = FirebaseDatabase.getInstance().getReference("Public Class").child(roomID).child("Accept")
                                        accept.child(studentUID).setValue(data).addOnSuccessListener {

                                            val deleteRequest = FirebaseDatabase.getInstance().getReference("Public Class").child(roomID).child("Request")
                                            deleteRequest.child(studentUID).removeValue().addOnSuccessListener {
                                                val uid = Firebase.auth.currentUser?.uid.toString()
                                                val getTeacherName = FirebaseDatabase.getInstance().getReference("Teachers").child(uid)
                                                getTeacherName.get().addOnSuccessListener {
                                                    if (it.exists()){
                                                        val teacherName = it.child("name").value.toString()
                                                        val setStudentNotification = Notification()
                                                        val description = "$teacherName accepted your request to join class $roomName."
                                                        setStudentNotification.studentNotification(studentUID, "", description, randomCode(), date, sortKey)

                                                    }
                                                }

                                                executeClassMembers()
                                                adapter.deleteItem(position)
                                                recyclerView.adapter?.notifyItemRemoved(position)

                                                noData(count)
                                            }
                                        }

                                    }
                                }
                                .setNegativeButton("IGNORE"){_,_ ->
                                    val deleteMember = FirebaseDatabase.getInstance().getReference("Public Class").child(roomID).child("Request")
                                    deleteMember.child(studentUID).removeValue().addOnSuccessListener {
                                        val joinClass = FirebaseDatabase.getInstance().getReference("JoinClass").child(studentUID)
                                        joinClass.child(roomID).removeValue().addOnSuccessListener {
                                            Toast.makeText(this@RoomMemberRequest, "$studentName ignored!", Toast.LENGTH_SHORT).show()
                                            executeClassMembers()
                                            adapter.deleteItem(position)
                                            recyclerView.adapter?.notifyItemRemoved(position)

                                            noData(count)
                                        }

                                    }
                                }
                                .show()
                        }

                    })

                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun noData(count : Int) {
        if(count > 0){
            no_request.visibility = View.GONE
        }
    }

    private fun executeGroupMembers() {
        databaseGroup.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    membersArrayList.clear()


                    for (postSnapShot in snapshot.children){
                        val members = postSnapShot.getValue(RoomMembersData::class.java)
                        membersArrayList.add(members!!)
                    }
                    val adapter = MembersAdapter(membersArrayList)
                    membersArrayList.sortByDescending {
                        it.name
                    }
                    recyclerView.adapter = adapter

                    //adapter click listener
                    adapter.setOnItemClickListener(object : MembersAdapter.onItemClickListener{
                        override fun onItemClick(position: Int) {
                            val studentName = membersArrayList[position].name
                            val type = membersArrayList[position].type
                            val studentUID = membersArrayList[position].uid.toString()
                            MaterialAlertDialogBuilder(this@RoomMemberRequest)
                                .setTitle("Accept Student?")
                                .setMessage("Are you sure you want to add $studentName in this room?")
                                .setPositiveButton("ACCEPT"){_,_ ->
                                    val isAccept = "true"
                                    val data = RoomMembersData(studentName, type, studentUID, isAccept)
                                    val addMember = FirebaseDatabase.getInstance().getReference("Public Group").child(roomID).child("Members")
                                    addMember.child(studentUID).setValue(data).addOnSuccessListener {
                                        val accept = FirebaseDatabase.getInstance().getReference("Public Group").child(roomID).child("Accept")
                                        accept.child(studentUID).setValue(data).addOnSuccessListener {
                                            val deleteRequest = FirebaseDatabase.getInstance().getReference("Public Group").child(roomID).child("Request")
                                            deleteRequest.child(studentUID).removeValue().addOnSuccessListener {
                                                executeGroupMembers()
                                                adapter.deleteItem(position)
                                                recyclerView.adapter?.notifyItemRemoved(position)

                                                val uid = Firebase.auth.currentUser?.uid.toString()
                                                val getTeacherName = FirebaseDatabase.getInstance().getReference("Teachers").child(uid)
                                                getTeacherName.get().addOnSuccessListener {
                                                    if (it.exists()){
                                                        val teacherName = it.child("name").value.toString()
                                                        val setStudentNotification = Notification()
                                                        val description = "$teacherName accepted your request to join group $roomName."
                                                        setStudentNotification.studentNotification(studentUID, "", description, randomCode(), date, sortKey)


                                                    }
                                                }
                                            }
                                        }


                                    }
                                }
                                .setNegativeButton("IGNORE"){_,_ ->
                                    val deleteMember = FirebaseDatabase.getInstance().getReference("Public Group").child(roomID).child("Request")
                                    deleteMember.child(studentUID).removeValue().addOnSuccessListener {
                                        val joinClass = FirebaseDatabase.getInstance().getReference("JoinGroup")
                                            .child(studentUID)
                                        joinClass.child(roomID).removeValue().addOnSuccessListener {
                                            Toast.makeText(this@RoomMemberRequest, "$studentName ignored!", Toast.LENGTH_SHORT).show()
                                            executeGroupMembers()
                                            adapter.deleteItem(position)
                                            recyclerView.adapter?.notifyItemRemoved(position)
                                        }

                                    }
                                }
                                .show()
                        }

                    })

                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun LocalDateTime.toMillis(zone: ZoneId = ZoneId.systemDefault()) = atZone(zone)?.toInstant()?.toEpochMilli()
    private fun randomCode(): String = List(6) {
        (('a'..'z') + ('A'..'Z') + ('0'..'9')).random()
    }.joinToString("")
}