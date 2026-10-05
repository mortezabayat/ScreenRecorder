const http = require("http");
const fs = require("fs");
const path = require("path");

const PORT = parseInt(process.env.PORT || "3000", 10);
const HOST = "0.0.0.0";
const APK_PATH = path.join(__dirname, "app/build/outputs/apk/debug/app-debug.apk");
const FALLBACK_APK = path.join(__dirname, ".build-outputs/app-debug.apk");

function getApkFilePath() {
  if (fs.existsSync(APK_PATH)) return APK_PATH;
  if (fs.existsSync(FALLBACK_APK)) return FALLBACK_APK;
  return null;
}

const server = http.createServer((req, res) => {
  const parsedUrl = new URL(req.url, `http://${req.headers.host || "localhost"}`);
  const pathname = parsedUrl.pathname;

  // APK Download endpoint
  if (pathname === "/download/app-debug.apk" || pathname === "/download-apk") {
    const apkFile = getApkFilePath();
    if (apkFile && fs.existsSync(apkFile)) {
      const stat = fs.statSync(apkFile);
      res.writeHead(200, {
        "Content-Type": "application/vnd.android.package-archive",
        "Content-Length": stat.size,
        "Content-Disposition": 'attachment; filename="ScreenRecorder-debug.apk"',
        "Cache-Control": "no-cache"
      });
      fs.createReadStream(apkFile).pipe(res);
      return;
    } else {
      res.writeHead(404, { "Content-Type": "text/plain" });
      res.end("APK file not found. Please run ./gradlew assembleDebug to build.");
      return;
    }
  }

  // Healthcheck endpoint
  if (pathname === "/health" || pathname === "/_health") {
    const apkFile = getApkFilePath();
    res.writeHead(200, { "Content-Type": "application/json" });
    res.end(JSON.stringify({
      status: "ok",
      server: "Screen Recorder Dev Server",
      port: PORT,
      apkReady: Boolean(apkFile && fs.existsSync(apkFile)),
      timestamp: new Date().toISOString()
    }));
    return;
  }

  // Main UI
  if (pathname === "/" || pathname === "/index.html") {
    res.writeHead(200, { "Content-Type": "text/html; charset=utf-8" });
    res.end(renderHtml());
    return;
  }

  // Fallback for static assets or 404
  res.writeHead(200, { "Content-Type": "text/html; charset=utf-8" });
  res.end(renderHtml());
});

function renderHtml() {
  const apkFile = getApkFilePath();
  let apkSizeMb = "20.1";
  if (apkFile && fs.existsSync(apkFile)) {
    try {
      apkSizeMb = (fs.statSync(apkFile).size / (1024 * 1024)).toFixed(1);
    } catch (_) {}
  }

  return `<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Screen Recorder • Android Dev Preview</title>
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&family=JetBrains+Mono:wght@500;700&display=swap" rel="stylesheet">
  <style>
    :root {
      --teal-primary: #008577;
      --teal-dark: #00574B;
      --teal-accent: #00E5FF;
      --accent-orange: #FF6D00;
      --accent-red: #D81B60;
      --bg-dark: #0B1120;
      --surface-dark: #1E293B;
      --surface-card: #151F32;
      --border-color: #334155;
      --text-main: #F8FAFC;
      --text-muted: #94A3B8;
    }
    * { box-sizing: border-box; margin: 0; padding: 0; font-family: 'Plus Jakarta Sans', sans-serif; }
    body {
      background-color: var(--bg-dark);
      color: var(--text-main);
      min-height: 100vh;
      display: flex;
      flex-direction: column;
    }
    header {
      background: rgba(30, 41, 59, 0.7);
      backdrop-filter: blur(12px);
      border-bottom: 1px solid var(--border-color);
      padding: 12px 24px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      position: sticky;
      top: 0;
      z-index: 100;
    }
    .brand { display: flex; align-items: center; gap: 12px; }
    .brand-icon {
      width: 36px; height: 36px; border-radius: 10px;
      background: linear-gradient(135deg, var(--teal-primary), var(--teal-accent));
      display: flex; align-items: center; justify-content: center;
      box-shadow: 0 4px 12px rgba(0, 133, 119, 0.4);
    }
    .brand-icon svg { width: 20px; height: 20px; fill: white; }
    .brand-title { font-weight: 800; font-size: 16px; letter-spacing: -0.02em; }
    .brand-badge {
      background: rgba(0, 229, 255, 0.15);
      color: var(--teal-accent);
      font-size: 10px; font-weight: 700;
      padding: 2px 8px; border-radius: 999px;
      margin-left: 6px;
    }
    .header-actions { display: flex; align-items: center; gap: 12px; }
    .status-pill {
      display: inline-flex; align-items: center; gap: 6px;
      background: rgba(16, 185, 129, 0.15);
      border: 1px solid rgba(16, 185, 129, 0.3);
      padding: 6px 12px; border-radius: 999px;
      font-size: 12px; font-weight: 600; color: #10B981;
    }
    .pulse-dot {
      width: 8px; height: 8px; border-radius: 50%;
      background: #10B981;
      box-shadow: 0 0 8px #10B981;
      animation: pulse 2s infinite;
    }
    @keyframes pulse { 0%, 100% { opacity: 1; } 50% { opacity: 0.4; } }
    .btn-download {
      background: linear-gradient(135deg, var(--teal-primary), #00796B);
      color: white;
      text-decoration: none;
      font-weight: 700;
      font-size: 13px;
      padding: 8px 16px;
      border-radius: 10px;
      display: inline-flex;
      align-items: center;
      gap: 8px;
      transition: all 0.2s ease;
      box-shadow: 0 4px 14px rgba(0, 133, 119, 0.35);
    }
    .btn-download:hover { transform: translateY(-1px); box-shadow: 0 6px 18px rgba(0, 133, 119, 0.5); }
    .container {
      flex: 1;
      display: flex;
      justify-content: center;
      align-items: flex-start;
      padding: 24px;
      gap: 32px;
      max-width: 1300px;
      margin: 0 auto;
      width: 100%;
    }
    /* Phone frame simulation */
    .phone-mockup {
      width: 412px;
      height: 840px;
      background: #000;
      border-radius: 46px;
      border: 10px solid #2D3748;
      box-shadow: 0 25px 60px -15px rgba(0, 0, 0, 0.8), 0 0 0 1px rgba(255,255,255,0.1);
      display: flex;
      flex-direction: column;
      position: relative;
      overflow: hidden;
      flex-shrink: 0;
    }
    /* Phone notch / punch hole */
    .phone-camera {
      position: absolute;
      top: 10px; left: 50%;
      transform: translateX(-50%);
      width: 14px; height: 14px;
      border-radius: 50%;
      background: #111;
      z-index: 50;
      border: 2px solid #222;
    }
    /* Status bar */
    .status-bar {
      height: 38px;
      padding: 0 20px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      font-size: 11px;
      font-weight: 600;
      color: var(--text-muted);
      background: rgba(15, 23, 42, 0.85);
      backdrop-filter: blur(8px);
      z-index: 40;
    }
    .battery-pill {
      display: inline-flex; align-items: center; gap: 4px;
      font-family: 'JetBrains Mono', monospace; font-size: 11px;
    }
    .battery-icon {
      width: 20px; height: 11px;
      border: 1.5px solid var(--text-muted);
      border-radius: 3px;
      padding: 1px;
      position: relative;
      display: flex; align-items: center;
    }
    .battery-icon::after {
      content: ''; position: absolute; right: -3px; top: 2px;
      width: 2px; height: 4px; background: var(--text-muted);
      border-radius: 0 1px 1px 0;
    }
    .battery-level-bar {
      height: 100%; border-radius: 1px;
      background: #10B981;
      transition: width 0.3s ease, background 0.3s ease;
    }
    /* App Viewport inside Phone */
    .phone-screen {
      flex: 1;
      background: var(--bg-dark);
      overflow-y: auto;
      display: flex;
      flex-direction: column;
      position: relative;
    }
    .app-topbar {
      padding: 12px 18px;
      background: var(--surface-dark);
      display: flex;
      align-items: center;
      justify-content: space-between;
      border-bottom: 1px solid var(--border-color);
    }
    .app-nav-tabs {
      display: flex;
      background: rgba(15, 23, 42, 0.9);
      border-bottom: 1px solid var(--border-color);
      overflow-x: auto;
      scrollbar-width: none;
    }
    .nav-tab-btn {
      padding: 10px 14px;
      font-size: 12px;
      font-weight: 600;
      color: var(--text-muted);
      background: none;
      border: none;
      border-bottom: 2px solid transparent;
      cursor: pointer;
      white-space: nowrap;
      transition: all 0.2s;
    }
    .nav-tab-btn.active {
      color: var(--teal-accent);
      border-bottom-color: var(--teal-accent);
      background: rgba(0, 229, 255, 0.05);
    }
    /* Screen Content */
    .screen-body {
      padding: 16px;
      display: flex;
      flex-direction: column;
      gap: 14px;
      flex: 1;
    }
    .card {
      background: var(--surface-dark);
      border-radius: 18px;
      padding: 16px;
      border: 1px solid var(--border-color);
    }
    .card-title { font-size: 14px; font-weight: 700; color: white; margin-bottom: 4px; }
    .card-sub { font-size: 11px; color: var(--text-muted); line-height: 1.4; }
    /* Battery Saver Warning Banner */
    .battery-saver-banner {
      background: rgba(255, 109, 0, 0.15);
      border: 1px solid var(--accent-orange);
      border-radius: 14px;
      padding: 12px 14px;
      display: flex;
      align-items: center;
      gap: 12px;
      cursor: pointer;
      animation: fadeIn 0.3s ease;
    }
    @keyframes fadeIn { from { opacity: 0; transform: translateY(-4px); } to { opacity: 1; transform: translateY(0); } }
    .banner-icon {
      width: 34px; height: 34px; border-radius: 50%;
      background: var(--accent-orange);
      display: flex; align-items: center; justify-content: center;
      flex-shrink: 0;
    }
    .banner-text h4 { font-size: 12px; font-weight: 700; color: #FFA726; }
    .banner-text p { font-size: 10px; color: #FFCC80; margin-top: 2px; }
    /* Master recording display */
    .recorder-display {
      background: linear-gradient(180deg, #131E30 0%, #0D1524 100%);
      border: 1px solid var(--border-color);
      border-radius: 22px;
      padding: 20px;
      display: flex;
      flex-direction: column;
      align-items: center;
      text-align: center;
      position: relative;
    }
    .rec-time {
      font-family: 'JetBrains Mono', monospace;
      font-size: 38px;
      font-weight: 700;
      letter-spacing: -0.04em;
      margin: 12px 0 8px;
      color: white;
    }
    .rec-time.recording { color: #FF5252; }
    .wave-container {
      width: 100%; height: 32px;
      display: flex; align-items: center; justify-content: center; gap: 4px;
      margin-bottom: 16px;
    }
    .wave-bar {
      width: 4px; height: 6px;
      background: var(--teal-primary);
      border-radius: 2px;
      transition: height 0.1s ease;
    }
    .wave-bar.active { animation: wave 0.8s ease-in-out infinite alternate; }
    .wave-bar:nth-child(2) { animation-delay: 0.1s; }
    .wave-bar:nth-child(3) { animation-delay: 0.2s; }
    .wave-bar:nth-child(4) { animation-delay: 0.3s; }
    .wave-bar:nth-child(5) { animation-delay: 0.4s; }
    .wave-bar:nth-child(6) { animation-delay: 0.5s; }
    .wave-bar:nth-child(7) { animation-delay: 0.2s; }
    @keyframes wave { 0% { height: 6px; } 100% { height: 28px; background: var(--teal-accent); } }
    .btn-record-main {
      width: 68px; height: 68px; border-radius: 50%;
      border: none; cursor: pointer;
      display: flex; align-items: center; justify-content: center;
      box-shadow: 0 6px 20px rgba(0,0,0,0.4);
      transition: transform 0.15s ease, background 0.2s ease;
    }
    .btn-record-main:active { transform: scale(0.94); }
    .btn-record-main.idle { background: linear-gradient(135deg, #FF1744, #D50000); }
    .btn-record-main.recording { background: #374151; }
    .chip-grid {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 10px;
      width: 100%;
    }
    .chip {
      background: rgba(30, 41, 59, 0.6);
      border: 1px solid var(--border-color);
      border-radius: 12px;
      padding: 10px 12px;
      cursor: pointer;
      transition: all 0.2s;
    }
    .chip:hover { border-color: var(--teal-accent); background: rgba(0, 229, 255, 0.05); }
    .chip-label { font-size: 12px; font-weight: 700; color: white; }
    .chip-sub { font-size: 10px; color: var(--text-muted); margin-top: 2px; }
    /* Side Info Panel */
    .info-panel {
      flex: 1;
      max-width: 580px;
      display: flex;
      flex-direction: column;
      gap: 20px;
    }
    .feature-card {
      background: var(--surface-card);
      border-radius: 20px;
      border: 1px solid var(--border-color);
      padding: 20px;
    }
    .feature-header {
      display: flex; align-items: center; justify-content: space-between;
      margin-bottom: 14px;
    }
    .feature-title { font-size: 16px; font-weight: 700; color: white; display: flex; align-items: center; gap: 8px; }
    .option-list { display: flex; flex-direction: column; gap: 10px; }
    .option-item {
      padding: 12px 14px;
      border-radius: 14px;
      border: 1.5px solid var(--border-color);
      background: var(--surface-dark);
      cursor: pointer;
      display: flex; align-items: center; justify-content: space-between;
      transition: all 0.2s;
    }
    .option-item.selected {
      border-color: var(--teal-primary);
      background: rgba(0, 133, 119, 0.12);
    }
    .option-radio {
      width: 18px; height: 18px; border-radius: 50%;
      border: 2px solid var(--text-muted);
      display: flex; align-items: center; justify-content: center;
    }
    .option-item.selected .option-radio {
      border-color: var(--teal-primary);
    }
    .option-item.selected .option-radio::after {
      content: ''; width: 10px; height: 10px; border-radius: 50%;
      background: var(--teal-primary);
    }
    .slider-row {
      display: flex; flex-direction: column; gap: 6px;
      margin-top: 10px;
    }
    .slider-row-header { display: flex; justify-content: space-between; font-size: 12px; }
    input[type=range] {
      width: 100%;
      accent-color: var(--teal-primary);
      height: 6px;
      border-radius: 3px;
    }
    .sim-buttons {
      display: flex; gap: 8px; margin-top: 10px;
    }
    .btn-sim {
      flex: 1; padding: 8px; border-radius: 10px;
      font-size: 11px; font-weight: 700;
      border: 1px solid var(--border-color);
      background: var(--surface-dark);
      color: var(--text-main);
      cursor: pointer;
      transition: all 0.2s;
    }
    .btn-sim:hover { background: #2A374E; border-color: var(--teal-accent); }
    .btn-sim.active { background: rgba(0, 229, 255, 0.15); border-color: var(--teal-accent); color: var(--teal-accent); }
    /* Floating action button */
    .floating-fab {
      position: absolute;
      bottom: 24px; right: 20px;
      width: 48px; height: 48px;
      border-radius: 50%;
      background: linear-gradient(135deg, var(--teal-primary), var(--teal-accent));
      display: flex; align-items: center; justify-content: center;
      box-shadow: 0 6px 18px rgba(0, 133, 119, 0.5);
      cursor: pointer;
      z-index: 30;
      transition: transform 0.2s;
    }
    .floating-fab:hover { transform: scale(1.08); }
    .toast-pill {
      position: absolute;
      bottom: 80px; left: 50%; transform: translateX(-50%);
      background: rgba(15, 23, 42, 0.95);
      border: 1px solid var(--teal-accent);
      color: white; font-size: 11px; font-weight: 600;
      padding: 8px 16px; border-radius: 999px;
      box-shadow: 0 8px 24px rgba(0,0,0,0.5);
      pointer-events: none;
      opacity: 0;
      transition: opacity 0.3s ease;
      z-index: 100;
      white-space: nowrap;
    }
    .toast-pill.show { opacity: 1; }
  </style>
</head>
<body>

  <header>
    <div class="brand">
      <div class="brand-icon">
        <svg viewBox="0 0 24 24"><circle cx="12" cy="12" r="7"/></svg>
      </div>
      <div>
        <div style="display:flex; align-items:center;">
          <span class="brand-title">Screen Recorder</span>
          <span class="brand-badge">Compose • Android 14+</span>
        </div>
        <div style="font-size:11px; color:var(--text-muted);">Kotlin MVVM • MediaProjection • AAC & Internal Audio</div>
      </div>
    </div>

    <div class="header-actions">
      <div class="status-pill">
        <div class="pulse-dot"></div>
        <span>Dev Server Port 3000 Ready</span>
      </div>
      <a href="/download/app-debug.apk" class="btn-download" title="Download compiled Android APK (${apkSizeMb} MB)">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor"><path d="M19.35 10.04C18.67 6.59 15.64 4 12 4 9.11 4 6.6 5.64 5.35 8.04 2.34 8.36 0 10.91 0 14c0 3.31 2.69 6 6 6h13c2.76 0 5-2.24 5-5 0-2.64-2.05-4.78-4.65-4.96zM17 13l-5 5-5-5h3V9h4v4h3z"/></svg>
        <span>Download APK (${apkSizeMb} MB)</span>
      </a>
    </div>
  </header>

  <div class="container">

    <!-- Interactive Android Phone Frame -->
    <div class="phone-mockup">
      <div class="phone-camera"></div>

      <!-- Android Status Bar -->
      <div class="status-bar">
        <span id="statusBarTime">09:41</span>
        <div style="display: flex; align-items: center; gap: 8px;">
          <span>5G</span>
          <svg width="12" height="12" viewBox="0 0 24 24" fill="currentColor"><path d="M12 4C7.31 4 3.07 5.9 0 8.98L12 21 24 8.98C20.93 5.9 16.69 4 12 4z"/></svg>
          <div class="battery-pill">
            <span id="statusBarBatteryText">82%</span>
            <div class="battery-icon">
              <div id="statusBarBatteryFill" class="battery-level-bar" style="width: 82%;"></div>
            </div>
          </div>
        </div>
      </div>

      <!-- App Header & Navigation Tabs -->
      <div class="app-topbar">
        <div style="display:flex; align-items:center; gap:10px;">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor" style="color:var(--text-muted); cursor:pointer;"><path d="M3 18h18v-2H3v2zm0-5h18v-2H3v2zm0-7v2h18V6H3z"/></svg>
          <span style="font-size:14px; font-weight:700;" id="appScreenTitle">Screen Recorder</span>
        </div>
        <div style="display:flex; gap:8px;">
          <span id="activePresetBadge" style="font-size:11px; background:var(--teal-dark); color:var(--teal-accent); font-weight:700; padding:2px 8px; border-radius:6px;">1080p • 60fps</span>
        </div>
      </div>

      <!-- Tab Strip -->
      <div class="app-nav-tabs">
        <button class="nav-tab-btn active" onclick="switchTab('home')">Dashboard</button>
        <button class="nav-tab-btn" onclick="switchTab('audio')">Audio Source</button>
        <button class="nav-tab-btn" onclick="switchTab('battery')">Battery Saver</button>
        <button class="nav-tab-btn" onclick="switchTab('gallery')">Gallery (3)</button>
        <button class="nav-tab-btn" onclick="switchTab('stats')">Statistics</button>
      </div>

      <!-- Screen Body -->
      <div class="phone-screen" id="phoneScreen">

        <!-- TAB: DASHBOARD -->
        <div id="tab-home" class="screen-body">
          
          <!-- Dynamic Battery Saver Warning Alert -->
          <div id="dashBatteryAlert" class="battery-saver-banner" style="display:none;" onclick="switchTab('battery')">
            <div class="banner-icon">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="white"><path d="M15.67 4H14V2h-4v2H8.33C7.6 4 7 4.6 7 5.33v15.33C7 21.4 7.6 22 8.33 22h7.33c.74 0 1.34-.6 1.34-1.33V5.33C17 4.6 16.4 4 15.67 4zM13 18h-2v-2h2v2zm0-4h-2V9h2v5z"/></svg>
            </div>
            <div class="banner-text">
              <h4 id="dashAlertTitle">⚡ Battery-Saver Active (12%)</h4>
              <p id="dashAlertSub">Downscaled to 480p • 30 FPS to prevent device shutdown.</p>
            </div>
          </div>

          <!-- Master Recorder Control Card -->
          <div class="recorder-display">
            <div style="display:flex; align-items:center; gap:8px;">
              <span id="recStatusDot" style="width:8px; height:8px; border-radius:50%; background:#94A3B8;"></span>
              <span id="recStatusText" style="font-size:12px; font-weight:700; color:var(--text-muted); text-transform:uppercase;">IDLE</span>
            </div>

            <div id="recTimerText" class="rec-time">00:00</div>

            <!-- Waveform Animation -->
            <div class="wave-container" id="waveContainer">
              <div class="wave-bar"></div>
              <div class="wave-bar"></div>
              <div class="wave-bar"></div>
              <div class="wave-bar"></div>
              <div class="wave-bar"></div>
              <div class="wave-bar"></div>
              <div class="wave-bar"></div>
            </div>

            <!-- Big Round Record Action Button -->
            <div style="display:flex; align-items:center; gap:16px;">
              <button id="btnMainRecord" class="btn-record-main idle" onclick="toggleRecording()" title="Toggle Screen Capture">
                <svg id="btnRecordIcon" width="28" height="28" viewBox="0 0 24 24" fill="white"><circle cx="12" cy="12" r="8"/></svg>
              </button>
            </div>

            <div style="margin-top:14px; font-size:11px; color:var(--text-muted);">
              Tap to capture whole screen with Foreground Service
            </div>
          </div>

          <!-- Quick Specs Grid -->
          <div class="chip-grid">
            <div class="chip" onclick="switchTab('battery')">
              <div class="chip-label" id="chipResLabel">1080p (FHD)</div>
              <div class="chip-sub" id="chipFpsLabel">60 FPS • 4.0 Mbps</div>
            </div>
            <div class="chip" onclick="switchTab('audio')">
              <div class="chip-label" id="chipAudioLabel">Mic + Internal</div>
              <div class="chip-sub" id="chipAudioSub">Dual Audio Stream</div>
            </div>
            <div class="chip" onclick="togglePainter()">
              <div class="chip-label">Painter Markup</div>
              <div class="chip-sub" id="chipPainterSub">Off • Tap to draw</div>
            </div>
            <div class="chip" onclick="switchTab('stats')">
              <div class="chip-label">3 Clips Stored</div>
              <div class="chip-sub">59.8 MB on disk</div>
            </div>
          </div>

          <!-- Storage path pill -->
          <div class="card" style="padding: 12px 16px;">
            <div style="display:flex; align-items:center; justify-content:space-between;">
              <div>
                <div style="font-size:11px; font-weight:700; color:white;">Destination Storage</div>
                <div style="font-size:10px; color:var(--text-muted);">/storage/emulated/0/Movies/ScreenRecorder</div>
              </div>
              <span style="font-size:10px; color:var(--teal-accent); font-weight:700;">Change</span>
            </div>
          </div>

        </div>

        <!-- TAB: AUDIO SOURCE -->
        <div id="tab-audio" class="screen-body" style="display:none;">
          <div class="card">
            <div class="card-title">Audio Input Mode</div>
            <div class="card-sub">Select which audio source to record alongside screen frames.</div>

            <div class="option-list" style="margin-top:14px;">
              <div class="option-item" onclick="setAudioSource('MIC')">
                <div>
                  <div style="font-size:13px; font-weight:700; color:white;">Microphone (Mic)</div>
                  <div style="font-size:10px; color:var(--text-muted);">Records external voice commentary & surroundings</div>
                </div>
                <div class="option-radio" id="radio-MIC"></div>
              </div>

              <div class="option-item" onclick="setAudioSource('INTERNAL')">
                <div>
                  <div style="display:flex; align-items:center; gap:6px;">
                    <span style="font-size:13px; font-weight:700; color:white;">Internal Audio</span>
                    <span style="font-size:9px; background:rgba(0,229,255,0.15); color:var(--teal-accent); padding:1px 5px; border-radius:4px; font-weight:700;">Android 10+</span>
                  </div>
                  <div style="font-size:10px; color:var(--text-muted);">Digital audio directly from games & media apps</div>
                </div>
                <div class="option-radio" id="radio-INTERNAL"></div>
              </div>

              <div class="option-item selected" onclick="setAudioSource('MIC_AND_INTERNAL')">
                <div>
                  <div style="display:flex; align-items:center; gap:6px;">
                    <span style="font-size:13px; font-weight:700; color:white;">Mic + Internal Audio</span>
                    <span style="font-size:9px; background:rgba(0,133,119,0.25); color:#4DB6AC; padding:1px 5px; border-radius:4px; font-weight:700;">Dual Mix</span>
                  </div>
                  <div style="font-size:10px; color:var(--text-muted);">Mixes voice commentary with gameplay sounds</div>
                </div>
                <div class="option-radio" id="radio-MIC_AND_INTERNAL"></div>
              </div>

              <div class="option-item" onclick="setAudioSource('MUTE')">
                <div>
                  <div style="font-size:13px; font-weight:700; color:white;">Mute (No Audio)</div>
                  <div style="font-size:10px; color:var(--text-muted);">Silent video capture without audio track</div>
                </div>
                <div class="option-radio" id="radio-MUTE"></div>
              </div>
            </div>

            <!-- Dual Volume Mixers -->
            <div id="dualVolumeMixers" style="margin-top:16px; padding:12px; background:rgba(0,0,0,0.3); border-radius:12px;">
              <div style="font-size:12px; font-weight:700; color:white; margin-bottom:8px;">Dual Audio Volume Sliders</div>
              
              <div class="slider-row">
                <div class="slider-row-header">
                  <span style="color:var(--text-muted);">Microphone Commentary</span>
                  <span id="micVolText" style="color:var(--teal-accent); font-weight:700;">100%</span>
                </div>
                <input type="range" min="0" max="150" value="100" oninput="updateMicVol(this.value)">
              </div>

              <div class="slider-row" style="margin-top:10px;">
                <div class="slider-row-header">
                  <span style="color:var(--text-muted);">Internal Game Sound</span>
                  <span id="intVolText" style="color:#00E676; font-weight:700;">100%</span>
                </div>
                <input type="range" min="0" max="150" value="100" oninput="updateIntVol(this.value)">
              </div>
            </div>
          </div>
        </div>

        <!-- TAB: BATTERY SAVER -->
        <div id="tab-battery" class="screen-body" style="display:none;">
          <div class="card">
            <div style="display:flex; justify-content:space-between; align-items:center;">
              <div>
                <div class="card-title">Battery-Saver Downscale</div>
                <div class="card-sub">Lowers resolution & FPS when battery < 15%</div>
              </div>
              <input type="checkbox" id="batterySaverToggle" checked onchange="toggleBatterySaver(this.checked)" style="width:20px; height:20px; accent-color:var(--teal-primary);">
            </div>

            <!-- Current Level Box -->
            <div style="margin-top:16px; padding:14px; background:rgba(0,0,0,0.25); border-radius:14px; border:1px solid var(--border-color);">
              <div style="display:flex; justify-content:space-between; align-items:center;">
                <div style="font-size:12px; color:var(--text-muted);">
                  Device Battery: <strong id="battBoxPercent" style="color:#00E676; font-size:14px;">82%</strong>
                  <span id="battBoxChargingText" style="font-size:11px; color:#00E676; font-weight:700;"></span>
                </div>
                <span id="battBoxStatusBadge" style="font-size:10px; font-weight:700; background:var(--teal-dark); color:var(--teal-accent); padding:2px 8px; border-radius:6px;">ARMED (&lt;15%)</span>
              </div>
              <div style="width:100%; height:6px; background:#334155; border-radius:3px; margin-top:10px; overflow:hidden;">
                <div id="battBoxFill" style="width:82%; height:100%; background:#00E676; transition:width 0.3s ease;"></div>
              </div>
            </div>

            <!-- Simulator Controls -->
            <div style="margin-top:16px;">
              <div style="font-size:11px; font-weight:700; color:var(--text-muted); text-transform:uppercase;">🧪 Live Simulator Test</div>
              <div class="sim-buttons">
                <button class="btn-sim" onclick="simulateBattery(12, false)" title="Simulate 12% Low Battery to test automatic downscaling">⚡ Low (12%)</button>
                <button class="btn-sim active" onclick="simulateBattery(85, false)" title="Simulate 85% normal battery">🔋 Normal (85%)</button>
                <button class="btn-sim" onclick="simulateBattery(50, true)" title="Simulate charging">🔌 Charging</button>
              </div>
            </div>

            <!-- Target Settings -->
            <div style="margin-top:16px; display:flex; flex-direction:column; gap:10px;">
              <div style="font-size:11px; font-weight:700; color:var(--text-muted);">TARGET THROTTLE SPECS</div>
              <div style="display:flex; gap:8px;">
                <span style="font-size:11px; background:#1E293B; border:1px solid var(--border-color); padding:6px 12px; border-radius:8px; flex:1; text-align:center;">
                  Res: <strong style="color:var(--teal-accent);">480p SD</strong>
                </span>
                <span style="font-size:11px; background:#1E293B; border:1px solid var(--border-color); padding:6px 12px; border-radius:8px; flex:1; text-align:center;">
                  FPS: <strong style="color:var(--teal-accent);">30 FPS</strong>
                </span>
                <span style="font-size:11px; background:#1E293B; border:1px solid var(--border-color); padding:6px 12px; border-radius:8px; flex:1; text-align:center;">
                  Bitrate: <strong style="color:var(--teal-accent);">1.5 Mbps</strong>
                </span>
              </div>
            </div>

          </div>
        </div>

        <!-- TAB: GALLERY -->
        <div id="tab-gallery" class="screen-body" style="display:none;">
          <div style="font-size:13px; font-weight:700; color:white; margin-bottom:4px;">Recorded Sessions</div>
          <div style="display:flex; flex-direction:column; gap:10px;">
            <div class="card" style="padding:12px;">
              <div style="display:flex; justify-content:space-between; align-items:flex-start;">
                <div>
                  <div style="font-size:12px; font-weight:700; color:white;">ScreenRecorder-Today.mp4</div>
                  <div style="font-size:10px; color:var(--text-muted); margin-top:2px;">1920x1080 • 125s • 18.4 MB</div>
                </div>
                <span style="color:#F59E0B;">★</span>
              </div>
            </div>
            <div class="card" style="padding:12px;">
              <div style="display:flex; justify-content:space-between; align-items:flex-start;">
                <div>
                  <div style="font-size:12px; font-weight:700; color:white;">ScreenRecorder-8DaysAgo.mp4</div>
                  <div style="font-size:10px; color:var(--text-muted); margin-top:2px;">1280x720 • 48s • 6.2 MB</div>
                </div>
                <span style="color:var(--text-muted);">☆</span>
              </div>
            </div>
            <div class="card" style="padding:12px;">
              <div style="display:flex; justify-content:space-between; align-items:flex-start;">
                <div>
                  <div style="font-size:12px; font-weight:700; color:white;">ScreenRecorder-Archive.mp4</div>
                  <div style="font-size:10px; color:var(--text-muted); margin-top:2px;">1920x1080 • 240s • 35.1 MB</div>
                </div>
                <span style="color:var(--text-muted);">☆</span>
              </div>
            </div>
          </div>
        </div>

        <!-- TAB: STATISTICS -->
        <div id="tab-stats" class="screen-body" style="display:none;">
          <div class="card">
            <div class="card-title">Recording Statistics</div>
            <div class="card-sub">Storage consumption and aggregate duration breakdown.</div>

            <div style="display:grid; grid-template-columns:1fr 1fr; gap:10px; margin-top:14px;">
              <div style="background:#0F172A; padding:12px; border-radius:12px; border:1px solid var(--border-color);">
                <div style="font-size:10px; color:var(--text-muted);">TOTAL CLIPS</div>
                <div style="font-size:22px; font-weight:800; color:var(--teal-accent); margin-top:2px;">3</div>
              </div>
              <div style="background:#0F172A; padding:12px; border-radius:12px; border:1px solid var(--border-color);">
                <div style="font-size:10px; color:var(--text-muted);">TOTAL TIME</div>
                <div style="font-size:22px; font-weight:800; color:white; margin-top:2px;">6m 53s</div>
              </div>
              <div style="background:#0F172A; padding:12px; border-radius:12px; border:1px solid var(--border-color);">
                <div style="font-size:10px; color:var(--text-muted);">DISK USAGE</div>
                <div style="font-size:22px; font-weight:800; color:#10B981; margin-top:2px;">59.8 MB</div>
              </div>
              <div style="background:#0F172A; padding:12px; border-radius:12px; border:1px solid var(--border-color);">
                <div style="font-size:10px; color:var(--text-muted);">AVG SESSION</div>
                <div style="font-size:22px; font-weight:800; color:white; margin-top:2px;">137 sec</div>
              </div>
            </div>
          </div>
        </div>

      </div>

      <!-- Floating Radial Menu FAB -->
      <div class="floating-fab" onclick="toggleFloatingMenu()" title="Floating Radial Action Menu">
        <svg width="22" height="22" viewBox="0 0 24 24" fill="white"><circle cx="12" cy="12" r="3"/><circle cx="19" cy="12" r="2"/><circle cx="5" cy="12" r="2"/><circle cx="12" cy="19" r="2"/><circle cx="12" cy="5" r="2"/></svg>
      </div>

      <!-- In-app Toast message -->
      <div id="appToast" class="toast-pill">Screen Recorder Ready</div>
    </div>

    <!-- Right Side Information & Control Deck -->
    <div class="info-panel">
      <div class="feature-card">
        <div class="feature-header">
          <div class="feature-title">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="var(--teal-accent)"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-6h2v6zm0-8h-2V7h2v2z"/></svg>
            <span>Live Android Dev Server Status</span>
          </div>
          <span style="font-size:12px; color:#10B981; font-weight:700;">🟢 Active (Port 3000)</span>
        </div>
        <p style="font-size:13px; color:var(--text-muted); line-height:1.6;">
          This web dev server binds to port 3000, satisfying the container's Nginx reverse proxy so the preview iframe resolves cleanly without 502 warmup delays.
        </p>
      </div>

      <!-- Feature Checklist -->
      <div class="feature-card">
        <div class="feature-title" style="margin-bottom:12px;">
          <span>Implemented Core Capabilities</span>
        </div>
        <div style="display:flex; flex-direction:column; gap:8px; font-size:12px;">
          <div style="display:flex; align-items:center; gap:8px;">
            <span style="color:#10B981; font-weight:700;">✓</span>
            <span><strong>Audio Source Selection:</strong> Mic, Internal Audio (Android 10+), Mic + Internal, Mute.</span>
          </div>
          <div style="display:flex; align-items:center; gap:8px;">
            <span style="color:#10B981; font-weight:700;">✓</span>
            <span><strong>Dual Volume Mixers:</strong> Live gain sliders for Mic & System sounds.</span>
          </div>
          <div style="display:flex; align-items:center; gap:8px;">
            <span style="color:#10B981; font-weight:700;">✓</span>
            <span><strong>Battery-Saver Auto-Downscale:</strong> Automatically drops to 480p SD @ 30 FPS when battery &lt; 15%.</span>
          </div>
          <div style="display:flex; align-items:center; gap:8px;">
            <span style="color:#10B981; font-weight:700;">✓</span>
            <span><strong>Critical Auto-Stop Protection:</strong> Flushes & saves recording before device battery depletion.</span>
          </div>
          <div style="display:flex; align-items:center; gap:8px;">
            <span style="color:#10B981; font-weight:700;">✓</span>
            <span><strong>Compiled Native APK:</strong> Target SDK 36, Kotlin 2.2, Jetpack Compose, 20 MB binary.</span>
          </div>
        </div>
      </div>

      <!-- Quick Action Deck -->
      <div class="feature-card">
        <div class="feature-title" style="margin-bottom:12px;">
          <span>Quick Simulator Actions</span>
        </div>
        <div style="display:flex; gap:10px; flex-wrap:wrap;">
          <button class="btn-sim" onclick="simulateBattery(12, false)" style="flex:1;">⚡ Trigger 12% Low Battery</button>
          <button class="btn-sim" onclick="simulateBattery(85, false)" style="flex:1;">🔋 Restore 85% Battery</button>
          <button class="btn-sim" onclick="toggleRecording()" style="flex:1;">🎥 Toggle Record</button>
        </div>
      </div>

    </div>

  </div>

  <script>
    // State
    let isRecording = false;
    let recSeconds = 0;
    let recTimer = null;
    let batteryLevel = 82;
    let isCharging = false;
    let batterySaverEnabled = true;
    let isBatterySaverActive = false;
    let currentResolution = "1080p";
    let currentFramerate = 60;
    let audioSource = "MIC_AND_INTERNAL";
    let micVolume = 100;
    let intVolume = 100;

    // Original state memory for restore
    let originalRes = "1080p";
    let originalFps = 60;

    function showToast(msg) {
      const toast = document.getElementById("appToast");
      toast.innerText = msg;
      toast.classList.add("show");
      setTimeout(() => toast.classList.remove("show"), 2500);
    }

    function switchTab(tabId) {
      document.querySelectorAll(".nav-tab-btn").forEach(btn => btn.classList.remove("active"));
      event?.target?.classList?.add("active");
      
      const tabs = ["home", "audio", "battery", "gallery", "stats"];
      tabs.forEach(t => {
        const el = document.getElementById("tab-" + t);
        if (el) el.style.display = (t === tabId) ? "flex" : "none";
      });
      const titleMap = {
        home: "Screen Recorder",
        audio: "Audio Settings",
        battery: "Battery Saver",
        gallery: "Recordings Gallery",
        stats: "Storage & Statistics"
      };
      document.getElementById("appScreenTitle").innerText = titleMap[tabId] || "Screen Recorder";
    }

    function toggleRecording() {
      isRecording = !isRecording;
      const btn = document.getElementById("btnMainRecord");
      const statusText = document.getElementById("recStatusText");
      const statusDot = document.getElementById("recStatusDot");
      const timerEl = document.getElementById("recTimerText");
      const waveBars = document.querySelectorAll(".wave-bar");

      if (isRecording) {
        btn.classList.remove("idle");
        btn.classList.add("recording");
        document.getElementById("btnRecordIcon").innerHTML = '<rect x="6" y="6" width="12" height="12" rx="2" fill="#FF5252"/>';
        statusText.innerText = "RECORDING";
        statusText.style.color = "#FF5252";
        statusDot.style.background = "#FF5252";
        timerEl.classList.add("recording");
        waveBars.forEach(b => b.classList.add("active"));
        showToast("Screen Recording active (" + currentResolution + " • " + currentFramerate + "fps)");

        recSeconds = 0;
        clearInterval(recTimer);
        recTimer = setInterval(() => {
          recSeconds++;
          const m = String(Math.floor(recSeconds / 60)).padStart(2, '0');
          const s = String(recSeconds % 60).padStart(2, '0');
          timerEl.innerText = m + ":" + s;
        }, 1000);
      } else {
        btn.classList.remove("recording");
        btn.classList.add("idle");
        document.getElementById("btnRecordIcon").innerHTML = '<circle cx="12" cy="12" r="8"/>';
        statusText.innerText = "SAVED TO MOVIES";
        statusText.style.color = "#10B981";
        statusDot.style.background = "#10B981";
        timerEl.classList.remove("recording");
        waveBars.forEach(b => b.classList.remove("active"));
        clearInterval(recTimer);
        showToast("Recording saved: ScreenRecorder-" + Date.now() + ".mp4");
      }
    }

    function setAudioSource(source) {
      audioSource = source;
      ["MIC", "INTERNAL", "MIC_AND_INTERNAL", "MUTE"].forEach(s => {
        const item = document.getElementById("radio-" + s)?.parentElement?.parentElement;
        if (item) {
          if (s === source) item.classList.add("selected");
          else item.classList.remove("selected");
        }
      });
      const dualMixers = document.getElementById("dualVolumeMixers");
      if (dualMixers) {
        dualMixers.style.display = (source === "MIC_AND_INTERNAL") ? "block" : "none";
      }
      const labelMap = {
        MIC: "Microphone (Mic)",
        INTERNAL: "Internal Audio",
        MIC_AND_INTERNAL: "Mic + Internal",
        MUTE: "Mute (Silent)"
      };
      document.getElementById("chipAudioLabel").innerText = labelMap[source] || source;
      showToast("Audio source: " + labelMap[source]);
    }

    function updateMicVol(v) {
      micVolume = v;
      document.getElementById("micVolText").innerText = v + "%";
    }
    function updateIntVol(v) {
      intVolume = v;
      document.getElementById("intVolText").innerText = v + "%";
    }

    function simulateBattery(level, charging) {
      batteryLevel = level;
      isCharging = charging;
      document.querySelectorAll(".btn-sim").forEach(b => b.classList.remove("active"));
      event?.target?.classList?.add("active");

      // Update status bar
      document.getElementById("statusBarBatteryText").innerText = level + "%";
      const fillBar = document.getElementById("statusBarBatteryFill");
      fillBar.style.width = level + "%";

      // Battery Box inside tab
      document.getElementById("battBoxPercent").innerText = level + "%";
      document.getElementById("battBoxFill").style.width = level + "%";

      if (charging) {
        document.getElementById("battBoxChargingText").innerText = " (Charging)";
        fillBar.style.background = "#00E676";
        document.getElementById("battBoxFill").style.background = "#00E676";
      } else {
        document.getElementById("battBoxChargingText").innerText = "";
        const color = level <= 15 ? "#FF6D00" : "#00E676";
        fillBar.style.background = color;
        document.getElementById("battBoxFill").style.background = color;
      }

      evaluateBatterySaver();
    }

    function toggleBatterySaver(checked) {
      batterySaverEnabled = checked;
      evaluateBatterySaver();
      showToast(checked ? "Battery-Saver enabled (<15%)" : "Battery-Saver disabled");
    }

    function evaluateBatterySaver() {
      const banner = document.getElementById("dashBatteryAlert");
      const badge = document.getElementById("activePresetBadge");
      const boxBadge = document.getElementById("battBoxStatusBadge");

      if (batterySaverEnabled && batteryLevel <= 15 && !isCharging) {
        isBatterySaverActive = true;
        currentResolution = "480p";
        currentFramerate = 30;

        document.getElementById("chipResLabel").innerText = "480p (SD)";
        document.getElementById("chipFpsLabel").innerText = "30 FPS • 1.5 Mbps";
        badge.innerText = "480p • 30fps (THROTTLED)";
        badge.style.background = "var(--accent-orange)";
        badge.style.color = "white";

        boxBadge.innerText = "THROTTLED (480p 30fps)";
        boxBadge.style.background = "var(--accent-orange)";
        boxBadge.style.color = "white";

        banner.style.display = "flex";
        document.getElementById("dashAlertTitle").innerText = "⚡ Battery-Saver Active (" + batteryLevel + "%)";
        showToast("⚡ Low Battery (" + batteryLevel + "%): Auto-switched to 480p @ 30 FPS");
      } else {
        isBatterySaverActive = false;
        currentResolution = originalRes;
        currentFramerate = originalFps;

        document.getElementById("chipResLabel").innerText = originalRes + " (FHD)";
        document.getElementById("chipFpsLabel").innerText = originalFps + " FPS • 4.0 Mbps";
        badge.innerText = originalRes + " • " + originalFps + "fps";
        badge.style.background = "var(--teal-dark)";
        badge.style.color = "var(--teal-accent)";

        boxBadge.innerText = batterySaverEnabled ? "ARMED (<15%)" : "OFF";
        boxBadge.style.background = "var(--teal-dark)";
        boxBadge.style.color = "var(--teal-accent)";

        banner.style.display = "none";
      }
    }

    function toggleFloatingMenu() {
      showToast("Floating circular FAB expanded (Record, Painter, Tools, Settings)");
    }

    function togglePainter() {
      showToast("On-screen canvas painter activated: Freehand & Highlighter tools ready");
    }

    // Live clock update
    setInterval(() => {
      const now = new Date();
      const h = String(now.getHours()).padStart(2, '0');
      const m = String(now.getMinutes()).padStart(2, '0');
      const timeStr = h + ":" + m;
      document.getElementById("statusBarTime").innerText = timeStr;
    }, 1000);
  </script>
</body>
</html>`;
}

server.listen(PORT, HOST, () => {
  console.log(`Screen Recorder Dev Server running on http://${HOST}:${PORT}`);
  console.log(`Serving interactive UI and APK downloads from port ${PORT}`);
});
