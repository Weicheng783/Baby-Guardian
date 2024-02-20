package team.baby.guardian

import android.content.Context
import android.content.Intent
import android.icu.text.DateFormat
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startActivity
import team.baby.guardian.ui.fontFamily
import team.baby.guardian.ui.theme.WeichengLogTheme
import java.io.PrintWriter
import java.io.StringWriter
import java.util.TimeZone

class CrashHandler(private val myContext: Context) : Thread.UncaughtExceptionHandler {
    private val LINE_SEPARATOR = "\n"
    val currentTime = DateFormat.getDateTimeInstance().format(System.currentTimeMillis())
    val timeZone = TimeZone.getDefault().id
    override fun uncaughtException(thread: Thread, exception: Throwable) {
        val stackTrace = StringWriter()
        exception.printStackTrace(PrintWriter(stackTrace))
        val errorReport = StringBuilder()
        errorReport.append("* User Time & Time Zone Information *\n")
        errorReport.append(currentTime + " ")
        errorReport.append(timeZone + "\n")
        errorReport.append("* App Version *\n")
        errorReport.append("Version: ${myContext.resources.getString(R.string.version)}\n")
        errorReport.append("Build Type: ${myContext.resources.getString(R.string.build_type)}\n\n")
        errorReport.append("** CAUSE OF CRASH/崩溃信息如下 **\n")
        errorReport.append("An error happened as described below:\n")
        errorReport.append(stackTrace.toString())
        errorReport.append(LINE_SEPARATOR)

        val intent = Intent(myContext, ExceptionDisplay::class.java).apply {
            putExtra("error", errorReport.toString())
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        myContext.startActivity(intent)

        android.os.Process.killProcess(android.os.Process.myPid())
        System.exit(10)
    }
}

class ExceptionDisplay : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WeichengLogTheme {
                ExceptionDisplayContent(intent.extras?.getString("error") ?: "")
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        intentData()
    }

    fun intentData() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
    }
}

@Composable
fun ExceptionDisplayContent(error: String) {
    var context = LocalContext.current

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
//                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Header
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.software_crash_happened),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Text(
                text = stringResource(R.string.your_app_has_crashed_sorry_about_the_inconvenience_but_please_send_us_the_error_info_if_you_could),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 16.dp),
                textAlign = TextAlign.Center,
            )

            // Error Details Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(bottom = 16.dp)
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            // Print Stack Trace Button (Developer Mode Only)
            Button(
                onClick = {
                    printStackTrace(error)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Print, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = stringResource(R.string.print_stack_trace))
                }
            }

            // Restart Application Button
            Button(
                onClick = {
                    restartApplication(context)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.RestartAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = stringResource(R.string.restart_application))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = stringResource(R.string.version) + "." + stringResource(R.string.build_type),
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
        }
    }
}

fun printStackTrace(error: String) {
    // Handle print stack trace action (only in developer mode)
    // For example, log the stack trace
    println(error)
}

fun restartApplication(context: Context) {
    // Handle restart application action
    val intent = Intent(context, MainActivity::class.java)
        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}