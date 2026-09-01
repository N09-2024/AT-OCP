/// Formulaire AT mobile par étapes — Phase 6.
/// Champs 100 % conformes à AutoSaveRequest (aucun champ Installation).
/// Auto-save silencieux + verrou gérés par AtFormController (Phases 6/8).
library;

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../../core/errors/failures.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/widgets/states.dart';
import '../../auth/presentation/auth_controller.dart';
import '../../referentiels/data/referentiel_models.dart';
import '../../referentiels/presentation/referentiels_providers.dart';
import '../data/models/autorisation_travail.dart';
import 'at_form_controller.dart';
import 'at_providers.dart';
import 'widgets/multi_select_step.dart';

class AtFormPage extends ConsumerWidget {
  final String atId;
  const AtFormPage({super.key, required this.atId});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final form = ref.watch(atFormProvider(atId));

    return Scaffold(
      appBar: AppBar(
        title: Text(form.at.numero ?? 'AT'),
        actions: [
          IconButton(
            tooltip: 'Aide Assistant IA HSE',
            icon: const Icon(Icons.psychology_alt_rounded, color: Color(0xFF7FC8A9)),
            onPressed: () {
              context.push(
                '/assistant',
                extra: {
                  'id': form.at.id,
                  'numero': form.at.numero,
                  'objet': form.data.objet,
                  'descriptionTravaux': form.data.descriptionTravaux,
                  'risques': form.data.risquesIds.toList(),
                  'mesures': form.data.mesuresIds.toList(),
                  'epis': form.data.episIds.toList(),
                  'permis': form.data.permisIds.toList(),
                },
              );
            },
          ),
          _SaveStatusIndicator(state: form),
        ],
      ),
      body: switch (form.loadError) {
        null => _AtFormStepper(atId: atId, state: form),
        final err => ErrorState(
            message: err.message,
            onRetry: () => ref.invalidate(atFormProvider(atId)),
          ),
      },
    );
  }
}

/// Indicateur d'auto-save : "Enregistrement…" / "Enregistré HH:mm" / erreur réessayable.
class _SaveStatusIndicator extends ConsumerWidget {
  final AtFormState state;
  const _SaveStatusIndicator({required this.state});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    switch (state.saveStatus) {
      case SaveStatus.saving:
        return const Padding(
          padding: EdgeInsets.only(right: 16),
          child: Center(
            child: SizedBox(
              width: 16,
              height: 16,
              child: CircularProgressIndicator(strokeWidth: 2, color: OcpColors.white),
            ),
          ),
        );
      case SaveStatus.saved:
        final h = state.lastSavedAt;
        return Padding(
          padding: const EdgeInsets.only(right: 12),
          child: Center(
            child: Text('Enregistré ${h == null ? '' : '${h.hour.toString().padLeft(2, '0')}:${h.minute.toString().padLeft(2, '0')}'}',
                style: const TextStyle(color: OcpColors.mint, fontSize: 11),),
          ),
        );
      case SaveStatus.error:
        return IconButton(
          tooltip: 'Échec de la sauvegarde — réessayer',
          onPressed: () => ref.read(atFormProvider(state.at.id).notifier).retrySave(),
          icon: const Icon(Icons.cloud_off_rounded, color: OcpColors.errorSoft),
        );
      case SaveStatus.idle:
        return const SizedBox.shrink();
    }
  }
}

class _AtFormStepper extends ConsumerStatefulWidget {
  final String atId;
  final AtFormState state;

  const _AtFormStepper({required this.atId, required this.state});

  @override
  ConsumerState<_AtFormStepper> createState() => _AtFormStepperState();
}

class _AtFormStepperState extends ConsumerState<_AtFormStepper> {
  int _step = 0;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      ref.read(hasPermissionProvider); // warm-up session watch
    });
  }

  @override
  Widget build(BuildContext context) {
    final ref = this.ref;
    final widget = this.widget;
    final readOnly = widget.state.readOnly;

    return PopScope(
      canPop: true,
      onPopInvokedWithResult: (didPop, result) {
        if (!didPop) return;
        // Sauvegarde finale + libération du verrou en quittant l'écran.
        ref.read(atFormProvider(widget.atId).notifier).releaseLockAndFlush();
      },
      child: Column(
        children: [
          if (readOnly) _ReadOnlyBanner(state: widget.state),
          Expanded(
            child: Stepper(
            currentStep: _step,
            onStepTapped: (i) => setState(() => _step = i),
            physics: const ClampingScrollPhysics(),
            controlsBuilder: (context, details) => Padding(
              padding: const EdgeInsets.only(top: 14),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  if (_step > 0 && !readOnly)
                    OutlinedButton(
                      onPressed: details.onStepCancel,
                      child: const Text('Précédent'),
                    )
                  else
                    const SizedBox.shrink(),
                  if (_step < 9 && !readOnly)
                    ElevatedButton(
                      onPressed: details.onStepContinue,
                      child: const Text('Suivant'),
                    ),
                ],
              ),
            ),
            steps: [
              _stepGeneral(readOnly),
              _stepPlanning(readOnly),
              _stepLocalisation(readOnly),
              _stepReferentiel(
                title: 'A. Risques liés aux travaux',
                items: ref.watch(risquesProvider),
                selected: widget.state.data.risquesIds,
                onToggle: (id) =>
                    ref.read(atFormProvider(widget.atId).notifier).toggleRisque(id),
                searchHint: 'Rechercher un risque…',
                emptyMessage: 'Aucun risque au référentiel.',
                readOnly: readOnly,
              ),
              _stepReferentiel(
                title: 'B. Mesures de sécurité à prendre',
                items: ref.watch(mesuresProvider),
                selected: widget.state.data.mesuresIds,
                onToggle: (id) =>
                    ref.read(atFormProvider(widget.atId).notifier).toggleMesure(id),
                searchHint: 'Rechercher une mesure…',
                emptyMessage: 'Aucune mesure au référentiel.',
                readOnly: readOnly,
              ),
              _stepReferentiel(
                title: 'C. Moyens d\'accès',
                items: ref.watch(moyensAccesProvider),
                selected: widget.state.data.moyensAccesIds,
                onToggle: (id) =>
                    ref.read(atFormProvider(widget.atId).notifier).toggleMoyenAcces(id),
                searchHint: 'Rechercher un moyen d\'accès…',
                emptyMessage: 'Aucun moyen d\'accès au référentiel.',
                readOnly: readOnly,
              ),
              _stepReferentiel(
                title: 'D. Équipements de protection (EPI)',
                items: ref.watch(episProvider),
                selected: widget.state.data.episIds,
                onToggle: (id) =>
                    ref.read(atFormProvider(widget.atId).notifier).toggleEpi(id),
                searchHint: 'Rechercher un EPI…',
                emptyMessage: 'Aucun EPI au référentiel.',
                readOnly: readOnly,
              ),
              _stepPermis(readOnly),
              _stepMesuresExecutant(readOnly),
              _stepRecapitulatif(readOnly),
            ],
          ),
        ),
      ],
      ),
    );
  }

  // ------------------------------------------------------------------
  // Étape 1 : informations générales & entreprises intervenantes
  // ------------------------------------------------------------------

  Step _stepGeneral(bool readOnly) {
    final data = widget.state.data;
    AtFormNotifier notifier() => ref.read(atFormProvider(widget.atId).notifier);
    final entreprisesAsync = ref.watch(entreprisesExternesProvider);

    return Step(
      isActive: _step >= 0,
      title: const Text('Général'),
      content: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          TextFormField(
            enabled: !readOnly,
            initialValue: data.objet,
            maxLength: 200,
            decoration: const InputDecoration(labelText: 'Objet de l\'intervention *'),
            onChanged: (v) => notifier().update((d) => d.copyWith(objet: v)),
          ),
          const SizedBox(height: 10),
          TextFormField(
            enabled: !readOnly,
            initialValue: data.descriptionTravaux,
            maxLines: 4,
            decoration: const InputDecoration(labelText: 'Description des travaux'),
            onChanged: (v) => notifier().update((d) => d.copyWith(descriptionTravaux: v)),
          ),
          const SizedBox(height: 12),
          entreprisesAsync.when(
            loading: () => const LinearProgressIndicator(),
            error: (_, __) => TextFormField(
              enabled: !readOnly,
              initialValue: data.entreprisesIntervenantes,
              decoration: const InputDecoration(labelText: 'Entreprises intervenantes (Tiers)'),
              onChanged: (v) => notifier().update((d) => d.copyWith(entreprisesIntervenantes: v)),
            ),
            data: (entreprisesList) => _EntrepriseDropdown(
              label: 'Entreprise extérieure (Tiers / Sous-traitant)',
              entreprises: entreprisesList,
              value: data.entreprisesIntervenantes,
              enabled: !readOnly,
              onChanged: (val) => notifier().update((d) => d.copyWith(entreprisesIntervenantes: val)),
            ),
          ),
        ],
      ),
    );
  }

  // ------------------------------------------------------------------
  // Étape 2 : planning
  // ------------------------------------------------------------------

  Step _stepPlanning(bool readOnly) {
    final data = widget.state.data;
    AtFormNotifier notifier() => ref.read(atFormProvider(widget.atId).notifier);
    String h(TimeOfDay? t) =>
        t == null ? '—' : '${t.hour.toString().padLeft(2, '0')}:${t.minute.toString().padLeft(2, '0')}';

    return Step(
      isActive: _step >= 1,
      title: const Text('Planning'),
      content: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          _DateTile(
            icon: Icons.calendar_today_outlined,
            label: 'Date de début',
            value: data.dateDebut,
            enabled: !readOnly,
            onPick: (d) => notifier().update((x) => x.copyWith(dateDebut: d)),
          ),
          _DateTile(
            icon: Icons.event_outlined,
            label: 'Date de fin',
            value: data.dateFin,
            enabled: !readOnly,
            firstDate: data.dateDebut,
            onPick: (d) => notifier().update((x) => x.copyWith(dateFin: d)),
          ),
          _TimeTile(
            label: 'Heure de début',
            value: h(data.heureDebut),
            enabled: !readOnly,
            initial: data.heureDebut,
            onPick: (t) => notifier().update((x) => x.copyWith(heureDebut: t)),
          ),
          _TimeTile(
            label: 'Heure de fin',
            value: h(data.heureFin),
            enabled: !readOnly,
            initial: data.heureFin,
            onPick: (t) => notifier().update((x) => x.copyWith(heureFin: t)),
          ),
        ],
      ),
    );
  }

  // ------------------------------------------------------------------
  // Étape 3 : localisation & services (zones P/E et services P/E)
  // ------------------------------------------------------------------

  Step _stepLocalisation(bool readOnly) {
    final data = widget.state.data;
    AtFormNotifier notifier() => ref.read(atFormProvider(widget.atId).notifier);
    final zonesAsync = ref.watch(zonesProvider);
    final servicesAsync = ref.watch(servicesProvider);
    final session = ref.watch(sessionProvider);
    final userProprietaireService = session?.utilisateur.service;

    final isSameService = userProprietaireService != null &&
        ((data.serviceIntervenantId != null && data.serviceIntervenantId == userProprietaireService.id) ||
            (data.servicesIntervenants.isNotEmpty &&
                data.servicesIntervenants.toLowerCase().trim() ==
                    userProprietaireService.nomService.toLowerCase().trim()));

    return Step(
      isActive: _step >= 2,
      title: const Text('Localisation & Services'),
      content: zonesAsync.when(
        loading: () => const LoadingState(message: 'Chargement des référentiels...'),
        error: (e, _) => ErrorState(
          message: 'Référentiels indisponibles.',
          onRetry: () {
            ref.invalidate(zonesProvider);
            ref.invalidate(servicesProvider);
          },
        ),
        data: (list) => Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            // 1. Zone propriétaire (P)
            _ZoneDropdown(
              label: 'Zone propriétaire (P) *',
              zones: list,
              valueId: data.zoneProprietaireId,
              fallbackName: data.zoneProprietaireNom,
              enabled: !readOnly,
              onChanged: (z) => notifier().update(
                (d) => d.copyWith(
                  zoneProprietaireId: z.id,
                  zoneProprietaireNom: z.nomZone,
                ),
              ),
            ),
            const SizedBox(height: 12),

            // 2. Zone exécutante / intervenante (E)
            _ZoneDropdown(
              label: 'Zone intervenante / exécutante (E) *',
              zones: list,
              valueId: data.zoneExecutanteId,
              fallbackName: data.zoneExecutanteNom,
              enabled: !readOnly,
              onChanged: (z) => notifier().update(
                (d) => d.copyWith(
                  zoneExecutanteId: z.id,
                  zoneExecutanteNom: z.nomZone,
                ),
              ),
            ),
            const SizedBox(height: 14),

            // 3. Service demandeur / propriétaire (P)
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
              decoration: BoxDecoration(
                color: OcpColors.surfaceAlt,
                borderRadius: BorderRadius.circular(8),
                border: Border.all(color: OcpColors.divider),
              ),
              child: Row(
                children: [
                  const Icon(Icons.business_rounded, size: 20, color: OcpColors.primary),
                  const SizedBox(width: 10),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        const Text(
                          'Service demandeur / propriétaire (P)',
                          style: TextStyle(fontSize: 11, color: OcpColors.slate),
                        ),
                        Text(
                          userProprietaireService?.nomService ?? 'Non renseigné (compte utilisateur)',
                          style: const TextStyle(
                            fontWeight: FontWeight.w700,
                            fontSize: 13,
                            color: OcpColors.ink,
                          ),
                        ),
                      ],
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 12),

            // 4. Service intervenant / exécutant (E) — Liste déroulante
            servicesAsync.when(
              loading: () => const LinearProgressIndicator(),
              error: (_, __) => TextFormField(
                enabled: !readOnly,
                initialValue: data.servicesIntervenants,
                decoration: const InputDecoration(
                  labelText: 'Service intervenant / exécutant (E) *',
                ),
                onChanged: (v) => notifier().update((d) => d.copyWith(servicesIntervenants: v)),
              ),
              data: (servicesList) => _ServiceDropdown(
                label: 'Service intervenant / exécutant (E) *',
                services: servicesList,
                valueId: data.serviceIntervenantId,
                valueNom: data.servicesIntervenants,
                forbiddenServiceId: userProprietaireService?.id,
                enabled: !readOnly,
                onChanged: (s) => notifier().update(
                  (d) => d.copyWith(
                    serviceIntervenantId: s.id,
                    servicesIntervenants: s.nomService ?? s.id,
                  ),
                ),
              ),
            ),

            // 5. Alerte d'incompatibilité de services
            if (isSameService) ...[
              const SizedBox(height: 10),
              Container(
                padding: const EdgeInsets.all(10),
                decoration: BoxDecoration(
                  color: OcpColors.errorSoft.withOpacity(0.12),
                  borderRadius: BorderRadius.circular(8),
                  border: Border.all(color: OcpColors.errorSoft),
                ),
                child: Row(
                  children: [
                    const Icon(Icons.warning_amber_rounded, size: 20, color: OcpColors.errorSoft),
                    const SizedBox(width: 8),
                    Expanded(
                      child: Text(
                        'Le service propriétaire (${userProprietaireService?.nomService}) doit être différent du service intervenant (règle de séparation des rôles CEEP / CEEE).',
                        style: const TextStyle(
                          fontSize: 12,
                          color: OcpColors.errorSoft,
                          fontWeight: FontWeight.w600,
                        ),
                      ),
                    ),
                  ],
                ),
              ),
            ],

            const SizedBox(height: 8),
            const Text(
              'Le service intervenant (E) désigné recevra l\'autorisation pour signature/visa du Chef d\'Équipe Exécutant (CEEE).',
              style: TextStyle(fontSize: 11, color: OcpColors.slate),
            ),
          ],
        ),
      ),
    );
  }

  // ------------------------------------------------------------------
  // Étapes référentiels génériques
  // ------------------------------------------------------------------

  Step _stepReferentiel({
    required String title,
    required AsyncValue<List<ReferentielItem>> items,
    required Set<String> selected,
    required ValueChanged<String> onToggle,
    required String searchHint,
    required String emptyMessage,
    required bool readOnly,
  }) {
    return Step(
      isActive: true,
      state: selected.isEmpty ? StepState.indexed : StepState.complete,
      title: Text(title),
      content: SizedBox(
        height: MediaQuery.of(context).size.height * 0.42,
        child: MultiSelectStep(
          items: items,
          selected: selected,
          onToggle: readOnly ? (_) {} : onToggle,
          searchHint: searchHint,
          emptyMessage: emptyMessage,
        ),
      ),
    );
  }

  // ------------------------------------------------------------------
  // Étape permis complémentaires
  // ------------------------------------------------------------------

  Step _stepPermis(bool readOnly) {
    final types = ref.watch(typesPermisProvider);
    final data = widget.state.data;
    AtFormNotifier notifier() => ref.read(atFormProvider(widget.atId).notifier);

    final asyncItems = types.whenData((list) => list
        .map((t) => ReferentielItem(id: t.id, nom: t.nom ?? '', description: t.description))
        .toList(),);

    return Step(
      isActive: true,
      title: const Text('E. Permis complémentaires'),
      content: SizedBox(
        height: MediaQuery.of(context).size.height * 0.42,
        child: MultiSelectStep(
          items: asyncItems,
          selected: data.permisIds,
          onToggle: readOnly ? (_) {} : (id) => notifier().togglePermis(id),
          searchHint: 'Rechercher un type de permis…',
          emptyMessage: 'Aucun type de permis au référentiel.',
        ),
      ),
    );
  }

  // ------------------------------------------------------------------
  // Étape 8 : Section F — Mesures exécutant
  // ------------------------------------------------------------------

  Step _stepMesuresExecutant(bool readOnly) {
    final data = widget.state.data;
    AtFormNotifier notifier() => ref.read(atFormProvider(widget.atId).notifier);

    return Step(
      isActive: true,
      title: const Text('F. Mesures exécutant'),
      content: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          const Text(
            'Mesures de sécurité complémentaires et précautions particulières prises par l\'exécutant.',
            style: TextStyle(fontSize: 12, color: OcpColors.slate),
          ),
          const SizedBox(height: 12),
          TextFormField(
            enabled: !readOnly,
            initialValue: data.mesuresSecuriteExecutant,
            maxLines: 5,
            decoration: const InputDecoration(
              labelText: 'Mesures exécutant (Section F)',
              hintText: 'Préciser les consignes spécifiques, balisage, surveillant...',
            ),
            onChanged: (v) => notifier().update((d) => d.copyWith(mesuresSecuriteExecutant: v)),
          ),
        ],
      ),
    );
  }

  // ------------------------------------------------------------------
  // Étape 9 : récapitulatif + soumission
  // ------------------------------------------------------------------

  Step _stepRecapitulatif(bool readOnly) {
    final d = widget.state.data;
    final hasSubmit = ref.watch(hasPermissionProvider)('SUBMIT_AT');
    final session = ref.watch(sessionProvider);
    final userProprietaireService = session?.utilisateur.service;

    final isSameService = userProprietaireService != null &&
        ((d.serviceIntervenantId != null && d.serviceIntervenantId == userProprietaireService.id) ||
            (d.servicesIntervenants.isNotEmpty &&
                d.servicesIntervenants.toLowerCase().trim() ==
                    userProprietaireService.nomService.toLowerCase().trim()));

    final canSubmit = hasSubmit &&
        !isSameService &&
        (widget.state.at.statut == StatutAt.brouillon ||
            widget.state.at.statut == StatutAt.demandeCreee);

    String listInfo(String label, Set<String> ids) =>
        '$label : ${ids.length} sélectionné${ids.length > 1 ? 's' : ''}';

    return Step(
      isActive: true,
      title: const Text('Récapitulatif'),
      content: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          _recapRow('Objet', d.objet),
          _recapRow('Période',
              '${_fr(d.dateDebut)} → ${_fr(d.dateFin)} (${_h(d.heureDebut)} - ${_h(d.heureFin)})',),
          _recapRow('Zone propriétaire (P)', d.zoneProprietaireNom),
          _recapRow('Zone exécutante / intervenante (E)', d.zoneExecutanteNom),
          _recapRow('Service demandeur / propriétaire (P)', userProprietaireService?.nomService),
          _recapRow('Service intervenant / exécutant (E)', d.servicesIntervenants.isNotEmpty ? d.servicesIntervenants : null),
          if (d.entreprisesIntervenantes.isNotEmpty)
            _recapRow('Entreprise extérieure', d.entreprisesIntervenantes),
          _recapRow(listInfo('Risques (A)', d.risquesIds), null),
          _recapRow(listInfo('Mesures (B)', d.mesuresIds), null),
          _recapRow(listInfo('Moyens d\'accès (C)', d.moyensAccesIds), null),
          _recapRow(listInfo('EPI (D)', d.episIds), null),
          _recapRow(listInfo('Permis (E)', d.permisIds), null),
          if (d.mesuresSecuriteExecutant.isNotEmpty)
            _recapRow('Mesures exécutant (F)', d.mesuresSecuriteExecutant),
          if (isSameService) ...[
            const SizedBox(height: 12),
            Container(
              padding: const EdgeInsets.all(10),
              decoration: BoxDecoration(
                color: OcpColors.errorSoft.withOpacity(0.15),
                borderRadius: BorderRadius.circular(8),
                border: Border.all(color: OcpColors.errorSoft),
              ),
              child: const Row(
                children: [
                  Icon(Icons.error_outline_rounded, color: OcpColors.errorSoft, size: 20),
                  SizedBox(width: 8),
                  Expanded(
                    child: Text(
                      'Soumission impossible : le service propriétaire et le service intervenant doivent obligatoirement être différents.',
                      style: TextStyle(fontSize: 12, color: OcpColors.errorSoft, fontWeight: FontWeight.bold),
                    ),
                  ),
                ],
              ),
            ),
          ],
          const SizedBox(height: 14),
          if (!readOnly && canSubmit)
            FilledButton.icon(
              style: FilledButton.styleFrom(minimumSize: const Size.fromHeight(50)),
              onPressed: _submitting ? null : _submit,
              icon: _submitting
                  ? const SizedBox(width: 18, height: 18, child: CircularProgressIndicator(strokeWidth: 2, color: OcpColors.white))
                  : const Icon(Icons.send_rounded),
              label: const Text('Soumettre pour validation'),
            )
          else
            Text(
              isSameService
                  ? 'Veuillez corriger le service intervenant à l\'étape 3 avant de soumettre.'
                  : 'Soumission non disponible avec vos permissions actuelles.',
              style: const TextStyle(fontSize: 12, color: OcpColors.slate),
            ),
        ],
      ),
    );
  }

  bool _submitting = false;

  Future<void> _submit() async {
    final session = ref.read(sessionProvider);
    final userProprietaireService = session?.utilisateur.service;
    final d = widget.state.data;

    final isSameService = userProprietaireService != null &&
        ((d.serviceIntervenantId != null && d.serviceIntervenantId == userProprietaireService.id) ||
            (d.servicesIntervenants.isNotEmpty &&
                d.servicesIntervenants.toLowerCase().trim() ==
                    userProprietaireService.nomService.toLowerCase().trim()));

    if (isSameService) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          backgroundColor: OcpColors.errorSoft,
          content: Text(
            'Le service propriétaire (${userProprietaireService?.nomService}) doit être différent du service intervenant.',
          ),
        ),
      );
      return;
    }

    setState(() => _submitting = true);
    final notifier = ref.read(atFormProvider(widget.atId).notifier);
    try {
      await notifier.flushSave();
      await ref.read(atApiProvider).soumettre(widget.atId);
      ref.invalidate(atDetailProvider(widget.atId));
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(const SnackBar(
            content: Text('AT soumise pour validation.'),),);
        context.go('/at/${widget.atId}');
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(SnackBar(
            content: Text(e is Failure
                ? e.message
                : 'Échec de la soumission — vérifiez les données et réessayez.',),),);
      }
    } finally {
      if (mounted) setState(() => _submitting = false);
    }
  }

  static String _fr(DateTime? d) => d == null ? '—' :
      '${d.day.toString().padLeft(2, '0')}/${d.month.toString().padLeft(2, '0')}/${d.year}';
  static String _h(TimeOfDay? t) => t == null ? '—'
      : '${t.hour.toString().padLeft(2, '0')}:${t.minute.toString().padLeft(2, '0')}';

  Widget _recapRow(String label, String? value) => Padding(
        padding: const EdgeInsets.symmetric(vertical: 2),
        child: Text(
          value == null ? label : '$label : $value',
          style: const TextStyle(fontSize: 13),
        ),
      );
}

// ----------------------------------------------------------------------
// Sous-widgets
// ----------------------------------------------------------------------

class _ReadOnlyBanner extends StatelessWidget {
  final AtFormState state;
  const _ReadOnlyBanner({required this.state});

  @override
  Widget build(BuildContext context) => Container(
        width: double.infinity,
        color: OcpColors.warningSoft,
        padding: const EdgeInsets.all(10),
        child: Row(children: [
          const Icon(Icons.lock_outline_rounded, size: 16, color: OcpColors.warning),
          const SizedBox(width: 8),
          Expanded(
            child: Text(
              'Lecture seule — édition indisponible (${state.lockHolderName ?? 'verrouillé'}). '
              'Vos modifications ne seront pas sauvegardées.',
              style: const TextStyle(fontSize: 12, color: OcpColors.ink),
            ),
          ),
        ],),
      );
}

class _DateTile extends StatelessWidget {
  final IconData icon;
  final String label;
  final DateTime? value;
  final DateTime? firstDate;
  final ValueChanged<DateTime> onPick;
  final bool enabled;

  const _DateTile({
    required this.icon,
    required this.label,
    required this.value,
    required this.onPick,
    required this.enabled,
    this.firstDate,
  });

  @override
  Widget build(BuildContext context) => Card(
        margin: const EdgeInsets.symmetric(vertical: 4),
        child: ListTile(
          leading: Icon(icon),
          title: Text(label, style: const TextStyle(fontSize: 13)),
          subtitle: Text(value == null
              ? 'Non définie'
              : '${value!.day.toString().padLeft(2, '0')}/${value!.month.toString().padLeft(2, '0')}/${value!.year}',
              style: const TextStyle(fontWeight: FontWeight.w700),),
          trailing: enabled ? const Icon(Icons.edit_calendar_rounded, size: 20) : null,
          onTap: enabled
              ? () async {
                  final now = DateTime.now();
                  final minDate = firstDate ?? DateTime(now.year - 1, 1, 1);
                  final maxDate = DateTime(now.year + 2, 12, 31);
                  DateTime initDate = value ?? now;
                  if (initDate.isBefore(minDate)) initDate = minDate;
                  if (initDate.isAfter(maxDate)) initDate = maxDate;

                  final picked = await showDatePicker(
                    context: context,
                    initialDate: initDate,
                    firstDate: minDate,
                    lastDate: maxDate,
                  );
                  if (picked != null) onPick(picked);
                }
              : null,
        ),
      );
}

class _TimeTile extends StatelessWidget {
  final String label;
  final String value;
  final TimeOfDay? initial;
  final ValueChanged<TimeOfDay> onPick;
  final bool enabled;

  const _TimeTile({
    required this.label,
    required this.value,
    required this.onPick,
    required this.enabled,
    this.initial,
  });

  @override
  Widget build(BuildContext context) => Card(
        margin: const EdgeInsets.symmetric(vertical: 4),
        child: ListTile(
          leading: const Icon(Icons.access_time_rounded),
          title: Text(label, style: const TextStyle(fontSize: 13)),
          subtitle: Text(value, style: const TextStyle(fontWeight: FontWeight.w700)),
          trailing: enabled ? const Icon(Icons.schedule_rounded, size: 20) : null,
          onTap: enabled
              ? () async {
                  final picked = await showTimePicker(context: context, initialTime: initial ?? TimeOfDay.now());
                  if (picked != null) onPick(picked);
                }
              : null,
        ),
      );
}

class _ZoneDropdown extends StatelessWidget {
  final String label;
  final List<Zone> zones;
  final String? valueId;
  final String? fallbackName;
  final ValueChanged<Zone> onChanged;
  final bool enabled;

  const _ZoneDropdown({
    required this.label,
    required this.zones,
    required this.valueId,
    required this.fallbackName,
    required this.onChanged,
    required this.enabled,
  });

  @override
  Widget build(BuildContext context) {
    Zone? value;
    for (final z in zones) {
      if (z.id == valueId || (fallbackName != null && z.nomZone == fallbackName)) {
        value = z;
        break;
      }
    }
    final selectedKey = value?.id ?? valueId;

    return DropdownButtonFormField<String>(
      value: selectedKey,
      isExpanded: true,
      decoration: InputDecoration(
        labelText: label,
        prefixIcon: const Icon(Icons.location_on_outlined, size: 20),
      ),
      hint: const Text('Sélectionner une zone'),
      items: [
        ...zones.map((z) => DropdownMenuItem(value: z.id, child: Text(z.libelle))),
        // Zone connue mais absente du référentiel : la conserver affichée.
        if (value == null && (valueId != null || fallbackName != null))
          DropdownMenuItem(
            value: valueId ?? fallbackName,
            child: Text(fallbackName ?? valueId!, overflow: TextOverflow.ellipsis),
          ),
      ],
      onChanged: enabled
          ? (id) {
              if (id == null) return;
              final found = zones.where((z) => z.id == id).firstOrNull;
              if (found != null) onChanged(found);
            }
          : null,
    );
  }
}

class _ServiceDropdown extends StatelessWidget {
  final String label;
  final List<ServiceOcp> services;
  final String? valueId;
  final String? valueNom;
  final String? forbiddenServiceId;
  final ValueChanged<ServiceOcp> onChanged;
  final bool enabled;

  const _ServiceDropdown({
    required this.label,
    required this.services,
    required this.valueId,
    required this.valueNom,
    this.forbiddenServiceId,
    required this.onChanged,
    required this.enabled,
  });

  @override
  Widget build(BuildContext context) {
    ServiceOcp? selected;
    for (final s in services) {
      if (s.id == valueId || (valueNom != null && valueNom!.isNotEmpty && s.nomService == valueNom)) {
        selected = s;
        break;
      }
    }

    final selectedKey = selected?.id ?? valueId;

    return DropdownButtonFormField<String>(
      value: selectedKey,
      isExpanded: true,
      decoration: InputDecoration(
        labelText: label,
        prefixIcon: const Icon(Icons.engineering_outlined, size: 20),
      ),
      hint: const Text('Sélectionner le service intervenant'),
      items: [
        ...services.map((s) {
          final isForbidden = forbiddenServiceId != null && s.id == forbiddenServiceId;
          return DropdownMenuItem<String>(
            value: s.id,
            child: Row(
              children: [
                Expanded(
                  child: Text(
                    s.libelle,
                    overflow: TextOverflow.ellipsis,
                    style: TextStyle(
                      color: isForbidden ? OcpColors.errorSoft : null,
                      fontWeight: isForbidden ? FontWeight.w600 : FontWeight.normal,
                    ),
                  ),
                ),
                if (isForbidden)
                  const Text(
                    ' (Service propriétaire)',
                    style: TextStyle(fontSize: 10, color: OcpColors.errorSoft),
                  ),
              ],
            ),
          );
        }),
        if (selected == null && (valueId != null || (valueNom != null && valueNom!.isNotEmpty)))
          DropdownMenuItem<String>(
            value: valueId ?? valueNom,
            child: Text(valueNom ?? valueId!, overflow: TextOverflow.ellipsis),
          ),
      ],
      onChanged: enabled
          ? (id) {
              if (id == null) return;
              final found = services.where((s) => s.id == id).firstOrNull;
              if (found != null) {
                onChanged(found);
              }
            }
          : null,
    );
  }
}

class _EntrepriseDropdown extends StatelessWidget {
  final String label;
  final List<EntrepriseExterne> entreprises;
  final String? value;
  final ValueChanged<String> onChanged;
  final bool enabled;

  const _EntrepriseDropdown({
    required this.label,
    required this.entreprises,
    required this.value,
    required this.onChanged,
    required this.enabled,
  });

  @override
  Widget build(BuildContext context) {
    final currentVal = (value == null || value!.isEmpty) ? '' : value;

    return DropdownButtonFormField<String>(
      value: (currentVal == '' || entreprises.any((e) => e.libelle == currentVal)) ? currentVal : null,
      isExpanded: true,
      decoration: InputDecoration(
        labelText: label,
        prefixIcon: const Icon(Icons.handyman_outlined, size: 20),
      ),
      items: [
        const DropdownMenuItem<String>(
          value: '',
          child: Text('Aucune (Régie interne OCP)', style: TextStyle(fontStyle: FontStyle.italic)),
        ),
        ...entreprises.map((e) => DropdownMenuItem<String>(
              value: e.libelle,
              child: Text(e.libelle, overflow: TextOverflow.ellipsis),
            )),
        if (currentVal != '' && !entreprises.any((e) => e.libelle == currentVal))
          DropdownMenuItem<String>(
            value: currentVal,
            child: Text(currentVal!, overflow: TextOverflow.ellipsis),
          ),
      ],
      onChanged: enabled
          ? (val) {
              onChanged(val ?? '');
            }
          : null,
    );
  }
}

