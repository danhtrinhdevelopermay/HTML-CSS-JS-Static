package com.equalizerfx.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.equalizerfx.app.audio.EqualizerBand

@Composable
fun EqualizerView(
    bands: List<EqualizerBand>,
    onBandLevelChange: (Int, Int) -> Unit,
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
                text = "20-BAND EQUALIZER",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(300.dp)
            ) {
                itemsIndexed(bands) { index, band ->
                    EqualizerBandSlider(
                        band = band,
                        onLevelChange = { level ->
                            onBandLevelChange(index, level)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun EqualizerBandSlider(
    band: EqualizerBand,
    onLevelChange: (Int) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(40.dp)
    ) {
        Text(
            text = "${band.level}",
            fontSize = 10.sp,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        
        Slider(
            value = band.level.toFloat(),
            onValueChange = { onLevelChange(it.toInt()) },
            valueRange = -1500f..1500f,
            modifier = Modifier
                .height(200.dp)
                .width(40.dp)
                .graphicsLayer {
                    rotationZ = 270f
                    transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0f, 0f)
                }
                .layout { measurable, constraints ->
                    val placeable = measurable.measure(
                        androidx.compose.ui.unit.Constraints(
                            minWidth = constraints.minHeight,
                            maxWidth = constraints.maxHeight,
                            minHeight = constraints.minWidth,
                            maxHeight = constraints.maxWidth,
                        )
                    )
                    layout(placeable.height, placeable.width) {
                        placeable.place(-placeable.width, 0)
                    }
                },
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF6200EE),
                activeTrackColor = Color(0xFF6200EE),
                inactiveTrackColor = Color(0xFF3700B3)
            )
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = formatFrequency(band.frequency),
            fontSize = 8.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            lineHeight = 10.sp
        )
    }
}

private fun formatFrequency(freq: Int): String {
    return when {
        freq >= 1000 -> "${freq / 1000}k"
        else -> "$freq"
    }
}
