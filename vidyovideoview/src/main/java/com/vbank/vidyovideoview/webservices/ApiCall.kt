package com.vbank.vidyovideoview.webservices

import com.vbank.vidyovideoview.helper.AppConstant
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiCall {
    /*This is used to create retrofit object for performing network opration*/

    val retrofitClient : ApiStories by lazy {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY
        val okHttpClient: OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(logging).build()
        val retrofit = Retrofit.Builder()
            .baseUrl(AppConstant.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
        return@lazy retrofit.create(ApiStories::class.java)
    }
}