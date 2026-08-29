/// Photos d'une AT — les photos vivent sur les visites préalables
/// (filtrées par documentSourceId) et la réception des travaux.
/// Ajout via caméra/galerie, prévisualisation, suppression avant/après envoi.
library;

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/errors/error_mapper.dart';
import '../../../core/errors/failures.dart';
import '../../../core/network/api_providers.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/utils/app_date.dart';
import '../../../core/utils/file_pick_service.dart';
import '../../../core/widgets/states.dart';
import '../../at/presentation/at_providers.dart' show atDetailProvider;
import '../data/photo_api.dart';

final photoApiProvider = Provider<PhotoApi>((ref) => PhotoApi(ref.watch(apiClientProvider)));
final filePickServiceProvider = Provider<FilePickService>((ref) => FilePickService());

class _PhotoGalleriesState {
  final List<VisitePrealable> visites;
  final ReceptionRef? reception;
  final List<PhotoRef> receptionPhotos;

  const _PhotoGalleriesState(this.visites, this.reception, this.receptionPhotos);
}

final atPhotosProvider = FutureProvider.autoDispose
    .family<_PhotoGalleriesState, String>((ref, atId) async {
  final photoApi = ref.watch(photoApiProvider);

  // 1. AT (pour documentSourceId)
  final at = await ref.watch(atDetailProvider(atId).future);
  final documentSourceId = at.documentSourceId;

  // 2. Visites du document source + réception liée
  if (documentSourceId == null) {
    final reception = await _tryReception(photoApi, atId);
    return _PhotoGalleriesState(const [], reception, const []);
  }
  final visites = await photoApi.visitesPourAt(documentSourceId);
  final reception = await _tryReception(photoApi, atId);
  final receptionPhotos =
      reception == null ? const <PhotoRef>[] : await photoApi.receptionPhotos(reception.id);
  return _PhotoGalleriesState(visites, reception, receptionPhotos);
});

Future<ReceptionRef?> _tryReception(PhotoApi api, String atId) async {
  try {
    return await api.receptionDeAt(atId);
  } catch (_) {
    return null; // 404 : pas encore de réception
  }
}

class AtPhotosPage extends ConsumerWidget {
  final String atId;
  const AtPhotosPage({super.key, required this.atId});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final galleries = ref.watch(atPhotosProvider(atId));

    return Scaffold(
      appBar: AppBar(title: const Text('Photos')),
      body: galleries.when(
        loading: () => const LoadingState(message: 'Chargement des photos...'),
        error: (e, _) => ErrorState(
          message: e is Failure ? e.message : 'Photos indisponibles.',
          onRetry: () => ref.invalidate(atPhotosProvider(atId)),
        ),
        data: (g) {
          final total = g.visites.fold<int>(0, (s, v) => s + v.photos.length) + g.receptionPhotos.length;
          if (total == 0 && g.visites.isEmpty) {
            return const EmptyState(
              message: 'Aucune photo. Les photos sont prises lors de la visite '
                  'préalable et de la réception des travaux.',
              icon: Icons.photo_camera_outlined,
            );
          }
          return ListView(
            padding: const EdgeInsets.only(bottom: 24),
            children: [
              for (final visite in g.visites) ...[
                _SectionHeader(
                  icon: Icons.travel_explore_rounded,
                  title: 'Visite préalable',
                  subtitle: visite.documentSourceNumero ?? visite.visiteurNomComplet,
                  onAdd: () => _pickAndUpload(context, ref, visiteId: visite.id),
                ),
                if (visite.photos.isEmpty)
                  const Padding(
                    padding: EdgeInsets.symmetric(horizontal: 16),
                    child: Text('Aucune photo pour cette visite.',
                        style: TextStyle(fontSize: 13, color: OcpColors.slate),),
                  )
                else
                  _PhotoGrid(
                    photos: visite.photos,
                    onDelete: (photoId) => _delete(
                      context,
                      ref,
                      visiteId: visite.id,
                      photoId: photoId,
                    ),
                  ),
              ],
              if (g.reception != null) ...[
                _SectionHeader(
                  icon: Icons.fact_check_outlined,
                  title: 'Réception des travaux',
                  subtitle: null,
                  onAdd: () => _pickAndUpload(context, ref, receptionId: g.reception!.id),
                ),
                if (g.receptionPhotos.isEmpty)
                  const Padding(
                    padding: EdgeInsets.symmetric(horizontal: 16),
                    child: Text('Aucune photo pour la réception.',
                        style: TextStyle(fontSize: 13, color: OcpColors.slate),),
                  )
                else
                  _PhotoGrid(
                    photos: g.receptionPhotos,
                    onDelete: (photoId) => _delete(
                      context,
                      ref,
                      receptionId: g.reception!.id,
                      photoId: photoId,
                    ),
                  ),
              ],
            ],
          );
        },
      ),
    );
  }

  Future<void> _pickAndUpload(
    BuildContext context,
    WidgetRef ref, {
    String? visiteId,
    String? receptionId,
  }) async {
    final picker = ref.read(filePickServiceProvider);
    final source = await showModalBottomSheet<ImageSourceChoice>(
      context: context,
      builder: (_) => SafeArea(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            ListTile(
              leading: const Icon(Icons.photo_camera_rounded),
              title: const Text('Prendre une photo'),
              onTap: () => Navigator.pop(context, ImageSourceChoice.camera),
            ),
            ListTile(
              leading: const Icon(Icons.photo_library_rounded),
              title: const Text('Choisir dans la galerie'),
              onTap: () => Navigator.pop(context, ImageSourceChoice.gallery),
            ),
          ],
        ),
      ),
    );
    if (source == null || !context.mounted) return;

    try {
      final file = source == ImageSourceChoice.camera
          ? await picker.prendrePhotoCamera()
          : await picker.choisirPhotoGalerie();
      if (file == null) return;

      final legende = await _askLegende(context);
      final api = ref.read(photoApiProvider);
      if (visiteId != null) {
        await api.addVisitePhoto(visiteId,
            bytes: file.bytes, filename: file.filename, mimeType: file.mimeType, legende: legende,);
      } else if (receptionId != null) {
        await api.addReceptionPhoto(receptionId,
            bytes: file.bytes, filename: file.filename, mimeType: file.mimeType, legende: legende,);
      }
      ref.invalidate(atPhotosProvider(atId));
    } catch (e) {
      if (context.mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text(e is Failure ? e.message : mapDioError(e).message)),
        );
      }
    }
  }

  Future<String?> _askLegende(BuildContext context) async {
    final controller = TextEditingController();
    return showDialog<String>(
      context: context,
      builder: (dialogContext) => AlertDialog(
        title: const Text('Légende (optionnel)'),
        content: TextField(
          controller: controller,
          decoration: const InputDecoration(hintText: 'Ex. : vanne avant dépose'),
        ),
        actions: [
          TextButton(onPressed: () => Navigator.pop(dialogContext), child: const Text('Passer')),
          FilledButton(
              onPressed: () => Navigator.pop(dialogContext, controller.text),
              child: const Text('Ajouter'),),
        ],
      ),
    );
  }

  Future<void> _delete(
    BuildContext context,
    WidgetRef ref, {
    required String photoId,
    String? visiteId,
    String? receptionId,
  }) async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (dialogContext) => AlertDialog(
        title: const Text('Supprimer cette photo ?'),
        actions: [
          TextButton(onPressed: () => Navigator.pop(dialogContext, false), child: const Text('Annuler')),
          FilledButton(onPressed: () => Navigator.pop(dialogContext, true), child: const Text('Supprimer')),
        ],
      ),
    );
    if (confirmed != true) return;

    try {
      final api = ref.read(photoApiProvider);
      if (visiteId != null) {
        await api.deleteVisitePhoto(visiteId, photoId);
      } else if (receptionId != null) {
        await api.deleteReceptionPhoto(receptionId, photoId);
      }
      ref.invalidate(atPhotosProvider(atId));
    } catch (e) {
      if (context.mounted) {
        ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(mapDioError(e).message)));
      }
    }
  }
}

enum ImageSourceChoice { camera, gallery }

class _SectionHeader extends StatelessWidget {
  final IconData icon;
  final String title;
  final String? subtitle;
  final VoidCallback? onAdd;

  const _SectionHeader({required this.icon, required this.title, this.subtitle, this.onAdd});

  @override
  Widget build(BuildContext context) => Padding(
        padding: const EdgeInsets.fromLTRB(20, 16, 20, 8),
        child: Row(
          children: [
            Icon(icon, size: 18, color: OcpColors.forest),
            const SizedBox(width: 8),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(title,
                      style: const TextStyle(
                          fontFamily: 'SpaceGrotesk', fontWeight: FontWeight.w700, fontSize: 15,),),
                  if (subtitle != null)
                    Text(subtitle!, style: const TextStyle(fontSize: 12, color: OcpColors.slate)),
                ],
              ),
            ),
            if (onAdd != null)
              IconButton(
                icon: const Icon(Icons.add_a_photo_outlined),
                tooltip: 'Ajouter une photo',
                onPressed: onAdd,
              ),
          ],
        ),
      );
}

class _PhotoGrid extends StatelessWidget {
  final List<PhotoRef> photos;
  final void Function(String photoId) onDelete;

  const _PhotoGrid({required this.photos, required this.onDelete});

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16),
      child: GridView.builder(
        shrinkWrap: true,
        physics: const NeverScrollableScrollPhysics(),
        gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
          crossAxisCount: 3,
          crossAxisSpacing: 8,
          mainAxisSpacing: 8,
        ),
        itemCount: photos.length,
        itemBuilder: (context, index) {
          final photo = photos[index];
          return Stack(
            fit: StackFit.expand,
            children: [
              Container(
                decoration: BoxDecoration(
                  color: OcpColors.sage,
                  borderRadius: BorderRadius.circular(10),
                  border: Border.all(color: OcpColors.border),
                ),
                child: ClipRRect(
                  borderRadius: BorderRadius.circular(10),
                  child: Stack(
                    alignment: Alignment.bottomLeft,
                    children: [
                      // Métadonnées seules disponibles via l'API (pas d'URL publique
                      // de téléchargement des photos de visite) :
                      const Icon(Icons.image_outlined, size: 32, color: OcpColors.slate),
                      if ((photo.legende ?? photo.nom ?? '').isNotEmpty)
                        Container(
                          color: OcpColors.deep.withValues(alpha: 0.7),
                          padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                          child: Text(
                            photo.legende ?? photo.nom!,
                            maxLines: 1,
                            overflow: TextOverflow.ellipsis,
                            style: const TextStyle(color: OcpColors.white, fontSize: 10),
                          ),
                        ),
                    ],
                  ),
                ),
              ),
              Positioned(
                top: 4,
                right: 4,
                child: GestureDetector(
                  onTap: () => onDelete(photo.id),
                  child: Container(
                    padding: const EdgeInsets.all(4),
                    decoration: const BoxDecoration(
                        color: OcpColors.error, shape: BoxShape.circle,),
                    child: const Icon(Icons.close_rounded, size: 12, color: OcpColors.white),
                  ),
                ),
              ),
              Positioned(
                bottom: 4,
                right: 4,
                child: Text(
                  AppDate.date(photo.dateCreation),
                  style: const TextStyle(fontSize: 9, color: OcpColors.white),
                ),
              ),
            ],
          );
        },
      ),
    );
  }
}
