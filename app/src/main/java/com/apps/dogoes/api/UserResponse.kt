package com.apps.dogoes.api

data class UserResponse(
    val _id: String,
    val status: Int,
    val geotag: String,
    val name: String,
    val email: String,
    val role: String,
    val token: String,
    val notes: String
)