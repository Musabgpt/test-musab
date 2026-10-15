# M80 Project - Agent Commands

## Project Structure
- `server.js` — Node HTTP server (port 3000)
- `test-sandbox.mjs` — Sandbox simulation tests
- `android/` — Android calculator app (Gradle, Kotlin/Java)
- `rust-calc/` — Rust calculator (optional, not built by default)
- HTML files: calculator, phonics games, sandbox

## Commands

### Node / Web
```bash
# Start server (port 3000)
npm start

# Run sandbox tests
npm test

# Install deps (none currently)
npm install
```

### Python
```bash
# Run calc.py
python calc.py

# No external deps (stdlib only)
pip install -r requirements.txt
```

### Android (APK)
```bash
# Build debug APK (requires JDK 17, Gradle)
cd android && ./gradlew assembleDebug --no-daemon

# APK output: android/app/build/outputs/apk/debug/app-debug.apk
# Also copied to apkout/app-debug.apk by CI
```

### GitHub Actions
- **Build APK**: `.github/workflows/build-apk.yml` — triggers on push to main
- **Test job**: added in workflow (runs `npm test`)

### Mobile Testing (Termux + Chrome)
```bash
# Start server in background
node server.js &

# Open in Chrome on device
# Use phone tool: open_url "http://127.0.0.1:3000"
```

## Ports
- **3000** — Node server (`server.js`)

## Key Files
- `server.js` — main web server
- `test-sandbox.mjs` — simulation test suite
- `android/app/build.gradle.kts` — Android build config
- `.github/workflows/build-apk.yml` — CI/CD