package team.baby.guardian.ui

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.provider.Settings.EXTRA_APP_PACKAGE
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.preferencesKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import team.baby.guardian.R
import java.util.Date
import java.util.Locale

@Composable
fun TempHumid(temp: String, humid: String) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(text = stringResource(R.string.temperature_c, temp), style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = stringResource(R.string.humidity, humid), style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun LineChart(tempHumidData: List<Pair<String, Pair<Float, Float>>>, device_serial: String) {
    val color_1 = MaterialTheme.colorScheme.primary
    val color_2 = MaterialTheme.colorScheme.error
    val color_3 = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)

    val axisColor = LocalContentColor.current.copy(alpha = 0.6f)
    val textColor = LocalContentColor.current.copy(alpha = 0.8f)
    val a = android.graphics.Paint()
    a.color = MaterialTheme.colorScheme.primary.hashCode()
    a.textSize = 30.5F

    val b = android.graphics.Paint()
    b.color = MaterialTheme.colorScheme.error.hashCode()
    b.textSize = 30.5F

    var showTemperatureChart by remember { mutableStateOf(true) }
    var showHumidityChart by remember { mutableStateOf(true) }

    var humidThreshold: Float? by remember { mutableStateOf(0.0F) }

    var temperatureText = stringResource(R.string.temperature_c_1)
    var humidityText = stringResource(R.string.humidity_1)

    LaunchedEffect(Unit) {
        val temperatureKey = preferencesKey<Boolean>("showTemperatureChart")
        val humidityKey = preferencesKey<Boolean>("showHumidityChart")
        humidThreshold = fetchHumidThreshold(device_serial)
        if (humidThreshold == null) {
            humidThreshold = 0.0F
        }
        Log.d("HUMID THRES:", humidThreshold.toString())
        showTemperatureChart = dataStore.data.first()[temperatureKey] ?: true
        showHumidityChart = dataStore.data.first()[humidityKey] ?: true
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
            .clip(shape = MaterialTheme.shapes.medium),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.humidity_threshold)+humidThreshold+"%", fontFamily = fontFamilyContent)
        Spacer(modifier = Modifier.height(10.dp))
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            val minTemperatureValue = tempHumidData.map { it.second.first - 0 }.minOrNull() ?: 0f
            val maxTemperatureValue = tempHumidData.map { it.second.first + 0 }.maxOrNull() ?: 100f

            // Draw Temperature Chart
            if (showTemperatureChart) {
                drawLine(
                    color = axisColor,
                    start = Offset(1f, size.height - 0.3f),
                    end = Offset(size.width, size.height),
                    strokeWidth = 2f
                )

                val legendMargin = 8.dp
                val legendSize = 20.dp

                drawRect(
                    color = color_1,
                    topLeft = Offset(size.width - legendSize.toPx() - legendMargin.toPx(), legendMargin.toPx()),
                    size = Size(legendSize.toPx(), legendSize.toPx()),
                    style = Fill
                )
                drawLine(
                    color = color_1,
                    start = Offset(size.width - legendSize.toPx() - legendMargin.toPx(), legendMargin.toPx() + legendSize.toPx() / 2),
                    end = Offset(size.width - legendMargin.toPx(), legendMargin.toPx() + legendSize.toPx() / 2),
                    strokeWidth = 2f
                )

                drawIntoCanvas {
                    if(temperatureText == "温度 (°C)"){
                        it.nativeCanvas.drawText(temperatureText, size.width - 5 * legendSize.toPx() - legendMargin.toPx(), legendMargin.toPx() + legendSize.toPx() * 0.7f, a)
                    }else{
                        it.nativeCanvas.drawText(temperatureText, size.width - 6 * legendSize.toPx() - legendMargin.toPx(), legendMargin.toPx() + legendSize.toPx() * 0.7f, a)
                    }
                }

                drawIntoCanvas {
                    val thirdQuartileLabelY = size.height * 0.25
                    val middleLabelY = size.height * 0.5
                    val firstQuartileLabelY = size.height * 0.75

                    it.nativeCanvas.drawText((minTemperatureValue + (maxTemperatureValue - minTemperatureValue) * 0.75).toString(), 5.dp.toPx(), thirdQuartileLabelY.toFloat(), a)
                    it.nativeCanvas.drawText(((minTemperatureValue + maxTemperatureValue) / 2).toString(), 5.dp.toPx(), middleLabelY.toFloat(), a)
                    it.nativeCanvas.drawText((minTemperatureValue + (maxTemperatureValue - minTemperatureValue) * 0.25).toString(), 5.dp.toPx(), firstQuartileLabelY.toFloat(), a)
                }

                val pathTemperature = Path()
                tempHumidData.forEachIndexed { index, (datetime, temperature) ->
                    val x = (size.width / (tempHumidData.size - 1)) * index
                    val yTemperature = size.height * (1 - (temperature.first - minTemperatureValue) / (maxTemperatureValue - minTemperatureValue))

                    if (index == 0) {
                        pathTemperature.moveTo(x, yTemperature)
                    } else {
                        val prevX = (size.width / (tempHumidData.size - 1)) * (index - 1)
                        val prevY = size.height * (1 - (tempHumidData[index - 1].second.first - minTemperatureValue) / (maxTemperatureValue - minTemperatureValue))
                        val controlPoint1 = Offset(prevX + (x - prevX) / 2, prevY)
                        val controlPoint2 = Offset(prevX + (x - prevX) / 2, yTemperature)

                        pathTemperature.cubicTo(controlPoint1.x, controlPoint1.y, controlPoint2.x, controlPoint2.y, x, yTemperature)
                    }
                }

                drawPath(pathTemperature, color = color_1, style = Stroke(4f))
            }
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            val minHumidityValue = tempHumidData.map { it.second.second - 0 }.minOrNull() ?: 0f
            val maxHumidityValue = tempHumidData.map { it.second.second + 0 }.maxOrNull() ?: 100f

            // Draw Humidity Chart
            if (showHumidityChart) {
                drawLine(
                    color = axisColor,
                    start = Offset(1f, size.height - 0.3f),
                    end = Offset(size.width, size.height),
                    strokeWidth = 2f
                )

                val legendMargin = 8.dp
                val legendSize = 20.dp

                drawRect(
                    color = color_2,
                    topLeft = Offset(size.width - legendSize.toPx() - legendMargin.toPx(), 1 * (legendMargin.toPx() + legendSize.toPx())),
                    size = Size(legendSize.toPx(), legendSize.toPx()),
                    style = Fill
                )
                drawLine(
                    color = color_2,
                    start = Offset(size.width - legendSize.toPx() - legendMargin.toPx(), 1 * (legendMargin.toPx() + legendSize.toPx()) + legendSize.toPx() / 2),
                    end = Offset(size.width - legendMargin.toPx(), 1 * (legendMargin.toPx() + legendSize.toPx()) + legendSize.toPx() / 2),
                    strokeWidth = 2f
                )

                drawIntoCanvas {
                    it.nativeCanvas.drawText(humidityText, size.width - 5 * legendSize.toPx() - legendMargin.toPx(), 1 * (legendMargin.toPx() + legendSize.toPx()) + legendSize.toPx() * 0.7f, b)
                }

                drawIntoCanvas {
                    val thirdQuartileLabelY = size.height * 0.25
                    val middleLabelY = size.height * 0.5
                    val firstQuartileLabelY = size.height * 0.75

                    it.nativeCanvas.drawText((minHumidityValue + (maxHumidityValue - minHumidityValue) * 0.75).toString(), 5.dp.toPx(), thirdQuartileLabelY.toFloat(), b)
                    it.nativeCanvas.drawText(((minHumidityValue + maxHumidityValue) / 2).toString(), 5.dp.toPx(), middleLabelY.toFloat(), b)
                    it.nativeCanvas.drawText((minHumidityValue + (maxHumidityValue - minHumidityValue) * 0.25).toString(), 5.dp.toPx(), firstQuartileLabelY.toFloat(), b)
                }

                val pathHumidity = Path()
                tempHumidData.forEachIndexed { index, (datetime, humidity) ->
                    val x = (size.width / (tempHumidData.size - 1)) * index
                    val yHumidity = size.height * (1 - (humidity.second - minHumidityValue) / (maxHumidityValue - minHumidityValue))

                    if (index == 0) {
                        pathHumidity.moveTo(x, yHumidity)
                    } else {
                        val prevX = (size.width / (tempHumidData.size - 1)) * (index - 1)
                        val prevY = size.height * (1 - (tempHumidData[index - 1].second.second - minHumidityValue) / (maxHumidityValue - minHumidityValue))
                        val controlPoint1 = Offset(prevX + (x - prevX) / 2, prevY)
                        val controlPoint2 = Offset(prevX + (x - prevX) / 2, yHumidity)

                        pathHumidity.cubicTo(controlPoint1.x, controlPoint1.y, controlPoint2.x, controlPoint2.y, x, yHumidity)
                    }
                }

                drawPath(pathHumidity, color = color_2, style = Stroke(4f))

                // Draw threshold line for Humidity
                val thresholdY = size.height * (1 - (humidThreshold!! - minHumidityValue) / (maxHumidityValue - minHumidityValue))
                drawLine(
                    color = color_3,
                    start = Offset(0f, thresholdY),
                    end = Offset(size.width, thresholdY),
                    strokeWidth = 2f,
                    pathEffect = PathEffect.cornerPathEffect(10f)
                )
            }
        }
    }
}

@Composable
fun LineChartAlt(tempHumidData: List<Pair<String, Pair<Float, Float>>>) {
    var temperatureText = stringResource(R.string.temperature_c_1)
    var humidityText = stringResource(R.string.humidity_1)

    val color_1 = MaterialTheme.colorScheme.primary
    val color_2 = MaterialTheme.colorScheme.error

    val axisColor = LocalContentColor.current.copy(alpha = 0.6f)
    val textColor = LocalContentColor.current.copy(alpha = 0.8f)
    val a = android.graphics.Paint()
    a.color = MaterialTheme.colorScheme.primary.hashCode()
    a.textSize = 30.5F

    val b = android.graphics.Paint()
    b.color = MaterialTheme.colorScheme.error.hashCode()
    b.textSize = 30.5F

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(shape = MaterialTheme.shapes.medium)
    ) {
        // Calculate Y-axis range
        val minValue = tempHumidData.map { it.second.first - 1 }.minOrNull() ?: 0f
        val maxValue = tempHumidData.map { it.second.second + 1 }.maxOrNull() ?: 100f

        // Draw X-axis
        drawLine(
            color = axisColor,
            start = Offset(1f, size.height - 0.3f),
            end = Offset(size.width, size.height),
            strokeWidth = 2f
        )

        // Draw Y-axis
        drawLine(
            color = axisColor,
            start = Offset(0f, 0f),
            end = Offset(0f, size.height),
            strokeWidth = 2f
        )

        // Draw Y-axis labels
        drawIntoCanvas {
            val thirdQuartileLabelY = size.height * 0.25 // 75% of the height
            val middleLabelY = size.height * 0.5 // Middle of the height
            val firstQuartileLabelY = size.height * 0.75 // 25% of the height

            it.nativeCanvas.drawText((minValue + (maxValue - minValue) * 0.75).toString(), 5.dp.toPx(), thirdQuartileLabelY.toFloat(), a)
            it.nativeCanvas.drawText(((minValue + maxValue) / 2).toString(), 5.dp.toPx(), middleLabelY.toFloat(), a)
            it.nativeCanvas.drawText((minValue + (maxValue - minValue) * 0.25).toString(), 5.dp.toPx(), firstQuartileLabelY.toFloat(), a)
        }

        // Draw legends and labels
        val legendMargin = 8.dp
        val legendSize = 20.dp

        // Legend for Temperature
        drawRect(
            color = color_1,
            topLeft = Offset(size.width - legendSize.toPx() - legendMargin.toPx(), legendMargin.toPx()),
            size = Size(legendSize.toPx(), legendSize.toPx()),
            style = Fill
        )
        drawLine(
            color = color_1,
            start = Offset(size.width - legendSize.toPx() - legendMargin.toPx(), legendMargin.toPx() + legendSize.toPx() / 2),
            end = Offset(size.width - legendMargin.toPx(), legendMargin.toPx() + legendSize.toPx() / 2),
            strokeWidth = 2f
        )

        // Legend for Humidity
        drawRect(
            color = color_2,
            topLeft = Offset(size.width - legendSize.toPx() - legendMargin.toPx(), 2 * (legendMargin.toPx() + legendSize.toPx())),
            size = Size(legendSize.toPx(), legendSize.toPx()),
            style = Fill
        )
        drawLine(
            color = color_2,
            start = Offset(size.width - legendSize.toPx() - legendMargin.toPx(), 2 * (legendMargin.toPx() + legendSize.toPx()) + legendSize.toPx() / 2),
            end = Offset(size.width - legendMargin.toPx(), 2 * (legendMargin.toPx() + legendSize.toPx()) + legendSize.toPx() / 2),
            strokeWidth = 2f
        )

        // Draw labels for legends
        drawIntoCanvas {
            if(temperatureText == "温度 (°C)"){
                it.nativeCanvas.drawText(temperatureText, size.width - 5 * legendSize.toPx() - legendMargin.toPx(), legendMargin.toPx() + legendSize.toPx() * 0.7f, a)
            }else{
                it.nativeCanvas.drawText(temperatureText, size.width - 6 * legendSize.toPx() - legendMargin.toPx(), legendMargin.toPx() + legendSize.toPx() * 0.7f, a)
            }
            it.nativeCanvas.drawText(humidityText, size.width - 5 * legendSize.toPx() - legendMargin.toPx(), 2 * (legendMargin.toPx() + legendSize.toPx()) + legendSize.toPx() * 0.7f, b)
        }

        // Draw data lines with curves
        val pathTemperature = Path()
        val pathHumidity = Path()

        tempHumidData.forEachIndexed { index, (datetime, b) ->
            val temperature = b.first
            val humidity = b.second

            val x = (size.width / (tempHumidData.size - 1)) * index
            val yTemperature = size.height * (1 - (temperature - minValue) / (maxValue - minValue))
            val yHumidity = size.height * (1 - (humidity - minValue) / (maxValue - minValue))

            // Draw Temperature curve
            if (index == 0) {
                pathTemperature.moveTo(x, yTemperature)
            } else {
                pathTemperature.lineTo(x, yTemperature)
            }

            // Draw Humidity curve
            if (index == 0) {
                pathHumidity.moveTo(x, yHumidity)
            } else {
                pathHumidity.lineTo(x, yHumidity)
            }
        }

        drawPath(pathTemperature, color = color_1, style = Stroke(4f))
        drawPath(pathHumidity, color = color_2, style = Stroke(4f))
    }
}

suspend fun fetchTempHumidData(deviceSerial: String): List<Pair<String, Pair<Float, Float>>>? {
    return withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient()
            val url = "https://weicheng.app/baby_guardian/temp_humid.php"

            val formBody = FormBody.Builder()
                .add("mode", "find")
                .add("device_serial", deviceSerial)
                .build()

            val request = Request.Builder()
                .url(url)
                .post(formBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""
            return@withContext parseTempHumidList(responseBody)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

private fun parseTempHumidList(response: String): List<Pair<String, Pair<Float, Float>>> {
    val tempHumidList = mutableListOf<Pair<String,Pair<Float, Float>>>()
//    Log.d("TEMP_HUMID", response)
    val jsonArray = Json.parseToJsonElement(response).jsonArray
    jsonArray.forEach { jsonElement ->
        val jsonObject = jsonElement.jsonObject
        val temperature = jsonObject["temp"]?.jsonPrimitive?.floatOrNull ?: 0f
        val humidity = jsonObject["humid"]?.jsonPrimitive?.floatOrNull ?: 0f
        val datetime = jsonObject["datetime"].toString()
        tempHumidList.add(Pair(datetime, Pair(temperature, humidity)))
    }
    return tempHumidList
}

@Composable
fun TempHumidDataFetcher(deviceSerial: String) {
    var tempHumidData by remember { mutableStateOf(emptyList<Pair<String, Pair<Float, Float>>>()) }
    var frequency by remember { mutableStateOf(4000) }
    var context = LocalContext.current
    var language: String? by remember {
        mutableStateOf("en")
    }
    LaunchedEffect(key1 = "update_freq") {
        withContext(Dispatchers.IO) {
            language = readSettings("language")
            if(language == null){
                language = supportedLanguages.first().code
            }
        }
    }

    // Read from SharedPreferences on Composable initialization
    LaunchedEffect(key1 = "update_freq") {
        withContext(Dispatchers.IO) {
            val sharedPreferences = context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
            frequency = sharedPreferences.getInt("update_freq", 4000)
        }
    }

    LaunchedEffect(deviceSerial) {
        while (true){
            val data = fetchTempHumidData(deviceSerial)
            if (data != null) {
                tempHumidData = data
            }
            try {
                delay(frequency.toLong())
            }catch (e: Exception){
                delay(5000)
            }
        }
    }

    var isSwitchOn by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Read from SharedPreferences on Composable initialization
    LaunchedEffect(key1 = "diagram_style") {
        withContext(Dispatchers.IO) {
            val sharedPreferences = context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
            isSwitchOn = sharedPreferences.getBoolean("diagram_style", false)
        }
    }

    if(isSwitchOn){
        LineChartAlt(tempHumidData)
    }else{
        LineChart(tempHumidData, deviceSerial)
    }

    // Display the data in a scrollable table
    val state = rememberScrollState()
    StyledTemperatureHumidityData(tempHumidData, state)
    Spacer(modifier = Modifier.height(5.dp))
    if(language == "zh"){
        GeminiUI("full", "given that data set: (date, temperature_data, humidity_data), give me some advice in SIMPLIFIED CHINESE on caring for baby and human beings, and also when to open the humidifier, this can be a value percentage: "+tempHumidData.toString(), "", true)
    }else{
        GeminiUI("full", "given that data set: (date, temperature_data, humidity_data), give me some advice on caring for baby and human beings, and also when to open the humidifier, this can be a value percentage: "+tempHumidData.toString(), "", true)
    }

//    Column(
//        modifier = Modifier
//            .background(MaterialTheme.colorScheme.secondaryContainer)
//            .padding(16.dp)
//            .height(300.dp)
//            .verticalScroll(state)
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxWidth(),
////                .verticalScroll(state)
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            for (index in tempHumidData.indices.reversed()) {
//                val data = tempHumidData[index]
//                val datetime = data.first
//                val temperature = data.second.first
//                val humidity = data.second.second
//                Text("At $datetime:")
//                Text("Temperature: $temperature °C")
//                Text("Humidity: $humidity %")
//            }
//        }
//    }
}

@Composable
fun StyledTemperatureHumidityData(tempHumidData: List<Pair<String, Pair<Float, Float>>>, state: ScrollState) {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(16.dp)
            .height(300.dp)
            .verticalScroll(state)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            for (data in tempHumidData.reversed()) {
                val datetime = data.first
                val temperature = data.second.first
                val humidity = data.second.second
                val isHighAlert = false

                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(
                            if (isHighAlert) Color.Red.copy(alpha = 0.1f) else MaterialTheme.colorScheme.tertiary.copy(
                                alpha = 0.3f
                            )
                        )
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
//                        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
//                        val date = dateFormat.parse(dateTimeString)
//                        val timestamp = date?.time ?: 0L
                        val datetime_processed = datetime.replace("\"", "")
//                        val formattedDateTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(datetime_processed))
                        Text(
                            text = datetime_processed,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            SensorReading(
                                icon = Icons.Default.Thermostat,
                                reading = "$temperature °C",
                                alertColor = if (isHighAlert) Color.Red else MaterialTheme.colorScheme.primary
                            )
                            SensorReading(
                                icon = Icons.Default.WaterDrop,
                                reading = "$humidity %",
                                alertColor = if (isHighAlert) Color.Red else MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SensorReading(icon: ImageVector, reading: String, alertColor: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = alertColor,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = reading,
            style = MaterialTheme.typography.headlineSmall.copy(
                color = alertColor,
                fontWeight = FontWeight.Bold
            )
        )
    }
}


@Composable
fun NotificationPermissionCheck() {
    val context = LocalContext.current
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    val hasNotificationPermission = checkNotificationPermission(notificationManager)

//    if (hasNotificationPermission) {
    RequestNotificationPermissionButton()
//    }
}

@Composable
fun RequestNotificationPermissionButton() {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { }
    Spacer(modifier = Modifier.height(10.dp))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
//            Text(
//                "Notification Permission Required",
//                style = MaterialTheme.typography.headlineSmall,
//                modifier = Modifier.padding(bottom = 8.dp)
//            )

            ClickableText(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary, textDecoration = TextDecoration.Underline)) {
                        append(stringResource(R.string.grant_notification_permission))
                    }
                },
                onClick = {
                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                        .putExtra(EXTRA_APP_PACKAGE, context.packageName)
                    launcher.launch(intent)
                }
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Icon(
            Icons.Default.Settings,
            contentDescription = "Settings",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(24.dp)
                .clickable {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        .setData(Uri.parse("package:" + context.packageName))
                    launcher.launch(intent)
                }
        )
    }
}

fun checkNotificationPermission(notificationManager: NotificationManager): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channelList = notificationManager.notificationChannels
        for (channel in channelList) {
            if (channel.importance == NotificationManager.IMPORTANCE_UNSPECIFIED) {
                return false
            }
        }
    }
    return true
}
