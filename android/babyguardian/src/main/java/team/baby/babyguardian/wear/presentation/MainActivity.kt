/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package team.baby.babyguardian.wear.presentation

import android.annotation.SuppressLint
import android.content.Context
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.icu.util.TimeZone
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.format.DateFormat
import android.text.format.DateUtils
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.ChipColors
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.media3.common.MimeTypes
import androidx.media3.session.MediaSession
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.Colors
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.ListHeader
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.TimeTextDefaults
import androidx.wear.compose.material.TitleCard
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import coil.compose.rememberAsyncImagePainter
import coil.imageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import team.baby.babyguardian.wear.R
import team.baby.babyguardian.wear.presentation.theme.BabyGuardianTheme
import java.util.Locale
import com.google.android.horologist.compose.layout.AppScaffold
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.google.android.horologist.compose.layout.rememberColumnState
import com.google.android.horologist.compose.material.Button
import com.google.android.horologist.compose.rotaryinput.rotaryWithScroll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
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
import org.json.JSONArray
import org.json.JSONException
import team.baby.babyguardian.wear.presentation.theme.primaryDark
import team.baby.babyguardian.wear.presentation.theme.primaryLight
import team.baby.babyguardian.wear.presentation.theme.secondaryDark
//import team.baby.guardian.ui.AudioComponent
//import team.baby.guardian.ui.GeminiUI
//import team.baby.guardian.ui.IconWithText
//import team.baby.guardian.ui.ImageComponent
//import team.baby.guardian.ui.NotificationItem
//import team.baby.guardian.ui.VideoComponent
//import team.baby.guardian.ui.dismissNotification
//import team.baby.guardian.ui.proactiveFetch
//import team.baby.guardian.ui.proactiveFetch_notified
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.security.SecureRandom
import java.time.LocalTime

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)

        setContent {
            WearApp("")
        }
    }
}

@SuppressLint("SuspiciousIndentation")
@OptIn(ExperimentalHorologistApi::class)
@Composable
fun WearApp(greetingName: String) {
    val navController = rememberSwipeDismissableNavController()
    BabyGuardianTheme {
        AppScaffold {
            SwipeDismissableNavHost(navController = navController, startDestination = "menu") {
                composable("menu") {
                    GreetingScreen("Weicheng", onShowList = { navController.navigate("list") }, onShowData = { navController.navigate("sensor_data") })
                }
                composable("list") {
                    ListScreen()
                }
                composable("sensor_data") {
                    DataScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun DataScreen() {
    val columnState = rememberColumnState()
    val customButtonColor = primaryDark // Replace with your desired color
    val customButtonColorA = secondaryDark
    val deviceSerial = "1"
    val ownerStatus = "owner"
    val context = LocalContext.current
    var frequency = 4000
    var humidThreshold: Float? by remember { mutableStateOf(0.0F) }

    LaunchedEffect(deviceSerial) {
        while (true) {
            // Perform the proactive fetch in a background thread
            humidThreshold = fetchHumidThreshold(deviceSerial)
            if (humidThreshold == null) {
                humidThreshold = 0.0F
            }
            try {
                delay(frequency.toLong())
            }catch (e: Exception){
                delay(4000)
            } // Fetch notifications according to a user set value
        }
    }

    ScreenScaffold(scrollState = columnState) {
        humidThreshold?.let {
            CircularProgressIndicator(
                progress = it,
                modifier = Modifier.fillMaxSize(),
                startAngle = 290f,
                endAngle = 250f,
                strokeWidth = 4.dp
            )
        }
        ScalingLazyColumn(
            columnState = columnState,
            modifier = Modifier
                .fillMaxSize()
        ) {
            item{
//                Greeting(greetingName = "Weicheng")
//                Spacer(Modifier.height(10.dp))
                ListHeader {
                    Text("Humidity Threshold: "+humidThreshold.toString()+ " %", color = customButtonColor, textAlign = TextAlign.Center)
                }
                Spacer(Modifier.height(5.dp))
//                com.google.android.horologist.compose.material.Chip(modifier = Modifier.fillMaxWidth(0.8f),label = "Live Data", onClick = onShowData)
            }
            item{
                Spacer(Modifier.height(10.dp))
                Text("The Baby Guardian Uno", color = customButtonColorA, textAlign = TextAlign.Center)
            }
            item{
                Spacer(Modifier.height(10.dp))
                TempHumidDataFetcher(deviceSerial)
            }
        }
    }
}

fun getGreeting(): Int {
    return when (LocalTime.now()) {
        in LocalTime.MIDNIGHT..LocalTime.of(5, 59) -> R.string.good_night
        in LocalTime.of(6, 0)..LocalTime.of(8, 59) -> R.string.good_morning_early
        in LocalTime.of(9, 0)..LocalTime.of(11, 59) -> R.string.good_morning_late
        in LocalTime.of(12, 0)..LocalTime.of(17, 59) -> R.string.good_afternoon
        in LocalTime.of(18, 0)..LocalTime.of(23, 59) -> R.string.good_evening
        else -> R.string.good_morning_early // Strange Corner Case?
    }
}

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun GreetingScreen(greetingName: String, onShowList: () -> Unit, onShowData: () -> Unit) {
    val scrollState = ScrollState(0)
    val columnState = rememberColumnState()

    // Wear design guidelines specify a 5.2% horizontal padding on each side of the list.
    val horizontalPadding = (0.052f * LocalConfiguration.current.screenWidthDp).dp

    /* If you have enough items in your list, use [ScalingLazyColumn] which is an optimized
     * version of LazyColumn for wear devices with some added features. For more information,
     * see d.android.com/wear/compose.
     */
    ScreenScaffold(scrollState = scrollState) {
        ScalingLazyColumn(
            columnState = columnState,
            modifier = Modifier
                .fillMaxSize()
        ) {
            item {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colors.secondary,
                    text = stringResource(id = getGreeting()),
                    fontFamily = fontFamilyTitle
                )
//                Text(
//                    text = stringResource(id = getGreeting()),
//                    fontWeight = FontWeight.Bold,
//                    style = androidx.compose.material3.MaterialTheme.typography.headlineLarge
//                )
            }
            item {
                Greeting(greetingName = greetingName)
            }
            item {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colors.secondary,
                    text = "Welcome to Baby Guardian",
                    fontFamily = fontFamilyTitle
                )
            }
            item{
                Spacer(Modifier.height(10.dp))
                Text("240311.beta", textAlign = TextAlign.Center)
            }
            item {
                Spacer(Modifier.height(10.dp))
            }
            item {
                checkDeviceStatus("1")
            }
            item {
                com.google.android.horologist.compose.material.Chip(
                    modifier = Modifier.fillMaxWidth(
                        0.8f
                    ), label = "Notifications", onClick = onShowList
                )
            }
            item {
                Spacer(Modifier.height(5.dp))
            }
            item {
                com.google.android.horologist.compose.material.Chip(
                    modifier = Modifier.fillMaxWidth(
                        0.8f
                    ), label = "Live Data", onClick = onShowData
                )
            }
        }
    }
}

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun ListScreen() {
    val columnState = rememberColumnState()
    val customButtonColor = primaryDark // Replace with your desired color
    val deviceSerial = "1"
    val ownerStatus = "owner"
    val context = LocalContext.current
    var notifications by remember { mutableStateOf<List<NotificationItem>>(emptyList()) }
    var frequency = 4000

    LaunchedEffect(deviceSerial) {
        while (true) {
            // Perform the proactive fetch in a background thread
            proactiveFetch_notified(deviceSerial) { fetchedNotifications ->
                notifications = fetchedNotifications.reversed().map {
                    NotificationItem(it.id, it.alert, it.type, it.datetime, it.addition)
                }
            }
            try {
                delay(frequency.toLong())
            }catch (e: Exception){
                delay(4000)
            } // Fetch notifications according to a user set value
        }
    }

    ScreenScaffold(scrollState = columnState) {
        /*
         * The Horologist [ScalingLazyColumn] takes care of the horizontal and vertical
         * padding for the list, so there is no need to specify it, as in the [GreetingScreen]
         * composable.
         */
        ScalingLazyColumn(
            columnState = columnState,
            modifier = Modifier
                .fillMaxSize()
        ) {
            item {
                ListHeader {
                    Text("Baby Guardian Central", color = customButtonColor)
                }
            }
            item {
                TitleCard(title = { Text("Baby Crying") }, onClick = { }) {
                    Text("Your baby is crying.\nThe reason might be: discomfort.\nPlease Check.")
                    Button(
                        imageVector = Icons.Default.Build,
                        contentDescription = "Example Button",
                        onClick = { }
                    )
                }
            }
            items(notifications.size){notification ->
                NotificationCard(notifications[notification], ownerStatus) {
                    // Handle dismiss button click
//                    dismissNotification(deviceSerial, notifications[notification].id) {
//                        // Update the UI after dismissal if needed
//                        // Reload notifications or update the UI state
//                        proactiveFetch(deviceSerial, context, ownerStatus) {
//
//                        }
                        proactiveFetch_notified(deviceSerial) { fetchedNotifications ->
                            notifications = fetchedNotifications.reversed().map {
                                NotificationItem(it.id, it.alert, it.type, it.datetime, it.addition)
                            }
                        }
//                    }
                }

                Spacer(modifier = Modifier.height(5.dp))

//                if(notifications[notification].addition != ""){
//                    GeminiUI("image", "please give me instructions in English that given that alert type "+notifications[notification].type+" and its actual content "+notifications[notification].alert+" together with that image, can you analyse the situation and give parents some advice given in the context of baby caring?", notifications[notification].addition, true)
//                }else{
//                    GeminiUI("full", "please give me instructions in English that given that alert type "+notifications[notification].type+" and its actual content "+notifications[notification].alert+" together with the text, can you analyse the situation and give parents some advice given in the context of baby caring?", notifications[notification].addition, true)
//                }

//                Spacer(modifier = Modifier.height(10.dp))
            }
            item {
                com.google.android.horologist.compose.material.Chip(label = "Example Chip", onClick = { })
            }
            item {
                Button(
                    imageVector = Icons.Default.Build,
                    contentDescription = "Example Button",
                    onClick = { }
                )
            }
        }
    }
}

val fontFamily = FontFamily(Font(R.font.lobster_two))
val fontFamilyTitle = FontFamily(Font(R.font.mplus_rounded1c))
val fontFamilyContent = FontFamily(Font(R.font.ubuntu))
val fontFamilyNotes = FontFamily(Font(R.font.caveat))

@Composable
fun Greeting(greetingName: String) {
    Text(
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colors.primary,
        text = stringResource(R.string.hello_world, greetingName)
    )
}

fun proactiveFetch_notified(deviceSerial: String, resultCallback: (List<NotificationItem>) -> Unit) {
    val url = "https://weicheng.app/baby_guardian/alert.php"
    val client = OkHttpClient()

    val formBody = FormBody.Builder()
        .add("mode", "find")
        .add("device_serial", deviceSerial)
        .add("status", "d2u_notified")
        .build()

    val request = Request.Builder()
        .url(url)
        .post(formBody)
        .build()

    val handler = Handler(Looper.getMainLooper())

    // Use a background thread for network operations
    GlobalScope.launch(Dispatchers.IO) {
        try {
            val response = client.newCall(request).execute()

            // Parse the response on the main thread
            handler.post {
                val responseBody = response.body?.string() ?: ""

                try {
                    val jsonArray = JSONArray(responseBody)

                    // Form notifications
                    val notifications = mutableListOf<NotificationItem>()

                    for (i in 0 until jsonArray.length()) {
                        val jsonObject = jsonArray.getJSONObject(i)
                        val id = jsonObject.optString("id")
                        val alert = jsonObject.optString("alert")
                        val type = jsonObject.optString("type")
                        val datetime = jsonObject.optString("datetime")
                        val addition = jsonObject.optString("addition")
                        // Create NotificationItem and add to the list
                        notifications.add(NotificationItem(id, alert, type, datetime, addition))
                    }

                    // Callback with formed notifications
                    resultCallback(notifications)
                } catch (e: JSONException) {
                    // Handle JSON parsing error
                    resultCallback(emptyList())
                }
            }
        } catch (e: IOException) {
            // Handle network errors
            handler.post {
                resultCallback(emptyList())
            }
        }
    }
}

data class NotificationItem(
    val id: String,
    val alert: String,
    val type: String,
    val datetime: String,
    val addition: String
)

@Composable
fun NotificationCard(
    notification: NotificationItem,
    ownerStatus: String,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
//            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
//                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                androidx.compose.material3.Text(
                    text = notification.type,
                    style = androidx.compose.material3.MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.primary
                )

                if(ownerStatus == "owner"){
                    IconButton(onClick = onDismiss) {
                        androidx.compose.material3.Icon(
                            Icons.Default.Delete,
                            contentDescription = "Dismiss"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            androidx.compose.material3.Text(
                text = notification.datetime,
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            androidx.compose.material3.Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(stringResource(R.string.alert))
                    }
                    append(notification.alert)
                },
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Show addition directly
            androidx.compose.material3.Text(
                text = notification.addition,
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
            )

            when {
                notification.addition.startsWith("https://") && notification.addition.endsWith(".mp4") -> {
                    // Show video component
                    VideoComponent(url = notification.addition)
                }
                notification.addition.startsWith("https://") && notification.addition.endsWith(".jpg") -> {
                    // Show image component
                    ImageComponent(imageUrl = notification.addition)
                }
                notification.addition.startsWith("https://") && (notification.addition.endsWith(".mp3") || notification.addition.endsWith(".wav") || notification.addition.endsWith(".flac")) -> {
                    // Show audio component
                    AudioComponent(url = notification.addition)
                }
            }
        }
    }
}

@Composable
fun AudioComponent(url: String) {
    val context = LocalContext.current
    val sessionMap = remember { mutableStateMapOf<Int, MediaSession>() }
    val secureRandom = SecureRandom.getInstanceStrong()

//    val mediaList = listOf(
////        MediaInfo(
////            stringResource(R.string.audio_playback),
////            url,
////            MimeTypes.AUDIO_MP4,
////            url
////        ),
//    )
    Spacer(modifier = Modifier.height(10.dp))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = {
//                    openImageInBrowser(url, context)
                }
            )
    ) {
        Column (
            horizontalAlignment = Alignment.CenterHorizontally
        ){
//            MediaCard(mediaList[0], context, secureRandom.nextInt(), sessionMap, true)
//            Spacer(modifier = Modifier.height(6.dp))
//            androidx.compose.material3.Button(onClick = { openImageInBrowser(url, context) }) {
//                Icon(
//                    Icons.Default.OpenInBrowser, ""
//                )
//                androidx.compose.material3.Text(stringResource(R.string.open_in_browser))
//            }
        }
    }
}

data class MediaInfo(val title: String, val url: String, val mimeType: String, val description: String)

@Composable
fun VideoComponent(url: String) {
    val context = LocalContext.current
    val sessionMap = remember { mutableStateMapOf<Int, MediaSession>() }
    val secureRandom = SecureRandom.getInstanceStrong()

    val mediaList = listOf(
        MediaInfo(
            "Video Playback",
            url,
            MimeTypes.VIDEO_MP4,
            url
        ),
    )
    Spacer(modifier = Modifier.height(10.dp))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = {
//                    openImageInBrowser(url, context)
                }
            )
    ) {
        Column (
            horizontalAlignment = Alignment.CenterHorizontally
        ){
//            MediaCard(mediaList[0], context, secureRandom.nextInt(), sessionMap, true)
//            androidx.compose.material3.Button(onClick = { openImageInBrowser(url, context) }) {
//                Icon(
//                    Icons.Default.OpenInBrowser, ""
//                )
//                androidx.compose.material3.Text("Open in Browser")
//            }
        }
    }
}

@Composable
fun ImageComponent(imageUrl: String) {
    Log.d("checkURL", imageUrl)

    var scaleState by remember { mutableStateOf(1f) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
//            .wrapContentWidth()
            .clickable(
                onClick = {
//                    openImageInBrowser(imageUrl, context)
                    // Handle click action, e.g., navigate to a detail screen
                }
            )
    ) {
        val imageLoader = LocalContext.current.imageLoader
        val imagePainter = rememberAsyncImagePainter(
            ImageRequest.Builder(LocalContext.current).data(data = imageUrl).apply(block = fun ImageRequest.Builder.() {
                crossfade(true)
                memoryCachePolicy(CachePolicy.DISABLED)
                diskCachePolicy(CachePolicy.WRITE_ONLY)
            }).build(), imageLoader = imageLoader
        )

        Image(
            painter = imagePainter,
            contentDescription = null, // Set a meaningful description if needed
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.6f / scaleState)
//                .clip(MaterialTheme.shapes.medium.copy(CornerRadius.Zero))
//                .wrapContentSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, _, _, pan ->
                        // Handle pan gestures if needed
                    }
                }
        )
    }
}

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
//            language = readSettings("language")
//            if(language == null){
//                language = supportedLanguages.first().code
//            }
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
//    LineChart(tempHumidData, deviceSerial)
//    Spacer(modifier = Modifier.height(5.dp))
    // Display the data in a scrollable table
    val state = rememberScrollState()
    StyledTemperatureHumidityData(tempHumidData, state)
    Spacer(modifier = Modifier.height(5.dp))
//    if(language == "zh"){
//        GeminiUI("full", "given that data set: (date, temperature_data, humidity_data), give me some advice in SIMPLIFIED CHINESE on caring for baby and human beings, and also when to open the humidifier, this can be a value percentage: "+tempHumidData.toString(), "", true)
//    }else{
//        GeminiUI("full", "given that data set: (date, temperature_data, humidity_data), give me some advice on caring for baby and human beings, and also when to open the humidifier, this can be a value percentage: "+tempHumidData.toString(), "", true)
//    }
}

@Composable
fun StyledTemperatureHumidityData(tempHumidData: List<Pair<String, Pair<Float, Float>>>, state: ScrollState) {
//    Column(
//        modifier = Modifier
//            .background(androidx.compose.material3.MaterialTheme.colorScheme.secondaryContainer)
//            .clip(androidx.compose.material3.MaterialTheme.shapes.medium)
////            .padding(16.dp)
//            .height(300.dp)
//            .verticalScroll(state)
//    ) {
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
//                        .padding(8.dp)
                        .clip(androidx.compose.material3.MaterialTheme.shapes.medium)
                        .background(androidx.compose.material3.MaterialTheme.colorScheme.secondaryContainer)
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(),
//                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
//                        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
//                        val date = dateFormat.parse(dateTimeString)
//                        val timestamp = date?.time ?: 0L
                        val datetime_processed = datetime.replace("\"", "")
//                        val formattedDateTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(datetime_processed))
                        androidx.compose.material3.Text(
                            text = datetime_processed,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            SensorReading(
                                icon = Icons.Default.Star,
                                reading = "$temperature °C",
                                alertColor = if (isHighAlert) Color.Red else androidx.compose.material3.MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            SensorReading(
                                icon = Icons.Default.Favorite,
                                reading = "$humidity %",
                                alertColor = if (isHighAlert) Color.Red else androidx.compose.material3.MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
            }
        }
//    }
}

@Composable
fun checkDeviceStatus(serialNumber: String) {
    val imageUrl by rememberUpdatedState("https://weicheng.app/baby_guardian/photos/$serialNumber/latest.jpg")
    var modifiedDate by remember { mutableStateOf("") }
    var deviceStatus by remember { mutableStateOf(DeviceStatus.OFFLINE) }

    fetchImageModifiedDateAsync(imageUrl) { result ->
        if (result != null) {
            modifiedDate = result
        }
    }

    var showDialog by remember { mutableStateOf(false) }
//    if (showDialog){
//        AlertDialogExample(
//            onDismissRequest = { showDialog = false },
//            onConfirmation = { showDialog = false },
//            dialogTitle = stringResource(R.string.device_offline),
//            dialogText = stringResource(R.string.your_device_is_currently_offline_power_on_the_device_for_connecting_to_a_nearby_wifi_access_point_if_the_issue_persists_reconfigure_the_wifi_by_showing_the_qr_code_to_the_target_device_s_camera_your_device_is_capable_for_auto_connecting_the_correct_wifi_once_the_correct_qr_is_being_detected),
//            icon = Icons.Default.Info
//        )
//    }

    deviceStatus = calculateDeviceStatus(modifiedDate)

    Column(
        modifier = Modifier
            .fillMaxSize(),
//        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Display the image and modified date
//        Image(
//            painter = rememberImagePainter(imageUrl),
//            contentDescription = null,
//            modifier = Modifier
//                .size(200.dp)
//                .clip(shape = RoundedCornerShape(8.dp))
//        )

//        Spacer(modifier = Modifier.height(5.dp))

//        androidx.compose.material3.Text(
//            textAlign = TextAlign.Center,
//            fontSize = 13.sp,
//            text = stringResource(R.string.last_seen, modifiedDate),
////            fontFamily = fontFamilyTitle,
//            fontWeight = FontWeight.Bold
//        )
        val timeDifference = calculateTimeDifference_cleartext(modifiedDate, LocalContext.current)
//        androidx.compose.material3.Text(
//            text = timeDifference,
////            fontFamily = fontFamilyTitle
//        )
        // Display the device status with appropriate AssistChip icon
        deviceStatus?.let { status ->
            Chip(
//                modifier = Modifier.fillMaxWidth(),
//                colors = ChipDefaults.,
                icon = {
                    when (status) {
                        DeviceStatus.ONLINE -> androidx.compose.material3.Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null
                        )
                        DeviceStatus.HIGH_LATENCY -> androidx.compose.material3.Icon(
                            Icons.Default.Info,
                            contentDescription = null
                        )
                        DeviceStatus.MAY_BE_OFFLINE -> androidx.compose.material3.Icon(
                            Icons.Default.Warning,
                            contentDescription = null
                        )
                        DeviceStatus.OFFLINE -> androidx.compose.material3.Icon(
                            Icons.Outlined.Warning,
                            contentDescription = null
                        )
                    }
                },
                label = {
                    when(status.label){
                        "Device Online" -> {
                            androidx.compose.material3.Text(
                                stringResource(R.string.device_online),
//                                fontFamily = fontFamilyTitle
                            )
                        }
                        "High Latency" -> {
                            androidx.compose.material3.Text(
                                stringResource(R.string.high_latency),
//                                fontFamily = fontFamilyTitle
                            )
                        }
                        "Device May be Offline" -> {
                            androidx.compose.material3.Text(
                                stringResource(R.string.device_may_be_offline),
//                                fontFamily = fontFamilyTitle
                            )
                        }
                        "Device Offline" -> {
                            androidx.compose.material3.Text(
                                stringResource(R.string.device_offline),
//                                fontFamily = fontFamilyTitle
                            )
                        }
                    }
                },
                onClick = { showDialog = status == DeviceStatus.OFFLINE }
            )
        }
    }
}

// Function to fetch the modified date of the image asynchronously.
@SuppressLint("CoroutineCreationDuringComposition")
@Composable
private fun fetchImageModifiedDateAsync(
    imageUrl: String,
    onComplete: (String?) -> Unit
) {
    val scope = rememberCoroutineScope()

    scope.launch {
        try {
            withContext(Dispatchers.IO) {
                val connection = URL(imageUrl).openConnection() as HttpURLConnection
                connection.setRequestProperty("If-Modified-Since", "Sat, 1 Jan 2000 00:00:00 GMT")
                connection.connect()

                val lastModified = connection.getHeaderField("Last-Modified")

                onComplete(lastModified)
            }
        } catch (e: IOException) {
            onComplete(null)
        }
    }
}

// Function to calculate the time difference between the modified image and current time
private fun calculateTimeDifference(modifiedDate: String?, context: Context): Long {
    if (modifiedDate == null) {
        return 99999
    }

    val dateFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH)
    val userTimeZone = TimeZone.getDefault()

    val currentDate = Calendar.getInstance()
    val modifiedTime = Calendar.getInstance()

    try {
        // Parse the modified date with its time zone
        dateFormat.timeZone = TimeZone.getTimeZone("GMT")
        modifiedTime.time = dateFormat.parse(modifiedDate)

        // Compare time zones and adjust the modified time if needed
        val timeZoneDifferenceMillis = userTimeZone.rawOffset - modifiedTime.timeZone.rawOffset
        modifiedTime.add(Calendar.MILLISECOND, timeZoneDifferenceMillis)

        // Calculate the time difference
        val timeDifferenceMillis = currentDate.timeInMillis - modifiedTime.timeInMillis

        return timeDifferenceMillis
    } catch (e: Exception) {
//        e.printStackTrace()
        return 99999
    }
}

// Enum class representing device status
enum class DeviceStatus(val label: String, val icon: ImageVector) {
    ONLINE("Device Online", Icons.Default.CheckCircle),
    HIGH_LATENCY("High Latency", Icons.Default.Info),
    MAY_BE_OFFLINE("Device May be Offline", Icons.Default.Warning),
    OFFLINE("Device Offline", Icons.Outlined.Warning)
}

// Function to determine the device status based on the time difference
@Composable
private fun calculateDeviceStatus(modifiedDate: String?): DeviceStatus {
    val timeDifference = calculateTimeDifference(modifiedDate, LocalContext.current)
    Log.d("diff", timeDifference.toString())

    return when {
        timeDifference <= 30000 -> DeviceStatus.ONLINE
        timeDifference <= 35000 -> DeviceStatus.HIGH_LATENCY
        timeDifference <= 60000 -> DeviceStatus.MAY_BE_OFFLINE
        else -> DeviceStatus.OFFLINE
    }
}

// Function to calculate the time difference between the modified image and current time
private fun calculateTimeDifference_cleartext(modifiedDate: String?, context: Context): String {
    if (modifiedDate == null) {
        return "N/A"
    }

    val dateFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH)
    val userTimeZone = TimeZone.getDefault()

    val currentDate = Calendar.getInstance()
    val modifiedTime = Calendar.getInstance()

    try {
        // Parse the modified date with its time zone
        dateFormat.timeZone = TimeZone.getTimeZone("GMT")
        modifiedTime.time = dateFormat.parse(modifiedDate)

        // Compare time zones and adjust the modified time if needed
        val timeZoneDifferenceMillis = userTimeZone.rawOffset - modifiedTime.timeZone.rawOffset
        modifiedTime.add(Calendar.MILLISECOND, timeZoneDifferenceMillis)

        // Calculate the time difference
        val timeDifferenceMillis = currentDate.timeInMillis - modifiedTime.timeInMillis

        return formatElapsedTime(timeDifferenceMillis, context)
    } catch (e: Exception) {
        e.printStackTrace()
        return "N/A"
    }
}

// Function to format elapsed time in a human-readable format
@SuppressLint("StringFormatInvalid")
private fun formatElapsedTime(timeMillis: Long, context: Context): String {
    val resources = context.resources

    return when {
        timeMillis < DateUtils.MINUTE_IN_MILLIS -> {
            val seconds = timeMillis / DateUtils.SECOND_IN_MILLIS
            resources.getQuantityString(
                R.plurals.seconds_ago,
                seconds.toInt(),
                seconds
            )
        }
        timeMillis < DateUtils.HOUR_IN_MILLIS -> {
            val minutes = timeMillis / DateUtils.MINUTE_IN_MILLIS
            resources.getQuantityString(
                R.plurals.minutes_ago,
                minutes.toInt(),
                minutes
            )
        }
        timeMillis < DateUtils.DAY_IN_MILLIS -> {
            val hours = timeMillis / DateUtils.HOUR_IN_MILLIS
            val minutes = (timeMillis % DateUtils.HOUR_IN_MILLIS) / DateUtils.MINUTE_IN_MILLIS
            resources.getString(
                R.string.hours_and_minutes_ago,
                hours.toInt(),
                minutes.toInt()
            )
        }
        else -> {
            val days = timeMillis / DateUtils.DAY_IN_MILLIS
            val hours = (timeMillis % DateUtils.DAY_IN_MILLIS) / DateUtils.HOUR_IN_MILLIS
            val minutes = (timeMillis % DateUtils.HOUR_IN_MILLIS) / DateUtils.MINUTE_IN_MILLIS
            resources.getString(
                R.string.days_and_hours_and_minutes_ago,
                days.toInt(),
                hours.toInt(),
                minutes.toInt()
            )
        }
    }
}

@Composable
fun SensorReading(icon: ImageVector, reading: String, alertColor: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        androidx.compose.material3.Icon(
            imageVector = icon,
            contentDescription = null,
            tint = alertColor,
//            modifier = Modifier.size(24.dp)
        )
        androidx.compose.material3.Text(
            text = reading,
            style = androidx.compose.material3.MaterialTheme.typography.headlineSmall.copy(
                color = alertColor,
//                fontWeight = FontWeight.Bold
            )
        )
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
fun LineChart(tempHumidData: List<Pair<String, Pair<Float, Float>>>, device_serial: String) {
    val color_1 = androidx.compose.material3.MaterialTheme.colorScheme.primary
    val color_2 = androidx.compose.material3.MaterialTheme.colorScheme.error
    val color_3 = androidx.compose.material3.MaterialTheme.colorScheme.error.copy(alpha = 0.8f)

    val axisColor = LocalContentColor.current.copy(alpha = 0.6f)
    val textColor = LocalContentColor.current.copy(alpha = 0.8f)
    val a = android.graphics.Paint()
    a.color = androidx.compose.material3.MaterialTheme.colorScheme.primary.hashCode()
    a.textSize = 30.5F

    val b = android.graphics.Paint()
    b.color = androidx.compose.material3.MaterialTheme.colorScheme.error.hashCode()
    b.textSize = 30.5F

    var showTemperatureChart by remember { mutableStateOf(true) }
    var showHumidityChart by remember { mutableStateOf(true) }

    var humidThreshold: Float? by remember { mutableStateOf(0.0F) }

    var temperatureText = stringResource(R.string.temperature_c_1)
    var humidityText = stringResource(R.string.humidity_1)

    LaunchedEffect(Unit) {
        val temperatureKey = true
        val humidityKey = true
        humidThreshold = fetchHumidThreshold(device_serial)
        if (humidThreshold == null) {
            humidThreshold = 0.0F
        }
        Log.d("HUMID THRES:", humidThreshold.toString())
//        showTemperatureChart = dataStore.data.first()[temperatureKey] ?: true
//        showHumidityChart = dataStore.data.first()[humidityKey] ?: true
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
            .clip(shape = androidx.compose.material3.MaterialTheme.shapes.medium),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        androidx.compose.material3.Text(
            stringResource(R.string.humidity_threshold) + humidThreshold + " %",
//            fontFamily = fontFamilyContent
        )
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