import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Box, Paper, Typography, Button, Chip, Divider, Alert, CircularProgress } from '@mui/material';
import { apiClient } from '../../../services/apiClient';
import { visaApi } from '../../../services/visaApi';
import FormulaireOCPViewer from '../../../components/common/FormulaireOCPViewer';
import SignaturePad from '../../../components/common/SignaturePad';
import { useAuthStore } from '../../../store/authStore';

export default function SignatureCeeePage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const user = useAuthStore((s) => s.user);
  const [at, setAt] = useState<any>(null);
  const [visaCeee, setVisaCeee] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [successMsg, setSuccessMsg] = useState<string | null>(null);
  const [actionLoading, setActionLoading] = useState(false);

  const load = async () => {
    try {
      const [atRes, visasRes] = await Promise.all([
        apiClient.get(`/autorisations-travail/${id}`),
        apiClient.get(`/visa/at/${id}`),
      ]);
      setAt(atRes.data);
      const visa =
        visasRes.data.find((v: any) => v.commentaire === 'g1VisaCeee' || v.champ === 'g1VisaCeee') ??
        visasRes.data.find((v: any) => v.statut === 'EN_ATTENTE');
      setVisaCeee(visa ?? null);
    } catch (err: any) {
      setError(err?.response?.data?.message || "Erreur de chargement de l'AT.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, [id]);

  /**
   * §8.1 S-HSE-SEC-31 — Le CEEP est uniquement le créateur/propriétaire de l'AT.
   */
  const isOwnerCeep =
    at != null &&
    user != null &&
    (at.proprietaireBrouillon?.id === user.id || at.ceep?.id === user.id);

  const accuserReception = async () => {
    setError(null);
    setActionLoading(true);
    try {
      await apiClient.put(`/autorisations-travail/${id}/accuser-reception-ceee`);
      await load();
      setSuccessMsg("Accusé de réception CEEE enregistré. Vous pouvez maintenant apposer votre signature ci-dessous.");
    } catch (err: any) {
      const raw = err?.response?.data;
      const msg =
        (typeof raw === 'string' ? raw : raw?.message) ||
        "Impossible d'accuser réception de cette AT.";
      setError(msg);
    } finally {
      setActionLoading(false);
    }
  };

  const signer = async (signatureBlob: Blob) => {
    setError(null);
    setActionLoading(true);
    try {
      if (visaCeee?.id) {
        const fd = new FormData();
        fd.append('signature', signatureBlob, 'signature.png');
        if (visaCeee.commentaire !== 'g1VisaCeee') fd.append('commentaire', 'g1VisaCeee');
        await apiClient.post(`/visa/${visaCeee.id}/sign`, fd);
      } else {
        // Fallback: créer et signer le visa CEEE directement
        await visaApi.createAndSignVisa(at.id, signatureBlob, 'g1VisaCeee', 2);
      }
      setSuccessMsg("Signature Visa CEEE enregistrée avec succès ✓");
      await load();
      setTimeout(() => navigate('/autorisations'), 2000);
    } catch (err: any) {
      setError(err?.response?.data?.message || "Erreur lors de l'enregistrement de la signature.");
    } finally {
      setActionLoading(false);
    }
  };

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', p: 6 }}>
        <CircularProgress color="primary" />
      </Box>
    );
  }

  if (!at) {
    return (
      <Box sx={{ maxWidth: 900, mx: 'auto', p: 3 }}>
        <Alert severity="error">AT introuvable ou vous n'avez pas accès.</Alert>
      </Box>
    );
  }

  return (
    <Box sx={{ maxWidth: 960, mx: 'auto', p: 3 }}>
      <Paper sx={{ p: 3, mb: 3, borderRadius: 3 }}>
        <Typography variant="h5" sx={{ fontWeight: 800, color: '#16241E' }} gutterBottom>
          Visa &amp; Signature CEEE — {at.numero}
        </Typography>
        <Typography color="text.secondary">
          {at.objet || 'Intervention OCP S-HSE-SEC-31'}
        </Typography>
        <Divider sx={{ my: 2 }} />
        <Typography variant="body2">
          <strong>Zone Propriétaire :</strong> {at.zoneProprietaire?.nomZone || at.zoneProprietaireNom || '—'}
        </Typography>
        <Typography variant="body2">
          <strong>Service Exécutant :</strong> {at.servicesIntervenants || at.zoneExecutante?.nomZone || '—'}
        </Typography>
        <Typography variant="body2">
          <strong>Date prévue :</strong> {at.dateDebut} ({at.heureDebut} → {at.heureFin})
        </Typography>

        {/* Message d'erreur */}
        {error && (
          <Alert severity="error" sx={{ my: 2, borderRadius: 2 }} onClose={() => setError(null)}>
            {error}
          </Alert>
        )}

        {/* Message de succès */}
        {successMsg && (
          <Alert severity="success" sx={{ my: 2, borderRadius: 2 }} onClose={() => setSuccessMsg(null)}>
            {successMsg}
          </Alert>
        )}

        {/* ─── CEEP : avertissement ─── */}
        {isOwnerCeep ? (
          <Alert severity="error" sx={{ my: 2, borderRadius: 2 }}>
            <strong>Action réservée au CEEE.</strong> Vous êtes connecté en tant que{' '}
            <strong>CEEP (créateur / propriétaire)</strong> de cette AT. Conformément au Standard OCP
            S-HSE-SEC-31 (§8.1), l'accusé de réception et la signature CEEE doivent être effectués par
            le <strong>Chef d'Équipe Exécutant (CEEE)</strong> depuis son propre compte.
          </Alert>
        ) : !at.dateReceptionCeee ? (
          /* ─── Étape 1 CEEE : Accusé de réception ─── */
          <Box sx={{ my: 2 }}>
            <Alert severity="info" sx={{ mb: 2, borderRadius: 2 }}>
              Étape 1 sur 2 : Vous devez d'abord accuser réception de cette AT au nom de l'équipe exécutante (CEEE).
            </Alert>
            <Button
              variant="contained"
              color="success"
              disabled={actionLoading}
              onClick={accuserReception}
              sx={{ fontWeight: 800, borderRadius: 2, px: 4, py: 1.2, fontSize: '1rem' }}
            >
              {actionLoading ? (
                <CircularProgress size={24} color="inherit" />
              ) : (
                "J'accuse réception (CEEE)"
              )}
            </Button>
          </Box>
        ) : (
          /* ─── Étape 2 CEEE : Signature disponible ─── */
          <Box sx={{ mt: 2 }}>
            <Chip
              label={`Reçue le ${new Date(at.dateReceptionCeee).toLocaleString('fr-FR')}`}
              color="success"
              sx={{ mb: 2, fontWeight: 700 }}
            />
            <Alert severity="success" sx={{ mb: 3, borderRadius: 2 }}>
              <strong>Accusé de réception validé ✓.</strong> Veuillez apposer votre signature manuscrite (Visa CEEE) ci-dessous.
            </Alert>

            {/* Zone de signature manuscrite CEEE très claire et visible */}
            <Paper
              elevation={3}
              sx={{
                p: 3,
                mb: 4,
                bgcolor: '#EDF2EE',
                border: '2px solid #3C7A5C',
                borderRadius: 3,
              }}
            >
              <Typography variant="h6" sx={{ fontWeight: 800, color: '#2E624A', mb: 1 }}>
                ✍️ Emplacement de Signature — Visa CEEE (Chef d'Équipe Exécutant)
              </Typography>
              <Typography variant="body2" sx={{ color: '#2E624A', mb: 2 }}>
                Tracez votre signature ci-dessous pour signer le visa CEEE (Section G) :
              </Typography>
              {actionLoading ? (
                <Box sx={{ display: 'flex', justifyContent: 'center', p: 3 }}>
                  <CircularProgress color="success" />
                </Box>
              ) : (
                <SignaturePad onSave={signer} />
              )}
            </Paper>

            {/* Aperçu complet du formulaire AT */}
            <Typography variant="h6" sx={{ fontWeight: 800, mb: 2 }}>
              Aperçu complet du Formulaire AT
            </Typography>
            <FormulaireOCPViewer
              at={at}
              signMode="ceee"
              onVisaCeee={(_formData, blob) => signer(blob)}
            />
          </Box>
        )}
      </Paper>
    </Box>
  );
}