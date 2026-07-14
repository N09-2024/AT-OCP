import { useEffect, useState, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Box, Typography, Paper, Divider, Button, CircularProgress,
  Alert, Chip
} from '@mui/material';
import { PermisService } from '../../../services/PermisService';
import type { PermisResponse, AnalyseIAResponse } from '../../../services/PermisService';
import { ArrowLeftIcon, CloudArrowUpIcon, CameraIcon, ArrowPathIcon } from '@heroicons/react/24/outline';
import { useAuthStore } from '../../../store/authStore';

export default function PermisDetailsPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const hasRole = useAuthStore((s) => s.hasRole);

  const [permis, setPermis] = useState<PermisResponse | null>(null);
  const [analyse, setAnalyse] = useState<AnalyseIAResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const fileInputRef = useRef<HTMLInputElement>(null);
  const cameraInputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    if (id) {
      loadData(id);
    }
  }, [id]);

  const loadData = async (permisId: string) => {
    try {
      setLoading(true);
      const permisData = await PermisService.getPermisById(permisId);
      setPermis(permisData);

      if (permisData.analyseIAId || permisData.fichierJointId) {
        const analyseData = await PermisService.getAnalyse(permisId);
        setAnalyse(analyseData);
      }
    } catch (err) {
      setError('Erreur lors du chargement des données.');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleFileUpload = async (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (!file || !id) return;

    try {
      setUploading(true);
      setError(null);
      await PermisService.uploadFichier(id, file);
      await loadData(id);
    } catch (err) {
      setError('Erreur lors du téléchargement ou de l\'analyse.');
      console.error(err);
    } finally {
      setUploading(false);
      // Reset input
      if (event.target) event.target.value = '';
    }
  };

  const handleReanalyse = async () => {
    if (!id) return;
    try {
      setUploading(true);
      setError(null);
      await PermisService.reanalyserPermis(id);
      await loadData(id);
    } catch (err) {
      setError('Erreur lors de la réanalyse.');
      console.error(err);
    } finally {
      setUploading(false);
    }
  };

  const downloadFile = async () => {
    if (!id || !permis?.fichierJointNom) return;
    try {
      const blob = await PermisService.downloadFichier(id);
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = permis.fichierJointNom;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
    } catch (err) {
      setError('Erreur lors du téléchargement du fichier.');
    }
  };

  if (loading) {
    return <Box sx={{ p: 3, display: 'flex', justifyContent: 'center' }}><CircularProgress /></Box>;
  }

  if (!permis) {
    return <Box sx={{ p: 3 }}><Alert severity="error">Permis introuvable</Alert></Box>;
  }

  return (
    <Box sx={{ p: 3 }}>
      {/* Hidden Inputs */}
      <input
        type="file"
        ref={fileInputRef}
        style={{ display: 'none' }}
        accept="image/png, image/jpeg, image/webp, application/pdf"
        onChange={handleFileUpload}
      />
      <input
        type="file"
        ref={cameraInputRef}
        style={{ display: 'none' }}
        accept="image/png, image/jpeg, image/webp"
        capture="environment"
        onChange={handleFileUpload}
      />

      <Box sx={{ display: 'flex', alignItems: 'center', mb: 3, gap: 2 }}>
        <Button startIcon={<ArrowLeftIcon width={20} />} onClick={() => navigate(-1)}>
          Retour
        </Button>
        <Typography variant="h5" sx={{ fontWeight: 'bold' }}>Détails du Permis #{permis.numero}</Typography>
      </Box>

      {error && <Alert severity="error" sx={{ mb: 3 }}>{error}</Alert>}

      <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', md: '1fr 1fr' }, gap: 3 }}>
        <Box>
          <Paper sx={{ p: 3, border: '1px solid', borderColor: 'divider', elevation: 0 }}>
            <Typography variant="h6" sx={{ mb: 2 }}>Informations Générales</Typography>
            <Box sx={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 2 }}>
              <Box>
                <Typography color="text.secondary" variant="body2">Type</Typography>
                <Typography sx={{ fontWeight: 'medium' }}>{permis.type}</Typography>
              </Box>
              <Box>
                <Typography color="text.secondary" variant="body2">Statut IA</Typography>
                <Chip label={permis.statutVerification} size="small" />
              </Box>
              <Box>
                <Typography color="text.secondary" variant="body2">Date d'émission</Typography>
                <Typography>{new Date(permis.dateEmission).toLocaleDateString()}</Typography>
              </Box>
              <Box>
                <Typography color="text.secondary" variant="body2">Date d'expiration</Typography>
                <Typography>{new Date(permis.dateExpiration).toLocaleDateString()}</Typography>
              </Box>
            </Box>
          </Paper>
        </Box>

        <Box>
          <Paper sx={{ p: 3, border: '1px solid', borderColor: 'divider', elevation: 0 }}>
            <Typography variant="h6" sx={{ mb: 2 }}>Document & Analyse IA</Typography>
            
            {permis.fichierJointNom ? (
              <Box sx={{ mb: 3 }}>
                <Typography color="text.secondary" variant="body2">Fichier actuel</Typography>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mt: 1 }}>
                  <Typography sx={{ fontWeight: 'medium' }}>{permis.fichierJointNom}</Typography>
                  <Button size="small" onClick={downloadFile}>Télécharger</Button>
                </Box>
              </Box>
            ) : (
              <Alert severity="info" sx={{ mb: 3 }}>Aucun document n'a été importé.</Alert>
            )}

            <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap' }}>
              {hasRole('UPLOAD_PERMIS') && (
                <>
                  <Button
                    variant="contained"
                    startIcon={<CloudArrowUpIcon width={20} />}
                    onClick={() => fileInputRef.current?.click()}
                    disabled={uploading}
                  >
                    Importer un permis
                  </Button>
                  <Button
                    variant="outlined"
                    startIcon={<CameraIcon width={20} />}
                    onClick={() => cameraInputRef.current?.click()}
                    disabled={uploading}
                  >
                    Photographier
                  </Button>
                </>
              )}
              {hasRole('ANALYSE_PERMIS') && permis.fichierJointId && (
                <Button
                  variant="outlined"
                  color="secondary"
                  startIcon={<ArrowPathIcon width={20} />}
                  onClick={handleReanalyse}
                  disabled={uploading}
                >
                  Relancer l'Analyse
                </Button>
              )}
            </Box>
            {uploading && <CircularProgress size={24} sx={{ mt: 2 }} />}
          </Paper>
        </Box>

        {analyse && (
          <Box sx={{ gridColumn: '1 / -1' }}>
            <Paper sx={{ p: 3, border: '1px solid', borderColor: 'divider', elevation: 0 }}>
              <Typography variant="h6" sx={{ mb: 2 }}>Résultats de l'Analyse IA</Typography>
              <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', md: 'repeat(3, 1fr)' }, gap: 2 }}>
                <Box>
                  <Typography color="text.secondary" variant="body2">Taux de Confiance</Typography>
                  <Typography variant="h6" color={analyse.tauxConfiance > 0.8 ? 'success.main' : 'warning.main'}>
                    {(analyse.tauxConfiance * 100).toFixed(1)}%
                  </Typography>
                </Box>
                <Box>
                  <Typography color="text.secondary" variant="body2">Date d'Analyse</Typography>
                  <Typography>{new Date(analyse.dateAnalyse).toLocaleString()}</Typography>
                </Box>
                <Box>
                  <Typography color="text.secondary" variant="body2">Résultat</Typography>
                  <Typography sx={{ fontWeight: 'medium' }}>{analyse.resultat}</Typography>
                </Box>
                
                <Box sx={{ gridColumn: '1 / -1' }}>
                  <Divider sx={{ my: 2 }} />
                  <Typography color="text.secondary" variant="body2" sx={{ mb: 1 }}>Commentaire IA</Typography>
                  <Typography>{analyse.commentaireIA || 'Aucun commentaire'}</Typography>
                </Box>

                <Box sx={{ gridColumn: '1 / -1' }}>
                  <Typography color="text.secondary" variant="body2" sx={{ mb: 1, mt: 2 }}>Texte Extrait (OCR)</Typography>
                  <Paper sx={{ p: 2, bgcolor: 'grey.50', maxHeight: 200, overflow: 'auto' }}>
                    <pre style={{ margin: 0, whiteSpace: 'pre-wrap', fontFamily: 'inherit', fontSize: '0.875rem' }}>
                      {analyse.ocrText}
                    </pre>
                  </Paper>
                </Box>
              </Box>
            </Paper>
          </Box>
        )}
      </Box>
    </Box>
  );
}
