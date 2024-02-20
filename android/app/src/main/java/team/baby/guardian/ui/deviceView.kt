package team.baby.guardian.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.imageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import team.baby.guardian.R
import team.baby.guardian.ScreenHelpers

@Composable
fun ClickableDeviceLatestPhoto(navController: NavController, serialNumber: String, deviceName: String, owner_status: String, context: Context) {
    var imageUrl by remember { mutableStateOf("https://weicheng.app/baby_guardian/photos/$serialNumber/latest.jpg") }
    Log.d("checkURL", imageUrl)

    var scaleState by remember { mutableStateOf(1f) }
    val coroutineScope = rememberCoroutineScope()

    var language: String? by remember {
        mutableStateOf("en")
    }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            language = readSettings("language")
            if(language == null){
                language = supportedLanguages.first().code
            }
        }
    }

    if(language == "zh"){
        GeminiUI("latest_image", "please interpret and describe this image for me in Simplified Chinese, also do some analysis.", imageUrl, true)
    }else{
        GeminiUI("latest_image", "please interpret and describe this image for me in English, also do some analysis.", imageUrl, true)
    }

    Box(
        modifier = Modifier
//            .fillMaxWidth()
//            .wrapContentWidth()
            .clickable(
                onClick = {
                    imageUrl = "https://weicheng.app/baby_guardian/photos/$serialNumber/latest.jpg"
                    clearCoilCache(context)
                    openImageInBrowser(imageUrl, context)
                    // Handle click action, e.g., navigate to a detail screen
                }
            )
    ) {
        LaunchedEffect(serialNumber) {
            while (true) {
                delay(30000) // Delay for 30 seconds
                // Update the image URL without using a timestamp
                imageUrl = "https://weicheng.app/baby_guardian/photos/$serialNumber/latest.jpg"
                clearCoilCache(context)
            }
        }
        val imageLoader = LocalContext.current.imageLoader
        val imagePainter = rememberAsyncImagePainter(
            ImageRequest.Builder(LocalContext.current).data(data = imageUrl).apply(block = fun ImageRequest.Builder.() {
                crossfade(true)
//                scale(Scale.FILL)
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

    Button(
        onClick = {
            imageUrl = "https://weicheng.app/baby_guardian/photos/$serialNumber/latest.jpg"
            coroutineScope.launch {
                saveSettings("serial_number", serialNumber)
                saveSettings("device_name", deviceName)
                saveSettings("owner_status", owner_status)
            }

            clearCoilCache(context)
            navController.navigate(ScreenHelpers.LiveView.name)
        },
        modifier = Modifier.padding(8.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.LiveTv,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(stringResource(R.string.watch_live_stream))
    }

    Button(
        onClick = {
            coroutineScope.launch {
                saveSettings("serial_number", serialNumber)
                saveSettings("device_name", deviceName)
                saveSettings("owner_status", owner_status)
            }
            navController.navigate(ScreenHelpers.Sensor.name)
        },
//        modifier = Modifier.padding(8.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Sensors,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(stringResource(R.string.device_sensor_data))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun DeviceLiveView(serialNumber: String, deviceName: String, ownerStatus: String, context: Context) {
    var scale by remember { mutableStateOf(1f) }
    var rotation by remember { mutableStateOf(0f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var context = LocalContext.current
    val state = rememberTransformableState { zoomChange, offsetChange, rotationChange ->
        scale *= zoomChange
        rotation += rotationChange
        offset += offsetChange
    }

    Scaffold(
        topBar = {

        },
        content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
//                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
//                    .clickable {
//                        // Handle click to toggle fullscreen or perform other actions
//                        scale = 1f
//                        rotation = 0f
//                        offset = Offset.Zero
//                    }
//                    .graphicsLayer(
//                        scaleX = scale,
//                        scaleY = scale,
//                        rotationZ = rotation,
//                        translationX = offset.x,
//                        translationY = offset.y
//                    )
//                    .transformable(state = state)
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                MediaScreen(serialNumber = serialNumber, deviceName = deviceName, ownerStatus = ownerStatus, context = context)
            }
        }
    )
//    Surface(
//        modifier = Modifier.fillMaxSize(),
//        color = MaterialTheme.colorScheme.background
//    ) {
//
//    }
}

private fun clearCoilCache(context: Context) {
    // Clear Coil cache
    val imageLoader = ImageLoader.Builder(context)
        .memoryCachePolicy(CachePolicy.DISABLED)
        .crossfade(true)
        .diskCachePolicy(CachePolicy.WRITE_ONLY)
        .build()

    imageLoader.memoryCache?.clear()
}

fun openImageInBrowser(imageUrl: String, context: Context) {
    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(imageUrl))
    context.startActivity(browserIntent)
}

suspend fun performHttpPost(url: String, keys: List<String>, parameters: List<String>): String {
    return withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient()

            // Create form body with keys and parameters
            val formBody = FormBody.Builder()
            for (i in keys.indices) {
                formBody.add(keys[i], parameters[i])
            }

            // Build the request
            val request = Request.Builder()
                .url(url)
                .post(formBody.build())
                .build()

            // Execute the request and get the response
            val response = client.newCall(request).execute()

            // Return the response body as a string
            response.body?.string() ?: "Empty response"
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }
}