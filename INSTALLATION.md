# Installation Guide

## Quick Start

1. Install dependencies:
```bash
cd NotificationLogger
npm install
```

2. Run on Android:
```bash
npm run android
```

## First Launch

1. App will request biometric authentication
2. Grant notification listener permission when prompted
3. Start receiving notifications!

## Troubleshooting

### Build Errors
- Clean build: `cd android && ./gradlew clean && cd ..`
- Clear cache: `npm start -- --reset-cache`

### Permission Issues
- Manually enable: Settings > Apps > Special Access > Notification Access
- Toggle "Notification Logger" ON

### Authentication Issues
- Ensure device has fingerprint enrolled
- Fallback to PIN/password if biometric fails

## Development

Run Metro bundler:
```bash
npm start
```

Build release APK:
```bash
cd android && ./gradlew assembleRelease
```
