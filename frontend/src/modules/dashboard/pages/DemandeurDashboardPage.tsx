import React, { useEffect, useState } from 'react';
import { Box, Typography, Paper, Button, Grid, CircularProgress, Chip, IconButton } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '../../../store/authStore';
import {
  PlusCircleIcon,
  ClipboardDocumentCheckIcon,
  DocumentTextIcon,
  ShieldCheckIcon,
  ArchiveBoxIcon,
  UserGroupIcon,
  EllipsisVerticalIcon
} from '@heroicons/react/24/outline';
import { DashboardService, DashboardData } from '../../../services/DashboardService';
import { DataGrid, GridColDef } from '@mui/x-data-grid';
import { PieChart, Pie, Cell, Tooltip, ResponsiveContainer, Legend } from 'recharts';
import { Timeline, TimelineItem, TimelineSeparator, TimelineConnector, TimelineContent, TimelineDot } from '@mui/lab';

const COLORS = ['#10b981', '#f59e0b', '#3b82f6', '#8b5cf6', '#ef4444', '#6b7280'];

export default function DemandeurDashboardPage() {
  const navigate = useNavigate();
  const user = useAuthStore((s) => s.user);
  const [data, setData] = useState<DashboardData | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    DashboardService.getStats()
      .then((res) => {
        setData(res);
      })
      .catch((err) => console.error('Erreur chargement dashboard', err))
      .finally(() => setLoading(false));
  }, []);

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
        <CircularProgress />
      </Box>
    );
  }

  const kpis = data?.kpis || { autorisationsEnCours: 0, visasEnAttente: 0, permisActifs: 0, receptionsEnAttente: 0, totalArchives: 0 };
  const recentATs = data?.recentAutorisations || [];
  const statusDist = data?.statusDistribution || {};

  const pieData = Object.keys(statusDist).map((key) => ({
    name: key,
    value: statusDist[key],
  }));

  const columns: GridColDef[] = [
    { field: 'id', headerName: 'N° AT', width: 130 },
    { field: 'titre', headerName: 'Titre', flex: 1 },
    { field: 'installation', headerName: 'Installation', flex: 1 },
    {
      field: 'statut',
      headerName: 'Statut',
      width: 150,
      renderCell: (params) => {
        let color: "default" | "primary" | "secondary" | "error" | "info" | "success" | "warning" = "default";
        switch (params.value) {
          case 'EN_COURS': color = 'success'; break;
          case 'SOUMISE': color = 'warning'; break;
          case 'VALIDEE': color = 'success'; break;
          case 'EN_ATTENTE_VISA': color = 'warning'; break;
          case 'REFUSEE': color = 'error'; break;
        }
        return <Chip label={params.value} color={color} size="small" sx={{ fontWeight: 600 }} />;
      }
    },
    { field: 'echeance', headerName: 'Échéance', width: 130 },
    {
      field: 'actions',
      headerName: '',
      width: 50,
      renderCell: (params) => (
        <IconButton size="small" onClick={() => navigate(`/autorisations/${params.row.id}`)}>
          <EllipsisVerticalIcon width={20} />
        </IconButton>
      )
    }
  ];

  return (
    <Box sx={{ p: 3, bgcolor: '#f8fafc', minHeight: '100vh' }}>
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
          color="primary"
          startIcon={<PlusCircleIcon width={20} />}
          onClick={() => navigate('/autorisations/nouvelle')}
          sx={{ borderRadius: 2, textTransform: 'none', fontWeight: 600, px: 3 }}
        >
          Nouvelle AT
        </Button>
      </Box>

      {/* KPIs */}
      <Grid container spacing={2} sx={{ mb: 4 }}>
        {[
          { label: 'AUTORISATIONS', value: `${kpis.autorisationsEnCours} En cours`, icon: <ClipboardDocumentCheckIcon width={32} color="#10b981" /> },
          { label: 'VISAS EN ATTENTE', value: `${kpis.visasEnAttente} À valider`, icon: <UserGroupIcon width={32} color="#10b981" /> },
          { label: 'PERMIS ACTIFS', value: `${kpis.permisActifs} Valides`, icon: <ShieldCheckIcon width={32} color="#10b981" /> },
          { label: 'RÉCEPTIONS', value: `${kpis.receptionsEnAttente} En attente`, icon: <ArchiveBoxIcon width={32} color="#10b981" /> },
          { label: 'ARCHIVES', value: `${kpis.totalArchives} Documents`, icon: <DocumentTextIcon width={32} color="#10b981" /> },
        ].map((kpi, idx) => (
          <Grid item xs={12} sm={6} md={2.4} key={idx}>
            <Paper sx={{ p: 2, borderRadius: 3, display: 'flex', flexDirection: 'column', height: '100%' }}>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2 }}>
                <Box>
                  <Typography variant="caption" sx={{ fontWeight: 700, color: 'text.secondary' }}>
                    {kpi.label}
                  </Typography>
                  <Typography variant="h5" sx={{ fontWeight: 800, mt: 0.5 }}>
                    {kpi.value.split(' ')[0]}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    {kpi.value.split(' ').slice(1).join(' ')}
                  </Typography>
                </Box>
                <Box sx={{ p: 1, bgcolor: '#ecfdf5', borderRadius: 2, height: 'fit-content' }}>
                  {kpi.icon}
                </Box>
              </Box>
              <Typography variant="caption" color="primary" sx={{ mt: 'auto', cursor: 'pointer', fontWeight: 600 }}>
                Voir plus →
              </Typography>
            </Paper>
          </Grid>
        ))}
      </Grid>

      <Grid container spacing={3}>
        {/* Main Section */}
        <Grid item xs={12} md={8}>
          <Paper sx={{ p: 3, borderRadius: 3, mb: 3 }}>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
              <Typography variant="h6" sx={{ fontWeight: 700 }}>
                Mes autorisations de travail
              </Typography>
              <Typography variant="caption" color="primary" sx={{ cursor: 'pointer', fontWeight: 600 }} onClick={() => navigate('/autorisations')}>
                Voir toutes →
              </Typography>
            </Box>
            <Box sx={{ height: 400, width: '100%' }}>
              <DataGrid
                rows={recentATs}
                columns={columns}
                hideFooter
                disableRowSelectionOnClick
                sx={{ border: 'none', '& .MuiDataGrid-cell': { borderBottom: '1px solid #f1f5f9' } }}
              />
            </Box>
          </Paper>

          <Grid container spacing={3}>
            {/* Pie Chart */}
            <Grid item xs={12} sm={6}>
              <Paper sx={{ p: 3, borderRadius: 3, height: '100%' }}>
                <Typography variant="h6" sx={{ fontWeight: 700, mb: 2 }}>
                  Répartition par statut
                </Typography>
                <Box sx={{ height: 250 }}>
                  <ResponsiveContainer width="100%" height="100%">
                    <PieChart>
                      <Pie
                        data={pieData}
                        innerRadius={60}
                        outerRadius={80}
                        paddingAngle={5}
                        dataKey="value"
                      >
                        {pieData.map((entry, index) => (
                          <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                        ))}
                      </Pie>
                      <Tooltip />
                      <Legend verticalAlign="middle" align="right" layout="vertical" />
                    </PieChart>
                  </ResponsiveContainer>
                </Box>
              </Paper>
            </Grid>
            {/* Recent Activities */}
            <Grid item xs={12} sm={6}>
              <Paper sx={{ p: 3, borderRadius: 3, height: '100%' }}>
                <Typography variant="h6" sx={{ fontWeight: 700, mb: 2 }}>
                  Activités récentes
                </Typography>
                {/* Fallback mock list since API doesn't provide activity logs yet */}
                <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                  {[
                    { text: 'AT-2026-1258 a été validée par Ahmed', time: 'Il y a 25 min', icon: <ClipboardDocumentCheckIcon width={24} color="#10b981"/>, bg: '#ecfdf5' },
                    { text: 'Visa requis pour AT-2026-1255', time: 'Il y a 1 heure', icon: <UserGroupIcon width={24} color="#f59e0b"/>, bg: '#fffbeb' },
                    { text: 'Permis de feu PFE-2026-045 expire demain', time: 'Il y a 2 heures', icon: <ShieldCheckIcon width={24} color="#10b981"/>, bg: '#ecfdf5' },
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
                <Typography variant="caption" color="primary" sx={{ display: 'block', mt: 2, cursor: 'pointer', fontWeight: 600 }}>
                  Voir toutes les activités →
                </Typography>
              </Paper>
            </Grid>
          </Grid>
        </Grid>

        {/* Right Section: Workflow */}
        <Grid item xs={12} md={4}>
          <Paper sx={{ p: 3, borderRadius: 3, height: '100%' }}>
            <Typography variant="h6" sx={{ fontWeight: 700, mb: 2 }}>
              Étapes du workflow
            </Typography>
            <Timeline sx={{ p: 0, [`& .MuiTimelineItem-root:before`]: { flex: 0, padding: 0 } }}>
              {[
                { label: '1. Brouillon', desc: 'Création de la demande', active: true },
                { label: '2. Soumise', desc: 'Demande soumise', active: true },
                { label: '3. Analyse des risques', desc: 'Évaluation et mesures', active: true },
                { label: '4. Autorisation', desc: 'Validation et visas', active: true },
                { label: '5. En cours', desc: 'Travaux en exécution', active: false },
                { label: '6. Réception', desc: 'Clôture et réception', active: false },
                { label: '7. Archivage', desc: 'Documents archivés', active: false },
              ].map((step, idx) => (
                <TimelineItem key={idx}>
                  <TimelineSeparator>
                    <TimelineDot color={step.active ? 'success' : 'grey'} variant={step.active ? 'filled' : 'outlined'} />
                    {idx < 6 && <TimelineConnector sx={{ bgcolor: step.active ? 'success.main' : 'grey.300' }} />}
                  </TimelineSeparator>
                  <TimelineContent sx={{ py: '12px', px: 2 }}>
                    <Typography variant="body2" sx={{ fontWeight: 700 }}>{step.label}</Typography>
                    <Typography variant="caption" color="text.secondary">{step.desc}</Typography>
                  </TimelineContent>
                </TimelineItem>
              ))}
            </Timeline>
          </Paper>
        </Grid>
      </Grid>
    </Box>
  );
}
