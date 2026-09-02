import React, { createContext, useContext, useState, useCallback } from 'react';
import {
  Dialog,
  DialogContent,
  DialogActions,
  Button,
  Typography,
  Box,
  Snackbar,
  Alert,
  Fade,
} from '@mui/material';
import WarningAmberRoundedIcon from '@mui/icons-material/WarningAmberRounded';
import ErrorOutlineRoundedIcon from '@mui/icons-material/ErrorOutlineRounded';
import CheckCircleOutlineRoundedIcon from '@mui/icons-material/CheckCircleOutlineRounded';
import InfoOutlinedIcon from '@mui/icons-material/InfoOutlined';
import AutoAwesomeRoundedIcon from '@mui/icons-material/AutoAwesomeRounded';
import { OCP } from '../theme/tokens';

export type PopinSeverity = 'info' | 'warning' | 'error' | 'success' | 'ai';

export interface PopinOptions {
  title?: string;
  message: string | React.ReactNode;
  severity?: PopinSeverity;
  confirmText?: string;
  cancelText?: string;
  isAi?: boolean;
}

export interface ToastOptions {
  message: string;
  severity?: 'success' | 'error' | 'warning' | 'info';
  duration?: number;
}

interface PopinContextType {
  confirm: (options: PopinOptions | string) => Promise<boolean>;
  alert: (options: PopinOptions | string) => Promise<void>;
  toast: (options: ToastOptions | string) => void;
}

const PopinContext = createContext<PopinContextType | null>(null);

export const usePopin = () => {
  const context = useContext(PopinContext);
  if (!context) {
    throw new Error('usePopin must be used within a PopinProvider');
  }
  return context;
};

export const PopinProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  // Dialog state
  const [dialogOpen, setDialogOpen] = useState(false);
  const [dialogConfig, setDialogConfig] = useState<PopinOptions & { isConfirm?: boolean }>({
    message: '',
  });
  const [resolver, setResolver] = useState<((value: any) => void) | null>(null);

  // Toast state
  const [toastOpen, setToastOpen] = useState(false);
  const [toastConfig, setToastConfig] = useState<ToastOptions>({
    message: '',
    severity: 'info',
    duration: 4000,
  });

  const confirm = useCallback((options: PopinOptions | string): Promise<boolean> => {
    return new Promise((resolve) => {
      const config: PopinOptions & { isConfirm: boolean } =
        typeof options === 'string'
          ? { message: options, isConfirm: true, severity: 'warning' }
          : { ...options, isConfirm: true, severity: options.severity || 'warning' };

      setDialogConfig(config);
      setResolver(() => resolve);
      setDialogOpen(true);
    });
  }, []);

  const alert = useCallback((options: PopinOptions | string): Promise<void> => {
    return new Promise((resolve) => {
      const config: PopinOptions & { isConfirm: boolean } =
        typeof options === 'string'
          ? { message: options, isConfirm: false, severity: 'info' }
          : { ...options, isConfirm: false, severity: options.severity || 'info' };

      setDialogConfig(config);
      setResolver(() => resolve);
      setDialogOpen(true);
    });
  }, []);

  const toast = useCallback((options: ToastOptions | string) => {
    if (typeof options === 'string') {
      setToastConfig({ message: options, severity: 'info', duration: 4000 });
    } else {
      setToastConfig({
        message: options.message,
        severity: options.severity || 'info',
        duration: options.duration || 4000,
      });
    }
    setToastOpen(true);
  }, []);

  const handleCloseDialog = (confirmed: boolean) => {
    setDialogOpen(false);
    if (resolver) {
      resolver(confirmed);
      setResolver(null);
    }
  };

  const isAi =
    dialogConfig.isAi ||
    dialogConfig.severity === 'ai' ||
    (typeof dialogConfig.message === 'string' && dialogConfig.message.includes('Contrôle IA'));

  const getSeverityIcon = (severity?: PopinSeverity, aiCheck?: boolean) => {
    if (aiCheck || severity === 'ai') {
      return <AutoAwesomeRoundedIcon sx={{ color: '#6366F1', fontSize: 30 }} />;
    }
    switch (severity) {
      case 'warning':
        return <WarningAmberRoundedIcon sx={{ color: OCP.warning, fontSize: 30 }} />;
      case 'error':
        return <ErrorOutlineRoundedIcon sx={{ color: OCP.error, fontSize: 30 }} />;
      case 'success':
        return <CheckCircleOutlineRoundedIcon sx={{ color: OCP.moss, fontSize: 30 }} />;
      case 'info':
      default:
        return <InfoOutlinedIcon sx={{ color: OCP.forest, fontSize: 30 }} />;
    }
  };

  const getHeaderColor = (severity?: PopinSeverity, aiCheck?: boolean) => {
    if (aiCheck || severity === 'ai') return '#EEF2FF';
    switch (severity) {
      case 'warning':
        return OCP.warningSoft;
      case 'error':
        return OCP.errorSoft;
      case 'success':
        return OCP.mintSoft;
      case 'info':
      default:
        return OCP.forestSoft;
    }
  };

  return (
    <PopinContext.Provider value={{ confirm, alert, toast }}>
      {children}

      {/* Modern Pop-in Modal Dialog */}
      <Dialog
        open={dialogOpen}
        onClose={() => handleCloseDialog(false)}
        maxWidth="sm"
        fullWidth
        TransitionComponent={Fade}
        transitionDuration={250}
        PaperProps={{
          sx: {
            borderRadius: 3,
            overflow: 'hidden',
            boxShadow: '0 20px 40px rgba(14,42,33,0.18)',
            border: `1px solid ${OCP.border}`,
          },
        }}
      >
        <Box
          sx={{
            display: 'flex',
            alignItems: 'center',
            gap: 2,
            px: 3,
            py: 2.2,
            backgroundColor: getHeaderColor(dialogConfig.severity, isAi),
            borderBottom: `1px solid ${OCP.borderSoft}`,
          }}
        >
          <Box
            sx={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              width: 44,
              height: 44,
              borderRadius: 2.5,
              backgroundColor: 'rgba(255,255,255,0.9)',
              boxShadow: '0 2px 8px rgba(0,0,0,0.06)',
            }}
          >
            {getSeverityIcon(dialogConfig.severity, isAi)}
          </Box>
          <Box sx={{ flex: 1 }}>
            <Typography
              variant="h6"
              sx={{
                fontWeight: 700,
                color: OCP.deep,
                fontSize: '1.15rem',
                lineHeight: 1.2,
              }}
            >
              {dialogConfig.title ||
                (isAi
                  ? 'Contrôle IA - Validation'
                  : dialogConfig.isConfirm
                  ? 'Confirmation'
                  : 'Information')}
            </Typography>
            <Typography variant="caption" sx={{ color: OCP.slate, fontWeight: 500 }}>
              Système AT-OCP
            </Typography>
          </Box>
        </Box>

        <DialogContent sx={{ px: 3, py: 3, backgroundColor: OCP.white }}>
          {typeof dialogConfig.message === 'string' ? (
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1.5 }}>
              {dialogConfig.message.split('\n\n').map((block, idx) => (
                <Typography
                  key={idx}
                  variant="body1"
                  sx={{
                    color: OCP.ink,
                    fontSize: '0.95rem',
                    lineHeight: 1.6,
                    whiteSpace: 'pre-line',
                    ...(block.startsWith('-') || block.includes('•')
                      ? {
                          backgroundColor: OCP.surfaceSoft,
                          p: 1.5,
                          borderRadius: 2,
                          border: `1px solid ${OCP.borderSoft}`,
                        }
                      : {}),
                  }}
                >
                  {block}
                </Typography>
              ))}
            </Box>
          ) : (
            dialogConfig.message
          )}
        </DialogContent>

        <DialogActions
          sx={{
            px: 3,
            py: 2,
            backgroundColor: OCP.surfaceSoft,
            borderTop: `1px solid ${OCP.borderSoft}`,
            gap: 1.5,
          }}
        >
          {dialogConfig.isConfirm && (
            <Button
              onClick={() => handleCloseDialog(false)}
              variant="outlined"
              sx={{
                borderRadius: 2,
                px: 2.5,
                py: 1,
                color: OCP.slate,
                borderColor: OCP.border,
                '&:hover': {
                  borderColor: OCP.slate,
                  backgroundColor: 'rgba(0,0,0,0.03)',
                },
              }}
            >
              {dialogConfig.cancelText || 'Annuler'}
            </Button>
          )}
          <Button
            onClick={() => handleCloseDialog(true)}
            variant="contained"
            autoFocus
            sx={{
              borderRadius: 2,
              px: 3,
              py: 1,
              backgroundColor:
                dialogConfig.severity === 'error'
                  ? OCP.error
                  : isAi
                  ? '#4F46E5'
                  : OCP.forest,
              color: OCP.white,
              fontWeight: 600,
              boxShadow: 'none',
              '&:hover': {
                backgroundColor:
                  dialogConfig.severity === 'error'
                    ? '#7D2E22'
                    : isAi
                    ? '#4338CA'
                    : OCP.forestDark,
                boxShadow: 'none',
              },
            }}
          >
            {dialogConfig.confirmText || (dialogConfig.isConfirm ? 'Confirmer' : 'OK')}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Modern Toast / Snackbar Notification */}
      <Snackbar
        open={toastOpen}
        autoHideDuration={toastConfig.duration}
        onClose={() => setToastOpen(false)}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
      >
        <Alert
          onClose={() => setToastOpen(false)}
          severity={toastConfig.severity}
          variant="filled"
          sx={{
            width: '100%',
            borderRadius: 2,
            boxShadow: '0 6px 16px rgba(0,0,0,0.12)',
            fontWeight: 600,
          }}
        >
          {toastConfig.message}
        </Alert>
      </Snackbar>
    </PopinContext.Provider>
  );
};
