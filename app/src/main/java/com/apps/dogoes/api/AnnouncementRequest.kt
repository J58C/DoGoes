package com.apps.dogoes.api

data class AnnouncementRequest(
    val title: String,
    val content: String
)

data class AnnouncementResponse(
    val title: String,
    val content: String
)