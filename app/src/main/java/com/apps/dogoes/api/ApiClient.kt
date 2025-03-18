package com.apps.dogoes.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.DELETE
import retrofit2.http.GET

object ApiClient {
    private const val BASE_URL = "https://sigmaskibidi.my.id/api/"

    val instance: ApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(ApiService::class.java)
    }
}

interface ApiService {
    @GET("api/announcements/user/{userID}")
    fun getUserAnnouncements(
        @Path("userID") userId: String
    ): Call<List<Announcement>>

    @POST("login")
    fun loginUser(
        @Body request: LoginRequest
    ): Call<UserResponse>

    @PUT("clients/{id}")
    fun updateUserStatus(
        @Path("id") userId: String,
        @Body request: UpdateStatusRequest
    ): Call<UserResponse>

    @PUT("password/{iduser}")
    fun changePassword(
        @Path("iduser") userId: String,
        @Body request: ChangePasswordRequest
    ): Call<Void>

    @POST("password/mailpw")
    fun sendResetPasswordEmail(
        @Body request: ResetPasswordRequest
    ): Call<Void>

    @POST("announcements")
    fun uploadAnnouncement(
        @Body announcement: AnnouncementRequest
    ): Call<AnnouncementResponse>

    @DELETE("announcements/{id}")
    fun deleteAnnouncement(
        @Path("id") id: String
    ): Call<Void>
}