import { ThemeProvider } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { BrowserRouter } from 'react-router-dom';
import theme from '../theme';
import AppRoutes from '../routes';
import { PopinProvider } from '../contexts/PopinContext';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      refetchOnWindowFocus: false,
      retry: 1,
    },
  },
});

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <ThemeProvider theme={theme}>
        <CssBaseline />
        <PopinProvider>
          <BrowserRouter>
            <AppRoutes />
          </BrowserRouter>
        </PopinProvider>
      </ThemeProvider>
    </QueryClientProvider>
  );
}

export default App;
