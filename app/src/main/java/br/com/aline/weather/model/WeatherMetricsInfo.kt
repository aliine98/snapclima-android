package br.com.aline.weather.model

import com.google.gson.annotations.SerializedName

data class WeatherMetricsInfo(
    val temp: Float,
    @SerializedName("temp_min") val tempMin: Float,
    @SerializedName("temp_max") val tempMax: Float,
    val humidity: Int
)