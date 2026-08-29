/// Client HTTP centralisé (Dio).
///
/// 1. Ajoute automatiquement le JWT (Authorization: Bearer).
/// 2. Détecte les 401 → appelle /auth/refresh-token → rejoue la requête initiale.
/// 3. Déconnecte (callback) si le refresh échoue.
/// 4. Évite les boucles infinies de refresh (verrou + marqueur interne).
///
/// Endpoints réels du backend : POST /api/auth/login, /api/auth/refresh-token (cf. rapport Phase 1).
library;

import 'package:dio/dio.dart';
import '../config/app_config.dart';
import '../storage/token_storage.dart';
import '../storage/secure_token_storage.dart';

typedef OnSessionExpired = void Function();

class ApiClient {
  late final Dio dio;
  final TokenStorage tokenStorage;

  /// Appelé quand le refresh a échoué : l'UI doit déconnecter et retourner au login.
  OnSessionExpired? onSessionExpired;

  /// Verrou anti-boucles : un seul refresh à la fois.
  Future<String?>? _refreshingFuture;

  ApiClient({TokenStorage? storage, Dio? dioOverride})
      : tokenStorage = storage ?? SecureTokenStorage() {
    dio = dioOverride ?? Dio(BaseOptions(
          baseUrl: '${AppConfig.baseUrl}${AppConfig.apiPrefix}',
          connectTimeout: AppConfig.requestTimeout,
          receiveTimeout: AppConfig.requestTimeout,
          sendTimeout: AppConfig.requestTimeout,
          headers: {'Accept': 'application/json'},
        ),);

    dio.interceptors.add(InterceptorsWrapper(
      onRequest: (options, handler) async {
        // Pas de JWT sur /auth/login et /auth/refresh-token.
        final isAuthPath = options.path.startsWith('/auth/');
        if (!isAuthPath) {
          final token = await tokenStorage.readAccessToken();
          if (token != null && token.isNotEmpty) {
            options.headers['Authorization'] = 'Bearer $token';
          }
        }
        handler.next(options);
      },
      onError: (error, handler) async {
        final status = error.response?.statusCode;
        final isAuthCall = error.requestOptions.path.startsWith('/auth/');
        final alreadyRetried = error.requestOptions.extra['__retried'] == true;

        if (status != 401 || isAuthCall || alreadyRetried) {
          return handler.next(error);
        }

        // 401 sur une requête métier → tenter le refresh UNE seule fois.
        final newToken = await _refreshToken();
        if (newToken == null) {
          onSessionExpired?.call();
          return handler.next(error);
        }

        try {
          final opts = error.requestOptions;
          opts.extra['__retried'] = true;
          opts.headers['Authorization'] = 'Bearer $newToken';
          final response = await dio.fetch(opts);
          return handler.resolve(response);
        } on DioException catch (e) {
          // Le rejeu a encore échoué en 401 → session vraiment expirée.
          if (e.response?.statusCode == 401) onSessionExpired?.call();
          return handler.next(e);
        }
      },
    ),);
  }

  /// Rafraîchit l'access token ; retourne le nouveau token ou null si échec.
  /// Les appels concurrents partagent le même refresh (verrou).
  Future<String?> _refreshToken() {
    return _refreshingFuture ??= _doRefresh().whenComplete(() => _refreshingFuture = null);
  }

  Future<String?> _doRefresh() async {
    try {
      final refreshToken = await tokenStorage.readRefreshToken();
      if (refreshToken == null || refreshToken.isEmpty) return null;

      // POST /api/auth/refresh-token { refreshToken } → { accessToken, refreshToken }
      final response = await dio.post(
        '/auth/refresh-token',
        data: {'refreshToken': refreshToken},
      );
      final data = response.data as Map<String, dynamic>;
      final newAccess = data['accessToken'] as String?;
      final newRefresh = data['refreshToken'] as String?;
      if (newAccess == null || newAccess.isEmpty) return null;

      await tokenStorage.writeTokens(
        accessToken: newAccess,
        refreshToken: newRefresh ?? refreshToken,
      );
      return newAccess;
    } catch (_) {
      return null;
    }
  }

  // ------------------------------------------------------------------
  // Méthodes GET/POST/PUT/DELETE/PATCH + helpers multipart et octets.
  // ------------------------------------------------------------------

  Future<Response<T>> get<T>(
    String path, {
    Map<String, dynamic>? queryParameters,
    Options? options,
  }) =>
      dio.get<T>(path, queryParameters: queryParameters, options: options);

  Future<Response<T>> post<T>(String path,
          {Object? body, Map<String, dynamic>? queryParameters, Options? options,}) =>
      dio.post<T>(path, data: body, queryParameters: queryParameters, options: options);

  Future<Response<T>> put<T>(String path,
          {Object? body, Map<String, dynamic>? queryParameters, Options? options,}) =>
      dio.put<T>(path, data: body, queryParameters: queryParameters, options: options);

  Future<Response<T>> patch<T>(String path, {Object? body, Options? options}) =>
      dio.patch<T>(path, data: body, options: options);

  Future<Response<T>> delete<T>(String path, {Object? body, Options? options}) =>
      dio.delete<T>(path, data: body, options: options);

  /// Pour les réponses binaires (PDF, images).
  Future<Response<List<int>>> downloadBytes(String path, {Map<String, dynamic>? queryParameters}) {
    return dio.get<List<int>>(
      path,
      queryParameters: queryParameters,
      options: Options(responseType: ResponseType.bytes),
    );
  }
}
