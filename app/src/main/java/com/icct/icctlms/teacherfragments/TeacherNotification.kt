package com.icct.icctlms.teacherfragments

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.icct.icctlms.R
import com.icct.icctlms.adapter.NotificationAdapter
import com.icct.icctlms.data.NotificationData
import com.icct.icctlms.database.CurrentUser
import com.icct.icctlms.database.Notification
import com.icct.icctlms.databinding.FragmentTeacherNotificationBinding
import com.icct.icctlms.gestures.SwipeGestures
import kotlinx.android.synthetic.main.room_members_item.*

class TeacherNotification : Fragment() {


    private var _binding: FragmentTeacherNotificationBinding? = null
    private val binding get() = _binding!!
    private lateinit var recyclerView: RecyclerView
    private lateinit var notificationArrayList: ArrayList<NotificationData>
    private lateinit var getNotification : DatabaseReference
    private lateinit var uid: String
    private lateinit var dialog: Dialog
    private lateinit var reviewedArrayList: ArrayList<NotificationData>
    private lateinit var getReviewed : DatabaseReference

    private lateinit var reviewedRecyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentTeacherNotificationBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        recyclerView = binding.notificationRecyclerview
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)

        reviewedRecyclerView = binding.notificationReviewedRecyclerview
        reviewedRecyclerView.setHasFixedSize(true)
        reviewedRecyclerView.layoutManager = LinearLayoutManager(context)

        notificationArrayList = arrayListOf()
        reviewedArrayList = arrayListOf()
        uid = Firebase.auth.currentUser?.uid.toString()
        getNotification = FirebaseDatabase.getInstance().getReference("Notifications").child(uid)
        getReviewed = FirebaseDatabase.getInstance().getReference("Reviewed").child(uid)
      executeNotification()
        executeReviewed()
        binding.refresh.setOnClickListener{
            startActivity(activity?.intent)
            activity?.finish()
        }
        progressDialogShow()
        return binding.root
    }

    private fun executeReviewed() {
        getReviewed.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()){
                    reviewedArrayList.clear()

                    for (classSnapShot in snapshot.children){
                        val classData = classSnapShot.getValue(NotificationData::class.java)!!
                        reviewedArrayList.add(classData)
                    }
                    val adapter = NotificationAdapter(reviewedArrayList)
                    reviewedRecyclerView.adapter = adapter
                    progressDialogHide()

                    val swipeGestures = object : SwipeGestures(this@TeacherNotification.requireContext()){

                        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                            val position : Int = viewHolder.adapterPosition
                            when(direction){
                                ItemTouchHelper.LEFT -> {
                                    MaterialAlertDialogBuilder(this@TeacherNotification.requireContext())
                                        .setMessage("Delete this notification?")
                                        .setPositiveButton("Delete"){_,_ ->
                                            val notificationID = reviewedArrayList[position].notificationID.toString()
                                            val description = reviewedArrayList[position].description.toString()

                                            val deleteReviewed = Notification()
                                            deleteReviewed.deleteReviewed(uid, notificationID)
                                            Snackbar.make(binding.root, "Deleted", Snackbar.LENGTH_SHORT).show()

                                            adapter.deleteItem(position)
                                            adapter.notifyItemRemoved(position)
                                            reviewedRecyclerView.adapter?.notifyItemRemoved(position)
                                            executeNotification()
                                            executeReviewed()
                                        }.setNegativeButton("Cancel"){_,_ ->
                                            executeNotification()
                                            executeReviewed()
                                        }.setOnCancelListener(){
                                            executeReviewed()
                                            executeNotification()
                                        }
                                        .show()

                                }
                            }



                        }
                    }
                    val touchHelper = ItemTouchHelper(swipeGestures)
                    touchHelper.attachToRecyclerView(reviewedRecyclerView)
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

    private fun executeNotification() {
        getNotification.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()){
                    notificationArrayList.clear()

                    for (classSnapShot in snapshot.children){
                        val classData = classSnapShot.getValue(NotificationData::class.java)!!
                        notificationArrayList.add(classData)
                    }
                    val adapter = NotificationAdapter(notificationArrayList)
                    recyclerView.adapter = adapter
                    progressDialogHide()

                    val swipeGestures = object : SwipeGestures(this@TeacherNotification.requireContext()){

                        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                            val position : Int = viewHolder.adapterPosition
                            when(direction){
                                ItemTouchHelper.LEFT -> {
                                    MaterialAlertDialogBuilder(this@TeacherNotification.requireContext())
                                        .setMessage("Mark as read?")
                                        .setPositiveButton("Okay"){_,_ ->
                                        val notificationID = notificationArrayList[position].notificationID.toString()
                                            val description = notificationArrayList[position].description.toString()

                                            Snackbar.make(binding.root, "Move to reviewed.", Snackbar.LENGTH_SHORT).show()
                                            val action = Notification()
                                            action.deleteUnread(uid, notificationID)
                                            action.moveToReviewed(uid, "", description, notificationID)

                                            adapter.deleteItem(position)
                                            recyclerView.adapter?.notifyItemRemoved(position)
                                            adapter.notifyItemRemoved(position)
                                            executeNotification()
                                            executeReviewed()

                                        }.setNegativeButton("Cancel"){_,_ ->
                                           executeNotification()
                                            executeReviewed()
                                        }.setOnCancelListener(){
                                            executeNotification()
                                            executeReviewed()
                                        }.show()

                                }
                            }



                        }
                    }
                    val touchHelper = ItemTouchHelper(swipeGestures)
                    touchHelper.attachToRecyclerView(recyclerView)
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
}