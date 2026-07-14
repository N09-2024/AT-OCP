import { Box, Typography, Paper, Button } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '../../../store/authStore';
import {
  PlusCircleIcon,
  ClipboardDocumentCheckIcon,
  DocumentTextIcon,
} from '@heroicons/react/24/outline';

export default function DemandeurDashboardPage() {
  const navigate = useNavigate();
  const user = useAuthStore((s) => s.user);

  return (
    <Box sx={{ p: 3 }}>
      {/* Header */}
      <Box sx={{ mb: 4 }}>
        <Typography variant="h4" sx={{ fontWeight: 800, color: 'text.primary' }}>
          Bonjour, {user?.prenom} 👋
        </Typography>
        <Typography variant="body1" color="text.secondary" sx={{ mt: 0.5 }}>
          Gérez vos demandes d'autorisations de travail
        </Typography>
      </Box>

      {/* Action principale */}
      <Paper
        sx={{
          p: 4,
          mb: 3,
          borderRadius: 3,
          background: 'linear-gradient(135deg, #0891b2 0%, #0e7490 100%)',
          color: 'white',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          boxShadow: '0 8px 24px rgba(8,145,178,0.3)',
        }}
      >
        <Box>
          <Typography variant="h6" sx={{ fontWeight: 700, mb: 0.5 }}>
            Créer une nouvelle demande d'intervention
          </Typography>
          <Typography variant="body2" sx={{ opacity: 0.85 }}>
            Remplissez le formulaire → Générez un Ordre de Travail → Créez une AT
          </Typography>
        </Box>
        <Button
          variant="contained"
          startIcon={<PlusCircleIcon width={20} />}
          onClick={() => navigate('/autorisations/nouvelle')}
          sx={{
            bgcolor: 'white',
            color: '#0891b2',
            fontWeight: 700,
            borderRadius: 2,
            px: 3,
            whiteSpace: 'nowrap',
            flexShrink: 0,
            ml: 3,
            '&:hover': { bgcolor: 'rgba(255,255,255,0.9)' },
          }}
        >
          Nouvelle demande
        </Button>
      </Paper>

      {/* Quick links */}
      <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', md: '1fr 1fr' }, gap: 3 }}>
        <Paper
          sx={{
            p: 3,
            borderRadius: 3,
            border: '1px solid',
            borderColor: 'divider',
            cursor: 'pointer',
            transition: 'all 0.2s',
            '&:hover': { borderColor: '#0891b2', boxShadow: '0 4px 12px rgba(8,145,178,0.1)' },
          }}
          onClick={() => navigate('/autorisations')}
        >
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
            <Box sx={{ p: 1.5, bgcolor: '#0891b222', borderRadius: 2 }}>
              <ClipboardDocumentCheckIcon width={28} color="#0891b2" />
            </Box>
            <Box>
              <Typography variant="h6" sx={{ fontWeight: 700 }}>Mes autorisations</Typography>
              <Typography variant="body2" color="text.secondary">
                Suivre l'état de vos AT en cours
              </Typography>
            </Box>
          </Box>
        </Paper>

        <Paper
          sx={{
            p: 3,
            borderRadius: 3,
            border: '1px solid',
            borderColor: 'divider',
            cursor: 'pointer',
            transition: 'all 0.2s',
            '&:hover': { borderColor: '#0891b2', boxShadow: '0 4px 12px rgba(8,145,178,0.1)' },
          }}
          onClick={() => navigate('/documents')}
        >
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
            <Box sx={{ p: 1.5, bgcolor: '#0891b222', borderRadius: 2 }}>
              <DocumentTextIcon width={28} color="#0891b2" />
            </Box>
            <Box>
              <Typography variant="h6" sx={{ fontWeight: 700 }}>Mes documents</Typography>
              <Typography variant="body2" color="text.secondary">
                Documents d'intervention importés
              </Typography>
            </Box>
          </Box>
        </Paper>
      </Box>

      {/* Info box */}
      <Paper
        sx={{
          p: 3,
          mt: 3,
          borderRadius: 3,
          bgcolor: '#f0f9ff',
          border: '1px solid #bae6fd',
        }}
      >
        <Typography variant="subtitle2" sx={{ fontWeight: 700, color: '#0369a1', mb: 1 }}>
          📋 Processus de demande d'AT
        </Typography>
        <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap' }}>
          {[
            '1. Remplir le formulaire de demande',
            '2. Générer l\'Ordre de Travail',
            '3. Créer l\'Autorisation de Travail',
            '4. Attendre la validation du Responsable OCP',
            '5. Clôturer l\'AT après les travaux',
          ].map((step) => (
            <Typography key={step} variant="caption" color="#0369a1" sx={{ display: 'block', mb: 0.5 }}>
              • {step}
            </Typography>
          ))}
        </Box>
      </Paper>
    </Box>
  );
}
