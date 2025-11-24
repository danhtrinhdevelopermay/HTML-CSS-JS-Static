package com.equalizerfx.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.equalizerfx.app.settings.PerformanceMode

@Composable
fun PerformanceSelector(
    currentMode: PerformanceMode,
    onModeChange: (PerformanceMode) -> Unit,
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
                text = "PERFORMANCE MODE",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                text = when (currentMode) {
                    PerformanceMode.LOW -> "🐌 Chip yếu - Tiết kiệm tối đa"
                    PerformanceMode.MEDIUM -> "⚡ Cân bằng - Khuyến nghị"
                    PerformanceMode.HIGH -> "🚀 Chip mạnh - Hiệu ứng đầy đủ"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF03DAC6),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PerformanceMode.values().forEach { mode ->
                    Button(
                        onClick = { onModeChange(mode) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (currentMode == mode) 
                                Color(0xFF6200EE) 
                            else 
                                Color(0xFF424242)
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = when (mode) {
                                PerformanceMode.LOW -> "Thấp"
                                PerformanceMode.MEDIUM -> "Trung bình"
                                PerformanceMode.HIGH -> "Cao"
                            }
                        )
                    }
                }
            }
            
            Text(
                text = when (currentMode) {
                    PerformanceMode.LOW -> "• Tắt Sub-Bass Wave\n• Tắt Image Pulse\n• Giảm FPS: 30\n• Không hiệu ứng phát sáng"
                    PerformanceMode.MEDIUM -> "• Bật Sub-Bass Wave (2 lớp)\n• Bật Image Pulse\n• FPS: 45\n• Không hiệu ứng phát sáng"
                    PerformanceMode.HIGH -> "• Bật tất cả visualizer\n• Sub-Bass Wave (3 lớp)\n• FPS: 60\n• Hiệu ứng phát sáng đầy đủ"
                },
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
