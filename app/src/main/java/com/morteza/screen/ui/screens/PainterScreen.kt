package com.morteza.screen.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.morteza.screen.model.DrawPath
import com.morteza.screen.ui.theme.AccentRed
import com.morteza.screen.ui.theme.TealPrimary
import com.morteza.screen.viewmodel.ScreenRecorderViewModel

@Composable
fun PainterScreen(
    viewModel: ScreenRecorderViewModel,
    modifier: Modifier = Modifier
) {
    val paths by viewModel.drawPaths.collectAsState()
    val currentPath by viewModel.currentPath.collectAsState()

    var selectedColor by remember { mutableStateOf(AccentRed) }
    var strokeWidth by remember { mutableFloatStateOf(10f) }
    var isHighlighter by remember { mutableStateOf(false) }
    var isEraser by remember { mutableStateOf(false) }

    val colors = listOf(
        AccentRed,
        TealPrimary,
        Color(0xFFFFEB3B),
        Color(0xFF2196F3),
        Color.White
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
    ) {
        // Drawing Canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(selectedColor, strokeWidth, isHighlighter, isEraser) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            viewModel.startDrawing(
                                point = offset,
                                color = selectedColor,
                                strokeWidth = strokeWidth,
                                isHighlighter = isHighlighter,
                                isEraser = isEraser
                            )
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            viewModel.continueDrawing(change.position)
                        },
                        onDragEnd = {
                            viewModel.endDrawing()
                        }
                    )
                }
        ) {
            val allPaths = if (currentPath != null) paths + currentPath!! else paths

            allPaths.forEach { drawPath ->
                if (drawPath.points.size > 1) {
                    val path = Path().apply {
                        moveTo(drawPath.points.first().x, drawPath.points.first().y)
                        for (i in 1 until drawPath.points.size) {
                            lineTo(drawPath.points[i].x, drawPath.points[i].y)
                        }
                    }

                    val color = if (drawPath.isEraser) Color.Black
                    else if (drawPath.isHighlighter) drawPath.color.copy(alpha = 0.4f)
                    else drawPath.color

                    val width = if (drawPath.isEraser) drawPath.strokeWidth * 2.5f
                    else if (drawPath.isHighlighter) drawPath.strokeWidth * 2f
                    else drawPath.strokeWidth

                    drawPath(
                        path = path,
                        color = color,
                        style = Stroke(
                            width = width,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            }
        }

        // Painter Bottom Toolbar
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .wrapContentWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.95f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Pen mode
                IconButton(
                    onClick = {
                        isHighlighter = false
                        isEraser = false
                    },
                    modifier = Modifier
                        .size(38.dp)
                        .background(if (!isHighlighter && !isEraser) TealPrimary else Color.Transparent, CircleShape)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Pen", tint = Color.White)
                }

                // Highlighter mode
                IconButton(
                    onClick = {
                        isHighlighter = true
                        isEraser = false
                    },
                    modifier = Modifier
                        .size(38.dp)
                        .background(if (isHighlighter) TealPrimary else Color.Transparent, CircleShape)
                ) {
                    Icon(Icons.Default.Brush, contentDescription = "Highlighter", tint = Color.White)
                }

                // Eraser mode
                IconButton(
                    onClick = {
                        isEraser = true
                        isHighlighter = false
                    },
                    modifier = Modifier
                        .size(38.dp)
                        .background(if (isEraser) TealPrimary else Color.Transparent, CircleShape)
                ) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = "Eraser", tint = Color.White)
                }

                Box(
                    modifier = Modifier
                        .height(24.dp)
                        .width(1.dp)
                        .background(Color.DarkGray)
                )

                // Color Palette
                if (!isEraser) {
                    colors.forEach { col ->
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(col)
                                .border(
                                    width = if (selectedColor == col) 3.dp else 1.dp,
                                    color = if (selectedColor == col) Color.White else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = col }
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .height(24.dp)
                        .width(1.dp)
                        .background(Color.DarkGray)
                )

                // Undo
                IconButton(onClick = { viewModel.undoDrawing() }) {
                    Icon(Icons.Default.Undo, contentDescription = "Undo", tint = Color.White)
                }

                // Clear
                IconButton(onClick = { viewModel.clearDrawing() }) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = AccentRed)
                }

                // Close Painter
                IconButton(onClick = { viewModel.togglePainter() }) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }
        }
    }
}
