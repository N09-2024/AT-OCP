/// Étape de sélection multiple (risques / mesures / EPI / moyens d'accès /
/// permis complémentaires). Données = référentiels backend réels, avec
/// recherche, sélection et désélection.
library;

import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/widgets/states.dart';
import '../../../referentiels/data/referentiel_models.dart';

class MultiSelectStep extends ConsumerStatefulWidget {
  final AsyncValue<List<ReferentielItem>> items;
  final Set<String> selected;
  final ValueChanged<String> onToggle;
  final String searchHint;
  final String emptyMessage;

  const MultiSelectStep({
    super.key,
    required this.items,
    required this.selected,
    required this.onToggle,
    required this.searchHint,
    required this.emptyMessage,
  });

  @override
  ConsumerState<MultiSelectStep> createState() => _MultiSelectStepState();
}

class _MultiSelectStepState extends ConsumerState<MultiSelectStep> {
  String _query = '';
  Timer? _debounce;

  @override
  void dispose() {
    _debounce?.cancel();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        if (widget.selected.isNotEmpty)
          Padding(
            padding: const EdgeInsets.only(bottom: 8),
            child: Text(
              '${widget.selected.length} sélectionné${widget.selected.length > 1 ? 's' : ''}',
              style: const TextStyle(
                fontSize: 13,
                fontWeight: FontWeight.w700,
                color: OcpColors.moss,
              ),
            ),
          ),
        TextField(
          decoration: InputDecoration(
            hintText: widget.searchHint,
            prefixIcon: const Icon(Icons.search_rounded),
          ),
          onChanged: (v) => setState(() => _query = v.trim().toLowerCase()),
        ),
        const SizedBox(height: 10),
        Expanded(
          child: widget.items.when(
            loading: () => const LoadingState(),
            error: (e, _) =>
                const ErrorState(message: 'Référentiel indisponible. Vérifiez votre connexion.'),
            data: (list) {
              final filtered = _query.isEmpty
                  ? list
                  : list.where((r) =>
                      r.nom.toLowerCase().contains(_query) ||
                      (r.description ?? '').toLowerCase().contains(_query),).toList();
              if (filtered.isEmpty) {
                return EmptyState(message: widget.emptyMessage, icon: Icons.rule_outlined);
              }
              return ListView.separated(
                itemCount: filtered.length,
                separatorBuilder: (_, _) => const SizedBox(height: 4),
                itemBuilder: (context, index) {
                  final item = filtered[index];
                  final checked = widget.selected.contains(item.id);
                  return Container(
                    decoration: BoxDecoration(
                      color: checked ? OcpColors.forestSoft.withValues(alpha: 0.35) : OcpColors.white,
                      borderRadius: BorderRadius.circular(10),
                      border: Border.all(
                        color: checked ? OcpColors.forest : OcpColors.borderSoft,
                        width: checked ? 1.5 : 1,
                      ),
                    ),
                    child: CheckboxListTile(
                      value: checked,
                      activeColor: OcpColors.forest,
                      controlAffinity: ListTileControlAffinity.leading,
                      contentPadding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                      title: Text(
                        item.nom,
                        style: TextStyle(
                          fontSize: 15,
                          fontWeight: checked ? FontWeight.w700 : FontWeight.w500,
                          color: OcpColors.ink,
                        ),
                      ),
                      subtitle: (item.description ?? '').isEmpty
                          ? null
                          : Text(
                              item.description!,
                              maxLines: 2,
                              overflow: TextOverflow.ellipsis,
                              style: const TextStyle(fontSize: 13, color: OcpColors.slate, height: 1.35),
                            ),
                      onChanged: (_) => widget.onToggle(item.id),
                    ),
                  );
                },
              );
            },
          ),
        ),
      ],
    );
  }
}
