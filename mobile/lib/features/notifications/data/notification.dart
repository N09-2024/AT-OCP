/// Modèle Notification — reflète NotificationResponse du backend.
library;

class Notification {
  final String id;
  final String? titre;
  final String? message;
  final DateTime? dateCreation;
  final DateTime? dateLecture;
  final bool lu;
  final String? type;
  final String? lien;
  final String? utilisateurId;

  const Notification({
    required this.id,
    this.titre,
    this.message,
    this.dateCreation,
    this.dateLecture,
    required this.lu,
    this.type,
    this.lien,
    this.utilisateurId,
  });

  factory Notification.fromJson(Map<String, dynamic> json) => Notification(
        id: json['id'] as String,
        titre: json['titre'] as String?,
        message: json['message'] as String?,
        dateCreation:
            json['dateCreation'] == null ? null : DateTime.tryParse(json['dateCreation'].toString()),
        dateLecture:
            json['dateLecture'] == null ? null : DateTime.tryParse(json['dateLecture'].toString()),
        lu: json['lu'] as bool? ?? false,
        type: json['type'] as String?,
        lien: json['lien'] as String?,
        utilisateurId: json['utilisateurId'] as String?,
      );
}
