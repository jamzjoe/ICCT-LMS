package com.icct.icctlms.fragments

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.icct.icctlms.R
import com.icct.icctlms.adapter.RecipientAdapter
import com.icct.icctlms.data.GroupListData
import com.icct.icctlms.data.RecipientData
import com.icct.icctlms.databinding.FragmentMessageBinding
import com.icct.icctlms.messages.StudentMessageRoom
import kotlin.collections.ArrayList

class Message : Fragment() {
    private var _binding : FragmentMessageBinding? = null
    private val binding get() = _binding!!
    private lateinit var recipientRecyclerView: RecyclerView
    private lateinit var recipientRecyclerView2: RecyclerView
    private lateinit var recipientArrayList: ArrayList<RecipientData>
    private lateinit var recipientArrayList2: ArrayList<RecipientData>
    private lateinit var uid : String
    private lateinit var getJoinClassTeachers : DatabaseReference
    private lateinit var getJoinGroupTeachers : DatabaseReference
    private lateinit var dialog: Dialog


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMessageBinding.inflate(inflater, container, false)
        uid = Firebase.auth.currentUser?.uid.toString()
        recipientRecyclerView = binding.recipientList
        recipientRecyclerView.setHasFixedSize(true)
        recipientRecyclerView.layoutManager = LinearLayoutManager(context)

        recipientRecyclerView2 = binding.recipientList2
        recipientRecyclerView2.setHasFixedSize(true)
        recipientRecyclerView2.layoutManager = LinearLayoutManager(context)

        recipientArrayList = arrayListOf()
        recipientArrayList2 = arrayListOf()

        progressDialogShow()
        getJoinClassTeachers = FirebaseDatabase.getInstance().getReference("JoinClass").child(uid)
        executeClassRecipient()
        getJoinGroupTeachers = FirebaseDatabase.getInstance().getReference("JoinGroup").child(uid)
        executeGroupRecipient()
        return binding.root
        // Inflate the layout for this fragment
    }

    private fun executeClassRecipient() {
        getJoinClassTeachers.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    recipientArrayList.clear()


                    for (postSnapShot in snapshot.children){
                        val members = postSnapShot.getValue(RecipientData::class.java)
                        recipientArrayList.add(members!!)
                    }
                    val adapter = RecipientAdapter(recipientArrayList)
                    recipientArrayList.sortByDescending {
                        it.name
                    }
                    recipientRecyclerView.adapter = adapter
                    progressDialogHide()
                    //adapter click listener
                    adapter.setOnItemClickListener(object : RecipientAdapter.onItemClickListener{
                        override fun onItemClick(position: Int) {
                            val teacherUID = recipientArrayList[position].uid
                            val profName = recipientArrayList[position].name
                            val roomID = recipientArrayList[position].roomID
                            val intent = (Intent(this@Message.requireContext(), StudentMessageRoom::class.java))
                            intent.putExtra("teacher_name", profName)
                            intent.putExtra("teacher_uid", teacherUID)
                            intent.putExtra("room_id", roomID)
                            startActivity(intent)
                        }

                    })

                }else{
                    progressDialogHide()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun executeGroupRecipient() {
        getJoinGroupTeachers.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    recipientArrayList2.clear()


                    for (postSnapShot in snapshot.children){
                        val members = postSnapShot.getValue(RecipientData::class.java)
                        recipientArrayList2.add(members!!)
                    }
                    val adapter = RecipientAdapter(recipientArrayList2)
                    recipientArrayList2.sortByDescending {
                        it.name
                    }
                    recipientRecyclerView2.adapter = adapter
                    progressDialogHide()
                    //adapter click listener
                    adapter.setOnItemClickListener(object : RecipientAdapter.onItemClickListener{
                        override fun onItemClick(position: Int) {
                            val teacherUID = recipientArrayList2[position].uid
                            val profName = recipientArrayList2[position].name
                            val roomID = recipientArrayList2[position].roomID
                            val intent = (Intent(this@Message.requireContext(), StudentMessageRoom::class.java))
                            intent.putExtra("teacher_name", profName)
                            intent.putExtra("teacher_uid", teacherUID)
                            intent.putExtra("room_id", roomID)
                            startActivity(intent)
                        }

                    })

                }else{
                    progressDialogHide()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }
    private fun progressDialogShow(){
        dialog = Dialog(this.requireContext())
        dialog.setContentView(R.layout.dialog_layout)
        dialog.setTitle("Loading please wait")
        dialog.setCancelable(false)
        dialog.show()
    }
    private fun progressDialogHide(){
        dialog.hide()
    }
}