/// Modèles d'authentification — reflètent EXACTEMENT les DTO backend :
/// LoginRequest{email, motDePasse} → JwtResponse{accessToken, refreshToken, type,
/// utilisateur, roles[], permissions[]}.
/// Source : backend/src/main/java/com/ocp/at/dto/ (cf. rapport Phase 1 §C-E).
library;

import 'utilisateur.dart';

class LoginRequest {
  final String email;
  final String motDePasse;

  const LoginRequest({required this.email, required this.motDePasse});

  Map<String, dynamic> toJson() => {'email': email, 'motDePasse': motDePasse};
}

class JwtResponse {
  final String accessToken;
  final String refreshToken;
  final String type;
  final Utilisateur utilisateur;
  final List<String> roles;
  final List<String> permissions;

  const JwtResponse({
    required this.accessToken,
    required this.refreshToken,
    required this.type,
    required this.utilisateur,
    required this.roles,
    required this.permissions,
  });

  factory JwtResponse.fromJson(Map<String, dynamic> json) => JwtResponse(
        accessToken: json['accessToken'] as String,
        refreshToken: json['refreshToken'] as String,
        type: json['type'] as String? ?? 'Bearer',
        utilisateur: Utilisateur.fromJson(json['utilisateur'] as Map<String, dynamic>),
        roles: (json['roles'] as List<dynamic>? ?? [])
            .map((e) => e as String)
            .toList(),
        permissions: (json['permissions'] as List<dynamic>? ?? [])
            .map((e) => e as String)
            .toList(),
      );
}

/// Session persistée entre les démarrages (tokens en secure storage,
/// utilisateur en mémoire / rechargé via GET /auth/me).
class AuthSession {
  final Utilisateur utilisateur;
  final List<String> roles;
  final List<String> permissions;

  const AuthSession({
    required this.utilisateur,
    required this.roles,
    required this.permissions,
  });

  bool hasPermission(String permission) => permissions.contains(permission);

  bool hasRole(String role) => roles.contains(role);

  /// Rôles "normaux" : ADMIN ou un des rôles internes OCP.
  bool get isOcpUser =>
      roles.contains('ADMIN') ||
      roles.any((r) => ['CEEP', 'CEEE', 'HCEP', 'HCEE', 'HMEP', 'HMEE', 'CE', 'HM', 'HC']
          .contains(r),);
}
