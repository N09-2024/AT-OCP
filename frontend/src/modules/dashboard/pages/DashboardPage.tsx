// In MUI v9 the Grid is a single component, no item/container/xs props
// We use sx-based layout with Box for row layouts and the new Grid syntax
import { Box, Typography, CircularProgress } from '@mui/material';
import {
  ClipboardDocumentCheckIcon,
  CheckBadgeIcon,
  ShieldCheckIcon,
  ArchiveBoxIcon,
  FolderIcon,
  CalendarDaysIcon,
} from '@heroicons/react/24/outline';
import { format } from 'date-fns';
import { fr } from 'date-fns/locale';
import { useEffect, useState } from 'react';
import StatCard from '../../../components/dashboard/StatCard';
import StatusChart from '../../../components/dashboard/StatusChart';
import AtTable from '../../../components/dashboard/AtTable';
import RecentActivity from '../../../components/dashboard/RecentActivity';
import WorkflowStepper from '../../../components/dashboard/WorkflowStepper';
import { useAuthStore } from '../../../store/authStore';
import { DashboardService, type DashboardData } from '../../../services/DashboardService';

export default function DashboardPage() {
  const user = useAuthStore((s) => s.user);
  const prenom = user?.prenom ?? 'Utilisateur';
  const nom = user?.nom ?? '';
  const today = format(new Date(), 'dd MMMM yyyy', { locale: fr });

  const [data, setData] = useState<DashboardData | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    DashboardService.getStats()
      .then((res) => {
        setData(res);
      })
      .catch((err) => {
        console.error('Erreur lors du chargement des statistiques', err);
      })
      .finally(() => {
        setLoading(false);
      });
  }, []);

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
        <CircularProgress color="success" />
      </Box>
    );
  }

  const kpis = data?.kpis;

  const KPI_CARDS = [
    {
      title: 'Autorisations',
      value: kpis?.autorisationsEnCours ?? 0,
      subtitle: 'En cours',
      icon: <ClipboardDocumentCheckIcon width={24} />,
      linkTo: '/autorisations',
    },
    {
      title: 'Visas en attente',
      value: kpis?.visasEnAttente ?? 0,
      subtitle: 'À valider',
      icon: <CheckBadgeIcon width={24} />,
      linkTo: '/visas',
    },
    {
      title: 'Permis actifs',
      value: kpis?.permisActifs ?? 0,
      subtitle: 'Valides',
      icon: <ShieldCheckIcon width={24} />,
      linkTo: '/permis',
    },
    {
      title: 'Réceptions',
      value: kpis?.receptionsEnAttente ?? 0,
      subtitle: 'En attente',
      icon: <ArchiveBoxIcon width={24} />,
      linkTo: '/receptions',
    },
    {
      title: 'Archives',
      value: kpis?.totalArchives ?? 0,
      subtitle: 'Documents',
      icon: <FolderIcon width={24} />,
      linkTo: '/archives',
    },
  ];

  return (
    <Box>
      {/* ── Page Header ── */}
      <Box sx={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', mb: 4 }}>
        <Box>
          <Typography variant="h4" sx={{ fontWeight: 'bold' }} color="text.primary">
            Bonjour, {prenom} {nom}
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Voici un aperçu de vos activités aujourd'hui.
          </Typography>
        </Box>
        <Box
          sx={{
            display: 'flex',
            alignItems: 'center',
            gap: 1,
            bgcolor: 'white',
            border: '1px solid',
            borderColor: 'divider',
            borderRadius: 2,
            px: 2,
            py: 1,
          }}
        >
          <CalendarDaysIcon width={18} color="#6B7280" />
          <Typography variant="body2" color="text.secondary">
            {today}
          </Typography>
        </Box>
      </Box>

      {/* ── Row 1 : KPI Cards ── */}
      <Box sx={{ display: 'flex', gap: 3, mb: 4, flexWrap: 'wrap' }}>
        {KPI_CARDS.map((card) => (
          <Box key={card.title} sx={{ flex: '1 1 180px', minWidth: 160 }}>
            <StatCard
              title={card.title}
              value={card.value}
              subtitle={card.subtitle}
              icon={card.icon}
              linkTo={card.linkTo}
            />
          </Box>
        ))}
      </Box>

      {/* 🚀 Main Content: Two Columns 🚀 */}
      <Box sx={{ display: 'flex', gap: 3, flexDirection: { xs: 'column', lg: 'row' } }}>
        
        {/* 🚀 Left Column (Table + Charts/Activity) 🚀 */}
        <Box sx={{ flex: '2 1 0', minWidth: 0, display: 'flex', flexDirection: 'column', gap: 3 }}>
          {/* AT Table */}
          <Box>
            <AtTable data={data?.recentAutorisations ?? []} />
          </Box>
          
          {/* Status Chart & Recent Activity Row */}
          <Box sx={{ display: 'flex', gap: 3, flexDirection: { xs: 'column', md: 'row' } }}>
            <Box sx={{ flex: '1 1 0', minWidth: 0 }}>
              <StatusChart data={data?.statusDistribution ?? {}} />
            </Box>
            <Box sx={{ flex: '1 1 0', minWidth: 0 }}>
              <RecentActivity />
            </Box>
          </Box>
        </Box>

        {/* 🚀 Right Column (Workflow Stepper) 🚀 */}
        <Box sx={{ flex: '1 1 0', minWidth: 0 }}>
          <WorkflowStepper />
        </Box>

      </Box>
    </Box>
  );
}
