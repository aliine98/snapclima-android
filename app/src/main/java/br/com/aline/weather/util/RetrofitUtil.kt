package br.com.aline.weather.util

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitUtil {
    fun getInstance(baseUrl: String): Retrofit {
        return Retrofit.Builder().baseUrl(baseUrl).addConverterFactory(GsonConverterFactory.create()).build()
    }
}