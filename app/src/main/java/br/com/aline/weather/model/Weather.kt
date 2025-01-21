package br.com.aline.weather.model

data class Weather(val weather: List<WeatherInfo>, val main: WeatherMetricsInfo, val wind: Wind, val rain: Rain?)