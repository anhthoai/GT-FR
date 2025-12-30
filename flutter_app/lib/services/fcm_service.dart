import 'package:firebase_messaging/firebase_messaging.dart';
import 'package:firebase_core/firebase_core.dart';
import 'package:flutter/widgets.dart';

import '../models/notification_item.dart';
import '../storage/notification_db.dart';
import '../storage/app_prefs.dart';
import 'notifications_service.dart';
import 'server_api.dart';
import '../firebase_options.dart';

String _buildEventId(RemoteMessage message) {
  final d = message.data;
  final alarmId = (d['alarm_id'] ?? '').toString();
  final time = (d['time'] ?? '').toString();
  if (alarmId.isNotEmpty && time.isNotEmpty) {
    return 'alarm:$alarmId|$time';
  }
  final mid = message.messageId;
  if (mid != null && mid.isNotEmpty) return 'mid:$mid';
  // Last resort: hash-like stable string for this payload
  final name = (d['name'] ?? '').toString();
  final group = (d['group'] ?? '').toString();
  final image = (d['image'] ?? '').toString();
  return 'p:$time|$group|$name|$image';
}

@pragma('vm:entry-point')
Future<void> firebaseMessagingBackgroundHandler(RemoteMessage message) async {
  // This MUST handle data-only messages while the app is backgrounded/killed.
  WidgetsFlutterBinding.ensureInitialized();
  await Firebase.initializeApp(
    options: DefaultFirebaseOptions.currentPlatform,
  );
  await NotificationsService.instance.init();

  final data = message.data;
  final name = (data['name'] ?? '').toString();
  final time = (data['time'] ?? '').toString();
  final group = (data['group'] ?? '').toString();
  final image = (data['image'] ?? '').toString();

  try {
    if (name.isNotEmpty || time.isNotEmpty || group.isNotEmpty || image.isNotEmpty) {
      await NotificationDb.instance.insert(
        NotificationItem(
          eventId: _buildEventId(message),
          name: name,
          time: time,
          group: group,
          imageUrl: image,
          receivedAt: DateTime.now(),
        ),
      );
    }
  } catch (e) {
    // ignore: avoid_print
    print('[FCM] background DB insert failed: $e');
  }

  final title = [
    if (group.isNotEmpty) '[$group]',
    if (name.isNotEmpty) name else 'Unknown',
  ].join(' ');

  await NotificationsService.instance.showBigPicture(
    title: (data['title'] ?? '').toString().trim().isNotEmpty
        ? (data['title'] ?? '').toString()
        : (title.isEmpty ? 'FRResult' : title),
    body: (data['body'] ?? '').toString().trim().isNotEmpty
        ? (data['body'] ?? '').toString()
        : (time.isNotEmpty ? time : 'Face recognition event'),
    imageUrl: image,
  );
}

class FcmService {
  FcmService._();
  static final FcmService instance = FcmService._();

  final FirebaseMessaging _messaging = FirebaseMessaging.instance;
  final AppPrefs _prefs = AppPrefs();
  final ServerApi _api = const ServerApi();

  Future<void> init() async {
    FirebaseMessaging.onBackgroundMessage(firebaseMessagingBackgroundHandler);

    // iOS permission prompt (Android auto)
    await _messaging.requestPermission(
      alert: true,
      badge: true,
      sound: true,
    );

    FirebaseMessaging.onMessage.listen(_onMessage);
    FirebaseMessaging.onMessageOpenedApp.listen(_onMessageOpened);

    // Register current token
    await _registerCurrentToken();

    // Re-register on refresh
    _messaging.onTokenRefresh.listen((t) async {
      await _registerToken(t);
    });
  }

  Future<void> _registerCurrentToken() async {
    final token = await _messaging.getToken();
    if (token == null || token.isEmpty) return;
    await _registerToken(token);
  }

  Future<void> _registerToken(String token) async {
    final serverUrl = await _prefs.getServerUrl();
    final adminId = await _prefs.getAdminId();
    if (serverUrl.isEmpty || adminId.isEmpty) return;

    await _api.registerDeviceToken(
      serverUrl: serverUrl,
      token: token,
      adminId: adminId,
    );
  }

  Future<void> manualRegisterNow() async {
    await _registerCurrentToken();
  }

  Future<void> _onMessage(RemoteMessage message) async {
    final data = message.data;
    final name = (data['name'] ?? '').toString();
    final time = (data['time'] ?? '').toString();
    final group = (data['group'] ?? '').toString();
    final image = (data['image'] ?? '').toString();

    try {
      if (name.isNotEmpty || time.isNotEmpty || group.isNotEmpty || image.isNotEmpty) {
        await NotificationDb.instance.insert(
          NotificationItem(
            eventId: _buildEventId(message),
            name: name,
            time: time,
            group: group,
            imageUrl: image,
            receivedAt: DateTime.now(),
          ),
        );
      }
    } catch (e) {
      NotificationsService.instance.log('[FCM] foreground DB insert failed: $e');
    }

    final title = [
      if (group.isNotEmpty) '[$group]',
      if (name.isNotEmpty) name else 'Unknown',
    ].join(' ');

    await NotificationsService.instance.showBigPicture(
      title: (data['title'] ?? '').toString().trim().isNotEmpty
          ? (data['title'] ?? '').toString()
          : (title.isEmpty ? 'FRResult' : title),
      body: (data['body'] ?? '').toString().trim().isNotEmpty
          ? (data['body'] ?? '').toString()
          : (time.isNotEmpty ? time : 'Face recognition event'),
      imageUrl: image,
    );
  }

  Future<void> _onMessageOpened(RemoteMessage message) async {
    // No-op: home screen reads from DB.
  }
}


