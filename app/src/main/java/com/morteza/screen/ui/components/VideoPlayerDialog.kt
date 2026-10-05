package com.morteza.screen.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.morteza.screen.model.VideoItem
import com.morteza.screen.ui.theme.AccentRed
import com.morteza.screen.ui.theme.TealDark
import com.morteza.screen.ui.theme.TealPrimary
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun VideoPlayerDialog(
    video: VideoItem?,
    onDismiss: () -> Unit,
    onDelete: (VideoItem) -> Unit,
    onShare: (VideoItem) -> Unit
) {
    if (video == null) return

    var isPlaying by remember { mutableStateOf(false) }
    var currentProgress by remember { mutableFloatStateOf(0.35f) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(TealDark)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = video.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                // Video Stage / Visualizer Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    // BitmapOverlay GL Watermark badge (from original BitmapOverlayVideoProcessor)
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(AccentRed, RoundedCornerShape(4.dp))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "OVERLAY GL • ${video.resolution}",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Play / Pause central button
                    IconButton(
                        onClick = { isPlaying = !isPlaying },
                        modifier = Modifier
                            .size(64.dp)
                            .background(TealPrimary.copy(alpha = 0.85f), RoundedCornerShape(32.dp))
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                // Scrubber Progress Bar
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Slider(
                        value = currentProgress,
                        onValueChange = { currentProgress = it },
                        colors = SliderDefaults.colors(
                            thumbColor = TealPrimary,
                            activeTrackColor = TealPrimary,
                            inactiveTrackColor = Color.DarkGray
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val currentSec = (currentProgress * video.durationSeconds).toLong()
                        Text(
                            text = String.format("%02d:%02d", currentSec / 60, currentSec % 60),
                            fontSize = 12.sp,
                            color = Color.LightGray
                        )
                        Text(
                            text = String.format("%02d:%02d", video.durationSeconds / 60, video.durationSeconds % 60),
                            fontSize = 12.sp,
                            color = Color.LightGray
                        )
                    }
                }

                // Metadata Details
                val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(video.timestamp))
                val sizeMb = String.format("%.1f MB", video.sizeBytes / (1024f * 1024f))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Resolution: ${video.resolution}", fontSize = 12.sp, color = Color.Gray)
                    Text("Size: $sizeMb", fontSize = 12.sp, color = Color.Gray)
                    Text("Date: $dateStr", fontSize = 12.sp, color = Color.Gray)
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFF334155))

                // Action Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { onShare(video) }) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = TealPrimary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Share", color = TealPrimary)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    TextButton(onClick = { onDelete(video) }) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = AccentRed)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete", color = AccentRed)
                    }
                }
            }
        }
    }
}
