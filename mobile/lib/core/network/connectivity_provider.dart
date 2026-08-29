/// Connectivité — détection de la perte de réseau (spec §35, phase 1 :
/// message global + reprise après reconnexion, PAS de synchronisation offline).
/// Les données en cours de saisie sont de toute façon conservées par
/// AtFormNotifier (état mémoire + flush à la reconnexion via retry).
library;

import 'dart:async';

import 'package:connectivity_plus/connectivity_plus.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

enum ConnectivityStatus { online, offline }

final connectivityProvider =
    StateNotifierProvider<ConnectivityNotifier, ConnectivityStatus>((ref) {
  return ConnectivityNotifier(Connectivity());
});

class ConnectivityNotifier extends StateNotifier<ConnectivityStatus> {
  final Connectivity _connectivity;
  StreamSubscription<List<ConnectivityResult>>? _subscription;

  ConnectivityNotifier(this._connectivity) : super(ConnectivityStatus.online) {
    // État initial puis écoute continue.
    _connectivity.checkConnectivity().then((result) => _apply(result));
    _subscription = _connectivity.onConnectivityChanged.listen(_apply);
  }

  void _apply(List<ConnectivityResult> results) {
    final none = results.contains(ConnectivityResult.none) || results.isEmpty;
    state = none ? ConnectivityStatus.offline : ConnectivityStatus.online;
  }

  @override
  void dispose() {
    _subscription?.cancel();
    super.dispose();
  }
}
