// Tests widgets - composants de base de l'UI.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:ocp_at_mobile/core/theme/app_theme.dart';
import 'package:ocp_at_mobile/core/widgets/at_card.dart';
import 'package:ocp_at_mobile/core/widgets/states.dart';
import 'package:ocp_at_mobile/core/widgets/statut_chip.dart';
import 'package:ocp_at_mobile/features/at/data/models/autorisation_travail.dart';

Widget _wrap(Widget child) => MaterialApp(
      theme: AppTheme.light(),
      home: Scaffold(body: child),
    );

void main() {
  group('StatutChip', () {
    testWidgets('affiche le libellé français du statut', (tester) async {
      await tester.pumpWidget(_wrap(const StatutChip(statut: StatutAt.atValidee)));
      expect(find.text('AT validée'), findsOneWidget);
    });

    testWidgets('statut inconnu affiché brut, null → tiret', (tester) async {
      await tester.pumpWidget(_wrap(const Column(children: [
        StatutChip(statut: 'STATUT_BIZARRE'),
        StatutChip(statut: null),
      ],),),);
      expect(find.text('STATUT_BIZARRE'), findsOneWidget);
      expect(find.text('-'), findsOneWidget);
    });
  });

  group('AtCard', () {
    testWidgets('affiche numéro, objet, zones P/E, source, statut', (tester) async {
      final at = AutorisationTravail.fromJson({
        'id': 'at1',
        'numero': 'AT-2026-000001',
        'objet': 'Remplacement vanne',
        'statut': 'AT_VALIDEE',
        'zoneProprietaireNom': 'Zone A',
        'zoneExecutanteNom': 'Zone B',
        'typeDocumentSource': 'DI',
        'documentSourceNumero': 'DI-2026-000010',
      });
      await tester.pumpWidget(_wrap(AtCard(at: at)));

      expect(find.text('AT-2026-000001'), findsOneWidget);
      expect(find.text('Remplacement vanne'), findsOneWidget);
      expect(find.text('Zone B'), findsOneWidget);
      expect(find.text('Zone A'), findsOneWidget);
      expect(find.text('DI'), findsOneWidget);
      expect(find.text('AT validée'), findsOneWidget);
      expect(find.text('Verrouillée'), findsNothing);
    });

    testWidgets('AT verrouillée : badge affiché', (tester) async {
      final at = AutorisationTravail.fromJson({
        'id': 'at1',
        'numero': 'AT-2026-000002',
        'etatVerrou': 'EN_COURS_EDITION',
      });
      await tester.pumpWidget(_wrap(AtCard(at: at)));
      expect(find.text('Verrouillée'), findsOneWidget);
    });
  });

  group('États UI', () {
    testWidgets('ErrorState affiche le message et déclenche Réessayer', (tester) async {
      var retries = 0;
      await tester.pumpWidget(_wrap(ErrorState(
        message: 'Erreur de connexion.',
        onRetry: () => retries++,
      ),),);

      expect(find.text('Erreur de connexion.'), findsOneWidget);
      expect(find.text('Réessayer'), findsOneWidget);

      await tester.tap(find.text('Réessayer'));
      expect(retries, 1);
    });

    testWidgets('EmptyState affiche le message', (tester) async {
      await tester.pumpWidget(_wrap(const EmptyState(message: 'Aucune AT trouvée.')));
      expect(find.text('Aucune AT trouvée.'), findsOneWidget);
    });

    testWidgets('LoadingState affiche le spinner et le message', (tester) async {
      await tester.pumpWidget(_wrap(const LoadingState(message: 'Chargement des AT...')));
      expect(find.byType(CircularProgressIndicator), findsOneWidget);
      expect(find.text('Chargement des AT...'), findsOneWidget);
    });
  });
}
