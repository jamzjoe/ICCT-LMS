package com.icct.icctlms.teacherfragments

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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.icct.icctlms.R
import com.icct.icctlms.adapter.RecipientAdapter
import com.icct.icctlms.adapter.TeacherRecipientAdapter
import com.icct.icctlms.data.RecipientData
import com.icct.icctlms.data.UserData
import com.icct.icctlms.databinding.FragmentTeacherMessageBinding
import com.icct.icctlms.messages.StudentMessageRoom
import com.icct.icctlms.messages.TeacherMessageRoom
import kotlinx.android.synthetic.main.fragment_teacher_class.*


class TeacherMessage : Fragment() {
    private var _binding : FragmentTeacherMessageBinding? = null
    private val binding get() = _binding!!
    private lateinit var teacherRecipientRecyclerView : RecyclerView
    private lateinit var teacherRecipientArrayList: ArrayList<UserData>
    private lateinit var getTeacherRecipient : DatabaseReference
    private lateinit var uid : String
    private lateinit var dialog: Dialog
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTeacherMessageBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        uid = Firebase.auth.currentUser?.uid.toString()
        teacherRecipientRecyclerView = binding.teacherRecipientList
        teacherRecipientRecyclerView.setHasFixedSize(true)
        teacherRecipientRecyclerView.layoutManager = LinearLayoutManager(context)

        teacherRecipientArrayList = arrayListOf()
        progressDialogShow()
        getTeacherRecipient = FirebaseDatabase.getInstance().getReference("Chat")
            .child("TeacherRecipientList")
        showRecipientList()
        return binding.root
    }

    private fun showRecipientList() {
        getTeacherRecipient.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    teacherRecipientArrayList.clear()


                    for (postSnapShot in snapshot.children){
                        val members = postSnapShot.getValue(UserData::class.java)
                        teacherRecipientArrayList.add(members!!)
                    }
                    val adapter = TeacherRecipientAdapter(teacherRecipientArrayList)
                    teacherRecipientArrayList.sortByDescending {
                        it.name
                    }
                    teacherRecipientRecyclerView.adapter = adapter
                    progressDialogHide()

                    //adapter click listener
                    adapter.setOnItemClickListener(object : TeacherRecipientAdapter.onItemClickListener{
                        override fun onItemClick(position: Int) {
                            val studentUID = teacherRecipientArrayList[position].account_id
                            val studentName = teacherRecipientArrayList[position].name
                            val intent = (Intent(this@TeacherMessage.requireContext(), TeacherMessageRoom::class.java))
                            intent.putExtra("student_name", studentName)
                            intent.putExtra("student_uid", studentUID)
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