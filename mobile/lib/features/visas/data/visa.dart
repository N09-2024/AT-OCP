/// Modèle Visa — reflète VisaResponse du backend.
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

  static String libelle(String? s) => s == null ? '—' : libelles[s] ?? s;
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
      );
}
