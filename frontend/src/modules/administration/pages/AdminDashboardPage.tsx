import { useEffect, useState, useMemo } from 'react';
import { Link } from 'react-router-dom';
import {
  Box,
  Typography,
  Card,
  CardContent,
  Chip,
  Button,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  IconButton,
  CircularProgress,
  TextField,
  InputAdornment,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
  Stack,
} from '@mui/material';
import {
  ClipboardDocumentCheckIcon,
  UserGroupIcon,
  ShieldCheckIcon,
  ArchiveBoxIcon,
  CalendarDaysIcon,
  ArrowRightIcon,
  MagnifyingGlassIcon,
  FunnelIcon,
  ArrowPathIcon,
  FolderIcon,
} from '@heroicons/react/24/outline';
import {
  PieChart, Pie, Cell, ResponsiveContainer,
  Tooltip as RechartsTooltip, Legend,
  BarChart, Bar, XAxis, YAxis, CartesianGrid,
} from 'recharts';
import { useAuthStore } from '../../../store/authStore';
import { AdminService } from '../../../services/AdminService';
import type { DashboardStats, AuditLogEntryFlat } from '../../../services/AdminService';
import { formatDistanceToNow } from 'date-fns';
import { fr } from 'date-fns/locale';

const PIE_COLORS = ['#16a34a', '#f59e0b', '#3b82f6', '#f97316', '#ef4444', '#94a3b8'];

const STATUS_MAP: Record<string, { label: string; bg: string; color: string }> = {
  EN_COURS:        { label: 'En cours',        bg: '#dcfce7', color: '#16a34a' },
  SOUMISE:         { label: 'Soumise',          bg: '#dbeafe', color: '#2563eb' },
  VALIDEE:         { label: 'Validée',          bg: '#dcfce7', color: '#16a34a' },
  EN_ATTENTE_VISA: { label: 'En attente visa',  bg: '#ffedd5', color: '#ea580c' },
  REFUSEE:         { label: 'Refusée',          bg: '#fee2e2', color: '#ef4444' },
  CLOTUREE:        { label: 'Clôturée',         bg: '#e2e8f0', color: '#475569' },
  BROUILLON:       { label: 'Brouillon',        bg: '#f1f5f9', color: '#64748b' },
  ARCHIVEE:        { label: 'Archivée',         bg: '#f1f5f9', color: '#64748b' },
};

function getStatusStyle(status: string) {
  const key = status?.toUpperCase().replace(/ /g, '_') ?? '';
  return STATUS_MAP[key] ?? { label: status, bg: '#f1f5f9', color: '#475569' };
}

export default function AdminDashboardPage() {
  const user = useAuthStore((s) => s.user);
  const [dateStr, setDateStr] = useState('');
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [activities, setActivities] = useState<AuditLogEntryFlat[]>([]);
  const [loading, setLoading] = useState(true);

  // Filters state
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [installationFilter, setInstallationFilter] = useState('');

  useEffect(() => {
    setDateStr(
      new Intl.DateTimeFormat('fr-FR', { day: '2-digit', month: 'long', year: 'numeric' }).format(new Date())
    );

    Promise.all([
      AdminService.getDashboardStats(),
      AdminService.listAuditLogs(),
    ])
      .then(([dashboardStats, logs]) => {
        setStats(dashboardStats);
        setActivities(logs.slice(0, 5));
      })
      .catch((err) => console.error('Error loading dashboard stats', err))
      .finally(() => setLoading(false));
  }, []);

  // Filtered table data
  const filteredRows = useMemo(() => {
    const rows = stats?.recentAutorisations ?? [];
    return rows.filter((row) => {
      const matchSearch =
        !search ||
        row.titre?.toLowerCase().includes(search.toLowerCase()) ||
        row.id?.toLowerCase().includes(search.toLowerCase()) ||
        row.installation?.toLowerCase().includes(search.toLowerCase());
      const matchStatus = !statusFilter || row.statut === statusFilter;
      const matchInstallation =
        !installationFilter ||
        row.installation?.toLowerCase().includes(installationFilter.toLowerCase());
      return matchSearch && matchStatus && matchInstallation;
    });
  }, [stats?.recentAutorisations, search, statusFilter, installationFilter]);

  const availableStatuses = useMemo(() => {
    const all = (stats?.recentAutorisations ?? []).map((r) => r.statut).filter(Boolean);
    return [...new Set(all)];
  }, [stats?.recentAutorisations]);

  const availableInstallations = useMemo(() => {
    const all = (stats?.recentAutorisations ?? []).map((r) => r.installation).filter(Boolean);
    return [...new Set(all)];
  }, [stats?.recentAutorisations]);

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '60vh' }}>
        <CircularProgress color="success" />
      </Box>
    );
  }

  const kpis = stats?.kpis;

  const STAT_CARDS = [
    {
      title: 'AUTORISATIONS',
      value: kpis?.autorisationsEnCours ?? 0,
      subtitle: 'En cours',
      icon: <ClipboardDocumentCheckIcon width={24} />,
      gradient: 'linear-gradient(135deg, #16a34a 0%, #22c55e 100%)',
      linkTo: '/autorisations?statut=EN_COURS',
    },
    {
      title: 'VISAS EN ATTENTE',
      value: kpis?.visasEnAttente ?? 0,
      subtitle: 'À valider',
      icon: <UserGroupIcon width={24} />,
      gradient: 'linear-gradient(135deg, #f59e0b 0%, #fbbf24 100%)',
      linkTo: '/visas',
    },
    {
      title: 'PERMIS ACTIFS',
      value: kpis?.permisActifs ?? 0,
      subtitle: 'Valides',
      icon: <ShieldCheckIcon width={24} />,
      gradient: 'linear-gradient(135deg, #3b82f6 0%, #60a5fa 100%)',
      linkTo: '/permis',
    },
    {
      title: 'RÉCEPTIONS',
      value: kpis?.receptionsEnAttente ?? 0,
      subtitle: 'En attente',
      icon: <ArchiveBoxIcon width={24} />,
      gradient: 'linear-gradient(135deg, #f97316 0%, #fb923c 100%)',
      linkTo: '/receptions',
    },
    {
      title: 'ARCHIVES',
      value: kpis?.totalArchives ?? 0,
      subtitle: 'Documents',
      icon: <FolderIcon width={24} />,
      gradient: 'linear-gradient(135deg, #8b5cf6 0%, #a78bfa 100%)',
      linkTo: '/archives',
    },
  ];

  const chartColors = PIE_COLORS;
  const chartData = stats?.statusDistribution
    ? Object.entries(stats.statusDistribution).map(([key, value], index) => ({
        name: getStatusStyle(key).label,
        value,
        color: chartColors[index % chartColors.length],
      }))
    : [];

  const monthlyData = stats?.monthlyStats
    ? stats.monthlyStats.map((item) => ({
        name: item.mois,
        'AT créées': item.total,
      }))
    : [];

  return (
    <Box sx={{ pb: 4 }}>
      {/* ── Header ── */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 4 }}>
        <Box>
          <Typography variant="h4" sx={{ fontWeight: 800, color: '#16a34a', mb: 0.5 }}>
            Bonjour, {user?.prenom} {user?.nom} 👋
          </Typography>
          <Typography variant="body1" color="text.secondary">
            Voici un aperçu complet de l'activité de la plateforme.
          </Typography>
        </Box>
        <Button
          variant="outlined"
          startIcon={<CalendarDaysIcon width={16} />}
          sx={{ borderColor: '#e2e8f0', color: 'text.primary', bgcolor: 'white', px: 2.5, py: 1.2, borderRadius: 2, fontWeight: 600, fontSize: 13 }}
        >
          {dateStr}
        </Button>
      </Box>

      {/* ── KPI Cards ── */}
      <Box sx={{ display: 'flex', gap: 2, mb: 4, flexWrap: 'wrap' }}>
        {STAT_CARDS.map((card) => (
          <Card
            key={card.title}
            sx={{
              flex: '1 1 170px',
              borderRadius: 3,
              overflow: 'hidden',
              boxShadow: '0 2px 8px rgba(0,0,0,0.08)',
              transition: 'transform 0.2s, box-shadow 0.2s',
              '&:hover': { transform: 'translateY(-3px)', boxShadow: '0 8px 24px rgba(0,0,0,0.12)' },
            }}
          >
            <CardContent sx={{ p: 0 }}>
              {/* Colored top banner */}
              <Box sx={{ background: card.gradient, p: 2, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <Typography variant="caption" sx={{ fontWeight: 700, color: 'rgba(255,255,255,0.9)', letterSpacing: 0.8, fontSize: 10 }}>
                  {card.title}
                </Typography>
                <Box sx={{ color: 'rgba(255,255,255,0.9)' }}>{card.icon}</Box>
              </Box>

              {/* Value */}
              <Box sx={{ p: 2.5, pt: 2 }}>
                <Typography variant="h3" sx={{ fontWeight: 900, color: '#0f172a', lineHeight: 1 }}>
                  {card.value}
                </Typography>
                <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
                  {card.subtitle}
                </Typography>
                <Box sx={{ mt: 2, pt: 2, borderTop: '1px solid #f1f5f9' }}>
                  <Typography
                    component={Link}
                    to={card.linkTo}
                    variant="body2"
                    sx={{
                      color: '#16a34a',
                      fontWeight: 700,
                      display: 'flex',
                      alignItems: 'center',
                      gap: 0.5,
                      textDecoration: 'none',
                      fontSize: 12,
                      '&:hover': { textDecoration: 'underline' },
                    }}
                  >
                    Voir plus <ArrowRightIcon width={12} />
                  </Typography>
                </Box>
              </Box>
            </CardContent>
          </Card>
        ))}
      </Box>

      {/* ── Authorizations Table ── */}
      <Paper sx={{ borderRadius: 3, boxShadow: '0 1px 4px rgba(0,0,0,0.06)', mb: 3, overflow: 'hidden' }}>
        {/* Table header + search */}
        <Box sx={{ p: 3, borderBottom: '1px solid #f1f5f9' }}>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2.5 }}>
            <Typography variant="h6" sx={{ fontWeight: 700 }}>
              Toutes les autorisations de travail
            </Typography>
            <Typography
              component={Link}
              to="/autorisations"
              variant="body2"
              sx={{ color: '#16a34a', fontWeight: 600, display: 'flex', alignItems: 'center', gap: 0.5, textDecoration: 'none', '&:hover': { textDecoration: 'underline' } }}
            >
              Voir toutes <ArrowRightIcon width={14} />
            </Typography>
          </Box>

          {/* Search bar + Filters */}
          <Stack direction={{ xs: 'column', md: 'row' }} spacing={2} alignItems="center">
            <TextField
              size="small"
              placeholder="Rechercher par titre, N°AT, installation..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              sx={{
              flexGrow: 1,
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
                    <MagnifyingGlassIcon style={{ width: 18, height: 18, color: '#94a3b8' }} />
                  </InputAdornment>
                ),
              }}
            />

            <TextField
              select
              size="small"
              label="Statut"
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
              sx={{
              minWidth: 200, flexShrink: 0,
              '& .MuiOutlinedInput-root': {
                bgcolor: '#f8fafc',
                borderRadius: 2,
                '& fieldset': { borderColor: '#e2e8f0' },
                '&:hover fieldset': { borderColor: '#cbd5e1' },
                '&.Mui-focused fieldset': { borderColor: '#3b82f6', borderWidth: '1px' },
              }
            }}
            >
              <MenuItem value=""><em>Tous les statuts</em></MenuItem>
              {availableStatuses.map((s) => (
                <MenuItem key={s} value={s}>{getStatusStyle(s).label}</MenuItem>
              ))}
            </TextField>

            <TextField
              select
              size="small"
              label="Installation"
              value={installationFilter}
              onChange={(e) => setInstallationFilter(e.target.value)}
              sx={{
              minWidth: 220, flexShrink: 0,
              '& .MuiOutlinedInput-root': {
                bgcolor: '#f8fafc',
                borderRadius: 2,
                '& fieldset': { borderColor: '#e2e8f0' },
                '&:hover fieldset': { borderColor: '#cbd5e1' },
                '&.Mui-focused fieldset': { borderColor: '#3b82f6', borderWidth: '1px' },
              }
            }}
            >
              <MenuItem value=""><em>Toutes les installations</em></MenuItem>
              {availableInstallations.map((inst) => (
                <MenuItem key={inst} value={inst}>{inst}</MenuItem>
              ))}
            </TextField>

            {(search || statusFilter || installationFilter) && (
              <Button
                size="small"
                variant="outlined"
                startIcon={<ArrowPathIcon width={14} />}
                onClick={() => { setSearch(''); setStatusFilter(''); setInstallationFilter(''); }}
                sx={{ borderRadius: 2, borderColor: '#e2e8f0', color: 'text.secondary', whiteSpace: 'nowrap', height: 40 }}
              >
                Réinitialiser
              </Button>
            )}
          </Stack>
        </Box>

        <TableContainer>
          <Table size="small">
            <TableHead>
              <TableRow sx={{ '& th': { fontWeight: 700, fontSize: 12, color: '#64748b', bgcolor: '#f8fafc', textTransform: 'uppercase', letterSpacing: 0.5 } }}>
                <TableCell>N° AT</TableCell>
                <TableCell>Titre</TableCell>
                <TableCell>Installation</TableCell>
                <TableCell>Statut</TableCell>
                <TableCell>Échéance</TableCell>
                <TableCell align="right" />
              </TableRow>
            </TableHead>
            <TableBody>
              {filteredRows.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={6} align="center" sx={{ py: 5, color: 'text.secondary' }}>
                    <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 1 }}>
                      <ClipboardDocumentCheckIcon width={36} color="#cbd5e1" />
                      <Typography variant="body2" color="text.secondary">
                        {search || statusFilter || installationFilter
                          ? 'Aucun résultat pour ces filtres.'
                          : 'Aucune autorisation récente trouvée.'}
                      </Typography>
                    </Box>
                  </TableCell>
                </TableRow>
              ) : (
                filteredRows.map((row) => {
                  const style = getStatusStyle(row.statut);
                  return (
                    <TableRow
                      key={row.id}
                      sx={{ '&:hover': { bgcolor: '#f8fafc' }, '&:last-child td': { border: 0 } }}
                    >
                      <TableCell sx={{ fontWeight: 600, color: '#16a34a', fontSize: 13 }}>
                        {row.id?.substring(0, 8)}…
                      </TableCell>
                      <TableCell sx={{ fontWeight: 500 }}>{row.titre}</TableCell>
                      <TableCell>{row.installation}</TableCell>
                      <TableCell>
                        <Chip
                          label={style.label}
                          size="small"
                          sx={{ bgcolor: style.bg, color: style.color, fontWeight: 700, fontSize: 11, borderRadius: 1 }}
                        />
                      </TableCell>
                      <TableCell sx={{ color: 'text.secondary' }}>{row.echeance}</TableCell>
                      <TableCell align="right">
                        <IconButton size="small" component={Link} to={`/permis/${row.id}`}>
                          <ArrowRightIcon width={16} color="#94a3b8" />
                        </IconButton>
                      </TableCell>
                    </TableRow>
                  );
                })
              )}
            </TableBody>
          </Table>
        </TableContainer>
      </Paper>

      {/* ── Bottom Row: Charts + Activity ── */}
      <Box sx={{ display: 'flex', gap: 3, flexDirection: { xs: 'column', lg: 'row' } }}>

        {/* Monthly Bar Chart */}
        {monthlyData.length > 0 && (
          <Paper sx={{ p: 3, borderRadius: 3, flex: '2 1 0', boxShadow: '0 1px 4px rgba(0,0,0,0.06)' }}>
            <Typography variant="h6" sx={{ fontWeight: 700, mb: 3 }}>
              Évolution mensuelle
            </Typography>
            <Box sx={{ height: 220 }}>
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={monthlyData} margin={{ top: 5, right: 20, left: -20, bottom: 5 }}>
                  <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f1f5f9" />
                  <XAxis dataKey="name" axisLine={false} tickLine={false} tick={{ fontSize: 12, fill: '#94a3b8' }} />
                  <YAxis axisLine={false} tickLine={false} tick={{ fontSize: 12, fill: '#94a3b8' }} />
                  <RechartsTooltip contentStyle={{ borderRadius: 8, border: 'none', boxShadow: '0 4px 12px rgba(0,0,0,0.1)' }} />
                  <Bar dataKey="AT créées" fill="#16a34a" radius={[4, 4, 0, 0]} maxBarSize={40} />
                </BarChart>
              </ResponsiveContainer>
            </Box>
          </Paper>
        )}

        {/* Status Pie Chart */}
        <Paper sx={{ p: 3, borderRadius: 3, flex: '1 1 0', boxShadow: '0 1px 4px rgba(0,0,0,0.06)' }}>
          <Typography variant="h6" sx={{ fontWeight: 700, mb: 2 }}>
            Répartition par statut
          </Typography>
          {chartData.length > 0 ? (
            <Box sx={{ height: 220 }}>
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie
                    data={chartData}
                    cx="50%"
                    cy="50%"
                    innerRadius={60}
                    outerRadius={85}
                    paddingAngle={2}
                    dataKey="value"
                    stroke="none"
                  >
                    {chartData.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={entry.color} />
                    ))}
                  </Pie>
                  <RechartsTooltip contentStyle={{ borderRadius: 8, border: 'none', boxShadow: '0 4px 12px rgba(0,0,0,0.1)' }} />
                  <Legend iconType="circle" wrapperStyle={{ fontSize: 12 }} />
                </PieChart>
              </ResponsiveContainer>
            </Box>
          ) : (
            <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: 180 }}>
              <Typography color="text.secondary" variant="body2">Aucune donnée disponible</Typography>
            </Box>
          )}
        </Paper>

        {/* Recent Activity */}
        <Paper sx={{ p: 3, borderRadius: 3, flex: '1.2 1 0', boxShadow: '0 1px 4px rgba(0,0,0,0.06)' }}>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2.5 }}>
            <Typography variant="h6" sx={{ fontWeight: 700 }}>
              Activités récentes
            </Typography>
          </Box>

          {activities.length > 0 ? (
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
              {activities.map((act) => {
                const isSuccess = !act.action.toLowerCase().includes('delete') && !act.action.toLowerCase().includes('erreur');
                return (
                  <Box key={act.id} sx={{ display: 'flex', gap: 2, alignItems: 'flex-start' }}>
                    <Box
                      sx={{
                        width: 36, height: 36, borderRadius: 2, flexShrink: 0,
                        display: 'flex', alignItems: 'center', justifyContent: 'center',
                        bgcolor: isSuccess ? '#dcfce7' : '#fee2e2',
                        color: isSuccess ? '#16a34a' : '#ef4444',
                      }}
                    >
                      <ClipboardDocumentCheckIcon width={18} />
                    </Box>
                    <Box sx={{ flex: 1, minWidth: 0 }}>
                      <Typography variant="body2" sx={{ fontWeight: 600, mb: 0.3 }} noWrap>
                        {act.action} — {act.entity}
                      </Typography>
                      <Typography variant="caption" color="text.secondary">
                        par {act.utilisateur} · {formatDistanceToNow(new Date(act.dateCreation), { addSuffix: true, locale: fr })}
                      </Typography>
                    </Box>
                  </Box>
                );
              })}
            </Box>
          ) : (
            <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: 120 }}>
              <Typography color="text.secondary" variant="body2">Aucune activité récente.</Typography>
            </Box>
          )}

          <Box sx={{ mt: 3, pt: 2, borderTop: '1px solid #f1f5f9' }}>
            <Typography
              component={Link}
              to="/administration/audit"
              variant="body2"
              sx={{ color: '#16a34a', fontWeight: 700, display: 'flex', alignItems: 'center', gap: 0.5, textDecoration: 'none', '&:hover': { textDecoration: 'underline' } }}
            >
              Voir le journal complet <ArrowRightIcon width={14} />
            </Typography>
          </Box>
        </Paper>
      </Box>
    </Box>
  );
}
