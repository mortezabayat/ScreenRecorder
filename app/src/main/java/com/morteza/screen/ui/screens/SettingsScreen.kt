package com.morteza.screen.ui.screens

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.morteza.screen.ui.theme.AccentRed
import com.morteza.screen.ui.theme.TealPrimary
import com.morteza.screen.viewmodel.ScreenRecorderViewModel

@Composable
fun SettingsScreen(
    viewModel: ScreenRecorderViewModel,
    modifier: Modifier = Modifier
) {
    val countdownDuration by viewModel.countdownDuration.collectAsState()
    val audioConfig by viewModel.audioConfig.collectAsState()
    val overlayWatermark by viewModel.overlayWatermarkEnabled.collectAsState()
    val isFloatingVisible by viewModel.isFloatingMenuVisible.collectAsState()
    val videos by viewModel.videos.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Preferences, overlay services, and recording timers",
                color = Color.Gray,
                fontSize = 12.sp
            )
        }

        // Countdown Timer Option
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Countdown Timer Animation", fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Animated numbers countdown before screen capture begins", color = Color.Gray, fontSize = 12.sp)

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(0 to "Off (0s)", 3 to "3s", 5 to "5s").forEach { (sec, label) ->
                            FilterChip(
                                selected = countdownDuration == sec,
                                onClick = { viewModel.setCountdownDuration(sec) },
                                label = { Text(label, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = TealPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }
        }

        // Microphone Audio Mix
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Microphone Audio", fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Mix microphone recording with system audio", color = Color.Gray, fontSize = 12.sp)
                    }

                    Switch(
                        checked = audioConfig.includeMic,
                        onCheckedChange = { viewModel.updateAudioConfig(audioConfig.copy(includeMic = it)) },
                        colors = SwitchDefaults.colors(checkedThumbColor = TealPrimary)
                    )
                }
            }
        }

        // BitmapOverlay GL Watermark
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Bitmap Overlay Video Processor", fontWeight = FontWeight.Bold, color = Color.White)
                        Text("GL-based canvas timestamp & logo badge on video frames", color = Color.Gray, fontSize = 12.sp)
                    }

                    Switch(
                        checked = overlayWatermark,
                        onCheckedChange = { viewModel.setOverlayWatermark(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = TealPrimary)
                    )
                }
            }
        }

        // Floating Circular Menu
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Floating Action Circular Menu", fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Always-on-top draggable floating action button", color = Color.Gray, fontSize = 12.sp)
                    }

                    Switch(
                        checked = isFloatingVisible,
                        onCheckedChange = { viewModel.toggleFloatingMenu() },
                        colors = SwitchDefaults.colors(checkedThumbColor = TealPrimary)
                    )
                }
            }
        }

        // Data Storage & Clear
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Storage Management", fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Total recordings saved: ${videos.size}", color = Color.Gray, fontSize = 12.sp)

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { viewModel.clearAllVideos() },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentRed)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = AccentRed)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Clear All Recordings")
                    }
                }
            }
        }
    }
}
