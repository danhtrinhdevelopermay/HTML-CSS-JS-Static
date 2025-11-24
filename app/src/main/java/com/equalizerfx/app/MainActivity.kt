package com.equalizerfx.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.equalizerfx.app.audio.AudioEngine
import com.equalizerfx.app.audio.AudioVisualizer
import com.equalizerfx.app.player.MediaPlayerManager
import com.equalizerfx.app.service.AudioService
import com.equalizerfx.app.ui.components.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

class MainActivity : ComponentActivity() {
    private lateinit var mediaPlayerManager: MediaPlayerManager
    private lateinit var audioEngine: AudioEngine
    private lateinit var audioVisualizer: AudioVisualizer
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        mediaPlayerManager = MediaPlayerManager(this)
        
        startService(Intent(this, AudioService::class.java))
        
        setContent {
            EqualizerFXTheme {
                MainScreen(
                    mediaPlayerManager = mediaPlayerManager,
                    onAudioEngineReady = { engine ->
                        audioEngine = engine
                    },
                    onVisualizerReady = { visualizer ->
                        audioVisualizer = visualizer
                    }
                )
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        if (::audioEngine.isInitialized) {
            audioEngine.release()
        }
        if (::audioVisualizer.isInitialized) {
            audioVisualizer.release()
        }
        mediaPlayerManager.release()
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainScreen(
    mediaPlayerManager: MediaPlayerManager,
    onAudioEngineReady: (AudioEngine) -> Unit,
    onVisualizerReady: (AudioVisualizer) -> Unit
) {
    val context = LocalContext.current
    
    val permissionsState = rememberMultiplePermissionsState(
        permissions = buildList {
            add(Manifest.permission.MODIFY_AUDIO_SETTINGS)
            add(Manifest.permission.RECORD_AUDIO)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.READ_MEDIA_AUDIO)
            } else {
                add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    )
    
    LaunchedEffect(Unit) {
        if (!permissionsState.allPermissionsGranted) {
            permissionsState.launchMultiplePermissionRequest()
        }
    }
    
    val audioSessionId by remember {
        derivedStateOf { mediaPlayerManager.audioSessionId }
    }
    
    val audioEngine = remember(audioSessionId) {
        if (audioSessionId != 0) {
            AudioEngine(audioSessionId).also { onAudioEngineReady(it) }
        } else {
            null
        }
    }
    
    val audioVisualizer = remember(audioSessionId) {
        if (audioSessionId != 0) {
            AudioVisualizer(audioSessionId).also { onVisualizerReady(it) }
        } else {
            null
        }
    }
    
    val isPlaying by mediaPlayerManager.isPlaying.collectAsState()
    val currentFile by mediaPlayerManager.currentFile.collectAsState()
    
    val equalizerBands by audioEngine?.equalizerBands?.collectAsState() ?: remember {
        mutableStateOf(emptyList())
    }
    val bassBoostLevel by audioEngine?.bassBoostLevel?.collectAsState() ?: remember {
        mutableStateOf(0)
    }
    val trebleBoostLevel by audioEngine?.trebleBoostLevel?.collectAsState() ?: remember {
        mutableStateOf(0)
    }
    val reverbLevel by audioEngine?.reverbLevel?.collectAsState() ?: remember {
        mutableStateOf(0)
    }
    val effect3DLevel by audioEngine?.effect3DLevel?.collectAsState() ?: remember {
        mutableStateOf(0)
    }
    val effect8DEnabled by audioEngine?.effect8DEnabled?.collectAsState() ?: remember {
        mutableStateOf(false)
    }
    
    val waveformData by audioVisualizer?.waveformData?.collectAsState() ?: remember {
        mutableStateOf(FloatArray(128))
    }
    val bassLevels by audioVisualizer?.bassLevels?.collectAsState() ?: remember {
        mutableStateOf(FloatArray(10))
    }
    val trebleLevels by audioVisualizer?.trebleLevels?.collectAsState() ?: remember {
        mutableStateOf(FloatArray(10))
    }
    val frequencyBands by audioVisualizer?.frequencyBands?.collectAsState() ?: remember {
        mutableStateOf(FloatArray(20))
    }
    
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            mediaPlayerManager.loadFile(it)
        }
    }
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF121212)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "EQUALIZER FX",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            PlayerControls(
                isPlaying = isPlaying,
                currentFile = currentFile,
                onPlayPause = {
                    if (isPlaying) {
                        mediaPlayerManager.pause()
                    } else {
                        mediaPlayerManager.play()
                    }
                },
                onStop = {
                    mediaPlayerManager.stop()
                },
                onSelectFile = {
                    filePickerLauncher.launch("audio/*")
                }
            )
            
            VisualizerView(
                waveformData = waveformData,
                bassLevels = bassLevels,
                trebleLevels = trebleLevels,
                frequencyBands = frequencyBands
            )
            
            EffectsControls(
                bassBoostLevel = bassBoostLevel,
                trebleBoostLevel = trebleBoostLevel,
                reverbLevel = reverbLevel,
                effect3DLevel = effect3DLevel,
                effect8DEnabled = effect8DEnabled,
                onBassBoostChange = { audioEngine?.setBassBoost(it) },
                onTrebleBoostChange = { audioEngine?.setTrebleBoost(it) },
                onReverbChange = { audioEngine?.setReverb(it) },
                onEffect3DChange = { audioEngine?.set3DEffect(it) },
                onEffect8DToggle = { audioEngine?.set8DEffect(it) }
            )
            
            EqualizerView(
                bands = equalizerBands,
                onBandLevelChange = { index, level ->
                    audioEngine?.setBandLevel(index, level)
                }
            )
        }
    }
}

@Composable
fun EqualizerFXTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFF6200EE),
            onPrimary = Color.White,
            secondary = Color(0xFF03DAC6),
            background = Color(0xFF121212),
            surface = Color(0xFF1E1E1E)
        ),
        content = content
    )
}
