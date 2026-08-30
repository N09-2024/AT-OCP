import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Box,
  Typography,
  TextField,
  Button,
  Paper,
  CircularProgress,
  Checkbox,
  FormGroup,
  FormControlLabel,
  Alert,
  Divider,
  Chip,
} from '@mui/material';
import { ArrowLeftIcon } from '@heroicons/react/24/outline';
import { AdminService } from '../../../services/AdminService';
import type { PermissionResponse, RoleFormData } from '../../../services/AdminService';

const DEFAULT_FORM: RoleFormData = {
  nom: '',
  description: '',
  permissionIds: [],
};

export default function RoleFormPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const isEdit = Boolean(id) && id !== 'nouveau';

  const [form, setForm] = useState<RoleFormData>(DEFAULT_FORM);
  const [permissions, setPermissions] = useState<PermissionResponse[]>([]);
  const [loading, setLoading] = useState(false);
  const [fetchLoading, setFetchLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  useEffect(() => {
    const loadData = async () => {
      try {
        const perms = await AdminService.listPermissions();
        setPermissions(perms);
        if (isEdit) {
          const roleData = await AdminService.getRole(id!);
          setForm(roleData);
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

  const handleChange = (field: keyof Omit<RoleFormData, 'permissionIds'>) => (
    e: React.ChangeEvent<HTMLInputElement>
  ) => {
    setForm((prev) => ({ ...prev, [field]: e.target.value }));
  };

  const togglePermission = (permId: string) => {
    setForm((prev) => ({
      ...prev,
      permissionIds: prev.permissionIds.includes(permId)
        ? prev.permissionIds.filter((p) => p !== permId)
        : [...prev.permissionIds, permId],
    }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setLoading(true);

    try {
      if (isEdit) {
        await AdminService.updateRole(id!, form);
        setSuccess('Rôle mis à jour avec succès');
      } else {
        await AdminService.createRole(form);
        setSuccess('Rôle créé avec succès');
        setTimeout(() => navigate('/administration/roles'), 1500);
      }
    } catch (err: any) {
      const msg = err?.response?.data?.message || "Erreur lors de l'enregistrement";
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  const groupedPermissions = permissions.reduce(
    (acc, perm) => {
      const cat = perm.categorie || 'Autres';
      if (!acc[cat]) acc[cat] = [];
      acc[cat].push(perm);
      return acc;
    },
    {} as Record<string, PermissionResponse[]>
  );

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
          onClick={() => navigate('/administration/roles')}
          startIcon={<ArrowLeftIcon width={18} />}
          sx={{ textTransform: 'none', fontWeight: 500, color: 'text.secondary' }}
        >
          Retour aux rôles
        </Button>
        <Typography variant="h5" sx={{ fontWeight: 'bold' }} color="text.primary">
          Détails du rôle standard
        </Typography>
        <Chip
          label="Norme S-HSE-SEC-31 (Protégé)"
          size="small"
          sx={{ bgcolor: '#EDF2EE', color: '#1F4D3E', fontWeight: 600, border: '1px solid #7FC8A9' }}
        />
      </Box>

      <Alert severity="info" sx={{ mb: 3, borderRadius: 2 }}>
        Ce rôle fait partie du référentiel normatif OCP <strong>S-HSE-SEC-31</strong>. La structure des rôles et leurs permissions sont fixées par la gouvernance HSE pour garantir l'intégrité du cycle de validation et ne peuvent pas être modifiées ou supprimées.
      </Alert>

      <Box sx={{ display: 'flex', gap: 3, flexDirection: { xs: 'column', md: 'row' } }}>
        {/* Left: Role details */}
        <Paper sx={{ flex: '1 1 380px', borderRadius: 3, boxShadow: '0 1px 3px rgba(0,0,0,0.08)', p: 4 }}>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2.5 }}>
            <TextField
              label="Nom du rôle"
              value={form.nom}
              slotProps={{ input: { readOnly: true } }}
              fullWidth
              size="small"
            />
            <TextField
              label="Description"
              value={form.description || 'Rôle standard de sécurité'}
              slotProps={{ input: { readOnly: true } }}
              multiline
              rows={3}
              fullWidth
              size="small"
            />

            <Typography variant="subtitle2" color="text.secondary" sx={{ mt: 1 }}>
              {form.permissionIds.length} permission(s) attribuée(s)
            </Typography>

            <Box sx={{ display: 'flex', gap: 2, mt: 2 }}>
              <Button
                variant="outlined"
                onClick={() => navigate('/administration/roles')}
                sx={{ borderRadius: 2, textTransform: 'none', fontWeight: 600 }}
              >
                Retour à la liste
              </Button>
            </Box>
          </Box>
        </Paper>

        {/* Right: Permissions */}
        <Paper sx={{ flex: '2 1 0', borderRadius: 3, boxShadow: '0 1px 3px rgba(0,0,0,0.08)', p: 3 }}>
          <Typography variant="h6" sx={{ fontWeight: 600, mb: 2 }}>
            Permissions associées
          </Typography>
          {Object.entries(groupedPermissions).map(([categorie, perms]) => (
            <Box key={categorie} sx={{ mb: 2.5 }}>
              <Typography variant="subtitle2" sx={{ fontWeight: 600, mb: 1, color: 'text.secondary' }}>
                {categorie}
              </Typography>
              <Divider sx={{ mb: 1 }} />
              <FormGroup>
                {perms.map((perm) => (
                  <FormControlLabel
                    key={perm.id}
                    control={
                      <Checkbox
                        checked={form.permissionIds.includes(perm.id)}
                        disabled
                        size="small"
                        sx={{
                          '&.Mui-checked': { color: '#1F4D3E' },
                          '&.Mui-disabled': { color: form.permissionIds.includes(perm.id) ? '#1F4D3E' : undefined }
                        }}
                      />
                    }
                    label={
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                        <Typography variant="body2">{perm.description}</Typography>
                        <Chip
                          label={perm.code}
                          size="small"
                          variant="outlined"
                          sx={{ fontSize: 10, height: 20 }}
                        />
                      </Box>
                    }
                  />
                ))}
              </FormGroup>
            </Box>
          ))}
        </Paper>
      </Box>
    </Box>
  );
}