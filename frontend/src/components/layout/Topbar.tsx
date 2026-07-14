import { Box, Typography, IconButton, Avatar, Badge, Menu, MenuItem } from '@mui/material';
import { BellIcon, ChevronDownIcon } from '@heroicons/react/24/outline';
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '../../store/authStore';

export default function Topbar() {
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const navigate = useNavigate();
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
      <Box>
        <Typography variant="h6" sx={{ fontWeight: 'bold' }} color="text.primary">
          Tableau de bord
        </Typography>
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
          <Avatar sx={{ width: 38, height: 38, bgcolor: 'grey.200', color: 'grey.700', fontSize: 14 }}>
            {prenom[0]}{nom[0]}
          </Avatar>
          <Box sx={{ display: { xs: 'none', md: 'block' } }}>
            <Typography variant="body2" sx={{ fontWeight: 600 }} color="text.primary">
              {prenom} {nom}
            </Typography>
            <Typography variant="caption" color="text.secondary">
              {role}
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
