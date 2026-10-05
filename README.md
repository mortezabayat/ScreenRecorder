# Screen Recorder (Android • Jetpack Compose)

A modern, full-featured **Screen Recorder** Android application built with **Kotlin**, **Jetpack Compose**, and **Material 3**.

---

## 🚀 Key Features

### 1. Foreground Service & Notification Shade Controls
- **Persistent Ongoing Notification**: Powered by `ScreenRecordService` with `foregroundServiceType="mediaProjection"`.
- **In-Notification Actions**: Users can **Pause**, **Resume**, or **Stop & Save** active recordings directly from the Android notification shade without reopening the app.
- **Live Timecode & Status**: Continuously displays elapsed time (`01:23`), resolution, and current capture status.
- **In-App Notification Shade Simulator**: Slide-down notification preview banner allows testing and interacting with notification controls in preview and emulator environments.

### 2. Visual Touch & Tap Indicator Overlay ("Show Taps")
- **Touch Event Visualization**: Renders high-contrast animated ripples and glowing touch dots at finger contact coordinates while recording.
- **Non-Consuming Interception**: Employs `PointerEventPass.Initial` within `awaitPointerEventScope` to capture multi-touch gestures without blocking underlying buttons, menus, or scrolling.
- **Expanding Tap Ripples**: Touch-down events trigger dynamic expanding ripple shockwaves with radial alpha fading.
- **Customizable Styling**: Configurable touch indicator colors (Teal, White, Cyan, Amber, Red).

### 3. Video Quality Presets & Frame Rate Configuration
- **Resolution Presets**:
  - `480p (SD)`: 854x480 (Compact file size, fast sharing, 1.5 Mbps)
  - `720p (HD)`: 1280x720 (Balanced quality & storage, 2.5 Mbps)
  - `1080p (FHD)`: 1920x1080 (Sharp, recommended for presentations & app demos, 4.0 Mbps)
  - `1440p (2K)`: 2560x1440 (Ultra-sharp high-density capture, 8.0 Mbps)
- **Frame Rate Selection**:
  - `30 FPS (Standard)`: Smooth, battery efficient.
  - `60 FPS (Ultra-Smooth)`: High-motion gaming, fluid UI transitions.
- **Orientation & Bitrate Scaling**: Supports `Landscape` (16:9) and `Portrait` (9:16) with dynamic bitrate adjustments and real-time disk usage estimates (`~MB/min`).
- **Quick-Access Modal**: Accessible both from the main dashboard QuickChip and the dedicated Settings card.

### 4. Dynamic Theme Switcher (Dark, Light, System)
- **Dynamic Theming Engine**: Supports **Dark Mode**, **Light Mode**, and **System Default** (adapting automatically to Android's `isSystemInDarkTheme()`).
- **Material 3 Color Schemes**: Handcrafted palette with primary Teal (`#008577`), deep dark backgrounds (`#0F172A`), clean light surfaces (`#FFFFFF` / `#F1F5F9`), and high-visibility accents (Red `#DD2C00`, Orange `#FF6D00`).
- **Instant Switching**: Available at the top of the Settings screen with live preview badges.

### 5. Future Date & Time Scheduled Recording
- **Schedule Future Sessions**: Automate recordings to start at a specific future date and time (for live streams, webinars, gaming tournaments, or automated app testing).
- **Time Offset Presets**: Quick options (`In 10m`, `In 30m`, `In 1 hour`, `In 3 hours`, `Tomorrow at 9:00 AM`).
- **Session Configuration**: Set custom titles, max duration limits (e.g. 15m, 30m), quality presets, and audio preferences per scheduled item.
- **Background Timer Engine**: Periodic scheduler constantly checks armed tasks, updates live countdown badges (`Starts in 14m 20s`), and auto-starts the session when the target epoch is reached.
- **Queue Management**: Arm, Disarm, Start Now, Cancel, or Delete scheduled recordings.

### 6. Floating Circular Action Menu (Always-on-Top)
- **Draggable Radial FAB**: Smoothly snaps to screen edges and expands radial tool buttons (Record, Stop, Pause, Painter markup, Tools, and Settings).
- **Idle Transparency**: Fades to subtle opacity when untouched and restores full contrast on interaction.

### 7. On-Screen Painter Annotation Tool
- **Live Screen Markup**: Annotate screens during recording with freehand drawing, highlighter mode (translucent glow), and eraser.
- **Customizable Palette**: Multiple stroke widths and colors (Teal, Red, Amber, Cyan, Green, Purple, White, Black) with undo and clear options.

### 8. In-App Video Trimming & Media Player
- **Interactive Trimmer**: Dual-thumb range slider to inspect video durations, preview start/end timecodes, and save trimmed clips.
- **Full Player Dialog**: Video preview player with play/pause, seek slider, elapsed timecode, star favorites, share via Android Intent, and deletion.

### 9. Recording Limits & Auto-Stop
- **Duration Limit**: Auto-stops and finalizes recordings after a configured number of minutes.
- **File Size Limit**: Safeguards storage by auto-stopping when reaching MB thresholds (e.g., 250MB, 500MB, 1GB).
- **Warning Notifications**: Alerts the user seconds before limit enforcement.

### 10. Destination Folder & Storage Auto-Cleanup
- **Storage Access Framework (SAF)**: Choose custom folders on device storage (`Movies`, `DCIM`, `Downloads`, or external SD).
- **Monthly Organization**: Automatically categorizes recordings into `YYYY-MM` date subfolders.
- **Auto-Cleanup Rules**: Automatically purges recordings older than configurable retention days (7, 14, 30, 90 days), with protection for starred favorite clips.

### 11. Recording Statistics & Storage Dashboard
- **Aggregate Analytics**: Computes and displays total count of recorded clips, total cumulative recording duration (`hours, minutes, seconds`), and total disk storage occupied (`MB` / `GB`).
- **Averages & Projections**: Calculates average duration per capture session and average file size.
- **Resolution Distribution Breakdown**: Visual progress bars showing percentage and count distribution across capture resolutions (`1080p Full HD`, `720p HD`, `480p SD`, etc.).
- **Record Highlights**: Automatically surfaces the longest single capture and the largest file saved to disk.
- **Directory & Clean Links**: Shows the active destination storage path and provides direct one-tap links to the auto-cleanup and gallery views.

### 12. Audio Source Selection (Mic, Internal Audio, Mic + Internal Audio)
- **Selectable Audio Inputs**:
  - **Microphone (Mic)**: Records speaker voice, external narration, and acoustic surroundings.
  - **Internal Audio**: Clean digital capture of Android system audio, mobile gameplay sounds, and app playback (Android 10+).
  - **Mic + Internal Audio**: Dual-stream recording capturing voice commentary while simultaneously recording internal game and device audio.
  - **Mute / Silent**: Video-only recording without audio tracks.
- **Dual Volume Mixers**: Independent gain sliders (0% to 150%) for microphone commentary and internal system sound when the dual mix mode is selected.
- **Studio Audio Formatting**: Toggle between `44.1 kHz Standard` and `48.0 kHz Studio` sample rates, and `Stereo (2 Channels)` or `Mono (1 Channel)` audio layouts.
- **Real-Time Status Indicator**: Current audio source is displayed on the main dashboard QuickChip and integrated into foreground service notification specs.

---

## 🛠️ Architecture & Tech Stack

- **Language**: Kotlin 1.9+
- **UI Framework**: Jetpack Compose with Material Design 3
- **Architecture**: MVVM with unidirectional data flow (`StateFlow` & `SharedFlow`)
- **Background Tasks**: Android Foreground Service (`ScreenRecordService`)
- **System Integrations**: Android `MediaProjection`, `NotificationManager`, `AudioManager`, and Storage Access Framework (`FileProvider`)

---

## 📦 Building the Project

Build the debug APK using Gradle:

```bash
./gradlew assembleDebug
```

The compiled APK will be generated at:
```
app/build/outputs/apk/debug/app-debug.apk
```
