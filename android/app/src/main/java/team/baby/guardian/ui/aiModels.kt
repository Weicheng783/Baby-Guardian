package team.baby.guardian.ui

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.NewReleases
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Card
import androidx.compose.material3.ChipColors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import team.baby.guardian.AlertDialogExample
import team.baby.guardian.R
import java.io.IOException
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun GeminiUI(useCase: String, inputAlt: String, imageUriAlt: String, hideInput: Boolean) {
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

    var isLoading by remember { mutableStateOf(false) }
    var input by remember { mutableStateOf("What things that I need to take care when baby crying or not sleep?") }
    if(language == "zh"){
        input = "当婴儿总是哭泣和吵闹不睡觉的时候，我需要注意什么?"
    }
    var mode by remember { mutableStateOf(useCase) }
    if(mode == "latest_image"){
        mode = "image"
    }
    var imageUri by remember { mutableStateOf("") }
    var showInstruction by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val coroutineScope = rememberCoroutineScope()
    var showAdvanced by remember { mutableStateOf(false) }
    var resulted by remember {
        mutableStateOf("")
    }
    var hideInputSwitch by remember {
        mutableStateOf(false)
    }

    coroutineScope.launch {
        if(readSettings("gemini_input") != null){
            input = readSettings("gemini_input").toString()
        }
        if(readSettings("gemini_mode") != null){
            mode = readSettings("gemini_mode").toString()
        }
        if(readSettings("gemini_image") != null){
            imageUri = readSettings("gemini_image").toString()
        }
    }

    if(inputAlt != ""){
        input = inputAlt
    }

    if(imageUriAlt != ""){
        imageUri = imageUriAlt
    }

    DisposableEffect(isLoading) {
        onDispose {
            if(isLoading){
                scope.launch(Dispatchers.IO) {
                    try {
                        isLoading = true
//                        val result = withContext(Dispatchers.IO) {
                        val result = gemini_ai_gen_1(input, mode, imageUri)
                        resulted = result
//                        }
//                        Log.d("GeminiAI", "Result: $result")
                    } catch (e: Exception) {
                        Log.e("GeminiAI", "Error: ${e.message}")
                    } finally {
                        isLoading = false
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if(!hideInput || hideInputSwitch){
            OutlinedTextField(
                value = input,
                onValueChange = {
                    input = it
                    coroutineScope.launch {
                        saveSettings("gemini_input", input)
                    }
                },
                label = { Text(stringResource(R.string.input)) },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(bottom = 16.dp)
            )
        }

        if(useCase != "latest_image"){
            Spacer(modifier = Modifier.width(16.dp))
            Button(
                onClick = {
                    showAdvanced = !showAdvanced
                    if(hideInput){
                        hideInputSwitch = !hideInputSwitch
                    }
                },
                modifier = Modifier.height(50.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if(!showAdvanced){
                        Icon(imageVector = Icons.Outlined.ArrowDownward, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.toggle_advanced_mode))
                    }else{
                        Spacer(modifier = Modifier.height(16.dp))
                        Icon(imageVector = Icons.Outlined.ArrowUpward, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.hide_advanced_mode))
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if(showAdvanced){
            OutlinedTextField(
                value = mode,
                onValueChange = {
                    mode = it
                    coroutineScope.launch {
                        saveSettings("gemini_mode", mode)
                    }
                },
                label = { Text(stringResource(R.string.mode)) },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = imageUri ?: "",
                onValueChange = {
                    imageUri = it
                    coroutineScope.launch {
                        saveSettings("gemini_image", imageUri)
                    }
                },
                label = { Text(stringResource(R.string.image_uri_optional)) },
                modifier = Modifier.fillMaxWidth(0.9f).padding(bottom = 16.dp)
            )
        }

        val buttonColors = androidx.compose.material3.ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.9f)
        )

        if (isLoading) {
            // Show loading indicator and message
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(8.dp))
                Text(stringResource(R.string.hang_tight_we_are_processing))
            }
        } else {
            // Show the button when not loading
            Button(
                onClick = {
                    isLoading = true
                },
                modifier = Modifier.height(50.dp),
//                    .fillMaxWidth()
//                    .height(50.dp),
                colors = buttonColors
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(imageVector = Icons.Outlined.NewReleases, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    if(useCase != "latest_image"){
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    if(useCase == "latest_image"){
                        Text(stringResource(R.string.gemini_interpret_this_image))
                    }else{
                        Text(stringResource(R.string.try_google_gemini))
                        Spacer(modifier = Modifier.width(8.dp))
                        AssistChip(onClick = { showInstruction = true }, label = { Text(
                            stringResource(R.string.new_aa), color = MaterialTheme.colorScheme.background) })
                    }
                }
            }
        }

        if(showInstruction){
            AlertDialogExample(
                onDismissRequest = { showInstruction = false },
                onConfirmation = { showInstruction = false },
                dialogTitle = stringResource(R.string.gemini_how_to),
                dialogText = stringResource(R.string.hi_welcome_to_google_gemini_model_tester_this_is_a_generative_model_that_can_help_you_analyse_things_make_decisions_and_happier_throughout_your_life_for_input_type_in_what_you_want_to_ask_for_mode_type_in_full_or_image_and_given_an_image_url_from_internet_when_the_mode_is_image_double_check_the_answers_before_using_them_models_can_make_errors_technical_correspondence_john_chen_gemini_model_account_weicheng_ao_api_front_back_end),
                icon = Icons.Default.NewReleases
            )
        }

        if(resulted != ""){
            Spacer(modifier = Modifier.height(10.dp))
            Card(modifier = Modifier.padding(10.dp)) {
                SelectionContainer {
                    Text(resulted, fontFamily = fontFamilyTitle, modifier = Modifier.padding(10.dp))
                }
            }
        }

        if(useCase != "latest_image"){
            Spacer(modifier = Modifier.height(10.dp))
            Text(stringResource(R.string.always_double_check_the_answers_before_using_them_models_can_make_errors), fontFamily = fontFamilyTitle, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@OptIn(DelicateCoroutinesApi::class)
suspend fun gemini_ai_gen_1(input: String, mode: String, imageUri: String): String {
    val url = "https://weicheng.app/baby_guardian/gemini_input.php"
    val client = OkHttpClient()

    Log.d("GEMINI param", input+" "+mode+" "+imageUri)

    val formBody = FormBody.Builder()
        .add("mode", mode)
        .add("input", input)
        .add("image_uri", imageUri)
        .build()

    val request = Request.Builder()
        .url(url)
        .post(formBody)
        .build()

    return try {
        // Use a background thread for network operations
        val response = withContext(Dispatchers.IO) {
            client.newCall(request).execute()
        }

        // Parse the response on the main thread
        val responseBody = response.body?.string() ?: ""
//        Log.d("GEMINI REQUEST", responseBody)
        responseBody
    } catch (e: IOException) {
        // Handle network errors
        ""
    }
}
