package com.apps.dogoes.api

data class UpdateStatusRequest(
    val status: Int,
    val geotag: String,
    val notes: String
)