import { useEffect, useState } from 'react';
import FormulaireOCPInteractive from './FormulaireOCPInteractive';
import { visaApi } from '../../services/visaApi';
import type { AutorisationTravail, Visa } from '../../types';
import { Box, CircularProgress } from '@mui/material';

interface FormulaireOCPViewerProps {
  at: AutorisationTravail;
  visas?: Visa[];
}

/**
 * Affiche le formulaire F-HSE-SEC-31-04 en lecture seule.
 * Les signatures sont chargées via Axios (JWT) car <img src="/api/..."> → 401.
 */
export default function FormulaireOCPViewer({ at, visas = [] }: FormulaireOCPViewerProps) {
  const [signatureUrls, setSignatureUrls] = useState<Record<string, string>>({});
  const [loadingSig, setLoadingSig] = useState(false);

  useEffect(() => {
    let cancelled = false;
    const created: string[] = [];

    async function load() {
      const withSig = visas.filter((v) => v.id && v.signaturePresente);
      if (withSig.length === 0) return;
      setLoadingSig(true);
      const map: Record<string, string> = {};
      await Promise.all(
        withSig.map(async (v) => {
          const url = await visaApi.fetchSignatureObjectUrl(v.id);
          if (url) {
            created.push(url);
            map[v.id] = url;
          }
        })
      );
      if (!cancelled) setSignatureUrls(map);
      setLoadingSig(false);
    }

    load();
    return () => {
      cancelled = true;
      created.forEach((u) => URL.revokeObjectURL(u));
    };
  }, [visas]);

  // Premier visa signé (ordre 1 / CEEP) pour section G
  const firstSigned = visas.find((v) => v.signaturePresente) || visas[0];
  const g1VisaCeep = firstSigned?.id ? signatureUrls[firstSigned.id] ?? null : null;

  const formattedData = {
    ...at,
    numero: at.numero,
    site: at.zoneProprietaire?.nomZone || '..................',
    entite: at.servicesIntervenants || '..................',
    lieu: at.zoneProprietaire?.nomZone || '..................',
    description: at.objet || at.descriptionTravaux || '',
    dateIntervention: at.dateDebut ? new Date(at.dateDebut).toISOString().split('T')[0] : '',
    heureDebut: at.heureDebut || '',
    heureFin: at.heureFin || '',
    risquesIds: (at.risques || []).map((r) => r.id),
    mesuresIds: (at.mesures || []).map((m) => m.id),
    episIds: (at.epis || []).map((e) => e.id),
    moyensAccesIds: (at.moyensAcces || []).map((ma) => ma.id),
    permisIds: (at.permis || []).map((p) => p.id),
    sectionF: at.mesuresSecuriteExecutant || '',
    g1NomCeep: at.proprietaireBrouillon
      ? `${at.proprietaireBrouillon.prenom} ${at.proprietaireBrouillon.nom}`
      : 'CEEP OCP',
    // blob: URL authentifiée — utilisable dans <img src>
    g1VisaCeep,
  };

  if (loadingSig) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
        <CircularProgress size={28} color="success" />
      </Box>
    );
  }

  return <FormulaireOCPInteractive initialData={formattedData} readOnly={true} />;
}
