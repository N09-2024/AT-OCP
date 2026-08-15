import { Card, Box, Typography } from '@mui/material';
import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip } from 'recharts';

interface StatusChartProps {
  data: Record<string, number>;
}

const STATUT_CONFIG: Record<string, { label: string; color: string }> = {
  BROUILLON: { label: 'Brouillon', color: '#5C6E67' },
  SOUMISE: { label: 'Soumises', color: '#1F4D3E' },
  EN_COURS: { label: 'En cours', color: '#3C7A5C' },
  VALIDEE: { label: 'Validées', color: '#3C7A5C' },
  REJETEE: { label: 'Rejetées', color: '#9A3D2F' },
  RENOUVELEE: { label: 'Renouvelées', color: '#3C7A5C' },
  CLOTUREE: { label: 'Clôturées', color: '#5C6E67' },
  ARCHIVEE: { label: 'Archivées', color: '#5C6E67' },
  ANNULEE: { label: 'Annulées', color: '#9A3D2F' }
};

export default function StatusChart({ data: statusData }: StatusChartProps) {
  const data = Object.entries(statusData || {}).map(([key, value]) => ({
    name: STATUT_CONFIG[key]?.label || key,
    value: value,
    color: STATUT_CONFIG[key]?.color || '#D6E3DC',
  }));

  const total = data.reduce((acc, item) => acc + item.value, 0);

  return (
    <Card sx={{ p: 3, height: '100%', display: 'flex', flexDirection: 'column' }}>
      <Typography variant="h6" sx={{ fontWeight: 'bold', mb: 3 }}>
        Répartition par statut
      </Typography>

      <Box sx={{ display: 'flex', alignItems: 'center', flexGrow: 1 }}>
        <Box sx={{ width: '50%', height: 180, position: 'relative' }}>
          <ResponsiveContainer width="100%" height="100%">
            <PieChart>
              <Pie data={data} innerRadius={55} outerRadius={75} paddingAngle={4} dataKey="value" stroke="none">
                {data.map((entry, index) => (
                  <Cell key={`cell-${index}`} fill={entry.color} />
                ))}
              </Pie>
              <Tooltip />
            </PieChart>
          </ResponsiveContainer>
        </Box>

        <Box sx={{ width: '50%', pl: 1 }}>
          {data.map((item, index) => (
            <Box key={index} sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 1.5 }}>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <Box sx={{ width: 10, height: 10, borderRadius: '50%', bgcolor: item.color, flexShrink: 0 }} />
                <Typography variant="body2" color="text.secondary" sx={{ fontSize: 12 }}>
                  {item.name}
                </Typography>
              </Box>
              <Box sx={{ display: 'flex', gap: 1, alignItems: 'center' }}>
                <Typography variant="body2" sx={{ fontWeight: 'bold' }}>
                  {item.value}
                </Typography>
                <Typography variant="caption" color="text.secondary">
                  ({Math.round((item.value / total) * 100)}%)
                </Typography>
              </Box>
            </Box>
          ))}
        </Box>
      </Box>
    </Card>
  );
}
