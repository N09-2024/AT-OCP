import { Box, List, ListItem, ListItemButton, ListItemIcon, ListItemText, Typography, Collapse, Chip } from '@mui/material';
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
  FolderIcon,
  ChevronDownIcon,
  ChevronRightIcon,
  MapPinIcon,
  BellIcon,
} from '@heroicons/react/24/outline';
import { useAuthStore } from '../../store/authStore';
import { usePrimaryRole } from '../../hooks/usePrimaryRole';
import { useState } from 'react';

// OCP Primary Green
const PRIMARY_COLOR = '#1F4D3E';

// ─── ADMIN ────────────────────────────────────────────────
const ADMIN_MENU = {
  label: 'Administration Système',
  sections: [
    {
      title: 'Vue d\'ensemble',
      items: [
        { text: 'Tableau de bord Admin', path: '/dashboard/admin', icon: <HomeIcon width={20} /> },
        { text: 'Statistiques & KPI', path: '/administration/statistiques', icon: <DocumentMagnifyingGlassIcon width={20} /> },
        { text: 'Vue globale AT', path: '/dashboard/global', icon: <ClipboardDocumentCheckIcon width={20} /> },
      ],
    },
    {
      title: 'Gestion des AT & Travaux',
      items: [
        { text: 'Autorisations de Travail', path: '/autorisations', icon: <ClipboardDocumentCheckIcon width={20} /> },
        { text: 'Documents source', path: '/documents', icon: <DocumentTextIcon width={20} /> },
        { text: 'Visites préalables', path: '/visites', icon: <MapPinIcon width={20} /> },
        { text: 'Permis de travail', path: '/permis', icon: <ShieldCheckIcon width={20} /> },
        { text: 'Réceptions travaux', path: '/receptions', icon: <ArchiveBoxIcon width={20} /> },
        { text: 'Archives & PDF', path: '/archives', icon: <FolderIcon width={20} /> },
      ],
    },
    {
      title: 'Gestion des comptes & Habilitations',
      items: [
        { text: 'Utilisateurs', path: '/administration/utilisateurs', icon: <UserGroupIcon width={20} /> },
        { text: 'Rôles & Permissions', path: '/administration/roles', icon: <ShieldCheckIcon width={20} /> },
        { text: 'Habilitations agents', path: '/habilitations', icon: <CheckBadgeIcon width={20} /> },
        { text: 'Inscriptions en attente', path: '/administration/inscriptions', icon: <UserPlusIcon width={20} /> },
      ],
    },
    {
      title: 'Système & Audit',
      items: [
        { text: 'Journal d\'audit', path: '/administration/audit', icon: <DocumentMagnifyingGlassIcon width={20} /> },
        { text: 'Paramètres', path: '/administration/parametres', icon: <Cog6ToothIcon width={20} /> },
      ],
    },
    {
      title: 'Référentiels OCP',
      collapsible: true,
      defaultOpen: false,
      items: [
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

// ─── CE (Chef d'Équipe) ──────────────────────────────────
const CE_MENU = {
  label: "Chef d'Équipe (CE)",
  sections: [
    {
      title: 'Espace Terrain & Intervention',
      items: [
        { text: 'Tableau de bord CE', path: '/dashboard/ce', icon: <HomeIcon width={20} /> },
        { text: 'Mes Autorisations (AT)', path: '/autorisations', icon: <ClipboardDocumentCheckIcon width={20} /> },
        { text: 'Nouvelle Demande / AT', path: '/autorisations/nouvelle', icon: <PlusCircleIcon width={20} /> },
      ],
    },
    {
      title: 'Suivi du Processus',
      items: [
        { text: 'Documents source (DI/OT/BT)', path: '/documents', icon: <DocumentTextIcon width={20} /> },
        { text: 'Visites préalables', path: '/visites', icon: <MapPinIcon width={20} /> },
        { text: 'Gestion des permis', path: '/permis', icon: <ShieldCheckIcon width={20} /> },
        { text: 'Réception travaux', path: '/receptions', icon: <ArchiveBoxIcon width={20} /> },
        { text: 'Notifications', path: '/notifications', icon: <BellIcon width={20} /> },
      ],
    },
  ],
};

// ─── HM (Haute Maîtrise) ──────────────────────────────────
const HM_MENU = {
  label: 'Haute Maîtrise (HM)',
  sections: [
    {
      title: 'Garantie & Surveillance',
      items: [
        { text: 'Tableau de bord HM', path: '/dashboard/hm', icon: <HomeIcon width={20} /> },
        { text: 'Visites à garantir', path: '/visites', icon: <MapPinIcon width={20} /> },
        { text: 'AT à valider (soumises)', path: '/autorisations?filtre=SOUMISE', icon: <CheckBadgeIcon width={20} /> },
      ],
    },
    {
      title: 'Consultation & Suivi',
      items: [
        { text: 'Toutes les AT du secteur', path: '/autorisations', icon: <ClipboardDocumentCheckIcon width={20} /> },
        { text: 'Permis de travail', path: '/permis', icon: <ShieldCheckIcon width={20} /> },
        { text: 'Notifications', path: '/notifications', icon: <BellIcon width={20} /> },
      ],
    },
  ],
};

// ─── HC (Hors Cadre) ──────────────────────────────────────
const HC_MENU = {
  label: 'Hors Cadre (HC)',
  sections: [
    {
      title: 'Classification & Garantie (§6-§8)',
      items: [
        { text: 'Tableau de bord HC', path: '/dashboard/hc', icon: <HomeIcon width={20} /> },
        { text: 'Classifier intervention (Niv 1/2)', path: '/documents', icon: <DocumentTextIcon width={20} /> },
        { text: 'AT à garantir / valider', path: '/autorisations?filtre=a-valider', icon: <CheckBadgeIcon width={20} /> },
      ],
    },
    {
      title: 'Pilotage & Archivage',
      items: [
        { text: 'Archives officielles AT', path: '/archives', icon: <FolderIcon width={20} /> },
        { text: 'Habilitations agents AT', path: '/habilitations', icon: <ShieldCheckIcon width={20} /> },
        { text: 'Toutes les ATs', path: '/autorisations', icon: <ClipboardDocumentCheckIcon width={20} /> },
        { text: 'Notifications', path: '/notifications', icon: <BellIcon width={20} /> },
      ],
    },
  ],
};

// ─── RESPONSABLE_EXTERIEUR ───────────────────────────────
const RESPONSABLE_EXTERIEUR_MENU = {
  label: 'Entreprise Extérieure',
  sections: [
    {
      title: 'Bons de Travaux & Permis',
      items: [
        { text: 'Tableau de bord Extérieur', path: '/dashboard/externe', icon: <HomeIcon width={20} /> },
        { text: 'Gestion des permis', path: '/permis', icon: <ShieldCheckIcon width={20} /> },
        { text: 'Documents source (BT)', path: '/documents', icon: <DocumentTextIcon width={20} /> },
        { text: 'ATs associées', path: '/autorisations', icon: <ClipboardDocumentCheckIcon width={20} /> },
        { text: 'Notifications', path: '/notifications', icon: <BellIcon width={20} /> },
      ],
    },
  ],
};

interface MenuItemRowProps {
  text: string;
  path: string;
  icon: React.ReactNode;
}

function MenuItemRow({ text, path, icon }: MenuItemRowProps) {
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
            fontWeight: 700,
            '& .MuiListItemIcon-root': { color: PRIMARY_COLOR },
          },
          '&:hover': {
            bgcolor: 'action.hover',
          },
        }}
      >
        <ListItemIcon sx={{ minWidth: 36, color: 'inherit' }}>
          {icon}
        </ListItemIcon>
        <ListItemText
          primary={text}
          slotProps={{ primary: { style: { fontSize: 13, fontWeight: 'inherit' } } }}
        />
      </ListItemButton>
    </ListItem>
  );
}

function SectionMenuRow({ section }: { section: any }) {
  const [open, setOpen] = useState(section.defaultOpen ?? false);

  if (section.collapsible) {
    return (
      <Box sx={{ mb: 1.5 }}>
        <Box
          onClick={() => setOpen(!open)}
          sx={{
            px: 4,
            py: 0.75,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            cursor: 'pointer',
            '&:hover': { bgcolor: 'action.hover' },
          }}
        >
          <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 700, fontSize: 11, letterSpacing: 0.5 }}>
            {section.title}
          </Typography>
          {open ? <ChevronDownIcon width={14} color="#5C6E67" /> : <ChevronRightIcon width={14} color="#5C6E67" />}
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
    <Box sx={{ mb: 1.5 }}>
      {section.title && (
        <Typography variant="caption" sx={{ px: 4, py: 0.75, display: 'block', color: 'text.secondary', fontWeight: 700, fontSize: 11, letterSpacing: 0.5 }}>
          {section.title}
        </Typography>
      )}
      <List disablePadding>
        {section.items.map((item: any) => (
          <MenuItemRow key={item.text} {...item} />
        ))}
      </List>
    </Box>
  );
}

export default function Sidebar() {
  const primaryRole = usePrimaryRole();

  let menuConfig = CE_MENU;
  if (primaryRole === 'ADMIN') menuConfig = ADMIN_MENU;
  else if (primaryRole === 'HC') menuConfig = HC_MENU;
  else if (primaryRole === 'HM') menuConfig = HM_MENU;
  else if (primaryRole === 'CE') menuConfig = CE_MENU;
  else if (primaryRole === 'RESPONSABLE_EXTERIEUR') menuConfig = RESPONSABLE_EXTERIEUR_MENU;

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
        '&::-webkit-scrollbar': { width: '4px' },
        '&::-webkit-scrollbar-thumb': { backgroundColor: '#D6E3DC', borderRadius: '4px' },
      }}
    >
      {/* OCP Header */}
      <Box sx={{ p: 2.5, borderBottom: '1px solid #E3ECE7' }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
          <svg width="34" height="34" viewBox="0 0 100 100" fill="none">
            <path d="M50 10L61 40H93L67 59L77 89L50 70L23 89L33 59L7 40H39L50 10Z" fill={PRIMARY_COLOR} />
            <circle cx="50" cy="50" r="15" fill="white" />
            <circle cx="50" cy="50" r="10" fill={PRIMARY_COLOR} />
          </svg>
          <Box>
            <Typography variant="h6" sx={{ fontWeight: 900, color: PRIMARY_COLOR, letterSpacing: -0.5, lineHeight: 1 }}>
              OCP GROUP
            </Typography>
            <Typography variant="caption" sx={{ color: '#3C7A5C', fontWeight: 700, fontSize: 10, display: 'block' }}>
              GESTION AT & HSE
            </Typography>
          </Box>
        </Box>
      </Box>

      {/* Menu Navigation */}
      <Box sx={{ flexGrow: 1, py: 2 }}>
        {menuConfig.sections.map((section) => (
          <SectionMenuRow key={section.title} section={section} />
        ))}
      </Box>

      {/* Footer */}
      <Box sx={{ p: 2, borderTop: '1px solid #E3ECE7' }}>
        <Typography variant="caption" color="text.secondary" sx={{ display: 'block', fontSize: 10, textAlign: 'center' }}>
          © 2026 OCP Group — Système AT Intelligente
        </Typography>
      </Box>
    </Box>
  );
}