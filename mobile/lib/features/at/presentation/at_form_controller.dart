/// Contrôleur du formulaire AT mobile - Phases 6/7/8 :
///
/// - Pré-remplissage depuis le détail AT réel.
/// - VERROU : /prendre-verrou à l'entrée ; si tenu par un autre → lecture seule
///   (jamais contourné). /liberer-verrou à la sortie si on le détient.
/// - AUTO-SAVE : PUT /{id}/autosave (payload = AutoSaveRequest backend exact).
///   Détection de diff (aucune requête inutile), anti-requêtes simultanées
///   (exécution séquentielle, dernier état gagne), debounce 1.2 s,
///   flush avant sortie. Les données saisies ne sont jamais perdues.
/// - Sélection des référentiels par IDs (risques, mesures, EPI, moyens, permis).
library;

import 'dart:async';
import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/errors/error_mapper.dart';
import '../../../core/errors/failures.dart';
import '../data/at_api.dart';
import '../data/models/autorisation_travail.dart';
import 'at_providers.dart';

// ---------------------------------------------------------------------
// Données du formulaire
// ---------------------------------------------------------------------

@immutable
class AtFormData {
  final String objet;
  final String descriptionTravaux;
  final DateTime? dateDebut;
  final DateTime? dateFin;
  final TimeOfDay? heureDebut;
  final TimeOfDay? heureFin;
  final String servicesIntervenants;
  final String? serviceIntervenantId;
  final String entreprisesIntervenantes;
  final String mesuresSecuriteExecutant;
  final String? zoneProprietaireId;
  final String? zoneProprietaireNom;
  final String? zoneExecutanteId;
  final String? zoneExecutanteNom;
  final Set<String> risquesIds;
  final Set<String> mesuresIds;
  final Set<String> episIds;
  final Set<String> moyensAccesIds;
  final Set<String> permisIds;
  final bool visiteEffectuee;
  final String visiteCommentaire;

  final double? latitude;
  final double? longitude;
  final String? photoPath;
  final String typeDocumentSource;
  final String documentSourceNumero;
  final String? documentSourceId;

  const AtFormData({
    this.objet = '',
    this.descriptionTravaux = '',
    this.dateDebut,
    this.dateFin,
    this.heureDebut,
    this.heureFin,
    this.servicesIntervenants = '',
    this.serviceIntervenantId,
    this.entreprisesIntervenantes = '',
    this.mesuresSecuriteExecutant = '',
    this.zoneProprietaireId,
    this.zoneProprietaireNom,
    this.zoneExecutanteId,
    this.zoneExecutanteNom,
    this.risquesIds = const {},
    this.mesuresIds = const {},
    this.episIds = const {},
    this.moyensAccesIds = const {},
    this.permisIds = const {},
    this.visiteEffectuee = false,
    this.visiteCommentaire = '',
    this.latitude,
    this.longitude,
    this.photoPath,
    this.typeDocumentSource = 'DI',
    this.documentSourceNumero = '',
    this.documentSourceId,
  });

  AtFormData copyWith({
    String? objet,
    String? descriptionTravaux,
    DateTime? dateDebut,
    DateTime? dateFin,
    TimeOfDay? heureDebut,
    TimeOfDay? heureFin,
    String? servicesIntervenants,
    String? serviceIntervenantId,
    String? entreprisesIntervenantes,
    String? mesuresSecuriteExecutant,
    String? zoneProprietaireId,
    String? zoneProprietaireNom,
    String? zoneExecutanteId,
    String? zoneExecutanteNom,
    Set<String>? risquesIds,
    Set<String>? mesuresIds,
    Set<String>? episIds,
    Set<String>? moyensAccesIds,
    Set<String>? permisIds,
    bool? visiteEffectuee,
    String? visiteCommentaire,
    double? latitude,
    double? longitude,
    String? photoPath,
    String? typeDocumentSource,
    String? documentSourceNumero,
    String? documentSourceId,
    bool clearDateDebut = false,
    bool clearDateFin = false,
    bool clearDocumentSource = false,
  }) =>
      AtFormData(
        objet: objet ?? this.objet,
        descriptionTravaux: descriptionTravaux ?? this.descriptionTravaux,
        dateDebut: clearDateDebut ? null : (dateDebut ?? this.dateDebut),
        dateFin: clearDateFin ? null : (dateFin ?? this.dateFin),
        heureDebut: heureDebut ?? this.heureDebut,
        heureFin: heureFin ?? this.heureFin,
        servicesIntervenants: servicesIntervenants ?? this.servicesIntervenants,
        serviceIntervenantId: serviceIntervenantId ?? this.serviceIntervenantId,
        entreprisesIntervenantes: entreprisesIntervenantes ?? this.entreprisesIntervenantes,
        mesuresSecuriteExecutant: mesuresSecuriteExecutant ?? this.mesuresSecuriteExecutant,
        zoneProprietaireId: zoneProprietaireId ?? this.zoneProprietaireId,
        zoneProprietaireNom: zoneProprietaireNom ?? this.zoneProprietaireNom,
        zoneExecutanteId: zoneExecutanteId ?? this.zoneExecutanteId,
        zoneExecutanteNom: zoneExecutanteNom ?? this.zoneExecutanteNom,
        risquesIds: risquesIds ?? this.risquesIds,
        mesuresIds: mesuresIds ?? this.mesuresIds,
        episIds: episIds ?? this.episIds,
        moyensAccesIds: moyensAccesIds ?? this.moyensAccesIds,
        permisIds: permisIds ?? this.permisIds,
        visiteEffectuee: visiteEffectuee ?? this.visiteEffectuee,
        visiteCommentaire: visiteCommentaire ?? this.visiteCommentaire,
        latitude: latitude ?? this.latitude,
        longitude: longitude ?? this.longitude,
        photoPath: photoPath ?? this.photoPath,
        typeDocumentSource: typeDocumentSource ?? this.typeDocumentSource,
        documentSourceNumero: documentSourceNumero ?? this.documentSourceNumero,
        documentSourceId:
            clearDocumentSource ? null : (documentSourceId ?? this.documentSourceId),
      );

  /// Payload AutoSaveRequest exact du backend.
  Map<String, dynamic> toAutoSaveJson() => {
        if (objet.isNotEmpty) 'objet': objet,
        'descriptionTravaux': descriptionTravaux,
        if (dateDebut != null) 'dateDebut': _isoDate(dateDebut!),
        if (dateFin != null) 'dateFin': _isoDate(dateFin!),
        if (heureDebut != null) 'heureDebut': _isoTime(heureDebut!),
        if (heureFin != null) 'heureFin': _isoTime(heureFin!),
        'servicesIntervenants': servicesIntervenants,
        if (serviceIntervenantId != null) 'serviceIntervenantId': serviceIntervenantId,
        'entreprisesIntervenantes': entreprisesIntervenantes,
        'mesuresSecuriteExecutant': mesuresSecuriteExecutant,
        'zoneProprietaireId': zoneProprietaireId,
        'zoneProprietaireNom': zoneProprietaireNom,
        'zoneExecutanteId': zoneExecutanteId,
        'zoneExecutanteNom': zoneExecutanteNom,
        'risquesIds': risquesIds.toList(),
        'mesuresIds': mesuresIds.toList(),
        'episIds': episIds.toList(),
        'moyensAccesIds': moyensAccesIds.toList(),
        'permisIds': permisIds.toList(),
        'visiteEffectuee': visiteEffectuee,
        'visiteCommentaire': visiteCommentaire,
        if (latitude != null) 'latitude': latitude,
        if (longitude != null) 'longitude': longitude,
        if (photoPath != null) 'photoPath': photoPath,
        'typeDocumentSource': typeDocumentSource,
        if (documentSourceNumero.isNotEmpty) 'documentSourceNumero': documentSourceNumero,
        if (documentSourceId != null && documentSourceId!.isNotEmpty)
          'documentSourceId': documentSourceId,
      };

  static String _isoDate(DateTime d) =>
      '${d.year.toString().padLeft(4, '0')}-${d.month.toString().padLeft(2, '0')}-${d.day.toString().padLeft(2, '0')}';

  static String _isoTime(TimeOfDay t) =>
      '${t.hour.toString().padLeft(2, '0')}:${t.minute.toString().padLeft(2, '0')}:00';

  static TimeOfDay? parseTime(String? s) {
    if (s == null || !s.contains(':')) return null;
    final p = s.split(':');
    return TimeOfDay(hour: int.tryParse(p[0]) ?? 0, minute: int.tryParse(p[1]) ?? 0);
  }

  factory AtFormData.fromAt(AutorisationTravail at) => AtFormData(
        objet: at.objet == 'Nouvelle AT' ? '' : (at.objet ?? ''),
        descriptionTravaux: at.descriptionTravaux ?? '',
        dateDebut: at.dateDebut,
        dateFin: at.dateFin,
        heureDebut: parseTime(at.heureDebut),
        heureFin: parseTime(at.heureFin),
        servicesIntervenants: at.servicesIntervenants ?? '',
        serviceIntervenantId: null,
        entreprisesIntervenantes: at.entreprisesIntervenantes ?? '',
        mesuresSecuriteExecutant: at.mesuresSecuriteExecutant ?? '',
        zoneProprietaireId: at.zoneProprietaireId,
        zoneProprietaireNom: at.zoneProprietaireNom,
        zoneExecutanteId: at.zoneExecutanteId,
        zoneExecutanteNom: at.zoneExecutanteNom,
        risquesIds: at.risquesIds.toSet(),
        mesuresIds: at.mesuresIds.toSet(),
        episIds: at.episIds.toSet(),
        moyensAccesIds: at.moyensAccesIds.toSet(),
        permisIds: at.permisIds.toSet(),
        visiteEffectuee: at.visiteEffectuee ?? false,
        visiteCommentaire: at.visiteCommentaire ?? '',
        latitude: at.latitude,
        longitude: at.longitude,
        photoPath: at.photoPath,
        typeDocumentSource: at.typeDocumentSource ?? 'DI',
        documentSourceNumero: at.documentSourceNumero ?? '',
        documentSourceId: at.documentSourceId,
      );
}

// ---------------------------------------------------------------------
// État UI du formulaire
// ---------------------------------------------------------------------

enum SaveStatus { idle, saving, saved, error }

class AtFormState {
  final AutorisationTravail at;
  final AtFormData data;
  final SaveStatus saveStatus;
  final DateTime? lastSavedAt;
  final Failure? saveFailure;
  final bool readOnly;
  final String? lockHolderName;
  final Failure? loadError;

  const AtFormState({
    required this.at,
    required this.data,
    this.saveStatus = SaveStatus.idle,
    this.lastSavedAt,
    this.saveFailure,
    this.readOnly = false,
    this.lockHolderName,
    this.loadError,
  });

  bool get verrouilleParAutre => readOnly && lockHolderName != null;

  AtFormState copyWith({
    AutorisationTravail? at,
    AtFormData? data,
    SaveStatus? saveStatus,
    DateTime? lastSavedAt,
    Failure? saveFailure,
    bool clearSaveFailure = false,
    bool? readOnly,
    String? lockHolderName,
    Failure? loadError,
  }) =>
      AtFormState(
        at: at ?? this.at,
        data: data ?? this.data,
        saveStatus: saveStatus ?? this.saveStatus,
        lastSavedAt: lastSavedAt ?? this.lastSavedAt,
        saveFailure: clearSaveFailure ? null : (saveFailure ?? this.saveFailure),
        readOnly: readOnly ?? this.readOnly,
        lockHolderName: lockHolderName ?? this.lockHolderName,
        loadError: loadError ?? this.loadError,
      );
}

// ---------------------------------------------------------------------
// Notifier : chargement + verrou + auto-save
// ---------------------------------------------------------------------

final atFormProvider =
    StateNotifierProvider.autoDispose.family<AtFormNotifier, AtFormState, String>((ref, id) {
  return AtFormNotifier(ref.watch(atApiProvider), id);
});

class AtFormNotifier extends StateNotifier<AtFormState> {
  final AtApi _api;
  final String atId;

  Timer? _debounceTimer;
  String _lastSavedJson = '';
  bool _saving = false;
  bool _dirtyAfterCurrentSave = false;
  bool _lockHeldByUs = false;

  static const Duration debounceDelay = Duration(milliseconds: 1200);

  AtFormNotifier(this._api, this.atId)
      : super(AtFormState(
          at: AutorisationTravail(id: atId),
          data: const AtFormData(),
        ),) {
    _init();
  }

  Future<void> _init() async {
    try {
      final at = await _api.findById(atId);
      state = state.copyWith(
        at: at,
        data: AtFormData.fromAt(at),
        // Lecture seule dès la création serveur si statut non éditable :
        readOnly: _statutNonEditable(at.statut),
      );
      _lastSavedJson = jsonEncode(state.data.toAutoSaveJson());

      if (!state.readOnly) {
        // Verrou d'édition exclusif - la décision finale appartient au serveur :
        // si tenu par un autre utilisateur, l'API échoue → lecture seule.
        try {
          await _api.prendreVerrou(atId);
          _lockHeldByUs = true;
        } catch (e) {
          final failure = mapDioError(e);
          state = state.copyWith(
            readOnly: true,
            lockHolderName:
                failure is ApiFailure ? failure.message : 'un autre utilisateur',
          );
        }
      }
    } catch (e) {
      state = state.copyWith(loadError: mapDioError(e));
    }
  }

  bool _statutNonEditable(String? statut) =>
      statut != StatutAt.brouillon &&
      statut != StatutAt.demandeCreee &&
      statut != StatutAt.classificationEffectuee;

  // --- Mise à jour des champs ---

  void update(AtFormData Function(AtFormData) mutator) {
    if (state.readOnly || state.loadError != null) return;
    final newData = mutator(state.data);
    state = state.copyWith(data: newData, clearSaveFailure: true);
    _scheduleSave();
  }

  /// Toggles propres à chaque liste de référentiels.
  void toggleRisque(String id) =>
      update((d) => d.copyWith(risquesIds: _toggle(d.risquesIds, id)));
  void toggleMesure(String id) =>
      update((d) => d.copyWith(mesuresIds: _toggle(d.mesuresIds, id)));
  void toggleEpi(String id) => update((d) => d.copyWith(episIds: _toggle(d.episIds, id)));
  void toggleMoyenAcces(String id) =>
      update((d) => d.copyWith(moyensAccesIds: _toggle(d.moyensAccesIds, id)));
  void togglePermis(String id) =>
      update((d) => d.copyWith(permisIds: _toggle(d.permisIds, id)));

  Set<String> _toggle(Set<String> ids, String id) {
    final copy = ids.toSet();
    copy.contains(id) ? copy.remove(id) : copy.add(id);
    return copy;
  }

  // --- Moteur auto-save ---

  void _scheduleSave() {
    _debounceTimer?.cancel();
    _debounceTimer = Timer(debounceDelay, () => unawaited(_saveNow()));
  }

  Future<void> _saveNow() async {
    if (state.readOnly || state.loadError != null) return;

    final payload = jsonEncode(state.data.toAutoSaveJson());
    if (payload == _lastSavedJson) return; // aucune modification → aucune requête

    if (_saving) {
      _dirtyAfterCurrentSave = true; // dernier état gagné après la sauvegarde en cours
      return;
    }
    _saving = true;
    state = state.copyWith(saveStatus: SaveStatus.saving);

    var donePayload = payload;
    try {
      int maxLoops = 10;
      while (maxLoops-- > 0) {
        await _api.autoSave(atId, jsonDecode(donePayload) as Map<String, dynamic>);
        _lastSavedJson = donePayload;
        if (!_dirtyAfterCurrentSave) break;
        _dirtyAfterCurrentSave = false;
        donePayload = jsonEncode(state.data.toAutoSaveJson());
        if (donePayload == _lastSavedJson) break;
      }
      if (mounted) {
        state = state.copyWith(
          saveStatus: SaveStatus.saved,
          lastSavedAt: DateTime.now(),
        );
      }
    } catch (e) {
      // Les données restent dans state.data : jamais de perte de saisie.
      if (mounted) {
        state = state.copyWith(
          saveStatus: SaveStatus.error,
          saveFailure: e is Failure ? e : mapDioError(e),
        );
      }
    } finally {
      _saving = false;
    }
  }

  /// Sauvegarde immédiate (sortie d'écran, bouton "Enregistrer maintenant").
  Future<void> flushSave() async {
    _debounceTimer?.cancel();
    await _saveNow();
  }

  Future<void> retrySave() => flushSave();

  /// Libère le verrou et sauvegarde avant de quitter (fire-and-forget autorisé).
  Future<void> releaseLockAndFlush() async {
    await flushSave();
    if (_lockHeldByUs) {
      try {
        await _api.libererVerrou(atId);
        _lockHeldByUs = false;
      } catch (_) {/* déjà libéré ou réseau coupé : non bloquant */}
    }
  }

  @override
  void dispose() {
    _debounceTimer?.cancel();
    if (_lockHeldByUs) {
      // Au minimum : ne jamais laisser le verrou orphelin volontairement.
      _api.libererVerrou(atId).ignore();
    }
    super.dispose();
  }
}
