import { Navigate } from 'react-router-dom';
import { usePrimaryRole, dashboardPathForRole } from '../../../hooks/usePrimaryRole';

/**
 * Point d'entrée /dashboard → redirige vers le dashboard du rôle principal.
 */
export default function DashboardRouter() {
  const role = usePrimaryRole();
  return <Navigate to={dashboardPathForRole(role)} replace />;
}
