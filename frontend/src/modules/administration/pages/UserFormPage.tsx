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
  Chip,
  Select,
  InputLabel,
  FormControl,
  OutlinedInput,
} from '@mui/material';
import { ArrowLeftIcon } from '@heroicons/react/24/outline';
import { AdminService } from '../../../services/AdminService';

interface UserFormData {
  email: string;
  prenom: string;
  nom: string;
  motDePasse: string;
  roleIds: string[];
  actived: boolean;
}

interface RoleOption {
  id: string;
  nom: string;
  description: string;
}

const DEFAULT_FORM: UserFormData = {
  email: '',
  prenom: '',
  nom: '',
  motDePasse: '',
  roleIds: [],
  actived: true,
};

export default function UserFormPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const isEdit = Boolean(id) && id !== 'nouveau';

  const [form, setForm] = useState<UserFormData>(DEFAULT_FORM);
  const [roles, setRoles] = useState<RoleOption[]>([]);
  const [loading, setLoading] = useState(false);
  const [fetchLoading, setFetchLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  useEffect(() => {
    const loadData = async () => {
      try {
        // Load roles from backend
        const rolesRes = await AdminService.listRoles();
        setRoles(rolesRes.content);

        if (isEdit) {
          const data = await AdminService.getUser(id!);
          setForm({
            email: data.email,
            prenom: data.prenom,
            nom: data.nom,
            motDePasse: '',
            roleIds: data.roles?.map((r: any) => r.id) || [],
            actived: data.actived,
          });
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

  const handleChange = (field: keyof UserFormData) => (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = field === 'actived' ? e.target.checked : e.target.value;
    setForm((prev) => ({ ...prev, [field]: value }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setLoading(true);

    try {
      if (isEdit) {
        await AdminService.updateUser(id!, form);
        // Sync roles: assign new, remove old
        const currentRoles = await AdminService.getUser(id!);
        const currentRoleIds: string[] = currentRoles.roles?.map((r: any) => r.id) || [];
        
        // Remove roles that were deselected
        for (const rid of currentRoleIds) {
          if (!form.roleIds.includes(rid)) {
            await AdminService.removeRole(id!, rid);
          }
        }
        // Assign new roles
        for (const rid of form.roleIds) {
          if (!currentRoleIds.includes(rid)) {
            await AdminService.assignRole(id!, rid);
          }
        }
        setSuccess('Utilisateur mis à jour avec succès');
      } else {
        const newUser = await AdminService.createUser(form);
        for (const rid of form.roleIds) {
          if (newUser?.id) {
            await AdminService.assignRole(newUser.id, rid);
          }
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

  return (
    <Box>
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

      <Paper sx={{ borderRadius: 3, boxShadow: '0 1px 3px rgba(0,0,0,0.08)', p: 4, maxWidth: 640 }}>
        <Box component="form" onSubmit={handleSubmit} sx={{ display: 'flex', flexDirection: 'column', gap: 2.5 }}>
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
          <TextField
            label="Email"
            type="email"
            value={form.email}
            onChange={handleChange('email')}
            required
            fullWidth
            size="small"
          />
          <TextField
            label={isEdit ? 'Nouveau mot de passe (laisser vide pour conserver)' : 'Mot de passe'}
            type="password"
            value={form.motDePasse}
            onChange={handleChange('motDePasse')}
            required={!isEdit}
            fullWidth
            size="small"
          />
          <FormControl fullWidth size="small">
            <InputLabel id="roles-label">Rôles</InputLabel>
            <Select
              labelId="roles-label"
              multiple
              value={form.roleIds}
              onChange={(e) => {
                const value = e.target.value;
                setForm((prev) => ({
                  ...prev,
                  roleIds: typeof value === 'string' ? value.split(',') : value,
                }));
              }}
              input={<OutlinedInput label="Rôles" />}
              renderValue={(selected) => (
                <Box sx={{ display: 'flex', gap: 0.5, flexWrap: 'wrap' }}>
                  {(selected as string[]).map((rid) => {
                    const role = roles.find((r) => r.id === rid);
                    return role ? (
                      <Chip key={rid} label={role.nom} size="small" />
                    ) : null;
                  })}
                </Box>
              )}
            >
              {roles.map((role) => (
                <MenuItem key={role.id} value={role.id}>
                  {role.nom} {role.description ? `- ${role.description}` : ''}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
          <FormControlLabel
            control={<Switch checked={form.actived} onChange={handleChange('actived')} />}
            label="Compte actif"
          />
          <Box sx={{ display: 'flex', gap: 2, mt: 1 }}>
            <Button
              type="submit"
              variant="contained"
              color="success"
              disabled={loading}
              sx={{ borderRadius: 2, textTransform: 'none', fontWeight: 600 }}
            >
              {loading ? <CircularProgress size={20} color="inherit" /> : isEdit ? 'Enregistrer' : 'Créer'}
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