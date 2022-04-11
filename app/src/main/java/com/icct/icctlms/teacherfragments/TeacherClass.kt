package com.icct.icctlms.teacherfragments

import android.app.Dialog
import android.app.ProgressDialog
import android.content.*
import android.os.Bundle
import android.os.Handler
import android.os.Message
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.icct.icctlms.Authentication.Teacher.TeacherMainActivity
import com.icct.icctlms.R
import com.icct.icctlms.RoomActivity
import com.icct.icctlms.adapter.CreateClassAdapter
import com.icct.icctlms.adapter.GroupListAdapter
import com.icct.icctlms.data.CreateClassData
import com.icct.icctlms.data.GroupListData
import com.icct.icctlms.data.RoomMembersData
import com.icct.icctlms.gestures.SwipeGestures
import kotlinx.android.synthetic.main.fragment_teacher_class.*
import kotlinx.android.synthetic.main.fragment_teacher_home.*


class TeacherClass : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var classArrayList: ArrayList<CreateClassData>
    private lateinit var recyclerView: RecyclerView
    private lateinit var roomID : String
    private lateinit var progressDialog : ProgressDialog
    private lateinit var groupArrayList : ArrayList<GroupListData>
    private lateinit var groupRecyclerView: RecyclerView
    private lateinit var subjectTitle : GroupListData
    private lateinit var classData : CreateClassData
    private lateinit var databaseGroup : DatabaseReference
    private lateinit var databaseClass : DatabaseReference
    private lateinit var uid : String
    private lateinit var dialog : Dialog
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?




    ): View? {

        val rootView = inflater.inflate(R.layout.fragment_teacher_class, container, false)
        recyclerView = rootView.findViewById(R.id.class_list)!!
        groupRecyclerView = rootView.findViewById(R.id.group_list)
        val auth = FirebaseAuth.getInstance()
        groupRecyclerView.setHasFixedSize(true)
        groupRecyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)
        classArrayList = arrayListOf<CreateClassData>()
        groupArrayList = arrayListOf<GroupListData>()
        uid = FirebaseAuth.getInstance().currentUser?.uid.toString()


        databaseGroup = FirebaseDatabase.getInstance().getReference("Group").child(uid)
        executeGroup()


        //retrieve class data snapshot

        databaseClass = FirebaseDatabase.getInstance().getReference("Class").child(uid)
        executeClass()


        return rootView


    }

    private fun executeClass() {
        databaseClass.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()){
                    classArrayList.clear()

                    for (classSnapShot in snapshot.children){
                        classData = classSnapShot.getValue(CreateClassData::class.java)!!
                        classArrayList.add(classData!!)
                    }
                    val adapter = CreateClassAdapter(classArrayList)
                    recyclerView.adapter = adapter

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
                                        }.show()

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




                }
            }


            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun executeGroup() {
        databaseGroup.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()){
                    groupArrayList.clear()

                    for (classSnapShot in snapshot.children){
                        subjectTitle = classSnapShot.getValue(GroupListData::class.java)!!
                        groupArrayList.add(subjectTitle!!)
                    }
                    val adapter = GroupListAdapter(groupArrayList)
                    groupRecyclerView.adapter = adapter
                    val swipeGestures = object : SwipeGestures(this@TeacherClass.requireContext()){

                        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                            val position : Int = viewHolder.adapterPosition
                            when(direction){
                                ItemTouchHelper.LEFT -> {
                                    MaterialAlertDialogBuilder(this@TeacherClass.requireContext())
                                        .setMessage("Are you certain that you want to delete this group?")
                                        .setPositiveButton("Okay"){_,_ ->
                                            progressDialogShow()
                                            val roomCode = groupArrayList[position].subjectCode.toString()
                                            val deleteGroupSelf = FirebaseDatabase.getInstance().getReference("Group").child(uid)
                                            deleteGroupSelf.child(roomCode).removeValue().addOnSuccessListener {
                                                val deleteGroupGlobal = FirebaseDatabase.getInstance().getReference("Public Group")
                                                deleteGroupGlobal.child(roomCode).removeValue().addOnSuccessListener {
                                                    val deleteGroupPost = FirebaseDatabase.getInstance().getReference("Group Post")
                                                    deleteGroupPost.child("Room ID: $roomCode").removeValue().addOnSuccessListener {

                                                        val databaseClass = FirebaseDatabase.getInstance().getReference("Public Group").child(roomCode)
                                                            .child("Members")
                                                        databaseClass.child(uid).removeValue()

                                                        adapter.deleteItem(position)
                                                        groupRecyclerView.adapter?.notifyItemRemoved(position)
                                                        Toast.makeText(this@TeacherClass.requireContext(), "Group deleted successfully!", Toast.LENGTH_SHORT).show()
                                                        progressDialogHide()
                                                    }
                                                }
                                            }


                                        }.setNegativeButton("Cancel"){_,_ ->
                                            executeGroup()
                                            executeClass()
                                        }.show()

                                }
                            }



                        }
                    }
                    val touchHelper = ItemTouchHelper(swipeGestures)
                    touchHelper.attachToRecyclerView(groupRecyclerView)

                    adapter.setOnItemClickListener(object : GroupListAdapter.onItemClickListener{

                        override fun onItemClick(position: Int) {
                            val intent = Intent(this@TeacherClass.requireContext(), RoomActivity::class.java)

                            roomID = groupArrayList[position].roomID.toString()
                            intent.putExtra("roomID", groupArrayList[position].roomID)
                            intent.putExtra("roomType", "Group")
                            intent.putExtra("roomName", groupArrayList[position].subjectTitle)
                            startActivity(intent)

                        }



                    })




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


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


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

        var isAllFabVisible: Boolean = false

        add_btn.setOnClickListener{
            wall.setBackgroundResource(R.color.transparent_color)
            wall.invalidate()
            val fadeIn = AnimationUtils.loadAnimation(context, R.anim.fade_in)
            wall.startAnimation(fadeIn)
            add_btn.startAnimation(AnimationUtils.loadAnimation(context, R.anim.rotate))
            if (!isAllFabVisible){
                Handler().postDelayed({
                    add_room_text.visibility = View.VISIBLE
                    add_room.show()
                },140)
                Handler().postDelayed({
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
            val autoCode = roomId
            subjectCode.setText(autoCode)


                MaterialAlertDialogBuilder(requireActivity())
                    .setView(dialogLayout)
                    .setPositiveButton("Okay"){_, _ ->

                        val title = subjectTitle.text.toString()
                        val code = subjectCode.text.toString()
                        copy(subjectCode)
                        val uid = auth.currentUser?.uid.toString()
                        val database = FirebaseDatabase.getInstance().getReference("Teachers")
                        database.child(uid).get().addOnSuccessListener {
                            if (it.exists()){
                                progressDialogShow()
                                name = it.child("name").value.toString()
                                type = it.child("type").value.toString()

                                val data = RoomMembersData(name, type)
                                val randomID = randomSecurityCode()
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
            val autoCode = roomId
            roomCode.setText(autoCode)

            MaterialAlertDialogBuilder(this.requireContext())
                .setView(dialogLayout)
                .setPositiveButton("Confirm"){_,_ ->
                    copy(roomCode)
                    val database = FirebaseDatabase.getInstance().getReference("Teachers")
                    database.child(uid).get().addOnSuccessListener {
                        if (it.exists()){
                            progressDialogShow()
                            name = it.child("name").value.toString()
                            type = it.child("type").value.toString()
                            val data = RoomMembersData(name, type)
                            val randomID = randomSecurityCode()
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

    private fun  showDialog(title: String, message : String){
        MaterialAlertDialogBuilder(this.requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK"){_,_ ->

            }.show()
    }

    private fun randomID(): String = List(16) {
        (('a'..'z') + ('A'..'Z') + ('0'..'9')).random()
    }.joinToString("")

    private fun randomSecurityCode(): String = List(6) {
        (('a'..'z') + ('A'..'Z') + ('0'..'9')).random()
    }.joinToString("")


}