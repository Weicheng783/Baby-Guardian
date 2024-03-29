package team.baby.guardian.ui

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.text.Layout
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.annotation.RequiresExtension
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.SignalCellularNodata
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.createDataStore
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import team.baby.guardian.R
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalTime
import kotlin.random.Random


@SuppressLint("CoroutineCreationDuringComposition", "UnusedBoxWithConstraintsScope")
@RequiresExtension(extension = Build.VERSION_CODES.R, version = 2)
@Composable
fun HomeScreen(
    navController: NavController,
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier.background(MaterialTheme.colorScheme.background)
) {
    var result by remember { mutableStateOf<List<String>?>(null) }
    val scrollState = rememberScrollState()
    var isFetchSuccessful by remember { mutableStateOf(false) }
    var context = LocalContext.current
    dataStore = LocalContext.current.createDataStore(name = "isBeta")

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = FocusRequester()

    var userLogged by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val temp = readUser("username")
            userLogged = temp.orEmpty()
        }
    }

    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val isNetworkAvailable = isNetworkAvailable(connectivityManager)
    val isNotMeteredNetwork = isMeteredNetwork(connectivityManager)

    if (isNotMeteredNetwork) {
        LaunchedEffect(Unit) {
            withContext(Dispatchers.IO) {
                result =
                    fetchNewVersion("https://weicheng.app/baby_guardian/version.txt", context)
                isFetchSuccessful = result != null
            }
        }
    }

    var markEnforcing: String? by remember { mutableStateOf("") }

    if (markEnforcing == "") {
        LaunchedEffect(Unit) {
            try {
                withContext(Dispatchers.IO) {
                    if(readUser("update_enforce") != null){
                        markEnforcing = readUser("update_enforce")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(state = scrollState)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() } // This is mandatory
            ) {
                // Hide keyboard and lose focus when clicked outside
                keyboardController?.hide()
                focusManager.clearFocus()
            },
    ) {
        var showTwoPane by remember { mutableStateOf(false) }
        if(windowSizeClass.widthSizeClass == WindowWidthSizeClass.Medium || windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded){
            showTwoPane = true
        }
        Row {
            Column(
                modifier = modifier
                    .weight(1f)
                    .height(calculateUsableScreenHeightDp().dp)
                    .verticalScroll(rememberScrollState()),
                //            verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    //                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))
                ) {
                    val greeting by remember { mutableStateOf(getGreeting()) }
                    Surface(
                        modifier = Modifier.padding(10.dp),
                    ) {
                        Text(
                            text = stringResource(id = greeting),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.headlineLarge
                        )
                    }
                    Text(
                        text = stringResource(R.string.title_main),
                        style = MaterialTheme.typography.headlineMedium,
                        fontFamily = fontFamilyTitle,
                        textAlign = TextAlign.Center
                        //                    modifier = Modifier.padding(dimensionResource(R.dimen.padding_small))
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val randomText =
                        when (Random.nextInt(0, 10)) { // Assuming you have 10 sentences
                            0 -> stringResource(R.string.affirm_1)
                            1 -> stringResource(R.string.affirm_2)
                            2 -> stringResource(R.string.affirm_3)
                            3 -> stringResource(R.string.affirm_4)
                            4 -> stringResource(R.string.affirm_5)
                            5 -> stringResource(R.string.affirm_6)
                            6 -> stringResource(R.string.affirm_7)
                            7 -> stringResource(R.string.affirm_8)
                            8 -> stringResource(R.string.affirm_9)
                            9 -> stringResource(R.string.affirm_10)
                            else -> "" // Handle any additional cases
                        }

//                    if (windowSizeClass.heightSizeClass == WindowHeightSizeClass.Compact) {
//                        Text(
//                            text = "Compact",
//                            fontSize = 20.sp,
//                            textAlign = TextAlign.Center,
//                            fontFamily = fontFamily,
//                        )
//                    } else if (windowSizeClass.heightSizeClass == WindowHeightSizeClass.Medium) {
//                        Text(
//                            text = "Medium",
//                            fontSize = 20.sp,
//                            textAlign = TextAlign.Center,
//                            fontFamily = fontFamily,
//                        )
//                    } else if (windowSizeClass.heightSizeClass == WindowHeightSizeClass.Expanded) {
//                        Text(
//                            text = "Expanded",
//                            fontSize = 20.sp,
//                            textAlign = TextAlign.Center,
//                            fontFamily = fontFamily,
//                        )
//                    }
//
//                    if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact) {
//                        Text(
//                            text = "W Compact",
//                            fontSize = 20.sp,
//                            textAlign = TextAlign.Center,
//                            fontFamily = fontFamily,
//                        )
//                    } else if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Medium) {
//                        Text(
//                            text = "W Medium",
//                            fontSize = 20.sp,
//                            textAlign = TextAlign.Center,
//                            fontFamily = fontFamily,
//                        )
//                    } else if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded) {
//                        Text(
//                            text = "W Expanded",
//                            fontSize = 20.sp,
//                            textAlign = TextAlign.Center,
//                            fontFamily = fontFamily,
//                        )
//                    }

                    Text(
                        text = randomText,
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center,
                        fontFamily = fontFamily,
                    )

                    //                AnimatedBackAnimationScreen()

                    //                Button(onClick = {
                    //                    // Simulate a crash (for testing purposes only)
                    //                    throw RuntimeException("Test Crash") // This line intentionally causes the app to crash
                    //                }) {
                    //                    Text(text = "Crash App")
                    //                }

                    if (isNotMeteredNetwork) {
                        if (isFetchSuccessful) {
                            // File fetched successfully
                            ResultedUpdatesResults(
                                result,
                                false,
                                result?.get(3).toBoolean(),
                                context = LocalContext.current
                            )
                        } else {
                            // File fetch unsuccessful
                            AssistChipConstructor(
                                text = stringResource(R.string.server_disconnected),
                                icon = Icons.Filled.CloudOff
                            )
                            InternetNotConnectedPage(
                                stringResource(R.string.no_internet_connection),
                                stringResource(R.string.check_internet_text),
                                Icons.Default.WifiOff
                            )
                        }
                    } else {
                        if (isNetworkAvailable) {
                            AssistChipConstructor(
                                text = stringResource(R.string.cellular_or_metered_network_detected),
                                icon = Icons.Filled.WifiOff
                            )
                            InternetNotConnectedPage(
                                stringResource(R.string.no_cellular_allowed),
                                stringResource(R.string.no_cellular_allowed_description),
                                Icons.Default.SignalCellularNodata
                            )
                        } else {
                            // File fetch unsuccessful
                            AssistChipConstructor(
                                text = stringResource(R.string.server_disconnected),
                                icon = Icons.Filled.CloudOff
                            )
                            InternetNotConnectedPage(
                                stringResource(R.string.no_internet_connection),
                                stringResource(R.string.check_internet_text),
                                Icons.Default.WifiOff
                            )
                        }
                    }

                    // Main Logic
                    // Check update forcing or not
                    if (markEnforcing != "true") {
                        if (userLogged != "") {
                            Text(
                                text = stringResource(R.string.welcome) + userLogged,
                                fontSize = 25.sp,
                                textAlign = TextAlign.Center, // Center the text horizontally
                                fontFamily = fontFamily // Example of specifying a font family
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.guest_mode),
                                fontSize = 25.sp,
                                textAlign = TextAlign.Center, // Center the text horizontally
                                fontFamily = fontFamily // Example of specifying a font family
                            )
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Park,
                                    contentDescription = stringResource(R.string.please_login_first),
                                    modifier = Modifier
                                        .size(120.dp)
                                        .padding(16.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = stringResource(R.string.journey_starts_from_login),
                                    fontFamily = fontFamilyTitle,
                                    fontSize = 25.sp,
                                    //                            modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    } else {
                        InternetNotConnectedPage(
                            stringResource(R.string.software_update_enforcing),
                            stringResource(R.string.dear_baby_guardian_you_must_install_this_software_update_to_continue_using_this_app_we_are_enforcing_this_because_we_have_major_updates_about_the_system_architecture_database_etc_and_the_latest_security_updates_please_catch_up_you_have_left_behind_see_you_in_the_next_update),
                            Icons.Default.Download
                        )
                        Column(
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            Section4()
                        }
                    }

                    if (userLogged != "" && markEnforcing != "true") {
                        DeviceListScreen(navController, userLogged)
                    }

                    if (userLogged != "" && markEnforcing != "true") {
                        GeminiUI("full", "", "", false)
                    }

                    // Disable Unreal Viewer starting from 240317 as it is no longer needed.
//                    if (userLogged != "" && markEnforcing != "true") {
//                        UnrealViewer()
//                    }

                    Text(
                        text = stringResource(R.string.contributions),
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center, // Center the text horizontally
                        fontFamily = fontFamily // Example of specifying a font family
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(R.string.acknowledgments),
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center, // Center the text horizontally
                        fontFamily = fontFamily // Example of specifying a font family
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(R.string.last_sentence),
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center, // Center the text horizontally
                        fontFamily = fontFamily // Example of specifying a font family
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stringResource(id = R.string.version) + "." + stringResource(id = R.string.build_type),
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center, // Center the text horizontally
                        fontFamily = fontFamily // Example of specifying a font family
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
//             TODO: Add two panes view according to form factors
            if (userLogged != "" && markEnforcing != "true") {
                if(windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded){
                    Column(
                        modifier = modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .height(calculateUsableScreenHeightDp().dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        //            verticalArrangement = Arrangement.SpaceBetween
                    ) {
//                    DeviceListScreen(navController, userLogged)
                        var coroutineScope = rememberCoroutineScope();
//                    navController.navigate(ScreenHelpers.Sensor.name)
                        SensorLiveView(serialNumber = "1", deviceName = "The Baby Guardian Uno", ownerStatus = "owner", context = LocalContext.current)
                    }
                }
            }
        }
    }
}

@SuppressLint("ServiceCast")
@Composable
fun calculateUsableScreenHeightDp(): Int {
    val context = LocalContext.current
    val windowManager = LocalView.current.context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val displayMetrics = DisplayMetrics()
    windowManager.defaultDisplay.getMetrics(displayMetrics)
    val density = LocalDensity.current.density
    return (displayMetrics.heightPixels / density).toInt()
}

fun isMeteredNetwork(connectivityManager: ConnectivityManager): Boolean {
    val network = connectivityManager.activeNetwork
    val capabilities = connectivityManager.getNetworkCapabilities(network)
    return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
}

fun isNetworkAvailable(connectivityManager: ConnectivityManager): Boolean {
    val network = connectivityManager.activeNetwork
    val capabilities = connectivityManager.getNetworkCapabilities(network)
    return capabilities != null &&
            (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
}

@Composable
fun InternetNotConnectedPage(largeText: String, contentText: String, imageVector: ImageVector) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = stringResource(id = R.string.no_internet_connection),
            modifier = Modifier
                .size(120.dp)
                .padding(16.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = largeText,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(8.dp)
        )
        Text(
            text = contentText,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(8.dp)
        )
    }
}

@Composable
fun ResultedUpdatesResults(result: List<String>?, showButton: Boolean, chooseBeta: Boolean, context: Context){
    var markEnforcing by remember { mutableStateOf(false) }
    val versionNo = stringResource(id = R.string.version).toInt()
    val beta = stringResource(id = R.string.build_type) == "beta"
    var coroutine = rememberCoroutineScope()

    if(markEnforcing || !markEnforcing){
        LaunchedEffect(Unit) {
            try {
                withContext(Dispatchers.IO) {
                    if((result?.get(4)?.endsWith("beta") == false)){
                        if(beta){
                            if((result[4].take(6).toInt()) >= versionNo){
                                saveUser("update_enforce", "true")
                                markEnforcing = true
                            }else{
                                saveUser("update_enforce", "")
                                markEnforcing = false
                            }
                        }else{
                            if((result[4].take(6).toInt()) >= versionNo){
                                saveUser("update_enforce", "true")
                                markEnforcing = true
                            }else{
                                saveUser("update_enforce", "")
                                markEnforcing = false
                            }
                        }
                    }else{
                        if(beta){
                            if((result?.get(4)?.take(6)?.toInt() ?: 240210) >= versionNo){
                                saveUser("update_enforce", "true")
                                markEnforcing = true
                            }else{
                                saveUser("update_enforce", "")
                                markEnforcing = false
                            }
                        }else{
                            saveUser("update_enforce", "")
                            markEnforcing = false
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    if((result?.get(0)?.take(6) ?: 240210) == stringResource(id = R.string.version)){
        if(!chooseBeta && stringResource(id = R.string.build_type)=="beta"){
            if((result?.get(0)?.endsWith("beta") == false)){
                if(markEnforcing){
                    AssistChipConstructor(
                        text = stringResource(R.string.update_enforcing),
                        icon = Icons.Filled.Download,
                    )
                }else{
                    AssistChipConstructor(
                        text = stringResource(R.string.server_connected_updates_available),
                        icon = Icons.Filled.Download
                    )
                }
                if(showButton){
                    Button(onClick = {
                        val webIntent: Intent =
                            Intent(Intent.ACTION_VIEW, Uri.parse(result.get(1) ?: ""))
                        try {
                            ContextCompat.startActivity(context, webIntent, null)
                        } catch (e: ActivityNotFoundException) {
                            // Define what your app should do if no activity can handle the intent.
                        }
                    }) {
                        Text(stringResource(R.string.download_upgrade_to_the_latest_stable))
                    }
                }
            }else{
                AssistChipConstructor(
                    text = stringResource(R.string.server_connected_downgrades_available),
                    icon = Icons.Filled.Restore
                )
                if(showButton){
                    Button(onClick = {
                        val webIntent: Intent =
                            Intent(Intent.ACTION_VIEW, Uri.parse(result?.get(1) ?: ""))
                        try {
                            ContextCompat.startActivity(context, webIntent, null)
                        } catch (e: ActivityNotFoundException) {
                            // Define what your app should do if no activity can handle the intent.
                        }
                    }) {
                        Text(stringResource(R.string.download_reinstall_the_latest_version))
                    }
                }
            }
        }else if((result?.get(0)?.endsWith("beta") == false) && stringResource(id = R.string.build_type)=="beta"){
            if(markEnforcing){
                AssistChipConstructor(
                    text = stringResource(R.string.update_enforcing),
                    icon = Icons.Filled.Download
                )
            }else{
                AssistChipConstructor(
                    text = stringResource(R.string.server_connected_updates_available),
                    icon = Icons.Filled.Download
                )
            }
            if(showButton){
                Button(onClick = {
                    coroutine.launch {
                        saveUser("update_enforce", "")
                        markEnforcing = false
                    }
                    val webIntent: Intent =
                        Intent(Intent.ACTION_VIEW, Uri.parse(result.get(1) ?: ""))
                    try {
                        ContextCompat.startActivity(context, webIntent, null)
                    } catch (e: ActivityNotFoundException) {
                        // Define what your app should do if no activity can handle the intent.
                    }
                }) {
                    Text(stringResource(R.string.download_the_latest_version))
                }
            }
        }else{
            AssistChipConstructor(
                text = stringResource(R.string.server_connected_running_the_latest_version),
                icon = Icons.Filled.CheckCircle
            )
        }
    }else{
        if((result?.get(0)?.take(6)?.toInt() ?: 240210) < stringResource(id = R.string.version).toInt() && (stringResource(R.string.build_type) == "beta")){
            AssistChipConstructor(
                text = stringResource(R.string.server_connected_downgrades_available),
                icon = Icons.Filled.Restore
            )
            if(showButton){
                Button(onClick = {
                    val webIntent: Intent =
                        Intent(Intent.ACTION_VIEW, Uri.parse(result?.get(1) ?: ""))
                    try {
                        ContextCompat.startActivity(context, webIntent, null)
                    } catch (e: ActivityNotFoundException) {
                        // Define what your app should do if no activity can handle the intent.
                    }
                }) {
                    Text(stringResource(R.string.download_reinstall_the_latest_version))
                }
            }
        }else{
            if((markEnforcing)){
                AssistChipConstructor(
                    text = stringResource(R.string.update_enforcing),
                    icon = Icons.Filled.Download
                )
            }else{
                AssistChipConstructor(
                    text = stringResource(R.string.server_connected_updates_available),
                    icon = Icons.Filled.Download
                )
            }
            if(showButton){
                Button(onClick = {
                    coroutine.launch {
                        saveUser("update_enforce", "")
                        markEnforcing = false
                    }
                    val webIntent: Intent =
                        Intent(Intent.ACTION_VIEW, Uri.parse(result?.get(1) ?: ""))
                    try {
                        ContextCompat.startActivity(context, webIntent, null)
                    } catch (e: ActivityNotFoundException) {
                        // Define what your app should do if no activity can handle the intent.
                    }
                }) {
                    Text(stringResource(R.string.download_the_latest_version))
                }
            }
        }
    }
}

suspend fun fetchNewVersion(url: String, context: Context): List<String>? {
    if (!isInternetLocationReachable(url)) {
        return null
    }

    try {
        val websiteUrl = URL(url)
        val connection = websiteUrl.openConnection() as HttpURLConnection

        // Set a timeout for the connection (optional)
        connection.connectTimeout = 5000 // 5 seconds
        connection.readTimeout = 5000 // 5 seconds

        dataStore = context.createDataStore(name = "isBeta")
        var fetchedBeta = readSettings("isBeta") == "true"

        val inputStream = connection.inputStream
        val reader = BufferedReader(InputStreamReader(inputStream))

        var line: String?
        val content = StringBuilder()

        while (reader.readLine().also { line = it } != null) {
            content.append(line).append("\n")
        }

        reader.close()
        inputStream.close()

        if (content.isEmpty()) {
            // Handle empty content
            return null
        }

        // Parse JSON content
        val jsonObject = JSONObject(content.toString())

        if (jsonObject.length() == 0) {
            // Handle empty JSON object
            return null
        }

        // Extract information
        val versionsObject = jsonObject.getJSONObject("versions")
        var latestVersionObject = versionsObject.getJSONObject(jsonObject.getString("latest"))

        if(fetchedBeta){
            latestVersionObject = versionsObject.getJSONObject(jsonObject.getString("latest_beta"))
        }

        val latestVersion = latestVersionObject.getString("version")
        val downloadLink = latestVersionObject.getString("download")
        val updateDescription = latestVersionObject.getString("updates")
        val forceUpdate = latestVersionObject.getString("force_update")

        // Create a list with the extracted information
        return listOf(latestVersion, downloadLink, updateDescription, fetchedBeta.toString(), forceUpdate)

    } catch (e: IOException) {
        // Handle network or IO errors
        return null
    } catch (e: JSONException) {
        // Handle JSON parsing errors
        return null
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun AssistChipConstructor(text: String, icon: ImageVector) {
    AssistChip(
        onClick = { },
        label = { Text(text) },
        leadingIcon = {
            Icon(
                icon,
                contentDescription = text,
                Modifier.size(AssistChipDefaults.IconSize)
            )
        }
    )
}

suspend fun isInternetLocationReachable(urlString: String): Boolean {
    return try {
        val url = URL(urlString)
        val connection = withContext(Dispatchers.IO) {
            url.openConnection()
        } as HttpURLConnection
        connection.connectTimeout = 3000 // 3 seconds
        connection.requestMethod = "HEAD"

        val responseCode = connection.responseCode

        responseCode in 200..299
    } catch (e: IOException) {
        false
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

@Composable
fun AnimatedBackAnimationScreen() {
    var showShortText by remember { mutableStateOf(true) }

    val transition = updateTransition(targetState = showShortText, label = "showShortTextTransition")

    val alpha by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 500) },
        label = "alphaTransition"
    ) { showShortText ->
        if (showShortText) 1f else 0f
    }

    val padding by transition.animateDp(
        transitionSpec = { tween(durationMillis = 500) },
        label = "paddingTransition"
    ) { showShortText ->
        if (showShortText) 16.dp else 64.dp
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
    ) {
        AnimatedVisibility(
            visible = showShortText,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Text("Short Text", modifier = Modifier.alpha(alpha))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { showShortText = !showShortText }) {
            Text("Toggle Text")
        }
    }
}