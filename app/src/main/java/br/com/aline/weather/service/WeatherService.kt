package br.com.aline.weather.service

import br.com.aline.weather.model.Weather
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherService {
    companion object {
        const val BASE_URL = "https://api.openweathermap.org/data/2.5/"
    }

    @GET("weather?units=metric")
    fun getCurrentWeather(
        @Query("lang") lang: String,
        @Query("lat") lat: String,
        @Query("lon") lon: String,
        @Query("appid") appid: String
    ): Call<Weather>
}