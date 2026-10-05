package com.morteza.screen.ui.components

import android.net.Uri
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.morteza.screen.model.VideoItem
import com.morteza.screen.ui.theme.AccentRed
import com.morteza.screen.ui.theme.TealDark
import com.morteza.screen.ui.theme.TealPrimary
import com.morteza.screen.util.ShareHelper
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.delay
import androidx.core.net.toUri

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerDialog(
    video: VideoItem?,
    onDismiss: () -> Unit,
    onDelete: (VideoItem) -> Unit,
    onShare: (VideoItem) -> Unit,
    onTrim: ((VideoItem) -> Unit)? = null
) {
    if (video == null) return

    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    var durationMs by remember { mutableLongStateOf(video.durationSeconds * 1000L) }
    var currentPositionMs by remember { mutableLongStateOf(0L) }
    var isSeeking by remember { mutableStateOf(false) }
    var hasError by remember { mutableStateOf(false) }

    // Resolve URI for ExoPlayer
    val videoUri = remember(video.path) {
        val file = File(video.path)
        if (file.exists() && file.length() > 0) {
            Uri.fromFile(file)
        } else if (video.path.startsWith("content://") || video.path.startsWith("http://") || video.path.startsWith("https://")) {
            video.path.toUri()
        } else {
            val cacheFile = File(context.cacheDir, video.name)
            if (cacheFile.exists() && cacheFile.length() > 1000) {
                Uri.fromFile(cacheFile)
            } else {
                // Fallback to valid sample media stream for demo/placeholder items
                "https://storage.googleapis.com/exoplayer-test-media-1/mp4/android-screens-10s.mp4".toUri()
            }
        }
    }

    // Initialize ExoPlayer
    val exoPlayer = remember(video.path) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoUri))
            prepare()
            playWhenReady = true
        }
    }

    DisposableEffect(video.path) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) {
                    if (exoPlayer.duration > 0) {
                        durationMs = exoPlayer.duration
                    }
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                hasError = true
            }
        }
        exoPlayer.addListener(listener)

        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    // Periodic progress ticker for custom scrubber
    LaunchedEffect(exoPlayer) {
        while (true) {
            if (!isSeeking && exoPlayer.isPlaying) {
                currentPositionMs = exoPlayer.currentPosition
            }
            delay(250)
        }
    }

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

                // Video Stage with ExoPlayer PlayerView
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    if (!hasError) {
                        AndroidView(
                            factory = { ctx ->
                                PlayerView(ctx).apply {
                                    player = exoPlayer
                                    useController = true
                                    setShowNextButton(false)
                                    setShowPreviousButton(false)
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(
                                Icons.Default.VideoCall,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Sample Video Demo",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Videos captured by the app play seamlessly here.",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { ShareHelper.playVideoExternal(context, video) },
                                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                            ) {
                                Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Open in System Video Player")
                            }
                        }
                    }

                    // Watermark badge
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
                }

                // Scrubber Progress Bar
                val totalSec = (durationMs / 1000L).coerceAtLeast(1L)
                val currentSec = (currentPositionMs / 1000L).coerceIn(0L, totalSec)
                val progressFraction = (currentPositionMs.toFloat() / durationMs.coerceAtLeast(1L).toFloat()).coerceIn(0f, 1f)

                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Slider(
                        value = progressFraction,
                        onValueChange = { fraction ->
                            isSeeking = true
                            currentPositionMs = (fraction * durationMs).toLong()
                        },
                        onValueChangeFinished = {
                            exoPlayer.seekTo(currentPositionMs)
                            isSeeking = false
                        },
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
                        Text(
                            text = String.format("%02d:%02d", currentSec / 60, currentSec % 60),
                            fontSize = 12.sp,
                            color = Color.LightGray
                        )
                        Text(
                            text = String.format("%02d:%02d", totalSec / 60, totalSec % 60),
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
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = { ShareHelper.playVideoExternal(context, video) }) {
                        Icon(Icons.Default.PlayCircle, contentDescription = null, tint = TealPrimary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("System Player", color = TealPrimary)
                    }

                    Row {
                        if (onTrim != null) {
                            TextButton(onClick = { onTrim(video) }) {
                                Icon(Icons.Default.ContentCut, contentDescription = null, tint = TealPrimary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Trim", color = TealPrimary)
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                        }

                        TextButton(onClick = { onShare(video) }) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = TealPrimary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Share", color = TealPrimary)
                        }

                        Spacer(modifier = Modifier.width(4.dp))

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
}
