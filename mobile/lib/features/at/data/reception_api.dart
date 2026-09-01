/// Client API pour la réception des travaux (GET /receptions/at/{atId}, POST /receptions, etc.)
library;

import '../../../core/network/api_client.dart';
import 'models/reception_travaux.dart';

class ReceptionApi {
  final ApiClient _client;
  const ReceptionApi(this._client);

  Future<ReceptionTravaux?> getByAtId(String atId) async {
    try {
      final res = await _client.get<Map<String, dynamic>>('/receptions/at/$atId');
      if (res.data == null) return null;
      return ReceptionTravaux.fromJson(res.data!);
    } catch (_) {
      return null;
    }
  }

  Future<ReceptionTravaux> getById(String id) async {
    final res = await _client.get<Map<String, dynamic>>('/receptions/$id');
    return ReceptionTravaux.fromJson(res.data!);
  }

  Future<ReceptionTravaux> create(ReceptionTravaux reception) async {
    final res = await _client.post<Map<String, dynamic>>(
      '/receptions',
      body: reception.toJson(),
    );
    return ReceptionTravaux.fromJson(res.data!);
  }

  Future<ReceptionTravaux> update(String id, ReceptionTravaux reception) async {
    final res = await _client.put<Map<String, dynamic>>(
      '/receptions/$id',
      body: reception.toJson(),
    );
    return ReceptionTravaux.fromJson(res.data!);
  }

  Future<ReceptionTravaux> signer(String id, String signaturePath) async {
    final res = await _client.put<Map<String, dynamic>>(
      '/receptions/$id/signer',
      body: signaturePath,
    );
    return ReceptionTravaux.fromJson(res.data!);
  }

  Future<ReceptionTravaux> cloturer(String id) async {
    final res = await _client.put<Map<String, dynamic>>('/receptions/$id/cloturer');
    return ReceptionTravaux.fromJson(res.data!);
  }
}
