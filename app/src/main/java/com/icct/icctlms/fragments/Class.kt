package com.icct.icctlms.fragments

import android.app.Dialog
import android.content.Intent
import android.opengl.Visibility
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.icct.icctlms.Authentication.Admin.ui.home.HomeViewModel
import com.icct.icctlms.Authentication.Teacher.TeacherMainActivity
import com.icct.icctlms.R
import com.icct.icctlms.StudentRoomActivity
import com.icct.icctlms.adapter.StudentClassAdapter
import com.icct.icctlms.adapter.StudentGroupAdapter
import com.icct.icctlms.data.CreateClassData
import com.icct.icctlms.data.GroupListData
import com.icct.icctlms.data.RoomMembersData
import com.icct.icctlms.database.Notification
import com.icct.icctlms.databinding.FragmentClassBinding
import com.icct.icctlms.gestures.SwipeGestures
import kotlinx.android.synthetic.main.fragment_class.*
import kotlinx.android.synthetic.main.fragment_teacher_class.*
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class Class : Fragment() {
    private lateinit var joinClass : DatabaseReference
    private lateinit var databaseReference : DatabaseReference
    private lateinit var groupRecyclerView: RecyclerView
    private lateinit var classRecyclerView : RecyclerView
    private lateinit var classArrayList : ArrayList<CreateClassData>
    private lateinit var groupArrayList: ArrayList<GroupListData>
    private lateinit var dialog: Dialog
    private lateinit var uid : String
    private lateinit var roomID : String
    private lateinit var bottomNav : BottomNavigationView
    private var _binding: FragmentClassBinding? = null
    private lateinit var hour : String
    private lateinit var finalHour : String
    private lateinit var today : Calendar
    private lateinit var sortKey : String
    private lateinit var date : String
    private lateinit var dateReviewed : String

    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentClassBinding.inflate(inflater, container, false)
        val root: View = binding.root
        groupRecyclerView = binding.studentGroupList
        groupRecyclerView.setHasFixedSize(true)
        binding.groupThats.visibility = View.GONE
        groupRecyclerView.visibility = View.GONE
        groupRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        groupArrayList = ArrayList()
        classRecyclerView = binding.studentClassList
        classRecyclerView.setHasFixedSize(true)
        classRecyclerView.layoutManager = LinearLayoutManager(context)
        classArrayList = ArrayList()
        val auth = FirebaseAuth.getInstance()
        uid = auth.currentUser?.uid.toString()

        progressDialogShow()

        databaseReference = FirebaseDatabase.getInstance().getReference("JoinGroup").child(uid)
        executeGroup()


        //join class
        joinClass = FirebaseDatabase.getInstance().getReference("JoinClass").child(uid)
        executeClass()
        binding.classBtn.setBackgroundResource(R.drawable.bottom_rec)

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
        date = "$trimMonth $day at $finalHour"

        //use this key to sort arraylist
        sortKey = now.toMillis().toString()
        return root
    }



    private fun executeClass() {
        joinClass.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()){
                    classArrayList.clear()

                    for (classSnapShot in snapshot.children){
                        val subjectTitle = classSnapShot.getValue(CreateClassData::class.java)
                        classArrayList.add(subjectTitle!!)
                    }
                    val adapter = StudentClassAdapter(classArrayList)
                    classRecyclerView.adapter = adapter
                    progressDialogHide()
                    val swipeGestures = object : SwipeGestures(this@Class.requireContext()){

                        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                            val position : Int = viewHolder.adapterPosition
                            when(direction){
                                ItemTouchHelper.LEFT -> {
                                    MaterialAlertDialogBuilder(this@Class.requireContext())
                                        .setMessage("Are you certain that you want to delete this group?")
                                        .setPositiveButton("Okay"){_,_ ->
                                            val roomID = classArrayList[position].roomID.toString()
                                            //delete student class view
                                            val deleteClassSelf = FirebaseDatabase.getInstance().getReference("JoinClass").child(uid)
                                            deleteClassSelf.child(roomID).removeValue().addOnSuccessListener {
                                                adapter.deleteItem(position)
                                                classRecyclerView.adapter?.notifyItemRemoved(position)
                                                executeClass()
                                                //delete self in Accept
                                                val deleteSelf = FirebaseDatabase.getInstance().getReference("Public " +
                                                        "Class").child(roomID).child("Accept").child(uid)
                                                deleteSelf.removeValue()
                                                deleteTimeline()
                                                //delete member in this room
                                                val deleteMember = FirebaseDatabase.getInstance().getReference("Public Class").child(roomID).child("Members")
                                                deleteMember.child(uid).removeValue().addOnSuccessListener {

                                                    Toast.makeText(this@Class.requireContext(), "Selected class deleted successfully!", Toast.LENGTH_SHORT).show()
                                                }


                                            }
                                        }.setNegativeButton("Cancel"){_,_ ->
                                            executeClass()
                                        }.setOnCancelListener(){
                                            executeClass()
                                            executeGroup()
                                        }.show()

                                }
                            }



                        }
                    }
                    val touchHelper = ItemTouchHelper(swipeGestures)
                    touchHelper.attachToRecyclerView(classRecyclerView)
                    adapter.setOnItemClickListener(object : StudentClassAdapter.onItemClickListener{

                        override fun onItemClick(position: Int) {

                            val uid = FirebaseAuth.getInstance().currentUser?.uid.toString()
                            val intent = Intent(this@Class.requireContext(), StudentRoomActivity::class.java)
                            roomID = classArrayList[position].subjectCode.toString()
                            val getBoolean = FirebaseDatabase.getInstance().getReference("Public Class").child(roomID).child("Accept")
                            getBoolean.child(uid).get().addOnSuccessListener {
                                if (it.exists()){

                                    val isAccept = it.child("accept").value.toString()

                                    if (isAccept != "true"){
                                        toastError("Please wait until your teacher accepts your request.")
                                    }
                                    else{
                                        intent.putExtra("student_room_name", classArrayList[position].subjectTitle)
                                        intent.putExtra("student_roomID", classArrayList[position].roomID)
                                        intent.putExtra("student_uid", classArrayList[position].uid)
                                        intent.putExtra("student_roomCode", classArrayList[position].roomID)
                                        intent.putExtra("student_section", classArrayList[position].section)
                                        intent.putExtra("student_name", classArrayList[position].name)
                                        intent.putExtra("student_type", classArrayList[position].type)
                                        intent.putExtra("student_room_type", "Class")
                                        startActivity(intent)
                                    }
                                }else{
                                    toastError("This class was deleted by the owner.")
                                }
                            }.addOnFailureListener{
                                toastError("Please wait until your teacher accepts your request.")
                            }

                        }



                    })




                }else{
                    progressDialogHide()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                progressDialogHide()
            }

        })
    }

    private fun deleteTimeline() {
        val deleteStudentTimeLine = FirebaseDatabase.getInstance().getReference("Student TimeLine").child(uid)
        deleteStudentTimeLine.removeValue()
    }

    private fun executeGroup() {
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()){
                    groupArrayList.clear()

                    for (classSnapShot in snapshot.children){
                        snapshot.child(uid)
                        val subjectTitle = classSnapShot.getValue(GroupListData::class.java)
                        groupArrayList.add(subjectTitle!!)

                    }
                    val adapter = StudentGroupAdapter(groupArrayList)
                    groupRecyclerView.adapter = adapter

                    val count = adapter.itemCount
                    progressDialogHide()

                    val swipeGestures = object : SwipeGestures(this@Class.requireContext()){

                        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                            val position : Int = viewHolder.adapterPosition
                            when(direction){
                                ItemTouchHelper.LEFT -> {
                                    MaterialAlertDialogBuilder(this@Class.requireContext())
                                        .setMessage("Are you certain that you want to delete this group?")
                                        .setPositiveButton("Okay"){_,_ ->
                                            val roomID = groupArrayList[position].roomID.toString()
                                            val deleteClassSelf = FirebaseDatabase.getInstance().getReference("JoinGroup").child(uid)
                                            deleteClassSelf.child(roomID).removeValue().addOnSuccessListener {
                                                adapter.deleteItem(position)
                                                groupRecyclerView.adapter?.notifyItemRemoved(position)

                                                executeGroup()
                                                val deleteSelf = FirebaseDatabase.getInstance().getReference("Public " +
                                                        "Group").child(roomID).child("Accept").child(uid)
                                                deleteSelf.removeValue()
                                                deleteTimeline()
                                                //delete member in this room
                                                val deleteMember = FirebaseDatabase.getInstance().getReference("Public Group").child(roomID).child("Members")
                                                deleteMember.child(uid).removeValue().addOnSuccessListener{
                                                    Toast.makeText(this@Class.requireContext(), "Selected group deleted successfully!", Toast.LENGTH_SHORT).show()
                                                }

                                            }
                                        }.setNegativeButton("Cancel"){_,_ ->
                                            executeGroup()
                                        }.setOnCancelListener {
                                            executeGroup()
                                            executeClass()
                                        }.show()

                                }
                            }



                        }
                    }
                    val touchHelper = ItemTouchHelper(swipeGestures)
                    touchHelper.attachToRecyclerView(groupRecyclerView)
                    adapter.setOnItemClickListener(object : StudentGroupAdapter.onItemClickListener{

                        override fun onItemClick(position: Int) {

                            val uid = FirebaseAuth.getInstance().currentUser?.uid.toString()
                            val intent = Intent(this@Class.requireContext(), StudentRoomActivity::class.java)
                            roomID = groupArrayList[position].subjectCode.toString()
                            val getBoolean = FirebaseDatabase.getInstance().getReference("Public Group").child(roomID).child("Accept")
                            getBoolean.child(uid).get().addOnSuccessListener{
                                if (it.exists()) {
                                    val isAccept = it.child("accept").value.toString()

                                    if (isAccept != "true") {
                                        toastError("Please wait until your teacher accepts your request.")
                                    }else{
                                        roomID = groupArrayList[position].roomID.toString()
                                        intent.putExtra("student_room_name", groupArrayList[position].subjectTitle)
                                        intent.putExtra("student_roomID", groupArrayList[position].roomID)
                                        intent.putExtra("student_uid", groupArrayList[position].uid)
                                        intent.putExtra("student_roomCode", groupArrayList[position].roomID)
                                        intent.putExtra("student_section", groupArrayList[position].section)
                                        intent.putExtra("student_name", groupArrayList[position].name)
                                        intent.putExtra("student_type", groupArrayList[position].type)
                                        intent.putExtra("student_room_type", "Group")
                                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        startActivity(intent)
                                    }
                                }else{
                                    toastError("This group was deleted by the owner.")
                                }
                            }.addOnFailureListener{
                                toastError("Please wait until your teacher accepts your request.")
                            }


                        }



                    })




                }else{
                    progressDialogHide()
                }
            }


            override fun onCancelled(error: DatabaseError) {
                progressDialogHide()
            }

        })
    }

    private  fun toastError(message: String){
        MaterialAlertDialogBuilder(this@Class.requireContext())
            .setTitle("Note")
            .setMessage(message)
            .setPositiveButton("OKAY"){_,_ ->

            }
            .show()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.grpBtn.setOnClickListener{
            hideClass()
            binding.classBtn.setBackgroundResource(R.color.transparent_color)
            binding.grpBtn.setBackgroundResource(R.drawable.bottom_rec)
        }
        binding.classBtn.setOnClickListener{
            hideGroup()
            binding.grpBtn.setBackgroundResource(R.color.transparent_color)
            binding.classBtn.setBackgroundResource(R.drawable.bottom_rec)
        }
        join_class.visibility = View.GONE
        join_room.visibility = View.GONE
        join_class_text.visibility = View.GONE
        join_room_text.visibility = View.GONE

        var isAllFabVisible = false

        join_btn.shrink()

        join_btn.setOnClickListener{
            if (!isAllFabVisible){
                student_class_wall.setBackgroundResource(R.color.transparent_color)
                join_class.show()
                join_room.show()
                join_class_text.visibility = View.VISIBLE
                join_room_text.visibility = View.VISIBLE

                join_btn.extend()

                isAllFabVisible = true
            }else{
                student_class_wall.setBackgroundResource(R.color.zero)
                join_class.hide()
                join_room.hide()
                join_class_text.visibility = View.GONE
                join_room_text.visibility = View.GONE

                join_btn.shrink()

                isAllFabVisible = false
            }
        }

        join_class.setOnClickListener{

            val inflater = layoutInflater
            val dialogLayout = inflater.inflate(R.layout.alert_dialog_join_class, null)
            val subjectCode = dialogLayout.findViewById<EditText>(R.id.join_subject_code)
            val auth = FirebaseAuth.getInstance()
            val uid = auth.currentUser?.uid.toString()

            val databaseGroup = FirebaseDatabase.getInstance().getReference("Public Class")


            MaterialAlertDialogBuilder(requireActivity())
                .setMessage("Join Class")
                .setView(dialogLayout)
                .setPositiveButton("JOIN"){_, _ ->
                    when {
                        TextUtils.isEmpty(subjectCode.text.toString().trim {it <= ' '}) -> {
                            toastError("Don't leave it blank!")

                        }else -> {
                        databaseGroup.child(subjectCode.text.toString()).get().addOnSuccessListener { it ->
                            if (it.exists()) {
                                val letter = it.child("letter").value.toString()
                                val name = it.child("name").value.toString()
                                val roomID = it.child("roomID").value.toString()
                                val section = it.child("section").value.toString()
                                val pass = it.child("subjectCode").value.toString()
                                val subjectTitle = it.child("subjectTitle").value.toString()
                                val type = it.child("type").value.toString()
                                val joinGroupUser = FirebaseDatabase.getInstance().getReference("JoinClass")

                                val getTeacherUID = FirebaseDatabase.getInstance().getReference("Public Class")
                                    .child(roomID)
                                getTeacherUID.get().addOnSuccessListener {
                                    val teacherUID = (when {
                                        it.exists() -> {
                                            it.child("uid").value.toString()
                                        }
                                        else -> {
                                            null
                                        }
                                    }).toString()

                                    val joinGroup = GroupListData(
                                        name,
                                        type,
                                        section,
                                        roomID,
                                        teacherUID,
                                        subjectTitle,
                                        pass,
                                        letter
                                    )
                                    joinGroupUser.child(uid).child(roomID).setValue(joinGroup)
                                        .addOnCompleteListener {
                                            executeClass()
                                            student_class_wall.setBackgroundResource(R.color.zero)
                                            join_class.hide()
                                            join_room.hide()
                                            join_class_text.visibility = View.GONE
                                            join_room_text.visibility = View.GONE
                                            join_btn.shrink()
                                        }
                                }


                                val userID = auth.currentUser?.uid.toString()
                                val currentUser = FirebaseDatabase.getInstance().getReference("Students")
                                currentUser.child(userID).get().addOnSuccessListener {
                                    if (it.exists()){
                                        val currentName = it.child("name").value.toString()
                                        val currentType = it.child("type").value.toString()
                                        val accept = FirebaseDatabase.getInstance().getReference("Public Class")
                                        accept.child(roomID).child("Accept").child(userID).get()
                                            .addOnSuccessListener {
                                                if (it.exists()){
                                                    toastError("This room is already added.")
                                                }else{
                                                    val data = RoomMembersData(currentName, currentType,
                                                        userID, "false")

                                                    accept.child(roomID).child("Accept").child(uid).setValue(data).addOnSuccessListener {
                                                        val request = FirebaseDatabase.getInstance().getReference("Public Class")
                                                        request.child(roomID).child("Request").child(uid).setValue(data)


                                                        val newNotification = Notification()
                                                        val me = ""
                                                        val description = "$currentName wants to join the class named $subjectTitle."
                                                        newNotification.notification(uid, me, description, randomCode(), date, sortKey)
                                                        val getTeacherUID = FirebaseDatabase.getInstance().getReference("Public Class").child(roomID)
                                                        getTeacherUID.get().addOnSuccessListener {
                                                            if (it.exists()) {
                                                                val newUID = it.child("uid").value.toString()
                                                                newNotification.notification(
                                                                    newUID,
                                                                    me,
                                                                    description,
                                                                    randomCode(),
                                                                    date,
                                                                    sortKey
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }




                                    }
                                }
                            } else {
                                showPopUp("Class not found.")
                            }
                        }
                    }

                    }
                }.setNegativeButton("Cancel"){_, _ ->

                }
                .show()
        }
        join_room.setOnClickListener{

            val inflater = layoutInflater
            val dialogLayout = inflater.inflate(R.layout.alert_dialog_join_room, null)
            val groupCode = dialogLayout.findViewById<EditText>(R.id.join_room_code)
            val auth = FirebaseAuth.getInstance()
            val uid = auth.currentUser?.uid.toString()

            val databaseGroup = FirebaseDatabase.getInstance().getReference("Public Group")


            MaterialAlertDialogBuilder(requireActivity())
                .setMessage("Join Group")
                .setView(dialogLayout)
                .setPositiveButton("JOIN"){_, _ ->
                    when{
                        TextUtils.isEmpty(groupCode.text.toString().trim {it <= ' '}) -> {
                            toastError("Do not leave it blank!")
                        }else -> {

                        databaseGroup.child(groupCode.text.toString()).get().addOnSuccessListener { it ->
                            if (it.exists()){
                                val letter = it.child("letter").value.toString()
                                val name = it.child("name").value.toString()
                                val roomID = it.child("roomID").value.toString()
                                val section = it.child("section").value.toString()
                                val subjectCode = it.child("subjectCode").value.toString()
                                val subjectTitle = it.child("subjectTitle").value.toString()
                                val type = it.child("type").value.toString()
                                val joinGroupUser = FirebaseDatabase.getInstance().getReference("JoinGroup")

                                val getTeacherUID = FirebaseDatabase.getInstance().getReference("Public Group")
                                    .child(roomID)
                                getTeacherUID.get().addOnSuccessListener {
                                    val teacherUID = (when {
                                        it.exists() -> {
                                            it.child("uid").value.toString()
                                        }
                                        else -> {
                                            null
                                        }
                                    }).toString()

                                    val joinGroup = GroupListData(name, type, section, roomID, teacherUID, subjectTitle, subjectCode, letter)
                                    joinGroupUser.child(uid).child(roomID).setValue(joinGroup).addOnCompleteListener{
                                        executeGroup()
                                        student_class_wall.setBackgroundResource(R.color.zero)
                                        join_class.hide()
                                        join_room.hide()
                                        join_class_text.visibility = View.GONE
                                        join_room_text.visibility = View.GONE

                                        join_btn.shrink()
                                    }
                                }


                                //group
                                val userID = auth.currentUser?.uid.toString()
                                val currentUser = FirebaseDatabase.getInstance().getReference("Students")
                                currentUser.child(userID).get().addOnSuccessListener {
                                    if (it.exists()){
                                        val currentName = it.child("name").value.toString()
                                        val currentType = it.child("type").value.toString()
                                        val accept = FirebaseDatabase.getInstance().getReference("Public Group")
                                        accept.child(roomID).child("Accept").child(userID).get()
                                            .addOnSuccessListener {
                                                if (it.exists()){
                                                    toastError("This room is already added.")
                                                }else{
                                                    val data = RoomMembersData(currentName, currentType,
                                                        userID, "false")

                                                    accept.child(roomID).child("Accept").child(uid).setValue(data).addOnSuccessListener {
                                                        val request = FirebaseDatabase
                                                            .getInstance().getReference("Public " +
                                                                    "Group")
                                                        request.child(roomID).child("Request").child(uid).setValue(data)


                                                        val newNotification = Notification()
                                                        val me = ""
                                                        val description =
                                                            "$currentName wants to join the group named $subjectTitle."
                                                        newNotification.notification(uid, me, description, randomCode(), date, sortKey)
                                                        val getTeacherUID = FirebaseDatabase
                                                            .getInstance().getReference("Public " +
                                                                    "Group").child(roomID)
                                                        getTeacherUID.get().addOnSuccessListener {
                                                            if (it.exists()) {
                                                                val newUID = it.child("uid").value.toString()
                                                                newNotification.notification(
                                                                    newUID,
                                                                    me,
                                                                    description,
                                                                    randomCode(),
                                                                    date,
                                                                    sortKey
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }




                                    }
                                }
                            }else{
                                showPopUp("Group not found.")
                            }
                        }
                    }
                    }
                }.setNegativeButton("Cancel"){_, _ ->

                }
                .show()
        }



    }

    private fun hideClass() {
        classRecyclerView.visibility = View.GONE
        groupRecyclerView.visibility = View.VISIBLE

        binding.groupThats.visibility = View.VISIBLE

        binding.classThats.visibility = View.GONE
    }

    private fun hideGroup() {
        groupRecyclerView.visibility = View.GONE
        classRecyclerView.visibility = View.VISIBLE

        binding.groupThats.visibility = View.GONE

        binding.classThats.visibility = View.VISIBLE
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
    private fun showPopUp(message : String){
        MaterialAlertDialogBuilder(requireActivity())
            .setMessage(message)
            .setPositiveButton("Okay"){_, _ ->

            }
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun LocalDateTime.toMillis(zone: ZoneId = ZoneId.systemDefault()) = atZone(zone)?.toInstant()?.toEpochMilli()
    private fun randomCode(): String = List(6) {
        (('a'..'z') + ('A'..'Z') + ('0'..'9')).random()
    }.joinToString("")
}
