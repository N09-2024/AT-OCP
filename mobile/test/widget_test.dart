// Smoke test de l'application OCP AT Mobile.
// Vérifie que l'app démarre sans crash (GoRouter + Riverpod).

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'package:ocp_at_mobile/main.dart';

void main() {
  testWidgets('App démarre sans crash', (WidgetTester tester) async {
    await tester.pumpWidget(const ProviderScope(child: App()));
    // Laisse les frames s'initialiser
    await tester.pump(const Duration(milliseconds: 300));
    // L'app s'est rendue sans exception
    expect(find.byType(MaterialApp), findsOneWidget);
  });
}
