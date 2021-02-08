package com.example.emergencyalert.data.network

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.io.File

interface AppApi {

    @FormUrlEncoded
    @POST("auth/user-auth")
    fun sendOtp(
        @Field("phone") phone: String
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("auth/verify-user-otp")
    fun verifyOTP(
        @Field("phone") phone: String,
        @Field("otp") otp: String
    ): Call<ResponseBody>


    @POST("user/form")
    fun uploadImage(
        @Header("Authorization") token: String,
        @Body file:RequestBody
    ):Call<ResponseBody>

    companion object {
        operator fun invoke(): AppApi {
            return Retrofit.Builder()
                .baseUrl("http://androidform.herokuapp.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(AppApi::class.java)
        }
    }
}