package com.icct.icctlms.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.icct.icctlms.adapter.TeacherPostAdapter
import com.icct.icctlms.data.TeacherPostData
import com.icct.icctlms.databinding.FragmentStudentHomeBinding
import com.icct.icctlms.tools.Dialog

class StudentHome : Fragment() {

    private var _binding : FragmentStudentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var postArrayList: ArrayList<TeacherPostData>
    private lateinit var timeLineRecyclerView: RecyclerView
    private lateinit var adapter : TeacherPostAdapter
    private lateinit var uid : String
    private lateinit var dialog : Dialog
    private lateinit var studentTimeLine : DatabaseReference
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentStudentHomeBinding.inflate(inflater, container, false)
        dialog = Dialog()
        postArrayList = arrayListOf()
        timeLineRecyclerView = binding.timelineList
        timeLineRecyclerView.setHasFixedSize(true)
        timeLineRecyclerView.layoutManager = LinearLayoutManager(context)
        uid = Firebase.auth.currentUser?.uid.toString()

        studentTimeLine = FirebaseDatabase.getInstance().getReference("Student TimeLine").child(uid)

        dialog.progressDialogShow(this.requireContext(), "Loading all post, please wait...")
        timeline()

        viewTimeLine()




        return binding.root
    }

    private fun viewTimeLine() {
        studentTimeLine.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    postArrayList.clear()
                    for (each in snapshot.children){
                        val post = each.getValue(TeacherPostData::class.java)
                        postArrayList.add(post!!)
                    }
                    adapter = TeacherPostAdapter(postArrayList)
                    dialog.progressDialogHide()
                    postArrayList.sortByDescending {
                        it.sortKey
                    }
                    timeLineRecyclerView.adapter = adapter
                    if (adapter.itemCount > 0){
                        binding.noDataView.visibility = View.GONE
                    }
                }else{
                    dialog.progressDialogHide()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun timeline() {
        val getRoomID = FirebaseDatabase.getInstance().getReference("JoinClass").child(uid)
        getRoomID.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    dialog.progressDialogHide()

                    postArrayList.clear()
                    for (each in snapshot.children){
                        val roomID = each.child("roomID").value.toString()

                        //retrieve class post to timeline
                        val postToStudentTimeLine = FirebaseDatabase.getInstance().getReference("Class Post").child("Room ID: $roomID")
                        postToStudentTimeLine.addListenerForSingleValueEvent(object: ValueEventListener{
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()){
                                    viewTimeLine()
                                    for (each in snapshot.children){
                                        val date = each.child("date").value.toString()
                                        val hours = each.child("hours").value.toString()
                                        val message = each.child("message").value.toString()
                                        val name = each.child("name").value.toString()
                                        val personName = each.child("personName").value.toString()
                                        val newRoomID = each.child("roomID").value.toString()
                                        val roomPostID = each.child("roomPostID").value.toString()
                                        val sortKey = each.child("sortKey").value.toString()
                                        val type = each.child("type").value.toString()

                                        val postData = TeacherPostData(
                                            roomID = newRoomID,
                                            personName = personName,
                                            sortKey = sortKey,
                                            postID = roomPostID,
                                            name = name,
                                            date = date,
                                            hours = hours,
                                            type= type,
                                            message = message,
                                            postData = null)

                                        val studentTimeLine = FirebaseDatabase.getInstance().getReference("Student TimeLine").child(uid).child(roomPostID)
                                        studentTimeLine.setValue(postData)
                                    }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                TODO("Not yet implemented")
                            }
                        })



                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })


            //retrieve join group roomID
            val getJoinGroup = FirebaseDatabase.getInstance().getReference("JoinGroup").child(uid)
        getJoinGroup.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    for (each in snapshot.children){
                        dialog.progressDialogHide()
                        val roomID = each.child("roomID").value.toString()
                        //retrieve room group post
                        val groupTimeLine = FirebaseDatabase.getInstance().getReference("Group Post").child("Room ID: $roomID")
                        groupTimeLine.addListenerForSingleValueEvent(object: ValueEventListener{
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()){
                                    viewTimeLine()
                                    for (each in snapshot.children){
                                        val date = each.child("date").value.toString()
                                        val hours = each.child("hours").value.toString()
                                        val message = each.child("message").value.toString()
                                        val name = each.child("name").value.toString()
                                        val personName = each.child("personName").value.toString()
                                        val newRoomID = each.child("roomID").value.toString()
                                        val roomPostID = each.child("roomPostID").value.toString()
                                        val sortKey = each.child("sortKey").value.toString()
                                        val type = each.child("type").value.toString()

                                        val postData = TeacherPostData(
                                            roomID = newRoomID,
                                            personName = personName,
                                            sortKey = sortKey,
                                            postID = roomPostID,
                                            name = name,
                                            date = date,
                                            hours = hours,
                                            type= type,
                                            message = message,
                                            postData = null)

                                        val studentTimeLine = FirebaseDatabase.getInstance().getReference("Student TimeLine").child(uid).child(roomPostID)
                                        studentTimeLine.setValue(postData)
                                    }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                TODO("Not yet implemented")
                            }
                        })
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

}