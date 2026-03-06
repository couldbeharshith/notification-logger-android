# Setup Android SDK Without Android Studio

## 1. Download Android Command Line Tools

Download from: https://developer.android.com/studio#command-line-tools-only
- Get "Command line tools only" for Windows
- Extract to: C:\Android\cmdline-tools\latest

## 2. Set Environment Variables

```powershell
# Run in PowerShell as Administrator:
[System.Environment]::SetEnvironmentVariable('ANDROID_HOME', 'C:\Android', 'User')
[System.Environment]::SetEnvironmentVariable('Path', $env:Path + ';C:\Android\cmdline-tools\latest\bin;C:\Android\platform-tools', 'User')
```

## 3. Install Required SDK Components

```bash
# Restart terminal, then run:
sdkmanager "platform-tools" "platforms;android-36" "build-tools;36.0.0"
sdkmanager --licenses
```

## 4. Connect Phone & Enable USB Debugging

- Settings > About Phone > Tap "Build Number" 7 times
- Settings > Developer Options > Enable "USB Debugging"
- Connect phone via USB
- Run: adb devices (should show your device)

## 5. Install Dependencies & Run

```bash
cd NotificationLogger
npm install
npm run android
```
