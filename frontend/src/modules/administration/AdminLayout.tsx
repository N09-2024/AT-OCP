import { Outlet } from 'react-router-dom';
import { Box } from '@mui/material';

export default function AdminLayout() {
  return (
    <Box sx={{ flex: 1, minWidth: 0, p: 0 }}>
      <Outlet />
    </Box>
  );
}