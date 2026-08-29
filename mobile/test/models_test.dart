// Tests des modèles — parsing des réponses JSON réelles du backend.

import 'package:flutter_test/flutter_test.dart';
import 'package:ocp_at_mobile/features/at/data/models/autorisation_travail.dart';
import 'package:ocp_at_mobile/features/auth/data/models/auth_models.dart';
import 'package:ocp_at_mobile/features/notifications/data/notification.dart';
import 'package:ocp_at_mobile/features/visas/data/visa.dart';

void main() {
  group('JwtResponse', () {
    test('parse la réponse de login du backend', () {
      final json = {
        'accessToken': 'access-123',
        'refreshToken': 'refresh-456',
        'type': 'Bearer',
        'utilisateur': {
          'id': 'u1',
          'email': 'ceep@ocp.ma',
          'nom': 'Propriétaire',
          'prenom': 'Chef',
          'actif': true,
          'roles': [
            {'id': 'r1', 'nom': 'CEEP'},
          ],
        },
        'roles': ['CEEP'],
        'permissions': ['READ_AT', 'CREATE_AT'],
      };

      final jwt = JwtResponse.fromJson(json);
      expect(jwt.accessToken, 'access-123');
      expect(jwt.utilisateur.email, 'ceep@ocp.ma');
      expect(jwt.roles, ['CEEP']);
      expect(jwt.permissions, contains('CREATE_AT'));
    });
  });

  group('AutorisationTravail', () {
    final json = {
      'id': 'at1',
      'numero': 'AT-2026-000001',
      'version': 2,
      'objet': 'Remplacement vanne',
      'statut': 'AT_VALIDEE',
      'etatVerrou': 'LIBRE',
      'zoneProprietaireNom': 'Zone A',
      'zoneExecutanteNom': 'Zone B',
      'typeDocumentSource': 'DI',
      'documentSourceNumero': 'DI-2026-000010',
      'risquesIds': ['r1', 'r2'],
      'episIds': ['e1'],
      'exportPdfAutorise': true,
      'exportPdfMotifsRefus': <String>[],
    };

    test('parse le DTO complet', () {
      final at = AutorisationTravail.fromJson(json);
      expect(at.numero, 'AT-2026-000001');
      expect(at.statut, StatutAt.atValidee);
      expect(at.zoneProprietaireNom, 'Zone A');
      expect(at.zoneExecutanteNom, 'Zone B');
      expect(at.risquesIds.length, 2);
      expect(at.exportPdfAutorise, true);
      expect(at.exportPdfMotifsRefus, isEmpty);
      expect(at.verrouilleParAutre, false);
    });

    test('aucune notion Installation dans le modèle (champs connus uniquement)', () {
      // Le modèle n'expose que les champs du DTO backend ; "Installation"
      // a été supprimée du projet et ne doit jamais réapparaître.
      final at = AutorisationTravail.fromJson(json);
      final knownFields = <String>{
        'id', 'numero', 'version', 'objet', 'descriptionTravaux', 'dateDebut',
        'dateFin', 'heureDebut', 'heureFin', 'statut', 'etatVerrou',
        'dateCreation', 'dateModification', 'proprietaireBrouillonId',
        'proprietaireBrouillonNomComplet', 'zoneProprietaireId',
        'zoneProprietaireNom', 'zoneExecutanteId', 'zoneExecutanteNom',
        'datePriseVerrou', 'dateLiberationVerrou', 'typeDocumentSource',
        'documentSourceId', 'documentSourceNumero', 'servicesIntervenants',
        'entreprisesIntervenantes', 'mesuresSecuriteExecutant', 'g1NomCeep',
        'g1NomCeee', 'dateReceptionCeee', 'latitude', 'longitude',
        'visiteCommentaire', 'visiteEffectuee', 'photoPath', 'risquesIds',
        'mesuresIds', 'episIds', 'moyensAccesIds', 'permisIds',
        'exportPdfAutorise', 'exportPdfMotifsRefus',
      };
      expect(knownFields, isNot(contains('installationId')));
      expect(knownFields, isNot(contains('installationNom')));
      expect(at.id, 'at1'); // sanity check
    });

    test('libellés de statut', () {
      expect(StatutAt.libelle('BROUILLON'), 'Brouillon');
      expect(StatutAt.libelle('AT_VALIDEE'), 'AT validée');
      expect(StatutAt.libelle('INCONNU'), 'INCONNU');
      expect(StatutAt.libelle(null), '—');
    });

    test('verrou détecté', () {
      final at = AutorisationTravail.fromJson({
        'id': 'at1',
        'etatVerrou': 'EN_COURS_EDITION',
        'proprietaireBrouillonNomComplet': 'Ali Alami',
      });
      expect(at.verrouilleParAutre, true);
      expect(at.proprietaireBrouillonNomComplet, 'Ali Alami');
    });
  });

  group('Page', () {
    test('parse la pagination Spring', () {
      final json = {
        'content': [
          {'id': 'at1'},
          {'id': 'at2'},
        ],
        'totalElements': 25,
        'totalPages': 2,
        'number': 0,
        'size': 20,
        'last': false,
      };
      final page = Page.fromJson(json, AutorisationTravail.fromJson);
      expect(page.content.length, 2);
      expect(page.totalElements, 25);
      expect(page.number, 0);
      expect(page.last, false);
    });
  });

  group('Visa', () {
    test('parse VisaResponse (hash jamais exposé)', () {
      final visa = Visa.fromJson({
        'id': 'v1',
        'statut': 'VALIDE',
        'signaturePresente': true,
        'utilisateurNomComplet': 'HCEE Nord',
        'ordre': 1,
      });
      expect(visa.statut, StatutVisa.valide);
      expect(visa.signaturePresente, true);
      expect(StatutVisa.libelle('EN_ATTENTE'), 'En attente');
    });
  });

  group('Notification', () {
    test('parse NotificationResponse', () {
      final n = Notification.fromJson({
        'id': 'n1',
        'titre': 'Visa requis',
        'message': "L'AT-2026-000001 attend votre visa.",
        'lu': false,
        'type': 'VISA',
        'lien': '/at/at1',
      });
      expect(n.lu, false);
      expect(n.type, 'VISA');
      expect(n.titre, 'Visa requis');
    });
  });

  group('AuthSession', () {
    test('permissions et rôles', () {
      final jwt = JwtResponse.fromJson({
        'accessToken': 'a',
        'refreshToken': 'r',
        'utilisateur': {
          'id': 'u1',
          'email': 'a@b.c',
          'actif': true,
          'roles': [
            {'id': 'r1', 'nom': 'HCEE'},
          ],
        },
        'roles': ['HCEE'],
        'permissions': ['READ_AT', 'VALIDATE_AT', 'REJECT_AT'],
      });
      final session = AuthSession(
        utilisateur: jwt.utilisateur,
        roles: jwt.roles,
        permissions: jwt.permissions,
      );
      expect(session.hasPermission('VALIDATE_AT'), true);
      expect(session.hasPermission('CREATE_AT'), false);
      expect(session.hasRole('HCEE'), true);
      expect(session.isOcpUser, true);
    });
  });
}
