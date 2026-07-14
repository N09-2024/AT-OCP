import { Outlet } from 'react-router-dom';
import { Box } from '@mui/material';
import Sidebar from '../components/layout/Sidebar';
import Topbar from '../components/layout/Topbar';

export default function MainLayout() {
  return (
    <Box sx={{ display: 'flex', minHeight: '100vh', backgroundColor: 'background.default' }}>
      <Sidebar />

      <Box sx={{ display: 'flex', flexDirection: 'column', flexGrow: 1, overflow: 'hidden' }}>
        <Topbar />

        <Box component="main" sx={{ flexGrow: 1, p: 4, overflow: 'auto' }}>
          <Outlet />
        </Box>
      </Box>
    </Box>
  );
}

