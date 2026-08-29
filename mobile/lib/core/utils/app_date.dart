/// Formateurs de dates/heures (intl) — cohérents avec l'affichage web.
library;

import 'package:intl/intl.dart';

final class AppDate {
  static String date(DateTime? d) => d == null ? '—' : DateFormat('dd/MM/yyyy').format(d);

  static String dateHeure(DateTime? d) =>
      d == null ? '—' : DateFormat('dd/MM/yyyy HH:mm').format(d);

  static String heure(DateTime? d) => d == null ? '—' : DateFormat('HH:mm').format(d);

  /// "HH:mm:ss" (LocalTime backend) → "HH:mm".
  static String heureSimple(String? localTime) {
    if (localTime == null || localTime.isEmpty) return '—';
    final parts = localTime.split(':');
    if (parts.length < 2) return localTime;
    return '${parts[0]}:${parts[1]}';
  }

  static String relative(DateTime? d) {
    if (d == null) return '—';
    final diff = DateTime.now().difference(d);
    if (diff.inMinutes < 1) return 'À l\'instant';
    if (diff.inMinutes < 60) return 'Il y a ${diff.inMinutes} min';
    if (diff.inHours < 24) return 'Il y a ${diff.inHours} h';
    if (diff.inDays < 7) return 'Il y a ${diff.inDays} j';
    return date(d);
  }
}
