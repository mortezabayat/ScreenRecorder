package com.morteza.screen.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.morteza.screen.model.AppThemeMode
import com.morteza.screen.model.AudioSourceOption
import com.morteza.screen.ui.theme.AccentOrange
import com.morteza.screen.ui.theme.AccentRed
import com.morteza.screen.ui.theme.TealDark
import com.morteza.screen.ui.theme.TealPrimary
import com.morteza.screen.viewmodel.ScreenRecorderViewModel

@Composable
fun SettingsScreen(
    viewModel: ScreenRecorderViewModel,
    modifier: Modifier = Modifier
) {
    val countdownDuration by viewModel.countdownDuration.collectAsState()
    val videoConfig by viewModel.videoConfig.collectAsState()
    val audioConfig by viewModel.audioConfig.collectAsState()
    val overlayWatermark by viewModel.overlayWatermarkEnabled.collectAsState()
    val isFloatingVisible by viewModel.isFloatingMenuVisible.collectAsState()
    val videos by viewModel.videos.collectAsState()
    val storageFolder by viewModel.storageFolder.collectAsState()
    val autoCleanupConfig by viewModel.autoCleanupConfig.collectAsState()
    val batterySaverConfig by viewModel.batterySaverConfig.collectAsState()
    val batteryState by viewModel.batteryState.collectAsState()
    val showTouchesEnabled by viewModel.showTouchesEnabled.collectAsState()
    val touchColor by viewModel.touchColor.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()

    var showCustomFolderDialog by remember { mutableStateOf(false) }
    var customPathInput by remember { mutableStateOf("") }

    // SAF Document Tree Folder Picker Launcher
    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        uri?.let {
            val segment = it.lastPathSegment ?: ""
            val folderName = segment.substringAfterLast(':').ifEmpty { "Custom Storage" }
            val path = "/storage/emulated/0/$folderName"
            viewModel.setDestinationFolder(
                path = path,
                displayName = folderName,
                uriString = it.toString()
            )
        }
    }

    if (showCustomFolderDialog) {
        AlertDialog(
            onDismissRequest = { showCustomFolderDialog = false },
            title = { Text("Custom Destination Folder", color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter custom folder name or storage path:", color = Color.LightGray, fontSize = 13.sp)
                    OutlinedTextField(
                        value = customPathInput,
                        onValueChange = { customPathInput = it },
                        placeholder = { Text("e.g. Work/Recordings", color = Color.Gray) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = customPathInput.trim().trimStart('/')
                        if (trimmed.isNotEmpty()) {
                            val fullPath = "/storage/emulated/0/$trimmed"
                            viewModel.setDestinationFolder(
                                path = fullPath,
                                displayName = trimmed
                            )
                        }
                        showCustomFolderDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomFolderDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF1E293B)
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Preferences, destination folder, and recording options",
                color = Color.Gray,
                fontSize = 12.sp
            )
        }

        // App Theme & Appearance Switcher Card
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(TealDark),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (themeMode) {
                                        AppThemeMode.DARK -> Icons.Default.DarkMode
                                        AppThemeMode.LIGHT -> Icons.Default.LightMode
                                        AppThemeMode.SYSTEM -> Icons.Default.BrightnessAuto
                                    },
                                    contentDescription = null,
                                    tint = TealPrimary
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("App Theme & Appearance", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                Text("Select light, dark, or system mode", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                            }
                        }

                        Surface(
                            color = TealPrimary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = themeMode.title,
                                color = TealPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Theme Mode Selector Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val modes = listOf(
                            Triple(AppThemeMode.DARK, "Dark", Icons.Default.DarkMode),
                            Triple(AppThemeMode.LIGHT, "Light", Icons.Default.LightMode),
                            Triple(AppThemeMode.SYSTEM, "System", Icons.Default.BrightnessAuto)
                        )

                        modes.forEach { (mode, label, icon) ->
                            val isSelected = themeMode == mode
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setThemeMode(mode) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                label = { Text(label, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = TealPrimary,
                                    selectedLabelColor = Color.White,
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    labelColor = MaterialTheme.colorScheme.onSurface
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // Destination Folder Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(TealDark),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Folder, contentDescription = null, tint = TealPrimary)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Destination Folder", fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Location for saved video files", color = Color.Gray, fontSize = 11.sp)
                            }
                        }

                        Button(
                            onClick = { folderPickerLauncher.launch(null) },
                            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Browse", fontSize = 12.sp)
                        }
                    }

                    // Active Path Display
                    Surface(
                        color = Color(0xFF0F172A),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = storageFolder.displayName,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = storageFolder.path,
                                color = Color.Gray,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // Folder Presets
                    Text("Quick Presets:", color = Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    val presets = listOf(
                        "Movies" to "/storage/emulated/0/Movies/ScreenRecorder",
                        "DCIM" to "/storage/emulated/0/DCIM/ScreenCapture",
                        "Downloads" to "/storage/emulated/0/Download/ScreenRecordings",
                        "Documents" to "/storage/emulated/0/Documents/ScreenRecordings"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        presets.forEach { (name, path) ->
                            FilterChip(
                                selected = storageFolder.path == path,
                                onClick = {
                                    viewModel.setDestinationFolder(path = path, displayName = "$name / ScreenRecordings")
                                },
                                label = { Text(name, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = TealPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    // Custom Path Button
                    OutlinedButton(
                        onClick = {
                            customPathInput = storageFolder.displayName
                            showCustomFolderDialog = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.LightGray)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Enter Custom Folder Path", fontSize = 12.sp)
                    }

                    HorizontalDivider(color = Color(0xFF334155))

                    // Organize by Date Subfolders
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Organize by Date (YYYY-MM)", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                            Text("Store videos in monthly subdirectories", color = Color.Gray, fontSize = 11.sp)
                        }

                        Switch(
                            checked = storageFolder.organizeByDate,
                            onCheckedChange = { viewModel.toggleOrganizeByDate(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = TealPrimary)
                        )
                    }
                }
            }
        }

        // Video Quality Presets & Frame Rate Configuration Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(TealDark),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.HighQuality, contentDescription = null, tint = TealPrimary)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Video Quality & Resolution Presets", fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Choose capture resolution, frame rate, and bitrate", color = Color.Gray, fontSize = 11.sp)
                            }
                        }
                    }

                    // Active Spec Badge
                    val currentPresetName = when {
                        videoConfig.height <= 480 || videoConfig.width <= 480 -> "480p"
                        videoConfig.height <= 720 || videoConfig.width <= 720 -> "720p"
                        videoConfig.height <= 1080 || videoConfig.width <= 1080 -> "1080p"
                        else -> "1440p"
                    }

                    Surface(
                        color = Color(0xFF0F172A),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Active Quality Preset", color = Color.Gray, fontSize = 11.sp)
                                Text(
                                    text = "$currentPresetName • ${videoConfig.resolution}",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "${videoConfig.framerate} FPS • ${videoConfig.bitrate / 1000} kbps AVC",
                                    color = TealPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            val mbPerMin = (videoConfig.bitrate.toLong() * 60L) / (8L * 1024L * 1024L)
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Estimated Size", color = Color.Gray, fontSize = 11.sp)
                                Text(
                                    text = "~$mbPerMin MB/min",
                                    fontWeight = FontWeight.Bold,
                                    color = AccentOrange,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    // Resolution Presets Selector
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Resolution Preset:", color = Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        val qualityPresets = listOf(
                            Triple("480p", "480p SD", "854x480"),
                            Triple("720p", "720p HD", "1280x720"),
                            Triple("1080p", "1080p FHD", "1920x1080"),
                            Triple("1440p", "1440p 2K", "2560x1440")
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            qualityPresets.forEach { (presetKey, label, sub) ->
                                val isSelected = currentPresetName == presetKey
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.selectQualityPreset(presetKey) },
                                    label = {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            Text(sub, fontSize = 9.sp, color = if (isSelected) Color.White.copy(alpha = 0.8f) else Color.Gray)
                                        }
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = TealPrimary,
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = Color(0xFF334155))

                    // Frame Rate Selector (30 FPS vs 60 FPS)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Frame Rate (FPS):", color = Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            FilterChip(
                                selected = videoConfig.framerate == 30,
                                onClick = { viewModel.selectFramerate(30) },
                                label = {
                                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                        Text("30 FPS", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text("Standard • Battery efficient", fontSize = 10.sp, color = if (videoConfig.framerate == 30) Color.White.copy(alpha = 0.8f) else Color.Gray)
                                    }
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = TealPrimary,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.weight(1f)
                            )

                            FilterChip(
                                selected = videoConfig.framerate == 60,
                                onClick = { viewModel.selectFramerate(60) },
                                label = {
                                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                        Text("60 FPS", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text("Ultra-Smooth • High Motion", fontSize = 10.sp, color = if (videoConfig.framerate == 60) Color.White.copy(alpha = 0.8f) else Color.Gray)
                                    }
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = TealPrimary,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    HorizontalDivider(color = Color(0xFF334155))

                    // Orientation Selector (Landscape vs Portrait)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Screen Orientation", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                            Text("Widescreen (16:9) vs Mobile (9:16)", color = Color.Gray, fontSize = 11.sp)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Landscape", "Portrait").forEach { orient ->
                                FilterChip(
                                    selected = videoConfig.orientation == orient,
                                    onClick = { viewModel.setOrientation(orient) },
                                    label = { Text(orient, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = TealPrimary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Battery-Saver Auto-Downscale Mode
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header & Master Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            val batteryIcon = when {
                                batteryState.isCharging -> Icons.Default.BatteryChargingFull
                                batteryState.levelPercent <= batterySaverConfig.thresholdPercent -> Icons.Default.BatteryAlert
                                else -> Icons.Default.BatteryStd
                            }
                            val batteryTint = when {
                                batteryState.isCharging -> Color(0xFF00E676)
                                batteryState.levelPercent <= batterySaverConfig.thresholdPercent -> AccentOrange
                                else -> TealPrimary
                            }

                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(batteryTint.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(batteryIcon, contentDescription = null, tint = batteryTint)
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = "Battery-Saver Downscale",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Auto-lowers resolution & FPS when battery < 15%",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = batterySaverConfig.isEnabled,
                            onCheckedChange = { viewModel.setBatterySaverEnabled(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = TealPrimary)
                        )
                    }

                    // Live Battery Status & Saver Active Pill
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (batterySaverConfig.isBatterySaverActive) AccentOrange.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = if (batterySaverConfig.isBatterySaverActive) androidx.compose.foundation.BorderStroke(1.dp, AccentOrange) else null,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Current Battery: ",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${batteryState.levelPercent}%",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (batteryState.levelPercent <= batterySaverConfig.thresholdPercent) AccentOrange else Color(0xFF00E676)
                                    )
                                    if (batteryState.isCharging) {
                                        Text(
                                            text = " (Charging)",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF00E676)
                                        )
                                    }
                                    if (batteryState.isSimulated) {
                                        Text(
                                            text = " • Simulated",
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }

                                if (batterySaverConfig.isBatterySaverActive) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = AccentOrange
                                    ) {
                                        Text(
                                            text = "THROTTLED: ${batterySaverConfig.targetResolutionPreset} • ${batterySaverConfig.targetFramerate}FPS",
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                } else {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (batterySaverConfig.isEnabled) TealDark else Color.Gray.copy(alpha = 0.3f)
                                    ) {
                                        Text(
                                            text = if (batterySaverConfig.isEnabled) "ARMED (<${batterySaverConfig.thresholdPercent}%)" else "OFF",
                                            color = if (batterySaverConfig.isEnabled) TealPrimary else Color.Gray,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            // Linear indicator
                            LinearProgressIndicator(
                                progress = { (batteryState.levelPercent / 100f).coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = if (batteryState.levelPercent <= batterySaverConfig.thresholdPercent) AccentOrange else Color(0xFF00E676),
                                trackColor = Color(0xFF334155)
                            )
                        }
                    }

                    if (batterySaverConfig.isEnabled) {
                        // Threshold selector chips
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Trigger Threshold",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(10, 15, 20, 25).forEach { pct ->
                                    FilterChip(
                                        selected = batterySaverConfig.thresholdPercent == pct,
                                        onClick = { viewModel.setBatterySaverThreshold(pct) },
                                        label = {
                                            Text(
                                                text = if (pct == 15) "$pct% (Default)" else "$pct%",
                                                fontSize = 11.sp
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = TealDark,
                                            selectedLabelColor = TealPrimary
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        // Target downscaled resolution preset
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Battery-Saver Target Resolution",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(
                                    "480p" to "480p SD (Max Savings)",
                                    "720p" to "720p HD (Balanced)"
                                ).forEach { (preset, label) ->
                                    FilterChip(
                                        selected = batterySaverConfig.targetResolutionPreset == preset,
                                        onClick = { viewModel.setBatterySaverTargetPreset(preset) },
                                        label = { Text(label, fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = TealDark,
                                            selectedLabelColor = TealPrimary
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        // Target downscaled framerate
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Battery-Saver Target Framerate",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(
                                    30 to "30 FPS Standard",
                                    24 to "24 FPS Ultra-Eco"
                                ).forEach { (fps, label) ->
                                    FilterChip(
                                        selected = batterySaverConfig.targetFramerate == fps,
                                        onClick = { viewModel.setBatterySaverTargetFramerate(fps) },
                                        label = { Text(label, fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = TealDark,
                                            selectedLabelColor = TealPrimary
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        // Critical Auto-Stop Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Auto-Stop at Critical Battery (≤5%)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Safely saves video before phone completely powers off",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = batterySaverConfig.autoStopAtCritical,
                                onCheckedChange = { viewModel.setBatterySaverAutoStopAtCritical(it) },
                                colors = SwitchDefaults.colors(checkedThumbColor = AccentOrange)
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                        // Simulator / Tester Controls
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "🧪 Battery Simulator & Testing",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                            Text(
                                text = "Test low battery downscaling without draining physical device battery:",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.setSimulatedBattery(12, isCharging = false) },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentOrange),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                                ) {
                                    Text("Simulate 12%", fontSize = 10.sp)
                                }

                                OutlinedButton(
                                    onClick = { viewModel.setSimulatedBattery(85, isCharging = false) },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF00E676)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                                ) {
                                    Text("Normal 85%", fontSize = 10.sp)
                                }

                                OutlinedButton(
                                    onClick = { viewModel.setSimulatedBattery(45, isCharging = true) },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TealPrimary),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                                ) {
                                    Text("Charging", fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Countdown Timer Option
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Countdown Timer Animation", fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Animated numbers countdown before screen capture begins", color = Color.Gray, fontSize = 12.sp)

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(0 to "Off (0s)", 3 to "3s", 5 to "5s").forEach { (sec, label) ->
                            FilterChip(
                                selected = countdownDuration == sec,
                                onClick = { viewModel.setCountdownDuration(sec) },
                                label = { Text(label, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = TealPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }
        }

        // Recording Limits & Auto-Stop Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Recording Limits & Auto-Stop", fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Configure maximum recording duration or file size thresholds", color = Color.Gray, fontSize = 12.sp)
                    }

                    Button(
                        onClick = { viewModel.navigateTo(com.morteza.screen.model.AppScreen.LIMITS) },
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Configure", fontSize = 12.sp)
                    }
                }
            }
        }

        // Audio Source & Recording Mode
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(TealDark),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.VolumeUp, contentDescription = null, tint = TealPrimary)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Audio Source",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Select audio input for screen recordings",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Badge showing current selection
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = TealPrimary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = audioConfig.audioSource.shortLabel,
                                color = TealPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Audio Source Selection List
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        AudioSourceItem(
                            title = "Microphone (Mic)",
                            description = "Records external voice commentary and surroundings",
                            icon = Icons.Default.Mic,
                            isSelected = audioConfig.audioSource == AudioSourceOption.MIC,
                            onClick = { viewModel.setAudioSource(AudioSourceOption.MIC) }
                        )

                        AudioSourceItem(
                            title = "Internal Audio",
                            description = "Records direct digital sound from games and apps (Android 10+)",
                            icon = Icons.Default.GraphicEq,
                            tag = "Android 10+",
                            isSelected = audioConfig.audioSource == AudioSourceOption.INTERNAL,
                            onClick = { viewModel.setAudioSource(AudioSourceOption.INTERNAL) }
                        )

                        AudioSourceItem(
                            title = "Mic + Internal Audio",
                            description = "Mixes microphone voice and internal device sound simultaneously",
                            icon = Icons.Default.Audiotrack,
                            tag = "Dual Track",
                            isSelected = audioConfig.audioSource == AudioSourceOption.MIC_AND_INTERNAL,
                            onClick = { viewModel.setAudioSource(AudioSourceOption.MIC_AND_INTERNAL) }
                        )

                        AudioSourceItem(
                            title = "Mute (No Audio)",
                            description = "Captures pure video stream without any sound tracks",
                            icon = Icons.Default.MicOff,
                            isSelected = audioConfig.audioSource == AudioSourceOption.MUTE,
                            onClick = { viewModel.setAudioSource(AudioSourceOption.MUTE) }
                        )
                    }

                    // Dual Volume Mixers (shown when Mic + Internal Audio is selected)
                    if (audioConfig.audioSource == AudioSourceOption.MIC_AND_INTERNAL) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Dual Audio Volume Mixers",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                // Microphone Volume Slider
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Microphone Commentary", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("${(audioConfig.micAudioVolume * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TealPrimary)
                                    }
                                    Slider(
                                        value = audioConfig.micAudioVolume,
                                        onValueChange = { viewModel.setAudioVolumes(audioConfig.internalAudioVolume, it) },
                                        valueRange = 0f..1.5f,
                                        colors = SliderDefaults.colors(thumbColor = TealPrimary, activeTrackColor = TealPrimary)
                                    )
                                }

                                // Internal Audio Volume Slider
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Internal / System Audio", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("${(audioConfig.internalAudioVolume * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00E5FF))
                                    }
                                    Slider(
                                        value = audioConfig.internalAudioVolume,
                                        onValueChange = { viewModel.setAudioVolumes(it, audioConfig.micAudioVolume) },
                                        valueRange = 0f..1.5f,
                                        colors = SliderDefaults.colors(thumbColor = Color(0xFF00E5FF), activeTrackColor = Color(0xFF00E5FF))
                                    )
                                }
                            }
                        }
                    }

                    // Format and Channel settings (when not muted)
                    if (audioConfig.audioSource != AudioSourceOption.MUTE) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Audio Format & Channels",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = audioConfig.sampleRate == 44100,
                                    onClick = { viewModel.setAudioSampleRate(44100) },
                                    label = { Text("44.1 kHz Standard", fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = TealDark,
                                        selectedLabelColor = TealPrimary
                                    )
                                )

                                FilterChip(
                                    selected = audioConfig.sampleRate == 48000,
                                    onClick = { viewModel.setAudioSampleRate(48000) },
                                    label = { Text("48.0 kHz Studio", fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = TealDark,
                                        selectedLabelColor = TealPrimary
                                    )
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = audioConfig.channelCount == 2,
                                    onClick = { viewModel.setAudioChannels(2) },
                                    label = { Text("Stereo (2 Channels)", fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = TealDark,
                                        selectedLabelColor = TealPrimary
                                    )
                                )

                                FilterChip(
                                    selected = audioConfig.channelCount == 1,
                                    onClick = { viewModel.setAudioChannels(1) },
                                    label = { Text("Mono (1 Channel)", fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = TealDark,
                                        selectedLabelColor = TealPrimary
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // BitmapOverlay GL Watermark
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Bitmap Overlay Video Processor", fontWeight = FontWeight.Bold, color = Color.White)
                        Text("GL-based canvas timestamp & logo badge on video frames", color = Color.Gray, fontSize = 12.sp)
                    }

                    Switch(
                        checked = overlayWatermark,
                        onCheckedChange = { viewModel.setOverlayWatermark(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = TealPrimary)
                    )
                }
            }
        }

        // Floating Circular Menu
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Floating Action Circular Menu", fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Always-on-top draggable floating action button", color = Color.Gray, fontSize = 12.sp)
                    }

                    Switch(
                        checked = isFloatingVisible,
                        onCheckedChange = { viewModel.toggleFloatingMenu() },
                        colors = SwitchDefaults.colors(checkedThumbColor = TealPrimary)
                    )
                }
            }
        }

        // Visual Touch Indicator (Show Taps) Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(TealDark),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.TouchApp, contentDescription = null, tint = TealPrimary)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Visual Touch Indicator (Show Taps)", fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Display animated ripples at touch points while recording", color = Color.Gray, fontSize = 11.sp)
                            }
                        }

                        Switch(
                            checked = showTouchesEnabled,
                            onCheckedChange = { viewModel.setShowTouchesEnabled(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = TealPrimary)
                        )
                    }

                    if (showTouchesEnabled) {
                        HorizontalDivider(color = Color(0xFF334155))

                        Text("Indicator Color:", color = Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)

                        val colors = listOf("Teal", "White", "Cyan", "Amber", "Red")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            colors.forEach { cName ->
                                FilterChip(
                                    selected = touchColor == cName,
                                    onClick = { viewModel.setTouchColor(cName) },
                                    label = { Text(cName, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = TealPrimary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Storage Management & Auto-Delete Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header & Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(AccentRed.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = AccentRed)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Auto-Delete Old Recordings", fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Automatically delete files after set number of days", color = Color.Gray, fontSize = 11.sp)
                            }
                        }

                        Switch(
                            checked = autoCleanupConfig.autoDeleteEnabled,
                            onCheckedChange = {
                                viewModel.updateAutoCleanupConfig(autoCleanupConfig.copy(autoDeleteEnabled = it))
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = AccentRed)
                        )
                    }

                    if (autoCleanupConfig.autoDeleteEnabled) {
                        HorizontalDivider(color = Color(0xFF334155))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Retention Period:", color = Color.LightGray, fontSize = 13.sp)
                            Text(
                                text = "${autoCleanupConfig.retentionDays} Days",
                                fontWeight = FontWeight.Black,
                                color = AccentRed,
                                fontSize = 15.sp
                            )
                        }

                        // Presets
                        val retentionPresets = listOf(3, 7, 14, 30, 60, 90)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            retentionPresets.forEach { days ->
                                FilterChip(
                                    selected = autoCleanupConfig.retentionDays == days,
                                    onClick = {
                                        viewModel.updateAutoCleanupConfig(autoCleanupConfig.copy(retentionDays = days))
                                    },
                                    label = { Text("${days}d", fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = AccentRed,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        // Slider
                        Slider(
                            value = autoCleanupConfig.retentionDays.toFloat(),
                            onValueChange = {
                                viewModel.updateAutoCleanupConfig(autoCleanupConfig.copy(retentionDays = it.toInt()))
                            },
                            valueRange = 1f..120f,
                            steps = 118,
                            colors = SliderDefaults.colors(
                                thumbColor = AccentRed,
                                activeTrackColor = AccentRed,
                                inactiveTrackColor = Color.DarkGray
                            )
                        )

                        // Protect Starred Videos Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Protect Starred Recordings", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                                Text("Never auto-delete recordings marked with a star", color = Color.Gray, fontSize = 11.sp)
                            }
                            Switch(
                                checked = autoCleanupConfig.protectStarredVideos,
                                onCheckedChange = {
                                    viewModel.updateAutoCleanupConfig(autoCleanupConfig.copy(protectStarredVideos = it))
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = TealPrimary)
                            )
                        }

                        // Reclaim Calculation Box
                        val cutoff = System.currentTimeMillis() - (autoCleanupConfig.retentionDays * 86_400_000L)
                        val eligibleToDelete = videos.filter {
                            it.timestamp < cutoff && !(autoCleanupConfig.protectStarredVideos && it.isStarred)
                        }
                        val reclaimMB = eligibleToDelete.sumOf { it.sizeBytes } / (1024 * 1024)

                        Surface(
                            color = Color(0xFF0F172A),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "${eligibleToDelete.size} recording(s) older than ${autoCleanupConfig.retentionDays}d",
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = "Can reclaim ~$reclaimMB MB of storage",
                                        color = AccentOrange,
                                        fontSize = 11.sp
                                    )
                                }

                                Button(
                                    onClick = { viewModel.performAutoCleanup(manualRun = true) },
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text("Clean Now", fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = Color(0xFF334155))

                    // Manual Bulk Clear
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Total Stored Recordings: ${videos.size}", color = Color.LightGray, fontSize = 12.sp)
                            val totalMB = videos.sumOf { it.sizeBytes } / (1024 * 1024)
                            Text("Total disk usage: ~$totalMB MB", color = Color.Gray, fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = { viewModel.clearAllVideos() },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentRed),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = AccentRed, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Clear All", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AudioSourceItem(
    title: String,
    description: String,
    icon: ImageVector,
    isSelected: Boolean,
    tag: String? = null,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) TealPrimary else MaterialTheme.colorScheme.surfaceVariant
    val containerBg = if (isSelected) TealPrimary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = containerBg,
        border = androidx.compose.foundation.BorderStroke(if (isSelected) 1.5.dp else 1.dp, borderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) TealPrimary else MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (tag != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF00E5FF).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = tag,
                                fontSize = 9.sp,
                                color = Color(0xFF00E5FF),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    text = description,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = TealPrimary,
                    unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

