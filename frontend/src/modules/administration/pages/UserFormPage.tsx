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
  Divider,
} from '@mui/material';
import { ArrowLeftIcon, InformationCircleIcon } from '@heroicons/react/24/outline';
import { AdminService } from '../../../services/AdminService';
import type { Service } from '../../../services/AdminService';

// -------------------------------------------------------
// Rôles standard OCP S-HSE-SEC-31 — l'admin choisit le niveau d'habilitation
// -------------------------------------------------------
const FIXED_ROLES = [
  {
    nom: 'CE',
    label: "Chef d'Équipe (CE)",
    description: "Chef d'Équipe terrain — position CEEP (Propriétaire) ou CEEE (Exécutant) résolue dynamiquement par le service",
    color: '#0891b2',
  },
  {
    nom: 'HM',
    label: "Haute Maîtrise (HM)",
    description: "Superviseur / Agent de Maîtrise — position HMEP (Propriétaire) ou HMEE (Exécutant - lecture seule)",
    color: '#16a34a',
  },
  {
    nom: 'HC',
    label: "Hors Cadre (HC)",
    description: "Cadre Responsable — position HCEP (Classification/Archivage) ou HCEE (Garant/Validation)",
    color: '#7c3aed',
  },
  {
    nom: 'RESPONSABLE_EXTERIEUR',
    label: "Responsable Entreprise Extérieure",
    description: "Responsable d'entreprise sous-traitante (Bons de Travail et permis uniquement)",
    color: '#6A1B9A',
  },
  {
    nom: 'ADMIN',
    label: "Administrateur Système",
    description: "Administrateur système avec tous les droits de gestion et supervision",
    color: '#B71C1C',
  },
];

interface UserFormData {
  email: string;
  prenom: string;
  nom: string;
  motDePasse: string;
  roleNom: string;
  serviceId: string;
  actived: boolean;
}

const DEFAULT_FORM: UserFormData = {
  email: '',
  prenom: '',
  nom: '',
  motDePasse: '',
  roleNom: '',
  serviceId: '',
  actived: true,
};

export default function UserFormPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const isEdit = Boolean(id) && id !== 'nouveau';

  const [form, setForm] = useState<UserFormData>(DEFAULT_FORM);
  const [resolvedRoleId, setResolvedRoleId] = useState<string>('');
  const [originalActived, setOriginalActived] = useState<boolean>(true);
  const [allRoles, setAllRoles] = useState<{ id: string; nom: string }[]>([]);
  const [servicesList, setServicesList] = useState<Service[]>([]);

  const [loading, setLoading] = useState(false);
  const [fetchLoading, setFetchLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  useEffect(() => {
    const loadData = async () => {
      try {
        const [rolesRes, servicesRes] = await Promise.all([
          AdminService.listRoles(),
          AdminService.listServices(),
        ]);
        setAllRoles(rolesRes.content);
        setServicesList(servicesRes);

        if (isEdit) {
          try {
            const data = await AdminService.getUser(id!);
            const firstRole = data.roles?.[0];
            const firstRoleNom = firstRole?.nom ?? '';
            setForm({
              email: data.email,
              prenom: data.prenom,
              nom: data.nom,
              motDePasse: '',
              roleNom: firstRoleNom,
              serviceId: data.service?.id ?? '',
              actived: data.actived,
            });
            setResolvedRoleId(firstRole?.id ?? '');
            setOriginalActived(data.actived);
          } catch (userErr: any) {
            if (userErr?.response?.status === 404) {
              // L'utilisateur n'existe plus (ex: DB réinitialisée) → redirection propre
              navigate('/administration/utilisateurs', {
                state: { error: "L'utilisateur demandé est introuvable. Il a peut-être été supprimé." },
              });
              return;
            }
            throw userErr;
          }
        }
      } catch (err) {
        console.error('Erreur chargement données', err);
        setError('Impossible de charger les données (rôles/services). Vérifiez la connexion au serveur.');
      } finally {
        setFetchLoading(false);
      }
    };
    loadData();
  }, [id, isEdit, navigate]);

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

    const serviceExempte = form.roleNom === 'ADMIN';
    if (!serviceExempte && !form.serviceId) {
      setError(
        "Le service d'appartenance est obligatoire pour ce rôle. " +
        "Sans service, le système ne pourra pas déterminer la position Propriétaire/Exécutant de cet utilisateur sur les AT."
      );
      return;
    }

    setLoading(true);

    try {
      if (isEdit) {
        await AdminService.updateUser(id!, form);

        const currentData = await AdminService.getUser(id!);
        const currentRoleIds: string[] = currentData.roles?.map((r: any) => r.id) || [];

        for (const rid of currentRoleIds) {
          await AdminService.removeRole(id!, rid);
        }
        if (resolvedRoleId) {
          await AdminService.assignRole(id!, resolvedRoleId);
        }

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

      {/* Note d'information Standard S-HSE-SEC-31 */}
      <Alert severity="info" icon={<InformationCircleIcon width={24} />} sx={{ mb: 3, borderRadius: 2 }}>
        <Typography variant="subtitle2" sx={{ fontWeight: 600 }}>
          Règle de résolution contextuelle (Standard S-HSE-SEC-31)
        </Typography>
        <Typography variant="caption">
          La position <strong>Propriétaire (P)</strong> ou <strong>Exécutant (E)</strong> est calculée dynamiquement par le système pour chaque Autorisation de Travail en fonction du <strong>Service d'appartenance</strong> sélectionné ci-dessous.
        </Typography>
      </Alert>

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

          <Divider sx={{ my: 1 }} />

          {/* Service d'appartenance (Fondamental pour P / E) */}
          <FormControl
            fullWidth
            size="small"
            required={form.roleNom !== 'ADMIN'}
            error={!form.serviceId && !!form.roleNom && form.roleNom !== 'ADMIN'}
          >
            <InputLabel id="service-label">
              Service d'appartenance (Zone OCP)
            </InputLabel>
            <Select
              labelId="service-label"
              value={form.serviceId}
              label="Service d'appartenance (Zone OCP)"
              onChange={(e) => setForm((prev) => ({ ...prev, serviceId: e.target.value as string }))}
            >
              {form.roleNom === 'ADMIN' && (
                <MenuItem value="">
                  <em>Aucun (Administrateur système)</em>
                </MenuItem>
              )}
              {servicesList.map((svc) => (
                <MenuItem key={svc.id} value={svc.id}>
                  {svc.nomService} ({svc.codeService}) {svc.zone ? `— Zone: ${svc.zone.nomZone}` : ''}
                </MenuItem>
              ))}
            </Select>
            <FormHelperText
              error={!form.serviceId && !!form.roleNom && form.roleNom !== 'ADMIN'}
            >
              {form.roleNom === 'ADMIN'
                ? "L'administrateur système n'est pas rattaché à un service opérationnel."
                : !form.serviceId && form.roleNom
                ? "⚠ Obligatoire — sans service, la position P/E ne pourra pas être résolue sur les AT."
                : "Le service détermine la zone de rattachement pour la résolution dynamique P / E lors des AT."}
            </FormHelperText>
          </FormControl>

          {/* Rôle — Fonction RH / Niveau d'habilitation */}
          <FormControl fullWidth size="small" required>
            <InputLabel id="role-label">Habilitation / Rôle de l'utilisateur</InputLabel>
            <Select
              labelId="role-label"
              value={form.roleNom}
              label="Habilitation / Rôle de l'utilisateur"
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
