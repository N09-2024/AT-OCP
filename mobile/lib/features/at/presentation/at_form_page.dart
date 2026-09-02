/// Formulaire AT mobile par étapes - Design UI/UX Moderne OCP S-HSE-SEC-31 :
/// - En-tête Wizard avec barre de progression animée et sélecteur d'étapes modal
/// - Navigation fluide Page par Page avec barre d'actions épinglée en bas
/// - Étape 1 : Général & Document source (DI, OT, BT) & Entreprises
/// - Étape 2 : Planning (Dates et heures)
/// - Étape 3 : Localisation (Zones P/E, Services P/E)
/// - Étape 4 : Visite préalable conjointe (§8.2 - GPS & Photo)
/// - Étape 5 à 8 : Référentiels (Risques A, Mesures B, Moyens d'accès C, EPI D)
/// - Étape 9 : Permis complémentaires (Section E) + Upload photo/document + Analyse IA Gemini
/// - Étape 10 : Mesures de sécurité de l'exécutant (Section F)
/// - Étape 11 : Récapitulatif + Point de contrôle + Soumission
library;

import 'dart:io';
import 'dart:math';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:geolocator/geolocator.dart';
import 'package:go_router/go_router.dart';
import 'package:image_picker/image_picker.dart';
import '../../../core/errors/failures.dart';
import '../../../core/network/api_providers.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/widgets/states.dart';
import '../../assistant/data/assistant_api.dart';
import '../../auth/presentation/auth_controller.dart';
import '../../referentiels/data/referentiel_models.dart';
import '../../referentiels/presentation/referentiels_providers.dart';
import '../data/models/autorisation_travail.dart';
import 'at_form_controller.dart';
import 'at_circuit_visas.dart';
import 'at_providers.dart';
import 'widgets/multi_select_step.dart';
import 'widgets/permis_upload_tile.dart';

/// Numéro de document source auto-attribué : {TYPE}-{6 chiffres aléatoires}
/// (ex. DI-483920). Principe web : le numéro est généré automatiquement,
/// jamais saisi manuellement.
String genererNumeroDocument(String type) {
  final alea = Random().nextInt(900000) + 100000; // 100000..999999 (6 chiffres)
  return '$type-$alea';
}

class AtFormPage extends ConsumerWidget {
  final String atId;
  const AtFormPage({super.key, required this.atId});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final form = ref.watch(atFormProvider(atId));

    return Scaffold(
      backgroundColor: OcpColors.sage,
      appBar: AppBar(
        backgroundColor: OcpColors.forest,
        foregroundColor: OcpColors.white,
        elevation: 0,
        title: Text(
          form.at.numero ?? 'Formulaire AT',
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
        null => _AtFormWizard(atId: atId, state: form),
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
            child: Row(
              children: [
                const Icon(Icons.cloud_done_rounded, size: 14, color: OcpColors.mint),
                const SizedBox(width: 4),
                Text(
                  h == null ? 'Enregistré' : '${h.hour.toString().padLeft(2, '0')}:${h.minute.toString().padLeft(2, '0')}',
                  style: const TextStyle(color: OcpColors.mint, fontSize: 11, fontWeight: FontWeight.w600),
                ),
              ],
            ),
          ),
        );
      case SaveStatus.error:
        return IconButton(
          tooltip: 'Échec de la sauvegarde - réessayer',
          onPressed: () => ref.read(atFormProvider(state.at.id).notifier).retrySave(),
          icon: const Icon(Icons.cloud_off_rounded, color: OcpColors.errorSoft),
        );
      case SaveStatus.idle:
        return const SizedBox.shrink();
    }
  }
}

class _StepMeta {
  final String title;
  final String subtitle;
  final IconData icon;
  const _StepMeta({required this.title, required this.subtitle, required this.icon});
}

class _AtFormWizard extends ConsumerStatefulWidget {
  final String atId;
  final AtFormState state;

  const _AtFormWizard({required this.atId, required this.state});

  @override
  ConsumerState<_AtFormWizard> createState() => _AtFormWizardState();
}

class _AtFormWizardState extends ConsumerState<_AtFormWizard> {
  int _step = 0;
  bool _gpsLoading = false;
  bool _submitting = false;
  bool _numeroAutoGenere = false;

  @override
  void initState() {
    super.initState();
    // Nouvelle AT : attribuer automatiquement le numéro de document source
    // ({TYPE}-{6 chiffres}) sans aucune saisie manuelle.
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (_numeroAutoGenere) return;
      _numeroAutoGenere = true;
      final d = widget.state.data;
      if (d.documentSourceNumero.isEmpty && !widget.state.readOnly) {
        ref.read(atFormProvider(widget.atId).notifier).update(
              (d) => d.copyWith(
                documentSourceNumero: genererNumeroDocument(d.typeDocumentSource),
              ),
            );
      }
    });
  }

  static const List<_StepMeta> _steps = [
    _StepMeta(
      title: 'Informations générales',
      subtitle: 'Objet, document source (DI/OT/BT) & entreprises',
      icon: Icons.description_outlined,
    ),
    _StepMeta(
      title: 'Planning & horaires',
      subtitle: 'Période et créneau horaire prévisionnels',
      icon: Icons.schedule_outlined,
    ),
    _StepMeta(
      title: 'Localisation & services',
      subtitle: 'Zones P/E et séparation des services CEEP/CEEE',
      icon: Icons.apartment_outlined,
    ),
    _StepMeta(
      title: 'Visite préalable conjointe (§8.2)',
      subtitle: 'Géolocalisation GPS, photo du chantier et contrôle',
      icon: Icons.my_location_rounded,
    ),
    _StepMeta(
      title: 'A. Risques liés aux travaux',
      subtitle: 'Identification des risques majeurs HSE',
      icon: Icons.warning_amber_rounded,
    ),
    _StepMeta(
      title: 'B. Mesures de sécurité',
      subtitle: 'Consignes de consignation et préparation',
      icon: Icons.shield_outlined,
    ),
    _StepMeta(
      title: "C. Moyens d'accès",
      subtitle: 'Échafaudages, nacelles, échelles et accès',
      icon: Icons.stairs_outlined,
    ),
    _StepMeta(
      title: 'D. Équipements de protection (EPI)',
      subtitle: 'EPI obligatoires pour l\'intervention',
      icon: Icons.safety_check_outlined,
    ),
    _StepMeta(
      title: 'E. Permis complémentaires',
      subtitle: 'Permis spécifiques et analyse IA Gemini',
      icon: Icons.badge_outlined,
    ),
    _StepMeta(
      title: "F. Mesures de l'exécutant",
      subtitle: 'Consignes et précautions de l\'équipe exécutante',
      icon: Icons.handyman_outlined,
    ),
    _StepMeta(
      title: 'Récapitulatif & soumission',
      subtitle: 'Vérification globale, signature CEEP et transmission',
      icon: Icons.send_rounded,
    ),
  ];

  Future<void> _captureGps() async {
    setState(() => _gpsLoading = true);
    try {
      bool serviceEnabled = await Geolocator.isLocationServiceEnabled();
      if (!serviceEnabled) {
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(content: Text('Veuillez activer le service de localisation GPS de votre appareil.')),
          );
        }
        return;
      }

      LocationPermission permission = await Geolocator.checkPermission();
      if (permission == LocationPermission.denied) {
        permission = await Geolocator.requestPermission();
        if (permission == LocationPermission.denied) {
          if (mounted) {
            ScaffoldMessenger.of(context).showSnackBar(
              const SnackBar(content: Text('Permission GPS refusée.')),
            );
          }
          return;
        }
      }

      if (permission == LocationPermission.deniedForever) {
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(content: Text('Permission GPS définitivement refusée. Veuillez l\'autoriser dans les paramètres.')),
          );
        }
        return;
      }

      final position = await Geolocator.getCurrentPosition(
        locationSettings: const LocationSettings(
          accuracy: LocationAccuracy.high,
          timeLimit: Duration(seconds: 12),
        ),
      );

      ref.read(atFormProvider(widget.atId).notifier).update(
        (d) => d.copyWith(
          latitude: position.latitude,
          longitude: position.longitude,
        ),
      );

      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            backgroundColor: OcpColors.forest,
            content: Text('Position GPS relevée : ${position.latitude.toStringAsFixed(5)}, ${position.longitude.toStringAsFixed(5)}'),
          ),
        );
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Erreur géolocalisation : $e')),
        );
      }
    } finally {
      if (mounted) setState(() => _gpsLoading = false);
    }
  }

  void _showStepSelectorModal() {
    showModalBottomSheet(
      context: context,
      backgroundColor: OcpColors.white,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
      ),
      builder: (ctx) => SafeArea(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Padding(
              padding: const EdgeInsets.fromLTRB(20, 16, 16, 8),
              child: Row(
                children: [
                  const Icon(Icons.list_alt_rounded, color: OcpColors.forest, size: 22),
                  const SizedBox(width: 10),
                  const Expanded(
                    child: Text(
                      'Sommaire des étapes (Standard OCP)',
                      style: TextStyle(
                        fontFamily: 'SpaceGrotesk',
                        fontWeight: FontWeight.w700,
                        fontSize: 16,
                        color: OcpColors.ink,
                      ),
                    ),
                  ),
                  IconButton(
                    onPressed: () => Navigator.pop(ctx),
                    icon: const Icon(Icons.close_rounded, size: 20),
                  ),
                ],
              ),
            ),
            const Divider(height: 1),
            Flexible(
              child: ListView.builder(
                shrinkWrap: true,
                itemCount: _steps.length,
                itemBuilder: (context, idx) {
                  final meta = _steps[idx];
                  final isCurrent = _step == idx;
                  final isPassed = _step > idx;

                  return ListTile(
                    dense: true,
                    selected: isCurrent,
                    selectedTileColor: OcpColors.forestSoft.withValues(alpha: 0.35),
                    leading: CircleAvatar(
                      radius: 14,
                      backgroundColor: isCurrent
                          ? OcpColors.forest
                          : (isPassed ? OcpColors.moss : OcpColors.borderSoft),
                      foregroundColor: isCurrent || isPassed ? OcpColors.white : OcpColors.slate,
                      child: isPassed
                          ? const Icon(Icons.check_rounded, size: 14)
                          : Text('${idx + 1}', style: const TextStyle(fontSize: 11, fontWeight: FontWeight.bold)),
                    ),
                    title: Text(
                      meta.title,
                      style: TextStyle(
                        fontWeight: isCurrent ? FontWeight.w800 : FontWeight.w600,
                        fontSize: 13,
                        color: isCurrent ? OcpColors.forest : OcpColors.ink,
                      ),
                    ),
                    subtitle: Text(
                      meta.subtitle,
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                      style: const TextStyle(fontSize: 11, color: OcpColors.slate),
                    ),
                    trailing: isCurrent
                        ? const Icon(Icons.arrow_forward_ios_rounded, size: 12, color: OcpColors.forest)
                        : null,
                    onTap: () {
                      Navigator.pop(ctx);
                      setState(() => _step = idx);
                    },
                  );
                },
              ),
            ),
          ],
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final readOnly = widget.state.readOnly;
    final meta = _steps[_step];
    final progress = (_step + 1) / _steps.length;

    return PopScope(
      canPop: true,
      onPopInvokedWithResult: (didPop, result) {
        if (!didPop) return;
        ref.read(atFormProvider(widget.atId).notifier).releaseLockAndFlush();
      },
      child: Column(
        children: [
          if (readOnly) _ReadOnlyBanner(state: widget.state),

          // ── EN-TÊTE WIZARD HAUT DE GAMME ─────────────────────────────
          Container(
            color: OcpColors.white,
            padding: const EdgeInsets.fromLTRB(16, 12, 16, 10),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                Row(
                  children: [
                    Container(
                      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
                      decoration: BoxDecoration(
                        color: OcpColors.forestSoft,
                        borderRadius: BorderRadius.circular(6),
                      ),
                      child: Text(
                        'Étape ${_step + 1} / ${_steps.length}',
                        style: const TextStyle(
                          fontSize: 11,
                          fontWeight: FontWeight.w800,
                          color: OcpColors.forestDark,
                        ),
                      ),
                    ),
                    const Spacer(),
                    InkWell(
                      borderRadius: BorderRadius.circular(6),
                      onTap: _showStepSelectorModal,
                      child: const Padding(
                        padding: EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                        child: Row(
                          children: [
                            Icon(Icons.list_alt_rounded, size: 16, color: OcpColors.forest),
                            SizedBox(width: 4),
                            Text(
                              'Sommaire',
                              style: TextStyle(
                                fontSize: 12,
                                fontWeight: FontWeight.w700,
                                color: OcpColors.forest,
                              ),
                            ),
                          ],
                        ),
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 6),
                Row(
                  children: [
                    Icon(meta.icon, size: 20, color: OcpColors.forest),
                    const SizedBox(width: 8),
                    Expanded(
                      child: Text(
                        meta.title,
                        style: const TextStyle(
                          fontFamily: 'SpaceGrotesk',
                          fontWeight: FontWeight.w800,
                          fontSize: 16,
                          color: OcpColors.ink,
                        ),
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 2),
                Text(
                  meta.subtitle,
                  style: const TextStyle(fontSize: 11, color: OcpColors.slate),
                ),
                const SizedBox(height: 8),
                ClipRRect(
                  borderRadius: BorderRadius.circular(4),
                  child: LinearProgressIndicator(
                    value: progress,
                    minHeight: 4.5,
                    backgroundColor: OcpColors.borderSoft,
                    valueColor: const AlwaysStoppedAnimation<Color>(OcpColors.forest),
                  ),
                ),
              ],
            ),
          ),

          // ── CONTENU DE L'ÉTAPE ACTIVE ─────────────────────────────────
          Expanded(
            child: SingleChildScrollView(
              padding: const EdgeInsets.fromLTRB(16, 14, 16, 24),
              child: _buildCurrentStepContent(readOnly),
            ),
          ),

          // ── BARRE D'ACTIONS ÉPINGLÉE EN BAS (STICKY FOOTER) ───────────
          Container(
            decoration: BoxDecoration(
              color: OcpColors.white,
              border: const Border(top: BorderSide(color: OcpColors.borderSoft)),
              boxShadow: [
                BoxShadow(
                  color: OcpColors.deep.withValues(alpha: 0.04),
                  blurRadius: 10,
                  offset: const Offset(0, -3),
                ),
              ],
            ),
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
            child: SafeArea(
              child: Row(
                children: [
                  if (_step > 0)
                    Expanded(
                      flex: 2,
                      child: OutlinedButton.icon(
                        style: OutlinedButton.styleFrom(
                          minimumSize: const Size.fromHeight(46),
                          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
                        ),
                        onPressed: () => setState(() => _step--),
                        icon: const Icon(Icons.arrow_back_rounded, size: 18),
                        label: const Text('Précédent'),
                      ),
                    )
                  else
                    const Spacer(flex: 2),
                  const SizedBox(width: 12),
                  Expanded(
                    flex: 3,
                    child: _step < _steps.length - 1
                        ? FilledButton.icon(
                            style: FilledButton.styleFrom(
                              backgroundColor: OcpColors.forest,
                              foregroundColor: OcpColors.white,
                              minimumSize: const Size.fromHeight(46),
                              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
                            ),
                            onPressed: () => setState(() => _step++),
                            icon: const Icon(Icons.arrow_forward_rounded, size: 18),
                            label: const Text('Suivant', style: TextStyle(fontWeight: FontWeight.w700)),
                          )
                        : FilledButton.icon(
                            style: FilledButton.styleFrom(
                              backgroundColor: OcpColors.forest,
                              foregroundColor: OcpColors.white,
                              minimumSize: const Size.fromHeight(46),
                              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
                            ),
                            onPressed: _submitting ? null : _submit,
                            icon: _submitting
                                ? const SizedBox(
                                    width: 16,
                                    height: 16,
                                    child: CircularProgressIndicator(strokeWidth: 2, color: OcpColors.white),
                                  )
                                : const Icon(Icons.send_rounded, size: 18),
                            label: Text(
                              _submitting ? 'Transmission…' : 'Signer & Transmettre',
                              style: const TextStyle(fontWeight: FontWeight.w800),
                            ),
                          ),
                  ),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildCurrentStepContent(bool readOnly) {
    switch (_step) {
      case 0:
        return _stepGeneral(readOnly);
      case 1:
        return _stepPlanning(readOnly);
      case 2:
        return _stepLocalisation(readOnly);
      case 3:
        return _stepVisitePrealable(readOnly);
      case 4:
        return _stepReferentiel(
          title: 'A. Risques liés aux travaux',
          items: ref.watch(risquesProvider),
          selected: widget.state.data.risquesIds,
          onToggle: (id) => ref.read(atFormProvider(widget.atId).notifier).toggleRisque(id),
          searchHint: 'Rechercher un risque…',
          emptyMessage: 'Aucun risque au référentiel.',
          readOnly: readOnly,
        );
      case 5:
        return _stepReferentiel(
          title: 'B. Mesures de sécurité à prendre',
          items: ref.watch(mesuresProvider),
          selected: widget.state.data.mesuresIds,
          onToggle: (id) => ref.read(atFormProvider(widget.atId).notifier).toggleMesure(id),
          searchHint: 'Rechercher une mesure…',
          emptyMessage: 'Aucune mesure au référentiel.',
          readOnly: readOnly,
        );
      case 6:
        return _stepReferentiel(
          title: 'C. Moyens d\'accès',
          items: ref.watch(moyensAccesProvider),
          selected: widget.state.data.moyensAccesIds,
          onToggle: (id) => ref.read(atFormProvider(widget.atId).notifier).toggleMoyenAcces(id),
          searchHint: 'Rechercher un moyen d\'accès…',
          emptyMessage: 'Aucun moyen d\'accès au référentiel.',
          readOnly: readOnly,
        );
      case 7:
        return _stepReferentiel(
          title: 'D. Équipements de protection (EPI)',
          items: ref.watch(episProvider),
          selected: widget.state.data.episIds,
          onToggle: (id) => ref.read(atFormProvider(widget.atId).notifier).toggleEpi(id),
          searchHint: 'Rechercher un EPI…',
          emptyMessage: 'Aucun EPI au référentiel.',
          readOnly: readOnly,
        );
      case 8:
        return _stepPermis(readOnly);
      case 9:
        return _stepMesuresExecutant(readOnly);
      case 10:
      default:
        return _stepRecapitulatif(readOnly);
    }
  }

  // ------------------------------------------------------------------
  // Étape 1 : informations générales, document source & tiers
  // ------------------------------------------------------------------

  Widget _stepGeneral(bool readOnly) {
    final data = widget.state.data;
    AtFormNotifier notifier() => ref.read(atFormProvider(widget.atId).notifier);
    final entreprisesAsync = ref.watch(entreprisesExternesProvider);

    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        _formCard(
          title: 'Nature de l\'intervention',
          children: [
            TextFormField(
              enabled: !readOnly,
              initialValue: data.objet,
              maxLength: 200,
              decoration: const InputDecoration(
                labelText: 'Objet des travaux *',
                hintText: 'Ex: Remplacement vanne DN150 sur circuit phosphorique',
                prefixIcon: Icon(Icons.edit_note_rounded, size: 20),
              ),
              onChanged: (v) => notifier().update((d) => d.copyWith(objet: v)),
            ),
            const SizedBox(height: 12),
            TextFormField(
              enabled: !readOnly,
              initialValue: data.descriptionTravaux,
              maxLines: 4,
              decoration: const InputDecoration(
                labelText: 'Description détaillée des travaux',
                hintText: 'Préciser les équipements, opérations de meulage, soudure...',
              ),
              onChanged: (v) => notifier().update((d) => d.copyWith(descriptionTravaux: v)),
            ),
          ],
        ),
        const SizedBox(height: 14),

        _formCard(
          title: 'Document source & entreprises',
          children: [
            DropdownButtonFormField<String>(
              initialValue: data.typeDocumentSource,
              isExpanded: true,
              decoration: const InputDecoration(
                labelText: 'Type de document source *',
                prefixIcon: Icon(Icons.description_outlined, size: 20),
              ),
              items: const [
                DropdownMenuItem(value: 'DI', child: Text('DI (Demande d\'Intervention)')),
                DropdownMenuItem(value: 'OT', child: Text('OT (Ordre de Travail)')),
                DropdownMenuItem(value: 'BT', child: Text('BT (Bon de Travail)')),
              ],
              onChanged: readOnly
                  ? null
                  : (v) {
                      final type = v ?? 'DI';
                      // Le numéro est ré-attribué automatiquement à chaque
                      // changement de type (principe web).
                      notifier().update((d) => d.copyWith(
                            typeDocumentSource: type,
                            documentSourceNumero: genererNumeroDocument(type),
                            clearDocumentSource: true,
                          ));
                    },
            ),
            const SizedBox(height: 16),
            // Numéro auto-attribué : {TYPE}-{6 chiffres aléatoires}, jamais
            // saisi manuellement (principe web).
            InputDecorator(
              decoration: const InputDecoration(
                labelText: 'N° du document source (auto-attribué)',
                prefixIcon: Icon(Icons.tag_rounded, size: 20),
                helperText: 'Généré automatiquement à partir du type de document.',
              ),
              child: Text(
                data.documentSourceNumero.isEmpty
                    ? 'Génération automatique…'
                    : data.documentSourceNumero,
                style: const TextStyle(
                  fontSize: 14,
                  fontWeight: FontWeight.w600,
                  color: OcpColors.ink,
                ),
              ),
            ),
            const SizedBox(height: 16),
            entreprisesAsync.when(
              loading: () => const LinearProgressIndicator(),
              error: (e, st) => TextFormField(
                enabled: !readOnly,
                initialValue: data.entreprisesIntervenantes,
                decoration: const InputDecoration(labelText: 'Entreprises intervenantes (sous-traitant)'),
                onChanged: (v) => notifier().update((d) => d.copyWith(entreprisesIntervenantes: v)),
              ),
              data: (entreprisesList) => _EntrepriseDropdown(
                label: 'Entreprise extérieure (sous-traitant)',
                entreprises: entreprisesList,
                value: data.entreprisesIntervenantes,
                enabled: !readOnly,
                onChanged: (val) => notifier().update((d) => d.copyWith(entreprisesIntervenantes: val)),
              ),
            ),
          ],
        ),
      ],
    );
  }

  // ------------------------------------------------------------------
  // Étape 2 : planning
  // ------------------------------------------------------------------

  Widget _stepPlanning(bool readOnly) {
    final data = widget.state.data;
    AtFormNotifier notifier() => ref.read(atFormProvider(widget.atId).notifier);
    String h(TimeOfDay? t) =>
        t == null ? '-' : '${t.hour.toString().padLeft(2, '0')}:${t.minute.toString().padLeft(2, '0')}';

    return _formCard(
      title: 'Planning prévisionnel de l\'intervention',
      children: [
        _DateTile(
          icon: Icons.calendar_today_outlined,
          label: 'Date de début de validité *',
          value: data.dateDebut,
          enabled: !readOnly,
          onPick: (d) => notifier().update((x) => x.copyWith(dateDebut: d)),
        ),
        const SizedBox(height: 12),
        _DateTile(
          icon: Icons.event_outlined,
          label: 'Date de fin de validité *',
          value: data.dateFin,
          enabled: !readOnly,
          firstDate: data.dateDebut,
          onPick: (d) => notifier().update((x) => x.copyWith(dateFin: d)),
        ),
        const SizedBox(height: 16),
        Row(
          children: [
            Expanded(
              child: _TimeTile(
                label: 'Heure de début',
                value: h(data.heureDebut),
                enabled: !readOnly,
                initial: data.heureDebut,
                onPick: (t) => notifier().update((x) => x.copyWith(heureDebut: t)),
              ),
            ),
            const SizedBox(width: 10),
            Expanded(
              child: _TimeTile(
                label: 'Heure de fin',
                value: h(data.heureFin),
                enabled: !readOnly,
                initial: data.heureFin,
                onPick: (t) => notifier().update((x) => x.copyWith(heureFin: t)),
              ),
            ),
          ],
        ),
      ],
    );
  }

  // ------------------------------------------------------------------
  // Étape 3 : localisation & services (zones P/E, services P/E)
  // ------------------------------------------------------------------

  Widget _stepLocalisation(bool readOnly) {
    final data = widget.state.data;
    AtFormNotifier notifier() => ref.read(atFormProvider(widget.atId).notifier);
    final zonesAsync = ref.watch(zonesProvider);
    final servicesAsync = ref.watch(servicesProvider);
    final session = ref.watch(sessionProvider);
    final userProprietaireService = session?.utilisateur.service;

    final isSameService = userProprietaireService != null &&
        ((data.serviceIntervenantId != null && data.serviceIntervenantId == userProprietaireService.id) ||
            (data.servicesIntervenants.isNotEmpty &&
                userProprietaireService.nomService != null &&
                data.servicesIntervenants.toLowerCase().trim() ==
                    userProprietaireService.nomService!.toLowerCase().trim()));

    return zonesAsync.when(
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
          _formCard(
            title: 'Zones géographiques & périmètre',
            children: [
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
              const SizedBox(height: 16),
              _ZoneDropdown(
                label: 'Zone exécutante (E) *',
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
            ],
          ),
          const SizedBox(height: 16),

          _formCard(
            title: 'Affectation des services (règle CEEP / CEEE)',
            children: [
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 12),
                decoration: BoxDecoration(
                  color: OcpColors.surfaceSoft,
                  borderRadius: BorderRadius.circular(8),
                  border: Border.all(color: OcpColors.border),
                ),
                child: Row(
                  children: [
                    const Icon(Icons.business_rounded, size: 20, color: OcpColors.forest),
                    const SizedBox(width: 10),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          const Text(
                            'Service demandeur (P)',
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
              const SizedBox(height: 16),
              servicesAsync.when(
                loading: () => const LinearProgressIndicator(),
                error: (e, st) => TextFormField(
                  enabled: !readOnly,
                  initialValue: data.servicesIntervenants,
                  decoration: const InputDecoration(
                    labelText: 'Service exécutant (E) *',
                  ),
                  onChanged: (v) => notifier().update((d) => d.copyWith(servicesIntervenants: v)),
                ),
                data: (servicesList) => _ServiceDropdown(
                  label: 'Service exécutant (E) *',
                  services: servicesList,
                  valueId: data.serviceIntervenantId,
                  valueNom: data.servicesIntervenants,
                  forbiddenServiceId: userProprietaireService?.id,
                  enabled: !readOnly,
                  onChanged: (s) => notifier().update(
                    (d) => d.copyWith(
                      serviceIntervenantId: s.id,
                      servicesIntervenants: s.nomService,
                    ),
                  ),
                ),
              ),
              if (isSameService) ...[
                const SizedBox(height: 10),
                Container(
                  padding: const EdgeInsets.all(10),
                  decoration: BoxDecoration(
                    color: OcpColors.errorSoft.withValues(alpha: 0.15),
                    borderRadius: BorderRadius.circular(8),
                    border: Border.all(color: OcpColors.errorSoft),
                  ),
                  child: const Row(
                    children: [
                      Icon(Icons.warning_amber_rounded, size: 20, color: OcpColors.errorSoft),
                      SizedBox(width: 8),
                      Expanded(
                        child: Text(
                          'Séparation obligatoire : le service demandeur (P) doit différer du service exécutant (E).',
                          style: TextStyle(fontSize: 12, color: OcpColors.errorSoft, fontWeight: FontWeight.bold),
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            ],
          ),
        ],
      ),
    );
  }

  // ------------------------------------------------------------------
  // Étape 4 : Visite préalable conjointe (§8.2 - GPS & Photo)
  // ------------------------------------------------------------------

  Widget _stepVisitePrealable(bool readOnly) {
    final data = widget.state.data;
    AtFormNotifier notifier() => ref.read(atFormProvider(widget.atId).notifier);
    final hasGps = data.latitude != null && data.longitude != null;

    Future<void> pickPhoto(ImageSource source) async {
      try {
        final picker = ImagePicker();
        final photo = await picker.pickImage(source: source, imageQuality: 85);
        if (photo == null) return;
        notifier().update((d) => d.copyWith(photoPath: photo.path));
      } catch (e) {
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(content: Text('Erreur appareil photo : $e')),
          );
        }
      }
    }

    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        // 1. GÉOLOCALISATION GPS
        _formCard(
          title: '1. Géolocalisation GPS du chantier (§8.2)',
          children: [
            if (hasGps) ...[
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
                decoration: BoxDecoration(
                  color: OcpColors.forestSoft.withValues(alpha: 0.35),
                  borderRadius: BorderRadius.circular(8),
                  border: Border.all(color: OcpColors.forest),
                ),
                child: Row(
                  children: [
                    const Icon(Icons.check_circle_rounded, color: OcpColors.forest, size: 18),
                    const SizedBox(width: 8),
                    Expanded(
                      child: Text(
                        'GPS: Lat ${data.latitude!.toStringAsFixed(5)}, Lng ${data.longitude!.toStringAsFixed(5)}',
                        style: const TextStyle(
                          fontWeight: FontWeight.w700,
                          fontSize: 13,
                          color: OcpColors.forestDark,
                        ),
                      ),
                    ),
                    if (!readOnly)
                      IconButton(
                        icon: const Icon(Icons.refresh_rounded, size: 20, color: OcpColors.forest),
                        tooltip: 'Relever à nouveau la position GPS',
                        onPressed: _gpsLoading ? null : _captureGps,
                      ),
                    if (!readOnly)
                      IconButton(
                        icon: const Icon(Icons.close_rounded, size: 18, color: OcpColors.slate),
                        tooltip: 'Effacer les coordonnées',
                        onPressed: () => notifier().update((d) => d.copyWith(latitude: null, longitude: null)),
                      ),
                  ],
                ),
              ),
            ] else ...[
              FilledButton.icon(
                style: FilledButton.styleFrom(
                  backgroundColor: OcpColors.forest,
                  foregroundColor: OcpColors.white,
                  minimumSize: const Size.fromHeight(46),
                  shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
                ),
                onPressed: (readOnly || _gpsLoading) ? null : _captureGps,
                icon: _gpsLoading
                    ? const SizedBox(
                        width: 16,
                        height: 16,
                        child: CircularProgressIndicator(strokeWidth: 2, color: OcpColors.white),
                      )
                    : const Icon(Icons.my_location_rounded, size: 18),
                label: Text(
                  _gpsLoading ? 'Acquisition GPS en cours…' : 'Relever la position GPS sur le chantier',
                  style: const TextStyle(fontWeight: FontWeight.w700, fontSize: 13),
                ),
              ),
            ],
            const SizedBox(height: 10),
            Row(
              children: [
                Expanded(
                  child: TextFormField(
                    enabled: !readOnly,
                    key: ValueKey('lat_${data.latitude}'),
                    initialValue: data.latitude?.toString() ?? '',
                    keyboardType: const TextInputType.numberWithOptions(decimal: true),
                    decoration: const InputDecoration(
                      labelText: 'Latitude',
                      isDense: true,
                    ),
                    onChanged: (v) => notifier().update((d) => d.copyWith(latitude: double.tryParse(v))),
                  ),
                ),
                const SizedBox(width: 8),
                Expanded(
                  child: TextFormField(
                    enabled: !readOnly,
                    key: ValueKey('lng_${data.longitude}'),
                    initialValue: data.longitude?.toString() ?? '',
                    keyboardType: const TextInputType.numberWithOptions(decimal: true),
                    decoration: const InputDecoration(
                      labelText: 'Longitude',
                      isDense: true,
                    ),
                    onChanged: (v) => notifier().update((d) => d.copyWith(longitude: double.tryParse(v))),
                  ),
                ),
              ],
            ),
          ],
        ),
        const SizedBox(height: 14),

        // 2. PHOTO D'INSPECTION
        _formCard(
          title: "2. Photo d'inspection du chantier (§8.2)",
          children: [
            if (data.photoPath != null && data.photoPath!.isNotEmpty) ...[
              ClipRRect(
                borderRadius: BorderRadius.circular(8),
                child: Image.file(
                  File(data.photoPath!),
                  height: 150,
                  width: double.infinity,
                  fit: BoxFit.cover,
                  errorBuilder: (context, error, stackTrace) => Container(
                    height: 70,
                    color: OcpColors.surfaceSoft,
                    alignment: Alignment.center,
                    child: const Text('📷 Photo enregistrée'),
                  ),
                ),
              ),
              const SizedBox(height: 8),
              if (!readOnly)
                Row(
                  children: [
                    Expanded(
                      child: OutlinedButton.icon(
                        onPressed: () => pickPhoto(ImageSource.camera),
                        icon: const Icon(Icons.replay_rounded, size: 16),
                        label: const Text('Reprendre'),
                      ),
                    ),
                    const SizedBox(width: 8),
                    IconButton(
                      icon: const Icon(Icons.delete_outline_rounded, color: OcpColors.errorSoft),
                      tooltip: 'Supprimer la photo',
                      onPressed: () => notifier().update((d) => d.copyWith(photoPath: null)),
                    ),
                  ],
                ),
            ] else ...[
              if (!readOnly)
                Row(
                  children: [
                    Expanded(
                      child: FilledButton.icon(
                        style: FilledButton.styleFrom(
                          backgroundColor: const Color(0xFF2E624A),
                          foregroundColor: OcpColors.white,
                          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
                        ),
                        onPressed: () => pickPhoto(ImageSource.camera),
                        icon: const Icon(Icons.photo_camera_rounded, size: 18),
                        label: const Text('Prendre photo', style: TextStyle(fontWeight: FontWeight.w700)),
                      ),
                    ),
                    const SizedBox(width: 8),
                    Expanded(
                      child: OutlinedButton.icon(
                        style: OutlinedButton.styleFrom(
                          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
                        ),
                        onPressed: () => pickPhoto(ImageSource.gallery),
                        icon: const Icon(Icons.photo_library_rounded, size: 18),
                        label: const Text('Galerie'),
                      ),
                    ),
                  ],
                ),
            ],
          ],
        ),
        const SizedBox(height: 14),

        // 3. CONSTATS TERRAIN
        _formCard(
          title: '3. Constats & remarques du terrain',
          children: [
            TextFormField(
              enabled: !readOnly,
              initialValue: data.visiteCommentaire,
              maxLines: 3,
              decoration: const InputDecoration(
                labelText: 'Observations de la visite conjointe',
                hintText: 'Accès sécurisés, risques spécifiques relevés sur site...',
              ),
              onChanged: (v) => notifier().update((d) => d.copyWith(visiteCommentaire: v)),
            ),
          ],
        ),
        const SizedBox(height: 14),

        // 4. POINT DE CONTRÔLE §8.2
        Container(
          padding: const EdgeInsets.all(12),
          decoration: BoxDecoration(
            color: data.visiteEffectuee
                ? OcpColors.forestSoft.withValues(alpha: 0.25)
                : OcpColors.warningSoft,
            borderRadius: BorderRadius.circular(12),
            border: Border.all(
              color: data.visiteEffectuee ? OcpColors.forest : OcpColors.warning,
              width: 1.5,
            ),
          ),
          child: Row(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Checkbox(
                value: data.visiteEffectuee,
                onChanged: readOnly
                    ? null
                    : (v) => notifier().update((d) => d.copyWith(visiteEffectuee: v ?? false)),
                activeColor: OcpColors.forest,
              ),
              Expanded(
                child: Padding(
                  padding: const EdgeInsets.only(top: 8),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      const Text(
                        'Point de contrôle obligatoire (§8.2) :',
                        style: TextStyle(fontWeight: FontWeight.w800, fontSize: 13, color: OcpColors.ink),
                      ),
                      const SizedBox(height: 4),
                      const Text(
                        '« Les conditions de sécurité et les mesures de prévention ont été inspectées conjointement (CEEP + CEEE) et sont effectivement mises en place sur le chantier »',
                        style: TextStyle(fontSize: 12, color: OcpColors.slate, height: 1.3),
                      ),
                      if (!data.visiteEffectuee) ...[
                        const SizedBox(height: 6),
                        const Text(
                          '⚠️ La validation conjointe de la visite préalable est requise pour soumettre l\'AT.',
                          style: TextStyle(fontSize: 11, color: OcpColors.errorSoft, fontStyle: FontStyle.italic),
                        ),
                      ],
                    ],
                  ),
                ),
              ),
            ],
          ),
        ),
      ],
    );
  }

  // ------------------------------------------------------------------
  // Étapes référentiels (A, B, C, D)
  // ------------------------------------------------------------------

  Widget _stepReferentiel({
    required String title,
    required AsyncValue<List<ReferentielItem>> items,
    required Set<String> selected,
    required ValueChanged<String> onToggle,
    required String searchHint,
    required String emptyMessage,
    required bool readOnly,
  }) {
    return _formCard(
      title: title,
      children: [
        SizedBox(
          height: MediaQuery.of(context).size.height * 0.62,
          child: MultiSelectStep(
            items: items,
            selected: selected,
            onToggle: readOnly ? (_) {} : onToggle,
            searchHint: searchHint,
            emptyMessage: emptyMessage,
          ),
        ),
      ],
    );
  }

  // ------------------------------------------------------------------
  // Étape 9 : Permis complémentaires (Section E) & IA Gemini
  // ------------------------------------------------------------------

  Widget _stepPermis(bool readOnly) {
    final types = ref.watch(typesPermisProvider);
    final data = widget.state.data;
    AtFormNotifier notifier() => ref.read(atFormProvider(widget.atId).notifier);
    final permisDocsAsync = ref.watch(permisDocumentsProvider(widget.atId));

    final asyncItems = types.whenData((list) => list
        .map((t) => ReferentielItem(id: t.id, nom: t.nom ?? '', description: t.description))
        .toList(),);

    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        _formCard(
          title: 'E. Sélection des permis requis',
          children: [
            SizedBox(
              height: MediaQuery.of(context).size.height * 0.40,
              child: MultiSelectStep(
                items: asyncItems,
                selected: data.permisIds,
                onToggle: readOnly ? (_) {} : (id) async {
                  notifier().togglePermis(id);
                  try {
                    await ref.read(permisDocumentApiProvider).initialiser(widget.atId);
                    ref.invalidate(permisDocumentsProvider(widget.atId));
                  } catch (_) {}
                },
                searchHint: 'Rechercher un type de permis…',
                emptyMessage: 'Aucun type de permis au référentiel.',
              ),
            ),
          ],
        ),
        const SizedBox(height: 14),

        if (data.permisIds.isNotEmpty) ...[
          _formCard(
            title: 'Documents de permis requis (analyse IA Gemini)',
            children: [
              permisDocsAsync.when(
                loading: () => const LinearProgressIndicator(),
                error: (e, _) => Text(
                  'Documents indisponibles ($e)',
                  style: const TextStyle(fontSize: 11, color: OcpColors.slate),
                ),
                data: (docs) {
                  final typesList = types.valueOrNull ?? [];
                  return Column(
                    children: data.permisIds.map((permisId) {
                      final typeObj = typesList.where((t) => t.id == permisId).firstOrNull;
                      final typeCode = permisId;
                      final typeNom = typeObj?.nom ?? permisId;
                      final doc = docs.where((d) => d.typePermisAttendu == typeCode || (typeObj?.nom != null && d.typePermisAttendu == typeObj!.nom)).firstOrNull;

                      return PermisUploadTile(
                        atId: widget.atId,
                        typePermis: typeCode,
                        typeNom: typeNom,
                        document: doc,
                        readOnly: readOnly,
                        onUpdated: () => ref.invalidate(permisDocumentsProvider(widget.atId)),
                      );
                    }).toList(),
                  );
                },
              ),
            ],
          ),
        ],
      ],
    );
  }

  // ------------------------------------------------------------------
  // Étape 10 : Mesures exécutant (Section F)
  // ------------------------------------------------------------------

  Widget _stepMesuresExecutant(bool readOnly) {
    final data = widget.state.data;
    AtFormNotifier notifier() => ref.read(atFormProvider(widget.atId).notifier);

    return _formCard(
      title: "F. Mesures de l'exécutant",
      children: [
        const Text(
          "Précautions spécifiques et consignes particulières établies par l'équipe exécutante.",
          style: TextStyle(fontSize: 13, color: OcpColors.slate, height: 1.4),
        ),
        const SizedBox(height: 14),
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
    );
  }

  // ------------------------------------------------------------------
  // Étape 11 : Récapitulatif + Soumission
  // ------------------------------------------------------------------

  Widget _stepRecapitulatif(bool readOnly) {
    final d = widget.state.data;
    final hasSubmit = ref.watch(hasPermissionProvider)('SUBMIT_AT');
    final session = ref.watch(sessionProvider);
    final userProprietaireService = session?.utilisateur.service;

    final isSameService = userProprietaireService != null &&
        ((d.serviceIntervenantId != null && d.serviceIntervenantId == userProprietaireService.id) ||
            (d.servicesIntervenants.isNotEmpty &&
                userProprietaireService.nomService != null &&
                d.servicesIntervenants.toLowerCase().trim() ==
                    userProprietaireService.nomService!.toLowerCase().trim()));

    final canSubmit = hasSubmit &&
        !isSameService &&
        (widget.state.at.statut == StatutAt.brouillon ||
            widget.state.at.statut == StatutAt.demandeCreee ||
            widget.state.at.statut == StatutAt.classificationEffectuee);

    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        _formCard(
          title: 'Synthèse du dossier AT',
          children: [
            _recapRow('Objet', d.objet),
            _recapRow('Document source', '${d.typeDocumentSource} ${d.documentSourceNumero}'),
            _recapRow('Période', '${_fr(d.dateDebut)} → ${_fr(d.dateFin)} (${_h(d.heureDebut)} - ${_h(d.heureFin)})'),
            _recapRow('Zone propriétaire (P)', d.zoneProprietaireNom),
            _recapRow('Zone exécutante (E)', d.zoneExecutanteNom),
            _recapRow('Service demandeur (P)', userProprietaireService?.nomService),
            _recapRow('Service exécutant (E)', d.servicesIntervenants.isNotEmpty ? d.servicesIntervenants : null),
            _recapRow('Visite préalable (§8.2)', d.visiteEffectuee ? '✅ Effectuée & Validée' : '⚠️ Non cochée'),
            if (d.entreprisesIntervenantes.isNotEmpty)
              _recapRow('Entreprise extérieure', d.entreprisesIntervenantes),
            const Divider(height: 16),
            _recapRow('Risques identifiés (A)', '${d.risquesIds.length} sélectionné(s)'),
            _recapRow('Mesures de sécurité (B)', '${d.mesuresIds.length} active(s)'),
            _recapRow('Moyens d\'accès (C)', '${d.moyensAccesIds.length} requis'),
            _recapRow('EPI obligatoires (D)', '${d.episIds.length} coché(s)'),
            _recapRow('Permis complémentaires (E)', '${d.permisIds.length} rattaché(s)'),
            if (d.mesuresSecuriteExecutant.isNotEmpty)
              _recapRow('Mesures exécutant (F)', d.mesuresSecuriteExecutant),
          ],
        ),
        if (isSameService) ...[
          const SizedBox(height: 12),
          Container(
            padding: const EdgeInsets.all(12),
            decoration: BoxDecoration(
              color: OcpColors.errorSoft.withValues(alpha: 0.15),
              borderRadius: BorderRadius.circular(10),
              border: Border.all(color: OcpColors.errorSoft),
            ),
            child: const Row(
              children: [
                Icon(Icons.error_outline_rounded, color: OcpColors.errorSoft, size: 20),
                SizedBox(width: 8),
                Expanded(
                  child: Text(
                    'Soumission impossible : le service propriétaire et le service intervenant doivent obligatoirement être différents (étape 3).',
                    style: TextStyle(fontSize: 12, color: OcpColors.errorSoft, fontWeight: FontWeight.bold),
                  ),
                ),
              ],
            ),
          ),
        ],
        const SizedBox(height: 16),
        if (!readOnly && canSubmit)
          FilledButton.icon(
            style: FilledButton.styleFrom(
              backgroundColor: OcpColors.forest,
              minimumSize: const Size.fromHeight(50),
              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
            ),
            onPressed: _submitting ? null : _submit,
            icon: _submitting
                ? const SizedBox(width: 18, height: 18, child: CircularProgressIndicator(strokeWidth: 2, color: OcpColors.white))
                : const Icon(Icons.send_rounded),
            label: Text(
              _submitting ? 'Transmission en cours…' : "Signer l'AT & Transmettre au CEEE",
              style: const TextStyle(fontWeight: FontWeight.w800, fontSize: 14),
            ),
          ),
      ],
    );
  }

  Future<void> _submit() async {
    final session = ref.read(sessionProvider);
    final userProprietaireService = session?.utilisateur.service;
    final d = widget.state.data;

    final isSameService = userProprietaireService != null &&
        ((d.serviceIntervenantId != null && d.serviceIntervenantId == userProprietaireService.id) ||
            (d.servicesIntervenants.isNotEmpty &&
                userProprietaireService.nomService != null &&
                d.servicesIntervenants.toLowerCase().trim() ==
                    userProprietaireService.nomService!.toLowerCase().trim()));

    if (isSameService) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          backgroundColor: OcpColors.errorSoft,
          content: Text(
            'Le service propriétaire (${userProprietaireService.nomService ?? ''}) doit être différent du service intervenant.',
          ),
        ),
      );
      return;
    }

    // §8.2 OCP S-HSE-SEC-31 - Visite préalable conjointe obligatoire avant
    // transmission (identique au web : handleSignerEtTransmettre).
    if (!d.visiteEffectuee) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          backgroundColor: OcpColors.warning,
          content: Text(
            'Visite préalable obligatoire (§8.2) : effectuez la visite conjointe de chantier, '
            'cochez la confirmation des mesures de prévention, puis transmettez l\'AT.',
          ),
        ),
      );
      return;
    }

    // Contrôle IA de complétude (non bloquant) - identique au web
    // (controlerAvantSoumission : si incomplet, confirmation « Soumettre quand même »).
    try {
      final ia = await AssistantApi(ref.read(apiClientProvider)).controlerDossier(
        description: d.descriptionTravaux,
        visiteFaite: d.visiteEffectuee,
        nbRisques: d.risquesIds.length,
        nbMesures: d.mesuresIds.length,
        nbEpis: d.episIds.length,
        nbPermis: d.permisIds.length,
        sectionFRenseignee: d.mesuresSecuriteExecutant.trim().isNotEmpty,
      );
      final alertes =
          (ia['alertes'] as List<dynamic>? ?? []).map((e) => e.toString()).toList();
      final complet = ia['complet'] != false;
      if ((!complet || alertes.isNotEmpty) && mounted) {
        final ok = await showDialog<bool>(
          context: context,
          builder: (ctx) => AlertDialog(
            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
            title: const Row(
              children: [
                Icon(Icons.psychology_alt_rounded, color: OcpColors.moss, size: 22),
                SizedBox(width: 8),
                Expanded(
                  child: Text('Contrôle IA - Alertes de complétude',
                      style: TextStyle(fontWeight: FontWeight.bold, fontSize: 15)),
                ),
              ],
            ),
            content: Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text(
                  "L'analyse IA a identifié les points suivants :",
                  style: TextStyle(fontSize: 12, color: OcpColors.slate),
                ),
                const SizedBox(height: 8),
                ...alertes.map((a) => Padding(
                      padding: const EdgeInsets.only(bottom: 4),
                      child: Row(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          const Text('• ', style: TextStyle(fontSize: 12, color: OcpColors.ink)),
                          Expanded(
                            child: Text(a,
                                style: const TextStyle(fontSize: 12, color: OcpColors.ink, height: 1.3)),
                          ),
                        ],
                      ),
                    )),
              ],
            ),
            actions: [
              TextButton(
                onPressed: () => Navigator.pop(ctx, false),
                child: const Text('Compléter le formulaire'),
              ),
              FilledButton(
                style: FilledButton.styleFrom(backgroundColor: OcpColors.forest),
                onPressed: () => Navigator.pop(ctx, true),
                child: const Text('Soumettre quand même'),
              ),
            ],
          ),
        );
        if (ok != true) return;
      }
    } catch (_) {
      /* IA indisponible : on ne bloque jamais le workflow métier */
    }

    setState(() => _submitting = true);
    final notifier = ref.read(atFormProvider(widget.atId).notifier);
    try {
      await notifier.flushSave();
      if (!mounted) return;
      // Étape 1 du circuit des visas : le CEEP signe l'AT (Visa CEEP, ordre 1)
      // AVANT la transmission au CEEE - 1.CEEP → 2.CEEE → 3.HCEP → 4.HCEE → 5.HMEP → 6.HMEE.
      final signe = await signerVisaCeepEtSoumettre(context, ref, widget.atId);
      if (!signe) return; // signature annulée → pas de soumission
      ref.invalidate(atDetailProvider(widget.atId));
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            backgroundColor: OcpColors.success,
            content: Text('Visa CEEP apposé - AT soumise et transmise au CEEE.'),
          ),
        );
        context.go('/at/${widget.atId}');
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            backgroundColor: OcpColors.errorSoft,
            content: Text(e is Failure ? e.message : 'Échec de la soumission - vérifiez les données.'),
          ),
        );
      }
    } finally {
      if (mounted) setState(() => _submitting = false);
    }
  }

  static String _fr(DateTime? d) => d == null ? '-' :
      '${d.day.toString().padLeft(2, '0')}/${d.month.toString().padLeft(2, '0')}/${d.year}';
  static String _h(TimeOfDay? t) => t == null ? '-'
      : '${t.hour.toString().padLeft(2, '0')}:${t.minute.toString().padLeft(2, '0')}';

  Widget _formCard({required String title, required List<Widget> children}) {
    return Card(
      elevation: 0,
      color: OcpColors.white,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(12),
        side: const BorderSide(color: OcpColors.borderSoft),
      ),
      child: Padding(
        padding: const EdgeInsets.all(20),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Text(
              title,
              style: const TextStyle(
                fontFamily: 'SpaceGrotesk',
                fontWeight: FontWeight.w700,
                fontSize: 15,
                letterSpacing: 0.2,
                color: OcpColors.forest,
              ),
            ),
            const SizedBox(height: 6),
            const Divider(height: 16, color: OcpColors.borderSoft),
            ...children,
          ],
        ),
      ),
    );
  }

  Widget _recapRow(String label, String? value) => Padding(
        padding: const EdgeInsets.symmetric(vertical: 7),
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            SizedBox(
              width: 158,
              child: Text(label, style: const TextStyle(fontSize: 13, color: OcpColors.slate, height: 1.35)),
            ),
            Expanded(
              child: Text(
                value ?? '-',
                style: const TextStyle(fontSize: 13, fontWeight: FontWeight.w600, color: OcpColors.ink, height: 1.35),
              ),
            ),
          ],
        ),
      );
}

// ----------------------------------------------------------------------
// Sous-widgets réutilisables
// ----------------------------------------------------------------------

class _ReadOnlyBanner extends StatelessWidget {
  final AtFormState state;
  const _ReadOnlyBanner({required this.state});

  @override
  Widget build(BuildContext context) {
    final verrouAutre = state.verrouilleParAutre;
    return Container(
      width: double.infinity,
      color: OcpColors.warningSoft,
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
      child: Row(
        children: [
          const Icon(Icons.lock_outline_rounded, size: 18, color: OcpColors.warning),
          const SizedBox(width: 10),
          Expanded(
            child: Text(
              verrouAutre
                  ? 'Formulaire verrouillé : ce dossier est en cours d\'édition par ${state.lockHolderName}.'
                  : 'AT signée et transmise : conformément au §8, le formulaire est verrouillé en lecture seule.',
              style: const TextStyle(fontSize: 12, color: OcpColors.ink, height: 1.4),
            ),
          ),
        ],
      ),
    );
  }
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
  Widget build(BuildContext context) => Material(
        color: OcpColors.surfaceSoft,
        borderRadius: BorderRadius.circular(10),
        child: ListTile(
          leading: Icon(icon, color: OcpColors.forest, size: 22),
          title: Text(label, style: const TextStyle(fontSize: 12, color: OcpColors.slate)),
          subtitle: Text(
            value == null
                ? 'Non définie'
                : '${value!.day.toString().padLeft(2, '0')}/${value!.month.toString().padLeft(2, '0')}/${value!.year}',
            style: const TextStyle(fontWeight: FontWeight.w700, fontSize: 14, color: OcpColors.ink),
          ),
          trailing: enabled ? const Icon(Icons.edit_calendar_rounded, size: 20, color: OcpColors.forest) : null,
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
    required this.initial,
    required this.onPick,
    required this.enabled,
  });

  @override
  Widget build(BuildContext context) => Material(
        color: OcpColors.surfaceSoft,
        borderRadius: BorderRadius.circular(10),
        child: ListTile(
          leading: const Icon(Icons.access_time_rounded, color: OcpColors.forest, size: 22),
          title: Text(label, style: const TextStyle(fontSize: 12, color: OcpColors.slate)),
          subtitle: Text(
            value,
            style: const TextStyle(fontWeight: FontWeight.w700, fontSize: 14, color: OcpColors.ink),
          ),
          trailing: enabled ? const Icon(Icons.edit_outlined, size: 18, color: OcpColors.forest) : null,
          onTap: enabled
              ? () async {
                  final picked = await showTimePicker(
                    context: context,
                    initialTime: initial ?? TimeOfDay.now(),
                  );
                  if (picked != null) onPick(picked);
                }
              : null,
        ),
      );
}

class _ZoneDropdown extends StatelessWidget {
  final String label;
  final List<dynamic> zones;
  final String? valueId;
  final String? fallbackName;
  final bool enabled;
  final ValueChanged<dynamic> onChanged;

  const _ZoneDropdown({
    required this.label,
    required this.zones,
    required this.valueId,
    required this.fallbackName,
    required this.enabled,
    required this.onChanged,
  });

  @override
  Widget build(BuildContext context) {
    final hasMatch = zones.any((z) => z.id == valueId || z.nomZone == fallbackName);
    final selectedValue = hasMatch
        ? zones.firstWhere((z) => z.id == valueId || z.nomZone == fallbackName).id
        : null;

    return DropdownButtonFormField<String>(
      initialValue: selectedValue,
      isExpanded: true,
      decoration: InputDecoration(
        labelText: label,
        prefixIcon: const Icon(Icons.place_outlined, size: 20),
      ),
      items: [
        const DropdownMenuItem(value: null, child: Text('-- Sélectionner une zone --')),
        ...zones.map((z) => DropdownMenuItem(
              value: z.id as String,
              child: Text(z.nomZone as String),
            )),
      ],
      onChanged: enabled
          ? (id) {
              if (id == null) return;
              final found = zones.firstWhere((z) => z.id == id);
              onChanged(found);
            }
          : null,
    );
  }
}

class _ServiceDropdown extends StatelessWidget {
  final String label;
  final List<dynamic> services;
  final String? valueId;
  final String? valueNom;
  final String? forbiddenServiceId;
  final bool enabled;
  final ValueChanged<dynamic> onChanged;

  const _ServiceDropdown({
    required this.label,
    required this.services,
    required this.valueId,
    required this.valueNom,
    required this.forbiddenServiceId,
    required this.enabled,
    required this.onChanged,
  });

  @override
  Widget build(BuildContext context) {
    final hasMatch = services.any((s) => s.id == valueId || s.nomService == valueNom);
    final selectedValue = hasMatch
        ? services.firstWhere((s) => s.id == valueId || s.nomService == valueNom).id
        : null;

    return DropdownButtonFormField<String>(
      initialValue: selectedValue,
      isExpanded: true,
      decoration: InputDecoration(
        labelText: label,
        prefixIcon: const Icon(Icons.handyman_outlined, size: 20),
      ),
      items: [
        const DropdownMenuItem(value: null, child: Text('-- Sélectionner le service exécutant --')),
        ...services.map((s) {
          final isForbidden = s.id == forbiddenServiceId;
          return DropdownMenuItem(
            value: s.id as String,
            child: Text(
              isForbidden ? '${s.nomService} (Propriétaire - Interdit)' : s.nomService as String,
              style: TextStyle(
                color: isForbidden ? OcpColors.slate : OcpColors.ink,
                fontStyle: isForbidden ? FontStyle.italic : FontStyle.normal,
              ),
            ),
          );
        }),
      ],
      onChanged: enabled
          ? (id) {
              if (id == null) return;
              final found = services.firstWhere((s) => s.id == id);
              onChanged(found);
            }
          : null,
    );
  }
}

class _EntrepriseDropdown extends StatelessWidget {
  final String label;
  final List<dynamic> entreprises;
  final String? value;
  final bool enabled;
  final ValueChanged<String?> onChanged;

  const _EntrepriseDropdown({
    required this.label,
    required this.entreprises,
    required this.value,
    required this.enabled,
    required this.onChanged,
  });

  @override
  Widget build(BuildContext context) {
    final hasMatch = entreprises.any((e) => e.nomEntreprise == value);

    return DropdownButtonFormField<String>(
      initialValue: hasMatch ? value : '',
      isExpanded: true,
      decoration: InputDecoration(
        labelText: label,
        prefixIcon: const Icon(Icons.domain_outlined, size: 20),
      ),
      items: [
        const DropdownMenuItem(value: '', child: Text('Aucune (Régie interne OCP)')),
        ...entreprises.map((e) => DropdownMenuItem(
              value: e.nomEntreprise as String,
              child: Text(e.nomEntreprise as String),
            )),
      ],
      onChanged: enabled ? onChanged : null,
    );
  }
}
