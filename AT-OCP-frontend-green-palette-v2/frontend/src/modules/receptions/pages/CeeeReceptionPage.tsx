import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Box,
  Typography,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Button,
  Chip,
  Stack,
  CircularProgress,
  Alert,
  Tooltip,
  IconButton,
} from '@mui/material';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import VisibilityIcon from '@mui/icons-material/Visibility';
import MarkEmailUnreadIcon from '@mui/icons-material/MarkEmailUnread';
import DoneAllIcon from '@mui/icons-material/DoneAll';
import { autorisationTravailApi } from '../../../services/autorisationTravailApi';
import { useAuthStore } from '../../../store/authStore';
import type { AutorisationTravail } from '../../../types';

/**
 * CeeeReceptionPage — Interface dédiée au CEEE pour recevoir et viser les AT
 * qui lui sont transmises par le CEEP, conformément au workflow S-HSE-SEC-31.
 *
 * Le CEEE NE signe JAMAIS depuis l'interface du CEEP.
 * Il accuse réception ici puis signe depuis /autorisations/:id/signature-ceee.
 */
export default function CeeeReceptionPage() {
  const navigate = useNavigate();
  const user = useAuthStore((s) => s.user);
  const [loading, setLoading] = useState(true);
  const [ats, setAts] = useState<AutorisationTravail[]>([]);
  const [error, setError] = useState<string | null>(null);

  const load = async () => {
    setLoading(true);
    setError(null);
    try {
      // Charge les AT filtrées pour ce CEEE (backend ne renvoie QUE celles liées à son service)
      const res = await autorisationTravailApi.findAll(0, 100);
      // Filtre côté client : AT où le CEEE doit agir (visite, rédaction, visa)
      const STATUTS_A_TRAITER = [
        'SOUMISE',          // legacy — AT soumise en attente de visa
        'AT_REDIGEE',       // AT rédigée, en attente signature CEEE
        'EN_VISITE_REDACTION', // §8.2-8.3 — Co-action CEEP+CEEE en cours
        'DEMANDE_CREEE',    // §8.1 — AT créée, CEEE notifié
      ];
      const soumises = (res.content || []).filter(
        (at: AutorisationTravail) => STATUTS_A_TRAITER.includes(at.statut)
      );
      setAts(soumises);
    } catch (e: any) {
      setError("Impossible de charger les AT en attente. Vérifiez votre connexion.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

  const getStatutChip = (statut: string) => {
    switch (statut) {
      case 'SOUMISE':
      case 'AT_REDIGEE':
        return <Chip label="✏️ AT Rédigée — à viser" color="warning" size="small" sx={{ fontWeight: 700 }} />;
      case 'EN_VISITE_REDACTION':
        return <Chip label="🔍 Visite & Rédaction" color="info" size="small" sx={{ fontWeight: 700 }} />;
      case 'DEMANDE_CREEE':
        return <Chip label="📋 Demande créée — notifié" color="primary" size="small" sx={{ fontWeight: 700 }} />;
      default:
        return <Chip label={statut} color="default" size="small" />;
    }
  };

  const hasAlreadySigned = (at: AutorisationTravail) => {
    // L'AT a été signée par ce CEEE si dateReceptionCeee est présente
    return !!(at as any).dateReceptionCeee;
  };

  return (
    <Box sx={{ p: 3 }}>
      {/* Header */}
      <Stack direction="row" sx={{ justifyContent: 'space-between', alignItems: 'flex-start', mb: 3 }}>
        <Box>
          <Typography variant="h4" sx={{ fontWeight: 800, color: '#0E2A21' }}>
            AT à Réceptionner & Viser
          </Typography>
          <Stack direction="row" spacing={1} sx={{ mt: 0.5, alignItems: 'center' }}>
            <Typography variant="body2" color="text.secondary">
              {user?.prenom} {user?.nom} — {user?.service?.nomService || 'Service exécutant'}
            </Typography>
            <Chip
              size="small"
              label="Position E (Exécutant)"
              color="primary"
              variant="outlined"
              sx={{ fontWeight: 700, fontSize: 11 }}
            />
          </Stack>
        </Box>
      </Stack>

      {/* Info banner */}
      <Alert severity="info" sx={{ mb: 3, borderRadius: 2 }}>
        <strong>Interface CEEE dédiée.</strong> Vous voyez uniquement les Autorisations de Travail transmises à votre
        service pour visa et exécution. Vous devez d'abord accuser réception, puis apposer votre visa depuis la page
        de signature dédiée.
      </Alert>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      {loading ? (
        <Box sx={{ display: 'flex', justifyContent: 'center', p: 6 }}>
          <CircularProgress color="primary" />
        </Box>
      ) : ats.length === 0 ? (
        <Paper sx={{ p: 4, textAlign: 'center', borderRadius: 3, border: '1px solid #D6E3DC' }}>
          <DoneAllIcon sx={{ fontSize: 48, color: '#1F4D3E', mb: 2 }} />
          <Typography variant="h6" sx={{ fontWeight: 700, color: '#5C6E67' }}>
            Aucune AT en attente de votre visa
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Toutes les AT transmises à votre service ont été signées, ou aucune n'a encore été soumise.
          </Typography>
        </Paper>
      ) : (
        <TableContainer component={Paper} sx={{ borderRadius: 3, border: '1px solid #D6E3DC', boxShadow: 'none' }}>
          <Table>
            <TableHead sx={{ bgcolor: '#EDF2EE' }}>
              <TableRow>
                <TableCell sx={{ fontWeight: 800, color: '#1F4D3E' }}>N° AT</TableCell>
                <TableCell sx={{ fontWeight: 800, color: '#1F4D3E' }}>Objet de l'intervention</TableCell>
                <TableCell sx={{ fontWeight: 800, color: '#1F4D3E' }}>Zone / Service P</TableCell>
                <TableCell sx={{ fontWeight: 800, color: '#1F4D3E' }}>Date prévue</TableCell>
                <TableCell sx={{ fontWeight: 800, color: '#1F4D3E' }}>Statut AT</TableCell>
                <TableCell sx={{ fontWeight: 800, color: '#1F4D3E' }}>Réception CEEE</TableCell>
                <TableCell align="right" sx={{ fontWeight: 800, color: '#1F4D3E' }}>Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {ats.map((at) => (
                <TableRow key={at.id} hover>
                  <TableCell sx={{ fontWeight: 800, color: '#1F4D3E' }}>
                    {at.numero}
                  </TableCell>

                  <TableCell>
                    <Typography variant="body2" sx={{ fontWeight: 600 }} noWrap>
                      {at.objet}
                    </Typography>
                    <Typography variant="caption" color="text.secondary" noWrap component="p">
                      {(at as any).descriptionTravaux || ''}
                    </Typography>
                  </TableCell>

                  <TableCell>
                    <Typography variant="body2">
                      {(at as any).zoneProprietaire?.nomZone || (at as any).site || '—'}
                    </Typography>
                    <Typography variant="caption" color="text.secondary">
                      {(at as any).entite || ''}
                    </Typography>
                  </TableCell>

                  <TableCell>
                    <Typography variant="body2">
                      {at.dateDebut ? new Date(at.dateDebut).toLocaleDateString('fr-FR') : '—'}
                    </Typography>
                    <Typography variant="caption" color="text.secondary">
                      {(at as any).heureDebut || ''} — {(at as any).heureFin || ''}
                    </Typography>
                  </TableCell>

                  <TableCell>{getStatutChip(at.statut)}</TableCell>

                  <TableCell>
                    {hasAlreadySigned(at) ? (
                      <Chip
                        label={`Reçue le ${new Date((at as any).dateReceptionCeee).toLocaleDateString('fr-FR')}`}
                        color="success"
                        size="small"
                        icon={<DoneAllIcon />}
                        sx={{ fontWeight: 700 }}
                      />
                    ) : (
                      <Chip
                        label="En attente d'accusé"
                        color="warning"
                        size="small"
                        icon={<MarkEmailUnreadIcon />}
                        variant="outlined"
                        sx={{ fontWeight: 700 }}
                      />
                    )}
                  </TableCell>

                  <TableCell align="right">
                    <Stack direction="row" spacing={0.5} sx={{ justifyContent: 'flex-end' }}>
                      <Tooltip title="Consulter l'AT complète">
                        <IconButton
                          size="small"
                          color="primary"
                          onClick={() => navigate(`/autorisations/${at.id}`)}
                        >
                          <VisibilityIcon fontSize="small" />
                        </IconButton>
                      </Tooltip>

                      <Tooltip title={hasAlreadySigned(at) ? 'Apposer votre visa (déjà réceptionné)' : 'Accuser réception et signer'}>
                        <Button
                          size="small"
                          variant={hasAlreadySigned(at) ? 'outlined' : 'contained'}
                          color="success"
                          startIcon={<CheckCircleIcon />}
                          onClick={() => navigate(`/autorisations/${at.id}/signature-ceee`)}
                          sx={{
                            fontWeight: 700,
                            textTransform: 'none',
                            borderRadius: 2,
                            px: 2,
                            bgcolor: hasAlreadySigned(at) ? 'transparent' : '#1F4D3E',
                            '&:hover': { bgcolor: hasAlreadySigned(at) ? '#EDF2EE' : '#2E624A' },
                          }}
                        >
                          {hasAlreadySigned(at) ? 'Viser' : 'Réceptionner & Viser'}
                        </Button>
                      </Tooltip>
                    </Stack>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      )}
    </Box>
  );
}
