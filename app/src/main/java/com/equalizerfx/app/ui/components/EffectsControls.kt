package com.equalizerfx.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun EffectsControls(
    bassBoostLevel: Int,
    trebleBoostLevel: Int,
    reverbLevel: Int,
    effect3DLevel: Int,
    effect8DEnabled: Boolean,
    onBassBoostChange: (Int) -> Unit,
    onTrebleBoostChange: (Int) -> Unit,
    onReverbChange: (Int) -> Unit,
    onEffect3DChange: (Int) -> Unit,
    onEffect8DToggle: (Boolean) -> Unit,
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
                text = "AUDIO EFFECTS",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            EffectSlider(
                label = "Bass Boost",
                value = bassBoostLevel,
                onValueChange = onBassBoostChange,
                valueRange = 0f..1000f,
                color = Color(0xFF00BCD4)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            EffectSlider(
                label = "Treble Boost",
                value = trebleBoostLevel,
                onValueChange = onTrebleBoostChange,
                valueRange = -1500f..1500f,
                color = Color(0xFFFF5722)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            EffectSlider(
                label = "Reverb",
                value = reverbLevel,
                onValueChange = onReverbChange,
                valueRange = 0f..100f,
                color = Color(0xFF4CAF50)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            EffectSlider(
                label = "3D Effect",
                value = effect3DLevel,
                onValueChange = onEffect3DChange,
                valueRange = 0f..1000f,
                color = Color(0xFFFF9800)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "8D Audio Effect",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
                
                Switch(
                    checked = effect8DEnabled,
                    onCheckedChange = onEffect8DToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF6200EE),
                        checkedTrackColor = Color(0xFF3700B3)
                    )
                )
            }
        }
    }
}

@Composable
fun EffectSlider(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White
            )
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
        
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = color,
                activeTrackColor = color,
                inactiveTrackColor = color.copy(alpha = 0.3f)
            )
        )
    }
}
