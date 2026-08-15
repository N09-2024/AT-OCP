import React, { useRef, useState, useEffect, useCallback } from 'react';
import {
  Box,
  Typography,
  Button,
  Paper,
  Stack,
  Alert,
  Chip,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Divider,
} from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import EditIcon from '@mui/icons-material/Edit';
import VisibilityIcon from '@mui/icons-material/Visibility';
import DrawIcon from '@mui/icons-material/Draw';
import { useAuthStore } from '../../store/authStore';

interface SignaturePadProps {
  onSave: (blob: Blob, dataUrl: string) => void;
  onClear?: () => void;
  title?: string;
  disabled?: boolean;
  savedDataUrl?: string | null;
}

export default function SignaturePad({
  onSave,
  onClear,
  title = 'Visa et Signature Manuscrite',
  disabled = false,
  savedDataUrl = null,
}: SignaturePadProps) {
  const user = useAuthStore((s) => s.user);
  const canvasRef = useRef<HTMLCanvasElement | null>(null);
  const [isDrawing, setIsDrawing] = useState(false);
  const [hasDrawn, setHasDrawn] = useState(false);
  const [isSaved, setIsSaved] = useState(!!savedDataUrl);
  const [previewOpen, setPreviewOpen] = useState(false);
  const [currentDataUrl, setCurrentDataUrl] = useState<string | null>(savedDataUrl);

  const getCanvasContext = useCallback(() => {
    const canvas = canvasRef.current;
    if (!canvas) return null;
    return canvas.getContext('2d');
  }, []);

  // Initialize Canvas dimensions and properties
  const initCanvas = useCallback(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    // Get parent box width
    const rect = canvas.getBoundingClientRect();
    canvas.width = rect.width || 400;
    canvas.height = 180;

    ctx.strokeStyle = '#0E2A21'; // Dark slate color for clean ink
    ctx.lineWidth = 2.5;
    ctx.lineCap = 'round';
    ctx.lineJoin = 'round';
  }, []);

  useEffect(() => {
    initCanvas();
    window.addEventListener('resize', initCanvas);
    return () => window.removeEventListener('resize', initCanvas);
  }, [initCanvas]);

  // Coordinates helper
  const getCoordinates = (e: React.MouseEvent<HTMLCanvasElement> | React.TouchEvent<HTMLCanvasElement>) => {
    const canvas = canvasRef.current;
    if (!canvas) return { x: 0, y: 0 };
    const rect = canvas.getBoundingClientRect();

    if ('touches' in e) {
      const touch = e.touches[0];
      return {
        x: touch.clientX - rect.left,
        y: touch.clientY - rect.top,
      };
    } else {
      return {
        x: e.clientX - rect.left,
        y: e.clientY - rect.top,
      };
    }
  };

  const startDrawing = (e: React.MouseEvent<HTMLCanvasElement> | React.TouchEvent<HTMLCanvasElement>) => {
    if (disabled || isSaved) return;
    const ctx = getCanvasContext();
    if (!ctx) return;

    const { x, y } = getCoordinates(e);
    ctx.beginPath();
    ctx.moveTo(x, y);
    setIsDrawing(true);
    setHasDrawn(true);
  };

  const draw = (e: React.MouseEvent<HTMLCanvasElement> | React.TouchEvent<HTMLCanvasElement>) => {
    if (!isDrawing || disabled || isSaved) return;
    const ctx = getCanvasContext();
    if (!ctx) return;

    const { x, y } = getCoordinates(e);
    ctx.lineTo(x, y);
    ctx.stroke();
  };

  const stopDrawing = () => {
    setIsDrawing(false);
  };

  const handleClear = () => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    if (ctx) {
      ctx.clearRect(0, 0, canvas.width, canvas.height);
    }
    setHasDrawn(false);
    setIsSaved(false);
    setCurrentDataUrl(null);
    if (onClear) onClear();
  };

  const handleSave = () => {
    const canvas = canvasRef.current;
    if (!canvas || !hasDrawn) return;

    canvas.toBlob((blob) => {
      if (blob) {
        const dataUrl = canvas.toDataURL('image/png');
        setCurrentDataUrl(dataUrl);
        setIsSaved(true);
        onSave(blob, dataUrl);
      }
    }, 'image/png');
  };

  const formattedDate = new Date().toLocaleString('fr-FR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });

  const userRolesStr = user?.roles?.map((r) => r.nom).join(', ') || 'Demandeur';

  return (
    <Paper
      elevation={0}
      sx={{
        p: 3,
        border: '1px solid',
        borderColor: isSaved ? '#7FC8A9' : '#D6E3DC',
        borderRadius: 3,
        bgcolor: isSaved ? '#EDF2EE' : '#FFFFFF',
        transition: 'all 0.3s ease',
      }}
    >
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
        <Typography variant="subtitle1" sx={{ fontWeight: 700, color: '#0E2A21', display: 'flex', alignItems: 'center', gap: 1 }}>
          <DrawIcon sx={{ color: '#3C7A5C' }} />
          {title}
        </Typography>

        {isSaved ? (
          <Chip icon={<CheckCircleIcon />} label="Visa Enregistré ✓" color="success" size="small" sx={{ fontWeight: 700 }} />
        ) : (
          <Chip label="Signature requise" color="warning" size="small" sx={{ fontWeight: 600 }} />
        )}
      </Box>

      {/* User info recap */}
      <Box sx={{ p: 2, mb: 2, bgcolor: '#F7FAF8', borderRadius: 2, border: '1px solid #E3ECE7' }}>
        <Typography variant="body2" sx={{ fontWeight: 600, color: '#16241E' }}>
          Signataire : {user?.nom ? `${user.prenom} ${user.nom}` : 'Utilisateur Connecté'}
        </Typography>
        <Typography variant="caption" color="text.secondary" sx={{ display: 'block' }}>
          Rôle / Fonction : {userRolesStr} | Date : {formattedDate}
        </Typography>
      </Box>

      {/* Signature Canvas / Display */}
      {isSaved && currentDataUrl ? (
        <Box sx={{ textAling: 'center', py: 2 }}>
          <Box
            component="img"
            src={currentDataUrl}
            alt="Signature enregistrée"
            sx={{
              maxHeight: 140,
              maxWidth: '100%',
              border: '1px dashed #3C7A5C',
              borderRadius: 2,
              p: 1,
              bgcolor: 'white',
              display: 'block',
              mx: 'auto',
            }}
          />
        </Box>
      ) : (
        <Box
          sx={{
            position: 'relative',
            border: '2px dashed',
            borderColor: isDrawing ? '#3C7A5C' : '#D6E3DC',
            borderRadius: 2,
            bgcolor: disabled ? '#E3ECE7' : '#F7FAF8',
            cursor: disabled ? 'not-allowed' : 'crosshair',
            overflow: 'hidden',
          }}
        >
          <canvas
            ref={canvasRef}
            onMouseDown={startDrawing}
            onMouseMove={draw}
            onMouseUp={stopDrawing}
            onMouseLeave={stopDrawing}
            onTouchStart={startDrawing}
            onTouchMove={draw}
            onTouchEnd={stopDrawing}
            style={{ width: '100%', display: 'block', touchAction: 'none' }}
          />
          {!hasDrawn && (
            <Typography
              variant="caption"
              sx={{
                position: 'absolute',
                top: '50%',
                left: '50%',
                transform: 'translate(-50%, -50%)',
                color: '#5C6E67',
                pointerEvents: 'none',
                userSelect: 'none',
                fontWeight: 500,
              }}
            >
              Signez ici à la souris ou au doigt
            </Typography>
          )}
        </Box>
      )}

      {/* Action Buttons */}
      <Stack direction="row" spacing={1.5} sx={{ mt: 2, justifyContent: 'flex-end' }}>
        {isSaved ? (
          <>
            <Button
              variant="outlined"
              size="small"
              startIcon={<VisibilityIcon />}
              onClick={() => setPreviewOpen(true)}
              sx={{ color: '#5C6E67', borderColor: '#D6E3DC' }}
            >
              Aperçu
            </Button>
            <Button
              variant="outlined"
              color="error"
              size="small"
              startIcon={<EditIcon />}
              onClick={handleClear}
            >
              Modifier la signature
            </Button>
          </>
        ) : (
          <>
            <Button
              variant="outlined"
              color="inherit"
              size="small"
              startIcon={<DeleteIcon />}
              onClick={handleClear}
              disabled={!hasDrawn || disabled}
            >
              Effacer
            </Button>
            {hasDrawn && (
              <Button
                variant="outlined"
                size="small"
                startIcon={<VisibilityIcon />}
                onClick={() => {
                  const canvas = canvasRef.current;
                  if (canvas) {
                    setCurrentDataUrl(canvas.toDataURL());
                    setPreviewOpen(true);
                  }
                }}
              >
                Aperçu
              </Button>
            )}
            <Button
              variant="contained"
              size="small"
              startIcon={<CheckCircleIcon />}
              onClick={handleSave}
              disabled={!hasDrawn || disabled}
              sx={{ bgcolor: '#3C7A5C', '&:hover': { bgcolor: '#2E624A' } }}
            >
              Enregistrer le visa
            </Button>
          </>
        )}
      </Stack>

      {/* Preview Dialog */}
      <Dialog open={previewOpen} onClose={() => setPreviewOpen(false)} maxWidth="xs" fullWidth>
        <DialogTitle sx={{ fontWeight: 700 }}>Aperçu du Visa & Signature</DialogTitle>
        <DialogContent>
          <Box sx={{ p: 2, border: '1px solid #D6E3DC', borderRadius: 2, bgcolor: '#FFFFFF', textAlign: 'center', my: 1 }}>
            <Typography variant="subtitle2" sx={{ fontWeight: 700, color: '#0E2A21' }}>
              OCP Group — Visa Validé
            </Typography>
            <Divider sx={{ my: 1 }} />
            <Typography variant="body2">
              Nom & Prénom : <strong>{user?.prenom} {user?.nom}</strong>
            </Typography>
            <Typography variant="body2">
              Qualité / Rôle : <strong>{userRolesStr}</strong>
            </Typography>
            <Typography variant="body2" sx={{ mb: 2 }}>
              Date & Heure : <strong>{formattedDate}</strong>
            </Typography>
            {currentDataUrl && (
              <Box
                component="img"
                src={currentDataUrl}
                alt="Aperçu Signature"
                sx={{ maxHeight: 120, maxWidth: '100%', border: '1px solid #D6E3DC', p: 1, borderRadius: 1 }}
              />
            )}
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setPreviewOpen(false)}>Fermer</Button>
        </DialogActions>
      </Dialog>
    </Paper>
  );
}
