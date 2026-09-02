/// Carte AT pour les listes - affiche les champs réellement présents
/// dans AutorisationTravailResponse (AUCUNE notion Installation).
library;

import 'package:flutter/material.dart';
import '../../features/at/data/models/autorisation_travail.dart';
import '../theme/app_colors.dart';
import '../utils/app_date.dart';
import 'statut_chip.dart';

class AtCard extends StatelessWidget {
  final AutorisationTravail at;
  final VoidCallback? onTap;

  const AtCard({super.key, required this.at, this.onTap});

  @override
  Widget build(BuildContext context) {
    return Card(
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(12),
        child: Padding(
          padding: const EdgeInsets.all(14),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                children: [
                  Expanded(
                    child: Text(
                      at.numero ?? 'AT sans numéro',
                      style: const TextStyle(
                        fontFamily: 'SpaceGrotesk',
                        fontWeight: FontWeight.w700,
                        fontSize: 15,
                        color: OcpColors.forest,
                      ),
                    ),
                  ),
                  StatutChip(statut: at.statut),
                ],
              ),
              if ((at.objet ?? '').isNotEmpty) ...[
                const SizedBox(height: 8),
                Text(
                  at.objet!,
                  maxLines: 2,
                  overflow: TextOverflow.ellipsis,
                  style: const TextStyle(fontSize: 14, color: OcpColors.ink),
                ),
              ],
              const SizedBox(height: 10),
              Wrap(
                spacing: 12,
                runSpacing: 4,
                children: [
                  _meta(Icons.location_on_outlined, at.zoneExecutanteNom ?? 'Zone -'),
                  _meta(Icons.account_tree_outlined, at.zoneProprietaireNom ?? 'Propriétaire -'),
                  if (at.typeDocumentSource != null)
                    _meta(Icons.description_outlined, at.typeDocumentSource!),
                ],
              ),
              const SizedBox(height: 8),
              Row(
                children: [
                  const Icon(Icons.calendar_today_outlined, size: 13, color: OcpColors.slate),
                  const SizedBox(width: 4),
                  Text(
                    AppDate.date(at.dateDebut),
                    style: const TextStyle(fontSize: 12, color: OcpColors.slate),
                  ),
                  const Spacer(),
                  if (at.etatVerrou == EtatVerrou.enCoursEdition) ...[
                    const Icon(Icons.lock_outline_rounded, size: 14, color: OcpColors.warning),
                    const SizedBox(width: 4),
                    const Text(
                      'Verrouillée',
                      style: TextStyle(fontSize: 12, color: OcpColors.warning),
                    ),
                  ],
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _meta(IconData icon, String label) => Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(icon, size: 14, color: OcpColors.slate),
          const SizedBox(width: 4),
          ConstrainedBox(
            constraints: const BoxConstraints(maxWidth: 160),
            child: Text(
              label,
              maxLines: 1,
              overflow: TextOverflow.ellipsis,
              style: const TextStyle(fontSize: 12, color: OcpColors.slate),
            ),
          ),
        ],
      );
}
