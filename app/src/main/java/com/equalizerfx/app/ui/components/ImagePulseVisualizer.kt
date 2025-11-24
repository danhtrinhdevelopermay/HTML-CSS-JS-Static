package com.equalizerfx.app.ui.components

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlin.math.pow

@Composable
fun ImagePulseVisualizer(
    bassLevels: FloatArray,
    selectedImageUri: Uri?,
    onSelectImage: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val avgBass = remember(bassLevels) {
        if (bassLevels.isNotEmpty()) {
            bassLevels.average().toFloat().pow(1.5f)
        } else {
            0f
        }
    }
    
    var smoothBass by remember { mutableStateOf(0f) }
    
    LaunchedEffect(avgBass) {
        smoothBass = smoothBass * 0.6f + avgBass * 0.4f
    }
    
    val scale by animateFloatAsState(
        targetValue = 1f + (smoothBass * 0.4f),
        animationSpec = tween(
            durationMillis = 100,
            easing = FastOutSlowInEasing
        ),
        label = "bass_pulse"
    )
    
    val glowAlpha by animateFloatAsState(
        targetValue = smoothBass * 0.8f,
        animationSpec = tween(
            durationMillis = 150,
            easing = LinearEasing
        ),
        label = "bass_glow"
    )
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1E1E)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "BASS PULSE IMAGE",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(Color(0xFF0A0A0A))
                    .border(
                        width = 2.dp,
                        color = Color(0xFF6200EE).copy(alpha = 0.3f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUri != null) {
                    val bitmap = remember(selectedImageUri) {
                        try {
                            context.contentResolver.openInputStream(selectedImageUri)?.use { stream ->
                                BitmapFactory.decodeStream(stream)
                            }
                        } catch (e: Exception) {
                            null
                        }
                    }
                    
                    if (bitmap != null) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            if (glowAlpha > 0.3f) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .scale(scale * 1.05f)
                                        .background(
                                            Brush.radialGradient(
                                                colors = listOf(
                                                    Color(0xFF6200EE).copy(alpha = glowAlpha * 0.4f),
                                                    Color.Transparent
                                                )
                                            )
                                        )
                                )
                            }
                            
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Pulse Image",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(32.dp)
                                    .scale(scale),
                                contentScale = ContentScale.Fit
                            )
                        }
                    } else {
                        Text(
                            text = "Không thể load hình ảnh",
                            color = Color.Gray
                        )
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = "Add Image",
                            modifier = Modifier.size(64.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Chưa có hình ảnh",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onSelectImage,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6200EE)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.AddPhotoAlternate,
                    contentDescription = "Select Image"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (selectedImageUri == null) "Chọn Hình Ảnh" else "Đổi Hình Ảnh")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LinearProgressIndicator(
                progress = smoothBass.coerceIn(0f, 1f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(MaterialTheme.shapes.small),
                color = Color(0xFF00BCD4),
                trackColor = Color(0xFF0A0A0A)
            )
            
            Text(
                text = "Bass Level: ${(smoothBass * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
