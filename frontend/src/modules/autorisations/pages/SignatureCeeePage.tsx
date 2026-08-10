import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { Box, Paper, Typography, Button, Chip, Divider, Alert } from '@mui/material';
import {apiClient} from '../../../services/apiClient';
import FormulaireOCPViewer from '../../../components/common/FormulaireOCPViewer';
import SignaturePad from '../../../components/common/SignaturePad';

export default function SignatureCeeePage() {
  const { id } = useParams(); // id de l'AT
  const [at, setAt] = useState<any>(null);
  const [visaCeee, setVisaCeee] = useState<any>(null);
  const [loading, setLoading] = useState(true);

  const load = async () => {
    const [atRes, visasRes] = await Promise.all([
      apiClient.get(`/autorisations-travail/${id}`),
      apiClient.get(`/visa/at/${id}`),
    ]);
    setAt(atRes.data);
    const visa = visasRes.data.find((v: any) => v.commentaire === 'g1VisaCeee' || v.champ === 'g1VisaCeee')
      ?? visasRes.data.find((v: any) => v.statut === 'EN_ATTENTE');
    setVisaCeee(visa ?? null);
    setLoading(false);
  };

  useEffect(() => { load(); }, [id]);

  const accuserReception = async () => {
    await apiClient.put(`/autorisations-travail/${id}/accuser-reception-ceee`);
    await load();
  };

  const signer = async (signatureBlob: Blob) => {
    if (!visaCeee) return;
    const formData = new FormData();
    formData.append('signature', signatureBlob);
    if (visaCeee.commentaire !== 'g1VisaCeee') {
      formData.append('commentaire', 'g1VisaCeee');
    }
    await apiClient.post(`/visa/${visaCeee.id}/sign`, formData);
    await load();
  };

  if (loading || !at) return null;

  return (
    <Box sx={{ maxWidth: 900, mx: 'auto', p: 3 }}>
      <Paper sx={{ p: 3, mb: 2 }}>
        <Typography variant="h5" gutterBottom>Intervention à signer — {at.numero}</Typography>
        <Typography color="text.secondary">{at.objet}</Typography>
        <Divider sx={{ my: 2 }} />
        <Typography variant="body2">Zone : {at.zoneProprietaire?.nomZone}</Typography>
        <Typography variant="body2">Date : {at.dateDebut} ({at.heureDebut} → {at.heureFin})</Typography>
        <Typography variant="body2">Entreprise(s) : {at.entreprisesIntervenantes}</Typography>

        {!at.dateReceptionCeee ? (
          <>
            <Alert severity="warning" sx={{ my: 2 }}>
              Vous devez accuser réception de cette AT avant de pouvoir la consulter et la signer.
            </Alert>
            <Button variant="contained" onClick={accuserReception}>J'accuse réception</Button>
          </>
        ) : (
          <>
            <Chip label={`Reçue le ${new Date(at.dateReceptionCeee).toLocaleString()}`} color="success" sx={{ my: 2 }} />
            <FormulaireOCPViewer at={at} />
            {visaCeee && visaCeee.statut === 'EN_ATTENTE' && (
              <Box sx={{ mt: 3 }}>
                <Typography variant="subtitle1">Votre signature (Visa CEEE)</Typography>
                <SignaturePad onSave={signer} />
              </Box>
            )}
          </>
        )}
      </Paper>
    </Box>
  );
}