package br.com.aline.weather

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import br.com.aline.weather.model.CityGeocode
import br.com.aline.weather.model.Weather
import br.com.aline.weather.model.CityName
import br.com.aline.weather.service.GeocodingService
import br.com.aline.weather.service.WeatherService
import br.com.aline.weather.util.RetrofitUtil
import coil3.load
import com.google.android.gms.location.*
import retrofit2.*
import weather.app.BuildConfig
import java.util.*
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {
    private lateinit var geocodingService: GeocodingService
    private lateinit var weatherService: WeatherService

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private lateinit var main: ConstraintLayout
    private lateinit var searchInput: TextView
    private lateinit var btnSearch: Button
    private lateinit var cityNameView: TextView
    private lateinit var placeholderIcon: ProgressBar
    private lateinit var icon: ImageView
    private lateinit var currentTemperature: TextView
    private lateinit var weatherCondition: TextView
    private lateinit var minTemperature: TextView
    private lateinit var maxTemperature: TextView
    private lateinit var humidity: TextView
    private lateinit var rainView: TextView
    private lateinit var windView: TextView

    private val weatherApiKey = BuildConfig.WEATHER_API_KEY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupViews()
        setupLocation()
        setupRetrofitServices()
        updateViewBasedOnLocation()
        setupSearchListeners()
        setupInsets()

        main.setOnClickListener {
            hideKeyboard()
        }
    }

    private fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(main) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = insets.top
                bottomMargin = insets.bottom
            }

            WindowInsetsCompat.CONSUMED
        }
    }

    private fun setupSearchListeners() {
        btnSearch.setOnClickListener {
            getCoordinates(searchInput.text.toString())
            hideKeyboard()
        }

        searchInput.setOnEditorActionListener { v, actionId, _ ->
            when {
                actionId == EditorInfo.IME_ACTION_GO
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_SEND -> {
                    getCoordinates(v.text.toString())
                    hideKeyboard()
                    return@setOnEditorActionListener true
                }
                else -> return@setOnEditorActionListener false
            }
        }
    }

    private fun setupLocation() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun updateViewBasedOnLocation() {
        val locationPermissionNotGranted = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED

        if (locationPermissionNotGranted) {
            requestLocationPermission()
        }

        val currLocation = fusedLocationProviderClient.getCurrentLocation(CurrentLocationRequest.Builder().setPriority(Priority
            .PRIORITY_HIGH_ACCURACY).setDurationMillis(TimeUnit.SECONDS.toMillis(30)).build(),null)

        currLocation.addOnSuccessListener {
            it?.run {
                geocodingService.getCityName(
                    latitude.toString(), longitude.toString(),
                    weatherApiKey
                ).enqueue(object : Callback<List<CityName>> {
                    override fun onResponse(call: Call<List<CityName>>, response: Response<List<CityName>>) {
                        response.body()?.get(0)?.run {
                            cityNameView.text = name
                        }
                    }

                    override fun onFailure(call: Call<List<CityName>>, t: Throwable) {
                        Log.e("api", t.stackTraceToString())
                    }

                })
                getCurrentWeather(latitude, longitude)
            }
        }

        currLocation.addOnFailureListener {
            Log.e("location", it.stackTraceToString())
            Toast.makeText(
                this@MainActivity,
                "Permissão de localização necessária! Permita e abra novamente o aplicativo",
                Toast.LENGTH_LONG
            ).show()
            requestLocationPermission()
        }
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            0
        )
    }

    private fun setupViews() {
        main = findViewById(R.id.main)
        searchInput = findViewById(R.id.search_city)
        btnSearch = findViewById(R.id.btn_search)
        cityNameView = findViewById(R.id.tv_city_name)
        placeholderIcon = findViewById(R.id.pb_icon)
        icon = findViewById(R.id.iv_weather_icon)
        weatherCondition = findViewById(R.id.tv_weather_condition)
        currentTemperature = findViewById(R.id.tv_current_weather)
        minTemperature = findViewById(R.id.tv_min_value)
        maxTemperature = findViewById(R.id.tv_max_value)
        humidity = findViewById(R.id.tv_humidity_value)
        rainView = findViewById(R.id.tv_rain_value)
        windView = findViewById(R.id.tv_wind_value)
    }

    private fun setupRetrofitServices() {
        geocodingService = RetrofitUtil.getInstance(GeocodingService.BASE_URL).create(GeocodingService::class.java)
        weatherService = RetrofitUtil.getInstance(WeatherService.BASE_URL).create(WeatherService::class.java)
    }

    private fun getCoordinates(cityName: String) {
        geocodingService.getCoordinates(cityName, weatherApiKey).enqueue(object : Callback<List<CityGeocode>> {
            override fun onResponse(call: Call<List<CityGeocode>>, response: Response<List<CityGeocode>>) {
                response.body()?.get(0)?.run {
                    cityNameView.text = name
                    getCurrentWeather(lat, lon)
                }
            }

            override fun onFailure(p0: Call<List<CityGeocode>>, p1: Throwable) {
                Log.e("api", p1.toString())
                Toast.makeText(this@MainActivity, "Erro ao pesquisar cidade", Toast.LENGTH_LONG).show()
            }
        })
    }

    fun getCurrentWeather(lat: Double, lon: Double) {
        weatherService.getCurrentWeather(Locale.getDefault().language, lat.toString(), lon.toString(), weatherApiKey)
        .enqueue(object : Callback<Weather> {
            @SuppressLint("SetTextI18n")
            override fun onResponse(call: Call<Weather>, response: Response<Weather>) {
                if (response.isSuccessful) {
                    response.body()?.run {
                        placeholderIcon.visibility = View.GONE
                        icon.load("https://openweathermap.org/img/wn/${weather[0].icon}@2x.png")
                        weatherCondition.text = weather[0].description.replaceFirst(weather[0].description[0],
                            weather[0].description[0].uppercaseChar())
                        currentTemperature.text = "${main.temp} ${getString(R.string.celsius)}"
                        minTemperature.text = "${main.tempMin} ${getString(R.string.celsius)}"
                        maxTemperature.text = "${main.tempMax} ${getString(R.string.celsius)}"
                        humidity.text = "${main.humidity} ${getString(R.string.percent)}"
                        rainView.text = "${rain?.precipitation ?: 0} ${getString(R.string.mm_h)}"
                        windView.text = "${wind.speed} ${getString(R.string.m_s)}"
                    }
                }
            }

            override fun onFailure(p0: Call<Weather>, p1: Throwable) {
                Log.d("api", p1.toString())
                Toast.makeText(
                    this@MainActivity,
                    "Erro ao buscar clima atual da cidade pesquisada",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }
}