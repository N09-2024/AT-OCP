/// Sélection de photos et documents — image_picker (caméra/galerie)
/// et file_picker v12 (API statique). Validation locale des types MIME
/// acceptés par le serveur (20 Mo max, PDF/PNG/JPEG/WEBP pour les permis).
library;

import 'dart:typed_data';
import 'package:file_picker/file_picker.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:image_picker/image_picker.dart';

class PickedFileData {
  final Uint8List bytes;
  final String filename;
  final String mimeType;

  const PickedFileData({
    required this.bytes,
    required this.filename,
    required this.mimeType,
  });
}

final filePickServiceProvider = Provider<FilePickService>((ref) => FilePickService());

class FilePickService {
  static const int maxBytes = 20 * 1024 * 1024; // limite serveur : 20 Mo

  Future<PickedFileData?> prendrePhotoCamera() async {
    final xfile = await ImagePicker().pickImage(
      source: ImageSource.camera,
      imageQuality: 85,
      maxWidth: 1920,
    );
    return _fromXFile(xfile);
  }

  Future<PickedFileData?> choisirPhotoGalerie() async {
    final xfile = await ImagePicker().pickImage(
      source: ImageSource.gallery,
      imageQuality: 85,
      maxWidth: 1920,
    );
    return _fromXFile(xfile);
  }

  /// Document de permis : PDF/PNG/JPEG/WEBP uniquement (contrôle serveur).
  Future<PickedFileData?> choisirDocumentPermis() async {
    final result = await FilePicker.platform.pickFiles(
      type: FileType.custom,
      allowedExtensions: ['pdf', 'png', 'jpg', 'jpeg', 'webp'],
      withData: true,
    );
    if (result == null || result.files.isEmpty) return null;
    final f = result.files.single;
    final bytes = f.bytes;
    if (bytes == null) {
      throw Exception('Impossible de lire le fichier sélectionné.');
    }
    if (bytes.length > maxBytes) {
      throw Exception('Fichier trop volumineux (max 20 Mo).');
    }
    return PickedFileData(
      bytes: bytes,
      filename: f.name,
      mimeType: _mimeFromName(f.name),
    );
  }

  Future<PickedFileData?> _fromXFile(XFile? xfile) async {
    if (xfile == null) return null;
    final bytes = await xfile.readAsBytes();
    if (bytes.length > maxBytes) {
      throw Exception('Photo trop volumineuse (max 20 Mo).');
    }
    return PickedFileData(
      bytes: bytes,
      filename: xfile.name,
      mimeType: xfile.mimeType ?? _mimeFromName(xfile.name),
    );
  }

  static String _mimeFromName(String name) {
    final lower = name.toLowerCase();
    if (lower.endsWith('.pdf')) return 'application/pdf';
    if (lower.endsWith('.png')) return 'image/png';
    if (lower.endsWith('.webp')) return 'image/webp';
    if (lower.endsWith('.jpg') || lower.endsWith('.jpeg')) return 'image/jpeg';
    return 'application/octet-stream';
  }
}
