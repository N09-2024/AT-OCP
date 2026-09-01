/// Routeur principal (GoRouter) — arborescence :
///
/// /login                         → LoginPage (publique)
/// /  (shell avec bottom nav)
///   ├── /                       → DashboardPage
///   ├── /at                     → AtListPage
///   ├── /notifications          → NotificationsPage
///   └── /profile                → ProfilePage
///   + routes overlay (sans bottom nav) :
///   ├── /at/:id                 → AtDetailPage
///   ├── /at/:id/visas           → AtVisasPage
///   ├── /at/:id/historique      → AtHistoriquePage
///   └── /at/:id/pdf             → AtPdfPage
///
/// Le redirect se ré-évalue à chaque changement d'état d'authentification
/// (refreshListenable) SANS recréer le routeur.
library;

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../core/widgets/app_shell.dart';
import '../features/at/presentation/at_create_page.dart';
import '../features/at/presentation/at_detail_page.dart';
import '../features/at/presentation/at_form_page.dart';
import '../features/at/presentation/at_historique_page.dart';
import '../features/at/presentation/at_list_page.dart';
import '../features/at/presentation/at_pdf_page.dart';
import '../features/at/presentation/at_reception_page.dart';
import '../features/at/presentation/at_visas_page.dart';
import '../features/assistant/presentation/assistant_page.dart';
import '../features/auth/presentation/auth_controller.dart';
import '../features/auth/presentation/login_page.dart';
import '../features/dashboard/presentation/dashboard_page.dart';
import '../features/notifications/presentation/notifications_page.dart';
import '../features/permis/presentation/at_permis_page.dart';
import '../features/photos/presentation/at_photos_page.dart';
import '../features/profile/presentation/profile_page.dart';

final _rootNavigatorKey = GlobalKey<NavigatorState>();

/// Notifie GoRouter quand l'état d'auth change (login/logout/expiration).
class _AuthRefreshListenable extends ChangeNotifier {
  _AuthRefreshListenable(Ref ref) {
    ref.listen<AuthState>(authControllerProvider, (_, _) => notifyListeners());
  }
}

final goRouterProvider = Provider<GoRouter>((ref) {
  final refresh = _AuthRefreshListenable(ref);

  return GoRouter(
    navigatorKey: _rootNavigatorKey,
    initialLocation: '/login',
    refreshListenable: refresh,
    redirect: (context, state) {
      final authState = ref.read(authControllerProvider);
      final isLoggedIn = authState is AuthAuthenticated;
      final isLoggingIn = authState is AuthLoading;
      final isAuthRoute = state.matchedLocation == '/login';

      // Pendant le login (spinner) on ne redirige pas.
      if (isLoggingIn) return null;
      if (!isLoggedIn && !isAuthRoute) return '/login';
      if (isLoggedIn && isAuthRoute) return '/';
      return null;
    },
    routes: [
      // --- Route publique : login ---
      GoRoute(
        path: '/login',
        builder: (context, state) => const LoginPage(),
      ),

      // --- Shell : bottom nav ---
      StatefulShellRoute.indexedStack(
        builder: (context, state, navigationShell) =>
            AppShell(navigationShell: navigationShell),
        branches: [
          StatefulShellBranch(
            routes: [
              GoRoute(
                path: '/',
                builder: (context, state) => const DashboardPage(),
              ),
            ],
          ),
          StatefulShellBranch(
            routes: [
              GoRoute(
                path: '/at',
                builder: (context, state) => AtListPage(
                  initialFilter: state.uri.queryParameters['filter'],
                ),
              ),
            ],
          ),
          StatefulShellBranch(
            routes: [
              GoRoute(
                path: '/notifications',
                builder: (context, state) => const NotificationsPage(),
              ),
            ],
          ),
          StatefulShellBranch(
            routes: [
              GoRoute(
                path: '/profile',
                builder: (context, state) => const ProfilePage(),
              ),
            ],
          ),
        ],
      ),

      // --- Routes overlay (sans shell) ---
      GoRoute(
        path: '/at/nouvelle',
        parentNavigatorKey: _rootNavigatorKey,
        builder: (context, state) => const AtCreatePage(),
      ),
      GoRoute(
        path: '/at/:id',
        parentNavigatorKey: _rootNavigatorKey,
        builder: (context, state) =>
            AtDetailPage(atId: state.pathParameters['id']!),
      ),
      GoRoute(
        path: '/at/:id/edit',
        parentNavigatorKey: _rootNavigatorKey,
        builder: (context, state) =>
            AtFormPage(atId: state.pathParameters['id']!),
      ),
      GoRoute(
        path: '/at/:id/photos',
        parentNavigatorKey: _rootNavigatorKey,
        builder: (context, state) =>
            AtPhotosPage(atId: state.pathParameters['id']!),
      ),
      GoRoute(
        path: '/at/:id/permis',
        parentNavigatorKey: _rootNavigatorKey,
        builder: (context, state) =>
            AtPermisPage(atId: state.pathParameters['id']!),
      ),
      GoRoute(
        path: '/at/:id/visas',
        parentNavigatorKey: _rootNavigatorKey,
        builder: (context, state) =>
            AtVisasPage(atId: state.pathParameters['id']!),
      ),
      GoRoute(
        path: '/at/:id/reception',
        parentNavigatorKey: _rootNavigatorKey,
        builder: (context, state) =>
            AtReceptionPage(atId: state.pathParameters['id']!),
      ),
      GoRoute(
        path: '/at/:id/historique',
        parentNavigatorKey: _rootNavigatorKey,
        builder: (context, state) =>
            AtHistoriquePage(atId: state.pathParameters['id']!),
      ),
      GoRoute(
        path: '/at/:id/pdf',
        parentNavigatorKey: _rootNavigatorKey,
        builder: (context, state) =>
            AtPdfPage(atId: state.pathParameters['id']!),
      ),
      GoRoute(
        path: '/assistant',
        parentNavigatorKey: _rootNavigatorKey,
        builder: (context, state) {
          final extra = state.extra;
          return AssistantPage(
            atContext: extra is Map<String, dynamic> ? extra : null,
          );
        },
      ),
    ],
  );
});
