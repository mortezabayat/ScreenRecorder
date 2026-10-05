package com.morteza.screen.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.morteza.screen.model.AppScreen
import com.morteza.screen.ui.theme.AccentOrange
import com.morteza.screen.ui.theme.AccentRed
import com.morteza.screen.ui.theme.ScreenTheme
import com.morteza.screen.ui.theme.TealDark
import com.morteza.screen.ui.theme.TealPrimary
import com.morteza.screen.viewmodel.ScreenRecorderViewModel

@Composable
fun StatisticsScreen(
    viewModel: ScreenRecorderViewModel,
    modifier: Modifier = Modifier
) {
    val videos by viewModel.videos.collectAsState()
    val storageFolder by viewModel.storageFolder.collectAsState()

    val totalCount = videos.size
    val totalDurationSeconds = videos.sumOf { it.durationSeconds }
    val totalSizeBytes = videos.sumOf { it.sizeBytes }
    val starredCount = videos.count { it.isStarred }

    val totalHours = totalDurationSeconds / 3600
    val totalMinutes = (totalDurationSeconds % 3600) / 60
    val totalSecs = totalDurationSeconds % 60

    val formattedDuration = when {
        totalHours > 0 -> String.format("%dh %02dm %02ds", totalHours, totalMinutes, totalSecs)
        totalMinutes > 0 -> String.format("%dm %02ds", totalMinutes, totalSecs)
        else -> "${totalSecs}s"
    }

    val totalSizeMB = totalSizeBytes / (1024f * 1024f)
    val formattedSize = if (totalSizeMB >= 1024f) {
        String.format("%.2f GB", totalSizeMB / 1024f)
    } else {
        String.format("%.1f MB", totalSizeMB)
    }

    val avgDurationSeconds = if (totalCount > 0) totalDurationSeconds / totalCount else 0
    val avgSizeMB = if (totalCount > 0) totalSizeMB / totalCount else 0f

    val longestVideo = videos.maxByOrNull { it.durationSeconds }
    val largestVideo = videos.maxByOrNull { it.sizeBytes }

    // Resolution breakdown
    val res1080p = videos.count { it.resolution.contains("1080") }
    val res720p = videos.count { it.resolution.contains("720") }
    val res480p = videos.count { it.resolution.contains("480") }
    val resOther = totalCount - (res1080p + res720p + res480p)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(TealDark),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Analytics, contentDescription = null, tint = TealPrimary)
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "Recording Statistics",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Summary of captures, duration, and disk storage usage",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // 3 Primary Metric Cards
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatMetricCard(
                        title = "Recordings",
                        value = "$totalCount",
                        sub = if (starredCount > 0) "$starredCount starred" else "Captures",
                        icon = Icons.Default.Videocam,
                        iconColor = TealPrimary,
                        modifier = Modifier.weight(1f)
                    )

                    StatMetricCard(
                        title = "Total Time",
                        value = formattedDuration,
                        sub = if (totalCount > 0) "Avg: ${avgDurationSeconds / 60}m ${avgDurationSeconds % 60}s" else "0m",
                        icon = Icons.Default.Timer,
                        iconColor = AccentOrange,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Storage Big Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(TealPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Storage, contentDescription = null, tint = TealPrimary)
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Storage Used by Recordings",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = formattedSize,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 24.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (totalCount > 0) String.format("Average %.1f MB per recording", avgSizeMB) else "No recordings saved yet",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Button(
                            onClick = { viewModel.navigateTo(AppScreen.SETTINGS) },
                            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Clean", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Storage Folder Path Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Folder, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Destination Storage Directory",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = storageFolder.path,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Resolution Distribution Breakdown Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Resolution Breakdown",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    ResolutionBarItem(
                        label = "1080p Full HD",
                        count = res1080p,
                        total = totalCount,
                        color = TealPrimary
                    )

                    ResolutionBarItem(
                        label = "720p HD",
                        count = res720p,
                        total = totalCount,
                        color = Color(0xFF00E5FF)
                    )

                    ResolutionBarItem(
                        label = "480p SD",
                        count = res480p,
                        total = totalCount,
                        color = AccentOrange
                    )

                    if (resOther > 0) {
                        ResolutionBarItem(
                            label = "Other / Custom",
                            count = resOther,
                            total = totalCount,
                            color = Color(0xFFA855F7)
                        )
                    }
                }
            }
        }

        // Highlight Records Card (Longest & Largest)
        if (longestVideo != null || largestVideo != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Record Highlights",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (longestVideo != null) {
                            val lMins = longestVideo.durationSeconds / 60
                            val lSecs = longestVideo.durationSeconds % 60
                            HighlightRow(
                                title = "Longest Capture",
                                value = String.format("%02dm %02ds", lMins, lSecs),
                                subtitle = longestVideo.name,
                                icon = Icons.Default.HourglassTop,
                                iconColor = AccentOrange
                            )
                        }

                        if (largestVideo != null) {
                            val sizeMB = largestVideo.sizeBytes / (1024f * 1024f)
                            HighlightRow(
                                title = "Largest File",
                                value = String.format("%.1f MB", sizeMB),
                                subtitle = "${largestVideo.name} (${largestVideo.resolution})",
                                icon = Icons.Default.SdCard,
                                iconColor = TealPrimary
                            )
                        }
                    }
                }
            }
        }

        // Quick Navigation Shortcuts
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.navigateTo(AppScreen.GALLERY) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.VideoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("View Gallery", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = { viewModel.navigateTo(AppScreen.LIMITS) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.HourglassBottom, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Set Limits", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun StatMetricCard(
    title: String,
    value: String,
    sub: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
            }

            Text(
                text = value,
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = sub,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun ResolutionBarItem(
    label: String,
    count: Int,
    total: Int,
    color: Color
) {
    val percentage = if (total > 0) (count.toFloat() / total.toFloat()) else 0f
    val percentText = (percentage * 100).toInt()

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
            Text("$count videos ($percentText%)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        LinearProgressIndicator(
            progress = { percentage },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
private fun HighlightRow(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StatisticsScreenPreview() {
    ScreenTheme(darkTheme = true) {
        StatisticsScreen(viewModel = viewModel())
    }
}
