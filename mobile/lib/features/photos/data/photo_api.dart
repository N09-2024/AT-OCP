/// API photos — endpoints réels du backend :
///   GET    /visites-prealables                    (liste ; filtrable par documentSourceId)
///   POST   /visites-prealables/{id}/photos        (multipart file + legende)
///   DELETE /visites-prealables/{id}/photos/{photoId}
///   GET    /receptions/at/{atId}                  (réception liée à l'AT)
///   GET    /receptions/{id}/photos | POST | DELETE /receptions/{id}/photos/{photoId}
///
/// Les photos vivent sur les visites et les réceptions (pas de galerie AT générique).
library;

import 'dart:typed_data';
import 'package:dio/dio.dart';
import '../../../core/network/api_client.dart';

class PhotoRef {
  final String id;
  final String? nom;
  final String? legende;
  final String? typeMime;
  final int? taille;
  final DateTime? dateCreation;
  final String? visitePrealableId;

  const PhotoRef({
    required this.id,
    this.nom,
    this.legende,
    this.typeMime,
    this.taille,
    this.dateCreation,
    this.visitePrealableId,
  });

  factory PhotoRef.fromJson(Map<String, dynamic> json) => PhotoRef(
        id: json['id'] as String,
        nom: json['nom'] as String?,
        legende: json['legende'] as String?,
        typeMime: json['typeMime'] as String?,
        taille: json['taille'] as int?,
        dateCreation:
            json['dateCreation'] == null ? null : DateTime.tryParse(json['dateCreation'].toString()),
        visitePrealableId: json['visitePrealableId'] as String?,
      );
}

class VisitePrealable {
  final String id;
  final DateTime? dateHeureDebut;
  final double? latitude;
  final double? longitude;
  final String? commentaire;
  final bool effectuee;
  final String? documentSourceId;
  final String? typeDocumentSource;
  final String? documentSourceNumero;
  final String? visiteurNomComplet;
  final List<PhotoRef> photos;

  const VisitePrealable({
    required this.id,
    this.dateHeureDebut,
    this.latitude,
    this.longitude,
    this.commentaire,
    required this.effectuee,
    this.documentSourceId,
    this.typeDocumentSource,
    this.documentSourceNumero,
    this.visiteurNomComplet,
    this.photos = const [],
  });

  factory VisitePrealable.fromJson(Map<String, dynamic> json) => VisitePrealable(
        id: json['id'] as String,
        dateHeureDebut: json['dateHeureDebut'] == null
            ? null
            : DateTime.tryParse(json['dateHeureDebut'].toString()),
        latitude: (json['latitude'] as num?)?.toDouble(),
        longitude: (json['longitude'] as num?)?.toDouble(),
        commentaire: json['commentaire'] as String?,
        effectuee: json['effectuee'] as bool? ?? false,
        documentSourceId: json['documentSourceId'] as String?,
        typeDocumentSource: json['typeDocumentSource'] as String?,
        documentSourceNumero: json['documentSourceNumero'] as String?,
        visiteurNomComplet: json['visiteurNomComplet'] as String?,
        photos: (json['photos'] as List<dynamic>? ?? [])
            .whereType<Map<String, dynamic>>()
            .map(PhotoRef.fromJson)
            .toList(),
      );
}

class ReceptionRef {
  final String id;
  final String? autorisationTravailId;

  const ReceptionRef({required this.id, this.autorisationTravailId});

  factory ReceptionRef.fromJson(Map<String, dynamic> json) => ReceptionRef(
        id: json['id'] as String,
        autorisationTravailId: json['autorisationTravailId'] as String?,
      );
}

class PhotoApi {
  final ApiClient _client;
  PhotoApi(this._client);

  /// Visites préalables liées au document source d'une AT
  /// (l'AT n'expose pas d'ID de visite : filtrage par documentSourceId).
  Future<List<VisitePrealable>> visitesPourAt(String documentSourceId) async {
    final response = await _client.get<List<dynamic>>('/visites-prealables');
    return (response.data ?? [])
        .whereType<Map<String, dynamic>>()
        .map(VisitePrealable.fromJson)
        .where((v) => v.documentSourceId == documentSourceId)
        .toList();
  }

  Future<PhotoRef> addVisitePhoto(
    String visiteId, {
    required Uint8List bytes,
    required String filename,
    required String mimeType,
    String? legende,
  }) async {
    final formData = FormData.fromMap({
      'file': MultipartFile.fromBytes(bytes, filename: filename, contentType: DioMediaType.parse(mimeType)),
      if (legende != null && legende.isNotEmpty) 'legende': legende,
    });
    final response = await _client.post<Map<String, dynamic>>(
      '/visites-prealables/$visiteId/photos',
      body: formData,
    );
    return PhotoRef.fromJson(response.data!);
  }

  Future<void> deleteVisitePhoto(String visiteId, String photoId) =>
      _client.delete<void>('/visites-prealables/$visiteId/photos/$photoId');

  /// Réception liée à une AT (GET /receptions/at/{atId}).
  Future<ReceptionRef?> receptionDeAt(String atId) async {
    final response = await _client.get<Map<String, dynamic>>('/receptions/at/$atId');
    if (response.data == null) return null;
    return ReceptionRef.fromJson(response.data!);
  }

  Future<List<PhotoRef>> receptionPhotos(String receptionId) async {
    final response = await _client.get<List<dynamic>>('/receptions/$receptionId/photos');
    return (response.data ?? [])
        .whereType<Map<String, dynamic>>()
        .map(PhotoRef.fromJson)
        .toList();
  }

  Future<PhotoRef> addReceptionPhoto(
    String receptionId, {
    required Uint8List bytes,
    required String filename,
    required String mimeType,
    String? legende,
  }) async {
    final formData = FormData.fromMap({
      'file': MultipartFile.fromBytes(bytes, filename: filename, contentType: DioMediaType.parse(mimeType)),
      if (legende != null && legende.isNotEmpty) 'legende': legende,
    });
    final response = await _client.post<Map<String, dynamic>>(
      '/receptions/$receptionId/photos',
      body: formData,
    );
    return PhotoRef.fromJson(response.data!);
  }

  Future<void> deleteReceptionPhoto(String receptionId, String photoId) =>
      _client.delete<void>('/receptions/$receptionId/photos/$photoId');
}
