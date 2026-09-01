/// Dashboard mobile — GET /api/dashboard/stats (KPIs réels) + accès rapides
/// selon les permissions de l'utilisateur. Aucune donnée fictive.
library;

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../../core/errors/failures.dart';
import '../../auth/presentation/auth_controller.dart';
import 'dashboard_providers.dart';

class DashboardPage extends ConsumerWidget {
  const DashboardPage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final session = ref.watch(sessionProvider);
    final stats = ref.watch(dashboardProvider);
    final unreadCount = ref.watch(unreadCountProvider);
    final hasPermission = ref.watch(hasPermissionProvider);

    return Scaffold(
      appBar: AppBar(
        title: const Text('Tableau de bord'),
        actions: [
          IconButton(
            tooltip: 'Assistant IA HSE',
            onPressed: () => context.push('/assistant'),
            icon: const Icon(Icons.psychology_alt_rounded, color: Color(0xFF7FC8A9)),
          ),
          IconButton(
            onPressed: () => context.push('/notifications'),
            icon: Badge(
              isLabelVisible: unreadCount > 0,
              label: Text('$unreadCount'),
              child: const Icon(Icons.notifications_outlined),
            ),
          ),
          IconButton(
            onPressed: () => context.push('/profile'),
            icon: const Icon(Icons.person_outline_rounded),
          ),
        ],
      ),
      body: RefreshIndicator(
        onRefresh: () async {
          ref.invalidate(dashboardProvider);
          ref.read(unreadCountProvider.notifier).refresh();
          await ref.read(dashboardProvider.future);
        },
        child: ListView(
          physics: const AlwaysScrollableScrollPhysics(),
          padding: const EdgeInsets.only(bottom: 24),
          children: [
            // --- Bandeau utilisateur ---
            Padding(
              padding: const EdgeInsets.fromLTRB(16, 12, 16, 0),
              child: Card(
                child: Padding(
                  padding: const EdgeInsets.all(16),
                  child: Row(
                    children: [
                      CircleAvatar(
                        radius: 26,
                        backgroundColor: Theme.of(context).colorScheme.primaryContainer,
                        child: Text(
                          _initials(session?.utilisateur.nomComplet ?? '?'),
                          style: const TextStyle(fontWeight: FontWeight.w700, fontSize: 18),
                        ),
                      ),
                      const SizedBox(width: 14),
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              session?.utilisateur.nomComplet ?? '—',
                              style: const TextStyle(
                                fontFamily: 'SpaceGrotesk',
                                fontWeight: FontWeight.w700,
                                fontSize: 16,
                              ),
                            ),
                            const SizedBox(height: 2),
                            Text(
                              (session?.roles ?? []).join(' · '),
                              style: TextStyle(
                                fontSize: 12,
                                color: Theme.of(context).colorScheme.onSurfaceVariant,
                              ),
                            ),
                          ],
                        ),
                      ),
                    ],
                  ),
                ),
              ),
            ),

            // --- KPIs ---
            stats.when(
              loading: () => const Padding(
                padding: EdgeInsets.all(32),
                child: Center(child: CircularProgressIndicator()),
              ),
              error: (e, _) => Padding(
                padding: const EdgeInsets.all(16),
                child: Card(
                  child: Padding(
                    padding: const EdgeInsets.all(16),
                    child: Text(
                      e is Failure ? e.message : 'Statistiques indisponibles.',
                      style: const TextStyle(fontSize: 13),
                    ),
                  ),
                ),
              ),
              data: (data) => Padding(
                padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                child: Column(
                  children: [
                    Row(
                      children: [
                        Expanded(
                          child: _kpiCard(
                            context,
                            icon: Icons.engineering_rounded,
                            label: 'AT en cours',
                            value: data.kpis.autorisationsEnCours,
                          ),
                        ),
                        Expanded(
                          child: _kpiCard(
                            context,
                            icon: Icons.draw_rounded,
                            label: 'Visas en attente',
                            value: data.kpis.visasEnAttente,
                          ),
                        ),
                      ],
                    ),
                    Row(
                      children: [
                        Expanded(
                          child: _kpiCard(
                            context,
                            icon: Icons.badge_outlined,
                            label: 'Permis actifs',
                            value: data.kpis.permisActifs,
                          ),
                        ),
                        Expanded(
                          child: _kpiCard(
                            context,
                            icon: Icons.inventory_2_outlined,
                            label: 'Archives',
                            value: data.kpis.totalArchives,
                          ),
                        ),
                      ],
                    ),
                  ],
                ),
              ),
            ),

            // --- Accès rapides ---
            const _SectionTitle('Accès rapides'),
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: [
                  _quickAction(
                    context,
                    icon: Icons.list_alt_rounded,
                    label: 'Mes AT',
                    onTap: () => context.go('/at?filter=mine'),
                  ),
                  const SizedBox(height: 8),
                  _quickAction(
                    context,
                    icon: Icons.fact_check_outlined,
                    label: 'AT à valider',
                    onTap: () => context.go('/at?filter=aValider'),
                  ),
                  const SizedBox(height: 8),
                  _quickAction(
                    context,
                    icon: Icons.notifications_outlined,
                    label: 'Notifications',
                    badge: unreadCount,
                    onTap: () => context.go('/notifications'),
                  ),
                  const SizedBox(height: 8),
                  _quickAction(
                    context,
                    icon: Icons.psychology_alt_rounded,
                    label: 'Assistant IA HSE (RAG)',
                    onTap: () => context.push('/assistant'),
                  ),
                  if (hasPermission('CREATE_AT')) ...[
                    const SizedBox(height: 8),
                    _quickAction(
                      context,
                      icon: Icons.add_circle_outline_rounded,
                      label: 'Créer une AT',
                      onTap: () => context.push('/at/nouvelle'),
                    ),
                  ],
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  String _initials(String name) {
    final parts = name.trim().split(RegExp(r'\s+'));
    if (parts.isEmpty) return '?';
    if (parts.length == 1) return parts.first.substring(0, 1).toUpperCase();
    return (parts.first.substring(0, 1) + parts.last.substring(0, 1)).toUpperCase();
  }

  Widget _kpiCard(BuildContext context,
      {required IconData icon, required String label, required int value,}) {
    return Padding(
      padding: const EdgeInsets.all(8),
      child: Card(
        child: Padding(
          padding: const EdgeInsets.all(14),
          child: Row(
            children: [
              Icon(icon, color: Theme.of(context).colorScheme.primary, size: 26),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      '$value',
                      style: const TextStyle(
                        fontFamily: 'SpaceGrotesk',
                        fontWeight: FontWeight.w800,
                        fontSize: 20,
                      ),
                    ),
                    Text(
                      label,
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                      style: TextStyle(
                        fontSize: 12,
                        color: Theme.of(context).colorScheme.onSurfaceVariant,
                      ),
                    ),
                  ],
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _quickAction(
    BuildContext context, {
    required IconData icon,
    required String label,
    int badge = 0,
    required VoidCallback onTap,
  }) {
    return Card(
      child: ListTile(
        leading: Icon(icon, color: Theme.of(context).colorScheme.primary),
        title: Text(label, style: const TextStyle(fontWeight: FontWeight.w600)),
        trailing: badge > 0
            ? Badge(label: Text('$badge'))
            : const Icon(Icons.chevron_right_rounded),
        onTap: onTap,
      ),
    );
  }
}

class _SectionTitle extends StatelessWidget {
  final String text;
  const _SectionTitle(this.text);

  @override
  Widget build(BuildContext context) => Padding(
        padding: const EdgeInsets.fromLTRB(20, 20, 20, 8),
        child: Text(
          text,
          style: const TextStyle(
            fontFamily: 'SpaceGrotesk',
            fontWeight: FontWeight.w700,
            fontSize: 15,
          ),
        ),
      );
}
