/// API d'authentification - endpoints réels du backend :
///   POST /api/auth/login           {email, motDePasse}
///   POST /api/auth/refresh-token   {refreshToken}   (géré par l'intercepteur)
///   POST /api/auth/logout          {refreshToken}
///   GET  /api/auth/me
library;

import '../../../core/network/api_client.dart';
import 'models/auth_models.dart';
import 'models/utilisateur.dart';

class AuthApi {
  final ApiClient _client;
  AuthApi(this._client);

  Future<JwtResponse> login(String email, String motDePasse) async {
    final response = await _client.post<Map<String, dynamic>>(
      '/auth/login',
      body: LoginRequest(email: email, motDePasse: motDePasse).toJson(),
    );
    return JwtResponse.fromJson(response.data!);
  }

  Future<Utilisateur> me() async {
    final response = await _client.get<Map<String, dynamic>>('/auth/me');
    return Utilisateur.fromJson(response.data!);
  }

  Future<void> logout(String? refreshToken) async {
    if (refreshToken == null) return;
    try {
      await _client.post<Map<String, dynamic>>(
        '/auth/logout',
        body: {'refreshToken': refreshToken},
      );
    } catch (_) {
      // Même si l'appel échoue (réseau, token déjà révoqué), on nettoie localement.
    }
  }
}
