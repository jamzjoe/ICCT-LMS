package com.icct.icctlms.data

data class HelpCenterData(
    val questions : String ?= null,
    val response : String ?= null,
    val uid : String ?= null,
    val sortKey : String ?= null)
