/// Providers du dashboard - GET /api/dashboard/stats
/// et compteur notifications non lues (GET /api/notifications/count-unread).
library;

import 'dart:async';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/errors/error_mapper.dart';
import '../../../core/network/api_providers.dart';
import '../../notifications/data/notification.dart';
import '../../notifications/notification_api.dart';
import '../data/models/dashboard_data.dart';

final notificationApiProvider =
    Provider<NotificationApi>((ref) => NotificationApi(ref.watch(apiClientProvider)));

final dashboardApiProvider =
    Provider<DashboardApi>((ref) => DashboardApi(ref.watch(apiClientProvider)));

final dashboardProvider = FutureProvider.autoDispose<DashboardData>((ref) async {
  final api = ref.watch(dashboardApiProvider);
  try {
    return await api.stats();
  } catch (e) {
    throw mapDioError(e);
  }
});

/// Compteur de notifications non lues - rafraîchi périodiquement (polling,
/// pas de temps réel côté backend).
final unreadCountProvider = StateNotifierProvider<UnreadCountNotifier, int>((ref) {
  return UnreadCountNotifier(ref.watch(notificationApiProvider));
});

class UnreadCountNotifier extends StateNotifier<int> {
  final NotificationApi _api;
  Timer? _timer;

  UnreadCountNotifier(this._api) : super(0) {
    refresh();
    _timer = Timer.periodic(const Duration(seconds: 30), (_) => refresh());
  }

  Future<void> refresh() async {
    try {
      state = await _api.countUnread();
    } catch (_) {/* compteur conservé si le réseau échoue */}
  }

  void decrement() {
    if (state > 0) state = state - 1;
  }

  void clear() => state = 0;

  @override
  void dispose() {
    _timer?.cancel();
    super.dispose();
  }
}

/// Liste des notifications de l'utilisateur connecté.
final notificationsProvider =
    FutureProvider.autoDispose<List<Notification>>((ref) async {
  final api = ref.watch(notificationApiProvider);
  try {
    return await api.findAll(size: 50);
  } catch (e) {
    throw mapDioError(e);
  }
});
