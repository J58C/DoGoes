package com.apps.dogoes.api

import com.google.gson.JsonObject

data class UserResponse(
    val msg: String,
    val user: JsonObject
)