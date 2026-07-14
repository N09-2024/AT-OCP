import { createTheme } from '@mui/material/styles';

const theme = createTheme({
  palette: {
    primary: {
      main: '#009A44',
      dark: '#006B3C',
      light: '#EAF7EF',
      contrastText: '#FFFFFF',
    },
    secondary: {
      main: '#006B3C', // Dark Green
    },
    background: {
      default: '#F7F9FB',
      paper: '#FFFFFF',
    },
    error: {
      main: '#DC2626', // Danger
    },
    warning: {
      main: '#F59E0B', // Warning
    },
    success: {
      main: '#16A34A', // Success
    },
    text: {
      primary: '#1F2937', // Gray 800
      secondary: '#6B7280', // Gray 500
    },
    divider: '#E5E7EB', // Borders
  },
  typography: {
    fontFamily: '"Inter", "Roboto", "Helvetica", "Arial", sans-serif',
    h1: {
      fontWeight: 700,
      fontSize: '2.5rem',
    },
    h2: {
      fontWeight: 600,
      fontSize: '2rem',
    },
    h3: {
      fontWeight: 600,
      fontSize: '1.75rem',
    },
    h4: {
      fontWeight: 600,
      fontSize: '1.5rem',
    },
    h5: {
      fontWeight: 600,
      fontSize: '1.25rem',
    },
    h6: {
      fontWeight: 600,
      fontSize: '1rem',
    },
    button: {
      textTransform: 'none',
      fontWeight: 500,
    },
  },
  shape: {
    borderRadius: 16, // Coins 16px as requested
  },
  components: {
    MuiButton: {
      styleOverrides: {
        root: {
          borderRadius: 8, // Boutons un peu moins arrondis que les cartes en général, ou 16px si on veut. Restons sur 8px pour les boutons, 16px pour les cartes
          boxShadow: 'none',
          '&:hover': {
            boxShadow: '0px 4px 6px -1px rgba(0, 0, 0, 0.1), 0px 2px 4px -1px rgba(0, 0, 0, 0.06)',
          },
        },
      },
    },
    MuiCard: {
      styleOverrides: {
        root: {
          borderRadius: 16,
          boxShadow: '0px 1px 3px 0px rgba(0, 0, 0, 0.1), 0px 1px 2px 0px rgba(0, 0, 0, 0.06)', // Ombre très discrète
          border: '1px solid #E5E7EB',
        },
      },
    },
    MuiPaper: {
      styleOverrides: {
        root: {
          backgroundImage: 'none',
        },
      },
    },
    MuiTableCell: {
      styleOverrides: {
        head: {
          fontWeight: 600,
          backgroundColor: '#F7F9FB',
          color: '#6B7280',
        },
      },
    },
  },
});

export default theme;
