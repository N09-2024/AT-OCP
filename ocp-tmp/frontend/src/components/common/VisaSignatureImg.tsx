import { useEffect, useState } from 'react';
import { Box, CircularProgress } from '@mui/material';
import { visaApi } from '../../services/visaApi';

interface Props {
  visaId?: string | null;
  alt?: string;
  height?: number | string;
  style?: React.CSSProperties;
}

/**
 * Affiche une signature de visa en chargeant le PNG via Axios (JWT)
 * car <img src="/api/..."> ne transmet pas Authorization → 401.
 */
export default function VisaSignatureImg({ visaId, alt = 'Signature', height = 28, style }: Props) {
  const [src, setSrc] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!visaId) {
      setSrc(null);
      return;
    }
    let objectUrl: string | null = null;
    let cancelled = false;
    setLoading(true);
    visaApi
      .fetchSignatureObjectUrl(visaId)
      .then((url) => {
        if (cancelled) {
          if (url) URL.revokeObjectURL(url);
          return;
        }
        objectUrl = url;
        setSrc(url);
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
      if (objectUrl) URL.revokeObjectURL(objectUrl);
    };
  }, [visaId]);

  if (!visaId) return null;
  if (loading) {
    return (
      <Box sx={{ display: 'inline-flex', alignItems: 'center', height }}>
        <CircularProgress size={16} />
      </Box>
    );
  }
  if (!src) return null;
  return <img src={src} alt={alt} style={{ height, border: '1px solid #00875A', ...style }} />;
}
