import { Card, Box, Typography } from '@mui/material';
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
} from 'recharts';

interface MonthlyChartProps {
  data: Array<{
    mois: string;
    total: number;
  }>;
}

export default function MonthlyChart({ data }: MonthlyChartProps) {
  return (
    <Card sx={{ p: 3, height: '100%', display: 'flex', flexDirection: 'column' }}>
      <Typography variant="h6" sx={{ fontWeight: 'bold', mb: 3 }}>
        Autorisations de travail par mois
      </Typography>
      <Box sx={{ flexGrow: 1, minHeight: 200 }}>
        <ResponsiveContainer width="100%" height="100%">
          <BarChart data={data} barSize={28}>
            <CartesianGrid strokeDasharray="3 3" stroke="#F3F4F6" vertical={false} />
            <XAxis dataKey="mois" axisLine={false} tickLine={false} tick={{ fontSize: 12, fill: '#6B7280' }} />
            <YAxis axisLine={false} tickLine={false} tick={{ fontSize: 12, fill: '#6B7280' }} />
            <Tooltip
              contentStyle={{ background: '#fff', border: '1px solid #E5E7EB', borderRadius: 8, boxShadow: '0 4px 6px -1px rgba(0,0,0,.1)' }}
              formatter={(value) => [`${value} AT`, 'Total']}
            />
            <Bar dataKey="total" fill="#009A44" radius={[6, 6, 0, 0]} />
          </BarChart>
        </ResponsiveContainer>
      </Box>
    </Card>
  );
}
