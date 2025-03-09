package com.apps.dogoes.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

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
    @POST("login")
    fun loginUser(
        @Body request: LoginRequest
    ): Call<UserResponse>

    @PUT("clients/{id}")
    fun updateUserStatus(
        @Path("id") userId: String,
        @Body request: UpdateStatusRequest
    ): Call<UserResponse>
}