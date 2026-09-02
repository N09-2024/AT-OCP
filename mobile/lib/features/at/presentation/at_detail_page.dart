/// Détail d'une Autorisation de Travail - Alignement 100 % avec le Web React & Spring Boot :
/// - Header avec statut, numéro d'AT, actions rapides (PDF officiel si débloqué, Assistant IA)
/// - Workflow Stepper officiel S-HSE-SEC-31
/// - Actions prioritaires selon rôle dynamique (CEEP, CEEE, HMEP, HMEE, HCEP, HCEE, ADMIN)
/// - Navigation par onglets clairs :
///   1. 📄 Formulaire & Visite (§8.2 - GPS & Photo)
///   2. 🛡️ Risques & Mesures HSE
///   3. 📋 Permis rattachés (IA Gemini)
///   4. ✍️ Visas & Signatures
///   5. 🕒 Historique & Audit
library;

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../../core/errors/failures.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/utils/app_date.dart';
import '../../../core/widgets/states.dart';
import '../../../core/widgets/statut_chip.dart';
import '../../../core/widgets/workflow_stepper.dart';
import '../../auth/presentation/auth_controller.dart';
import '../../visas/data/visa.dart';
import '../data/models/autorisation_travail.dart';
import 'at_circuit_visas.dart';
import 'at_providers.dart';
import 'at_roles.dart';
import 'at_visas_page.dart';
import 'at_workflow_actions.dart';

class AtDetailPage extends ConsumerStatefulWidget {
  final String atId;
  const AtDetailPage({super.key, required this.atId});

  @override
  ConsumerState<AtDetailPage> createState() => _AtDetailPageState();
}

class _AtDetailPageState extends ConsumerState<AtDetailPage> with SingleTickerProviderStateMixin {
  late TabController _tabController;

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 5, vsync: this);
  }

  @override
  void dispose() {
    _tabController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final detail = ref.watch(atDetailProvider(widget.atId));

    return Scaffold(
      backgroundColor: OcpColors.sage,
      appBar: AppBar(
        backgroundColor: OcpColors.forest,
        foregroundColor: OcpColors.white,
        elevation: 0,
        title: Text(
          detail.valueOrNull?.numero ?? 'Détail AT',
          style: const TextStyle(
            fontFamily: 'SpaceGrotesk',
            fontWeight: FontWeight.w700,
            fontSize: 16,
            color: OcpColors.white,
          ),
        ),
        actions: [
          IconButton(
            tooltip: 'Assistant IA HSE',
            icon: const Icon(Icons.psychology_alt_rounded, color: OcpColors.mint),
            onPressed: () {
              final at = detail.valueOrNull;
              context.push(
                '/assistant',
                extra: at == null
                    ? null
                    : {
                        'id': at.id,
                        'numero': at.numero,
                        'objet': at.objet,
                        'descriptionTravaux': at.descriptionTravaux,
                        'statut': at.statut,
                      },
              );
            },
          ),
          IconButton(
            tooltip: 'Actualiser',
            icon: const Icon(Icons.refresh_rounded, color: OcpColors.white),
            onPressed: () => ref.invalidate(atDetailProvider(widget.atId)),
          ),
        ],
      ),
      body: detail.when(
        loading: () => const LoadingState(message: 'Chargement de l\'autorisation de travail...'),
        error: (error, _) => ErrorState(
          message: error is Failure ? error.message : 'Erreur de chargement du dossier.',
          onRetry: () => ref.invalidate(atDetailProvider(widget.atId)),
        ),
        data: (value) => _AtDetailBody(
          at: value,
          tabController: _tabController,
        ),
      ),
    );
  }
}

class _AtDetailBody extends ConsumerWidget {
  final AutorisationTravail at;
  final TabController tabController;

  const _AtDetailBody({
    required this.at,
    required this.tabController,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    // ── Bouton PDF - identique au web (showPdf = pdfUnlocked && participant) ─
    final visas = ref.watch(visasProvider(at.id)).valueOrNull ?? [];
    final etat = CircuitVisasEtat.resolve(visas: visas, at: at);
    final roles = AtRoles.resolve(session: ref.watch(sessionProvider), at: at);
    final bool pdfUnlocked = etat.hmee ||
        etat.prochainRoleRequis == null ||
        at.statut == StatutAt.travauxReceptiones ||
        at.statut == StatutAt.archivee ||
        at.statut == 'CLOTUREE';
    final bool showPdf =
        pdfUnlocked && at.exportPdfAutorise != false && roles.isWorkflowParticipant;

    return Column(
      children: [
        // ── Header Statut & Actions Rapides ────────────────────────────
        Container(
          color: OcpColors.white,
          padding: const EdgeInsets.fromLTRB(16, 12, 16, 10),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              Row(
                children: [
                  StatutChip(statut: at.statut),
                  const Spacer(),
                  if (showPdf)
                    FilledButton.icon(
                      style: FilledButton.styleFrom(
                        backgroundColor: OcpColors.forest,
                        foregroundColor: OcpColors.white,
                        padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
                        minimumSize: const Size(0, 32),
                        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(6)),
                      ),
                      onPressed: () => context.push('/at/${at.id}/pdf'),
                      icon: const Icon(Icons.picture_as_pdf_rounded, size: 14),
                      label: const Text('PDF Officiel', style: TextStyle(fontSize: 11, fontWeight: FontWeight.bold)),
                    ),
                ],
              ),
              const SizedBox(height: 8),
              Text(
                at.objet ?? 'Sans objet spécifié',
                style: const TextStyle(
                  fontFamily: 'SpaceGrotesk',
                  fontWeight: FontWeight.w700,
                  fontSize: 15,
                  color: OcpColors.ink,
                ),
              ),
            ],
          ),
        ),

        // ── Onglets Navigation ─────────────────────────────────────────
        Container(
          color: OcpColors.white,
          child: TabBar(
            controller: tabController,
            isScrollable: true,
            tabAlignment: TabAlignment.start,
            labelColor: OcpColors.forest,
            unselectedLabelColor: OcpColors.slate,
            indicatorColor: OcpColors.forest,
            indicatorWeight: 3,
            labelStyle: const TextStyle(fontWeight: FontWeight.w700, fontSize: 13),
            unselectedLabelStyle: const TextStyle(fontWeight: FontWeight.w500, fontSize: 13),
            tabs: const [
              Tab(text: 'Actions & Workflow'),
              Tab(text: 'Détails & Visite §8.2'),
              Tab(text: 'Risques & Mesures'),
              Tab(text: 'Permis rattachés'),
              Tab(text: 'Signatures'),
            ],
          ),
        ),

        // ── Contenu des onglets ────────────────────────────────────────
        Expanded(
          child: TabBarView(
            controller: tabController,
            children: [
              // Onglet 1 : Actions & Workflow
              _TabActionsAndWorkflow(at: at),

              // Onglet 2 : Détails & Visite préalable §8.2
              _TabDetailsAndVisite(at: at),

              // Onglet 3 : Risques & Mesures
              _TabRisquesAndMesures(at: at),

              // Onglet 4 : Permis
              _TabPermis(at: at),

              // Onglet 5 : Signatures & Historique
              _TabSignatures(at: at),
            ],
          ),
        ),
      ],
    );
  }
}

// ─────────────────────────────────────────────────────────────────────────────
// Onglet 1 : Actions & Workflow
// ─────────────────────────────────────────────────────────────────────────────

class _TabActionsAndWorkflow extends StatelessWidget {
  final AutorisationTravail at;
  const _TabActionsAndWorkflow({required this.at});

  @override
  Widget build(BuildContext context) {
    return ListView(
      padding: const EdgeInsets.all(16),
      children: [
        // Stepper officiel S-HSE-SEC-31
        WorkflowStepper(statut: at.statut),
        const SizedBox(height: 12),

        // Actions dynamiques attribuées selon le rôle
        AtWorkflowActions(at: at),
        const SizedBox(height: 14),

        // Raccourcis utiles
        _card(
          title: 'Accès rapides au dossier',
          children: [
            ListTile(
              dense: true,
              leading: const Icon(Icons.photo_library_outlined, color: OcpColors.forest),
              title: const Text('Galerie photos (visite & réception)'),
              trailing: const Icon(Icons.chevron_right_rounded),
              onTap: () => context.push('/at/${at.id}/photos'),
            ),
            const Divider(height: 1),
            ListTile(
              dense: true,
              leading: const Icon(Icons.history_rounded, color: OcpColors.forest),
              title: const Text('Journal d\'audit et historique complet'),
              trailing: const Icon(Icons.chevron_right_rounded),
              onTap: () => context.push('/at/${at.id}/historique'),
            ),
          ],
        ),
      ],
    );
  }
}

// ─────────────────────────────────────────────────────────────────────────────
// Onglet 2 : Détails & Visite préalable §8.2
// ─────────────────────────────────────────────────────────────────────────────

class _TabDetailsAndVisite extends StatelessWidget {
  final AutorisationTravail at;
  const _TabDetailsAndVisite({required this.at});

  @override
  Widget build(BuildContext context) {
    return ListView(
      padding: const EdgeInsets.all(16),
      children: [
        _card(
          title: 'Description de l\'intervention',
          children: [
            _row('Objet', at.objet ?? '-'),
            _row('Description détaillée', at.descriptionTravaux ?? '-'),
            _row('Document source', _sourceLabel(at)),
          ],
        ),
        const SizedBox(height: 12),

        _card(
          title: 'Périmètre & Affectation',
          children: [
            _row('Zone Propriétaire (P)', at.zoneProprietaireNom ?? '-'),
            _row('Zone Exécutante (E)', at.zoneExecutanteNom ?? '-'),
            _row('Demandeur CEEP (P)', at.g1NomCeep ?? at.proprietaireBrouillonNomComplet ?? '-'),
            _row('Service Intervenant (E)', at.servicesIntervenants ?? '-'),
            _row('Entreprise extérieure', at.entreprisesIntervenantes ?? 'Régie interne OCP'),
          ],
        ),
        const SizedBox(height: 12),

        _card(
          title: 'Planning & Validité',
          children: [
            _row('Période', '${AppDate.date(at.dateDebut)} → ${AppDate.date(at.dateFin)}'),
            _row('Horaires', '${AppDate.heureSimple(at.heureDebut)} → ${AppDate.heureSimple(at.heureFin)}'),
          ],
        ),
        const SizedBox(height: 12),

        _card(
          title: 'Visite Préalable Conjointe (§8.2 Standard OCP)',
          children: [
            _row('Visite effectuée', at.visiteEffectuee == true ? '✅ Validée conjointement' : '⚠️ Non validée'),
            if (at.latitude != null && at.longitude != null)
              _row('Coordonnées GPS', 'Lat ${at.latitude!.toStringAsFixed(5)}, Lng ${at.longitude!.toStringAsFixed(5)}'),
            if ((at.visiteCommentaire ?? '').isNotEmpty)
              _row('Constats terrain', at.visiteCommentaire!),
          ],
        ),
      ],
    );
  }
}

// ─────────────────────────────────────────────────────────────────────────────
// Onglet 3 : Risques & Mesures HSE
// ─────────────────────────────────────────────────────────────────────────────

class _TabRisquesAndMesures extends StatelessWidget {
  final AutorisationTravail at;
  const _TabRisquesAndMesures({required this.at});

  @override
  Widget build(BuildContext context) {
    return ListView(
      padding: const EdgeInsets.all(16),
      children: [
        _card(
          title: 'A. Risques liés aux travaux (${at.risquesIds.length})',
          children: [
            if (at.risquesIds.isEmpty)
              const Text('Aucun risque majeur sélectionné.', style: TextStyle(fontSize: 12, color: OcpColors.slate))
            else
              Wrap(
                spacing: 6,
                runSpacing: 6,
                children: at.risquesIds
                    .map((r) => Chip(
                          label: Text(r, style: const TextStyle(fontSize: 11, fontWeight: FontWeight.w600)),
                          backgroundColor: OcpColors.errorSoft,
                        ))
                    .toList(),
              ),
          ],
        ),
        const SizedBox(height: 12),

        _card(
          title: 'B. Mesures de sécurité & Consignation (${at.mesuresIds.length})',
          children: [
            if (at.mesuresIds.isEmpty)
              const Text('Aucune mesure de consignation active.', style: TextStyle(fontSize: 12, color: OcpColors.slate))
            else
              Wrap(
                spacing: 6,
                runSpacing: 6,
                children: at.mesuresIds
                    .map((m) => Chip(
                          label: Text(m, style: const TextStyle(fontSize: 11, fontWeight: FontWeight.w600)),
                          backgroundColor: OcpColors.forestSoft,
                        ))
                    .toList(),
              ),
          ],
        ),
        const SizedBox(height: 12),

        _card(
          title: 'C. Moyens d\'accès (${at.moyensAccesIds.length})',
          children: [
            if (at.moyensAccesIds.isEmpty)
              const Text('Moyens d\'accès standards.', style: TextStyle(fontSize: 12, color: OcpColors.slate))
            else
              Wrap(
                spacing: 6,
                runSpacing: 6,
                children: at.moyensAccesIds
                    .map((a) => Chip(
                          label: Text(a, style: const TextStyle(fontSize: 11, fontWeight: FontWeight.w600)),
                          backgroundColor: OcpColors.surfaceSoft,
                        ))
                    .toList(),
              ),
          ],
        ),
        const SizedBox(height: 12),

        _card(
          title: 'D. Équipements de Protection Individuelle (${at.episIds.length})',
          children: [
            if (at.episIds.isEmpty)
              const Text('EPI standards (casque, chaussures, gilet).', style: TextStyle(fontSize: 12, color: OcpColors.slate))
            else
              Wrap(
                spacing: 6,
                runSpacing: 6,
                children: at.episIds
                    .map((e) => Chip(
                          label: Text(e, style: const TextStyle(fontSize: 11, fontWeight: FontWeight.w600)),
                          backgroundColor: OcpColors.mintSoft,
                        ))
                    .toList(),
              ),
          ],
        ),
        const SizedBox(height: 12),

        if ((at.mesuresSecuriteExecutant ?? '').isNotEmpty)
          _card(
            title: 'F. Mesures de sécurité de l\'exécutant',
            children: [
              Text(
                at.mesuresSecuriteExecutant!,
                style: const TextStyle(fontSize: 12, color: OcpColors.ink, height: 1.4),
              ),
            ],
          ),
      ],
    );
  }
}

// ─────────────────────────────────────────────────────────────────────────────
// Onglet 4 : Permis rattachés
// ─────────────────────────────────────────────────────────────────────────────

class _TabPermis extends StatelessWidget {
  final AutorisationTravail at;
  const _TabPermis({required this.at});

  @override
  Widget build(BuildContext context) {
    return ListView(
      padding: const EdgeInsets.all(16),
      children: [
        _card(
          title: 'Permis de travail complémentaires (${at.permisIds.length})',
          children: [
            if (at.permisIds.isEmpty)
              const Padding(
                padding: EdgeInsets.symmetric(vertical: 8),
                child: Text('Aucun permis complémentaire rattaché à cette AT.', style: TextStyle(fontSize: 12, color: OcpColors.slate)),
              )
            else
              ...at.permisIds.map(
                (p) => ListTile(
                  contentPadding: EdgeInsets.zero,
                  dense: true,
                  leading: const Icon(Icons.badge_outlined, color: OcpColors.forest),
                  title: Text(p, style: const TextStyle(fontWeight: FontWeight.w700, fontSize: 13)),
                  trailing: const Chip(
                    label: Text('Rattaché', style: TextStyle(fontSize: 10, fontWeight: FontWeight.bold)),
                    backgroundColor: OcpColors.forestSoft,
                  ),
                ),
              ),
            const SizedBox(height: 8),
            OutlinedButton.icon(
              style: OutlinedButton.styleFrom(minimumSize: const Size.fromHeight(42)),
              onPressed: () => context.push('/at/${at.id}/permis'),
              icon: const Icon(Icons.upload_file_rounded, size: 16),
              label: const Text('Gérer les documents de permis & Validation IA'),
            ),
          ],
        ),
      ],
    );
  }
}

// ─────────────────────────────────────────────────────────────────────────────
// Onglet 5 : Signatures & Visas
// ─────────────────────────────────────────────────────────────────────────────

class _TabSignatures extends ConsumerWidget {
  final AutorisationTravail at;
  const _TabSignatures({required this.at});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final visasAsync = ref.watch(visasProvider(at.id));

    return ListView(
      padding: const EdgeInsets.all(16),
      children: [
        _card(
          title: 'Section G : Circuit officiel des Visas & Signatures',
          children: [
            const Text(
              'Ordre séquentiel réglementaire (Standard S-HSE-SEC-31) :\n'
              '1. CEEP (Demandeur) → 2. CEEE (Exécutant) → 3. HCEP (Propriétaire) → 4. HCEE → 5. HMEP → 6. HMEE (Validation finale & Déblocage PDF)',
              style: TextStyle(fontSize: 12, color: OcpColors.slate, height: 1.4),
            ),
            const SizedBox(height: 12),
            OutlinedButton.icon(
              style: OutlinedButton.styleFrom(minimumSize: const Size.fromHeight(42)),
              onPressed: () => context.push('/at/${at.id}/visas'),
              icon: const Icon(Icons.draw_rounded, size: 16),
              label: const Text('Ouvrir la feuille complète des visas & Signatures'),
            ),
          ],
        ),
        const SizedBox(height: 12),

        visasAsync.when(
          loading: () => const Center(
            child: Padding(
              padding: EdgeInsets.all(24),
              child: CircularProgressIndicator(color: OcpColors.forest),
            ),
          ),
          error: (e, _) => Card(
            color: OcpColors.warningSoft,
            child: Padding(
              padding: const EdgeInsets.all(14),
              child: Text(
                'Visas : ${e is Failure ? e.message : 'Non initialisés ou en attente.'}',
                style: const TextStyle(fontSize: 12, color: OcpColors.warning),
              ),
            ),
          ),
          data: (items) {
            final sorted = List<Visa>.from(items)
              ..sort((a, b) => (a.ordre ?? 99).compareTo(b.ordre ?? 99));

            if (sorted.isEmpty) {
              return Card(
                color: OcpColors.surfaceSoft,
                child: const Padding(
                  padding: EdgeInsets.all(16),
                  child: Text(
                    'Les visas seront créés automatiquement dès la soumission officielle de l\'AT.',
                    style: TextStyle(fontSize: 12, color: OcpColors.slate),
                  ),
                ),
              );
            }

            return Column(
              children: sorted.map((v) {
                final isSigned = v.signaturePresente || v.statut == StatutVisa.valide;
                return Card(
                  elevation: 0,
                  margin: const EdgeInsets.only(bottom: 8),
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(12),
                    side: BorderSide(
                      color: isSigned ? OcpColors.success : OcpColors.borderSoft,
                    ),
                  ),
                  child: ListTile(
                    leading: CircleAvatar(
                      backgroundColor: isSigned ? OcpColors.forestSoft : OcpColors.surfaceSoft,
                      child: Icon(
                        isSigned ? Icons.verified_rounded : Icons.pending_actions_rounded,
                        color: isSigned ? OcpColors.forest : OcpColors.slate,
                        size: 20,
                      ),
                    ),
                    title: Text(
                      v.commentaire ?? (v.ordre != null ? 'Visa Étape ${v.ordre}' : 'Visa réglementaire'),
                      style: const TextStyle(fontWeight: FontWeight.w700, fontSize: 13),
                    ),
                    subtitle: Text(
                      isSigned
                          ? 'Signé par ${v.utilisateurNomComplet ?? 'Signataire'} le ${AppDate.dateHeure(v.dateSignature ?? v.dateVisa)}'
                          : 'En attente de signature',
                      style: TextStyle(
                        fontSize: 11,
                        color: isSigned ? OcpColors.forest : OcpColors.slate,
                      ),
                    ),
                    trailing: Chip(
                      label: Text(
                        isSigned ? 'Signé' : 'En attente',
                        style: const TextStyle(fontSize: 10, fontWeight: FontWeight.bold),
                      ),
                      backgroundColor: isSigned ? OcpColors.forestSoft : OcpColors.warningSoft,
                    ),
                    onTap: () => context.push('/at/${at.id}/visas'),
                  ),
                );
              }).toList(),
            );
          },
        ),
      ],
    );
  }
}

// ─────────────────────────────────────────────────────────────────────────────
// Helpers de rendu UI
// ─────────────────────────────────────────────────────────────────────────────

Widget _card({required String title, required List<Widget> children}) {
  return Card(
    elevation: 0,
    color: OcpColors.white,
    shape: RoundedRectangleBorder(
      borderRadius: BorderRadius.circular(12),
      side: const BorderSide(color: OcpColors.borderSoft),
    ),
    child: Padding(
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Text(
            title,
            style: const TextStyle(
              fontFamily: 'SpaceGrotesk',
              fontWeight: FontWeight.w700,
              fontSize: 14,
              color: OcpColors.forest,
            ),
          ),
          const SizedBox(height: 12),
          ...children,
        ],
      ),
    ),
  );
}

Widget _row(String label, String value) {
  return Padding(
    padding: const EdgeInsets.symmetric(vertical: 4),
    child: Row(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        SizedBox(
          width: 140,
          child: Text(label, style: const TextStyle(fontSize: 12, color: OcpColors.slate)),
        ),
        Expanded(
          child: Text(
            value,
            style: const TextStyle(fontSize: 12, fontWeight: FontWeight.w600, color: OcpColors.ink),
          ),
        ),
      ],
    ),
  );
}

String _sourceLabel(AutorisationTravail at) {
  final parts = <String>[];
  if (at.typeDocumentSource != null && at.typeDocumentSource!.isNotEmpty) {
    parts.add(at.typeDocumentSource!);
  }
  if (at.documentSourceNumero != null && at.documentSourceNumero!.isNotEmpty) {
    parts.add(at.documentSourceNumero!);
  }
  return parts.isEmpty ? '-' : parts.join(' ');
}
