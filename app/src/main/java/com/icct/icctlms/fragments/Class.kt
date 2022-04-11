package com.icct.icctlms.fragments

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.icct.icctlms.Authentication.Teacher.TeacherMainActivity
import com.icct.icctlms.R
import com.icct.icctlms.StudentRoomActivity
import com.icct.icctlms.adapter.StudentClassAdapter
import com.icct.icctlms.adapter.StudentGroupAdapter
import com.icct.icctlms.data.CreateClassData
import com.icct.icctlms.data.GroupListData
import com.icct.icctlms.data.RoomMembersData
import com.icct.icctlms.gestures.SwipeGestures
import kotlinx.android.synthetic.main.fragment_class.*
import kotlinx.android.synthetic.main.fragment_teacher_class.*

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
        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            // Inflate the layout for this fragment
            val rootView = inflater.inflate(R.layout.fragment_class, container, false)
            groupRecyclerView = rootView.findViewById(R.id.student_group_list)
            groupRecyclerView.setHasFixedSize(true)
            groupRecyclerView.layoutManager = LinearLayoutManager(context)
            groupArrayList = ArrayList()

            classRecyclerView = rootView.findViewById(R.id.student_class_list)
            classRecyclerView.setHasFixedSize(true)
            classRecyclerView.layoutManager = LinearLayoutManager(context)
            classArrayList = ArrayList()
            val auth = FirebaseAuth.getInstance()
            uid = auth.currentUser?.uid.toString()

            databaseReference = FirebaseDatabase.getInstance().getReference("JoinGroup").child(uid)
            executeGroup()


            //join class
            joinClass = FirebaseDatabase.getInstance().getReference("JoinClass").child(uid)
            executeClass()


            return rootView
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

                    val swipeGestures = object : SwipeGestures(this@Class.requireContext()){

                        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                            val position : Int = viewHolder.adapterPosition
                            when(direction){
                                ItemTouchHelper.LEFT -> {
                                    MaterialAlertDialogBuilder(this@Class.requireContext())
                                        .setMessage("Are you certain that you want to delete this group?")
                                        .setPositiveButton("Okay"){_,_ ->
                                            val roomID = classArrayList[position].roomID.toString()
                                            val deleteClassSelf = FirebaseDatabase.getInstance().getReference("JoinClass").child(uid)
                                            deleteClassSelf.child(roomID).removeValue().addOnSuccessListener {
                                                adapter.deleteItem(position)
                                                groupRecyclerView.adapter?.notifyItemRemoved(position)
                                                Toast.makeText(this@Class.requireContext(), "Selected group deleted successfully!", Toast.LENGTH_SHORT).show()
                                            }
                                        }.setNegativeButton("Cancel"){_,_ ->
                                            executeClass()
                                        }.show()

                                }
                            }



                        }
                    }
                    val touchHelper = ItemTouchHelper(swipeGestures)
                    touchHelper.attachToRecyclerView(classRecyclerView)
                    adapter.setOnItemClickListener(object : StudentClassAdapter.onItemClickListener{

                        override fun onItemClick(position: Int) {
                            val intent = Intent(this@Class.requireContext(), StudentRoomActivity::class.java)
                            roomID = classArrayList[position].roomID.toString()

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



                    })




                }
            }


            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
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
                                                Toast.makeText(this@Class.requireContext(), "Selected group deleted successfully!", Toast.LENGTH_SHORT).show()
                                            }
                                        }.setNegativeButton("Cancel"){_,_ ->
                                            executeGroup()
                                        }.show()

                                }
                            }



                        }
                    }
                    val touchHelper = ItemTouchHelper(swipeGestures)
                    touchHelper.attachToRecyclerView(groupRecyclerView)
                    adapter.setOnItemClickListener(object : StudentGroupAdapter.onItemClickListener{

                        override fun onItemClick(position: Int) {
                            val intent = Intent(this@Class.requireContext(), StudentRoomActivity::class.java)
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



                    })




                }
            }


            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
                .setPositiveButton("Okay"){_, _ ->
                    databaseGroup.child(subjectCode.text.toString()).get().addOnSuccessListener {
                        if (it.exists()) {
                            val letter = it.child("letter").value.toString()
                            val name = it.child("name").value.toString()
                            val roomID = it.child("roomID").value.toString()
                            val section = it.child("section").value.toString()
                            val pass = it.child("subjectCode").value.toString()
                            val subjectTitle = it.child("subjectTitle").value.toString()
                            val type = it.child("type").value.toString()
                            val joinGroupUser = FirebaseDatabase.getInstance().getReference("JoinClass")



                            val joinGroup = GroupListData(
                                name,
                                type,
                                section,
                                roomID,
                                uid,
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
                            val userID = auth.currentUser?.uid.toString()
                            val currentUser = FirebaseDatabase.getInstance().getReference("Students")
                            currentUser.child(userID).get().addOnSuccessListener {
                                if (it.exists()){
                                    val currentName = it.child("name").value.toString()
                                    val currentType = it.child("type").value.toString()
                                    val data = RoomMembersData(currentName, currentType, userID)
                                    val randomID = randomCode()
                                    val databasePublic = FirebaseDatabase.getInstance().getReference("Public Class")
                                    databasePublic.child(roomID).child("Members").child(uid).setValue(data)
                                }
                            }
                        } else {
                            showPopUp("Class not found.")
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
                .setPositiveButton("Okay"){_, _ ->
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




                            val joinGroup = GroupListData(name, type, section, roomID, uid, subjectTitle, subjectCode, letter)
                            joinGroupUser.child(uid).child(roomID).setValue(joinGroup).addOnCompleteListener{
                                executeGroup()
                                student_class_wall.setBackgroundResource(R.color.zero)
                                join_class.hide()
                                join_room.hide()
                                join_class_text.visibility = View.GONE
                                join_room_text.visibility = View.GONE

                                join_btn.shrink()
                            }
                            val userID = auth.currentUser?.uid.toString()
                            val currentUser = FirebaseDatabase.getInstance().getReference("Students")
                            currentUser.child(userID).get().addOnSuccessListener {
                                if (it.exists()){
                                    val currentName = it.child("name").value.toString()
                                    val currentType = it.child("type").value.toString()
                                    val data = RoomMembersData(currentName, currentType, userID)
                                    val databasePublic = FirebaseDatabase.getInstance().getReference("Public Group")
                                    databasePublic.child(roomID).child("Members").child(uid).setValue(data)
                                }
                            }
                        }else{
                            showPopUp("Group not found.")
                        }
                    }
                }.setNegativeButton("Cancel"){_, _ ->

                }
                .show()
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
    private fun showPopUp(message : String){
        MaterialAlertDialogBuilder(requireActivity())
            .setMessage(message)
            .setPositiveButton("Okay"){_, _ ->

            }
            .show()
    }

    private fun randomCode(): String = List(6) {
        (('a'..'z') + ('A'..'Z') + ('0'..'9')).random()
    }.joinToString("")
}
