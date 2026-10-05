package com.morteza.screen.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.morteza.screen.model.AppScreen
import com.morteza.screen.model.RecordingStatus
import com.morteza.screen.ui.theme.AccentOrange
import com.morteza.screen.ui.theme.AccentRed
import com.morteza.screen.ui.theme.TealDark
import com.morteza.screen.ui.theme.TealPrimary
import com.morteza.screen.viewmodel.ScreenRecorderViewModel

@Composable
fun HomeScreen(
    viewModel: ScreenRecorderViewModel,
    modifier: Modifier = Modifier
) {
    val status by viewModel.recordingStatus.collectAsState()
    val elapsedSeconds by viewModel.elapsedSeconds.collectAsState()
    val videoConfig by viewModel.videoConfig.collectAsState()
    val audioConfig by viewModel.audioConfig.collectAsState()
    val videos by viewModel.videos.collectAsState()
    val isFloatingVisible by viewModel.isFloatingMenuVisible.collectAsState()

    val isRecording = status == RecordingStatus.RECORDING || status == RecordingStatus.PAUSED

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero / Recording Status Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isRecording) "RECORDING ACTIVE" else "SCREEN RECORDER",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isRecording) AccentRed else TealPrimary
                        )

                        if (isRecording) {
                            val mins = elapsedSeconds / 60
                            val secs = elapsedSeconds % 60
                            Text(
                                text = String.format("%02d:%02d", mins, secs),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                    }

                    Text(
                        text = if (isRecording) "Screen & Audio are being captured" else "Ready to capture display with Floating Circular Menu",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    // Control Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (!isRecording) {
                            Button(
                                onClick = { viewModel.startRecordingFlow() },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.FiberManualRecord, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Start Recording")
                            }
                        } else {
                            Button(
                                onClick = { viewModel.stopRecording() },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Stop, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Stop & Save")
                            }

                            FilledTonalButton(
                                onClick = {
                                    if (status == RecordingStatus.PAUSED) viewModel.resumeRecording()
                                    else viewModel.pauseRecording()
                                },
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(
                                    imageVector = if (status == RecordingStatus.PAUSED) Icons.Default.PlayArrow else Icons.Default.Pause,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (status == RecordingStatus.PAUSED) "Resume" else "Pause")
                            }
                        }

                        OutlinedButton(
                            onClick = { viewModel.togglePainter() },
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.Palette, contentDescription = null, tint = TealPrimary)
                        }
                    }
                }
            }
        }

        // Specs & Config Quick Chips
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickChip(
                    label = videoConfig.resolution,
                    sub = "${videoConfig.framerate} FPS",
                    icon = Icons.Default.Videocam,
                    modifier = Modifier.weight(1f)
                )
                QuickChip(
                    label = "${videoConfig.bitrate / 1000} kbps",
                    sub = "AVC / H.264",
                    icon = Icons.Default.Layers,
                    modifier = Modifier.weight(1f)
                )
                QuickChip(
                    label = if (audioConfig.includeMic) "Mic Enabled" else "No Mic",
                    sub = "AAC Audio",
                    icon = Icons.Default.Mic,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Floating Circular Menu Spotlight Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.6f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(TealDark, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.TouchApp, contentDescription = null, tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Floating Circular Menu",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Drag anywhere on screen, tap to open radial tools",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Switch(
                        checked = isFloatingVisible,
                        onCheckedChange = { viewModel.toggleFloatingMenu() },
                        colors = SwitchDefaults.colors(checkedThumbColor = TealPrimary)
                    )
                }
            }
        }

        // Recent Recordings Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Recordings (${videos.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                TextButton(onClick = { viewModel.navigateTo(AppScreen.GALLERY) }) {
                    Text("View All", color = TealPrimary)
                }
            }
        }

        if (videos.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No recordings yet. Tap 'Start Recording' to begin!", color = Color.Gray)
                }
            }
        } else {
            items(videos.take(3)) { video ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.selectVideoForPreview(video) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.Black),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = TealPrimary)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = video.name,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                maxLines = 1,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            val mins = video.durationSeconds / 60
                            val secs = video.durationSeconds % 60
                            Text(
                                text = "${video.resolution} • ${String.format("%02d:%02d", mins, secs)} • ${(video.sizeBytes / (1024f * 1024f)).toInt()} MB",
                                color = Color.Gray,
                                fontSize = 11.sp
                            )
                        }

                        IconButton(onClick = { viewModel.selectVideoForPreview(video) }) {
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickChip(
    label: String,
    sub: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.8f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = label, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
            Text(text = sub, color = Color.Gray, fontSize = 10.sp)
        }
    }
}
