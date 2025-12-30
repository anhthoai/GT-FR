import 'dart:math';

import 'package:flutter/foundation.dart';
import 'package:flutter_local_notifications/flutter_local_notifications.dart';

class NotificationsService {
  NotificationsService._();
  static final NotificationsService instance = NotificationsService._();

  final FlutterLocalNotificationsPlugin _plugin =
      FlutterLocalNotificationsPlugin();

  Future<void> init() async {
    const android = AndroidInitializationSettings('@mipmap/ic_launcher');
    const ios = DarwinInitializationSettings();
    const settings = InitializationSettings(android: android, iOS: ios);
    await _plugin.initialize(settings);

    // Android 13+ runtime permission
    final androidImpl = _plugin.resolvePlatformSpecificImplementation<
        AndroidFlutterLocalNotificationsPlugin>();
    await androidImpl?.requestNotificationsPermission();
  }

  Future<void> showBigPicture({
    required String title,
    required String body,
  }) async {
    // Keep it simple: we render a normal notification with text.
    // The app UI shows the image.
    const androidDetails = AndroidNotificationDetails(
      'fr_result',
      'FR Results',
      channelDescription: 'Face recognition events',
      importance: Importance.max,
      priority: Priority.high,
    );
    const details = NotificationDetails(android: androidDetails);
    await _plugin.show(Random().nextInt(1 << 30), title, body, details);
  }

  void log(String msg) {
    if (kDebugMode) {
      // ignore: avoid_print
      print(msg);
    }
  }
}


