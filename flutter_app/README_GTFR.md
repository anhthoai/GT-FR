# GT-FR Flutter Mobile App (Android + iOS)

This replaces the legacy `mobile-app/` Android-only project with a **Flutter** app that works on **Android and iOS**.

## Features

- Save **Server URL** and **Admin ID**
- Register device **FCM token** to GT-FR server:
  - `POST {ServerURL}/RegisterDevice?token=...&adminid=...`
  - expects response body: `success`
- Receive FCM push notifications (data payload) and show:
  - A local notification
  - An in-app **Inbox** with image + name/time/group

## Firebase setup (recommended: FlutterFire CLI)

This project is configured to use the **FlutterFire CLI** generated `lib/firebase_options.dart`.

From `flutter_app/` run:

```bash
dart pub global activate flutterfire_cli
flutterfire configure --project=gt-face-notify
```

This generates/updates:
- `flutter_app/lib/firebase_options.dart`
- `flutter_app/android/app/google-services.json`
- (for iOS) you still need `GoogleService-Info.plist` in Xcode (see below)

Run:

```bash
cd flutter_app
flutter pub get
flutter run
```

In the app:
- Set **Server URL**: `https://java.goldtek.vn/FRServer`
- Set **Admin ID**: your admin id (e.g. `admin`)
- Tap **Re-register token**

## iOS setup (required for push notifications)

iOS requires additional Firebase + Apple Push Notification (APNs) configuration:

- Add an iOS app in Firebase Console (bundle id for Runner)
- Download `GoogleService-Info.plist` and place it into:
  - `flutter_app/ios/Runner/GoogleService-Info.plist`
- Configure APNs key/cert in Firebase (required for real push delivery)
- Open `flutter_app/ios/Runner.xcworkspace` in Xcode and run on a device

## Server requirements

- `server-app` must be deployed and reachable from the phone
- `RegisterDevice` servlet must be working and writing into `device_lists`


