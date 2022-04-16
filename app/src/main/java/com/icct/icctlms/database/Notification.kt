package com.icct.icctlms.database

import android.content.Context
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.FirebaseDatabase
import com.icct.icctlms.data.NotificationData
import com.icct.icctlms.databinding.FragmentTeacherNotificationBinding

class Notification {
    private val notificationReference = FirebaseDatabase.getInstance().getReference("Notifications")
    private val reviewed = FirebaseDatabase.getInstance().getReference("Reviewed")
    fun notification(uid : String, senderName: String, description: String, notificationID : String){
        val data = NotificationData(uid, senderName, description, notificationID)
        notificationReference.child(uid).child(notificationID).setValue(data)

    }
    fun moveToReviewed(uid : String, senderName: String, description: String, notificationID: String){
        val data = NotificationData(uid, senderName, description, notificationID)
        reviewed.child(uid).child(notificationID).setValue(data)
    }
    fun deleteReviewed(uid: String, notificationID: String){
        val deleteReviewed = FirebaseDatabase.getInstance().getReference("Reviewed").child(uid)
        deleteReviewed.child(notificationID).removeValue().addOnSuccessListener {

        }
    }
    fun deleteUnread(uid: String, notificationID: String ){
val deleteUnread = FirebaseDatabase.getInstance().getReference("Notifications").child(uid)
        deleteUnread.child(notificationID).removeValue().addOnSuccessListener {
        }
    }

}