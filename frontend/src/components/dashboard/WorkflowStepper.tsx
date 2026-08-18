import { Box, Card, Typography } from '@mui/material';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import CheckCircleOutlineIcon from '@mui/icons-material/RadioButtonUnchecked';
import type { StatutAT } from '../../types';

/** Étapes du logigramme S-HSE-SEC-31 §7 */
const STEPS: { id: number; key: StatutAT | StatutAT[]; label: string; sublabel: string }[] = [
  { id: 0, key: 'CLASSIFICATION_EFFECTUEE', label: 'Classification', sublabel: 'Niveau 1/2 - HCEP' },
  { id: 1, key: 'DEMANDE_CREEE', label: 'Demande d\'intervention', sublabel: 'DI / OT / BT - CEEP' },
  { id: 2, key: 'VISITE_REALISEE', label: 'Visite chantier', sublabel: 'Analyse risques - CEEP + HCEE/HMEP' },
  { id: 3, key: 'AT_REDIGEE', label: 'Rédaction AT + Permis', sublabel: 'Sur le terrain - CEEP / HCEE / CEEE' },
  { id: 4, key: ['INTERVENTION_EN_COURS', 'AT_RECONDUITE'], label: 'Intervention', sublabel: 'Travaux + reconduction poste' },
  { id: 5, key: 'FIN_TRAVAUX_DECLAREE', label: 'Fin des travaux', sublabel: 'Déclaration CEEE' },
  { id: 6, key: 'TRAVAUX_RECEPTIONES', label: 'Réception', sublabel: 'Essais + clôture - CEEP + CEEE' },
  { id: 7, key: 'ARCHIVEE', label: 'Archivage', sublabel: '≥ 1 an - entité propriétaire' },
];

const ORDER: StatutAT[] = [
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

function stepIndex(statut?: string | null): number {
  if (!statut) return -1;
  // Legacy mapping
  const map: Record<string, StatutAT> = {
    BROUILLON: 'DEMANDE_CREEE',
    SOUMISE: 'VISITE_REALISEE',
    VALIDEE: 'AT_REDIGEE',
    RENOUVELEE: 'AT_RECONDUITE',
    CLOTUREE: 'TRAVAUX_RECEPTIONES',
  };
  const s = (map[statut] || statut) as StatutAT;
  const i = ORDER.indexOf(s);
  if (i >= 0) return i;
  return -1;
}

interface Props {
  statut?: string | null;
  statutWorkflow?: string | null;
}

export default function WorkflowStepper({ statut, statutWorkflow }: Props) {
  const current = stepIndex(statutWorkflow || statut);

  return (
    <Card sx={{ p: 3, height: '100%', display: 'flex', flexDirection: 'column' }}>
      <Typography variant="h6" sx={{ fontWeight: 'bold', mb: 1 }}>
        Workflow S-HSE-SEC-31
      </Typography>
      <Typography variant="caption" color="text.secondary" sx={{ mb: 2 }}>
        Logigramme officiel §7 - Autorisation de travail
      </Typography>

      <Box sx={{ display: 'flex', flexDirection: 'column', flexGrow: 1 }}>
        {STEPS.map((step, index) => {
          const keys = Array.isArray(step.key) ? step.key : [step.key];
          const stepOrd = Math.min(...keys.map((k) => ORDER.indexOf(k)).filter((i) => i >= 0));
          const done = current > stepOrd || (current === stepOrd && keys.includes('ARCHIVEE' as StatutAT) && current === ORDER.length - 1);
          const active = current === stepOrd || (Array.isArray(step.key) && keys.some((k) => ORDER.indexOf(k) === current));

          return (
            <Box key={step.id} sx={{ display: 'flex', alignItems: 'flex-start', gap: 2 }}>
              <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', pt: 0.5 }}>
                <Box sx={{ color: active || done ? 'primary.main' : 'grey.300', display: 'flex', zIndex: 1 }}>
                  {done || active ? <CheckCircleIcon sx={{ fontSize: 22 }} /> : <CheckCircleOutlineIcon sx={{ fontSize: 22 }} />}
                </Box>
                {index < STEPS.length - 1 && (
                  <Box sx={{ width: 2, height: 28, bgcolor: done ? 'primary.main' : 'grey.200', my: 0.25 }} />
                )}
              </Box>
              <Box sx={{ pt: 0.25 }}>
                <Typography
                  variant="body2"
                  sx={{
                    fontWeight: active ? 700 : 500,
                    color: active ? 'primary.main' : done ? 'text.primary' : 'text.secondary',
                  }}
                >
                  {step.id}. {step.label}
                </Typography>
                <Typography variant="caption" color="text.secondary">
                  {step.sublabel}
                </Typography>
              </Box>
            </Box>
          );
        })}
      </Box>
    </Card>
  );
}
