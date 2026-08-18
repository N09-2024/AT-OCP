import { apiClient } from './apiClient';
import type { UtilisateurResponse } from './AuthService';

// -------------------------------------------------------
// Types for Admin module
// -------------------------------------------------------
export interface AuditLogEntry {
  id: string;
  date: string;
  action: string;
  resultat: string;
  adresseIP: string;
  navigateur: string;
  utilisateur?: {
    id: string;
    nom: string;
    prenom: string;
    email: string;
  };
}

export type AuditLogEntryFlat = {
  id: string;
  dateCreation: string;
  action: string;
  entity: string;
  entityId: string | null;
  utilisateur: string;
  details: string;
};

export interface SystemSettings {
  maintenanceMode: boolean;
  sessionTimeoutMinutes: number;
  maxLoginAttempts: number;
  inscriptionOuverte: boolean;
  emailNotifications: boolean;
  retentionDays: number;
}

export interface AdminStats {
  totalUsers: number;
  activeUsers: number;
  totalRoles: number;
  pendingActions: number;
  totalPermissions: number;
}

export interface DashboardStats {
  kpis: {
    autorisationsEnCours: number;
    visasEnAttente: number;
    permisActifs: number;
    receptionsEnAttente: number;
    totalArchives: number;
  };
  monthlyStats: { mois: string; total: number }[];
  statusDistribution: Record<string, number>;
  recentAutorisations: {
    id: string;
    titre: string;
    installation: string;
    statut: string;
    echeance: string;
  }[];
}

export interface RoleResponse {
  id: string;
  nom: string;
  description?: string;
  permissionsCount?: number;
  usersCount?: number;
}

export interface PermissionResponse {
  id: string;
  code: string;
  description: string;
  categorie: string;
}

export interface RoleFormData {
  nom: string;
  description: string;
  permissionIds: string[];
}

export interface Zone {
  id: string;
  nomZone: string;
  descriptionZone?: string;
  codeZone: string;
}

export interface Service {
  id: string;
  nomService: string;
  descriptionService?: string;
  codeService: string;
  zone?: Zone;
}

export interface Installation {
  id: string;
  nomInstallation: string;
  codeInstallation: string;
  atelier?: string;
  localisation?: string;
  service?: Service;
}

// -------------------------------------------------------
// User-related API calls
// -------------------------------------------------------
const mapUserFromApi = (u: any): any => ({
  id: u.id,
  email: u.email,
  prenom: u.prenom,
  nom: u.nom,
  service: u.service,
  roles: u.roles || [],
  actived: u.actif,
  derniereConnexion: u.derniereConnexion,
});

export const AdminService = {
  // --- Users ---
  listUsers: async (search?: string, page = 0, size = 20) => {
    const params = new URLSearchParams();
    if (search) params.set('search', search);
    params.set('page', String(page));
    params.set('size', String(size));
    const res = await apiClient.get(`/users?${params}`);
    return {
      ...res.data,
      content: res.data.content.map(mapUserFromApi),
    };
  },

  getUser: async (id: string) => {
    const res = await apiClient.get<UtilisateurResponse>(`/users/${id}`);
    return mapUserFromApi(res.data);
  },

  createUser: async (data: any) => {
    const res = await apiClient.post('/users', {
      email: data.email,
      motDePasse: data.motDePasse,
      prenom: data.prenom,
      nom: data.nom,
      serviceId: data.serviceId || null,
      roleNom: data.roleNom || null,
    });
    return res.data;
  },

  updateUser: async (id: string, data: any) => {
    const payload: any = {
      email: data.email,
      prenom: data.prenom,
      nom: data.nom,
      serviceId: data.serviceId || null,
    };
    if (data.motDePasse) {
      payload.motDePasse = data.motDePasse;
    }
    const res = await apiClient.put(`/users/${id}`, payload);
    return res.data;
  },

  deleteUser: async (id: string) => {
    await apiClient.delete(`/users/${id}`);
  },

  activateUser: async (id: string) => {
    const res = await apiClient.patch(`/users/${id}/activate`);
    return mapUserFromApi(res.data);
  },

  deactivateUser: async (id: string) => {
    const res = await apiClient.patch(`/users/${id}/deactivate`);
    return mapUserFromApi(res.data);
  },

  unlockUser: async (id: string) => {
    const res = await apiClient.patch(`/users/${id}/unlock`);
    return res.data;
  },

  assignRole: async (userId: string, roleId: string) => {
    const res = await apiClient.post(`/users/${userId}/roles`, { roleId });
    return res.data;
  },

  removeRole: async (userId: string, roleId: string) => {
    const res = await apiClient.delete(`/users/${userId}/roles/${roleId}`);
    return res.data;
  },

  // --- Roles ---
  listRoles: async (search?: string) => {
    const params = new URLSearchParams();
    if (search) params.set('search', search);
    const res = await apiClient.get(`/roles?${params}`);
    return {
      ...res.data,
      content: res.data.content.map((r: any) => ({
        id: r.id,
        nom: r.nom,
        description: r.description,
        permissionsCount: r.permissions?.length || 0,
        usersCount: r.usersCount || 0,
      })),
    };
  },

  getRole: async (id: string) => {
    const res = await apiClient.get<RoleResponse>(`/roles/${id}`);
    const role = res.data;
    // Get permissions for this role
    const permRes = await apiClient.get<PermissionResponse[]>(`/roles/${id}/permissions`);
    return {
      nom: role.nom,
      description: role.description || '',
      permissionIds: permRes.data.map((p) => p.id),
    };
  },

  createRole: async (data: RoleFormData) => {
    const res = await apiClient.post('/roles', {
      nom: data.nom,
      description: data.description,
    });
    // Assign permissions
    for (const permId of data.permissionIds) {
      await apiClient.post(`/roles/${res.data.id}/permissions`, { permissionId: permId });
    }
    return res.data;
  },

  updateRole: async (id: string, data: RoleFormData) => {
    const res = await apiClient.put(`/roles/${id}`, {
      nom: data.nom,
      description: data.description,
    });
    // Get current permissions
    const currentPerms = await apiClient.get<PermissionResponse[]>(`/roles/${id}/permissions`);
    const currentIds = currentPerms.data.map((p) => p.id);
    // Remove removed permissions
    for (const pid of currentIds) {
      if (!data.permissionIds.includes(pid)) {
        await apiClient.delete(`/roles/${id}/permissions/${pid}`);
      }
    }
    // Add new permissions
    for (const pid of data.permissionIds) {
      if (!currentIds.includes(pid)) {
        await apiClient.post(`/roles/${id}/permissions`, { permissionId: pid });
      }
    }
    return res.data;
  },

  deleteRole: async (id: string) => {
    await apiClient.delete(`/roles/${id}`);
  },

  // --- Pending Users / Registration Approval ---
  listPendingUsers: async () => {
    const res = await apiClient.get('/users/pending');
    return res.data;
  },

  approveUser: async (id: string) => {
    const res = await apiClient.patch(`/users/${id}/approve`);
    return res.data;
  },

  rejectUser: async (id: string) => {
    await apiClient.delete(`/users/${id}/reject`);
  },

  // --- Permissions ---
  listPermissions: async () => {
    const res = await apiClient.get('/permissions?size=200');
    return res.data.content;
  },

  // --- Dashboard ---
  getAdminStats: async (): Promise<AdminStats> => {
    // Pulls real counts from the endpoints that actually back this data,
    // instead of relabeling the unrelated AT-workflow KPIs from /dashboard/stats.
    const [usersRes, rolesRes, pendingRes, permissionsRes] = await Promise.all([
      apiClient.get('/users?size=1000'),
      apiClient.get('/roles?size=200'),
      apiClient.get('/users/pending'),
      apiClient.get('/permissions?size=200'),
    ]);

    const users: any[] = usersRes.data.content ?? [];

    return {
      totalUsers: usersRes.data.totalElements ?? users.length,
      activeUsers: users.filter((u) => u.actif).length,
      totalRoles: rolesRes.data.totalElements ?? rolesRes.data.content?.length ?? 0,
      pendingActions: Array.isArray(pendingRes.data) ? pendingRes.data.length : 0,
      totalPermissions: permissionsRes.data.totalElements ?? permissionsRes.data.content?.length ?? 0,
    };
  },

  getDashboardStats: async (): Promise<DashboardStats> => {
    const res = await apiClient.get('/dashboard/stats');
    return res.data;
  },

  // --- Audit Logs ---
  listAuditLogs: async () => {
    try {
      const res = await apiClient.get('/audit-logs?size=1000&sort=date,desc');
      const items = Array.isArray(res.data) ? res.data : (res.data?.content || []);
      return items.map((entry: any): AuditLogEntryFlat => {
        let userName = 'Système';
        if (entry.utilisateur) {
          const fullName = `${entry.utilisateur.prenom || ''} ${entry.utilisateur.nom || ''}`.trim();
          userName = fullName || entry.utilisateur.email || entry.utilisateur.matricule || 'Utilisateur';
        }
        return {
          id: entry.id,
          dateCreation: entry.date,
          action: entry.action,
          entity: entry.resultat || 'SUCCES',
          entityId: entry.id,
          utilisateur: userName,
          details: `${entry.action} depuis ${entry.adresseIP || '127.0.0.1'} (${entry.systemeExploitation || 'Navigateur'})`,
        };
      });
    } catch {
      return [];
    }
  },

  // --- Settings ---
  getSettings: async (): Promise<SystemSettings> => {
    try {
      const res = await apiClient.get('/settings');
      return res.data;
    } catch {
      return {
        maintenanceMode: false,
        sessionTimeoutMinutes: 60,
        maxLoginAttempts: 5,
        inscriptionOuverte: false,
        emailNotifications: true,
        retentionDays: 365,
      };
    }
  },

  updateSettings: async (settings: SystemSettings): Promise<void> => {
    await apiClient.put('/settings', settings);
  },

  // --- Zones (Referentiels) ---
  listZones: async (_search?: string): Promise<Zone[]> => {
    const res = await apiClient.get('/zones');
    // Backend GET /zones returns a List<ZoneResponse> directly (no pagination)
    const raw: any[] = Array.isArray(res.data) ? res.data : (res.data.content ?? []);
    return raw.map((z: any) => ({
      id: z.id,
      nomZone: z.nomZone,
      descriptionZone: z.descriptionZone,
      codeZone: z.codeZone,
    }));
  },

  getZone: async (id: string): Promise<Zone> => {
    const res = await apiClient.get<Zone>(`/zones/${id}`);
    return res.data;
  },

  createZone: async (data: { nomZone: string; codeZone: string; descriptionZone?: string }): Promise<Zone> => {
    const res = await apiClient.post('/zones', {
      nomZone: data.nomZone,
      codeZone: data.codeZone,
      descriptionZone: data.descriptionZone,
    });
    return res.data;
  },

  updateZone: async (id: string, data: { nomZone?: string; codeZone?: string; descriptionZone?: string }): Promise<Zone> => {
    const res = await apiClient.put(`/zones/${id}`, {
      nomZone: data.nomZone,
      codeZone: data.codeZone,
      descriptionZone: data.descriptionZone,
    });
    return res.data;
  },

  deleteZone: async (id: string): Promise<void> => {
    await apiClient.delete(`/zones/${id}`);
  },

  // --- Services (Referentiels) ---
  listServices: async (): Promise<Service[]> => {
    const res = await apiClient.get('/services?size=500');
    const raw: any[] = Array.isArray(res.data) ? res.data : (res.data.content ?? []);
    return raw.map((s: any) => ({
      id: s.id,
      nomService: s.nomService,
      descriptionService: s.descriptionService,
      codeService: s.codeService,
      zone: s.zone,
    }));
  },

  // --- Installations (Referentiels) ---
  listInstallations: async (search?: string): Promise<{ content: Installation[]; totalElements: number; totalPages: number; number: number }> => {
    const params = new URLSearchParams();
    if (search) params.set('search', search);
    const res = await apiClient.get(`/installations?${params}`);
    const raw: any[] = Array.isArray(res.data) ? res.data : (res.data.content ?? []);
    return {
      content: raw.map((i: any) => ({
        id: i.id,
        nomInstallation: i.nomInstallation,
        codeInstallation: i.codeInstallation,
        atelier: i.atelier,
        localisation: i.localisation,
        service: i.service,
      })),
      totalElements: res.data.totalElements ?? raw.length,
      totalPages: res.data.totalPages ?? 1,
      number: res.data.number ?? 0,
    };
  },

  getInstallation: async (id: string): Promise<Installation> => {
    const res = await apiClient.get<Installation>(`/installations/${id}`);
    return res.data;
  },

  createInstallation: async (data: { nomInstallation: string; codeInstallation: string; atelier?: string; localisation?: string; serviceId?: string }): Promise<Installation> => {
    const res = await apiClient.post('/installations', {
      nomInstallation: data.nomInstallation,
      codeInstallation: data.codeInstallation,
      atelier: data.atelier,
      localisation: data.localisation,
      serviceId: data.serviceId,
    });
    return res.data;
  },

  updateInstallation: async (id: string, data: { nomInstallation?: string; codeInstallation?: string; atelier?: string; localisation?: string; serviceId?: string }): Promise<Installation> => {
    const res = await apiClient.put(`/installations/${id}`, {
      nomInstallation: data.nomInstallation,
      codeInstallation: data.codeInstallation,
      atelier: data.atelier,
      localisation: data.localisation,
      serviceId: data.serviceId,
    });
    return res.data;
  },

  deleteInstallation: async (id: string): Promise<void> => {
    await apiClient.delete(`/installations/${id}`);
  },
};