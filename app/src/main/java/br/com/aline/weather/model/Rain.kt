package br.com.aline.weather.model

import com.google.gson.annotations.SerializedName

data class Rain(@SerializedName("1h") val precipitation: Float)
