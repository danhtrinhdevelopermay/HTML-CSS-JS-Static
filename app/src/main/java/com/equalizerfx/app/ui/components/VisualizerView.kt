package com.equalizerfx.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.abs

@Composable
fun VisualizerView(
    waveformData: FloatArray,
    bassLevels: FloatArray,
    trebleLevels: FloatArray,
    frequencyBands: FloatArray,
    subBassWave: FloatArray,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1E1E)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "VISUALIZER",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                text = "SUB-BASS WAVE (20Hz - 100Hz)",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF00BCD4)
            )
            SubBassWaveVisualizer(
                subBassWave = subBassWave,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .padding(bottom = 12.dp)
            )
            
            WaveformVisualizer(
                waveformData = waveformData,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(bottom = 8.dp)
            )
            
            Text(
                text = "BASS",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
            BarsVisualizer(
                levels = bassLevels,
                color = Color(0xFF00BCD4),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(bottom = 8.dp)
            )
            
            Text(
                text = "TREBLE",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
            BarsVisualizer(
                levels = trebleLevels,
                color = Color(0xFFFF5722),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(bottom = 8.dp)
            )
            
            Text(
                text = "FREQUENCY BANDS (20Hz - 20kHz)",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
            BarsVisualizer(
                levels = frequencyBands,
                color = Color(0xFF6200EE),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
            )
        }
    }
}

@Composable
fun WaveformVisualizer(
    waveformData: FloatArray,
    modifier: Modifier = Modifier
) {
    var smoothedData by remember { mutableStateOf(waveformData) }
    
    LaunchedEffect(waveformData) {
        smoothedData = smoothWaveform(waveformData, smoothedData)
    }
    
    Canvas(modifier = modifier.background(Color(0xFF0A0A0A))) {
        if (smoothedData.isEmpty()) return@Canvas
        
        val width = size.width
        val height = size.height
        val centerY = height / 2
        
        val path = Path()
        val step = width / smoothedData.size
        
        path.moveTo(0f, centerY)
        
        for (i in smoothedData.indices) {
            val x = i * step
            val y = centerY + (smoothedData[i] - 0.5f) * height * 1.8f
            
            if (i == 0) {
                path.moveTo(x, y)
            } else {
                val prevX = (i - 1) * step
                val prevY = centerY + (smoothedData[i - 1] - 0.5f) * height * 1.8f
                val controlX = (prevX + x) / 2
                path.quadraticBezierTo(controlX, prevY, x, y)
            }
        }
        
        drawPath(
            path = path,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color(0xFF6200EE),
                    Color(0xFF03DAC6),
                    Color(0xFF6200EE)
                )
            ),
            style = Stroke(width = 3f),
            alpha = 0.9f
        )
    }
}

private fun smoothWaveform(newData: FloatArray, oldData: FloatArray): FloatArray {
    val smoothFactor = 0.3f
    return FloatArray(newData.size) { i ->
        if (i < oldData.size) {
            oldData[i] + (newData[i] - oldData[i]) * smoothFactor
        } else {
            newData[i]
        }
    }
}

@Composable
fun BarsVisualizer(
    levels: FloatArray,
    color: Color,
    modifier: Modifier = Modifier
) {
    var smoothedLevels by remember { mutableStateOf(levels) }
    
    LaunchedEffect(levels) {
        smoothedLevels = smoothBars(levels, smoothedLevels)
    }
    
    Canvas(modifier = modifier.background(Color(0xFF0A0A0A))) {
        if (smoothedLevels.isEmpty()) return@Canvas
        
        val width = size.width
        val height = size.height
        val barWidth = (width / smoothedLevels.size) * 0.75f
        val spacing = (width / smoothedLevels.size) * 0.25f
        
        for (i in smoothedLevels.indices) {
            val barHeight = (smoothedLevels[i] * height * 1.5f).coerceAtMost(height)
            val x = i * (barWidth + spacing)
            val y = height - barHeight
            
            val gradient = Brush.verticalGradient(
                colors = listOf(
                    color.copy(alpha = 1f),
                    color.copy(alpha = 0.6f),
                    color.copy(alpha = 0.3f)
                ),
                startY = y,
                endY = height
            )
            
            drawRoundRect(
                brush = gradient,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 4, barWidth / 4),
                alpha = 0.95f
            )
            
            if (smoothedLevels[i] > 0.6f) {
                drawRoundRect(
                    color = color.copy(alpha = 0.3f),
                    topLeft = Offset(x - 2f, y - 2f),
                    size = Size(barWidth + 4f, barHeight + 2f),
                    cornerRadius = CornerRadius(barWidth / 4, barWidth / 4),
                    alpha = 0.5f
                )
            }
        }
    }
}

private fun smoothBars(newLevels: FloatArray, oldLevels: FloatArray): FloatArray {
    val smoothFactor = 0.4f
    return FloatArray(newLevels.size) { i ->
        if (i < oldLevels.size) {
            oldLevels[i] + (newLevels[i] - oldLevels[i]) * smoothFactor
        } else {
            newLevels[i]
        }
    }
}

@Composable
fun SubBassWaveVisualizer(
    subBassWave: FloatArray,
    modifier: Modifier = Modifier
) {
    var smoothedWave by remember { mutableStateOf(subBassWave) }
    var phase by remember { mutableStateOf(0f) }
    
    LaunchedEffect(subBassWave) {
        smoothedWave = smoothSubBassWave(subBassWave, smoothedWave)
    }
    
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(16)
            phase += 0.05f
            if (phase > 2 * kotlin.math.PI) {
                phase -= (2 * kotlin.math.PI).toFloat()
            }
        }
    }
    
    Canvas(modifier = modifier.background(Color(0xFF0A0A0A))) {
        if (smoothedWave.isEmpty()) return@Canvas
        
        val width = size.width
        val height = size.height
        val centerY = height / 2
        
        val avgAmplitude = smoothedWave.average().toFloat()
        
        for (layer in 0 until 3) {
            val path = Path()
            val step = width / smoothedWave.size
            val layerOffset = layer * 15f
            val layerAlpha = 1f - (layer * 0.25f)
            
            path.moveTo(0f, centerY)
            
            for (i in smoothedWave.indices) {
                val x = i * step
                val baseAmplitude = smoothedWave[i] * height * 0.4f
                val waveOffset = kotlin.math.sin((i * 0.2f + phase + layer * 0.5f).toDouble()).toFloat() * 20f
                val y1 = centerY - baseAmplitude - layerOffset + waveOffset
                val y2 = centerY + baseAmplitude + layerOffset - waveOffset
                
                if (i == 0) {
                    path.moveTo(x, y1)
                } else {
                    val prevX = (i - 1) * step
                    val prevAmplitude = smoothedWave[i - 1] * height * 0.4f
                    val prevWaveOffset = kotlin.math.sin(((i - 1) * 0.2f + phase + layer * 0.5f).toDouble()).toFloat() * 20f
                    val prevY1 = centerY - prevAmplitude - layerOffset + prevWaveOffset
                    val controlX = (prevX + x) / 2
                    path.quadraticBezierTo(controlX, prevY1, x, y1)
                }
            }
            
            for (i in smoothedWave.indices.reversed()) {
                val x = i * step
                val baseAmplitude = smoothedWave[i] * height * 0.4f
                val waveOffset = kotlin.math.sin((i * 0.2f + phase + layer * 0.5f).toDouble()).toFloat() * 20f
                val y2 = centerY + baseAmplitude + layerOffset - waveOffset
                
                if (i == smoothedWave.size - 1) {
                    path.lineTo(x, y2)
                } else {
                    val nextX = (i + 1) * step
                    val nextAmplitude = smoothedWave[i + 1] * height * 0.4f
                    val nextWaveOffset = kotlin.math.sin(((i + 1) * 0.2f + phase + layer * 0.5f).toDouble()).toFloat() * 20f
                    val nextY2 = centerY + nextAmplitude + layerOffset - nextWaveOffset
                    val controlX = (x + nextX) / 2
                    path.quadraticBezierTo(controlX, nextY2, x, y2)
                }
            }
            
            path.close()
            
            val gradient = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF1E88E5).copy(alpha = 0.6f * layerAlpha),
                    Color(0xFF00BCD4).copy(alpha = 0.4f * layerAlpha),
                    Color(0xFF1E88E5).copy(alpha = 0.3f * layerAlpha)
                )
            )
            
            drawPath(
                path = path,
                brush = gradient,
                alpha = layerAlpha
            )
            
            if (layer == 0 && avgAmplitude > 0.3f) {
                drawPath(
                    path = path,
                    color = Color(0xFF00BCD4).copy(alpha = 0.2f),
                    style = Stroke(width = 2f),
                    alpha = (avgAmplitude - 0.3f) * 2f
                )
            }
        }
        
        if (avgAmplitude > 0.5f) {
            val pulseRadius = 40f + (avgAmplitude * 60f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF00BCD4).copy(alpha = 0.4f),
                        Color.Transparent
                    ),
                    center = Offset(width / 2, centerY),
                    radius = pulseRadius
                ),
                center = Offset(width / 2, centerY),
                radius = pulseRadius
            )
        }
    }
}

private fun smoothSubBassWave(newWave: FloatArray, oldWave: FloatArray): FloatArray {
    val smoothFactor = 0.35f
    return FloatArray(newWave.size) { i ->
        if (i < oldWave.size) {
            oldWave[i] + (newWave[i] - oldWave[i]) * smoothFactor
        } else {
            newWave[i]
        }
    }
}
