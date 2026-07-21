import { useEffect, useState } from 'react';
import {
  Box, Typography, Card, CircularProgress,
  TextField, InputAdornment, Select, MenuItem,
  FormControl, InputLabel, Stack, Button, Chip, Divider,
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
  ClockIcon, MagnifyingGlassIcon, ArrowPathIcon,
} from '@heroicons/react/24/outline';

const PIE_COLORS = ['#16a34a', '#f59e0b', '#3b82f6', '#f97316', '#ef4444', '#94a3b8'];

const STATUS_LABELS: Record<string, string> = {
  EN_COURS: 'En cours', SOUMISE: 'Soumise', VALIDEE: 'Validée',
  EN_ATTENTE_VISA: 'En attente visa', REFUSEE: 'Refusée',
  CLOTUREE: 'Clôturée', BROUILLON: 'Brouillon', ARCHIVEE: 'Archivée',
};

export default function StatistiquesPage() {
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [adminStats, setAdminStats] = useState<AdminStats | null>(null);
  const [loading, setLoading] = useState(true);

  // Filters for monthly chart
  const [monthSearch, setMonthSearch] = useState('');
  const [chartType, setChartType] = useState<'bar' | 'line'>('bar');

  // Filters for status chart
  const [statusSearch, setStatusSearch] = useState('');

  useEffect(() => {
    Promise.all([
      AdminService.getDashboardStats(),
      AdminService.getAdminStats(),
    ])
      .then(([dashStats, admStats]) => {
        setStats(dashStats);
        setAdminStats(admStats);
      })
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

  // Status pie data (with filter)
  const statusData = stats?.statusDistribution
    ? Object.entries(stats.statusDistribution)
        .filter(([name]) =>
          !statusSearch || (STATUS_LABELS[name] ?? name).toLowerCase().includes(statusSearch.toLowerCase())
        )
        .map(([name, value], index) => ({
          name: STATUS_LABELS[name] ?? name,
          value,
          color: PIE_COLORS[index % PIE_COLORS.length],
        }))
    : [];

  // Monthly bar data (with filter)
  const monthlyDataFull = stats?.monthlyStats
    ? stats.monthlyStats.map((item) => ({
        name: item.mois,
        'AT créées': item.total,
      }))
    : [];

  const monthlyDataFiltered = monthSearch
    ? monthlyDataFull.filter(d => d.name.toLowerCase().includes(monthSearch.toLowerCase()))
    : monthlyDataFull;

  const KPI_PLATFORM = [
    {
      label: 'Total Utilisateurs',
      value: adminStats?.totalUsers ?? 0,
      icon: <UserGroupIcon width={22} />,
      color: '#3b82f6',
      bg: '#eff6ff',
    },
    {
      label: 'Utilisateurs Actifs',
      value: adminStats?.activeUsers ?? 0,
      icon: <UserGroupIcon width={22} />,
      color: '#16a34a',
      bg: '#f0fdf4',
    },
    {
      label: 'Total Rôles',
      value: adminStats?.totalRoles ?? 0,
      icon: <ShieldCheckIcon width={22} />,
      color: '#8b5cf6',
      bg: '#f5f3ff',
    },
    {
      label: 'Actions en attente',
      value: adminStats?.pendingActions ?? 0,
      icon: <ClockIcon width={22} />,
      color: '#f59e0b',
      bg: '#fffbeb',
    },
    {
      label: 'AT en cours',
      value: stats?.kpis?.autorisationsEnCours ?? 0,
      icon: <ClipboardDocumentCheckIcon width={22} />,
      color: '#16a34a',
      bg: '#f0fdf4',
    },
    {
      label: 'Visas en attente',
      value: stats?.kpis?.visasEnAttente ?? 0,
      icon: <ClockIcon width={22} />,
      color: '#f97316',
      bg: '#fff7ed',
    },
  ];

  return (
    <Box sx={{ pb: 4 }}>
      {/* Header */}
      <Box sx={{ mb: 4 }}>
        <Typography variant="h4" sx={{ fontWeight: 800, color: '#16a34a', mb: 0.5 }}>
          Statistiques Globales
        </Typography>
        <Typography variant="body1" color="text.secondary">
          Analyse détaillée de l'activité de la plateforme — données en temps réel
        </Typography>
      </Box>

      {/* KPI Cards Row */}
      <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap', mb: 4 }}>
        {KPI_PLATFORM.map((kpi) => (
          <Card
            key={kpi.label}
            sx={{
              minWidth: 180, flexShrink: 0, p: 2.5, borderRadius: 3,
              boxShadow: '0 1px 4px rgba(0,0,0,0.06)',
              transition: 'transform 0.2s',
              '&:hover': { transform: 'translateY(-2px)' },
            }}
          >
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, mb: 1.5 }}>
              <Box sx={{ p: 1, borderRadius: 2, bgcolor: kpi.bg, color: kpi.color }}>
                {kpi.icon}
              </Box>
              <Typography variant="caption" sx={{ fontWeight: 700, color: 'text.secondary', textTransform: 'uppercase', fontSize: 10 }}>
                {kpi.label}
              </Typography>
            </Box>
            <Typography variant="h4" sx={{ fontWeight: 900, color: kpi.color, lineHeight: 1 }}>
              {kpi.value}
            </Typography>
          </Card>
        ))}
      </Box>

      <Divider sx={{ mb: 4 }} />

      {/* Charts Row */}
      <Box sx={{ display: 'flex', gap: 3, flexDirection: { xs: 'column', lg: 'row' }, mb: 3 }}>

        {/* Monthly Chart with filters */}
        <Card sx={{ p: 3, flex: '2 1 0', borderRadius: 3, boxShadow: '0 1px 4px rgba(0,0,0,0.06)' }}>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2.5, flexWrap: 'wrap', gap: 2 }}>
            <Typography variant="h6" sx={{ fontWeight: 700 }}>
              Évolution Mensuelle des AT
            </Typography>
            <Stack direction="row" spacing={1.5} alignItems="center">
              <TextField
                size="small"
                placeholder="Filtrer par mois..."
                value={monthSearch}
                onChange={e => setMonthSearch(e.target.value)}
                sx={{
              width: 160,
              '& .MuiOutlinedInput-root': {
                bgcolor: '#f8fafc',
                borderRadius: 2,
                '& fieldset': { borderColor: '#e2e8f0' },
                '&:hover fieldset': { borderColor: '#cbd5e1' },
                '&.Mui-focused fieldset': { borderColor: '#3b82f6', borderWidth: '1px' },
              }
            }}
                InputProps={{
                  startAdornment: (
                    <InputAdornment position="start">
                      <MagnifyingGlassIcon width={14} color="#94a3b8" />
                    </InputAdornment>
                  ),
                }}
              />
              <TextField
                select
                size="small"
                label="Type"
                value={chartType}
                onChange={e => setChartType(e.target.value as 'bar' | 'line')}
                sx={{
              width: 110,
              '& .MuiOutlinedInput-root': {
                bgcolor: '#f8fafc',
                borderRadius: 2,
                '& fieldset': { borderColor: '#e2e8f0' },
                '&:hover fieldset': { borderColor: '#cbd5e1' },
                '&.Mui-focused fieldset': { borderColor: '#3b82f6', borderWidth: '1px' },
              }
            }}
              >
                <MenuItem value="bar">Barres</MenuItem>
                <MenuItem value="line">Lignes</MenuItem>
              </TextField>
              {monthSearch && (
                <Button size="small" variant="outlined" startIcon={<ArrowPathIcon width={14} />}
                  onClick={() => setMonthSearch('')}
                  sx={{ borderRadius: 2, borderColor: '#e2e8f0', color: 'text.secondary', whiteSpace: 'nowrap' }}
                >
                  Réinitialiser
                </Button>
              )}
            </Stack>
          </Box>

          <Box sx={{ height: 280, width: '100%' }}>
            {monthlyDataFiltered.length > 0 ? (
              <ResponsiveContainer>
                {chartType === 'bar' ? (
                  <BarChart data={monthlyDataFiltered} margin={{ top: 5, right: 20, left: -20, bottom: 5 }}>
                    <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f1f5f9" />
                    <XAxis dataKey="name" axisLine={false} tickLine={false} tick={{ fontSize: 12, fill: '#94a3b8' }} />
                    <YAxis axisLine={false} tickLine={false} tick={{ fontSize: 12, fill: '#94a3b8' }} />
                    <RechartsTooltip contentStyle={{ borderRadius: 8, border: 'none', boxShadow: '0 4px 12px rgba(0,0,0,0.1)' }} />
                    <Bar dataKey="AT créées" fill="#16a34a" radius={[4, 4, 0, 0]} maxBarSize={45} />
                  </BarChart>
                ) : (
                  <LineChart data={monthlyDataFiltered} margin={{ top: 5, right: 20, left: -20, bottom: 5 }}>
                    <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f1f5f9" />
                    <XAxis dataKey="name" axisLine={false} tickLine={false} tick={{ fontSize: 12, fill: '#94a3b8' }} />
                    <YAxis axisLine={false} tickLine={false} tick={{ fontSize: 12, fill: '#94a3b8' }} />
                    <RechartsTooltip contentStyle={{ borderRadius: 8, border: 'none', boxShadow: '0 4px 12px rgba(0,0,0,0.1)' }} />
                    <Line type="monotone" dataKey="AT créées" stroke="#16a34a" strokeWidth={2.5} dot={{ fill: '#16a34a', r: 4 }} />
                  </LineChart>
                )}
              </ResponsiveContainer>
            ) : (
              <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: '100%' }}>
                <Typography color="text.secondary">Aucune donnée mensuelle disponible</Typography>
              </Box>
            )}
          </Box>
        </Card>

        {/* Status Pie Chart with filter */}
        <Card sx={{ p: 3, flex: '1 1 0', borderRadius: 3, boxShadow: '0 1px 4px rgba(0,0,0,0.06)' }}>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
            <Typography variant="h6" sx={{ fontWeight: 700 }}>
              Répartition par Statut
            </Typography>
          </Box>

          <TextField
            size="small"
            fullWidth
            placeholder="Filtrer par statut..."
            value={statusSearch}
            onChange={e => setStatusSearch(e.target.value)}
            sx={{
              mb: 2,
              '& .MuiOutlinedInput-root': {
                bgcolor: '#f8fafc',
                borderRadius: 2,
                '& fieldset': { borderColor: '#e2e8f0' },
                '&:hover fieldset': { borderColor: '#cbd5e1' },
                '&.Mui-focused fieldset': { borderColor: '#3b82f6', borderWidth: '1px' },
              }
            }}
            InputProps={{
              startAdornment: (
                <InputAdornment position="start">
                  <MagnifyingGlassIcon width={14} color="#94a3b8" />
                </InputAdornment>
              ),
            }}
          />

          <Box sx={{ height: 250, width: '100%' }}>
            {statusData.length > 0 ? (
              <ResponsiveContainer>
                <PieChart>
                  <Pie
                    data={statusData}
                    cx="50%" cy="50%"
                    innerRadius={65} outerRadius={90}
                    paddingAngle={2}
                    dataKey="value"
                    stroke="none"
                  >
                    {statusData.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={entry.color} />
                    ))}
                  </Pie>
                  <RechartsTooltip contentStyle={{ borderRadius: 8, border: 'none', boxShadow: '0 4px 12px rgba(0,0,0,0.1)' }} />
                  <Legend iconType="circle" wrapperStyle={{ fontSize: 12 }} />
                </PieChart>
              </ResponsiveContainer>
            ) : (
              <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: '100%' }}>
                <Typography color="text.secondary">Aucune donnée disponible</Typography>
              </Box>
            )}
          </Box>

          {/* Status legend chips */}
          {statusData.length > 0 && (
            <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.8, mt: 1 }}>
              {statusData.map(item => (
                <Chip
                  key={item.name}
                  label={`${item.name}: ${item.value}`}
                  size="small"
                  sx={{ bgcolor: `${item.color}18`, color: item.color, fontWeight: 600, fontSize: 11, borderRadius: 1.5 }}
                />
              ))}
            </Box>
          )}
        </Card>
      </Box>
    </Box>
  );
}
