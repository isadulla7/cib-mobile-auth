import 'dart:math';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';

class DeviceIdService {
  static const _key = 'cib_device_id';
  final FlutterSecureStorage _storage;
  final Random _random = Random.secure();

  DeviceIdService({FlutterSecureStorage? storage})
      : _storage = storage ?? const FlutterSecureStorage();

  Future<String> getOrCreate() async {
    final existing = await _storage.read(key: _key);
    if (existing != null && existing.length >= 16) return existing;

    final bytes = List<int>.generate(32, (_) => _random.nextInt(256));
    final id = bytes.map((b) => b.toRadixString(16).padLeft(2, '0')).join();
    await _storage.write(key: _key, value: id);
    return id;
  }
}
