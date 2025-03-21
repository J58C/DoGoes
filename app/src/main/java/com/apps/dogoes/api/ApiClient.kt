package com.apps.dogoes.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

object ApiClient {
    private const val BASE_URL = "https://sigmaskibidi.my.id/"

    val instance: ApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(ApiService::class.java)
    }
}

interface ApiService {
    @POST("login")
    fun loginUser(
        @Body request: LoginRequest
    ): Call<UserResponse>

    @PUT("api/clients/update/{id}")
    fun updateUserStatus(
        @Path("id") userId: String,
        @Body request: UpdateStatusRequest
    ): Call<UserResponse>

    @PUT("api/password/update/{id}")
    fun changePassword(
        @Path("id") id: String,
        @Body request: ChangePasswordRequest
    ): Call<Void>

    @POST("password/mailpw")
    fun sendResetPasswordEmail(
        @Body request: ResetPasswordRequest
    ): Call<Void>

    @POST("api/announcements/add")
    fun uploadAnnouncement(
        @Body announcement: AddAnnouncementRequest
    ): Call<AddAnnouncementResponse>

    @POST("api/announcements/delete/{id}")
    fun deleteAnnouncement(
        @Path("id") id: String,
        @Body request: DeleteAnnouncementRequest
    ): Call<Void>

    @POST("api/announcements/user/{id}")
    fun getUserAnnouncements(
        @Path("id") id: String,
        @Body request: AnnouncementRequest
    ): Call<List<AnnouncementResponse>>
}