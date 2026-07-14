import { Box, Typography, IconButton, Avatar, Badge, Menu, MenuItem, Breadcrumbs } from '@mui/material';
import { BellIcon, ChevronDownIcon, ChevronRightIcon } from '@heroicons/react/24/outline';
import { useState } from 'react';
import { useNavigate, useLocation, Link } from 'react-router-dom';
import { useAuthStore } from '../../store/authStore';

export default function Topbar() {
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const navigate = useNavigate();
  const location = useLocation();
  const logout = useAuthStore((state) => state.logout);
  const user = useAuthStore((state) => state.user);

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
      <Box sx={{ display: 'flex', alignItems: 'center' }}>
        {pathnames.length > 0 ? (
          <Breadcrumbs separator={<ChevronRightIcon width={12} color="#9ca3af" />} aria-label="breadcrumb">
            {pathnames.map((value, index) => {
              const last = index === pathnames.length - 1;
              const to = `/${pathnames.slice(0, index + 1).join('/')}`;
              let name = value.charAt(0).toUpperCase() + value.slice(1).replace('-', ' ');
              if (name === 'Dashboard') name = 'Tableau de bord';

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

      <Box sx={{ display: 'flex', alignItems: 'center', gap: 3 }}>
        <IconButton sx={{ color: 'text.secondary' }}>
          <Badge
            badgeContent={5}
            color="success"
            sx={{ '& .MuiBadge-badge': { right: -3, top: 3 } }}
          >
            <BellIcon width={22} />
          </Badge>
        </IconButton>

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
              {role.replace('_', ' ')}
            </Typography>
          </Box>
          <ChevronDownIcon width={16} color="#6B7280" />
        </Box>

        <Menu
          anchorEl={anchorEl}
          open={Boolean(anchorEl)}
          onClose={handleClose}
          anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
          transformOrigin={{ vertical: 'top', horizontal: 'right' }}
          slotProps={{ paper: { elevation: 3, sx: { mt: 1, borderRadius: 2, minWidth: 160 } } }}
        >
          <MenuItem onClick={handleClose}>Mon Profil</MenuItem>
          <MenuItem onClick={handleClose}>Modifier le mot de passe</MenuItem>
          <MenuItem onClick={handleLogout} sx={{ color: 'error.main' }}>
            Déconnexion
          </MenuItem>
        </Menu>
      </Box>
    </Box>
  );
}
