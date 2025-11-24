package com.equalizerfx.app.audio

import android.media.audiofx.Visualizer
import android.util.Log
import com.equalizerfx.app.settings.PerformanceConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.log10
import kotlin.math.sqrt

class AudioVisualizer {
    private var visualizer: Visualizer? = null
    private var currentSessionId: Int = -1
    private var currentSamplingRate: Int = 44100
    private var captureSize: Int = 1024
    private var captureRate: Int = 30
    
    private val _initializationFailed = MutableStateFlow(false)
    val initializationFailed: StateFlow<Boolean> = _initializationFailed
    
    private val _waveformData = MutableStateFlow<FloatArray>(FloatArray(128))
    val waveformData: StateFlow<FloatArray> = _waveformData
    
    private val _fftData = MutableStateFlow<FloatArray>(FloatArray(128))
    val fftData: StateFlow<FloatArray> = _fftData
    
    private val _bassLevels = MutableStateFlow<FloatArray>(FloatArray(10))
    val bassLevels: StateFlow<FloatArray> = _bassLevels
    
    private val _trebleLevels = MutableStateFlow<FloatArray>(FloatArray(10))
    val trebleLevels: StateFlow<FloatArray> = _trebleLevels
    
    private val _frequencyBands = MutableStateFlow<FloatArray>(FloatArray(20))
    val frequencyBands: StateFlow<FloatArray> = _frequencyBands
    
    private val _subBassWave = MutableStateFlow<FloatArray>(FloatArray(64))
    val subBassWave: StateFlow<FloatArray> = _subBassWave
    
    private var performanceConfig: PerformanceConfig = PerformanceConfig.forMode(com.equalizerfx.app.settings.PerformanceMode.MEDIUM)
    
    companion object {
        private const val TAG = "AudioVisualizer"
    }
    
    init {
        
    }
    
    fun updatePerformanceConfig(config: PerformanceConfig) {
        performanceConfig = config
        
        val allowedSizes = intArrayOf(128, 256, 512, 1024, 2048)
        captureSize = allowedSizes.find { it >= config.visualizerCaptureSize } 
            ?: allowedSizes.last()
        
        captureRate = config.visualizerCaptureRate.coerceAtLeast(1000)
        
        if (visualizer != null && currentSessionId != -1) {
            releaseVisualizer()
            initializeVisualizer(currentSessionId)
        }
    }
    
    fun switchSession(sessionId: Int) {
        if (sessionId == 0 || sessionId == AudioEngine.SYSTEM_AUDIO_SESSION) {
            if (sessionId == AudioEngine.SYSTEM_AUDIO_SESSION) {
                Log.w(TAG, "Attempting system audio visualizer - may require root")
            } else {
                Log.w(TAG, "Invalid session ID for visualizer. Waiting...")
                return
            }
        }
        
        if (sessionId != currentSessionId) {
            releaseVisualizer()
            initializeVisualizer(sessionId)
        }
    }
    
    private fun initializeVisualizer(sessionId: Int) {
        currentSessionId = sessionId
        try {
            if (sessionId == AudioEngine.SYSTEM_AUDIO_SESSION) {
                Log.w(TAG, "Attempting to attach visualizer to system audio (session 0). This requires root/system permissions.")
            }
            
            val maxCaptureRate = Visualizer.getMaxCaptureRate()
            val safeCaptureRate = captureRate.coerceIn(1000, maxCaptureRate)
            
            visualizer = Visualizer(sessionId).apply {
                captureSize = this@AudioVisualizer.captureSize
                setDataCaptureListener(
                    object : Visualizer.OnDataCaptureListener {
                        override fun onWaveFormDataCapture(
                            visualizer: Visualizer?,
                            waveform: ByteArray?,
                            samplingRate: Int
                        ) {
                            waveform?.let { processWaveform(it) }
                        }
                        
                        override fun onFftDataCapture(
                            visualizer: Visualizer?,
                            fft: ByteArray?,
                            samplingRate: Int
                        ) {
                            if (samplingRate > 0) {
                                currentSamplingRate = samplingRate
                            }
                            fft?.let { processFft(it) }
                        }
                    },
                    safeCaptureRate,
                    true,
                    true
                )
                enabled = true
            }
            Log.d(TAG, "Visualizer initialized successfully for session $sessionId with captureSize=$captureSize, captureRate=$safeCaptureRate (max=$maxCaptureRate)")
            _initializationFailed.value = false
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException: Cannot attach visualizer to session $sessionId. " +
                    "System audio mode requires root/system permissions.", e)
            _initializationFailed.value = true
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing visualizer for session $sessionId", e)
            _initializationFailed.value = true
        }
    }
    
    private fun processWaveform(waveform: ByteArray) {
        if (waveform.isEmpty()) {
            _waveformData.value = FloatArray(performanceConfig.waveformDataPoints)
            return
        }
        
        val dataPoints = performanceConfig.waveformDataPoints.coerceIn(1, waveform.size)
        val normalized = FloatArray(dataPoints)
        
        if (dataPoints == 1) {
            normalized[0] = (waveform[0].toInt() + 128) / 256f
        } else if (waveform.size <= dataPoints) {
            for (i in 0 until waveform.size.coerceAtMost(dataPoints)) {
                normalized[i] = (waveform[i].toInt() + 128) / 256f
            }
        } else {
            for (i in 0 until dataPoints) {
                val position = i * (waveform.size - 1).toFloat() / (dataPoints - 1)
                val lowerIndex = position.toInt().coerceIn(0, waveform.size - 1)
                val upperIndex = (lowerIndex + 1).coerceAtMost(waveform.size - 1)
                val fraction = position - lowerIndex
                
                val lowerValue = (waveform[lowerIndex].toInt() + 128) / 256f
                val upperValue = (waveform[upperIndex].toInt() + 128) / 256f
                normalized[i] = lowerValue * (1 - fraction) + upperValue * fraction
            }
        }
        _waveformData.value = normalized
    }
    
    private fun processFft(fft: ByteArray) {
        val numBins = kotlin.math.min(fft.size / 2, 512)
        val magnitudes = FloatArray(numBins)
        val bassLevels = FloatArray(10)
        val trebleLevels = FloatArray(10)
        val frequencyBands = FloatArray(20)
        val subBassWave = FloatArray(64)
        
        for (i in 0 until numBins) {
            val rfk = fft[2 * i].toInt()
            val ifk = fft[2 * i + 1].toInt()
            val magnitude = sqrt((rfk * rfk + ifk * ifk).toFloat())
            magnitudes[i] = magnitude
        }
        
        val maxMagnitude = magnitudes.maxOrNull() ?: 1f
        for (i in magnitudes.indices) {
            magnitudes[i] = (magnitudes[i] / maxMagnitude).coerceIn(0f, 1f)
        }
        
        val bassIndexRange = (numBins * 0.1).toInt().coerceAtLeast(10)
        for (i in 0 until 10) {
            val startIndex = (i * bassIndexRange / 10).coerceIn(0, numBins - 1)
            val endIndex = ((i + 1) * bassIndexRange / 10).coerceIn(0, numBins - 1)
            var sum = 0f
            var count = 0
            for (j in startIndex until endIndex) {
                sum += magnitudes[j]
                count++
            }
            val avgLevel = if (count > 0) sum / count else 0f
            bassLevels[i] = (avgLevel * performanceConfig.bassLevelBoost).coerceIn(0f, 1f)
        }
        
        val trebleStart = (numBins * 0.5).toInt()
        for (i in 0 until 10) {
            val index = (trebleStart + i * (numBins - trebleStart) / 10).coerceIn(0, numBins - 1)
            trebleLevels[i] = (magnitudes[index] * 1.3f).coerceIn(0f, 1f)
        }
        
        for (i in 0 until 20) {
            val index = (i * numBins / 20).coerceIn(0, numBins - 1)
            frequencyBands[i] = (magnitudes[index] * 1.4f).coerceIn(0f, 1f)
        }
        
        if (performanceConfig.enableSubBassWave) {
            val binFrequency = currentSamplingRate.toFloat() / captureSize
            val minSubBassFreq = 20f
            val maxSubBassFreq = 100f
            
            val subBassBins = mutableListOf<Pair<Int, Float>>()
            for (binIndex in 0 until numBins) {
                val centerFreq = binIndex * binFrequency
                if (centerFreq >= minSubBassFreq && centerFreq <= maxSubBassFreq) {
                    subBassBins.add(Pair(binIndex, magnitudes[binIndex]))
                }
            }
            
            if (subBassBins.isEmpty()) {
                val fallbackBin = kotlin.math.min(1, magnitudes.size - 1)
                for (i in 0 until 64) {
                    subBassWave[i] = (magnitudes[fallbackBin] * 3.0f).coerceIn(0f, 1f)
                }
            } else if (subBassBins.size == 1) {
                val magnitude = subBassBins[0].second
                for (i in 0 until 64) {
                    subBassWave[i] = (magnitude * 3.0f).coerceIn(0f, 1f)
                }
            } else {
                for (i in 0 until 64) {
                    val position = i * (subBassBins.size - 1) / 63.0
                    val lowerIndex = position.toInt().coerceIn(0, subBassBins.size - 1)
                    val upperIndex = (lowerIndex + 1).coerceAtMost(subBassBins.size - 1)
                    val fraction = (position - lowerIndex).toFloat()
                    
                    val interpolatedMagnitude = subBassBins[lowerIndex].second * (1 - fraction) + 
                                               subBassBins[upperIndex].second * fraction
                    subBassWave[i] = (interpolatedMagnitude * 3.0f).coerceIn(0f, 1f)
                }
            }
            _subBassWave.value = subBassWave
        }
        
        _fftData.value = magnitudes
        _bassLevels.value = bassLevels
        _trebleLevels.value = trebleLevels
        _frequencyBands.value = frequencyBands
    }
    
    private fun releaseVisualizer() {
        try {
            visualizer?.enabled = false
            visualizer?.release()
            visualizer = null
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing visualizer", e)
        }
    }
    
    fun release() {
        releaseVisualizer()
    }
}
