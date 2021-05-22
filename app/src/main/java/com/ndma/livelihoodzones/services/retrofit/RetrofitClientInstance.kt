package com.ndma.livelihoodzones.services.retrofit

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClientInstance {
    fun getRetrofitInstance(baseUrl: String): Retrofit {
        val httpClient =
            OkHttpClient.Builder().connectTimeout(60, TimeUnit.SECONDS)
        httpClient.addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .build()
            chain.proceed(request)
        }
        return Retrofit.Builder().addConverterFactory(GsonConverterFactory.create())
            .baseUrl(baseUrl).client(httpClient.build()).build()
    }

    fun<T> buildService(service: Class<T>,baseUrl: String): T{
        return getRetrofitInstance(baseUrl)?.create(service)
    }

}