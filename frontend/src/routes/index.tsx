import { Routes, Route, Navigate } from 'react-router-dom';
import AuthLayout from '../layouts/AuthLayout';
import MainLayout from '../layouts/MainLayout';
import LoginPage from '../modules/auth/pages/LoginPage';
import RegisterPage from '../modules/auth/pages/RegisterPage';
import DashboardPage from '../modules/dashboard/pages/DashboardPage';
import DashboardRouter from '../modules/dashboard/pages/DashboardRouter';
import CEDashboardPage from '../modules/dashboard/pages/CEDashboardPage';
import HMDashboardPage from '../modules/dashboard/pages/HMDashboardPage';
import HCDashboardPage from '../modules/dashboard/pages/HCDashboardPage';
import AdminDashboardPage from '../modules/dashboard/pages/AdminDashboardPage';
import ResponsableExterieurDashboardPage from '../modules/dashboard/pages/ResponsableExterieurDashboardPage';
import DemandeurDashboardPage from '../modules/dashboard/pages/DemandeurDashboardPage';
import ResponsableEntrepriseDashboardPage from '../modules/dashboard/pages/ResponsableEntrepriseDashboardPage';
import ResponsableOcpDashboardPage from '../modules/dashboard/pages/ResponsableOcpDashboardPage';
import AdminLayout from '../modules/administration/AdminLayout';
import UsersListPage from '../modules/administration/pages/UsersListPage';
import UserFormPage from '../modules/administration/pages/UserFormPage';
import PendingUsersPage from '../modules/administration/pages/PendingUsersPage';
import SettingsPage from '../modules/administration/pages/SettingsPage';
import AuditLogPage from '../modules/administration/pages/AuditLogPage';
import StatistiquesPage from '../modules/administration/pages/StatistiquesPage';
import RolesListPage from '../modules/administration/pages/RolesListPage';
import RoleFormPage from '../modules/administration/pages/RoleFormPage';
import PermisListPage from '../modules/permis/pages/PermisListPage';
import PermisDetailsPage from '../modules/permis/pages/PermisDetailsPage';
import ProfilePage from '../modules/profile/pages/ProfilePage';
import NotificationsPage from '../modules/profile/pages/NotificationsPage';

// AT & Workflow Pages
import AutorisationListPage from '../modules/autorisations/pages/AutorisationListPage';
import AutorisationFormPage from '../modules/autorisations/pages/AutorisationFormPage';
import AutorisationDetailPage from '../modules/autorisations/pages/AutorisationDetailPage';
import SignatureCeeePage from '../modules/autorisations/pages/SignatureCeeePage';
import ValidationOCPPage from '../modules/visas/pages/ValidationOCPPage';
import ReceptionTravauxPage from '../modules/receptions/pages/ReceptionTravauxPage';
import CeeeReceptionPage from '../modules/receptions/pages/CeeeReceptionPage';
import ArchiveListPage from '../modules/archives/pages/ArchiveListPage';
import DocumentsListPage from '../modules/documents/pages/DocumentsListPage';
import VisitesListPage from '../modules/visites/pages/VisitesListPage';

// Référentiels
import ZonesListPage from '../modules/administration/pages/ZonesListPage';
import ZoneFormPage from '../modules/administration/pages/ZoneFormPage';
import InstallationsListPage from '../modules/administration/pages/InstallationsListPage';
import InstallationFormPage from '../modules/administration/pages/InstallationFormPage';
import ServicesListPage from '../modules/administration/pages/ServicesListPage';
import ServiceFormPage from '../modules/administration/pages/ServiceFormPage';
import SimpleReferentielPage from '../modules/administration/pages/SimpleReferentielPage';
import SimpleReferentielForm from '../modules/administration/pages/SimpleReferentielForm';
import HabilitationsPage from '../modules/administration/pages/HabilitationsPage';

import { ProtectedRoute, RoleGuard } from '../components/guards/Guards';

const REFERENTIEL_ROUTES = [
  {
    key: 'equipements',
    apiPath: '/equipements',
    routeBase: '/administration/equipements',
    title: 'Équipements',
    subtitle: 'Gestion des équipements utilisés dans les travaux',
    createLabel: 'Nouvel équipement',
    searchPlaceholder: 'Rechercher un équipement...',
    labelField: 'nomEquipement' as 'nom',
    fields: [
      { key: 'nomEquipement', label: "Nom de l'équipement", required: true },
      { key: 'codeEquipement', label: "Code de l'équipement", required: true },
      { key: 'descriptionEquipement', label: 'Description', multiline: true, rows: 3 },
    ],
  },
  {
    key: 'epis',
    apiPath: '/epis',
    routeBase: '/administration/epis',
    title: 'EPI',
    subtitle: 'Équipements de protection individuelle',
    createLabel: 'Nouvel EPI',
    searchPlaceholder: 'Rechercher un EPI...',
    labelField: 'nomEPI' as 'nom',
    fields: [
      { key: 'nomEPI', label: "Nom de l'EPI", required: true },
      { key: 'descriptionEPI', label: 'Description', multiline: true, rows: 3 },
    ],
  },
  {
    key: 'risques',
    apiPath: '/risques',
    routeBase: '/administration/risques',
    title: 'Risques',
    subtitle: 'Gestion des risques associés aux travaux',
    createLabel: 'Nouveau risque',
    searchPlaceholder: 'Rechercher un risque...',
    labelField: 'nomRisque' as 'nom',
    fields: [
      { key: 'nomRisque', label: 'Nom du risque', required: true },
      { key: 'niveau', label: 'Niveau (ex: FAIBLE, MOYEN, ELEVE)' },
      { key: 'descriptionRisque', label: 'Description', multiline: true, rows: 3 },
    ],
  },
  {
    key: 'mesures-prevention',
    apiPath: '/mesures-preparation',
    routeBase: '/administration/mesures-prevention',
    title: 'Mesures de prévention',
    subtitle: 'Gestion des mesures de prévention et de préparation',
    createLabel: 'Nouvelle mesure',
    searchPlaceholder: 'Rechercher une mesure...',
    labelField: 'nomMesure' as 'nom',
    fields: [
      { key: 'nomMesure', label: 'Nom de la mesure', required: true },
      { key: 'descriptionMesure', label: 'Description', multiline: true, rows: 3 },
    ],
  },
  {
    key: 'moyens-acces',
    apiPath: '/moyens-acces',
    routeBase: '/administration/moyens-acces',
    title: "Moyens d'accès",
    subtitle: "Gestion des moyens d'accès aux zones de travail",
    createLabel: "Nouveau moyen d'accès",
    searchPlaceholder: "Rechercher un moyen d'accès...",
    labelField: 'nomMoyen' as 'nom',
    fields: [
      { key: 'nomMoyen', label: "Nom du moyen d'accès", required: true },
      { key: 'descriptionMoyen', label: 'Description', multiline: true, rows: 3 },
    ],
  },
  {
    key: 'entreprises',
    apiPath: '/entreprises-externes',
    routeBase: '/administration/entreprises',
    title: 'Entreprises externes',
    subtitle: 'Gestion des entreprises sous-traitantes',
    createLabel: 'Nouvelle entreprise',
    searchPlaceholder: 'Rechercher une entreprise...',
    labelField: 'nomEntreprise' as 'nom',
    fields: [
      { key: 'nomEntreprise', label: "Nom de l'entreprise", required: true },
      { key: 'responsable', label: 'Responsable' },
      { key: 'telephone', label: 'Téléphone' },
      { key: 'adresse', label: 'Adresse', multiline: true, rows: 2 },
    ],
  },
  {
    key: 'types-permis',
    apiPath: '/types-permis',
    routeBase: '/administration/types-permis',
    title: 'Types de permis',
    subtitle: 'Gestion des types de permis de travail',
    createLabel: 'Nouveau type',
    searchPlaceholder: 'Rechercher un type...',
    labelField: 'nom' as 'nom',
    fields: [
      { key: 'nom', label: 'Nom du type', required: true },
      { key: 'description', label: 'Description', multiline: true, rows: 3 },
    ],
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
        {/* Dashboard Router & Sub-dashboards per role */}
        <Route path="dashboard">
          <Route index element={<DashboardRouter />} />
          <Route path="ce" element={<RoleGuard roles={['CE', 'CEEP', 'CEEE', 'ADMIN']}><CEDashboardPage /></RoleGuard>} />
          <Route path="demandeur" element={<RoleGuard roles={['CE', 'CEEP', 'CEEE', 'DEMANDEUR', 'ADMIN']}><DemandeurDashboardPage /></RoleGuard>} />
          <Route path="hm" element={<RoleGuard roles={['HM', 'HMEP', 'HMEE', 'ADMIN']}><HMDashboardPage /></RoleGuard>} />
          <Route path="hc" element={<RoleGuard roles={['HC', 'HCEP', 'HCEE', 'ADMIN']}><HCDashboardPage /></RoleGuard>} />
          <Route path="ocp" element={<RoleGuard roles={['HC', 'HCEP', 'HCEE', 'RESPONSABLE_OCP', 'ADMIN']}><ResponsableOcpDashboardPage /></RoleGuard>} />
          <Route path="admin" element={<RoleGuard roles={['ADMIN']}><AdminDashboardPage /></RoleGuard>} />
          <Route path="externe" element={<RoleGuard roles={['RESPONSABLE_EXTERIEUR', 'RESPONSABLE_ENTREPRISE', 'ADMIN']}><ResponsableExterieurDashboardPage /></RoleGuard>} />
          <Route path="entreprise" element={<RoleGuard roles={['RESPONSABLE_EXTERIEUR', 'RESPONSABLE_ENTREPRISE', 'ADMIN']}><ResponsableEntrepriseDashboardPage /></RoleGuard>} />
          <Route path="global" element={<DashboardPage />} />
        </Route>
        <Route path="documents" element={<DocumentsListPage />} />
        <Route path="visites" element={<VisitesListPage />} />

        {/* Autorisations de travail */}
        <Route path="autorisations">
          <Route index element={<AutorisationListPage />} />
          <Route path="nouvelle" element={<AutorisationFormPage />} />
          <Route path=":id/editer" element={<AutorisationFormPage />} />
          <Route path=":id/signature-ceee" element={<SignatureCeeePage />} />
          <Route path=":id/ceee" element={<SignatureCeeePage />} />
          <Route path=":id" element={<AutorisationDetailPage />} />
        </Route>

        {/* Visas & Validation OCP */}
        <Route path="visas">
          <Route path="validation/:id" element={<ValidationOCPPage />} />
        </Route>

        {/* Réceptions des travaux */}
        <Route path="receptions">
          <Route index element={<ReceptionTravauxPage />} />
          {/* Page dédiée CEEE : réception + visa des AT transmises par le CEEP */}
          <Route path="ceee" element={<RoleGuard roles={['CE', 'CEEE', 'ADMIN']}><CeeeReceptionPage /></RoleGuard>} />
        </Route>

        {/* Archives & PDF */}
        <Route path="archives" element={<ArchiveListPage />} />

        {/* Permis */}
        <Route path="permis">
          <Route index element={<PermisListPage />} />
          <Route path="importer" element={<PermisListPage />} />
          <Route path="photographier" element={<PermisListPage />} />
          <Route path=":id" element={<PermisDetailsPage />} />
        </Route>

        {/* Profil & Notifications */}
        <Route path="profil" element={<ProfilePage />} />
        <Route path="notifications" element={<NotificationsPage />} />

        {/* Habilitations AT (§9 HCEP - F-HSE-SEC-31-02) */}
        <Route path="habilitations" element={<HabilitationsPage />} />

        {/* Administration espace réservé */}
        <Route
          path="administration"
          element={
            <RoleGuard roles={['ADMIN']}>
              <AdminLayout />
            </RoleGuard>
          }
        >
          <Route index element={<Navigate to="/administration/statistiques" replace />} />
          <Route path="statistiques" element={<StatistiquesPage />} />
          <Route path="utilisateurs" element={<UsersListPage />} />
          <Route path="utilisateurs/:id" element={<UserFormPage />} />
          <Route path="roles" element={<RolesListPage />} />
          <Route path="roles/nouveau" element={<RoleFormPage />} />
          <Route path="roles/:id" element={<RoleFormPage />} />
          <Route path="inscriptions" element={<PendingUsersPage />} />
          <Route path="parametres" element={<SettingsPage />} />
          <Route path="audit" element={<AuditLogPage />} />

          <Route path="zones" element={<ZonesListPage />} />
          <Route path="zones/nouveau" element={<ZoneFormPage />} />
          <Route path="zones/:id" element={<ZoneFormPage />} />
          <Route path="installations" element={<InstallationsListPage />} />
          <Route path="installations/nouveau" element={<InstallationFormPage />} />
          <Route path="installations/:id" element={<InstallationFormPage />} />
          <Route path="services" element={<ServicesListPage />} />
          <Route path="services/nouveau" element={<ServiceFormPage />} />
          <Route path="services/:id" element={<ServiceFormPage />} />

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
                  fields={ref.fields}
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
                  fields={ref.fields}
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
                  fields={ref.fields}
                />
              }
            />,
          ])}
        </Route>
      </Route>

      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  );
}
