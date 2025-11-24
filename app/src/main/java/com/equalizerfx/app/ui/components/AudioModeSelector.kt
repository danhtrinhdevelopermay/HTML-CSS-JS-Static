package com.equalizerfx.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.equalizerfx.app.audio.AudioMode

@Composable
fun AudioModeSelector(
    currentMode: AudioMode,
    onModeChange: (AudioMode) -> Unit,
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
                text = "AUDIO SOURCE",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = currentMode == AudioMode.SYSTEM_AUDIO,
                    onClick = { onModeChange(AudioMode.SYSTEM_AUDIO) },
                    label = {
                        Text(
                            text = "System Audio",
                            color = if (currentMode == AudioMode.SYSTEM_AUDIO) Color.White else Color.Gray
                        )
                    },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF6200EE),
                        containerColor = Color(0xFF2A2A2A)
                    )
                )
                
                FilterChip(
                    selected = currentMode == AudioMode.FILE_PLAYBACK,
                    onClick = { onModeChange(AudioMode.FILE_PLAYBACK) },
                    label = {
                        Text(
                            text = "File Playback",
                            color = if (currentMode == AudioMode.FILE_PLAYBACK) Color.White else Color.Gray
                        )
                    },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF6200EE),
                        containerColor = Color(0xFF2A2A2A)
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = if (currentMode == AudioMode.SYSTEM_AUDIO) {
                    "⚠️ System Audio (chỉ hoạt động trên thiết bị root/custom ROM)"
                } else {
                    "✓ File Playback - Điều chỉnh file nhạc trong app"
                },
                style = MaterialTheme.typography.bodySmall,
                color = if (currentMode == AudioMode.SYSTEM_AUDIO) Color(0xFFFFAA00) else Color(0xFF4CAF50)
            )
        }
    }
}
