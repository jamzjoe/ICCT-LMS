package com.icct.icctlms.data

data class NotificationData(
    val notificationType : String ?= null,
    val senderName: String ?= null,
    val description: String ?= null,
    val notificationID : String ?= null,
    val dateTime : String ?= null,
    val sortKey : String ?= null)
