package team.baby.guardian.ui

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.SnackbarDefaults.backgroundColor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.ToggleOff
import androidx.compose.material.icons.filled.ToggleOn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.media3.common.MimeTypes
import androidx.media3.session.MediaSession
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import coil.compose.rememberAsyncImagePainter
import coil.imageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONException
import team.baby.guardian.R
import java.io.IOException
import java.security.SecureRandom


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SensorLiveView(serialNumber: String, deviceName: String, ownerStatus: String, context: Context) {
    Scaffold(
        topBar = {

        },
        content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column (
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment= Alignment.CenterHorizontally
                ){
                    Spacer(modifier = Modifier.height(10.dp))
                    Icon(
                        imageVector = Icons.Filled.Sensors,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = stringResource(R.string.live_sensor_data_for) + serialNumber,
                        style = MaterialTheme.typography.headlineSmall,
                        fontFamily = fontFamilyTitle
                    )
                    Text(
                        text = deviceName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontFamily = fontFamilyTitle
                    )
                    var ownerStatus_translation = ""
                    if(ownerStatus == "owner"){
                        ownerStatus_translation = stringResource(R.string.owner)
                    }else{
                        ownerStatus_translation = stringResource(R.string.friend)
                    }
                    Text(
                        text = stringResource(R.string.you_are_the) + ownerStatus_translation,
                        style = MaterialTheme.typography.headlineSmall,
                        fontFamily = fontFamilyTitle
                    )
                }

                NotificationPermissionCheck()
                Row (
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ){
                    SwitchWithSharedPreference()
                }

                UpdateFrequencyWithSharedPreference()

                checkDeviceStatus(serialNumber)
                TempHumidDataFetcher(serialNumber)
                if(ownerStatus == "owner"){
                    Controls(serialNumber)
                }else{
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = stringResource(R.string.only_owner_can_set_controls_and_dismiss_notifications),
                        style = MaterialTheme.typography.headlineSmall,
                        fontFamily = fontFamilyTitle,
                        textAlign = TextAlign.Center
                    )
                }

                babyNotify(serialNumber, ownerStatus)
            }
        }
    )
}

@Composable
fun SwitchWithSharedPreference() {
    var isSwitchOn by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Read from SharedPreferences on Composable initialization
    LaunchedEffect(key1 = "diagram_style") {
        withContext(Dispatchers.IO) {
            val sharedPreferences = context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
            isSwitchOn = sharedPreferences.getBoolean("diagram_style", false)
        }
    }

    DisposableEffect(isSwitchOn) {
        onDispose {
            val sharedPreferences =
                context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
            sharedPreferences.edit().putBoolean("diagram_style", isSwitchOn).apply()
        }
    }

    // Icon based on switch state
    val switchIcon =
        if (isSwitchOn) Icons.Default.ToggleOn else Icons.Default.ToggleOff

    Column (
        horizontalAlignment = Alignment.CenterHorizontally,
    ){
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = stringResource(R.string.draw_two_lines_into_one_graph), textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.width(10.dp))
            Switch(
                checked = isSwitchOn,
                onCheckedChange = { isChecked ->
                    isSwitchOn = isChecked
                },
                modifier = Modifier
            )
        }
    }

}

@Composable
fun UpdateFrequencyWithSharedPreference() {
    var frequency by remember { mutableStateOf(4000) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Read from SharedPreferences on Composable initialization
    LaunchedEffect(key1 = "update_freq") {
        withContext(Dispatchers.IO) {
            val sharedPreferences = context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
            frequency = sharedPreferences.getInt("update_freq", 4000)
        }
    }

    DisposableEffect(frequency) {
        onDispose {
            scope.launch(Dispatchers.IO) {
                val sharedPreferences =
                    context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
                sharedPreferences.edit().putInt("update_freq", frequency).apply()
            }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = stringResource(R.string.update_frequency_now) + frequency / 1000 + stringResource(
                R.string.seconds_per_update
            ),
            textAlign = TextAlign.Center
        )
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = {
                if (frequency > 4000) {
                    frequency -= 1000
                }
            }) {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowUp,
                    contentDescription = "Frequency Up"
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(text = stringResource(R.string.quicker), textAlign = TextAlign.Center)
            }

            Spacer(modifier = Modifier.width(10.dp))

            Button(onClick = {
                if (frequency < 60000) {
                    frequency += 1000
                }
            }) {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = "Frequency Down"
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(text = stringResource(R.string.slower), textAlign = TextAlign.Center)
            }
        }
        Text(
            text = stringResource(R.string.you_need_to_back_and_reenter_to_confirm),
            textAlign = TextAlign.Center,
            fontFamily = fontFamilyTitle
        )

    }

}

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun babyNotify(deviceSerial: String, ownerStatus: String) {
    var notifications by remember { mutableStateOf<List<NotificationItem>>(emptyList()) }

    val context = LocalContext.current

    // Initialize the WorkManager in your application or activity
    val workManager = WorkManager.getInstance(context)

//    WorkManager.getInstance(context).enqueue(alertWorkRequest)

    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "ALERT_WORK_UNIQUE_NAME", // A unique name for the work
        ExistingPeriodicWorkPolicy.UPDATE, // Replace the existing work if any
        alertWorkRequest
    )

    var frequency by remember { mutableStateOf(4000) }
    var language: String? by remember {
        mutableStateOf("en")
    }

    // Read from SharedPreferences on Composable initialization
    LaunchedEffect(key1 = "update_freq") {
        withContext(Dispatchers.IO) {
            val sharedPreferences = context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
            frequency = sharedPreferences.getInt("update_freq", 4000)

            language = readSettings("language")
            if(language == null){
                language = supportedLanguages.first().code
            }
        }
    }

    LaunchedEffect(deviceSerial) {
        while (true) {
            // Perform the proactive fetch in a background thread
            proactiveFetch(deviceSerial, context, ownerStatus) {

            }
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

    val scope = CoroutineScope(Dispatchers.Default)
    scope.launch {
        while (true) {
            withContext(Dispatchers.IO) {
                val workRequest = OneTimeWorkRequestBuilder<AlertWorker>().build()
                WorkManager.getInstance(context).enqueue(workRequest)
            }
            try {
                delay(frequency.toLong())
            }catch (e: Exception){
                delay(4000)
            }
        }
    }

    // Enqueue the periodic work request
//    workManager.enqueueUniquePeriodicWork(
//        "alertWorkRequest",
//        ExistingPeriodicWorkPolicy.KEEP,
//        alertWorkRequest
//    )

    // UI for displaying the notifications
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
//                .verticalScroll(rememberScrollState())
//                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            var lazyColumnModifier = Modifier.height(1000.dp)

            if (notifications.isEmpty()) {
                lazyColumnModifier = Modifier.height(0.dp)
                IconWithText(
                    icon = Icons.Default.BeachAccess,
                    message = stringResource(R.string.your_day_is_clear_for_now_nothing_needs_attention_have_a_wonderful_day)
                )
            }

            LazyColumn(
                modifier = lazyColumnModifier
            ){
                items(notifications.size){notification ->
                    NotificationCard(notifications[notification], ownerStatus) {
                        // Handle dismiss button click
                        dismissNotification(deviceSerial, notifications[notification].id) {
                            // Update the UI after dismissal if needed
                            // Reload notifications or update the UI state
                            proactiveFetch(deviceSerial, context, ownerStatus) {

                            }
                            proactiveFetch_notified(deviceSerial) { fetchedNotifications ->
                                notifications = fetchedNotifications.reversed().map {
                                    NotificationItem(it.id, it.alert, it.type, it.datetime, it.addition)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(5.dp))

                    if(notifications[notification].addition != ""){
                        if(language == "zh"){
                            GeminiUI("image", "please give me instructions in Simplified Chinese language that given that alert type "+notifications[notification].type+" and its actual content "+notifications[notification].alert+" together with that image, can you analyse the situation and give parents some advice given in the context of baby caring?", notifications[notification].addition, true)
                        }else{
                            GeminiUI("image", "please give me instructions in English that given that alert type "+notifications[notification].type+" and its actual content "+notifications[notification].alert+" together with that image, can you analyse the situation and give parents some advice given in the context of baby caring?", notifications[notification].addition, true)
                        }
                    }else{
                        if(language == "zh"){
                            GeminiUI("full", "please give me instructions in Simplified Chinese language that given that alert type "+notifications[notification].type+" and its actual content "+notifications[notification].alert+" together with the text, can you analyse the situation and give parents some advice given in the context of baby caring?", notifications[notification].addition, true)
                        }else{
                            GeminiUI("full", "please give me instructions in English that given that alert type "+notifications[notification].type+" and its actual content "+notifications[notification].alert+" together with the text, can you analyse the situation and give parents some advice given in the context of baby caring?", notifications[notification].addition, true)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

        }
    }
}

@Composable
fun IconWithText(icon: ImageVector, message: String) {
    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "Icon",
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineSmall
        )
    }
}

// Function to perform proactive HTTP POST fetch in the background
@SuppressLint("ServiceCast")
fun proactiveFetch(deviceSerial: String, context: Context, ownerStatus: String, resultCallback: (List<NotificationItem>) -> Unit) {
    val url = "https://weicheng.app/baby_guardian/alert.php"
    val client = OkHttpClient()

    val formBody = FormBody.Builder()
        .add("mode", "find")
        .add("device_serial", deviceSerial)
        .add("status", "d2u_sent")
        .build()

    val request = Request.Builder()
        .url(url)
        .post(formBody)
        .build()

    val handler = Handler(Looper.getMainLooper())
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

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
                        sendNotification(context, type, deviceSerial+" - "+alert+" at "+datetime+" | "+addition)
//                        if(ownerStatus == "owner"){
                            makeUpdateRequest(deviceSerial, id)
//                        }
                        // Create NotificationItem and add to the list
                        vibrateDeviceOnce(vibrator)
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

fun vibrateDeviceOnce(vibrator: Vibrator) {
    vibrator.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE))
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

@OptIn(DelicateCoroutinesApi::class)
private fun makeUpdateRequest(deviceSerial: String, notificationId: String) {
    val url = "https://weicheng.app/baby_guardian/alert.php"
    val client = OkHttpClient()

    val formBody = FormBody.Builder()
        .add("mode", "update")
        .add("device_serial", deviceSerial)
        .add("status", "d2u_notified")
        .add("id", notificationId)
        .build()

    val request = Request.Builder()
        .url(url)
        .post(formBody)
        .build()

    // Execute the update request asynchronously
    GlobalScope.launch(Dispatchers.IO) {
        try {
            val response = client.newCall(request).execute()

            // Handle the response if needed
        } catch (e: IOException) {
            // Handle network errors
//            e.printStackTrace()
        }
    }
}

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
                Text(
                    text = notification.type,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )

                if(ownerStatus == "owner"){
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Delete, contentDescription = "Dismiss")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = notification.datetime,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(stringResource(R.string.alert))
                    }
                    append(notification.alert)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Show addition directly
            Text(
                text = notification.addition,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            when {
                notification.addition.startsWith("https://") && notification.addition.endsWith(".mp4") -> {
                    // Show video component
                    VideoComponent(url = notification.addition)
                }
                notification.addition.startsWith("https://") && notification.addition.endsWith(".png") -> {
                    // Show image component
                    ImageComponent(imageUrl = notification.addition)
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

    val mediaList = listOf(
        MediaInfo(
            stringResource(R.string.audio_playback),
            url,
            MimeTypes.AUDIO_MP4,
            url
        ),
    )
    Spacer(modifier = Modifier.height(10.dp))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = {
                    openImageInBrowser(url, context)
                }
            )
    ) {
        Column (
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            MediaCard(mediaList[0], context, secureRandom.nextInt(), sessionMap, true)
            Spacer(modifier = Modifier.height(6.dp))
            Button(onClick = { openImageInBrowser(url, context) }) {
                Icon(
                    Icons.Default.OpenInBrowser, ""
                )
                Text(stringResource(R.string.open_in_browser))
            }
        }
    }
}

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
                    openImageInBrowser(url, context)
                }
            )
    ) {
        Column (
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            MediaCard(mediaList[0], context, secureRandom.nextInt(), sessionMap, true)
            Button(onClick = { openImageInBrowser(url, context) }) {
                Icon(
                    Icons.Default.OpenInBrowser, ""
                )
                Text("Open in Browser")
            }
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
                    openImageInBrowser(imageUrl, context)
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

data class NotificationItem(
    val id: String,
    val alert: String,
    val type: String,
    val datetime: String,
    val addition: String
)

// Example dismissNotification function
fun dismissNotification(deviceSerial: String, notificationId: String, onDismissed: () -> Unit) {
    val url = "https://weicheng.app/baby_guardian/alert.php"
    val client = OkHttpClient()

    val formBody = FormBody.Builder()
        .add("mode", "delete")
        .add("device_serial", deviceSerial)
        .add("id", notificationId)
        .build()

    val request = Request.Builder()
        .url(url)
        .post(formBody)
        .build()

    GlobalScope.launch(Dispatchers.IO) {
        try {
            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                // Dismissal successful
                onDismissed()
            } else {
                // Handle unsuccessful dismissal
                val errorMessage = response.body?.string() ?: "Dismissal failed"
                Log.e("DismissNotification", "Dismissal failed: $errorMessage")

                // You may want to show an error message to the user
            }
        } catch (e: IOException) {
            // Handle network errors
            Log.e("DismissNotification", "Network error: ${e.message}")

            // You may want to show an error message to the user
        } catch (e: Exception) {
            // Handle other exceptions
            Log.e("DismissNotification", "Error: ${e.message}")

            // You may want to show an error message to the user
        }
    }
}