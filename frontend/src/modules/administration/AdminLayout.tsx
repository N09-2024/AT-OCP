import { Outlet, NavLink } from 'react-router-dom';
import { Box, Typography, List, ListItem, ListItemButton, ListItemIcon, ListItemText, Divider } from '@mui/material';
import {
  HomeIcon,
  UsersIcon,
  ShieldCheckIcon,
  Cog6ToothIcon,
  ClockIcon,
  ArrowLeftOnRectangleIcon,
  UserPlusIcon,
} from '@heroicons/react/24/outline';

const ADMIN_MENU = [
  { text: 'Tableau de bord', path: '/administration', icon: <HomeIcon width={20} />, end: true },
  { text: 'Utilisateurs', path: '/administration/utilisateurs', icon: <UsersIcon width={20} />, end: false },
  { text: 'Rôles & Permissions', path: '/administration/roles', icon: <ShieldCheckIcon width={20} />, end: false },
  { text: 'Inscriptions', path: '/administration/inscriptions', icon: <UserPlusIcon width={20} />, end: false },
  { text: 'Paramètres', path: '/administration/parametres', icon: <Cog6ToothIcon width={20} />, end: false },
  { text: "Journal d'activité", path: '/administration/audit', icon: <ClockIcon width={20} />, end: false },
];

export default function AdminLayout() {
  return (
    <Box sx={{ display: 'flex', gap: 3, minHeight: 'calc(100vh - 100px)' }}>
      {/* Admin Sidebar */}
      <Box
        sx={{
          width: 260,
          flexShrink: 0,
          bgcolor: 'white',
          borderRadius: 3,
          border: '1px solid',
          borderColor: 'divider',
          display: 'flex',
          flexDirection: 'column',
          alignSelf: 'flex-start',
          position: 'sticky',
          top: 88,
        }}
      >
        <Box sx={{ p: 2.5, pb: 1.5 }}>
          <Typography variant="subtitle1" sx={{ fontWeight: 700, color: 'text.primary' }}>
            Administration
          </Typography>
          <Typography variant="caption" color="text.secondary">
            Gestion du système
          </Typography>
        </Box>
        <Divider />
        <List sx={{ px: 1.5, py: 1.5 }}>
          {ADMIN_MENU.map((item) => (
            <ListItem key={item.text} disablePadding sx={{ mb: 0.25 }}>
              <ListItemButton
                component={NavLink as any}
                to={item.path}
                end={item.end}
                sx={{
                  borderRadius: 2,
                  py: 1,
                  '&.active': {
                    bgcolor: 'primary.main',
                    color: 'white',
                    '& .MuiListItemIcon-root': { color: 'white' },
                  },
                }}
              >
                <ListItemIcon sx={{ minWidth: 36, color: 'text.secondary' }}>
                  {item.icon}
                </ListItemIcon>
                <ListItemText
                  primary={item.text}
                  slotProps={{ primary: { style: { fontSize: 13, fontWeight: 500 } } }}
                />
              </ListItemButton>
            </ListItem>
          ))}
        </List>
        <Divider />
        <Box sx={{ p: 2 }}>
          <ListItemButton
            component={NavLink as any}
            to="/dashboard"
            sx={{ borderRadius: 2, py: 1 }}
          >
            <ListItemIcon sx={{ minWidth: 36, color: 'text.secondary' }}>
              <ArrowLeftOnRectangleIcon width={20} />
            </ListItemIcon>
            <ListItemText
              primary="Retour au tableau de bord"
              slotProps={{ primary: { style: { fontSize: 13, fontWeight: 500 } } }}
            />
          </ListItemButton>
        </Box>
      </Box>

      {/* Admin Content */}
      <Box sx={{ flex: 1, minWidth: 0 }}>
        <Outlet />
      </Box>
    </Box>
  );
}