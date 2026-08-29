/// Providers Riverpod du module AT :
/// - atListProvider : liste paginée avec recherche + filtre statut
///   (GET /autorisations-travail?statut=&search=&page=&size=).
/// - atDetailProvider : détail par id.
library;

import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/errors/error_mapper.dart';
import '../../../core/errors/failures.dart';
import '../../../core/network/api_providers.dart';
import '../data/at_api.dart';
import '../data/models/autorisation_travail.dart';

final atApiProvider = Provider<AtApi>((ref) => AtApi(ref.watch(apiClientProvider)));

/// État de la liste AT.
sealed class AtListState {
  const AtListState();
}

class AtListLoading extends AtListState {
  const AtListLoading();
}

class AtListLoaded extends AtListState {
  final List<AutorisationTravail> items;
  final int totalElements;
  final bool loadingMore;
  final bool hasMore;

  const AtListLoaded(
    this.items, {
    required this.totalElements,
    required this.hasMore,
    this.loadingMore = false,
  });
}

class AtListError extends AtListState {
  final Failure failure;
  const AtListError(this.failure);
}

enum AtFilterScope { all, mine, aValider }

/// Notifier de la liste : recherche (debounce géré par l'UI), filtre statut,
/// filtre portée (Toutes, Mes AT, À valider), pagination par append, refresh pull-to-refresh.
class AtListNotifier extends StateNotifier<AtListState> {
  final AtApi _api;

  static const int pageSize = 20;

  String? _statut;
  String _search = '';
  AtFilterScope _scope = AtFilterScope.all;
  int _currentPage = 0;

  AtListNotifier(this._api) : super(const AtListLoading()) {
    reload();
  }

  Future<void> reload() async {
    _currentPage = 0;
    state = const AtListLoading();
    try {
      final page = await _api.findAll(AtListQuery(
        statut: _statut,
        search: _search,
        mine: _scope == AtFilterScope.mine ? true : null,
        aValider: _scope == AtFilterScope.aValider ? true : null,
        page: 0,
        size: pageSize,
      ),);
      state = AtListLoaded(
        page.content,
        totalElements: page.totalElements,
        hasMore: !page.last,
      );
    } catch (e) {
      state = AtListError(mapDioError(e));
    }
  }

  Future<void> loadMore() async {
    if (state is! AtListLoaded) return;
    final loaded = state as AtListLoaded;
    if (!loaded.hasMore || loaded.loadingMore) return;

    state = AtListLoaded(
      loaded.items,
      totalElements: loaded.totalElements,
      hasMore: loaded.hasMore,
      loadingMore: true,
    );

    try {
      final page = await _api.findAll(AtListQuery(
        statut: _statut,
        search: _search,
        mine: _scope == AtFilterScope.mine ? true : null,
        aValider: _scope == AtFilterScope.aValider ? true : null,
        page: _currentPage + 1,
        size: pageSize,
      ),);
      _currentPage += 1;
      state = AtListLoaded(
        [...loaded.items, ...page.content],
        totalElements: page.totalElements,
        hasMore: !page.last,
      );
    } catch (_) {
      // Échec du load-more : on garde l'état courant sans écraser la liste.
      state = loaded;
    }
  }

  void setScope(AtFilterScope scope) {
    if (_scope == scope) return;
    _scope = scope;
    reload();
  }

  void setStatut(String? statut) {
    if (_statut == statut) return;
    _statut = statut;
    reload();
  }

  void setSearch(String search) {
    final normalized = search.trim();
    if (_search == normalized) return;
    _search = normalized;
    reload();
  }

  AtFilterScope get scope => _scope;
  String? get statut => _statut;
  String get search => _search;
}

final atListProvider =
    StateNotifierProvider.autoDispose<AtListNotifier, AtListState>((ref) {
  return AtListNotifier(ref.watch(atApiProvider));
});

/// Détail d'une AT par id (erreurs mappées en Failure pour l'UI).
final atDetailProvider =
    FutureProvider.autoDispose.family<AutorisationTravail, String>((ref, id) async {
  final api = ref.watch(atApiProvider);
  try {
    return await api.findById(id);
  } catch (e) {
    throw mapDioError(e);
  }
});
