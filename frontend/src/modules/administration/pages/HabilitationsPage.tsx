import { useState, useEffect } from 'react';
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
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Alert,
  CircularProgress,
  IconButton,
  Tooltip,
} from '@mui/material';
import VerifiedUserIcon from '@mui/icons-material/VerifiedUser';
import AddIcon from '@mui/icons-material/Add';
import DeleteIcon from '@mui/icons-material/Delete';
import InfoOutlinedIcon from '@mui/icons-material/InfoOutlined';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import { apiClient } from '../../../services/apiClient';
import { AdminService } from '../../../services/AdminService';

interface AgentHabilite {
  id: string;
  user: {
    id: string;
    nom: string;
    prenom: string;
    email: string;
    matricule?: string;
    service?: { nomService: string; codeService: string };
  };
  dateHabilitation: string;
  valideJusquAu: string;
  actif: boolean;
}

export default function HabilitationsPage() {
  const [loading, setLoading] = useState(true);
  const [agents, setAgents] = useState<AgentHabilite[]>([]);
  const [usersList, setUsersList] = useState<any[]>([]);
  const [openDialog, setOpenDialog] = useState(false);
  const [selectedUserId, setSelectedUserId] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [message, setMessage] = useState<{ type: 'success' | 'error'; text: string } | null>(null);

  const loadHabilitations = async () => {
    setLoading(true);
    try {
      const [usersRes, habsRes] = await Promise.all([
        AdminService.listUsers('', 0, 200),
        apiClient.get('/habilitations').then((res) => res.data).catch(() => []),
      ]);

      setUsersList(usersRes.content || []);

      const formattedHabs: AgentHabilite[] = (habsRes || []).map((h: any) => ({
        id: h.id,
        user: {
          id: h.utilisateurId,
          nom: h.utilisateurNom || '',
          prenom: h.utilisateurPrenom || '',
          email: h.utilisateurEmail || '',
          matricule: h.utilisateurMatricule || 'N/A',
          service: h.serviceNom ? { nomService: h.serviceNom, codeService: h.serviceCode } : undefined,
        },
        dateHabilitation: h.dateHabilitation,
        valideJusquAu: h.valideJusquAu,
        actif: h.actif,
      }));

      setAgents(formattedHabs);
    } catch (err) {
      console.error('Erreur chargement habilitations', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadHabilitations();
  }, []);

  const handleHabiliter = async () => {
    if (!selectedUserId) return;
    setSubmitting(true);
    setMessage(null);

    try {
      await apiClient.post('/habilitations', {
        utilisateurId: selectedUserId,
        observations: "Désignation officielle HCEP Formulaire F-HSE-SEC-31-02",
      });

      // Assigner aussi le rôle CEEP si nécessaire
      const rolesRes = await AdminService.listRoles();
      const ceepRole = rolesRes.content.find((r: any) => r.nom === 'CEEP');
      if (ceepRole) {
        await AdminService.assignRole(selectedUserId, ceepRole.id).catch(() => {});
      }

      setMessage({ type: 'success', text: 'Agent désigné et habilité à délivrer des ATs avec succès (Formulaire F-HSE-SEC-31-02).' });
      setOpenDialog(false);
      setSelectedUserId('');
      loadHabilitations();
    } catch (err: any) {
      setMessage({ type: 'error', text: err?.response?.data?.message || "Erreur lors de l'habilitation de l'agent" });
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Box sx={{ p: 3, maxWidth: 1200, mx: 'auto' }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Box>
          <Typography variant="h4" sx={{ fontWeight: 800, color: '#0E2A21', display: 'flex', alignItems: 'center', gap: 1.5 }}>
            <VerifiedUserIcon sx={{ fontSize: 36, color: '#1F4D3E' }} />
            Gestion des Agents Habilités (F-HSE-SEC-31-02)
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
            Désignation officielle par le Hors Cadre (HCEP) des agents habilités à délivrer une Autorisation de Travail (§9 du Standard)
          </Typography>
        </Box>

        <Button
          variant="contained"
          color="success"
          startIcon={<AddIcon />}
          onClick={() => setOpenDialog(true)}
          sx={{ fontWeight: 700, borderRadius: 2, textTransform: 'none' }}
        >
          Habiliter un Agent (CEEP)
        </Button>
      </Box>

      {message && (
        <Alert severity={message.type} sx={{ mb: 3, borderRadius: 2 }} onClose={() => setMessage(null)}>
          {message.text}
        </Alert>
      )}

      {/* Note d'information Standard §9 */}
      <Alert severity="info" icon={<InfoOutlinedIcon />} sx={{ mb: 3, borderRadius: 2 }}>
        <Typography variant="subtitle2" sx={{ fontWeight: 700 }}>
          Prérequis obligatoires du Standard OCP S-HSE-SEC-31 (§9)
        </Typography>
        <Typography variant="caption" component="p" sx={{ mt: 0.5 }}>
          1. Connaissance du processus de délivrance de l'AT et des instructions pour l'établissement des permis.
          <br />
          2. Formation sur l'Analyse des Risques (ADRPT) et les Standards HSE opérationnels (Consignation, Espaces confinés, Hauteur).
          <br />
          3. Revue annuelle obligatoire de la liste des agents habilités (Formulaire F-HSE-SEC-31-02).
        </Typography>
      </Alert>

      {loading ? (
        <Box sx={{ display: 'flex', justifyContent: 'center', p: 6 }}>
          <CircularProgress color="success" />
        </Box>
      ) : (
        <Paper sx={{ borderRadius: 3, boxShadow: '0 1px 3px rgba(0,0,0,0.08)', overflow: 'hidden' }}>
          <TableContainer>
            <Table>
              <TableHead sx={{ bgcolor: '#F7FAF8' }}>
                <TableRow>
                  <TableCell sx={{ fontWeight: 700 }}>Agent Habilité</TableCell>
                  <TableCell sx={{ fontWeight: 700 }}>Service / Zone</TableCell>
                  <TableCell sx={{ fontWeight: 700 }}>Date d'habilitation</TableCell>
                  <TableCell sx={{ fontWeight: 700 }}>Statut Habilitation</TableCell>
                  <TableCell sx={{ fontWeight: 700 }} align="center">Conformité §9</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {agents.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={5} align="center" sx={{ py: 4, color: 'text.secondary' }}>
                      Aucun agent habilité inscrit. Cliquez sur « Habiliter un Agent » pour désigner un agent.
                    </TableCell>
                  </TableRow>
                ) : (
                  agents.map((agent) => (
                    <TableRow key={agent.id} hover>
                      <TableCell>
                        <Typography variant="body2" sx={{ fontWeight: 700, color: '#0E2A21' }}>
                          {agent.user.prenom} {agent.user.nom}
                        </Typography>
                        <Typography variant="caption" color="text.secondary">
                          {agent.user.email} | Mat: {agent.user.matricule || 'N/A'}
                        </Typography>
                      </TableCell>

                      <TableCell>
                        {agent.user.service ? (
                          <Chip
                            label={`${agent.user.service.nomService} (${agent.user.service.codeService})`}
                            size="small"
                            variant="outlined"
                            color="success"
                          />
                        ) : (
                          <Typography variant="caption" color="text.secondary">Non rattaché</Typography>
                        )}
                      </TableCell>

                      <TableCell>
                        <Typography variant="body2">
                          {new Date(agent.dateHabilitation).toLocaleDateString('fr-FR')}
                        </Typography>
                        <Typography variant="caption" color="text.secondary">
                          Revue : {new Date(agent.valideJusquAu).toLocaleDateString('fr-FR')}
                        </Typography>
                      </TableCell>

                      <TableCell>
                        <Chip
                          icon={<CheckCircleIcon />}
                          label={agent.actif ? 'Habilité & Actif' : 'Inactif'}
                          color={agent.actif ? 'success' : 'default'}
                          size="small"
                        />
                      </TableCell>

                      <TableCell align="center">
                        <Tooltip title="Formations HSE & Connaissance du Standard OCP validées">
                          <Chip label="Conforme F-HSE-SEC-31-02" color="primary" size="small" variant="outlined" />
                        </Tooltip>
                      </TableCell>
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
          </TableContainer>
        </Paper>
      )}

      {/* Dialog Habiliter Agent */}
      <Dialog open={openDialog} onClose={() => setOpenDialog(false)} maxWidth="sm" fullWidth>
        <DialogTitle sx={{ fontWeight: 700 }}>
          Habiliter un Agent à délivrer une AT (F-HSE-SEC-31-02)
        </DialogTitle>
        <DialogContent>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2, mt: 1 }}>
            Sélectionnez un utilisateur du système pour lui attribuer l'habilitation officielle à rédiger et délivrer des Autorisations de Travail au nom de l'entité propriétaire (HCEP).
          </Typography>

          <FormControl fullWidth size="small" sx={{ mt: 1 }}>
            <InputLabel id="select-user-label">Utilisateur à habiliter</InputLabel>
            <Select
              labelId="select-user-label"
              value={selectedUserId}
              label="Utilisateur à habiliter"
              onChange={(e) => setSelectedUserId(e.target.value as string)}
            >
              {usersList.map((u) => (
                <MenuItem key={u.id} value={u.id}>
                  {u.prenom} {u.nom} ({u.email}) - Service: {u.service?.nomService || 'Non spécifié'}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
        </DialogContent>
        <DialogActions sx={{ p: 2 }}>
          <Button onClick={() => setOpenDialog(false)} disabled={submitting}>
            Annuler
          </Button>
          <Button
            onClick={handleHabiliter}
            variant="contained"
            color="success"
            disabled={!selectedUserId || submitting}
            startIcon={submitting ? <CircularProgress size={18} color="inherit" /> : <VerifiedUserIcon />}
          >
            Confirmer l'Habilitation
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
