package team.baby.guardian.ui

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.preference.PreferenceManager
import android.provider.Settings
import android.util.Log
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import team.baby.guardian.MainActivity
import team.baby.guardian.R
import java.util.concurrent.TimeUnit

class AlertWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    private var deviceSerial: String? = null

    init {
        this.deviceSerial = inputData.getString("deviceSerial")
    }

    override fun doWork(): Result {
        try {
            // Fetch new alerts from the server
            Log.d("AlertWorker_notification", "CHECKING")
            deviceSerial?.let {
                Log.d("AlertWorker_notification_serial", deviceSerial!!)
                proactiveFetch_notified(it) { fetchedNotifications ->
                    // Check for new alerts and send notifications
                    checkAndSendNotifications(fetchedNotifications)
                }
            }
        } catch (e: Exception) {
            // Handle errors
            return Result.failure()
        }

        return Result.success()
    }

    private fun checkAndSendNotifications(newNotifications: List<NotificationItem>) {
        // Implement logic to compare newNotifications with the existing notifications
        // and send system notifications for new alerts
        // You can use Android's NotificationManager for sending notifications
        for (notification in newNotifications) {
            sendNotification(
                applicationContext,
                notification.type,
                "${notification.alert}\n${notification.addition}\n${notification.datetime}"
            )
            Log.d("AlertWorker_notification", "NOTIFICATION SENT")
        }
    }
}

val alertWorkRequest = PeriodicWorkRequestBuilder<AlertWorker>(
    repeatInterval = 15,
    TimeUnit.MINUTES
).build()

fun sendNotification(context: Context, title: String, content: String) {
    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

//    Log.d("NotificationDebug", "sendNotification called with title: $title, content: $content")

    // Check if notification permission is granted
    if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
        // Create a unique notification ID (you can use a timestamp for simplicity)
        val notificationId = System.currentTimeMillis().toInt()

        // Create a unique notification channel for Android Oreo and above
        val channelId = "1545" // Replace with your unique channel ID
        val channelName = "Guardian Notify" // Replace with your channel name
        val channelDescription = "Your Channel Description" // Replace with your channel description
        val importance = NotificationManager.IMPORTANCE_HIGH

        val notificationChannel = NotificationChannel(channelId, channelName, importance).apply {
            description = channelDescription
        }

        // Register the channel with the system
        val notificationManager_1 = context.getSystemService(NotificationManager::class.java)
        notificationManager_1.createNotificationChannel(notificationChannel)

        // Now create the NotificationCompat.Builder
        val notificationBuilder = NotificationCompat.Builder(context, channelId).apply {
            setContentTitle(title)
            setContentText(content)
            setSmallIcon(R.drawable.ic_launcher_foreground)
            priority = NotificationCompat.PRIORITY_HIGH
            setAutoCancel(true)
        }

        // Create an intent to handle the notification click
        val notificationIntent = Intent(context, MainActivity::class.java) // Replace with your main activity
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Attach the pending intent to the notification
        notificationBuilder.setContentIntent(pendingIntent)

        // Before sending notification
//        Log.d("AlertWorker_notification", "Before sending notification")

        // Send notification
        notificationManager_1.notify(notificationId, notificationBuilder.build())

        // After sending notification
//        Log.d("AlertWorker_notification", "After sending notification")

    } else {
        // Handle case where notification permission is not granted
        // Check if the permission has been requested before
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val isPermissionRequested = sharedPreferences.getBoolean("notification_permission_requested", false)

        if (!isPermissionRequested) {
            // Request notification permission only once
            requestNotificationPermission(context)
            // Update the preference to indicate that the permission has been requested
            sharedPreferences.edit().putBoolean("notification_permission_requested", true).apply()
        } else {
            // The permission has been previously requested, and the user chose not to enable notifications
            // Show a message to the user indicating that notifications are disabled
            Log.e("NotificationPermission", "Notification permission not granted")
        }
    }
}

// Helper function to request notification permission
@SuppressLint("QueryPermissionsNeeded")
fun requestNotificationPermission(context: Context) {
    // Use an Intent to navigate to the app's notification settings
    val intent = Intent().apply {
        action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
    }

    // Check if the device has an activity to handle the intent
    if (intent.resolveActivity(context.packageManager) != null) {
        context.startActivity(intent)
    }
}

@Composable
fun NotificationTestButton(onClick: () -> Unit) {
    Button(onClick = onClick) {
        Text("Send Test Notification")
    }
}

fun testSendNotification(context: Context) {
    // Call the sendNotification function with test data
    sendNotification(context, "Test Notification", "This is a test notification message")
}