package com.example.emergencyalert.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.emergencyalert.data.network.AppApi
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class AuthRepository {
    fun sendOtp(phone:String) : LiveData<String> {
        val loginResponse= MutableLiveData<String>();
        AppApi().sendOtp(phone)
            .enqueue(object : Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    loginResponse.value=t.message;
                }

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if(response.isSuccessful){
                        loginResponse.value=response.body()?.string()
                    }
                    else{
                        loginResponse.value=response.errorBody()?.string();
                    }
                }
            })
        return loginResponse;
    }

    fun VerifyOTP(phone: String,otp:String):LiveData<String>{
        var otpresponse=MutableLiveData<String>();
        AppApi().verifyOTP(phone, otp)
            .enqueue(object :Callback<ResponseBody>{
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    otpresponse.value=t.message;
                }

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if(response.isSuccessful&&response.code()==201){
                        otpresponse.value=response.body()?.string()
                    }
                    else{
                        otpresponse.value=response.errorBody()?.string();
                    }
                }

            });
        return otpresponse;
    }

    fun setProfileImage(token:String,fromlat:Double,fromLng:Double,file:String,desc:String): LiveData<String> {
        var responsebody= MutableLiveData<String>();
        var data: File?= File(file)
        Log.d("AuthRepo", "setProfileImage: "+file)
        val builder=MultipartBody.Builder().setType(MultipartBody.FORM);
        builder.addFormDataPart("fromLat",fromlat.toString())
            .addFormDataPart("fromLng",fromLng.toString())
            .addFormDataPart("imageDesc",desc)
            .addFormDataPart("image",data!!.name, RequestBody.create(MultipartBody.FORM,file));
        val requestBody=builder.build();
        AppApi().uploadImage(token,requestBody)
            .enqueue(object : Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    responsebody.value=t.message;
                }
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    Log.d("Upload Image", "onResponse: "+response.code())
                    if(response.isSuccessful){
                        responsebody.value=response.body()?.string()
                    }
                    else{
                        responsebody.value=response.errorBody()?.string();
                    }
                }

            });
        return responsebody;
    }
}