package com.apps.testapp.api

data class UserResponse(
    val _id: String,
    val status: Boolean,
    val geotag: String,
    val name: String,
    val email: String,
    val role: String,
    val password: String,
    val createdAt: String,
    val updatedAt: String
)
