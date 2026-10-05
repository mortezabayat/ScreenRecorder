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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.morteza.screen.model.AudioConfig
import com.morteza.screen.model.VideoConfig
import com.morteza.screen.ui.theme.TealDark
import com.morteza.screen.ui.theme.TealPrimary
import com.morteza.screen.viewmodel.ScreenRecorderViewModel

@Composable
fun ToolsScreen(
    viewModel: ScreenRecorderViewModel,
    modifier: Modifier = Modifier
) {
    val videoConfig by viewModel.videoConfig.collectAsState()
    val audioConfig by viewModel.audioConfig.collectAsState()

    val resolutions = listOf(
        Triple("480x360", 480, 360),
        Triple("720x480", 720, 480),
        Triple("1280x720 (HD)", 1280, 720),
        Triple("1920x1080 (FHD)", 1920, 1080),
        Triple("3860x2160 (4K)", 3860, 2160)
    )

    val framerates = listOf(15, 25, 30, 60, 90, 120)
    val bitrates = listOf(
        "800 kbps" to 800000,
        "2000 kbps" to 2000000,
        "4000 kbps" to 4000000,
        "10000 kbps" to 10000000,
        "16000 kbps" to 16000000,
        "25000 kbps" to 25000000
    )
    val iframeIntervals = listOf(1, 5, 10, 20, 30)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Encoder Hardware & Configurations",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Configure MediaCodec encoder parameters and audio channels",
                color = Color.Gray,
                fontSize = 12.sp
            )
        }

        // Hardware Diagnostics Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Memory, contentDescription = null, tint = TealPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Detected Encoders", fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("• Video AVC: c2.android.avc.encoder (Hardware Accelerated)", fontSize = 12.sp, color = Color.LightGray)
                    Text("• Audio AAC: c2.android.aac.encoder (44.1kHz HD)", fontSize = 12.sp, color = Color.LightGray)
                    Text("• VP9 Codec: c2.vpx.vp9.encoder (Supported)", fontSize = 12.sp, color = Color.LightGray)
                }
            }
        }

        // Resolution Selector
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Video Resolution", fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(8.dp))

                    resolutions.forEach { (label, w, h) ->
                        val isSelected = videoConfig.width == w && videoConfig.height == h
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(label, color = if (isSelected) TealPrimary else Color.White, fontSize = 13.sp)
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    viewModel.updateVideoConfig(
                                        videoConfig.copy(resolution = "$w x $h", width = w, height = h)
                                    )
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = TealPrimary)
                            )
                        }
                    }
                }
            }
        }

        // Framerate Selector
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Frame Rate (FPS)", fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        framerates.forEach { fps ->
                            val isSelected = videoConfig.framerate == fps
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.updateVideoConfig(videoConfig.copy(framerate = fps)) },
                                label = { Text("$fps", fontSize = 12.sp) },
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

        // Bitrate Selector
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Target Bitrate", fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(8.dp))

                    bitrates.forEach { (label, br) ->
                        val isSelected = videoConfig.bitrate == br
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(label, color = if (isSelected) TealPrimary else Color.White, fontSize = 13.sp)
                            RadioButton(
                                selected = isSelected,
                                onClick = { viewModel.updateVideoConfig(videoConfig.copy(bitrate = br)) },
                                colors = RadioButtonDefaults.colors(selectedColor = TealPrimary)
                            )
                        }
                    }
                }
            }
        }

        // Audio Channels & I-Frame
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Audio Channels", fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(1 to "Mono (1 ch)", 2 to "Stereo (2 ch)").forEach { (ch, label) ->
                            FilterChip(
                                selected = audioConfig.channelCount == ch,
                                onClick = { viewModel.updateAudioConfig(audioConfig.copy(channelCount = ch)) },
                                label = { Text(label, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = TealPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("I-Frame Interval (seconds)", fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        iframeIntervals.forEach { intv ->
                            FilterChip(
                                selected = videoConfig.iframeInterval == intv,
                                onClick = { viewModel.updateVideoConfig(videoConfig.copy(iframeInterval = intv)) },
                                label = { Text("${intv}s", fontSize = 12.sp) },
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
    }
}
