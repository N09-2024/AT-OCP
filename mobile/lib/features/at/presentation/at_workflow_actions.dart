/// Actions de workflow de l'AT — boutons conditionnés par le statut RÉEL
/// de l'AT (machine à états WorkflowATServiceImpl) et les permissions de
/// l'utilisateur. Le serveur revalide systématiquement chaque transition.
///
/// Transitions exposées (endpoints réels) :
///   visite | rediger | submit | validate | reject(motif) |
///   demarrer-intervention | declarer-fin | reconduire(depasse24h) |
///   incident(motif) | reception-standard | close
library;

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/errors/error_mapper.dart';
import '../../../core/errors/failures.dart';
import '../../../core/theme/app_colors.dart';
import '../../auth/presentation/auth_controller.dart';
import '../data/at_api.dart';
import '../data/models/autorisation_travail.dart';
import 'at_providers.dart';

class AtWorkflowActions extends ConsumerWidget {
  final AutorisationTravail at;
  const AtWorkflowActions({super.key, required this.at});

  // ------------------------------------------------------------------
  // Construction des boutons selon statut + permissions
  // ------------------------------------------------------------------

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final perm = ref.watch(hasPermissionProvider);
    final boutons = _actionsPourStatut(context, ref, perm);
    if (boutons.isEmpty) return const SizedBox.shrink();

    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        Padding(
          padding: const EdgeInsets.only(top: 10, bottom: 6),
          child: Row(
            children: [
              const Icon(Icons.route_rounded, size: 16, color: OcpColors.forest),
              const SizedBox(width: 6),
              Expanded(
                child: Text('Étape suivante (${StatutAt.libelle(at.statut)})',
                    style: const TextStyle(
                        fontFamily: 'SpaceGrotesk',
                        fontWeight: FontWeight.w700,
                        fontSize: 13,),),
              ),
            ],
          ),
        ),
        ...boutons,
      ],
    );
  }

  List<Widget> _actionsPourStatut(
      BuildContext context, WidgetRef ref, bool Function(String) perm,) {
    final b = <Widget>[];
    final statut = at.statut;

    switch (statut) {
      case StatutAt.demandeCreee:
      case StatutAt.enVisiteRedaction:
        if (perm('CREATE_VISITE')) {
          b.add(_secondaire(context, Icons.directions_walk_rounded,
              'Marquer la visite effectuée', () => _marquerVisite(context, ref),),);
        }
        if (perm('SIGN_AT') || perm('VALIDATE_AT')) {
          b.add(_secondaire(context, Icons.edit_note_rounded, "Rédiger l'AT", () => _rediger(context, ref)));
        }
        break;

      case StatutAt.visiteRealisee:
        if (perm('SIGN_AT') || perm('VALIDATE_AT')) {
          b.add(_secondaire(context, Icons.edit_note_rounded, "Rédiger l'AT", () => _rediger(context, ref)));
        }
        break;

      case StatutAt.atRedigee:
        if (perm('SUBMIT_AT')) {
          b.add(_primaire(context, Icons.send_rounded, 'Soumettre pour validation', () async {
            if (!await _confirmer(context, 'Soumettre cette AT pour validation ?')) return;
            await _executer(context, ref, (api) => api.soumettre(at.id), 'AT soumise.');
          }),);
        }
        break;

      case StatutAt.soumise:
        if (perm('VALIDATE_AT')) {
          b.add(_primaire(context, Icons.check_circle_rounded, "Valider l'AT", () async {
            await _executer(context, ref, (api) => api.valider(at.id), 'AT validée.');
          }),);
        }
        if (perm('REJECT_AT')) {
          b.add(_danger(context, Icons.cancel_rounded, "Rejeter l'AT",
              () => _refuser(context, ref),),);
        }
        break;

      case StatutAt.atValidee:
      case StatutAt.validee:
        if (perm('START_INTERVENTION')) {
          b.add(_primaire(context, Icons.play_arrow_rounded, "Démarrer l'intervention",
              () async {
            await _executer(context, ref, (api) => api.demarrerIntervention(at.id),
                'Intervention démarrée.',);
          }),);
        }
        break;

      case StatutAt.enCours:
      case StatutAt.interventionEnCours:
      case StatutAt.enReconduction:
      case StatutAt.atReconduite:
      case StatutAt.renouvelee:
        if (perm('DECLARE_FIN_TRAVAUX')) {
          b.add(_primaire(context, Icons.task_alt_rounded, 'Déclarer la fin des travaux',
              () async {
            if (!await _confirmer(context, 'Confirmer la fin des travaux ?')) return;
            await _executer(
                context, ref, (api) => api.declarerFin(at.id), 'Fin des travaux déclarée.',);
          }),);
        }
        if (perm('RENEW_AT')) {
          b.add(_secondaire(
              context, Icons.update_rounded, 'Reconduire (début de poste)',
              () => _reconduire(context, ref),),);
        }
        break;

      case StatutAt.declareeTerminee:
      case StatutAt.finTravauxDeclaree:
        if (perm('RECEIVE_AT') || perm('CLOSE_AT')) {
          b.add(_primaire(context, Icons.handshake_rounded,
              'Réception conjointe des travaux', () async {
            if (!await _confirmer(context, 'Effectuer la réception conjointe ?')) return;
            await _executer(context, ref, (api) => api.receptionStandard(at.id),
                'Réception enregistrée.',);
          }),);
        }
        break;

      case StatutAt.receptionnees:
      case StatutAt.travauxReceptiones:
        if (perm('CLOSE_AT')) {
          b.add(_primaire(context, Icons.lock_clock_rounded,
              "Clôturer définitivement l'AT", () async {
            if (!await _confirmer(context, 'Clôturer définitivement cette AT ?')) return;
            await _executer(context, ref, (api) => api.cloturer(at.id), 'AT clôturée.');
          }),);
        }
        break;
      default:
        break;
    }

    // Incident disponible pendant les phases actives du cycle.
    if (perm('SIGN_AT') || perm('CREATE_VISITE')) {
      const actifs = <String?>[
        StatutAt.interventionEnCours,
        StatutAt.enCours,
        StatutAt.atValidee,
        StatutAt.validee,
        StatutAt.enReconduction,
        StatutAt.atReconduite,
      ];
      if (actifs.contains(statut)) {
        b.add(_danger(context, Icons.warning_amber_rounded, 'Signaler un incident',
            () => _signalerIncident(context, ref),),);
      }
    }
    return b;
  }

  // ------------------------------------------------------------------
  // Actions concrètes
  // ------------------------------------------------------------------

  Future<void> _marquerVisite(BuildContext context, WidgetRef ref) async =>
      _executer(context, ref, (api) => api.marquerVisite(at.id), 'Visite enregistrée.');

  Future<void> _rediger(BuildContext context, WidgetRef ref) async =>
      _executer(context, ref, (api) => api.rediger(at.id), 'Rédaction de l\'AT activée.');

  Future<void> _refuser(BuildContext context, WidgetRef ref) async {
    final motifController = TextEditingController();
    final ok = await showDialog<bool>(
      context: context,
      builder: (dialogContext) => AlertDialog(
        title: const Text('Motif du rejet'),
        content: TextField(
          controller: motifController,
          maxLines: 3,
          autofocus: true,
          decoration: const InputDecoration(hintText: 'Ex. : mesures insuffisantes...'),
        ),
        actions: [
          TextButton(
              onPressed: () => Navigator.pop(dialogContext, false),
              child: const Text('Annuler'),),
          FilledButton(
            style: FilledButton.styleFrom(backgroundColor: OcpColors.error),
            onPressed: () => Navigator.pop(dialogContext, true),
            child: const Text('Rejeter'),
          ),
        ],
      ),
    );
    final motif = motifController.text.trim();
    if (ok != true || !context.mounted) return;
    if (motif.isEmpty) {
      ScaffoldMessenger.of(context)
          .showSnackBar(const SnackBar(content: Text('Un motif est obligatoire.')));
      return;
    }
    await _executer(context, ref, (api) => api.refuser(at.id, motif), 'AT rejetée.');
  }

  Future<void> _reconduire(BuildContext context, WidgetRef ref) async {
    bool depasse24h = false;
    final ok = await showDialog<bool>(
      context: context,
      builder: (dialogContext) => StatefulBuilder(
        builder: (dialogContext2, setDialogState) => AlertDialog(
          title: const Text('Reconduction'),
          content: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              const Text('La reconduction s\'effectue au début d\'un nouveau poste.',
                  style: TextStyle(fontSize: 13),),
              SwitchListTile(
                contentPadding: EdgeInsets.zero,
                title: const Text('Durée > 24 h', style: TextStyle(fontSize: 13)),
                subtitle: const Text('Une nouvelle visite sera exigée.',
                    style: TextStyle(fontSize: 11),),
                value: depasse24h,
                onChanged: (v) => setDialogState(() => depasse24h = v),
              ),
            ],
          ),
          actions: [
            TextButton(onPressed: () => Navigator.pop(dialogContext, false), child: const Text('Annuler')),
            FilledButton(onPressed: () => Navigator.pop(dialogContext, true), child: const Text('Reconduire')),
          ],
        ),
      ),
    );
    if (ok != true || !context.mounted) return;
    await _executer(
        context,
        ref,
        (api) => api.reconduire(at.id, depasse24h: depasse24h),
        'AT reconduite.${depasse24h ? ' Nouvelle visite requise.' : ''}',);
  }

  Future<void> _signalerIncident(BuildContext context, WidgetRef ref) async {
    final motifController = TextEditingController();
    final ok = await showDialog<bool>(
      context: context,
      builder: (dialogContext) => AlertDialog(
        title: const Text('Signaler un incident / changement'),
        content: TextField(
          controller: motifController,
          maxLines: 3,
          decoration: const InputDecoration(hintText: 'Description (optionnel)'),
        ),
        actions: [
          TextButton(onPressed: () => Navigator.pop(dialogContext, false), child: const Text('Annuler')),
          FilledButton(
            style: FilledButton.styleFrom(backgroundColor: OcpColors.warning),
            onPressed: () => Navigator.pop(dialogContext, true),
            child: const Text('Signaler'),
          ),
        ],
      ),
    );
    if (ok != true || !context.mounted) return;
    final motif = motifController.text.trim();
    await _executer(
        context,
        ref,
        (api) => api.signalerIncident(at.id, motif.isEmpty ? null : motif),
        'Incident signalé — retour à la visite.',);
  }

  // ------------------------------------------------------------------
  // Exécution générique : mutation serveur + refresh détail + notification
  // ------------------------------------------------------------------

  Future<void> _executer(
    BuildContext context,
    WidgetRef ref,
    Future<dynamic> Function(AtApi api) call,
    String success,
  ) async {
    try {
      await call(ref.read(atApiProvider));
      ref.invalidate(atDetailProvider(at.id));
      if (context.mounted) {
        ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(success)));
      }
    } catch (e) {
      if (context.mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(content: Text(e is Failure ? e.message : mapDioError(e).message)),);
      }
    }
  }

  Future<bool> _confirmer(BuildContext context, String message) async {
    final result = await showDialog<bool>(
      context: context,
      builder: (dialogContext) => AlertDialog(
        title: const Text('Confirmation'),
        content: Text(message),
        actions: [
          TextButton(onPressed: () => Navigator.pop(dialogContext, false), child: const Text('Annuler')),
          FilledButton(onPressed: () => Navigator.pop(dialogContext, true), child: const Text('Confirmer')),
        ],
      ),
    );
    return result == true;
  }

  // ------------------------------------------------------------------
  // Styles de boutons
  // ------------------------------------------------------------------

  Widget _primaire(BuildContext context, IconData icon, String label,
          Future<void> Function() onTap,) =>
      ElevatedButton.icon(onPressed: onTap, icon: Icon(icon), label: Text(label));

  Widget _secondaire(BuildContext context, IconData icon, String label, Future<void> Function() onTap) =>
      OutlinedButton.icon(onPressed: onTap, icon: Icon(icon), label: Text(label));

  Widget _danger(BuildContext context, IconData icon, String label, VoidCallback onTap) =>
      OutlinedButton.icon(
        onPressed: onTap,
        icon: Icon(icon, color: OcpColors.error),
        label: Text(label, style: const TextStyle(color: OcpColors.error)),
        style: OutlinedButton.styleFrom(
            side: BorderSide(color: OcpColors.error.withValues(alpha: 0.5)),),
      );
}
