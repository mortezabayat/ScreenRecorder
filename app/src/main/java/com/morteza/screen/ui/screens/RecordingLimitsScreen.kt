package com.morteza.screen.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.morteza.screen.model.RecordingLimitsConfig
import com.morteza.screen.ui.theme.AccentOrange
import com.morteza.screen.ui.theme.AccentRed
import com.morteza.screen.ui.theme.ScreenTheme
import com.morteza.screen.ui.theme.TealDark
import com.morteza.screen.ui.theme.TealPrimary
import com.morteza.screen.viewmodel.ScreenRecorderViewModel
import kotlin.math.roundToInt

@Composable
fun RecordingLimitsScreen(
    viewModel: ScreenRecorderViewModel,
    modifier: Modifier = Modifier
) {
    val limitsConfig by viewModel.recordingLimitsConfig.collectAsState()
    val videoConfig by viewModel.videoConfig.collectAsState()

    val durationPresets = listOf(1, 5, 10, 15, 30, 60, 120)
    val fileSizePresets = listOf(
        25 to "25 MB",
        50 to "50 MB",
        100 to "100 MB",
        250 to "250 MB",
        500 to "500 MB",
        1000 to "1 GB",
        2000 to "2 GB"
    )

    // Calculate estimated minutes possible with current bitrate & max file size
    val totalBitrateBps = videoConfig.bitrate + 128_000
    val bytesPerSecond = totalBitrateBps / 8.0
    val estimatedMinutesForFileLimit = if (bytesPerSecond > 0) {
        ((limitsConfig.maxFileSizeMB * 1024.0 * 1024.0) / bytesPerSecond / 60.0).roundToInt()
    } else 0

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Recording Limits & Auto-Stop",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Set threshold limits to protect device storage and automatically end recordings.",
                color = Color.Gray,
                fontSize = 12.sp
            )
        }

        // Active Protection Status Banner
        item {
            val isAnyLimitActive = limitsConfig.durationLimitEnabled || limitsConfig.fileSizeLimitEnabled
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isAnyLimitActive) TealDark.copy(alpha = 0.5f) else Color(0xFF1E293B)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(if (isAnyLimitActive) TealPrimary else Color.Gray, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isAnyLimitActive) Icons.Default.Timer else Icons.Default.TimerOff,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = if (isAnyLimitActive) "Auto-Stop Safeguard Active" else "No Recording Limits Set",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        val summaryText = buildString {
                            if (limitsConfig.durationLimitEnabled) append("Max: ${limitsConfig.maxDurationMinutes}m ")
                            if (limitsConfig.fileSizeLimitEnabled) append("Max Size: ${limitsConfig.maxFileSizeMB}MB")
                            if (!isAnyLimitActive) append("Recording will continue until manually stopped.")
                        }
                        Text(
                            text = summaryText,
                            color = Color.LightGray,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // 1. Duration Limit Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.HourglassBottom, contentDescription = null, tint = TealPrimary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Maximum Duration Limit", fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Stop automatically after elapsed time", color = Color.Gray, fontSize = 11.sp)
                            }
                        }

                        Switch(
                            checked = limitsConfig.durationLimitEnabled,
                            onCheckedChange = {
                                viewModel.updateLimitsConfig(limitsConfig.copy(durationLimitEnabled = it))
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = TealPrimary)
                        )
                    }

                    if (limitsConfig.durationLimitEnabled) {
                        HorizontalDivider(color = Color(0xFF334155))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Threshold Duration:", color = Color.LightGray, fontSize = 13.sp)
                            Text(
                                text = "${limitsConfig.maxDurationMinutes} minutes",
                                fontWeight = FontWeight.Black,
                                color = TealPrimary,
                                fontSize = 16.sp
                            )
                        }

                        // Presets
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            durationPresets.take(5).forEach { mins ->
                                FilterChip(
                                    selected = limitsConfig.maxDurationMinutes == mins,
                                    onClick = {
                                        viewModel.updateLimitsConfig(limitsConfig.copy(maxDurationMinutes = mins))
                                    },
                                    label = { Text("${mins}m", fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = TealPrimary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        // Fine-grain Slider
                        Slider(
                            value = limitsConfig.maxDurationMinutes.toFloat(),
                            onValueChange = {
                                viewModel.updateLimitsConfig(limitsConfig.copy(maxDurationMinutes = it.roundToInt()))
                            },
                            valueRange = 1f..120f,
                            steps = 118,
                            colors = SliderDefaults.colors(
                                thumbColor = TealPrimary,
                                activeTrackColor = TealPrimary,
                                inactiveTrackColor = Color.DarkGray
                            )
                        )

                        Text(
                            text = "A stop trigger will fire at ${limitsConfig.maxDurationMinutes * 60} seconds.",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // 2. File Size Limit Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Storage, contentDescription = null, tint = AccentOrange)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Maximum File Size Limit", fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Stop before exceeding target storage", color = Color.Gray, fontSize = 11.sp)
                            }
                        }

                        Switch(
                            checked = limitsConfig.fileSizeLimitEnabled,
                            onCheckedChange = {
                                viewModel.updateLimitsConfig(limitsConfig.copy(fileSizeLimitEnabled = it))
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = AccentOrange)
                        )
                    }

                    if (limitsConfig.fileSizeLimitEnabled) {
                        HorizontalDivider(color = Color(0xFF334155))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Threshold File Size:", color = Color.LightGray, fontSize = 13.sp)
                            Text(
                                text = if (limitsConfig.maxFileSizeMB >= 1000) {
                                    String.format("%.1f GB", limitsConfig.maxFileSizeMB / 1024f)
                                } else {
                                    "${limitsConfig.maxFileSizeMB} MB"
                                },
                                fontWeight = FontWeight.Black,
                                color = AccentOrange,
                                fontSize = 16.sp
                            )
                        }

                        // Presets
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            fileSizePresets.take(4).forEach { (mb, label) ->
                                FilterChip(
                                    selected = limitsConfig.maxFileSizeMB == mb,
                                    onClick = {
                                        viewModel.updateLimitsConfig(limitsConfig.copy(maxFileSizeMB = mb))
                                    },
                                    label = { Text(label, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = AccentOrange,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        // Fine-grain Slider
                        Slider(
                            value = limitsConfig.maxFileSizeMB.toFloat(),
                            onValueChange = {
                                viewModel.updateLimitsConfig(limitsConfig.copy(maxFileSizeMB = it.roundToInt()))
                            },
                            valueRange = 10f..2000f,
                            steps = 198,
                            colors = SliderDefaults.colors(
                                thumbColor = AccentOrange,
                                activeTrackColor = AccentOrange,
                                inactiveTrackColor = Color.DarkGray
                            )
                        )

                        // Bitrate context
                        Surface(
                            color = Color.Black.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "At current video bitrate (${videoConfig.bitrate / 1000} kbps), ${limitsConfig.maxFileSizeMB} MB provides ~${estimatedMinutesForFileLimit} minutes of recording.",
                                    color = Color.LightGray,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Auto-Stop Behavior Options
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Auto-Stop Session Action", fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Action performed when any limit threshold is hit", color = Color.Gray, fontSize = 11.sp)

                    listOf("Stop and Save", "Stop and Prompt Preview").forEach { action ->
                        val isSelected = limitsConfig.autoStopAction == action
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(action, color = if (isSelected) TealPrimary else Color.White, fontSize = 13.sp)
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    viewModel.updateLimitsConfig(limitsConfig.copy(autoStopAction = action))
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = TealPrimary)
                            )
                        }
                    }
                }
            }
        }

        // Reset to Unlimited Button
        item {
            OutlinedButton(
                onClick = {
                    viewModel.updateLimitsConfig(
                        RecordingLimitsConfig(
                            durationLimitEnabled = false,
                            maxDurationMinutes = 10,
                            fileSizeLimitEnabled = false,
                            maxFileSizeMB = 500
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.LightGray),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.RestartAlt, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Reset All Limits (Unlimited Recording)")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RecordingLimitsScreenPreview() {
    ScreenTheme(darkTheme = true) {
        RecordingLimitsScreen(viewModel = viewModel())
    }
}
