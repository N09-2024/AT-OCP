/// Modèle Utilisateur — reflète UtilisateurResponse du backend.
/// Champs JSON : id, matricule, nom, prenom, email, telephone, photo, actif,
/// compteVerrouille, motDePasseExpire, enAttenteValidation, dateCreation,
/// dateModification, derniereConnexion, service{...}, roles[{id, nom, description}].
library;

class ServiceRef {
  final String id;
  final String? nomService;
  final String? codeService;

  const ServiceRef({required this.id, this.nomService, this.codeService});

  factory ServiceRef.fromJson(Map<String, dynamic> json) => ServiceRef(
        id: json['id'] as String,
        nomService: json['nomService'] as String?,
        codeService: json['codeService'] as String?,
      );

  Map<String, dynamic> toJson() => {'id': id, 'nomService': nomService, 'codeService': codeService};
}

class RoleRef {
  final String id;
  final String nom;

  const RoleRef({required this.id, required this.nom});

  factory RoleRef.fromJson(Map<String, dynamic> json) =>
      RoleRef(id: json['id'] as String, nom: json['nom'] as String);
}

class Utilisateur {
  final String id;
  final String? matricule;
  final String? nom;
  final String? prenom;
  final String email;
  final String? telephone;
  final bool actif;
  final bool enAttenteValidation;
  final ServiceRef? service;
  final List<RoleRef> roles;

  const Utilisateur({
    required this.id,
    this.matricule,
    this.nom,
    this.prenom,
    required this.email,
    this.telephone,
    required this.actif,
    required this.enAttenteValidation,
    this.service,
    required this.roles,
  });

  String get nomComplet =>
      '${prenom ?? ''} ${nom ?? ''}'.trim().isEmpty ? email : '${prenom ?? ''} ${nom ?? ''}'.trim();

  factory Utilisateur.fromJson(Map<String, dynamic> json) => Utilisateur(
        id: json['id'] as String,
        matricule: json['matricule'] as String?,
        nom: json['nom'] as String?,
        prenom: json['prenom'] as String?,
        email: json['email'] as String? ?? '',
        telephone: json['telephone'] as String?,
        actif: json['actif'] as bool? ?? true,
        enAttenteValidation: json['enAttenteValidation'] as bool? ?? false,
        service: json['service'] == null
            ? null
            : ServiceRef.fromJson(json['service'] as Map<String, dynamic>),
        roles: (json['roles'] as List<dynamic>? ?? [])
            .map((e) => RoleRef.fromJson(e as Map<String, dynamic>))
            .toList(),
      );
}
