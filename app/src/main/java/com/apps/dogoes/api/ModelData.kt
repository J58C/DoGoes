@file:Suppress("PropertyName")

package com.apps.dogoes.api

data class AddAnnouncementResponse(
    val _id: String
)

data class AddAnnouncementRequest(
    val title: String,
    val content: String,
    val user_id: String,
    val secret_key: String
)

data class AnnouncementResponse(
    val announcement_id: String,
    val title: String,
    val content: String,
    val user_id: String,
)

data class AnnouncementRequest(
    val secret_key: String
)

data class ChangePasswordRequest(
    val oldPW: String,
    val newPW: String,
    val secret_key: String
)

data class DeleteAnnouncementRequest(
    val secret_key: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class ResetPasswordRequest(
    val email: String
)

data class UpdateStatusRequest(
    val status: Int,
    val geotag: String,
    val notes: String,
    val secret_key: String
)

data class UserResponse(
    val _id: String,
    val name: String,
    val email: String,
    val role: String,
    val status: Int,
    val geotag: String,
    val notes: String,
    val token: String
)