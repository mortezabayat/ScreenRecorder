package com.morteza.screen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.morteza.screen.model.AppScreen
import com.morteza.screen.model.RecordingStatus
import com.morteza.screen.ui.components.CountDownOverlay
import com.morteza.screen.ui.components.FloatingCircularMenu
import com.morteza.screen.ui.components.RecordingTimerOverlay
import com.morteza.screen.ui.components.VideoPlayerDialog
import com.morteza.screen.ui.screens.*
import com.morteza.screen.ui.theme.*
import com.morteza.screen.viewmodel.ScreenRecorderViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: ScreenRecorderViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ScreenTheme {
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()
                val currentScreen by viewModel.currentScreen.collectAsState()
                val recordingStatus by viewModel.recordingStatus.collectAsState()
                val elapsedSeconds by viewModel.elapsedSeconds.collectAsState()
                val toastMessage by viewModel.toastMessage.collectAsState()
                val selectedVideo by viewModel.selectedVideoForPreview.collectAsState()
                val isPainterActive by viewModel.isPainterActive.collectAsState()

                val isRecording = recordingStatus == RecordingStatus.RECORDING || recordingStatus == RecordingStatus.PAUSED

                val snackbarHostState = remember { SnackbarHostState() }
                LaunchedEffect(toastMessage) {
                    toastMessage?.let {
                        snackbarHostState.showSnackbar(it)
                    }
                }

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        ModalDrawerSheet(
                            drawerContainerColor = Color(0xFF0F172A),
                            drawerContentColor = Color.White
                        ) {
                            // Drawer Header (migrated from nav_header_main.xml)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(TealDark)
                                    .padding(24.dp)
                            ) {
                                Column {
                                    Box(
                                        modifier = Modifier
                                            .size(52.dp)
                                            .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(14.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Videocam,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Text(
                                        text = "Screen",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "android.studio@android.com",
                                        fontSize = 12.sp,
                                        color = Color.LightGray
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Main Drawer Items (from activity_main_drawer.xml)
                            DrawerItem(
                                label = "Home",
                                icon = Icons.Default.Home,
                                selected = currentScreen == AppScreen.HOME,
                                onClick = {
                                    viewModel.navigateTo(AppScreen.HOME)
                                    scope.launch { drawerState.close() }
                                }
                            )

                            DrawerItem(
                                label = "Gallery",
                                icon = Icons.Default.Collections,
                                selected = currentScreen == AppScreen.GALLERY,
                                onClick = {
                                    viewModel.navigateTo(AppScreen.GALLERY)
                                    scope.launch { drawerState.close() }
                                }
                            )

                            DrawerItem(
                                label = "Slideshow",
                                icon = Icons.Default.Slideshow,
                                selected = currentScreen == AppScreen.SLIDESHOW,
                                onClick = {
                                    viewModel.navigateTo(AppScreen.SLIDESHOW)
                                    scope.launch { drawerState.close() }
                                }
                            )

                            DrawerItem(
                                label = "Tools",
                                icon = Icons.Default.Build,
                                selected = currentScreen == AppScreen.TOOLS,
                                onClick = {
                                    viewModel.navigateTo(AppScreen.TOOLS)
                                    scope.launch { drawerState.close() }
                                }
                            )

                            DrawerItem(
                                label = "Painter",
                                icon = Icons.Default.Palette,
                                selected = isPainterActive,
                                onClick = {
                                    viewModel.togglePainter()
                                    scope.launch { drawerState.close() }
                                }
                            )

                            DrawerItem(
                                label = "Settings",
                                icon = Icons.Default.Settings,
                                selected = currentScreen == AppScreen.SETTINGS,
                                onClick = {
                                    viewModel.navigateTo(AppScreen.SETTINGS)
                                    scope.launch { drawerState.close() }
                                }
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp), color = Color(0xFF334155))

                            Text(
                                text = "Communicate",
                                color = Color.Gray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                            )

                            DrawerItem(
                                label = "Share",
                                icon = Icons.Default.Share,
                                selected = currentScreen == AppScreen.SHARE,
                                onClick = {
                                    viewModel.navigateTo(AppScreen.SHARE)
                                    scope.launch { drawerState.close() }
                                }
                            )

                            DrawerItem(
                                label = "Send",
                                icon = Icons.Default.Send,
                                selected = currentScreen == AppScreen.SEND,
                                onClick = {
                                    viewModel.navigateTo(AppScreen.SEND)
                                    scope.launch { drawerState.close() }
                                }
                            )
                        }
                    }
                ) {
                    Scaffold(
                        topBar = {
                            TopAppBar(
                                title = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = currentScreen.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 19.sp
                                        )
                                        if (isRecording) {
                                            Spacer(modifier = Modifier.width(12.dp))
                                            val mins = elapsedSeconds / 60
                                            val secs = elapsedSeconds % 60
                                            Surface(
                                                color = AccentRed,
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(6.dp)
                                                            .background(Color.White, CircleShape)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = String.format("%02d:%02d", mins, secs),
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White
                                                    )
                                                }
                                            }
                                        }
                                    }
                                },
                                navigationIcon = {
                                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                                    }
                                },
                                actions = {
                                    if (!isRecording) {
                                        IconButton(onClick = { viewModel.startRecordingFlow() }) {
                                            Icon(Icons.Default.FiberManualRecord, contentDescription = "Record", tint = AccentRed)
                                        }
                                    } else {
                                        IconButton(onClick = { viewModel.stopRecording() }) {
                                            Icon(Icons.Default.Stop, contentDescription = "Stop", tint = AccentRed)
                                        }
                                    }

                                    IconButton(onClick = { viewModel.togglePainter() }) {
                                        Icon(Icons.Default.Palette, contentDescription = "Painter", tint = Color.White)
                                    }

                                    IconButton(onClick = { viewModel.navigateTo(AppScreen.SETTINGS) }) {
                                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = TealPrimary,
                                    titleContentColor = Color.White,
                                    navigationIconContentColor = Color.White,
                                    actionIconContentColor = Color.White
                                )
                            )
                        },
                        snackbarHost = { SnackbarHost(snackbarHostState) },
                        containerColor = DarkBackground
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            // Active Screen Content
                            when (currentScreen) {
                                AppScreen.HOME -> HomeScreen(viewModel = viewModel)
                                AppScreen.GALLERY -> GalleryScreen(viewModel = viewModel)
                                AppScreen.SLIDESHOW -> SlideshowScreen(viewModel = viewModel)
                                AppScreen.TOOLS -> ToolsScreen(viewModel = viewModel)
                                AppScreen.SETTINGS -> SettingsScreen(viewModel = viewModel)
                                AppScreen.SHARE -> ShareScreen(viewModel = viewModel)
                                AppScreen.SEND -> SendScreen(viewModel = viewModel)
                                AppScreen.PAINTER -> PainterScreen(viewModel = viewModel)
                            }

                            // Interactive Floating Circular Menu overlay
                            FloatingCircularMenu(viewModel = viewModel)

                            // Active Screen Recording Session Timer Overlay
                            RecordingTimerOverlay(viewModel = viewModel)

                            // 5s Countdown overlay
                            CountDownOverlay(viewModel = viewModel)

                            // Painter screen markup overlay
                            if (isPainterActive && currentScreen != AppScreen.PAINTER) {
                                PainterScreen(viewModel = viewModel)
                            }

                            // Video Preview Player Dialog
                            VideoPlayerDialog(
                                video = selectedVideo,
                                onDismiss = { viewModel.selectVideoForPreview(null) },
                                onDelete = { viewModel.deleteVideo(it) },
                                onShare = { viewModel.showToast("Sharing ${it.name}") }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DrawerItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        label = { Text(label, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) },
        icon = { Icon(icon, contentDescription = null, tint = if (selected) TealPrimary else Color.Gray) },
        selected = selected,
        onClick = onClick,
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = TealDark.copy(alpha = 0.3f),
            selectedTextColor = Color.White,
            unselectedTextColor = Color.LightGray
        )
    )
}
