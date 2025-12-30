import 'package:firebase_messaging/firebase_messaging.dart';

import '../models/notification_item.dart';
import '../storage/notification_db.dart';
import '../storage/app_prefs.dart';
import 'notifications_service.dart';
import 'server_api.dart';

@pragma('vm:entry-point')
Future<void> firebaseMessagingBackgroundHandler(RemoteMessage message) async {
  // Keep background handler minimal. The app will show stored events when opened.
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

    if (name.isNotEmpty || time.isNotEmpty || group.isNotEmpty || image.isNotEmpty) {
      await NotificationDb.instance.insert(
        NotificationItem(
          name: name,
          time: time,
          group: group,
          imageUrl: image,
          receivedAt: DateTime.now(),
        ),
      );
    }

    await NotificationsService.instance.showBigPicture(
      title: name.isEmpty ? 'FRResult' : name,
      body: [time, group].where((s) => s.isNotEmpty).join(' • '),
    );
  }

  Future<void> _onMessageOpened(RemoteMessage message) async {
    // No-op: home screen reads from DB.
  }
}


