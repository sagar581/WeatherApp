package com.example.weatherapp

import android.Manifest
import android.content.ContentValues.TAG
import android.nfc.Tag
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import com.example.library.SecondActivity
import com.example.library.Utils
import com.example.weatherapp.databinding.ActivityMainBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

//0d6456c97f16f046c5508713dd4cd035


class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val utils = Utils()

        utils.showIt()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 0
            )
        }
        initFCM()
        fetchWeatherData("Gujarat")
        SearchCity()

    }

    private fun initFCM() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("Main Activity", "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            // Log and toast
            Log.d("Main Activity", "token:$token")
            Toast.makeText(this, "token:$token", Toast.LENGTH_SHORT).show()
        })
    }


    private fun SearchCity(){
        val searchView = binding.searchView
        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener,
            android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    fetchWeatherData(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }

        })
    }

    private fun fetchWeatherData(cityName: String) {

        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .build()
            .create(ApiInterface::class.java)

        val response = retrofit.getWeatherData(cityName, "0d6456c97f16f046c5508713dd4cd035", "metric")
        response.enqueue(object : Callback<WeatherApp> {
            override fun onResponse(call: Call<WeatherApp>, response: Response<WeatherApp>) {

                val responseBody = response.body()!!
                val temperature = responseBody.main.temp.toString()
                val humidity = responseBody.main.humidity
                val windSpeed = responseBody.wind.speed
                val sunrise = responseBody.sys.sunrise.toLong()
                val sunset = responseBody.sys.sunset.toLong()
                val seaLevel = responseBody.main.pressure
                val condition = responseBody.weather.firstOrNull()?.main?: "unknown"
                val maxTemp = responseBody.main.temp_max
                val minTemp = responseBody.main.temp_min
                binding.temp.text = "$temperature" + " C"
                binding.weather.text = condition
                binding.maxTemp.text = "Max Temp: +$maxTemp C"
                binding.minTemp.text = "Min Temp: +$minTemp C"
                binding.humidity.text = "$humidity"
                binding.windSpeed.text = "$windSpeed"
                binding.sunrise.text = "${Time(sunrise)}"
                binding.sunset.text = "${Time(sunset)}"
                binding.sea.text = "$seaLevel hPA"
                binding.condition.text = "$condition"
                val (date, day) = getCurrentDateAndDay()
                binding.day.text ="$day"
                    binding.date.text ="$date"
                    binding.cityName.text = "$cityName"

                changeWeather(condition)


                Log.d("TAG", "onResponse: $temperature")
            }

            override fun onFailure(call: Call<WeatherApp>, t: Throwable) {

            }

        })

    }

    private fun changeWeather(condition:String) {
        when(condition){
            "Clouds", "Partly Clouds", "Overcast", "Mist", "Foggy" ->{
                binding.root.setBackgroundResource(R.drawable.colud_background)
                binding.LottieAnimationView.setAnimation(R.raw.cloud)
            }
            "Clear Sky", "Clear", "Sunny"->{
                binding.root.setBackgroundResource(R.drawable.sunny_background)
                binding.LottieAnimationView.setAnimation(R.raw.sun)
            }
            "Light Rain", "Moderate Rain", "Heavy Rain", "Drizzle", "Showers" ->{
                binding.root.setBackgroundResource(R.drawable.rain_background)
                binding.LottieAnimationView.setAnimation(R.raw.rain)
            }
            "Light Snow", "Moderate Snow", "Heavy Snow", "Blizzard" ->{
                binding.root.setBackgroundResource(R.drawable.snow_background)
                binding.LottieAnimationView.setAnimation(R.raw.snow)
            }
            else -> {
                binding.root.setBackgroundResource(R.drawable.sunny_background)
                binding.LottieAnimationView.setAnimation(R.raw.sun)
            }
        }
        binding.LottieAnimationView.playAnimation()
    }

    fun getCurrentDateAndDay(): Pair<String, String> {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val dayFormat = SimpleDateFormat("EEEE", Locale.getDefault())

        val date = dateFormat.format(calendar.time)
        val day = dayFormat.format(calendar.time)

        return Pair(date, day)
    }

    fun Time(timestamp:Long): String{
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp*1000))
    }


}


