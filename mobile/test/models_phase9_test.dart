// Tests des modèles Phases 9-10 - permis, photos, visites préalables.

import 'package:flutter_test/flutter_test.dart';
import 'package:ocp_at_mobile/features/permis/data/permis_api.dart';
import 'package:ocp_at_mobile/features/photos/data/photo_api.dart';

void main() {
  group('Permis', () {
    final json = {
      'id': 'p1',
      'numero': 'PERM-2026-000001',
      'typePermis': {'id': 't1', 'nom': 'Travail en hauteur', 'description': null},
      'dateEmission': '2026-08-20',
      'statutVerification': 'CONFORME',
      'estObligatoire': true,
      'fichierJointId': 'f1',
      'fichierJointNom': 'permis-hauteur.pdf',
    };

    test('parse PermisResponse', () {
      final p = Permis.fromJson(json);
      expect(p.numero, 'PERM-2026-000001');
      expect(p.typePermis?.libelle, 'Travail en hauteur');
      expect(p.statutVerification, StatutPermisVerif.conforme);
      expect(p.estObligatoire, true);
    });

    test('libellés de statut de vérification', () {
      expect(StatutPermisVerif.libelle('A_VERIFIER'), 'À vérifier');
      expect(StatutPermisVerif.libelle('CONFORME'), 'Conforme');
      expect(StatutPermisVerif.libelle('NON_CONFORME'), 'Non conforme');
      expect(StatutPermisVerif.libelle('EXPIRE'), 'Expiré');
      expect(StatutPermisVerif.libelle(null), '-');
    });
  });

  group('VisitePrealable / PhotoRef', () {
    test('parse la visite avec ses photos', () {
      final v = VisitePrealable.fromJson({
        'id': 'v1',
        'effectuee': true,
        'latitude': 32.29,
        'longitude': -9.23,
        'documentSourceId': 'di-1',
        'typeDocumentSource': 'DI',
        'documentSourceNumero': 'DI-2026-000010',
        'photos': [
          {
            'id': 'ph1',
            'nom': 'vanne.jpg',
            'legende': 'Vanne avant dépose',
            'taille': 102400,
          }
        ],
      });
      expect(v.effectuee, true);
      expect(v.documentSourceNumero, 'DI-2026-000010');
      expect(v.photos.length, 1);
      expect(v.photos.first.legende, 'Vanne avant dépose');
      // Aucune notion Installation :
      expect(v.typeDocumentSource == 'DI', true);
    });

    test('photo minimale', () {
      final p = PhotoRef.fromJson({'id': 'ph2'});
      expect(p.id, 'ph2');
      expect(p.nom, isNull);
    });
  });
}
