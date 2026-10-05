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
    val elapsedSeconds by viewModel.elapsedSeconds.collectAsState()

    if (!isVisible) return

    var isExpanded by remember { mutableStateOf(false) }
    var offsetX by remember { mutableFloatStateOf(650f) }
    var offsetY by remember { mutableFloatStateOf(800f) }

    val isRecording = status == RecordingStatus.RECORDING || status == RecordingStatus.PAUSED

    // Close button rotation
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 45f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "rotation"
    )

    // Sub-buttons fan out distance animation
    val radiusAnim by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(durationMillis = 250),
        label = "radius"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        // Dim overlay when expanded
        if (isExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable { isExpanded = false }
            )
        }

        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                .pointerInput(isExpanded) {
                    if (!isExpanded) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            offsetX += dragAmount.x
                            offsetY += dragAmount.y
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            // Radial Sub-Action Buttons
            if (radiusAnim > 5f) {
                val angles = listOf(140.0, 180.0, 220.0, 260.0)

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
                    .size(60.dp)
                    .shadow(12.dp, CircleShape)
            ) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.Close else if (isRecording) Icons.Default.Stop else Icons.Default.Add,
                    contentDescription = "Screen Recorder Menu",
                    modifier = Modifier
                        .size(30.dp)
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
            .size(48.dp)
            .shadow(8.dp, CircleShape)
            .background(backgroundColor, CircleShape)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
    }
}
