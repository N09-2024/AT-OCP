/// API notifications + dashboard — endpoints réels du backend :
///   GET  /notifications?page=&size=          (Page)
///   GET  /notifications/count-unread         → {count}
///   PUT  /notifications/{id}/read
///   PUT  /notifications/read-all
///   GET  /dashboard/stats
library;

import '../../../core/network/api_client.dart';
import 'data/notification.dart';
import '../dashboard/data/models/dashboard_data.dart';
import '../at/data/models/autorisation_travail.dart' show Page;

class NotificationApi {
  final ApiClient _client;
  NotificationApi(this._client);

  Future<List<Notification>> findAll({int page = 0, int size = 20}) async {
    final response = await _client.get<Map<String, dynamic>>(
      '/notifications',
      queryParameters: {'page': page, 'size': size},
    );
    final pageData = Page.fromJson(response.data!, Notification.fromJson);
    return pageData.content;
  }

  Future<int> countUnread() async {
    final response = await _client.get<Map<String, dynamic>>('/notifications/count-unread');
    final count = response.data?['count'];
    return count is int ? count : int.tryParse(count.toString()) ?? 0;
  }

  Future<void> markAsRead(String id) =>
      _client.put<void>('/notifications/$id/read');

  Future<void> markAllAsRead() => _client.put<void>('/notifications/read-all');
}

class DashboardApi {
  final ApiClient _client;
  DashboardApi(this._client);

  Future<DashboardData> stats() async {
    final response = await _client.get<Map<String, dynamic>>('/dashboard/stats');
    return DashboardData.fromJson(response.data!);
  }
}
