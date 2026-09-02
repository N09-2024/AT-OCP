/// Carte interactive pour chaque permis complémentaire requis (Section E) :
/// - Upload de document ou prise de photo via caméra
/// - Déclenchement de l'analyse IA Gemini Vision
/// - Affichage du statut, motif de rejet et score de confiance
/// - Relance d'analyse
library;

import 'package:file_picker/file_picker.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:image_picker/image_picker.dart';
import '../../../../core/theme/app_colors.dart';
import '../../data/models/permis_document.dart';
import '../at_providers.dart';

class PermisUploadTile extends ConsumerStatefulWidget {
  final String atId;
  final String typePermis;
  final String typeNom;
  final PermisDocument? document;
  final bool readOnly;
  final VoidCallback onUpdated;

  const PermisUploadTile({
    super.key,
    required this.atId,
    required this.typePermis,
    required this.typeNom,
    required this.document,
    required this.readOnly,
    required this.onUpdated,
  });

  @override
  ConsumerState<PermisUploadTile> createState() => _PermisUploadTileState();
}

class _PermisUploadTileState extends ConsumerState<PermisUploadTile> {
  bool _uploading = false;
  String? _error;

  static const Map<String, String> _typeIcons = {
    'PERMIS_FEU': '🔥',
    'ESPACE_CONFINE': '🚧',
    'TRAVAIL_HAUTEUR': '🪜',
    'FOUILLE': '⛏️',
    'CONSIGNATION_ENERGIES': '⚡',
    'PLAN_CONSIGNATION': '📋',
  };

  Future<void> _pickFromCamera() async {
    try {
      final picker = ImagePicker();
      final photo = await picker.pickImage(source: ImageSource.camera, imageQuality: 85);
      if (photo == null) return;
      await _uploadFile(photo.path, photo.name);
    } catch (e) {
      setState(() => _error = 'Erreur appareil photo : $e');
    }
  }

  Future<void> _pickFromFile() async {
    try {
      final result = await FilePicker.platform.pickFiles(
        type: FileType.custom,
        allowedExtensions: ['pdf', 'jpg', 'jpeg', 'png'],
      );
      if (result == null || result.files.single.path == null) return;
      final file = result.files.single;
      await _uploadFile(file.path!, file.name);
    } catch (e) {
      setState(() => _error = 'Erreur sélection fichier : $e');
    }
  }

  Future<void> _uploadFile(String filePath, String fileName) async {
    setState(() {
      _uploading = true;
      _error = null;
    });
    try {
      final api = ref.read(permisDocumentApiProvider);
      await api.upload(
        atId: widget.atId,
        typePermis: widget.typePermis,
        filePath: filePath,
        fileName: fileName,
      );
      widget.onUpdated();
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            backgroundColor: OcpColors.success,
            content: Text('Document "${widget.typeNom}" envoyé pour analyse IA.'),
          ),
        );
      }
    } catch (e) {
      setState(() => _error = 'Échec de l\'envoi : $e');
    } finally {
      if (mounted) setState(() => _uploading = false);
    }
  }

  Future<void> _relancerAnalyse() async {
    if (widget.document == null) return;
    setState(() {
      _uploading = true;
      _error = null;
    });
    try {
      final api = ref.read(permisDocumentApiProvider);
      await api.relancerAnalyse(widget.document!.id);
      widget.onUpdated();
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            backgroundColor: OcpColors.forest,
            content: Text('Analyse IA relancée.'),
          ),
        );
      }
    } catch (e) {
      setState(() => _error = 'Échec de la relance : $e');
    } finally {
      if (mounted) setState(() => _uploading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final doc = widget.document;
    final icon = _typeIcons[widget.typePermis] ?? '📄';
    final statut = doc?.statut ?? StatutPermisDocument.enAttenteUpload;

    Color borderColor;
    Color bgColor;
    Widget statusBadge;

    switch (statut) {
      case StatutPermisDocument.valide:
        borderColor = OcpColors.success;
        bgColor = OcpColors.success.withValues(alpha: 0.08);
        statusBadge = const Chip(
          avatar: Icon(Icons.check_circle_rounded, color: OcpColors.success, size: 16),
          label: Text('Validé IA', style: TextStyle(color: OcpColors.success, fontSize: 11, fontWeight: FontWeight.bold)),
          backgroundColor: Colors.transparent,
          side: BorderSide(color: OcpColors.success),
          visualDensity: VisualDensity.compact,
        );
      case StatutPermisDocument.rejete:
        borderColor = OcpColors.errorSoft;
        bgColor = OcpColors.errorSoft.withValues(alpha: 0.08);
        statusBadge = const Chip(
          avatar: Icon(Icons.cancel_rounded, color: OcpColors.errorSoft, size: 16),
          label: Text('Rejeté', style: TextStyle(color: OcpColors.errorSoft, fontSize: 11, fontWeight: FontWeight.bold)),
          backgroundColor: Colors.transparent,
          side: BorderSide(color: OcpColors.errorSoft),
          visualDensity: VisualDensity.compact,
        );
      case StatutPermisDocument.enAttenteAnalyse:
        borderColor = OcpColors.warning;
        bgColor = OcpColors.warning.withValues(alpha: 0.08);
        statusBadge = const Chip(
          avatar: SizedBox(
            width: 12,
            height: 12,
            child: CircularProgressIndicator(strokeWidth: 2, color: OcpColors.warning),
          ),
          label: Text('Analyse en cours…', style: TextStyle(color: OcpColors.warning, fontSize: 11)),
          backgroundColor: Colors.transparent,
          side: BorderSide(color: OcpColors.warning),
          visualDensity: VisualDensity.compact,
        );
      case StatutPermisDocument.enAttenteUpload:
        borderColor = OcpColors.border;
        bgColor = OcpColors.surfaceSoft;
        statusBadge = const Chip(
          label: Text('En attente', style: TextStyle(color: OcpColors.slate, fontSize: 11)),
          backgroundColor: Colors.transparent,
          side: BorderSide(color: OcpColors.border),
          visualDensity: VisualDensity.compact,
        );
    }

    return Container(
      margin: const EdgeInsets.only(bottom: 12),
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: bgColor,
        borderRadius: BorderRadius.circular(10),
        border: Border.all(color: borderColor, width: 1.2),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          // En-tête : Icône + Titre + Badge statut
          Row(
            children: [
              Text(icon, style: const TextStyle(fontSize: 20)),
              const SizedBox(width: 8),
              Expanded(
                child: Text(
                  widget.typeNom,
                  style: const TextStyle(fontWeight: FontWeight.w700, fontSize: 14, color: OcpColors.ink),
                ),
              ),
              statusBadge,
            ],
          ),

          // Message d'erreur
          if (_error != null) ...[
            const SizedBox(height: 6),
            Text(
              _error!,
              style: const TextStyle(fontSize: 11, color: OcpColors.errorSoft, fontWeight: FontWeight.w600),
            ),
          ],

          // Informations détaillées d'analyse
          if (doc != null && doc.motifRejet != null && doc.motifRejet!.isNotEmpty) ...[
            const SizedBox(height: 8),
            Container(
              padding: const EdgeInsets.all(8),
              decoration: BoxDecoration(
                color: OcpColors.errorSoft.withValues(alpha: 0.1),
                borderRadius: BorderRadius.circular(6),
              ),
              child: Text(
                '❌ Motif : ${doc.motifRejet}',
                style: const TextStyle(fontSize: 11, color: OcpColors.errorSoft),
              ),
            ),
          ],

          if (doc != null && doc.scoreConfiance != null && statut == StatutPermisDocument.valide) ...[
            const SizedBox(height: 6),
            Text(
              'Confiance IA : ${(doc.scoreConfiance! * 100).toStringAsFixed(0)}% • ${doc.typeExtrait ?? ''}',
              style: const TextStyle(fontSize: 11, color: OcpColors.forestDark),
            ),
          ],

          if (_uploading) ...[
            const SizedBox(height: 10),
            const LinearProgressIndicator(),
          ] else if (!widget.readOnly) ...[
            const SizedBox(height: 10),
            Row(
              children: [
                // Bouton Prendre une photo (Caméra)
                Expanded(
                  child: OutlinedButton.icon(
                    style: OutlinedButton.styleFrom(
                      padding: const EdgeInsets.symmetric(vertical: 8),
                      side: const BorderSide(color: OcpColors.forest),
                    ),
                    onPressed: _uploading ? null : _pickFromCamera,
                    icon: const Icon(Icons.photo_camera_rounded, size: 16, color: OcpColors.forest),
                    label: const Text('Photo', style: TextStyle(fontSize: 12, color: OcpColors.forest)),
                  ),
                ),
                const SizedBox(width: 8),

                // Bouton Importer Fichier (PDF/Image)
                Expanded(
                  child: OutlinedButton.icon(
                    style: OutlinedButton.styleFrom(
                      padding: const EdgeInsets.symmetric(vertical: 8),
                      side: const BorderSide(color: OcpColors.forest),
                    ),
                    onPressed: _uploading ? null : _pickFromFile,
                    icon: const Icon(Icons.upload_file_rounded, size: 16, color: OcpColors.forest),
                    label: const Text('Fichier/PDF', style: TextStyle(fontSize: 12, color: OcpColors.forest)),
                  ),
                ),

                // Bouton Relancer si rejeté
                if (statut == StatutPermisDocument.rejete) ...[
                  const SizedBox(width: 8),
                  IconButton(
                    tooltip: 'Relancer l\'analyse IA',
                    icon: const Icon(Icons.refresh_rounded, color: OcpColors.warning),
                    onPressed: _uploading ? null : _relancerAnalyse,
                  ),
                ],
              ],
            ),
          ],
        ],
      ),
    );
  }
}
