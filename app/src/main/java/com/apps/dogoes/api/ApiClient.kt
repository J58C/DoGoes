package com.apps.testapp.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call
import retrofit2.http.GET

object ApiClient {
    private const val BASE_URL = "https://tesbackv1.vercel.app/api/"

    val instance: ApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(ApiService::class.java)
    }
}

interface ApiService {
    @GET("products")
    fun getUsers(): Call<List<UserResponse>>
}