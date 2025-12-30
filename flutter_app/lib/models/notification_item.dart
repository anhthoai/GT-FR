class NotificationItem {
  final int? id;
  final String eventId;
  final String name;
  final String time;
  final String group;
  final String imageUrl;
  final DateTime receivedAt;

  NotificationItem({
    this.id,
    required this.eventId,
    required this.name,
    required this.time,
    required this.group,
    required this.imageUrl,
    required this.receivedAt,
  });

  Map<String, Object?> toMap() => {
        'id': id,
        'event_id': eventId,
        'name': name,
        'time': time,
        'grp': group,
        'image_url': imageUrl,
        'received_at': receivedAt.toIso8601String(),
      };

  static NotificationItem fromMap(Map<String, Object?> m) {
    return NotificationItem(
      id: (m['id'] as int?),
      eventId: (m['event_id'] as String?) ?? '',
      name: (m['name'] as String?) ?? '',
      time: (m['time'] as String?) ?? '',
      group: (m['grp'] as String?) ?? '',
      imageUrl: (m['image_url'] as String?) ?? '',
      receivedAt: DateTime.tryParse((m['received_at'] as String?) ?? '') ??
          DateTime.fromMillisecondsSinceEpoch(0),
    );
  }
}


