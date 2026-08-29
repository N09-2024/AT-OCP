/// Stockage sécurisé des tokens JWT et de la session (rôles/permissions)
/// via flutter_secure_storage (Keychain / Keystore).
/// Le JWT ne doit JAMAIS être stocké dans SharedPreferences.
///
/// Les permissions ne sont renvoyées que par POST /auth/login : on les
/// persiste donc avec la session pour survivre au redémarrage de l'application.
library;

import 'dart:convert';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'token_storage.dart';

class SecureTokenStorage implements TokenStorage {
  static const _keyAccessToken = 'ocp_at.access_token';
  static const _keyRefreshToken = 'ocp_at.refresh_token';
  static const _keySession = 'ocp_at.session';

  final FlutterSecureStorage _storage;

  SecureTokenStorage([FlutterSecureStorage? storage])
      : _storage = storage ??
            const FlutterSecureStorage(
              // v11 : Android fortement chiffré par défaut ; iOS accessible
              // tant que l'appareil est déverrouillé.
              aOptions: AndroidOptions(),
              iOptions: IOSOptions(accessibility: KeychainAccessibility.unlocked),
            );

  @override
  Future<String?> readAccessToken() => _storage.read(key: _keyAccessToken);

  @override
  Future<String?> readRefreshToken() => _storage.read(key: _keyRefreshToken);

  @override
  Future<void> writeTokens({required String accessToken, required String refreshToken}) =>
      _storage.write(key: _keyAccessToken, value: accessToken).then(
            (_) => _storage.write(key: _keyRefreshToken, value: refreshToken),
          );

  /// Session sérialisée : {utilisateur, roles, permissions} (issus du login).
  @override
  Future<Map<String, dynamic>?> readSession() async {
    final raw = await _storage.read(key: _keySession);
    if (raw == null || raw.isEmpty) return null;
    try {
      return jsonDecode(raw) as Map<String, dynamic>;
    } catch (_) {
      return null;
    }
  }

  @override
  Future<void> writeSession(Map<String, dynamic> sessionJson) =>
      _storage.write(key: _keySession, value: jsonEncode(sessionJson));

  @override
  Future<void> deleteAll() async {
    await _storage.delete(key: _keyAccessToken);
    await _storage.delete(key: _keyRefreshToken);
    await _storage.delete(key: _keySession);
  }
}
