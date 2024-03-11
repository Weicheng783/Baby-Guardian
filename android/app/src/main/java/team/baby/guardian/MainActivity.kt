package team.baby.guardian

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresExtension
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import team.baby.guardian.ui.readSettings
import team.baby.guardian.ui.theme.WeichengLogTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import team.baby.guardian.passkeys.BabyPasskeysActivity
import java.util.Locale

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    @RequiresExtension(extension = Build.VERSION_CODES.R, version = 2)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
//        val config = resources.configuration
//        val lang = "en"
//        val locale = Locale(lang)
//        Locale.setDefault(locale)
//        config.setLocale(locale)
//
//        createConfigurationContext(config)
//        resources.updateConfiguration(config, resources.displayMetrics)

        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)

            WeichengLogTheme {
//                var locale by remember { mutableStateOf(Locale.getDefault().language) }
                LaunchedEffect(Unit) {
                    withContext(Dispatchers.IO) {
                        val temp = readSettings("language")
                        if(temp != null){
                            val config = resources.configuration
                            val newLocale = Locale(temp)
                            Locale.setDefault(newLocale)
                            config.setLocale(newLocale)
                            createConfigurationContext(config)
                            resources.updateConfiguration(config, resources.displayMetrics)
                        }
                    }
                }
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val context = LocalContext.current
                    val crashHandler = CrashHandler(context)
                    Thread.setDefaultUncaughtExceptionHandler(crashHandler)
                    BabyPasskeysActivity()
                    LogApp(context = context, windowSizeClass = windowSizeClass)
                }
            }
        }
    }

}