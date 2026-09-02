/// Écran de signature manuscrite - l'utilisateur signe avec son doigt.
/// Le PNG généré est envoyé au backend via POST /visa/{id}/sign
/// (multipart "signature" + "commentaire"). Le serveur enregistre
/// hash + IP + horodatage (non-répudiation).
library;

import 'dart:typed_data';
import 'package:flutter/material.dart';
import 'package:signature/signature.dart';

class SignatureResult {
  final Uint8List pngBytes;
  const SignatureResult(this.pngBytes);
}

class SignatureScreen extends StatefulWidget {
  final String signataireNom;
  const SignatureScreen({super.key, required this.signataireNom});

  @override
  State<SignatureScreen> createState() => _SignatureScreenState();
}

class _SignatureScreenState extends State<SignatureScreen> {
  late final SignatureController _controller;
  bool _hasStrokes = false;
  bool _sending = false;

  @override
  void initState() {
    super.initState();
    _controller = SignatureController(
      penStrokeWidth: 3,
      penColor: const Color(0xFF16241E),
      exportBackgroundColor: Colors.white,
    );
    _controller.addListener(() {
      final has = _controller.isNotEmpty;
      if (has != _hasStrokes) setState(() => _hasStrokes = has);
    });
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  Future<void> _valider() async {
    if (!_hasStrokes || _sending) return;
    setState(() => _sending = true);
    try {
      final png = await _controller.toPngBytes();
      if (png == null) throw Exception('Génération du PNG impossible.');
      if (mounted) Navigator.of(context).pop(SignatureResult(png));
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(content: Text('Impossible de générer la signature. Réessayez.')),);
      }
    } finally {
      if (mounted) setState(() => _sending = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Signature manuscrite')),
      body: SafeArea(
        child: Column(
          children: [
            Padding(
              padding: const EdgeInsets.fromLTRB(20, 16, 20, 8),
              child: Row(
                children: [
                  const Icon(Icons.badge_outlined, size: 18),
                  const SizedBox(width: 8),
                  Expanded(
                    child: Text(
                      widget.signataireNom,
                      style: const TextStyle(fontWeight: FontWeight.w700, fontSize: 15),
                    ),
                  ),
                  Text(
                    'Signez avec votre doigt dans le cadre ci-dessous.',
                    style: TextStyle(fontSize: 11, color: Theme.of(context).colorScheme.onSurfaceVariant),
                  ),
                ],
              ),
            ),
            Expanded(
              child: Padding(
                padding: const EdgeInsets.symmetric(horizontal: 20),
                child: Container(
                  decoration: BoxDecoration(
                    color: Colors.white,
                    borderRadius: BorderRadius.circular(12),
                    border: Border.all(
                        color: _hasStrokes ? const Color(0xFF3C7A5C) : const Color(0xFFD6E3DC),
                        width: _hasStrokes ? 2 : 1,),
                  ),
                  child: ClipRRect(
                    borderRadius: BorderRadius.circular(12),
                    child: Stack(
                      children: [
                        Signature(
                          controller: _controller,
                          backgroundColor: Colors.white,
                        ),
                        if (!_hasStrokes)
                          const Center(
                            child: Text(
                              'Zone de signature',
                              style: TextStyle(color: Color(0xFF9E9E9E), fontSize: 16),
                            ),
                          ),
                      ],
                    ),
                  ),
                ),
              ),
            ),
            Padding(
              padding: const EdgeInsets.all(20),
              child: Row(
                children: [
                  Expanded(
                    child: OutlinedButton.icon(
                      onPressed: _hasStrokes ? _controller.clear : null,
                      icon: const Icon(Icons.delete_outline_rounded),
                      label: const Text('Effacer'),
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    flex: 2,
                    child: FilledButton.icon(
                      onPressed: (_hasStrokes && !_sending) ? _valider : null,
                      icon: _sending
                          ? const SizedBox(
                              width: 18,
                              height: 18,
                              child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white),
                            )
                          : const Icon(Icons.draw_rounded),
                      label: const Text('Valider la signature'),
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}
