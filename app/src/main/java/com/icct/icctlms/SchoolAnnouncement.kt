package com.icct.icctlms

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.*
import com.icct.icctlms.adapter.ViewersAnnouncementAdapter
import com.icct.icctlms.data.AnnouncementData

class SchoolAnnouncement : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var database : DatabaseReference
    private lateinit var dialog : Dialog
    private lateinit var announcementArrayList: ArrayList<AnnouncementData>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_school_announcement)

        database = FirebaseDatabase.getInstance().getReference("Admin").child("Announcements")
        
        recyclerView = findViewById(R.id.teacher_news_and_updates)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this.applicationContext)
        
        announcementArrayList = arrayListOf()
        
        executeNews()
    }

    private fun executeNews() {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    announcementArrayList.clear()


                    for (postSnapShot in snapshot.children){
                        val announcement = postSnapShot.getValue(AnnouncementData::class.java)
                        announcementArrayList.add(announcement!!)
                    }
                    val adapter = ViewersAnnouncementAdapter(announcementArrayList)
                    announcementArrayList.sortByDescending {
                        it.announcerName
                    }
                    recyclerView.adapter = adapter

                    //adapter click listener
                    adapter.setOnItemClickListener(object : ViewersAnnouncementAdapter.onItemClickListener{
                        override fun onItemClick(position: Int) {
                            val message = announcementArrayList[position].description

                            MaterialAlertDialogBuilder(this@SchoolAnnouncement)
                                .setMessage("Announcement: $message")
                                .setPositiveButton("OKAY"){_,_ ->
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

    private fun progressDialogShow(){
        dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_layout)
        dialog.setTitle("Loading please wait")
        dialog.setCancelable(false)
        dialog.show()
    }
    private fun progressDialogHide(){
        dialog.hide()
    }
}