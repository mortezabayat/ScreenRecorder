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
import com.morteza.screen.model.AudioSourceOption
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
    val limitsConfig by viewModel.recordingLimitsConfig.collectAsState()
    val scheduledRecordings by viewModel.scheduledRecordings.collectAsState()
    var showQualityMenu by remember { mutableStateOf(false) }

    val isRecording = status == RecordingStatus.RECORDING || status == RecordingStatus.PAUSED

    if (showQualityMenu) {
        val currentPresetName = when {
            videoConfig.height <= 480 || videoConfig.width <= 480 -> "480p"
            videoConfig.height <= 720 || videoConfig.width <= 720 -> "720p"
            videoConfig.height <= 1080 || videoConfig.width <= 1080 -> "1080p"
            else -> "1440p"
        }

        AlertDialog(
            onDismissRequest = { showQualityMenu = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.HighQuality, contentDescription = null, tint = TealPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Video Quality & FPS Presets", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Select Resolution Preset:", color = Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)

                    val presets = listOf(
                        Triple("480p", "480p SD", "854x480"),
                        Triple("720p", "720p HD", "1280x720"),
                        Triple("1080p", "1080p FHD", "1920x1080"),
                        Triple("1440p", "1440p 2K", "2560x1440")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        presets.forEach { (presetKey, label, sub) ->
                            val isSelected = currentPresetName == presetKey
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.selectQualityPreset(presetKey) },
                                label = {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Text(sub, fontSize = 9.sp, color = if (isSelected) Color.White.copy(alpha = 0.8f) else Color.Gray)
                                    }
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = TealPrimary,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    HorizontalDivider(color = Color(0xFF334155))

                    Text("Frame Rate (FPS):", color = Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = videoConfig.framerate == 30,
                            onClick = { viewModel.selectFramerate(30) },
                            label = { Text("30 FPS (Standard)", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = TealPrimary,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = videoConfig.framerate == 60,
                            onClick = { viewModel.selectFramerate(60) },
                            label = { Text("60 FPS (Ultra-Smooth)", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = TealPrimary,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Surface(
                        color = Color(0xFF0F172A),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val mbPerMin = (videoConfig.bitrate.toLong() * 60L) / (8L * 1024L * 1024L)
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Active: ${videoConfig.resolution} @ ${videoConfig.framerate}fps", color = Color.LightGray, fontSize = 11.sp)
                            Text("~$mbPerMin MB/min", color = AccentOrange, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showQualityMenu = false },
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                ) {
                    Text("Apply & Close")
                }
            },
            containerColor = Color(0xFF1E293B)
        )
    }

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
                            text = when (status) {
                                RecordingStatus.RECORDING -> "RECORDING ACTIVE"
                                RecordingStatus.PAUSED -> "RECORDING PAUSED"
                                else -> "SCREEN RECORDER"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (status) {
                                RecordingStatus.RECORDING -> AccentRed
                                RecordingStatus.PAUSED -> AccentOrange
                                else -> TealPrimary
                            }
                        )

                        if (isRecording) {
                            val mins = elapsedSeconds / 60
                            val secs = elapsedSeconds % 60
                            Text(
                                text = String.format("%02d:%02d", mins, secs),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = if (status == RecordingStatus.PAUSED) AccentOrange else Color.White
                            )
                        }
                    }

                    Text(
                        text = when (status) {
                            RecordingStatus.RECORDING -> "Screen & Audio are being captured"
                            RecordingStatus.PAUSED -> "Session is paused — video file is preserved. Tap Resume when ready."
                            else -> "Ready to capture display with Floating Circular Menu"
                        },
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

                            Button(
                                onClick = {
                                    if (status == RecordingStatus.PAUSED) viewModel.resumeRecording()
                                    else viewModel.pauseRecording()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (status == RecordingStatus.PAUSED) AccentOrange else Color(0xFF334155),
                                    contentColor = Color.White
                                ),
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
                            Icon(Icons.Default.Palette, contentDescription = "Painter", tint = TealPrimary)
                        }

                        OutlinedButton(
                            onClick = { viewModel.navigateTo(com.morteza.screen.model.AppScreen.SCHEDULE) },
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.Schedule, contentDescription = "Schedule", tint = TealPrimary)
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
                    sub = "${videoConfig.framerate} FPS • Presets",
                    icon = Icons.Default.Videocam,
                    modifier = Modifier.weight(1f),
                    onClick = { showQualityMenu = true }
                )
                QuickChip(
                    label = "${videoConfig.bitrate / 1000} kbps",
                    sub = "AVC / H.264",
                    icon = Icons.Default.Layers,
                    modifier = Modifier.weight(1f)
                )
                val audioIcon = when (audioConfig.audioSource) {
                    AudioSourceOption.MIC -> Icons.Default.Mic
                    AudioSourceOption.INTERNAL -> Icons.Default.VolumeUp
                    AudioSourceOption.MIC_AND_INTERNAL -> Icons.Default.Audiotrack
                    AudioSourceOption.MUTE -> Icons.Default.MicOff
                }
                QuickChip(
                    label = audioConfig.audioSource.shortLabel,
                    sub = if (audioConfig.audioSource == AudioSourceOption.MUTE) "Muted" else "${audioConfig.sampleRate / 1000}kHz • ${if (audioConfig.channelCount == 2) "Stereo" else "Mono"}",
                    icon = audioIcon,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.navigateTo(AppScreen.SETTINGS) }
                )
            }
        }

        // Recording Limits Quick Status
        item {
            val isLimitsActive = limitsConfig.durationLimitEnabled || limitsConfig.fileSizeLimitEnabled
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.navigateTo(AppScreen.LIMITS) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isLimitsActive) TealDark.copy(alpha = 0.4f) else Color(0xFF1E293B).copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isLimitsActive) Icons.Default.HourglassBottom else Icons.Default.HourglassDisabled,
                            contentDescription = null,
                            tint = if (isLimitsActive) TealPrimary else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isLimitsActive) {
                                val d = if (limitsConfig.durationLimitEnabled) "${limitsConfig.maxDurationMinutes}m" else "No time limit"
                                val s = if (limitsConfig.fileSizeLimitEnabled) "${limitsConfig.maxFileSizeMB}MB" else "No size limit"
                                "Auto-Stop: $d • $s"
                            } else {
                                "Set Recording Limit (Duration / Size)"
                            },
                            color = if (isLimitsActive) Color.White else Color.Gray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Text(
                        text = if (isLimitsActive) "Active" else "Configure",
                        color = TealPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Scheduled Recordings Quick Banner
        val pendingScheduled = scheduledRecordings.filter { it.status == com.morteza.screen.model.ScheduledStatus.ARMED }
        if (pendingScheduled.isNotEmpty()) {
            val next = pendingScheduled.minByOrNull { it.targetEpochMillis }
            if (next != null) {
                val remainingMins = ((next.targetEpochMillis - System.currentTimeMillis()) / (1000 * 60)).coerceAtLeast(0)
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.navigateTo(com.morteza.screen.model.AppScreen.SCHEDULE) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.7f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Alarm, contentDescription = null, tint = AccentOrange, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Scheduled: ${next.title}",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Starts in ~$remainingMins min • ${next.qualityPreset}",
                                        color = Color.Gray,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Text(
                                text = "Manage",
                                color = TealPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
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
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = if (onClick != null) modifier.clickable { onClick() } else modifier,
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
