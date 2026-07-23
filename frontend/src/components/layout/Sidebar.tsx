import { Box, List, ListItem, ListItemButton, ListItemIcon, ListItemText, Typography, Divider, Chip, Collapse } from '@mui/material';
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
  DocumentMagnifyingGlassIcon,
  PlusCircleIcon,
  ClipboardDocumentListIcon,
  UserPlusIcon,
  CameraIcon,
  CloudArrowUpIcon,
  MagnifyingGlassCircleIcon,
  ClockIcon,
  ChevronDownIcon,
  ChevronRightIcon
} from '@heroicons/react/24/outline';
import { useAuthStore } from '../../store/authStore';
import { useState } from 'react';

// OCP Primary Green
const PRIMARY_COLOR = '#16a34a';

// ─── ADMIN ────────────────────────────────────────────────
const ADMIN_MENU = {
  label: 'Administration',
  sections: [
    {
      title: 'Tableau de bord',
      items: [
        { text: 'Vue d\'ensemble', path: '/dashboard', icon: <HomeIcon width={20} /> },
        { text: 'Statistiques', path: '/administration/statistiques', icon: <DocumentMagnifyingGlassIcon width={20} /> },
      ],
    },
    {
      title: 'Gestion des comptes',
      items: [
        { text: 'Utilisateurs', path: '/administration/utilisateurs', icon: <UserGroupIcon width={20} /> },
        { text: 'Rôles & Permissions', path: '/administration/roles', icon: <ShieldCheckIcon width={20} /> },
        { text: 'Inscriptions en attente', path: '/administration/inscriptions', icon: <UserPlusIcon width={20} />, badge: true },
      ],
    },
    {
      title: 'Système',
      items: [
        { text: 'Journal d\'audit', path: '/administration/audit', icon: <DocumentMagnifyingGlassIcon width={20} /> },
        { text: 'Paramètres', path: '/administration/parametres', icon: <Cog6ToothIcon width={20} /> },
      ],
    },
    {
      title: 'Référentiels',
      collapsible: true,
      defaultOpen: true,
      items: [
        { text: 'Installations', path: '/administration/installations', icon: <BookOpenIcon width={20} /> },
        { text: 'Zones', path: '/administration/zones', icon: <BookOpenIcon width={20} /> },
        { text: 'Équipements', path: '/administration/equipements', icon: <BookOpenIcon width={20} /> },
        { text: 'Risques', path: '/administration/risques', icon: <BookOpenIcon width={20} /> },
        { text: 'Mesures de prévention', path: '/administration/mesures-prevention', icon: <BookOpenIcon width={20} /> },
        { text: 'EPI', path: '/administration/epis', icon: <BookOpenIcon width={20} /> },
        { text: "Moyens d'accès", path: '/administration/moyens-acces', icon: <BookOpenIcon width={20} /> },
        { text: 'Types de permis', path: '/administration/types-permis', icon: <BookOpenIcon width={20} /> },
        { text: 'Entreprises externes', path: '/administration/entreprises', icon: <BookOpenIcon width={20} /> },
        { text: 'Services OCP', path: '/administration/services', icon: <BookOpenIcon width={20} /> },
      ],
    },
  ],
};

// ─── DEMANDEUR ────────────────────────────────────────────
const DEMANDEUR_MENU = {
  label: 'Demandeur',
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
    <ListItem disablePadding sx={{ mb: 0.5, px: 2 }}>
      <ListItemButton
        component={NavLink as any}
        to={path}
        sx={{
          borderRadius: 2,
          py: 1,
          px: 2,
          color: 'text.secondary',
          '&.active': {
            bgcolor: `${PRIMARY_COLOR}15`,
            color: PRIMARY_COLOR,
            fontWeight: 600,
            '& .MuiListItemIcon-root': { color: PRIMARY_COLOR },
          },
          '&:hover': {
            bgcolor: 'action.hover',
          }
        }}
      >
        <ListItemIcon sx={{ minWidth: 36, color: 'inherit' }}>
          {icon}
        </ListItemIcon>
        <ListItemText
          primary={text}
          slotProps={{ primary: { style: { fontSize: 14, fontWeight: 'inherit' } } }}
        />
        {badge && pendingCount !== undefined && pendingCount > 0 && (
          <Chip
            label={pendingCount}
            size="small"
            color="error"
            sx={{ height: 20, fontSize: 11, fontWeight: 700, minWidth: 24 }}
          />
        )}
      </ListItemButton>
    </ListItem>
  );
}

function SectionMenuRow({ section }: { section: any }) {
  const [open, setOpen] = useState(section.defaultOpen ?? false);

  if (section.collapsible) {
    return (
      <Box sx={{ mb: 2 }}>
        <Box
          onClick={() => setOpen(!open)}
          sx={{
            px: 4,
            py: 1,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            cursor: 'pointer',
            '&:hover': { bgcolor: 'action.hover' },
          }}
        >
          <Typography
            variant="caption"
            sx={{
              color: 'text.secondary',
              fontWeight: 600,
              fontSize: 12,
            }}
          >
            {section.title}
          </Typography>
          {open ? (
            <ChevronDownIcon width={14} style={{ color: '#64748b' }} />
          ) : (
            <ChevronRightIcon width={14} style={{ color: '#64748b' }} />
          )}
        </Box>
        <Collapse in={open} timeout="auto" unmountOnExit>
          <List disablePadding>
            {section.items.map((item: any) => (
              <MenuItemRow key={item.text} {...item} />
            ))}
          </List>
        </Collapse>
      </Box>
    );
  }

  return (
    <Box sx={{ mb: 2 }}>
      <Typography
        variant="caption"
        sx={{
          px: 4,
          py: 1,
          display: 'block',
          color: 'text.secondary',
          fontWeight: 600,
          fontSize: 12,
        }}
      >
        {section.title}
      </Typography>
      <List disablePadding>
        {section.items.map((item: any) => (
          <MenuItemRow key={item.text} {...item} />
        ))}
      </List>
    </Box>
  );
}

export default function Sidebar() {
  const user = useAuthStore((s) => s.user);

  const hasRole = (roleName: string) =>
    user?.roles?.some((r) => r.nom === roleName) ?? false;

  let menuConfig = DEMANDEUR_MENU;
  if (hasRole('ADMIN')) menuConfig = ADMIN_MENU;
  else if (hasRole('RESPONSABLE_OCP')) menuConfig = RESPONSABLE_OCP_MENU;
  else if (hasRole('RESPONSABLE_ENTREPRISE')) menuConfig = RESPONSABLE_ENTREPRISE_MENU;

  return (
    <Box
      sx={{
        width: 280,
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
        '&::-webkit-scrollbar': { width: '4px' },
        '&::-webkit-scrollbar-thumb': { backgroundColor: '#cbd5e1', borderRadius: '4px' },
      }}
    >
      {/* OCP Logo Header */}
      <Box sx={{ p: 3, display: 'flex', alignItems: 'center', gap: 2 }}>
        {/* Placeholder for the OCP star logo */}
        <Box sx={{ display: 'flex', flexDirection: 'column' }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <svg width="32" height="32" viewBox="0 0 100 100" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M50 10L61 40H93L67 59L77 89L50 70L23 89L33 59L7 40H39L50 10Z" fill={PRIMARY_COLOR} />
              <circle cx="50" cy="50" r="15" fill="white" />
              <circle cx="50" cy="50" r="10" fill={PRIMARY_COLOR} />
            </svg>
            <Typography variant="h5" sx={{ fontWeight: 900, color: PRIMARY_COLOR, letterSpacing: -0.5 }}>
              OCP
            </Typography>
          </Box>
          <Typography variant="caption" sx={{ color: PRIMARY_COLOR, fontWeight: 700, fontSize: '0.6rem', letterSpacing: 0.5, mt: -0.5, ml: 0.5 }}>
            SUCCESSFUL TOGETHER
          </Typography>
        </Box>
      </Box>

      {/* Navigation */}
      <Box sx={{ flexGrow: 1, py: 1 }}>
        <SectionMenuRow section={{ title: '', collapsible: false, items: menuConfig.sections[0]?.items ?? [] }} />
        {menuConfig.sections.slice(1).map((section) => (
          <SectionMenuRow key={section.title} section={section} />
        ))}
      </Box>

      {/* Footer / Copyright */}
      <Box sx={{ p: 3, mt: 'auto' }}>
        <Typography variant="caption" color="text.secondary" sx={{ display: 'block', fontSize: 11 }}>
          © 2026 OCP Group
          <br />
          Tous droits réservés
        </Typography>
      </Box>
    </Box>
  );
}