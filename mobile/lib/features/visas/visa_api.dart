/// API visas — endpoints réels du backend (VisaController, préfixe /api/visa) :
///   POST /visa                          (VisaRequest{autorisationTravailId, commentaire, ordre})
///   POST /visa/{id}/sign                (multipart: signature PNG + commentaire)
///   GET  /visa/at/{atId}
///   GET  /visa/{id}/signature           (PNG byte[])
library;

import 'dart:typed_data';
import 'package:dio/dio.dart';
import '../../../core/network/api_client.dart';
import 'data/visa.dart';

class VisaApi {
  final ApiClient _client;
  VisaApi(this._client);

  Future<Visa> create({
    required String autorisationTravailId,
    String? commentaire,
    int? ordre,
  }) async {
    final response = await _client.post<Map<String, dynamic>>(
      '/visa',
      body: {
        'autorisationTravailId': autorisationTravailId,
        'commentaire': ?commentaire,
        'ordre': ?ordre,
      },
    );
    return Visa.fromJson(response.data!);
  }

  /// Signature manuscrite : PNG en multipart, champ "signature".
  Future<Visa> sign({
    required String visaId,
    required Uint8List signaturePng,
    String? commentaire,
  }) async {
    final formData = FormData.fromMap({
      'signature': MultipartFile.fromBytes(signaturePng, filename: 'signature.png'),
      if (commentaire != null && commentaire.isNotEmpty) 'commentaire': commentaire,
    });
    final response = await _client.post<Map<String, dynamic>>(
      '/visa/$visaId/sign',
      body: formData,
    );
    return Visa.fromJson(response.data!);
  }

  Future<List<Visa>> findByAt(String atId) async {
    final response = await _client.get<List<dynamic>>('/visa/at/$atId');
    return (response.data ?? [])
        .whereType<Map<String, dynamic>>()
        .map(Visa.fromJson)
        .toList();
  }

  Future<Uint8List> downloadSignature(String visaId) async {
    final response = await _client.downloadBytes('/visa/$visaId/signature');
    return Uint8List.fromList(response.data ?? []);
  }
}
