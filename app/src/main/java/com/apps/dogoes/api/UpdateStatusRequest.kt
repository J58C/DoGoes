package com.apps.dogoes.api

data class UpdateStatusRequest(
    val status: Boolean,
    val geotag: String
)