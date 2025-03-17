@file:Suppress("PropertyName")

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

data class UpdateStatusRequest(
    val status: Int,
    val geotag: String,
    val notes: String
)

data class AnnouncementRequest(
    val user_id: String,
    val title: String,
    val content: String
)

data class AnnouncementResponse(
    val _id: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class ChangePasswordRequest(
    val oldPW: String,
    val newPW: String
)

data class ResetPasswordRequest(
    val email: String
)