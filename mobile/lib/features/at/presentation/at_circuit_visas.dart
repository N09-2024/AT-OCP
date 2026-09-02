/// Circuit officiel des Visas & Signatures - Standard OCP S-HSE-SEC-31 (Section G)
///
/// Ordre séquentiel réglementaire (aligné sur VisaServiceImpl côté backend) :
///   1. CEEP  (Demandeur Propriétaire) - signe l'AT le premier, dès la soumission
///   2. CEEE  (Chef d'Équipe Exécutant Intervenant)
///   3. HCEP  (Hors Cadre Propriétaire)
///   4. HCEE  (Hors Cadre Exécutant)
///   5. HMEP  (Haute Maîtrise Propriétaire)
///   6. HMEE  (Haute Maîtrise Exécutante - validation finale & déblocage PDF)
library;

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/network/api_providers.dart';
import '../../auth/presentation/auth_controller.dart';
import '../../visas/data/visa.dart';
import '../../visas/presentation/signature_screen.dart';
import '../../visas/visa_api.dart';
import '../data/models/autorisation_travail.dart';
import 'at_providers.dart';

/// Ordre officiel des rôles signataires.
const List<String> kCircuitVisas = ['CEEP', 'CEEE', 'HCEP', 'HCEE', 'HMEP', 'HMEE'];

/// Libellés métier des rôles (Section G).
String libelleRoleVisa(String role) => switch (role) {
      'CEEP' => 'CEEP - Demandeur Propriétaire',
      'CEEE' => "CEEE - Chef d'Équipe Exécutant",
      'HCEP' => 'HCEP - Hors Cadre Propriétaire',
      'HCEE' => 'HCEE - Hors Cadre Exécutant',
      'HMEP' => 'HMEP - Haute Maîtrise Propriétaire',
      'HMEE' => 'HMEE - Haute Maîtrise Exécutante',
      _ => role,
    };

/// Ordre (1..6) attendu pour un rôle dans le circuit.
int ordreRoleVisa(String role) => kCircuitVisas.indexOf(role) + 1;

/// Commentaire-marqueur apposé sur le visa créé pour un rôle :
/// CEEP → `g1VisaCeep`, CEEE → `g1VisaCeee` (marqueurs du web, reconnus par le
/// générateur PDF), autres → `Visa {ROLE} - Signature officielle`
/// (identique ValidationOCPPage web). Le backend résout le rôle via ce marqueur.
String commentaireVisaPourRole(String role) => switch (role) {
      'CEEP' => 'g1VisaCeep',
      'CEEE' => 'g1VisaCeee',
      _ => 'Visa $role - Signature officielle',
    };

/// Détection tri-niveau d'un visa SIGNÉ pour un rôle - alignée sur detectVisa()
/// de AutorisationDetailPage.tsx et isRoleSigned() de VisaServiceImpl :
///   1) v.commentaire contient le rôle
///   2) v.role contient le rôle
///   3) v.utilisateur.roles[] contient le rôle
bool visaSignePourRole(List<Visa> visas, String role) {
  final kw = role.toUpperCase();
  return visas.any((v) {
    final isSigned = v.signaturePresente ||
        v.statut == StatutVisa.valide ||
        v.statut == StatutVisa.validation ||
        v.statut == StatutVisa.signature;
    if (!isSigned) return false;
    if ((v.commentaire ?? '').toUpperCase().contains(kw)) return true;
    if ((v.role ?? '').toUpperCase().contains(kw)) return true;
    return (v.utilisateur?.roles ?? []).any((r) => r == kw || r.contains(kw));
  });
}

/// Rôle du circuit associé à une ligne de visa (détection tri-niveau,
/// fallback par ordre 1..6 pour les lignes sans marqueur).
String roleDeVisa(Visa v) {
  final comment = (v.commentaire ?? '').toUpperCase();
  final directRole = (v.role ?? '').toUpperCase();
  final userRoles = v.utilisateur?.roles ?? [];
  for (final kw in kCircuitVisas) {
    if (comment.contains(kw)) return kw;
  }
  for (final kw in kCircuitVisas) {
    if (directRole.contains(kw)) return kw;
  }
  for (final kw in kCircuitVisas) {
    if (userRoles.any((r) => r == kw || r.contains(kw))) return kw;
  }
  final ordre = v.ordre ?? 0;
  if (ordre >= 1 && ordre <= kCircuitVisas.length) return kCircuitVisas[ordre - 1];
  return '';
}

/// État de signature du circuit des visas pour une AT donnée.
///
/// Fallbacks statut calqués sur VisaServiceImpl.signVisa :
///   ceepSigné = visa CEEP || statut != BROUILLON
///   ceeeSigné = visa CEEE || dateReceptionCeee != null
///               || statut ∉ {BROUILLON, SOUMISE, DEMANDE_CREEE}
class CircuitVisasEtat {
  final bool ceep;
  final bool ceee;
  final bool hcep;
  final bool hcee;
  final bool hmep;
  final bool hmee;

  const CircuitVisasEtat({
    required this.ceep,
    required this.ceee,
    required this.hcep,
    required this.hcee,
    required this.hmep,
    required this.hmee,
  });

  factory CircuitVisasEtat.resolve({required List<Visa> visas, AutorisationTravail? at}) {
    final statut = at?.statut;
    final horsBrouillon = statut != null && statut != StatutAt.brouillon;
    final horsDebutCircuit = statut != null &&
        statut != StatutAt.brouillon &&
        statut != StatutAt.soumise &&
        statut != StatutAt.demandeCreee;
    return CircuitVisasEtat(
      ceep: visaSignePourRole(visas, 'CEEP') || horsBrouillon,
      ceee: visaSignePourRole(visas, 'CEEE') ||
          at?.dateReceptionCeee != null ||
          horsDebutCircuit,
      hcep: visaSignePourRole(visas, 'HCEP'),
      hcee: visaSignePourRole(visas, 'HCEE'),
      hmep: visaSignePourRole(visas, 'HMEP'),
      hmee: visaSignePourRole(visas, 'HMEE'),
    );
  }

  bool roleSigne(String role) => switch (role) {
        'CEEP' => ceep,
        'CEEE' => ceee,
        'HCEP' => hcep,
        'HCEE' => hcee,
        'HMEP' => hmep,
        'HMEE' => hmee,
        _ => false,
      };

  /// Première étape du circuit non encore marquée signée (fallbacks inclus),
  /// null si le circuit est complet.
  String? get prochainRoleRequis {
    for (final role in kCircuitVisas) {
      if (!roleSigne(role)) return role;
    }
    return null;
  }

  /// Toutes les étapes strictement AVANT [role] sont marquées signées.
  bool precedentsSignes(String role) {
    final idx = kCircuitVisas.indexOf(role);
    if (idx <= 0) return true;
    return kCircuitVisas.take(idx).every(roleSigne);
  }
}

/// Étape 1 du circuit : le CEEP signe l'AT (Visa CEEP - ordre 1, marqueur
/// `g1VisaCeep`) puis l'AT est soumise dans le circuit des visas (transmission
/// au CEEE). Identique au flux web (AutorisationFormPage) : create + sign → soumettre.
///
/// Retourne false si la signature a été annulée ; lève une exception en cas
/// d'échec API (l'appelant gère l'affichage de l'erreur).
Future<bool> signerVisaCeepEtSoumettre(BuildContext context, WidgetRef ref, String atId) async {
  final result = await _demanderSignature(
    context,
    ref,
    signataireDefaut: 'CEEP (Demandeur Propriétaire)',
  );
  if (result == null) return false;

  final visaApi = VisaApi(ref.read(apiClientProvider));
  final visa = await visaApi.create(
    autorisationTravailId: atId,
    commentaire: commentaireVisaPourRole('CEEP'),
    ordre: ordreRoleVisa('CEEP'),
  );
  await visaApi.sign(
    visaId: visa.id,
    signaturePng: result.pngBytes,
    commentaire: commentaireVisaPourRole('CEEP'),
  );
  await ref.read(atApiProvider).soumettre(atId);
  return true;
}

/// Crée et signe un visa pour un rôle du circuit (create + sign, comme le web).
///
/// Fidèle à ValidationOCPPage : après la signature d'un visa Hors Cadre /
/// Haute Maîtrise (HCEP, HCEE, HMEP, HMEE), l'AT est VALIDÉE
/// (`POST /autorisations-travail/{id}/validate`) - c'est cette validation qui
/// fait passer le statut à VALIDEE et débloque le démarrage par le CEEE.
Future<void> creerEtSignerVisa(BuildContext context, WidgetRef ref, String atId, String role) async {
  final result = await _demanderSignature(
    context,
    ref,
    signataireDefaut: libelleRoleVisa(role),
  );
  if (result == null) return;

  final visaApi = VisaApi(ref.read(apiClientProvider));
  final visa = await visaApi.create(
    autorisationTravailId: atId,
    commentaire: commentaireVisaPourRole(role),
    ordre: ordreRoleVisa(role),
  );
  await visaApi.sign(
    visaId: visa.id,
    signaturePng: result.pngBytes,
    commentaire: commentaireVisaPourRole(role),
  );

  // Validation de l'AT après visa HC/HM (identique au web : handleValider
  // enchaîne createAndSignVisa puis autorisationTravailApi.valider).
  if (role == 'HCEP' || role == 'HCEE' || role == 'HMEP' || role == 'HMEE') {
    await ref.read(atApiProvider).valider(atId);
  }
}

/// Refus d'une AT par un rôle HC/HM - identique ValidationOCPPage.handleRefuser :
/// visa marqué `Refus: {motif}` + `POST /autorisations-travail/{id}/reject`
/// → statut REJETEE. Retourne false si l'utilisateur annule (signature ou motif vide).
Future<bool> refuserAt(
  BuildContext context,
  WidgetRef ref,
  String atId, {
  required String role,
  required String motif,
}) async {
  final result = await _demanderSignature(
    context,
    ref,
    signataireDefaut: libelleRoleVisa(role),
  );
  if (result == null) return false;

  final visaApi = VisaApi(ref.read(apiClientProvider));
  final visa = await visaApi.create(
    autorisationTravailId: atId,
    commentaire: 'Refus: $motif',
    ordre: ordreRoleVisa(role),
  );
  await visaApi.sign(
    visaId: visa.id,
    signaturePng: result.pngBytes,
    commentaire: 'Refus: $motif',
  );
  await ref.read(atApiProvider).refuser(atId, motif);
  return true;
}

Future<SignatureResult?> _demanderSignature(
  BuildContext context,
  WidgetRef ref, {
  required String signataireDefaut,
}) async {
  final session = ref.read(sessionProvider);
  return Navigator.of(context).push<SignatureResult>(
    MaterialPageRoute(
      builder: (_) => SignatureScreen(
        signataireNom: session?.utilisateur.nomComplet ?? signataireDefaut,
      ),
    ),
  );
}
