import { useState, useEffect } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import {
  Box,
  Typography,
  Paper,
  Grid,
  TextField,
  FormControlLabel,
  Checkbox,
  Button,
  Alert,
  CircularProgress,
  Divider,
  Chip,
} from '@mui/material';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import VerifiedIcon from '@mui/icons-material/Verified';
import SignaturePad from '../../../components/common/SignaturePad';
import { receptionApi, type ReceptionTravauxRequest } from '../../../services/receptionApi';
import { autorisationTravailApi } from '../../../services/autorisationTravailApi';
import type { AutorisationTravail, ReceptionTravaux } from '../../../types';

export default function ReceptionTravauxPage() {
  const [searchParams] = useSearchParams();
  const atId = searchParams.get('atId');
  const navigate = useNavigate();

  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [at, setAt] = useState<AutorisationTravail | null>(null);
  const [existingReception, setExistingReception] = useState<ReceptionTravaux | null>(null);

  // Form State
  const todayStr = new Date().toISOString().split('T')[0];
  const formatDateInput = (val?: string) => {
    if (!val) return todayStr;
    if (val.includes('T')) return val.split('T')[0];
    return val.slice(0, 10);
  };

  const [form, setForm] = useState<ReceptionTravauxRequest>({
    autorisationTravailId: atId || '',
    dateDebutTravauxReelle: todayStr,
    dateFinTravauxReelle: todayStr,
    travauxConformes: true,
    zoneNettoyee: true,
    consignationRetiree: true,
    equipementRemisEnService: true,
    installationRemiseEnEtat: true,
    essaisEffectues: true,
    essaisConformes: true,
    travauxRealises: '',
    commentaireResponsable: '',
  });

  // Signature state
  const [_signatureBlob, setSignatureBlob] = useState<Blob | null>(null);
  const [signatureDataUrl, setSignatureDataUrl] = useState<string | null>(null);
  const [visaRecorded, setVisaRecorded] = useState(false);

  useEffect(() => {
    if (!atId) return;
    setLoading(true);

    const loadData = async () => {
      try {
        const atData = await autorisationTravailApi.findById(atId);
        setAt(atData);
        const rec = await receptionApi.getByAtId(atId).catch(() => null);
        if (rec) {
          setExistingReception(rec);
          setForm({
            autorisationTravailId: atId,
            dateDebutTravauxReelle: formatDateInput(rec.dateDebutTravauxReelle || (atData as any)?.dateDebutReelle || todayStr),
            dateFinTravauxReelle: formatDateInput(rec.dateFinTravauxReelle || (atData as any)?.dateFinReelle || todayStr),
            travauxConformes: rec.travauxConformes ?? true,
            zoneNettoyee: rec.zoneNettoyee ?? true,
            consignationRetiree: rec.consignationRetiree ?? true,
            equipementRemisEnService: rec.equipementRemisEnService ?? true,
            installationRemiseEnEtat: rec.installationRemiseEnEtat ?? true,
            essaisEffectues: rec.essaisEffectues ?? true,
            essaisConformes: rec.essaisConformes ?? true,
            travauxRealises: rec.travauxRealises || '',
            commentaireResponsable: rec.commentaireResponsable || '',
          });
        }
      } catch (err) {
        console.error('Erreur chargement AT/Réception:', err);
      } finally {
        setLoading(false);
      }
    };

    loadData();
  }, [atId]);

  const handleCheckbox = (field: keyof ReceptionTravauxRequest) => (e: React.ChangeEvent<HTMLInputElement>) => {
    setForm((prev) => ({ ...prev, [field]: e.target.checked }));
  };

  const handleText = (field: keyof ReceptionTravauxRequest) => (e: React.ChangeEvent<HTMLInputElement>) => {
    setForm((prev) => ({ ...prev, [field]: e.target.value }));
  };

  // Check mandatory conditions
  const isChecklistComplete =
    form.travauxConformes &&
    form.zoneNettoyee &&
    form.consignationRetiree &&
    form.equipementRemisEnService &&
    form.installationRemiseEnEtat &&
    form.essaisEffectues &&
    form.essaisConformes;

  const handleSubmitAndClose = async () => {
    if (!atId || !at) return;
    if (!isChecklistComplete) {
      alert('Toutes les conditions obligatoires de la checklist doivent être validées pour pouvoir réceptionner et clôturer.');
      return;
    }
    if (!visaRecorded) {
      alert('Le visa avec signature manuscrite est obligatoire pour la réception.');
      return;
    }

    setSubmitting(true);
    try {
      let recId = existingReception?.id;
      if (!recId) {
        const created = await receptionApi.create({ ...form, autorisationTravailId: atId });
        recId = created.id;
      } else {
        await receptionApi.update(recId, form);
      }

      // Signer la réception
      await receptionApi.signer(recId, 'Signature réception validée');

      // Clôturer l'AT
      await receptionApi.cloturer(recId);

      alert('Réception des travaux enregistrée et Autorisation de Travail clôturée avec succès !');
      navigate('/autorisations');
    } catch (err: any) {
      console.error(err);
      alert(err.response?.data?.message || 'Erreur lors de la réception des travaux.');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', p: 8 }}>
        <CircularProgress color="success" />
      </Box>
    );
  }

  return (
    <Box sx={{ p: 3, maxWidth: 1000, mx: 'auto' }}>
      <Button startIcon={<ArrowBackIcon />} onClick={() => navigate('/autorisations')} sx={{ mb: 2, color: '#5C6E67' }}>
        Retour
      </Button>

      <Paper sx={{ p: 4, borderRadius: 3, border: '2px solid #1F4D3E' }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
          <Box>
            <Typography variant="h5" sx={{ fontWeight: 800, color: '#1F4D3E' }}>
              SECTION - Réception des Travaux & Clôture de l'AT
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Autorisation N° {at?.numero || atId} - {at?.objet || ''}
            </Typography>
          </Box>
          <Chip label="Module 9" color="primary" size="small" />
        </Box>

        <Divider sx={{ mb: 3 }} />

        {/* Dates réelles */}
        <Grid container spacing={2} sx={{ mb: 4 }}>
          <Grid size={{ xs: 12, sm: 6 }}>
            <TextField
              fullWidth
              type="date"
              label="Date réelle de début des travaux"
              slotProps={{ inputLabel: { shrink: true } }}
              value={form.dateDebutTravauxReelle}
              onChange={handleText('dateDebutTravauxReelle')}
            />
          </Grid>
          <Grid size={{ xs: 12, sm: 6 }}>
            <TextField
              fullWidth
              type="date"
              label="Date réelle de fin / Réception"
              slotProps={{ inputLabel: { shrink: true } }}
              value={form.dateFinTravauxReelle}
              onChange={handleText('dateFinTravauxReelle')}
            />
          </Grid>
          <Grid size={12}>
            <TextField
              fullWidth
              multiline
              rows={2}
              label="Synthèse des travaux réalisés"
              value={form.travauxRealises}
              onChange={handleText('travauxRealises')}
            />
          </Grid>
        </Grid>

        {/* MANDATORY CHECKLIST */}
        <Typography variant="subtitle1" sx={{ fontWeight: 800, color: '#0E2A21', mb: 1 }}>
          Checklist de Réception (Conditions Obligatoires)
        </Typography>

        <Paper sx={{ p: 3, mb: 4, bgcolor: isChecklistComplete ? '#EDF2EE' : '#F7FAF8', border: `1px solid ${isChecklistComplete ? '#7FC8A9' : '#FBEAE3'}` }}>
          <Grid container spacing={1.5}>
            <Grid size={{ xs: 12, sm: 6 }}>
              <FormControlLabel
                control={<Checkbox checked={form.travauxConformes} onChange={handleCheckbox('travauxConformes')} color="success" />}
                label={<Typography sx={{ fontWeight: 600 }}>✓ Travaux conformes au cahier des charges</Typography>}
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <FormControlLabel
                control={<Checkbox checked={form.zoneNettoyee} onChange={handleCheckbox('zoneNettoyee')} color="success" />}
                label={<Typography sx={{ fontWeight: 600 }}>✓ Zone nettoyée et mise au propre</Typography>}
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <FormControlLabel
                control={<Checkbox checked={form.consignationRetiree} onChange={handleCheckbox('consignationRetiree')} color="success" />}
                label={<Typography sx={{ fontWeight: 600 }}>✓ Consignation des énergies retirée</Typography>}
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <FormControlLabel
                control={<Checkbox checked={form.equipementRemisEnService} onChange={handleCheckbox('equipementRemisEnService')} color="success" />}
                label={<Typography sx={{ fontWeight: 600 }}>✓ Équipement remis en service</Typography>}
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <FormControlLabel
                control={<Checkbox checked={form.installationRemiseEnEtat} onChange={handleCheckbox('installationRemiseEnEtat')} color="success" />}
                label={<Typography sx={{ fontWeight: 600 }}>✓ Installation remise en état</Typography>}
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <FormControlLabel
                control={<Checkbox checked={form.essaisEffectues && form.essaisConformes} onChange={(e) => {
                  setForm((p) => ({ ...p, essaisEffectues: e.target.checked, essaisConformes: e.target.checked }));
                }} color="success" />}
                label={<Typography sx={{ fontWeight: 600 }}>✓ Essais effectués et conformes</Typography>}
              />
            </Grid>
          </Grid>

          {!isChecklistComplete && (
            <Alert severity="error" sx={{ mt: 2, fontWeight: 600 }}>
              Toutes les conditions doivent être cochées avant de clôturer.
            </Alert>
          )}
        </Paper>

        {/* Signature Section */}
        <Typography variant="subtitle1" sx={{ fontWeight: 800, color: '#0E2A21', mb: 1 }}>
          Visa et Signature Manuscrite du Responsable
        </Typography>

        <Box sx={{ mb: 4 }}>
          <SignaturePad
            title="Visa de Réception des Travaux"
            onSave={(blob, dataUrl) => {
              setSignatureBlob(blob);
              setSignatureDataUrl(dataUrl);
              setVisaRecorded(true);
            }}
            onClear={() => {
              setSignatureBlob(null);
              setSignatureDataUrl(null);
              setVisaRecorded(false);
            }}
            savedDataUrl={signatureDataUrl}
          />
        </Box>

        {/* Submit */}
        <Box sx={{ display: 'flex', justifyContent: 'flex-end' }}>
          <Button
            variant="contained"
            size="large"
            startIcon={submitting ? <CircularProgress size={20} color="inherit" /> : <VerifiedIcon />}
            onClick={handleSubmitAndClose}
            disabled={!isChecklistComplete || !visaRecorded || submitting}
            sx={{ bgcolor: '#1F4D3E', '&:hover': { bgcolor: '#2E624A' }, px: 4, py: 1.5, fontWeight: 800, fontSize: 16 }}
          >
            Réceptionner et Clôturer l'AT
          </Button>
        </Box>
      </Paper>
    </Box>
  );
}
