package team.baby.guardian.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.Icons.Default
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.extractor.text.webvtt.WebvttCssStyle.FontSizeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONException
import team.baby.guardian.R
import java.io.File
import java.io.IOException


@Composable
fun Controls(deviceSerial: String) {
    var selectedItem by remember { mutableStateOf<String?>(null) }
    var inputValue by remember { mutableStateOf("") }

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = FocusRequester()

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

    var showConfirmationDialog_a by remember { mutableStateOf(false) }
    var selectedItem1 = ""
    if (showConfirmationDialog_a) {
        AlertDialog(
            onDismissRequest = {
                showConfirmationDialog_a = false
            },
            title = {
                Text(stringResource(R.string.device_control_update))
            },
            text = {
                if(selectedItem == null){
                    Text(stringResource(R.string.you_have_not_select_anything_for_change))
                }else{
                    when(selectedItem) {
                        "Humidity Threshold" -> {
                            selectedItem1 = "Humidifier Intensity"
                        }

                        "湿度阈值" -> {
                            selectedItem1 = "Humidifier Intensity"
                        }

                        "Soothing Intensity" -> {
                            selectedItem1 = "Soothing Intensity"
                        }

                        "安抚强度" -> {
                            selectedItem1 = "Soothing Intensity"
                        }

                        "Sound File" -> {
                            selectedItem1 = "Sound File"
                        }

                        "声音文件" -> {
                            selectedItem1 = "Sound File"
                        }

                        "Sound Level" -> {
                            selectedItem1 = "Sound Level"
                        }

                        "音量" -> {
                            selectedItem1 = "Sound Level"
                        }

                        "Negative Emotion Report Frequency" -> {
                            selectedItem1 = "Negative Emotion Report Intensity"
                        }

                        "通知频率" -> {
                            selectedItem1 = "Negative Emotion Report Intensity"
                        }

                        "Command Line" -> {
                            selectedItem1 = "Command"
                        }

                        "命令行" -> {
                            selectedItem1 = "Command"
                        }
                    }

                    Text(
                        stringResource(
                            R.string.update_the_device_with_value,
                            selectedItem!!,
                            inputValue
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmationDialog_a = false
                        if(selectedItem != null){
                            controlDevice(deviceSerial, selectedItem1, inputValue){

                            }
                        }
                    },
                ) {
                    if(selectedItem == null){
                        Text(stringResource(R.string.back))
                    }else{
                        Text(stringResource(R.string.confirm_button))
                    }
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        // Your code to dismiss the dialog
                        showConfirmationDialog_a = false
                    },
                ) {
                    Text(stringResource(R.string.cancel_button))
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() } // This is mandatory
            ) {
                // Hide keyboard and lose focus when clicked outside
                keyboardController?.hide()
                focusManager.clearFocus()
            },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = stringResource(R.string.device_manual_controls), fontFamily = fontFamily, fontSize = 25.sp)

        var items = listOf(
            "Soothing Intensity",
            "Humidity Threshold",
            "Sound File",
            "Sound Level",
            "Negative Emotion Report Frequency",
            "Command Line"
        )

        if(language == "zh"){
            items = listOf(
                "安抚强度",
                "湿度阈值",
                "声音文件",
                "音量",
                "通知频率",
                "命令行"
            )
        }

        // Selectable List
        SelectableList(
            items = items,
            selectedItem = selectedItem,
            onItemSelected = { item ->
                selectedItem = item
            }
        )

        Row (
            horizontalArrangement = Arrangement.Center
        ){
            OutlinedTextField(
                value = inputValue,
                onValueChange = { inputValue = it },
                label = { Text(stringResource(R.string.value_change_to)) },
                leadingIcon = {
                    Icon(
                        imageVector = Default.DataObject,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                modifier = Modifier
//                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Go
                ),
                keyboardActions = KeyboardActions(
                    onNext = {
//                    focusManager.moveFocus(FocusDirection.Down)
                        focusManager.clearFocus()
                    },
                    onDone = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    }
                )
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
        // Button
        Button(
            onClick = { showConfirmationDialog_a = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.submit_1))
        }
    }
}

@Composable
fun SelectableList(
    items: List<String>,
    selectedItem: String?,
    onItemSelected: (String) -> Unit
) {
    Column {
        for(item in items){
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = (item == selectedItem),
                        onClick = { onItemSelected(item) }
                    )
                    .padding(16.dp)
            ) {
                RadioButton(
                    selected = (item == selectedItem),
                    onClick = null
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = item)
            }
        }
    }
}

@Composable
fun RadioButton(
    selected: Boolean,
    onClick: (() -> Unit)?,
) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            .clickable { onClick?.invoke() }
    ) {
        if (selected) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(MaterialTheme.colorScheme.primary)
                    .align(Alignment.Center)
            )
        }
    }
}

fun controlDevice(deviceSerial: String, type: String, content: String, resultCallback: (List<NotificationItem>) -> Unit) {
    val url = "https://weicheng.app/baby_guardian/alert.php"
    val client = OkHttpClient()

    val formBody = FormBody.Builder()
        .add("mode", "insert")
        .add("device_serial", deviceSerial)
        .add("alert", content)
        .add("type", type)
        .add("status", "u2d_sent")
        .add("addition", "")
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
                Log.d("User Controls", responseBody)
            }
        } catch (e: IOException) {
            // Handle network errors
            handler.post {
                resultCallback(emptyList())
            }
        }
    }
}