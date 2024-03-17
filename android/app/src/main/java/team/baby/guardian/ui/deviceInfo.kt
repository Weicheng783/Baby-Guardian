package team.baby.guardian.ui

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import team.baby.guardian.R
import java.net.HttpURLConnection
import java.net.URL
import android.content.Context
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.icu.util.TimeZone
import android.text.format.DateUtils
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PeopleAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import team.baby.guardian.AlertDialogExample
import java.io.IOException
import java.util.Locale

@Composable
fun DeviceListScreen(navController: NavController, username: String) {
    var devices by remember { mutableStateOf<List<Device>>(emptyList()) }
    var referenceName: String? = ""
    var showNew by remember {
        mutableStateOf("")
    }
    var context = LocalContext.current

    DisposableEffect(Unit) {
        val coroutineScope = CoroutineScope(Dispatchers.IO)
        coroutineScope.launch {
            try {
                val url = URL("https://weicheng.app/baby_guardian/device_list.php")
                val connection = url.openConnection() as HttpURLConnection

                // Set the request method to POST
                connection.requestMethod = "POST"

                // Enable input/output streams
                connection.doInput = true
                connection.doOutput = true

                referenceName = readSettings("username")
                // Write POST data
                val postData = "username=$referenceName"
                Log.d("ccc", postData)
                val outputStream = connection.outputStream
                outputStream.write(postData.toByteArray(Charsets.UTF_8))
                outputStream.flush()
                outputStream.close()

                // Read the response
                val inputStream = connection.inputStream
                val response = inputStream.bufferedReader().use { it.readText() }

                withContext(Dispatchers.Main) {
                    devices = parseResponse(response)
                }
            } catch (e: Exception) {
                // Handle error
                e.printStackTrace()
            }
        }

        onDispose {
            // Cleanup or cancel any ongoing work
            coroutineScope.cancel()
        }
    }

    var performHttpRequest_permit by remember { mutableStateOf(false) }
    var url by remember { mutableStateOf("") }
    var keysList by remember { mutableStateOf("username,serial_number,owner_status".split(",").map { it.trim() }) }
    var parametersList by remember { mutableStateOf("".split(",").map { it.trim() }) }

    var requestResult by remember { mutableStateOf("") }
    var friend_username by remember { mutableStateOf("") }

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = FocusRequester()

    var showConfirmationDialog_a by remember { mutableStateOf(false) }
    var showConfirmationDialog_b by remember { mutableStateOf(false) }
    var showConfirmationDialog_c by remember { mutableStateOf(false) }
    var showConfirmationDialog_d by remember { mutableStateOf(false) }
    var showConfirmationDialog_e by remember { mutableStateOf(false) }

    var showText_a by remember { mutableStateOf(false) }
    var showText_b by remember { mutableStateOf(false) }
    var showText_c by remember { mutableStateOf(false) }
    var showText_d by remember { mutableStateOf(false) }

    val buttonColors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.onErrorContainer
    )

    val buttonColors_a = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.tertiary
    )

    LaunchedEffect(performHttpRequest_permit) {
        launch(Dispatchers.IO) {
            requestResult = performHttpPost(url, keysList, parametersList)
            performHttpRequest_permit = false
        }
    }

    var notifications by remember { mutableStateOf<List<NotificationItem>>(emptyList()) }

    if (devices.isEmpty()) {
        // User has no devices
        Spacer(modifier = Modifier.height(16.dp))
        Text(stringResource(R.string.no_devices_found_for_this_user))
        Text(stringResource(R.string.to_register_new_device))
        Text(stringResource(R.string.enter_your_wifi_details_and_update_qr))
        Text(stringResource(R.string.put_product_s_camera_to_the_qr_code))
//        Spacer(modifier = Modifier.height(10.dp))
        DeviceInfo()
//        Spacer(modifier = Modifier.height(10.dp))
    } else {
        // User has devices, display details using Card elements
        Column(
//            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            for(device in devices){
                Card(
                    modifier = Modifier
                        .padding(5.dp)
                        .fillMaxWidth(0.94f)
                        .clickable {
                            showText_a = false
                            showText_b = false
                            showText_c = false
                            showText_d = false
                        }
//                        .fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier,
//                            .padding(16.dp),
//                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = device.modelDescription, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text(text = stringResource(R.string.serial_number) + device.serialNumber)
                        Text(text = stringResource(R.string.activation_date) + device.activationDate)
                        var ownerStatus_translation = ""
                        if(device.ownerStatus == "owner"){
                            ownerStatus_translation = stringResource(id = R.string.owner)
                        }else{
                            ownerStatus_translation = stringResource(id = R.string.friend)
                        }
                        Text(text = stringResource(R.string.you_are_the) + ownerStatus_translation)
                        checkDeviceStatus(device.serialNumber)

                        var deviceSerial = device.serialNumber

                        LaunchedEffect(deviceSerial) {
                            while (true) {
                                // Perform the proactive fetch in a background thread
                                proactiveFetch(deviceSerial, context, device.ownerStatus) {

                                }
                                proactiveFetch_notified(deviceSerial) { fetchedNotifications ->
                                    notifications = fetchedNotifications.reversed().map {
                                        NotificationItem(it.id, it.alert, it.type, it.datetime, it.addition)
                                    }
                                }
                                delay(2000) // Fetch notifications every 5 seconds
                            }
                        }

                        val transition = updateTransition(targetState = showText_a, label = "showShortTextTransition")
                        val alpha by transition.animateFloat(
                            transitionSpec = { tween(durationMillis = 500) },
                            label = "alphaTransition"
                        ) { showText_a ->
                            if (showText_a) 1f else 0f
                        }

                        val padding by transition.animateDp(
                            transitionSpec = { tween(durationMillis = 500) },
                            label = "paddingTransition"
                        ) { showText_a ->
                            if (showText_a) 16.dp else 64.dp
                        }

                        LazyRow (
                            verticalAlignment = Alignment.CenterVertically
                        ){
                            items(1) { item ->
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        // Split keys and parameters strings into lists
                                        if (showText_a) {
                                            showConfirmationDialog_a = !showConfirmationDialog_a
                                        } else {
                                            showText_a = true
                                            showText_b = false
                                            showText_c = false
                                            showText_d = false
                                        }
                                    },
                                    colors = buttonColors,
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.PersonOff,
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    AnimatedVisibility(
                                        visible = showText_a,
                                        enter = fadeIn(),
                                        exit = fadeOut()
                                    ) {
                                        if (showText_a) {
                                            if(device.ownerStatus == "owner"){
                                                Spacer(modifier = Modifier.width(8.dp))
                                            }else{
                                                Spacer(modifier = Modifier.width(20.dp))
                                            }

                                            if (device.ownerStatus == "owner") {
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    stringResource(R.string.remove_all_shared_friends),
                                                    modifier = Modifier.alpha(alpha)
                                                )
                                            } else {
                                                Text(
                                                    stringResource(R.string.remove_this_shared_device),
                                                    modifier = Modifier.alpha(alpha)
                                                )
                                            }
                                        }
                                    }
                                }

                                if(device.ownerStatus == "owner"){
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        if (device.ownerStatus == "owner") {
//                                Spacer(modifier = Modifier.width(10.dp))
                                            Button(
                                                onClick = {
                                                    if(showText_b){
                                                        showConfirmationDialog_b = !showConfirmationDialog_b
                                                    }else{
                                                        showText_b = true
                                                        showText_a = false
                                                        showText_c = false
                                                        showText_d = false
                                                    }
                                                },
                                                colors = buttonColors,
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.RemoveCircleOutline,
                                                    contentDescription = null,
//                                        modifier = Modifier.size(24.dp)
                                                )
                                                if(showText_b){
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(stringResource(R.string.remove_this_device))
                                                }
                                            }
                                        }

                                        if (device.ownerStatus == "owner"){
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Button(
                                                onClick = {
                                                    if(showText_c){
                                                        showConfirmationDialog_c = !showConfirmationDialog_c
                                                    }else{
                                                        showText_b = false
                                                        showText_a = false
                                                        showText_c = true
                                                        showText_d = false
                                                    }
                                                },
                                                colors = buttonColors_a,
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.PeopleAlt,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(24.dp)
                                                )
//                                            Spacer(modifier = Modifier.width(10.dp))
                                                if(showText_c){
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(stringResource(R.string.add_a_friend))
                                                }
                                            }

                                            Spacer(modifier = Modifier.width(10.dp))

                                            Button(
                                                onClick = {
                                                    // Split keys and parameters strings into lists
                                                    if (showText_d) {
                                                        showConfirmationDialog_e = !showConfirmationDialog_e
                                                    } else {
                                                        showText_a = false
                                                        showText_b = false
                                                        showText_c = false
                                                        showText_d = true
                                                    }
                                                },
                                                colors = buttonColors,
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.RestartAlt,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                                AnimatedVisibility(
                                                    visible = showText_d,
                                                    enter = fadeIn(),
                                                    exit = fadeOut()
                                                ) {
                                                    if (showText_d) {
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(
                                                            stringResource(R.string.restart_device),
//                                                        modifier = Modifier.alpha(alpha)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                            }
                        }

                        if (showConfirmationDialog_a) {
                            AlertDialog(
                                onDismissRequest = {
                                    showConfirmationDialog_a = false
                                },
                                title = {
                                    if(device.ownerStatus == "owner"){
                                        Text(stringResource(R.string.friends_reset))
                                    }else{
                                        Text(stringResource(R.string.remove_shared_device))
                                    }
                                },
                                text = {
                                    if(device.ownerStatus == "owner"){
                                        Text(stringResource(R.string.are_you_sure_you_want_to_reset_all_friends_remove_all_shared_friends))
                                    }else{
                                        Text(stringResource(R.string.are_you_sure_you_want_to_remove_this_shared_device_from_your_account))
                                    }
                                },
                                confirmButton = {
                                    Button(
                                        onClick = {
                                            // Your code to confirm the action
                                            showConfirmationDialog_a = false
                                            url = "https://weicheng.app/baby_guardian/deregister.php"
                                            keysList = "username,serial_number,owner_status".split(",").map { it.trim() }
                                            parametersList = "$referenceName,${device.serialNumber},friend".split(",").map { it.trim() }

                                            // Perform HTTP POST request
                                            performHttpRequest_permit = true
                                        },
                                        colors = buttonColors,
                                    ) {
                                        Text(stringResource(R.string.confirm_button))
                                    }
                                },
                                dismissButton = {
                                    Button(
                                        onClick = {
                                            // Your code to dismiss the dialog
                                            showConfirmationDialog_a = false
                                        },
                                        colors = buttonColors,
                                    ) {
                                        Text(stringResource(R.string.cancel_button))
                                    }
                                }
                            )
                        }

                        if (showConfirmationDialog_b) {
                            AlertDialog(
                                onDismissRequest = {
                                    showConfirmationDialog_b = false
                                },
                                title = {
                                    Text(stringResource(R.string.device_removal))
                                },
                                text = {
                                    Text(stringResource(R.string.are_you_sure_you_want_to_remove_this_device_from_your_account))
                                },
                                confirmButton = {
                                    Button(
                                        onClick = {
                                            showConfirmationDialog_b = false
                                            url = "https://weicheng.app/baby_guardian/deregister.php"
                                            keysList = "username,serial_number".split(",").map { it.trim() }
                                            parametersList = "$referenceName,${device.serialNumber}".split(",").map { it.trim() }

                                            // Perform HTTP POST request
                                            performHttpRequest_permit = true
                                        },
                                        colors = buttonColors,
                                    ) {
                                        Text(stringResource(R.string.confirm_button))
                                    }
                                },
                                dismissButton = {
                                    Button(
                                        onClick = {
                                            // Your code to dismiss the dialog
                                            showConfirmationDialog_b = false
                                        },
                                        colors = buttonColors,
                                    ) {
                                        Text(stringResource(R.string.cancel_button))
                                    }
                                }
                            )
                        }

                        if (showConfirmationDialog_c) {
                            AlertDialog(
                                onDismissRequest = {
                                    showConfirmationDialog_c = false
                                    showText_c = false
                                },
                                title = {
                                    Text(stringResource(R.string.friend_sharing))
                                },
                                text = {
                                    Text(
                                        stringResource(
                                            R.string.are_you_sure_you_want_to_share_this_device_with,
                                            friend_username
                                        ))
                                },
                                confirmButton = {
                                    Button(
                                        onClick = {
                                            // Split keys and parameters strings into lists
                                            url = "https://weicheng.app/baby_guardian/register.php"
                                            keysList = "username,serial_number,owner_status".split(",").map { it.trim() }
                                            parametersList = "$friend_username,${device.serialNumber},friend".split(",").map { it.trim() }

                                            // Perform HTTP POST request
                                            performHttpRequest_permit = true
                                            showConfirmationDialog_c = false
                                            showText_c = false
                                            showConfirmationDialog_d = true
                                        },
//                                        colors = buttonColors,
                                    ) {
                                        Text(stringResource(R.string.confirm_button))
                                    }
                                },
                                dismissButton = {
                                    Button(
                                        onClick = {
                                            // Your code to dismiss the dialog
                                            showConfirmationDialog_c = false
                                            showText_c = false
                                        },
//                                        colors = buttonColors,
                                    ) {
                                        Text(stringResource(R.string.cancel_button))
                                    }
                                }
                            )
                        }

                        if (showConfirmationDialog_d) {
                            AlertDialog(
                                onDismissRequest = {
                                    showConfirmationDialog_d = false
                                    showText_c = false
                                },
                                title = {
                                    Text(stringResource(R.string.friend_invitation_sent))
                                },
                                text = {
                                    Text(stringResource(R.string.your_friend_sharing_invitation_has_been_sent_yes))
                                },
                                confirmButton = {
                                    Button(
                                        onClick = {
                                            showConfirmationDialog_d = false
                                            showText_c = false
                                        },
                                        colors = buttonColors,
                                    ) {
                                        Text(stringResource(R.string.okay_nice))
                                    }
                                },
                                dismissButton = {
//                                    Button(
//                                        onClick = {
//                                            // Your code to dismiss the dialog
//                                            showConfirmationDialog_a = false
//                                        },
//                                        colors = buttonColors,
//                                    ) {
//                                        Text("Okay")
//                                    }
                                }
                            )
                        }

                        if (showConfirmationDialog_e) {
                            AlertDialog(
                                onDismissRequest = {
                                    showConfirmationDialog_e = false
                                },
                                title = {
                                    Text(stringResource(R.string.restart_device_note))
                                },
                                text = {
                                    Text(stringResource(R.string.are_you_sure_you_want_to_restart_the_device_we_are_strongly_advise_you_check_if_there_any_support_running_on_that_device_and_device_restart_can_cause_sudden_drop_in_service_but_if_you_are_experiencing_any_continuous_delay_or_no_result_from_sensors_restart_may_help))
                                },
                                confirmButton = {
                                    Button(
                                        onClick = {
                                            // Code to confirm the action
                                            showConfirmationDialog_e = false
                                            url = "https://weicheng.app/baby_guardian/alert.php"
                                            keysList = "mode,device_serial,alert,type,status,addition".split(",").map { it.trim() }
                                            parametersList = "insert,${device.serialNumber},reboot,reboot,u2d_sent,".split(",").map { it.trim() }

                                            // Perform HTTP POST request
                                            performHttpRequest_permit = true
                                        },
                                        colors = buttonColors,
                                    ) {
                                        Text(stringResource(R.string.confirm_button))
                                    }
                                },
                                dismissButton = {
                                    Button(
                                        onClick = {
                                            // Your code to dismiss the dialog
                                            showConfirmationDialog_e = false
                                        },
                                        colors = buttonColors,
                                    ) {
                                        Text(stringResource(R.string.cancel_button))
                                    }
                                }
                            )
                        }

                        if(device.ownerStatus == "owner" && showText_c){
                            Row (
                                horizontalArrangement = Arrangement.Center
                            ){
                                OutlinedTextField(
                                    value = friend_username,
                                    onValueChange = { friend_username = it },
                                    label = { Text(text = stringResource(R.string.friend_user_name)) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.People,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    modifier = Modifier
//                                    .fillMaxWidth()
                                        .focusRequester(focusRequester),
                                    keyboardOptions = KeyboardOptions.Default.copy(
                                        imeAction = ImeAction.Next
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        ClickableDeviceLatestPhoto(navController, device.serialNumber, device.modelDescription, device.ownerStatus, context)
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }

        // showNew means give user an option to register a new device
        if(showNew == ""){
            Button(
                onClick = {
                    showNew = "true"
                },
                modifier = Modifier
//                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.add_a_new_device_or_configure_wifi))
            }
        }else{
            if(showNew == "true"){
                Button(
                    onClick = {
                        showNew = ""
                    },
                    modifier = Modifier
//                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowUp,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.hide_configuration))
                }
            }
            DeviceInfo()
        }
    }
}

data class Device(
    val serialNumber: String,
    val modelDescription: String,
    val activationDate: String,
    val ownerStatus: String
)
fun parseResponse(response: String): List<Device> {
    val devices = mutableListOf<Device>()

    try {
        // Check if the response is a JSON object
        if (response.startsWith("{")) {
            val jsonObject = JSONObject(response)
            val message = jsonObject.getString("message")

            // Handle the message or throw an exception if needed
            if (message == "No devices found for the user.") {
                return devices
            } else {
                throw JSONException("Unexpected JSON object structure")
            }
        }

        // Assume the response is a JSON array
        val jsonArray = JSONArray(response)
        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            val serialNumber = jsonObject.getString("serial_number")
            val modelDescription = jsonObject.getString("model_description")
            val activationDate = jsonObject.getString("activation_date")
            val ownerStatus = jsonObject.getString("owner_status")

            devices.add(Device(serialNumber, modelDescription, activationDate, ownerStatus))
        }
    } catch (e: JSONException) {
        e.printStackTrace()
    }

    return devices
}

//https://gist.github.com/dev-niiaddy/8f936062291e3d328c7d10bb644273d0
@Composable
fun rememberQrBitmapPainter(
    content: String,
    size: Dp = 150.dp,
    padding: Dp = 0.dp
): BitmapPainter {

    val density = LocalDensity.current
    val sizePx = with(density) { size.roundToPx() }
    val paddingPx = with(density) { padding.roundToPx() }

    var bitmap by remember(content) {
        mutableStateOf<Bitmap?>(null)
    }

    LaunchedEffect(bitmap) {
        if (bitmap != null) return@LaunchedEffect

        launch(Dispatchers.IO) {
            val qrCodeWriter = QRCodeWriter()

            val encodeHints = mutableMapOf<EncodeHintType, Any?>()
                .apply {
                    this[EncodeHintType.MARGIN] = paddingPx
                }

            val bitmapMatrix = try {
                qrCodeWriter.encode(
                    content, BarcodeFormat.QR_CODE,
                    sizePx, sizePx, encodeHints
                )
            } catch (ex: WriterException) {
                null
            }

            val matrixWidth = bitmapMatrix?.width ?: sizePx
            val matrixHeight = bitmapMatrix?.height ?: sizePx

            val newBitmap = Bitmap.createBitmap(
                bitmapMatrix?.width ?: sizePx,
                bitmapMatrix?.height ?: sizePx,
                Bitmap.Config.ARGB_8888,
            )

            val pixels = IntArray(matrixWidth * matrixHeight)

            for (x in 0 until matrixWidth) {
                for (y in 0 until matrixHeight) {
                    val shouldColorPixel = bitmapMatrix?.get(x, y) ?: false
                    val pixelColor = if (shouldColorPixel) Color.BLACK else Color.WHITE

                    pixels[y * matrixWidth + x] = pixelColor
                }
            }

            newBitmap.setPixels(pixels, 0, matrixWidth, 0, 0, matrixWidth, matrixHeight)

            bitmap = newBitmap
        }
    }

    return remember(bitmap) {
        val currentBitmap = bitmap ?: Bitmap.createBitmap(
            sizePx, sizePx,
            Bitmap.Config.ARGB_8888,
        ).apply { eraseColor(Color.TRANSPARENT) }

        BitmapPainter(currentBitmap.asImageBitmap())
    }
}

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun DeviceInfo(){
    // Column with the UI components
    val softwareVer = stringResource(id = R.string.version) + "." + stringResource(id = R.string.build_type)
    var ssid by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("ssid=$ssid,password=$password,username=,userLanguage=,softwareVer=$softwareVer") }
    var showQR by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()
    coroutineScope.launch {
        if(readSettings("ssid") != null){
            ssid = readSettings("ssid").toString()
        }
        if(readSettings("ssid_password") != null){
            password = readSettings("ssid_password").toString()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize(),
//            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Input fields for WiFi SSID and Password
            OutlinedTextField(
                value = ssid,
                onValueChange = {
                    ssid = it;
                    coroutineScope.launch {
                        saveSettings("ssid", ssid)
                    }
                },
                label = { Text(stringResource(R.string.wifi_ssid)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it;
                    coroutineScope.launch {
                        saveSettings("ssid_password", password)
                    }
                },
                label = { Text(stringResource(R.string.wifi_password)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        // Hide keyboard on Done
                        LocalSoftwareKeyboardController
                    }
                )
            )

        var referenceName by remember { mutableStateOf("") }
        var selectedLanguage by remember { mutableStateOf(supportedLanguages.first()) }
        LaunchedEffect(Unit) {
            try {
                withContext(Dispatchers.IO) {
                    referenceName = readUser("username").toString()
                    if(referenceName == "null") {referenceName = ""}
                }

                withContext(Dispatchers.IO) {
                    val temp = readSettings("language")
                    if(temp != null){
                        if(temp == "en"){
                            selectedLanguage = Language("en", "English/英文")
                        }else{
                            selectedLanguage = Language("zh", "Chinese (Simplified)/简体中文")
                        }
                    }
                }
            } catch (e: Exception) {
                // Handle exceptions if needed
                e.printStackTrace()
            }
        }

        // Button to change the image on click
        if(showQR == ""){
            Button(
                onClick = {
                    // Update image URL based on input fields
                    imageUrl = "ssid=$ssid,password=$password,username=$referenceName,userLanguage=${selectedLanguage.code},softwareVer=$softwareVer"
                    showQR = "true"
                    coroutineScope.launch {
                        ssid?.let { saveSettings("ssid", it) }
                        password?.let { saveSettings("ssid_password", it) }
                    }
                },
                modifier = Modifier
//                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.QrCode2,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.show_qr_code))
            }
        }else{
            Button(
                onClick = {
                    imageUrl = "ssid=$ssid,password=$password,username=$referenceName,userLanguage=${selectedLanguage.code},softwareVer=$softwareVer"
                    coroutineScope.launch {
                        ssid?.let { saveSettings("ssid", it) }
                        password?.let { saveSettings("ssid_password", it) }
                    }
                },
                modifier = Modifier
//                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.QrCode2,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.update_qr_code))
            }
            Button(
                onClick = {
                    showQR = ""
                },
                modifier = Modifier
//                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowUp,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.hide_qr_code))
            }
            Spacer(modifier = Modifier.height(16.dp))
            // Display the image with the updated URL
            UpdateBrightness()
            Image(
                painter = rememberQrBitmapPainter(imageUrl),
                contentDescription = "QR Code for Registration",
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.size(300.dp),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Column (
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(stringResource(R.string.please_keep_on_full_brightness_and_place_your_phone_parallel_to_the_device_until_you_hear_a_medium_pitched_beep), fontFamily = fontFamilyTitle, textAlign = TextAlign.Center)
            }
        }

    }
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
    if (showDialog){
        AlertDialogExample(
            onDismissRequest = { showDialog = false },
            onConfirmation = { showDialog = false },
            dialogTitle = stringResource(R.string.device_offline),
            dialogText = stringResource(R.string.your_device_is_currently_offline_power_on_the_device_for_connecting_to_a_nearby_wifi_access_point_if_the_issue_persists_reconfigure_the_wifi_by_showing_the_qr_code_to_the_target_device_s_camera_your_device_is_capable_for_auto_connecting_the_correct_wifi_once_the_correct_qr_is_being_detected),
            icon = Icons.Default.CloudOff
        )
    }

    deviceStatus = calculateDeviceStatus(modifiedDate)

    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
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

        Text(
            textAlign = TextAlign.Center,
            fontSize = 13.sp,
            text = stringResource(R.string.last_seen, modifiedDate),
            fontFamily = fontFamilyTitle,
            fontWeight = FontWeight.Bold
        )
        val timeDifference = calculateTimeDifference_cleartext(modifiedDate, LocalContext.current)
        Text(
            text = timeDifference,
            fontFamily = fontFamilyTitle
        )
        // Display the device status with appropriate AssistChip icon
        deviceStatus?.let { status ->
            AssistChip(
//                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    when (status) {
                        DeviceStatus.ONLINE -> Icon(Icons.Default.CheckCircle, contentDescription = null)
                        DeviceStatus.HIGH_LATENCY -> Icon(Icons.Default.Info, contentDescription = null)
                        DeviceStatus.MAY_BE_OFFLINE -> Icon(Icons.Default.Warning, contentDescription = null)
                        DeviceStatus.OFFLINE -> Icon(Icons.Outlined.CloudOff, contentDescription = null)
                    }
                },
                label = {
                    when(status.label){
                        "Device Online" -> {
                            Text(stringResource(R.string.device_online), fontFamily = fontFamilyTitle)
                        }
                        "High Latency" -> {
                            Text(stringResource(R.string.high_latency), fontFamily = fontFamilyTitle)
                        }
                        "Device May be Offline" -> {
                            Text(stringResource(R.string.device_may_be_offline), fontFamily = fontFamilyTitle)
                        }
                        "Device Offline" -> {
                            Text(stringResource(R.string.device_offline), fontFamily = fontFamilyTitle)
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
    OFFLINE("Device Offline", Icons.Outlined.CloudOff)
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