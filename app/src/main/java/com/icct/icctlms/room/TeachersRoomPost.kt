package com.icct.icctlms.room

import android.app.Dialog
import android.app.ProgressDialog
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.icct.icctlms.R
import com.icct.icctlms.adapter.RoomPostAdapter
import com.icct.icctlms.data.RoomPostData
import kotlinx.android.synthetic.main.activity_room.*
import kotlinx.android.synthetic.main.fragment_student_room_post.*
import kotlinx.android.synthetic.main.fragment_teachers_room_post.*
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class TeachersRoomPost : Fragment() {
    private lateinit var dialog : Dialog
    private lateinit var progressDialog : ProgressDialog
    private lateinit var recyclerView: RecyclerView
    private lateinit var postArrayList: ArrayList<RoomPostData>
    private lateinit var roomType : String
    private lateinit var roomID : String
    private lateinit var databaseReference: DatabaseReference
    private lateinit var uid : String
    private lateinit var finalDate : String
    private lateinit var sortKey : String
    private lateinit var finalHour : String
    private lateinit var hour : String
    private lateinit var today : Calendar
    private lateinit var date : String
    private lateinit var day : String
    private lateinit var postData : String
    private lateinit var userName : String
    private lateinit var roomName : String
    private lateinit var deleteThis : DatabaseReference
    private lateinit var deleteThisPostClass : DatabaseReference
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_teachers_room_post, container, false)
        recyclerView = rootView.findViewById(R.id.teacher_room_post_list)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)
        postArrayList = arrayListOf()
        uid = FirebaseAuth.getInstance().currentUser?.uid.toString()
        roomID = arguments?.getString("teacher_roomID_").toString()
        roomType = arguments?.getString("teacher_room_type_").toString()
        roomName = arguments?.getString("teacher_room_name_").toString()



        deleteThis = FirebaseDatabase.getInstance().getReference("Group Post").child("Room ID: $roomID")
        deleteThisPostClass = FirebaseDatabase.getInstance().getReference("Class Post").child("Room ID: $roomID")
        databaseReference = FirebaseDatabase.getInstance().getReference("Teachers").child(uid)
        databaseReference.get().addOnSuccessListener {
            if (it.exists()){
                userName = it.child("name").value.toString()
            }
        }


        val now = LocalDateTime.now()
        today = Calendar.getInstance()
        sortKey = now.toMillis().toString()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            date = SimpleDateFormat("MMMM d, y", Locale.getDefault()).format(today.time).toString()
        }
        day = SimpleDateFormat("EEEE", Locale.getDefault()).format(today.time).toString()
        finalDate = "$day, $date"
        val time = LocalTime.now()
        val pattern = "hh:mm a"
        hour = LocalTime.now().toString()
        finalHour = time.format(DateTimeFormatter.ofPattern(pattern))

        Toast.makeText(this.requireContext(), roomType, Toast.LENGTH_SHORT).show()


        executePost()
        

        return rootView
    }

    private fun executePost() {
        if(roomType == "Group"){
            databaseReference = FirebaseDatabase.getInstance().getReference("Group Post").child("Room ID: $roomID")
            databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        postArrayList.clear()

                        for (postSnapShot in snapshot.children){
                            snapshot.child(uid)
                            val subjectTitle = postSnapShot.getValue(RoomPostData::class.java)
                            postArrayList.add(subjectTitle!!)
                        }
                        val adapter = RoomPostAdapter(postArrayList)
                        postArrayList.sortByDescending {
                            it.sortKey
                        }
                        recyclerView.adapter = adapter






//adapter click listener
                        adapter.setOnItemClickListener(object : RoomPostAdapter.onItemClickListener{
                            override fun onItemClick(position: Int) {
                                val message = postArrayList[position].message.toString()
                                val myInflater = layoutInflater
                                val myLayout = myInflater.inflate(R.layout.update_post, null)
                                val newMessage = myLayout.findViewById<EditText>(R.id.update_post)
                                val roomID = postArrayList[position].roomID
                                val postData = postArrayList[position].roomPostID.toString()
                                newMessage.setText(message)
                                MaterialAlertDialogBuilder(requireContext())
                                    .setMessage("Current post: $message")
                                    .setPositiveButton("Update"){_,_ ->
                                        MaterialAlertDialogBuilder(requireContext())
                                            .setView(myLayout)
                                            .setTitle("Update post")
                                            .setPositiveButton("Post"){_,_ ->

                                                val myMessage = newMessage.text.toString()
                                                val data = RoomPostData(roomID, roomName, sortKey, postData, userName, finalDate, finalHour, "Teacher", myMessage)
                                                val updateData = FirebaseDatabase.getInstance().getReference("Group Post").child("Room ID: $roomID")
                                                updateData.child(postData).setValue(data)
                                                executePost()
                                                showToast()
                                            }.setNegativeButton("Cancel"){_,_ ->

                                            }.show()
                                    }.setNegativeButton("Cancel"){_,_ ->

                                    }.setNeutralButton("Delete"){_,_->
                                        MaterialAlertDialogBuilder(this@TeachersRoomPost.requireContext())
                                            .setMessage("Are you sure you want to delete this post?\n \n" +
                                                    message
                                            )
                                            .setPositiveButton("OK"){_,_->
                                                deleteThis.child(postData).removeValue().addOnSuccessListener {
                                                    showToastMessage("$message has been deleted.")
                                                }
                                                adapter.deleteItem(position)

                                            }.setNegativeButton("Cancel"){_,_ ->

                                            }.show()
                                    }.show()
                            }

                        })


                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
        }else if(roomType == "Class"){
            databaseReference = FirebaseDatabase.getInstance().getReference("Class Post").child("Room ID: $roomID")
            databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        postArrayList.clear()

                        for (postSnapShot in snapshot.children){
                            snapshot.child(uid)
                            val subjectTitle = postSnapShot.getValue(RoomPostData::class.java)
                            postArrayList.add(subjectTitle!!)
                        }
                        val adapter = RoomPostAdapter(postArrayList)
                        postArrayList.sortByDescending {
                            it.sortKey
                        }
                        recyclerView.adapter = adapter





//adapter click listener
                        adapter.setOnItemClickListener(object : RoomPostAdapter.onItemClickListener{
                            override fun onItemClick(position: Int) {
                                val message = postArrayList[position].message.toString()
                                val myInflater = layoutInflater
                                val myLayout = myInflater.inflate(R.layout.update_post, null)
                                val newMessage = myLayout.findViewById<EditText>(R.id.update_post)
                                val postData = postArrayList[position].roomPostID.toString()
                                newMessage.setText(message)
                                MaterialAlertDialogBuilder(requireContext())
                                    .setMessage("Current post: $message")
                                    .setPositiveButton("Update"){_,_ ->
                                        MaterialAlertDialogBuilder(requireContext())
                                            .setView(myLayout)
                                            .setTitle("Update post")
                                            .setPositiveButton("Post"){_,_ ->

                                                val myMessage = newMessage.text.toString()
                                                val data = RoomPostData(roomID, userName, sortKey, postData, roomName, finalDate, finalHour, "Teacher", myMessage)
                                                val updateData = FirebaseDatabase.getInstance().getReference("Class Post").child("Room ID: $roomID")
                                                updateData.child(postData).setValue(data)
                                                executePost()
                                                showToast()
                                            }.setNegativeButton("Cancel"){_,_ ->

                                            }.show()
                                    }.setNegativeButton("Cancel"){_,_ ->

                                    }.setNeutralButton("Delete"){_,_->
                                        MaterialAlertDialogBuilder(this@TeachersRoomPost.requireContext())
                                            .setMessage("Are you sure you want to delete this post?\n \n" +
                                                    message
                                            )
                                            .setPositiveButton("OK"){_,_->
                                                deleteThisPostClass.child(postData).removeValue().addOnSuccessListener {
                                                    showToastMessage("$message has been deleted.")
                                                }
                                                adapter.deleteItem(position)

                                            }.setNegativeButton("Cancel"){_,_ ->

                                            }.show()
                                    }.show()
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        buttonAction()
        teacher_post_message.setOnClickListener{
            val view = layoutInflater.inflate(R.layout.teacher_post_view, null)
            val etMessage : EditText = view.findViewById(R.id.teacher_post_view_et_message)

            MaterialAlertDialogBuilder(this.requireContext())
                .setView(view)
                .setTitle("Create post")
                .setPositiveButton("Post"){_,_ ->
                    if(roomType == "Group"){
                        progressDialogShow()
                        val databaseUserTeacher = FirebaseDatabase.getInstance().getReference("Teachers")
                        databaseUserTeacher.child(uid).get().addOnSuccessListener {
                            if (it.exists()){
                                postData = randomID()
                                val message = etMessage.text.toString()
                                val userName = it.child("name").value.toString()
                                val data = RoomPostData(roomID, userName, sortKey, postData, roomName, finalDate, finalHour, "Teacher", message)
                                val databaseRoomPost = FirebaseDatabase.getInstance().getReference("Group Post")
                                databaseRoomPost.child("Room ID: $roomID").child(postData).setValue(data).addOnSuccessListener {
                                    progressDialogHide()
                                    executePost()
                                    teacher_post_message.visibility = View.GONE
                                    teacher_post_message_text.visibility = View.GONE
                                    teacher_wall.setBackgroundResource(R.color.zero)

                                }
                            }
                        }
                    }else if(roomType == "Class"){
                        progressDialogShow()
                        val databaseUserTeacher = FirebaseDatabase.getInstance().getReference("Teachers")
                        databaseUserTeacher.child(uid).get().addOnSuccessListener {
                            if (it.exists()){
                                postData = randomID()
                                val message = etMessage.text.toString()
                                val userName = it.child("name").value.toString()
                                val data = RoomPostData(roomID, userName, sortKey, postData, roomName, finalDate, finalHour, "Teacher", message)
                                val databaseRoomPost = FirebaseDatabase.getInstance().getReference("Class Post")
                                databaseRoomPost.child("Room ID: $roomID").child(postData).setValue(data).addOnSuccessListener {
                                    progressDialogHide()
                                    executePost()
                                    teacher_post_message.visibility = View.GONE
                                    teacher_post_message_text.visibility = View.GONE
                                    teacher_wall.setBackgroundResource(R.color.zero)

                                }
                            }
                        }
                    }



                }.show()

        }
    }

    private fun buttonAction() {
        teacher_post_message.visibility = View.GONE
        teacher_post_message_text.visibility = View.GONE

        var isAllFabVisible = false


        teacher_post_btn.setOnClickListener{
            if (!isAllFabVisible){
                teacher_wall.setBackgroundResource(R.color.transparent_color)
                teacher_wall.invalidate()
                val fadeIn = AnimationUtils.loadAnimation(context, R.anim.fade_in)
                teacher_wall.startAnimation(fadeIn)
                teacher_post_btn.startAnimation(AnimationUtils.loadAnimation(this.requireContext(), R.anim.rotate))
                teacher_post_message.show()
                teacher_post_message_text.visibility = View.VISIBLE

                isAllFabVisible = true
            }else{
                teacher_wall.setBackgroundResource(R.color.zero)
                teacher_wall.invalidate()
                teacher_post_btn.startAnimation(AnimationUtils.loadAnimation(this.requireContext(), R.anim.rotate))
                teacher_post_message.hide()
                teacher_post_message_text.visibility = View.GONE

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

    @RequiresApi(Build.VERSION_CODES.O)
    fun LocalDateTime.toMillis(zone: ZoneId = ZoneId.systemDefault()) = atZone(zone)?.toInstant()?.toEpochMilli()

    private fun showToast(){
        Toast.makeText(this.requireContext(), "Posting an update was successful.", Toast.LENGTH_SHORT).show()
    }
    private fun showToastMessage(message : String){
        Toast.makeText(this.requireContext(), message, Toast.LENGTH_SHORT).show()
    }
    private fun randomID(): String = List(16) {
        (('a'..'z') + ('A'..'Z') + ('0'..'9')).random()
    }.joinToString("")
}