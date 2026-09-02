/// Modèle Dashboard - reflète DashboardDataResponse du backend.
/// NB : AtSummaryDto possede un champ texte "installation" (residu historique
/// rempli avec le nom de l'equipement) - conserve tel quel pour compatibilite,
/// jamais utilise comme entite.
library;

class DashboardData {
  final KpiStats kpis;
  final Map<String, int> statusDistribution;
  final List<AtSummary> recentAutorisations;

  const DashboardData({
    required this.kpis,
    required this.statusDistribution,
    required this.recentAutorisations,
  });

  factory DashboardData.fromJson(Map<String, dynamic> json) => DashboardData(
        kpis: KpiStats.fromJson(json['kpis'] as Map<String, dynamic>? ?? {}),
        statusDistribution: (json['statusDistribution'] as Map<String, dynamic>? ?? {})
            .map((k, v) => MapEntry(k, v is int ? v : int.tryParse(v.toString()) ?? 0)),
        recentAutorisations: (json['recentAutorisations'] as List<dynamic>? ?? [])
            .map((e) => AtSummary.fromJson(e as Map<String, dynamic>))
            .toList(),
      );
}

class KpiStats {
  final int autorisationsEnCours;
  final int visasEnAttente;
  final int permisActifs;
  final int receptionsEnAttente;
  final int totalArchives;

  const KpiStats({
    this.autorisationsEnCours = 0,
    this.visasEnAttente = 0,
    this.permisActifs = 0,
    this.receptionsEnAttente = 0,
    this.totalArchives = 0,
  });

  factory KpiStats.fromJson(Map<String, dynamic> json) => KpiStats(
        autorisationsEnCours: json['autorisationsEnCours'] as int? ?? 0,
        visasEnAttente: json['visasEnAttente'] as int? ?? 0,
        permisActifs: json['permisActifs'] as int? ?? 0,
        receptionsEnAttente: json['receptionsEnAttente'] as int? ?? 0,
        totalArchives: json['totalArchives'] as int? ?? 0,
      );
}

class AtSummary {
  final String id;
  final String? titre;
  final String? statut;
  final String? echeance;

  const AtSummary({required this.id, this.titre, this.statut, this.echeance});

  factory AtSummary.fromJson(Map<String, dynamic> json) => AtSummary(
        id: json['id'] as String,
        titre: json['titre'] as String?,
        statut: json['statut'] as String?,
        echeance: json['echeance'] as String?,
      );
}
