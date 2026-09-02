/// Actions de workflow de l'AT - Alignement 100 % avec la version Web React / Spring Boot
/// Standard OCP S-HSE-SEC-31 :
///
/// CIRCUIT OFFICIEL DES VISAS (Section G) :
///   BROUILLON → CEEP rédige, signe l'AT (Visa CEEP - Étape 1) puis soumet
///   CIRCUIT → 1.CEEP → 2.CEEE → 3.HCEP → 4.HCEE → 5.HMEP → 6.HMEE
///
/// RÈGLES PDF (identique web) :
///   Bouton : pdfUnlocked = visa HMEE OU toutes signatures OU statut final.
///   Bannière de blocage : motifs du DTO exportPdfMotifsRefus (calcul serveur).
///
/// RÔLES : effectifs et liés à l'AT (at_roles.dart) - chaque bouton n'apparaît
/// que pour le rôle de l'utilisateur SUR CETTE AT et pour l'étape atteinte.
///
/// DÉTECTION DES VISAS : tri-niveau (commentaire → role direct → utilisateur.roles)
/// identique à detectVisa() de AutorisationDetailPage.tsx
library;

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../../core/errors/failures.dart';
import '../../../core/theme/app_colors.dart';
import '../../auth/presentation/auth_controller.dart';
import '../data/at_api.dart';
import '../data/models/autorisation_travail.dart';
import 'at_circuit_visas.dart';
import 'at_providers.dart';
import 'at_roles.dart';
import 'at_visas_page.dart';

/// Style des boutons d'action du workflow
enum _AtBtnStyle { primaire, secondaire, danger }

class AtWorkflowActions extends ConsumerWidget {
  final AutorisationTravail at;
  const AtWorkflowActions({super.key, required this.at});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final session = ref.watch(sessionProvider);
    final visasAsync = ref.watch(visasProvider(at.id));
    final visas = visasAsync.valueOrNull ?? [];

    // ── 1. Résolution des rôles EFFECTIFS pour CETTE AT ──────────────────────
    // Rôles liés à l'AT (propriété, Section G, service exécutant) - voir
    // at_roles.dart. Le CEEP de l'AT ne voit pas les boutons CEEE, etc.
    final roles = AtRoles.resolve(session: session, at: at);

    final statut = at.statut;

    // ── 2. Détection séquentielle des Visas (identique à detectVisa web) ────
    // Circuit : 1.CEEP → 2.CEEE → 3.HCEP → 4.HCEE → 5.HMEP → 6.HMEE.
    // Le CEEP signe l'AT le premier (Visa CEEP apposé dès la soumission).

    final bool hasCeepVisaExact = visaSignePourRole(visas, 'CEEP');

    final bool hasCeeeVisaExact = visaSignePourRole(visas, 'CEEE');

    final bool hasCeeeVisa = hasCeeeVisaExact ||
        statut == 'EN_ATTENTE_VALIDATION' ||
        statut == StatutAt.validee ||
        statut == StatutAt.atValidee ||
        statut == StatutAt.atRedigee ||
        at.dateReceptionCeee != null;

    final bool hasHcepVisa = visaSignePourRole(visas, 'HCEP');
    final bool hasHceeVisa = visaSignePourRole(visas, 'HCEE');
    final bool hasHmepVisa = visaSignePourRole(visas, 'HMEP');
    final bool hasHmeeVisa = visaSignePourRole(visas, 'HMEE');

    final bool allSignaturesComplete = hasCeeeVisa &&
        hasHcepVisa &&
        hasHceeVisa &&
        hasHmepVisa &&
        hasHmeeVisa;

    // ── 3. Condition PDF - web (pdfUnlocked) + garde DTO (rapport §M) ───────
    // Bouton : pdfUnlocked = hasHmeeVisa || allSignaturesComplete ||
    //          statut IN [CLOTUREE, TRAVAUX_RECEPTIONES, ARCHIVEE]
    //          ET exportPdfAutorise (serveur : statut ≥ soumise + permis conformes).
    // Bannière de blocage : motifs du DTO exportPdfMotifsRefus.
    final bool pdfUnlocked = hasHmeeVisa ||
        allSignaturesComplete ||
        statut == StatutAt.travauxReceptiones ||
        statut == StatutAt.archivee ||
        statut == 'CLOTUREE';

    final bool isWorkflowParticipant = roles.isWorkflowParticipant;

    final bool showPdf =
        pdfUnlocked && at.exportPdfAutorise != false && isWorkflowParticipant;

    // ── 4. Conditions de visibilité des boutons - identiques au web ─────────
    final bool canSignVisaStatus = statut == StatutAt.soumise ||
        statut == StatutAt.atRedigee ||
        statut == 'EN_ATTENTE_VALIDATION' ||
        statut == StatutAt.validee ||
        statut == StatutAt.atValidee;

    // Étape 0 - BROUILLON : le CEEP de l'AT uniquement (web : BROUILLON && isCeep).
    // La soumission se fait depuis le formulaire (signature du Visa CEEP - Étape 1).
    final bool showEditDraft = statut == StatutAt.brouillon && roles.isCeep;

    // Visa CEEP (Étape 1 - signé dès la soumission ; bouton de rattrapage mobile
    // pour les AT antérieures, réservé au CEEP de CETTE AT)
    final bool showSignCeep = canSignVisaStatus && roles.isCeep && !hasCeepVisaExact;

    // Visa CEEE (Étape 2 - CEEE de CETTE AT uniquement)
    final bool showSignCeee = canSignVisaStatus && roles.isCeee && !hasCeeeVisaExact;

    // Visa HCEP (Étape 3)
    final bool showSignHcep = canSignVisaStatus && roles.isHcep && hasCeeeVisa && !hasHcepVisa;

    // Visa HCEE (Étape 4)
    final bool showSignHcee = canSignVisaStatus && roles.isHcee && hasHcepVisa && !hasHceeVisa;

    // Visa HMEP (Étape 5)
    final bool showSignHmep = canSignVisaStatus && roles.isHmep && hasHceeVisa && !hasHmepVisa;

    // Visa HMEE (Étape 6 - validation finale)
    final bool showSignHmee = canSignVisaStatus && roles.isHmee && hasHmepVisa && !hasHmeeVisa;

    // Étape 4 : Démarrer l'intervention - web : (VALIDEE || AT_VALIDEE) && isCeee
    final bool showDemarrer = (statut == StatutAt.atValidee || statut == StatutAt.validee) &&
        roles.isCeee;

    // Étape 5 : Reconduction - web : (INTERVENTION_EN_COURS || AT_RECONDUITE) && isCeee
    final bool showReconduire = (statut == StatutAt.interventionEnCours ||
            statut == StatutAt.atReconduite) &&
        roles.isCeee;

    // Étape 6 : Fin des travaux - web : (INTERVENTION_EN_COURS || AT_RECONDUITE) && isCeee
    final bool showDeclarerFin = (statut == StatutAt.interventionEnCours ||
            statut == StatutAt.atReconduite) &&
        roles.isCeee;

    // Arrêt d'urgence - sécurité : participants pendant l'intervention
    final bool showSignalerIncident = (statut == StatutAt.interventionEnCours ||
            statut == StatutAt.atReconduite) &&
        isWorkflowParticipant;

    // Étape 7 : Réception conjointe - web : FIN_TRAVAUX_DECLAREE && isCe
    final bool showReceptionner = statut == StatutAt.finTravauxDeclaree && roles.isCe;

    // Étape 8 : Archivage - web : (CLOTUREE || TRAVAUX_RECEPTIONES) && isHc
    final bool showArchiver = (statut == StatutAt.travauxReceptiones || statut == 'CLOTUREE') &&
        roles.isHc;

    // ── 5. Assemblage des boutons ─────────────────────────────────────────────
    final boutons = <Widget>[];

    // ── BROUILLON ──
    // Identique web : le détail n'expose que « Reprendre le brouillon » au CEEP
    // de l'AT ; la soumission (avec signature du Visa CEEP - Étape 1) se fait
    // depuis le formulaire.
    if (showEditDraft) {
      boutons.add(_btn(
        context,
        style: _AtBtnStyle.primaire,
        icon: Icons.edit_rounded,
        label: 'Reprendre le brouillon',
        onPressed: () => context.push('/at/${at.id}/edit'),
      ));
    } else if (statut == StatutAt.brouillon ||
        statut == StatutAt.demandeCreee ||
        statut == StatutAt.classificationEffectuee ||
        statut == StatutAt.enVisiteRedaction) {
      boutons.add(_banner(
        icon: Icons.hourglass_top_rounded,
        message: 'Dossier en cours de rédaction et signature initiale par le CEEP (Demandeur Propriétaire).',
      ));
    }

    // ── CIRCUIT DES VISAS ──
    if (showSignCeep) {
      boutons.add(_btn(
        context,
        style: _AtBtnStyle.primaire,
        icon: Icons.draw_rounded,
        label: "Signer l'AT (Visa CEEP - Demandeur Propriétaire)",
        tooltip: 'Apposer votre visa en tant que Demandeur Propriétaire (CEEP) - Étape 1 du circuit',
        onPressed: () => context.push('/at/${at.id}/visas'),
      ));
    }
    if (showSignCeee) {
      boutons.add(_btn(
        context,
        style: _AtBtnStyle.primaire,
        icon: Icons.draw_rounded,
        label: 'Accuser réception et apposer votre Visa CEEE',
        tooltip: 'Accuser réception et apposer votre visa CEEE - Étape 2 du circuit',
        onPressed: () => context.push('/at/${at.id}/visas'),
      ));
    }
    if (showSignHcep) {
      boutons.add(_btn(
        context,
        style: _AtBtnStyle.primaire,
        icon: Icons.draw_rounded,
        label: "Signer l'AT (Visa HCEP - Hors Cadre Propriétaire)",
        tooltip: 'Apposer votre visa en tant que Hors Cadre Propriétaire (HCEP) - Étape 3 du circuit',
        onPressed: () => context.push('/at/${at.id}/visas'),
      ));
    }
    if (showSignHcee) {
      boutons.add(_btn(
        context,
        style: _AtBtnStyle.primaire,
        icon: Icons.draw_rounded,
        label: "Signer l'AT (Visa HCEE - Hors Cadre Exécutant)",
        tooltip: 'Apposer votre visa en tant que Hors Cadre Exécutant (HCEE) - Étape 4 du circuit',
        onPressed: () => context.push('/at/${at.id}/visas'),
      ));
    }
    if (showSignHmep) {
      boutons.add(_btn(
        context,
        style: _AtBtnStyle.primaire,
        icon: Icons.draw_rounded,
        label: "Signer l'AT (Visa HMEP - Haute Maîtrise Propriétaire)",
        tooltip: 'Apposer votre visa en tant que Haute Maîtrise Propriétaire (HMEP) - Étape 5 du circuit',
        onPressed: () => context.push('/at/${at.id}/visas'),
      ));
    }
    if (showSignHmee) {
      boutons.add(_btn(
        context,
        style: _AtBtnStyle.primaire,
        icon: Icons.draw_rounded,
        label: "Signer l'AT (Visa HMEE - Validation finale)",
        tooltip: 'Apposer votre visa en tant que Haute Maîtrise Exécutante (HMEE) - Étape 6 du circuit',
        onPressed: () => context.push('/at/${at.id}/visas'),
      ));
    }

    // Bouton consultatif si aucun bouton de visa n'est affiché mais on est en phase visa
    if (canSignVisaStatus &&
        !showSignCeep &&
        !showSignCeee &&
        !showSignHcep &&
        !showSignHcee &&
        !showSignHmep &&
        !showSignHmee &&
        isWorkflowParticipant) {
      boutons.add(_btn(
        context,
        style: _AtBtnStyle.secondaire,
        icon: Icons.draw_rounded,
        label: 'Consulter la feuille des Visas & Signatures',
        onPressed: () => context.push('/at/${at.id}/visas'),
      ));
    }

    // ── ÉTAPE 4 : DÉMARRAGE ──
    if (showDemarrer) {
      boutons.add(_btn(
        context,
        style: _AtBtnStyle.primaire,
        icon: Icons.play_arrow_rounded,
        label: "Démarrer l'intervention",
        tooltip: 'Vérifier les conditions pré-démarrage',
        onPressed: () => _dialogueDemarrage(context, ref),
      ));
    } else if ((statut == StatutAt.atValidee || statut == StatutAt.validee) && isWorkflowParticipant) {
      boutons.add(_banner(
        icon: Icons.verified_rounded,
        message: "AT validée par l'ensemble des visas. En attente de démarrage par l'équipe exécutante (CEEE).",
      ));
    }

    // ── ÉTAPES 5 & 6 : INTERVENTION ──
    if (showDeclarerFin) {
      boutons.add(_btn(
        context,
        style: _AtBtnStyle.primaire,
        icon: Icons.task_alt_rounded,
        label: 'Déclarer la fin des travaux',
        tooltip: 'Déclarer la fin des travaux',
        onPressed: () => _dialogueFinTravaux(context, ref),
      ));
    }
    if (showReconduire) {
      boutons.add(_btn(
        context,
        style: _AtBtnStyle.secondaire,
        icon: Icons.update_rounded,
        label: 'Demander une reconduction',
        tooltip: 'Demander une reconduction au Responsable OCP (HMEP)',
        onPressed: () => _reconduire(context, ref),
      ));
    }
    if (showSignalerIncident && !showDeclarerFin) {
      // Visible même pour les non-CEEE pendant l'intervention (sécurité)
      boutons.add(_btn(
        context,
        style: _AtBtnStyle.danger,
        icon: Icons.warning_amber_rounded,
        label: "Signaler un incident / Arrêt d'urgence chantier",
        onPressed: () => _signalerIncident(context, ref),
      ));
    }
    if (showSignalerIncident && roles.isCeee) {
      boutons.add(_btn(
        context,
        style: _AtBtnStyle.danger,
        icon: Icons.warning_amber_rounded,
        label: "Signaler un incident / Arrêt d'urgence chantier",
        onPressed: () => _signalerIncident(context, ref),
      ));
    }
    if ((statut == StatutAt.interventionEnCours ||
            statut == StatutAt.atReconduite) &&
        !showDeclarerFin &&
        isWorkflowParticipant) {
      boutons.add(_banner(
        icon: Icons.engineering_rounded,
        message: "Intervention en cours d'exécution sur le chantier par le CEEE.",
      ));
    }

    // ── ÉTAPE 7 : RÉCEPTION ──
    if (showReceptionner) {
      boutons.add(_btn(
        context,
        style: _AtBtnStyle.primaire,
        icon: Icons.verified_rounded,
        label: 'Réception conjointe & Clôture',
        onPressed: () => context.push('/at/${at.id}/reception'),
      ));
    } else if (statut == StatutAt.finTravauxDeclaree && isWorkflowParticipant) {
      boutons.add(_banner(
        icon: Icons.pending_actions_rounded,
        message: 'Travaux terminés. En attente de la réception conjointe sur le terrain (CEEP + CEEE).',
      ));
    }

    // ── ÉTAPE 8 : ARCHIVAGE ──
    if (showArchiver) {
      boutons.add(_btn(
        context,
        style: _AtBtnStyle.primaire,
        icon: Icons.inventory_2_rounded,
        label: 'Archiver officiellement (durée légale ≥ 1 an)',
        onPressed: () async {
          if (!await _confirmer(context, "Archiver définitivement cette autorisation de travail pour conservation réglementaire (durée légale ≥ 1 an) ?")) return;
          if (!context.mounted) return;
          await _executer(context, ref, (api) => api.archiver(at.id), 'AT archivée avec succès.');
        },
      ));
    }

    // ── AT ARCHIVÉE ──
    if (statut == StatutAt.archivee) {
      boutons.add(_banner(
        icon: Icons.archive_rounded,
        message: 'Autorisation de travail archivée réglementairement. Cycle de vie S-HSE-SEC-31 complet.',
      ));
    }

    // ── PDF OFFICIEL (après HMEE ou clôture) ──
    if (showPdf) {
      boutons.add(const SizedBox(height: 4));
      boutons.add(Padding(
        padding: const EdgeInsets.only(bottom: 8),
        child: FilledButton.icon(
          style: FilledButton.styleFrom(
            backgroundColor: OcpColors.forest,
            foregroundColor: OcpColors.white,
            minimumSize: const Size.fromHeight(46),
            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
          ),
          onPressed: () => context.push('/at/${at.id}/pdf'),
          icon: const Icon(Icons.picture_as_pdf_rounded, size: 20),
          label: const Text(
            'Télécharger le PDF Officiel',
            style: TextStyle(fontWeight: FontWeight.w700, fontSize: 13),
          ),
        ),
      ));
    }

    // ── BANNIÈRE BLOCAGE PDF (si le PDF n'est pas encore disponible) ──
    // ── BANNIÈRE BLOCAGE PDF (motifs calculés côté serveur, §M) ──
    final motifsRefus = at.exportPdfMotifsRefus;
    if (at.exportPdfAutorise == false &&
        motifsRefus.isNotEmpty &&
        isWorkflowParticipant) {
      boutons.add(_pdfBlocageBanniere(motifsRefus));
    }

    if (boutons.isEmpty) return const SizedBox.shrink();

    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        Padding(
          padding: const EdgeInsets.only(top: 8, bottom: 8),
          child: Row(
            children: [
              const Icon(Icons.account_tree_rounded, size: 18, color: OcpColors.forest),
              const SizedBox(width: 8),
              Expanded(
                child: Text(
                  'Actions disponibles (${StatutAt.libelle(at.statut)})',
                  style: const TextStyle(
                    fontFamily: 'SpaceGrotesk',
                    fontWeight: FontWeight.w700,
                    fontSize: 13,
                    color: OcpColors.ink,
                  ),
                ),
              ),
            ],
          ),
        ),
        ...boutons,
      ],
    );
  }

  // ─────────────────────────────────────────────────────────────────────────
  // Détection des visas : voir at_circuit_visas.dart (visaSignePourRole) -
  // détection tri-niveau partagée, alignée sur detectVisa() web et
  // isRoleSigned() de VisaServiceImpl.
  // ─────────────────────────────────────────────────────────────────────────

  // ─────────────────────────────────────────────────────────────────────────
  // Helpers UI
  // ─────────────────────────────────────────────────────────────────────────

  Widget _btn(
    BuildContext context, {
    required _AtBtnStyle style,
    required IconData icon,
    required String label,
    required VoidCallback onPressed,
    String? tooltip,
  }) {
    final Widget button = switch (style) {
      _AtBtnStyle.primaire => Padding(
          padding: const EdgeInsets.only(bottom: 8),
          child: FilledButton.icon(
            style: FilledButton.styleFrom(
              backgroundColor: OcpColors.forest,
              foregroundColor: OcpColors.white,
              minimumSize: const Size.fromHeight(46),
              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
            ),
            onPressed: onPressed,
            icon: Icon(icon, size: 18),
            label: Text(label, style: const TextStyle(fontWeight: FontWeight.w700, fontSize: 13)),
          ),
        ),
      _AtBtnStyle.secondaire => Padding(
          padding: const EdgeInsets.only(bottom: 8),
          child: OutlinedButton.icon(
            style: OutlinedButton.styleFrom(
              foregroundColor: OcpColors.forest,
              side: const BorderSide(color: OcpColors.forest),
              minimumSize: const Size.fromHeight(46),
              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
            ),
            onPressed: onPressed,
            icon: Icon(icon, size: 18),
            label: Text(label, style: const TextStyle(fontWeight: FontWeight.w700, fontSize: 13)),
          ),
        ),
      _AtBtnStyle.danger => Padding(
          padding: const EdgeInsets.only(bottom: 8),
          child: OutlinedButton.icon(
            style: OutlinedButton.styleFrom(
              foregroundColor: OcpColors.errorSoft,
              side: const BorderSide(color: OcpColors.errorSoft),
              minimumSize: const Size.fromHeight(46),
              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
            ),
            onPressed: onPressed,
            icon: Icon(icon, size: 18),
            label: Text(label, style: const TextStyle(fontWeight: FontWeight.w700, fontSize: 13)),
          ),
        ),
    };
    if (tooltip != null) {
      return Tooltip(message: tooltip, child: button);
    }
    return button;
  }

  Widget _banner({required IconData icon, required String message}) {
    return Container(
      margin: const EdgeInsets.only(bottom: 8),
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: OcpColors.surfaceSoft,
        borderRadius: BorderRadius.circular(10),
        border: Border.all(color: OcpColors.borderSoft),
      ),
      child: Row(
        children: [
          Icon(icon, color: OcpColors.forest, size: 20),
          const SizedBox(width: 10),
          Expanded(
            child: Text(
              message,
              style: const TextStyle(fontSize: 12, color: OcpColors.ink, height: 1.3),
            ),
          ),
        ],
      ),
    );
  }

  Widget _pdfBlocageBanniere(List<String> motifs) {
    return Container(
      margin: const EdgeInsets.only(bottom: 8),
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: const Color(0xFFFFF8E1),
        borderRadius: BorderRadius.circular(10),
        border: Border.all(color: const Color(0xFFFFB300)),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Row(
            children: [
              Icon(Icons.warning_amber_rounded, color: Color(0xFFFFB300), size: 18),
              SizedBox(width: 8),
              Expanded(
                child: Text(
                  'Conditions requises pour télécharger le PDF officiel :',
                  style: TextStyle(
                    fontWeight: FontWeight.w700,
                    fontSize: 12,
                    color: Color(0xFF7A5800),
                  ),
                ),
              ),
            ],
          ),
          const SizedBox(height: 6),
          ...motifs.map((m) => Padding(
                padding: const EdgeInsets.only(left: 8, top: 2),
                child: Row(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Text('• ', style: TextStyle(color: Color(0xFF7A5800), fontSize: 12)),
                    Expanded(
                      child: Text(m, style: const TextStyle(color: Color(0xFF7A5800), fontSize: 12, height: 1.3)),
                    ),
                  ],
                ),
              )),
        ],
      ),
    );
  }

  // ─────────────────────────────────────────────────────────────────────────
  // Dialogue Démarrage - Readiness Check §8.3 (identique au web :
  // contrôles chargés depuis GET /intervention/readiness, bouton désactivé
  // tant qu'une condition bloquante échoue, POST /intervention/start).
  // ─────────────────────────────────────────────────────────────────────────

  Future<void> _dialogueDemarrage(BuildContext context, WidgetRef ref) async {
    ReadinessCheck? readiness;
    String? erreur;
    bool chargementLance = false;

    await showDialog(
      context: context,
      builder: (ctx) => StatefulBuilder(
        builder: (ctx, setModalState) {
          // Chargement initial des contrôles serveur (une seule fois).
          if (!chargementLance) {
            chargementLance = true;
            () async {
              try {
                final r = await ref.read(atApiProvider).readiness(at.id);
                if (ctx.mounted) setModalState(() => readiness = r);
              } catch (e) {
                if (ctx.mounted) setModalState(() => erreur = e.toString());
              }
            }();
          }

          final checks = readiness?.checks ?? const <ReadinessCheckItem>[];
          final bloquantesKo =
              checks.where((c) => !c.passed && c.blocking).toList();

          return AlertDialog(
            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
            title: const Row(
              children: [
                Icon(Icons.play_circle_fill_rounded, color: OcpColors.forest, size: 24),
                SizedBox(width: 8),
                Expanded(
                  child: Text('Démarrage des travaux (CEEE)',
                      style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
                ),
              ],
            ),
            content: SingleChildScrollView(
              child: Column(
                mainAxisSize: MainAxisSize.min,
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: [
                  if (readiness == null && erreur == null) ...[
                    const SizedBox(height: 8),
                    const Center(
                      child: Padding(
                        padding: EdgeInsets.all(16),
                        child: CircularProgressIndicator(color: OcpColors.forest),
                      ),
                    ),
                    const Center(
                      child: Text('Chargement des contrôles pré-démarrage...',
                          style: TextStyle(fontSize: 12, color: OcpColors.slate)),
                    ),
                  ] else if (erreur != null) ...[
                    Container(
                      padding: const EdgeInsets.all(12),
                      decoration: BoxDecoration(
                        color: OcpColors.errorSoft,
                        borderRadius: BorderRadius.circular(10),
                      ),
                      child: Text(
                        'Readiness check indisponible : $erreur',
                        style: const TextStyle(fontSize: 12, color: OcpColors.errorSoft),
                      ),
                    ),
                  ] else ...[
                    if (readiness!.numero != null)
                      Text(
                        'AT ${readiness!.numero}'
                        '${readiness!.zone != null ? ' · Zone ${readiness!.zone}' : ''}',
                        style: const TextStyle(fontSize: 12, color: OcpColors.slate),
                      ),
                    const SizedBox(height: 8),
                    ...checks.map((c) => ListTile(
                          dense: true,
                          contentPadding: EdgeInsets.zero,
                          leading: Icon(
                            c.passed
                                ? Icons.check_circle_rounded
                                : (c.blocking
                                    ? Icons.cancel_rounded
                                    : Icons.warning_amber_rounded),
                            size: 20,
                            color: c.passed
                                ? OcpColors.success
                                : (c.blocking ? OcpColors.errorSoft : OcpColors.warning),
                          ),
                          title: Text(c.label ?? c.code ?? 'Contrôle',
                              style: const TextStyle(fontSize: 12)),
                          subtitle: (c.message ?? '').isEmpty
                              ? null
                              : Text(c.message!,
                                  style: const TextStyle(fontSize: 11, color: OcpColors.slate)),
                        )),
                    if (bloquantesKo.isNotEmpty)
                      Container(
                        margin: const EdgeInsets.only(top: 8),
                        padding: const EdgeInsets.all(12),
                        decoration: BoxDecoration(
                          color: OcpColors.errorSoft.withValues(alpha: 0.15),
                          borderRadius: BorderRadius.circular(10),
                          border: Border.all(color: OcpColors.errorSoft),
                        ),
                        child: Text(
                          '${bloquantesKo.length} condition(s) bloquante(s) détectée(s). Résolvez-les avant de démarrer.',
                          style: const TextStyle(fontSize: 12, color: OcpColors.errorSoft, fontWeight: FontWeight.w600),
                        ),
                      ),
                  ],
                ],
              ),
            ),
            actions: [
              TextButton(
                onPressed: () => Navigator.of(ctx).pop(),
                child: const Text('Annuler'),
              ),
              FilledButton(
                style: FilledButton.styleFrom(backgroundColor: OcpColors.forest),
                onPressed: (readiness != null && bloquantesKo.isEmpty)
                    ? () async {
                        Navigator.of(ctx).pop();
                        if (!context.mounted) return;
                        await _executer(
                          context,
                          ref,
                          (api) => api.demarrerIntervention(at.id),
                          'Démarrage des travaux enregistré avec succès. Statut : INTERVENTION EN COURS.',
                        );
                      }
                    : null,
                child: const Text('Confirmer le démarrage'),
              ),
            ],
          );
        },
      ),
    );
  }

  // ─────────────────────────────────────────────────────────────────────────
  // Dialogue Fin des Travaux - POST /intervention/end (payload identique au
  // web : travauxRealises + materielRetire + zoneNettoyee + protectionsRetablies).
  // ─────────────────────────────────────────────────────────────────────────

  Future<void> _dialogueFinTravaux(BuildContext context, WidgetRef ref) async {
    bool materielRetire = false;
    bool zoneNettoyee = false;
    bool protectionsRetablies = false;
    final rapportController = TextEditingController();

    await showDialog(
      context: context,
      builder: (ctx) => StatefulBuilder(
        builder: (ctx, setDialogState) => AlertDialog(
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
          title: const Text('Déclaration de fin des travaux (CEEE)',
              style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
          content: SingleChildScrollView(
            child: Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                const Text(
                  'Vérifications préalables obligatoires sur le chantier :',
                  style: TextStyle(fontWeight: FontWeight.w700, fontSize: 13, color: OcpColors.ink),
                ),
                const SizedBox(height: 8),
                CheckboxListTile(
                  dense: true,
                  title: const Text('Matériel et outillage entièrement évacués', style: TextStyle(fontSize: 12)),
                  value: materielRetire,
                  onChanged: (v) => setDialogState(() => materielRetire = v ?? false),
                ),
                CheckboxListTile(
                  dense: true,
                  title:
                      const Text("Zone d'intervention nettoyée et dégagée", style: TextStyle(fontSize: 12)),
                  value: zoneNettoyee,
                  onChanged: (v) => setDialogState(() => zoneNettoyee = v ?? false),
                ),
                CheckboxListTile(
                  dense: true,
                  title: const Text('Dispositifs de sécurité et protections rétablis',
                      style: TextStyle(fontSize: 12)),
                  value: protectionsRetablies,
                  onChanged: (v) => setDialogState(() => protectionsRetablies = v ?? false),
                ),
                const SizedBox(height: 10),
                TextField(
                  controller: rapportController,
                  maxLines: 3,
                  decoration: const InputDecoration(
                    labelText: 'Rapport succinct des travaux réalisés',
                    hintText: 'Préciser les opérations effectuées, état final...',
                  ),
                ),
              ],
            ),
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.of(ctx).pop(),
              child: const Text('Annuler'),
            ),
            FilledButton(
              style: FilledButton.styleFrom(backgroundColor: OcpColors.forest),
              onPressed: (materielRetire && zoneNettoyee && protectionsRetablies)
                  ? () async {
                      Navigator.of(ctx).pop();
                      if (!context.mounted) return;
                      final api = ref.read(atApiProvider);
                      try {
                        await api.declarerFinTravaux(
                          at.id,
                          travauxRealises: rapportController.text,
                          materielRetire: materielRetire,
                          zoneNettoyee: zoneNettoyee,
                          protectionsRetablies: protectionsRetablies,
                        );
                        ref.invalidate(atDetailProvider(at.id));
                        ref.invalidate(visasProvider(at.id));
                        if (context.mounted) {
                          ScaffoldMessenger.of(context).showSnackBar(
                            const SnackBar(
                              backgroundColor: OcpColors.success,
                              content: Text(
                                  'Fin des travaux déclarée par le CEEE avec succès. Prêt pour réception conjointe.'),
                            ),
                          );
                        }
                      } catch (e) {
                        if (context.mounted) {
                          ScaffoldMessenger.of(context).showSnackBar(
                            SnackBar(
                              backgroundColor: OcpColors.errorSoft,
                              content: Text(e is Failure ? e.message : 'Action échouée : $e'),
                            ),
                          );
                        }
                      }
                    }
                  : null,
              child: const Text('Déclarer la Fin'),
            ),
          ],
        ),
      ),
    );
  }

  // ─────────────────────────────────────────────────────────────────────────
  // Dialogue Reconduction (CEEE → HMEP) - identique au web :
  // POST /reconductions avec nouvelleDateFin + motif obligatoires.
  // ─────────────────────────────────────────────────────────────────────────

  Future<void> _reconduire(BuildContext context, WidgetRef ref) async {
    final motifController = TextEditingController();
    DateTime? nouvelleDateFin;

    final conf = await showDialog<bool>(
      context: context,
      builder: (ctx) => StatefulBuilder(
        builder: (ctx, setState) {
          final dateInitiale = at.dateFin ?? DateTime.now();
          return AlertDialog(
            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
            title: const Text('Demande de reconduction de poste',
                style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
            content: SingleChildScrollView(
              child: Column(
                mainAxisSize: MainAxisSize.min,
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: [
                  const Text(
                    "Prolonger la durée d'intervention pour le poste suivant.\nDécision requise par HMEP.",
                    style: TextStyle(fontSize: 12, color: OcpColors.slate),
                  ),
                  const SizedBox(height: 12),
                  InkWell(
                    borderRadius: BorderRadius.circular(10),
                    onTap: () async {
                      final picked = await showDatePicker(
                        context: ctx,
                        initialDate: nouvelleDateFin ?? dateInitiale,
                        firstDate: DateTime.now(),
                        lastDate: DateTime.now().add(const Duration(days: 365)),
                      );
                      if (picked != null) setState(() => nouvelleDateFin = picked);
                    },
                    child: InputDecorator(
                      decoration: const InputDecoration(
                        labelText: 'Nouvelle date de fin *',
                        suffixIcon: Icon(Icons.calendar_today_rounded, size: 18),
                      ),
                      child: Text(
                        nouvelleDateFin == null
                            ? 'Sélectionner une date'
                            : '${nouvelleDateFin!.day.toString().padLeft(2, '0')}/${nouvelleDateFin!.month.toString().padLeft(2, '0')}/${nouvelleDateFin!.year}',
                        style: TextStyle(
                          fontSize: 13,
                          color: nouvelleDateFin == null ? OcpColors.slate : OcpColors.ink,
                        ),
                      ),
                    ),
                  ),
                  const SizedBox(height: 12),
                  TextField(
                    controller: motifController,
                    maxLines: 2,
                    decoration: const InputDecoration(
                      labelText: 'Motif de la reconduction *',
                      hintText: 'Ex: Finitions soudure requérant un poste supplémentaire...',
                    ),
                  ),
                ],
              ),
            ),
            actions: [
              TextButton(onPressed: () => Navigator.pop(ctx, false), child: const Text('Annuler')),
              FilledButton(
                style: FilledButton.styleFrom(backgroundColor: OcpColors.forest),
                onPressed: (nouvelleDateFin != null && motifController.text.trim().isNotEmpty)
                    ? () => Navigator.pop(ctx, true)
                    : null,
                child: const Text('Soumettre au HMEP'),
              ),
            ],
          );
        },
      ),
    );

    if (conf == true && nouvelleDateFin != null && context.mounted) {
      final api = ref.read(atApiProvider);
      try {
        await api.demanderReconduction(
          atId: at.id,
          nouvelleDateFin:
              '${nouvelleDateFin!.year.toString().padLeft(4, '0')}-${nouvelleDateFin!.month.toString().padLeft(2, '0')}-${nouvelleDateFin!.day.toString().padLeft(2, '0')}',
          motif: motifController.text.trim(),
        );
        ref.invalidate(atDetailProvider(at.id));
        ref.invalidate(visasProvider(at.id));
        if (context.mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(
              backgroundColor: OcpColors.success,
              content: Text('Demande de reconduction soumise au HMEP. Vous serez notifié de la décision.'),
            ),
          );
        }
      } catch (e) {
        if (context.mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              backgroundColor: OcpColors.errorSoft,
              content: Text(e is Failure ? e.message : 'Action échouée : $e'),
            ),
          );
        }
      }
    }
  }

  // ─────────────────────────────────────────────────────────────────────────
  // Signalement d'incident / Arrêt d'urgence
  // ─────────────────────────────────────────────────────────────────────────

  Future<void> _signalerIncident(BuildContext context, WidgetRef ref) async {
    final controller = TextEditingController();
    final conf = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
        title: const Row(
          children: [
            Icon(Icons.warning_rounded, color: OcpColors.errorSoft, size: 22),
            SizedBox(width: 8),
            Text("Arrêt d'urgence chantier", style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
          ],
        ),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            const Text(
              "Signaler un événement inattendu, une anomalie ou un incident nécessitant l'arrêt immédiat des opérations.",
              style: TextStyle(fontSize: 12, color: OcpColors.slate),
            ),
            const SizedBox(height: 10),
            TextField(
              controller: controller,
              maxLines: 3,
              decoration: const InputDecoration(
                labelText: "Description de l'anomalie / incident *",
                hintText: 'Décrire le risque constaté...',
              ),
            ),
          ],
        ),
        actions: [
          TextButton(onPressed: () => Navigator.pop(ctx, false), child: const Text('Annuler')),
          FilledButton(
            style: FilledButton.styleFrom(backgroundColor: OcpColors.errorSoft),
            onPressed: () => Navigator.pop(ctx, true),
            child: const Text("Signaler l'arrêt"),
          ),
        ],
      ),
    );

    if (conf == true && context.mounted) {
      await _executer(
        context,
        ref,
        (api) => api.signalerIncident(
          at.id,
          controller.text.trim().isNotEmpty ? controller.text.trim() : "Arrêt d'urgence",
        ),
        "Incident enregistré. Les superviseurs et HSE ont été alertés.",
      );
    }
  }

  // ─────────────────────────────────────────────────────────────────────────
  // Helpers communs
  // ─────────────────────────────────────────────────────────────────────────

  Future<void> _executer(
    BuildContext context,
    WidgetRef ref,
    Future<dynamic> Function(AtApi api) action,
    String messageSucces,
  ) async {
    final api = ref.read(atApiProvider);
    try {
      await action(api);
      ref.invalidate(atDetailProvider(at.id));
      ref.invalidate(visasProvider(at.id));
      if (context.mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(backgroundColor: OcpColors.success, content: Text(messageSucces)),
        );
      }
    } catch (e) {
      if (context.mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            backgroundColor: OcpColors.errorSoft,
            content: Text(e is Failure ? e.message : 'Action échouée : $e'),
          ),
        );
      }
    }
  }

  Future<bool> _confirmer(BuildContext context, String message) async {
    final res = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
        title: const Text('Confirmation', style: TextStyle(fontWeight: FontWeight.bold)),
        content: Text(message),
        actions: [
          TextButton(onPressed: () => Navigator.pop(ctx, false), child: const Text('Annuler')),
          FilledButton(
            style: FilledButton.styleFrom(backgroundColor: OcpColors.forest),
            onPressed: () => Navigator.pop(ctx, true),
            child: const Text('Confirmer'),
          ),
        ],
      ),
    );
    return res ?? false;
  }
}
