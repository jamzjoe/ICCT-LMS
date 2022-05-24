package com.icct.icctlms.tools

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDateTime
import java.time.ZoneId

class RandomCode {
    val today = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        LocalDateTime.now()
    } else {
        TODO("VERSION.SDK_INT < O")
    }
    //use this key to sort arraylist
    @RequiresApi(Build.VERSION_CODES.O)
    val sortKey = today.toMillis().toString()

    fun randomCode(): String = List(20) {
        (('a'..'z') + ('A'..'Z') + ('0'..'9') + sortKey).random()
    }.joinToString("")

    fun sortKey(): String = List(sortKey.length){
        sortKey
    }.toString()


    @RequiresApi(Build.VERSION_CODES.O)
    fun LocalDateTime.toMillis(zone: ZoneId = ZoneId.systemDefault()) = atZone(zone)?.toInstant()?.toEpochMilli()
}