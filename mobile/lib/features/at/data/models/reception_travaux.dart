/// Modèle ReceptionTravaux pour mobile.
/// Reflète ReceptionTravauxResponse et ReceptionTravauxRequest du backend.
library;

class ReceptionTravaux {
  final String id;
  final String autorisationTravailId;
  final DateTime? dateDebutTravauxReelle;
  final DateTime? dateFinTravauxReelle;
  final bool travauxConformes;
  final bool zoneNettoyee;
  final bool consignationRetiree;
  final bool equipementRemisEnService;
  final bool installationRemiseEnEtat;
  final bool essaisEffectues;
  final bool essaisConformes;
  final String? travauxRealises;
  final String? commentaireResponsable;
  final String? signatureResponsable;
  final DateTime? dateSignature;
  final bool cloturee;

  const ReceptionTravaux({
    required this.id,
    required this.autorisationTravailId,
    this.dateDebutTravauxReelle,
    this.dateFinTravauxReelle,
    this.travauxConformes = true,
    this.zoneNettoyee = true,
    this.consignationRetiree = true,
    this.equipementRemisEnService = true,
    this.installationRemiseEnEtat = true,
    this.essaisEffectues = true,
    this.essaisConformes = true,
    this.travauxRealises,
    this.commentaireResponsable,
    this.signatureResponsable,
    this.dateSignature,
    this.cloturee = false,
  });

  bool get isChecklistComplete =>
      travauxConformes &&
      zoneNettoyee &&
      consignationRetiree &&
      equipementRemisEnService &&
      installationRemiseEnEtat &&
      essaisEffectues &&
      essaisConformes;

  factory ReceptionTravaux.fromJson(Map<String, dynamic> json) {
    DateTime? parseDate(dynamic v) =>
        v == null ? null : DateTime.tryParse(v.toString());

    return ReceptionTravaux(
      id: json['id'] as String,
      autorisationTravailId: json['autorisationTravailId'] as String? ??
          (json['autorisationTravail'] is Map ? json['autorisationTravail']['id'] as String : ''),
      dateDebutTravauxReelle: parseDate(json['dateDebutTravauxReelle']),
      dateFinTravauxReelle: parseDate(json['dateFinTravauxReelle']),
      travauxConformes: json['travauxConformes'] as bool? ?? true,
      zoneNettoyee: json['zoneNettoyee'] as bool? ?? true,
      consignationRetiree: json['consignationRetiree'] as bool? ?? true,
      equipementRemisEnService: json['equipementRemisEnService'] as bool? ?? true,
      installationRemiseEnEtat: json['installationRemiseEnEtat'] as bool? ?? true,
      essaisEffectues: json['essaisEffectues'] as bool? ?? true,
      essaisConformes: json['essaisConformes'] as bool? ?? true,
      travauxRealises: json['travauxRealises'] as String?,
      commentaireResponsable: json['commentaireResponsable'] as String?,
      signatureResponsable: json['signatureResponsable'] as String?,
      dateSignature: parseDate(json['dateSignature']),
      cloturee: json['cloturee'] as bool? ?? false,
    );
  }

  Map<String, dynamic> toJson() => {
        'autorisationTravailId': autorisationTravailId,
        if (dateDebutTravauxReelle != null)
          'dateDebutTravauxReelle': _isoDate(dateDebutTravauxReelle!),
        if (dateFinTravauxReelle != null)
          'dateFinTravauxReelle': _isoDate(dateFinTravauxReelle!),
        'travauxConformes': travauxConformes,
        'zoneNettoyee': zoneNettoyee,
        'consignationRetiree': consignationRetiree,
        'equipementRemisEnService': equipementRemisEnService,
        'installationRemiseEnEtat': installationRemiseEnEtat,
        'essaisEffectues': essaisEffectues,
        'essaisConformes': essaisConformes,
        'travauxRealises': travauxRealises ?? '',
        'commentaireResponsable': commentaireResponsable ?? '',
      };

  static String _isoDate(DateTime d) =>
      '${d.year.toString().padLeft(4, '0')}-${d.month.toString().padLeft(2, '0')}-${d.day.toString().padLeft(2, '0')}';
}
