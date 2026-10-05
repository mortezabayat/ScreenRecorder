# Screen Recorder (React)

A cross-framework rewrite of the **ScreenRecorder** Android application (originally created by Morteza Bayat) into a modern, full-featured React web application.

## Key Features

- **Screen & Display Capture**: Capture screens, application windows, or browser tabs using the MediaStream Display Capture API with audio track mixing (display audio + microphone).
- **Floating Circular Action Menu**: A draggable, edge-snapping radial menu based on the original `FloatingCircularMenuService` and `FloatingUiHelper`. Features smooth circular expansion of action items (Record, Stop, Pause, Painter, Tools, Settings) and idle fade transitions.
- **5-Second Countdown Animation**: Animated countdown overlay with scale and alpha transitions before recording starts, directly ported from `SplashActivity` and `CountDownAnimation`.
- **Bitmap Overlay Video Processor**: Real-time canvas timestamp, logo, and resolution watermark overlay on video frames, ported from `BitmapOverlayVideoProcessor.java`.
- **On-Screen Painter Tool (`PAINTER_UI`)**: Annotation canvas for freehand drawing, highlighter mode, customizable colors and brush sizes, undo, and clear functionality while recording.
- **Recordings Gallery & Previews**: Persistent IndexedDB storage for full video blobs, preview player modal with custom playback controls, timecode display, video metadata inspector, and download options.
- **Encoder Hardware Config (Tools)**: Configurable resolution presets (480p up to 4K UHD), video bitrates (800 kbps to 25 Mbps), framerates (15 to 120 FPS), I-frame intervals, and audio channels (mono/stereo).
- **Slideshow View**: Sequential presentation reel of recorded media captures with customizable slide intervals.
- **Share & Send/Export**: Web Share API integration, clip export options with compression profiles (WebM, MP4, Audio-Only).

## Development

```bash
npm install
npm run dev
```

Listens on port 3000 (`http://localhost:3000`).

## Build

```bash
npm run build
```
