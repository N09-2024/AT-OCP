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
            <CartesianGrid strokeDasharray="3 3" stroke="#EDF2EE" vertical={false} />
            <XAxis dataKey="mois" axisLine={false} tickLine={false} tick={{ fontSize: 12, fill: '#5C6E67' }} />
            <YAxis axisLine={false} tickLine={false} tick={{ fontSize: 12, fill: '#5C6E67' }} />
            <Tooltip
              contentStyle={{ background: '#FFFFFF', border: '1px solid #D6E3DC', borderRadius: 8, boxShadow: '0 4px 6px -1px rgba(0,0,0,.1)' }}
              formatter={(value) => [`${value} AT`, 'Total']}
            />
            <Bar dataKey="total" fill="#3C7A5C" radius={[6, 6, 0, 0]} />
          </BarChart>
        </ResponsiveContainer>
      </Box>
    </Card>
  );
}
