package com.morteza.screen.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.morteza.screen.model.RecordingStatus
import com.morteza.screen.ui.theme.AccentOrange
import com.morteza.screen.ui.theme.AccentRed
import com.morteza.screen.ui.theme.TealPrimary
import com.morteza.screen.viewmodel.ScreenRecorderViewModel
import kotlin.math.roundToInt

@Composable
fun RecordingTimerOverlay(
    viewModel: ScreenRecorderViewModel,
    modifier: Modifier = Modifier
) {
    val status by viewModel.recordingStatus.collectAsState()
    val elapsedSeconds by viewModel.elapsedSeconds.collectAsState()
    val limitsConfig by viewModel.recordingLimitsConfig.collectAsState()
    val videoConfig by viewModel.videoConfig.collectAsState()

    val isActive = status == RecordingStatus.RECORDING || status == RecordingStatus.PAUSED
    val isPaused = status == RecordingStatus.PAUSED

    // Overlay position coordinates (draggable)
    var offsetX by remember { mutableFloatStateOf(40f) }
    var offsetY by remember { mutableFloatStateOf(80f) }

    // Expanded actions mode on the timer pill
    var isExpanded by remember { mutableStateOf(false) }

    // Pulsing recording dot animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isPaused) 1f else 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotAlpha"
    )

    AnimatedVisibility(
        visible = isActive,
        enter = fadeIn(tween(250)) + scaleIn(tween(250)),
        exit = fadeOut(tween(200)) + scaleOut(tween(200)),
        modifier = modifier.fillMaxSize()
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                    .shadow(16.dp, RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF0F172A).copy(alpha = 0.92f))
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            offsetX += dragAmount.x
                            offsetY += dragAmount.y
                        }
                    }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Pulsing Red / Amber Status Dot
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                (if (isPaused) AccentOrange else AccentRed).copy(alpha = dotAlpha)
                            )
                    )

                    // REC / PAUSED Badge
                    Surface(
                        color = (if (isPaused) AccentOrange else AccentRed).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (isPaused) "PAUSED" else "REC",
                            color = if (isPaused) AccentOrange else AccentRed,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Formatted Duration Timecode
                    val hours = elapsedSeconds / 3600
                    val minutes = (elapsedSeconds % 3600) / 60
                    val seconds = elapsedSeconds % 60
                    val timeString = if (hours > 0) {
                        String.format("%02d:%02d:%02d", hours, minutes, seconds)
                    } else {
                        String.format("%02d:%02d", minutes, seconds)
                    }

                    val fullTimeDisplay = if (limitsConfig.durationLimitEnabled) {
                        val limitHours = limitsConfig.maxDurationMinutes / 60
                        val limitMins = limitsConfig.maxDurationMinutes % 60
                        val limitStr = if (limitHours > 0) {
                            String.format("%02d:%02d:00", limitHours, limitMins)
                        } else {
                            String.format("%02d:00", limitMins)
                        }
                        "$timeString / $limitStr"
                    } else {
                        timeString
                    }

                    Text(
                        text = fullTimeDisplay,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.clickable { isExpanded = !isExpanded }
                    )

                    // File size limit badge if enabled
                    if (limitsConfig.fileSizeLimitEnabled) {
                        val estBytes = viewModel.calculateEstimatedBytes(elapsedSeconds, videoConfig.bitrate)
                        val estMB = (estBytes / (1024f * 1024f)).roundToInt()
                        Surface(
                            color = Color(0xFF334155),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "$estMB/${limitsConfig.maxFileSizeMB}MB",
                                color = AccentOrange,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Quick Action Buttons on Expanded Pill
                    if (isExpanded) {
                        Box(
                            modifier = Modifier
                                .height(20.dp)
                                .width(1.dp)
                                .background(Color.DarkGray)
                        )

                        // Pause / Resume Button
                        IconButton(
                            onClick = {
                                if (isPaused) viewModel.resumeRecording() else viewModel.pauseRecording()
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                contentDescription = if (isPaused) "Resume" else "Pause",
                                tint = if (isPaused) AccentOrange else Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Painter Button
                        IconButton(
                            onClick = { viewModel.togglePainter() },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = "Painter",
                                tint = TealPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Stop Recording Button
                        IconButton(
                            onClick = {
                                viewModel.stopRecording()
                                isExpanded = false
                            },
                            modifier = Modifier
                                .size(28.dp)
                                .background(AccentRed, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Stop",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Expand / Collapse Chevron
                    IconButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ChevronLeft else Icons.Default.ChevronRight,
                            contentDescription = if (isExpanded) "Collapse" else "Expand",
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
