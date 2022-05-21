package com.icct.icctlms.teacherfragments

import android.app.Dialog
import android.content.*
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.view.isEmpty
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.icct.icctlms.R
import com.icct.icctlms.RoomActivity
import com.icct.icctlms.adapter.CreateClassAdapter
import com.icct.icctlms.adapter.GroupListAdapter
import com.icct.icctlms.data.CreateClassData
import com.icct.icctlms.data.GroupListData
import com.icct.icctlms.data.RoomMembersData
import com.icct.icctlms.database.Notification
import com.icct.icctlms.databinding.FragmentTeacherClassBinding
import com.icct.icctlms.gestures.SwipeGestures
import kotlinx.android.synthetic.main.activity_teacher_main.*
import kotlinx.android.synthetic.main.fragment_class.*
import kotlinx.android.synthetic.main.fragment_teacher_class.*
import kotlinx.android.synthetic.main.fragment_teacher_home.*
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList


class TeacherClass : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var classArrayList: ArrayList<CreateClassData>
    private lateinit var recyclerView: RecyclerView
    private lateinit var roomID : String
    private lateinit var groupArrayList : ArrayList<GroupListData>
    private lateinit var groupRecyclerView: RecyclerView
    private lateinit var subjectTitle : GroupListData
    private lateinit var classData : CreateClassData
    private lateinit var databaseGroup : DatabaseReference
    private lateinit var databaseClass : DatabaseReference
    private lateinit var uid : String
    private lateinit var dialog : Dialog
    private lateinit var teacherNav : BottomNavigationView
    private var _binding: FragmentTeacherClassBinding? = null
    private val binding get() = _binding!!
    private lateinit var sortKey : String
    private lateinit var today : Calendar
    private lateinit var date : String
    private lateinit var hour : String
    private lateinit var finalHour : String
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?




    ): View {

        _binding = FragmentTeacherClassBinding.inflate(inflater, container, false)
        recyclerView = binding.classList
        groupRecyclerView = binding.groupList
        groupRecyclerView.visibility = View.GONE
        binding.groupThats.visibility = View.GONE
        groupRecyclerView.setHasFixedSize(true)
        groupRecyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)
        classArrayList = arrayListOf()
        groupArrayList = arrayListOf()
        uid = FirebaseAuth.getInstance().currentUser?.uid.toString()


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


        progressDialogShow()
        databaseGroup = FirebaseDatabase.getInstance().getReference("Group").child(uid)
        executeGroup()

        sortKey = now.toMillis().toString()
        //retrieve class data snapshot

        databaseClass = FirebaseDatabase.getInstance().getReference("Class").child(uid)
        executeClass()


        binding.classBtn.setBackgroundResource(R.drawable.bottom_rec)
        return binding.root


    }


    private fun hideClass() {
        recyclerView.visibility = View.GONE
        groupRecyclerView.visibility = View.VISIBLE
        binding.groupThats.visibility = View.VISIBLE

        binding.classThats.visibility = View.GONE

    }

    private fun hideGroup() {
        groupRecyclerView.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
        binding.classThats.visibility = View.VISIBLE

        binding.groupThats.visibility = View.GONE

    }

    private fun executeClass() {
        databaseClass.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()){
                    classArrayList.clear()

                    for (classSnapShot in snapshot.children){
                        classData = classSnapShot.getValue(CreateClassData::class.java)!!
                        classArrayList.add(classData)
                    }
                    val adapter = CreateClassAdapter(classArrayList)
                    recyclerView.adapter = adapter
                    progressDialogHide()

                    val swipeGestures = object : SwipeGestures(this@TeacherClass.requireContext()){

                        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                            val position : Int = viewHolder.adapterPosition
                            when(direction){
                                ItemTouchHelper.LEFT -> {
                                    MaterialAlertDialogBuilder(this@TeacherClass.requireContext())
                                        .setMessage("Are you certain that you want to drop your class??")
                                        .setPositiveButton("Okay"){_,_ ->
                                            progressDialogShow()

                                            val roomCode = classArrayList[position].subjectCode.toString()
                                            val deleteClassSelf = FirebaseDatabase.getInstance().getReference("Class").child(uid)
                                            deleteClassSelf.child(roomCode).removeValue().addOnSuccessListener {
                                                val deleteClassGlobal = FirebaseDatabase.getInstance().getReference("Public Class")
                                                deleteClassGlobal.child(roomCode).removeValue().addOnSuccessListener {
                                                    val deleteClassPost = FirebaseDatabase.getInstance().getReference("Class Post")
                                                    deleteClassPost.child("Room ID: $roomCode").removeValue().addOnSuccessListener {


                                                        val databaseClass = FirebaseDatabase.getInstance().getReference("Public Class").child(roomCode)
                                                            .child("Members")
                                                        databaseClass.child(uid).removeValue()
                                                        deleteTimeline()

                                                        adapter.deleteItem(position)
                                                        recyclerView.adapter?.notifyItemRemoved(position)
                                                        Toast.makeText(this@TeacherClass.requireContext(), "Class deleted successfully!", Toast.LENGTH_SHORT).show()
                                                        progressDialogHide()
                                                    }
                                                }
                                            }



                                        }.setNegativeButton("Cancel"){_,_ ->
                                            executeGroup()
                                            executeClass()
                                        }.setOnCancelListener(){
                                            executeClass()
                                            executeGroup()
                                        }
                                        .show()

                                }
                            }



                        }
                    }
                    val touchHelper = ItemTouchHelper(swipeGestures)
                    touchHelper.attachToRecyclerView(recyclerView)
                    adapter.setOnItemClickListener(object : CreateClassAdapter.onItemClickListener{

                        override fun onItemClick(position: Int) {
                            val intent = Intent(this@TeacherClass.requireContext(), RoomActivity::class.java)
                            roomID = classArrayList[position].roomID.toString()
                            intent.putExtra("roomID", classArrayList[position].roomID)
                            intent.putExtra("roomType", "Class")
                            intent.putExtra("roomName", classArrayList[position].subjectTitle)
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

    private fun deleteTimeline() {
        val deleteTime = FirebaseDatabase.getInstance().getReference("Teacher TimeLine").child(uid)
        deleteTime.removeValue()
    }

    private fun executeGroup() {
        databaseGroup.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()) {
                    groupArrayList.clear()

                    for (classSnapShot in snapshot.children) {
                        subjectTitle = classSnapShot.getValue(GroupListData::class.java)!!
                        groupArrayList.add(subjectTitle)
                    }
                    val adapter = GroupListAdapter(groupArrayList)
                    groupRecyclerView.adapter = adapter
                    progressDialogHide()
                    val swipeGestures = object : SwipeGestures(this@TeacherClass.requireContext()) {

                        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                            val position: Int = viewHolder.adapterPosition
                            when (direction) {
                                ItemTouchHelper.LEFT -> {
                                    MaterialAlertDialogBuilder(this@TeacherClass.requireContext())
                                        .setMessage("Are you certain that you want to delete this group?")
                                        .setPositiveButton("Okay") { _, _ ->
                                            progressDialogShow()
                                            val roomCode =
                                                groupArrayList[position].subjectCode.toString()
                                            val deleteGroupSelf =
                                                FirebaseDatabase.getInstance().getReference("Group")
                                                    .child(uid)
                                            deleteGroupSelf.child(roomCode).removeValue()
                                                .addOnSuccessListener {
                                                    val deleteGroupGlobal =
                                                        FirebaseDatabase.getInstance()
                                                            .getReference("Public Group")
                                                    deleteGroupGlobal.child(roomCode).removeValue()
                                                        .addOnSuccessListener {
                                                            val deleteGroupPost =
                                                                FirebaseDatabase.getInstance()
                                                                    .getReference("Group Post")
                                                            deleteGroupPost.child("Room ID: $roomCode")
                                                                .removeValue()
                                                                .addOnSuccessListener {

                                                                    val databaseClass =
                                                                        FirebaseDatabase.getInstance()
                                                                            .getReference("Public Group")
                                                                            .child(roomCode)
                                                                            .child("Members")
                                                                    databaseClass.child(uid)
                                                                        .removeValue()
                                                                    deleteTimeline()

                                                                    adapter.deleteItem(position)
                                                                    groupRecyclerView.adapter?.notifyItemRemoved(
                                                                        position
                                                                    )
                                                                    Toast.makeText(
                                                                        this@TeacherClass.requireContext(),
                                                                        "Group deleted successfully!",
                                                                        Toast.LENGTH_SHORT
                                                                    ).show()
                                                                    progressDialogHide()
                                                                }
                                                        }
                                                }


                                        }.setNegativeButton("Cancel") { _, _ ->
                                            executeGroup()
                                            executeClass()
                                        }.setOnCancelListener(){
                                            executeGroup()
                                            executeClass()
                                        }
                                        .show()

                                }
                            }


                        }
                    }
                    val touchHelper = ItemTouchHelper(swipeGestures)
                    touchHelper.attachToRecyclerView(groupRecyclerView)

                    adapter.setOnItemClickListener(object : GroupListAdapter.onItemClickListener {

                        override fun onItemClick(position: Int) {
                            val intent =
                                Intent(this@TeacherClass.requireContext(), RoomActivity::class.java)

                            roomID = groupArrayList[position].roomID.toString()
                            intent.putExtra("roomID", groupArrayList[position].roomID)
                            intent.putExtra("roomType", "Group")
                            intent.putExtra("roomName", groupArrayList[position].subjectTitle)
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

        auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid.toString()
        val databaseClass = FirebaseDatabase.getInstance().getReference("Class")
        val databaseGroup = FirebaseDatabase.getInstance().getReference("Group")
        var name = ""
        var type = ""

        add_class.visibility = View.GONE
        add_room.visibility = View.GONE
        add_class_text.visibility = View.GONE
        add_room_text.visibility = View.GONE

        var isAllFabVisible = false

        add_btn.setOnClickListener{
            wall.setBackgroundResource(R.color.transparent_color)
            wall.invalidate()
            val fadeIn = AnimationUtils.loadAnimation(context, R.anim.fade_in)
            wall.startAnimation(fadeIn)
            add_btn.startAnimation(AnimationUtils.loadAnimation(context, R.anim.rotate))
            if (!isAllFabVisible){
                Handler(Looper.myLooper()!!).postDelayed({
                    add_room_text.visibility = View.VISIBLE
                    add_room.show()
                },140)
                Handler(Looper.myLooper()!!).postDelayed({
                    add_class.show()
                    add_class_text.visibility = View.VISIBLE
                },130)


                isAllFabVisible = true
            }else{
                wall.setBackgroundResource(R.color.zero)
                wall.invalidate()
                val fadeOut = AnimationUtils.loadAnimation(context, R.anim.fade_out)
                wall.startAnimation(fadeOut)
                add_class.hide()
                add_room.hide()
                add_class_text.visibility = View.GONE
                add_room_text.visibility = View.GONE


                isAllFabVisible = false
            }
        }


        add_class.setOnClickListener{

            val inflater = layoutInflater
            val dialogLayout = inflater.inflate(R.layout.alert_dialog_create_class, null)
            val subjectTitle = dialogLayout.findViewById<EditText>(R.id.create_subject_title)
            val subjectCode = dialogLayout.findViewById<EditText>(R.id.create_subject_code)
            val copyBtn = dialogLayout.findViewById<TextView>(R.id.copy_btn)
            val roomId = randomID()
            subjectCode.setText(roomId)


                MaterialAlertDialogBuilder(requireActivity())
                    .setView(dialogLayout)
                    .setPositiveButton("CREATE"){_, _ ->

                        val title = subjectTitle.text.toString()
                        val code = subjectCode.text.toString()
                        copy(subjectCode)
                        val database = FirebaseDatabase.getInstance().getReference("Teachers")
                        when{
                            TextUtils.isEmpty(title.trim {it <= ' '}) -> {
                                toastError("Do not leave it blank.")
                            }else ->{
                            database.child(uid).get().addOnSuccessListener {
                                if (it.exists()){
                                    progressDialogShow()
                                    name = it.child("name").value.toString()
                                    type = it.child("type").value.toString()

                                    val data = RoomMembersData(name, type)
                                    val letter = title[0].toString().uppercase() + title[1].toString().uppercase()
                                    val createClass = CreateClassData(name, type, name, roomId, uid, title, code, letter)
                                    //database create class query firebase
                                    val databasePublic = FirebaseDatabase.getInstance().getReference("Public Class")
                                    databasePublic.child(roomId).setValue(createClass)
                                    databaseClass.child(uid).child(roomId).setValue(createClass).addOnCompleteListener{
                                        executeClass()
                                        databasePublic.child(roomId).child("Members").child(uid).setValue(data)
                                        wall.setBackgroundResource(R.color.zero)
                                        wall.invalidate()
                                        val fadeOut = AnimationUtils.loadAnimation(context, R.anim.fade_out)
                                        wall.startAnimation(fadeOut)
                                        add_class.hide()
                                        add_room.hide()
                                        add_class_text.visibility = View.GONE
                                        add_room_text.visibility = View.GONE
                                        progressDialogHide()

                                        val newNotification = Notification()
                                        val me = ""
                                        val description = "You successfully created the class named $title."
                                        newNotification.notification(uid, me, description, randomID(), date, sortKey)

                                    }
                                }

                            }
                            }
                        }



                    }.setNegativeButton("Cancel"){_, _ ->

                    }
                    .show()


            copyBtn.setOnClickListener{
                copy(subjectCode)
            }
        }
        add_room.setOnClickListener{
            val inflater = layoutInflater
            val dialogLayout = inflater.inflate(R.layout.alert_dialog_create_group, null)
            val groupName = dialogLayout.findViewById<EditText>(R.id.group_subject_title)
            val section = dialogLayout.findViewById<EditText>(R.id.group_section)
            val roomCode = dialogLayout.findViewById<EditText>(R.id.group_subject_code)
            val copyBtn = dialogLayout.findViewById<TextView>(R.id.copy_btn)
            val roomId = randomID()
            roomCode.setText(roomId)

            MaterialAlertDialogBuilder(this.requireContext())
                .setView(dialogLayout)
                .setPositiveButton("Create"){_,_ ->
                    copy(roomCode)
                    val database = FirebaseDatabase.getInstance().getReference("Teachers")
                    when{
                        TextUtils.isEmpty(groupName.text.toString().trim{it <= ' '}) ->{
                            toastError("Please input group name.")
                        }TextUtils.isEmpty(section.text.toString().trim {it <= ' '}) -> {
                            toastError("Please input section name.")
                        }
                        else -> {
                        database.child(uid).get().addOnSuccessListener {
                            if (it.exists()){
                                progressDialogShow()
                                name = it.child("name").value.toString()
                                type = it.child("type").value.toString()
                                val data = RoomMembersData(name, type)
                                val finalSection = section.text.toString()
                                val finalGroupName = groupName.text.toString()
                                val securityCode = roomCode.text.toString()
                                val letter = finalGroupName[0].toString().uppercase() + finalGroupName[1].toString().uppercase()
                                val createGroup = GroupListData(name, type, finalSection, roomId, uid, finalGroupName, securityCode, letter)
                                //database create class query firebase
                                val databasePublic = FirebaseDatabase.getInstance().getReference("Public Group")
                                databasePublic.child(roomId).setValue(createGroup)
                                databaseGroup.child(uid).child(roomId).setValue(createGroup).addOnCompleteListener{
                                    executeGroup()
                                    databasePublic.child(roomId).child("Members").child(uid).setValue(data)
                                    wall.setBackgroundResource(R.color.zero)
                                    wall.invalidate()
                                    val fadeOut = AnimationUtils.loadAnimation(context, R.anim.fade_out)
                                    wall.startAnimation(fadeOut)
                                    add_class.hide()
                                    add_room.hide()
                                    add_class_text.visibility = View.GONE
                                    add_room_text.visibility = View.GONE
                                    progressDialogHide()

                                    val newNotification = Notification()
                                    val me = ""
                                    val description = "You successfully created the group named $finalGroupName, section $finalSection."
                                    newNotification.notification(uid, me, description, randomID(), date, sortKey)

                                }
                            }

                        }
                        }
                    }
                }.setNegativeButton("Cancel"){_,_ ->

                }.show()


            copyBtn.setOnClickListener{
                copy(roomCode)
            }
        }


    }

    private fun copy(code : EditText){
        val clipboardManager = activity?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("text", code.text.toString())
        clipboardManager.setPrimaryClip(clipData)
        Toast.makeText(context, "Security code copied successfully.", Toast.LENGTH_SHORT).show()

    }
    private  fun toastError(message: String){
        MaterialAlertDialogBuilder(this@TeacherClass.requireContext())
            .setTitle("Note")
            .setMessage(message)
            .setPositiveButton("OKAY"){_,_ ->

            }
            .show()
    }

    private fun randomID(): String = List(16) {
        (('a'..'z') + ('A'..'Z') + ('0'..'9')).random()
    }.joinToString("")

    @RequiresApi(Build.VERSION_CODES.O)
    fun LocalDateTime.toMillis(zone: ZoneId = ZoneId.systemDefault()) = atZone(zone)?.toInstant()?.toEpochMilli()
}