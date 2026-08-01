import React from 'react';
import FormulaireOCPInteractive from './FormulaireOCPInteractive';
import { visaApi } from '../../services/visaApi';
import type { AutorisationTravail, Visa } from '../../types';

interface FormulaireOCPViewerProps {
  at: AutorisationTravail;
  visas?: Visa[];
}

export default function FormulaireOCPViewer({ at, visas = [] }: FormulaireOCPViewerProps) {
  // Map Visa data to Section G signature fields
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
    g1NomCeep: at.proprietaireBrouillon ? `${at.proprietaireBrouillon.prenom} ${at.proprietaireBrouillon.nom}` : 'CEEP OCP',
    g1VisaCeep: visas.length > 0 && visas[0].id ? visaApi.getSignatureImageUrl(visas[0].id) : null,
  };

  return <FormulaireOCPInteractive initialData={formattedData} readOnly={true} />;
}
