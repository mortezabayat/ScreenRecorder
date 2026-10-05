package com.morteza.screen.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.morteza.screen.model.RecordingStatus
import com.morteza.screen.ui.theme.AccentRed
import com.morteza.screen.viewmodel.ScreenRecorderViewModel

@Composable
fun CountDownOverlay(
    viewModel: ScreenRecorderViewModel,
    modifier: Modifier = Modifier
) {
    val status by viewModel.recordingStatus.collectAsState()
    val count by viewModel.countdownValue.collectAsState()

    if (status != RecordingStatus.COUNTDOWN || count == null) return

    val scale = remember { Animatable(1.4f) }
    val alpha = remember { Animatable(1f) }

    LaunchedEffect(count) {
        scale.snapTo(1.6f)
        alpha.snapTo(1f)
        scale.animateTo(0.8f, animationSpec = tween(900))
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$count",
                fontSize = 120.sp,
                fontWeight = FontWeight.Black,
                color = AccentRed,
                modifier = Modifier.scale(scale.value)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Screen Recording starts soon...",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.9f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedButton(
                onClick = { viewModel.cancelCountdown() },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Icon(Icons.Default.Close, contentDescription = null, tint = AccentRed)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cancel Countdown")
            }
        }
    }
}
