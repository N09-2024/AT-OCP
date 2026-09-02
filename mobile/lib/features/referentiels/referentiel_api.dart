/// API des référentiels - endpoints réels du backend :
///   GET /zones, /zones/{id}/services
///   GET /services, /services/{id}/chefs-equipe
///   GET /equipements, /entreprises-externes, /risques, /mesures-preparation,
///       /epis, /moyens-acces, /types-permis  (+ /search sur la plupart)
library;

import '../../../core/network/api_client.dart';
import 'data/referentiel_models.dart';

class ReferentielApi {
  final ApiClient _client;
  ReferentielApi(this._client);

  Future<List<Zone>> zones() async => _loadList('/zones', Zone.fromJson);

  Future<List<ServiceOcp>> services({String? zoneId}) async {
    if (zoneId != null) {
      return _loadList('/zones/$zoneId/services', ServiceOcp.fromJson);
    }
    return _loadList('/services', ServiceOcp.fromJson);
  }

  Future<List<Equipement>> equipements() => _loadList('/equipements', Equipement.fromJson);

  Future<List<EntrepriseExterne>> entreprisesExternes() =>
      _loadList('/entreprises-externes', EntrepriseExterne.fromJson);

  Future<List<ReferentielItem>> risques() async =>
      _loadRefentiels('/risques', ReferentielKeys.risques);

  Future<List<ReferentielItem>> mesures() async =>
      _loadRefentiels('/mesures-preparation', ReferentielKeys.mesures);

  Future<List<ReferentielItem>> epis() async => _loadRefentiels('/epis', ReferentielKeys.epis);

  Future<List<ReferentielItem>> moyensAcces() async =>
      _loadRefentiels('/moyens-acces', ReferentielKeys.moyensAcces);

  Future<List<TypePermis>> typesPermis() => _loadList('/types-permis', TypePermis.fromJson);

  // ------------------------------------------------------------------

  Future<List<T>> _loadList<T>(
    String path,
    T Function(Map<String, dynamic>) fromJson,
  ) async {
    final response = await _client.get<List<dynamic>>(path);
    return (response.data ?? [])
        .whereType<Map<String, dynamic>>()
        .map(fromJson)
        .toList();
  }

  Future<List<ReferentielItem>> _loadRefentiels(
    String path,
    (String, String) keys,
  ) async {
    final response = await _client.get<List<dynamic>>(path);
    return (response.data ?? [])
        .whereType<Map<String, dynamic>>()
        .map((json) => ReferentielItem.fromJson(
              json,
              nomKey: keys.$1,
              descriptionKey: keys.$2,
            ),)
        .toList();
  }
}
