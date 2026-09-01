/// Page de réception conjointe des travaux & clôture de l'AT — Mobile.
/// Réplique exacte du comportement et des règles de ReceptionTravauxPage.tsx.
library;

import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:signature/signature.dart';

import '../../../core/errors/error_mapper.dart';
import '../../../core/errors/failures.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/widgets/states.dart';
import '../data/models/autorisation_travail.dart';
import '../data/models/reception_travaux.dart';
import 'at_providers.dart';

class AtReceptionPage extends ConsumerStatefulWidget {
  final String atId;
  const AtReceptionPage({super.key, required this.atId});

  @override
  ConsumerState<AtReceptionPage> createState() => _AtReceptionPageState();
}

class _AtReceptionPageState extends ConsumerState<AtReceptionPage> {
  DateTime _dateDebut = DateTime.now();
  DateTime _dateFin = DateTime.now();
  final _travauxController = TextEditingController();
  final _commentaireController = TextEditingController();

  bool _travauxConformes = true;
  bool _zoneNettoyee = true;
  bool _consignationRetiree = true;
  bool _equipementRemisEnService = true;
  bool _installationRemiseEnEtat = true;
  bool _essaisEffectues = true;
  bool _essaisConformes = true;

  late final SignatureController _sigController;
  bool _hasSignature = false;
  bool _submitting = false;
  bool _initialized = false;
  ReceptionTravaux? _existingReception;

  @override
  void initState() {
    super.initState();
    _sigController = SignatureController(
      penStrokeWidth: 3,
      penColor: const Color(0xFF16241E),
      exportBackgroundColor: Colors.white,
    );
    _sigController.addListener(() {
      final has = _sigController.isNotEmpty;
      if (has != _hasSignature) setState(() => _hasSignature = has);
    });
  }

  @override
  void dispose() {
    _travauxController.dispose();
    _commentaireController.dispose();
    _sigController.dispose();
    super.dispose();
  }

  void _initFromData(AutorisationTravail at, ReceptionTravaux? rec) {
    if (_initialized) return;
    _initialized = true;
    if (rec != null) {
      _existingReception = rec;
      _dateDebut = rec.dateDebutTravauxReelle ?? at.dateDebut ?? DateTime.now();
      _dateFin = rec.dateFinTravauxReelle ?? at.dateFin ?? DateTime.now();
      _travauxConformes = rec.travauxConformes;
      _zoneNettoyee = rec.zoneNettoyee;
      _consignationRetiree = rec.consignationRetiree;
      _equipementRemisEnService = rec.equipementRemisEnService;
      _installationRemiseEnEtat = rec.installationRemiseEnEtat;
      _essaisEffectues = rec.essaisEffectues;
      _essaisConformes = rec.essaisConformes;
      _travauxController.text = rec.travauxRealises ?? '';
      _commentaireController.text = rec.commentaireResponsable ?? '';
      if (rec.signatureResponsable != null && rec.signatureResponsable!.isNotEmpty) {
        _hasSignature = true;
      }
    } else {
      _dateDebut = at.dateDebut ?? DateTime.now();
      _dateFin = at.dateFin ?? DateTime.now();
    }
  }

  bool get _isChecklistComplete =>
      _travauxConformes &&
      _zoneNettoyee &&
      _consignationRetiree &&
      _equipementRemisEnService &&
      _installationRemiseEnEtat &&
      _essaisEffectues &&
      _essaisConformes;

  Future<void> _submitReception(AutorisationTravail at) async {
    if (!_isChecklistComplete) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Toutes les conditions obligatoires de la checklist doivent être validées.'),
          backgroundColor: OcpColors.error,
        ),
      );
      return;
    }

    if (!_hasSignature && _existingReception?.signatureResponsable == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('La signature manuscrite du responsable est obligatoire.'),
          backgroundColor: OcpColors.error,
        ),
      );
      return;
    }

    setState(() => _submitting = true);
    final receptionApi = ref.read(receptionApiProvider);

    try {
      final req = ReceptionTravaux(
        id: _existingReception?.id ?? '',
        autorisationTravailId: widget.atId,
        dateDebutTravauxReelle: _dateDebut,
        dateFinTravauxReelle: _dateFin,
        travauxConformes: _travauxConformes,
        zoneNettoyee: _zoneNettoyee,
        consignationRetiree: _consignationRetiree,
        equipementRemisEnService: _equipementRemisEnService,
        installationRemiseEnEtat: _installationRemiseEnEtat,
        essaisEffectues: _essaisEffectues,
        essaisConformes: _essaisConformes,
        travauxRealises: _travauxController.text.trim(),
        commentaireResponsable: _commentaireController.text.trim(),
      );

      String recId;
      if (_existingReception == null || _existingReception!.id.isEmpty) {
        final created = await receptionApi.create(req);
        recId = created.id;
      } else {
        final updated = await receptionApi.update(_existingReception!.id, req);
        recId = updated.id;
      }

      // Apposer la signature si nouvelle saisie
      if (_sigController.isNotEmpty) {
        final pngBytes = await _sigController.toPngBytes();
        if (pngBytes != null) {
          final base64Sig = 'data:image/png;base64,${base64Encode(pngBytes)}';
          await receptionApi.signer(recId, base64Sig);
        }
      }

      // Clôturer l'AT
      await receptionApi.cloturer(recId);

      ref.invalidate(atDetailProvider(widget.atId));
      ref.invalidate(atReceptionProvider(widget.atId));

      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('Réception enregistrée et AT clôturée avec succès !'),
            backgroundColor: OcpColors.forest,
          ),
        );
        context.pop();
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text(e is Failure ? e.message : mapDioError(e).message),
            backgroundColor: OcpColors.error,
          ),
        );
      }
    } finally {
      if (mounted) setState(() => _submitting = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final atAsync = ref.watch(atDetailProvider(widget.atId));
    final recAsync = ref.watch(atReceptionProvider(widget.atId));

    return Scaffold(
      appBar: AppBar(
        title: const Text('Réception des travaux'),
      ),
      body: atAsync.when(
        loading: () => const LoadingState(message: 'Chargement de l\'AT...'),
        error: (e, _) => ErrorState(
          message: e is Failure ? e.message : 'Erreur de chargement.',
          onRetry: () => ref.invalidate(atDetailProvider(widget.atId)),
        ),
        data: (at) {
          final rec = recAsync.valueOrNull;
          _initFromData(at, rec);

          final alreadyClosed = rec?.cloturee == true ||
              at.statut == StatutAt.receptionnees ||
              at.statut == StatutAt.travauxReceptiones ||
              at.statut == StatutAt.archivee;

          return ListView(
            padding: const EdgeInsets.fromLTRB(16, 12, 16, 32),
            children: [
              // En-tête AT
              Card(
                child: Padding(
                  padding: const EdgeInsets.all(14),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(
                        children: [
                          Container(
                            padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                            decoration: BoxDecoration(
                              color: OcpColors.forestSoft,
                              borderRadius: BorderRadius.circular(6),
                            ),
                            child: Text(
                              at.numero ?? 'AT',
                              style: const TextStyle(
                                fontWeight: FontWeight.w700,
                                fontSize: 13,
                                color: OcpColors.forest,
                              ),
                            ),
                          ),
                          const SizedBox(width: 8),
                          Expanded(
                            child: Text(
                              at.objet ?? '—',
                              style: const TextStyle(fontWeight: FontWeight.w600, fontSize: 14),
                              overflow: TextOverflow.ellipsis,
                            ),
                          ),
                        ],
                      ),
                      const SizedBox(height: 6),
                      const Text(
                        'Étape 6 du Logigramme : Réception conjointe & essais de remise en service (CEEP + CEEE).',
                        style: TextStyle(fontSize: 12, color: OcpColors.slate),
                      ),
                    ],
                  ),
                ),
              ),
              const SizedBox(height: 14),

              // Dates réelles
              _sectionHeader('Période réelle des travaux'),
              Card(
                child: Padding(
                  padding: const EdgeInsets.all(12),
                  child: Column(
                    children: [
                      _dateRow('Date réelle début', _dateDebut, (d) => setState(() => _dateDebut = d), !alreadyClosed),
                      const Divider(height: 12),
                      _dateRow('Date réelle fin / réception', _dateFin, (d) => setState(() => _dateFin = d), !alreadyClosed),
                    ],
                  ),
                ),
              ),
              const SizedBox(height: 14),

              // Synthèse des travaux
              _sectionHeader('Synthèse des travaux réalisés'),
              Card(
                child: Padding(
                  padding: const EdgeInsets.all(12),
                  child: TextField(
                    controller: _travauxController,
                    enabled: !alreadyClosed,
                    maxLines: 3,
                    decoration: const InputDecoration(
                      hintText: 'Description synthétique des travaux exécutés...',
                      border: InputBorder.none,
                    ),
                  ),
                ),
              ),
              const SizedBox(height: 14),

              // Checklist obligatoire
              _sectionHeader('Checklist de réception (Obligatoire)'),
              Card(
                color: _isChecklistComplete ? OcpColors.mintSoft : OcpColors.surfaceSoft,
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(12),
                  side: BorderSide(
                    color: _isChecklistComplete ? OcpColors.mint : OcpColors.errorSoft,
                  ),
                ),
                child: Padding(
                  padding: const EdgeInsets.symmetric(vertical: 8, horizontal: 6),
                  child: Column(
                    children: [
                      _checkTile(
                        'Travaux conformes au cahier des charges',
                        _travauxConformes,
                        (v) => setState(() => _travauxConformes = v),
                        !alreadyClosed,
                      ),
                      _checkTile(
                        'Zone nettoyée et mise au propre',
                        _zoneNettoyee,
                        (v) => setState(() => _zoneNettoyee = v),
                        !alreadyClosed,
                      ),
                      _checkTile(
                        'Consignation des énergies retirée',
                        _consignationRetiree,
                        (v) => setState(() => _consignationRetiree = v),
                        !alreadyClosed,
                      ),
                      _checkTile(
                        'Équipement remis en service',
                        _equipementRemisEnService,
                        (v) => setState(() => _equipementRemisEnService = v),
                        !alreadyClosed,
                      ),
                      _checkTile(
                        'Installation remise en état',
                        _installationRemiseEnEtat,
                        (v) => setState(() => _installationRemiseEnEtat = v),
                        !alreadyClosed,
                      ),
                      _checkTile(
                        'Essais effectués et conformes',
                        _essaisEffectues && _essaisConformes,
                        (v) => setState(() {
                          _essaisEffectues = v;
                          _essaisConformes = v;
                        }),
                        !alreadyClosed,
                      ),
                    ],
                  ),
                ),
              ),
              if (!_isChecklistComplete)
                const Padding(
                  padding: EdgeInsets.only(top: 6, left: 4),
                  child: Text(
                    '⚠️ Toutes les conditions doivent être validées pour pouvoir réceptionner.',
                    style: TextStyle(fontSize: 12, color: OcpColors.error, fontWeight: FontWeight.w600),
                  ),
                ),
              const SizedBox(height: 14),

              // Commentaire responsable
              _sectionHeader('Commentaire / Remarques'),
              Card(
                child: Padding(
                  padding: const EdgeInsets.all(12),
                  child: TextField(
                    controller: _commentaireController,
                    enabled: !alreadyClosed,
                    maxLines: 2,
                    decoration: const InputDecoration(
                      hintText: 'Observations éventuelles lors de la réception...',
                      border: InputBorder.none,
                    ),
                  ),
                ),
              ),
              const SizedBox(height: 14),

              // Signature
              _sectionHeader('Visa et Signature Manuscrite du Responsable'),
              if (alreadyClosed && _existingReception?.signatureResponsable != null)
                Card(
                  child: Padding(
                    padding: const EdgeInsets.all(16),
                    child: Row(
                      children: [
                        const Icon(Icons.verified_rounded, color: OcpColors.forest, size: 28),
                        const SizedBox(width: 12),
                        const Expanded(
                          child: Text(
                            'Réception déjà signée et validée.',
                            style: TextStyle(fontWeight: FontWeight.w600, color: OcpColors.forest),
                          ),
                        ),
                      ],
                    ),
                  ),
                )
              else ...[
                Card(
                  child: Padding(
                    padding: const EdgeInsets.all(12),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.stretch,
                      children: [
                        Row(
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          children: [
                            const Text(
                              'Cadre de signature manuscrite :',
                              style: TextStyle(fontSize: 12, fontWeight: FontWeight.w600),
                            ),
                            if (_hasSignature)
                              TextButton.icon(
                                onPressed: () => _sigController.clear(),
                                icon: const Icon(Icons.clear_rounded, size: 16),
                                label: const Text('Effacer', style: TextStyle(fontSize: 12)),
                              ),
                          ],
                        ),
                        const SizedBox(height: 8),
                        Container(
                          height: 180,
                          decoration: BoxDecoration(
                            color: Colors.white,
                            borderRadius: BorderRadius.circular(8),
                            border: Border.all(
                              color: _hasSignature ? OcpColors.forest : OcpColors.border,
                              width: _hasSignature ? 2 : 1,
                            ),
                          ),
                          child: ClipRRect(
                            borderRadius: BorderRadius.circular(8),
                            child: Signature(
                              controller: _sigController,
                              backgroundColor: Colors.white,
                            ),
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
              ],
              const SizedBox(height: 20),

              // Bouton d'action
              if (!alreadyClosed)
                FilledButton.icon(
                  style: FilledButton.styleFrom(
                    backgroundColor: OcpColors.forest,
                    minimumSize: const Size.fromHeight(52),
                    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
                  ),
                  onPressed: (_submitting || !_isChecklistComplete) ? null : () => _submitReception(at),
                  icon: _submitting
                      ? const SizedBox(
                          width: 20,
                          height: 20,
                          child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white),
                        )
                      : const Icon(Icons.verified_rounded),
                  label: const Text(
                    'Réceptionner et Clôturer l\'AT',
                    style: TextStyle(fontSize: 15, fontWeight: FontWeight.w700),
                  ),
                ),
            ],
          );
        },
      ),
    );
  }

  Widget _sectionHeader(String title) => Padding(
        padding: const EdgeInsets.only(left: 4, bottom: 6),
        child: Text(
          title,
          style: const TextStyle(
            fontFamily: 'SpaceGrotesk',
            fontWeight: FontWeight.w700,
            fontSize: 14,
            color: OcpColors.forest,
          ),
        ),
      );

  Widget _dateRow(String label, DateTime value, ValueChanged<DateTime> onPick, bool enabled) => Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(label, style: const TextStyle(fontSize: 13, color: OcpColors.slate)),
          TextButton.icon(
            onPressed: enabled
                ? () async {
                    final d = await showDatePicker(
                      context: context,
                      initialDate: value,
                      firstDate: DateTime.now().subtract(const Duration(days: 365)),
                      lastDate: DateTime.now().add(const Duration(days: 365)),
                    );
                    if (d != null) onPick(d);
                  }
                : null,
            icon: const Icon(Icons.calendar_today_outlined, size: 16),
            label: Text(
              '${value.day.toString().padLeft(2, '0')}/${value.month.toString().padLeft(2, '0')}/${value.year}',
              style: const TextStyle(fontWeight: FontWeight.w700),
            ),
          ),
        ],
      );

  Widget _checkTile(String title, bool value, ValueChanged<bool> onChanged, bool enabled) =>
      CheckboxListTile(
        dense: true,
        contentPadding: EdgeInsets.zero,
        controlAffinity: ListTileControlAffinity.leading,
        activeColor: OcpColors.forest,
        title: Text(
          title,
          style: TextStyle(
            fontSize: 13,
            fontWeight: value ? FontWeight.w600 : FontWeight.w400,
            color: value ? OcpColors.ink : OcpColors.slate,
          ),
        ),
        value: value,
        onChanged: enabled ? (v) => onChanged(v ?? false) : null,
      );
}
