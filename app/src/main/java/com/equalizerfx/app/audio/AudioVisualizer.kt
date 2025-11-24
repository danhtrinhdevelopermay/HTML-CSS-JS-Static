package com.equalizerfx.app.audio

import android.media.audiofx.Visualizer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.log10
import kotlin.math.sqrt

class AudioVisualizer(private val audioSessionId: Int) {
    private var visualizer: Visualizer? = null
    
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
    
    companion object {
        private const val TAG = "AudioVisualizer"
        private const val CAPTURE_SIZE = 1024
    }
    
    init {
        initializeVisualizer()
    }
    
    private fun initializeVisualizer() {
        try {
            visualizer = Visualizer(audioSessionId).apply {
                captureSize = CAPTURE_SIZE
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
                            fft?.let { processFft(it) }
                        }
                    },
                    Visualizer.getMaxCaptureRate(),
                    true,
                    true
                )
                enabled = true
            }
            Log.d(TAG, "Visualizer initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing visualizer", e)
        }
    }
    
    private fun processWaveform(waveform: ByteArray) {
        val normalized = FloatArray(128)
        for (i in 0 until 128.coerceAtMost(waveform.size)) {
            normalized[i] = (waveform[i].toInt() + 128) / 256f
        }
        _waveformData.value = normalized
    }
    
    private fun processFft(fft: ByteArray) {
        val magnitudes = FloatArray(128)
        val bassLevels = FloatArray(10)
        val trebleLevels = FloatArray(10)
        val frequencyBands = FloatArray(20)
        
        for (i in 0 until 128) {
            val rfk = fft[2 * i].toInt()
            val ifk = fft[2 * i + 1].toInt()
            val magnitude = sqrt((rfk * rfk + ifk * ifk).toFloat())
            magnitudes[i] = magnitude
        }
        
        val maxMagnitude = magnitudes.maxOrNull() ?: 1f
        for (i in magnitudes.indices) {
            magnitudes[i] = (magnitudes[i] / maxMagnitude).coerceIn(0f, 1f)
        }
        
        for (i in 0 until 10) {
            val index = (i * 12.8).toInt().coerceIn(0, 127)
            bassLevels[i] = magnitudes[index]
        }
        
        for (i in 0 until 10) {
            val index = (64 + i * 6.4).toInt().coerceIn(0, 127)
            trebleLevels[i] = magnitudes[index]
        }
        
        for (i in 0 until 20) {
            val index = (i * 6.4).toInt().coerceIn(0, 127)
            frequencyBands[i] = magnitudes[index]
        }
        
        _fftData.value = magnitudes
        _bassLevels.value = bassLevels
        _trebleLevels.value = trebleLevels
        _frequencyBands.value = frequencyBands
    }
    
    fun release() {
        try {
            visualizer?.release()
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing visualizer", e)
        }
    }
}
