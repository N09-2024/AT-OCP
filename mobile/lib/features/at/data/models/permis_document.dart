/// Modèle pour les documents de permis complémentaires soumis à l'analyse IA (Section E).
library;

enum StatutPermisDocument {
  enAttenteUpload,
  enAttenteAnalyse,
  valide,
  rejete,
}

class PermisDocument {
  final String id;
  final String atId;
  final String typePermisAttendu;
  final String? fileOriginalName;
  final String? fileContentType;
  final StatutPermisDocument statut;
  final DateTime? dateUpload;
  final DateTime? dateAnalyse;
  final String? typeExtrait;
  final String? dateDebutExtrait;
  final String? dateFinExtrait;
  final String? responsablesExtraits;
  final String? motifRejet;
  final double? scoreConfiance;
  final String? commentaireIA;

  const PermisDocument({
    required this.id,
    required this.atId,
    required this.typePermisAttendu,
    this.fileOriginalName,
    this.fileContentType,
    this.statut = StatutPermisDocument.enAttenteUpload,
    this.dateUpload,
    this.dateAnalyse,
    this.typeExtrait,
    this.dateDebutExtrait,
    this.dateFinExtrait,
    this.responsablesExtraits,
    this.motifRejet,
    this.scoreConfiance,
    this.commentaireIA,
  });

  static StatutPermisDocument _parseStatut(String? s) {
    return switch (s?.toUpperCase()) {
      'VALIDE' => StatutPermisDocument.valide,
      'REJETE' => StatutPermisDocument.rejete,
      'EN_ATTENTE_ANALYSE' => StatutPermisDocument.enAttenteAnalyse,
      _ => StatutPermisDocument.enAttenteUpload,
    };
  }

  factory PermisDocument.fromJson(Map<String, dynamic> json) {
    DateTime? parseDate(dynamic v) =>
        v == null ? null : DateTime.tryParse(v.toString());

    return PermisDocument(
      id: json['id'] as String? ?? '',
      atId: json['atId'] as String? ?? '',
      typePermisAttendu: json['typePermisAttendu'] as String? ?? '',
      fileOriginalName: json['fileOriginalName'] as String?,
      fileContentType: json['fileContentType'] as String?,
      statut: _parseStatut(json['statut'] as String?),
      dateUpload: parseDate(json['dateUpload']),
      dateAnalyse: parseDate(json['dateAnalyse']),
      typeExtrait: json['typeExtrait'] as String?,
      dateDebutExtrait: json['dateDebutExtrait'] as String?,
      dateFinExtrait: json['dateFinExtrait'] as String?,
      responsablesExtraits: json['responsablesExtraits'] as String?,
      motifRejet: json['motifRejet'] as String?,
      scoreConfiance: (json['scoreConfiance'] as num?)?.toDouble(),
      commentaireIA: json['commentaireIA'] as String?,
    );
  }
}
