import { Card, Box, Typography } from '@mui/material';
import { CheckCircleIcon } from '@heroicons/react/24/solid';
import { CheckCircleIcon as CheckCircleOutline } from '@heroicons/react/24/outline';

const STEPS = [
  { id: 1, label: 'Brouillon', sublabel: 'Création de la demande', done: true },
  { id: 2, label: 'Soumise', sublabel: 'Demande soumise', done: true },
  { id: 3, label: 'Analyse des risques', sublabel: 'Évaluation et mesures', done: true },
  { id: 4, label: 'Autorisation', sublabel: 'Validation et visas', active: true },
  { id: 5, label: 'En cours', sublabel: 'Travaux en exécution', done: false },
  { id: 6, label: 'Réception', sublabel: 'Clôture et réception', done: false },
  { id: 7, label: 'Archivage', sublabel: 'Documents archivés', done: false },
];

export default function WorkflowStepper() {
  return (
    <Card sx={{ p: 3, height: '100%', display: 'flex', flexDirection: 'column' }}>
      <Typography variant="h6" sx={{ fontWeight: 'bold', mb: 3 }}>
        Étapes du workflow
      </Typography>

      <Box sx={{ display: 'flex', flexDirection: 'column', flexGrow: 1 }}>
        {STEPS.map((step, index) => (
          <Box key={step.id} sx={{ display: 'flex', alignItems: 'flex-start', gap: 2 }}>
            {/* Icon + connector line */}
            <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', pt: 0.5 }}>
              <Box
                sx={{
                  color: step.active || step.done ? 'primary.main' : 'grey.300',
                  display: 'flex',
                  zIndex: 1,
                }}
              >
                {step.done || step.active ? (
                  <CheckCircleIcon width={22} />
                ) : (
                  <CheckCircleOutline width={22} />
                )}
              </Box>
              {index < STEPS.length - 1 && (
                <Box
                  sx={{
                    width: 2,
                    height: 28,
                    bgcolor: step.done ? 'primary.main' : 'grey.200',
                    my: 0.25,
                  }}
                />
              )}
            </Box>

            {/* Text */}
            <Box sx={{ pt: 0.25, pb: index < STEPS.length - 1 ? 0 : 0 }}>
              <Typography
                variant="body2"
                sx={{
                  fontWeight: step.active ? 700 : 500,
                  color: step.active ? 'primary.main' : step.done ? 'text.primary' : 'text.secondary',
                }}
              >
                {step.id}. {step.label}
              </Typography>
              <Typography variant="caption" color="text.secondary">
                {step.sublabel}
              </Typography>
            </Box>
          </Box>
        ))}
      </Box>
    </Card>
  );
}
