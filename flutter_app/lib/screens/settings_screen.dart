import 'package:flutter/material.dart';

import '../services/fcm_service.dart';
import '../storage/app_prefs.dart';

class SettingsScreen extends StatefulWidget {
  const SettingsScreen({super.key});

  @override
  State<SettingsScreen> createState() => _SettingsScreenState();
}

class _SettingsScreenState extends State<SettingsScreen> {
  final _prefs = AppPrefs();
  final _serverCtrl = TextEditingController();
  final _adminCtrl = TextEditingController();
  bool _saving = false;

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    _serverCtrl.text = await _prefs.getServerUrl();
    _adminCtrl.text = await _prefs.getAdminId();
    if (mounted) setState(() {});
  }

  Future<void> _save() async {
    final server = _serverCtrl.text.trim();
    final admin = _adminCtrl.text.trim();
    if (server.isEmpty || admin.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Please enter Server URL and Admin ID')),
      );
      return;
    }

    setState(() => _saving = true);
    try {
      await _prefs.setServerUrl(server);
      await _prefs.setAdminId(admin);
      await FcmService.instance.manualRegisterNow();
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Saved. Token registration triggered.')),
      );
      Navigator.of(context).pop(true);
    } finally {
      if (mounted) setState(() => _saving = false);
    }
  }

  @override
  void dispose() {
    _serverCtrl.dispose();
    _adminCtrl.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Settings')),
      body: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          children: [
            TextField(
              controller: _serverCtrl,
              decoration: const InputDecoration(
                labelText: 'Server URL',
                hintText: 'https://java.goldtek.vn/FRServer',
              ),
            ),
            const SizedBox(height: 12),
            TextField(
              controller: _adminCtrl,
              decoration: const InputDecoration(
                labelText: 'Admin ID',
                hintText: 'admin',
              ),
            ),
            const SizedBox(height: 20),
            SizedBox(
              width: double.infinity,
              child: FilledButton(
                onPressed: _saving ? null : _save,
                child: Text(_saving ? 'Saving…' : 'Save'),
              ),
            ),
          ],
        ),
      ),
    );
  }
}


