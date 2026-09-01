/// Configuration centralisee des environnements.
///
/// L'URL du backend est definie une seule fois ici, choisie via --dart-define :
///   flutter run --dart-define=ENV=dev     (emulateur Android : 10.0.2.2 = localhost hote)
///   flutter run --dart-define=ENV=staging
///   flutter run --dart-define=ENV=prod
///
/// Ne jamais hardcoder une URL de backend ailleurs dans l'application.
library;

import 'package:flutter/foundation.dart';

enum AppEnv { dev, staging, prod }

class AppConfig {
  static const String env = String.fromEnvironment('ENV', defaultValue: 'dev');

  static AppEnv get environment {
    switch (env) {
      case 'staging':
        return AppEnv.staging;
      case 'prod':
        return AppEnv.prod;
      default:
        return AppEnv.dev;
    }
  }

  /// Base URL du backend Spring Boot (sans le suffixe /api, ajoute par ApiClient).
  ///
  /// Toujours ABSOLUE : Flutter Web n'a pas de proxy en dev (`flutter run -d chrome`),
  /// une URL relative tomberait sur le serveur de dev Flutter (404). Le backend
  /// autorise CORS * (SecurityConfig).
  static String get baseUrl {
    const raw = String.fromEnvironment('API_BASE_URL');
    if (raw.isNotEmpty) return raw;
    switch (environment) {
      case AppEnv.prod:
        return 'https://at-ocp.ocp.ma';
      case AppEnv.staging:
        return 'https://staging-at-ocp.ocp.ma';
      case AppEnv.dev:
        // Émulateur Android : 10.0.2.2 = localhost de l'hôte.
        // Web (Chrome/Edge) et desktop : localhost direct.
        return kIsWeb || defaultTargetPlatform != TargetPlatform.android
            ? 'http://localhost:8080'
            : 'http://10.0.2.2:8080';
    }
  }

  static const String apiPrefix = '/api';

  static bool get isDev => environment == AppEnv.dev;

  /// Duree par defaut des appels HTTP.
  static const Duration requestTimeout = Duration(seconds: 30);
}
