/// Visas d'une AT — GET /visa/at/{atId} + signature manuscrite
/// (POST /visa/{id}/sign, multipart PNG) + accusé de réception CEEE.
/// Permissions côté mobile = affichage ; le serveur reste l'autorité finale
/// (qui peut signer et à quelle étape est vérifié côté backend).
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
import '../../at/presentation/at_providers.dart';
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
    final hasPermission = ref.watch(hasPermissionProvider);
    final canSign = hasPermission('SIGN_AT') || hasPermission('VALIDATE_AT');
    final session = ref.watch(sessionProvider);
    final at = detailAsync.valueOrNull;

    // Accusé de réception CEEE : affiché tant que non confirmé ; le serveur
    // vérifie que l'utilisateur est bien le CEEE lié à l'AT.
    final peutAccuserReception =
        at != null && at.dateReceptionCeee == null && !_statutTropTot(at.statut);

    return Scaffold(
      appBar: AppBar(title: const Text('Visas & signatures')),
      body: visas.when(
        loading: () => const LoadingState(message: 'Chargement des visas...'),
        error: (e, _) => ErrorState(
          message: e is Failure ? e.message : 'Erreur de chargement.',
          onRetry: () => ref.invalidate(visasProvider(atId)),
        ),
        data: (items) {
          return ListView(
            padding: const EdgeInsets.symmetric(vertical: 12, horizontal: 16),
            children: [
              if (peutAccuserReception &&
                  session != null &&
                  hasPermission('SIGN_AT')) ...[
                _AccuseReceptionCard(onConfirm: () => _accuserReception(context, ref)),
                const SizedBox(height: 8),
              ],
              ...items.map((v) => _VisaTile(
                    visa: v,
                    canSign: canSign,
                    onSign: () => _signer(context, ref, v),
                  ),),
              if (items.isEmpty)
                const Padding(
                  padding: EdgeInsets.symmetric(vertical: 24),
                  child: EmptyState(message: 'Aucun visa créé pour cette AT.', icon: Icons.draw_outlined),
                ),
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
      if (context.mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(content: Text('Réception confirmée — vous pouvez signer.')),);
      }
    } catch (e) {
      if (context.mounted) {
        ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(mapDioError(e).message)));
      }
    }
  }

  Future<void> _signer(BuildContext context, WidgetRef ref, Visa visa) async {
    final session = ref.read(sessionProvider);
    final result = await Navigator.of(context).push<SignatureResult>(
      MaterialPageRoute(
        builder: (_) =>
            SignatureScreen(signataireNom: session?.utilisateur.nomComplet ?? 'Signataire'),
      ),
    );
    if (result == null || !context.mounted) return;

    try {
      await VisaApi(ref.read(apiClientProvider)).sign(
        visaId: visa.id,
        signaturePng: result.pngBytes,
      );
      ref.invalidate(visasProvider(atId));
      if (context.mounted) {
        ScaffoldMessenger.of(context).showSnackBar(const SnackBar(
            content: Text('Signature enregistrée et scellée par le serveur.'),),);
      }
    } catch (e) {
      if (context.mounted) {
        ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(mapDioError(e).message)));
      }
    }
  }
}

class _AccuseReceptionCard extends StatelessWidget {
  final VoidCallback onConfirm;
  const _AccuseReceptionCard({required this.onConfirm});

  @override
  Widget build(BuildContext context) => Card(
        color: OcpColors.mintSoft,
        child: ListTile(
          leading: const Icon(Icons.mark_email_read_outlined, color: OcpColors.forest),
          title: const Text('Accuser réception de l\'AT',
              style: TextStyle(fontWeight: FontWeight.w700, fontSize: 14),),
          subtitle: const Text('Préalable obligatoire avant votre signature.',
              style: TextStyle(fontSize: 12),),
          trailing: FilledButton.tonal(
            onPressed: onConfirm,
            child: const Text('OK'),
          ),
        ),
      );
}

class _VisaTile extends StatelessWidget {
  final Visa visa;
  final bool canSign;
  final VoidCallback? onSign;

  const _VisaTile({required this.visa, required this.canSign, this.onSign});

  Color get _statusColor {
    switch (visa.statut) {
      case StatutVisa.valide:
      case StatutVisa.validation:
        return OcpColors.success;
      case StatutVisa.refuse:
        return OcpColors.error;
      default:
        return OcpColors.warning;
    }
  }

  bool get _peutEtreSigne => canSign && visa.statut == StatutVisa.enAttente;

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
                Icon(
                  visa.signaturePresente ? Icons.verified_rounded : Icons.pending_actions_rounded,
                  color: _statusColor,
                  size: 22,
                ),
                const SizedBox(width: 10),
                Expanded(
                  child: Text(
                    visa.utilisateurNomComplet ?? 'Visa',
                    style: const TextStyle(fontWeight: FontWeight.w700, fontSize: 14),
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
                    StatutVisa.libelle(visa.statut),
                    style: TextStyle(
                        color: _statusColor, fontSize: 12, fontWeight: FontWeight.w700,),
                  ),
                ),
              ],
            ),
            if ((visa.commentaire ?? '').isNotEmpty) ...[
              const SizedBox(height: 8),
              Text(visa.commentaire!, style: const TextStyle(fontSize: 13, color: OcpColors.ink)),
            ],
            const SizedBox(height: 8),
            Wrap(
              spacing: 16,
              runSpacing: 4,
              children: [
                _meta('Visa', AppDate.dateHeure(visa.dateVisa)),
                _meta('Signature', AppDate.dateHeure(visa.dateSignature)),
                if (visa.ordre != null) _meta('Ordre', '${visa.ordre}'),
              ],
            ),
            if (_peutEtreSigne) ...[
              const SizedBox(height: 12),
              FilledButton.icon(
                onPressed: onSign,
                icon: const Icon(Icons.draw_rounded),
                label: Text(visa.signaturePresente ? 'Re-signer' : 'Signer ce visa'),
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
          Text(value, style: const TextStyle(fontSize: 12, color: OcpColors.ink)),
        ],
      );
}
