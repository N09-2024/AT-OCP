import React, { useState, useEffect, useRef } from 'react';
import {
  Box, Typography, Paper, Button, Stepper, Step, StepLabel,
  CircularProgress, TextField, Grid, Checkbox, FormControlLabel,
  FormGroup, ToggleButton, ToggleButtonGroup, Chip, Alert,
  LinearProgress, Divider
} from '@mui/material';
import LocationOnIcon from '@mui/icons-material/LocationOn';
import CameraAltIcon from '@mui/icons-material/CameraAlt';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import ErrorIcon from '@mui/icons-material/Error';
import UploadFileIcon from '@mui/icons-material/UploadFile';
import { useNavigate } from 'react-router-dom';
import { apiClient } from '../../../services/apiClient';

const DOCUMENT_TYPES = [
  { value: 'DI', label: "Demande d'Intervention", endpoint: '/demandes-intervention' },
  { value: 'OT', label: 'Ordre de Travail', endpoint: '/ordres-travail' },
  { value: 'BT', label: 'Bon de Travail', endpoint: '/bons-travail' },
];

const STEPS = [
  'Document source',
  'Visite préalable',
  'Informations',
  'A - Risques',
  'B - Mesures',
  'C - Accès',
  'D - EPI',
  'E - Permis',
  'F - Validation',
];

export default function AutorisationFormPage() {
  const navigate = useNavigate();
  const photoInputRef = useRef<HTMLInputElement>(null);
  const [activeStep, setActiveStep] = useState(0);
  const [loading, setLoading] = useState(false);

  // Step 0 — Document Source
  const [docType, setDocType] = useState<string>('DI');
  const [docList, setDocList] = useState<any[]>([]);
  const [selectedDoc, setSelectedDoc] = useState<any | null>(null);

  // Step 1 — Visite Préalable
  const [visiteId, setVisiteId] = useState<string | null>(null);
  const [gpsCoords, setGpsCoords] = useState<{ lat: number; lng: number } | null>(null);
  const [gpsLoading, setGpsLoading] = useState(false);
  const [gpsError, setGpsError] = useState<string | null>(null);
  const [photoFile, setPhotoFile] = useState<File | null>(null);
  const [photoUploaded, setPhotoUploaded] = useState(false);
  const [visiteCommentaire, setVisiteCommentaire] = useState('');

  // AT Draft
  const [createdAtId, setCreatedAtId] = useState<string | null>(null);

  // Form State
  const [formData, setFormData] = useState({
    objet: '',
    descriptionTravaux: '',
    dateDebut: '',
    dateFin: '',
    heureDebut: '',
    heureFin: '',
    servicesIntervenants: '',
    entreprisesIntervenantes: '',
    mesuresSecuriteExecutant: '',
    risquesIds: [] as string[],
    mesuresIds: [] as string[],
    episIds: [] as string[],
    moyensAccesIds: [] as string[],
    permisIds: [] as string[],
  });

  // Referentials
  const [refRisques, setRefRisques] = useState<any[]>([]);
  const [refMesures, setRefMesures] = useState<any[]>([]);
  const [refEpis, setRefEpis] = useState<any[]>([]);
  const [refMoyens, setRefMoyens] = useState<any[]>([]);
  const [refPermis, setRefPermis] = useState<any[]>([]);

  // Permis analysis state
  const [permisState, setPermisState] = useState<Record<string, {
    uploading: boolean;
    analysing: boolean;
    resultat: string | null;
    permisEntityId: string | null;
  }>>({});

  // Load document list when docType changes
  useEffect(() => {
    const found = DOCUMENT_TYPES.find(d => d.value === docType);
    if (!found) return;
    setLoading(true);
    setSelectedDoc(null);
    setDocList([]);
    apiClient.get(`${found.endpoint}?page=0&size=100`)
      .then(res => setDocList(res.data.content || []))
      .catch(() => setDocList([]))
      .finally(() => setLoading(false));
  }, [docType]);

  // Load referentials once
  useEffect(() => {
    apiClient.get('/risques?size=100').then(res => setRefRisques(res.data.content || []));
    apiClient.get('/mesures-preparation?size=100').then(res => setRefMesures(res.data.content || []));
    apiClient.get('/epis?size=100').then(res => setRefEpis(res.data.content || []));
    apiClient.get('/moyens-acces?size=100').then(res => setRefMoyens(res.data.content || []));
    apiClient.get('/types-permis?size=100').then(res => setRefPermis(res.data.content || []));
  }, []);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    setFormData(prev => ({ ...prev, [e.target.name]: e.target.value }));
  };

  const handleCheckboxChange = (
    field: 'risquesIds' | 'mesuresIds' | 'episIds' | 'moyensAccesIds' | 'permisIds',
    id: string
  ) => {
    setFormData(prev => {
      const list = prev[field];
      return list.includes(id)
        ? { ...prev, [field]: list.filter(i => i !== id) }
        : { ...prev, [field]: [...list, id] };
    });
  };

  const handleGetGps = () => {
    setGpsLoading(true);
    setGpsError(null);
    navigator.geolocation.getCurrentPosition(
      pos => {
        setGpsCoords({ lat: pos.coords.latitude, lng: pos.coords.longitude });
        setGpsLoading(false);
      },
      () => {
        setGpsError("Impossible de récupérer la localisation. Veuillez autoriser l'accès GPS.");
        setGpsLoading(false);
      }
    );
  };

  const handlePhotoChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0] ?? null;
    setPhotoFile(file);
    setPhotoUploaded(false);
  };

  const handlePermisUpload = async (typePermisId: string, file: File) => {
    if (!createdAtId) return;
    setPermisState(prev => ({ ...prev, [typePermisId]: { uploading: true, analysing: false, resultat: null, permisEntityId: null } }));
    try {
      const permisListRes = await apiClient.get(`/permis/at/${createdAtId}`);
      const permisList: any[] = permisListRes.data || [];
      const permisEntity = permisList.find((p: any) => p.typePermis?.id === typePermisId);
      if (!permisEntity) {
        alert('Veuillez sauvegarder avant d\'uploader un permis.');
        setPermisState(prev => ({ ...prev, [typePermisId]: { uploading: false, analysing: false, resultat: null, permisEntityId: null } }));
        return;
      }
      const fd = new FormData();
      fd.append('file', file);
      await apiClient.post(`/permis/${permisEntity.id}/upload`, fd, {
        headers: { 'Content-Type': 'multipart/form-data' },
      });
      setPermisState(prev => ({ ...prev, [typePermisId]: { uploading: false, analysing: true, resultat: null, permisEntityId: permisEntity.id } }));
      const analyseRes = await apiClient.get(`/permis/${permisEntity.id}/analyse`);
      setPermisState(prev => ({
        ...prev,
        [typePermisId]: {
          uploading: false,
          analysing: false,
          resultat: analyseRes.data?.resultat ?? 'NON_CONFORME',
          permisEntityId: permisEntity.id,
        },
      }));
    } catch {
      setPermisState(prev => ({ ...prev, [typePermisId]: { uploading: false, analysing: false, resultat: 'ERREUR', permisEntityId: null } }));
    }
  };

  const canGoNext = (): boolean => {
    if (activeStep === 1) return !!gpsCoords && !!photoFile;
    return true;
  };

  const handleNext = async () => {
    setLoading(true);
    try {
      if (activeStep === 0) {
        let res;
        if (selectedDoc) {
          res = await apiClient.post(`/documents/${docType}/${selectedDoc.id}/creer-at`);
        } else {
          res = await apiClient.post('/autorisations-travail');
        }
        setCreatedAtId(res.data.id);
        setFormData(prev => ({
          ...prev,
          objet: res.data.objet || selectedDoc?.objet || selectedDoc?.titre || '',
          descriptionTravaux: res.data.descriptionTravaux || selectedDoc?.description || '',
        }));
      } else if (activeStep === 1) {
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
          await apiClient.post(`/visites-prealables/${newVisiteId}/photos`, fd, {
            headers: { 'Content-Type': 'multipart/form-data' },
          });
          setPhotoUploaded(true);
        }
      } else if (activeStep > 1 && activeStep < STEPS.length - 1 && createdAtId) {
        await apiClient.put(`/autorisations-travail/${createdAtId}/autosave`, formData);
      } else if (activeStep === STEPS.length - 1 && createdAtId) {
        await apiClient.put(`/autorisations-travail/${createdAtId}/autosave`, formData);
        await apiClient.post(`/autorisations-travail/${createdAtId}/submit`);
        navigate('/autorisations');
        return;
      }
      setActiveStep(prev => prev + 1);
    } catch (err) {
      console.error(err);
      alert("Une erreur s'est produite. Veuillez réessayer.");
    } finally {
      setLoading(false);
    }
  };

  const handleBack = () => setActiveStep(prev => prev - 1);

  const renderCheckboxGrid = (
    items: any[],
    field: 'risquesIds' | 'mesuresIds' | 'episIds' | 'moyensAccesIds' | 'permisIds',
    labelKey: string,
    emptyMsg: string
  ) => (
    <Grid container spacing={1}>
      {items.length === 0 ? (
        <Grid size={12}>
          <Alert severity="info">{emptyMsg}</Alert>
        </Grid>
      ) : items.map(item => (
        <Grid size={{ xs: 12, sm: 6, md: 4 }} key={item.id}>
          <FormControlLabel
            control={
              <Checkbox
                checked={(formData[field] as string[]).includes(item.id)}
                onChange={() => handleCheckboxChange(field, item.id)}
              />
            }
            label={item[labelKey] || item.nom || item.id}
          />
        </Grid>
      ))}
    </Grid>
  );

  const renderStep = (step: number) => {
    switch (step) {
      case 0:
        return (
          <Box>
            <Typography variant="h6" sx={{ mb: 1 }}>Type de document source</Typography>
            <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
              Choisissez le type de document à l'origine de l'AT, puis sélectionnez le document (optionnel).
            </Typography>
            <ToggleButtonGroup
              value={docType}
              exclusive
              onChange={(_, v) => v && setDocType(v)}
              sx={{ mb: 3 }}
            >
              {DOCUMENT_TYPES.map(dt => (
                <ToggleButton key={dt.value} value={dt.value} sx={{ px: 4, py: 1.5, fontWeight: 700 }}>
                  {dt.value} — {dt.label}
                </ToggleButton>
              ))}
            </ToggleButtonGroup>

            <Typography variant="subtitle1" sx={{ mb: 1, fontWeight: 700 }}>
              Sélectionner un(e) {DOCUMENT_TYPES.find(d => d.value === docType)?.label}{' '}
              <Typography component="span" variant="body2" color="text.secondary">(optionnel)</Typography>
            </Typography>

            {loading ? <CircularProgress /> : (
              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1.5, maxHeight: 400, overflowY: 'auto' }}>
                {docList.length === 0 ? (
                  <Alert severity="info">Aucun document de type {docType} trouvé.</Alert>
                ) : docList.map(doc => (
                  <Paper
                    key={doc.id}
                    elevation={selectedDoc?.id === doc.id ? 4 : 1}
                    sx={{
                      p: 2,
                      cursor: 'pointer',
                      border: selectedDoc?.id === doc.id ? '2px solid #0891b2' : '1px solid #e2e8f0',
                      transition: 'all 0.2s',
                      '&:hover': { borderColor: '#0891b2', bgcolor: '#f0f9ff' },
                    }}
                    onClick={() => setSelectedDoc((prev: any) => prev?.id === doc.id ? null : doc)}
                  >
                    <Typography variant="subtitle2" sx={{ fontWeight: 700 }}>
                      {doc.numero || doc.reference || doc.id} — {doc.objet || doc.titre || doc.libelle}
                    </Typography>
                    <Typography variant="caption" color="text.secondary">
                      Statut: {doc.statut} | {doc.dateCreation ? new Date(doc.dateCreation).toLocaleDateString('fr-FR') : ''}
                    </Typography>
                  </Paper>
                ))}
              </Box>
            )}
            <Alert severity="info" sx={{ mt: 2 }}>
              La sélection d'un document est optionnelle. Vous pouvez créer une AT sans document source.
            </Alert>
          </Box>
        );

      case 1:
        return (
          <Box>
            <Alert severity="warning" sx={{ mb: 3 }}>
              La visite préalable sur le chantier est <strong>obligatoire</strong> avant de passer à l'étape suivante.
            </Alert>
            <Grid container spacing={3}>
              <Grid size={12}>
                <Typography variant="h6" gutterBottom>📍 Localisation GPS du chantier</Typography>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, flexWrap: 'wrap' }}>
                  <Button
                    variant="outlined"
                    startIcon={gpsLoading ? <CircularProgress size={18} /> : <LocationOnIcon />}
                    onClick={handleGetGps}
                    disabled={gpsLoading}
                    color={gpsCoords ? 'success' : 'primary'}
                  >
                    {gpsCoords ? 'Position enregistrée ✓' : 'Obtenir ma position GPS'}
                  </Button>
                  {gpsCoords && (
                    <Chip
                      icon={<CheckCircleIcon />}
                      color="success"
                      label={`Lat: ${gpsCoords.lat.toFixed(5)}, Lng: ${gpsCoords.lng.toFixed(5)}`}
                    />
                  )}
                </Box>
                {gpsError && <Alert severity="error" sx={{ mt: 1 }}>{gpsError}</Alert>}
              </Grid>

              <Grid size={12}>
                <Divider sx={{ mb: 2 }} />
                <Typography variant="h6" gutterBottom>📷 Photo du chantier</Typography>
                <input
                  ref={photoInputRef}
                  type="file"
                  accept="image/*"
                  style={{ display: 'none' }}
                  onChange={handlePhotoChange}
                />
                <Button
                  variant="outlined"
                  startIcon={<CameraAltIcon />}
                  onClick={() => photoInputRef.current?.click()}
                  color={photoFile ? 'success' : 'primary'}
                >
                  {photoFile ? `Photo sélectionnée : ${photoFile.name}` : 'Photographier / Importer une photo'}
                </Button>
                {photoFile && (
                  <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mt: 1 }}>
                    La photo sera envoyée lors du clic sur "Suivant".
                  </Typography>
                )}
              </Grid>

              <Grid size={12}>
                <Divider sx={{ mb: 2 }} />
                <TextField
                  fullWidth
                  multiline
                  rows={3}
                  label="Observations de la visite (optionnel)"
                  value={visiteCommentaire}
                  onChange={e => setVisiteCommentaire(e.target.value)}
                />
              </Grid>
            </Grid>
          </Box>
        );

      case 2:
        return (
          <Grid container spacing={3}>
            <Grid size={12}>
              <Typography variant="h6">Informations de l'intervention</Typography>
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField fullWidth label="Objet" name="objet" value={formData.objet} onChange={handleChange} required />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                fullWidth
                label="Lieu / Équipement"
                value={selectedDoc?.equipement?.nomEquipement || selectedDoc?.installation?.nomInstallation || ''}
                disabled
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField fullWidth label="Services intervenants" name="servicesIntervenants" value={formData.servicesIntervenants} onChange={handleChange} />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField fullWidth label="Entreprises intervenantes" name="entreprisesIntervenantes" value={formData.entreprisesIntervenantes} onChange={handleChange} />
            </Grid>
            <Grid size={12}>
              <TextField fullWidth multiline rows={3} label="Description de l'intervention" name="descriptionTravaux" value={formData.descriptionTravaux} onChange={handleChange} />
            </Grid>
            <Grid size={{ xs: 12, sm: 3 }}>
              <TextField fullWidth type="date" label="Date début" name="dateDebut" slotProps={{ inputLabel: { shrink: true } }} value={formData.dateDebut} onChange={handleChange} />
            </Grid>
            <Grid size={{ xs: 12, sm: 3 }}>
              <TextField fullWidth type="time" label="Heure début" name="heureDebut" slotProps={{ inputLabel: { shrink: true } }} value={formData.heureDebut} onChange={handleChange} />
            </Grid>
            <Grid size={{ xs: 12, sm: 3 }}>
              <TextField fullWidth type="date" label="Date fin" name="dateFin" slotProps={{ inputLabel: { shrink: true } }} value={formData.dateFin} onChange={handleChange} />
            </Grid>
            <Grid size={{ xs: 12, sm: 3 }}>
              <TextField fullWidth type="time" label="Heure fin" name="heureFin" slotProps={{ inputLabel: { shrink: true } }} value={formData.heureFin} onChange={handleChange} />
            </Grid>
          </Grid>
        );

      case 3:
        return (
          <Box>
            <Typography variant="h6" sx={{ mb: 2 }}>A - Risques évalués liés à / au chantier</Typography>
            <FormGroup>
              {renderCheckboxGrid(refRisques, 'risquesIds', 'nomRisque', 'Aucun risque configuré dans le référentiel.')}
            </FormGroup>
          </Box>
        );

      case 4:
        return (
          <Box>
            <Typography variant="h6" sx={{ mb: 2 }}>B - Mesures prises pour préparer l'intervention</Typography>
            <FormGroup>
              {renderCheckboxGrid(refMesures, 'mesuresIds', 'nomMesure', 'Aucune mesure configurée dans le référentiel.')}
            </FormGroup>
          </Box>
        );

      case 5:
        return (
          <Box>
            <Typography variant="h6" sx={{ mb: 2 }}>C - Moyens d'accès nécessaires</Typography>
            <FormGroup>
              {renderCheckboxGrid(refMoyens, 'moyensAccesIds', 'nomMoyen', "Aucun moyen d'accès configuré.")}
            </FormGroup>
          </Box>
        );

      case 6:
        return (
          <Box>
            <Typography variant="h6" sx={{ mb: 2 }}>D - EPI spécifiques nécessaires</Typography>
            <FormGroup>
              {renderCheckboxGrid(refEpis, 'episIds', 'nomEPI', 'Aucun EPI configuré.')}
            </FormGroup>
          </Box>
        );

      case 7:
        return (
          <Box>
            <Typography variant="h6" sx={{ mb: 1 }}>E - Permis spécifiques nécessaires</Typography>
            <Alert severity="info" sx={{ mb: 3 }}>
              Pour chaque permis requis : (1) cochez-le, (2) importez le document officiel, (3) l'IA vérifiera sa conformité.
            </Alert>
            {refPermis.length === 0
              ? <Alert severity="info">Aucun type de permis configuré dans le référentiel.</Alert>
              : refPermis.map(p => {
                const state = permisState[p.id] ?? { uploading: false, analysing: false, resultat: null, permisEntityId: null };
                const isSelected = formData.permisIds.includes(p.id);
                return (
                  <Paper key={p.id} sx={{ p: 2, mb: 2, border: isSelected ? '1px solid #0891b2' : '1px solid #e2e8f0' }}>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, flexWrap: 'wrap' }}>
                      <FormControlLabel
                        control={<Checkbox checked={isSelected} onChange={() => handleCheckboxChange('permisIds', p.id)} />}
                        label={<Typography sx={{ fontWeight: 700 }}>{p.nom}</Typography>}
                      />
                      {isSelected && (
                        <>
                          <input
                            type="file"
                            accept="image/*,application/pdf"
                            id={`upload-${p.id}`}
                            style={{ display: 'none' }}
                            onChange={e => {
                              const file = e.target.files?.[0];
                              if (file) handlePermisUpload(p.id, file);
                            }}
                          />
                          <label htmlFor={`upload-${p.id}`}>
                            <Button
                              component="span"
                              variant="outlined"
                              size="small"
                              startIcon={state.uploading ? <CircularProgress size={16} /> : <UploadFileIcon />}
                              disabled={state.uploading || state.analysing}
                            >
                              {state.uploading ? 'Envoi...' : 'Importer / Photographier'}
                            </Button>
                          </label>
                          {state.analysing && (
                            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                              <CircularProgress size={18} />
                              <Typography variant="caption">Analyse IA en cours...</Typography>
                            </Box>
                          )}
                          {state.resultat && (
                            <Chip
                              icon={state.resultat === 'CONFORME' ? <CheckCircleIcon /> : <ErrorIcon />}
                              label={state.resultat === 'CONFORME' ? 'Conforme ✓' : state.resultat === 'NON_CONFORME' ? 'Non Conforme ✗' : 'Erreur'}
                              color={state.resultat === 'CONFORME' ? 'success' : 'error'}
                            />
                          )}
                        </>
                      )}
                    </Box>
                    {state.analysing && <LinearProgress sx={{ mt: 1 }} />}
                  </Paper>
                );
              })}
          </Box>
        );

      case 8:
        return (
          <Grid container spacing={3}>
            <Grid size={12}>
              <Typography variant="h6">F - Mesures de sécurité prises par l'exécutant</Typography>
              <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                (Référence du mode opératoire, consignes particulières...)
              </Typography>
              <TextField
                fullWidth
                multiline
                rows={4}
                label="Mesures de sécurité"
                name="mesuresSecuriteExecutant"
                value={formData.mesuresSecuriteExecutant}
                onChange={handleChange}
              />
            </Grid>
            <Grid size={12}>
              <Paper sx={{ p: 3, bgcolor: '#f0fdf4', border: '1px solid #bbf7d0' }}>
                <Typography variant="h6" color="#166534" gutterBottom>✅ Prêt à soumettre</Typography>
                <Typography variant="body2" color="#15803d" sx={{ mb: 2 }}>
                  En soumettant cette AT, elle sera transmise au responsable OCP (CEEP/CEEE) pour validation.
                </Typography>
                <Box sx={{ display: 'flex', flexDirection: 'column', gap: 0.5 }}>
                  <Typography variant="caption">
                    📋 Document source : <strong>{selectedDoc ? `${docType} — ${selectedDoc.numero || selectedDoc.reference}` : 'Aucun'}</strong>
                  </Typography>
                  <Typography variant="caption">
                    🗺️ Visite préalable : <strong>{visiteId ? 'Enregistrée ✓' : 'Non enregistrée'}</strong>
                  </Typography>
                  <Typography variant="caption">
                    📌 Risques identifiés : <strong>{formData.risquesIds.length}</strong>
                  </Typography>
                  <Typography variant="caption">
                    🦺 EPI requis : <strong>{formData.episIds.length}</strong>
                  </Typography>
                  <Typography variant="caption">
                    📄 Permis nécessaires : <strong>{formData.permisIds.length}</strong>
                  </Typography>
                </Box>
              </Paper>
            </Grid>
          </Grid>
        );

      default:
        return null;
    }
  };

  return (
    <Box sx={{ p: 3, maxWidth: 1100, mx: 'auto' }}>
      <Typography variant="h4" sx={{ mb: 1, fontWeight: 800 }}>Nouvelle Autorisation de Travail</Typography>
      <Typography variant="body2" color="text.secondary" sx={{ mb: 4 }}>
        Suivez les étapes ci-dessous pour créer votre AT.
      </Typography>

      <Stepper activeStep={activeStep} alternativeLabel sx={{ mb: 4 }}>
        {STEPS.map(label => (
          <Step key={label}>
            <StepLabel>{label}</StepLabel>
          </Step>
        ))}
      </Stepper>

      <Paper sx={{ p: 4, minHeight: 300 }}>
        {renderStep(activeStep)}

        <Box sx={{ display: 'flex', justifyContent: 'space-between', mt: 4 }}>
          <Button onClick={handleBack} disabled={activeStep === 0}>
            ← Retour
          </Button>
          <Button
            variant="contained"
            onClick={handleNext}
            disabled={!canGoNext() || loading}
            size="large"
          >
            {loading
              ? <CircularProgress size={22} color="inherit" />
              : activeStep === STEPS.length - 1
                ? "✅ Soumettre l'AT"
                : 'Suivant →'}
          </Button>
        </Box>
      </Paper>
    </Box>
  );
}
