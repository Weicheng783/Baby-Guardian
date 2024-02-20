package team.baby.guardian.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.withFrameNanos
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

suspend fun fetchHumidThreshold(deviceSerial: String): Float? {
    return withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient()
            val url = "https://weicheng.app/baby_guardian/alert.php"

            val formBody = FormBody.Builder()
                .add("mode", "humidifier_thres")
                .add("device_serial", deviceSerial)
                .add("type", "Humidifier Intensity")
                .add("status", "u2d_received")
                .build()

            val request = Request.Builder()
                .url(url)
                .post(formBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""
            return@withContext parseHumidThreshold(responseBody)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

private fun parseHumidThreshold(response: String): Float {
    try {
        val json = Json { ignoreUnknownKeys = true }
        val jsonElement = json.parseToJsonElement(response)

        if (jsonElement is JsonObject) {
            val alertElement = jsonElement.jsonObject["alert"]

            if (alertElement is JsonPrimitive) {
                // Case where "alert" is a primitive (e.g., a number)
                return alertElement.contentOrNull?.toFloatOrNull() ?: 0.0F
            } else if (alertElement is JsonObject) {
                // Case where "alert" is an object with a property containing the value
                val humidityElement = alertElement.jsonObject["humidity"]
                return humidityElement?.jsonPrimitive?.contentOrNull?.toFloatOrNull() ?: 0.0F
            } else {
                // Handle the case where "alert" is neither a primitive nor an object
                return 0.0F
//                throw IllegalArgumentException("Unexpected format for 'alert' property")
            }
        } else {
            // Handle the case where the response is not a JSON object
            return 0.0F
//            throw IllegalArgumentException("Unexpected response format")
        }
    } catch (e: Exception) {
        e.printStackTrace()
        return 0.0F // or handle the error as appropriate for your use case
    }
}