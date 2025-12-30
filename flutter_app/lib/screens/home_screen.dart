import 'package:flutter/material.dart';

import '../models/notification_item.dart';
import '../services/fcm_service.dart';
import '../storage/app_prefs.dart';
import '../storage/notification_db.dart';
import 'settings_screen.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  final _prefs = AppPrefs();
  String _serverUrl = '';
  String _adminId = '';
  List<NotificationItem> _items = const [];

  @override
  void initState() {
    super.initState();
    _refreshAll();
  }

  Future<void> _refreshAll() async {
    _serverUrl = await _prefs.getServerUrl();
    _adminId = await _prefs.getAdminId();
    _items = await NotificationDb.instance.list(limit: 200);
    if (mounted) setState(() {});
  }

  Future<void> _openSettings() async {
    final changed = await Navigator.of(context).push<bool>(
      MaterialPageRoute(builder: (_) => const SettingsScreen()),
    );
    if (changed == true) await _refreshAll();
  }

  Future<void> _clear() async {
    await NotificationDb.instance.clear();
    await _refreshAll();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('GT-FR Notifications'),
        actions: [
          IconButton(
            onPressed: _openSettings,
            icon: const Icon(Icons.settings),
            tooltip: 'Settings',
          ),
          IconButton(
            onPressed: _clear,
            icon: const Icon(Icons.delete_outline),
            tooltip: 'Clear',
          ),
        ],
      ),
      body: RefreshIndicator(
        onRefresh: _refreshAll,
        child: ListView(
          padding: const EdgeInsets.all(16),
          children: [
            Card(
              child: Padding(
                padding: const EdgeInsets.all(12),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Text('Current settings',
                        style: TextStyle(fontWeight: FontWeight.bold)),
                    const SizedBox(height: 8),
                    Text('Server URL: ${_serverUrl.isEmpty ? '(not set)' : _serverUrl}'),
                    Text('Admin ID: ${_adminId.isEmpty ? '(not set)' : _adminId}'),
                    const SizedBox(height: 8),
                    Row(
                      children: [
                        FilledButton.icon(
                          onPressed: _openSettings,
                          icon: const Icon(Icons.tune),
                          label: const Text('Edit'),
                        ),
                        const SizedBox(width: 12),
                        OutlinedButton.icon(
                          onPressed: () async {
                            await FcmService.instance.manualRegisterNow();
                            if (!context.mounted) return;
                            ScaffoldMessenger.of(context).showSnackBar(
                              const SnackBar(content: Text('Token registration triggered')),
                            );
                          },
                          icon: const Icon(Icons.refresh),
                          label: const Text('Re-register token'),
                        ),
                      ],
                    )
                  ],
                ),
              ),
            ),
            const SizedBox(height: 12),
            const Text('Inbox',
                style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
            const SizedBox(height: 8),
            if (_items.isEmpty)
              const Padding(
                padding: EdgeInsets.only(top: 24),
                child: Center(
                  child: Text('No notifications yet.'),
                ),
              )
            else
              ..._items.map(_NotificationTile.new),
            const SizedBox(height: 40),
          ],
        ),
      ),
    );
  }
}

class _NotificationTile extends StatelessWidget {
  final NotificationItem item;
  const _NotificationTile(this.item);

  @override
  Widget build(BuildContext context) {
    return Card(
      child: ListTile(
        leading: item.imageUrl.isEmpty
            ? const CircleAvatar(child: Icon(Icons.person))
            : ClipRRect(
                borderRadius: BorderRadius.circular(6),
                child: Image.network(
                  item.imageUrl,
                  width: 56,
                  height: 56,
                  fit: BoxFit.cover,
                  errorBuilder: (_, __, ___) =>
                      const SizedBox(width: 56, height: 56, child: Icon(Icons.broken_image)),
                ),
              ),
        title: Text(item.name.isEmpty ? 'Unknown' : item.name),
        subtitle: Text(
          [
            if (item.time.isNotEmpty) item.time,
            if (item.group.isNotEmpty) item.group,
          ].join(' • '),
        ),
        trailing: Text(
          _fmt(item.receivedAt),
          style: const TextStyle(fontSize: 12),
        ),
      ),
    );
  }

  static String _fmt(DateTime dt) {
    String two(int v) => v.toString().padLeft(2, '0');
    return '${two(dt.hour)}:${two(dt.minute)}';
  }
}


