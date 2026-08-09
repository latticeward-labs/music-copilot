package com.latticeward.musiccopilot.spike

import android.Manifest
import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.latticeward.musiccopilot.spike.library.MediaStoreLibrary
import com.latticeward.musiccopilot.spike.playback.SpikePlaybackService
import com.latticeward.musiccopilot.spike.trace.SpikeTrace
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class HarnessState(
    val controllerConnected: Boolean = false,
    val mediaPermission: Boolean = false,
    val notificationPermission: Boolean = false,
    val scannedCount: Int = 0,
    val queueCount: Int = 0,
    val currentIndex: Int = -1,
    val positionMs: Long = 0,
    val isPlaying: Boolean = false,
    val status: String = "Connecting to service-owned MediaSession",
)

class MainActivity : ComponentActivity() {
    private val mutableState = MutableStateFlow(HarnessState())
    private val state = mutableState.asStateFlow()
    private val mediaStoreLibrary by lazy { MediaStoreLibrary(this) }
    private var scannedItems: List<MediaItem> = emptyList()
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var controller: MediaController? = null

    private val mediaPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            updatePermissionState("Media permission result: $granted")
            SpikeTrace.event(code = "MEDIA_PERMISSION_RESULT", "granted" to granted)
        }

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            updatePermissionState("Notification permission result: $granted")
            SpikeTrace.event(code = "NOTIFICATION_PERMISSION_RESULT", "granted" to granted)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        updatePermissionState()
        connectController()
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    HarnessScreen(
                        state = state.collectAsState().value,
                        onRequestMedia = ::requestMediaPermission,
                        onRequestNotifications = ::requestNotificationPermission,
                        onScan = ::scanLibrary,
                        onLoadAndPlay = ::loadQueueAndPlay,
                        onPlayPause = ::togglePlayPause,
                        onNext = { controller?.seekToNextMediaItem() },
                        onPrevious = { controller?.seekToPreviousMediaItem() },
                        onSeekBack = ::seekBackTenSeconds,
                    )
                }
            }
        }
        lifecycleScope.launch {
            while (isActive) {
                delay(500)
                publishControllerState()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updatePermissionState()
    }

    override fun onDestroy() {
        controller?.removeListener(playerListener)
        controller = null
        controllerFuture?.let { MediaController.releaseFuture(it) }
        controllerFuture = null
        super.onDestroy()
    }

    private fun connectController() {
        val token = SessionToken(this, ComponentName(this, SpikePlaybackService::class.java))
        controllerFuture = MediaController.Builder(this, token).buildAsync().also { future ->
            future.addListener(
                {
                    runCatching { future.get(10, TimeUnit.SECONDS) }
                        .onSuccess { connected ->
                            controller = connected
                            connected.addListener(playerListener)
                            publishControllerState("Connected to the service-owned session")
                        }
                        .onFailure {
                            mutableState.value = mutableState.value.copy(
                                controllerConnected = false,
                                status = "Controller connection failed",
                            )
                            SpikeTrace.event(code = "CONTROLLER_CONNECTION_FAILED")
                        }
                },
                ContextCompat.getMainExecutor(this),
            )
        }
    }

    private fun requestMediaPermission() {
        mediaPermissionLauncher.launch(requiredMediaPermission())
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            updatePermissionState("Notification runtime permission is not used on this API")
        }
    }

    private fun scanLibrary() {
        if (!hasMediaPermission()) {
            mutableState.value = mutableState.value.copy(status = "Grant media access before scan")
            return
        }
        mutableState.value = mutableState.value.copy(status = "Scanning MediaStore")
        lifecycleScope.launch {
            runCatching { mediaStoreLibrary.scan() }
                .onSuccess { scan ->
                    scannedItems = scan.mediaItems
                    mutableState.value = mutableState.value.copy(
                        scannedCount = scan.mediaItems.size,
                        status = "Scan complete in ${scan.elapsedMs} ms; metadata is not displayed or logged",
                    )
                }
                .onFailure {
                    scannedItems = emptyList()
                    mutableState.value = mutableState.value.copy(
                        scannedCount = 0,
                        status = "Scan failed or access was revoked",
                    )
                    SpikeTrace.event(code = "MEDIASTORE_SCAN_FAILED")
                }
        }
    }

    private fun loadQueueAndPlay() {
        val activeController = controller
        if (activeController == null) {
            mutableState.value = mutableState.value.copy(status = "Controller is not connected")
            return
        }
        if (scannedItems.isEmpty()) {
            mutableState.value = mutableState.value.copy(status = "Scan produced no playable items")
            return
        }
        activeController.setMediaItems(scannedItems)
        activeController.prepare()
        activeController.play()
        SpikeTrace.event(code = "QUEUE_SUBMITTED", "items" to scannedItems.size)
    }

    private fun togglePlayPause() {
        controller?.let { activeController ->
            if (activeController.isPlaying) activeController.pause() else activeController.play()
        }
    }

    private fun seekBackTenSeconds() {
        controller?.let { activeController ->
            activeController.seekTo((activeController.currentPosition - 10_000L).coerceAtLeast(0))
        }
    }

    private fun publishControllerState(status: String? = null) {
        val activeController = controller
        if (activeController == null) return
        mutableState.value = mutableState.value.copy(
            controllerConnected = true,
            queueCount = activeController.mediaItemCount,
            currentIndex = activeController.currentMediaItemIndex,
            positionMs = activeController.currentPosition.coerceAtLeast(0),
            isPlaying = activeController.isPlaying,
            status = status ?: mutableState.value.status,
        )
    }

    private fun updatePermissionState(status: String? = null) {
        mutableState.value = mutableState.value.copy(
            mediaPermission = hasMediaPermission(),
            notificationPermission = hasNotificationPermission(),
            status = status ?: mutableState.value.status,
        )
    }

    private fun hasMediaPermission(): Boolean =
        ContextCompat.checkSelfPermission(this, requiredMediaPermission()) == PackageManager.PERMISSION_GRANTED

    private fun hasNotificationPermission(): Boolean =
        Build.VERSION.SDK_INT < 33 ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    private fun requiredMediaPermission(): String =
        if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_AUDIO
        else Manifest.permission.READ_EXTERNAL_STORAGE

    private val playerListener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            publishControllerState()
        }
    }
}

@Composable
private fun HarnessScreen(
    state: HarnessState,
    onRequestMedia: () -> Unit,
    onRequestNotifications: () -> Unit,
    onScan: () -> Unit,
    onLoadAndPlay: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeekBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Cluster B Android Spike Harness", style = MaterialTheme.typography.headlineSmall)
        Text("Not a product UI or final architecture. No personal media metadata is shown or logged.")
        Text("Status: ${state.status}")
        Text("Controller connected: ${state.controllerConnected}")
        Text("Media permission: ${state.mediaPermission}")
        Text("Notification permission: ${state.notificationPermission}")
        Text("Scan count: ${state.scannedCount}")
        Text("Queue count: ${state.queueCount}; current index: ${state.currentIndex}")
        Text("Playback: ${if (state.isPlaying) "Playing" else "Paused"}; position: ${state.positionMs} ms")

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onRequestMedia) { Text("Media access") }
            OutlinedButton(onClick = onRequestNotifications) { Text("Notifications") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onScan) { Text("Scan") }
            Button(onClick = onLoadAndPlay) { Text("Load + play") }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedButton(onClick = onPrevious) { Text("Previous item") }
            Button(onClick = onPlayPause) { Text("Play / Pause") }
            OutlinedButton(onClick = onNext) { Text("Next") }
        }
        OutlinedButton(onClick = onSeekBack) { Text("Seek back 10 s") }
        Text(
            "Previous item is a harness transport command only; numeric T_PREVIOUS remains undecided in Issue #16.",
            style = MaterialTheme.typography.bodySmall,
        )
    }
}
