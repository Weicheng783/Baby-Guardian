package team.baby.guardian.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon.createWithResource
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.AlertDialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Snackbar
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.credentials.CreatePasswordRequest
import androidx.credentials.CreatePasswordResponse
import androidx.credentials.CreatePublicKeyCredentialRequest
import androidx.credentials.CreatePublicKeyCredentialResponse
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetPasswordOption
import androidx.credentials.GetPublicKeyCredentialOption
import androidx.credentials.PasswordCredential
import androidx.credentials.PublicKeyCredential
import androidx.credentials.exceptions.CreateCredentialCancellationException
import androidx.credentials.exceptions.CreateCredentialCustomException
import androidx.credentials.exceptions.CreateCredentialException
import androidx.credentials.exceptions.CreateCredentialInterruptedException
import androidx.credentials.exceptions.CreateCredentialProviderConfigurationException
import androidx.credentials.exceptions.CreateCredentialUnknownException
import androidx.credentials.exceptions.publickeycredential.CreatePublicKeyCredentialDomException
import kotlinx.coroutines.Dispatchers
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
import org.json.JSONObject
import team.baby.guardian.AlertDialogExample
import team.baby.guardian.MainActivity
import team.baby.guardian.R
import team.baby.guardian.passkeys.DataProvider
import team.baby.guardian.passkeys.readFromAsset
import java.security.SecureRandom
import java.util.Vector

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun PassKeyBaby(username: String, password: String) {
    var context = LocalContext.current
    var credentialManager = CredentialManager.create(context)
    DataProvider.initSharedPref(context)

    var user_name by remember { mutableStateOf("") }
    var regResponse by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    coroutineScope.launch {
        user_name = if(readSettings("passkey_username") == null){
            ""
        }else{
            readSettings("passkey_username").toString()
        }

        regResponse = if(readSettings("passkey_regResponse") == null){
            ""
        }else{
            readSettings("passkey_regResponse").toString()
        }
    }

    // State to track whether the sign-in process is ongoing
    var isSignInInProgress by remember { mutableStateOf(false) }
    var isSignUpInProgress by remember { mutableStateOf(false) }
    var isCreationInProgress by remember { mutableStateOf(false) }
    var isSignUpPasskeyInProgress by remember { mutableStateOf(false) }

    var responseText by remember { mutableStateOf("") }
    var cookies by remember { mutableStateOf<List<Cookie>>(emptyList()) }

    var errorMsg by remember { mutableStateOf("") }
    var isShowAlertDialog by remember { mutableStateOf(false) }
    var snackbarVisible by remember { mutableStateOf(false) }
    var snackbarVisibleA by remember { mutableStateOf(false) }
    var snackbarVisibleB by remember { mutableStateOf(false) }
    var loginResult by remember { mutableStateOf(false) }

    var signedUserName by remember { mutableStateOf("") }
    LaunchedEffect(Unit){
        if(readSettings("username")!=null){
            signedUserName = readSettings("username")!!
        }
    }

    if(isShowAlertDialog) {
        AlertDialogExample(
            onDismissRequest = { isShowAlertDialog = false },
            onConfirmation = { isShowAlertDialog = false },
            dialogTitle = "Passkey",
            dialogText = "Ahhh, a problem happens: "+errorMsg+" , please try again.",
            icon = ImageVector.vectorResource(id = R.drawable.passkey),
        )
    }

    // Use LaunchedEffect to launch a coroutine when the composable is first displayed
    LaunchedEffect(isSignInInProgress) {
        // This coroutine will be canceled when the composable is removed from the screen
        if(isSignInInProgress){
            var re = signInUser(credentialManager, context, cookies) {
                isSignInInProgress = it
            }
            if(re){
                loginResult = true
                snackbarVisible = true
            }else{
                loginResult = false
                snackbarVisible = true
            }
        }
    }

    LaunchedEffect(isSignUpInProgress) {
        // This coroutine will be canceled when the composable is removed from the screen
        if(isSignUpInProgress){
            errorMsg = signUpWithPassword(credentialManager, context, username, password)!!
            isSignUpInProgress = false
            if(errorMsg != ""){
                loginResult = false
                snackbarVisible = true
            }
        }
    }

    LaunchedEffect(isCreationInProgress) {
        // This coroutine will be canceled when the composable is removed from the screen
        if(isCreationInProgress){
            try {
                user_name = readSettings("username")!!
                val url1 = "https://plausible-dapper-justice.glitch.me/auth/username"
                val headers1 = mapOf("X-Requested-With" to "XMLHttpRequest")
                val body1 = mapOf("username" to user_name)
                val response1 = makeHttpPostRequestWithCookies(url1, headers1, body1, cookies)
                if (response1 != null) {
                    responseText = response1.body?.string() ?: ""
                }
                cookies = response1?.let { extractCookiesFromResponse(it) }!!

                val url2 = "https://plausible-dapper-justice.glitch.me/auth/password"
                val headers2 = mapOf("X-Requested-With" to "XMLHttpRequest")
                val body2 = mapOf("username" to user_name, "password" to "a")
                val response2 = makeHttpPostRequestWithCookies(url2, headers2, body2, cookies)
                if (response2 != null) {
                    responseText = response2.body?.string() ?: ""
                }

                val url3 = "https://plausible-dapper-justice.glitch.me/auth/registerRequest"
                val headers3 = mapOf("X-Requested-With" to "XMLHttpRequest")
                val body3 = emptyMap<String, String>()
                val response3 = makeHttpPostRequestWithCookies(url3, headers3, body3, cookies)
                if (response3 != null) {
                    responseText = response3.body?.string() ?: ""
                    Log.d("RESPONSE3", responseText)
                    saveSettings("passkey_regResponse", responseText)
                }

                regResponse = if(readSettings("passkey_regResponse") == null){
                    ""
                }else{
                    readSettings("passkey_regResponse").toString()
                }
                signUpWithPasskeys(credentialManager, context, username, regResponse)

                val url4 = "https://plausible-dapper-justice.glitch.me/auth/registerResponse"
                val headers4 = mapOf("X-Requested-With" to "XMLHttpRequest")
                val body4 = readSettings("passkey_regResponse_feedback")!!
                val response4 = makeHttpPostRequestWithCookiesA(url4, headers4, body4, cookies)
                Log.d("body4", body4)
                Log.d("Response4", response4.toString())
                if (response4 != null) {
                    responseText = response4.body?.string() ?: ""
                    if(parseIdFromJson(responseText) != ""){
                        saveUser("username", parseIdFromJson(responseText))
                        loginResult = true
                        snackbarVisibleB = true
                    }else{
                        loginResult = false
                        snackbarVisibleB = true
                    }
                }else{
                    loginResult = false
                    snackbarVisibleB = true
                }

            }catch (e: Exception){
                Log.d("EEE",e.message.toString())
                errorMsg = e.message.toString()+". Always Double Check Duplicate Passkeys"
                isShowAlertDialog = true
            }

            isCreationInProgress = false
        }
    }

    LaunchedEffect(isSignUpPasskeyInProgress) {
        // This coroutine will be canceled when the composable is removed from the screen
        if(isSignUpPasskeyInProgress){
            try {
                user_name = readSettings("passkey_username")!!
                val url1 = "https://plausible-dapper-justice.glitch.me/auth/username"
                val headers1 = mapOf("X-Requested-With" to "XMLHttpRequest")
                val body1 = mapOf("username" to user_name)
                val response1 = makeHttpPostRequestWithCookies(url1, headers1, body1, cookies)
                if (response1 != null) {
                    responseText = response1.body?.string() ?: ""
                }
                cookies = response1?.let { extractCookiesFromResponse(it) }!!

                val url2 = "https://plausible-dapper-justice.glitch.me/auth/password"
                val headers2 = mapOf("X-Requested-With" to "XMLHttpRequest")
                val body2 = mapOf("username" to user_name, "password" to "a")
                val response2 = makeHttpPostRequestWithCookies(url2, headers2, body2, cookies)
                if (response2 != null) {
                    responseText = response2.body?.string() ?: ""
                }

                val url3 = "https://plausible-dapper-justice.glitch.me/auth/registerRequest"
                val headers3 = mapOf("X-Requested-With" to "XMLHttpRequest")
                val body3 = emptyMap<String, String>()
                val response3 = makeHttpPostRequestWithCookies(url3, headers3, body3, cookies)
                if (response3 != null) {
                    responseText = response3.body?.string() ?: ""
                    Log.d("RESPONSE3", responseText)
                    saveSettings("passkey_regResponse", responseText)
                }

                regResponse = if(readSettings("passkey_regResponse") == null){
                    ""
                }else{
                    readSettings("passkey_regResponse").toString()
                }
                signUpWithPasskeys(credentialManager, context, username, regResponse)

                val url4 = "https://plausible-dapper-justice.glitch.me/auth/registerResponse"
                val headers4 = mapOf("X-Requested-With" to "XMLHttpRequest")
                val body4 = readSettings("passkey_regResponse_feedback")!!
                val response4 = makeHttpPostRequestWithCookiesA(url4, headers4, body4, cookies)
                Log.d("body4", body4)
                Log.d("Response4", response4.toString())
                if (response4 != null) {
                    responseText = response4.body?.string() ?: ""
                    if(parseIdFromJson(responseText) != ""){
                        saveUser("username", parseIdFromJson(responseText))
                        loginResult = true
                        snackbarVisibleA = true
                    }else{
                        loginResult = false
                        snackbarVisibleA = true
                    }
                }else{
                    loginResult = false
                    snackbarVisibleA = true
                }

            }catch (e: Exception){
                Log.d("EEE",e.message.toString())
                errorMsg = e.message.toString()+". Always Double Check Duplicate Passkeys"
                isShowAlertDialog = true
            }

            isSignUpPasskeyInProgress = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Add UI components here as needed

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
                if(loginResult) {
                    // Login successful, display a message
                    Text(stringResource(R.string.login_successful))
                    context.startActivity(Intent(context, MainActivity::class.java))
                    (context as MainActivity).finish()
                }else{
                    // Login unsuccessful, display an error message
                    Text(stringResource(R.string.login_failed))
                }
            }
        }

        if (snackbarVisibleA) {
            Snackbar(
                modifier = Modifier.padding(16.dp),
                action = {
                    Button(onClick = { snackbarVisibleA = false }) {
                        Text(stringResource(R.string.dismiss))
                    }
                }
            ) {
                if(loginResult) {
                    Text(stringResource(R.string.registration_via_passkey_successful))
                }else{
                    Text(stringResource(R.string.login_failed))
                }
            }
        }

        if (snackbarVisibleB) {
            Snackbar(
                modifier = Modifier.padding(16.dp),
                action = {
                    Button(onClick = { snackbarVisibleB = false }) {
                        Text(stringResource(R.string.dismiss))
                    }
                }
            ) {
                if(loginResult) {
                    Text(stringResource(R.string.registration_via_passkey_successful))
                }else{
                    Text(stringResource(R.string.registration_failed_one_user_can_only_have_one_passkey_if_this_is_an_error_please_try_again))
                }
            }
        }

        if(signedUserName == "") {
            // Sign-in button
            Button(
                onClick = {
                    if (!isSignInInProgress) {
                        // Start the sign-in process
                        isSignInInProgress = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isSignInInProgress
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.passkey),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = stringResource(R.string.sign_in_with_saved_methods))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sign-up button
            //        Button(
            //            onClick = {
            //                if (!isSignUpInProgress) {
            //                    // Start the sign-up process
            //                    isSignUpInProgress = true
            //                }
            //            },
            //            enabled = !isSignUpInProgress
            //        ) {
            //            Text(text = "Sign Up")
            //        }

            //        Spacer(modifier = Modifier.height(16.dp))

            // Create Passkey button
            Button(
                onClick = {
                    if (!isSignUpPasskeyInProgress) {
                        // Start the sign-up process
                        isSignUpPasskeyInProgress = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isSignUpPasskeyInProgress
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.passkey),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = stringResource(R.string.sign_up_with_passkey))
            }

//            Spacer(modifier = Modifier.height(16.dp))
        }

        if(signedUserName != ""){
            Button(
                onClick = {
                    if (!isCreationInProgress) {
                        // Start the sign-up process
                        isCreationInProgress = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isCreationInProgress
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.passkey),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = stringResource(R.string.create_passkey))
            }
        }
    }
}

private suspend fun signInUser(credentialManager: CredentialManager, context: Context, cookies: List<Cookie>, updateSignInStatus: (Boolean) -> Unit): Boolean {
    try {
        // Use either sign up or sign in based on your logic
        // For demonstration, I'm using signUpWithPassword here
        val res = signInWithSavedCredentials(credentialManager, context, cookies)
        return res
//        simulateServerDelayAndLogIn()
        // Perform any additional logic or UI updates after successful sign-in
    } catch (e: Exception) {
        Log.d("SIGNINUSER", e.toString())
        // Handle sign-in failure and update UI accordingly
        // For example, show a toast or error message
    } finally {
        // Reset the sign-in in progress state
        updateSignInStatus(false)
    }
    return false
}

private suspend fun signUpWithPasskeys(credentialManager: CredentialManager, context: Context, username: String, regResponse: String){
    val data = createPasskey(credentialManager, context, username, regResponse)
    if (data != null) {
        Log.d("PASSKEY SIGN UP", data.data.toString())
    }
    data?.let {
        DataProvider.setSignedInThroughPasskeys(true)
    }
}

fun parseIdFromJson(jsonString: String): String {
    try {
        val jsonObject = JSONObject(jsonString)

        // Check if the JSON contains the "error" field
        if (jsonObject.has("error")) {
            // Handle the error case if needed
            val errorMessage = jsonObject.optString("error", "Unknown error")
            // Handle the error message as per your application's requirements
//            println("Error: $errorMessage")
        } else {
            // Extract the "id" field if it exists
            val id = jsonObject.optString("username", "")
            if (id.isNotEmpty()) {
                // Successfully extracted the "id" field
                return id
            } else {
                // Handle the case where "id" is not present in the JSON
//                println("ID not found in JSON")
            }
        }
    } catch (e: Exception) {
        // Handle JSON parsing exception if necessary
//        println("JSON Parsing Error: ${e.message}")
    }

    // Return an empty string or handle the default case as needed
    return ""
}

private suspend fun signUpWithPassword(credentialManager: CredentialManager, context: Context, username: String, password: String): String? {
    return createPassword(credentialManager, context, username, password)
//    simulateServerDelayAndLogIn()
}

private suspend fun signInWithSavedCredentials(credentialManager: CredentialManager, context: Context, cookies: List<Cookie>): Boolean {
    val data = getSavedCredentials(credentialManager, context, cookies)
    val cookiesA = extractCookiesFromResponse(data.second)
    var res = false
    data?.let {
        data.first?.let { it1 -> Log.d("SAVED SIGNIN", it1) }
        it.first?.let { it1 ->
            res = sendSignInResponseToServer(it1, cookiesA)
        }
    }
    return res
}

suspend fun sendSignInResponseToServer(data: String, cookies: List<Cookie>): Boolean {
    val url = "https://plausible-dapper-justice.glitch.me/auth/signinResponse"
    val headers = mapOf("X-Requested-With" to "XMLHttpRequest")
    val body = data
    val response = makeHttpPostRequestWithCookiesA(url, headers, body, cookies)
    if (response != null) {
        val responseText = response.body?.string() ?: ""
        Log.d("RESPONSE LOGIN FINAL", responseText)
        saveSettings("passkey_AuthRes", responseText)
        val id = parseIdFromJson(responseText)
        if(id != ""){
            saveUser("username", id)
            return true
        }
    }
    if (response != null) {
//        return response.body.toString()
    }
    return false
}

private suspend fun createPassword(credentialManager: CredentialManager, context: Context, username: String, password: String): String? {
    val request = CreatePasswordRequest(
        username,
        password
    )
    try {
        credentialManager.createCredential(context, request) as CreatePasswordResponse
    } catch (e: Exception) {
        Log.e("Auth", "createPassword failed with exception: " + e.message)
        return e.message
    }
    return ""
}

private suspend fun createPasskey(credentialManager: CredentialManager, context: Context, username: String, regResponse: String): CreatePublicKeyCredentialResponse? {
    Log.d("passkey_regResponse_PRECHECK", regResponse)
    val request = CreatePublicKeyCredentialRequest(fetchRegistrationJsonFromServer(context, username, regResponse))
    var response: CreatePublicKeyCredentialResponse? = null
    try {
        response = credentialManager.createCredential(
            context,
            request
        ) as CreatePublicKeyCredentialResponse
    } catch (e: CreateCredentialException) {
        handlePasskeyFailure(e)
    }
    if (response != null) {
        val parsed = response.data.getString("androidx.credentials.BUNDLE_KEY_REGISTRATION_RESPONSE_JSON")
        if (parsed != null) {
            saveSettings("passkey_regResponse_feedback", parsed)
        }
        if (parsed != null) {
            Log.d("passkey_regResponse_feedback", parsed)
        }
    }
    return response
}

private fun fetchRegistrationJsonFromServer(context: Context, username: String, responsed: String): String {
    //Update userId, name and Display name in the mock
    return responsed.replace("<userId>", getEncodedUserId())
        .replace("<userName>", username)
        .replace("<userDisplayName>", username)
        .replace("<challenge>", getEncodedChallenge())
}

private fun getEncodedUserId(): String {
    val random = SecureRandom()
    val bytes = ByteArray(64)
    random.nextBytes(bytes)
    return Base64.encodeToString(
        bytes,
        Base64.NO_WRAP or Base64.URL_SAFE or Base64.NO_PADDING
    )
}

private fun getEncodedChallenge(): String {
    val random = SecureRandom()
    val bytes = ByteArray(32)
    random.nextBytes(bytes)
    return Base64.encodeToString(
        bytes,
        Base64.NO_WRAP or Base64.URL_SAFE or Base64.NO_PADDING
    )
}

private fun handlePasskeyFailure(e: CreateCredentialException) {
    val msg = when (e) {
        is CreatePublicKeyCredentialDomException -> {
            // Handle the passkey DOM errors thrown according to the
            // WebAuthn spec using e.domError
            "An error occurred while creating a passkey, please check logs for additional details."
        }
        is CreateCredentialCancellationException -> {
            // The user intentionally canceled the operation and chose not
            // to register the credential.
            "The user intentionally canceled the operation and chose not to register the credential. Check logs for additional details."
        }
        is CreateCredentialInterruptedException -> {
            // Retry-able error. Consider retrying the call.
            "The operation was interrupted, please retry the call. Check logs for additional details."
        }
        is CreateCredentialProviderConfigurationException -> {
            // Your app is missing the provider configuration dependency.
            // Most likely, you're missing "credentials-play-services-auth".
            "Your app is missing the provider configuration dependency. Check logs for additional details."
        }
        is CreateCredentialUnknownException -> {
            "An unknown error occurred while creating passkey. Check logs for additional details."
        }
        is CreateCredentialCustomException -> {
            // You have encountered an error from a 3rd-party SDK. If you
            // make the API call with a request object that's a subclass of
            // CreateCustomCredentialRequest using a 3rd-party SDK, then you
            // should check for any custom exception type constants within
            // that SDK to match with e.type. Otherwise, drop or log the
            // exception.
            "An unknown error occurred from a 3rd party SDK. Check logs for additional details."
        }
        else -> {
            Log.w("Auth", "Unexpected exception type ${e::class.java.name}")
            "An unknown error occurred."
        }
    }
    Log.e("Auth", "createPasskey failed with exception: " + e.message.toString())
}

private suspend fun getSavedCredentials(credentialManager: CredentialManager, context: Context, cookies: List<Cookie>): Pair<String?, Response> {
    val objecttt = fetchAuthJsonFromServer(context, cookies)
    val response = objecttt.second
    val normalFetch = objecttt.first
    val getPublicKeyCredentialOption =
        GetPublicKeyCredentialOption(normalFetch, null)
    val getPasswordOption = GetPasswordOption()
    val result = try {
        credentialManager.getCredential(
            context,
            GetCredentialRequest(
                listOf(
                    getPublicKeyCredentialOption,
                    getPasswordOption
                )
            )
        )
    } catch (e: Exception) {
        Log.e("Auth", "getCredential failed with exception: " + e.message.toString())
        return Pair("",response)
    }

    if (result.credential is PublicKeyCredential) {
        val cred = result.credential as PublicKeyCredential
        DataProvider.setSignedInThroughPasskeys(true)
        Log.d("RESPONSE AA", cred.authenticationResponseJson)
        return Pair(cred.authenticationResponseJson, response)
    }
    if (result.credential is PasswordCredential) {
        val cred = result.credential as PasswordCredential
        DataProvider.setSignedInThroughPasskeys(false)
        return Pair("Got Password - User:${cred.id} Password: ${cred.password}", response)
    }
    if (result.credential is CustomCredential) {
        //If you are also using any external sign-in libraries, parse them here with the
        // utility functions provided.
    }
    return Pair(null, response)
}

private suspend fun fetchAuthJsonFromServer(context: Context, cookies: List<Cookie>): Pair<String, Response> {
    val url = "https://plausible-dapper-justice.glitch.me/auth/signinRequest"
    val headers = mapOf("X-Requested-With" to "XMLHttpRequest")
    val body = emptyMap<String, String>()
    val response = makeHttpPostRequestWithCookies(url, headers, body, cookies)

    if (response != null) {
        val responseText = response.body?.string() ?: ""
        Log.d("RESPONSE LOGIN A", responseText)
        saveSettings("passkey_AuthChallenge", responseText)

        // Parse the responseText as JSON and return
        try {
            val jsonResponse = JSONObject(responseText)
            return Pair(jsonResponse.toString(),response)
        } catch (e: Exception) {
            // Handle JSON parsing exception if necessary
            Log.e("JSON Parsing Error", e.message ?: "Unknown error")
        }
    }

    return Pair("",response!!)
}

suspend fun makeHttpPostRequestWithCookies(
    url: String,
    headers: Map<String, String>,
    body: Map<String, String>,
    cookies: List<Cookie> = emptyList()
): Response? = withContext(Dispatchers.IO) {
    try {
        val client = OkHttpClient.Builder()
            .cookieJar(object : CookieJar {
                private val cookieStore: MutableMap<String, List<Cookie>> = mutableMapOf()

                override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                    cookieStore[url.host] = cookies
                }

                override fun loadForRequest(url: HttpUrl): List<Cookie> {
                    return cookieStore[url.host] ?: emptyList()
                }
            })
            .build()

        val jsonBody = buildJsonObject {
            body.forEach { (key, value) ->
                put(key, value)
            }
        }.toString()

        val requestBody = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType())

        val requestBuilder = Request.Builder()
            .url(url)
            .post(requestBody)

        headers.forEach { (key, value) ->
            requestBuilder.addHeader(key, value)
        }

        // Add cookies to the request
        cookies.forEach {
            requestBuilder.addHeader("Cookie", "${it.name}=${it.value}")
        }

        val request = requestBuilder.build()

        val response = client.newCall(request).execute()
//        val responseBody = response.body?.string() ?: ""

        // Do not return a pair, just the responseBody
        response
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

suspend fun makeHttpPostRequestWithCookiesA(
    url: String,
    headers: Map<String, String>,
    body: String,
    cookies: List<Cookie> = emptyList()
): Response? = withContext(Dispatchers.IO) {
    try {
        val client = OkHttpClient.Builder()
            .cookieJar(object : CookieJar {
                private val cookieStore: MutableMap<String, List<Cookie>> = mutableMapOf()

                override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                    cookieStore[url.host] = cookies
                }

                override fun loadForRequest(url: HttpUrl): List<Cookie> {
                    return cookieStore[url.host] ?: emptyList()
                }
            })
            .build()

        val jsonBody = body

        val requestBody = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType())

        val requestBuilder = Request.Builder()
            .url(url)
            .post(requestBody)

        headers.forEach { (key, value) ->
            requestBuilder.addHeader(key, value)
        }

        // Add cookies to the request
        cookies.forEach {
            requestBuilder.addHeader("Cookie", "${it.name}=${it.value}")
        }

        val request = requestBuilder.build()

        val response = client.newCall(request).execute()
//        val responseBody = response.body?.string() ?: ""

        // Do not return a pair, just the responseBody
        response
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

// Function to extract cookies from the response headers
private fun extractCookiesFromResponse(response: Response): List<Cookie> {
    val newCookies = response.headers("Set-Cookie").mapNotNull {
        Cookie.parse(response.request.url, it)
    }

    // Save the new cookies
    response.request.url.host?.let {
        response.request.header("Cookie")
    }

    return newCookies
}