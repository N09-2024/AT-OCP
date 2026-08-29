// Tests étendus — spec §40 :
//   - payload AutoSaveRequest exact attendu par le backend
//   - mapping DioException → Failure (messages compréhensibles)
//   - formatage des dates/heures

import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:ocp_at_mobile/core/errors/error_mapper.dart';
import 'package:ocp_at_mobile/core/errors/failures.dart';
import 'package:ocp_at_mobile/core/utils/app_date.dart';
import 'package:ocp_at_mobile/features/at/data/models/autorisation_travail.dart';
import 'package:ocp_at_mobile/features/at/presentation/at_form_controller.dart';
import 'package:ocp_at_mobile/features/notifications/presentation/notifications_page.dart';

void main() {
  group('AtFormData.toAutoSaveJson (payload backend exact)', () {
    test('champs renseignés + formats ISO', () {
      final data = AtFormData(
        objet: 'Remplacement vanne',
        descriptionTravaux: 'Dépose et repose',
        dateDebut: DateTime(2026, 9, 1),
        dateFin: DateTime(2026, 9, 2),
        heureDebut: const TimeOfDay(hour: 8, minute: 30),
        heureFin: const TimeOfDay(hour: 17, minute: 0),
        servicesIntervenants: 'Service Maintenance',
        entreprisesIntervenantes: 'Entreprise X',
        mesuresSecuriteExecutant: 'Consignation',
        zoneProprietaireId: 'z1',
        zoneProprietaireNom: 'Zone P',
        zoneExecutanteId: 'z2',
        zoneExecutanteNom: 'Zone E',
        risquesIds: {'r1', 'r2'},
        mesuresIds: {'m1'},
        episIds: {'e1', 'e2', 'e3'},
        moyensAccesIds: {'ma1'},
        permisIds: {'p1'},
        visiteEffectuee: true,
        visiteCommentaire: 'Chantier conforme',
      );

      final json = data.toAutoSaveJson();
      expect(json['objet'], 'Remplacement vanne');
      expect(json['dateDebut'], '2026-09-01');
      expect(json['dateFin'], '2026-09-02');
      expect(json['heureDebut'], '08:30:00');
      expect(json['heureFin'], '17:00:00');
      expect(json['zoneProprietaireId'], 'z1');
      expect(json['risquesIds'], containsAll(['r1', 'r2']));
      expect(json['episIds'].length, 3);
      expect(json['visiteEffectuee'], true);
      // Champs toujours présents (même vides) comme le backend les attend :
      expect(json.containsKey('descriptionTravaux'), true);
      expect(json.containsKey('entreprisesIntervenantes'), true);
      expect(json.containsKey('mesuresSecuriteExecutant'), true);
      expect(json.containsKey('zoneExecutanteId'), true);
      // Aucun champ Installation :
      expect(json.containsKey('installationId'), false);
      expect(json.containsKey('installation'), false);
    });

    test('champs vides : dates/heures omises, textes vides conservés', () {
      final json = const AtFormData().toAutoSaveJson();
      expect(json.containsKey('dateDebut'), false);
      expect(json.containsKey('heureDebut'), false);
      expect(json['descriptionTravaux'], '');
      expect(json['risquesIds'], isEmpty);
      expect(json['visiteEffectuee'], false);
    });

    test('fromAt : pré-remplissage complet depuis le DTO', () {
      final at = AutorisationTravail.fromJson({
        'id': 'at1',
        'objet': 'Réparation pompe',
        'dateDebut': '2026-09-01',
        'heureDebut': '14:15:00',
        'zoneExecutanteNom': 'Zone E',
        'risquesIds': ['r1'],
      });
      final data = AtFormData.fromAt(at);
      expect(data.objet, 'Réparation pompe');
      expect(data.heureDebut, const TimeOfDay(hour: 14, minute: 15));
      expect(data.risquesIds, {'r1'});
      expect(data.zoneExecutanteNom, 'Zone E');
    });

    test('parseTime LocalTime backend', () {
      expect(AtFormData.parseTime('09:05:00'), const TimeOfDay(hour: 9, minute: 5));
      expect(AtFormData.parseTime(null), isNull);
      expect(AtFormData.parseTime('invalide'), isNull);
    });
  });

  group('mapDioError (messages compréhensibles)', () {
    Response<dynamic> response(int status, [dynamic data]) =>
        Response(requestOptions: RequestOptions(), statusCode: status, data: data);

    test('400 avec message métier → message conservé tel quel', () {
      final f = mapDioError(DioException(
          requestOptions: RequestOptions(),
          type: DioExceptionType.badResponse,
          response: response(400, {'message': 'Le motif est obligatoire.'}),),);
      expect(f, isA<ApiFailure>());
      expect(f.message, 'Le motif est obligatoire.');
    });

    test('403 → droits insuffisants', () {
      final f = mapDioError(DioException(
          requestOptions: RequestOptions(),
          type: DioExceptionType.badResponse,
          response: response(403),),);
      expect(f.message, contains('droits'));
    });

    test('404 → introuvable', () {
      final f = mapDioError(DioException(
          requestOptions: RequestOptions(),
          type: DioExceptionType.badResponse,
          response: response(404),),);
      expect(f.message, contains('introuvable'));
    });

    test('500 avec message backend exploitable', () {
      final f = mapDioError(DioException(
          requestOptions: RequestOptions(),
          type: DioExceptionType.badResponse,
          response: response(500, {'message': 'Cette AT est déjà signée.'}),),);
      expect(f.message, 'Cette AT est déjà signée.');
    });

    test('500 sans message → message générique avec code', () {
      final f = mapDioError(DioException(
          requestOptions: RequestOptions(),
          type: DioExceptionType.badResponse,
          response: response(500),),);
      expect(f.message, contains('500'));
    });

    test('erreur réseau → NetworkFailure', () {
      final f = mapDioError(DioException(
          requestOptions: RequestOptions(),
          type: DioExceptionType.connectionError,),);
      expect(f, isA<NetworkFailure>());
      expect(f.message, contains('connexion'));
    });

    test('timeout → TimeoutFailure', () {
      final f = mapDioError(DioException(
          requestOptions: RequestOptions(),
          type: DioExceptionType.receiveTimeout,),);
      expect(f, isA<TimeoutFailure>());
    });

    test('Failure transmis tel quel (pas de double wrapping)', () {
      const original = SessionExpiredFailure();
      expect(mapDioError(original), same(original));
    });
  });

  group('AppDate (affichage cohérent web)', () {
    test('heureSimple LocalTime', () {
      expect(AppDate.heureSimple('08:30:00'), '08:30');
      expect(AppDate.heureSimple(null), '—');
    });

    test('dateHeure et relative', () {
      final now = DateTime.now();
      expect(AppDate.relative(now), "À l'instant");
      expect(AppDate.relative(now.subtract(const Duration(minutes: 5))), 'Il y a 5 min');
      expect(AppDate.dateHeure(DateTime(2026, 8, 28, 14, 5)), '28/08/2026 14:05');
    });
  });

  group('routeFromLien (navigation depuis notifications)', () {
    test('chemins AT directs et normalisés', () {
      expect(routeFromLien('/at/abc123'), '/at/abc123');
      expect(routeFromLien('at/abc123'), '/at/abc123');
      expect(routeFromLien('/autorisations/abc123'), '/at/abc123');
      expect(routeFromLien('/autorisations/abc123/editer'), '/at/abc123');
    });

    test('liens inexploitables ou hors périmètre mobile', () {
      expect(routeFromLien(null), isNull);
      expect(routeFromLien(''), isNull);
      expect(routeFromLien('/admin/users'), isNull);
      expect(routeFromLien('/referentiels/zones'), isNull);
    });

    test('URL web complète avec hash', () {
      expect(routeFromLien('https://at-ocp.ocp.ma/#/at/abc123'), '/at/abc123');
    });
  });
}
