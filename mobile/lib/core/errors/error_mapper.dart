/// Conversion des exceptions Dio en Failure exploitables par l'UI.
library;

import 'package:dio/dio.dart';
import 'failures.dart';

Failure mapDioError(Object error) {
  if (error is SessionExpiredFailure) return error;
  if (error is Failure) return error;

  if (error is DioException) {
    switch (error.type) {
      case DioExceptionType.connectionError:
      case DioExceptionType.connectionTimeout:
        return const NetworkFailure();
      case DioExceptionType.sendTimeout:
      case DioExceptionType.receiveTimeout:
        return const TimeoutFailure();
      case DioExceptionType.badResponse:
        return _fromResponse(error.response);
      case DioExceptionType.cancel:
        return const UnexpectedFailure();
      default:
        if (error.error != null && error.error is Failure) return error.error as Failure;
        return const NetworkFailure();
    }
  }
  return const UnexpectedFailure();
}

Failure _fromResponse(Response? response) {
  if (response == null) return const UnexpectedFailure();
  final status = response.statusCode ?? 500;
  final message = _extractMessage(response);

  switch (status) {
    case 400:
      return ApiFailure(message ?? 'Requête invalide. Vérifiez les informations saisies.', statusCode: 400);
    case 401:
      // Le backend envoie des messages utiles en 401 (ex. login :
      // "Identifiants invalides ou compte verrouillé") — les conserver.
      return ApiFailure(message ?? 'Non authentifié. Veuillez vous reconnecter.', statusCode: 401);
    case 403:
      return const ApiFailure("Vous n'avez pas les droits nécessaires pour cette action.", statusCode: 403);
    case 404:
      return const ApiFailure('Élément introuvable.', statusCode: 404);
    case 409:
      return ApiFailure(message ?? 'Conflit : l\'opération ne peut pas être effectuée.', statusCode: 409);
    case 422:
      return ApiFailure(message ?? 'Données invalides.', statusCode: 422);
    default:
      return ApiFailure(message ?? 'Erreur serveur ($status). Réessayez plus tard.', statusCode: status);
  }
}

/// Extrait le message métier du backend si présent.
/// Formats gérés : {message: "..."}, {error: "..."}, "texte brut".
String? _extractMessage(Response response) {
  final data = response.data;
  if (data is Map<String, dynamic>) {
    final msg = data['message'] ?? data['error'];
    if (msg is String && msg.trim().isNotEmpty) return msg;
  } else if (data is String && data.trim().isNotEmpty && !data.trim().startsWith('<')) {
    return data;
  }
  return null;
}
