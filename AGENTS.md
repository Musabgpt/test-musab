# M80 Project - Agent Commands (Android Only)

## Project Structure
- `android/` — Android calculator app (Gradle Kotlin DSL, Java)

## Commands

### Android (APK)
```bash
# Build debug APK (requires JDK 17, Gradle)
cd android && ./gradlew assembleDebug --no-daemon

# APK output: android/app/build/outputs/apk/debug/app-debug.apk
# Also copied to apkout/app-debug.apk by CI
```

### GitHub Actions
- **Build APK**: `.github/workflows/build-apk.yml` — triggers on push to main

## Key Files
- `android/app/build.gradle.kts` — Android build config
- `android/app/src/main/java/com/musab/calculator/Evaluator.java` — Expression evaluator
- `android/app/src/main/java/com/musab/calculator/MainActivity.java` — UI & logic
- `.github/workflows/build-apk.yml` — CI/CD