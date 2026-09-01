/// API des Autorisations de Travail — endpoints réels du backend
/// (AutorisationTravailController, préfixe /api) :
///
///   GET  /autorisations-travail?statut=&search=&page=&size=&sort=
///   GET  /autorisations-travail/{id}
///   POST /autorisations-travail                       (brouillon vierge)
///   POST /documents/{type}/{id}/creer-at              (depuis DI/OT/BT)
///   POST /documents/{type}/{id}/classifier?niveau=
///   PUT  /autorisations-travail/{id}/autosave
///   PUT  /autorisations-travail/{id}/prendre-verrou | liberer-verrou | transferer-verrou
///   POST /autorisations-travail/{id}/submit | validate | reject | renew | close |
///        demarrer-intervention | declarer-fin | visite | rediger | reconduire |
///        incident | reception-standard | accuser-reception-ceee
///   GET  /autorisations-travail/{id}/historique
///   GET  /autorisations-travail/{id}/visas
///   GET  /autorisations-travail/{id}/export-pdf       (byte[])
library;

import 'dart:typed_data';
import '../../../core/network/api_client.dart';
import 'models/autorisation_travail.dart';

class AtListQuery {
  final String? statut;
  final String? search;
  final bool? mine;
  final bool? aValider;
  final int page;
  final int size;
  final String? sort;

  const AtListQuery({
    this.statut,
    this.search,
    this.mine,
    this.aValider,
    this.page = 0,
    this.size = 20,
    this.sort,
  });

  Map<String, dynamic> toQuery() => {
        if (statut != null && statut!.isNotEmpty) 'statut': statut,
        if (search != null && search!.isNotEmpty) 'search': search,
        if (mine != null && mine == true) 'mine': true,
        if (aValider != null && aValider == true) 'aValider': true,
        'page': page,
        'size': size,
        if (sort != null && sort!.isNotEmpty) 'sort': sort,
      };
}

class AtApi {
  final ApiClient _client;
  AtApi(this._client);

  Future<Page<AutorisationTravail>> findAll(AtListQuery query) async {
    final response = await _client.get<Map<String, dynamic>>(
      '/autorisations-travail',
      queryParameters: query.toQuery(),
    );
    return Page.fromJson(response.data!, AutorisationTravail.fromJson);
  }

  Future<AutorisationTravail> findById(String id) async {
    final response = await _client.get<Map<String, dynamic>>('/autorisations-travail/$id');
    return AutorisationTravail.fromJson(response.data!);
  }

  Future<AutorisationTravail> createDirect() async {
    final response = await _client.post<Map<String, dynamic>>('/autorisations-travail');
    return AutorisationTravail.fromJson(response.data!);
  }

  Future<AutorisationTravail> createFromDocument(String type, String id) async {
    final response =
        await _client.post<Map<String, dynamic>>('/documents/$type/$id/creer-at');
    return AutorisationTravail.fromJson(response.data!);
  }

  Future<AutorisationTravail> classifier(String type, String id, {int niveau = 2}) async {
    final response = await _client.post<Map<String, dynamic>>(
      '/documents/$type/$id/classifier',
      queryParameters: {'niveau': niveau},
    );
    return AutorisationTravail.fromJson(response.data!);
  }

  /// Auto-save du brouillon — corps = AutoSaveRequest du backend.
  Future<AutorisationTravail> autoSave(String id, Map<String, dynamic> autoSaveRequest) async {
    final response =
        await _client.put<Map<String, dynamic>>('/autorisations-travail/$id/autosave', body: autoSaveRequest);
    return AutorisationTravail.fromJson(response.data!);
  }

  Future<void> prendreVerrou(String id) =>
      _client.put<void>('/autorisations-travail/$id/prendre-verrou');

  Future<void> libererVerrou(String id) =>
      _client.put<void>('/autorisations-travail/$id/liberer-verrou');

  Future<void> transfererVerrou(String id, String nouvelUtilisateurId) =>
      _client.put<void>('/autorisations-travail/$id/transferer-verrou', body: {
        'nouvelUtilisateurId': nouvelUtilisateurId,
      },);

  // --- Transitions workflow (retour = AT mise à jour) ---

  Future<AutorisationTravail> soumettre(String id) =>
      _transition(id, 'submit');

  Future<AutorisationTravail> valider(String id) => _transition(id, 'validate');

  Future<AutorisationTravail> refuser(String id, String motif) async {
    final response = await _client.post<Map<String, dynamic>>(
      '/autorisations-travail/$id/reject',
      body: {'motif': motif},
    );
    return AutorisationTravail.fromJson(response.data!);
  }

  Future<AutorisationTravail> renouveler(String id) => _transition(id, 'renew');

  Future<AutorisationTravail> cloturer(String id) => _transition(id, 'close');

  Future<AutorisationTravail> demarrerIntervention(String id) =>
      _transition(id, 'demarrer-intervention');

  Future<AutorisationTravail> declarerFin(String id) => _transition(id, 'declarer-fin');

  Future<AutorisationTravail> marquerVisite(String id) => _transition(id, 'visite');

  Future<AutorisationTravail> rediger(String id) => _transition(id, 'rediger');

  Future<AutorisationTravail> reconduire(String id, {bool depasse24h = false}) async {
    final response = await _client.post<Map<String, dynamic>>(
      '/autorisations-travail/$id/reconduire',
      queryParameters: {'depasse24h': depasse24h},
    );
    return AutorisationTravail.fromJson(response.data!);
  }

  Future<AutorisationTravail> signalerIncident(String id, String? motif) async {
    final response = await _client.post<Map<String, dynamic>>(
      '/autorisations-travail/$id/incident',
      queryParameters: motif == null ? null : {'motif': motif},
    );
    return AutorisationTravail.fromJson(response.data!);
  }

  Future<AutorisationTravail> receptionStandard(String id) =>
      _transition(id, 'reception-standard');

  Future<void> archiver(String id) async {
    await _client.post<dynamic>('/archives/archive/$id');
  }

  Future<AutorisationTravail> accuserReceptionCeee(String id) =>
      _putTransition(id, 'accuser-reception-ceee');

  Future<AutorisationTravail> _transition(String id, String action) async {
    final response = await _client.post<Map<String, dynamic>>('/autorisations-travail/$id/$action');
    return AutorisationTravail.fromJson(response.data!);
  }

  Future<AutorisationTravail> _putTransition(String id, String action) async {
    final response = await _client.put<Map<String, dynamic>>('/autorisations-travail/$id/$action');
    return AutorisationTravail.fromJson(response.data!);
  }

  // --- Historique & visas ---

  Future<List<Map<String, dynamic>>> historique(String id) async {
    final response = await _client.get<List<dynamic>>('/autorisations-travail/$id/historique');
    return (response.data ?? []).cast<Map<String, dynamic>>();
  }

  Future<List<Map<String, dynamic>>> visas(String id) async {
    final response = await _client.get<List<dynamic>>('/autorisations-travail/$id/visas');
    return (response.data ?? []).cast<Map<String, dynamic>>();
  }

  /// PDF officiel (byte[]) — afficher uniquement si exportPdfAutorise.
  Future<Uint8List> exportPdf(String id) async {
    final response = await _client.downloadBytes('/autorisations-travail/$id/export-pdf');
    return Uint8List.fromList(response.data ?? []);
  }
}
