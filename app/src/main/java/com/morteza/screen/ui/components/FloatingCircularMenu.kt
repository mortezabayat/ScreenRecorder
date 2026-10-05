package com.morteza.screen.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.morteza.screen.model.AppScreen
import com.morteza.screen.model.RecordingStatus
import com.morteza.screen.ui.theme.AccentRed
import com.morteza.screen.ui.theme.TealDark
import com.morteza.screen.ui.theme.TealPrimary
import com.morteza.screen.viewmodel.ScreenRecorderViewModel
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun FloatingCircularMenu(
    viewModel: ScreenRecorderViewModel,
    modifier: Modifier = Modifier
) {
    val isVisible by viewModel.isFloatingMenuVisible.collectAsState()
    val status by viewModel.recordingStatus.collectAsState()

    if (!isVisible) return

    var isExpanded by remember { mutableStateOf(false) }
    var dragOffsetX by remember { mutableFloatStateOf(0f) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }

    val isRecording = status == RecordingStatus.RECORDING || status == RecordingStatus.PAUSED

    // Rotation for toggle icon
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 45f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "rotation"
    )

    // Sub-buttons radial fan-out distance
    val radiusAnim by animateFloatAsState(
        targetValue = if (isExpanded) 160f else 0f,
        animationSpec = tween(durationMillis = 250),
        label = "radius"
    )

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Dim overlay when radial menu is expanded
        if (isExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f))
                    .clickable { isExpanded = false }
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 80.dp, end = 20.dp)
                .offset { IntOffset(dragOffsetX.roundToInt(), dragOffsetY.roundToInt()) }
                .pointerInput(isExpanded) {
                    if (!isExpanded) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            dragOffsetX += dragAmount.x
                            dragOffsetY += dragAmount.y
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            // Radial Sub-Action Buttons
            if (radiusAnim > 5f) {
                val angles = listOf(135.0, 180.0, 225.0, 270.0)

                if (isRecording) {
                    // Button 1: Pause / Resume
                    RadialSubButton(
                        angleDeg = angles[0],
                        radius = radiusAnim,
                        icon = if (status == RecordingStatus.PAUSED) Icons.Default.PlayArrow else Icons.Default.Pause,
                        backgroundColor = Color(0xFFFFB300),
                        onClick = {
                            if (status == RecordingStatus.PAUSED) viewModel.resumeRecording() else viewModel.pauseRecording()
                            isExpanded = false
                        }
                    )

                    // Button 2: Stop Recording
                    RadialSubButton(
                        angleDeg = angles[1],
                        radius = radiusAnim,
                        icon = Icons.Default.Stop,
                        backgroundColor = AccentRed,
                        onClick = {
                            viewModel.stopRecording()
                            isExpanded = false
                        }
                    )

                    // Button 3: Painter Screen Markup
                    RadialSubButton(
                        angleDeg = angles[2],
                        radius = radiusAnim,
                        icon = Icons.Default.Palette,
                        backgroundColor = TealPrimary,
                        onClick = {
                            viewModel.togglePainter()
                            isExpanded = false
                        }
                    )

                    // Button 4: Settings
                    RadialSubButton(
                        angleDeg = angles[3],
                        radius = radiusAnim,
                        icon = Icons.Default.Settings,
                        backgroundColor = TealDark,
                        onClick = {
                            viewModel.navigateTo(AppScreen.SETTINGS)
                            isExpanded = false
                        }
                    )
                } else {
                    // Button 1: Start Recording
                    RadialSubButton(
                        angleDeg = angles[0],
                        radius = radiusAnim,
                        icon = Icons.Default.FiberManualRecord,
                        backgroundColor = AccentRed,
                        onClick = {
                            viewModel.startRecordingFlow()
                            isExpanded = false
                        }
                    )

                    // Button 2: Tools Screen
                    RadialSubButton(
                        angleDeg = angles[1],
                        radius = radiusAnim,
                        icon = Icons.Default.Build,
                        backgroundColor = TealDark,
                        onClick = {
                            viewModel.navigateTo(AppScreen.TOOLS)
                            isExpanded = false
                        }
                    )

                    // Button 3: Home Screen
                    RadialSubButton(
                        angleDeg = angles[2],
                        radius = radiusAnim,
                        icon = Icons.Default.Home,
                        backgroundColor = TealPrimary,
                        onClick = {
                            viewModel.navigateTo(AppScreen.HOME)
                            isExpanded = false
                        }
                    )

                    // Button 4: Settings Screen
                    RadialSubButton(
                        angleDeg = angles[3],
                        radius = radiusAnim,
                        icon = Icons.Default.Settings,
                        backgroundColor = TealDark,
                        onClick = {
                            viewModel.navigateTo(AppScreen.SETTINGS)
                            isExpanded = false
                        }
                    )
                }
            }

            // Central Floating Button
            FloatingActionButton(
                onClick = { isExpanded = !isExpanded },
                shape = CircleShape,
                containerColor = if (isRecording) AccentRed else Color.White,
                contentColor = if (isRecording) Color.White else TealPrimary,
                modifier = Modifier
                    .size(56.dp)
                    .shadow(12.dp, CircleShape)
            ) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.Close else if (isRecording) Icons.Default.Videocam else Icons.Default.Add,
                    contentDescription = "Screen Recorder Menu",
                    modifier = Modifier
                        .size(28.dp)
                        .rotate(rotation)
                )
            }
        }
    }
}

@Composable
private fun RadialSubButton(
    angleDeg: Double,
    radius: Float,
    icon: ImageVector,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    val rad = Math.toRadians(angleDeg)
    val subX = (cos(rad) * radius).toFloat()
    val subY = (sin(rad) * radius).toFloat()

    IconButton(
        onClick = onClick,
        modifier = Modifier
            .offset { IntOffset(subX.roundToInt(), subY.roundToInt()) }
            .size(46.dp)
            .shadow(8.dp, CircleShape)
            .background(backgroundColor, CircleShape)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(22.dp)
        )
    }
}
