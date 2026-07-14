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
import RolesListPage from '../modules/administration/pages/RolesListPage';
import RoleFormPage from '../modules/administration/pages/RoleFormPage';
import PendingUsersPage from '../modules/administration/pages/PendingUsersPage';
import SettingsPage from '../modules/administration/pages/SettingsPage';
import AuditLogPage from '../modules/administration/pages/AuditLogPage';
import PermisListPage from '../modules/permis/pages/PermisListPage';
import PermisDetailsPage from '../modules/permis/pages/PermisDetailsPage';
import { useAuthStore } from '../store/authStore';

const ProtectedRoute = ({ children }: { children: React.ReactNode }) => {
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  if (!isAuthenticated) {
    return <Navigate to="/auth/login" replace />;
  }
  return <>{children}</>;
};

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
          <Route path="utilisateurs" element={<UsersListPage />} />
          <Route path="utilisateurs/:id" element={<UserFormPage />} />
          <Route path="roles" element={<RolesListPage />} />
          <Route path="roles/:id" element={<RoleFormPage />} />
          <Route path="parametres" element={<SettingsPage />} />
          <Route path="inscriptions" element={<PendingUsersPage />} />
          <Route path="audit" element={<AuditLogPage />} />
        </Route>
        
        <Route path="permis">
          <Route index element={<PermisListPage />} />
          <Route path=":id" element={<PermisDetailsPage />} />
        </Route>
        {/* We will add other module routes here later */}
      </Route>

      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  );
}
