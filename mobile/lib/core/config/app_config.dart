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
  static String get baseUrl {
    const raw = String.fromEnvironment('API_BASE_URL');
    if (raw.isNotEmpty) return raw;
    if (kIsWeb) {
      // Sur le web, chemin relatif pour s'appuyer sur le reverse proxy nginx ou dev server
      return '';
    }
    switch (environment) {
      case AppEnv.prod:
        return 'https://at-ocp.ocp.ma';
      case AppEnv.staging:
        return 'https://staging-at-ocp.ocp.ma';
      case AppEnv.dev:
        // 10.0.2.2 : hote local vu depuis l'emulateur Android.
        // iOS simulateur / desktop : utiliser http://localhost:8080
        return defaultTargetPlatform == TargetPlatform.android
            ? 'http://10.0.2.2:8080'
            : 'http://localhost:8080';
    }
  }

  static const String apiPrefix = '/api';

  static bool get isDev => environment == AppEnv.dev;

  /// Duree par defaut des appels HTTP.
  static const Duration requestTimeout = Duration(seconds: 30);
}
