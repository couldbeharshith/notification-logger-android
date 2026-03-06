# Notification Logger - Android

A React Native Android app that captures and logs all notifications with encryption, biometric authentication, and a modern dark-mode UI.

## Features

- **Encrypted Storage**: SQLCipher with AES-256-GCM encryption
- **Biometric Authentication**: Fingerprint/PIN authentication required
- **Notification Capture**: Captures all notification details automatically
- **Dark Mode UI**: Modern glass morphism design
- **Minimal Resource Usage**: <50MB memory, <30MB app size
- **Fast Performance**: 300ms launch time, 60fps scrolling

## Setup

### Prerequisites

- Node.js >= 18
- Android SDK
- Android device/emulator with API 24+

### Installation

1. Install dependencies:
```bash
cd NotificationLogger
npm install
```

2. Install Android dependencies:
```bash
cd android
./gradlew clean
cd ..
```

3. Run the app:
```bash
npm run android
```

### Permissions

The app requires:
1. **Notification Listener Permission**: Grant in Settings > Apps > Special Access > Notification Access
2. **Biometric Permission**: Automatically requested on first launch

## Architecture

- **Native Layer**: Kotlin modules for notification capture, encryption, and biometric auth
- **Bridge Layer**: React Native TurboModules for JS-Native communication
- **UI Layer**: React Native with TypeScript, Zustand state management
- **Storage**: SQLCipher encrypted database

## Performance Targets

- Memory: <50MB during operation
- App Size: <30MB
- Launch Time: <300ms to first render
- Scroll Performance: 60fps with 10,000+ notifications
- Filter Response: <50ms

## Security

- AES-256-GCM encryption at rest
- Android Keystore for key management
- Biometric authentication required
- Auto-lock after 5 minutes (configurable)
- No data in logs or screenshots

## Development

### Build Release APK

```bash
cd android
./gradlew assembleRelease
```

The APK will be at: `android/app/build/outputs/apk/release/app-release.apk`

### Debug

```bash
npm run android
# Then open Chrome DevTools at chrome://inspect
```

## License

Private - All Rights Reserved
