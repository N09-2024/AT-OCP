/// Notifications — GET /api/notifications (paginé), PUT /{id}/read, PUT /read-all.
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
import '../../dashboard/presentation/dashboard_providers.dart';

/// Extrait une route GoRouter du champ `lien` backend (ex. "/at/abc123",
/// "at/abc123", "/autorisations/abc123" ou une URL web avec fragment "#/at/abc123").
/// Retourne null si non exploitable ou hors périmètre mobile.
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

  // Nettoyage : query string + slash initial.
  final queryIndex = l.indexOf('?');
  if (queryIndex >= 0) l = l.substring(0, queryIndex);
  if (!l.startsWith('/')) l = '/$l';

  // Seules les routes connues de l'app mobile sont suivies.
  final lower = l.toLowerCase();
  if (lower.startsWith('/autorisations/')) {
    final id = l.substring('/autorisations/'.length).split('/').first;
    return id.isNotEmpty ? '/at/$id' : null;
  }
  if (lower.startsWith('/at/') || lower == '/notifications' || lower.startsWith('/permis/')) {
    return l;
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
                      onTap: () async {
                        if (!n.lu) {
                          await api.markAsRead(n.id);
                          ref.invalidate(notificationsProvider);
                          ref.read(unreadCountProvider.notifier).decrement();
                        }
                        // Navigation via le champ lien si exploitable.
                        final route = routeFromLien(n.lien);
                        if (route != null && context.mounted) {
                          context.push(route);
                        }
                      },
                    );
                  },
                ),
              ),
      ),
    );
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
