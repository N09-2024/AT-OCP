// Tests du AuthController — login, logout, restauration, expiration de session.
// Fakes en mémoire : aucun canal de plateforme requis.

import 'package:dio/dio.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:ocp_at_mobile/core/network/api_client.dart';
import 'package:ocp_at_mobile/core/storage/token_storage.dart';
import 'package:ocp_at_mobile/features/auth/data/auth_api.dart';
import 'package:ocp_at_mobile/features/auth/data/models/auth_models.dart';
import 'package:ocp_at_mobile/features/auth/data/models/utilisateur.dart';
import 'package:ocp_at_mobile/features/auth/presentation/auth_controller.dart';

class InMemoryTokenStorage implements TokenStorage {
  String? access;
  String? refresh;
  Map<String, dynamic>? session;

  @override
  Future<void> deleteAll() async {
    access = null;
    refresh = null;
    session = null;
  }

  @override
  Future<String?> readAccessToken() async => access;

  @override
  Future<String?> readRefreshToken() async => refresh;

  @override
  Future<Map<String, dynamic>?> readSession() async => session;

  @override
  Future<void> writeSession(Map<String, dynamic> sessionJson) async => session = sessionJson;

  @override
  Future<void> writeTokens({required String accessToken, required String refreshToken}) async {
    access = accessToken;
    refresh = refreshToken;
  }
}

class FakeAuthApi extends AuthApi {
  FakeAuthApi(super.client);

  JwtResponse? loginResponse;
  Object? loginError;
  bool logoutCalled = false;

  @override
  Future<JwtResponse> login(String email, String motDePasse) async {
    if (loginError != null) throw loginError!;
    return loginResponse!;
  }

  @override
  Future<void> logout(String? refreshToken) async => logoutCalled = true;

  @override
  Future<Utilisateur> me() async => _utilisateur;
}

final Utilisateur _utilisateur = Utilisateur(
  id: 'u1',
  email: 'ceep@ocp.ma',
  nom: 'Propriétaire',
  prenom: 'Chef',
  actif: true,
  enAttenteValidation: false,
  roles: const [RoleRef(id: 'r1', nom: 'CEEP')],
);

JwtResponse _jwt() => JwtResponse(
      accessToken: 'access-1',
      refreshToken: 'refresh-1',
      type: 'Bearer',
      utilisateur: _utilisateur,
      roles: const ['CEEP'],
      permissions: const ['READ_AT', 'CREATE_AT'],
    );

void main() {
  late InMemoryTokenStorage storage;
  late ApiClient client;
  late FakeAuthApi api;
  late AuthController controller;

  setUp(() {
    storage = InMemoryTokenStorage();
    client = ApiClient(storage: storage);
    api = FakeAuthApi(client);
    controller = AuthController(api, storage, client);
  });

  test('login réussi → AuthAuthenticated, tokens + session persistés', () async {
    api.loginResponse = _jwt();

    final ok = await controller.login('ceep@ocp.ma', 'Password123!');

    expect(ok, true);
    expect(controller.state, isA<AuthAuthenticated>());
    final session = (controller.state as AuthAuthenticated).session;
    expect(session.roles, ['CEEP']);
    expect(session.hasPermission('CREATE_AT'), true);
    expect(storage.access, 'access-1');
    expect(storage.refresh, 'refresh-1');
    expect(storage.session?['roles'], ['CEEP']);
  });

  test('login en échec (message métier 401) → AuthError, aucun token stocké', () async {
    api.loginError = DioException(
      requestOptions: RequestOptions(),
      type: DioExceptionType.badResponse,
      response: Response(
        requestOptions: RequestOptions(),
        statusCode: 401,
        data: {'message': 'Identifiants invalides ou compte verrouillé'},
      ),
    );

    final ok = await controller.login('ceep@ocp.ma', 'wrong');

    expect(ok, false);
    final state = controller.state;
    expect(state, isA<AuthError>());
    expect((state as AuthError).failure.message, 'Identifiants invalides ou compte verrouillé');
    expect(storage.access, isNull);
  });

  test('logout → révocation appelée, tokens et session purgés, non authentifié',
      () async {
    api.loginResponse = _jwt();
    await controller.login('ceep@ocp.ma', 'Password123!');

    await controller.logout();

    expect(api.logoutCalled, true);
    expect(controller.state, isA<AuthUnauthenticated>());
    expect(storage.access, isNull);
    expect(storage.session, isNull);
  });

  test('expiration de session (refresh KO) → déconnexion + purge des tokens',
      () async {
    api.loginResponse = _jwt();
    await controller.login('ceep@ocp.ma', 'Password123!');
    expect(controller.state, isA<AuthAuthenticated>());

    client.onSessionExpired!(); // déclenché par l'intercepteur 401 du client HTTP

    expect(controller.state, isA<AuthUnauthenticated>());
    expect(storage.access, isNull);
    expect(storage.refresh, isNull);
  });

  test('restoreSession : tokens + session stockés → AuthAuthenticated', () async {
    api.loginResponse = _jwt();
    await controller.login('ceep@ocp.ma', 'Password123!');
    controller.state = const AuthUnauthenticated(); // simule un redémarrage

    await controller.restoreSession();

    expect(controller.state, isA<AuthAuthenticated>());
    final session = (controller.state as AuthAuthenticated).session;
    expect(session.utilisateur.email, 'ceep@ocp.ma'); // profil rafraîchi via /me
    expect(session.roles, ['CEEP']);
  });

  test('restoreSession sans tokens → AuthUnauthenticated', () async {
    await controller.restoreSession();
    expect(controller.state, isA<AuthUnauthenticated>());
  });

  test('restoreSession hors-ligne : la session locale est conservée', () async {
    api.loginResponse = _jwt();
    await controller.login('ceep@ocp.ma', 'Password123!');
    controller.state = const AuthUnauthenticated();
    // /me échoue réseau (mais pas 401) → session locale conservée :
    final offlineApi = _OfflineMeApi(client);

    final offlineController = AuthController(offlineApi, storage, client);

    await offlineController.restoreSession();

    expect(offlineController.state, isA<AuthAuthenticated>());
  });
}

class _OfflineMeApi extends FakeAuthApi {
  _OfflineMeApi(super.client);

  @override
  Future<Utilisateur> me() async {
    throw DioException(
      requestOptions: RequestOptions(),
      type: DioExceptionType.connectionError,
    );
  }
}
