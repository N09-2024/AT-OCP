import React, { useState, useEffect } from 'react';
import {
  Box,
  Typography,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Button,
  Chip,
  CircularProgress,
  Stack,
  IconButton,
  Tooltip,
  TablePagination,
  Alert,
} from '@mui/material';
import DownloadIcon from '@mui/icons-material/Download';
import VerifiedUserIcon from '@mui/icons-material/VerifiedUser';
import PictureAsPdfIcon from '@mui/icons-material/PictureAsPdf';
import QrCodeIcon from '@mui/icons-material/QrCode';
import { archiveApi } from '../../../services/archiveApi';
import type { Archive } from '../../../types';

export default function ArchiveListPage() {
  const [loading, setLoading] = useState(true);
  const [archives, setArchives] = useState<Archive[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const [verificationState, setVerificationState] = useState<Record<string, boolean | null>>({});

  const loadData = async () => {
    setLoading(true);
    try {
      const res = await archiveApi.getAll(page, pageSize);
      setArchives(res.content || []);
      setTotal(res.totalElements || 0);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, [page, pageSize]);

  const handleDownload = async (id: string, numeroArchive: string) => {
    try {
      const blob = await archiveApi.downloadArchive(id);
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `${numeroArchive || 'Archive'}.pdf`;
      a.click();
    } catch (e) {
      alert('Erreur lors du téléchargement de l\'archive.');
    }
  };

  const handleVerify = async (id: string) => {
    try {
      const valid = await archiveApi.verifyArchive(id);
      setVerificationState((prev) => ({ ...prev, [id]: valid }));
    } catch (e) {
      setVerificationState((prev) => ({ ...prev, [id]: false }));
    }
  };

  return (
    <Box sx={{ p: 3 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Box>
          <Typography variant="h4" sx={{ fontWeight: 800, color: '#0E2A21' }}>
            Archivage Numérique & Coffre-Fort (Module 10)
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Consultation, téléchargeable PDF, empreintes SHA-256 et vérification d'intégrité
          </Typography>
        </Box>
      </Box>

      <TableContainer component={Paper} sx={{ borderRadius: 3, border: '1px solid #D6E3DC', boxShadow: 'none' }}>
        {loading ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', p: 6 }}>
            <CircularProgress color="success" />
          </Box>
        ) : (
          <Table sx={{ minWidth: 650 }}>
            <TableHead sx={{ bgcolor: '#F7FAF8' }}>
              <TableRow>
                <TableCell sx={{ fontWeight: 700 }}>N° Archive</TableCell>
                <TableCell sx={{ fontWeight: 700 }}>N° AT</TableCell>
                <TableCell sx={{ fontWeight: 700 }}>Version</TableCell>
                <TableCell sx={{ fontWeight: 700 }}>Date Archivage</TableCell>
                <TableCell sx={{ fontWeight: 700 }}>Empreinte SHA-256</TableCell>
                <TableCell sx={{ fontWeight: 700 }}>Intégrité</TableCell>
                <TableCell align="right" sx={{ fontWeight: 700 }}>Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {archives.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={7} align="center" sx={{ py: 6 }}>
                    <Typography variant="body1" color="text.secondary">
                      Aucune archive trouvée.
                    </Typography>
                  </TableCell>
                </TableRow>
              ) : (
                archives.map((row) => (
                  <TableRow key={row.id} hover>
                    <TableCell sx={{ fontWeight: 700, color: '#1F4D3E' }}>
                      {row.numeroArchive || row.id.substring(0, 8)}
                    </TableCell>

                    <TableCell sx={{ fontWeight: 600 }}>{row.numeroAT}</TableCell>

                    <TableCell>v{row.version || 1}</TableCell>

                    <TableCell>
                      {row.dateArchivage ? new Date(row.dateArchivage).toLocaleDateString('fr-FR') : '—'}
                    </TableCell>

                    <TableCell>
                      <Typography variant="caption" sx={{ fontFamily: 'monospace', bgcolor: '#E3ECE7', p: 0.5, borderRadius: 1 }}>
                        {row.hashSHA256 ? `${row.hashSHA256.substring(0, 12)}...` : 'N/A'}
                      </Typography>
                    </TableCell>

                    <TableCell>
                      {verificationState[row.id] === true && (
                        <Chip label="Intègre ✓" color="success" size="small" sx={{ fontWeight: 700 }} />
                      )}
                      {verificationState[row.id] === false && (
                        <Chip label="Altéré ✗" color="error" size="small" sx={{ fontWeight: 700 }} />
                      )}
                      {verificationState[row.id] === undefined && (
                        <Chip label="Non vérifiée" size="small" variant="outlined" />
                      )}
                    </TableCell>

                    <TableCell align="right">
                      <Stack direction="row" spacing={0.5} sx={{ justifyContent: 'flex-end' }}>
                        <Tooltip title="Vérifier l'intégrité SHA-256">
                          <IconButton size="small" onClick={() => handleVerify(row.id)} color="primary">
                            <VerifiedUserIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>

                        <Tooltip title="Télécharger le PDF d'archive">
                          <IconButton size="small" onClick={() => handleDownload(row.id, row.numeroArchive)} color="error">
                            <DownloadIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>
                      </Stack>
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        )}

        <TablePagination
          component="div"
          count={total}
          page={page}
          onPageChange={(_, p) => setPage(p)}
          rowsPerPage={pageSize}
          onRowsPerPageChange={(e) => {
            setPageSize(parseInt(e.target.value, 10));
            setPage(0);
          }}
        />
      </TableContainer>
    </Box>
  );
}
