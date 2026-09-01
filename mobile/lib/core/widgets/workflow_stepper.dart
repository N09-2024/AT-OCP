/// Composant visualisant les 8 étapes du logigramme S-HSE-SEC-31 §7.
/// Réplique exacte du WorkflowStepper React web.
library;

import 'package:flutter/material.dart';
import '../theme/app_colors.dart';

class WorkflowStepInfo {
  final int id;
  final List<String> keys;
  final String label;
  final String sublabel;

  const WorkflowStepInfo({
    required this.id,
    required this.keys,
    required this.label,
    required this.sublabel,
  });
}

class WorkflowStepper extends StatelessWidget {
  final String? statut;
  final String? statutWorkflow;

  const WorkflowStepper({
    super.key,
    this.statut,
    this.statutWorkflow,
  });

  static const List<WorkflowStepInfo> steps = [
    WorkflowStepInfo(
      id: 0,
      keys: ['CLASSIFICATION_EFFECTUEE'],
      label: 'Classification',
      sublabel: 'Niveau 1/2 - HCEP',
    ),
    WorkflowStepInfo(
      id: 1,
      keys: ['DEMANDE_CREEE'],
      label: 'Demande d\'intervention',
      sublabel: 'DI / OT / BT - CEEP',
    ),
    WorkflowStepInfo(
      id: 2,
      keys: ['VISITE_REALISEE', 'EN_VISITE_REDACTION'],
      label: 'Visite chantier',
      sublabel: 'Analyse risques - CEEP + HCEE/HMEP',
    ),
    WorkflowStepInfo(
      id: 3,
      keys: ['AT_REDIGEE', 'SOUMISE', 'AT_VALIDEE', 'VALIDEE'],
      label: 'Rédaction AT + Permis',
      sublabel: 'Sur le terrain - CEEP / HCEE / CEEE',
    ),
    WorkflowStepInfo(
      id: 4,
      keys: ['INTERVENTION_EN_COURS', 'EN_COURS', 'AT_RECONDUITE', 'EN_RECONDUCTION', 'RENOUVELEE'],
      label: 'Intervention',
      sublabel: 'Travaux + reconduction poste',
    ),
    WorkflowStepInfo(
      id: 5,
      keys: ['FIN_TRAVAUX_DECLAREE', 'DECLAREE_TERMINEE'],
      label: 'Fin des travaux',
      sublabel: 'Déclaration CEEE',
    ),
    WorkflowStepInfo(
      id: 6,
      keys: ['TRAVAUX_RECEPTIONES', 'RECEPTIONEES', 'CLOTUREE'],
      label: 'Réception',
      sublabel: 'Essais + clôture - CEEP + CEEE',
    ),
    WorkflowStepInfo(
      id: 7,
      keys: ['ARCHIVEE'],
      label: 'Archivage',
      sublabel: '≥ 1 an - entité propriétaire',
    ),
  ];

  static const List<String> order = [
    'CLASSIFICATION_EFFECTUEE',
    'DEMANDE_CREEE',
    'VISITE_REALISEE',
    'AT_REDIGEE',
    'INTERVENTION_EN_COURS',
    'AT_RECONDUITE',
    'FIN_TRAVAUX_DECLAREE',
    'TRAVAUX_RECEPTIONES',
    'ARCHIVEE',
  ];

  static int getStepIndex(String? statut) {
    if (statut == null || statut.isEmpty) return -1;
    const map = {
      'BROUILLON': 'DEMANDE_CREEE',
      'EN_VISITE_REDACTION': 'VISITE_REALISEE',
      'SOUMISE': 'AT_REDIGEE',
      'AT_VALIDEE': 'AT_REDIGEE',
      'VALIDEE': 'AT_REDIGEE',
      'EN_COURS': 'INTERVENTION_EN_COURS',
      'EN_RECONDUCTION': 'AT_RECONDUITE',
      'RENOUVELEE': 'AT_RECONDUITE',
      'DECLAREE_TERMINEE': 'FIN_TRAVAUX_DECLAREE',
      'RECEPTIONEES': 'TRAVAUX_RECEPTIONES',
      'CLOTUREE': 'TRAVAUX_RECEPTIONES',
    };
    final resolved = map[statut] ?? statut;
    final idx = order.indexOf(resolved);
    return idx;
  }

  @override
  Widget build(BuildContext context) {
    final currentOrderIdx = getStepIndex(statutWorkflow?.isNotEmpty == true ? statutWorkflow : statut);

    return Card(
      elevation: 0,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(12),
        side: const BorderSide(color: OcpColors.borderSoft),
      ),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                const Icon(Icons.alt_route_rounded, size: 20, color: OcpColors.forest),
                const SizedBox(width: 8),
                const Text(
                  'Workflow S-HSE-SEC-31',
                  style: TextStyle(
                    fontFamily: 'SpaceGrotesk',
                    fontWeight: FontWeight.w700,
                    fontSize: 15,
                    color: OcpColors.deep,
                  ),
                ),
                const Spacer(),
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
                  decoration: BoxDecoration(
                    color: OcpColors.mintSoft,
                    borderRadius: BorderRadius.circular(6),
                  ),
                  child: const Text(
                    'Logigramme §7',
                    style: TextStyle(
                      fontSize: 11,
                      fontWeight: FontWeight.w600,
                      color: OcpColors.forest,
                    ),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 14),
            ...List.generate(steps.length, (index) {
              final step = steps[index];
              final stepOrderIdx = step.keys
                  .map((k) => order.indexOf(k))
                  .where((i) => i >= 0)
                  .fold<int>(999, (min, i) => i < min ? i : min);

              final isDone = currentOrderIdx > stepOrderIdx ||
                  (currentOrderIdx == stepOrderIdx &&
                      step.keys.contains('ARCHIVEE') &&
                      currentOrderIdx == order.length - 1);
              final isActive = currentOrderIdx == stepOrderIdx ||
                  step.keys.any((k) => order.indexOf(k) == currentOrderIdx);

              final isLast = index == steps.length - 1;

              return IntrinsicHeight(
                child: Row(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    // Colonne icône + ligne
                    SizedBox(
                      width: 24,
                      child: Column(
                        children: [
                          Icon(
                            isDone || isActive
                                ? Icons.check_circle_rounded
                                : Icons.radio_button_unchecked_rounded,
                            size: 20,
                            color: isDone || isActive ? OcpColors.forest : OcpColors.border,
                          ),
                          if (!isLast)
                            Expanded(
                              child: Container(
                                width: 2,
                                margin: const EdgeInsets.symmetric(vertical: 2),
                                color: isDone ? OcpColors.forest : OcpColors.borderSoft,
                              ),
                            ),
                        ],
                      ),
                    ),
                    const SizedBox(width: 12),
                    // Colonne labels
                    Expanded(
                      child: Padding(
                        padding: EdgeInsets.only(bottom: isLast ? 0 : 16),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              '${step.id}. ${step.label}',
                              style: TextStyle(
                                fontSize: 13,
                                fontWeight: isActive ? FontWeight.w700 : (isDone ? FontWeight.w600 : FontWeight.w500),
                                color: isActive
                                    ? OcpColors.forest
                                    : (isDone ? OcpColors.ink : OcpColors.slate),
                              ),
                            ),
                            const SizedBox(height: 1),
                            Text(
                              step.sublabel,
                              style: const TextStyle(
                                fontSize: 11,
                                color: OcpColors.slate,
                              ),
                            ),
                          ],
                        ),
                      ),
                    ),
                  ],
                ),
              );
            }),
          ],
        ),
      ),
    );
  }
}
