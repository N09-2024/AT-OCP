/// API permis — endpoints réels du backend (PermisController, /api/permis) :
///   GET  /permis/at/{atId}
///   POST /permis                      (PermisRequest{typePermisId, estObligatoire, commentaire, autorisationTravailId})
///   POST /permis/{id}/upload          (multipart "file" — PDF/PNG/JPEG/WEBP uniquement)
///   GET  /permis/{id}/download        (byte[])
///   PUT  /permis/{id}/reanalyser      (analyse IA)
///   DELETE /permis/{id}
library;

import 'dart:typed_data';
import 'package:dio/dio.dart';
import '../../../core/network/api_client.dart';
import '../../referentiels/data/referentiel_models.dart';

class StatutPermisVerif {
  static const String aVerifier = 'A_VERIFIER';
  static const String conforme = 'CONFORME';
  static const String nonConforme = 'NON_CONFORME';
  static const String expire = 'EXPIRE';

  static String libelle(String? s) => switch (s) {
        aVerifier => 'À vérifier',
        conforme => 'Conforme',
        nonConforme => 'Non conforme',
        expire => 'Expiré',
        _ => s ?? '—',
      };
}

class Permis {
  final String id;
  final String? numero;
  final TypePermis? typePermis;
  final DateTime? dateEmission;
  final DateTime? dateExpiration;
  final String? statutVerification;
  final bool? estObligatoire;
  final String? commentaire;
  final String? fichierJointId;
  final String? fichierJointNom;

  const Permis({
    required this.id,
    this.numero,
    this.typePermis,
    this.dateEmission,
    this.dateExpiration,
    this.statutVerification,
    this.estObligatoire,
    this.commentaire,
    this.fichierJointId,
    this.fichierJointNom,
  });

  factory Permis.fromJson(Map<String, dynamic> json) => Permis(
        id: json['id'] as String,
        numero: json['numero'] as String?,
        typePermis: json['typePermis'] == null
            ? null
            : TypePermis.fromJson(json['typePermis'] as Map<String, dynamic>),
        dateEmission:
            json['dateEmission'] == null ? null : DateTime.tryParse(json['dateEmission'].toString()),
        dateExpiration:
            json['dateExpiration'] == null ? null : DateTime.tryParse(json['dateExpiration'].toString()),
        statutVerification: json['statutVerification'] as String?,
        estObligatoire: json['estObligatoire'] as bool?,
        commentaire: json['commentaire'] as String?,
        fichierJointId: json['fichierJointId'] as String?,
        fichierJointNom: json['fichierJointNom'] as String?,
      );
}

class PermisApi {
  final ApiClient _client;
  PermisApi(this._client);

  Future<List<Permis>> findByAt(String atId) async {
    final response = await _client.get<List<dynamic>>('/permis/at/$atId');
    return (response.data ?? [])
        .whereType<Map<String, dynamic>>()
        .map(Permis.fromJson)
        .toList();
  }

  Future<Permis> create({
    required String autorisationTravailId,
    required String typePermisId,
    bool estObligatoire = false,
    String? commentaire,
  }) async {
    final response = await _client.post<Map<String, dynamic>>(
      '/permis',
      body: {
        'autorisationTravailId': autorisationTravailId,
        'typePermisId': typePermisId,
        'estObligatoire': estObligatoire,
        if (commentaire != null && commentaire.isNotEmpty) 'commentaire': commentaire,
      },
    );
    return Permis.fromJson(response.data!);
  }

  /// Upload du fichier de permis — types MIME acceptés par le serveur :
  /// application/pdf, image/png, image/jpeg, image/webp.
  Future<void> uploadFichier(
    String permisId, {
    required Uint8List bytes,
    required String filename,
    required String mimeType,
  }) async {
    final formData = FormData.fromMap({
      'file': MultipartFile.fromBytes(bytes, filename: filename, contentType: DioMediaType.parse(mimeType)),
    });
    await _client.post<Map<String, dynamic>>('/permis/$permisId/upload', body: formData);
  }

  Future<Uint8List> downloadFichier(String permisId) async {
    final response = await _client.downloadBytes('/permis/$permisId/download');
    return Uint8List.fromList(response.data ?? []);
  }

  Future<void> reanalyser(String permisId) =>
      _client.put<void>('/permis/$permisId/reanalyser');

  Future<void> delete(String permisId) => _client.delete<void>('/permis/$permisId');
}
