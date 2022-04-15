package com.icct.icctlms.Authentication.Teacher

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.*
import com.icct.icctlms.R
import com.icct.icctlms.RoomActivity
import com.icct.icctlms.adapter.MembersAdapter
import com.icct.icctlms.data.CountData
import com.icct.icctlms.data.RoomMembersData

class RoomMemberRequest : AppCompatActivity() {
    private lateinit var roomID : String
    private lateinit var roomType : String
    private lateinit var databaseClass : DatabaseReference
    private lateinit var databaseGroup : DatabaseReference
    private lateinit var recyclerView: RecyclerView
    private lateinit var membersArrayList: ArrayList<RoomMembersData>
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

        databaseGroup = FirebaseDatabase.getInstance().getReference("Public Group").child(roomID).child("Request")
        databaseClass = FirebaseDatabase.getInstance().getReference("Public Class").child(roomID).child("Request")
        if(roomType == "Group"){
            executeGroupMembers()
        }else if(roomType == "Class"){
            executeClassMembers()
        }
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

                    val count = adapter.itemCount.toString()
                    val data = CountData(count)
                    val membersCount = FirebaseDatabase.getInstance().getReference("Public Class").child(roomID)
                    membersCount.child("Population").setValue(data)

                    Toast.makeText(this@RoomMemberRequest, count, Toast.LENGTH_SHORT).show()

                    //adapter click listener
                    adapter.setOnItemClickListener(object : MembersAdapter.onItemClickListener{
                        override fun onItemClick(position: Int) {
                            val studentName = membersArrayList[position].name
                            val type = membersArrayList[position].type
                            val uid = membersArrayList[position].uid.toString()
                            MaterialAlertDialogBuilder(this@RoomMemberRequest)
                                .setTitle("Request")
                                .setMessage("Are you sure you want to add $studentName in this room?")
                                .setPositiveButton("ACCEPT"){_,_ ->
                                    val isAccept = "true"
                                    val data = RoomMembersData(studentName, type, uid, isAccept)
                                    val addMember = FirebaseDatabase.getInstance().getReference("Public Class").child(roomID).child("Members")
                                    addMember.child(uid).setValue(data).addOnSuccessListener {
                                        val accept = FirebaseDatabase.getInstance().getReference("Public Class").child(roomID).child("Accept")
                                        accept.child(uid).setValue(data).addOnSuccessListener {

                                            val deleteRequest = FirebaseDatabase.getInstance().getReference("Public Class").child(roomID).child("Request")
                                            deleteRequest.child(uid).removeValue().addOnSuccessListener {
                                                executeClassMembers()
                                                adapter.deleteItem(position)
                                                recyclerView.adapter?.notifyItemRemoved(position)
                                            }
                                        }

                                    }
                                }
                                .setNegativeButton("IGNORE"){_,_ ->
                                    val deleteMember = FirebaseDatabase.getInstance().getReference("Public Class").child(roomID).child("Request")
                                    deleteMember.child(uid).removeValue().addOnSuccessListener {
                                        val joinClass = FirebaseDatabase.getInstance().getReference("JoinClass").child(uid)
                                        joinClass.child(roomID).removeValue().addOnSuccessListener {
                                            Toast.makeText(this@RoomMemberRequest, "$studentName ignored!", Toast.LENGTH_SHORT).show()
                                            executeClassMembers()
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
                            val uid = membersArrayList[position].uid.toString()
                            MaterialAlertDialogBuilder(this@RoomMemberRequest)
                                .setTitle("Kick Student")
                                .setMessage("Are you sure you want to add in this room $studentName ?")
                                .setPositiveButton("ACCEPT"){_,_ ->
                                    val isAccept = "true"
                                    val data = RoomMembersData(studentName, type, uid, isAccept)
                                    val addMember = FirebaseDatabase.getInstance().getReference("Public Group").child(roomID).child("Members")
                                    addMember.child(uid).setValue(data).addOnSuccessListener {
                                        val accept = FirebaseDatabase.getInstance().getReference("Public Group").child(roomID).child("Accept")
                                        accept.child(uid).setValue(data).addOnSuccessListener {
                                            val deleteRequest = FirebaseDatabase.getInstance().getReference("Public Group").child(roomID).child("Request")
                                            deleteRequest.child(uid).removeValue().addOnSuccessListener {
                                                executeGroupMembers()
                                                adapter.deleteItem(position)
                                                recyclerView.adapter?.notifyItemRemoved(position)
                                            }
                                        }


                                    }
                                }
                                .setNegativeButton("IGNORE"){_,_ ->
                                    val deleteMember = FirebaseDatabase.getInstance().getReference("Public Group").child(roomID).child("Request")
                                    deleteMember.child(uid).removeValue().addOnSuccessListener {
                                        val joinClass = FirebaseDatabase.getInstance().getReference("JoinGroup")
                                            .child(uid)
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

}