package com.icct.icctlms.room

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.*
import com.icct.icctlms.R
import com.icct.icctlms.adapter.MembersAdapter
import com.icct.icctlms.adapter.TeacherPostAdapter
import com.icct.icctlms.data.RoomMembersData
import com.icct.icctlms.data.TeacherPostData

class StudentRoomMembers : Fragment() {
    private lateinit var roomID : String
    private lateinit var type : String
    private lateinit var uid : String
    private lateinit var name : String
    private lateinit var database: DatabaseReference
    private lateinit var recyclerView: RecyclerView
    private lateinit var databaseGroup : DatabaseReference
    private lateinit var membersArrayList: ArrayList<RoomMembersData>
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_student_room_members, container, false)

        // Inflate the layout for this fragment
        //read the data snapshot of members
        roomID = arguments?.getString("student_roomID_").toString()
        type = arguments?.getString("student_type_").toString()
        uid = arguments?.getString("student_uid_").toString()
        name = arguments?.getString("student_person_name").toString()

        recyclerView = rootView.findViewById(R.id.student_room_members_list)!!
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)
        membersArrayList = arrayListOf<RoomMembersData>()

        database = FirebaseDatabase.getInstance().getReference("Public Class").child(roomID).child("Members")
        executeClass()




        databaseGroup = FirebaseDatabase.getInstance().getReference("Public Group").child(roomID).child("Members")
        executeGroup()




        return rootView
    }

    private fun executeClass() {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    membersArrayList.clear()


                    for (postSnapShot in snapshot.children){
                        snapshot.child(uid)
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
                            Toast.makeText(this@StudentRoomMembers.requireContext(), "You click $position", Toast.LENGTH_SHORT).show()
                        }

                    })

                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun executeGroup() {
        databaseGroup.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    membersArrayList.clear()


                    for (postSnapShot in snapshot.children){
                        snapshot.child(uid)
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