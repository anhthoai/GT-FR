import 'dart:math';

import 'package:flutter/foundation.dart';
import 'package:flutter_local_notifications/flutter_local_notifications.dart';
import 'package:http/http.dart' as http;

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
  }

  Future<void> requestPermissionIfNeeded() async {
    // IMPORTANT:
    // Must be called while the app has an Activity (foreground), otherwise Android context can be null.
    try {
      final androidImpl = _plugin.resolvePlatformSpecificImplementation<
          AndroidFlutterLocalNotificationsPlugin>();
      await androidImpl?.requestNotificationsPermission();
    } catch (e) {
      log('[Notifications] request permission failed (ignored): $e');
    }
  }

  Future<void> showBigPicture({
    required String title,
    required String body,
    String imageUrl = '',
  }) async {
    AndroidNotificationDetails androidDetails;

    final trimmed = imageUrl.trim();
    if (trimmed.isNotEmpty && trimmed.startsWith('http')) {
      try {
        final uri = Uri.parse(trimmed);
        final resp = await http.get(uri);
        if (resp.statusCode >= 200 && resp.statusCode < 300) {
          final style = BigPictureStyleInformation(
            ByteArrayAndroidBitmap(resp.bodyBytes),
            contentTitle: title,
            summaryText: body,
          );

          androidDetails = AndroidNotificationDetails(
            'fr_result',
            'FR Results',
            channelDescription: 'Face recognition events',
            importance: Importance.max,
            priority: Priority.high,
            styleInformation: style,
            largeIcon: ByteArrayAndroidBitmap(resp.bodyBytes),
          );
        } else {
          androidDetails = const AndroidNotificationDetails(
            'fr_result',
            'FR Results',
            channelDescription: 'Face recognition events',
            importance: Importance.max,
            priority: Priority.high,
          );
        }
      } catch (_) {
        androidDetails = const AndroidNotificationDetails(
          'fr_result',
          'FR Results',
          channelDescription: 'Face recognition events',
          importance: Importance.max,
          priority: Priority.high,
        );
      }
    } else {
      androidDetails = const AndroidNotificationDetails(
        'fr_result',
        'FR Results',
        channelDescription: 'Face recognition events',
        importance: Importance.max,
        priority: Priority.high,
      );
    }

    final details = NotificationDetails(android: androidDetails);
    await _plugin.show(Random().nextInt(1 << 30), title, body, details);
  }

  void log(String msg) {
    if (kDebugMode) {
      // ignore: avoid_print
      print(msg);
    }
  }
}


