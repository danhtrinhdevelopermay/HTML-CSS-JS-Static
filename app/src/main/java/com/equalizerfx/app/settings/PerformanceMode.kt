package com.equalizerfx.app.settings

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class PerformanceMode {
    LOW,
    MEDIUM,
    HIGH;
    
    companion object {
        fun fromString(value: String): PerformanceMode {
            return try {
                valueOf(value.uppercase())
            } catch (e: Exception) {
                MEDIUM
            }
        }
    }
}

data class PerformanceConfig(
    val visualizerCaptureRate: Int,
    val visualizerCaptureSize: Int,
    val animationFPS: Int,
    val enableSubBassWave: Boolean,
    val subBassWaveLayers: Int,
    val enableImagePulse: Boolean,
    val enableGlowEffects: Boolean,
    val bassLevelBoost: Float,
    val waveformDataPoints: Int
) {
    companion object {
        fun forMode(mode: PerformanceMode): PerformanceConfig {
            return when (mode) {
                PerformanceMode.LOW -> PerformanceConfig(
                    visualizerCaptureRate = 10000,
                    visualizerCaptureSize = 512,
                    animationFPS = 30,
                    enableSubBassWave = false,
                    subBassWaveLayers = 1,
                    enableImagePulse = false,
                    enableGlowEffects = false,
                    bassLevelBoost = 1.5f,
                    waveformDataPoints = 64
                )
                PerformanceMode.MEDIUM -> PerformanceConfig(
                    visualizerCaptureRate = 20000,
                    visualizerCaptureSize = 1024,
                    animationFPS = 45,
                    enableSubBassWave = true,
                    subBassWaveLayers = 2,
                    enableImagePulse = true,
                    enableGlowEffects = false,
                    bassLevelBoost = 2.0f,
                    waveformDataPoints = 128
                )
                PerformanceMode.HIGH -> PerformanceConfig(
                    visualizerCaptureRate = 30000,
                    visualizerCaptureSize = 1024,
                    animationFPS = 60,
                    enableSubBassWave = true,
                    subBassWaveLayers = 3,
                    enableImagePulse = true,
                    enableGlowEffects = true,
                    bassLevelBoost = 2.2f,
                    waveformDataPoints = 128
                )
            }
        }
    }
}

class PerformanceSettings(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "performance_settings",
        Context.MODE_PRIVATE
    )
    
    private val _performanceMode = MutableStateFlow(loadPerformanceMode())
    val performanceMode: StateFlow<PerformanceMode> = _performanceMode
    
    private val _config = MutableStateFlow(PerformanceConfig.forMode(_performanceMode.value))
    val config: StateFlow<PerformanceConfig> = _config
    
    private fun loadPerformanceMode(): PerformanceMode {
        val savedMode = prefs.getString(KEY_PERFORMANCE_MODE, PerformanceMode.MEDIUM.name)
        return PerformanceMode.fromString(savedMode ?: PerformanceMode.MEDIUM.name)
    }
    
    fun setPerformanceMode(mode: PerformanceMode) {
        _performanceMode.value = mode
        _config.value = PerformanceConfig.forMode(mode)
        prefs.edit().putString(KEY_PERFORMANCE_MODE, mode.name).apply()
    }
    
    companion object {
        private const val KEY_PERFORMANCE_MODE = "performance_mode"
    }
}
