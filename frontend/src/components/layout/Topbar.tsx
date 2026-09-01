import { Box, Typography, IconButton, Avatar, Badge, Menu, MenuItem, Breadcrumbs, Divider } from '@mui/material';
import { BellIcon, ChevronDownIcon, ChevronRightIcon, UserCircleIcon, LockClosedIcon, ArrowRightOnRectangleIcon } from '@heroicons/react/24/outline';
import { useState } from 'react';
import { useNavigate, useLocation, Link } from 'react-router-dom';
import { useAuthStore } from '../../store/authStore';
import { useNotifications } from '../../hooks/useNotifications';

export default function Topbar() {
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const navigate = useNavigate();
  const location = useLocation();
  const logout = useAuthStore((state) => state.logout);
  const user = useAuthStore((state) => state.user);
  const { unreadCount } = useNotifications();

  const prenom = user?.prenom ?? 'Utilisateur';
  const nom = user?.nom ?? '';
  const role = user?.roles?.[0]?.nom ?? 'Utilisateur';

  const handleMenu = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };

  const handleClose = () => {
    setAnchorEl(null);
  };

  const handleLogout = () => {
    handleClose();
    logout();
    navigate('/auth/login');
  };

  const pathnames = location.pathname.split('/').filter((x) => x);

  const PATH_LABELS: Record<string, string> = {
    dashboard: 'Tableau de bord',
    administration: 'Administration',
    utilisateurs: 'Utilisateurs',
    roles: 'Rôles',
    inscriptions: 'Inscriptions',
    audit: "Journal d'audit",
    parametres: 'Paramètres',
    zones: 'Zones',
    equipements: 'Équipements',
    risques: 'Risques',
    'mesures-prevention': 'Mesures de prévention',
    epis: 'EPI',
    'moyens-acces': "Moyens d'accès",
    'types-permis': 'Types de permis',
    entreprises: 'Entreprises',
    services: 'Services',
    permis: 'Permis',
    notifications: 'Notifications',
    profil: 'Mon Profil',
    nouveau: 'Nouveau',
  };

  return (
    <Box
      sx={{
        height: 72,
        bgcolor: 'white',
        borderBottom: '1px solid',
        borderColor: 'divider',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        px: 4,
        flexShrink: 0,
      }}
    >
      {/* Left: Breadcrumb */}
      <Box sx={{ display: 'flex', alignItems: 'center', minWidth: 0, flex: 1 }}>
        {pathnames.length > 0 ? (
          <Breadcrumbs separator={<ChevronRightIcon width={12} color="#5C6E67" />} aria-label="breadcrumb">
            {pathnames.map((value, index) => {
              const last = index === pathnames.length - 1;
              const to = `/${pathnames.slice(0, index + 1).join('/')}`;
              const name = PATH_LABELS[value] ?? (value.charAt(0).toUpperCase() + value.slice(1).replace('-', ' '));

              return last ? (
                <Typography color="text.primary" key={to} sx={{ fontWeight: 600, fontSize: 14 }}>
                  {name}
                </Typography>
              ) : (
                <Typography
                  component={Link}
                  to={to}
                  color="text.secondary"
                  key={to}
                  sx={{ fontSize: 14, textDecoration: 'none', '&:hover': { color: 'primary.main' } }}
                >
                  {name}
                </Typography>
              );
            })}
          </Breadcrumbs>
        ) : (
          <Typography variant="h6" sx={{ fontWeight: 'bold' }} color="text.primary">
            AT System
          </Typography>
        )}
      </Box>



      {/* Right: Bell + User */}
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 3 }}>
        {/* Notification bell */}
        <IconButton
          sx={{ color: 'text.secondary' }}
          onClick={() => navigate('/notifications')}
          title="Voir les notifications"
        >
          <Badge
            badgeContent={unreadCount || null}
            color="error"
            sx={{ '& .MuiBadge-badge': { right: -3, top: 3 } }}
          >
            <BellIcon width={22} />
          </Badge>
        </IconButton>

        {/* User menu */}
        <Box
          sx={{ display: 'flex', alignItems: 'center', gap: 1.5, cursor: 'pointer' }}
          onClick={handleMenu}
        >
          <Avatar sx={{ width: 38, height: 38, bgcolor: 'primary.main', color: 'white', fontSize: 14 }}>
            {prenom[0]}{nom[0]}
          </Avatar>
          <Box sx={{ display: { xs: 'none', md: 'block' } }}>
            <Typography variant="body2" sx={{ fontWeight: 600 }} color="text.primary">
              {prenom} {nom}
            </Typography>
            <Typography variant="caption" color="text.secondary">
              {role.replace(/_/g, ' ')}
            </Typography>
          </Box>
          <ChevronDownIcon width={16} color="#5C6E67" />
        </Box>

        <Menu
          anchorEl={anchorEl}
          open={Boolean(anchorEl)}
          onClose={handleClose}
          anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
          transformOrigin={{ vertical: 'top', horizontal: 'right' }}
          slotProps={{ paper: { elevation: 3, sx: { mt: 1, borderRadius: 2, minWidth: 180 } } }}
        >
          <MenuItem
            onClick={() => { handleClose(); navigate('/profil'); }}
            sx={{ gap: 1.5 }}
          >
            <UserCircleIcon width={18} color="#5C6E67" />
            Mon Profil
          </MenuItem>
          <MenuItem
            onClick={() => { handleClose(); navigate('/profil?tab=security'); }}
            sx={{ gap: 1.5 }}
          >
            <LockClosedIcon width={18} color="#5C6E67" />
            Modifier le mot de passe
          </MenuItem>
          <Divider />
          <MenuItem onClick={handleLogout} sx={{ color: 'error.main', gap: 1.5 }}>
            <ArrowRightOnRectangleIcon width={18} />
            Déconnexion
          </MenuItem>
        </Menu>
      </Box>
    </Box>
  );
}
