/// Visas et Signatures officielles d'une AT - Conforme au Standard OCP S-HSE-SEC-31 Section G
/// CIRCUIT OFFICIEL : 1.CEEP → 2.CEEE → 3.HCEP → 4.HCEE → 5.HMEP → 6.HMEE
/// - Rôles EFFECTIFS par AT (at_roles.dart) : chaque bouton n'apparaît que pour
///   le rôle lié à CETTE AT (CEEP propriétaire, CEEE du service exécutant, etc.)
///   et pour l'étape atteinte dans le circuit.
/// - Signature manuscrite sur écran tactile (SignatureScreen) envoyée en multipart PNG
/// - Création + signature du visa à la volée pour le rôle dont c'est le tour (comme le web)
/// - Détection tri-niveau : commentaire → role direct → utilisateur.roles
library;

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/errors/error_mapper.dart';
import '../../../core/errors/failures.dart';
import '../../../core/network/api_providers.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/utils/app_date.dart';
import '../../../core/widgets/states.dart';
import '../../auth/presentation/auth_controller.dart';
import '../../at/data/models/autorisation_travail.dart';
import '../../at/presentation/at_circuit_visas.dart';
import '../../at/presentation/at_providers.dart';
import '../../at/presentation/at_roles.dart';
import '../../visas/data/visa.dart';
import '../../visas/visa_api.dart';
import '../../visas/presentation/signature_screen.dart';

final visasProvider =
    FutureProvider.autoDispose.family<List<Visa>, String>((ref, atId) async {
  final api = VisaApi(ref.watch(apiClientProvider));
  try {
    return await api.findByAt(atId);
  } catch (e) {
    throw mapDioError(e);
  }
});

class AtVisasPage extends ConsumerWidget {
  final String atId;
  const AtVisasPage({super.key, required this.atId});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final visas = ref.watch(visasProvider(atId));
    final detailAsync = ref.watch(atDetailProvider(atId));
    final session = ref.watch(sessionProvider);
    final at = detailAsync.valueOrNull;

    // ── Rôles EFFECTIFS pour CETTE AT (propriété, Section G, service exécutant)
    // - voir at_roles.dart. Résolus dès l'ouverture (fallback AT vide) pour que
    // les boutons du rôle strict n'attendent pas le chargement du détail. ──
    final roles = AtRoles.resolve(
      session: session,
      at: at ?? AutorisationTravail(id: atId),
    );

    bool autorisePour(String role) => roles.autorisePour(role);

    return Scaffold(
      backgroundColor: OcpColors.sage,
      appBar: AppBar(
        backgroundColor: OcpColors.forest,
        foregroundColor: OcpColors.white,
        elevation: 0,
        title: Text(
          at?.numero != null ? 'Visas - ${at!.numero}' : 'Visas & Signatures',
          style: const TextStyle(
            fontFamily: 'SpaceGrotesk',
            fontWeight: FontWeight.w700,
            fontSize: 16,
            color: OcpColors.white,
          ),
        ),
        actions: [
          IconButton(
            tooltip: 'Actualiser',
            icon: const Icon(Icons.refresh_rounded, color: OcpColors.white),
            onPressed: () {
              ref.invalidate(visasProvider(atId));
              ref.invalidate(atDetailProvider(atId));
            },
          ),
        ],
      ),
      body: visas.when(
        loading: () => const LoadingState(message: 'Chargement de la feuille des visas...'),
        error: (e, _) => ErrorState(
          message: e is Failure ? e.message : 'Erreur de chargement des visas.',
          onRetry: () => ref.invalidate(visasProvider(atId)),
        ),
        data: (items) {
          final etat = CircuitVisasEtat.resolve(visas: items, at: at);
          final statut = at?.statut;
          final statutTerminal = statut == StatutAt.archivee ||
              statut == StatutAt.rejetee ||
              statut == StatutAt.annulee;
          // Statuts pré-circuit : le visa CEEP (Étape 1) est signé puis l'AT soumise.
          final enPhaseBrouillon = statut == StatutAt.brouillon ||
              statut == StatutAt.demandeCreee ||
              statut == StatutAt.classificationEffectuee ||
              statut == StatutAt.enVisiteRedaction;

          final peutAccuserReception = at != null &&
              at.dateReceptionCeee == null &&
              roles.isCeee &&
              !_statutTropTot(at.statut) &&
              etat.ceep;

          // Prochaine étape actionnable : premier rôle (dans l'ordre officiel du
          // circuit) autorisé pour l'utilisateur courant, sans ligne de visa
          // existante et dont toutes les étapes précédentes sont acquises.
          String? roleActionnable;
          for (final role in kCircuitVisas) {
            final aLigne = items.any((v) => roleDeVisa(v) == role);
            if (!aLigne && autorisePour(role) && etat.precedentsSignes(role)) {
              roleActionnable = role;
              break;
            }
          }
          final roleAction = roleActionnable;

          final sortedItems = List<Visa>.from(items)
            ..sort((a, b) => (a.ordre ?? 99).compareTo(b.ordre ?? 99));

          return ListView(
            padding: const EdgeInsets.all(16),
            children: [
              // ── Bandeau Logigramme S-HSE-SEC-31 ──────────────────────────
              Card(
                color: OcpColors.white,
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(12),
                  side: const BorderSide(color: OcpColors.borderSoft),
                ),
                child: Padding(
                  padding: const EdgeInsets.all(14),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      const Row(
                        children: [
                          Icon(Icons.verified_user_rounded, size: 20, color: OcpColors.forest),
                          SizedBox(width: 8),
                          Expanded(
                            child: Text(
                              'Circuit séquentiel officiel des Visas (Section G)',
                              style: TextStyle(
                                fontFamily: 'SpaceGrotesk',
                                fontWeight: FontWeight.w700,
                                fontSize: 13,
                                color: OcpColors.forest,
                              ),
                            ),
                          ),
                        ],
                      ),
                      const SizedBox(height: 8),
                      const Text(
                        '1. Visa CEEP (Demandeur Propriétaire - signe l\'AT le premier)\n'
                        '2. Visa CEEE (Chef d\'Équipe Exécutant Intervenant)\n'
                        '3. Visa HCEP (Hors Cadre Propriétaire)\n'
                        '4. Visa HCEE (Hors Cadre Exécutant)\n'
                        '5. Visa HMEP (Haute Maîtrise Propriétaire)\n'
                        '6. Visa HMEE (Haute Maîtrise Exécutante - Validation finale & Déblocage PDF)',
                        style: TextStyle(fontSize: 12, color: OcpColors.ink, height: 1.4, fontWeight: FontWeight.w500),
                      ),
                      const SizedBox(height: 4),
                      const Text(
                        'Le CEEP appose le premier visa (Étape 1) dès la soumission de l\'AT ; '
                        'chaque visa suivant se débloque après le précédent.',
                        style: TextStyle(fontSize: 11, color: OcpColors.slate, height: 1.3, fontStyle: FontStyle.italic),
                      ),
                    ],
                  ),
                ),
              ),
              const SizedBox(height: 12),

              // ── Accusé de réception préalable pour CEEE ───────────────────
              if (peutAccuserReception && session != null) ...[
                _AccuseReceptionCard(onConfirm: () => _accuserReception(context, ref)),
                const SizedBox(height: 12),
              ],

              // ── Liste des Visas ───────────────────────────────────────────
              ...sortedItems.map((v) {
                final ordre = v.ordre ?? 1;
                final detectedRole = roleDeVisa(v);
                // Étape précédente : rôles antérieurs du circuit acquis (fallbacks
                // statut inclus) ; fallback ordre pour les lignes sans rôle reconnu.
                final isPrecedentValide = detectedRole.isNotEmpty
                    ? etat.precedentsSignes(detectedRole)
                    : sortedItems
                        .where((other) => (other.ordre ?? 0) < ordre)
                        .every((other) => other.signaturePresente || other.statut == StatutVisa.valide);

                final bool isUserAuthorizedForThisVisa = autorisePour(detectedRole);

                // Titre lisible : les marqueurs techniques (g1VisaCeep...) sont remplacés
                final commentaireBrut = v.commentaire?.trim() ?? '';
                final titre = (commentaireBrut.isEmpty ||
                            commentaireBrut.toUpperCase().startsWith('G1VISA')) &&
                        detectedRole.isNotEmpty
                    ? 'Visa ${libelleRoleVisa(detectedRole)} (Étape ${ordreRoleVisa(detectedRole)})'
                    : (commentaireBrut.isNotEmpty ? commentaireBrut : 'Visa réglementaire');

                return _VisaTile(
                  visa: v,
                  titre: titre,
                  isUserAuthorized: isUserAuthorizedForThisVisa,
                  isPrecedentValide: isPrecedentValide,
                  onSign: () => _signer(context, ref, v),
                );
              }),

              // ── Carte d'action : le rôle dont c'est le tour crée + signe ──
              if (roleAction != null && !statutTerminal) ...[
                const SizedBox(height: 4),
                _CarteSignatureRole(
                  role: roleAction,
                  avecSoumission: enPhaseBrouillon && roleAction == 'CEEP',
                  onSign: () => _actionSignerRole(
                    context,
                    ref,
                    roleAction,
                    enPhaseBrouillon && roleAction == 'CEEP',
                  ),
                  onRefuse: (roleAction == 'HCEP' ||
                          roleAction == 'HCEE' ||
                          roleAction == 'HMEP' ||
                          roleAction == 'HMEE')
                      ? () => _refuserRole(context, ref, roleAction)
                      : null,
                ),
                const SizedBox(height: 12),
              ],

              // ── Aucun visa : explication du circuit ───────────────────────
              if (items.isEmpty) const _EmptyVisasTemplate(),
            ],
          );
        },
      ),
    );
  }

  bool _statutTropTot(String? statut) =>
      statut == StatutAt.brouillon || statut == StatutAt.classificationEffectuee;

  Future<void> _accuserReception(BuildContext context, WidgetRef ref) async {
    try {
      await ref.read(atApiProvider).accuserReceptionCeee(atId);
      ref.invalidate(atDetailProvider(atId));
      ref.invalidate(visasProvider(atId));
      if (context.mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            backgroundColor: OcpColors.success,
            content: Text('Accusé de réception confirmé par le CEEE.'),
          ),
        );
      }
    } catch (e) {
      if (context.mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            backgroundColor: OcpColors.errorSoft,
            content: Text(mapDioError(e).message),
          ),
        );
      }
    }
  }

  Future<void> _signer(BuildContext context, WidgetRef ref, Visa visa) async {
    final session = ref.read(sessionProvider);
    final result = await Navigator.of(context).push<SignatureResult>(
      MaterialPageRoute(
        builder: (_) => SignatureScreen(
          signataireNom: session?.utilisateur.nomComplet ?? 'Signataire habilité',
        ),
      ),
    );
    if (result == null || !context.mounted) return;

    try {
      await VisaApi(ref.read(apiClientProvider)).sign(
        visaId: visa.id,
        signaturePng: result.pngBytes,
        commentaire: visa.commentaire,
      );
      // Identique au web : la signature d'un visa Hors Cadre / Haute Maîtrise
      // déclenche la VALIDATION de l'AT (statut VALIDEE → débloque le démarrage).
      final role = roleDeVisa(visa);
      if (role == 'HCEP' || role == 'HCEE' || role == 'HMEP' || role == 'HMEE') {
        await ref.read(atApiProvider).valider(atId);
      }
      ref.invalidate(visasProvider(atId));
      ref.invalidate(atDetailProvider(atId));
      if (context.mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            backgroundColor: OcpColors.success,
            content: Text('Signature manuscrite enregistrée et scellée avec succès.'),
          ),
        );
      }
    } catch (e) {
      if (context.mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            backgroundColor: OcpColors.errorSoft,
            content: Text(mapDioError(e).message),
          ),
        );
      }
    }
  }

  /// Refus d'une AT par un rôle HC/HM - identique au web
  /// (ValidationOCPPage.handleRefuser : motif obligatoire + visa `Refus:` + reject).
  Future<void> _refuserRole(BuildContext context, WidgetRef ref, String role) async {
    final motifController = TextEditingController();
    final conf = await showDialog<bool>(
      context: context,
      builder: (ctx) => StatefulBuilder(
        builder: (ctx, setDialogState) => AlertDialog(
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
          title: const Text("Refuser l'AT",
              style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
          content: TextField(
            controller: motifController,
            maxLines: 3,
            autofocus: true,
            onChanged: (_) => setDialogState(() {}),
            decoration: const InputDecoration(
              labelText: 'Motif du refus *',
              hintText: 'Préciser le motif du rejet de l\'AT...',
            ),
          ),
          actions: [
            TextButton(onPressed: () => Navigator.pop(ctx, false), child: const Text('Annuler')),
            FilledButton(
              style: FilledButton.styleFrom(backgroundColor: OcpColors.errorSoft),
              onPressed: motifController.text.trim().isEmpty
                  ? null
                  : () => Navigator.pop(ctx, true),
              child: const Text('Confirmer le refus'),
            ),
          ],
        ),
      ),
    );
    if (conf != true || !context.mounted) return;

    try {
      final ok = await refuserAt(
        context,
        ref,
        atId,
        role: role,
        motif: motifController.text.trim(),
      );
      if (!ok || !context.mounted) return;
      ref.invalidate(visasProvider(atId));
      ref.invalidate(atDetailProvider(atId));
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          backgroundColor: OcpColors.errorSoft,
          content: Text('Autorisation de Travail rejetée.'),
        ),
      );
    } catch (e) {
      if (context.mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            backgroundColor: OcpColors.errorSoft,
            content: Text(mapDioError(e).message),
          ),
        );
      }
    }
  }

  /// Action de signature pour le rôle dont c'est le tour :
  /// - CEEP en brouillon → signature du Visa CEEP (Étape 1) puis soumission ;
  /// - sinon → création + signature du visa du rôle (comme le flux web).
  Future<void> _actionSignerRole(
    BuildContext context,
    WidgetRef ref,
    String role,
    bool avecSoumission,
  ) async {
    try {
      String message;
      if (avecSoumission) {
        final ok = await signerVisaCeepEtSoumettre(context, ref, atId);
        if (!ok || !context.mounted) return;
        message = 'Visa CEEP apposé - AT soumise et circuit des visas initialisé.';
      } else {
        await creerEtSignerVisa(context, ref, atId, role);
        if (!context.mounted) return;
        // Identique au web : après visa HC/HM l'AT est validée (statut VALIDEE).
        final avecValidation =
            role == 'HCEP' || role == 'HCEE' || role == 'HMEP' || role == 'HMEE';
        message = avecValidation
            ? 'Visa $role apposé - AT validée avec succès.'
            : 'Visa $role apposé et scellé avec succès.';
      }
      ref.invalidate(visasProvider(atId));
      ref.invalidate(atDetailProvider(atId));
      if (context.mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(backgroundColor: OcpColors.success, content: Text(message)),
        );
      }
    } catch (e) {
      if (context.mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            backgroundColor: OcpColors.errorSoft,
            content: Text(mapDioError(e).message),
          ),
        );
      }
    }
  }
}

// ─────────────────────────────────────────────────────────────────────────────
// Composants Visuels
// ─────────────────────────────────────────────────────────────────────────────

class _AccuseReceptionCard extends StatelessWidget {
  final VoidCallback onConfirm;
  const _AccuseReceptionCard({required this.onConfirm});

  @override
  Widget build(BuildContext context) => Card(
        color: OcpColors.mintSoft,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(12),
          side: const BorderSide(color: OcpColors.mint),
        ),
        child: Padding(
          padding: const EdgeInsets.all(14),
          child: Row(
            children: [
              const Icon(Icons.mark_email_read_rounded, color: OcpColors.forest, size: 24),
              const SizedBox(width: 12),
              const Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'Accuser réception de l\'AT (CEEE)',
                      style: TextStyle(fontWeight: FontWeight.w700, fontSize: 13, color: OcpColors.forest),
                    ),
                    SizedBox(height: 2),
                    Text(
                      'Préalable obligatoire avant la signature du visa CEEE.',
                      style: TextStyle(fontSize: 11, color: OcpColors.ink),
                    ),
                  ],
                ),
              ),
              FilledButton(
                style: FilledButton.styleFrom(
                  backgroundColor: OcpColors.forest,
                  padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 8),
                  minimumSize: const Size(0, 36),
                ),
                onPressed: onConfirm,
                child: const Text('Confirmer', style: TextStyle(fontSize: 12, fontWeight: FontWeight.bold)),
              ),
            ],
          ),
        ),
      );
}

/// Carte d'action pour le rôle dont c'est le tour dans le circuit :
/// permet de créer ET signer son visa à la volée (même parcours que le web).
/// Pour les rôles HC/HM : la signature VALIDE l'AT (statut VALIDEE, comme
/// ValidationOCPPage) et un bouton « Refuser » est proposé avec motif.
class _CarteSignatureRole extends StatelessWidget {
  final String role;
  final bool avecSoumission;
  final VoidCallback onSign;
  final VoidCallback? onRefuse;

  const _CarteSignatureRole({
    required this.role,
    required this.avecSoumission,
    required this.onSign,
    this.onRefuse,
  });

  static const Set<String> _rolesValidation = {'HCEP', 'HCEE', 'HMEP', 'HMEE'};

  bool get _avecValidation => _rolesValidation.contains(role);

  @override
  Widget build(BuildContext context) {
    final ordre = ordreRoleVisa(role);
    return Card(
      color: OcpColors.mintSoft,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(12),
        side: const BorderSide(color: OcpColors.forest, width: 1.5),
      ),
      child: Padding(
        padding: const EdgeInsets.all(14),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Row(
              children: [
                const CircleAvatar(
                  radius: 18,
                  backgroundColor: OcpColors.white,
                  child: Icon(Icons.draw_rounded, color: OcpColors.forest, size: 18),
                ),
                const SizedBox(width: 10),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        'Étape $ordre - Visa ${libelleRoleVisa(role)}',
                        style: const TextStyle(
                          fontFamily: 'SpaceGrotesk',
                          fontWeight: FontWeight.w700,
                          fontSize: 13,
                          color: OcpColors.forest,
                        ),
                      ),
                      const SizedBox(height: 2),
                      Text(
                        avecSoumission
                            ? "C'est votre tour : signez l'AT (Visa CEEP) puis soumettez-la pour lancer le circuit des visas."
                            : _avecValidation
                                ? "C'est votre tour : signez votre visa pour VALIDER l'AT et la faire avancer (ou refusez-la avec motif)."
                                : "C'est votre tour : créez et signez votre visa pour faire avancer le circuit.",
                        style: const TextStyle(fontSize: 11, color: OcpColors.ink, height: 1.3),
                      ),
                    ],
                  ),
                ),
              ],
            ),
            const SizedBox(height: 12),
            FilledButton.icon(
              style: FilledButton.styleFrom(
                backgroundColor: OcpColors.forest,
                foregroundColor: OcpColors.white,
                minimumSize: const Size.fromHeight(42),
                shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
              ),
              onPressed: onSign,
              icon: const Icon(Icons.draw_rounded, size: 18),
              label: Text(
                avecSoumission
                    ? "Signer l'AT (Visa CEEP) & Soumettre l'AT"
                    : _avecValidation
                        ? "Signer & Valider l'AT (Visa $role - Étape $ordre)"
                        : "Signer l'AT (Visa $role - Étape $ordre)",
                style: const TextStyle(fontWeight: FontWeight.w700, fontSize: 13),
              ),
            ),
            if (onRefuse != null) ...[
              const SizedBox(height: 8),
              OutlinedButton.icon(
                style: OutlinedButton.styleFrom(
                  foregroundColor: OcpColors.errorSoft,
                  side: const BorderSide(color: OcpColors.errorSoft),
                  minimumSize: const Size.fromHeight(42),
                  shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
                ),
                onPressed: onRefuse,
                icon: const Icon(Icons.block_rounded, size: 18),
                label: const Text(
                  "Refuser l'AT (motif obligatoire)",
                  style: TextStyle(fontWeight: FontWeight.w700, fontSize: 13),
                ),
              ),
            ],
          ],
        ),
      ),
    );
  }
}

class _VisaTile extends StatelessWidget {
  final Visa visa;
  final String titre;
  final bool isUserAuthorized;
  final bool isPrecedentValide;
  final VoidCallback onSign;

  const _VisaTile({
    required this.visa,
    required this.titre,
    required this.isUserAuthorized,
    required this.isPrecedentValide,
    required this.onSign,
  });

  bool get _isSigned => visa.signaturePresente || visa.statut == StatutVisa.valide;
  bool get _canSignNow => isUserAuthorized && !_isSigned && isPrecedentValide;

  @override
  Widget build(BuildContext context) {
    return Card(
      elevation: 0,
      margin: const EdgeInsets.only(bottom: 10),
      color: OcpColors.white,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(12),
        side: BorderSide(
          color: _isSigned
              ? OcpColors.success
              : (_canSignNow ? OcpColors.forest : OcpColors.borderSoft),
          width: _canSignNow ? 1.5 : 1,
        ),
      ),
      child: Padding(
        padding: const EdgeInsets.all(14),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Row(
              children: [
                CircleAvatar(
                  radius: 18,
                  backgroundColor: _isSigned
                      ? OcpColors.forestSoft
                      : (_canSignNow ? OcpColors.mintSoft : OcpColors.surfaceSoft),
                  child: Icon(
                    _isSigned ? Icons.verified_rounded : Icons.draw_rounded,
                    color: _isSigned ? OcpColors.forest : (_canSignNow ? OcpColors.forest : OcpColors.slate),
                    size: 18,
                  ),
                ),
                const SizedBox(width: 10),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        titre,
                        style: const TextStyle(
                          fontFamily: 'SpaceGrotesk',
                          fontWeight: FontWeight.w700,
                          fontSize: 14,
                          color: OcpColors.ink,
                        ),
                      ),
                      Text(
                        _isSigned
                            ? 'Signé par ${visa.utilisateurNomComplet ?? 'Signataire'} le ${AppDate.dateHeure(visa.dateSignature ?? visa.dateVisa)}'
                            : (isPrecedentValide
                                ? 'Prêt pour signature'
                                : 'En attente du visa précédent'),
                        style: TextStyle(
                          fontSize: 11,
                          color: _isSigned ? OcpColors.forest : OcpColors.slate,
                        ),
                      ),
                    ],
                  ),
                ),
                Chip(
                  label: Text(
                    _isSigned ? 'Signé' : (_canSignNow ? 'À signer' : 'En attente'),
                    style: const TextStyle(fontSize: 10, fontWeight: FontWeight.bold),
                  ),
                  backgroundColor: _isSigned
                      ? OcpColors.forestSoft
                      : (_canSignNow ? OcpColors.mintSoft : OcpColors.warningSoft),
                ),
              ],
            ),

            if (!_isSigned && !isPrecedentValide) ...[
              const SizedBox(height: 10),
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
                decoration: BoxDecoration(
                  color: OcpColors.surfaceSoft,
                  borderRadius: BorderRadius.circular(6),
                ),
                child: const Row(
                  children: [
                    Icon(Icons.lock_clock_outlined, size: 14, color: OcpColors.slate),
                    SizedBox(width: 6),
                    Expanded(
                      child: Text(
                        'Ce visa sera débloqué dès que le visa précédent dans l\'ordre aura été apposé.',
                        style: TextStyle(fontSize: 11, color: OcpColors.slate),
                      ),
                    ),
                  ],
                ),
              ),
            ],

            if (_canSignNow) ...[
              const SizedBox(height: 12),
              FilledButton.icon(
                style: FilledButton.styleFrom(
                  backgroundColor: OcpColors.forest,
                  foregroundColor: OcpColors.white,
                  minimumSize: const Size.fromHeight(42),
                  shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
                ),
                onPressed: onSign,
                icon: const Icon(Icons.draw_rounded, size: 18),
                label: const Text(
                  'Apposer ma signature manuscrite sur ce Visa',
                  style: TextStyle(fontWeight: FontWeight.w700, fontSize: 13),
                ),
              ),
            ],
          ],
        ),
      ),
    );
  }
}

class _EmptyVisasTemplate extends StatelessWidget {
  const _EmptyVisasTemplate();

  @override
  Widget build(BuildContext context) {
    return Card(
      color: OcpColors.white,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(12),
        side: const BorderSide(color: OcpColors.borderSoft),
      ),
      child: const Padding(
        padding: EdgeInsets.all(16),
        child: Column(
          children: [
            Icon(Icons.draw_outlined, size: 36, color: OcpColors.forest),
            SizedBox(height: 8),
            Text(
              'Circuit des Visas S-HSE-SEC-31',
              style: TextStyle(fontFamily: 'SpaceGrotesk', fontWeight: FontWeight.bold, fontSize: 15),
            ),
            SizedBox(height: 6),
            Text(
              'Les 6 visas réglementaires sont apposés séquentiellement :\n'
              'CEEP → CEEE → HCEP → HCEE → HMEP → HMEE.\n'
              'Le CEEP signe l\'AT le premier, dès la soumission officielle.',
              textAlign: TextAlign.center,
              style: TextStyle(fontSize: 12, color: OcpColors.slate, height: 1.4),
            ),
          ],
        ),
      ),
    );
  }
}
