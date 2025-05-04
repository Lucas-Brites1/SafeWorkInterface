package com.safework.api

import ApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitManager {
    private const val API_URL = "https://1e0f-179-159-219-68.ngrok-free.app"

    private val retrofit: Retrofit by lazy {
        Retrofit
            .Builder()
            .baseUrl(API_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    internal fun <T> createService(serviceClass: Class<T>): T {
        return retrofit.create(serviceClass)
    }
}