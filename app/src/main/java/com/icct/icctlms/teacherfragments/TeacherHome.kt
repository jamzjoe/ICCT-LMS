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
import com.google.firebase.database.*
import com.icct.icctlms.Authentication.Teacher.CreateTeacherPost
import com.icct.icctlms.R
import com.icct.icctlms.adapter.TeacherPostAdapter
import com.icct.icctlms.data.RoomIDData
import com.icct.icctlms.data.TeacherPostData
import kotlinx.android.synthetic.main.fragment_teacher_class.*
import kotlinx.android.synthetic.main.fragment_teacher_home.*





class TeacherHome : Fragment() {

    private lateinit var postArrayList: ArrayList<TeacherPostData>
    private lateinit var dbref: DatabaseReference
    private lateinit var recyclerView: RecyclerView
    private lateinit var roomIDArrayList: ArrayList<String>
    private lateinit var roomIDList : ArrayList<String>
    private lateinit var text : AutoCompleteTextView
    private lateinit var databaseRoom : DatabaseReference
    private lateinit var dbrefClass : DatabaseReference
    private lateinit var dialog : Dialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_teacher_home, container, false)
        val postButton = rootView.findViewById<FloatingActionButton>(R.id.post_message)
        val classButton = rootView.findViewById<FloatingActionButton>(R.id.post_class)
        recyclerView = rootView.findViewById(R.id.post_list)!!
        val auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid.toString()
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)
        postArrayList = arrayListOf()
        roomIDList = arrayListOf()
        roomIDArrayList = arrayListOf()
        progressDialogShow()



        text = rootView.findViewById<AutoCompleteTextView>(R.id.auto_complete_txt)

        databaseRoom = FirebaseDatabase.getInstance().getReference("Group").child(uid)
        databaseRoom.addListenerForSingleValueEvent(object  : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                roomIDList.clear()
                if (snapshot.exists()){
                    progressDialogHide()
                    roomIDList.clear()
                    for (each in snapshot.children){

                        val myID = each.getValue(RoomIDData::class.java)?.roomID.toString()

                        roomIDArrayList.add(myID)
                        val adapterItems = ArrayAdapter<String>(this@TeacherHome.requireContext(), R.layout.choose_list, roomIDArrayList)
                        text.setAdapter(adapterItems)

                    }



                }else{
                    progressDialogHide()
                }



                text.setOnItemClickListener { parent, _, position, _ ->
                    val item = parent.getItemAtPosition(position).toString()
                    roomIDList.clear()

                    dbref = FirebaseDatabase.getInstance().getReference("Group Post")
                        .child("Room ID: $item")
                    listenGroup()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

        databaseRoom = FirebaseDatabase.getInstance().getReference("Class").child(uid)
        databaseRoom.addListenerForSingleValueEvent(object  : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                roomIDList.clear()
                if (snapshot.exists()){
                    roomIDList.clear()
                    for (each in snapshot.children){

                        val myID = each.getValue(RoomIDData::class.java)?.roomID.toString()

                        roomIDArrayList.add(myID)
                        val adapterItems = ArrayAdapter<String>(this@TeacherHome.requireContext(), R.layout.choose_list, roomIDArrayList)
                        text.setAdapter(adapterItems)

                    }



                }



                text.setOnItemClickListener { parent, _, position, _ ->
                    val item = parent.getItemAtPosition(position).toString()
                    roomIDList.clear()


                    dbrefClass = FirebaseDatabase.getInstance().getReference("Class Post")
                        .child("Room ID: $item")
                    dbref = FirebaseDatabase.getInstance().getReference("Group Post")
                        .child("Room ID: $item")
                    listenClass()
                    listenGroup()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })


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

        return rootView
    }

    private fun listenClass() {
        dbrefClass.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    postArrayList.clear()

                    for (classSnapShot in snapshot.children){
                        val postData = classSnapShot.getValue(TeacherPostData::class.java)!!
                        postArrayList.add(postData)
                    }
                    val adapter = TeacherPostAdapter(postArrayList)
                    postArrayList.sortByDescending {
                        it.sortKey
                    }
                    recyclerView.adapter = adapter



                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun listenGroup() {
        dbref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    postArrayList.clear()

                    for (classSnapShot in snapshot.children){
                        val postData = classSnapShot.getValue(TeacherPostData::class.java)!!
                        postArrayList.add(postData)
                    }
                    val adapter = TeacherPostAdapter(postArrayList)
                    postArrayList.sortByDescending {
                        it.sortKey
                    }
                    recyclerView.adapter = adapter



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