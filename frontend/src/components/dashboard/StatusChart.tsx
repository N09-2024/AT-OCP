import { Card, Box, Typography } from '@mui/material';
import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip } from 'recharts';

interface StatusChartProps {
  data: Record<string, number>;
}

const STATUT_CONFIG: Record<string, { label: string; color: string }> = {
  BROUILLON: { label: 'Brouillon', color: '#9CA3AF' },
  SOUMISE: { label: 'Soumises', color: '#3B82F6' },
  EN_COURS: { label: 'En cours', color: '#16A34A' },
  VALIDEE: { label: 'Validées', color: '#009A44' },
  REJETEE: { label: 'Rejetées', color: '#DC2626' },
  RENOUVELEE: { label: 'Renouvelées', color: '#8B5CF6' },
  CLOTUREE: { label: 'Clôturées', color: '#4B5563' },
  ARCHIVEE: { label: 'Archivées', color: '#6B7280' },
  ANNULEE: { label: 'Annulées', color: '#EF4444' }
};

export default function StatusChart({ data: statusData }: StatusChartProps) {
  const data = Object.entries(statusData || {}).map(([key, value]) => ({
    name: STATUT_CONFIG[key]?.label || key,
    value: value,
    color: STATUT_CONFIG[key]?.color || '#CBD5E1',
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
