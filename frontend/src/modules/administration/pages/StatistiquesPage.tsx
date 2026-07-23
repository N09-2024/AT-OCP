import { useEffect, useState } from 'react';
import {
  Box, Typography, Paper, CircularProgress,
  TextField, InputAdornment, MenuItem, Divider,
  Grid,
} from '@mui/material';
import { AdminService, type DashboardStats, type AdminStats } from '../../../services/AdminService';
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid,
  Tooltip as RechartsTooltip, ResponsiveContainer,
  PieChart, Pie, Cell, Legend,
  LineChart, Line,
} from 'recharts';
import {
  UserGroupIcon, ShieldCheckIcon, ClipboardDocumentCheckIcon,
  ClockIcon, MagnifyingGlassIcon, CheckCircleIcon,
} from '@heroicons/react/24/outline';

const PIE_COLORS = ['#16a34a', '#f59e0b', '#3b82f6', '#f97316', '#ef4444', '#94a3b8'];

const STATUS_LABELS: Record<string, string> = {
  EN_COURS: 'En cours', SOUMISE: 'Soumise', VALIDEE: 'Validée',
  EN_ATTENTE_VISA: 'En attente visa', REFUSEE: 'Refusée',
  CLOTUREE: 'Clôturée', BROUILLON: 'Brouillon', ARCHIVEE: 'Archivée',
};

const PRIMARY = '#16a34a';

function StatCard({ label, value, icon, color, bg, trend }: {
  label: string; value: number; icon: React.ReactNode;
  color: string; bg: string; trend?: string;
}) {
  return (
    <Paper
      sx={{
        p: 3,
        borderRadius: 3,
        boxShadow: '0 1px 3px rgba(0,0,0,0.07)',
        borderTop: `3px solid ${color}`,
        display: 'flex',
        flexDirection: 'column',
        gap: 1,
        height: '100%',
        transition: 'box-shadow 0.2s',
        '&:hover': { boxShadow: '0 4px 12px rgba(0,0,0,0.12)' },
      }}
    >
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
        <Typography variant="caption" sx={{ fontWeight: 700, color: 'text.secondary', textTransform: 'uppercase', fontSize: 11, letterSpacing: 0.5 }}>
          {label}
        </Typography>
        <Box sx={{ p: 1, borderRadius: 2, bgcolor: bg, color, display: 'flex' }}>
          {icon}
        </Box>
      </Box>
      <Typography variant="h3" sx={{ fontWeight: 900, color, lineHeight: 1 }}>
        {value}
      </Typography>
      {trend && (
        <Typography variant="caption" color="text.secondary" sx={{ fontSize: 11 }}>
          {trend}
        </Typography>
      )}
    </Paper>
  );
}

export default function StatistiquesPage() {
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [adminStats, setAdminStats] = useState<AdminStats | null>(null);
  const [loading, setLoading] = useState(true);
  const [monthSearch, setMonthSearch] = useState('');
  const [chartType, setChartType] = useState<'bar' | 'line'>('bar');
  const [statusSearch, setStatusSearch] = useState('');

  useEffect(() => {
    Promise.all([AdminService.getDashboardStats(), AdminService.getAdminStats()])
      .then(([dashStats, admStats]) => { setStats(dashStats); setAdminStats(admStats); })
      .catch(err => console.error('Error loading stats', err))
      .finally(() => setLoading(false));
  }, []);

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '60vh' }}>
        <CircularProgress color="success" />
      </Box>
    );
  }

  const statusData = stats?.statusDistribution
    ? Object.entries(stats.statusDistribution)
        .filter(([name]) => !statusSearch || (STATUS_LABELS[name] ?? name).toLowerCase().includes(statusSearch.toLowerCase()))
        .map(([name, value], index) => ({ name: STATUS_LABELS[name] ?? name, value, color: PIE_COLORS[index % PIE_COLORS.length] }))
    : [];

  const monthlyDataFull = stats?.monthlyStats
    ? stats.monthlyStats.map((item) => ({ name: item.mois, 'AT créées': item.total }))
    : [];

  const monthlyDataFiltered = monthSearch
    ? monthlyDataFull.filter(d => d.name.toLowerCase().includes(monthSearch.toLowerCase()))
    : monthlyDataFull;

  const kpiCards = [
    { label: 'Total Utilisateurs', value: adminStats?.totalUsers ?? 0, icon: <UserGroupIcon width={20} />, color: '#3b82f6', bg: '#eff6ff', trend: 'Comptes enregistrés' },
    { label: 'Utilisateurs Actifs', value: adminStats?.activeUsers ?? 0, icon: <CheckCircleIcon width={20} />, color: PRIMARY, bg: '#f0fdf4', trend: 'Comptes activés' },
    { label: 'Total Rôles', value: adminStats?.totalRoles ?? 0, icon: <ShieldCheckIcon width={20} />, color: '#8b5cf6', bg: '#f5f3ff', trend: 'Rôles configurés' },
    { label: 'Actions en attente', value: adminStats?.pendingActions ?? 0, icon: <ClockIcon width={20} />, color: '#f59e0b', bg: '#fffbeb', trend: 'À traiter' },
    { label: 'AT en cours', value: stats?.kpis?.autorisationsEnCours ?? 0, icon: <ClipboardDocumentCheckIcon width={20} />, color: PRIMARY, bg: '#f0fdf4', trend: 'Autorisations actives' },
    { label: 'Visas en attente', value: stats?.kpis?.visasEnAttente ?? 0, icon: <ClockIcon width={20} />, color: '#f97316', bg: '#fff7ed', trend: 'En attente de visa' },
  ];

  const fieldSx = {
    '& .MuiOutlinedInput-root': {
      bgcolor: '#f8fafc', borderRadius: 2,
      '& fieldset': { borderColor: '#e2e8f0' },
      '&:hover fieldset': { borderColor: '#cbd5e1' },
      '&.Mui-focused fieldset': { borderColor: PRIMARY, borderWidth: '1px' },
    }
  };

  return (
    <Box sx={{ pb: 4 }}>
      {/* Header */}
      <Box sx={{ mb: 4 }}>
        <Typography variant="h5" sx={{ fontWeight: 800, color: 'text.primary' }}>
          Statistiques Globales
        </Typography>
        <Typography variant="body2" color="text.secondary">
          Analyse de l'activité de la plateforme — données en temps réel
        </Typography>
      </Box>

      {/* KPI Grid */}
      <Grid container spacing={2.5} sx={{ mb: 4 }}>
        {kpiCards.map((kpi) => (
          <Grid key={kpi.label} size={{ xs: 6, sm: 4, md: 2 }}>
            <StatCard {...kpi} />
          </Grid>
        ))}
      </Grid>

      <Divider sx={{ mb: 4 }} />

      {/* Charts */}
      <Grid container spacing={3}>
        {/* Monthly Chart */}
        <Grid size={{ xs: 12, lg: 7 }}>
          <Paper sx={{ p: 3, borderRadius: 3, boxShadow: '0 1px 3px rgba(0,0,0,0.07)', height: '100%' }}>
            {/* Chart header */}
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3, flexWrap: 'wrap', gap: 2 }}>
              <Box>
                <Typography variant="subtitle1" sx={{ fontWeight: 700 }}>
                  Évolution Mensuelle des AT
                </Typography>
                <Typography variant="caption" color="text.secondary">
                  Nombre d'autorisations créées par mois
                </Typography>
              </Box>
              <Box sx={{ display: 'flex', gap: 1.5, alignItems: 'center', flexWrap: 'wrap' }}>
                <TextField
                  size="small"
                  placeholder="Filtrer par mois…"
                  value={monthSearch}
                  onChange={e => setMonthSearch(e.target.value)}
                  sx={{ width: 160, ...fieldSx }}
                  slotProps={{
                    input: {
                      startAdornment: (
                        <InputAdornment position="start">
                          <MagnifyingGlassIcon width={14} color="#94a3b8" />
                        </InputAdornment>
                      ),
                    }
                  }}
                />
                <TextField
                  select size="small" value={chartType}
                  onChange={e => setChartType(e.target.value as 'bar' | 'line')}
                  sx={{ width: 110, ...fieldSx }}
                >
                  <MenuItem value="bar">Barres</MenuItem>
                  <MenuItem value="line">Lignes</MenuItem>
                </TextField>
              </Box>
            </Box>

            <Box sx={{ height: 300 }}>
              {monthlyDataFiltered.length > 0 ? (
                <ResponsiveContainer>
                  {chartType === 'bar' ? (
                    <BarChart data={monthlyDataFiltered} margin={{ top: 5, right: 10, left: -20, bottom: 5 }}>
                      <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f1f5f9" />
                      <XAxis dataKey="name" axisLine={false} tickLine={false} tick={{ fontSize: 12, fill: '#94a3b8' }} />
                      <YAxis axisLine={false} tickLine={false} tick={{ fontSize: 12, fill: '#94a3b8' }} />
                      <RechartsTooltip contentStyle={{ borderRadius: 8, border: 'none', boxShadow: '0 4px 12px rgba(0,0,0,0.1)' }} />
                      <Bar dataKey="AT créées" fill={PRIMARY} radius={[6, 6, 0, 0]} maxBarSize={50} />
                    </BarChart>
                  ) : (
                    <LineChart data={monthlyDataFiltered} margin={{ top: 5, right: 10, left: -20, bottom: 5 }}>
                      <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f1f5f9" />
                      <XAxis dataKey="name" axisLine={false} tickLine={false} tick={{ fontSize: 12, fill: '#94a3b8' }} />
                      <YAxis axisLine={false} tickLine={false} tick={{ fontSize: 12, fill: '#94a3b8' }} />
                      <RechartsTooltip contentStyle={{ borderRadius: 8, border: 'none', boxShadow: '0 4px 12px rgba(0,0,0,0.1)' }} />
                      <Line type="monotone" dataKey="AT créées" stroke={PRIMARY} strokeWidth={2.5} dot={{ fill: PRIMARY, r: 4 }} />
                    </LineChart>
                  )}
                </ResponsiveContainer>
              ) : (
                <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: '100%', flexDirection: 'column', gap: 1 }}>
                  <ClipboardDocumentCheckIcon width={40} color="#cbd5e1" />
                  <Typography color="text.disabled" variant="body2">Aucune donnée mensuelle disponible</Typography>
                </Box>
              )}
            </Box>
          </Paper>
        </Grid>

        {/* Status Pie */}
        <Grid size={{ xs: 12, lg: 5 }}>
          <Paper sx={{ p: 3, borderRadius: 3, boxShadow: '0 1px 3px rgba(0,0,0,0.07)', height: '100%' }}>
            <Box sx={{ mb: 3 }}>
              <Typography variant="subtitle1" sx={{ fontWeight: 700 }}>
                Répartition par Statut
              </Typography>
              <Typography variant="caption" color="text.secondary">
                Distribution des autorisations selon leur statut
              </Typography>
            </Box>

            <TextField
              size="small" fullWidth
              placeholder="Filtrer par statut…"
              value={statusSearch}
              onChange={e => setStatusSearch(e.target.value)}
              sx={{ mb: 2.5, ...fieldSx }}
              slotProps={{
                input: {
                  startAdornment: (
                    <InputAdornment position="start">
                      <MagnifyingGlassIcon width={14} color="#94a3b8" />
                    </InputAdornment>
                  ),
                }
              }}
            />

            <Box sx={{ height: 280 }}>
              {statusData.length > 0 ? (
                <ResponsiveContainer>
                  <PieChart>
                    <Pie
                      data={statusData} cx="50%" cy="50%"
                      innerRadius={70} outerRadius={100}
                      paddingAngle={3} dataKey="value" stroke="none"
                    >
                      {statusData.map((entry, index) => (
                        <Cell key={`cell-${index}`} fill={entry.color} />
                      ))}
                    </Pie>
                    <RechartsTooltip contentStyle={{ borderRadius: 8, border: 'none', boxShadow: '0 4px 12px rgba(0,0,0,0.1)' }} />
                    <Legend
                      iconType="circle"
                      wrapperStyle={{ fontSize: 12, paddingTop: 8 }}
                      formatter={(value) => <span style={{ color: '#475569' }}>{value}</span>}
                    />
                  </PieChart>
                </ResponsiveContainer>
              ) : (
                <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: '100%', flexDirection: 'column', gap: 1 }}>
                  <ShieldCheckIcon width={40} color="#cbd5e1" />
                  <Typography color="text.disabled" variant="body2">Aucune donnée disponible</Typography>
                </Box>
              )}
            </Box>
          </Paper>
        </Grid>
      </Grid>
    </Box>
  );
}
