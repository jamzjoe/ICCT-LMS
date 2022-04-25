package com.icct.icctlms.fragments

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.icct.icctlms.R
import com.icct.icctlms.adapter.NotificationAdapter
import com.icct.icctlms.data.NotificationData
import com.icct.icctlms.database.Notification
import com.icct.icctlms.databinding.FragmentNotificationBinding
import com.icct.icctlms.gestures.SwipeGestures
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class Notification : Fragment() {
    private var _binding : FragmentNotificationBinding? = null
    private val binding get() = _binding!!
    private lateinit var unreadRecyclerView: RecyclerView
    private lateinit var readRecyclerView: RecyclerView
    private lateinit var readArrayList: ArrayList<NotificationData>
    private lateinit var unreadArrayList: ArrayList<NotificationData>
    private lateinit var uid : String
    private lateinit var hour : String
    private lateinit var finalHour : String
    private lateinit var today : Calendar
    private lateinit var date : String
    private lateinit var sortKey : String
    private lateinit var getUnread : DatabaseReference
    private lateinit var getRead : DatabaseReference
    private lateinit var dialog: Dialog
    private lateinit var dateReviewed : String


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationBinding.inflate(inflater, container, false)
        unreadRecyclerView = binding.studentNotificationRec
        unreadRecyclerView.setHasFixedSize(true)
        unreadRecyclerView.layoutManager = LinearLayoutManager(context)
        
        readRecyclerView = binding.studentNotificationReviewedRecyclerview
        readRecyclerView.setHasFixedSize(true)
        readRecyclerView.layoutManager = LinearLayoutManager(context)
        
        readArrayList = arrayListOf()
        unreadArrayList = arrayListOf()


        uid = Firebase.auth.currentUser?.uid.toString()

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
        dateReviewed = "Reviewed in $trimMonth $day at $finalHour"
        //use this key to sort arraylist
        sortKey = now.toMillis().toString()
        // Inflate the layout for this fragment
        
        getUnread = FirebaseDatabase.getInstance().getReference("Notifications").child("Student").child(uid)
        getRead = FirebaseDatabase.getInstance().getReference("Reviewed").child("Student").child(uid)
        
        executeRead()
        executeUnread()
        progressDialogShow()
        return binding.root
    }

    private fun executeRead() {

        getRead.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()){
                    readArrayList.clear()

                    for (classSnapShot in snapshot.children){
                        val classData = classSnapShot.getValue(NotificationData::class.java)!!
                        readArrayList.add(classData)
                    }
                    val adapter = NotificationAdapter(readArrayList)
                    readRecyclerView.adapter = adapter
                    progressDialogHide()
                    readArrayList.sortByDescending {
                        it.sortKey
                    }

                    val swipeGestures = object : SwipeGestures(this@Notification.requireContext()){

                        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                            val position : Int = viewHolder.adapterPosition
                            when(direction){
                                ItemTouchHelper.LEFT -> {
                                    MaterialAlertDialogBuilder(this@Notification.requireContext())
                                        .setMessage("Delete this notification?")
                                        .setPositiveButton("Delete"){_,_ ->
                                            val notificationID = readArrayList[position].notificationID.toString()
                                            val description = readArrayList[position].description.toString()

                                            val deleteReviewed = Notification()
                                            deleteReviewed.deleteStudentReviewed(uid, notificationID)
                                            Snackbar.make(binding.root, "Deleted", Snackbar.LENGTH_SHORT).show()

                                            adapter.deleteItem(position)
                                            adapter.notifyItemRemoved(position)
                                            readRecyclerView.adapter?.notifyItemRemoved(position)
                                            executeUnread()
                                            executeRead()
                                        }.setNegativeButton("Cancel"){_,_ ->
                                            executeUnread()
                                            executeRead()
                                        }.setOnCancelListener(){
                                            executeRead()
                                            executeUnread()
                                        }
                                        .show()

                                }
                            }



                        }
                    }
                    val touchHelper = ItemTouchHelper(swipeGestures)
                    touchHelper.attachToRecyclerView(readRecyclerView)
                    adapter.setOnItemClickListener(object : NotificationAdapter.onItemClickListener{

                        override fun onItemClick(position: Int) {

                            val delete = FirebaseDatabase.getInstance().getReference()


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

    private fun executeUnread() {
        getUnread.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()){
                    unreadArrayList.clear()

                    for (classSnapShot in snapshot.children){
                        val classData = classSnapShot.getValue(NotificationData::class.java)!!
                        unreadArrayList.add(classData)
                    }
                    val adapter = NotificationAdapter(unreadArrayList)
                    unreadRecyclerView.adapter = adapter
                    progressDialogHide()
                    unreadArrayList.sortByDescending {
                        it.sortKey
                    }

                    val swipeGestures = object : SwipeGestures(this@Notification.requireContext()){

                        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                            val position : Int = viewHolder.adapterPosition
                            when(direction){
                                ItemTouchHelper.LEFT -> {
                                    MaterialAlertDialogBuilder(this@Notification.requireContext())
                                        .setMessage("Mark as read?")
                                        .setPositiveButton("Okay"){_,_ ->
                                            val notificationID = unreadArrayList[position].notificationID.toString()
                                            val description = unreadArrayList[position].description.toString()

                                            Snackbar.make(binding.root, "Move to reviewed.", Snackbar.LENGTH_SHORT).show()
                                            val action = Notification()
                                            action.deleteStudentUnread(uid, notificationID)
                                            action.moveToStudentReviewed(uid, "", description, notificationID, dateReviewed, sortKey)

                                            adapter.deleteItem(position)
                                            unreadRecyclerView.adapter?.notifyItemRemoved(position)
                                            adapter.notifyItemRemoved(position)
                                            executeUnread()
                                            executeRead()

                                        }.setNegativeButton("Cancel"){_,_ ->
                                            executeUnread()
                                            executeRead()
                                        }.setOnCancelListener(){
                                            executeUnread()
                                            executeRead()
                                        }.show()

                                }
                            }



                        }
                    }
                    val touchHelper = ItemTouchHelper(swipeGestures)
                    touchHelper.attachToRecyclerView(unreadRecyclerView)
                    adapter.setOnItemClickListener(object : NotificationAdapter.onItemClickListener{

                        override fun onItemClick(position: Int) {

                            val delete = FirebaseDatabase.getInstance().getReference()


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
    @RequiresApi(Build.VERSION_CODES.O)
    fun LocalDateTime.toMillis(zone: ZoneId = ZoneId.systemDefault()) = atZone(zone)?.toInstant()?.toEpochMilli()


}