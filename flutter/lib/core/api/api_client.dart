import 'dart:convert';
import 'package:http/http.dart' as http;
import '../device/device_id_service.dart';
import '../security/token_store.dart';

class ApiException implements Exception {
  final int statusCode;
  final String code;
  final String message;
  final Map<String, dynamic> details;

  ApiException(this.statusCode, this.code, this.message, this.details);

  @override
  String toString() => '$code: $message';
}

class ApiClient {
  final String baseUrl;
  final http.Client _http;
  final DeviceIdService _deviceId;
  final TokenStore _tokens;

  ApiClient({
    required this.baseUrl,
    http.Client? httpClient,
    DeviceIdService? deviceId,
    TokenStore? tokens,
  })  : _http = httpClient ?? http.Client(),
        _deviceId = deviceId ?? DeviceIdService(),
        _tokens = tokens ?? TokenStore();

  Future<Map<String, dynamic>> getJson(
    String path, {
    bool authenticated = false,
  }) async {
    final response = await _http.get(
      Uri.parse('$baseUrl$path'),
      headers: await _headers(authenticated: authenticated),
    );
    return _decode(response);
  }

  Future<Map<String, dynamic>> postJson(
    String path,
    Map<String, dynamic> body, {
    bool authenticated = false,
  }) async {
    final response = await _http.post(
      Uri.parse('$baseUrl$path'),
      headers: await _headers(authenticated: authenticated),
      body: jsonEncode(body),
    );
    return _decode(response);
  }

  Future<Map<String, String>> _headers({required bool authenticated}) async {
    final headers = <String, String>{
      'Content-Type': 'application/json',
      'X-Device-Id': await _deviceId.getOrCreate(),
    };
    if (authenticated) {
      final access = await _tokens.accessToken();
      if (access != null) headers['Authorization'] = 'Bearer $access';
    }
    return headers;
  }

  Map<String, dynamic> _decode(http.Response response) {
    final dynamic raw =
        response.body.isEmpty ? <String, dynamic>{} : jsonDecode(response.body);
    final json = raw is Map<String, dynamic>
        ? raw
        : <String, dynamic>{'data': raw};

    if (response.statusCode >= 200 && response.statusCode < 300) return json;

    throw ApiException(
      response.statusCode,
      json['code']?.toString() ?? 'HTTP_${response.statusCode}',
      json['message']?.toString() ?? 'Request failed',
      (json['details'] as Map?)?.cast<String, dynamic>() ?? const {},
    );
  }
}
