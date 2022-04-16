package com.icct.icctlms.database

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class CurrentUser {
    fun currentUser(){
        val uid = Firebase.auth.currentUser?.uid.toString()
        println(uid)
    }
}