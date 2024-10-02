package com.example.artimo_smart_frame

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface TherapyApiService {
    @GET("diary/all")
    suspend fun getVideoList(): DataTherapyModel

    @GET("diary/latest")
    suspend fun getLatest(): LatestTherapyModel

    @POST("diary/set-light-color/{id}")
    suspend fun setIoT(@Path("id") id: String): Response<String>
}

