/// Notifications - GET /api/notifications (paginé), PUT /{id}/read, PUT /read-all.
/// Affichage lues/non lues, marquage individuel et global.
library;

import 'package:flutter/material.dart' hide Notification;
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../../core/errors/failures.dart';
import '../../../core/theme/app_colors.dart';
import '../../../core/utils/app_date.dart';
import '../../../core/widgets/states.dart';
import '../data/notification.dart';
import '../notification_api.dart';
import '../../dashboard/presentation/dashboard_providers.dart';

/// Traduit le champ `lien` backend en route mobile réelle, comme le web route
/// ses notifications. Liens émis côté serveur et leur équivalent mobile :
///   /autorisations/{id}                      → /at/{id}
///   /autorisations/{id}/editer?mode=viser    → /at/{id}/visas  (visa CEEE)
///   /at/{id}/signature-ceee                  → /at/{id}/visas
///   /visas/validation/{id}?role=…            → /at/{id}/visas  (visa HC/HM)
///   /receptions?atId={id}                    → /at/{id}/reception
/// Retourne null si le lien n'a pas d'équivalent mobile (jamais de push cassé).
String? routeFromLien(String? lien) {
  if (lien == null || lien.trim().isEmpty) return null;
  var l = lien.trim();

  // Fragment SPA d'abord ("https://host/#/at/xxx" → "/at/xxx").
  final hashIndex = l.indexOf('#');
  if (hashIndex >= 0) l = l.substring(hashIndex + 1);

  // URL absolue sans fragment : garder le path.
  if (l.toLowerCase().startsWith('http')) {
    final uri = Uri.tryParse(l);
    if (uri == null) return null;
    l = uri.path;
  }

  final uri = Uri.tryParse(l);
  if (uri == null) return null;
  final path = uri.path;
  final lower = path.toLowerCase();
  final query = uri.queryParameters;

  String premierSegment(String prefixe) {
    final reste = path.substring(prefixe.length);
    final id = reste.split('/').first;
    return id;
  }

  // Détail / édition / visa d'une AT web.
  if (lower.startsWith('/autorisations/')) {
    final id = premierSegment('/autorisations/');
    if (id.isEmpty) return null;
    return lower.contains('/editer') || query.containsKey('mode')
        ? '/at/$id/visas'
        : '/at/$id';
  }
  if (lower.startsWith('/at/')) {
    final id = premierSegment('/at/');
    if (id.isEmpty) return null;
    return '/at/$id/visas';
  }
  if (lower.startsWith('/visas/validation/')) {
    final id = premierSegment('/visas/validation/');
    return id.isNotEmpty ? '/at/$id/visas' : null;
  }
  // Réception conjointe : /receptions?atId=xxx.
  if (lower.startsWith('/receptions')) {
    final atId = query['atId'];
    return (atId != null && atId.isNotEmpty) ? '/at/$atId/reception' : null;
  }
  return null;
}

class NotificationsPage extends ConsumerWidget {
  const NotificationsPage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final notifs = ref.watch(notificationsProvider);
    final unreadCount = ref.watch(unreadCountProvider);
    final api = ref.watch(notificationApiProvider);

    return Scaffold(
      appBar: AppBar(
        title: const Text('Notifications'),
        actions: [
          if (unreadCount > 0)
            TextButton(
              onPressed: () async {
                await api.markAllAsRead();
                ref.invalidate(notificationsProvider);
                ref.read(unreadCountProvider.notifier).clear();
              },
              child: const Text('Tout lire'),
            ),
        ],
      ),
      body: notifs.when(
        loading: () => const LoadingState(message: 'Chargement des notifications...'),
        error: (e, _) => ErrorState(
          message: e is Failure ? e.message : 'Erreur de chargement.',
          onRetry: () => ref.invalidate(notificationsProvider),
        ),
        data: (items) => items.isEmpty
            ? const EmptyState(
                message: 'Aucune notification.',
                icon: Icons.notifications_off_outlined,
              )
            : RefreshIndicator(
                onRefresh: () async {
                  ref.invalidate(notificationsProvider);
                  ref.read(unreadCountProvider.notifier).refresh();
                  await ref.read(notificationsProvider.future);
                },
                child: ListView.separated(
                  physics: const AlwaysScrollableScrollPhysics(),
                  itemCount: items.length,
                  separatorBuilder: (_, _) => const Divider(height: 1),
                  itemBuilder: (context, index) {
                    final n = items[index];
                    return _NotificationTile(
                      notification: n,
                      onTap: () => _ouvrirNotification(context, ref, api, n),
                    );
                  },
                ),
              ),
      ),
    );
  }

  /// Ouverture d'une notification : marquage lu (non bloquant) puis navigation
  /// vers la route mobile correspondant au champ `lien`. Toute erreur réseau ou
  /// de navigation est interceptée et affichée proprement (jamais de crash).
  Future<void> _ouvrirNotification(
    BuildContext context,
    WidgetRef ref,
    NotificationApi api,
    Notification n,
  ) async {
    if (!n.lu) {
      try {
        await api.markAsRead(n.id);
        ref.invalidate(notificationsProvider);
        ref.read(unreadCountProvider.notifier).decrement();
      } catch (_) {
        // Le marquage lu ne doit jamais empêcher l'ouverture : on continue.
      }
    }

    final route = routeFromLien(n.lien);
    if (route == null || !context.mounted) return;
    try {
      await context.push(route);
    } catch (_) {
      if (!context.mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          backgroundColor: OcpColors.errorSoft,
          content: Text('Impossible d\'ouvrir la cible : ${n.titre ?? 'notification'}.'),
        ),
      );
    }
  }
}

class _NotificationTile extends StatelessWidget {
  final Notification notification;
  final VoidCallback onTap;

  const _NotificationTile({required this.notification, required this.onTap});

  @override
  Widget build(BuildContext context) {
    return ListTile(
      onTap: onTap,
      contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 4),
      leading: CircleAvatar(
        backgroundColor: notification.lu ? OcpColors.sage : OcpColors.forestSoft,
        child: Icon(
          _iconForType(notification.type),
          size: 20,
          color: notification.lu ? OcpColors.slate : OcpColors.forest,
        ),
      ),
      title: Text(
        notification.titre ?? 'Notification',
        style: TextStyle(
          fontWeight: notification.lu ? FontWeight.w500 : FontWeight.w700,
          fontSize: 14,
        ),
      ),
      subtitle: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          if ((notification.message ?? '').isNotEmpty)
            Text(notification.message!, maxLines: 2, overflow: TextOverflow.ellipsis),
          const SizedBox(height: 2),
          Text(
            AppDate.relative(notification.dateCreation),
            style: const TextStyle(fontSize: 11, color: OcpColors.slate),
          ),
        ],
      ),
      trailing: notification.lu ? null : const Badge(),
    );
  }

  IconData _iconForType(String? type) {
    switch (type?.toUpperCase()) {
      case 'VISA':
      case 'SIGNATURE':
        return Icons.draw_rounded;
      case 'VALIDATION':
        return Icons.fact_check_outlined;
      case 'RECONDUCTION':
        return Icons.update_rounded;
      case 'ARCHIVE':
        return Icons.inventory_2_outlined;
      default:
        return Icons.notifications_outlined;
    }
  }
}
