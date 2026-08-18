import { useEffect, useState } from 'react';
import FormulaireOCPInteractive from './FormulaireOCPInteractive';
import { visaApi } from '../../services/visaApi';
import type { AutorisationTravail, Visa } from '../../types';
import { Box, CircularProgress } from '@mui/material';

interface FormulaireOCPViewerProps {
  at: AutorisationTravail;
  visas?: Visa[];
  signMode?: 'ceep' | 'ceee' | 'all' | 'none';
  onVisaCeee?: (formData: any, blob: Blob) => Promise<void>;
}

/**
 * Affiche le formulaire F-HSE-SEC-31-04 en lecture seule.
 * Les signatures sont chargées via Axios (JWT) car <img src="/api/..."> → 401.
 */
export default function FormulaireOCPViewer({ at, visas = [], signMode, onVisaCeee }: FormulaireOCPViewerProps) {
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

  // Le PDF officiel devient visible dans le formulaire uniquement lorsque
  // les deux visas Haute Maîtrise (HMEP + HMEE) sont positifs.
  const isPositiveVisa = (v: Visa) => v.statut === 'VALIDE' || v.statut === 'VALIDATION' || v.statut === 'SIGNATURE';
  const hasHmepVisa = visas.some((v) =>
    isPositiveVisa(v) && (
      v.commentaire?.toUpperCase().includes('HMEP') ||
      (v as any).utilisateurNomComplet?.toUpperCase?.().includes('HMEP')
    )
  );
  const hasHmeeVisa = visas.some((v) =>
    isPositiveVisa(v) && (
      v.commentaire?.toUpperCase().includes('HMEE') ||
      (v as any).utilisateurNomComplet?.toUpperCase?.().includes('HMEE')
    )
  );

  const formattedData = {
    ...at,
    numero: at.numero,
    site: at.zoneProprietaire?.nomZone || (at as any).zoneProprietaireNom || (at as any).site || '',
    zoneProprietaireNom: at.zoneProprietaire?.nomZone || (at as any).zoneProprietaireNom || '',
    zoneProprietaireId: at.zoneProprietaire?.id || (at as any).zoneProprietaireId || '',
    entite: (at as any).serviceDemandeur || (at as any).entite || (at.proprietaireBrouillon?.service?.nomService) || '',
    serviceDemandeur: (at as any).serviceDemandeur || (at as any).entite || (at.proprietaireBrouillon?.service?.nomService) || '',
    zoneExecutanteNom: at.zoneExecutante?.nomZone || (at as any).zoneExecutanteNom || (at as any).lieu || '',
    zoneExecutante: at.zoneExecutante?.nomZone || (at as any).zoneExecutanteNom || (at as any).lieu || '',
    zoneExecutanteId: at.zoneExecutante?.id || (at as any).zoneExecutanteId || '',
    lieu: at.zoneExecutante?.nomZone || (at as any).zoneExecutanteNom || (at as any).lieu || '',
    servicesIntervenants: at.servicesIntervenants || (at as any).serviceIntervenant || '',
    serviceIntervenant: at.servicesIntervenants || (at as any).serviceIntervenant || '',
    entreprisesIntervenantes: at.entreprisesIntervenantes || '',
    description: at.objet || at.descriptionTravaux || '',
    dateIntervention: at.dateDebut ? new Date(at.dateDebut).toISOString().split('T')[0] : '',
    heureDebut: at.heureDebut || '',
    heureFin: at.heureFin || '',
    risquesIds: (at as any).risquesIds || (at.risques || []).map((r) => r.id),
    mesuresIds: (at as any).mesuresIds || (at.mesures || []).map((m) => m.id),
    episIds: (at as any).episIds || (at.epis || []).map((e) => e.id),
    moyensAccesIds: (at as any).moyensAccesIds || (at.moyensAcces || []).map((ma) => ma.id),
    permisIds: (at as any).permisIds || (at.permis || []).map((p) => p.typePermis?.id || p.id),
    sectionF: at.mesuresSecuriteExecutant || '',
    typeDocumentSource: at.typeDocumentSource || 'DI',
    documentSourceType: at.typeDocumentSource || 'DI',
    documentSourceId: (at as any).documentSourceId || '',
    documentSourceNumero: (at as any).documentSourceNumero || '',
    g1NomCeep: (at as any).g1NomCeep || ((at as any).proprietaireBrouillonNomComplet || (at.proprietaireBrouillon ? `${at.proprietaireBrouillon.prenom} ${at.proprietaireBrouillon.nom}` : 'CEEP OCP')),
    g1NomCeee: (at as any).g1NomCeee || '',
    latitude: (at as any).latitude || null,
    longitude: (at as any).longitude || null,
    visiteCommentaire: (at as any).visiteCommentaire || '',
    visiteEffectuee: (at as any).visiteEffectuee ?? true,
    photoPath: (at as any).photoPath || null,
    // blob: URL authentifiée - utilisable dans <img src>
    g1VisaCeep,
    hmepVisaSigne: hasHmepVisa,
    hmeeVisaSigne: hasHmeeVisa,
  };

  if (loadingSig) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
        <CircularProgress size={28} color="success" />
      </Box>
    );
  }

  return (
    <FormulaireOCPInteractive
      initialData={formattedData}
      readOnly={!signMode || signMode === 'none'}
      signMode={signMode ?? 'none'}
      onVisaCeee={
        onVisaCeee
          ? async (formData, blob) => onVisaCeee(formData, blob)  // ← wrap async
          : undefined
      }
    />
  );
}
