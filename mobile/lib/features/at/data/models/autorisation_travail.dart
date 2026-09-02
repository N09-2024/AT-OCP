/// Modèle AutorisationTravail - reflète AutorisationTravailResponse du backend.
/// Statuts = valeurs EXACTES de l'enum StatutAT (22 valeurs).
/// AUCUNE notion "Installation" (supprimée du projet).
library;

class StatutAt {
  // Valeurs de l'enum backend StatutAT - ne pas renommer.
  static const String brouillon = 'BROUILLON';
  static const String classificationEffectuee = 'CLASSIFICATION_EFFECTUEE';
  static const String demandeCreee = 'DEMANDE_CREEE';
  static const String enVisiteRedaction = 'EN_VISITE_REDACTION';
  static const String visiteRealisee = 'VISITE_REALISEE';
  static const String atRedigee = 'AT_REDIGEE';
  static const String soumise = 'SOUMISE';
  static const String validee = 'VALIDEE';
  static const String atValidee = 'AT_VALIDEE';
  static const String enCours = 'EN_COURS';
  static const String interventionEnCours = 'INTERVENTION_EN_COURS';
  static const String enReconduction = 'EN_RECONDUCTION';
  static const String atReconduite = 'AT_RECONDUITE';
  static const String renouvelee = 'RENOUVELEE';
  static const String declareeTerminee = 'DECLAREE_TERMINEE';
  static const String finTravauxDeclaree = 'FIN_TRAVAUX_DECLAREE';
  static const String receptionnees = 'RECEPTIONEES';
  static const String travauxReceptiones = 'TRAVAUX_RECEPTIONES';
  static const String archivee = 'ARCHIVEE';
  static const String rejetee = 'REJETEE';
  static const String annulee = 'ANNULEE';

  /// Libellés français pour l'affichage.
  static const Map<String, String> libelles = {
    brouillon: 'Brouillon',
    classificationEffectuee: 'Classifiée',
    demandeCreee: 'Demande créée',
    enVisiteRedaction: 'En visite / rédaction',
    visiteRealisee: 'Visite réalisée',
    atRedigee: 'AT rédigée',
    soumise: 'Soumise',
    validee: 'Validée',
    atValidee: 'AT validée',
    enCours: 'En cours',
    interventionEnCours: 'Intervention en cours',
    enReconduction: 'En reconduction',
    atReconduite: 'AT reconduite',
    renouvelee: 'Renouvelée',
    declareeTerminee: 'Fin déclarée',
    finTravauxDeclaree: 'Travaux finis déclarés',
    receptionnees: 'Réceptionnée',
    travauxReceptiones: 'Travaux réceptionnés',
    archivee: 'Archivée',
    rejetee: 'Rejetée',
    annulee: 'Annulée',
  };

  static String libelle(String? statut) =>
      statut == null ? '-' : libelles[statut] ?? statut;
}

class EtatVerrou {
  static const String libre = 'LIBRE';
  static const String enCoursEdition = 'EN_COURS_EDITION';
}

class TypeDocumentSource {
  static const String di = 'DI';
  static const String ot = 'OT';
  static const String bt = 'BT';
}

class AutorisationTravail {
  final String id;
  final String? numero;
  final int? version;
  final String? objet;
  final String? descriptionTravaux;

  final DateTime? dateDebut;
  final DateTime? dateFin;
  final String? heureDebut; // "HH:mm:ss"
  final String? heureFin;

  final String? statut;
  final String? etatVerrou;

  final DateTime? dateCreation;
  final DateTime? dateModification;

  final String? proprietaireBrouillonId;
  final String? proprietaireBrouillonNomComplet;

  final String? zoneProprietaireId;
  final String? zoneProprietaireNom;
  final String? zoneExecutanteId;
  final String? zoneExecutanteNom;

  final DateTime? datePriseVerrou;
  final DateTime? dateLiberationVerrou;

  final String? typeDocumentSource; // DI | OT | BT
  final String? documentSourceId;
  final String? documentSourceNumero;

  final String? servicesIntervenants;
  final String? entreprisesIntervenantes;
  final String? mesuresSecuriteExecutant;

  final String? g1NomCeep;
  final String? g1NomCeee;
  final DateTime? dateReceptionCeee;

  final double? latitude;
  final double? longitude;
  final String? visiteCommentaire;
  final bool? visiteEffectuee;
  final String? photoPath;

  final List<String> risquesIds;
  final List<String> mesuresIds;
  final List<String> episIds;
  final List<String> moyensAccesIds;
  final List<String> permisIds;

  /// Export PDF conditionnel - décide de l'affichage du bouton PDF.
  final bool? exportPdfAutorise;
  final List<String> exportPdfMotifsRefus;

  const AutorisationTravail({
    required this.id,
    this.numero,
    this.version,
    this.objet,
    this.descriptionTravaux,
    this.dateDebut,
    this.dateFin,
    this.heureDebut,
    this.heureFin,
    this.statut,
    this.etatVerrou,
    this.dateCreation,
    this.dateModification,
    this.proprietaireBrouillonId,
    this.proprietaireBrouillonNomComplet,
    this.zoneProprietaireId,
    this.zoneProprietaireNom,
    this.zoneExecutanteId,
    this.zoneExecutanteNom,
    this.datePriseVerrou,
    this.dateLiberationVerrou,
    this.typeDocumentSource,
    this.documentSourceId,
    this.documentSourceNumero,
    this.servicesIntervenants,
    this.entreprisesIntervenantes,
    this.mesuresSecuriteExecutant,
    this.g1NomCeep,
    this.g1NomCeee,
    this.dateReceptionCeee,
    this.latitude,
    this.longitude,
    this.visiteCommentaire,
    this.visiteEffectuee,
    this.photoPath,
    this.risquesIds = const [],
    this.mesuresIds = const [],
    this.episIds = const [],
    this.moyensAccesIds = const [],
    this.permisIds = const [],
    this.exportPdfAutorise,
    this.exportPdfMotifsRefus = const [],
  });

  bool get verrouilleParAutre =>
      etatVerrou == EtatVerrou.enCoursEdition;

  factory AutorisationTravail.fromJson(Map<String, dynamic> json) {
    DateTime? parseDate(dynamic v) =>
        v == null ? null : DateTime.tryParse(v.toString());

    return AutorisationTravail(
      id: json['id'] as String,
      numero: json['numero'] as String?,
      version: json['version'] as int?,
      objet: json['objet'] as String?,
      descriptionTravaux: json['descriptionTravaux'] as String?,
      dateDebut: parseDate(json['dateDebut']),
      dateFin: parseDate(json['dateFin']),
      heureDebut: json['heureDebut'] as String?,
      heureFin: json['heureFin'] as String?,
      statut: json['statut'] as String?,
      etatVerrou: json['etatVerrou'] as String?,
      dateCreation: parseDate(json['dateCreation']),
      dateModification: parseDate(json['dateModification']),
      proprietaireBrouillonId: json['proprietaireBrouillonId'] as String?,
      proprietaireBrouillonNomComplet: json['proprietaireBrouillonNomComplet'] as String?,
      zoneProprietaireId: json['zoneProprietaireId'] as String?,
      zoneProprietaireNom: json['zoneProprietaireNom'] as String?,
      zoneExecutanteId: json['zoneExecutanteId'] as String?,
      zoneExecutanteNom: json['zoneExecutanteNom'] as String?,
      datePriseVerrou: parseDate(json['datePriseVerrou']),
      dateLiberationVerrou: parseDate(json['dateLiberationVerrou']),
      typeDocumentSource: json['typeDocumentSource'] as String?,
      documentSourceId: json['documentSourceId'] as String?,
      documentSourceNumero: json['documentSourceNumero'] as String?,
      servicesIntervenants: json['servicesIntervenants'] as String?,
      entreprisesIntervenantes: json['entreprisesIntervenantes'] as String?,
      mesuresSecuriteExecutant: json['mesuresSecuriteExecutant'] as String?,
      g1NomCeep: json['g1NomCeep'] as String?,
      g1NomCeee: json['g1NomCeee'] as String?,
      dateReceptionCeee: parseDate(json['dateReceptionCeee']),
      latitude: (json['latitude'] as num?)?.toDouble(),
      longitude: (json['longitude'] as num?)?.toDouble(),
      visiteCommentaire: json['visiteCommentaire'] as String?,
      visiteEffectuee: json['visiteEffectuee'] as bool?,
      photoPath: json['photoPath'] as String?,
      risquesIds: _stringList(json['risquesIds']),
      mesuresIds: _stringList(json['mesuresIds']),
      episIds: _stringList(json['episIds']),
      moyensAccesIds: _stringList(json['moyensAccesIds']),
      permisIds: _stringList(json['permisIds']),
      exportPdfAutorise: json['exportPdfAutorise'] as bool?,
      exportPdfMotifsRefus: _stringList(json['exportPdfMotifsRefus']),
    );
  }

  static List<String> _stringList(dynamic v) =>
      v == null ? const [] : (v as List<dynamic>).map((e) => e.toString()).toList();
}

/// Page Spring (GET /autorisations-travail) : { content: [...], totalElements,
/// totalPages, number, size, ... }.
class Page<T> {
  final List<T> content;
  final int totalElements;
  final int totalPages;
  final int number;
  final int size;
  final bool last;

  const Page({
    required this.content,
    required this.totalElements,
    required this.totalPages,
    required this.number,
    required this.size,
    required this.last,
  });

  factory Page.fromJson(
    Map<String, dynamic> json,
    T Function(Map<String, dynamic>) fromJsonT,
  ) =>
      Page(
        content: (json['content'] as List<dynamic>? ?? [])
            .map((e) => fromJsonT(e as Map<String, dynamic>))
            .toList(),
        totalElements: json['totalElements'] as int? ?? 0,
        totalPages: json['totalPages'] as int? ?? 0,
        number: json['number'] as int? ?? 0,
        size: json['size'] as int? ?? 20,
        last: json['last'] as bool? ?? true,
      );
}
