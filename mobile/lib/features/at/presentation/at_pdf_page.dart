/// PDF officiel d'une AT - GET /autorisations-travail/{id}/export-pdf (byte[]).
/// Le PDF est généré par le backend ; le mobile se contente de l'afficher.
/// Accès conditionné par exportPdfAutorise côté détail AT (déjà contrôlé serveur).
library;

import 'dart:io';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_pdfview/flutter_pdfview.dart';
import 'package:path_provider/path_provider.dart';
import '../../../core/errors/error_mapper.dart';
import '../../../core/errors/failures.dart';
import '../../../core/widgets/states.dart';
import 'at_providers.dart';

enum PdfState { loading, ready, error }

final atPdfProvider =
    FutureProvider.autoDispose.family<String, String>((ref, atId) async {
  final api = ref.watch(atApiProvider);
  try {
    final bytes = await api.exportPdf(atId);
    final dir = await getTemporaryDirectory();
    final file = File('${dir.path}/at_$atId.pdf');
    await file.writeAsBytes(bytes, flush: true);
    return file.path;
  } catch (e) {
    throw mapDioError(e);
  }
});

class AtPdfPage extends ConsumerWidget {
  final String atId;
  const AtPdfPage({super.key, required this.atId});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final pdf = ref.watch(atPdfProvider(atId));

    return Scaffold(
      appBar: AppBar(title: const Text('PDF officiel')),
      body: pdf.when(
        loading: () => const LoadingState(message: 'Génération du PDF...'),
        error: (e, _) => ErrorState(
          message: e is Failure ? e.message : 'PDF indisponible.',
          onRetry: () => ref.invalidate(atPdfProvider(atId)),
        ),
        data: (path) => PDFView(
          filePath: path,
          enableSwipe: true,
          swipeHorizontal: false,
          autoSpacing: true,
          pageFling: true,
          onError: (error) {
            debugPrint('Erreur PDFView: $error');
          },
        ),
      ),
    );
  }
}
