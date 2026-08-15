import { Card, Box, Typography, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Chip, IconButton } from '@mui/material';
import { EllipsisVerticalIcon, ArrowRightIcon } from '@heroicons/react/24/outline';
import { Link } from 'react-router-dom';

interface AtTableProps {
  data: Array<{
    id: string;
    titre: string;
    installation: string;
    statut: string;
    echeance: string;
  }>;
}

const STATUT_CONFIG: Record<string, { label: string; bg: string; color: string }> = {
  BROUILLON: { label: 'Brouillon', bg: '#EDF2EE', color: '#5C6E67' },
  SOUMISE: { label: 'Soumise', bg: '#DCEBE3', color: '#1F4D3E' },
  EN_COURS: { label: 'En cours', bg: '#DCEBE3', color: '#3C7A5C' },
  VALIDEE: { label: 'Validée', bg: '#DCEBE3', color: '#3C7A5C' },
  REJETEE: { label: 'Rejetée', bg: '#FEE2E2', color: '#9A3D2F' },
  RENOUVELEE: { label: 'Renouvelée', bg: '#E2F0E8', color: '#3C7A5C' },
  CLOTUREE: { label: 'Clôturée', bg: '#EDF2EE', color: '#5C6E67' },
  ARCHIVEE: { label: 'Archivée', bg: '#EDF2EE', color: '#5C6E67' },
  ANNULEE: { label: 'Annulée', bg: '#FEE2E2', color: '#9A3D2F' }
};

export default function AtTable({ data }: AtTableProps) {
  return (
    <Card sx={{ p: 0, overflow: 'hidden' }}>
      <Box sx={{ p: 3, display: 'flex', alignItems: 'center', justifyContent: 'space-between', borderBottom: '1px solid', borderColor: 'divider' }}>
        <Typography variant="h6" sx={{ fontWeight: 'bold' }}>
          Mes autorisations de travail
        </Typography>
        <Box
          component={Link}
          to="/autorisations"
          sx={{ display: 'flex', alignItems: 'center', gap: 1, color: 'primary.main', textDecoration: 'none', fontWeight: 500, fontSize: '0.875rem' }}
        >
          Voir toutes <ArrowRightIcon width={16} />
        </Box>
      </Box>
      <TableContainer>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>N° AT</TableCell>
              <TableCell>Titre</TableCell>
              <TableCell>Zone / Équipement</TableCell>
              <TableCell>Statut</TableCell>
              <TableCell>Échéance</TableCell>
              <TableCell align="right" />
            </TableRow>
          </TableHead>
          <TableBody>
            {data.map((row) => {
              const config = STATUT_CONFIG[row.statut] || { label: row.statut, bg: '#EDF2EE', color: '#5C6E67' };
              return (
                <TableRow key={row.id} sx={{ '&:last-child td, &:last-child th': { border: 0 } }}>
                  <TableCell sx={{ fontWeight: 500, fontSize: '0.85rem' }}>{row.id.substring(0, 8)}...</TableCell>
                  <TableCell>{row.titre}</TableCell>
                  <TableCell>{row.installation || '-'}</TableCell>
                  <TableCell>
                    <Chip
                      label={config.label}
                      size="small"
                      sx={{ bgcolor: config.bg, color: config.color, fontWeight: 'bold', borderRadius: 1 }}
                    />
                  </TableCell>
                  <TableCell>{row.echeance}</TableCell>
                  <TableCell align="right">
                    <IconButton size="small">
                      <EllipsisVerticalIcon width={20} />
                    </IconButton>
                  </TableCell>
                </TableRow>
              );
            })}
          </TableBody>
        </Table>
      </TableContainer>
    </Card>
  );
}
