package team.baby.guardian.ui

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.preferencesKey
import androidx.navigation.NavController
import team.baby.guardian.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.Charset

suspend fun saveUser(key: String, value: String){
    val dataStoreKey = preferencesKey<String>(key)
    dataStore.edit{ settings ->
        settings[dataStoreKey] = value
    }
}

suspend fun readUser(key: String): String? {
    val dataStoreKey = preferencesKey<String>(key)
    val preferences = dataStore.data.first()
    return preferences[dataStoreKey]
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController) {
    var userLogged by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val temp = readUser("username")
            if (temp != null) {
                userLogged = temp
            }
        }
    }

    Scaffold(
        topBar = {
//            TopAppBar(
//                title = { Text(text = "Settings") },
//                backgroundColor = MaterialTheme.colorScheme.primarySurface
//            )
        },
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                LoginElements(userLogged)
//                Spacer(modifier = Modifier.height(16.dp))
//                AsyncImage(
//                    model = ImageRequest.Builder(LocalContext.current)
//                        .data(R.drawable.ic_launcher_foreground)
//                        .build(),
//                    contentDescription = "baby guardian logo",
//                    modifier = Modifier.fillMaxSize()
//                )
            }
        }
    )
}

@Composable
fun LoginElements(userLogged: String) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var logged by remember {
        mutableStateOf(userLogged)
    }
    if(logged == "null"){ logged = "" }

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = FocusRequester()

    // Define mutable state to control the login flow
    var loginTrigger by remember { mutableStateOf(false) }
    var logoutTrigger by remember { mutableStateOf(false) }

    // Define mutable state to store the login result
    var loginResult by remember { mutableStateOf<List<String>?>(null) }

    var url = "https://weicheng.app/baby_guardian/user.php"

    var snackbarVisible by remember { mutableStateOf(false) }

    var coroutine = rememberCoroutineScope()

    // Use LaunchedEffect to trigger the login attempt when loginTrigger is true
    LaunchedEffect(loginTrigger) {
        try {
            withContext(Dispatchers.IO) {
                if (loginTrigger) {
                    val result = loginUser(url, username, password)
                    // Update the state with the login result
                    loginResult = result
                    // Reset loginTrigger to false after the login attempt
                    loginTrigger = false
                    // Show the Snackbar with the login status message
                    snackbarVisible = true
                    logged = readUser("username").toString()
                }
            }
        } catch (e: Exception) {
            // Handle exceptions if needed
            e.printStackTrace()
        }
    }

    LaunchedEffect(Unit) {
        try {
            withContext(Dispatchers.IO) {
                logged = readUser("username").toString()
                if(logged == "null") {logged = ""}
            }
        } catch (e: Exception) {
            // Handle exceptions if needed
            e.printStackTrace()
        }
    }

    LaunchedEffect(logoutTrigger) {
        try {
            withContext(Dispatchers.IO) {
                if (logoutTrigger) {
                    // Update the state with the login result
                    loginResult = null
                    // Reset loginTrigger to false after the login attempt
                    logoutTrigger = false
                    // Show the Snackbar with the login status message
                    saveUser("username","")
                    saveUser("password","")
                    saveUser("serial_number","")
                    saveUser("device_name","")
                    saveUser("owner_status","")
                    saveUser("mark_enforcing", "")
                    logged = ""
                }
            }
        } catch (e: Exception) {
            // Handle exceptions if needed
            e.printStackTrace()
        }
    }

    // Check the login result and perform UI actions accordingly
//    loginResult?.let {
//        // Login successful, display a message or perform any other actions
//        // You can customize this part based on your application's logic
//        // For now, just printing the message
//        println("Login successful: $it")
//    } ?: run {
//        // Login unsuccessful, handle accordingly
//        // For now, just printing an error message
//        println("Login failed")
//    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() } // This is mandatory
            ) {
                // Hide keyboard and lose focus when clicked outside
                keyboardController?.hide()
                focusManager.clearFocus()
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.title_main),
            style = MaterialTheme.typography.headlineMedium,
            fontFamily = fontFamilyTitle
//                    modifier = Modifier.padding(dimensionResource(R.dimen.padding_small))
        )
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = null,
            modifier = Modifier
                .size(120.dp)
                .clip(MaterialTheme.shapes.medium)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Log.d("aaaaa", logged)
        Log.d("bbbbb", loginResult.toString())
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

        var keyboardController = LocalSoftwareKeyboardController.current
        val context = LocalContext.current

        if(markEnforcing != "true") {
            if (loginResult.toString() != "null" || logged != "") {
                Text(
                    text = logged,
                    style = MaterialTheme.typography.headlineMedium,
                    fontFamily = fontFamilyTitle
                    //                    modifier = Modifier.padding(dimensionResource(R.dimen.padding_small))
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        // Set logoutTrigger to true when the button is clicked
                        logoutTrigger = true

                        // Hide the keyboard after the login attempt
                        keyboardController?.hide()
                    },
                    modifier = Modifier
                        //                    .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = stringResource(R.string.log_out))
                }

                PassKeyBaby(username, password)
            } else {
                OutlinedTextField(
                    value = username,
                    onValueChange = {
                        username = it;
                        coroutine.launch {
                            saveSettings("passkey_username", username)
                        }
                    },
                    label = { Text(text = stringResource(R.string.username)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .clickable {
                            // Hide keyboard and lose focus when clicked outside
                            keyboardController?.hide()
                            focusManager.clearFocus()
                        },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )
                DisposableEffect(context) {
                    onDispose {
                        keyboardController?.hide()
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(text = stringResource(R.string.password)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                        }
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.unregistered_user_will_be_automatically_registered_into_the_system),
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center, // Center the text horizontally
                    fontFamily = fontFamily // Example of specifying a font family
                )

                Spacer(modifier = Modifier.height(16.dp))

                PassKeyBaby(username, password)

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        // Set loginTrigger to true when the button is clicked
                        loginTrigger = true

                        // Hide the keyboard after the login attempt
                        keyboardController?.hide()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = stringResource(R.string.submit))
                }
            }
            // Snackbar to display the login status message
            if (snackbarVisible) {
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        Button(onClick = { snackbarVisible = false }) {
                            Text(stringResource(R.string.dismiss))
                        }
                    }
                ) {
                    loginResult?.let {
                        // Login successful, display a message
                        Text(stringResource(R.string.login_successful))
                    } ?: run {
                        // Login unsuccessful, display an error message
                        Text(stringResource(R.string.login_failed))
                    }
                }
            }
        }else{
            InternetNotConnectedPage(
                stringResource(R.string.software_update_enforcing),
                stringResource(R.string.dear_baby_guardian_you_must_install_this_software_update_to_continue_using_this_app_we_are_enforcing_this_because_we_have_major_updates_about_the_system_architecture_database_etc_and_the_latest_security_updates_please_catch_up_you_have_left_behind_see_you_in_the_next_update),
                Icons.Default.Download
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            textAlign = TextAlign.Center,
            text = stringResource(id = R.string.version) + "." + stringResource(id = R.string.build_type),
            fontFamily = fontFamily
        )

    }
}


suspend fun loginUser(url: String, username: String, password: String): List<String>? {
    if (!isInternetLocationReachable(url)) {
        return null
    }

    return withContext(Dispatchers.IO) {
        try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.connectTimeout = 5000 // 5 seconds
            connection.readTimeout = 5000 // 5 seconds
            connection.doOutput = true

            val postData = "username=${URLEncoder.encode(username, "UTF-8")}&password=${URLEncoder.encode(password, "UTF-8")}"
            val outputStream = connection.outputStream
            outputStream.write(postData.toByteArray(Charset.defaultCharset()))
            outputStream.flush()
            outputStream.close()

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Handle successful login
                val inputStream = connection.inputStream
                val reader = BufferedReader(InputStreamReader(inputStream))
                val content = StringBuilder()

                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    content.append(line).append("\n")
                }

                reader.close()
                inputStream.close()

                val jsonObject = JSONObject(content.toString())

                if (jsonObject.has("success") && jsonObject.getBoolean("success")) {
                    // Login successful
                    val message = jsonObject.getString("message")
                    saveUser("username", username)
                    return@withContext listOf(message)
                } else {
                    // Login unsuccessful, check response reason
                    val reason = jsonObject.optString("reason", "")
                    if (reason == "incorrect_password") {
                        // Incorrect password
                        return@withContext null
                    } else {
                        // Handle other reasons or suspend the user
                        // You can customize this part based on your application's logic
                        // For now, just returning null
                        return@withContext null
                    }
                }
            } else {
                // Handle unsuccessful HTTP response
                return@withContext null
            }

        } catch (e: IOException) {
            // Handle network or IO errors
            return@withContext null
        } catch (e: JSONException) {
            // Handle JSON parsing errors
            return@withContext null
        }
    }
}