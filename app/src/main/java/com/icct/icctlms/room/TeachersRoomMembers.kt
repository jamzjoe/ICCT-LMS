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
import com.icct.icctlms.data.RoomMembersData


class TeachersRoomMembers : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var databaseGroup: DatabaseReference
    private lateinit var databaseClass: DatabaseReference
    private lateinit var membersArrayList: ArrayList<RoomMembersData>
    private lateinit var roomID : String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_teachers_room_members, container, false)
        roomID = arguments?.getString("teacher_roomID_").toString()
        recyclerView = rootView.findViewById(R.id.teacher_room_members)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)
        membersArrayList = arrayListOf()


        databaseGroup = FirebaseDatabase.getInstance().getReference("Public Group").child(roomID).child("Members")
        databaseClass = FirebaseDatabase.getInstance().getReference("Public Class").child(roomID).child("Members")
        executeGroupMembers()
        executeClassMembers()

        return rootView
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

                    //adapter click listener
                    adapter.setOnItemClickListener(object : MembersAdapter.onItemClickListener{
                        override fun onItemClick(position: Int) {
                            val studentName = membersArrayList[position].name
                            val uid = membersArrayList[position].uid.toString()
                            MaterialAlertDialogBuilder(this@TeachersRoomMembers.requireContext())
                                .setTitle("Kick Student")
                                .setMessage("Are you sure you want to kick $studentName ?")
                                .setPositiveButton("CONFIRM"){_,_ ->
                                    val deleteMember = FirebaseDatabase.getInstance().getReference("Public Class").child(roomID).child("Members")
                                    deleteMember.child(uid).removeValue().addOnSuccessListener {
                                        val joinClass = FirebaseDatabase.getInstance().getReference("JoinClass")
                                            .child(uid)
                                        joinClass.child(roomID).removeValue().addOnSuccessListener {
                                            Toast.makeText(this@TeachersRoomMembers.requireContext(), "$studentName kicked successfully!", Toast.LENGTH_SHORT).show()
                                            executeClassMembers()
                                        }

                                    }

                                }
                                .setNegativeButton("CANCEL"){_,_ ->

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
                            val uid = membersArrayList[position].uid.toString()
                            MaterialAlertDialogBuilder(this@TeachersRoomMembers.requireContext())
                                .setTitle("Kick Student")
                                .setMessage("Are you sure you want to kick $studentName ?")
                                .setPositiveButton("CONFIRM"){_,_ ->
                                    val deleteMember = FirebaseDatabase.getInstance().getReference("Public Group").child(roomID).child("Members")
                                    deleteMember.child(uid).removeValue().addOnSuccessListener {
                                        val joinGroup = FirebaseDatabase.getInstance().getReference("JoinGroup")
                                            .child(uid)
                                        joinGroup.child(roomID).removeValue().addOnSuccessListener {
                                            Toast.makeText(
                                                this@TeachersRoomMembers.requireContext(),
                                                "$studentName kicked successfully!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            executeGroupMembers()
                                        }
                                    }


                                }
                                .setNegativeButton("CANCEL"){_,_ ->

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