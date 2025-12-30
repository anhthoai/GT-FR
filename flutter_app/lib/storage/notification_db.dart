import 'package:path/path.dart' as p;
import 'package:path_provider/path_provider.dart';
import 'package:sqflite/sqflite.dart';

import '../models/notification_item.dart';

class NotificationDb {
  static final NotificationDb instance = NotificationDb._();
  NotificationDb._();

  Database? _db;

  Future<Database> get db async {
    final existing = _db;
    if (existing != null) return existing;

    final dir = await getApplicationDocumentsDirectory();
    final dbPath = p.join(dir.path, 'notifications.db');
    _db = await openDatabase(
      dbPath,
      version: 1,
      onCreate: (db, _) async {
        await db.execute('''
          CREATE TABLE notifications (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT NOT NULL,
            time TEXT NOT NULL,
            grp TEXT NOT NULL,
            image_url TEXT NOT NULL,
            received_at TEXT NOT NULL
          )
        ''');
      },
    );
    return _db!;
  }

  Future<int> insert(NotificationItem item) async {
    final database = await db;
    return database.insert('notifications', item.toMap());
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
  }
}


