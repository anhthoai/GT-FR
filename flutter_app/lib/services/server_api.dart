import 'dart:convert';

import 'package:http/http.dart' as http;

class ServerApi {
  const ServerApi();

  static Uri _buildRegisterDeviceUri({
    required String serverUrl,
    required String token,
    required String adminId,
  }) {
    // Expected on server: /FRServer/RegisterDevice?token=...&adminid=...
    var base = serverUrl.trim();
    if (base.endsWith('/')) base = base.substring(0, base.length - 1);
    if (!base.startsWith('http://') && !base.startsWith('https://')) {
      base = 'https://$base';
    }
    final uri = Uri.parse('$base/RegisterDevice').replace(queryParameters: {
      'token': token,
      'adminid': adminId,
    });
    return uri;
  }

  Future<bool> registerDeviceToken({
    required String serverUrl,
    required String token,
    required String adminId,
  }) async {
    final uri = _buildRegisterDeviceUri(
      serverUrl: serverUrl,
      token: token,
      adminId: adminId,
    );

    // The legacy Android app sends a POST + JSON body, but servlet reads query params.
    final res = await http.post(
      uri,
      headers: const {'Content-Type': 'application/json; charset=UTF-8'},
      body: jsonEncode({'token': token}),
    );

    final body = res.body.trim();
    return res.statusCode >= 200 && res.statusCode < 300 && body == 'success';
  }
}


