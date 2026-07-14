import { Box, List, ListItem, ListItemButton, ListItemIcon, ListItemText, Typography, Divider, Chip } from '@mui/material';
import { NavLink } from 'react-router-dom';
import {
  HomeIcon,
  BookOpenIcon,
  DocumentTextIcon,
  UserGroupIcon,
  ClipboardDocumentCheckIcon,
  ShieldCheckIcon,
  CheckBadgeIcon,
  ArchiveBoxIcon,
  Cog6ToothIcon,
  WrenchScrewdriverIcon,
  ChartBarIcon,
  DocumentMagnifyingGlassIcon,
  PlusCircleIcon,
  ClipboardDocumentListIcon,
  UserPlusIcon,
  CameraIcon,
  CloudArrowUpIcon,
  MagnifyingGlassCircleIcon,
  ClockIcon,
  BellAlertIcon,
} from '@heroicons/react/24/outline';
import { useAuthStore } from '../../store/authStore';

// ─── ADMIN ────────────────────────────────────────────────
const ADMIN_MENU = {
  label: 'Administration',
  color: '#7c3aed',
  sections: [
    {
      title: 'Tableau de bord',
      items: [
        { text: 'Vue d\'ensemble', path: '/dashboard', icon: <HomeIcon width={20} /> },
        { text: 'Statistiques', path: '/administration/stats', icon: <ChartBarIcon width={20} /> },
      ],
    },
    {
      title: 'Gestion des comptes',
      items: [
        { text: 'Utilisateurs', path: '/administration/utilisateurs', icon: <UserGroupIcon width={20} /> },
        { text: 'Inscriptions en attente', path: '/administration/inscriptions', icon: <UserPlusIcon width={20} />, badge: true },
        { text: 'Rôles & Permissions', path: '/administration/roles', icon: <ShieldCheckIcon width={20} /> },
      ],
    },
    {
      title: 'Système',
      items: [
        { text: 'Référentiels', path: '/referentiels', icon: <BookOpenIcon width={20} /> },
        { text: 'Journal d\'audit', path: '/administration/audit', icon: <DocumentMagnifyingGlassIcon width={20} /> },
        { text: 'Paramètres', path: '/administration/parametres', icon: <Cog6ToothIcon width={20} /> },
      ],
    },
  ],
};

// ─── DEMANDEUR ────────────────────────────────────────────
const DEMANDEUR_MENU = {
  label: 'Demandeur',
  color: '#0891b2',
  sections: [
    {
      title: 'Mon espace',
      items: [
        { text: 'Tableau de bord', path: '/dashboard', icon: <HomeIcon width={20} /> },
        { text: 'Mes autorisations', path: '/autorisations', icon: <ClipboardDocumentCheckIcon width={20} /> },
      ],
    },
    {
      title: 'Actions',
      items: [
        { text: 'Nouvelle demande', path: '/autorisations/nouvelle', icon: <PlusCircleIcon width={20} /> },
        { text: 'Mes documents', path: '/documents', icon: <DocumentTextIcon width={20} /> },
      ],
    },
  ],
};

// ─── RESPONSABLE OCP ─────────────────────────────────────
const RESPONSABLE_OCP_MENU = {
  label: 'Responsable OCP',
  color: '#16a34a',
  sections: [
    {
      title: 'Mon espace',
      items: [
        { text: 'Tableau de bord', path: '/dashboard', icon: <HomeIcon width={20} /> },
        { text: 'Autorisations à traiter', path: '/autorisations', icon: <ClipboardDocumentListIcon width={20} /> },
      ],
    },
    {
      title: 'Validation',
      items: [
        { text: 'À signer', path: '/autorisations?filtre=a-signer', icon: <CheckBadgeIcon width={20} /> },
        { text: 'À valider / Rejeter', path: '/autorisations?filtre=a-valider', icon: <ClockIcon width={20} /> },
        { text: 'Réceptionner travaux', path: '/receptions', icon: <ArchiveBoxIcon width={20} /> },
      ],
    },
    {
      title: 'Permis',
      items: [
        { text: 'Consulter les permis', path: '/permis', icon: <ShieldCheckIcon width={20} /> },
      ],
    },
  ],
};

// ─── RESPONSABLE ENTREPRISE EXTERNE ──────────────────────
const RESPONSABLE_ENTREPRISE_MENU = {
  label: 'Resp. Entreprise',
  color: '#ea580c',
  sections: [
    {
      title: 'Mon espace',
      items: [
        { text: 'Tableau de bord', path: '/dashboard', icon: <HomeIcon width={20} /> },
      ],
    },
    {
      title: 'Permis de travail',
      items: [
        { text: 'Importer un permis', path: '/permis/importer', icon: <CloudArrowUpIcon width={20} /> },
        { text: 'Photographier un permis', path: '/permis/photographier', icon: <CameraIcon width={20} /> },
        { text: 'Consulter les résultats', path: '/permis', icon: <MagnifyingGlassCircleIcon width={20} /> },
      ],
    },
  ],
};

interface MenuItemRowProps {
  text: string;
  path: string;
  icon: React.ReactNode;
  badge?: boolean;
  pendingCount?: number;
}

function MenuItemRow({ text, path, icon, badge, pendingCount }: MenuItemRowProps) {
  return (
    <ListItem disablePadding sx={{ mb: 0.25 }}>
      <ListItemButton
        component={NavLink as any}
        to={path}
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
          {icon}
        </ListItemIcon>
        <ListItemText
          primary={text}
          slotProps={{ primary: { style: { fontSize: 13, fontWeight: 500 } } }}
        />
        {badge && pendingCount !== undefined && pendingCount > 0 && (
          <Chip
            label={pendingCount}
            size="small"
            color="error"
            sx={{ height: 18, fontSize: 10, fontWeight: 700, minWidth: 22 }}
          />
        )}
      </ListItemButton>
    </ListItem>
  );
}

function SectionMenu({ sections, color }: { sections: typeof ADMIN_MENU['sections']; color: string }) {
  return (
    <>
      {sections.map((section) => (
        <Box key={section.title} sx={{ mb: 1 }}>
          <Typography
            variant="caption"
            sx={{
              px: 2,
              py: 0.5,
              display: 'block',
              color: 'text.disabled',
              fontWeight: 700,
              letterSpacing: '0.08em',
              textTransform: 'uppercase',
              fontSize: 10,
            }}
          >
            {section.title}
          </Typography>
          <List sx={{ px: 1.5, py: 0 }}>
            {section.items.map((item) => (
              <MenuItemRow key={item.text} {...item} />
            ))}
          </List>
        </Box>
      ))}
    </>
  );
}

export default function Sidebar() {
  const user = useAuthStore((s) => s.user);

  const hasRole = (roleName: string) =>
    user?.roles?.some((r) => r.nom === roleName) ?? false;

  // Determine active menu config
  let menuConfig = DEMANDEUR_MENU; // default
  if (hasRole('ADMIN')) menuConfig = ADMIN_MENU;
  else if (hasRole('RESPONSABLE_OCP')) menuConfig = RESPONSABLE_OCP_MENU;
  else if (hasRole('RESPONSABLE_ENTREPRISE')) menuConfig = RESPONSABLE_ENTREPRISE_MENU;

  return (
    <Box
      sx={{
        width: 270,
        flexShrink: 0,
        height: '100vh',
        position: 'sticky',
        top: 0,
        bgcolor: 'white',
        borderRight: '1px solid',
        borderColor: 'divider',
        display: 'flex',
        flexDirection: 'column',
        overflowY: 'auto',
      }}
    >
      {/* Logo */}
      <Box sx={{ p: 2.5, display: 'flex', alignItems: 'center', gap: 2 }}>
        <Box
          sx={{
            width: 38,
            height: 38,
            bgcolor: menuConfig.color,
            borderRadius: 2,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            color: 'white',
            fontSize: 10,
            fontWeight: 800,
            letterSpacing: 1,
            flexShrink: 0,
          }}
        >
          OCP
        </Box>
        <Box>
          <Typography variant="subtitle2" sx={{ fontWeight: 700, lineHeight: 1.2, color: 'text.primary' }}>
            AT System
          </Typography>
          <Typography variant="caption" sx={{ color: menuConfig.color, fontWeight: 600 }}>
            {menuConfig.label}
          </Typography>
        </Box>
      </Box>

      <Divider />

      {/* Navigation */}
      <Box sx={{ flexGrow: 1, overflowY: 'auto', py: 1.5 }}>
        <SectionMenu sections={menuConfig.sections} color={menuConfig.color} />
      </Box>

      <Divider />

      {/* Bottom: user info */}
      <Box sx={{ px: 2.5, py: 2 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, mb: 1 }}>
          <Box
            sx={{
              width: 32,
              height: 32,
              borderRadius: '50%',
              bgcolor: menuConfig.color + '22',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: menuConfig.color,
              fontWeight: 700,
              fontSize: 12,
              flexShrink: 0,
            }}
          >
            {user?.prenom?.[0]?.toUpperCase()}{user?.nom?.[0]?.toUpperCase()}
          </Box>
          <Box sx={{ overflow: 'hidden' }}>
            <Typography variant="body2" sx={{ fontWeight: 600, lineHeight: 1.2, noWrap: true }}>
              {user?.prenom} {user?.nom}
            </Typography>
            <Typography variant="caption" color="text.secondary" sx={{ fontSize: 10 }}>
              {user?.email}
            </Typography>
          </Box>
        </Box>
        <Box sx={{ display: 'flex', gap: 0.5, flexWrap: 'wrap' }}>
          {user?.roles?.map((r) => (
            <Chip
              key={r.id}
              label={r.nom.replace('_', ' ')}
              size="small"
              sx={{
                bgcolor: menuConfig.color + '18',
                color: menuConfig.color,
                fontWeight: 700,
                fontSize: 9,
                height: 18,
              }}
            />
          ))}
        </Box>
        <Typography variant="caption" color="text.disabled" sx={{ mt: 1.5, display: 'block' }}>
          © 2026 OCP Group
        </Typography>
      </Box>
    </Box>
  );
}