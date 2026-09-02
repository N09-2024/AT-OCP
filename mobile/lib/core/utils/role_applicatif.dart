/// Rôle applicatif principal — INTERFACES_PAR_ROLE.md (S-HSE-SEC-31 v1.0).
///
/// Rôles applicatifs : CE · HM · HC · ADMIN · RESPONSABLE_EXTERIEUR.
/// Les positions contextuelles P/E sont résolues PAR AT (at_roles.dart), jamais
/// ici : ce module ne sert qu'au choix de l'espace de travail (dashboard) et aux
/// gardes de routes.
///
/// Priorité de résolution (§1) : ADMIN > HC > HM > CE > RESPONSABLE_EXTERIEUR.
/// Si plusieurs rôles métier sont détenus, l'UI affiche un sélecteur de contexte.
library;

enum RoleApplicatif { admin, hc, hm, ce, externe }

/// Tous les rôles applicatifs détenus (pour le sélecteur de contexte).
Set<RoleApplicatif> roleApplicatifs(Iterable<String> roles) {
  final r = roles.map((e) => e.toUpperCase()).toSet();
  return {
    if (r.contains('ADMIN')) RoleApplicatif.admin,
    if (r.contains('HC') || r.contains('HCEP') || r.contains('HCEE'))
      RoleApplicatif.hc,
    if (r.contains('HM') || r.contains('HMEP') || r.contains('HMEE'))
      RoleApplicatif.hm,
    if (r.contains('CE') || r.contains('CEEP') || r.contains('CEEE'))
      RoleApplicatif.ce,
    if (r.contains('RESPONSABLE_EXTERIEUR')) RoleApplicatif.externe,
  };
}

/// Rôle applicatif principal selon la priorité du §1.
/// Aucun rôle reconnu → RESPONSABLE_EXTERIEUR (fail-closed : consultation seule).
RoleApplicatif roleApplicatifPrincipal(Iterable<String> roles) {
  final all = roleApplicatifs(roles);
  const priorite = [
    RoleApplicatif.admin,
    RoleApplicatif.hc,
    RoleApplicatif.hm,
    RoleApplicatif.ce,
    RoleApplicatif.externe,
  ];
  for (final role in priorite) {
    if (all.contains(role)) return role;
  }
  return RoleApplicatif.externe;
}

/// L'utilisateur peut-il créer une AT / une demande ? (§2 — « rôle CE »,
/// ADMIN inclus ; RESPONSABLE_EXTERIEUR explicitement interdit §6).
bool peutCreerAt(Iterable<String> roles) {
  final all = roleApplicatifs(roles);
  return all.contains(RoleApplicatif.ce) || all.contains(RoleApplicatif.admin);
}
