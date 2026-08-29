/// Référentiels backend : Zone, Service, Equipement, EntrepriseExterne,
/// Risque, MesurePreparation, EPI, MoyenAcces, TypePermis.
/// Chaque modèle reflète les champs des Response DTO réels.
library;

class Zone {
  final String id;
  final String? nomZone;
  final String? codeZone;
  final String? descriptionZone;

  const Zone({required this.id, this.nomZone, this.codeZone, this.descriptionZone});

  String get libelle => nomZone ?? codeZone ?? id;

  factory Zone.fromJson(Map<String, dynamic> json) => Zone(
        id: json['id'] as String,
        nomZone: json['nomZone'] as String?,
        codeZone: json['codeZone'] as String?,
        descriptionZone: json['descriptionZone'] as String?,
      );
}

class ServiceOcp {
  final String id;
  final String? nomService;
  final String? codeService;
  final String? zoneId;

  const ServiceOcp({required this.id, this.nomService, this.codeService, this.zoneId});

  String get libelle => nomService ?? codeService ?? id;

  factory ServiceOcp.fromJson(Map<String, dynamic> json) => ServiceOcp(
        id: json['id'] as String,
        nomService: json['nomService'] as String?,
        codeService: json['codeService'] as String?,
        zoneId: json['zone'] == null ? null : (json['zone']['id'] as String?),
      );
}

class Equipement {
  final String id;
  final String? nomEquipement;
  final String? codeEquipement;

  const Equipement({required this.id, this.nomEquipement, this.codeEquipement});

  String get libelle => nomEquipement ?? codeEquipement ?? id;

  factory Equipement.fromJson(Map<String, dynamic> json) => Equipement(
        id: json['id'] as String,
        nomEquipement: json['nomEquipement'] as String?,
        codeEquipement: json['codeEquipement'] as String?,
      );
}

class EntrepriseExterne {
  final String id;
  final String? nomEntreprise;

  const EntrepriseExterne({required this.id, this.nomEntreprise});

  String get libelle => nomEntreprise ?? id;

  factory EntrepriseExterne.fromJson(Map<String, dynamic> json) => EntrepriseExterne(
        id: json['id'] as String,
        nomEntreprise: json['nomEntreprise'] as String?,
      );
}

/// Élément de référentiel sélectionnable (risques, mesures, EPI, moyens d'accès).
class ReferentielItem {
  final String id;
  final String nom;
  final String? description;

  const ReferentielItem({required this.id, required this.nom, this.description});

  factory ReferentielItem.fromJson(
    Map<String, dynamic> json, {
    String nomKey = 'nom',
    String descriptionKey = 'description',
  }) {
    final itemId = (json['id'] ?? '').toString();
    return ReferentielItem(
      id: itemId,
      nom: json[nomKey] as String? ?? json['nomRisque'] as String? ?? itemId,
      description: json[descriptionKey] as String?,
    );
  }
}

/// Nom des champs par ressource (extraits des Response DTO backend).
final class ReferentielKeys {
  static const risques = ('nomRisque', 'descriptionRisque');
  static const mesures = ('nomMesure', 'descriptionMesure');
  static const epis = ('nomEPI', 'descriptionEPI');
  static const moyensAcces = ('nomMoyen', 'descriptionMoyen');
}

class TypePermis {
  final String id;
  final String? nom;
  final String? description;

  const TypePermis({required this.id, this.nom, this.description});

  String get libelle => nom ?? id;

  factory TypePermis.fromJson(Map<String, dynamic> json) => TypePermis(
        id: json['id'] as String,
        nom: json['nom'] as String?,
        description: json['description'] as String?,
      );
}
