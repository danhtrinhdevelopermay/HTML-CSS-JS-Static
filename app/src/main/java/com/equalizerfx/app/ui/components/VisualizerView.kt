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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.dp
import kotlin.math.abs

@Composable
fun VisualizerView(
    waveformData: FloatArray,
    bassLevels: FloatArray,
    trebleLevels: FloatArray,
    frequencyBands: FloatArray,
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
                text = "FREQUENCY BANDS",
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
