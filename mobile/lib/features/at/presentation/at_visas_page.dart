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
          // Trier les visas par ordre séquentiel
          final sortedItems = List<Visa>.from(items)
            ..sort((a, b) => (a.ordre ?? 99).compareTo(b.ordre ?? 99));

          return ListView(
            padding: const EdgeInsets.symmetric(vertical: 12, horizontal: 16),
            children: [
              // Bandeau explicatif de la séquence des visas
              Card(
                color: OcpColors.surfaceSoft,
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(10),
                  side: const BorderSide(color: OcpColors.borderSoft),
                ),
                child: const Padding(
                  padding: EdgeInsets.all(12),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(
                        children: [
                          Icon(Icons.info_outline_rounded, size: 18, color: OcpColors.forest),
                          SizedBox(width: 8),
                          Text(
                            'Ordre séquentiel réglementaire (S-HSE-SEC-31)',
                            style: TextStyle(fontWeight: FontWeight.w700, fontSize: 13, color: OcpColors.forest),
                          ),
                        ],
                      ),
                      SizedBox(height: 6),
                      Text(
                        '1. CEEE (Exécutant) → 2. HCEP (Propriétaire) → 3. HCEE → 4. HMEP → 5. HMEE',
                        style: TextStyle(fontSize: 12, color: OcpColors.ink, fontWeight: FontWeight.w600),
                      ),
                    ],
                  ),
                ),
              ),
              const SizedBox(height: 8),

              if (peutAccuserReception &&
                  session != null &&
                  hasPermission('SIGN_AT')) ...[
                _AccuseReceptionCard(onConfirm: () => _accuserReception(context, ref)),
                const SizedBox(height: 8),
              ],
              ...sortedItems.map((v) {
                // Vérifier si tous les visas d'ordre inférieur sont déjà signés/validés
                final isPrecedentValide = sortedItems
                    .where((other) => (other.ordre ?? 0) < (v.ordre ?? 0))
                    .every((other) => other.signaturePresente || other.statut == StatutVisa.valide);

                return _VisaTile(
                  visa: v,
                  canSign: canSign,
                  isPrecedentValide: isPrecedentValide,
                  onSign: () => _signer(context, ref, v),
                );
              }),
              if (sortedItems.isEmpty)
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
  final bool isPrecedentValide;
  final VoidCallback? onSign;

  const _VisaTile({
    required this.visa,
    required this.canSign,
    required this.isPrecedentValide,
    this.onSign,
  });

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

  bool get _peutEtreSigne => canSign && visa.statut == StatutVisa.enAttente && isPrecedentValide;

  @override
  Widget build(BuildContext context) {
    return Card(
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(12),
        side: BorderSide(
          color: _peutEtreSigne ? OcpColors.forest : OcpColors.borderSoft,
          width: _peutEtreSigne ? 1.5 : 1,
        ),
      ),
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
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        visa.utilisateurNomComplet ?? 'Visa',
                        style: const TextStyle(fontWeight: FontWeight.w700, fontSize: 14),
                      ),
                      if (visa.ordre != null)
                        Text(
                          'Étape ${visa.ordre} dans l\'ordre des visas',
                          style: const TextStyle(fontSize: 11, color: OcpColors.slate),
                        ),
                    ],
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
              ],
            ),
            if (!isPrecedentValide && visa.statut == StatutVisa.enAttente) ...[
              const SizedBox(height: 10),
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
                decoration: BoxDecoration(
                  color: OcpColors.surfaceSoft,
                  borderRadius: BorderRadius.circular(6),
                ),
                child: const Row(
                  children: [
                    Icon(Icons.lock_clock_outlined, size: 16, color: OcpColors.slate),
                    SizedBox(width: 6),
                    Expanded(
                      child: Text(
                        'En attente de la signature du visa précédent dans l\'ordre.',
                        style: TextStyle(fontSize: 11, color: OcpColors.slate),
                      ),
                    ),
                  ],
                ),
              ),
            ],
            if (_peutEtreSigne) ...[
              const SizedBox(height: 12),
              FilledButton.icon(
                style: FilledButton.styleFrom(
                  backgroundColor: OcpColors.forest,
                  minimumSize: const Size.fromHeight(44),
                ),
                onPressed: onSign,
                icon: const Icon(Icons.draw_rounded),
                label: Text(visa.signaturePresente ? 'Re-signer' : 'Signer ce visa maintenant'),
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

