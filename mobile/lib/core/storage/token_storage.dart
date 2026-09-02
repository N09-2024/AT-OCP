/// Contrat du stockage sécurisé - permet de substituer une implémentation
/// en mémoire dans les tests (le JWT ne quitte jamais le secure storage en prod).
library;

abstract interface class TokenStorage {
  Future<String?> readAccessToken();
  Future<String?> readRefreshToken();
  Future<void> writeTokens({required String accessToken, required String refreshToken});
  Future<Map<String, dynamic>?> readSession();
  Future<void> writeSession(Map<String, dynamic> sessionJson);
  Future<void> deleteAll();
}
