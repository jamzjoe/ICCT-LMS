package com.icct.icctlms.room

import android.app.Dialog
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.icct.icctlms.R
import com.icct.icctlms.adapter.ClassRoomPostAdapter
import com.icct.icctlms.adapter.GroupRoomPostAdapter
import com.icct.icctlms.adapter.RoomPostAdapter
import com.icct.icctlms.data.RoomPostData
import com.icct.icctlms.gestures.SwipeGestures
import kotlinx.android.synthetic.main.activity_room.*
import kotlinx.android.synthetic.main.fragment_student_room_post.*
import kotlinx.android.synthetic.main.fragment_teacher_home.*
import java.io.File
import java.text.SimpleDateFormat
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class StudentRoomPost : Fragment() {

    private lateinit var roomCode : String
    private lateinit var uid: String
    private lateinit var name: String
    private lateinit var roomID: String
    private lateinit var section : String
    private lateinit var postArrayList: ArrayList<RoomPostData>
    private lateinit var database: DatabaseReference
    private lateinit var recyclerView: RecyclerView
    private lateinit var postID : String
    private lateinit var personName : String
    private lateinit var storageReference : StorageReference
    private lateinit var profile : ImageView
    private lateinit var createPostButton : Button
    private lateinit var etContainer : TextInputLayout
    private lateinit var submit : Button
    private lateinit var backButton : ImageView
    private lateinit var postData: String
    private lateinit var sortKey : String
    private lateinit var pattern : String
    private lateinit var finalDate : String
    private lateinit var finalHour : String
    private lateinit var hour : String
    private lateinit var auth : FirebaseAuth
    private lateinit var userID : String
    private lateinit var roomType : String
    private lateinit var dialog : Dialog
    private lateinit var today : Calendar
    private lateinit var day : String
    private lateinit var date : String
    private lateinit var privacy : String
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val rootView = inflater.inflate(R.layout.fragment_student_room_post, container, false)
        // Inflate the layout for this fragment
        profile = rootView.findViewById(R.id.room_post_pic)
        submit = rootView.findViewById(R.id.room_submit)
        recyclerView = rootView.findViewById(R.id.student_room_post_list)
        createPostButton = rootView.findViewById(R.id.room_create)
        etContainer = rootView.findViewById(R.id.etContainer)

        backButton = rootView.findViewById(R.id.back_create)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)
        postArrayList = arrayListOf()
        today = Calendar.getInstance()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            date = SimpleDateFormat("MMMM d, y").format(today.time).toString()
        }
        day = SimpleDateFormat("EEEE").format(today.time).toString()
        pattern = "hh:mm a"
        val now = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDateTime.now()
        } else {
            TODO("VERSION.SDK_INT < O")
        }
        sortKey = now.toMillis().toString()
        finalDate = "$day, $date"
        hour = LocalTime.now().toString()
        val time = LocalTime.now()
        finalHour = time.format(DateTimeFormatter.ofPattern(pattern))
        auth = FirebaseAuth.getInstance()
        userID = auth.currentUser?.uid.toString()







        roomCode = arguments?.getString("student_roomCode_").toString()
        uid = arguments?.getString("student_uid_").toString()
        name = arguments?.getString("student_name_").toString()
        roomID = arguments?.getString("student_roomID_").toString()
        section = arguments?.getString("student_section_").toString()
        personName = arguments?.getString("student_person_name").toString()
        roomType = arguments?.getString("student_room_type_").toString()
        postID = randomCode()
        postData = randomID()




   executeThis()



        showProfile()
        showPost()


        return rootView

    }

    private fun executeThis() {
        if(roomType == "Group"){
            database = FirebaseDatabase.getInstance().getReference("Group Post").child("Room ID: $roomID")
            database.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        postArrayList.clear()

                        for (postSnapShot in snapshot.children){
                            val subjectTitle = postSnapShot.getValue(RoomPostData::class.java)
                            postArrayList.add(subjectTitle!!)
                        }
                        val adapter = GroupRoomPostAdapter(postArrayList)
                        postArrayList.sortByDescending {
                            it.sortKey
                        }
                        recyclerView.adapter = adapter
                        if (adapter.itemCount > 0){
                            room_no_data_view.visibility = View.GONE
                        }
                        val swipeGestures = object : SwipeGestures(this@StudentRoomPost.requireContext()){

                            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                                val position : Int = viewHolder.adapterPosition
                                when(direction){
                                    ItemTouchHelper.LEFT -> {
                                        MaterialAlertDialogBuilder(this@StudentRoomPost.requireContext())
                                            .setMessage("Are you certain to remove this post?")
                                            .setPositiveButton("Okay"){_,_ ->
                                                val postData = postArrayList[position].roomPostID.toString()
                                                val roomID = postArrayList[position].roomID.toString()
                                                val type = postArrayList[position].type
                                                if(type != "Teacher"){
                                                    val deleteGroup = FirebaseDatabase.getInstance().getReference("Group Post").child("Room ID: $roomID")
                                                    deleteGroup.child(postData).removeValue().addOnSuccessListener {
                                                        adapter.deleteItem(position)
                                                        recyclerView.adapter?.notifyItemRemoved(position)
                                                        alert("Deleted successfully!")
                                                    }.addOnFailureListener{
                                                    }
                                                }else{
                                                    alert("You do not have the authority to remove this post.")
                                                    executeThis()
                                                }

                                            }.setNegativeButton("Cancel"){_, _ ->
                                                executeThis()
                                            }.show()

                                    }
                                }



                            }
                        }

                        val touchHelper = ItemTouchHelper(swipeGestures)
                        touchHelper.attachToRecyclerView(recyclerView)






//adapter click listener
                        adapter.setOnItemClickListener(object : GroupRoomPostAdapter.onItemClickListener{
                            override fun onItemClick(position: Int) {
                                val message = postArrayList[position].message.toString()
                                MaterialAlertDialogBuilder(requireContext())
                                    .setTitle("Message")
                                    .setMessage(message)
                                    .setPositiveButton("Update"){_,_ ->
                                        alert("In development, stay updated.")
                                    }.setNegativeButton("Cancel"){_,_ ->

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
            database = FirebaseDatabase.getInstance().getReference("Class Post").child("Room ID: $roomID")
            database.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        postArrayList.clear()

                        for (postSnapShot in snapshot.children){
                            val subjectTitle = postSnapShot.getValue(RoomPostData::class.java)
                            postArrayList.add(subjectTitle!!)
                        }
                        val adapter = ClassRoomPostAdapter(postArrayList)
                        postArrayList.sortByDescending {
                            it.sortKey
                        }
                        recyclerView.adapter = adapter
                        if (adapter.itemCount > 0){
                            room_no_data_view.visibility = View.GONE
                        }
                        val swipeGestures = object : SwipeGestures(this@StudentRoomPost.requireContext()){

                            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                                val position : Int = viewHolder.adapterPosition
                                when(direction){
                                    ItemTouchHelper.LEFT -> {
                                        MaterialAlertDialogBuilder(this@StudentRoomPost.requireContext())
                                            .setMessage("Are you certain to remove this post?")
                                            .setPositiveButton("Okay"){_,_ ->
                                                val postData = postArrayList[position].roomPostID.toString()
                                                val roomID = postArrayList[position].roomID.toString()
                                                val type = postArrayList[position].type
                                                if(type != "Teacher"){
                                                    val deleteGroup = FirebaseDatabase.getInstance().getReference("Class Post").child("Room ID: $roomID")
                                                    deleteGroup.child(postData).removeValue().addOnSuccessListener {
                                                        adapter.deleteItem(position)
                                                        recyclerView.adapter?.notifyItemRemoved(position)
                                                        alert("Deleted successfully!")
                                                    }.addOnFailureListener{
                                                    }
                                                }else{
                                                    alert("You do not have the authority to remove this post.")
                                                    executeThis()
                                                }

                                            }.setNegativeButton("Cancel"){_, _ ->
                                                executeThis()
                                            }.show()

                                    }
                                }



                            }
                        }

                        val touchHelper = ItemTouchHelper(swipeGestures)
                        touchHelper.attachToRecyclerView(recyclerView)






//adapter click listener
                        adapter.setOnItemClickListener(object : ClassRoomPostAdapter.onItemClickListener{
                            override fun onItemClick(position: Int) {
                                val message = postArrayList[position].message.toString()
                                MaterialAlertDialogBuilder(requireContext())
                                    .setTitle("Message")
                                    .setMessage(message)
                                    .setPositiveButton("Update"){_,_ ->
                                        alert("In development, stay updated.")
                                    }.setNegativeButton("Cancel"){_,_ ->

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
        student_post_message.setOnClickListener{
            val view = layoutInflater.inflate(R.layout.post_view, null)
            val etMessage : EditText = view.findViewById(R.id.post_view_et_message)

            MaterialAlertDialogBuilder(this.requireContext())
                .setView(view)
                .setTitle("Create post")
                .setPositiveButton("Post"){_,_ ->


                    if(roomType == "Group"){
                        val getPrivacy = FirebaseDatabase.getInstance().getReference("Public Group").child(roomID)
                        getPrivacy.child("Privacy").get().addOnSuccessListener {
                            privacy = it.child("privacy").value.toString()
                            if (privacy != "true"){
                                progressDialogShow()
                                val databaseUserStudent = FirebaseDatabase.getInstance().getReference("Students")
                                databaseUserStudent.child(uid).get().addOnSuccessListener {
                                    if (it.exists()){
                                        postData = randomID()
                                        val message = etMessage.text.toString()
                                        val data = RoomPostData(roomID, personName, sortKey, postData, name, finalDate, finalHour, "Student", message)
                                        val databaseRoomPost = FirebaseDatabase.getInstance().getReference("Group Post")
                                        databaseRoomPost.child("Room ID: $roomID").child(postData).setValue(data).addOnSuccessListener {
                                            progressDialogHide()
                                            executeThis()
                                            hideThis()

                                        }
                                    }
                                }
                            }else
                            {
                                showPop()
                                hideThis()
                            }
                        }

                    }else if(roomType == "Class"){
                        val getPrivacy = FirebaseDatabase.getInstance().getReference("Public Class").child(roomID)
                        getPrivacy.child("Privacy").get().addOnSuccessListener {
                            privacy = it.child("privacy").value.toString()
                            if(privacy != "true"){
                                progressDialogShow()
                                val databaseUserStudent = FirebaseDatabase.getInstance().getReference("Students")
                                databaseUserStudent.child(uid).get().addOnSuccessListener {
                                    if (it.exists()){
                                        postData = randomID()
                                        val message = etMessage.text.toString()
                                        val userName = it.child("name").value.toString()
                                        val subjectName = it.child("subjectTitle").value.toString()
                                        val data = RoomPostData(roomID, personName, sortKey, postData, name, finalDate, finalHour, "Student", message)
                                        val databaseRoomPost = FirebaseDatabase.getInstance().getReference("Class Post")
                                        databaseRoomPost.child("Room ID: $roomID").child(postData).setValue(data).addOnSuccessListener {
                                            progressDialogHide()
                                            executeThis()
                                            hideThis()

                                        }
                                    }
                                }
                            }else{
                                showPop()
                                hideThis()
                            }
                        }

                    }



                }.show()

        }
    }

    private fun alert(message : String){
        MaterialAlertDialogBuilder(this.requireContext())
            .setMessage(message)
            .setPositiveButton("Okay"){_,_ ->

            }
            .show()
    }

    private fun hideThis(){
        student_post_message.visibility = View.GONE
        student_post_message_text.visibility = View.GONE
        student_wall.setBackgroundResource(R.color.zero)
    }
    private fun showPop(){
        MaterialAlertDialogBuilder(this.requireContext())
            .setMessage("Your teacher has set the permissions for posting to private.")
            .show()
    }


    private fun showPost() {
        createPostButton.setOnClickListener{
            createPostButton.visibility = View.GONE
            profile.visibility = View.VISIBLE
            etContainer.visibility = View.VISIBLE
            submit.visibility = View.VISIBLE
            backButton.visibility = View.VISIBLE
        }
        backButton.setOnClickListener{
            createPostButton.visibility = View.VISIBLE
            profile.visibility = View.GONE
            etContainer.visibility = View.GONE
            submit.visibility = View.GONE
            backButton.visibility = View.GONE
        }
    }

    private fun showProfile() {
        storageReference = FirebaseStorage.getInstance().getReference("Teachers/$uid")
        val localFile = File.createTempFile("tempImage", "jpg")
        storageReference.getFile(localFile).addOnSuccessListener {
            val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
            profile.setImageBitmap(bitmap)



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


    private fun buttonAction() {
        student_post_message.visibility = View.GONE
        student_post_message_text.visibility = View.GONE

        var isAllFabVisible = false


        student_post_btn.setOnClickListener{
            if (!isAllFabVisible){
                student_wall.setBackgroundResource(R.color.transparent_color)
                student_wall.invalidate()
                val fadeIn = AnimationUtils.loadAnimation(context, R.anim.fade_in)
                student_wall.startAnimation(fadeIn)
                student_post_btn.startAnimation(AnimationUtils.loadAnimation(this.requireContext(), R.anim.rotate))
                student_post_message.show()
                student_post_message_text.visibility = View.VISIBLE

                isAllFabVisible = true
            }else{
                student_wall.setBackgroundResource(R.color.zero)
                student_wall.invalidate()
                student_post_btn.startAnimation(AnimationUtils.loadAnimation(this.requireContext(), R.anim.rotate))
                student_post_message.hide()
                student_post_message_text.visibility = View.GONE

                isAllFabVisible = false
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun LocalDateTime.toMillis(zone: ZoneId = ZoneId.systemDefault()) = atZone(zone)?.toInstant()?.toEpochMilli()
    private fun randomID(): String = List(16) {
        (('a'..'z') + ('A'..'Z') + ('0'..'9')).random()
    }.joinToString("")
    private fun randomCode(): String = List(6) {
        (('a'..'z') + ('A'..'Z') + ('0'..'9')).random()
    }.joinToString("")


}