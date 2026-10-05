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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.morteza.screen.model.VideoItem
import com.morteza.screen.ui.theme.AccentOrange
import com.morteza.screen.ui.theme.TealDark
import com.morteza.screen.ui.theme.TealPrimary
import kotlin.math.roundToLong

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoTrimmingDialog(
    video: VideoItem?,
    onDismiss: () -> Unit,
    onSaveTrim: (startSec: Long, endSec: Long, saveAsNew: Boolean) -> Unit
) {
    if (video == null) return

    val totalDuration = video.durationSeconds.coerceAtLeast(2L)

    var startSec by remember(video) { mutableFloatStateOf(0f) }
    var endSec by remember(video) { mutableFloatStateOf(totalDuration.toFloat()) }
    var isPreviewPlaying by remember { mutableStateOf(false) }
    var saveAsNewVideo by remember { mutableStateOf(true) }

    fun formatTime(seconds: Long): String {
        val m = seconds / 60
        val s = seconds % 60
        return String.format("%02d:%02d", m, s)
    }

    val trimmedDuration = (endSec.roundToLong() - startSec.roundToLong()).coerceAtLeast(1L)
    val trimmedSizeMb = (video.sizeBytes * (trimmedDuration.toDouble() / totalDuration) / (1024 * 1024))

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight()
                .padding(16.dp),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(TealDark),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.ContentCut, contentDescription = null, tint = TealPrimary)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Trim Screen Recording", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                            Text(video.name, color = Color.Gray, fontSize = 11.sp, maxLines = 1)
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                // Simulated Preview Window
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        IconButton(
                            onClick = { isPreviewPlaying = !isPreviewPlaying },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = if (isPreviewPlaying) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                                contentDescription = null,
                                tint = TealPrimary,
                                modifier = Modifier.size(44.dp)
                            )
                        }
                        Text(
                            text = if (isPreviewPlaying) "Simulating trimmed playback..." else "Tap to preview trimmed segment",
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )
                    }

                    // Watermark / Trim bounds pill overlay
                    Surface(
                        color = Color.Black.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 8.dp)
                    ) {
                        Text(
                            text = "${formatTime(startSec.roundToLong())} ➔ ${formatTime(endSec.roundToLong())} (Duration: ${formatTime(trimmedDuration)})",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Range Slider Timeline
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Trim Boundaries", color = Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text(
                            text = "Original: ${formatTime(totalDuration)}",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    RangeSlider(
                        value = startSec..endSec,
                        onValueChange = { range ->
                            val s = range.start
                            val e = range.endInclusive
                            if (e - s >= 1f) {
                                startSec = s
                                endSec = e
                            }
                        },
                        valueRange = 0f..totalDuration.toFloat(),
                        steps = (totalDuration - 1).toInt().coerceAtMost(300),
                        colors = SliderDefaults.colors(
                            thumbColor = TealPrimary,
                            activeTrackColor = TealPrimary,
                            inactiveTrackColor = Color.DarkGray
                        )
                    )

                    // Micro-Stepper Adjustment Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Start point controls
                        Column {
                            Text("Start Point", color = Color.Gray, fontSize = 10.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                FilledIconButton(
                                    onClick = { startSec = (startSec - 1f).coerceAtLeast(0f) },
                                    modifier = Modifier.size(24.dp),
                                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color(0xFF334155))
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "-1s", modifier = Modifier.size(12.dp))
                                }
                                Text(
                                    text = formatTime(startSec.roundToLong()),
                                    color = Color.White,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp)
                                )
                                FilledIconButton(
                                    onClick = { startSec = (startSec + 1f).coerceAtMost(endSec - 1f) },
                                    modifier = Modifier.size(24.dp),
                                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color(0xFF334155))
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "+1s", modifier = Modifier.size(12.dp))
                                }
                            }
                        }

                        // End point controls
                        Column(horizontalAlignment = Alignment.End) {
                            Text("End Point", color = Color.Gray, fontSize = 10.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                FilledIconButton(
                                    onClick = { endSec = (endSec - 1f).coerceAtLeast(startSec + 1f) },
                                    modifier = Modifier.size(24.dp),
                                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color(0xFF334155))
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "-1s", modifier = Modifier.size(12.dp))
                                }
                                Text(
                                    text = formatTime(endSec.roundToLong()),
                                    color = Color.White,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp)
                                )
                                FilledIconButton(
                                    onClick = { endSec = (endSec + 1f).coerceAtMost(totalDuration.toFloat()) },
                                    modifier = Modifier.size(24.dp),
                                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color(0xFF334155))
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "+1s", modifier = Modifier.size(12.dp))
                                }
                            }
                        }
                    }
                }

                // Output Details Box
                Surface(
                    color = Color(0xFF0F172A),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Trimmed Duration", color = Color.Gray, fontSize = 11.sp)
                            Text(
                                text = formatTime(trimmedDuration),
                                fontWeight = FontWeight.Bold,
                                color = TealPrimary,
                                fontSize = 15.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Estimated Size", color = Color.Gray, fontSize = 11.sp)
                            Text(
                                text = String.format("%.1f MB", trimmedSizeMb),
                                fontWeight = FontWeight.Bold,
                                color = AccentOrange,
                                fontSize = 15.sp
                            )
                        }
                    }
                }

                // Save as New vs Overwrite Options
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilterChip(
                        selected = saveAsNewVideo,
                        onClick = { saveAsNewVideo = true },
                        label = { Text("Save as New Video", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = TealPrimary,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = !saveAsNewVideo,
                        onClick = { saveAsNewVideo = false },
                        label = { Text("Overwrite Original", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AccentOrange,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color.Gray)
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Button(
                        onClick = {
                            onSaveTrim(startSec.roundToLong(), endSec.roundToLong(), saveAsNewVideo)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.ContentCut, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Trim & Save")
                    }
                }
            }
        }
    }
}
