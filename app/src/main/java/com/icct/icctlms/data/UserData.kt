package com.icct.icctlms.data

data class UserData(
    val account_id: String ?= null,
    val email : String ?= null,
    val name : String ?= null,
    val school : String ?= null,
    val type: String ?= null

)
