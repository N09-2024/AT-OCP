/// Erreurs applicatives : les exceptions techniques (Dio, JSON...) sont converties
/// en [Failure] compréhensibles par l'UI. Jamais de stack trace affichée à l'utilisateur.
library;

sealed class Failure {
  final String message;
  const Failure(this.message);
}

/// Erreur métier renvoyée par le backend (message exploitable, affiché tel quel).
class ApiFailure extends Failure {
  final int? statusCode;
  const ApiFailure(super.message, {this.statusCode});
}

/// Absence de réseau / serveur injoignable.
class NetworkFailure extends Failure {
  const NetworkFailure() : super('Aucune connexion réseau. Vérifiez votre connexion et réessayez.');
}

/// Délai dépassé.
class TimeoutFailure extends Failure {
  const TimeoutFailure() : super('Le serveur ne répond pas. Réessayez plus tard.');
}

/// Erreur inattendue (parsing, bug...).
class UnexpectedFailure extends Failure {
  const UnexpectedFailure() : super('Une erreur inattendue est survenue. Réessayez.');
}

/// Session expirée / refresh impossible → déconnexion.
class SessionExpiredFailure extends Failure {
  const SessionExpiredFailure() : super('Session expirée. Veuillez vous reconnecter.');
}
