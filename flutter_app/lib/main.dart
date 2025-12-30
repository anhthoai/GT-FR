import 'package:flutter/material.dart';
import 'package:firebase_core/firebase_core.dart';

import 'services/fcm_service.dart';
import 'services/notifications_service.dart';
import 'screens/home_screen.dart';
import 'firebase_options.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await Firebase.initializeApp(
    options: DefaultFirebaseOptions.currentPlatform,
  );
  await NotificationsService.instance.init();
  await FcmService.instance.init();
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'GT-Face Notify',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: const Color(0xFF0A7AFF)),
      ),
      home: const HomeScreen(),
    );
  }
}
