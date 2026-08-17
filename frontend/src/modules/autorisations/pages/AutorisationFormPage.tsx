import React, { useState, useEffect } from 'react';
import {
  Box,
  Typography,
  Paper,
  Button,
  CircularProgress,
  Chip,
  Alert,
  Container,
  Stack,
  IconButton,
  Tooltip,
} from '@mui/material';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import SaveIcon from '@mui/icons-material/Save';
import AutoAwesomeIcon from '@mui/icons-material/AutoAwesome';
import SecurityIcon from '@mui/icons-material/Security';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import { useNavigate, useParams, useSearchParams } from 'react-router-dom';
import FormulaireOCPInteractive from '../../../components/common/FormulaireOCPInteractive';
import { apiClient } from '../../../services/apiClient';
import { autorisationTravailApi } from '../../../services/autorisationTravailApi';
import { visaApi } from '../../../services/visaApi';
import { iaApi } from '../../../services/iaApi';
import { useAuthStore } from '../../../store/authStore';

export default function AutorisationFormPage() {
  const navigate = useNavigate();
  const { id: draftId } = useParams<{ id: string }>();
  const [searchParams] = useSearchParams();
  const currentUser = useAuthStore((s) => s.user);
  const roles = currentUser?.roles?.map((r: any) => r.nom) || [];
  const isCeee = roles.includes('CEEE');

  const [loading, setLoading] = useState(false);
  const [atId, setAtId] = useState<string | null>(draftId || null);
  const [initialData, setInitialData] = useState<any>(null);
  const [statusMsg, setStatusMsg] = useState<string | null>(null);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);
  const [atStatut, setAtStatut] = useState<string | null>(null);

  // Mode viser CEEE : formulaire partagé, seule case visa CEEE active
  const modeViser = searchParams.get('mode') === 'viser' || (isCeee && atStatut === 'SOUMISE');
  const signMode: 'all' | 'ceee' = modeViser ? 'ceee' : 'all';

  // Form Data (F-HSE-SEC-31-04)
  const [formInteractiveData, setFormInteractiveData] = useState<any>({});

  // Load existing draft if editing
  useEffect(() => {
    if (!draftId) return;
    setLoading(true);
    apiClient
      .get(`/autorisations-travail/${draftId}`)
      .then((res) => {
        const at = res.data;
        setInitialData(at);
        setAtStatut(at.statut || null);
        setAtId(at.id);
        setFormInteractiveData({
          numero: at.numero || '',
          objet: at.objet || '',
          description: at.descriptionTravaux || at.objet || '',
          descriptionTravaux: at.descriptionTravaux || '',
          dateIntervention: at.dateDebut || '',
          dateDebut: at.dateDebut || '',
          dateFin: at.dateFin || '',
          heureDebut: at.heureDebut || '',
          heureFin: at.heureFin || '',
          servicesIntervenants: at.servicesIntervenants || '',
          serviceIntervenantId: at.serviceIntervenantId || null,
          entreprisesIntervenantes: at.entreprisesIntervenantes || '',
          sectionF: at.mesuresSecuriteExecutant || '',
          mesuresSecuriteExecutant: at.mesuresSecuriteExecutant || '',
          risquesIds: at.risquesIds || (at.risques || []).map((r: any) => r.id),
          mesuresIds: at.mesuresIds || (at.mesures || []).map((m: any) => m.id),
          episIds: at.episIds || (at.epis || []).map((e: any) => e.id),
          moyensAccesIds: at.moyensAccesIds || (at.moyensAcces || []).map((m: any) => m.id),
          permisIds: at.permisIds || (at.permis || []).map((p: any) => p.typePermis?.id || p.id),
          zoneProprietaire: at.zoneProprietaire || null,
          zoneExecutante: at.zoneExecutante || null,
          typeDocumentSource: at.typeDocumentSource || 'DI',
          documentSourceType: at.typeDocumentSource || 'DI',
          documentSourceId: at.documentSourceId || '',
          documentSourceNumero: at.documentSourceNumero || '',
          g1NomCeep: at.g1NomCeep || '',
          g1NomCeee: at.g1NomCeee || '',
          latitude: at.latitude || null,
          longitude: at.longitude || null,
          visiteCommentaire: at.visiteCommentaire || '',
          visiteEffectuee: at.visiteEffectuee ?? true,
          photoPath: at.photoPath || null,
          _loaded: true,
        });
      })
      .catch((err) => {
        console.error("Erreur chargement brouillon", err);
        setAtId(null);
        setFormInteractiveData({});
        setErrorMsg("Le brouillon d'AT n'existe plus ou a été supprimé.");
      })
      .finally(() => setLoading(false));
  }, [draftId]);

  // Save Draft Handler
  const handleSaveDraft = async (rawData: any) => {
    const data = rawData ?? {};
    setFormInteractiveData(data);
    setErrorMsg(null);
    try {
      let currentId = atId;
      if (!currentId) {
        const res = await apiClient.post('/autorisations-travail');
        currentId = res.data.id;
        setAtId(currentId);
      }

      const payload = {
        objet: data.description || data.objet || 'Intervention OCP S-HSE-SEC-31',
        descriptionTravaux: data.description || '',
        dateDebut: data.dateIntervention || null,
        dateFin: data.dateIntervention || null,
        heureDebut: (data.heureDebut || '08:00').toString().substring(0, 8),
        heureFin: (data.heureFin || '17:00').toString().substring(0, 8),
        servicesIntervenants: data.servicesIntervenants || '',
        serviceIntervenantId: data.serviceIntervenantId || null,
        zoneProprietaireId: data.zoneProprietaireId || data.siteId || null,
        entreprisesIntervenantes: data.entreprisesIntervenantes || '',
        mesuresSecuriteExecutant: data.sectionF || '',
        risquesIds: (data.risquesIds || []).map(String),
        mesuresIds: (data.mesuresIds || []).map(String),
        episIds: (data.episIds || []).map(String),
        moyensAccesIds: (data.moyensAccesIds || []).map(String),
        permisIds: (data.permisIds || []).map(String),
        typeDocumentSource: data.documentSourceType || data.typeDocumentSource || 'DI',
        documentSourceId: data.documentSourceId || null,
        documentSourceNumero: data.documentSourceNumero || null,
        latitude: data.latitude || null,
        longitude: data.longitude || null,
        visiteCommentaire: data.visiteCommentaire || null,
        visiteEffectuee: data.visiteEffectuee ?? true,
        photoPath: data.photoPath || null,
      };

      const { data: saved } = await apiClient.put(`/autorisations-travail/${currentId}/autosave`, payload);

      setFormInteractiveData((prev: any) => ({
        ...prev,
        ...data,
        _loaded: true,
        id: saved.id,
        numero: saved.numero || prev?.numero,
        risquesIds: ((saved.risquesIds && saved.risquesIds.length > 0) ? saved.risquesIds : (data.risquesIds || prev?.risquesIds || [])).map(String),
        mesuresIds: ((saved.mesuresIds && saved.mesuresIds.length > 0) ? saved.mesuresIds : (data.mesuresIds || prev?.mesuresIds || [])).map(String),
        episIds: ((saved.episIds && saved.episIds.length > 0) ? saved.episIds : (data.episIds || prev?.episIds || [])).map(String),
        moyensAccesIds: ((saved.moyensAccesIds && saved.moyensAccesIds.length > 0) ? saved.moyensAccesIds : (data.moyensAccesIds || prev?.moyensAccesIds || [])).map(String),
        permisIds: ((saved.permisIds && saved.permisIds.length > 0) ? saved.permisIds : (data.permisIds || prev?.permisIds || [])).map(String),
        description: saved.descriptionTravaux || data.description || '',
        servicesIntervenants: saved.servicesIntervenants || data.servicesIntervenants || '',
        sectionF: saved.mesuresSecuriteExecutant || data.sectionF || '',
      }));

      setStatusMsg('Brouillon enregistré ✓');
      setTimeout(() => setStatusMsg(null), 3000);
      return saved;
    } catch (err: any) {
      setErrorMsg(err.response?.data?.message || 'Erreur lors de la sauvegarde du brouillon.');
      throw err;
    }
  };

  // Submit Handler (Signature & Workflow Transmission to CEEE)
  const handleSubmitAT = async (data: any, signatureBlob?: Blob) => {
    setErrorMsg(null);

    // Controle IA non bloquant
    try {
      const ctrl = await iaApi.controlerDossier({
        description: data?.description,
        visiteFaite: true,
        nbRisques: (data?.risquesIds || []).length,
        nbMesures: (data?.mesuresIds || []).length,
        nbEpis: (data?.episIds || []).length,
        nbPermis: (data?.permisIds || []).length,
        sectionFRenseignee: !!(data?.sectionF || data?.mesuresSecuriteExecutant),
      });
      if (!ctrl.complet && ctrl.alertes?.length) {
        const ok = window.confirm(
          'Contrôle IA — alertes :\n\n' +
          ctrl.alertes.join('\n') +
          '\n\nSoumettre quand même ?'
        );
        if (!ok) return;
      }
    } catch {
      /* IA indisponible : on continue */
    }

    setFormInteractiveData(data);
    setLoading(true);
    setStatusMsg('Validation et transmission de l\'AT au CEEE...');
    try {
      // Sauvegarder d'abord le brouillon (crée l'AT si besoin et sauvegarde les données)
      const saved = await handleSaveDraft(data);
      const currentId = saved?.id || atId;

      if (!currentId) {
        throw new Error("Impossible d'obtenir l'identifiant de l'AT.");
      }

      // Le visa CEEP doit être créé ET signé avant la soumission.
      // L'ancien code cherchait un visa EN_ATTENTE qui n'était pas garanti d'exister,
      // ce qui pouvait laisser l'AT avec uniquement la date de création dans le PDF,
      // sans fichier PNG de signature.
      if (!signatureBlob) {
        throw new Error("La signature CEEP est obligatoire avant la transmission de l'AT.");
      }

      await visaApi.createAndSignVisa(
        currentId,
        signatureBlob,
        'g1VisaCeep',
        1
      );

      await autorisationTravailApi.soumettre(currentId);

      setStatusMsg('AT soumise et transmise avec succès au Chef d\'Équipe Exécutant (CEEE) ✓');
      setTimeout(() => navigate('/autorisations'), 2000);
    } catch (err: any) {
      console.error(err);
      setErrorMsg(err.response?.data?.message || 'Erreur lors de la soumission de l\'AT.');
    } finally {
      setLoading(false);
    }
  };

  const handleVisaCeee = async (_data: any, signatureBlob: Blob) => {
    if (!atId) {
      setErrorMsg("AT introuvable");
      return;
    }
    setLoading(true);
    try {
      await visaApi.createAndSignVisa(atId, signatureBlob, 'Visa CEEE — signature exécutant', 2);
      setStatusMsg('Visa CEEE enregistré avec succès.');
      setTimeout(() => navigate(`/autorisations/${atId}`), 1500);
    } catch (err: any) {
      console.error(err);
      setErrorMsg(err.response?.data?.message || 'Erreur lors de l\'enregistrement du visa CEEE.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Container maxWidth="xl" sx={{ py: 3 }}>
      {/* HEADER BANNER */}
      <Paper
        elevation={0}
        sx={{
          p: 2.5,
          mb: 3,
          bgcolor: '#0E2A21',
          color: 'white',
          borderRadius: 3,
          border: '1px solid #16241E',
        }}
      >
        <Stack direction="row" sx={{ justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 2 }}>
          <Stack direction="row" spacing={2} sx={{ alignItems: 'center' }}>
            <Tooltip title="Retour à la liste des AT">
              <IconButton onClick={() => navigate('/autorisations')} sx={{ color: 'white', bgcolor: '#16241E', '&:hover': { bgcolor: '#16241E' } }}>
                <ArrowBackIcon />
              </IconButton>
            </Tooltip>
            <Box>
              <Stack direction="row" spacing={1.5} sx={{ alignItems: 'center' }}>
                <Typography variant="h5" sx={{ fontWeight: 800 }}>
                  {formInteractiveData?.numero || (draftId ? 'Édition AT' : 'Nouvelle Autorisation de Travail')}
                </Typography>
                <Chip label="F-HSE-SEC-31-04" size="small" sx={{ bgcolor: '#1F4D3E', color: 'white', fontWeight: 700 }} />
                {atStatut && <Chip label={atStatut} color="warning" size="small" sx={{ fontWeight: 700 }} />}
              </Stack>
              <Typography variant="body2" sx={{ color: '#5C6E67', mt: 0.5 }}>
                Standard OCP S-HSE-SEC-31 &mdash; Formulaire d'Autorisation de Travail et Permis
              </Typography>
            </Box>
          </Stack>

          <Stack direction="row" spacing={1.5}>
            {!modeViser && (
              <Button
                variant="outlined"
                startIcon={<SaveIcon />}
                onClick={() => handleSaveDraft(formInteractiveData)}
                disabled={loading}
                sx={{ color: 'white', borderColor: '#5C6E67', '&:hover': { borderColor: '#5C6E67', bgcolor: 'rgba(255,255,255,0.05)' }, borderRadius: 2 }}
              >
                Enregistrer Brouillon
              </Button>
            )}

            {/* Bouton de soumission unique : voir "Signer & Transmettre" dans le formulaire
                ci-dessous (handleSignerEtTransmettre), qui vérifie la Visite Préalable §8.2,
                enrichit GPS/photo/commentaire et attache la signature CEEP avant transmission.
                L'ancien bouton ici dupliquait la soumission en sautant ces étapes
                (transmission "vide"/sans signature) — supprimé pour éviter toute confusion. */}
          </Stack>
        </Stack>
      </Paper>

      {/* NOTIFICATIONS */}
      {statusMsg && (
        <Alert severity="success" icon={<CheckCircleIcon />} sx={{ mb: 3, borderRadius: 2, fontWeight: 600 }}>
          {statusMsg}
        </Alert>
      )}

      {errorMsg && (
        <Alert severity="error" sx={{ mb: 3, borderRadius: 2, fontWeight: 600 }} onClose={() => setErrorMsg(null)}>
          {errorMsg}
        </Alert>
      )}

      {/* REGLEMENTARY INFO */}
      <Alert severity="info" sx={{ mb: 3, borderRadius: 2 }}>
        <strong>Standard OCP S-HSE-SEC-31 (§2 & §8.1) :</strong> Le CEEP remplit l'AT (Sections A à G), appose sa signature et transmet l'AT au CEEE du service exécutant. La zone propriétaire et la zone exécutante doivent appartenir à des services différents.
      </Alert>

      {/* MAIN FORMULAIRE OCP INTERACTIVE */}
      <Paper elevation={0} sx={{ p: 1, borderRadius: 3, border: '1px solid #D6E3DC' }}>
        <FormulaireOCPInteractive
          key={atId || draftId || 'new'}
          initialData={formInteractiveData}
          readOnly={false}
          signMode={signMode}
          onChange={(data) => setFormInteractiveData(data)}
          onSave={modeViser ? undefined : handleSaveDraft}
          onAutoSave={modeViser ? undefined : handleSaveDraft}
          onSubmitAT={modeViser ? undefined : handleSubmitAT}
          onVisaCeee={modeViser ? handleVisaCeee : undefined}
          loading={loading}
        />
      </Paper>
    </Container>
  );
}