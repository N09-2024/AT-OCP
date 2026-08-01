import React, { useState, useEffect, useRef } from 'react';
import {
  Box,
  Typography,
  Paper,
  Button,
  Stepper,
  Step,
  StepLabel,
  CircularProgress,
  TextField,
  Grid,
  Checkbox,
  FormControlLabel,
  Chip,
  Alert,
  Divider,
  Container,
  Card,
  CardContent,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
} from '@mui/material';
import LocationOnIcon from '@mui/icons-material/LocationOn';
import CameraAltIcon from '@mui/icons-material/CameraAlt';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import ErrorIcon from '@mui/icons-material/Error';
import UploadFileIcon from '@mui/icons-material/UploadFile';
import SendIcon from '@mui/icons-material/Send';
import SecurityIcon from '@mui/icons-material/Security';
import AssignmentIcon from '@mui/icons-material/Assignment';
import ConstructionIcon from '@mui/icons-material/Construction';
import VisibilityIcon from '@mui/icons-material/Visibility';
import { useNavigate, useParams } from 'react-router-dom';
import FormulaireOCPInteractive from '../../../components/common/FormulaireOCPInteractive';
import { apiClient } from '../../../services/apiClient';
import { visaApi } from '../../../services/visaApi';
import { useAuthStore } from '../../../store/authStore';

const WORKFLOW_STEPS = [
  '0 & 1 - Classification & Doc Source',
  '2 - Visite Préalable Chantier',
  '3 - Rédaction Formulaire F-HSE-SEC-31-04',
  'Validation & Visa Terrain',
];

const DOCUMENT_TYPES = [
  { value: 'DI', label: "Demande d'Intervention (DI)", endpoint: '/demandes-intervention' },
  { value: 'OT', label: 'Ordre de Travail (OT)', endpoint: '/ordres-travail' },
  { value: 'BT', label: 'Bon de Travail (BT)', endpoint: '/bons-travail' },
];

export default function AutorisationFormPage() {
  const navigate = useNavigate();
  const { id: draftId } = useParams<{ id: string }>();
  const currentUser = useAuthStore((s) => s.user);
  const photoInputRef = useRef<HTMLInputElement>(null);

  const [activeStep, setActiveStep] = useState(draftId ? 2 : 0);
  const [loading, setLoading] = useState(false);
  const [atId, setAtId] = useState<string | null>(draftId || null);
  const [initialData, setInitialData] = useState<any>(null);
  const [statusMsg, setStatusMsg] = useState<string | null>(null);

  // Étape 0 — Classification
  const [niveauClassification, setNiveauClassification] = useState<'NIVEAU_1' | 'NIVEAU_2'>('NIVEAU_2');
  const [estTiers, setEstTiers] = useState<boolean>(true);
  const [natureIntervention, setNatureIntervention] = useState<string>('Intervention de maintenance avec risques spécifiques');

  // Étape 1 — Document Source
  const [docType, setDocType] = useState<'DI' | 'OT' | 'BT'>('DI');
  const [docList, setDocList] = useState<any[]>([]);
  const [selectedDoc, setSelectedDoc] = useState<any | null>(null);

  // Étape 2 — Visite Préalable (§8.2 Standard OCP)
  const [visiteId, setVisiteId] = useState<string | null>(null);
  const [gpsCoords, setGpsCoords] = useState<{ lat: number; lng: number } | null>(null);
  const [gpsLoading, setGpsLoading] = useState(false);
  const [gpsError, setGpsError] = useState<string | null>(null);
  const [photoFile, setPhotoFile] = useState<File | null>(null);
  const [visiteCommentaire, setVisiteCommentaire] = useState('');
  const [preventionEnPlace, setPreventionEnPlace] = useState(false);

  // Étape 3 — Form Data (F-HSE-SEC-31-04)
  const [formInteractiveData, setFormInteractiveData] = useState<any>({});

  // Fetch document sources when docType changes
  useEffect(() => {
    const found = DOCUMENT_TYPES.find((d) => d.value === docType);
    if (!found) return;
    setLoading(true);
    setSelectedDoc(null);
    setDocList([]);
    apiClient
      .get(`${found.endpoint}?page=0&size=100`)
      .then((res) => setDocList(Array.isArray(res.data) ? res.data : res.data?.content || []))
      .catch(() => setDocList([]))
      .finally(() => setLoading(false));
  }, [docType]);

  // Load existing draft if editing
  useEffect(() => {
    if (!draftId) return;
    setLoading(true);
    apiClient
      .get(`/autorisations-travail/${draftId}`)
      .then((res) => {
        setInitialData(res.data);
        setFormInteractiveData(res.data);
      })
      .catch((err) => {
        console.error(err);
        alert("Impossible de charger le brouillon d'AT.");
        navigate('/autorisations');
      })
      .finally(() => setLoading(false));
  }, [draftId, navigate]);

  // GPS Geolocation Handler
  const handleGetGps = () => {
    setGpsLoading(true);
    setGpsError(null);
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        setGpsCoords({ lat: pos.coords.latitude, lng: pos.coords.longitude });
        setGpsLoading(false);
      },
      () => {
        setGpsError("Impossible de récupérer la géolocalisation GPS du chantier. Veuillez autoriser l'accès GPS.");
        setGpsLoading(false);
      }
    );
  };

  // Photo Capture Handler
  const handlePhotoChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0] ?? null;
    setPhotoFile(file);
  };

  // Stepper Guard (Contraintes du Standard S-HSE-SEC-31)
  const canGoNext = (): boolean => {
    if (activeStep === 0) {
      if (niveauClassification === 'NIVEAU_1' && !estTiers) {
        return false; // Interventions Niveau 1 internes : pas d'AT requise
      }
      return true;
    }
    if (activeStep === 1) {
      // Visite préalable obligatoire (§8.2)
      return !!gpsCoords && !!photoFile && preventionEnPlace;
    }
    return true;
  };

  // Save Draft Handler
  const handleSaveDraft = async (data: any) => {
    setFormInteractiveData(data);
    setLoading(true);
    setStatusMsg('Enregistrement du brouillon...');
    try {
      let currentId = atId;
      if (!currentId) {
        let res;
        if (selectedDoc) {
          res = await apiClient.post(`/documents/${docType}/${selectedDoc.id}/creer-at`);
        } else {
          res = await apiClient.post('/autorisations-travail');
        }
        currentId = res.data.id;
        setAtId(currentId);
      }

      const payload = {
        objet: data.description || selectedDoc?.objet || 'Intervention OCP S-HSE-SEC-31',
        descriptionTravaux: data.description || '',
        dateDebut: data.dateIntervention || null,
        dateFin: data.dateIntervention || null,
        heureDebut: data.heureDebut || '08:00',
        heureFin: data.heureFin || '17:00',
        servicesIntervenants: data.servicesIntervenants || '',
        entreprisesIntervenantes: data.entreprisesIntervenantes || '',
        mesuresSecuriteExecutant: data.sectionF || '',
        risquesIds: data.risquesIds || [],
        mesuresIds: data.mesuresIds || [],
        episIds: data.episIds || [],
        moyensAccesIds: data.moyensAccesIds || [],
        permisIds: data.permisIds || [],
      };

      await apiClient.put(`/autorisations-travail/${currentId}/autosave`, payload);
      setStatusMsg('Brouillon enregistré ✓');
      setTimeout(() => setStatusMsg(null), 3000);
    } catch (err: any) {
      console.error(err);
      alert(err.response?.data?.message || 'Erreur lors de la sauvegarde du brouillon.');
    } finally {
      setLoading(false);
    }
  };

  // Submit Handler (Signature & Workflow Transition)
  const handleSubmitAT = async (data: any, signatureBlob?: Blob) => {
    setFormInteractiveData(data);
    setLoading(true);
    setStatusMsg('Enregistrement et validation finale...');
    try {
      let currentId = atId;
      if (!currentId) {
        let res;
        if (selectedDoc) {
          res = await apiClient.post(`/documents/${docType}/${selectedDoc.id}/creer-at`);
        } else {
          res = await apiClient.post('/autorisations-travail');
        }
        currentId = res.data.id;
        setAtId(currentId);
      }

      // AutoSave final
      const payload = {
        objet: data.description || selectedDoc?.objet || 'Intervention OCP S-HSE-SEC-31',
        descriptionTravaux: data.description || '',
        dateDebut: data.dateIntervention || null,
        dateFin: data.dateIntervention || null,
        heureDebut: data.heureDebut || '08:00',
        heureFin: data.heureFin || '17:00',
        servicesIntervenants: data.servicesIntervenants || '',
        entreprisesIntervenantes: data.entreprisesIntervenantes || '',
        mesuresSecuriteExecutant: data.sectionF || '',
        risquesIds: data.risquesIds || [],
        mesuresIds: data.mesuresIds || [],
        episIds: data.episIds || [],
        moyensAccesIds: data.moyensAccesIds || [],
        permisIds: data.permisIds || [],
      };
      await apiClient.put(`/autorisations-travail/${currentId}/autosave`, payload);

      // Signer le visa si blob disponible
      if (signatureBlob && currentId) {
        await visaApi.createAndSignVisa(currentId, signatureBlob, 'Visa CEEP/Demandeur initial', 1);
      }

      // Soumettre l'AT au Responsable OCP
      await apiClient.post(`/autorisations-travail/${currentId}/submit`);

      alert('L\'Autorisation de Travail a été visée et transmise avec succès au Responsable OCP (Étape 3 terminée, statut AT_REDIGEE).');
      navigate('/autorisations');
    } catch (err: any) {
      console.error(err);
      alert(err.response?.data?.message || 'Erreur lors de la soumission.');
    } finally {
      setLoading(false);
    }
  };

  // Next Step Transition
  const handleNextStep = async () => {
    setLoading(true);
    try {
      if (activeStep === 0) {
        // Enregistrer la classification (§6)
        await apiClient.post('/classifications', {
          niveau: niveauClassification,
          estTiers,
          natureIntervention,
        });

        // Enregistrer le brouillon d'AT initial (§8.1)
        let res;
        if (selectedDoc) {
          res = await apiClient.post(`/documents/${docType}/${selectedDoc.id}/creer-at`);
        } else {
          res = await apiClient.post('/autorisations-travail');
        }
        setAtId(res.data.id);
        setFormInteractiveData((prev: any) => ({
          ...prev,
          numero: res.data.numero || 'AT-2026-XXXX',
          description: selectedDoc?.objet || selectedDoc?.description || prev.description || '',
          di: docType === 'DI' ? selectedDoc?.numero || '' : '',
          ot: docType === 'OT' ? selectedDoc?.numero || '' : '',
          bt: docType === 'BT' ? selectedDoc?.numero || '' : '',
        }));
      } else if (activeStep === 1) {
        // Étape 2 — Enregistrer la Visite Préalable (§8.2)
        const visiteRes = await apiClient.post('/visites-prealables', {
          documentSourceId: selectedDoc?.id,
          typeDocumentSource: selectedDoc ? docType : null,
          latitude: gpsCoords?.lat,
          longitude: gpsCoords?.lng,
          commentaire: visiteCommentaire,
        });
        const newVisiteId = visiteRes.data.id;
        setVisiteId(newVisiteId);

        if (photoFile) {
          const fd = new FormData();
          fd.append('file', photoFile);
          // Pas de Content-Type manuel → boundary correct pour Spring
          await apiClient.post(`/visites-prealables/${newVisiteId}/photos`, fd);
        }
      }
      setActiveStep((prev) => prev + 1);
    } catch (err: any) {
      console.error(err);
      alert(err.response?.data?.message || 'Erreur lors du passage à l\'étape suivante.');
    } finally {
      setLoading(false);
    }
  };

  const handleBackStep = () => setActiveStep((prev) => prev - 1);

  return (
    <Container maxWidth="lg" sx={{ py: 3 }}>
      {statusMsg && (
        <Alert severity="success" sx={{ mb: 2 }}>
          {statusMsg}
        </Alert>
      )}

      {/* HEADER PAGE */}
      <Paper elevation={1} sx={{ p: 2, mb: 3, bgcolor: '#0f172a', color: 'white', borderRadius: 2 }}>
        <Grid container spacing={2} sx={{ alignItems: 'center' }}>
          <Grid size={{ xs: 12, md: 8 }}>
            <Typography variant="h5" sx={{ fontWeight: 900, letterSpacing: -0.5 }}>
              Processus d'Autorisation de Travail (Standard OCP S-HSE-SEC-31)
            </Typography>
            <Typography variant="body2" sx={{ color: '#94a3b8', mt: 0.5 }}>
              Workflow réglementaire officiel : Classification (§6) &rarr; Demande (§8.1) &rarr; Visite Terrain (§8.2) &rarr; Rédaction AT (F-HSE-SEC-31-04)
            </Typography>
          </Grid>
          <Grid size={{ xs: 12, md: 4 }} sx={{ textAlign: { md: 'right' } }}>
            <Chip
              label={currentUser?.roles?.[0]?.nom || 'CEEP Exécutant'}
              color="success"
              sx={{ fontWeight: 800, fontSize: 12, px: 1 }}
            />
          </Grid>
        </Grid>
      </Paper>

      {/* STEPPER NAVIGATOR */}
      <Paper elevation={2} sx={{ p: 2, mb: 3, borderRadius: 2 }}>
        <Stepper activeStep={activeStep} alternativeLabel>
          {WORKFLOW_STEPS.map((label, idx) => (
            <Step key={label}>
              <StepLabel>
                <Typography variant="caption" sx={{ fontWeight: activeStep === idx ? 800 : 500 }}>
                  {label}
                </Typography>
              </StepLabel>
            </Step>
          ))}
        </Stepper>
      </Paper>

      {/* STEP CONTENT SWITCHER */}
      {activeStep === 0 && (
        /* ÉTAPE 0 & 1 : CLASSIFICATION ET DOCUMENT SOURCE */
        <Grid container spacing={3}>
          <Grid size={{ xs: 12, md: 6 }}>
            <Card elevation={3} sx={{ height: '100%', borderRadius: 2 }}>
              <CardContent>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2 }}>
                  <SecurityIcon color="primary" />
                  <Typography variant="h6" sx={{ fontWeight: 800 }}>
                    Étape 0 — Classification de l'intervention (§6)
                  </Typography>
                </Box>
                <Alert severity="info" sx={{ mb: 2 }}>
                  Selon §6 du Standard OCP S-HSE-SEC-31, le responsable HCEP classifie les interventions avant toute action.
                </Alert>

                <FormControl fullWidth margin="normal">
                  <InputLabel>Niveau de classification</InputLabel>
                  <Select
                    value={niveauClassification}
                    label="Niveau de classification"
                    onChange={(e) => setNiveauClassification(e.target.value as any)}
                  >
                    <MenuItem value="NIVEAU_1">
                      Niveau 1 — Interventions de routine / Pas d'AT (ADRPT / Plan de prév.)
                    </MenuItem>
                    <MenuItem value="NIVEAU_2">
                      Niveau 2 — AUTORISATION DE TRAVAIL OBLIGATOIRE
                    </MenuItem>
                  </Select>
                </FormControl>

                <FormControlLabel
                  control={
                    <Checkbox
                      checked={estTiers}
                      onChange={(e) => setEstTiers(e.target.checked)}
                      color="primary"
                    />
                  }
                  label="Intervention réalisée par une Entreprise Extérieure (Tiers)"
                  sx={{ mt: 1, display: 'block' }}
                />

                <TextField
                  fullWidth
                  label="Nature de l'intervention"
                  margin="normal"
                  multiline
                  rows={2}
                  value={natureIntervention}
                  onChange={(e) => setNatureIntervention(e.target.value)}
                />

                {niveauClassification === 'NIVEAU_1' && !estTiers && (
                  <Alert severity="warning" sx={{ mt: 2 }}>
                    <strong>Attention :</strong> Les interventions de Niveau 1 internes ne nécessitent pas d'Autorisation de Travail. Elles sont couvertes par l'ADRPT et la liste F-HSE-SEC-31-01.
                  </Alert>
                )}
              </CardContent>
            </Card>
          </Grid>

          <Grid size={{ xs: 12, md: 6 }}>
            <Card elevation={3} sx={{ height: '100%', borderRadius: 2 }}>
              <CardContent>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2 }}>
                  <AssignmentIcon color="success" />
                  <Typography variant="h6" sx={{ fontWeight: 800 }}>
                    Étape 1 — Demande d'intervention (§8.1)
                  </Typography>
                </Box>
                <Alert severity="success" sx={{ mb: 2 }}>
                  Associez un document d'accompagnement obligatoire : DI, OT, ou BT (§8.1).
                </Alert>

                <FormControl fullWidth margin="normal">
                  <InputLabel>Type de document source</InputLabel>
                  <Select
                    value={docType}
                    label="Type de document source"
                    onChange={(e) => setDocType(e.target.value as any)}
                  >
                    {DOCUMENT_TYPES.map((dt) => (
                      <MenuItem key={dt.value} value={dt.value}>
                        {dt.label}
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>

                <Typography variant="subtitle2" sx={{ mt: 2, mb: 1, fontWeight: 700 }}>
                  Sélectionner la Demande / Ordre / Bon de travail :
                </Typography>
                {loading ? (
                  <CircularProgress size={24} sx={{ display: 'block', my: 2 }} />
                ) : docList.length === 0 ? (
                  <Alert severity="info">Aucun document en attente. Une AT directe sera créée.</Alert>
                ) : (
                  <Box sx={{ maxHeight: 200, overflowY: 'auto', border: '1px solid #e2e8f0', borderRadius: 1 }}>
                    {docList.map((doc) => (
                      <Box
                        key={doc.id}
                        onClick={() => setSelectedDoc(doc)}
                        sx={{
                          p: 1.5,
                          borderBottom: '1px solid #f1f5f9',
                          cursor: 'pointer',
                          bgcolor: selectedDoc?.id === doc.id ? '#e0f2fe' : 'transparent',
                          '&:hover': { bgcolor: '#f8fafc' },
                        }}
                      >
                        <Typography variant="subtitle2" sx={{ fontWeight: 700, color: '#0284c7' }}>
                          {doc.numero} &mdash; {doc.objet || doc.description}
                        </Typography>
                        <Typography variant="caption" sx={{ color: '#64748b' }}>
                          Équipement: {doc.equipement?.nomEquipement || 'Secteur Chimie/Mine'}
                        </Typography>
                      </Box>
                    ))}
                  </Box>
                )}
              </CardContent>
            </Card>
          </Grid>
        </Grid>
      )}

      {activeStep === 1 && (
        /* ÉTAPE 2 : VISITE PRÉALABLE DU CHANTIER SUR LE TERRAIN (§8.2) */
        <Card elevation={3} sx={{ borderRadius: 2 }}>
          <CardContent>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2 }}>
              <ConstructionIcon color="warning" />
              <Typography variant="h6" sx={{ fontWeight: 800 }}>
                Étape 2 — Visite préalable conjointe du chantier (§8.2 Standard OCP)
              </Typography>
            </Box>

            <Alert severity="warning" sx={{ mb: 3 }}>
              <strong>Contrainte réglementaire §8.2 :</strong> La visite doit être réalisée conjointement sur le terrain par le <strong>CEEP</strong> et le <strong>CEEE</strong>. La géolocalisation et la photo de chantier sont obligatoires.
            </Alert>

            <Grid container spacing={3}>
              {/* Géolocalisation GPS */}
              <Grid size={{ xs: 12, md: 6 }}>
                <Paper variant="outlined" sx={{ p: 2, textAlign: 'center', bgcolor: '#fafafa' }}>
                  <Typography variant="subtitle1" sx={{ fontWeight: 800, mb: 1 }}>
                    1. Relevé des Coordonnées GPS (§8.2)
                  </Typography>
                  {gpsCoords ? (
                    <Chip
                      icon={<LocationOnIcon />}
                      label={`GPS: Lat ${gpsCoords.lat.toFixed(5)}, Lng ${gpsCoords.lng.toFixed(5)}`}
                      color="success"
                      sx={{ fontSize: 13, py: 2, px: 1, fontWeight: 700 }}
                    />
                  ) : (
                    <Button
                      variant="contained"
                      color="primary"
                      startIcon={<LocationOnIcon />}
                      onClick={handleGetGps}
                      disabled={gpsLoading}
                    >
                      {gpsLoading ? 'Acquisition GPS...' : 'Relever ma position GPS sur le chantier'}
                    </Button>
                  )}
                  {gpsError && (
                    <Typography variant="caption" color="error" sx={{ display: 'block', mt: 1 }}>
                      {gpsError}
                    </Typography>
                  )}
                </Paper>
              </Grid>

              {/* Photo du Chantier */}
              <Grid size={{ xs: 12, md: 6 }}>
                <Paper variant="outlined" sx={{ p: 2, textAlign: 'center', bgcolor: '#fafafa' }}>
                  <Typography variant="subtitle1" sx={{ fontWeight: 800, mb: 1 }}>
                    2. Photo d'Inspections du Chantier (§8.2)
                  </Typography>
                  <input
                    type="file"
                    accept="image/*"
                    capture="environment"
                    ref={photoInputRef}
                    onChange={handlePhotoChange}
                    style={{ display: 'none' }}
                  />
                  {photoFile ? (
                    <Chip
                      icon={<CheckCircleIcon />}
                      label={`Photo enregistrée : ${photoFile.name}`}
                      color="success"
                      sx={{ fontSize: 13, py: 2, px: 1, fontWeight: 700 }}
                    />
                  ) : (
                    <Button
                      variant="contained"
                      color="secondary"
                      startIcon={<CameraAltIcon />}
                      onClick={() => photoInputRef.current?.click()}
                    >
                      Prendre une photo du chantier
                    </Button>
                  )}
                </Paper>
              </Grid>

              {/* Commentaires Visite */}
              <Grid size={12}>
                <TextField
                  fullWidth
                  multiline
                  rows={3}
                  label="Constats et remarques de la visite préalable (Zone d'intervention, risques résiduels)"
                  value={visiteCommentaire}
                  onChange={(e) => setVisiteCommentaire(e.target.value)}
                  placeholder="Décrire les constats effectués sur le terrain..."
                />
              </Grid>

              {/* Point de contrôle (Gateway) — Prévention en place ? */}
              <Grid size={12}>
                <Paper sx={{ p: 2, bgcolor: preventionEnPlace ? '#f0fdf4' : '#fff1f2', border: `2px solid ${preventionEnPlace ? '#16a34a' : '#e11d48'}` }}>
                  <FormControlLabel
                    control={
                      <Checkbox
                        checked={preventionEnPlace}
                        onChange={(e) => setPreventionEnPlace(e.target.checked)}
                        color="success"
                      />
                    }
                    label={
                      <Typography variant="subtitle1" sx={{ fontWeight: 900, color: preventionEnPlace ? '#15803d' : '#be123c' }}>
                        Point de contrôle obligatoire (§8.2) : « Toutes les actions de prévention et mesures de protection sont-elles mises en place sur le chantier ? »
                      </Typography>
                    }
                  />
                  {!preventionEnPlace && (
                    <Typography variant="body2" sx={{ color: '#be123c', mt: 0.5, fontStyle: 'italic', ml: 4 }}>
                      ⚠️ Conformément au standard S-HSE-SEC-31, la rédaction de l'AT ne peut pas démarrer tant que les mesures de prévention ne sont pas en place.
                    </Typography>
                  )}
                </Paper>
              </Grid>
            </Grid>
          </CardContent>
        </Card>
      )}

      {activeStep === 2 && (
        /* ÉTAPE 3 : RÉDACTION DU FORMULAIRE F-HSE-SEC-31-04 SUR LE TERRAIN */
        <Box>
          <Alert severity="info" sx={{ mb: 2 }}>
            <strong>Étape 3 — Rédaction de l'AT sur le terrain (F-HSE-SEC-31-04) :</strong> Complétez l'ensemble des sections A à G. Cliquez sur les cases de signature du Visa CEEP/CEEE pour ajouter votre signature manuscrite.
          </Alert>

          <FormulaireOCPInteractive
            initialData={formInteractiveData}
            readOnly={false}
            onSave={handleSaveDraft}
            onSubmitAT={handleSubmitAT}
            loading={loading}
          />
        </Box>
      )}

      {activeStep === 3 && (
        /* STEP 4 : VALIDATION & FINALISATION */
        <Card elevation={3} sx={{ borderRadius: 2, textAlign: 'center', p: 4 }}>
          <CheckCircleIcon sx={{ fontSize: 64, color: '#16a34a', mb: 2 }} />
          <Typography variant="h5" sx={{ fontWeight: 900, mb: 1 }}>
            Formulaire F-HSE-SEC-31-04 Prêt pour Signature et Transmission
          </Typography>
          <Typography variant="body1" sx={{ color: '#64748b', mb: 3 }}>
            L'Autorisation de Travail a été rédigée sur le terrain. Cliquez sur le bouton ci-dessous pour transmettre officiellement l'AT au Responsable OCP.
          </Typography>

          <Button
            variant="contained"
            color="success"
            size="large"
            startIcon={<SendIcon />}
            onClick={() => handleSubmitAT(formInteractiveData)}
            disabled={loading}
            sx={{ px: 4, py: 1.5, fontWeight: 800 }}
          >
            {loading ? 'Transmission en cours...' : 'Transmettre l\'AT au Responsable OCP'}
          </Button>
        </Card>
      )}

      {/* FOOTER ACTIONS NAVIGATION */}
      {activeStep < 2 && (
        <Box sx={{ display: 'flex', justifyContent: 'space-between', mt: 3 }}>
          <Button disabled={activeStep === 0 || loading} onClick={handleBackStep}>
            Précédent
          </Button>
          <Button
            variant="contained"
            color="primary"
            onClick={handleNextStep}
            disabled={!canGoNext() || loading}
          >
            {loading ? 'Chargement...' : 'Étape suivante'}
          </Button>
        </Box>
      )}
    </Container>
  );
}
