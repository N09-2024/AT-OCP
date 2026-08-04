/**
 * Snippet à intégrer dans frontend/src/routes/index.tsx
 *
 * Remplacer :
 *   <Route path="dashboard" element={<DashboardPage />} />
 *
 * Par le bloc ci-dessous (après imports des nouveaux dashboards).
 */

/*
import DashboardRouter from '../modules/dashboard/pages/DashboardRouter';
import CEDashboardPage from '../modules/dashboard/pages/CEDashboardPage';
import HMDashboardPage from '../modules/dashboard/pages/HMDashboardPage';
import HCDashboardPage from '../modules/dashboard/pages/HCDashboardPage';
import AdminDashboardPage from '../modules/dashboard/pages/AdminDashboardPage';
import ResponsableExterieurDashboardPage from '../modules/dashboard/pages/ResponsableExterieurDashboardPage';
import { RoleGuard } from '../components/guards/Guards';

// Dans les routes protégées (MainLayout) :

<Route path="dashboard" element={<DashboardRouter />} />
<Route
  path="dashboard/ce"
  element={
    <RoleGuard roles={['CE', 'ADMIN']}>
      <CEDashboardPage />
    </RoleGuard>
  }
/>
<Route
  path="dashboard/hm"
  element={
    <RoleGuard roles={['HM', 'ADMIN']}>
      <HMDashboardPage />
    </RoleGuard>
  }
/>
<Route
  path="dashboard/hc"
  element={
    <RoleGuard roles={['HC', 'ADMIN']}>
      <HCDashboardPage />
    </RoleGuard>
  }
/>
<Route
  path="dashboard/admin"
  element={
    <RoleGuard roles={['ADMIN']}>
      <AdminDashboardPage />
    </RoleGuard>
  }
/>
<Route
  path="dashboard/externe"
  element={
    <RoleGuard roles={['RESPONSABLE_EXTERIEUR', 'ADMIN']}>
      <ResponsableExterieurDashboardPage />
    </RoleGuard>
  }
/>
*/

export {};
