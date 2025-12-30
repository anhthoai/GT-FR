import 'package:shared_preferences/shared_preferences.dart';

class AppPrefs {
  static const _kServerUrl = 'server_url';
  static const _kAdminId = 'admin_id';

  Future<String> getServerUrl() async {
    final sp = await SharedPreferences.getInstance();
    return sp.getString(_kServerUrl) ?? '';
  }

  Future<String> getAdminId() async {
    final sp = await SharedPreferences.getInstance();
    return sp.getString(_kAdminId) ?? '';
  }

  Future<void> setServerUrl(String url) async {
    final sp = await SharedPreferences.getInstance();
    await sp.setString(_kServerUrl, url);
  }

  Future<void> setAdminId(String adminId) async {
    final sp = await SharedPreferences.getInstance();
    await sp.setString(_kAdminId, adminId);
  }
}


