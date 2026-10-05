package com.morteza.screen.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.morteza.screen.model.AppScreen
import com.morteza.screen.model.VideoItem
import com.morteza.screen.ui.components.VideoTrimmingDialog
import com.morteza.screen.ui.theme.AccentRed
import com.morteza.screen.ui.theme.ScreenTheme
import com.morteza.screen.ui.theme.TealPrimary
import com.morteza.screen.util.ShareHelper
import com.morteza.screen.viewmodel.ScreenRecorderViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun GalleryScreen(
    viewModel: ScreenRecorderViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val videos by viewModel.videos.collectAsState()
    val autoCleanupConfig by viewModel.autoCleanupConfig.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedVideoForShareSheet by remember { mutableStateOf<VideoItem?>(null) }
    var videoToTrim by remember { mutableStateOf<VideoItem?>(null) }

    val filtered = videos.filter { it.name.contains(searchQuery, ignoreCase = true) }

    // Scan real video files from storage
    LaunchedEffect(Unit) {
        viewModel.loadRealVideosFromFolder(context)
    }

    // Video Trimming Dialog
    VideoTrimmingDialog(
        video = videoToTrim,
        onDismiss = { videoToTrim = null },
        onSaveTrim = { start, end, saveAsNew ->
            videoToTrim?.let { v ->
                viewModel.trimVideo(v, start, end, saveAsNew)
            }
        }
    )

    // Share Options Dialog
    selectedVideoForShareSheet?.let { video ->
        AlertDialog(
            onDismissRequest = { selectedVideoForShareSheet = null },
            title = {
                Text(
                    text = "Share Recording",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = video.name,
                        color = Color.LightGray,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${video.resolution} • ${video.durationSeconds}s • ${video.path}",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = {
                            selectedVideoForShareSheet = null
                            ShareHelper.shareVideo(context, video)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Share Video via Apps (Share Intent)")
                    }

                    OutlinedButton(
                        onClick = {
                            selectedVideoForShareSheet = null
                            ShareHelper.copyVideoDetails(context, video)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.LightGray)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Copy Video Info & Path")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { selectedVideoForShareSheet = null }) {
                    Text("Close", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF1E293B)
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search Box
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search recordings...", color = Color.Gray) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TealPrimary,
                unfocusedBorderColor = Color(0xFF334155),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        // Auto-Cleanup Policy Banner
        if (autoCleanupConfig.autoDeleteEnabled) {
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                color = Color(0xFF1E293B),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = null,
                        tint = AccentRed,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Auto-cleanup active: files older than ${autoCleanupConfig.retentionDays}d are pruned. Tap ★ to protect.",
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val totalMB = videos.sumOf { it.sizeBytes } / (1024f * 1024f)
            Text(
                text = "${filtered.size} recordings • ${String.format("%.1f MB", totalMB)} total",
                color = Color.Gray,
                fontSize = 12.sp
            )

            Row {
                IconButton(onClick = { viewModel.loadRealVideosFromFolder(context) }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Scan Storage", tint = TealPrimary, modifier = Modifier.size(18.dp))
                }
                TextButton(
                    onClick = { viewModel.navigateTo(AppScreen.STATISTICS) },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.Analytics, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Stats", color = TealPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (filtered.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No recordings found", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filtered) { video ->
                    GalleryVideoCard(
                        video = video,
                        onSelect = { viewModel.selectVideoForPreview(video) },
                        onToggleStar = { viewModel.toggleStarVideo(video) },
                        onTrim = { videoToTrim = video },
                        onShare = { ShareHelper.shareVideo(context, video) },
                        onDelete = { viewModel.deleteVideo(video) }
                    )
                }
            }
        }
    }
}

@Composable
private fun GalleryVideoCard(
    video: VideoItem,
    onSelect: () -> Unit,
    onToggleStar: () -> Unit,
    onTrim: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
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
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayCircle,
                    contentDescription = null,
                    tint = TealPrimary,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = video.name,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        fontSize = 13.sp,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (video.isStarred) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Default.Star,
                            contentDescription = "Protected",
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                val mins = video.durationSeconds / 60
                val secs = video.durationSeconds % 60
                val sizeMb = String.format("%.1f MB", video.sizeBytes / (1024f * 1024f))
                val dateStr = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(video.timestamp))

                val daysAgo = ((System.currentTimeMillis() - video.timestamp) / 86_400_000L).toInt()
                val ageLabel = when {
                    daysAgo <= 0 -> "Today"
                    daysAgo == 1 -> "1 day ago"
                    else -> "$daysAgo days ago"
                }

                Text(
                    text = "${video.resolution} • ${String.format("%02d:%02d", mins, secs)} • $sizeMb",
                    color = Color.LightGray,
                    fontSize = 11.sp
                )
                Text(
                    text = "$dateStr ($ageLabel)",
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onToggleStar) {
                    Icon(
                        imageVector = if (video.isStarred) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Protect",
                        tint = if (video.isStarred) Color(0xFFFFC107) else Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = Color.LightGray, modifier = Modifier.size(20.dp))
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(Color(0xFF0F172A))
                    ) {
                        DropdownMenuItem(
                            text = { Text("Play Video", color = Color.White) },
                            onClick = {
                                showMenu = false
                                onSelect()
                            },
                            leadingIcon = { Icon(Icons.Default.PlayArrow, contentDescription = null, tint = TealPrimary) }
                        )
                        DropdownMenuItem(
                            text = { Text("Trim Video", color = Color.White) },
                            onClick = {
                                showMenu = false
                                onTrim()
                            },
                            leadingIcon = { Icon(Icons.Default.ContentCut, contentDescription = null, tint = TealPrimary) }
                        )
                        DropdownMenuItem(
                            text = { Text("Share Video", color = Color.White) },
                            onClick = {
                                showMenu = false
                                onShare()
                            },
                            leadingIcon = { Icon(Icons.Default.Share, contentDescription = null, tint = TealPrimary) }
                        )
                        HorizontalDivider(color = Color(0xFF334155))
                        DropdownMenuItem(
                            text = { Text("Delete Video", color = AccentRed) },
                            onClick = {
                                showMenu = false
                                onDelete()
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = AccentRed) }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GalleryScreenPreview() {
    ScreenTheme(darkTheme = true) {
        GalleryScreen(viewModel = viewModel())
    }
}
