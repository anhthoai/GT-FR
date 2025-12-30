import 'package:path/path.dart' as p;
import 'package:path_provider/path_provider.dart';
import 'package:sqflite/sqflite.dart';
import 'dart:async';

import '../models/notification_item.dart';

class NotificationDb {
  static final NotificationDb instance = NotificationDb._();
  NotificationDb._();

  Database? _db;
  final StreamController<void> _changes = StreamController<void>.broadcast();

  Stream<void> get changes => _changes.stream;

  Future<Database> get db async {
    final existing = _db;
    if (existing != null) return existing;

    final dir = await getApplicationDocumentsDirectory();
    final dbPath = p.join(dir.path, 'notifications.db');
    _db = await openDatabase(
      dbPath,
      version: 2,
      onCreate: (db, _) async {
        await db.execute('''
          CREATE TABLE notifications (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            event_id TEXT NOT NULL,
            name TEXT NOT NULL,
            time TEXT NOT NULL,
            grp TEXT NOT NULL,
            image_url TEXT NOT NULL,
            received_at TEXT NOT NULL
          )
        ''');
        await db.execute('CREATE UNIQUE INDEX idx_notifications_event_id ON notifications(event_id)');
      },
      onUpgrade: (db, oldVersion, newVersion) async {
        if (oldVersion < 2) {
          await db.execute("ALTER TABLE notifications ADD COLUMN event_id TEXT NOT NULL DEFAULT ''");
          // Backfill existing rows with a stable value to satisfy uniqueness.
          await db.execute("UPDATE notifications SET event_id = 'legacy_' || id WHERE event_id = ''");
          await db.execute('CREATE UNIQUE INDEX IF NOT EXISTS idx_notifications_event_id ON notifications(event_id)');
        }
      },
    );
    return _db!;
  }

  Future<int> insert(NotificationItem item) async {
    final database = await db;
    final id = await database.insert(
      'notifications',
      item.toMap(),
      conflictAlgorithm: ConflictAlgorithm.ignore,
    );
    _changes.add(null);
    return id;
  }

  Future<List<NotificationItem>> list({int limit = 100}) async {
    final database = await db;
    final rows = await database.query(
      'notifications',
      orderBy: 'id DESC',
      limit: limit,
    );
    return rows.map((m) => NotificationItem.fromMap(m)).toList();
  }

  Future<void> clear() async {
    final database = await db;
    await database.delete('notifications');
    _changes.add(null);
  }
}


