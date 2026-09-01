/// Chip de statut AT — RÉPLIQUE EXACTE de la configuration du frontend React
/// (`frontend/src/components/dashboard/AtTable.tsx` → STATUT_CONFIG :
/// couple { bg, color }), étendue aux statuts du workflow standard
/// S-HSE-SEC-31 absents du tableau web, dans le même langage visuel.
library;

import 'package:flutter/material.dart';
import '../../features/at/data/models/autorisation_travail.dart';
import '../theme/app_colors.dart';

class _StatutStyle {
  final Color bg;
  final Color fg;
  const _StatutStyle(this.bg, this.fg);
}

/// Map littérale du web (AtTable.tsx) :
/// BROUILLON  { bg '#EDF2EE', color '#5C6E67' }
/// SOUMISE    { bg '#DCEBE3', color '#1F4D3E' }
/// EN_COURS   { bg '#DCEBE3', color '#3C7A5C' }
/// VALIDEE    { bg '#DCEBE3', color '#3C7A5C' }
/// REJETEE    { bg '#FEE2E2', color '#9A3D2F' }
/// RENOUVELEE { bg '#E2F0E8', color '#3C7A5C' }
/// CLOTUREE   { bg '#EDF2EE', color '#5C6E67' }
/// ARCHIVEE   { bg '#EDF2EE', color '#5C6E67' }
/// ANNULEE    { bg '#FEE2E2', color '#9A3D2F' }
const Map<String, _StatutStyle> _statutStyles = {
  // --- Valeurs identiques au web ---
  StatutAt.brouillon: _StatutStyle(OcpColors.sage, OcpColors.slate),
  StatutAt.soumise: _StatutStyle(OcpColors.forestSoft, OcpColors.forest),
  StatutAt.enCours: _StatutStyle(OcpColors.forestSoft, OcpColors.moss),
  StatutAt.validee: _StatutStyle(OcpColors.forestSoft, OcpColors.moss),
  StatutAt.rejetee: _StatutStyle(Color(0xFFFEE2E2), OcpColors.error),
  StatutAt.renouvelee: _StatutStyle(OcpColors.mintSoft, OcpColors.moss),
  StatutAt.archivee: _StatutStyle(OcpColors.sage, OcpColors.slate),
  StatutAt.annulee: _StatutStyle(Color(0xFFFEE2E2), OcpColors.error),

  // --- Statuts du workflow standard (mêmes pairs bg/fg du thème OCP) ---
  StatutAt.classificationEffectuee: _StatutStyle(OcpColors.sage, OcpColors.slate),
  StatutAt.demandeCreee: _StatutStyle(OcpColors.sage, OcpColors.slate),
  StatutAt.enVisiteRedaction: _StatutStyle(OcpColors.warningSoft, OcpColors.warning),
  StatutAt.visiteRealisee: _StatutStyle(OcpColors.warningSoft, OcpColors.warning),
  StatutAt.atRedigee: _StatutStyle(OcpColors.forestSoft, OcpColors.forest),
  StatutAt.atValidee: _StatutStyle(OcpColors.mintSoft, OcpColors.moss),
  StatutAt.interventionEnCours: _StatutStyle(OcpColors.mintSoft, OcpColors.moss),
  StatutAt.enReconduction: _StatutStyle(OcpColors.mintSoft, OcpColors.moss),
  StatutAt.atReconduite: _StatutStyle(OcpColors.mintSoft, OcpColors.moss),
  StatutAt.declareeTerminee: _StatutStyle(OcpColors.warningSoft, OcpColors.warning),
  StatutAt.finTravauxDeclaree: _StatutStyle(OcpColors.warningSoft, OcpColors.warning),
  StatutAt.receptionnees: _StatutStyle(OcpColors.forestSoft, OcpColors.forest),
  StatutAt.travauxReceptiones: _StatutStyle(OcpColors.forestSoft, OcpColors.forest),
};

class StatutChip extends StatelessWidget {
  final String? statut;
  const StatutChip({super.key, required this.statut});

  _StatutStyle get _style =>
      _statutStyles[statut] ?? const _StatutStyle(OcpColors.sage, OcpColors.slate);

  @override
  Widget build(BuildContext context) {
    final style = _style;
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
      decoration: BoxDecoration(
        color: style.bg,
        borderRadius: BorderRadius.circular(20),
      ),
      child: Text(
        StatutAt.libelle(statut),
        style: TextStyle(
          color: style.fg,
          fontSize: 12,
          fontWeight: FontWeight.w700,
        ),
      ),
    );
  }
}
