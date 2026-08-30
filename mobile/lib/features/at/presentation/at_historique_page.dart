/// Historique d'une AT — GET /autorisations-travail/{id}/historique.
/// Champs réels de HistoriqueATResponse : dateAction, action, ancienStatut,
/// nouveauStatut, commentaire, utilisateurNomComplet.
library;

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/errors/error_mapper.dart';
import '../../../core/errors/failures.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/utils/app_date.dart';
import '../../../core/widgets/states.dart';
import '../data/models/autorisation_travail.dart';
import 'at_providers.dart';

/// Un enregistrement d'historique (Map = HistoriqueATResponse du backend).
final atHistoriqueProvider = FutureProvider.autoDispose
    .family<List<Map<String, dynamic>>, String>((ref, id) async {
  final api = ref.watch(atApiProvider);
  try {
    return await api.historique(id);
  } catch (e) {
    throw mapDioError(e);
  }
});

class AtHistoriquePage extends ConsumerWidget {
  final String atId;
  const AtHistoriquePage({super.key, required this.atId});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final historique = ref.watch(atHistoriqueProvider(atId));

    return Scaffold(
      appBar: AppBar(title: const Text('Historique')),
      body: historique.when(
        loading: () => const LoadingState(message: 'Chargement de l\'historique...'),
        error: (e, _) => ErrorState(
          message: e is Failure ? e.message : 'Erreur de chargement.',
          onRetry: () => ref.invalidate(atHistoriqueProvider(atId)),
        ),
        data: (items) => items.isEmpty
            ? const EmptyState(message: 'Aucun événement dans l\'historique.')
            : ListView.separated(
                padding: const EdgeInsets.symmetric(vertical: 12, horizontal: 16),
                itemCount: items.length,
                separatorBuilder: (_, _) => const SizedBox(height: 8),
                itemBuilder: (context, index) => _HistoriqueTile(item: items[index]),
              ),
      ),
    );
  }
}

class _HistoriqueTile extends StatelessWidget {
  final Map<String, dynamic> item;
  const _HistoriqueTile({required this.item});

  @override
  Widget build(BuildContext context) {
    final dateAction = DateTime.tryParse('${item['dateAction'] ?? ''}');
    final action = '${item['action'] ?? ''}';
    final commentaire = item['commentaire'] as String?;
    final utilisateur = item['utilisateurNomComplet'] as String?;

    return Card(
      child: Padding(
        padding: const EdgeInsets.all(12),
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            CircleAvatar(
              radius: 16,
              backgroundColor: OcpColors.forestSoft,
              child: const Icon(Icons.history_rounded, size: 16, color: OcpColors.forest),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    _libelleAction(action),
                    style: const TextStyle(fontWeight: FontWeight.w700, fontSize: 14),
                  ),
                  if (_transition != null)
                    Text(
                      _transition!,
                      style: const TextStyle(fontSize: 12, color: OcpColors.moss),
                    ),
                  if ((utilisateur ?? '').isNotEmpty)
                    Text(utilisateur!,
                        style: const TextStyle(fontSize: 12, color: OcpColors.slate),),
                  if ((commentaire ?? '').isNotEmpty) ...[
                    const SizedBox(height: 4),
                    Text(commentaire!,
                        style: const TextStyle(fontSize: 13, color: OcpColors.ink),),
                  ],
                  const SizedBox(height: 4),
                  Text(
                    AppDate.dateHeure(dateAction),
                    style: const TextStyle(fontSize: 11, color: OcpColors.slate),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  /// Libellés lisibles des actions TypeActionAT du backend.
  static const Map<String, String> _libelles = {
    'CLASSIFICATION': 'Classification',
    'CREATION_DEMANDE': 'Création de la demande',
    'VISITE_CHANTIER': 'Visite de chantier',
    'REDACTION_AT': 'Rédaction de l\'AT',
    'DEBUT_INTERVENTION': 'Début d\'intervention',
    'RECONDUCTION': 'Reconduction',
    'DECLARATION_FIN': 'Déclaration de fin',
    'RECEPTION_CONJOINTE': 'Réception conjointe',
    'ARCHIVAGE_OFFICIEL': 'Archivage officiel',
    'CREATION': 'Création',
    'MODIFICATION': 'Modification',
    'AUTO_SAVE': 'Sauvegarde automatique',
    'TRANSFERT': 'Transfert',
    'SOUMISSION': 'Soumission',
    'VALIDATION': 'Validation',
    'REFUS': 'Refus',
    'RENOUVELLEMENT': 'Renouvellement',
    'CLOTURE': 'Clôture',
    'EXPORT_PDF': 'Export PDF',
    'ANNULATION': 'Annulation',
    'RECEPTION_TRAVAUX': 'Réception des travaux',
    'VALIDATION_RECEPTION': 'Validation de réception',
    'VALIDATION_RECEPTION_CEEP': 'Validation CEEP de réception',
    'DEMANDE_RECONDUCTION': 'Demande de reconduction',
    'APPROBATION_RECONDUCTION': 'Approbation de reconduction',
  };

  static String _libelleAction(String action) =>
      _libelles[action] ?? action.replaceAll('_', ' ').toLowerCase();

  String? get _transition {
    final ancien = item['ancienStatut'] as String?;
    final nouveau = item['nouveauStatut'] as String?;
    if (ancien == null && nouveau == null) return null;
    return '${StatutAt.libelle(ancien)} → ${StatutAt.libelle(nouveau)}';
  }
}
