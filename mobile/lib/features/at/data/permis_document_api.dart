/// API client pour la gestion et l'analyse IA des documents de permis.
library;

import 'package:dio/dio.dart';
import '../../../core/network/api_client.dart';
import 'models/permis_document.dart';

class PermisDocumentApi {
  final ApiClient _client;

  PermisDocumentApi(this._client);

  /// Liste les documents de permis rattachés à une AT.
  Future<List<PermisDocument>> getPermisDocuments(String atId) async {
    final response = await _client.dio.get('/permis-documents/at/$atId');
    final list = response.data as List<dynamic>? ?? [];
    return list
        .map((e) => PermisDocument.fromJson(e as Map<String, dynamic>))
        .toList();
  }

  /// Initialise la liste des permis requis selon la sélection de la section E.
  Future<List<PermisDocument>> initialiser(String atId) async {
    final response = await _client.dio.post('/permis-documents/at/$atId/initialiser');
    final list = response.data as List<dynamic>? ?? [];
    return list
        .map((e) => PermisDocument.fromJson(e as Map<String, dynamic>))
        .toList();
  }

  /// Upload un document (photo ou fichier PDF) pour un type de permis et déclenche l'IA.
  Future<PermisDocument> upload({
    required String atId,
    required String typePermis,
    required String filePath,
    required String fileName,
  }) async {
    final formData = FormData.fromMap({
      'typePermis': typePermis,
      'file': await MultipartFile.fromFile(filePath, filename: fileName),
    });

    final response = await _client.dio.post(
      '/permis-documents/at/$atId/upload',
      data: formData,
    );
    return PermisDocument.fromJson(response.data as Map<String, dynamic>);
  }

  /// Relance l'analyse IA sur un document déjà uploadé.
  Future<PermisDocument> relancerAnalyse(String id) async {
    final response = await _client.dio.post('/permis-documents/$id/relancer-analyse');
    return PermisDocument.fromJson(response.data as Map<String, dynamic>);
  }
}
