/// Permis d'une AT — GET /permis/at/{atId}, POST /permis, upload multipart
/// (PDF/PNG/JPEG/WEBP), relance d'analyse IA, suppression.
/// Le statut CONFORME des permis obligatoires conditionne l'export PDF (serveur).
library;

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/errors/error_mapper.dart';
import '../../../core/errors/failures.dart';
import '../../../core/network/api_providers.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/utils/app_date.dart';
import '../../../core/utils/file_pick_service.dart';
import '../../../core/widgets/states.dart';
import '../../auth/presentation/auth_controller.dart';
import '../../referentiels/presentation/referentiels_providers.dart';
import '../data/permis_api.dart';

final permisApiProvider = Provider<PermisApi>((ref) => PermisApi(ref.watch(apiClientProvider)));

final atPermisProvider = FutureProvider.autoDispose.family<List<Permis>, String>((ref, atId) async {
  try {
    return await ref.watch(permisApiProvider).findByAt(atId);
  } catch (e) {
    throw mapDioError(e);
  }
});

class AtPermisPage extends ConsumerWidget {
  final String atId;
  const AtPermisPage({super.key, required this.atId});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final permisList = ref.watch(atPermisProvider(atId));
    final canManage = ref.watch(hasPermissionProvider)('UPLOAD_PERMIS') ||
        ref.watch(hasPermissionProvider)('CREATE_PERMIS');

    return Scaffold(
      appBar: AppBar(title: const Text('Permis complémentaires')),
      floatingActionButton: canManage
          ? FloatingActionButton.extended(
              onPressed: () => _ajouterPermis(context, ref),
              icon: const Icon(Icons.add_rounded),
              label: const Text('Permis'),
            )
          : null,
      body: permisList.when(
        loading: () => const LoadingState(message: 'Chargement des permis...'),
        error: (e, _) => ErrorState(
          message: e is Failure ? e.message : 'Permis indisponibles.',
          onRetry: () => ref.invalidate(atPermisProvider(atId)),
        ),
        data: (items) => items.isEmpty
            ? const EmptyState(
                message: 'Aucun permis rattaché à cette AT.',
                icon: Icons.badge_outlined,
              )
            : RefreshIndicator(
                onRefresh: () async => ref.invalidate(atPermisProvider(atId)),
                child: ListView.separated(
                  physics: const AlwaysScrollableScrollPhysics(),
                  padding: const EdgeInsets.symmetric(vertical: 12),
                  itemCount: items.length,
                  separatorBuilder: (_, __) => const SizedBox(height: 8),
                  itemBuilder: (context, index) => _PermisTile(
                    permis: items[index],
                    onUpload: canManage ? () => _uploadFichier(context, ref, items[index]) : null,
                    onReanalyser: canManage ? () => _reanalyser(context, ref, items[index]) : null,
                  ),
                ),
              ),
      ),
    );
  }

  Future<void> _ajouterPermis(BuildContext context, WidgetRef ref) async {
    final types = await ref.read(typesPermisProvider.future);
    if (!context.mounted || types.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Aucun type de permis disponible au référentiel.')),
      );
      return;
    }

    String? typeChoisi = types.first.id;
    bool obligatoire = false;
    final commentaire = TextEditingController();

    final ok = await showDialog<bool>(
      context: context,
      builder: (dialogContext) => StatefulBuilder(
        builder: (dialogContext, setDialogState) => AlertDialog(
          title: const Text('Nouveau permis'),
          content: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              DropdownButtonFormField<String>(
                value: typeChoisi,
                isExpanded: true,
                decoration: const InputDecoration(labelText: 'Type de permis'),
                items: types.map((t) => DropdownMenuItem(value: t.id, child: Text(t.libelle))).toList(),
                onChanged: (v) => setDialogState(() => typeChoisi = v),
              ),
              const SizedBox(height: 8),
              SwitchListTile(
                contentPadding: EdgeInsets.zero,
                title: const Text('Obligatoire', style: TextStyle(fontSize: 14)),
                subtitle: const Text('Sa conformité conditionne l\'export PDF',
                    style: TextStyle(fontSize: 11),),
                value: obligatoire,
                onChanged: (v) => setDialogState(() => obligatoire = v),
              ),
              TextField(
                controller: commentaire,
                decoration: const InputDecoration(labelText: 'Commentaire (optionnel)'),
              ),
            ],
          ),
          actions: [
            TextButton(onPressed: () => Navigator.pop(dialogContext, false), child: const Text('Annuler')),
            FilledButton(onPressed: () => Navigator.pop(dialogContext, true), child: const Text('Créer')),
          ],
        ),
      ),
    );

    if (ok != true || typeChoisi == null) return;
    try {
      await ref.read(permisApiProvider).create(
            autorisationTravailId: atId,
            typePermisId: typeChoisi!,
            estObligatoire: obligatoire,
            commentaire: commentaire.text,
          );
      ref.invalidate(atPermisProvider(atId));
    } catch (e) {
      if (context.mounted) {
        ScaffoldMessenger.of(context)
            .showSnackBar(SnackBar(content: Text(mapDioError(e).message)));
      }
    }
  }

  Future<void> _uploadFichier(BuildContext context, WidgetRef ref, Permis permis) async {
    try {
      final file = await ref.read(filePickServiceProvider).choisirDocumentPermis();
      if (file == null) return;
      await ref.read(permisApiProvider).uploadFichier(
            permis.id,
            bytes: file.bytes,
            filename: file.filename,
            mimeType: file.mimeType,
          );
      ref.invalidate(atPermisProvider(atId));
      if (context.mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(content: Text('Fichier envoyé — analyse IA lancée.')),);
      }
    } catch (e) {
      if (context.mounted) {
        ScaffoldMessenger.of(context)
            .showSnackBar(SnackBar(content: Text(e is Failure ? e.message : mapDioError(e).message)));
      }
    }
  }

  Future<void> _reanalyser(BuildContext context, WidgetRef ref, Permis permis) async {
    try {
      await ref.read(permisApiProvider).reanalyser(permis.id);
      ref.invalidate(atPermisProvider(atId));
    } catch (e) {
      if (context.mounted) {
        ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(mapDioError(e).message)));
      }
    }
  }
}

class _PermisTile extends StatelessWidget {
  final Permis permis;
  final VoidCallback? onUpload;
  final VoidCallback? onReanalyser;

  const _PermisTile({required this.permis, this.onUpload, this.onReanalyser});

  Color get _statusColor => switch (permis.statutVerification) {
        StatutPermisVerif.conforme => OcpColors.success,
        StatutPermisVerif.nonConforme => OcpColors.error,
        StatutPermisVerif.expire => OcpColors.warning,
        _ => OcpColors.slate,
      };

  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(14),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Expanded(
                  child: Text(
                    permis.typePermis?.libelle ?? permis.numero ?? 'Permis',
                    style: const TextStyle(fontWeight: FontWeight.w700, fontSize: 15),
                  ),
                ),
                if (permis.estObligatoire == true)
                  const Padding(
                    padding: EdgeInsets.only(right: 6),
                    child: Tooltip(
                      message: 'Permis obligatoire',
                      child: Icon(Icons.priority_high_rounded, size: 16, color: OcpColors.warning),
                    ),
                  ),
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                  decoration: BoxDecoration(
                    color: _statusColor.withValues(alpha: 0.12),
                    borderRadius: BorderRadius.circular(20),
                    border: Border.all(color: _statusColor.withValues(alpha: 0.4)),
                  ),
                  child: Text(
                    StatutPermisVerif.libelle(permis.statutVerification),
                    style: TextStyle(color: _statusColor, fontSize: 12, fontWeight: FontWeight.w700),
                  ),
                ),
              ],
            ),
            if ((permis.numero ?? '').isNotEmpty)
              Padding(
                padding: const EdgeInsets.only(top: 4),
                child: Text(permis.numero!, style: const TextStyle(fontSize: 12, color: OcpColors.slate)),
              ),
            const SizedBox(height: 6),
            Wrap(
              spacing: 16,
              children: [
                _meta('Émission', AppDate.date(permis.dateEmission)),
                _meta('Expiration', AppDate.date(permis.dateExpiration)),
                _meta('Fichier', permis.fichierJointNom ?? 'aucun'),
              ],
            ),
            if ((permis.commentaire ?? '').isNotEmpty)
              Padding(
                padding: const EdgeInsets.only(top: 6),
                child: Text(permis.commentaire!, style: const TextStyle(fontSize: 12)),
              ),
            if (onUpload != null) ...[
              const SizedBox(height: 10),
              Row(
                children: [
                  Expanded(
                    child: OutlinedButton.icon(
                      onPressed: onUpload,
                      icon: const Icon(Icons.upload_file_outlined, size: 18),
                      label: Text(permis.fichierJointId == null ? 'Envoyer le fichier' : 'Remplacer'),
                    ),
                  ),
                  if (permis.fichierJointId != null) ...[
                    const SizedBox(width: 8),
                    IconButton(
                      tooltip: 'Relancer l\'analyse IA',
                      onPressed: onReanalyser,
                      icon: const Icon(Icons.psychology_alt_outlined, size: 20),
                    ),
                  ],
                ],
              ),
            ],
          ],
        ),
      ),
    );
  }

  Widget _meta(String label, String value) => Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(label, style: const TextStyle(fontSize: 11, color: OcpColors.slate)),
          Text(value, style: const TextStyle(fontSize: 12)),
        ],
      );
}
