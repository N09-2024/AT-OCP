import React, { useState, useEffect, useRef } from 'react';
import {
  Box,
  Typography,
  Paper,
  Button,
  Grid,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  TextField,
  Alert,
  CircularProgress,
} from '@mui/material';
import PictureAsPdfIcon from '@mui/icons-material/PictureAsPdf';
import DescriptionIcon from '@mui/icons-material/Description';
import EditIcon from '@mui/icons-material/Edit';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import SignaturePad from './SignaturePad';
import { apiClient } from '../../services/apiClient';
import { useAuthStore } from '../../store/authStore';

import jsPDF from 'jspdf';
import html2canvas from 'html2canvas';
import { Document, Packer, Paragraph, TextRun, AlignmentType } from 'docx';

export interface FormulaireOCPInteractiveProps {
  initialData?: any;
  readOnly?: boolean;
  onSave?: (formData: any) => Promise<void>;
  onSubmitAT?: (formData: any, signatureBlob?: Blob) => Promise<void>;
  loading?: boolean;
}

export default function FormulaireOCPInteractive({
  initialData = {},
  readOnly = false,
  onSave,
  onSubmitAT,
  loading = false,
}: FormulaireOCPInteractiveProps) {
  const currentUser = useAuthStore((s) => s.user);
  const containerRef = useRef<HTMLDivElement>(null);

  // Lists for dropdowns loaded from backend
  const [zonesList, setZonesList] = useState<any[]>([]);
  const [servicesList, setServicesList] = useState<any[]>([]);
  const [entreprisesList, setEntreprisesList] = useState<any[]>([]);
  const [usersList, setUsersList] = useState<any[]>([]);
  const [docSourceType, setDocSourceType] = useState<'DI' | 'OT' | 'BT'>('DI');
  const [docSourceList, setDocSourceList] = useState<any[]>([]);

  // Referentiels for checkboxes
  const [refRisques, setRefRisques] = useState<any[]>([]);
  const [refMesures, setRefMesures] = useState<any[]>([]);
  const [refEpis, setRefEpis] = useState<any[]>([]);
  const [refMoyens, setRefMoyens] = useState<any[]>([]);
  const [refPermis, setRefPermis] = useState<any[]>([]);

  // Signature Modal State
  const [sigDialogOpen, setSigDialogOpen] = useState(false);
  const [activeSigField, setActiveSigField] = useState<string | null>(null);

  // Form State
  const todayStr = new Date().toISOString().split('T')[0];
  const [formData, setFormData] = useState({
    numero: initialData.numero || '',
    site: initialData.zoneProprietaire?.nomZone || currentUser?.service?.zone?.nomZone || '',
    entite: initialData.servicesIntervenants || currentUser?.service?.nomService || '',
    documentSourceType: initialData.typeDocumentSource || 'DI',
    documentSourceId: initialData.documentSourceId || '',
    documentSourceNumero: initialData.documentSourceNumero || '',
    di: initialData.typeDocumentSource === 'DI' ? initialData.documentSourceNumero || '' : '',
    ot: initialData.typeDocumentSource === 'OT' ? initialData.documentSourceNumero || '' : '',
    bt: initialData.typeDocumentSource === 'BT' ? initialData.documentSourceNumero || '' : '',
    lieu: initialData.zoneProprietaire?.nomZone || '',
    servicesIntervenants: initialData.servicesIntervenants || '',
    entreprisesIntervenantes: initialData.entreprisesIntervenantes || '',
    description: initialData.objet || initialData.descriptionTravaux || '',
    dateIntervention: initialData.dateDebut || todayStr,
    heureDebut: initialData.heureDebut || '08:00',
    heureFin: initialData.heureFin || '17:00',

    // Checkboxes arrays (IDs)
    risquesIds: (initialData.risques || []).map((r: any) => r.id),
    mesuresIds: (initialData.mesures || []).map((m: any) => m.id),
    episIds: (initialData.epis || []).map((e: any) => e.id),
    moyensAccesIds: (initialData.moyensAcces || []).map((ma: any) => ma.id),
    permisIds: (initialData.permis || []).map((p: any) => p.id),

    // Textes
    sectionF: initialData.mesuresSecuriteExecutant || '',

    // Section G Visas (1er poste, 2ème, 3ème)
    g1NomCeep: initialData.g1NomCeep || `${currentUser?.prenom || ''} ${currentUser?.nom || ''}`,
    g1VisaCeep: initialData.g1VisaCeep || null,
    g1NomCeee: initialData.g1NomCeee || '',
    g1VisaCeee: initialData.g1VisaCeee || null,

    g2NomCeep: initialData.g2NomCeep || '',
    g2VisaCeep: initialData.g2VisaCeep || null,
    g2NomCeee: initialData.g2NomCeee || '',
    g2VisaCeee: initialData.g2VisaCeee || null,

    g3NomCeep: initialData.g3NomCeep || '',
    g3VisaCeep: initialData.g3VisaCeep || null,
    g3NomCeee: initialData.g3NomCeee || '',
    g3VisaCeee: initialData.g3VisaCeee || null,

    // Réception
    dateReception: initialData.dateReception || todayStr,
    heureReception: initialData.heureReception || '17:00',
    remiseEnPlace: initialData.remiseEnPlace || [] as string[],
    essaiConcluant: initialData.essaiConcluant || 'oui',
    valCeep: initialData.valCeep || '',
    valCeee: initialData.valCeee || '',
    valSt: initialData.valSt || '',
  });

  const [sigBlobs, setSigBlobs] = useState<Record<string, Blob>>({});

  // Fetch referentiels on mount
  useEffect(() => {
    const extractList = (res: any) => (Array.isArray(res.data) ? res.data : res.data?.content || []);
    apiClient.get('/zones').then((res) => setZonesList(extractList(res))).catch(() => {});
    apiClient.get('/services').then((res) => setServicesList(extractList(res))).catch(() => {});
    apiClient.get('/entreprises-externes').then((res) => setEntreprisesList(extractList(res))).catch(() => {});
    apiClient.get('/users?size=200').then((res) => setUsersList(extractList(res))).catch(() => {});

    apiClient.get('/risques').then((res) => setRefRisques(extractList(res))).catch(() => {});
    apiClient.get('/mesures-preparation').then((res) => setRefMesures(extractList(res))).catch(() => {});
    apiClient.get('/epis').then((res) => setRefEpis(extractList(res))).catch(() => {});
    apiClient.get('/moyens-acces').then((res) => setRefMoyens(extractList(res))).catch(() => {});
    apiClient.get('/types-permis').then((res) => setRefPermis(extractList(res))).catch(() => {});
  }, []);

  // Fetch document sources when docType changes
  useEffect(() => {
    let endpoint = '/demandes-intervention';
    if (docSourceType === 'OT') endpoint = '/ordres-travail';
    if (docSourceType === 'BT') endpoint = '/bons-travail';

    apiClient
      .get(`${endpoint}?page=0&size=100`)
      .then((res) => {
        const list = Array.isArray(res.data) ? res.data : res.data?.content || [];
        setDocSourceList(list);
      })
      .catch(() => setDocSourceList([]));
  }, [docSourceType]);

  // Handle document source selection (Auto-population)
  const handleSelectDocSource = (docId: string) => {
    const found = docSourceList.find((d) => d.id === docId);
    if (!found) return;

    setFormData((prev) => ({
      ...prev,
      documentSourceId: found.id,
      documentSourceNumero: found.numero || '',
      di: docSourceType === 'DI' ? found.numero || '' : '',
      ot: docSourceType === 'OT' ? found.numero || '' : '',
      bt: docSourceType === 'BT' ? found.numero || '' : '',
      description: found.objet || found.description || prev.description,
      lieu: found.equipement?.installation?.zone?.nomZone || found.installation?.zone?.nomZone || prev.lieu,
    }));
  };

  // Toggle checkbox helper
  const toggleCheckbox = (field: 'risquesIds' | 'mesuresIds' | 'episIds' | 'moyensAccesIds' | 'permisIds', id: string) => {
    if (readOnly) return;
    setFormData((prev) => {
      const list = prev[field] as string[];
      return list.includes(id) ? { ...prev, [field]: list.filter((i: string) => i !== id) } : { ...prev, [field]: [...list, id] };
    });
  };

  // Toggle Remise en place checkbox
  const toggleRemiseEnPlace = (itemKey: string) => {
    if (readOnly) return;
    setFormData((prev) => {
      const list = prev.remiseEnPlace;
      return list.includes(itemKey) ? { ...prev, remiseEnPlace: list.filter((i: string) => i !== itemKey) } : { ...prev, remiseEnPlace: [...list, itemKey] };
    });
  };

  // Signature handling
  const handleOpenSignature = (fieldKey: string) => {
    if (readOnly) return;
    setActiveSigField(fieldKey);
    setSigDialogOpen(true);
  };

  const handleSaveSignature = (blob: Blob, dataUrl: string) => {
    if (!activeSigField) return;
    setFormData((prev) => ({ ...prev, [activeSigField]: dataUrl }));
    setSigBlobs((prev) => ({ ...prev, [activeSigField]: blob }));
    setSigDialogOpen(false);
  };

  // Export PDF
  const exportPDF = async () => {
    if (!containerRef.current) return;
    try {
      const canvas = await html2canvas(containerRef.current, {
        scale: 2,
        useCORS: true,
        backgroundColor: '#ffffff',
        logging: false,
      });

      const pdf = new jsPDF({ orientation: 'portrait', unit: 'mm', format: 'a4' });
      const pw = pdf.internal.pageSize.getWidth();
      const ph = pdf.internal.pageSize.getHeight();
      const iw = pw - 10;
      const pageH_px = Math.floor(((ph - 10) / iw) * canvas.width);
      let y = 0,
        page = 0;

      while (y < canvas.height) {
        if (page > 0) pdf.addPage();
        const sliceH = Math.min(pageH_px, canvas.height - y);
        const sl = document.createElement('canvas');
        sl.width = canvas.width;
        sl.height = sliceH;
        const ctx = sl.getContext('2d');
        if (ctx) ctx.drawImage(canvas, 0, y, canvas.width, sliceH, 0, 0, canvas.width, sliceH);
        const sliceIH = (sliceH / canvas.width) * iw;
        pdf.addImage(sl.toDataURL('image/png'), 'PNG', 5, 5, iw, sliceIH);
        y += sliceH;
        page++;
      }

      const num = (formData.numero || 'AT').replace(/[^a-zA-Z0-9-]/g, '_');
      pdf.save(`Formulaire_AT_OCP_${num}_F-HSE-SEC-31-04.pdf`);
    } catch (e: any) {
      alert('Erreur lors de l\'export PDF : ' + e.message);
    }
  };

  // Export Word (.docx)
  const exportWord = async () => {
    const v = (val: string) => val || '.......................';
    const c = (checked: boolean) => (checked ? '[X]' : '[ ]');

    const isRisk = (id: string) => formData.risquesIds.includes(id);
    const isMesure = (id: string) => formData.mesuresIds.includes(id);
    const isEpi = (id: string) => formData.episIds.includes(id);
    const isMoyen = (id: string) => formData.moyensAccesIds.includes(id);
    const isPermis = (id: string) => formData.permisIds.includes(id);

    const doc = new Document({
      sections: [
        {
          children: [
            new Paragraph({
              children: [new TextRun({ text: 'F-HSE-SEC-31-04 | Edition 1.0 | 01/07/2016', size: 14, color: '666666' })],
              alignment: AlignmentType.RIGHT,
            }),
            new Paragraph({
              children: [new TextRun({ text: 'FORMULAIRE - Autorisation de travail OCP', bold: true, size: 28 })],
              alignment: AlignmentType.CENTER,
            }),
            new Paragraph({ text: '' }),
            new Paragraph({ children: [new TextRun({ text: 'Autorisation de travail n° : ', bold: true }), new TextRun(v(formData.numero))] }),
            new Paragraph({ children: [new TextRun({ text: 'Site : ', bold: true }), new TextRun(v(formData.site)), new TextRun('   Entité : '), new TextRun(v(formData.entite))] }),
            new Paragraph({ children: [new TextRun({ text: 'DI n° : ', bold: true }), new TextRun(v(formData.di)), new TextRun('   OT n° : '), new TextRun(v(formData.ot)), new TextRun('   BT n° : '), new TextRun(v(formData.bt))] }),
            new Paragraph({ children: [new TextRun({ text: 'Lieu : ', bold: true }), new TextRun(v(formData.lieu))] }),
            new Paragraph({ children: [new TextRun({ text: 'Services : ', bold: true }), new TextRun(v(formData.servicesIntervenants))] }),
            new Paragraph({ children: [new TextRun({ text: 'Entreprises : ', bold: true }), new TextRun(v(formData.entreprisesIntervenantes))] }),
            new Paragraph({ children: [new TextRun({ text: 'Description : ', bold: true }), new TextRun(v(formData.description))] }),
            new Paragraph({ children: [new TextRun({ text: 'Date : ', bold: true }), new TextRun(v(formData.dateIntervention)), new TextRun('  Début : '), new TextRun(v(formData.heureDebut)), new TextRun('  Fin : '), new TextRun(v(formData.heureFin))] }),
            new Paragraph({ text: '' }),

            new Paragraph({ children: [new TextRun({ text: 'A- RISQUES ÉVALUÉS', bold: true, size: 18 })] }),
            ...refRisques.map((r) => new Paragraph({ children: [new TextRun(`${c(isRisk(r.id))} ${r.nomRisque}`)] })),
            new Paragraph({ text: '' }),

            new Paragraph({ children: [new TextRun({ text: 'B- MESURES DE PRÉPARATION', bold: true, size: 18 })] }),
            ...refMesures.map((m) => new Paragraph({ children: [new TextRun(`${c(isMesure(m.id))} ${m.nomMesure}`)] })),
            new Paragraph({ text: '' }),

            new Paragraph({ children: [new TextRun({ text: 'C- MOYENS D\'ACCÈS', bold: true, size: 18 })] }),
            ...refMoyens.map((ma) => new Paragraph({ children: [new TextRun(`${c(isMoyen(ma.id))} ${ma.nomMoyen}`)] })),
            new Paragraph({ text: '' }),

            new Paragraph({ children: [new TextRun({ text: 'D- EPI SPÉCIFIQUES', bold: true, size: 18 })] }),
            ...refEpis.map((e) => new Paragraph({ children: [new TextRun(`${c(isEpi(e.id))} ${e.nomEPI}`)] })),
            new Paragraph({ text: '' }),

            new Paragraph({ children: [new TextRun({ text: 'E- PERMIS NÉCESSAIRES', bold: true, size: 18 })] }),
            ...refPermis.map((p) => new Paragraph({ children: [new TextRun(`${c(isPermis(p.id))} ${p.nom}`)] })),
            new Paragraph({ text: '' }),

            new Paragraph({ children: [new TextRun({ text: 'F- MESURES SÉCURITÉ EXÉCUTANT', bold: true, size: 18 })] }),
            new Paragraph({ children: [new TextRun(v(formData.sectionF))] }),
            new Paragraph({ text: '' }),

            new Paragraph({ children: [new TextRun({ text: 'G- VALIDATION DE L\'AUTORISATION DE TRAVAIL', bold: true, size: 18 })] }),
            new Paragraph({ text: `1er poste — CEEP: ${v(formData.g1NomCeep)}  Visa: ${formData.g1VisaCeep ? 'Signé' : 'Non signé'}   CEEE: ${v(formData.g1NomCeee)}  Visa: ${formData.g1VisaCeee ? 'Signé' : 'Non signé'}` }),
            new Paragraph({ text: `2ème poste — CEEP: ${v(formData.g2NomCeep)}  Visa: ${formData.g2VisaCeep ? 'Signé' : 'Non signé'}   CEEE: ${v(formData.g2NomCeee)}  Visa: ${formData.g2VisaCeee ? 'Signé' : 'Non signé'}` }),
            new Paragraph({ text: `3ème poste — CEEP: ${v(formData.g3NomCeep)}  Visa: ${formData.g3VisaCeep ? 'Signé' : 'Non signé'}   CEEE: ${v(formData.g3NomCeee)}  Visa: ${formData.g3VisaCeee ? 'Signé' : 'Non signé'}` }),
            new Paragraph({ text: '' }),

            new Paragraph({ children: [new TextRun({ text: 'RÉCEPTION DES TRAVAUX', bold: true, size: 18 })] }),
            new Paragraph({ text: `Date réception : ${v(formData.dateReception)}   Heure : ${v(formData.heureReception)}` }),
            new Paragraph({ text: `Essai concluant : ${formData.essaiConcluant === 'oui' ? 'OUI' : 'NON'}` }),
            new Paragraph({ text: `Validation CEEP : ${v(formData.valCeep)}   CEEE : ${v(formData.valCeee)}   Sous-traitant : ${v(formData.valSt)}` }),
          ],
        },
      ],
    });

    const num = (formData.numero || 'AT').replace(/[^a-zA-Z0-9-]/g, '_');
    const blob = await Packer.toBlob(doc);
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `Formulaire_AT_OCP_${num}_F-HSE-SEC-31-04.docx`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  };

  return (
    <Box sx={{ pb: 4 }}>
      {/* TOOLBAR EXPORTS & ACTIONS */}
      <Paper
        elevation={2}
        sx={{
          p: 1.5,
          mb: 2,
          display: 'flex',
          justify: 'space-between',
          alignItems: 'center',
          flexWrap: 'wrap',
          gap: 1.5,
          bgcolor: '#1e293b',
          color: '#ffffff',
          borderRadius: 2,
        }}
      >
        <Box sx={{ display: 'flex', gap: 1, alignItems: 'center' }}>
          <Button variant="contained" color="error" startIcon={<PictureAsPdfIcon />} onClick={exportPDF} size="small">
            Exporter PDF
          </Button>
          <Button variant="contained" color="primary" startIcon={<DescriptionIcon />} onClick={exportWord} size="small">
            Exporter Word (.docx)
          </Button>
        </Box>

        <Typography variant="body2" sx={{ fontWeight: 700, color: '#94a3b8' }}>
          Standard OCP S-HSE-SEC-31 &mdash; Formulaire F-HSE-SEC-31-04
        </Typography>

        {!readOnly && (
          <Box sx={{ display: 'flex', gap: 1 }}>
            {onSave && (
              <Button variant="outlined" color="inherit" onClick={() => onSave(formData)} disabled={loading} size="small">
                Enregistrer Brouillon
              </Button>
            )}
            {onSubmitAT && (
              <Button
                variant="contained"
                color="success"
                startIcon={<CheckCircleIcon />}
                onClick={() => onSubmitAT(formData, sigBlobs['g1VisaCeep'] || sigBlobs['g1VisaCeee'])}
                disabled={loading}
                size="small"
              >
                Signer & Transmettre AT
              </Button>
            )}
          </Box>
        )}
      </Paper>

      {/* DOCUMENT PAPER F-HSE-SEC-31-04 */}
      <Paper
        ref={containerRef}
        elevation={4}
        sx={{
          maxWidth: 920,
          mx: 'auto',
          bgcolor: '#ffffff',
          border: '2px solid #000000',
          borderRadius: 0,
          p: 2,
          fontFamily: 'Arial, sans-serif',
          color: '#000000',
        }}
      >
        {/* HEADER BLOCK */}
        <Box sx={{ border: '2px solid #000', mb: 1, display: 'grid', gridTemplateColumns: '90px 1fr 165px' }}>
          <Box sx={{ borderRight: '2px solid #000', p: 1, textAlign: 'center', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center' }}>
            <svg width="45" height="45" viewBox="0 0 100 100">
              <polygon points="50,5 61,38 96,38 68,58 79,91 50,71 21,91 32,58 4,38 39,38" fill="#00875A" />
              <circle cx="50" cy="55" r="14" fill="white" />
              <circle cx="50" cy="55" r="9" fill="#00875A" />
            </svg>
            <Typography variant="caption" sx={{ fontWeight: 900, color: '#00875A', fontSize: 13, display: 'block', mt: 0.5 }}>
              OCP
            </Typography>
          </Box>

          <Box sx={{ borderRight: '2px solid #000', p: 1, textAlign: 'center', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center' }}>
            <Typography variant="subtitle1" sx={{ fontWeight: 900, textTransform: 'uppercase', letterSpacing: 0.5, fontSize: 13, lineHeight: 1 }}>
              FORMULAIRE
            </Typography>
            <Typography variant="h6" sx={{ fontWeight: 900, fontSize: 16, mt: 0.5 }}>
              Autorisation de travail
            </Typography>
          </Box>

          <Box sx={{ p: 1, fontSize: 10, lineHeight: 1.6 }}>
            <Typography variant="caption" sx={{ fontWeight: 900, fontSize: 11, display: 'block' }}>
              F-HSE-SEC-31-04
            </Typography>
            <div>Edition : 1.0</div>
            <div>Date : 01/07/2016</div>
            <div>Page : 1/2</div>
          </Box>
        </Box>

        {/* IDENTIFICATION FIELDS TABLE */}
        <Box sx={{ border: '2px solid #000', mb: 1, fontSize: 11 }}>
          {/* Row 1: Site / Entité / AT n° */}
          <Grid container sx={{ borderBottom: '1px solid #000' }}>
            <Grid size={6} sx={{ borderRight: '1px solid #000', p: 0.75, display: 'flex', alignItems: 'center', gap: 1 }}>
              <strong>Site :</strong>
              {readOnly ? (
                <span>{formData.site || '..............'}</span>
              ) : (
                <select
                  style={{ border: 'none', borderBottom: '1px solid #666', background: 'transparent', fontSize: '11px', width: '120px' }}
                  value={formData.site}
                  onChange={(e) => setFormData((p) => ({ ...p, site: e.target.value }))}
                >
                  <option value="">Sélectionner site...</option>
                  {zonesList.map((z) => (
                    <option key={z.id} value={z.nomZone}>
                      {z.nomZone}
                    </option>
                  ))}
                </select>
              )}
              &nbsp;&nbsp;
              <strong>Entité :</strong>
              {readOnly ? (
                <span>{formData.entite || '..............'}</span>
              ) : (
                <select
                  style={{ border: 'none', borderBottom: '1px solid #666', background: 'transparent', fontSize: '11px', width: '120px' }}
                  value={formData.entite}
                  onChange={(e) => setFormData((p) => ({ ...p, entite: e.target.value }))}
                >
                  <option value="">Sélectionner entité...</option>
                  {servicesList.map((s) => (
                    <option key={s.id} value={s.nomService}>
                      {s.nomService}
                    </option>
                  ))}
                </select>
              )}
            </Grid>

            <Grid size={6} sx={{ p: 0.75 }}>
              <strong>Autorisation de travail n° :</strong>{' '}
              <span style={{ color: '#00875A', fontWeight: 900 }}>{formData.numero || 'AT-2026-XXXX'}</span>
              <Typography variant="caption" sx={{ display: 'block', fontStyle: 'italic', fontSize: 9, color: '#64748b' }}>
                (Document valable pendant 24 heures à instruire sur le terrain)
              </Typography>
            </Grid>
          </Grid>

          {/* Row 2: Document Source (DI / OT / BT) */}
          <Grid container sx={{ borderBottom: '1px solid #000', bgcolor: '#f8fafc', p: 0.75, alignItems: 'center' }}>
            <Grid size={12}>
              {!readOnly && (
                <Box sx={{ display: 'flex', gap: 1, alignItems: 'center', mb: 0.5 }}>
                  <strong>Document source :</strong>
                  <select
                    style={{ fontSize: '11px', padding: '1px 4px' }}
                    value={docSourceType}
                    onChange={(e) => setDocSourceType(e.target.value as any)}
                  >
                    <option value="DI">Demande d'Intervention (DI)</option>
                    <option value="OT">Ordre de Travail (OT)</option>
                    <option value="BT">Bon de Travail (BT)</option>
                  </select>
                  <select
                    style={{ fontSize: '11px', padding: '1px 4px', maxWidth: '250px' }}
                    onChange={(e) => handleSelectDocSource(e.target.value)}
                  >
                    <option value="">Sélectionner un N° document source...</option>
                    {docSourceList.map((d) => (
                      <option key={d.id} value={d.id}>
                        {d.numero} &mdash; {d.objet || d.description}
                      </option>
                    ))}
                  </select>
                </Box>
              )}
              <strong>DI n° :</strong> {formData.di || '..........'} &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
              <strong>OT n° :</strong> {formData.ot || '..........'} &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
              <strong>BT n° :</strong> {formData.bt || '..........'}
            </Grid>
          </Grid>

          {/* Row 3: Lieu / Services / Entreprises */}
          <Grid container sx={{ borderBottom: '1px solid #000', p: 0.75, alignItems: 'center' }}>
            <Grid size={4}>
              <strong>Lieu d'intervention :</strong>{' '}
              {readOnly ? (
                formData.lieu || '..................'
              ) : (
                <select
                  style={{ border: 'none', borderBottom: '1px solid #666', background: 'transparent', fontSize: '11px', width: '130px' }}
                  value={formData.lieu}
                  onChange={(e) => setFormData((p) => ({ ...p, lieu: e.target.value }))}
                >
                  <option value="">Sélectionner lieu...</option>
                  {zonesList.map((z) => (
                    <option key={z.id} value={z.nomZone}>
                      {z.nomZone}
                    </option>
                  ))}
                </select>
              )}
            </Grid>
            <Grid size={4}>
              <strong>Services :</strong>{' '}
              {readOnly ? (
                formData.servicesIntervenants || '..................'
              ) : (
                <select
                  style={{ border: 'none', borderBottom: '1px solid #666', background: 'transparent', fontSize: '11px', width: '130px' }}
                  value={formData.servicesIntervenants}
                  onChange={(e) => setFormData((p) => ({ ...p, servicesIntervenants: e.target.value }))}
                >
                  <option value="">Sélectionner service...</option>
                  {servicesList.map((s) => (
                    <option key={s.id} value={s.nomService}>
                      {s.nomService}
                    </option>
                  ))}
                </select>
              )}
            </Grid>
            <Grid size={4}>
              <strong>Entreprises :</strong>{' '}
              {readOnly ? (
                formData.entreprisesIntervenantes || '..................'
              ) : (
                <select
                  style={{ border: 'none', borderBottom: '1px solid #666', background: 'transparent', fontSize: '11px', width: '130px' }}
                  value={formData.entreprisesIntervenantes}
                  onChange={(e) => setFormData((p) => ({ ...p, entreprisesIntervenantes: e.target.value }))}
                >
                  <option value="">Sélectionner entreprise...</option>
                  {entreprisesList.map((e) => (
                    <option key={e.id} value={e.nomEntreprise}>
                      {e.nomEntreprise}
                    </option>
                  ))}
                </select>
              )}
            </Grid>
          </Grid>

          {/* Row 4: Description / Date / Heure */}
          <Grid container sx={{ p: 0.75, alignItems: 'center' }}>
            <Grid size={7} sx={{ pr: 1, borderRight: '1px solid #000' }}>
              <strong>Description de l'intervention :</strong>
              {readOnly ? (
                <div style={{ fontSize: '11px', fontWeight: 600 }}>{formData.description || '...........................................'}</div>
              ) : (
                <input
                  type="text"
                  style={{ width: '100%', border: 'none', borderBottom: '1px solid #666', fontSize: '11px' }}
                  value={formData.description}
                  onChange={(e) => setFormData((p) => ({ ...p, description: e.target.value }))}
                  placeholder="Décrire l'intervention..."
                />
              )}
            </Grid>

            <Grid size={5} sx={{ pl: 1 }}>
              <strong>Date :</strong>{' '}
              {readOnly ? (
                formData.dateIntervention
              ) : (
                <input
                  type="date"
                  style={{ border: 'none', borderBottom: '1px solid #666', fontSize: '11px', width: '110px' }}
                  value={formData.dateIntervention}
                  onChange={(e) => setFormData((p) => ({ ...p, dateIntervention: e.target.value }))}
                />
              )}
              <br />
              <strong>H.début :</strong>{' '}
              {readOnly ? (
                formData.heureDebut
              ) : (
                <input
                  type="time"
                  style={{ border: 'none', borderBottom: '1px solid #666', fontSize: '11px', width: '70px' }}
                  value={formData.heureDebut}
                  onChange={(e) => setFormData((p) => ({ ...p, heureDebut: e.target.value }))}
                />
              )}
              &nbsp;&nbsp;
              <strong>H.fin :</strong>{' '}
              {readOnly ? (
                formData.heureFin
              ) : (
                <input
                  type="time"
                  style={{ border: 'none', borderBottom: '1px solid #666', fontSize: '11px', width: '70px' }}
                  value={formData.heureFin}
                  onChange={(e) => setFormData((p) => ({ ...p, heureFin: e.target.value }))}
                />
              )}
            </Grid>
          </Grid>
        </Box>

        {/* SECTION A: RISQUES ÉVALUÉS */}
        <Box sx={{ border: '2px solid #000', mb: 1 }}>
          <Box sx={{ bgcolor: '#c8c8c8', p: 0.5, borderBottom: '1.5px solid #000' }}>
            <Typography variant="subtitle2" sx={{ fontWeight: 900, fontSize: 11, color: '#000' }}>
              A- Risques évalués liés à / au
            </Typography>
          </Box>
          <Box sx={{ p: 1 }}>
            <Grid container spacing={1}>
              {refRisques.length > 0 ? (
                refRisques.map((r) => (
                  <Grid size={4} key={r.id}>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5, py: 0.25 }}>
                      <input
                        type="checkbox"
                        disabled={readOnly}
                        checked={formData.risquesIds.includes(r.id)}
                        onChange={() => toggleCheckbox('risquesIds', r.id)}
                        style={{ cursor: readOnly ? 'default' : 'pointer' }}
                      />
                      <label style={{ fontSize: '11px', cursor: readOnly ? 'default' : 'pointer' }}>{r.nomRisque}</label>
                    </Box>
                  </Grid>
                ))
              ) : (
                <Grid size={12}>
                  <Typography variant="caption" sx={{ fontStyle: 'italic' }}>
                    Chargement des risques...
                  </Typography>
                </Grid>
              )}
            </Grid>
          </Box>
        </Box>

        {/* SECTION B: MESURES PRISES */}
        <Box sx={{ border: '2px solid #000', mb: 1 }}>
          <Box sx={{ bgcolor: '#c8c8c8', p: 0.5, borderBottom: '1.5px solid #000' }}>
            <Typography variant="subtitle2" sx={{ fontWeight: 900, fontSize: 11, color: '#000' }}>
              B- Mesures prises pour préparer l'intervention
            </Typography>
          </Box>
          <Box sx={{ p: 1 }}>
            <Grid container spacing={1}>
              {refMesures.map((m) => (
                <Grid size={4} key={m.id}>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5, py: 0.25 }}>
                    <input
                      type="checkbox"
                      disabled={readOnly}
                      checked={formData.mesuresIds.includes(m.id)}
                      onChange={() => toggleCheckbox('mesuresIds', m.id)}
                      style={{ cursor: readOnly ? 'default' : 'pointer' }}
                    />
                    <label style={{ fontSize: '11px', cursor: readOnly ? 'default' : 'pointer' }}>{m.nomMesure}</label>
                  </Box>
                </Grid>
              ))}
            </Grid>
          </Box>
        </Box>

        {/* SECTION C: MOYENS D'ACCÈS */}
        <Box sx={{ border: '2px solid #000', mb: 1 }}>
          <Box sx={{ bgcolor: '#c8c8c8', p: 0.5, borderBottom: '1.5px solid #000' }}>
            <Typography variant="subtitle2" sx={{ fontWeight: 900, fontSize: 11, color: '#000' }}>
              C- Moyens d'accès nécessaires
            </Typography>
          </Box>
          <Box sx={{ p: 1 }}>
            <Grid container spacing={1}>
              {refMoyens.map((ma) => (
                <Grid size={4} key={ma.id}>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5, py: 0.25 }}>
                    <input
                      type="checkbox"
                      disabled={readOnly}
                      checked={formData.moyensAccesIds.includes(ma.id)}
                      onChange={() => toggleCheckbox('moyensAccesIds', ma.id)}
                      style={{ cursor: readOnly ? 'default' : 'pointer' }}
                    />
                    <label style={{ fontSize: '11px', cursor: readOnly ? 'default' : 'pointer' }}>{ma.nomMoyen}</label>
                  </Box>
                </Grid>
              ))}
            </Grid>
          </Box>
        </Box>

        {/* SECTION D: EPI SPÉCIFIQUES */}
        <Box sx={{ border: '2px solid #000', mb: 1 }}>
          <Box sx={{ bgcolor: '#c8c8c8', p: 0.5, borderBottom: '1.5px solid #000' }}>
            <Typography variant="subtitle2" sx={{ fontWeight: 900, fontSize: 11, color: '#000' }}>
              D- EPI spécifiques nécessaires
            </Typography>
          </Box>
          <Box sx={{ p: 1 }}>
            <Grid container spacing={1}>
              {refEpis.map((e) => (
                <Grid size={4} key={e.id}>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5, py: 0.25 }}>
                    <input
                      type="checkbox"
                      disabled={readOnly}
                      checked={formData.episIds.includes(e.id)}
                      onChange={() => toggleCheckbox('episIds', e.id)}
                      style={{ cursor: readOnly ? 'default' : 'pointer' }}
                    />
                    <label style={{ fontSize: '11px', cursor: readOnly ? 'default' : 'pointer' }}>{e.nomEPI}</label>
                  </Box>
                </Grid>
              ))}
            </Grid>
          </Box>
        </Box>

        {/* SECTION E: PERMIS NÉCESSAIRES */}
        <Box sx={{ border: '2px solid #000', mb: 1 }}>
          <Box sx={{ bgcolor: '#c8c8c8', p: 0.5, borderBottom: '1.5px solid #000' }}>
            <Typography variant="subtitle2" sx={{ fontWeight: 900, fontSize: 11, color: '#000' }}>
              E- Permis nécessaires
            </Typography>
          </Box>
          <Box sx={{ p: 1 }}>
            <Grid container spacing={1}>
              {refPermis.map((p) => (
                <Grid size={4} key={p.id}>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5, py: 0.25 }}>
                    <input
                      type="checkbox"
                      disabled={readOnly}
                      checked={formData.permisIds.includes(p.id)}
                      onChange={() => toggleCheckbox('permisIds', p.id)}
                      style={{ cursor: readOnly ? 'default' : 'pointer' }}
                    />
                    <label style={{ fontSize: '11px', cursor: readOnly ? 'default' : 'pointer' }}>{p.nom}</label>
                  </Box>
                </Grid>
              ))}
            </Grid>
          </Box>
        </Box>

        {/* SECTION F: MESURES SÉCURITÉ EXÉCUTANT */}
        <Box sx={{ border: '2px solid #000', mb: 1 }}>
          <Box sx={{ bgcolor: '#c8c8c8', p: 0.5, borderBottom: '1.5px solid #000' }}>
            <Typography variant="subtitle2" sx={{ fontWeight: 900, fontSize: 11, color: '#000' }}>
              F- Mesures de sécurité prises par l'exécutant (Référence du mode opératoire, ....)
            </Typography>
          </Box>
          <Box sx={{ p: 1 }}>
            {readOnly ? (
              <div style={{ fontSize: '11px' }}>{formData.sectionF || 'Conformité aux règles HSE du secteur.'}</div>
            ) : (
              <textarea
                rows={2}
                style={{ width: '100%', border: 'none', borderBottom: '1px solid #aaa', fontSize: '11px', resize: 'vertical' }}
                placeholder="Décrire les mesures de sécurité spécifiques..."
                value={formData.sectionF}
                onChange={(e) => setFormData((p) => ({ ...p, sectionF: e.target.value }))}
              />
            )}
          </Box>
        </Box>

        {/* SECTION G: VALIDATION (3 POSTES) AVEC SIGNATURE MANUELLE */}
        <Box sx={{ border: '2px solid #000', mb: 1 }}>
          <Box sx={{ bgcolor: '#c8c8c8', p: 0.5, borderBottom: '1.5px solid #000' }}>
            <Typography variant="subtitle2" sx={{ fontWeight: 900, fontSize: 11, color: '#000' }}>
              G- Validation de l'autorisation de travail
            </Typography>
          </Box>

          <Box sx={{ p: 1, overflowX: 'auto' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '11px' }}>
              <thead>
                <tr>
                  <th style={{ border: '1px solid #000', background: '#e0e0e0', padding: '3px', width: '33%' }}>1er poste</th>
                  <th style={{ border: 'none', width: '2%' }}></th>
                  <th style={{ border: '1px solid #000', background: '#e0e0e0', padding: '3px', width: '32%' }}>2ème poste</th>
                  <th style={{ border: 'none', width: '2%' }}></th>
                  <th style={{ border: '1px solid #000', background: '#e0e0e0', padding: '3px', width: '31%' }}>3ème poste</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  {/* Poste 1 */}
                  <td style={{ border: '1px solid #000', padding: '4px', verticalAlign: 'top' }}>
                    <div style={{ display: 'flex', gap: '4px', marginBottom: '4px' }}>
                      <strong style={{ width: '70px' }}>Nom CEEP:</strong>
                      {readOnly ? (
                        <span>{formData.g1NomCeep}</span>
                      ) : (
                        <select
                          style={{ fontSize: '10px', width: '130px' }}
                          value={formData.g1NomCeep}
                          onChange={(e) => setFormData((p) => ({ ...p, g1NomCeep: e.target.value }))}
                        >
                          <option value="">Sélectionner CEEP...</option>
                          {usersList.map((u) => (
                            <option key={u.id} value={`${u.prenom} ${u.nom}`}>
                              {u.prenom} {u.nom}
                            </option>
                          ))}
                        </select>
                      )}
                    </div>
                    <div style={{ display: 'flex', gap: '4px', alignItems: 'center' }}>
                      <strong style={{ width: '70px' }}>Visa CEEP:</strong>
                      {formData.g1VisaCeep ? (
                        <img src={formData.g1VisaCeep} alt="Visa CEEP" style={{ height: '28px', border: '1px solid #00875A' }} />
                      ) : (
                        <Button
                          size="small"
                          variant="outlined"
                          color="primary"
                          startIcon={<EditIcon sx={{ fontSize: 14 }} />}
                          sx={{ fontSize: 9, py: 0 }}
                          onClick={() => handleOpenSignature('g1VisaCeep')}
                          disabled={readOnly}
                        >
                          Signer manuellement
                        </Button>
                      )}
                    </div>

                    <div style={{ borderTop: '1px solid #ccc', marginTop: '6px', paddingTop: '4px' }}>
                      <div style={{ display: 'flex', gap: '4px', marginBottom: '4px' }}>
                        <strong style={{ width: '70px' }}>Nom CEEE:</strong>
                        {readOnly ? (
                          <span>{formData.g1NomCeee}</span>
                        ) : (
                          <select
                            style={{ fontSize: '10px', width: '130px' }}
                            value={formData.g1NomCeee}
                            onChange={(e) => setFormData((p) => ({ ...p, g1NomCeee: e.target.value }))}
                          >
                            <option value="">Sélectionner CEEE...</option>
                            {usersList.map((u) => (
                              <option key={u.id} value={`${u.prenom} ${u.nom}`}>
                                {u.prenom} {u.nom}
                              </option>
                            ))}
                          </select>
                        )}
                      </div>
                      <div style={{ display: 'flex', gap: '4px', alignItems: 'center' }}>
                        <strong style={{ width: '70px' }}>Visa CEEE:</strong>
                        {formData.g1VisaCeee ? (
                          <img src={formData.g1VisaCeee} alt="Visa CEEE" style={{ height: '28px', border: '1px solid #00875A' }} />
                        ) : (
                          <Button
                            size="small"
                            variant="outlined"
                            color="secondary"
                            startIcon={<EditIcon sx={{ fontSize: 14 }} />}
                            sx={{ fontSize: 9, py: 0 }}
                            onClick={() => handleOpenSignature('g1VisaCeee')}
                            disabled={readOnly}
                          >
                            Signer manuellement
                          </Button>
                        )}
                      </div>
                    </div>
                  </td>

                  <td style={{ border: 'none' }}></td>

                  {/* Poste 2 */}
                  <td style={{ border: '1px solid #000', padding: '4px', verticalAlign: 'top' }}>
                    <div style={{ display: 'flex', gap: '4px', marginBottom: '4px' }}>
                      <strong style={{ width: '70px' }}>Nom CEEP:</strong>
                      <input
                        type="text"
                        disabled={readOnly}
                        style={{ width: '110px', fontSize: '10px' }}
                        value={formData.g2NomCeep}
                        onChange={(e) => setFormData((p) => ({ ...p, g2NomCeep: e.target.value }))}
                      />
                    </div>
                    <div style={{ display: 'flex', gap: '4px', alignItems: 'center' }}>
                      <strong style={{ width: '70px' }}>Visa CEEP:</strong>
                      {formData.g2VisaCeep ? (
                        <img src={formData.g2VisaCeep} alt="Visa CEEP" style={{ height: '28px', border: '1px solid #00875A' }} />
                      ) : (
                        <Button
                          size="small"
                          variant="outlined"
                          sx={{ fontSize: 9, py: 0 }}
                          onClick={() => handleOpenSignature('g2VisaCeep')}
                          disabled={readOnly}
                        >
                          Signer
                        </Button>
                      )}
                    </div>
                  </td>

                  <td style={{ border: 'none' }}></td>

                  {/* Poste 3 */}
                  <td style={{ border: '1px solid #000', padding: '4px', verticalAlign: 'top' }}>
                    <div style={{ display: 'flex', gap: '4px', marginBottom: '4px' }}>
                      <strong style={{ width: '70px' }}>Nom CEEP:</strong>
                      <input
                        type="text"
                        disabled={readOnly}
                        style={{ width: '110px', fontSize: '10px' }}
                        value={formData.g3NomCeep}
                        onChange={(e) => setFormData((p) => ({ ...p, g3NomCeep: e.target.value }))}
                      />
                    </div>
                    <div style={{ display: 'flex', gap: '4px', alignItems: 'center' }}>
                      <strong style={{ width: '70px' }}>Visa CEEP:</strong>
                      {formData.g3VisaCeep ? (
                        <img src={formData.g3VisaCeep} alt="Visa CEEP" style={{ height: '28px', border: '1px solid #00875A' }} />
                      ) : (
                        <Button
                          size="small"
                          variant="outlined"
                          sx={{ fontSize: 9, py: 0 }}
                          onClick={() => handleOpenSignature('g3VisaCeep')}
                          disabled={readOnly}
                        >
                          Signer
                        </Button>
                      )}
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </Box>
        </Box>

        {/* RÉCEPTION DES TRAVAUX (BANNIÈRE ROUGE) */}
        <Box sx={{ border: '2px solid #000', mb: 1 }}>
          <Box sx={{ bgcolor: '#dc2626', color: '#ffffff', p: 0.5, borderBottom: '1.5px solid #000' }}>
            <Typography variant="subtitle2" sx={{ fontWeight: 900, fontSize: 11 }}>
              Réception des travaux
            </Typography>
          </Box>

          <Box sx={{ p: 1, fontSize: 11 }}>
            <Grid container spacing={1} sx={{ mb: 1 }}>
              <Grid size={6}>
                <strong>Date de réception :</strong>{' '}
                <input
                  type="date"
                  disabled={readOnly}
                  value={formData.dateReception}
                  onChange={(e) => setFormData((p) => ({ ...p, dateReception: e.target.value }))}
                  style={{ border: 'none', borderBottom: '1px solid #666', fontSize: '11px', width: '120px' }}
                />
              </Grid>
              <Grid size={6}>
                <strong>Heure de réception :</strong>{' '}
                <input
                  type="time"
                  disabled={readOnly}
                  value={formData.heureReception}
                  onChange={(e) => setFormData((p) => ({ ...p, heureReception: e.target.value }))}
                  style={{ border: 'none', borderBottom: '1px solid #666', fontSize: '11px', width: '80px' }}
                />
              </Grid>
            </Grid>

            <strong>Cocher en cas de remise en place :</strong>
            <Grid container spacing={1} sx={{ mt: 0.5 }}>
              {[
                'Boulonnerie',
                'Cache bride',
                'Support circuit',
                'Cache tambour',
                'Garde-corps',
                'Cache moteur',
                'Caillebotis',
                'Couvercle',
                "Arrêt d'urgence",
                'Cache compensateur',
                'Cache accouplement',
                'Trappe',
                'Capot convoyeur',
              ].map((item) => (
                <Grid size={4} key={item}>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                    <input
                      type="checkbox"
                      disabled={readOnly}
                      checked={formData.remiseEnPlace.includes(item)}
                      onChange={() => toggleRemiseEnPlace(item)}
                    />
                    <label style={{ fontSize: '11px' }}>{item}</label>
                  </Box>
                </Grid>
              ))}
            </Grid>

            {/* ESSAI CONCLUANT */}
            <Box sx={{ border: '1px solid #000', p: 0.75, mt: 1, bgcolor: '#fffbeb' }}>
              <Grid container spacing={1} sx={{ alignItems: 'center' }}>
                <Grid size={3}>
                  <strong>Essai concluant</strong>
                  <div style={{ display: 'flex', gap: '12px', marginTop: '4px' }}>
                    <label style={{ fontWeight: 700 }}>
                      <input
                        type="radio"
                        name="essai"
                        value="oui"
                        disabled={readOnly}
                        checked={formData.essaiConcluant === 'oui'}
                        onChange={() => setFormData((p) => ({ ...p, essaiConcluant: 'oui' }))}
                      />{' '}
                      Oui
                    </label>
                    <label style={{ fontWeight: 700 }}>
                      <input
                        type="radio"
                        name="essai"
                        value="non"
                        disabled={readOnly}
                        checked={formData.essaiConcluant === 'non'}
                        onChange={() => setFormData((p) => ({ ...p, essaiConcluant: 'non' }))}
                      />{' '}
                      Non
                    </label>
                  </div>
                </Grid>

                <Grid size={9}>
                  <Typography variant="caption" sx={{ fontStyle: 'italic', fontSize: 9, lineHeight: 1.1, display: 'block' }}>
                    ! Pour les modifications assujetties au Standard MOC, les exigences de ce standard, notamment celles du PSSR doivent être remplies avant d'entamer les essais.
                    <br />
                    La réception ne se fait que si les conditions de base sont assurées, que les mesures sécuritaires sont en place et que les essais sont concluants.
                  </Typography>
                </Grid>
              </Grid>
            </Box>
          </Box>
        </Box>

        {/* VALIDATION DE LA RÉCEPTION (BANNIÈRE ROUGE) */}
        <Box sx={{ border: '2px solid #000' }}>
          <Box sx={{ bgcolor: '#dc2626', color: '#ffffff', p: 0.5, borderBottom: '1.5px solid #000' }}>
            <Typography variant="subtitle2" sx={{ fontWeight: 900, fontSize: 11 }}>
              Validation de la réception
            </Typography>
          </Box>

          <Grid container sx={{ textAlign: 'center', fontSize: 11, fontWeight: 700 }}>
            <Grid size={4} sx={{ p: 1, borderRight: '1px solid #000' }}>
              <div>CEEP (Nom & Visa)</div>
              {formData.valCeep ? (
                <img src={formData.valCeep} alt="Visa CEEP" style={{ height: '30px', marginTop: '4px' }} />
              ) : (
                <Button size="small" sx={{ fontSize: 9, mt: 0.5 }} onClick={() => handleOpenSignature('valCeep')} disabled={readOnly}>
                  Signer CEEP
                </Button>
              )}
            </Grid>
            <Grid size={4} sx={{ p: 1, borderRight: '1px solid #000' }}>
              <div>CEEE (Nom & Visa)</div>
              {formData.valCeee ? (
                <img src={formData.valCeee} alt="Visa CEEE" style={{ height: '30px', marginTop: '4px' }} />
              ) : (
                <Button size="small" sx={{ fontSize: 9, mt: 0.5 }} onClick={() => handleOpenSignature('valCeee')} disabled={readOnly}>
                  Signer CEEE
                </Button>
              )}
            </Grid>
            <Grid size={4} sx={{ p: 1 }}>
              <div>Sous-traitant (Nom & Visa)</div>
              {formData.valSt ? (
                <img src={formData.valSt} alt="Visa Sous-traitant" style={{ height: '30px', marginTop: '4px' }} />
              ) : (
                <Button size="small" sx={{ fontSize: 9, mt: 0.5 }} onClick={() => handleOpenSignature('valSt')} disabled={readOnly}>
                  Signer Sous-traitant
                </Button>
              )}
            </Grid>
          </Grid>

          <Box sx={{ borderTop: '1px solid #000', p: 0.5, bgcolor: '#f8fafc' }}>
            <Typography variant="caption" sx={{ fontStyle: 'italic', fontSize: 9, textAlign: 'center', display: 'block' }}>
              N.B : Les 3 souches doivent être dûment instruites et visées
            </Typography>
          </Box>
        </Box>
      </Paper>

      {/* DIALOG FOR MANUAL SIGNATURE PAD */}
      <Dialog open={sigDialogOpen} onClose={() => setSigDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle sx={{ fontWeight: 800 }}>Visa avec Signature Manuelle</DialogTitle>
        <DialogContent dividers>
          <Alert severity="info" sx={{ mb: 2 }}>
            Dessinez votre signature manuscrite sur la zone ci-dessous à l'aide de votre souris ou écran tactile.
          </Alert>
          <SignaturePad onSave={handleSaveSignature} onClear={() => {}} title="Signer le Visa sur le Formulaire" />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setSigDialogOpen(false)}>Annuler</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}