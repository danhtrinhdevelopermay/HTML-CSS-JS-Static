package com.equalizerfx.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp

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
    Canvas(modifier = modifier.background(Color(0xFF0A0A0A))) {
        if (waveformData.isEmpty()) return@Canvas
        
        val width = size.width
        val height = size.height
        val centerY = height / 2
        
        val path = Path()
        val step = width / waveformData.size
        
        path.moveTo(0f, centerY)
        
        for (i in waveformData.indices) {
            val x = i * step
            val y = centerY + (waveformData[i] - 0.5f) * height
            path.lineTo(x, y)
        }
        
        drawPath(
            path = path,
            color = Color(0xFF6200EE),
            alpha = 0.8f
        )
    }
}

@Composable
fun BarsVisualizer(
    levels: FloatArray,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.background(Color(0xFF0A0A0A))) {
        if (levels.isEmpty()) return@Canvas
        
        val width = size.width
        val height = size.height
        val barWidth = (width / levels.size) * 0.8f
        val spacing = (width / levels.size) * 0.2f
        
        for (i in levels.indices) {
            val barHeight = levels[i] * height
            val x = i * (barWidth + spacing)
            val y = height - barHeight
            
            drawRect(
                color = color,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                alpha = 0.8f
            )
        }
    }
}
