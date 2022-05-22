package com.icct.icctlms.database

import com.google.firebase.database.FirebaseDatabase
import com.icct.icctlms.data.NewMessage

class NewMessageIsTrue {

    fun newMessage(message : String, uid : String){
        val data = NewMessage(message)
        FirebaseDatabase.getInstance().getReference("Chat").child("StudentSend").child(uid).child("NewMessage").setValue(data)
    }

    fun teacherNewMessage(message : String, uid : String){
        val data = NewMessage(message)
        FirebaseDatabase.getInstance().getReference("Chat").child("TeacherReceived").child(uid).child("NewMessage").setValue(data)
    }
}