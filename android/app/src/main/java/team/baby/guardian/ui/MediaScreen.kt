package team.baby.guardian.ui

import android.annotation.SuppressLint
import android.app.PictureInPictureParams
import android.content.ComponentName
import android.content.ContentValues.TAG
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Rational
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ComponentActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaController
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionToken
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerControlView
import androidx.media3.ui.PlayerView
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import team.baby.guardian.R


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun MediaScreen(
    serialNumber: String,
    deviceName: String,
    ownerStatus: String,
    context: Context
) {
//    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    Column (
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment= Alignment.CenterHorizontally
    ){
        Icon(
            imageVector = Icons.Filled.LiveTv,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = stringResource(R.string.live_streaming_for, serialNumber),
            style = MaterialTheme.typography.headlineSmall,
            fontFamily = fontFamilyTitle
        )
        Text(
            text = deviceName,
            style = MaterialTheme.typography.headlineSmall,
            fontFamily = fontFamilyTitle
        )
        var ownerStatus_translation = ""
        if(ownerStatus == "owner"){
            ownerStatus_translation = stringResource(id = R.string.owner)
        }else{
            ownerStatus_translation = stringResource(id = R.string.friend)
        }
        Text(
            text = stringResource(R.string.you_are_the_1, ownerStatus_translation),
            style = MaterialTheme.typography.headlineSmall,
            fontFamily = fontFamilyTitle
        )
        Spacer(modifier = Modifier.height(8.dp))
    }

    val mediaList = listOf(
//        MediaInfo("Test", "https://storage.googleapis.com/exoplayer-test-media-0/Jazz_In_Paris.mp3", MimeTypes.BASE_TYPE_AUDIO, "Description 2"),
        MediaInfo(
            stringResource(R.string.baby_guardian_device, serialNumber),
            "http://weicheng.app:8080/live/baby_guardian_$serialNumber.flv",
            MimeTypes.VIDEO_FLV,
            "http://weicheng.app:8080/live/baby_guardian_$serialNumber.flv"
        ),
        MediaInfo(
            stringResource(R.string.baby_guardian_device, serialNumber),
            "http://weicheng.app:8080/live/baby_guardian_audio_$serialNumber.flv",
            MimeTypes.AUDIO_AAC,
            "http://weicheng.app:8080/live/baby_guardian_audio_$serialNumber.flv"
        ),
        MediaInfo(
            stringResource(R.string.baby_radio_station),
            "http://weicheng.app:8080/live/baby_guardian_radio_$serialNumber.flv",
            MimeTypes.AUDIO_AAC,
            "http://weicheng.app:8080/live/baby_guardian_radio_$serialNumber.flv"
        ),
    )
    val sessionMap = remember { mutableStateMapOf<Int, MediaSession>() }
    MediaCard(mediaList[0], context, 0, sessionMap, false)

    Column (
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Spacer(modifier = Modifier.height(10.dp))
        Text(stringResource(R.string.live_audio_playback), fontFamily = fontFamilyTitle)
        val sessionMap_1 = remember { mutableStateMapOf<Int, MediaSession>() }
        MediaCard(mediaList[1], context, 1, sessionMap_1, true)
    }

    Column (
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.baby_radio_station), fontFamily = fontFamilyTitle)
        val sessionMap_2 = remember { mutableStateMapOf<Int, MediaSession>() }
        MediaCard(mediaList[2], context, 2, sessionMap_2, true)
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun MediaCard(
    mediaInfo: MediaInfo,
    context: Context,
    index: Int,
    sessionMap: SnapshotStateMap<Int, MediaSession>,
    b: Boolean
) {
    var exoPlayer by remember { mutableStateOf(createExoPlayer(context)) }
    val mediaSession = sessionMap.getOrPut(index) {
        MediaSession.Builder(context, exoPlayer).setId("session_$index").build()
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
//            .padding(16.dp)
    ) {
        PlayerSection(mediaInfo, context, exoPlayer, mediaSession, b, index)
        val mediaItem = MediaItem.Builder()
            .setUri(mediaInfo.url)
            .setMimeType(mediaInfo.mimeType)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setArtist(mediaInfo.description)
                    .setTitle(mediaInfo.title)
//                    .setArtworkUri(mediaInfo.url)
                    .build()
            )
            .build()
        exoPlayer.setMediaItems(listOf(mediaItem))

        val playbackServiceComponent = ComponentName(context, PlaybackService::class.java)
        val sessionToken = SessionToken(context, playbackServiceComponent)
        val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()

        exoPlayer.addListener(playbackStateListener(controllerFuture, mediaItem, exoPlayer, b))
        if(!b){
            exoPlayer.play()
        }
    }
}

@SuppressLint("RestrictedApi")
internal fun Context.findActivity(): ComponentActivity {
    var context = this
    while (context is ContextWrapper) {
        if (context is ComponentActivity) return context
        context = context.baseContext
    }
    throw IllegalStateException("Picture in picture should be called in the context of an Activity")
}

//fun enterPiPMode(){
//    var aspectRadio = Rational(16,9)
//    val params = PictureInPictureParams.Builder().setAspectRatio(aspectRadio).build()
//    activity?.enterPictureInPictureMode(params)
//}

class PiPActivity : AppCompatActivity() {
    @androidx.annotation.OptIn(UnstableApi::class) override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Hide the status bar.
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        // Remember that you should never show the action bar if the
        // status bar is hidden, so hide that too if necessary.
        actionBar?.hide()
        setContentView(R.layout.activity_pip)

//        var expPlayer1 = createExoPlayer(applicationContext)
        var exoPlayer = ExoPlayer.Builder(applicationContext).build()

        var sessionMap = SnapshotStateMap<Int, MediaSession>()
        var serial = intent.getStringExtra("device_serial")
        if (serial != null) {
            Log.d("SERIAL", serial)
        }else{
            Log.d("SERIAL", "NULL!")
        }

        val mediaList = listOf(
//        MediaInfo("Test", "https://storage.googleapis.com/exoplayer-test-media-0/Jazz_In_Paris.mp3", MimeTypes.BASE_TYPE_AUDIO, "Description 2"),
            MediaInfo(
                "1",
                "http://weicheng.app:8080/live/baby_guardian_$serial.flv",
                MimeTypes.VIDEO_FLV,
                "http://weicheng.app:8080/live/baby_guardian_$serial.flv"
            ),
            MediaInfo(
                "1",
                "http://weicheng.app:8080/live/baby_guardian_audio_$serial.flv",
                MimeTypes.AUDIO_AAC,
                "http://weicheng.app:8080/live/baby_guardian_audio_$serial.flv"
            ),
            MediaInfo(
                "1",
                "http://weicheng.app:8080/live/baby_guardian_radio_$serial.flv",
                MimeTypes.AUDIO_AAC,
                "http://weicheng.app:8080/live/baby_guardian_radio_$serial.flv"
            ),
        )

//        val mediaSession = sessionMap.getOrPut(2) {
//            MediaSession.Builder(applicationContext, exoPlayer).setId("session_a_1").build()
//        }

        val mediaItem = MediaItem.Builder()
            .setUri(mediaList[0].url)
            .setMimeType(mediaList[0].mimeType)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setArtist(mediaList[0].description)
                    .setTitle(mediaList[0].title)
//                    .setArtworkUri(mediaInfo.url)
                    .build()
            )
            .build()
        exoPlayer.setMediaItems(listOf(mediaItem))
        exoPlayer.prepare()

        val playbackServiceComponent = ComponentName(applicationContext, PlaybackService::class.java)
        val sessionToken = SessionToken(applicationContext, playbackServiceComponent)
        val controllerFuture = MediaController.Builder(applicationContext, sessionToken).buildAsync()

        exoPlayer.addListener(playbackStateListener(controllerFuture, mediaItem, exoPlayer, false))

        // Initialize ExoPlayer and controls
        val playerView = findViewById<PlayerView>(R.id.playerView)
        val playerControlView = PlayerControlView(this)
        enterPictureInPictureMode(
            PictureInPictureParams.Builder()
                .setAspectRatio(Rational(16, 9))
//                .setSourceRectHint(sourceRectHint)
                .setAutoEnterEnabled(true)
                .build()
        )
        playerControlView.player = playerView.player
        playerView.player = exoPlayer
        playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
        exoPlayer.playWhenReady = true
        exoPlayer.play()
        exoPlayer.repeatMode = Player.REPEAT_MODE_ALL;
        playerControlView.player?.play()

        // Add other setup for ExoPlayer and controls
//        playerControlView.apply {
//            player = playerView.player // Assuming exoPlayer is assigned to playerView.player
//            showTimeoutMs = 0
//            player?.play()
//        }
    }
}


@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun PlayerSection(
    mediaInfo: MediaInfo,
    context: Context,
    exoPlayer: ExoPlayer,
    mediaSession: MediaSession,
    b: Boolean,
    index: Int
) {
    val playerView = remember {
        PlayerView(context)
    }

    DisposableEffect(Unit) {
        exoPlayer.playWhenReady = !b

        playerView.player = exoPlayer
        playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL

        onDispose {
//            exoPlayer.removeListener(playbackStateListener(controllerFuture, mediaItem))
            exoPlayer.stop()
            exoPlayer.clearMediaItems()

//            MediaController.releaseFuture(controllerFuture)
            mediaSession.release()
        }
    }

    var isFullScreen by remember { mutableStateOf(false) }
    var rotationState by remember { mutableStateOf(0f) }

    var scale by remember { mutableStateOf(1f) }
    var rotation by remember { mutableStateOf(0f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var context = LocalContext.current
    val state = rememberTransformableState { zoomChange, offsetChange, rotationChange ->
        scale *= zoomChange
        rotation += rotationChange
        offset += offsetChange
    }
    val density = LocalDensity.current.density
    val originalAspectRatio = 1920f / 1080f
    var context_2: Context? = null

    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AndroidView(
            factory = { playerView },
            modifier = Modifier
                .fillMaxSize()
                .aspectRatio(originalAspectRatio)
//                .onGloballyPositioned { layoutCoordinates ->
//                    val builder = PictureInPictureParams.Builder()
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//                        builder.setAutoEnterEnabled(true)
//                    }
//                    context
//                        .findActivity()
//                        .setPictureInPictureParams(builder.build())
//                }
        ) { view ->
            context_2 = view.context
            PlayerControlView(context_2!!).apply {
                player = playerView.player // Assuming exoPlayer is assigned to playerView.player
                showTimeoutMs = 0
            }
        }
        // Inside MainActivity
        Button(onClick = {
            val intent = Intent(context_2, PiPActivity::class.java)
            intent.putExtra("device_serial", (index+1).toString())
            context_2?.startActivity(intent)
        }) {
            Icon(imageVector = Icons.Filled.PictureInPicture, contentDescription = "PictureInPicture Mode")
            Spacer(Modifier.width(10.dp))
            Text(text = stringResource(R.string.enter_pip_mode))
        }

//        Button(onClick = {
//            context_2?.findActivity()?.enterPictureInPictureMode(
//                PictureInPictureParams.Builder().build()
//            )
//        }) {
//            Text(text = stringResource(R.string.enter_pip_mode))
//        }
        if(!b){
            Spacer(modifier = Modifier.height(16.dp))
            Row (
                modifier = Modifier.fillMaxWidth(0.9f),
                horizontalArrangement = Arrangement.Center
            ){
                Icon(
                    imageVector = Icons.Default.WarningAmber,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = stringResource(R.string.delay_time_is_normally_between_10_30_seconds_due_to_the_limited_bandwidth_of_our_server_if_you_are_experiencing_higher_delay_or_video_is_not_playing_blank_black_screen_stuck_please_refresh_the_page_or_this_indicates_the_device_is_currently_offline_if_the_issue_persists_restart_the_device_and_contact_us_as_a_last_resort))
            }
        }
    }

//    Column {
//        AndroidView(
//            factory = { playerView },
////            modifier = modifier
//        ) { view ->
//            PlayerControlView(context).apply {
//                player = exoPlayer
//                showTimeoutMs = 0
//            }
//        }
//
//        ButtonToggleFullScreen(
//            isFullScreen = isFullScreen,
//            onToggle = { isFullScreen = !isFullScreen }
//        )
//    }
}

@Composable
fun ButtonToggleFullScreen(
    isFullScreen: Boolean,
    onToggle: () -> Unit
) {
    val buttonText = if (isFullScreen) "Exit Fullscreen" else "Fullscreen"
    TextButton(
        onClick = onToggle,
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Text(text = buttonText)
    }
}
data class MediaInfo(val title: String, val url: String, val mimeType: String, val description: String)

fun createExoPlayer(context: Context): ExoPlayer {
    return ExoPlayer.Builder(context).build()
}

fun playbackStateListener(controllerFuture: ListenableFuture<MediaController>, mediaItem: MediaItem, exoPlayer: ExoPlayer, b: Boolean): Player.Listener {
    controllerFuture.addListener({
        try {
            val mediaController = controllerFuture.get()

            // Ensure that the MediaController is connected before performing operations
            if (mediaController.isConnected) {
                if(!b){
                    mediaController.setMediaItem(mediaItem)
                    mediaController.prepare()
                    mediaController.play()
                }
//                exoPlayer.play()
            } else {
                Log.e(TAG, "MediaController is not connected.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating MediaController", e)
        }
    }, MoreExecutors.directExecutor())

    return object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            val stateString: String = when (playbackState) {
                ExoPlayer.STATE_IDLE -> "ExoPlayer.STATE_IDLE      -"
                ExoPlayer.STATE_BUFFERING -> "ExoPlayer.STATE_BUFFERING -"
                ExoPlayer.STATE_READY -> "ExoPlayer.STATE_READY     -"
                ExoPlayer.STATE_ENDED -> "ExoPlayer.STATE_ENDED     -"
                else -> "UNKNOWN_STATE             -"
            }
            Log.d(TAG, "changed state to $stateString")
            ExoPlayer.EVENT_IS_PLAYING_CHANGED
        }

//        override fun onIsPlayingChanged(isPlaying: Boolean){
//            val mediaController = controllerFuture.get()
//            if(isPlaying){
//                mediaController.play()
//            }else{
//                mediaController.stop()
//            }
//        }

    }
}
