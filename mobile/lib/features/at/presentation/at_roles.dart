/// Rôles EFFECTIFS de l'utilisateur POUR UNE AT donnée — résolution contextuelle.
///
/// Les rôles ne se déduisent pas seulement des rôles globaux du compte : ils sont
/// liés à l'AT (propriété du brouillon, affectations Section G, service exécutant).
/// Un utilisateur = des rôles précis sur CETTE AT ; le CEEP de l'AT ne voit jamais
/// les boutons CEEE et réciproquement (les services propriétaire et exécutant sont
/// forcément différents — règle backend).
///
/// Règles de liaison (alignées backend `AutorisationTravailServiceImpl` / `VisaServiceImpl`) :
/// - ADMIN ......................... tous les droits (identique web).
/// - CEEP de CETTE AT .............. créateur/propriétaire du brouillon, ou nommé
///   en Section G (`g1NomCeep`), ou rôle synthétique CE non-exécutant. Un CEEP
///   strict qui n'est pas propriétaire de l'AT est spectateur (le backend refuse
///   de toute façon : « Seul le CEEP rédacteur de cette AT peut modifier »).
/// - CEEE de CETTE AT .............. nommé en Section G (`g1NomCeee`, liste possible
///   « Prenom Nom / Prenom Nom »), ou chef d'équipe (CEEE strict ou CE synthétique)
///   dont le service EST le service exécutant de l'AT.
/// - HCEP / HCEE ................... rôles globaux stricts, ou HC synthétique
///   désambiguïisé par le service (exécutant → HCEE, sinon HCEP).
/// - HMEP / HMEE ................... rôles globaux stricts, ou HM synthétique
///   désambiguïisé par le service (exécutant → HMEE, sinon HMEP).
library;

import '../../auth/data/models/auth_models.dart';
import '../data/models/autorisation_travail.dart';

class AtRoles {
  final bool isAdmin;
  final bool isCeep;
  final bool isCeee;
  final bool isHcep;
  final bool isHcee;
  final bool isHmep;
  final bool isHmee;

  const AtRoles({
    required this.isAdmin,
    required this.isCeep,
    required this.isCeee,
    required this.isHcep,
    required this.isHcee,
    required this.isHmep,
    required this.isHmee,
  });

  const AtRoles.vide()
      : isAdmin = false,
        isCeep = false,
        isCeee = false,
        isHcep = false,
        isHcee = false,
        isHmep = false,
        isHmee = false;

  bool get isCe => isCeep || isCeee;
  bool get isHc => isHcep || isHcee;
  bool get isHm => isHmep || isHmee;
  bool get isWorkflowParticipant =>
      isAdmin || isCeep || isCeee || isHcep || isHcee || isHmep || isHmee;

  /// L'utilisateur est-il autorisé à agir sous ce rôle du circuit sur CETTE AT ?
  bool autorisePour(String role) => switch (role) {
        'CEEP' => isCeep,
        'CEEE' => isCeee,
        'HCEP' => isHcep,
        'HCEE' => isHcee,
        'HMEP' => isHmep,
        'HMEE' => isHmee,
        _ => isAdmin,
      };

  /// Résolution des rôles effectifs pour l'utilisateur courant SUR CETTE AT.
  static AtRoles resolve({required AuthSession? session, required AutorisationTravail at}) {
    if (session == null) return const AtRoles.vide();

    final user = session.utilisateur;
    final userRoles = session.roles.map((r) => r.toUpperCase()).toList();
    final isAdmin = userRoles.contains('ADMIN');

    // Rôles stricts & synthétiques du compte (identique web)
    final isCeeeStrict = userRoles.contains('CEEE');
    final isHcepStrict = userRoles.contains('HCEP');
    final isHceeStrict = userRoles.contains('HCEE');
    final isHmepStrict = userRoles.contains('HMEP');
    final isHmeeStrict = userRoles.contains('HMEE');
    final isCeSynth = userRoles.contains('CE');
    final isHcSynth = userRoles.contains('HC');
    final isHmSynth = userRoles.contains('HM');

    // ── Liaisons à CETTE AT (Section G + propriété + services) ──
    final isCreatorOrOwner =
        at.proprietaireBrouillonId != null && at.proprietaireBrouillonId == user.id;
    final isAssignedAsCeep = _nomEgal(at.g1NomCeep, user.nomComplet);
    final isAssignedAsCeee = _nomDansListe(at.g1NomCeee, user.nomComplet);
    final isExecutantService =
        _serviceLie(user.service?.nomService, at.servicesIntervenants);

    // CEEE de CETTE AT : nommé en Section G, ou chef d'équipe (CEEE strict / CE
    // synthétique) rattaché au service exécutant de l'AT.
    final isCeee = isAdmin ||
        isAssignedAsCeee ||
        ((isCeeeStrict || isCeSynth) && isExecutantService);

    // CEEP de CETTE AT : propriétaire du brouillon ou nommé en Section G.
    // Exclusif du CEEE (le service propriétaire ≠ service exécutant) : un
    // exécutant de l'AT ne peut pas être son CEEP.
    final isCeep = isAdmin ||
        isCreatorOrOwner ||
        isAssignedAsCeep ||
        (isCeSynth && !isCeee && !isExecutantService);

    // Hors Cadre / Haute Maîtrise : rôles hiérarchiques non affectés par AT ;
    // rôles synthétiques HC/HM désambiguïsés par le service exécutant.
    final isHcep = isAdmin || isHcepStrict || (isHcSynth && !isExecutantService);
    final isHcee = isAdmin || isHceeStrict || (isHcSynth && isExecutantService);
    final isHmep = isAdmin || isHmepStrict || (isHmSynth && !isExecutantService);
    final isHmee = isAdmin || isHmeeStrict || (isHmSynth && isExecutantService);

    return AtRoles(
      isAdmin: isAdmin,
      isCeep: isCeep,
      isCeee: isCeee,
      isHcep: isHcep,
      isHcee: isHcee,
      isHmep: isHmep,
      isHmee: isHmee,
    );
  }

  // ── Helpers de correspondance (noms Section G / services) ──────────────────

  static String _norm(String? s) =>
      (s ?? '').toLowerCase().replaceAll(RegExp(r'\s+'), ' ').trim();

  static bool _nomEgal(String? a, String? b) {
    final x = _norm(a);
    final y = _norm(b);
    return x.isNotEmpty && x == y;
  }

  /// `g1NomCeee` peut lister plusieurs signataires : « Prenom Nom / Prenom Nom ».
  static bool _nomDansListe(String? liste, String? nom) {
    final n = _norm(nom);
    final l = _norm(liste);
    if (n.isEmpty || l.isEmpty) return false;
    for (final part in l.split('/')) {
      if (part.trim() == n) return true;
    }
    return l.contains(n);
  }

  /// Correspondance tolérante entre le service de l'utilisateur et le(s) service(s)
  /// exécutant(s) de l'AT (dans les deux sens, insensible à la casse).
  static bool _serviceLie(String? serviceUtilisateur, String? servicesAt) {
    final s = _norm(serviceUtilisateur);
    final a = _norm(servicesAt);
    if (s.isEmpty || a.isEmpty) return false;
    return a.contains(s) || s.contains(a);
  }
}
