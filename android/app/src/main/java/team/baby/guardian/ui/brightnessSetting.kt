package team.baby.guardian.ui

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import team.baby.guardian.R

//@Composable
//fun setBrightness(mode: String) {
//    val context = LocalContext.current
//
//    // Check if the WRITE_SETTINGS permission is granted
//    if (Settings.System.canWrite(context)) {
//        // Get the current screen brightness mode and value
//        val originalBrightnessMode = Settings.System.getInt(
//            context.contentResolver,
//            Settings.System.SCREEN_BRIGHTNESS_MODE
//        )
//        val originalBrightnessValue = Settings.System.getInt(
//            context.contentResolver,
//            Settings.System.SCREEN_BRIGHTNESS
//        )
//
//        // If the brightness mode is set to automatic, change it to manual
//        if (originalBrightnessMode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
//            Settings.System.putInt(
//                context.contentResolver,
//                Settings.System.SCREEN_BRIGHTNESS_MODE,
//                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
//            )
//        }
//
//        // Set the screen brightness based on the mode
//        when (mode) {
//            "max" -> {
//                val maxBrightness = 255
//                Settings.System.putInt(
//                    context.contentResolver,
//                    Settings.System.SCREEN_BRIGHTNESS,
//                    maxBrightness
//                )
//
//                // Apply the brightness change
//                applyBrightness(maxBrightness, context)
//            }
//            "original" -> {
//                // Restore the original brightness value
//                Settings.System.putInt(
//                    context.contentResolver,
//                    Settings.System.SCREEN_BRIGHTNESS,
//                    originalBrightnessValue
//                )
//
//                // Apply the brightness change
//                applyBrightness(originalBrightnessValue, context)
//            }
//            // Add more cases as needed for other modes
//        }
//    }
//}

//@Composable
//fun applyBrightness(brightnessValue: Int, context: Context) {
//    val activity = LocalContext.current as? ComponentActivity
//
//    activity?.let {
//        val window = it.window
//        val layoutParams = window.attributes
//        layoutParams.screenBrightness = brightnessValue / 255f
//        window.attributes = layoutParams
//    }
//}

fun setBrightness(context: Context, isFull: Boolean) {
    val activity = context as? Activity ?: return
    val layoutParams: WindowManager.LayoutParams = activity.window.attributes
    layoutParams.screenBrightness = if (isFull) 1.0f else WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
    activity.window.attributes = layoutParams
}

class UpdateBrightnessHelper(val fragment: Fragment) : DefaultLifecycleObserver {
    init {
        fragment.lifecycle.addObserver(this)
    }

    override fun onStart(owner: LifecycleOwner) {
        setBrightness(fragment.requireContext(), true)
    }

    override fun onStop(owner: LifecycleOwner) {
        setBrightness(fragment.requireContext(), false)
    }


    override fun onDestroy(owner: LifecycleOwner) {
        fragment.lifecycle.removeObserver(this)
    }
}

class QrCodeFragment : Fragment() {
    private val updateBrightness = UpdateBrightnessHelper(this)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Method to generate the QR code and set it to the ImageView

        // Increase the screen brightness to maximum when displaying the QR code
        setBrightness(requireContext(), true)
    }

    override fun onDestroyView() {
        // Reset the screen brightness to its original value when the fragment is destroyed
        setBrightness(requireContext(), false)
        super.onDestroyView()
    }

}

@Composable
fun UpdateBrightness() {
    val context = LocalContext.current
    DisposableEffect(Unit) {
        setBrightness(context, true)
        onDispose {
            setBrightness(context, false)
        }
    }
}

// Represents the state of the UI for SomeScreenFragment
data class SomeScreenViewState(
    // Represents whether the QR code dialog should be displayed or hidden.
    val showQrCodeDialog: Boolean = false
)

// ViewModel for SomeScreenFragment, handles MVI logic and manages the view state
class SomeScreenViewModel : ViewModel() {

    // MutableStateFlow to hold the current view state with initial value
    private val _viewState: MutableStateFlow<SomeScreenViewState> = MutableStateFlow(SomeScreenViewState())
    // Exposed StateFlow for observing the view state changes
    val viewState: StateFlow<SomeScreenViewState> = _viewState
}

@Composable
fun SomeScreen(state: SomeScreenViewState) {
    Scaffold {
        Box(modifier = Modifier.padding(it)) {
            // main content
        }
        // ANY MAGIC HERE ?
        if (state.showQrCodeDialog) {
            QrCodeDialog()
            // ANY MAGIC HERE ?
        }
    }
}

@Composable
fun QrCodeDialog() {
    // Compose dialog with QR code
}