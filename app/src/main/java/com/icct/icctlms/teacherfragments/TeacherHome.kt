package com.icct.icctlms.teacherfragments

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.icct.icctlms.Authentication.Teacher.CreateTeacherPost
import com.icct.icctlms.R
import com.icct.icctlms.adapter.TeacherPostAdapter
import com.icct.icctlms.data.RoomIDData
import com.icct.icctlms.data.TeacherPostData
import com.icct.icctlms.databinding.FragmentTeacherHomeBinding
import kotlinx.android.synthetic.main.fragment_teacher_class.*
import kotlinx.android.synthetic.main.fragment_teacher_home.*





class TeacherHome : Fragment() {

    private lateinit var postArrayList: ArrayList<TeacherPostData>
    private lateinit var recyclerView: RecyclerView
    private lateinit var dialog : Dialog
    private lateinit var uid : String
    private var _binding : FragmentTeacherHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var postButton : FloatingActionButton
    private lateinit var classButton : FloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTeacherHomeBinding.inflate(inflater, container, false)
        postButton = binding.postMessage
        classButton = binding.postClass
        recyclerView = binding.postList

        uid = Firebase.auth.currentUser?.uid.toString()
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)
        postArrayList = arrayListOf()
        progressDialogShow()
        timeline()
        viewTimeLine()

        buttons()

        return binding.root
    }

    private fun buttons() {
        postButton.setOnClickListener{
            val intent = Intent(this.requireContext(), CreateTeacherPost::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }
        classButton.setOnClickListener{
            MaterialAlertDialogBuilder(this.requireContext())
                .setTitle("NOTE FROM DEVELOPER")
                .setMessage("Relax and chill; the developer is currently watching K drama. Keep yourself up to date.")
                .setPositiveButton("Okay"){_,_ ->

                }.show()
        }
    }

    private fun viewTimeLine() {
        val teacherTimeLine = FirebaseDatabase.getInstance().getReference("Teacher TimeLine").child(uid)
        teacherTimeLine.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    postArrayList.clear()
                    progressDialogHide()
                    for (each in snapshot.children){
                        val post = each.getValue(TeacherPostData::class.java)
                        postArrayList.add(post!!)
                    }
                    val adapter = TeacherPostAdapter(postArrayList)
                    postArrayList.sortByDescending {
                        it.sortKey
                    }
                    recyclerView.adapter = adapter
                    if (adapter.itemCount > 0){
                        binding.noDataView.visibility = View.GONE
                    }
                }else{
                    progressDialogHide()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun timeline() {
        //retrieve class room post
        val getRoomID = FirebaseDatabase.getInstance().getReference("Class").child(uid)
        getRoomID.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    progressDialogHide()
                    postArrayList.clear()
                    for (each in snapshot.children){
                        val roomID = each.child("roomID").value.toString()
                        val postToStudentTimeLine = FirebaseDatabase.getInstance().getReference("Class Post").child("Room ID: $roomID")

                        //retrieve class post in a room
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

                                        val studentTimeLine = FirebaseDatabase.getInstance().getReference("Teacher TimeLine").child(uid).child(roomPostID)
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

        //retrieve group room post
        val getGroupID = FirebaseDatabase.getInstance().getReference("Group").child(uid)
        getGroupID.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    progressDialogHide()
                    for (each in snapshot.children){
                        val roomID = each.child("roomID").value.toString()

                        //retrieve group post in a room
                        val teacherTimeLine = FirebaseDatabase.getInstance().getReference("Group Post").child("Room ID: $roomID")
                        teacherTimeLine.addListenerForSingleValueEvent(object: ValueEventListener{
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

                                        val studentTimeLine = FirebaseDatabase.getInstance().getReference("Teacher TimeLine").child(uid).child(roomPostID)
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



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        post_message.visibility = View.GONE
        post_message_text.visibility = View.GONE
        post_class.visibility = View.GONE
        post_class_text.visibility = View.GONE

        var isAllFabVisible = false

        post_btn.setOnClickListener{
            if (!isAllFabVisible){
                change_opacity.setBackgroundResource(R.color.transparent_color)
                change_opacity.invalidate()
                val fadeIn = AnimationUtils.loadAnimation(context, R.anim.fade_in)
                change_opacity.startAnimation(fadeIn)
                post_btn.startAnimation(AnimationUtils.loadAnimation(context, R.anim.rotate))
                Handler(Looper.myLooper()!!).postDelayed({
                    post_message_text.visibility = View.VISIBLE
                    post_message.show()
                },140)
                Handler(Looper.myLooper()!!).postDelayed({
                    post_class.show()
                    post_class_text.visibility = View.VISIBLE
                },130)


                isAllFabVisible = true
            }else{
                post_btn.startAnimation(AnimationUtils.loadAnimation(context, R.anim.rotate))
                change_opacity.setBackgroundResource(R.color.zero)
                change_opacity.invalidate()
                val fadeOut = AnimationUtils.loadAnimation(context, R.anim.fade_out)
                change_opacity.startAnimation(fadeOut)
                post_class.hide()
                post_message.hide()
                post_class_text.visibility = View.GONE
                post_message_text.visibility = View.GONE

                isAllFabVisible = false
            }
        }
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