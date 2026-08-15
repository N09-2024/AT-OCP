import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Box,
  Typography,
  TextField,
  Button,
  Paper,
  CircularProgress,
  Alert,
} from '@mui/material';
import { ArrowLeftIcon } from '@heroicons/react/24/outline';
import { apiClient } from '../../../services/apiClient';

// FieldDef describes a single input field in the form
export interface FieldDef {
  key: string;          // the JSON key sent to the backend
  label: string;        // displayed label
  required?: boolean;
  multiline?: boolean;
  rows?: number;
  readKey?: string;     // optional: use a different key when reading (GET) — defaults to `key`
}

interface SimpleReferentielFormProps {
  title: string;
  apiPath: string;       // e.g. '/epis'
  routeBase: string;     // e.g. '/administration/epis'
  fields: FieldDef[];    // the list of fields to render and submit
}

export default function SimpleReferentielForm({
  title,
  apiPath,
  routeBase,
  fields,
}: SimpleReferentielFormProps) {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const isEdit = Boolean(id) && id !== 'nouveau';

  // Generic state: one value per field key
  const [values, setValues] = useState<Record<string, string>>(() =>
    Object.fromEntries(fields.map((f) => [f.key, '']))
  );
  const [loading, setLoading] = useState(false);
  const [fetchLoading, setFetchLoading] = useState(isEdit);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  useEffect(() => {
    if (!isEdit) return;
    const loadItem = async () => {
      try {
        const res = await apiClient.get(`${apiPath}/${id}`);
        const data = res.data;
        // Populate each field from the response
        setValues(
          Object.fromEntries(
            fields.map((f) => {
              const readKey = f.readKey ?? f.key;
              return [f.key, data[readKey] ?? ''];
            })
          )
        );
      } catch {
        setError('Impossible de charger les données');
      } finally {
        setFetchLoading(false);
      }
    };
    loadItem();
  }, [id, isEdit, apiPath]);

  const handleChange = (key: string) => (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    setValues((prev) => ({ ...prev, [key]: e.target.value }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    // Validate required fields
    for (const f of fields) {
      if (f.required && !values[f.key]?.trim()) {
        setError(`Le champ "${f.label}" est obligatoire`);
        return;
      }
    }

    setError('');
    setSuccess('');
    setLoading(true);

    // Build payload: only include non-empty strings
    const payload: Record<string, any> = {};
    for (const f of fields) {
      if (values[f.key] !== undefined) {
        payload[f.key] = values[f.key].trim();
      }
    }

    try {
      if (isEdit) {
        await apiClient.put(`${apiPath}/${id}`, payload);
        setSuccess('Modifié avec succès');
      } else {
        await apiClient.post(apiPath, payload);
        setSuccess('Créé avec succès');
        setTimeout(() => navigate(routeBase), 1200);
      }
    } catch (err: any) {
      setError(err?.response?.data?.message || "Erreur lors de l'enregistrement");
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
    <Box sx={{ maxWidth: 1200, mx: 'auto', width: '100%' }}>
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 3 }}>
        <Button
          onClick={() => navigate(routeBase)}
          startIcon={<ArrowLeftIcon width={18} />}
          sx={{ textTransform: 'none', fontWeight: 500, color: 'text.secondary' }}
        >
          Retour
        </Button>
        <Typography variant="h5" sx={{ fontWeight: 'bold' }} color="text.primary">
          {isEdit ? `Modifier — ${title}` : `Nouveau — ${title}`}
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

      <Paper elevation={0} sx={{ borderRadius: 2, border: '1px solid', borderColor: 'divider', p: { xs: 3, md: 5 } }}>
        <Box component="form" onSubmit={handleSubmit} sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
          {fields.map((f, idx) => (
            <TextField
              key={f.key}
              label={f.label}
              value={values[f.key] ?? ''}
              onChange={handleChange(f.key)}
              required={f.required}
              fullWidth
              autoFocus={idx === 0}
              multiline={f.multiline}
              rows={f.multiline ? (f.rows ?? 3) : undefined}
            />
          ))}
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
              onClick={() => navigate(routeBase)}
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
