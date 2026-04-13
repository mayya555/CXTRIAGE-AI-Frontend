package com.simats.cxtriageai

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    // 10.0.2.2 for Emulator, Local Machine IP (detected as 10.137.176.10) for Physical Device
    const val BASE_URL = "http://180.235.121.245:8033/"
    const val GET_STATIC_URL = "${BASE_URL}static/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: ApiService by lazy { 
        retrofit.create(ApiService::class.java)
    }
}
