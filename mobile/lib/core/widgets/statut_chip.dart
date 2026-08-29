/// Chip de statut AT — couleurs cohérentes avec le frontend React.
library;

import 'package:flutter/material.dart';
import '../../features/at/data/models/autorisation_travail.dart';
import '../theme/app_colors.dart';

class StatutChip extends StatelessWidget {
  final String? statut;
  const StatutChip({super.key, required this.statut});

  Color get _color {
    switch (statut) {
      case StatutAt.brouillon:
        return OcpColors.statutBrouillon;
      case StatutAt.demandeCreee:
      case StatutAt.atRedigee:
        return OcpColors.statutDemandee;
      case StatutAt.enVisiteRedaction:
      case StatutAt.visiteRealisee:
        return OcpColors.statutEnVisite;
      case StatutAt.soumise:
        return OcpColors.statutSoumise;
      case StatutAt.validee:
      case StatutAt.atValidee:
        return OcpColors.statutValidee;
      case StatutAt.enCours:
      case StatutAt.interventionEnCours:
      case StatutAt.enReconduction:
      case StatutAt.atReconduite:
      case StatutAt.renouvelee:
        return OcpColors.statutEnCours;
      case StatutAt.declareeTerminee:
      case StatutAt.finTravauxDeclaree:
        return OcpColors.statutDeclareeTerminee;
      case StatutAt.receptionnees:
      case StatutAt.travauxReceptiones:
        return OcpColors.statutReceptionnee;
      case StatutAt.archivee:
        return OcpColors.statutArchivee;
      case StatutAt.rejetee:
        return OcpColors.statutRejetee;
      case StatutAt.annulee:
        return OcpColors.statutAnnulee;
      default:
        return OcpColors.slate;
    }
  }

  @override
  Widget build(BuildContext context) {
    final color = _color;
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
      decoration: BoxDecoration(
        color: color.withValues(alpha: 0.12),
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: color.withValues(alpha: 0.4)),
      ),
      child: Text(
        StatutAt.libelle(statut),
        style: TextStyle(
          color: color,
          fontSize: 12,
          fontWeight: FontWeight.w700,
        ),
      ),
    );
  }
}
