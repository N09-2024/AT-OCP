/// Contrôleur d'authentification (Riverpod StateNotifier).
/// Gère : connexion, déconnexion, restauration de session au démarrage,
/// expiration de session (callback branché sur le ApiClient).
///
/// Les rôles/permissions ne sont renvoyés que par /auth/login : ils sont donc
/// persistés dans le stockage sécurisé et restaurés au démarrage.
library;

import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/errors/error_mapper.dart';
import '../../../core/errors/failures.dart';
import '../../../core/network/api_client.dart';
import '../../../core/network/api_providers.dart';
import '../../../core/storage/token_storage.dart';
import '../data/auth_api.dart';
import '../data/models/auth_models.dart';
import '../data/models/utilisateur.dart';

sealed class AuthState {
  const AuthState();
}

class AuthInitial extends AuthState {
  const AuthInitial();
}

class AuthLoading extends AuthState {
  const AuthLoading();
}

class AuthAuthenticated extends AuthState {
  final AuthSession session;
  const AuthAuthenticated(this.session);
}

class AuthError extends AuthState {
  final Failure failure;
  const AuthError(this.failure);
}

class AuthUnauthenticated extends AuthState {
  const AuthUnauthenticated();
}

class AuthController extends StateNotifier<AuthState> {
  final AuthApi _api;
  final TokenStorage _tokens;
  final ApiClient _client;

  AuthController(this._api, this._tokens, this._client) : super(const AuthInitial()) {
    // Déconnexion complète quand le refresh token échoue (intercepteur 401) :
    // purge des tokens + retour à l'écran de connexion.
    _client.onSessionExpired = () async {
      await _tokens.deleteAll();
      if (state is! AuthUnauthenticated) {
        state = const AuthUnauthenticated();
      }
    };
  }

  /// Au démarrage : si un couple de tokens + une session existent → session
  /// restaurée, profil rafraîchi via GET /auth/me si le réseau le permet.
  Future<void> restoreSession() async {
    final access = await _tokens.readAccessToken();
    final refresh = await _tokens.readRefreshToken();
    final stored = await _tokens.readSession();

    if (access == null || access.isEmpty || refresh == null || refresh.isEmpty || stored == null) {
      state = const AuthUnauthenticated();
      return;
    }

    final utilisateur = Utilisateur.fromJson(stored['utilisateur'] as Map<String, dynamic>);
    final roles = (stored['roles'] as List<dynamic>? ?? []).map((e) => e.toString()).toList();
    final permissions =
        (stored['permissions'] as List<dynamic>? ?? []).map((e) => e.toString()).toList();

    state = AuthAuthenticated(AuthSession(
      utilisateur: utilisateur,
      roles: roles,
      permissions: permissions,
    ),);

    // Rafraîchissement best-effort du profil (données plus fraîches côté serveur).
    try {
      final fresh = await _api.me();
      state = AuthAuthenticated(AuthSession(
        utilisateur: fresh,
        roles: fresh.roles.map((r) => r.nom).isNotEmpty
            ? fresh.roles.map((r) => r.nom).toList()
            : roles,
        permissions: permissions,
      ),);
    } on DioException catch (_) {
      // Hors ligne ou serveur injoignable : on garde la session locale.
    } catch (_) {/* idem */}
  }

  Future<bool> login(String email, String motDePasse) async {
    state = const AuthLoading();
    try {
      final jwt = await _api.login(email, motDePasse);
      await _tokens.writeTokens(
        accessToken: jwt.accessToken,
        refreshToken: jwt.refreshToken,
      );
      await _tokens.writeSession({
        'utilisateur': {
          'id': jwt.utilisateur.id,
          'email': jwt.utilisateur.email,
          'nom': jwt.utilisateur.nom,
          'prenom': jwt.utilisateur.prenom,
          'matricule': jwt.utilisateur.matricule,
          'telephone': jwt.utilisateur.telephone,
          'actif': jwt.utilisateur.actif,
          'enAttenteValidation': jwt.utilisateur.enAttenteValidation,
          'service': jwt.utilisateur.service == null
              ? null
              : {
                  'id': jwt.utilisateur.service!.id,
                  'nomService': jwt.utilisateur.service!.nomService,
                  'codeService': jwt.utilisateur.service!.codeService,
                },
          'roles': jwt.utilisateur.roles
              .map((r) => {'id': r.id, 'nom': r.nom})
              .toList(),
        },
        'roles': jwt.roles,
        'permissions': jwt.permissions,
      });
      state = AuthAuthenticated(AuthSession(
        utilisateur: jwt.utilisateur,
        roles: jwt.roles,
        permissions: jwt.permissions,
      ),);
      return true;
    } catch (e) {
      state = AuthError(mapDioError(e));
      return false;
    }
  }

  Future<void> logout() async {
    final refresh = await _tokens.readRefreshToken();
    await _api.logout(refresh);
    await _tokens.deleteAll();
    state = const AuthUnauthenticated();
  }
}

// --- Providers ---

final authApiProvider = Provider<AuthApi>((ref) => AuthApi(ref.watch(apiClientProvider)));

final authControllerProvider = StateNotifierProvider<AuthController, AuthState>((ref) {
  return AuthController(
    ref.watch(authApiProvider),
    ref.watch(secureTokenStorageProvider),
    ref.watch(apiClientProvider),
  );
});

/// Session courante (null si non connecté) — accès rapide pour l'UI et les gardes.
final sessionProvider = Provider<AuthSession?>((ref) {
  final state = ref.watch(authControllerProvider);
  return state is AuthAuthenticated ? state.session : null;
});

/// Permission helper : ref.watch(hasPermissionProvider)('VALIDATE_AT').
final hasPermissionProvider = Provider<bool Function(String)>((ref) {
  final session = ref.watch(sessionProvider);
  return (permission) => session?.hasPermission(permission) ?? false;
});
