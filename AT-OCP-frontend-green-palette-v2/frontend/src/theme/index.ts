import { createTheme } from '@mui/material/styles';
import { OCP, OCP_FONTS } from './tokens';

const theme = createTheme({
  palette: {
    primary: {
      main: OCP.forest,
      dark: OCP.deep,
      light: OCP.mint,
      contrastText: OCP.white,
    },
    secondary: {
      main: OCP.moss,
      dark: OCP.mossDark,
      light: OCP.mint,
      contrastText: OCP.white,
    },
    success: {
      main: OCP.moss,
      dark: OCP.forestDark,
      light: OCP.mintSoft,
      contrastText: OCP.white,
    },
    warning: {
      main: OCP.warning,
      light: OCP.warningSoft,
      contrastText: OCP.ink,
    },
    error: {
      main: OCP.error,
      light: OCP.errorSoft,
      contrastText: OCP.white,
    },
    background: {
      default: OCP.surfaceSoft,
      paper: OCP.white,
    },
    text: {
      primary: OCP.ink,
      secondary: OCP.slate,
    },
    divider: OCP.border,
  },
  typography: {
    fontFamily: OCP_FONTS.body,
    h1: { fontFamily: OCP_FONTS.display, fontWeight: 700, fontSize: '2.5rem' },
    h2: { fontFamily: OCP_FONTS.display, fontWeight: 700, fontSize: '2rem' },
    h3: { fontFamily: OCP_FONTS.display, fontWeight: 700, fontSize: '1.75rem' },
    h4: { fontFamily: OCP_FONTS.display, fontWeight: 700, fontSize: '1.5rem' },
    h5: { fontFamily: OCP_FONTS.display, fontWeight: 600, fontSize: '1.25rem' },
    h6: { fontFamily: OCP_FONTS.display, fontWeight: 600, fontSize: '1rem' },
    subtitle1: { fontWeight: 600 },
    subtitle2: { fontWeight: 600 },
    button: { textTransform: 'none', fontWeight: 600 },
  },
  shape: { borderRadius: 8 },
  components: {
    MuiButton: {
      styleOverrides: {
        root: {
          borderRadius: 8,
          textTransform: 'none',
          fontWeight: 600,
          boxShadow: 'none',
          '&:hover': { boxShadow: 'none' },
          '&:focus-visible': { outline: `2px solid ${OCP.mint}`, outlineOffset: 2 },
        },
        contained: { boxShadow: 'none', '&:hover': { boxShadow: 'none' } },
        outlined: { borderWidth: 1, '&:hover': { borderWidth: 1 } },
      },
      variants: [
        { props: { variant: 'contained', color: 'primary' }, style: { backgroundColor: OCP.forest, color: OCP.white, '&:hover': { backgroundColor: OCP.forestDark } } },
        { props: { variant: 'contained', color: 'secondary' }, style: { backgroundColor: OCP.moss, color: OCP.white, '&:hover': { backgroundColor: OCP.mossDark } } },
        { props: { variant: 'outlined', color: 'primary' }, style: { borderColor: OCP.forest, color: OCP.forest, '&:hover': { borderColor: OCP.forest, backgroundColor: OCP.forestSoft } } },
        { props: { variant: 'outlined', color: 'secondary' }, style: { borderColor: OCP.moss, color: OCP.moss, '&:hover': { borderColor: OCP.moss, backgroundColor: OCP.mintSoft } } },
      ],
    },
    MuiCard: {
      styleOverrides: {
        root: { borderRadius: 12, boxShadow: '0 1px 3px rgba(14,42,33,0.08)', border: `1px solid ${OCP.border}` },
      },
    },
    MuiPaper: { styleOverrides: { root: { backgroundImage: 'none' } } },
    MuiTableCell: {
      styleOverrides: {
        head: { fontWeight: 600, backgroundColor: OCP.sage, color: OCP.slate, borderBottom: `2px solid ${OCP.border}` },
        root: { borderBottom: `1px solid ${OCP.borderSoft}`, padding: '12px 16px' },
      },
    },
    MuiOutlinedInput: {
      styleOverrides: {
        root: {
          borderRadius: 8,
          '&:hover .MuiOutlinedInput-notchedOutline': { borderColor: OCP.forest },
          '&.Mui-focused .MuiOutlinedInput-notchedOutline': { borderColor: OCP.forest },
        },
        notchedOutline: { borderColor: OCP.border },
      },
    },
    MuiTextField: { defaultProps: { variant: 'outlined' } },
    MuiChip: { styleOverrides: { root: { borderRadius: 999, fontWeight: 600 } } },
    MuiLink: { styleOverrides: { root: { color: OCP.forest, '&:hover': { color: OCP.moss } } } },
  },
});

export default theme;
