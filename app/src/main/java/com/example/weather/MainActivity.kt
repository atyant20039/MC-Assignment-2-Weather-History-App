package com.example.weather

import android.app.DatePickerDialog
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.DatePicker
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    private lateinit var weatherViewModel: WeatherViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

                    weatherViewModel.weather.observe(this, Observer {
                        minTempState = it!!.minTemp
                        maxTempState = it!!.maxTemp
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

                    LaunchedEffect(Unit) {
                        weatherViewModel.getData(date)
                    }

                    LaunchedEffect(date) {
                        weatherViewModel.getData(date)
                    }

                    Column (
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {

                        Text(
                            text = "New Delhi",
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

                        WeatherGraphics(maxtemp = maxTempState)

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            TempCard(temp = minTempState, min = true)
                            TempCard(temp = maxTempState, min = false)
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
fun WeatherGraphics(maxtemp: Double?) {
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
                if (maxtemp != null && maxtemp < 20) {
                    R.drawable.snowflake
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
        modifier = Modifier.fillMaxSize(0.75f),
    )
}

@Composable
fun TempCard(temp: Double?, min: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(150.dp)
    ) {
        Text(
            text = if (temp != null && temp != 0.0) "$temp°C" else "-",
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.SansSerif,
            fontSize = 40.sp,
        )

        HLine()

        val str: String = if (min) "Min" else "Max"
        Text(
            text = str,
            fontWeight = FontWeight.Normal,
            fontFamily = FontFamily.Monospace,
            fontSize = 32.sp,
        )
    }
}