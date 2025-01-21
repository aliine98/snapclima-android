package br.com.aline.weather.service

import br.com.aline.weather.model.CityGeocode
import br.com.aline.weather.model.CityName
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GeocodingService {
    companion object {
        const val BASE_URL = "https://api.openweathermap.org/geo/1.0/"
    }

    @GET("direct?limit=1")
    fun getCoordinates(@Query("q") cityName: String, @Query("appid") appid: String): Call<List<CityGeocode>>

    @GET("reverse?limit=1")
    fun getCityName(@Query("lat") lat: String, @Query("lon") lon: String, @Query("appid") appid: String):
    Call<List<CityName>>

}