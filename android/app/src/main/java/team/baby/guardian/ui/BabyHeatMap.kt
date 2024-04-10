package team.baby.guardian.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import kotlin.math.min
import kotlin.math.roundToInt

@Serializable
data class AlertResponse(
    val id: Int,
    val device_serial: String,
    val alert: String,
    val type: String,
    val status: String,
    val datetime: String,
    val addition: String?,
    val deliveredTo: String?
)

@Composable
fun BabyHeatMap() {
    var alerts by remember { mutableStateOf<List<AlertResponse>>(emptyList()) }

    LaunchedEffect(Unit) {
        fetchAlertsPeriodically { newAlerts ->
            alerts = newAlerts
        }
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .height(300.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Baby Guardian Temperature 8x8 Matrix",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        AlertList(alerts)
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .height(300.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Baby Guardian Heatmap",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        AlertHeatMaps(alerts)
    }
}

@Composable
fun AlertList(alerts: List<AlertResponse>) {
    LazyColumn (
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(alerts.asReversed()) { alert ->
            AlertItem(alert)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun AlertItem(alert: AlertResponse) {
    Column {
        Text(
            text = "Date: ${alert.datetime}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary
        )
        Text(
            text = "Alert: ${alert.alert}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun AlertHeatMaps(alerts: List<AlertResponse>) {
    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(alerts.asReversed()) { alert ->
            Text(
                text = alert.datetime,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Column (
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HeatMap(alert)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun HeatMap(alert: AlertResponse) {
    val temperatureData = parseTemperatureData(alert)
    val maxValue = temperatureData.flatten().maxOrNull() ?: 0.0
    val minValue = temperatureData.flatten().minOrNull() ?: 0.0
    val colorGradient = heatMapColorGradient(maxValue, minValue)

    Canvas(modifier = Modifier
        .fillMaxWidth()
        .height(200.dp)) {
        val cellSize = min(size.width, size.height) / temperatureData.size

        temperatureData.forEachIndexed { rowIndex, row ->
            row.forEachIndexed { colIndex, value ->
                val x = colIndex * cellSize
                val y = rowIndex * cellSize

                val color = colorGradient(value)

                drawRect(color, topLeft = Offset(x, y), size = Size(cellSize, cellSize))
            }
        }
    }
}

private fun parseTemperatureData(alert: AlertResponse): List<List<Double>> {
    return alert.alert.split(" ").chunked(8) { chunk ->
        chunk.map { it.toDoubleOrNull() ?: 0.0 }
    }
}

private fun heatMapColorGradient(maxValue: Double, minValue: Double): (Double) -> Color {
    val colors = listOf(
        Color(255, 0, 0),  // Red
        Color(255, 255, 0),// Yellow
        Color(0, 255, 0)   // Green
    )

    return { value ->
        val percentage = (value - minValue) / (maxValue - minValue)
        val index = (percentage * (colors.size - 1)).roundToInt().coerceIn(0, colors.size - 1)
        colors[index]
    }
}

private suspend fun fetchAlertsPeriodically(callback: (List<AlertResponse>) -> Unit) {
    val client = OkHttpClient()

    while (true) {
        val requestBody = FormBody.Builder()
            .add("mode", "find")
            .add("device_serial", "1")
            .add("type", "TEMP_MATRIX")
            .add("status", "matrix_sent")
            .build()

        val request = Request.Builder()
            .url("https://weicheng.app/baby_guardian/alert.php")
            .post(requestBody)
            .build()

        try {
            val response = withContext(Dispatchers.IO) {
                client.newCall(request).execute()
            }

            val responseBody = response.body?.string()

            if (!responseBody.isNullOrEmpty()) {
                val json = Json { ignoreUnknownKeys = true }
                val alerts = json.decodeFromString<List<AlertResponse>>(responseBody)
                callback(alerts)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        delay(30 * 1000L) // Delay for 30 seconds
    }
}