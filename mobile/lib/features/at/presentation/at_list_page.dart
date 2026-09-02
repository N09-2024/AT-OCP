/// Liste des AT - GET /autorisations-travail (paginé).
/// Recherche, filtre par statut, pull-to-refresh, chargement progressif.
library;

import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../../core/widgets/at_card.dart';
import '../../../core/widgets/states.dart';
import '../data/models/autorisation_travail.dart';
import 'at_providers.dart';

class AtListPage extends ConsumerStatefulWidget {
  final String? initialFilter;
  final String? initialStatut;
  const AtListPage({super.key, this.initialFilter, this.initialStatut});

  @override
  ConsumerState<AtListPage> createState() => _AtListPageState();
}

class _AtListPageState extends ConsumerState<AtListPage> {
  final _searchController = TextEditingController();
  Timer? _debounce;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) => _appliquerFiltresInitiaux());
  }

  @override
  void didUpdateWidget(covariant AtListPage oldWidget) {
    super.didUpdateWidget(oldWidget);
    // Le lien dashboard → liste peut changer les paramètres alors que la page
    // reste montée (StatefulShellRoute) : on ré-applique les filtres.
    if (oldWidget.initialFilter != widget.initialFilter ||
        oldWidget.initialStatut != widget.initialStatut) {
      WidgetsBinding.instance.addPostFrameCallback((_) => _appliquerFiltresInitiaux());
    }
  }

  void _appliquerFiltresInitiaux() {
    final notifier = ref.read(atListProvider.notifier);
    switch (widget.initialFilter) {
      case 'mine':
        notifier.setScope(AtFilterScope.mine);
      case 'aValider':
        notifier.setScope(AtFilterScope.aValider);
    }
    final statut = widget.initialStatut;
    if (statut != null && statut.isNotEmpty) {
      notifier.setStatut(statut);
    }
  }

  @override
  void dispose() {
    _debounce?.cancel();
    _searchController.dispose();
    super.dispose();
  }

  void _onSearchChanged(String value) {
    _debounce?.cancel();
    _debounce = Timer(const Duration(milliseconds: 400), () {
      ref.read(atListProvider.notifier).setSearch(value);
    });
  }

  /// Filtre statut : valeurs réelles de l'enum backend les plus utiles au terrain.
  static const _statuts = <String?>[
    null,
    StatutAt.brouillon,
    StatutAt.demandeCreee,
    StatutAt.enVisiteRedaction,
    StatutAt.visiteRealisee,
    StatutAt.atRedigee,
    StatutAt.soumise,
    StatutAt.atValidee,
    StatutAt.interventionEnCours,
    StatutAt.atReconduite,
    StatutAt.declareeTerminee,
    StatutAt.finTravauxDeclaree,
    StatutAt.travauxReceptiones,
    StatutAt.receptionnees,
    StatutAt.archivee,
    StatutAt.rejetee,
  ];

  @override
  Widget build(BuildContext context) {
    final listState = ref.watch(atListProvider);
    final notifier = ref.read(atListProvider.notifier);

    return Scaffold(
      appBar: AppBar(title: const Text('Autorisations de Travail')),
      body: Column(
        children: [
          // --- Filtre portée (Toutes / Mes AT / À valider) ---
          Padding(
            padding: const EdgeInsets.fromLTRB(16, 12, 16, 4),
            child: SegmentedButton<AtFilterScope>(
              segments: const [
                ButtonSegment(value: AtFilterScope.all, label: Text('Toutes')),
                ButtonSegment(value: AtFilterScope.mine, label: Text('Mes AT')),
                ButtonSegment(value: AtFilterScope.aValider, label: Text('À valider')),
              ],
              selected: {notifier.scope},
              onSelectionChanged: (selected) {
                if (selected.isNotEmpty) {
                  notifier.setScope(selected.first);
                }
              },
            ),
          ),

          // --- Recherche + filtre statut ---
          Padding(
            padding: const EdgeInsets.fromLTRB(16, 6, 16, 4),
            child: TextField(
              controller: _searchController,
              onChanged: _onSearchChanged,
              decoration: InputDecoration(
                hintText: 'Rechercher (numéro, objet...)',
                prefixIcon: const Icon(Icons.search_rounded),
                suffixIcon: _searchController.text.isEmpty
                    ? null
                    : IconButton(
                        icon: const Icon(Icons.clear_rounded),
                        onPressed: () {
                          _searchController.clear();
                          notifier.setSearch('');
                        },
                      ),
              ),
            ),
          ),
          SizedBox(
            height: 44,
            child: ListView.separated(
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 6),
              scrollDirection: Axis.horizontal,
              itemCount: _statuts.length,
              separatorBuilder: (_, _) => const SizedBox(width: 8),
              itemBuilder: (context, index) {
                final statut = _statuts[index];
                final selected = notifier.statut == statut;
                return FilterChip(
                  selected: selected,
                  label: Text(statut == null ? 'Tous statuts' : StatutAt.libelle(statut)),
                  onSelected: (_) => notifier.setStatut(statut),
                );
              },
            ),
          ),
          const SizedBox(height: 4),

          // --- Contenu ---
          Expanded(
            child: switch (listState) {
              AtListLoading() => const LoadingState(message: 'Chargement des AT...'),
              AtListError() => ErrorState(
                  message: listState.failure.message,
                  onRetry: () => notifier.reload(),
                ),
              AtListLoaded() => listState.items.isEmpty
                  ? const EmptyState(message: 'Aucune AT trouvée.')
                  : RefreshIndicator(
                      onRefresh: () => notifier.reload(),
                      child: ListView.builder(
                        physics: const AlwaysScrollableScrollPhysics(),
                        padding: const EdgeInsets.only(bottom: 24),
                        itemCount: listState.items.length + (listState.hasMore ? 1 : 0),
                        itemBuilder: (context, index) {
                          if (index >= listState.items.length) {
                            // Dernier item visible → charger la page suivante.
                            WidgetsBinding.instance.addPostFrameCallback((_) {
                              if (mounted) notifier.loadMore();
                            });
                            return const Padding(
                              padding: EdgeInsets.all(16),
                              child: Center(
                                child: SizedBox(
                                  width: 24,
                                  height: 24,
                                  child: CircularProgressIndicator(strokeWidth: 2.5),
                                ),
                              ),
                            );
                          }
                          final at = listState.items[index];
                          return AtCard(
                            at: at,
                            onTap: () => context.push('/at/${at.id}'),
                          );
                        },
                      ),
                    ),
            },
          ),
        ],
      ),
    );
  }
}
