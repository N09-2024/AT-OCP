import { useEffect, useState } from 'react';
import {
  Box, Typography, Paper, Button, Grid, CircularProgress, Chip, IconButton,
  Table, TableBody, TableCell, TableContainer, TableHead, TableRow,
} from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '../../../store/authStore';
import {
  PlusCircleIcon,
  ClipboardDocumentCheckIcon,
  DocumentTextIcon,
  ShieldCheckIcon,
  ArchiveBoxIcon,
  UserGroupIcon,
  EllipsisVerticalIcon,
} from '@heroicons/react/24/outline';
import { DashboardService, type DashboardData } from '../../../services/DashboardService';
import { PieChart, Pie, Cell, Tooltip, ResponsiveContainer, Legend } from 'recharts';

const COLORS = ['#7FC8A9', '#A87532', '#1F4D3E', '#3C7A5C', '#9A3D2F', '#5C6E67'];

const WORKFLOW_STEPS = [
  { label: '1. Brouillon', desc: 'Création de la demande', active: true },
  { label: '2. Soumise', desc: 'Demande soumise', active: true },
  { label: '3. Analyse des risques', desc: 'Évaluation et mesures', active: true },
  { label: '4. Autorisation', desc: 'Validation et visas', active: true },
  { label: '5. En cours', desc: 'Travaux en exécution', active: false },
  { label: '6. Réception', desc: 'Clôture et réception', active: false },
  { label: '7. Archivage', desc: 'Documents archivés', active: false },
];

export default function DemandeurDashboardPage() {
  const navigate = useNavigate();
  const user = useAuthStore((s) => s.user);
  const [data, setData] = useState<DashboardData | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    DashboardService.getStats()
      .then((res) => setData(res))
      .catch((err) => console.error('Erreur chargement dashboard', err))
      .finally(() => setLoading(false));
  }, []);

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
        <CircularProgress color="success" />
      </Box>
    );
  }

  const kpis = data?.kpis || { autorisationsEnCours: 0, visasEnAttente: 0, permisActifs: 0, receptionsEnAttente: 0, totalArchives: 0 };
  const recentATs = data?.recentAutorisations || [];
  const statusDist = data?.statusDistribution || {};
  const pieData = Object.keys(statusDist).map((key) => ({ name: key, value: statusDist[key] }));

  return (
    <Box sx={{ p: 3, bgcolor: '#F7FAF8', minHeight: '100vh' }}>
      {/* Header */}
      <Box sx={{ mb: 4, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Box>
          <Typography variant="h5" sx={{ fontWeight: 800, color: 'text.primary' }}>
            Bonjour, {user?.prenom} {user?.nom}
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
            Voici un aperçu de vos activités aujourd'hui.
          </Typography>
        </Box>
        <Button
          variant="contained"
          startIcon={<PlusCircleIcon width={20} />}
          onClick={() => navigate('/autorisations/nouvelle')}
          sx={{ borderRadius: 2, textTransform: 'none', fontWeight: 600, px: 3, bgcolor: '#1F4D3E', '&:hover': { bgcolor: '#2E624A' } }}
        >
          Nouvelle AT
        </Button>
      </Box>

      {/* KPIs */}
      <Grid container spacing={2} sx={{ mb: 4 }}>
        {[
          { label: 'AUTORISATIONS EN COURS', value: kpis.autorisationsEnCours, icon: <ClipboardDocumentCheckIcon width={32} color="#7FC8A9" />, link: '/autorisations' },
          { label: 'VISAS EN ATTENTE', value: kpis.visasEnAttente, icon: <UserGroupIcon width={32} color="#7FC8A9" />, link: '/autorisations?filtre=SOUMISE' },
          { label: 'PERMIS ACTIFS', value: kpis.permisActifs, icon: <ShieldCheckIcon width={32} color="#7FC8A9" />, link: '/permis' },
          { label: 'RÉCEPTIONS', value: kpis.receptionsEnAttente, icon: <ArchiveBoxIcon width={32} color="#7FC8A9" />, link: '/receptions' },
          { label: 'ARCHIVES', value: kpis.totalArchives, icon: <DocumentTextIcon width={32} color="#7FC8A9" />, link: '/archives' },
        ].map((kpi) => (
          <Grid size={{ xs: 12, sm: 6, md: 2.4 }} key={kpi.label}>
            <Paper
              sx={{ p: 2.5, borderRadius: 3, cursor: 'pointer', '&:hover': { boxShadow: 4 }, transition: 'box-shadow .2s' }}
              onClick={() => navigate(kpi.link)}
            >
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                <Box>
                  <Typography variant="caption" sx={{ fontWeight: 700, color: 'text.secondary', fontSize: 10, letterSpacing: 0.5 }}>
                    {kpi.label}
                  </Typography>
                  <Typography variant="h4" sx={{ fontWeight: 900, mt: 0.5, color: '#0E2A21' }}>
                    {kpi.value}
                  </Typography>
                </Box>
                <Box sx={{ p: 1, bgcolor: '#E2F0E8', borderRadius: 2 }}>
                  {kpi.icon}
                </Box>
              </Box>
            </Paper>
          </Grid>
        ))}
      </Grid>

      <Grid container spacing={3}>
        {/* Main Section */}
        <Grid size={{ xs: 12, md: 8 }}>
          {/* AT Table */}
          <Paper sx={{ p: 3, borderRadius: 3, mb: 3 }}>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
              <Typography variant="h6" sx={{ fontWeight: 700 }}>Mes autorisations de travail</Typography>
              <Typography
                variant="caption" color="primary"
                sx={{ cursor: 'pointer', fontWeight: 600 }}
                onClick={() => navigate('/autorisations')}
              >
                Voir toutes →
              </Typography>
            </Box>
            <TableContainer>
              <Table size="small">
                <TableHead sx={{ bgcolor: '#F7FAF8' }}>
                  <TableRow>
                    <TableCell sx={{ fontWeight: 700 }}>N° AT</TableCell>
                    <TableCell sx={{ fontWeight: 700 }}>Objet</TableCell>
                    <TableCell sx={{ fontWeight: 700 }}>Statut</TableCell>
                    <TableCell sx={{ fontWeight: 700 }}>Échéance</TableCell>
                    <TableCell sx={{ fontWeight: 700 }} />
                  </TableRow>
                </TableHead>
                <TableBody>
                  {recentATs.length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={5} align="center" sx={{ py: 4 }}>
                        <Typography variant="body2" color="text.secondary">Aucune autorisation récente.</Typography>
                      </TableCell>
                    </TableRow>
                  ) : (
                    recentATs.map((at: any) => (
                      <TableRow key={at.id} hover>
                        <TableCell sx={{ fontWeight: 700, color: '#1F4D3E' }}>{at.id}</TableCell>
                        <TableCell sx={{ maxWidth: 200 }}>
                          <Typography variant="body2" noWrap>{at.titre || at.objet}</Typography>
                        </TableCell>
                        <TableCell>
                          <Chip label={at.statut} size="small" color={at.statut === 'VALIDEE' ? 'success' : at.statut === 'SOUMISE' ? 'warning' : at.statut === 'REJETEE' ? 'error' : 'default'} sx={{ fontWeight: 600 }} />
                        </TableCell>
                        <TableCell>{at.echeance || '-'}</TableCell>
                        <TableCell>
                          <IconButton size="small" onClick={() => navigate(`/autorisations/${at.id}`)}>
                            <EllipsisVerticalIcon width={18} />
                          </IconButton>
                        </TableCell>
                      </TableRow>
                    ))
                  )}
                </TableBody>
              </Table>
            </TableContainer>
          </Paper>

          {/* Charts row */}
          <Grid container spacing={3}>
            <Grid size={{ xs: 12, sm: 6 }}>
              <Paper sx={{ p: 3, borderRadius: 3, height: '100%' }}>
                <Typography variant="h6" sx={{ fontWeight: 700, mb: 2 }}>Répartition par statut</Typography>
                <Box sx={{ height: 220 }}>
                  <ResponsiveContainer width="100%" height="100%">
                    <PieChart>
                      <Pie data={pieData} innerRadius={55} outerRadius={80} paddingAngle={4} dataKey="value">
                        {pieData.map((_e, idx) => <Cell key={idx} fill={COLORS[idx % COLORS.length]} />)}
                      </Pie>
                      <Tooltip />
                      <Legend verticalAlign="middle" align="right" layout="vertical" />
                    </PieChart>
                  </ResponsiveContainer>
                </Box>
              </Paper>
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <Paper sx={{ p: 3, borderRadius: 3, height: '100%' }}>
                <Typography variant="h6" sx={{ fontWeight: 700, mb: 2 }}>Activités récentes</Typography>
                <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                  {[
                    { text: 'AT-2026-1258 validée par Ahmed', time: 'Il y a 25 min', bg: '#E2F0E8', icon: <ClipboardDocumentCheckIcon width={22} color="#7FC8A9" /> },
                    { text: 'Visa requis pour AT-2026-1255', time: 'Il y a 1 heure', bg: '#F6EEDC', icon: <UserGroupIcon width={22} color="#A87532" /> },
                    { text: 'Permis PFE-2026-045 expire demain', time: 'Il y a 2 heures', bg: '#E2F0E8', icon: <ShieldCheckIcon width={22} color="#7FC8A9" /> },
                  ].map((act, i) => (
                    <Box key={i} sx={{ display: 'flex', gap: 2, alignItems: 'center' }}>
                      <Box sx={{ p: 1, bgcolor: act.bg, borderRadius: 2 }}>{act.icon}</Box>
                      <Box>
                        <Typography variant="body2" sx={{ fontWeight: 600 }}>{act.text}</Typography>
                        <Typography variant="caption" color="text.secondary">{act.time}</Typography>
                      </Box>
                    </Box>
                  ))}
                </Box>
              </Paper>
            </Grid>
          </Grid>
        </Grid>

        {/* Right: Workflow Steps */}
        <Grid size={{ xs: 12, md: 4 }}>
          <Paper sx={{ p: 3, borderRadius: 3, height: '100%' }}>
            <Typography variant="h6" sx={{ fontWeight: 700, mb: 2 }}>Étapes du workflow AT</Typography>
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 0 }}>
              {WORKFLOW_STEPS.map((step, idx) => (
                <Box key={idx} sx={{ display: 'flex', gap: 2, alignItems: 'flex-start' }}>
                  {/* Dot + line */}
                  <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', minWidth: 20 }}>
                    <Box sx={{
                      width: 14, height: 14, borderRadius: '50%', mt: 0.5,
                      bgcolor: step.active ? '#1F4D3E' : '#D6E3DC',
                      border: step.active ? '2px solid #1F4D3E' : '2px solid #D6E3DC',
                      flexShrink: 0,
                    }} />
                    {idx < WORKFLOW_STEPS.length - 1 && (
                      <Box sx={{ width: 2, flex: 1, minHeight: 28, bgcolor: step.active ? '#1F4D3E' : '#D6E3DC' }} />
                    )}
                  </Box>
                  {/* Content */}
                  <Box sx={{ pb: 2 }}>
                    <Typography variant="body2" sx={{ fontWeight: 700, color: step.active ? '#1F4D3E' : '#5C6E67' }}>
                      {step.label}
                    </Typography>
                    <Typography variant="caption" color="text.secondary">{step.desc}</Typography>
                  </Box>
                </Box>
              ))}
            </Box>
          </Paper>
        </Grid>
      </Grid>
    </Box>
  );
}
