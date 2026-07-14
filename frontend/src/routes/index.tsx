import { Routes, Route, Navigate } from 'react-router-dom';
import AuthLayout from '../layouts/AuthLayout';
import MainLayout from '../layouts/MainLayout';
import LoginPage from '../modules/auth/pages/LoginPage';
import RegisterPage from '../modules/auth/pages/RegisterPage';
import DashboardPage from '../modules/dashboard/pages/DashboardPage';
import AdminLayout from '../modules/administration/AdminLayout';
import AdminDashboardPage from '../modules/administration/pages/AdminDashboardPage';
import UsersListPage from '../modules/administration/pages/UsersListPage';
import UserFormPage from '../modules/administration/pages/UserFormPage';
import PendingUsersPage from '../modules/administration/pages/PendingUsersPage';
import SettingsPage from '../modules/administration/pages/SettingsPage';
import AuditLogPage from '../modules/administration/pages/AuditLogPage';
import PermisListPage from '../modules/permis/pages/PermisListPage';
import PermisDetailsPage from '../modules/permis/pages/PermisDetailsPage';

// Référentiels existants
import ZonesListPage from '../modules/administration/pages/ZonesListPage';
import ZoneFormPage from '../modules/administration/pages/ZoneFormPage';
import InstallationsListPage from '../modules/administration/pages/InstallationsListPage';
import InstallationFormPage from '../modules/administration/pages/InstallationFormPage';

// Pages dédiées pour les services (avec zones)
import ServicesListPage from '../modules/administration/pages/ServicesListPage';
import ServiceFormPage from '../modules/administration/pages/ServiceFormPage';

// Pages génériques pour les référentiels simples
import SimpleReferentielPage from '../modules/administration/pages/SimpleReferentielPage';
import SimpleReferentielForm from '../modules/administration/pages/SimpleReferentielForm';

import { useAuthStore } from '../store/authStore';

const ProtectedRoute = ({ children }: { children: React.ReactNode }) => {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  if (!isAuthenticated) {
    return <Navigate to="/auth/login" replace />;
  }
  return <>{children}</>;
};

// -------------------------------------------------------
// Referentiel route configs — one entry per referential
// -------------------------------------------------------
const REFERENTIEL_ROUTES = [
  {
    key: 'equipements',
    apiPath: '/equipements',
    routeBase: '/administration/equipements',
    title: 'Équipements',
    subtitle: 'Gestion des équipements utilisés dans les travaux',
    createLabel: 'Nouvel équipement',
    searchPlaceholder: 'Rechercher un équipement...',
    labelField: 'nom' as const,
  },
  {
    key: 'types-intervention',
    apiPath: '/types-intervention',
    routeBase: '/administration/types-intervention',
    title: "Types d'intervention",
    subtitle: "Gestion des types d'intervention",
    createLabel: "Nouveau type d'intervention",
    searchPlaceholder: "Rechercher un type d'intervention...",
    labelField: 'nom' as const,
  },
  {
    key: 'risques',
    apiPath: '/risques',
    routeBase: '/administration/risques',
    title: 'Risques',
    subtitle: 'Gestion des risques associés aux travaux',
    createLabel: 'Nouveau risque',
    searchPlaceholder: 'Rechercher un risque...',
    labelField: 'nom' as const,
  },
  {
    key: 'mesures-prevention',
    apiPath: '/mesures-preparation',
    routeBase: '/administration/mesures-prevention',
    title: 'Mesures de prévention',
    subtitle: 'Gestion des mesures de prévention et de préparation',
    createLabel: 'Nouvelle mesure',
    searchPlaceholder: 'Rechercher une mesure...',
    labelField: 'nom' as const,
  },
  {
    key: 'epis',
    apiPath: '/epis',
    routeBase: '/administration/epis',
    title: 'EPI',
    subtitle: 'Équipements de protection individuelle',
    createLabel: 'Nouvel EPI',
    searchPlaceholder: 'Rechercher un EPI...',
    labelField: 'nom' as const,
  },
  {
    key: 'moyens-acces',
    apiPath: '/moyens-acces',
    routeBase: '/administration/moyens-acces',
    title: "Moyens d'accès",
    subtitle: "Gestion des moyens d'accès aux zones de travail",
    createLabel: "Nouveau moyen d'accès",
    searchPlaceholder: "Rechercher un moyen d'accès...",
    labelField: 'nom' as const,
  },
  {
    key: 'types-permis',
    apiPath: '/permis/types',
    routeBase: '/administration/types-permis',
    title: 'Types de permis',
    subtitle: 'Gestion des types de permis de travail',
    createLabel: 'Nouveau type de permis',
    searchPlaceholder: 'Rechercher un type de permis...',
    labelField: 'nom' as const,
  },
  {
    key: 'entreprises',
    apiPath: '/entreprises-externes',
    routeBase: '/administration/entreprises',
    title: 'Entreprises externes',
    subtitle: 'Gestion des entreprises sous-traitantes',
    createLabel: 'Nouvelle entreprise',
    searchPlaceholder: 'Rechercher une entreprise...',
    labelField: 'nom' as const,
  },
];

export default function AppRoutes() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/dashboard" replace />} />

      <Route path="/auth" element={<AuthLayout />}>
        <Route path="login" element={<LoginPage />} />
        <Route path="register" element={<RegisterPage />} />
      </Route>

      <Route
        path="/"
        element={
          <ProtectedRoute>
            <MainLayout />
          </ProtectedRoute>
        }
      >
        <Route path="dashboard" element={<DashboardPage />} />

        <Route path="administration" element={<AdminLayout />}>
          <Route index element={<AdminDashboardPage />} />

          {/* Utilisateurs */}
          <Route path="utilisateurs" element={<UsersListPage />} />
          <Route path="utilisateurs/:id" element={<UserFormPage />} />

          {/* Inscriptions / Paramètres / Audit */}
          <Route path="inscriptions" element={<PendingUsersPage />} />
          <Route path="parametres" element={<SettingsPage />} />
          <Route path="audit" element={<AuditLogPage />} />

          {/* Référentiels — Zones (page dédiée existante) */}
          <Route path="zones" element={<ZonesListPage />} />
          <Route path="zones/nouveau" element={<ZoneFormPage />} />
          <Route path="zones/:id" element={<ZoneFormPage />} />

          {/* Référentiels — Installations (page dédiée existante) */}
          <Route path="installations" element={<InstallationsListPage />} />
          <Route path="installations/nouveau" element={<InstallationFormPage />} />
          <Route path="installations/:id" element={<InstallationFormPage />} />

          {/* Référentiels — Services (page dédiée avec zones) */}
          <Route path="services" element={<ServicesListPage />} />
          <Route path="services/nouveau" element={<ServiceFormPage />} />
          <Route path="services/:id" element={<ServiceFormPage />} />

          {/* Référentiels — Pages génériques */}
          {REFERENTIEL_ROUTES.flatMap((ref) => [
            <Route
              key={`${ref.key}-list`}
              path={ref.key}
              element={
                <SimpleReferentielPage
                  title={ref.title}
                  subtitle={ref.subtitle}
                  apiPath={ref.apiPath}
                  routeBase={ref.routeBase}
                  labelField={ref.labelField}
                  createLabel={ref.createLabel}
                  searchPlaceholder={ref.searchPlaceholder}
                />
              }
            />,
            <Route
              key={`${ref.key}-new`}
              path={`${ref.key}/nouveau`}
              element={
                <SimpleReferentielForm
                  title={ref.title}
                  apiPath={ref.apiPath}
                  routeBase={ref.routeBase}
                  labelField={ref.labelField}
                  labelPlaceholder={ref.title}
                />
              }
            />,
            <Route
              key={`${ref.key}-edit`}
              path={`${ref.key}/:id`}
              element={
                <SimpleReferentielForm
                  title={ref.title}
                  apiPath={ref.apiPath}
                  routeBase={ref.routeBase}
                  labelField={ref.labelField}
                  labelPlaceholder={ref.title}
                />
              }
            />,
          ])}
        </Route>

        <Route path="permis">
          <Route index element={<PermisListPage />} />
          <Route path=":id" element={<PermisDetailsPage />} />
        </Route>
      </Route>

      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  );
}
