import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Box,
  Typography,
  TextField,
  Button,
  Paper,
  CircularProgress,
  MenuItem,
  FormControlLabel,
  Switch,
  Alert,
  Select,
  InputLabel,
  FormControl,
  FormHelperText,
} from '@mui/material';
import { ArrowLeftIcon } from '@heroicons/react/24/outline';
import { AdminService } from '../../../services/AdminService';

// -------------------------------------------------------
// Rôles fixes du système — l'admin choisit parmi ces 4 rôles
// -------------------------------------------------------
const FIXED_ROLES = [
  {
    nom: 'RESPONSABLE_OCP',
    label: 'Responsable OCP',
    description: 'Responsable OCP validateur des autorisations de travail',
    color: '#1565C0',
  },
  {
    nom: 'RESPONSABLE_ENTREPRISE',
    label: 'Responsable Externe',
    description: "Responsable d'une entreprise externe (sous-traitant)",
    color: '#6A1B9A',
  },
  {
    nom: 'DEMANDEUR',
    label: 'Demandeur',
    description: "Demandeur d'autorisation de travail",
    color: '#2E7D32',
  },
  {
    nom: 'ADMIN',
    label: 'Administrateur',
    description: 'Administrateur système avec tous les droits',
    color: '#B71C1C',
  },
];

interface UserFormData {
  email: string;
  prenom: string;
  nom: string;
  motDePasse: string;
  roleNom: string; // single role selection by name
  actived: boolean;
}

const DEFAULT_FORM: UserFormData = {
  email: '',
  prenom: '',
  nom: '',
  motDePasse: '',
  roleNom: '',
  actived: true,
};

export default function UserFormPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const isEdit = Boolean(id) && id !== 'nouveau';

  const [form, setForm] = useState<UserFormData>(DEFAULT_FORM);
  // roleId resolved from the backend roles list
  const [resolvedRoleId, setResolvedRoleId] = useState<string>('');
  const [originalActived, setOriginalActived] = useState<boolean>(true);
  const [allRoles, setAllRoles] = useState<{ id: string; nom: string }[]>([]);

  const [loading, setLoading] = useState(false);
  const [fetchLoading, setFetchLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  useEffect(() => {
    const loadData = async () => {
      try {
        // Load all roles from backend to get their IDs
        const rolesRes = await AdminService.listRoles();
        setAllRoles(rolesRes.content);

        if (isEdit) {
          const data = await AdminService.getUser(id!);
          // Get the first role the user has
          const firstRole = data.roles?.[0];
          const firstRoleNom = firstRole?.nom ?? '';
          setForm({
            email: data.email,
            prenom: data.prenom,
            nom: data.nom,
            motDePasse: '',
            roleNom: firstRoleNom,
            actived: data.actived,
          });
          setResolvedRoleId(firstRole?.id ?? '');
          setOriginalActived(data.actived);
        }
      } catch (err) {
        console.error('Erreur chargement données', err);
        setError('Impossible de charger les données');
      } finally {
        setFetchLoading(false);
      }
    };
    loadData();
  }, [id, isEdit]);

  // When the admin picks a role name, resolve its ID from the backend list
  const handleRoleChange = (selectedNom: string) => {
    setForm((prev) => ({ ...prev, roleNom: selectedNom }));
    const found = allRoles.find((r) => r.nom === selectedNom);
    setResolvedRoleId(found?.id ?? '');
  };

  const handleChange =
    (field: keyof UserFormData) => (e: React.ChangeEvent<HTMLInputElement>) => {
      const value = field === 'actived' ? e.target.checked : e.target.value;
      setForm((prev) => ({ ...prev, [field]: value }));
    };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    if (!form.roleNom) {
      setError('Veuillez sélectionner un rôle pour cet utilisateur.');
      return;
    }

    setLoading(true);

    try {
      if (isEdit) {
        await AdminService.updateUser(id!, form);

        // Sync role: remove all old roles, assign the new one
        const currentData = await AdminService.getUser(id!);
        const currentRoleIds: string[] = currentData.roles?.map((r: any) => r.id) || [];

        for (const rid of currentRoleIds) {
          await AdminService.removeRole(id!, rid);
        }
        if (resolvedRoleId) {
          await AdminService.assignRole(id!, resolvedRoleId);
        }

        // PUT /users/{id} ignores the actif field on the backend — the
        // active/inactive state has to go through its own endpoints.
        if (form.actived !== originalActived) {
          if (form.actived) {
            await AdminService.activateUser(id!);
          } else {
            await AdminService.deactivateUser(id!);
          }
          setOriginalActived(form.actived);
        }

        setSuccess('Utilisateur mis à jour avec succès');
      } else {
        const newUser = await AdminService.createUser(form);
        if (newUser?.id && resolvedRoleId) {
          await AdminService.assignRole(newUser.id, resolvedRoleId);
        }
        setSuccess('Utilisateur créé avec succès');
        setTimeout(() => navigate('/administration/utilisateurs'), 1500);
      }
    } catch (err: any) {
      const msg = err?.response?.data?.message || "Erreur lors de l'enregistrement";
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  if (fetchLoading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: 300 }}>
        <CircularProgress color="success" />
      </Box>
    );
  }

  const selectedRoleMeta = FIXED_ROLES.find((r) => r.nom === form.roleNom);

  return (
    <Box sx={{ maxWidth: 1200, mx: 'auto', width: '100%', p: 3 }}>
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 3 }}>
        <Button
          onClick={() => navigate('/administration/utilisateurs')}
          startIcon={<ArrowLeftIcon width={18} />}
          sx={{ textTransform: 'none', fontWeight: 500, color: 'text.secondary' }}
        >
          Retour
        </Button>
        <Typography variant="h5" sx={{ fontWeight: 'bold' }} color="text.primary">
          {isEdit ? "Modifier l'utilisateur" : 'Nouvel utilisateur'}
        </Typography>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 3, borderRadius: 2 }}>
          {error}
        </Alert>
      )}
      {success && (
        <Alert severity="success" sx={{ mb: 3, borderRadius: 2 }}>
          {success}
        </Alert>
      )}

      <Paper sx={{ borderRadius: 3, boxShadow: '0 1px 3px rgba(0,0,0,0.08)', p: 4, maxWidth: 1200 }}>
        <Box
          component="form"
          onSubmit={handleSubmit}
          sx={{ display: 'flex', flexDirection: 'column', gap: 2.5 }}
        >
          {/* Nom & Prénom */}
          <Box sx={{ display: 'flex', gap: 2 }}>
            <TextField
              label="Prénom"
              value={form.prenom}
              onChange={handleChange('prenom')}
              required
              fullWidth
              size="small"
            />
            <TextField
              label="Nom"
              value={form.nom}
              onChange={handleChange('nom')}
              required
              fullWidth
              size="small"
            />
          </Box>

          {/* Email */}
          <TextField
            label="Email"
            type="email"
            value={form.email}
            onChange={handleChange('email')}
            required
            fullWidth
            size="small"
          />

          {/* Mot de passe */}
          <TextField
            label={
              isEdit
                ? 'Nouveau mot de passe (laisser vide pour conserver)'
                : 'Mot de passe'
            }
            type="password"
            value={form.motDePasse}
            onChange={handleChange('motDePasse')}
            required={!isEdit}
            fullWidth
            size="small"
          />

          {/* Rôle — choix unique parmi les 4 rôles système */}
          <FormControl fullWidth size="small" required>
            <InputLabel id="role-label">Rôle de l'utilisateur</InputLabel>
            <Select
              labelId="role-label"
              value={form.roleNom}
              label="Rôle de l'utilisateur"
              onChange={(e) => handleRoleChange(e.target.value as string)}
            >
              {FIXED_ROLES.map((role) => (
                <MenuItem key={role.nom} value={role.nom}>
                  <Box>
                    <Typography
                      variant="body2"
                      sx={{ fontWeight: 600, color: role.color }}
                    >
                      {role.label}
                    </Typography>
                    <Typography variant="caption" color="text.secondary">
                      {role.description}
                    </Typography>
                  </Box>
                </MenuItem>
              ))}
            </Select>
            {selectedRoleMeta && (
              <FormHelperText sx={{ color: selectedRoleMeta.color, fontWeight: 500 }}>
                ✓ {selectedRoleMeta.label} — {selectedRoleMeta.description}
              </FormHelperText>
            )}
          </FormControl>

          {/* Compte actif */}
          <FormControlLabel
            control={
              <Switch checked={form.actived} onChange={handleChange('actived')} />
            }
            label="Compte actif"
          />

          {/* Actions */}
          <Box sx={{ display: 'flex', gap: 2, mt: 1 }}>
            <Button
              type="submit"
              variant="contained"
              color="success"
              disabled={loading}
              sx={{ borderRadius: 2, textTransform: 'none', fontWeight: 600 }}
            >
              {loading ? (
                <CircularProgress size={20} color="inherit" />
              ) : isEdit ? (
                'Enregistrer'
              ) : (
                'Créer'
              )}
            </Button>
            <Button
              variant="outlined"
              onClick={() => navigate('/administration/utilisateurs')}
              sx={{ borderRadius: 2, textTransform: 'none', fontWeight: 500 }}
            >
              Annuler
            </Button>
          </Box>
        </Box>
      </Paper>
    </Box>
  );
}
