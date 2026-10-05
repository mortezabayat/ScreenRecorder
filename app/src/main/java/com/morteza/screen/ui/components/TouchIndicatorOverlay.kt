package com.morteza.screen.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import com.morteza.screen.model.RecordingStatus
import com.morteza.screen.viewmodel.ScreenRecorderViewModel
import kotlinx.coroutines.launch

private data class ActiveTouch(
    val id: Long,
    val position: Offset
)

private data class TapRipple(
    val id: Long,
    val center: Offset,
    val radiusAnim: Animatable<Float, *>,
    val alphaAnim: Animatable<Float, *>
)

@Composable
fun TouchIndicatorOverlay(
    viewModel: ScreenRecorderViewModel,
    modifier: Modifier = Modifier
) {
    val status by viewModel.recordingStatus.collectAsState()
    val isTouchesEnabled by viewModel.showTouchesEnabled.collectAsState()
    val touchColorName by viewModel.touchColor.collectAsState()

    // Active touch visualization is shown during active recording or pause if enabled
    val isRecordingActive = (status == RecordingStatus.RECORDING || status == RecordingStatus.PAUSED) && isTouchesEnabled

    if (!isRecordingActive) return

    val primaryColor = when (touchColorName) {
        "White" -> Color.White
        "Cyan" -> Color(0xFF00E5FF)
        "Amber" -> Color(0xFFFFB300)
        "Red" -> Color(0xFFFF5252)
        else -> Color(0xFF00ADB5) // TealPrimary
    }

    var activePointers by remember { mutableStateOf<List<ActiveTouch>>(emptyList()) }
    val ripples = remember { mutableStateListOf<TapRipple>() }
    val coroutineScope = rememberCoroutineScope()

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        // Pass.Initial intercepts touch events for visualization without consuming them!
                        val event = awaitPointerEvent(PointerEventPass.Initial)

                        val currentTouches = mutableListOf<ActiveTouch>()
                        for (change in event.changes) {
                            if (change.pressed) {
                                currentTouches.add(ActiveTouch(change.id.value, change.position))

                                // If this pointer just touched down, trigger an animated expanding ripple
                                if (!change.previousPressed) {
                                    val rippleId = System.nanoTime()
                                    val radiusAnim = Animatable(10f)
                                    val alphaAnim = Animatable(0.9f)
                                    val ripple = TapRipple(rippleId, change.position, radiusAnim, alphaAnim)
                                    ripples.add(ripple)

                                    coroutineScope.launch {
                                        launch {
                                            radiusAnim.animateTo(
                                                targetValue = 65f,
                                                animationSpec = tween(durationMillis = 350, easing = LinearEasing)
                                            )
                                        }
                                        launch {
                                            alphaAnim.animateTo(
                                                targetValue = 0f,
                                                animationSpec = tween(durationMillis = 350, easing = LinearEasing)
                                            )
                                        }
                                        // Clean up ripple after animation
                                        ripples.removeAll { it.id == rippleId }
                                    }
                                }
                            }
                        }
                        activePointers = currentTouches
                    }
                }
            }
    ) {
        // 1. Draw animated tap ripples
        for (ripple in ripples) {
            val r = ripple.radiusAnim.value
            val a = ripple.alphaAnim.value
            if (a > 0f) {
                // Expanding outer ring
                drawCircle(
                    color = primaryColor.copy(alpha = a * 0.7f),
                    radius = r,
                    center = ripple.center,
                    style = Stroke(width = 3.5f)
                )
                // Soft expanding halo
                drawCircle(
                    color = primaryColor.copy(alpha = a * 0.25f),
                    radius = r * 0.85f,
                    center = ripple.center
                )
            }
        }

        // 2. Draw persistent active touches / pointer indicators
        for (touch in activePointers) {
            // Outer translucent glow ring
            drawCircle(
                color = primaryColor.copy(alpha = 0.35f),
                radius = 32f,
                center = touch.position
            )
            // Stroke border
            drawCircle(
                color = Color.White.copy(alpha = 0.85f),
                radius = 22f,
                center = touch.position,
                style = Stroke(width = 2.5f)
            )
            // Inner vibrant solid dot
            drawCircle(
                color = primaryColor,
                radius = 16f,
                center = touch.position
            )
            // Center white pinpoint highlight
            drawCircle(
                color = Color.White,
                radius = 6f,
                center = touch.position
            )
        }
    }
}
