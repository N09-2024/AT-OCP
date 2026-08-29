/// Écran intermédiaire de création : POST /api/autorisations-travail
/// crée un brouillon avec verrou automatique côté serveur, puis redirige
/// vers le formulaire d'édition.
library;

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../../core/widgets/states.dart';
import 'at_providers.dart';

class AtCreatePage extends ConsumerStatefulWidget {
  const AtCreatePage({super.key});

  @override
  ConsumerState<AtCreatePage> createState() => _AtCreatePageState();
}

class _AtCreatePageState extends ConsumerState<AtCreatePage> {
  String? _error;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) => _create());
  }

  Future<void> _create() async {
    try {
      final at = await ref.read(atApiProvider).createDirect();
      if (!mounted) return;
      // Remplace cette page par le formulaire (retour direct à la liste).
      context.pushReplacement('/at/${at.id}/edit');
    } catch (_) {
      if (mounted) setState(() => _error = 'Création impossible. Vérifiez votre connexion et réessayez.');
    }
  }

  @override
  Widget build(BuildContext context) {
    if (_error != null) {
      return Scaffold(
        appBar: AppBar(title: const Text('Nouvelle AT')),
        body: ErrorState(
          message: _error!,
          onRetry: () => setState(() {
            _error = null;
            _create();
          }),
        ),
      );
    }
    return const Scaffold(body: LoadingState(message: 'Création du brouillon...'));
  }
}
