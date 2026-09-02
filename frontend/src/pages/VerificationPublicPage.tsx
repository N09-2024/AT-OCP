import React, { useEffect, useState } from 'react';
import { useParams, useSearchParams } from 'react-router-dom';
import {
  Box, Typography, CircularProgress, Paper, Alert, Divider, Chip, Stack,
} from '@mui/material';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import ErrorIcon from '@mui/icons-material/Error';
import QrCodeScannerIcon from '@mui/icons-material/QrCodeScanner';
import VerifiedIcon from '@mui/icons-material/Verified';
import { apiClient } from '../services/apiClient';

interface VerificationResponse {
  valide: boolean;
  numeroAT?: string;
  numeroArchive?: string;
  statut?: string;
  dateArchivage?: string;
  archivisteNom?: string;
  hashSHA256?: string;
  installation?: string;
  zone?: string;
  message?: string;
}

export default function VerificationPublicPage() {
  const { numero } = useParams<{ numero: string }>();
  const [searchParams] = useSearchParams();
  const refFromQuery = searchParams.get('ref') || numero;

  const [loading, setLoading] = useState(true);
  const [result, setResult] = useState<VerificationResponse | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!refFromQuery) {
      setError('Référence d\'AT ou d\'archive manquante dans l\'URL.');
      setLoading(false);
      return;
    }
    (async () => {
      try {
        const { data } = await apiClient.get<VerificationResponse>(
          `/verification/${encodeURIComponent(refFromQuery)}`
        );
        setResult(data);
      } catch (err: any) {
        setError(err.response?.data?.message || 'Erreur lors de la vérification du dossier.');
      } finally {
        setLoading(false);
      }
    })();
  }, [refFromQuery]);

  return (
    <Box
      sx={{
        minHeight: '100vh',
        bgcolor: '#F4F7F4',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        p: 3,
      }}
    >
      <Paper
        sx={{
          maxWidth: 600,
          width: '100%',
          borderRadius: 3,
          p: 4,
          boxShadow: '0 20px 60px rgba(31, 77, 62, 0.12)',
        }}
      >
        {/* En-tête OCP */}
        <Box sx={{ textAlign: 'center', mb: 3 }}>
          <QrCodeScannerIcon sx={{ fontSize: 48, color: '#1F4D3E', mb: 1 }} />
          <Typography variant="h5" sx={{ fontWeight: 800, color: '#1F4D3E' }}>
            Vérification Dossier AT
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Système de Gestion des Autorisations de Travail - OCP Group
          </Typography>
        </Box>

        <Divider sx={{ mb: 3 }} />

        {loading && (
          <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', py: 4, gap: 2 }}>
            <CircularProgress color="success" />
            <Typography variant="body2" color="text.secondary">
              Vérification en cours pour la référence : <strong>{refFromQuery}</strong>
            </Typography>
          </Box>
        )}

        {!loading && error && (
          <Alert severity="error" icon={<ErrorIcon color="error" />}>
            {error}
          </Alert>
        )}

        {!loading && result && (
          <>
            <Alert
              severity={result.valide ? 'success' : 'error'}
              icon={result.valide ? <VerifiedIcon /> : <ErrorIcon color="error" />}
              sx={{ mb: 3, borderRadius: 2 }}
            >
              <Typography variant="subtitle1" sx={{ fontWeight: 800 }}>
                {result.valide
                  ? '✅ Dossier authentifié et valide'
                  : '❌ Dossier introuvable ou non archivé'}
              </Typography>
              <Typography variant="body2">{result.message}</Typography>
            </Alert>

            {result.valide && (
              <Stack spacing={1.5}>
                <Box sx={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 1.5 }}>
                  <Box>
                    <Typography variant="caption" color="text.secondary">Numéro AT</Typography>
                    <Typography variant="body2" sx={{ fontWeight: 700 }}>{result.numeroAT || '-'}</Typography>
                  </Box>
                  <Box>
                    <Typography variant="caption" color="text.secondary">Numéro d'archive</Typography>
                    <Typography variant="body2" sx={{ fontWeight: 700 }}>{result.numeroArchive || '-'}</Typography>
                  </Box>
                  <Box>
                    <Typography variant="caption" color="text.secondary">Statut</Typography>
                    <Chip
                      label={result.statut || 'ARCHIVEE'}
                      color="success"
                      size="small"
                      icon={<CheckCircleIcon />}
                    />
                  </Box>
                  <Box>
                    <Typography variant="caption" color="text.secondary">Date d'archivage</Typography>
                    <Typography variant="body2" sx={{ fontWeight: 600 }}>
                      {result.dateArchivage
                        ? new Date(result.dateArchivage).toLocaleString('fr-FR')
                        : '-'}
                    </Typography>
                  </Box>
                  {result.installation && (
                    <Box>
                      <Typography variant="caption" color="text.secondary">Installation</Typography>
                      <Typography variant="body2">{result.installation}</Typography>
                    </Box>
                  )}
                  {result.zone && (
                    <Box>
                      <Typography variant="caption" color="text.secondary">Zone</Typography>
                      <Typography variant="body2">{result.zone}</Typography>
                    </Box>
                  )}
                  {result.archivisteNom && (
                    <Box sx={{ gridColumn: '1 / -1' }}>
                      <Typography variant="caption" color="text.secondary">Archivé par</Typography>
                      <Typography variant="body2" sx={{ fontWeight: 600 }}>{result.archivisteNom}</Typography>
                    </Box>
                  )}
                </Box>

                {result.hashSHA256 && (
                  <>
                    <Divider />
                    <Box>
                      <Typography variant="caption" color="text.secondary">Empreinte d'intégrité SHA-256</Typography>
                      <Typography
                        variant="caption"
                        sx={{
                          fontFamily: 'monospace',
                          fontSize: '0.7rem',
                          wordBreak: 'break-all',
                          display: 'block',
                          color: '#1F4D3E',
                          mt: 0.5,
                        }}
                      >
                        {result.hashSHA256}
                      </Typography>
                    </Box>
                  </>
                )}
              </Stack>
            )}
          </>
        )}

        <Divider sx={{ mt: 3, mb: 2 }} />
        <Typography variant="caption" color="text.secondary" sx={{ textAlign: 'center', display: 'block' }}>
          OCP Group · Système de Gestion AT HSE · Standard S-HSE-SEC-31
        </Typography>
      </Paper>
    </Box>
  );
}
