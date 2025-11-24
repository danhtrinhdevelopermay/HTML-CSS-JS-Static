package com.equalizerfx.app.audio

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.PresetReverb
import android.media.audiofx.Virtualizer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

enum class AudioMode {
    SYSTEM_AUDIO,
    FILE_PLAYBACK
}

class AudioEngine {
    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var presetReverb: PresetReverb? = null
    
    private val _currentMode = MutableStateFlow(AudioMode.FILE_PLAYBACK)
    val currentMode: StateFlow<AudioMode> = _currentMode
    
    private val _initializationFailed = MutableStateFlow(false)
    val initializationFailed: StateFlow<Boolean> = _initializationFailed
    
    private val _equalizerBands = MutableStateFlow<List<EqualizerBand>>(emptyList())
    val equalizerBands: StateFlow<List<EqualizerBand>> = _equalizerBands
    
    private val _bassBoostLevel = MutableStateFlow(0)
    val bassBoostLevel: StateFlow<Int> = _bassBoostLevel
    
    private val _trebleBoostLevel = MutableStateFlow(0)
    val trebleBoostLevel: StateFlow<Int> = _trebleBoostLevel
    
    private val _reverbLevel = MutableStateFlow(0)
    val reverbLevel: StateFlow<Int> = _reverbLevel
    
    private val _effect3DLevel = MutableStateFlow(0)
    val effect3DLevel: StateFlow<Int> = _effect3DLevel
    
    private val _effect8DEnabled = MutableStateFlow(false)
    val effect8DEnabled: StateFlow<Boolean> = _effect8DEnabled
    
    private var effect8DAngle = 0f
    private var currentSessionId: Int = 0
    
    companion object {
        private const val TAG = "AudioEngine"
        const val MAX_BASS_BOOST = 1000
        const val MAX_VIRTUALIZER = 1000
        const val SYSTEM_AUDIO_SESSION = 0
    }
    
    init {
        
    }
    
    fun switchMode(mode: AudioMode, sessionId: Int = SYSTEM_AUDIO_SESSION) {
        _currentMode.value = mode
        val targetSessionId = when (mode) {
            AudioMode.SYSTEM_AUDIO -> SYSTEM_AUDIO_SESSION
            AudioMode.FILE_PLAYBACK -> {
                if (sessionId == 0 || sessionId == SYSTEM_AUDIO_SESSION) {
                    Log.w(TAG, "File playback mode requires valid media session. Waiting for session...")
                    _initializationFailed.value = false
                    return
                }
                sessionId
            }
        }
        
        if (targetSessionId != currentSessionId) {
            releaseEffects()
            initializeEffects(targetSessionId)
        }
    }
    
    private fun initializeEffects(sessionId: Int) {
        currentSessionId = sessionId
        try {
            if (sessionId == SYSTEM_AUDIO_SESSION) {
                Log.w(TAG, "Attempting to attach effects to system audio (session 0). This requires root/system permissions.")
            }
            
            equalizer = Equalizer(0, sessionId).apply {
                enabled = true
                
                val numBands = numberOfBands.toInt()
                val bands = mutableListOf<EqualizerBand>()
                
                for (i in 0 until numBands) {
                    val freq = getCenterFreq(i.toShort())
                    val freqRange = getBandFreqRange(i.toShort())
                    bands.add(
                        EqualizerBand(
                            index = i,
                            frequency = freq / 1000,
                            minFreq = freqRange[0] / 1000,
                            maxFreq = freqRange[1] / 1000,
                            level = 0
                        )
                    )
                }
                
                val additionalBands = createAdditionalBands(numBands, bands)
                _equalizerBands.value = bands + additionalBands
            }
            
            bassBoost = BassBoost(0, sessionId).apply {
                enabled = true
            }
            
            virtualizer = Virtualizer(0, sessionId).apply {
                enabled = true
            }
            
            presetReverb = PresetReverb(0, sessionId).apply {
                enabled = true
                preset = PresetReverb.PRESET_NONE
            }
            
            Log.d(TAG, "Audio effects initialized successfully for session $sessionId")
            _initializationFailed.value = false
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException: Cannot attach effects to session $sessionId. " +
                    "System audio mode requires root/system permissions.", e)
            _initializationFailed.value = true
            
            if (_currentMode.value == AudioMode.SYSTEM_AUDIO) {
                Log.w(TAG, "Automatically switching to FILE_PLAYBACK mode due to SecurityException")
                _currentMode.value = AudioMode.FILE_PLAYBACK
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing audio effects for session $sessionId", e)
            _initializationFailed.value = true
        }
    }
    
    private fun createAdditionalBands(existingCount: Int, existingBands: List<EqualizerBand>): List<EqualizerBand> {
        val targetBands = 20
        val additionalCount = targetBands - existingCount
        if (additionalCount <= 0) return emptyList()
        
        val additionalBands = mutableListOf<EqualizerBand>()
        val frequencies = generate20BandFrequencies()
        
        val usedFreqs = existingBands.map { it.frequency }.toSet()
        val availableFreqs = frequencies.filter { it !in usedFreqs }
        
        for (i in 0 until additionalCount.coerceAtMost(availableFreqs.size)) {
            val freq = availableFreqs[i]
            val bandwidth = calculateBandwidth(freq)
            additionalBands.add(
                EqualizerBand(
                    index = existingCount + i,
                    frequency = freq,
                    minFreq = (freq - bandwidth / 2).coerceAtLeast(20),
                    maxFreq = freq + bandwidth / 2,
                    level = 0,
                    isVirtual = true
                )
            )
        }
        
        return additionalBands
    }
    
    private fun generate20BandFrequencies(): List<Int> {
        return generateLogarithmicFrequencies(20, 20, 20000)
    }
    
    private fun generateLogarithmicFrequencies(count: Int, minFreq: Int, maxFreq: Int): List<Int> {
        val logMin = kotlin.math.ln(minFreq.toDouble())
        val logMax = kotlin.math.ln(maxFreq.toDouble())
        val step = (logMax - logMin) / (count - 1)
        
        return (0 until count).map { i ->
            val freq = kotlin.math.exp(logMin + i * step)
            if (i == count - 1) maxFreq else freq.toInt().coerceIn(minFreq, maxFreq)
        }
    }
    
    private fun calculateBandwidth(centerFreq: Int): Int {
        return when {
            centerFreq < 100 -> 20
            centerFreq < 1000 -> 50
            centerFreq < 5000 -> 100
            else -> 200
        }
    }
    
    fun setBandLevel(bandIndex: Int, level: Int) {
        try {
            val bands = _equalizerBands.value.toMutableList()
            if (bandIndex < bands.size) {
                val band = bands[bandIndex]
                bands[bandIndex] = band.copy(level = level)
                _equalizerBands.value = bands
                
                if (!band.isVirtual && equalizer != null) {
                    equalizer?.setBandLevel(bandIndex.toShort(), level.toShort())
                }
                
                updateTrebleFromBands()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting band level", e)
        }
    }
    
    private fun updateTrebleFromBands() {
        val bands = _equalizerBands.value
        val trebleBands = bands.filter { it.frequency >= 4000 }
        if (trebleBands.isNotEmpty()) {
            val avgTreble = trebleBands.map { it.level }.average().toInt()
            _trebleBoostLevel.value = avgTreble
        }
    }
    
    fun setBassBoost(level: Int) {
        try {
            val clampedLevel = level.coerceIn(0, MAX_BASS_BOOST)
            bassBoost?.setStrength(clampedLevel.toShort())
            _bassBoostLevel.value = clampedLevel
        } catch (e: Exception) {
            Log.e(TAG, "Error setting bass boost", e)
        }
    }
    
    fun setTrebleBoost(level: Int) {
        _trebleBoostLevel.value = level
        
        val bands = _equalizerBands.value
        val trebleBandIndices = bands.filter { it.frequency >= 4000 }.map { it.index }
        
        trebleBandIndices.forEach { index ->
            setBandLevel(index, level)
        }
    }
    
    fun setReverb(level: Int) {
        try {
            _reverbLevel.value = level
            val preset = when {
                level == 0 -> PresetReverb.PRESET_NONE
                level < 25 -> PresetReverb.PRESET_SMALLROOM
                level < 50 -> PresetReverb.PRESET_MEDIUMROOM
                level < 75 -> PresetReverb.PRESET_LARGEROOM
                else -> PresetReverb.PRESET_PLATE
            }
            presetReverb?.preset = preset.toShort()
        } catch (e: Exception) {
            Log.e(TAG, "Error setting reverb", e)
        }
    }
    
    fun set3DEffect(level: Int) {
        try {
            val clampedLevel = level.coerceIn(0, MAX_VIRTUALIZER)
            virtualizer?.setStrength(clampedLevel.toShort())
            _effect3DLevel.value = clampedLevel
        } catch (e: Exception) {
            Log.e(TAG, "Error setting 3D effect", e)
        }
    }
    
    fun set8DEffect(enabled: Boolean) {
        _effect8DEnabled.value = enabled
        if (enabled) {
            effect8DAngle = 0f
        }
    }
    
    fun process8DAudio(audioData: ShortArray): ShortArray {
        if (!_effect8DEnabled.value) return audioData
        
        effect8DAngle += 0.1f
        if (effect8DAngle > 2 * PI) effect8DAngle -= (2 * PI).toFloat()
        
        val processed = ShortArray(audioData.size)
        val leftGain = (cos(effect8DAngle) + 1) / 2
        val rightGain = (sin(effect8DAngle) + 1) / 2
        
        for (i in audioData.indices step 2) {
            if (i + 1 < audioData.size) {
                processed[i] = (audioData[i] * leftGain).toInt().toShort()
                processed[i + 1] = (audioData[i + 1] * rightGain).toInt().toShort()
            }
        }
        
        return processed
    }
    
    private fun releaseEffects() {
        try {
            equalizer?.release()
            bassBoost?.release()
            virtualizer?.release()
            presetReverb?.release()
            equalizer = null
            bassBoost = null
            virtualizer = null
            presetReverb = null
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing audio effects", e)
        }
    }
    
    fun release() {
        releaseEffects()
    }
}

data class EqualizerBand(
    val index: Int,
    val frequency: Int,
    val minFreq: Int,
    val maxFreq: Int,
    val level: Int,
    val isVirtual: Boolean = false
)
