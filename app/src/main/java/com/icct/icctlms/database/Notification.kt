package com.icct.icctlms.database

import android.content.Context
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.FirebaseDatabase
import com.icct.icctlms.data.NotificationData
import com.icct.icctlms.databinding.FragmentTeacherNotificationBinding

class Notification {
    private val notificationReference = FirebaseDatabase.getInstance().getReference("Notifications")
    private val reviewed = FirebaseDatabase.getInstance().getReference("Reviewed")
    private val studentReviewed = FirebaseDatabase.getInstance().getReference("Reviewed").child("Student")
    fun notification(
        uid: String,
        senderName: String,
        description: String,
        notificationID: String,
        dateTime: String,
        sortKey: String
    ) {
        val data = NotificationData(uid, senderName, description, notificationID, dateTime, sortKey)
        notificationReference.child(uid).child(notificationID).setValue(data)

    }

    fun studentNotification(
        uid: String,
        senderName: String,
        description: String,
        notificationID: String,
        dateTime: String,
        sortKey: String
    ) {
        val data = NotificationData(uid, senderName, description, notificationID, dateTime, sortKey)
        notificationReference.child("Student").child(uid).child(notificationID).setValue(data)

    }

    fun moveToReviewed(
        uid: String,
        senderName: String,
        description: String,
        notificationID: String,
        dateTime: String,
        sortKey: String
    ) {
        val data = NotificationData(uid, senderName, description, notificationID, dateTime, sortKey)
        reviewed.child(uid).child(notificationID).setValue(data)
    }

    fun moveToStudentReviewed(
        uid: String,
        senderName: String,
        description: String,
        notificationID: String,
        dateTime: String,
        sortKey: String
    ) {
        val data = NotificationData(uid, senderName, description, notificationID, dateTime, sortKey)
        studentReviewed.child(uid).child(notificationID).setValue(data)
    }

    fun deleteReviewed(uid: String, notificationID: String) {
        val deleteReviewed = FirebaseDatabase.getInstance().getReference("Reviewed").child(uid)
        deleteReviewed.child(notificationID).removeValue().addOnSuccessListener {

        }
    }

    fun deleteUnread(uid: String, notificationID: String) {
        val deleteUnread = FirebaseDatabase.getInstance().getReference("Notifications").child(uid)
        deleteUnread.child(notificationID).removeValue().addOnSuccessListener {
        }
    }

    fun deleteStudentUnread(uid: String, notificationID: String) {
        val deleteUnread = FirebaseDatabase.getInstance().getReference("Notifications").child("Student").child(uid)
        deleteUnread.child(notificationID).removeValue().addOnSuccessListener {
        }
    }

    fun deleteStudentReviewed(uid: String, notificationID: String) {
        val deleteReviewed = FirebaseDatabase.getInstance().getReference("Reviewed").child("Student").child(uid)
        deleteReviewed.child(notificationID).removeValue().addOnSuccessListener {

        }
    }
}