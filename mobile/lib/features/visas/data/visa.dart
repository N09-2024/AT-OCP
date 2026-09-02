/// Modèle Visa - reflète VisaResponse du backend.
/// Le hash de signature n'est jamais exposé par l'API (seul signaturePresente).
library;

class StatutVisa {
  static const String enAttente = 'EN_ATTENTE';
  static const String valide = 'VALIDE';
  static const String refuse = 'REFUSE';
  static const String validation = 'VALIDATION';
  static const String signature = 'SIGNATURE';

  static const Map<String, String> libelles = {
    enAttente: 'En attente',
    valide: 'Validé',
    refuse: 'Refusé',
    validation: 'Validation',
    signature: 'Signature',
  };

  static String libelle(String? s) => s == null ? '-' : libelles[s] ?? s;
}

class VisaUtilisateur {
  final String? id;
  final String? nomComplet;
  final List<String> roles;

  const VisaUtilisateur({this.id, this.nomComplet, this.roles = const []});

  factory VisaUtilisateur.fromJson(Map<String, dynamic> json) {
    final rawRoles = json['roles'];
    List<String> roles = [];
    if (rawRoles is List) {
      for (final r in rawRoles) {
        if (r is String) {
          roles.add(r.toUpperCase());
        } else if (r is Map) {
          final nom = (r['nom'] ?? r['name'] ?? '').toString().toUpperCase();
          if (nom.isNotEmpty) roles.add(nom);
        }
      }
    }
    return VisaUtilisateur(
      id: json['id']?.toString(),
      nomComplet: json['nomComplet']?.toString() ?? json['nom']?.toString(),
      roles: roles,
    );
  }
}

class Visa {
  final String id;
  final DateTime? dateVisa;
  final DateTime? dateSignature;
  final String? statut;
  final String? commentaire;
  final int? ordre;
  final bool signaturePresente;
  final String? adresseIP;
  final String? utilisateurId;
  final String? utilisateurNomComplet;
  final String? autorisationTravailId;

  /// Rôle direct du visa (ex: "CEEE", "HCEP") - peut venir du backend
  final String? role;

  /// Objet utilisateur signataire enrichi (avec ses rôles)
  final VisaUtilisateur? utilisateur;

  const Visa({
    required this.id,
    this.dateVisa,
    this.dateSignature,
    this.statut,
    this.commentaire,
    this.ordre,
    required this.signaturePresente,
    this.adresseIP,
    this.utilisateurId,
    this.utilisateurNomComplet,
    this.autorisationTravailId,
    this.role,
    this.utilisateur,
  });

  factory Visa.fromJson(Map<String, dynamic> json) => Visa(
        id: json['id'] as String,
        dateVisa: json['dateVisa'] == null ? null : DateTime.tryParse(json['dateVisa'].toString()),
        dateSignature: json['dateSignature'] == null
            ? null
            : DateTime.tryParse(json['dateSignature'].toString()),
        statut: json['statut'] as String?,
        commentaire: json['commentaire'] as String?,
        ordre: json['ordre'] as int?,
        signaturePresente: json['signaturePresente'] as bool? ?? false,
        adresseIP: json['adresseIP'] as String?,
        utilisateurId: json['utilisateurId'] as String?,
        utilisateurNomComplet: json['utilisateurNomComplet'] as String?,
        autorisationTravailId: json['autorisationTravailId'] as String?,
        role: (json['role'] ?? json['roleSignataire'] ?? json['utilisateurRole'])?.toString(),
        utilisateur: json['utilisateur'] is Map<String, dynamic>
            ? VisaUtilisateur.fromJson(json['utilisateur'] as Map<String, dynamic>)
            : null,
      );
}
