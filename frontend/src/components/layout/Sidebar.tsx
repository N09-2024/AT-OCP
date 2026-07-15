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
  ChartBarIcon,
  DocumentMagnifyingGlassIcon,
  PlusCircleIcon,
  ClipboardDocumentListIcon,
  UserPlusIcon,
  CameraIcon,
  CloudArrowUpIcon,
  MagnifyingGlassCircleIcon,
  ClockIcon,
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
            bgcolor: '#e6f4ea',
            color: 'primary.dark',
            fontWeight: 600,
            '& .MuiListItemIcon-root': { color: 'primary.dark' },
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

import { useState } from 'react';
import { Collapse } from '@mui/material';
import { ChevronDownIcon, ChevronRightIcon } from '@heroicons/react/24/outline';

function SectionMenu({ sections }: { sections: any[] }) {
  return (
    <>
      {sections.map((section) => (
        <SectionMenuRow key={section.title} section={section} />
      ))}
    </>
  );
}

function SectionMenuRow({ section }: { section: any }) {
  const [open, setOpen] = useState(section.defaultOpen ?? false);

  if (section.collapsible) {
    return (
      <Box sx={{ mb: 1 }}>
        <Box
          onClick={() => setOpen(!open)}
          sx={{
            px: 2,
            py: 0.5,
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
              color: 'text.disabled',
              fontWeight: 700,
              letterSpacing: '0.08em',
              textTransform: 'uppercase',
              fontSize: 10,
            }}
          >
            {section.title}
          </Typography>
          {open ? (
            <ChevronDownIcon width={12} style={{ color: '#999' }} />
          ) : (
            <ChevronRightIcon width={12} style={{ color: '#999' }} />
          )}
        </Box>
        <Collapse in={open} timeout="auto" unmountOnExit>
          <List sx={{ px: 1.5, py: 0 }}>
            {section.items.map((item: any) => (
              <MenuItemRow key={item.text} {...item} />
            ))}
          </List>
        </Collapse>
      </Box>
    );
  }

  return (
    <Box sx={{ mb: 1 }}>
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
        '&::-webkit-scrollbar': {
          width: '5px',
        },
        '&::-webkit-scrollbar-thumb': {
          backgroundColor: '#e5e7eb',
          borderRadius: '4px',
        },
        '&::-webkit-scrollbar-thumb:hover': {
          backgroundColor: '#d1d5db',
        },
        '&::-webkit-scrollbar-track': {
          backgroundColor: 'transparent',
        },
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
        <SectionMenu sections={menuConfig.sections} />
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
            <Typography variant="body2" sx={{ fontWeight: 600, lineHeight: 1.2, whiteSpace: 'nowrap' }}>
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