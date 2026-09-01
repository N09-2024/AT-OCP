/// Détail d'une AT — GET /autorisations-travail/{id}.
/// Affiche les sections réelles du formulaire OCP : infos générales,
/// zones P/E, planning, visite préalable, référentiels sélectionnés,
/// visas, export PDF (si exportPdfAutorise).
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
import '../data/models/autorisation_travail.dart';
import 'at_providers.dart';
import 'at_workflow_actions.dart';

class AtDetailPage extends ConsumerWidget {
  final String atId;
  const AtDetailPage({super.key, required this.atId});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final detail = ref.watch(atDetailProvider(atId));

    return Scaffold(
      appBar: AppBar(
        title: Text(detail.valueOrNull?.numero ?? 'Détail AT'),
        actions: [
          IconButton(
            tooltip: 'Consulter l\'Assistant IA sur cette AT',
            icon: const Icon(Icons.psychology_alt_rounded, color: Color(0xFF7FC8A9)),
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
        ],
      ),
      body: detail.when(
        loading: () => const LoadingState(message: 'Chargement de l\'AT...'),
        error: (error, _) => ErrorState(
          message: error is Failure ? error.message : 'Erreur de chargement.',
          onRetry: () => ref.invalidate(atDetailProvider(atId)),
        ),
        data: (value) => _AtDetailContent(at: value),
      ),
    );
  }
}

class _AtDetailContent extends ConsumerWidget {
  final AutorisationTravail at;
  const _AtDetailContent({required this.at});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final atId = at.id;
    return ListView(
      padding: const EdgeInsets.only(bottom: 24),
      children: [
        // --- Statut bandeau ---
        Padding(
          padding: const EdgeInsets.fromLTRB(16, 12, 16, 0),
          child: Row(
            children: [
              StatutChip(statut: at.statut),
              const Spacer(),
              if (at.version != null)
                Chip(
                  label: Text('Version ${at.version}'),
                  visualDensity: VisualDensity.compact,
                ),
            ],
          ),
        ),

        // --- Workflow Stepper S-HSE-SEC-31 ---
        Padding(
          padding: const EdgeInsets.fromLTRB(16, 10, 16, 0),
          child: WorkflowStepper(statut: at.statut),
        ),

        // --- Verrou d'édition ---
        if (at.etatVerrou == 'EN_COURS_EDITION' &&
            at.proprietaireBrouillonNomComplet != null)
          Padding(
            padding: const EdgeInsets.fromLTRB(16, 8, 16, 0),
            child: Card(
              color: OcpColors.forestSoft,
              child: Padding(
                padding: const EdgeInsets.all(12),
                child: Row(
                  children: [
                    const Icon(Icons.lock_rounded, size: 20, color: OcpColors.forest),
                    const SizedBox(width: 10),
                    Expanded(
                      child: Text(
                        'En cours d\'édition par ${at.proprietaireBrouillonNomComplet}.',
                        style: const TextStyle(fontSize: 12, color: OcpColors.forestDark),
                      ),
                    ),
                  ],
                ),
              ),
            ),
          ),

        // --- Objet & travaux ---
        _section(
          title: 'Nature de l\'intervention',
          children: [
            _row('Objet', at.objet ?? '—'),
            _row('Description', at.descriptionTravaux ?? '—'),
            _row('Document source', _sourceLabel(at)),
          ],
        ),

        // --- Périmètre ---
        _section(
          title: 'Périmètre & intervenants',
          children: [
            _row('Zone Propriétaire (P)', at.zoneProprietaireNom ?? '—'),
            _row('Zone Exécutante (E)', at.zoneExecutanteNom ?? '—'),
            _row('Services intervenants', at.servicesIntervenants ?? '—'),
            _row('Entreprises extérieures', at.entreprisesIntervenantes ?? '—'),
          ],
        ),

        // --- Planning ---
        _section(
          title: 'Planning prévisionnel',
          children: [
            _row('Période',
                '${AppDate.date(at.dateDebut)} → ${AppDate.date(at.dateFin)}',),
            _row('Horaires',
                '${AppDate.heureSimple(at.heureDebut)} → ${AppDate.heureSimple(at.heureFin)}',),
          ],
        ),

        // --- Visite préalable (§8.2) ---
        _section(
          title: 'Visite préalable',
          children: [
            _row('Effectuée', at.visiteEffectuee == true ? 'Oui' : 'Non'),
            if ((at.visiteCommentaire ?? '').isNotEmpty)
              _row('Commentaire', at.visiteCommentaire!),
            if (at.latitude != null && at.longitude != null)
              _row('Position GPS', '${at.latitude}, ${at.longitude}'),
          ],
        ),

        // --- Référentiels (comptages) ---
        _section(
          title: 'Mesures de sécurité & référentiels',
          children: [
            _row('Risques identifiés', '${at.risquesIds.length}'),
            _row('Mesures de préparation', '${at.mesuresIds.length}'),
            _row('EPI requis', '${at.episIds.length}'),
            _row('Moyens d\'accès', '${at.moyensAccesIds.length}'),
            _row('Permis complémentaires', '${at.permisIds.length}'),
          ],
        ),

        // --- Mesures exécutant (section F) ---
        if ((at.mesuresSecuriteExecutant ?? '').isNotEmpty)
          _section(
            title: 'Section F — Mesures exécutant',
            children: [
              _row('Mesures complémentaires', at.mesuresSecuriteExecutant!),
            ],
          ),

        // --- Actions de transition workflow ---
        Padding(
          padding: const EdgeInsets.fromLTRB(16, 12, 16, 0),
          child: AtWorkflowActions(at: at),
        ),

        // --- Raccourcis modules (pages dédiées) ---
        Padding(
          padding: const EdgeInsets.fromLTRB(16, 8, 16, 0),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              const SizedBox(height: 8),
              if (ref.watch(hasPermissionProvider)('EDIT_AT') &&
                  (at.statut == StatutAt.brouillon ||
                      at.statut == StatutAt.demandeCreee))
                ElevatedButton.icon(
                  onPressed: () => context.push('/at/$atId/edit'),
                  icon: const Icon(Icons.edit_rounded),
                  label: const Text('Modifier le formulaire'),
                ),
              if (at.exportPdfAutorise == true)
                ElevatedButton.icon(
                  onPressed: () => context.push('/at/$atId/pdf'),
                  icon: const Icon(Icons.picture_as_pdf_rounded),
                  label: const Text('PDF officiel'),
                )
              else if (at.exportPdfMotifsRefus.isNotEmpty)
                Padding(
                  padding: const EdgeInsets.only(top: 4),
                  child: Text(
                    'PDF indisponible : ${at.exportPdfMotifsRefus.join(' • ')}',
                    style: const TextStyle(fontSize: 12, color: OcpColors.slate),
                  ),
                ),
              const SizedBox(height: 8),
              OutlinedButton.icon(
                onPressed: () => context.push('/at/$atId/photos'),
                icon: const Icon(Icons.photo_library_outlined),
                label: const Text('Photos (visite & réception)'),
              ),
              const SizedBox(height: 8),
              OutlinedButton.icon(
                onPressed: () => context.push('/at/$atId/permis'),
                icon: const Icon(Icons.badge_outlined),
                label: const Text('Permis complémentaires'),
              ),
              const SizedBox(height: 8),
              OutlinedButton.icon(
                onPressed: () => context.push('/at/$atId/visas'),
                icon: const Icon(Icons.draw_rounded),
                label: const Text('Visas & signatures'),
              ),
              const SizedBox(height: 8),
              OutlinedButton.icon(
                onPressed: () => context.push('/at/$atId/reception'),
                icon: const Icon(Icons.verified_outlined),
                label: const Text('Réception des travaux'),
              ),
              const SizedBox(height: 8),
              OutlinedButton.icon(
                onPressed: () => context.push('/at/$atId/historique'),
                icon: const Icon(Icons.history_rounded),
                label: const Text('Historique'),
              ),
            ],
          ),
        ),
      ],
    );
  }

  String _sourceLabel(AutorisationTravail at) {
    final type = at.typeDocumentSource;
    if (type == null) return '—';
    return '$type ${at.documentSourceNumero ?? at.documentSourceId ?? ''}'.trim();
  }

  Widget _section({required String title, required List<Widget> children}) => Padding(
        padding: const EdgeInsets.fromLTRB(16, 14, 16, 0),
        child: Card(
          child: Padding(
            padding: const EdgeInsets.all(14),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  title,
                  style: const TextStyle(
                    fontFamily: 'SpaceGrotesk',
                    fontWeight: FontWeight.w700,
                    fontSize: 15,
                    color: OcpColors.forest,
                  ),
                ),
                const SizedBox(height: 10),
                ...children,
              ],
            ),
          ),
        ),
      );

  Widget _row(String label, String? value) => Padding(
        padding: const EdgeInsets.symmetric(vertical: 3),
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            SizedBox(
              width: 140,
              child: Text(label, style: const TextStyle(fontSize: 13, color: OcpColors.slate)),
            ),
            Expanded(
              child: Text(
                (value == null || value.isEmpty) ? '—' : value,
                style: const TextStyle(fontSize: 13, color: OcpColors.ink),
              ),
            ),
          ],
        ),
      );
}

