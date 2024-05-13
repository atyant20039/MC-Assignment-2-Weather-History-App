package com.example.weather

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.Manifest
import android.widget.DatePicker
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat.requestLocationUpdates
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import coil.size.Size
import com.example.weather.api.ApiInterface
import com.example.weather.api.ApiUtilities
import com.example.weather.model.DateModel
import com.example.weather.repository.WeatherRepository
import com.example.weather.ui.theme.WeatherTheme
import com.example.weather.viewModel.WeatherViewModel
import com.example.weather.viewModel.WeatherViewModelFactory
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    private lateinit var weatherViewModel: WeatherViewModel
    private var latitude: String = "0";
    private var longitude: String = "0";

    private val LOCATION_PERMISSION_REQUEST_CODE = 100

    // Activity result launcher for requesting location permissions
    private val requestLocationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Location permission granted, proceed with location updates
            requestLocationUpdates()
        } else {
            // Location permission denied, handle accordingly (e.g., show a message or disable location-related functionality)
            Toast.makeText(
                this,
                "Location permission denied. Weather data based on device location won't be available.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun isLocationPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermissions() {
        requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationUpdates() {
        // Add the code for requesting location updates here
        // This could be the same code we discussed in the previous step
        val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        fusedLocationClient.lastLocation
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful && task.result != null) {
                    // Got last known location. If not null, update API call with these coordinates
                    val location: Location = task.result
                    latitude = location.latitude.toString()
                    longitude = location.longitude.toString()
                    Log.d("TAG", "Location updated by last location: $latitude $longitude")
                } else {
                    // Handle failure to get location
                    Log.e("TAG", "Error getting location: ${task.exception?.message}")
                    Toast.makeText(
                        this,
                        "Error getting location.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .addOnFailureListener { e ->
                // Handle failure to get location
                Log.e("TAG", "Error getting location: ${e.message}")
                Toast.makeText(
                    this,
                    "Error getting location.",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!isLocationPermissionGranted()) {
            requestLocationPermissions()
        } else {
            // Location permissions already granted, proceed with location updates
            requestLocationUpdates()
        }

        val repository = (application as MyApplication).weatherRepository

        weatherViewModel = ViewModelProvider(this, WeatherViewModelFactory(repository)).get(WeatherViewModel::class.java)

        setContent {
            WeatherTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var date: String by rememberSaveable {
                        mutableStateOf(
                            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(
                            Calendar.getInstance().time))
                    }

                    var minTempState:Double? by rememberSaveable {
                        mutableStateOf(null)
                    }
                    var maxTempState:Double? by rememberSaveable {
                        mutableStateOf(null)
                    }

                    var meanTempState: Double? by rememberSaveable {
                        mutableStateOf(null)
                    }
                    var weatherCodeState: Int? by rememberSaveable {
                        mutableStateOf(null)
                    }
                    var precipitationState: Double? by rememberSaveable {
                        mutableStateOf(null)
                    }
                    var windSpeedState: Double? by rememberSaveable {
                        mutableStateOf(null)
                    }
                    var windDirectionState: Int? by rememberSaveable {
                        mutableStateOf(null)
                    }

                    weatherViewModel.weather.observe(this, Observer {
                        minTempState = it!!.minTemp
                        maxTempState = it!!.maxTemp
                        meanTempState = it!!.meanTemp
                        weatherCodeState = it!!.weatherCode
                        precipitationState = it!!.precipitation
                        windSpeedState = it!!.windSpeed
                        windDirectionState = it!!.windDirection
                    })

                    val currentCalendar = Calendar.getInstance()
                    currentCalendar.time = Date()

                    val mContext = LocalContext.current

                    val mDatePickerDialog = DatePickerDialog(
                        mContext,
                        { _: DatePicker, mYear: Int, mMonth: Int, mDayOfMonth: Int ->
                            val formattedMonth = String.format("%02d", mMonth + 1) // Adding 1 because months start from 0
                            val formattedDay = String.format("%02d", mDayOfMonth)
                            date = "$mYear-$formattedMonth-$formattedDay"
                        }, currentCalendar.get(Calendar.YEAR), currentCalendar.get(Calendar.MONTH), currentCalendar.get(Calendar.DAY_OF_MONTH)
                    )

                    mDatePickerDialog.datePicker.maxDate = currentCalendar.timeInMillis

                    LaunchedEffect(Unit) {
                        weatherViewModel.getData(date, latitude, longitude)
                    }

                    LaunchedEffect(date) {
                        weatherViewModel.getData(date, latitude, longitude)
                    }

                    LaunchedEffect(latitude, longitude) {
                        Log.d("TAG", "onCreate: $latitude $longitude")
                        weatherViewModel.getData(date, latitude, longitude)
                    }

//                    LazyColumn {
//
//                    }
                    Column (
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {

                        Text(
                            text = "Weather",
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier
                                .padding(4.dp)
                        )

                        Button(onClick = {
                            mDatePickerDialog.show()
                        }) {
                            Text(
                                text = date,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Normal,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier
                                    .padding(4.dp)
                            )
                        }

                        HLine()

                        WeatherGraphics(weatherCode = weatherCodeState)

                        LazyColumn (
                            verticalArrangement = Arrangement.spacedBy(64.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            item {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                ) {
                                    TempCard(value = minTempState.toString(), key = "Min Temp", longitude, latitude)
                                    TempCard(value = maxTempState.toString(), key = "Max Temp", longitude, latitude)
                                }
                            }
                            item {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                ) {
                                    TempCard(value = meanTempState.toString(), key = "Mean Temp", longitude, latitude)
                                    TempCard(value = precipitationState.toString(), key = "Rain", longitude, latitude)
                                }
                            }
                            item {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                ) {
                                    TempCard(value = windSpeedState.toString(), key = "Wind Speed", longitude, latitude)
                                    TempCard(value = windDirectionState.toString(), key = "Direction", longitude, latitude)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HLine() {
    // Draws a horizontal line from 0 to max width available
    val canvasWidth = remember { mutableFloatStateOf(0f) }
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .onSizeChanged {
                canvasWidth.floatValue = it.width.toFloat()
            }
    ) {
        drawLine(
            start = Offset(x = 0f, y = 0f),
            end = Offset(x = canvasWidth.floatValue, y = 0f),
            color = Color.Black,
            strokeWidth = 5F
        )
    }
}

@Composable
fun WeatherGraphics(weatherCode: Int?) {
    val context = LocalContext.current
    val imageLoader = ImageLoader.Builder(context)
        .components {
            if (Build.VERSION.SDK_INT >= 28) {
                add(ImageDecoderDecoder.Factory())
            } else {
                add(GifDecoder.Factory())
            }
        }
        .build()
    Image(
        painter = rememberAsyncImagePainter(
            ImageRequest.Builder(context)
                .data(data =
                if (weatherCode != null) {
                    if (weatherCode < 40 && weatherCode >= 0){
                        R.drawable.sun
                    }
                    else if (weatherCode >= 40 && weatherCode <= 60){
                        R.drawable.clouds
                    }
                    else if (weatherCode > 60 && weatherCode < 70){
                        R.drawable.rain
                    }
                    else if (weatherCode >= 70){
                        R.drawable.snowflake
                    } else {
                        R.drawable.sun
                    }
                } else {
                    R.drawable.sun
                }
                )
                .apply(block = {
                    size(Size.ORIGINAL)
                })
                .build(), imageLoader = imageLoader
        ),
        contentDescription = "Weather graphics",
        modifier = Modifier.fillMaxSize(0.60f),
    )
}

@Composable
fun TempCard(value: String?, key: String, longitude:String, latitude:String) {
    val unit:String = if (key == "Min Temp" || key == "Max Temp" || key == "Mean Temp") "°C" else if (key == "Rain") "mm" else if (key == "Wind Speed") "km/h" else if (key == "Direction") "°" else ""
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(150.dp)
    ) {
        Text(
            text = if (value != null && value != "0.0" && longitude != "0" && latitude != "0") "$value $unit" else "-",
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.SansSerif,
            fontSize = 28.sp,
        )

        HLine()

        Text(
            text = key,
            fontWeight = FontWeight.Normal,
            fontFamily = FontFamily.Monospace,
            fontSize = 16.sp,
        )
    }
}