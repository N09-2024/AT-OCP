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
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  TextField,
  Alert,
  CircularProgress,
  Chip,
  Card,
  CardContent,
  CardHeader,
  Divider,
  Stack,
  IconButton,
  Radio,
  RadioGroup,
  FormControlLabel,
} from '@mui/material';
import PictureAsPdfIcon from '@mui/icons-material/PictureAsPdf';
import DescriptionIcon from '@mui/icons-material/Description';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import AutoAwesomeIcon from '@mui/icons-material/AutoAwesome';
import WarningAmberIcon from '@mui/icons-material/WarningAmber';
import ShieldIcon from '@mui/icons-material/Shield';
import BuildIcon from '@mui/icons-material/Build';
import RuleIcon from '@mui/icons-material/Rule';
import VerifiedUserIcon from '@mui/icons-material/VerifiedUser';
import AssignmentIcon from '@mui/icons-material/Assignment';
import AccessTimeIcon from '@mui/icons-material/AccessTime';
import CheckBoxIcon from '@mui/icons-material/CheckBox';
import CheckBoxOutlineBlankIcon from '@mui/icons-material/CheckBoxOutlineBlank';
import DrawIcon from '@mui/icons-material/Draw';
import CloseIcon from '@mui/icons-material/Close';

import SignaturePad from './SignaturePad';
import { apiClient } from '../../services/apiClient';
import { iaApi } from '../../services/iaApi';
import { autorisationTravailApi } from '../../services/autorisationTravailApi';
import { useAuthStore } from '../../store/authStore';

import jsPDF from 'jspdf';
import { Document, Packer, Paragraph, TextRun, AlignmentType } from 'docx';

export interface FormulaireOCPInteractiveProps {
  initialData?: any;
  readOnly?: boolean;
  /** all = édition complète | ceee = CEEE vise seulement | none = lecture */
  signMode?: 'all' | 'ceep' | 'ceee' | 'none';
  onSave?: (formData: any) => Promise<void>;
  /** Sauvegarde automatique (cases cochées) sans bouton */
  onAutoSave?: (formData: any) => Promise<void>;
  onSubmitAT?: (formData: any, signatureBlob?: Blob) => Promise<void>;
  onVisaCeee?: (formData: any, signatureBlob: Blob) => Promise<void>;
  loading?: boolean;
}

export default function FormulaireOCPInteractive({
  initialData = {},
  readOnly = false,
  signMode = 'all',
  onSave,
  onAutoSave,
  onSubmitAT,
  onVisaCeee,
  loading = false,
}: FormulaireOCPInteractiveProps) {
  const currentUser = useAuthStore((s) => s.user);
  const containerRef = useRef<HTMLDivElement>(null);
  const fieldsLocked = readOnly || signMode === 'ceee' || signMode === 'none';
  const canSignCeep = !readOnly && (signMode === 'all' || signMode === 'ceep');
  const canSignCeee = !readOnly && (signMode === 'all' || signMode === 'ceee');

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
  const [iaLoading, setIaLoading] = useState(false);
  const [iaRapport, setIaRapport] = useState<string | null>(null);
  const [iaAlertes, setIaAlertes] = useState<string[]>([]);
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
    serviceIntervenantId: initialData.serviceIntervenantId || null,
    entreprisesIntervenantes: initialData.entreprisesIntervenantes || '',
    description: initialData.description || initialData.descriptionTravaux || initialData.objet || '',
    dateIntervention: initialData.dateDebut || todayStr,
    heureDebut: initialData.heureDebut || '08:00',
    heureFin: initialData.heureFin || '17:00',

    // Checkboxes arrays (IDs)
    risquesIds: initialData.risquesIds || (initialData.risques || []).map((r: any) => r.id) || [],
    mesuresIds: initialData.mesuresIds || (initialData.mesures || []).map((m: any) => m.id) || [],
    episIds: initialData.episIds || (initialData.epis || []).map((e: any) => e.id) || [],
    moyensAccesIds: initialData.moyensAccesIds || (initialData.moyensAcces || []).map((ma: any) => ma.id) || [],
    permisIds: initialData.permisIds || (initialData.permis || []).map((p: any) => p.typePermis?.id || p.id) || [],

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
    remiseEnPlace: initialData.remiseEnPlace || ([] as string[]),
    essaiConcluant: initialData.essaiConcluant || 'oui',
    valCeep: initialData.valCeep || '',
    valCeee: initialData.valCeee || '',
    valSt: initialData.valSt || '',
  });

  const [saveStatus, setSaveStatus] = useState<'idle' | 'saving' | 'saved' | 'error'>('idle');
  const [lastSavedTime, setLastSavedTime] = useState<string | null>(null);

  const autoSaveTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const autoSaveInFlightRef = useRef(false);
  const pendingDataRef = useRef<any>(null);

  const performAutoSave = async (snap: any) => {
    if (!onAutoSave) return;
    autoSaveInFlightRef.current = true;
    setSaveStatus('saving');
    try {
      await onAutoSave(snap);
      setSaveStatus('saved');
      const now = new Date();
      const timeStr = now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
      setLastSavedTime(timeStr);
    } catch (e) {
      console.error('Autosave error', e);
      setSaveStatus('error');
    } finally {
      autoSaveInFlightRef.current = false;
      if (pendingDataRef.current) {
        const nextSnap = pendingDataRef.current;
        pendingDataRef.current = null;
        performAutoSave(nextSnap);
      }
    }
  };

  const scheduleAutoSave = (snap: any) => {
    if (!onAutoSave) return;
    pendingDataRef.current = snap;
    if (autoSaveTimerRef.current) clearTimeout(autoSaveTimerRef.current);
    autoSaveTimerRef.current = setTimeout(() => {
      if (autoSaveInFlightRef.current) {
        return;
      }
      const dataToSave = pendingDataRef.current;
      pendingDataRef.current = null;
      performAutoSave(dataToSave);
    }, 800);
  };

  useEffect(() => {
    const handleBeforeUnloadOrVisibility = () => {
      if (pendingDataRef.current && onAutoSave && !autoSaveInFlightRef.current) {
        const snap = pendingDataRef.current;
        pendingDataRef.current = null;
        onAutoSave(snap).catch(() => {});
      }
    };
    const onVisibilityChange = () => {
      if (document.visibilityState === 'hidden') {
        handleBeforeUnloadOrVisibility();
      }
    };
    window.addEventListener('beforeunload', handleBeforeUnloadOrVisibility);
    document.addEventListener('visibilitychange', onVisibilityChange);
    return () => {
      window.removeEventListener('beforeunload', handleBeforeUnloadOrVisibility);
      document.removeEventListener('visibilitychange', onVisibilityChange);
    };
  }, [onAutoSave]);

  const updateTextValue = (field: string, value: any) => {
    if (readOnly || fieldsLocked) return;
    setFormData((prev: any) => {
      const updated = { ...prev, [field]: value };
      scheduleAutoSave(updated);
      return updated;
    });
  };

  // Hydrate when parent charges AT async
  useEffect(() => {
    if (!initialData || (!initialData._loaded && !initialData.numero && !initialData.description && !initialData.descriptionTravaux)) {
      return;
    }
    setFormData((prev: any) => ({
      ...prev,
      numero: initialData.numero || prev.numero || '',
      site: initialData.zoneProprietaire?.nomZone || initialData.site || prev.site || '',
      entite: initialData.servicesIntervenants || prev.entite || '',
      documentSourceType: initialData.typeDocumentSource || prev.documentSourceType || 'DI',
      documentSourceId: initialData.documentSourceId || prev.documentSourceId || '',
      documentSourceNumero: initialData.documentSourceNumero || prev.documentSourceNumero || '',
      di: initialData.typeDocumentSource === 'DI' ? initialData.documentSourceNumero || prev.di : prev.di,
      ot: initialData.typeDocumentSource === 'OT' ? initialData.documentSourceNumero || prev.ot : prev.ot,
      bt: initialData.typeDocumentSource === 'BT' ? initialData.documentSourceNumero || prev.bt : prev.bt,
      lieu: initialData.zoneProprietaire?.nomZone || prev.lieu || '',
      servicesIntervenants: initialData.servicesIntervenants || prev.servicesIntervenants || '',
      serviceIntervenantId: initialData.serviceIntervenantId || prev.serviceIntervenantId || null,
      entreprisesIntervenantes: initialData.entreprisesIntervenantes || prev.entreprisesIntervenantes || '',
      description: initialData.description || initialData.descriptionTravaux || initialData.objet || prev.description || '',
      dateIntervention: initialData.dateIntervention || initialData.dateDebut || prev.dateIntervention,
      heureDebut: initialData.heureDebut || prev.heureDebut || '08:00',
      heureFin: initialData.heureFin || prev.heureFin || '17:00',
      risquesIds: (initialData.risquesIds || (initialData.risques || []).map((r: any) => r.id) || prev.risquesIds || []).map((x: any) => String(x)),
      mesuresIds: (initialData.mesuresIds || (initialData.mesures || []).map((m: any) => m.id) || prev.mesuresIds || []).map((x: any) => String(x)),
      episIds: (initialData.episIds || (initialData.epis || []).map((e: any) => e.id) || prev.episIds || []).map((x: any) => String(x)),
      moyensAccesIds: (initialData.moyensAccesIds || (initialData.moyensAcces || []).map((m: any) => m.id) || prev.moyensAccesIds || []).map((x: any) => String(x)),
      permisIds: (initialData.permisIds || (initialData.permis || []).map((p: any) => p.typePermis?.id || p.id) || prev.permisIds || []).map((x: any) => String(x)),
      sectionF: initialData.sectionF || initialData.mesuresSecuriteExecutant || prev.sectionF || '',
      g1NomCeep: initialData.g1NomCeep || prev.g1NomCeep || '',
      g1NomCeee: initialData.g1NomCeee || prev.g1NomCeee || '',
      g1VisaCeep: initialData.g1VisaCeep || prev.g1VisaCeep || null,
      g1VisaCeee: initialData.g1VisaCeee || prev.g1VisaCeee || null,
    }));
  }, [initialData]);

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

  // Handle document source selection
  const handleSelectDocSource = (docId: string) => {
    const found = docSourceList.find((d) => d.id === docId);
    if (!found) return;
    const numDoc = found.numero || found.numDi || found.numOt || found.numBt || docId;
    setFormData((prev: any) => ({
      ...prev,
      documentSourceType: docSourceType,
      documentSourceId: docId,
      documentSourceNumero: numDoc,
      di: docSourceType === 'DI' ? numDoc : prev.di,
      ot: docSourceType === 'OT' ? numDoc : prev.ot,
      bt: docSourceType === 'BT' ? numDoc : prev.bt,
      description: found.objet || found.description || prev.description,
      lieu: found.equipement?.installation?.zone?.nomZone || found.installation?.zone?.nomZone || prev.lieu,
    }));
  };

  const handleSelectServiceIntervenant = async (serviceIdOrName: string) => {
    if (readOnly || signMode === 'ceee' || signMode === 'none') return;
    const found = servicesList.find((s) => s.id === serviceIdOrName || s.nomService === serviceIdOrName);
    const nomService = found?.nomService || serviceIdOrName;
    setFormData((p) => ({ ...p, servicesIntervenants: nomService, serviceIntervenantId: found?.id || null }));
    if (!found?.id) {
      setFormData((p) => ({ ...p, g1NomCeee: '' }));
      return;
    }
    try {
      const res = await apiClient.get(`/services/${found.id}/chefs-equipe`);
      const chefs = Array.isArray(res.data) ? res.data : [];
      const display = chefs.map((c: any) => c.displayName || `${c.prenom || ''} ${c.nom || ''}`.trim()).filter(Boolean).join(' / ');
      setFormData((p) => ({ ...p, g1NomCeee: display }));
    } catch { /* ignore */ }
  };

  // Toggle checkbox helper
  const normId = (id: any) => (id == null ? '' : String(id));
  const isChecked = (field: 'risquesIds' | 'mesuresIds' | 'episIds' | 'moyensAccesIds' | 'permisIds', id: any) => {
    const list = (formData[field] as any[]) || [];
    const sid = normId(id);
    return list.some((x) => normId(x) === sid);
  };

  const toggleCheckbox = (field: 'risquesIds' | 'mesuresIds' | 'episIds' | 'moyensAccesIds' | 'permisIds', id: string) => {
    if (readOnly || fieldsLocked) return;
    const sid = normId(id);
    if (!sid) return;
    setFormData((prev: any) => {
      const list = ((prev[field] as any[]) || []).map(normId);
      const next = list.includes(sid) ? list.filter((i: string) => i !== sid) : [...list, sid];
      const updated = { ...prev, [field]: next };
      scheduleAutoSave(updated);
      return updated;
    });
  };

  // Toggle Remise en place checkbox
  const toggleRemiseEnPlace = (itemKey: string) => {
    if (readOnly) return;
    setFormData((prev) => {
      const list = prev.remiseEnPlace;
      return list.includes(itemKey)
        ? { ...prev, remiseEnPlace: list.filter((i: string) => i !== itemKey) }
        : { ...prev, remiseEnPlace: [...list, itemKey] };
    });
  };

  /** IA Assistance */
  const handleAnalyserIA = async () => {
    if (fieldsLocked) return;
    const desc = formData.description || '';
    if (!desc.trim()) {
      alert("Saisissez d'abord la description de l'intervention.");
      return;
    }
    setIaLoading(true);
    setIaRapport(null);
    setIaAlertes([]);
    try {
      const res = await iaApi.analyserIntervention(desc);
      const matchIds = (list: any[], labels: string[]) => {
        const ids: string[] = [];
        for (const label of labels || []) {
          const low = label.toLowerCase();
          const found = list.find(
            (x) =>
              (x.nom || x.nomRisque || x.nomMesure || x.nomEPI || x.libelle || x.nomType || '')
                .toLowerCase()
                .includes(low.split(' ')[0]) ||
              low.includes(
                (x.nom || x.nomRisque || x.nomMesure || x.nomEPI || x.libelle || x.nomType || '').toLowerCase().slice(0, 6)
              )
          );
          if (found?.id && !ids.includes(found.id)) ids.push(found.id);
        }
        return ids;
      };
      setFormData((prev: any) => ({
        ...prev,
        risquesIds: Array.from(new Set([...(prev.risquesIds || []), ...matchIds(refRisques, res.risques)])),
        mesuresIds: Array.from(new Set([...(prev.mesuresIds || []), ...matchIds(refMesures, res.mesures)])),
        episIds: Array.from(new Set([...(prev.episIds || []), ...matchIds(refEpis, res.epis)])),
        permisIds: Array.from(new Set([...(prev.permisIds || []), ...matchIds(refPermis, res.permis)])),
      }));
      setIaRapport(res.rapport || 'Suggestions appliquées — vérifiez les cases A/B/D/E.');
      setIaAlertes(res.alertes || []);
    } catch (e: any) {
      alert(e.response?.data?.message || "Erreur analyse IA");
    } finally {
      setIaLoading(false);
    }
  };

  const handleOpenSignature = (fieldKey: string) => {
    const key = fieldKey.toLowerCase();
    const isCeeeField = key.includes('ceee');
    if (readOnly || signMode === 'none') return;
    if (signMode === 'ceee' && !isCeeeField) return;
    if (signMode === 'ceep' && isCeeeField) return;
    setActiveSigField(fieldKey);
    setSigDialogOpen(true);
  };

  const handleSaveSignature = (blob: Blob, dataUrl: string) => {
    if (!activeSigField) return;
    setFormData((prev) => ({ ...prev, [activeSigField]: dataUrl }));
    setSigBlobs((prev) => ({ ...prev, [activeSigField]: blob }));
    setSigDialogOpen(false);
  };

  // Export PDF Server
  const exportPDFServer = async () => {
    const atId = initialData?.id || formData.numero;
    if (!atId) {
      alert("Aucun ID d'AT disponible pour l'export PDF serveur.");
      return;
    }
    try {
      const blob = await autorisationTravailApi.exportPdf(initialData?.id || atId);
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `${formData.numero || 'AT'}.pdf`;
      a.click();
    } catch (err: any) {
      alert(err.response?.data?.message || err.message || "Export PDF refusé ou indisponible.");
    }
  };

  // Export Word (.docx)
  const exportWord = () => {
    const doc = new Document({
      sections: [
        {
          properties: {},
          children: [
            new Paragraph({
              alignment: AlignmentType.CENTER,
              children: [
                new TextRun({ text: "AUTORISATION DE TRAVAIL OCP", bold: true, size: 32, color: "00875A" }),
              ],
            }),
            new Paragraph({
              alignment: AlignmentType.CENTER,
              children: [
                new TextRun({ text: "Standard S-HSE-SEC-31 | Formulaire F-HSE-SEC-31-04", italics: true, size: 20 }),
              ],
            }),
            new Paragraph({ text: "" }),
            new Paragraph({ children: [new TextRun({ text: `Numéro AT: ${formData.numero || 'Brouillon'}`, bold: true })] }),
            new Paragraph({ children: [new TextRun({ text: `Site: ${formData.site || 'N/A'}` })] }),
            new Paragraph({ children: [new TextRun({ text: `Entité: ${formData.entite || 'N/A'}` })] }),
            new Paragraph({ children: [new TextRun({ text: `Description: ${formData.description || 'N/A'}` })] }),
            new Paragraph({ children: [new TextRun({ text: `Date intervention: ${formData.dateIntervention} (${formData.heureDebut} - ${formData.heureFin})` })] }),
          ],
        },
      ],
    });

    Packer.toBlob(doc).then((blob) => {
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `${formData.numero || 'AT'}.docx`;
      a.click();
      window.URL.revokeObjectURL(url);
    });
  };

  return (
    <Box sx={{ pb: 6, maxWidth: 1120, mx: 'auto' }}>
      {/* FLOATING ACTION TOOLBAR */}
      <Paper
        elevation={6}
        sx={{
          p: 2,
          mb: 3,
          position: 'sticky',
          top: 16,
          zIndex: 100,
          display: 'flex',
          justify: 'space-between',
          alignItems: 'center',
          flexWrap: 'wrap',
          gap: 1.5,
          bgcolor: '#0f172a',
          color: '#ffffff',
          borderRadius: 3,
          backdropFilter: 'blur(10px)',
          border: '1px solid rgba(255, 255, 255, 0.1)',
        }}
      >
        <Box sx={{ display: 'flex', gap: 1.5, alignItems: 'center' }}>
          <Button variant="contained" color="error" startIcon={<PictureAsPdfIcon />} onClick={exportPDFServer} size="small" sx={{ fontWeight: 700, borderRadius: 2 }}>
            PDF Officiel
          </Button>
          <Button variant="outlined" sx={{ color: '#94a3b8', borderColor: '#334155', fontWeight: 600, '&:hover': { borderColor: '#64748b', bgcolor: '#1e293b' } }} startIcon={<DescriptionIcon />} onClick={exportWord} size="small">
            Word (.docx)
          </Button>

          {/* AUTOSAVE CHIP */}
          {saveStatus === 'saving' && (
            <Chip label="Enregistrement..." color="info" size="small" icon={<CircularProgress size={12} color="inherit" />} sx={{ fontWeight: 700 }} />
          )}
          {saveStatus === 'saved' && (
            <Chip label={`Enregistré à ${lastSavedTime}`} color="success" size="small" icon={<CheckCircleIcon sx={{ fontSize: 14 }} />} sx={{ fontWeight: 700 }} />
          )}
          {saveStatus === 'error' && (
            <Chip label="Échec enregistrement" color="error" size="small" sx={{ fontWeight: 700 }} />
          )}
        </Box>

        <Typography variant="subtitle2" sx={{ fontWeight: 800, color: '#94a3b8', letterSpacing: 0.5, display: { xs: 'none', md: 'block' } }}>
          OCP S-HSE-SEC-31 &bull; F-HSE-SEC-31-04
        </Typography>

        {(!readOnly || signMode === 'ceee') && (
          <Box sx={{ display: 'flex', gap: 1 }}>
            {onSave && (
              <Button variant="outlined" sx={{ color: '#ffffff', borderColor: '#475569', '&:hover': { borderColor: '#94a3b8', bgcolor: '#1e293b' } }} onClick={() => onSave(formData)} disabled={loading} size="small">
                Brouillon
              </Button>
            )}
            {signMode !== 'ceee' && !fieldsLocked && (
              <Button
                variant="contained"
                sx={{ background: 'linear-gradient(135deg, #7c3aed 0%, #6d28d9 100%)', color: '#fff', fontWeight: 700 }}
                startIcon={iaLoading ? <CircularProgress size={14} color="inherit" /> : <AutoAwesomeIcon />}
                onClick={handleAnalyserIA}
                disabled={loading || iaLoading}
                size="small"
              >
                Assistant IA
              </Button>
            )}
            {onSubmitAT && signMode !== 'ceee' && (
              <Button
                variant="contained"
                sx={{ bgcolor: '#00875A', '&:hover': { bgcolor: '#006c48' }, fontWeight: 700, borderRadius: 2 }}
                startIcon={<CheckCircleIcon />}
                onClick={() => onSubmitAT(formData, sigBlobs['g1VisaCeep'])}
                disabled={loading}
                size="small"
              >
                Signer & Transmettre
              </Button>
            )}
            {onVisaCeee && signMode === 'ceee' && (
              <Button
                variant="contained"
                sx={{ bgcolor: '#00875A', '&:hover': { bgcolor: '#006c48' }, fontWeight: 700 }}
                startIcon={<CheckCircleIcon />}
                onClick={() => {
                  const blob = sigBlobs['g1VisaCeee'] || sigBlobs['valCeee'];
                  if (!blob) {
                    alert('Signez d\'abord la case Visa CEEE (section G).');
                    return;
                  }
                  onVisaCeee(formData, blob);
                }}
                disabled={loading}
                size="small"
              >
                Enregistrer visa CEEE
              </Button>
            )}
          </Box>
        )}
      </Paper>

      {/* IA SUGGESTION BANNER */}
      {(iaRapport || iaAlertes.length > 0) && (
        <Alert
          severity={iaAlertes.length ? 'warning' : 'success'}
          sx={{ mb: 3, borderRadius: 3, boxShadow: 2 }}
          onClose={() => {
            setIaRapport(null);
            setIaAlertes([]);
          }}
        >
          <Typography variant="subtitle2" sx={{ fontWeight: 800 }}>
            {iaRapport}
          </Typography>
          {iaAlertes.length > 0 && (
            <ul style={{ margin: '4px 0 0 16px', padding: 0 }}>
              {iaAlertes.map((a, i) => (
                <li key={i}>{a}</li>
              ))}
            </ul>
          )}
        </Alert>
      )}

      {/* FORM HERO HEADER BANNER */}
      <Paper
        elevation={3}
        sx={{
          p: 3,
          mb: 3,
          borderRadius: 3,
          background: 'linear-gradient(135deg, #00875A 0%, #004d33 100%)',
          color: '#ffffff',
          position: 'relative',
          overflow: 'hidden',
        }}
      >
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 2, position: 'relative', zIndex: 1 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
            <Box
              sx={{
                width: 56,
                height: 56,
                borderRadius: 2.5,
                bgcolor: 'rgba(255, 255, 255, 0.15)',
                backdropFilter: 'blur(10px)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                border: '1px solid rgba(255, 255, 255, 0.2)',
              }}
            >
              <svg width="36" height="36" viewBox="0 0 100 100">
                <polygon points="50,5 61,38 96,38 68,58 79,91 50,71 21,91 32,58 4,38 39,38" fill="#ffffff" />
                <circle cx="50" cy="55" r="14" fill="#00875A" />
                <circle cx="50" cy="55" r="9" fill="#ffffff" />
              </svg>
            </Box>
            <Box>
              <Typography variant="h5" sx={{ fontWeight: 900, textTransform: 'uppercase', letterSpacing: 0.5 }}>
                Autorisation de Travail
              </Typography>
              <Typography variant="body2" sx={{ opacity: 0.9 }}>
                Groupe OCP &bull; Standard S-HSE-SEC-31 &bull; Formulaire F-HSE-SEC-31-04
              </Typography>
            </Box>
          </Box>

          <Stack direction="row" spacing={1.5} sx={{ alignItems: 'center' }}>
            <Chip
              label={formData.numero ? `AT N° ${formData.numero}` : 'Brouillon en cours'}
              sx={{ bgcolor: 'rgba(255,255,255,0.2)', color: '#fff', fontWeight: 800, fontSize: 13, border: '1px solid rgba(255,255,255,0.3)' }}
            />
            <Chip label="Édition 1.0" sx={{ bgcolor: 'rgba(0,0,0,0.2)', color: '#fff', fontWeight: 600, fontSize: 12 }} />
          </Stack>
        </Box>
      </Paper>

      {/* SECTION 0: IDENTIFICATION & DOCUMENT SOURCE */}
      <Card sx={{ mb: 3, borderRadius: 3, border: '1px solid #e2e8f0', boxShadow: '0 4px 12px rgba(0,0,0,0.03)' }}>
        <CardHeader
          avatar={<AssignmentIcon sx={{ color: '#00875A' }} />}
          title={<Typography variant="h6" sx={{ fontWeight: 800, color: '#1e293b' }}>Identification & Document Source</Typography>}
          subheader="Rattachement administratif et périmètre de l'intervention"
          sx={{ bgcolor: '#f8fafc', borderBottom: '1px solid #e2e8f0', py: 1.5 }}
        />
        <CardContent sx={{ p: 3 }}>
          <Grid container spacing={2.5}>
            {/* Document Source Type Selector */}
            <Grid size={{ xs: 12, md: 4 }}>
              <Typography variant="caption" sx={{ fontWeight: 700, color: '#64748b', mb: 0.5, display: 'block' }}>
                Type Document Source
              </Typography>
              <Stack direction="row" spacing={1}>
                {(['DI', 'OT', 'BT'] as const).map((type) => (
                  <Chip
                    key={type}
                    label={type}
                    clickable={!fieldsLocked}
                    color={docSourceType === type ? 'primary' : 'default'}
                    onClick={() => {
                      if (fieldsLocked) return;
                      setDocSourceType(type);
                    }}
                    sx={{
                      fontWeight: 800,
                      px: 1.5,
                      bgcolor: docSourceType === type ? '#00875A' : '#f1f5f9',
                      color: docSourceType === type ? '#ffffff' : '#475569',
                    }}
                  />
                ))}
              </Stack>
            </Grid>

            {/* Document Source Select */}
            <Grid size={{ xs: 12, md: 8 }}>
              <FormControl fullWidth size="small" disabled={fieldsLocked}>
                <InputLabel>Sélectionner le document source ({docSourceType})</InputLabel>
                <Select
                  value={formData.documentSourceId}
                  label={`Sélectionner le document source (${docSourceType})`}
                  onChange={(e) => handleSelectDocSource(e.target.value)}
                >
                  <MenuItem value="">
                    <em>Aucun document lié</em>
                  </MenuItem>
                  {docSourceList.map((d) => (
                    <MenuItem key={d.id} value={d.id}>
                      <strong>{d.numero || d.numDi || d.numOt || d.numBt}</strong> &mdash; {d.objet || d.description || 'Sans objet'}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>

            <Grid size={12}>
              <Divider sx={{ my: 1 }} />
            </Grid>

            {/* Site / Zone */}
            <Grid size={{ xs: 12, sm: 6, md: 3 }}>
              <FormControl fullWidth size="small" disabled={readOnly}>
                <InputLabel>Site / Zone Propriétaire</InputLabel>
                <Select
                  value={formData.site}
                  label="Site / Zone Propriétaire"
                  onChange={(e) => updateTextValue('site', e.target.value)}
                >
                  <MenuItem value=""><em>Sélectionner zone...</em></MenuItem>
                  {zonesList.map((z) => (
                    <MenuItem key={z.id} value={z.nomZone}>
                      {z.nomZone}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>

            {/* Entité */}
            <Grid size={{ xs: 12, sm: 6, md: 3 }}>
              <FormControl fullWidth size="small" disabled={readOnly}>
                <InputLabel>Entité / Service Demandeur</InputLabel>
                <Select
                  value={formData.entite}
                  label="Entité / Service Demandeur"
                  onChange={(e) => updateTextValue('entite', e.target.value)}
                >
                  <MenuItem value=""><em>Sélectionner entité...</em></MenuItem>
                  {servicesList.map((s) => (
                    <MenuItem key={s.id} value={s.nomService}>
                      {s.nomService}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>

            {/* Service Intervenant */}
            <Grid size={{ xs: 12, sm: 6, md: 3 }}>
              <FormControl fullWidth size="small" disabled={fieldsLocked}>
                <InputLabel>Service Intervenant (CEEE)</InputLabel>
                <Select
                  value={formData.serviceIntervenantId || formData.servicesIntervenants}
                  label="Service Intervenant (CEEE)"
                  onChange={(e) => handleSelectServiceIntervenant(e.target.value)}
                >
                  <MenuItem value=""><em>Sélectionner service...</em></MenuItem>
                  {servicesList.map((s) => (
                    <MenuItem key={s.id} value={s.id}>
                      {s.nomService}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>

            {/* Entreprises Intervenantes */}
            <Grid size={{ xs: 12, sm: 6, md: 3 }}>
              <FormControl fullWidth size="small" disabled={fieldsLocked}>
                <InputLabel>Entreprises Intervenantes</InputLabel>
                <Select
                  value={formData.entreprisesIntervenantes}
                  label="Entreprises Intervenantes"
                  onChange={(e) => updateTextValue('entreprisesIntervenantes', e.target.value)}
                >
                  <MenuItem value=""><em>Aucune (Régie interne)</em></MenuItem>
                  {entreprisesList.map((ee) => (
                    <MenuItem key={ee.id} value={ee.nomEntreprise}>
                      {ee.nomEntreprise}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>
          </Grid>
        </CardContent>
      </Card>

      {/* SECTION 1: OBJET & HORAIRES DE L'INTERVENTION */}
      <Card sx={{ mb: 3, borderRadius: 3, border: '1px solid #e2e8f0', boxShadow: '0 4px 12px rgba(0,0,0,0.03)' }}>
        <CardHeader
          avatar={<AccessTimeIcon sx={{ color: '#00875A' }} />}
          title={<Typography variant="h6" sx={{ fontWeight: 800, color: '#1e293b' }}>Description & Planning des Travaux</Typography>}
          subheader="Objet détaillé et fenêtre d'intervention programmée"
          sx={{ bgcolor: '#f8fafc', borderBottom: '1px solid #e2e8f0', py: 1.5 }}
        />
        <CardContent sx={{ p: 3 }}>
          <Grid container spacing={2.5}>
            <Grid size={{ xs: 12, sm: 4 }}>
              <TextField
                fullWidth
                size="small"
                type="date"
                label="Date d'intervention"
                slotProps={{ inputLabel: { shrink: true } }}
                value={formData.dateIntervention}
                disabled={fieldsLocked}
                onChange={(e) => updateTextValue('dateIntervention', e.target.value)}
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 4 }}>
              <TextField
                fullWidth
                size="small"
                type="time"
                label="Heure de début"
                slotProps={{ inputLabel: { shrink: true } }}
                value={formData.heureDebut}
                disabled={fieldsLocked}
                onChange={(e) => updateTextValue('heureDebut', e.target.value)}
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 4 }}>
              <TextField
                fullWidth
                size="small"
                type="time"
                label="Heure de fin estimée"
                slotProps={{ inputLabel: { shrink: true } }}
                value={formData.heureFin}
                disabled={fieldsLocked}
                onChange={(e) => updateTextValue('heureFin', e.target.value)}
              />
            </Grid>

            <Grid size={12}>
              <TextField
                fullWidth
                multiline
                rows={3}
                label="Description détaillée des travaux à effectuer"
                placeholder="Décrivez les opérations, équipements concernés et périmètre exact..."
                value={formData.description}
                disabled={fieldsLocked}
                onChange={(e) => updateTextValue('description', e.target.value)}
              />
            </Grid>
          </Grid>
        </CardContent>
      </Card>

      {/* SECTION A: RISQUES LIÉS AUX TRAVAUX */}
      <Card sx={{ mb: 3, borderRadius: 3, border: '1px solid #fee2e2', boxShadow: '0 4px 12px rgba(239,68,68,0.05)' }}>
        <CardHeader
          avatar={<WarningAmberIcon sx={{ color: '#dc2626' }} />}
          title={
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
              <Typography variant="h6" sx={{ fontWeight: 800, color: '#991b1b' }}>
                A. Risques liés aux travaux
              </Typography>
              <Chip label={`${formData.risquesIds.length} sélectionné(s)`} color="error" size="small" sx={{ fontWeight: 800 }} />
            </Box>
          }
          subheader="Identification des risques majeurs HSE sur le chantier"
          sx={{ bgcolor: '#fff5f5', borderBottom: '1px solid #fee2e2', py: 1.5 }}
        />
        <CardContent sx={{ p: 3 }}>
          <Grid container spacing={1.5}>
            {refRisques.map((r) => {
              const active = isChecked('risquesIds', r.id);
              return (
                <Grid key={r.id} size={{ xs: 12, sm: 6, md: 4 }}>
                  <Paper
                    elevation={0}
                    onClick={() => toggleCheckbox('risquesIds', r.id)}
                    sx={{
                      p: 1.5,
                      borderRadius: 2,
                      cursor: fieldsLocked ? 'default' : 'pointer',
                      border: active ? '2px solid #ef4444' : '1px solid #cbd5e1',
                      bgcolor: active ? '#fef2f2' : '#ffffff',
                      transition: 'all 0.15s ease-in-out',
                      display: 'flex',
                      alignItems: 'center',
                      gap: 1.5,
                      '&:hover': fieldsLocked ? {} : { borderColor: '#f87171', bgcolor: '#fff5f5' },
                    }}
                  >
                    {active ? <CheckBoxIcon sx={{ color: '#dc2626' }} /> : <CheckBoxOutlineBlankIcon sx={{ color: '#94a3b8' }} />}
                    <Typography variant="body2" sx={{ fontWeight: active ? 700 : 500, color: active ? '#991b1b' : '#334155' }}>
                      {r.nom || r.nomRisque || r.libelle}
                    </Typography>
                  </Paper>
                </Grid>
              );
            })}
          </Grid>
        </CardContent>
      </Card>

      {/* SECTION B: MESURES DE PRÉVENTION ET DE SÉCURITÉ */}
      <Card sx={{ mb: 3, borderRadius: 3, border: '1px solid #d1fae5', boxShadow: '0 4px 12px rgba(16,185,129,0.05)' }}>
        <CardHeader
          avatar={<ShieldIcon sx={{ color: '#00875A' }} />}
          title={
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
              <Typography variant="h6" sx={{ fontWeight: 800, color: '#065f46' }}>
                B. Mesures de sécurité à prendre
              </Typography>
              <Chip label={`${formData.mesuresIds.length} active(s)`} color="success" size="small" sx={{ fontWeight: 800, bgcolor: '#00875A' }} />
            </Box>
          }
          subheader="Consignes obligatoires de consignation, isolation et préparation"
          sx={{ bgcolor: '#f0fdf4', borderBottom: '1px solid #d1fae5', py: 1.5 }}
        />
        <CardContent sx={{ p: 3 }}>
          <Grid container spacing={1.5}>
            {refMesures.map((m) => {
              const active = isChecked('mesuresIds', m.id);
              return (
                <Grid key={m.id} size={{ xs: 12, sm: 6, md: 4 }}>
                  <Paper
                    elevation={0}
                    onClick={() => toggleCheckbox('mesuresIds', m.id)}
                    sx={{
                      p: 1.5,
                      borderRadius: 2,
                      cursor: fieldsLocked ? 'default' : 'pointer',
                      border: active ? '2px solid #00875A' : '1px solid #cbd5e1',
                      bgcolor: active ? '#ecfdf5' : '#ffffff',
                      transition: 'all 0.15s ease-in-out',
                      display: 'flex',
                      alignItems: 'center',
                      gap: 1.5,
                      '&:hover': fieldsLocked ? {} : { borderColor: '#10b981', bgcolor: '#f0fdf4' },
                    }}
                  >
                    {active ? <CheckBoxIcon sx={{ color: '#00875A' }} /> : <CheckBoxOutlineBlankIcon sx={{ color: '#94a3b8' }} />}
                    <Typography variant="body2" sx={{ fontWeight: active ? 700 : 500, color: active ? '#065f46' : '#334155' }}>
                      {m.nom || m.nomMesure || m.libelle}
                    </Typography>
                  </Paper>
                </Grid>
              );
            })}
          </Grid>
        </CardContent>
      </Card>

      {/* SECTION C & D: MOYENS D'ACCÈS ET EPI */}
      <Grid container spacing={3} sx={{ mb: 3 }}>
        {/* SECTION C: MOYENS D'ACCÈS */}
        <Grid size={{ xs: 12, md: 6 }}>
          <Card sx={{ height: '100%', borderRadius: 3, border: '1px solid #e2e8f0' }}>
            <CardHeader
              avatar={<BuildIcon sx={{ color: '#0284c7' }} />}
              title={<Typography variant="h6" sx={{ fontWeight: 800, color: '#0369a1' }}>C. Moyens d'accès</Typography>}
              subheader="Échafaudages, nacelles, échelles"
              sx={{ bgcolor: '#f0f9ff', borderBottom: '1px solid #e0f2fe', py: 1.5 }}
            />
            <CardContent sx={{ p: 2.5 }}>
              <Grid container spacing={1.5}>
                {refMoyens.map((ma) => {
                  const active = isChecked('moyensAccesIds', ma.id);
                  return (
                    <Grid key={ma.id} size={12}>
                      <Paper
                        elevation={0}
                        onClick={() => toggleCheckbox('moyensAccesIds', ma.id)}
                        sx={{
                          p: 1.25,
                          borderRadius: 2,
                          cursor: fieldsLocked ? 'default' : 'pointer',
                          border: active ? '2px solid #0284c7' : '1px solid #cbd5e1',
                          bgcolor: active ? '#f0f9ff' : '#ffffff',
                          display: 'flex',
                          alignItems: 'center',
                          gap: 1.5,
                        }}
                      >
                        {active ? <CheckBoxIcon sx={{ color: '#0284c7' }} /> : <CheckBoxOutlineBlankIcon sx={{ color: '#94a3b8' }} />}
                        <Typography variant="body2" sx={{ fontWeight: active ? 700 : 500, color: active ? '#0369a1' : '#334155' }}>
                          {ma.nom || ma.libelle}
                        </Typography>
                      </Paper>
                    </Grid>
                  );
                })}
              </Grid>
            </CardContent>
          </Card>
        </Grid>

        {/* SECTION D: EPI */}
        <Grid size={{ xs: 12, md: 6 }}>
          <Card sx={{ height: '100%', borderRadius: 3, border: '1px solid #e2e8f0' }}>
            <CardHeader
              avatar={<RuleIcon sx={{ color: '#6366f1' }} />}
              title={<Typography variant="h6" sx={{ fontWeight: 800, color: '#4338ca' }}>D. Équipements de Protection (EPI)</Typography>}
              subheader="Protections individuelles requises"
              sx={{ bgcolor: '#eef2ff', borderBottom: '1px solid #e0e7ff', py: 1.5 }}
            />
            <CardContent sx={{ p: 2.5 }}>
              <Grid container spacing={1.5}>
                {refEpis.map((e) => {
                  const active = isChecked('episIds', e.id);
                  return (
                    <Grid key={e.id} size={12}>
                      <Paper
                        elevation={0}
                        onClick={() => toggleCheckbox('episIds', e.id)}
                        sx={{
                          p: 1.25,
                          borderRadius: 2,
                          cursor: fieldsLocked ? 'default' : 'pointer',
                          border: active ? '2px solid #6366f1' : '1px solid #cbd5e1',
                          bgcolor: active ? '#eef2ff' : '#ffffff',
                          display: 'flex',
                          alignItems: 'center',
                          gap: 1.5,
                        }}
                      >
                        {active ? <CheckBoxIcon sx={{ color: '#6366f1' }} /> : <CheckBoxOutlineBlankIcon sx={{ color: '#94a3b8' }} />}
                        <Typography variant="body2" sx={{ fontWeight: active ? 700 : 500, color: active ? '#4338ca' : '#334155' }}>
                          {e.nom || e.nomEPI || e.libelle}
                        </Typography>
                      </Paper>
                    </Grid>
                  );
                })}
              </Grid>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* SECTION E: PERMIS COMPLÉMENTAIRES OBLIGATOIRES */}
      <Card sx={{ mb: 3, borderRadius: 3, border: '1px solid #fde68a', boxShadow: '0 4px 12px rgba(245,158,11,0.08)' }}>
        <CardHeader
          avatar={<WarningAmberIcon sx={{ color: '#d97706' }} />}
          title={
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
              <Typography variant="h6" sx={{ fontWeight: 800, color: '#b45309' }}>
                E. Permis complémentaires obligatoires
              </Typography>
              <Chip label={`${formData.permisIds.length} requis`} color="warning" size="small" sx={{ fontWeight: 800 }} />
            </Box>
          }
          subheader="Tout permis coché doit être annexé et validé CONFORME pour valider l'export PDF"
          sx={{ bgcolor: '#fffbeb', borderBottom: '1px solid #fde68a', py: 1.5 }}
        />
        <CardContent sx={{ p: 3 }}>
          <Grid container spacing={1.5}>
            {refPermis.map((p) => {
              const active = isChecked('permisIds', p.id);
              return (
                <Grid key={p.id} size={{ xs: 12, sm: 6, md: 4 }}>
                  <Paper
                    elevation={0}
                    onClick={() => toggleCheckbox('permisIds', p.id)}
                    sx={{
                      p: 1.5,
                      borderRadius: 2,
                      cursor: fieldsLocked ? 'default' : 'pointer',
                      border: active ? '2px solid #d97706' : '1px solid #cbd5e1',
                      bgcolor: active ? '#fffbeb' : '#ffffff',
                      display: 'flex',
                      alignItems: 'center',
                      gap: 1.5,
                      '&:hover': fieldsLocked ? {} : { borderColor: '#f59e0b', bgcolor: '#fef3c7' },
                    }}
                  >
                    {active ? <CheckBoxIcon sx={{ color: '#d97706' }} /> : <CheckBoxOutlineBlankIcon sx={{ color: '#94a3b8' }} />}
                    <Typography variant="body2" sx={{ fontWeight: active ? 700 : 500, color: active ? '#b45309' : '#334155' }}>
                      {p.nomType || p.nom || p.libelle}
                    </Typography>
                  </Paper>
                </Grid>
              );
            })}
          </Grid>
        </CardContent>
      </Card>

      {/* SECTION F: MESURES SPÉCIFIQUES EXÉCUTANT */}
      <Card sx={{ mb: 3, borderRadius: 3, border: '1px solid #e2e8f0' }}>
        <CardHeader
          avatar={<RuleIcon sx={{ color: '#00875A' }} />}
          title={<Typography variant="h6" sx={{ fontWeight: 800, color: '#1e293b' }}>F. Mesures particulières de l'Exécutant</Typography>}
          subheader="Consignes spécifiques ajoutées par le Chef d'Équipe ou l'Entreprise Extérieure"
          sx={{ bgcolor: '#f8fafc', borderBottom: '1px solid #e2e8f0', py: 1.5 }}
        />
        <CardContent sx={{ p: 3 }}>
          <TextField
            fullWidth
            multiline
            rows={3}
            label="Consignes particulières additionnelles"
            placeholder="Mesures spécifiques observées sur le terrain..."
            value={formData.sectionF}
            disabled={fieldsLocked}
            onChange={(e) => updateTextValue('sectionF', e.target.value)}
          />
        </CardContent>
      </Card>

      {/* SECTION G: VISAS & SIGNATURES */}
      <Card sx={{ mb: 3, borderRadius: 3, border: '1px solid #e2e8f0' }}>
        <CardHeader
          avatar={<DrawIcon sx={{ color: '#00875A' }} />}
          title={<Typography variant="h6" sx={{ fontWeight: 800, color: '#1e293b' }}>G. Approbations, Visas & Signatures Numériques</Typography>}
          subheader="Validation des 3 postes de travail par CEEP (Émetteur) et CEEE (Exécutant)"
          sx={{ bgcolor: '#f8fafc', borderBottom: '1px solid #e2e8f0', py: 1.5 }}
        />
        <CardContent sx={{ p: 3 }}>
          <Grid container spacing={3}>
            {/* 1er Poste */}
            <Grid size={{ xs: 12, md: 4 }}>
              <Paper elevation={0} sx={{ p: 2, borderRadius: 2.5, border: '1px solid #cbd5e1', bgcolor: '#f8fafc' }}>
                <Typography variant="subtitle2" sx={{ fontWeight: 800, color: '#00875A', mb: 1.5 }}>
                  1er Poste de Travail
                </Typography>
                <Stack spacing={1.5}>
                  <TextField
                    fullWidth
                    size="small"
                    label="Chef d'Équipe Emetteur (CEEP)"
                    value={formData.g1NomCeep}
                    disabled={fieldsLocked}
                    onChange={(e) => updateTextValue('g1NomCeep', e.target.value)}
                  />
                  <Button
                    variant="outlined"
                    size="small"
                    color="primary"
                    startIcon={<DrawIcon />}
                    disabled={!canSignCeep}
                    onClick={() => handleOpenSignature('g1VisaCeep')}
                    sx={{ justifyContent: 'flex-start', textTransform: 'none' }}
                  >
                    {formData.g1VisaCeep ? 'Visa CEEP signé (cliquer pour modifier)' : 'Signer Visa CEEP'}
                  </Button>

                  <Divider />

                  <TextField
                    fullWidth
                    size="small"
                    label="Chef d'Équipe Exécutant (CEEE)"
                    value={formData.g1NomCeee}
                    disabled={fieldsLocked}
                    onChange={(e) => updateTextValue('g1NomCeee', e.target.value)}
                  />
                  <Button
                    variant="outlined"
                    size="small"
                    color="success"
                    startIcon={<DrawIcon />}
                    disabled={!canSignCeee}
                    onClick={() => handleOpenSignature('g1VisaCeee')}
                    sx={{ justifyContent: 'flex-start', textTransform: 'none' }}
                  >
                    {formData.g1VisaCeee ? 'Visa CEEE signé (cliquer pour modifier)' : 'Signer Visa CEEE'}
                  </Button>
                </Stack>
              </Paper>
            </Grid>

            {/* 2ème Poste */}
            <Grid size={{ xs: 12, md: 4 }}>
              <Paper elevation={0} sx={{ p: 2, borderRadius: 2.5, border: '1px solid #cbd5e1', bgcolor: '#f8fafc' }}>
                <Typography variant="subtitle2" sx={{ fontWeight: 800, color: '#475569', mb: 1.5 }}>
                  2ème Poste (Reconduction)
                </Typography>
                <Stack spacing={1.5}>
                  <TextField
                    fullWidth
                    size="small"
                    label="Nom CEEP (2e poste)"
                    value={formData.g2NomCeep}
                    disabled={fieldsLocked}
                    onChange={(e) => updateTextValue('g2NomCeep', e.target.value)}
                  />
                  <Button
                    variant="outlined"
                    size="small"
                    disabled={!canSignCeep}
                    onClick={() => handleOpenSignature('g2VisaCeep')}
                    sx={{ justifyContent: 'flex-start', textTransform: 'none' }}
                  >
                    {formData.g2VisaCeep ? 'Visa CEEP 2e poste' : 'Signer CEEP (2e poste)'}
                  </Button>

                  <Divider />

                  <TextField
                    fullWidth
                    size="small"
                    label="Nom CEEE (2e poste)"
                    value={formData.g2NomCeee}
                    disabled={fieldsLocked}
                    onChange={(e) => updateTextValue('g2NomCeee', e.target.value)}
                  />
                  <Button
                    variant="outlined"
                    size="small"
                    color="success"
                    disabled={!canSignCeee}
                    onClick={() => handleOpenSignature('g2VisaCeee')}
                    sx={{ justifyContent: 'flex-start', textTransform: 'none' }}
                  >
                    {formData.g2VisaCeee ? 'Visa CEEE 2e poste' : 'Signer CEEE (2e poste)'}
                  </Button>
                </Stack>
              </Paper>
            </Grid>

            {/* 3ème Poste */}
            <Grid size={{ xs: 12, md: 4 }}>
              <Paper elevation={0} sx={{ p: 2, borderRadius: 2.5, border: '1px solid #cbd5e1', bgcolor: '#f8fafc' }}>
                <Typography variant="subtitle2" sx={{ fontWeight: 800, color: '#475569', mb: 1.5 }}>
                  3ème Poste (Reconduction)
                </Typography>
                <Stack spacing={1.5}>
                  <TextField
                    fullWidth
                    size="small"
                    label="Nom CEEP (3e poste)"
                    value={formData.g3NomCeep}
                    disabled={fieldsLocked}
                    onChange={(e) => updateTextValue('g3NomCeep', e.target.value)}
                  />
                  <Button
                    variant="outlined"
                    size="small"
                    disabled={!canSignCeep}
                    onClick={() => handleOpenSignature('g3VisaCeep')}
                    sx={{ justifyContent: 'flex-start', textTransform: 'none' }}
                  >
                    {formData.g3VisaCeep ? 'Visa CEEP 3e poste' : 'Signer CEEP (3e poste)'}
                  </Button>

                  <Divider />

                  <TextField
                    fullWidth
                    size="small"
                    label="Nom CEEE (3e poste)"
                    value={formData.g3NomCeee}
                    disabled={fieldsLocked}
                    onChange={(e) => updateTextValue('g3NomCeee', e.target.value)}
                  />
                  <Button
                    variant="outlined"
                    size="small"
                    color="success"
                    disabled={!canSignCeee}
                    onClick={() => handleOpenSignature('g3VisaCeee')}
                    sx={{ justifyContent: 'flex-start', textTransform: 'none' }}
                  >
                    {formData.g3VisaCeee ? 'Visa CEEE 3e poste' : 'Signer CEEE (3e poste)'}
                  </Button>
                </Stack>
              </Paper>
            </Grid>
          </Grid>
        </CardContent>
      </Card>

      {/* SECTION H: CLÔTURE & RÉCEPTION DE TRAVAUX */}
      <Card sx={{ mb: 3, borderRadius: 3, border: '1px solid #e2e8f0' }}>
        <CardHeader
          avatar={<VerifiedUserIcon sx={{ color: '#00875A' }} />}
          title={<Typography variant="h6" sx={{ fontWeight: 800, color: '#1e293b' }}>H. Réception des Travaux & Restitution</Typography>}
          subheader="Déclaration de fin d'intervention et conformité de remise en état"
          sx={{ bgcolor: '#f8fafc', borderBottom: '1px solid #e2e8f0', py: 1.5 }}
        />
        <CardContent sx={{ p: 3 }}>
          <Grid container spacing={2.5}>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                fullWidth
                size="small"
                type="date"
                label="Date de réception"
                slotProps={{ inputLabel: { shrink: true } }}
                value={formData.dateReception}
                disabled={readOnly}
                onChange={(e) => updateTextValue('dateReception', e.target.value)}
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                fullWidth
                size="small"
                type="time"
                label="Heure de réception"
                slotProps={{ inputLabel: { shrink: true } }}
                value={formData.heureReception}
                disabled={readOnly}
                onChange={(e) => updateTextValue('heureReception', e.target.value)}
              />
            </Grid>

            <Grid size={12}>
              <Typography variant="subtitle2" sx={{ fontWeight: 700, mb: 1 }}>
                Restitution des installations :
              </Typography>
              <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
                {[
                  { key: 'protect', label: 'Protections retirées' },
                  { key: 'propre', label: 'Lieu nettoyé & propre' },
                  { key: 'consigne', label: 'Déconsignation effectuée' },
                ].map((item) => {
                  const checked = formData.remiseEnPlace.includes(item.key);
                  return (
                    <Chip
                      key={item.key}
                      label={item.label}
                      icon={checked ? <CheckBoxIcon /> : <CheckBoxOutlineBlankIcon />}
                      clickable={!readOnly}
                      color={checked ? 'success' : 'default'}
                      onClick={() => toggleRemiseEnPlace(item.key)}
                      sx={{ fontWeight: 700 }}
                    />
                  );
                })}
              </Stack>
            </Grid>

            <Grid size={12}>
              <FormControl component="fieldset">
                <Typography variant="subtitle2" sx={{ fontWeight: 700, mb: 0.5 }}>
                  Essais de bon fonctionnement concluants :
                </Typography>
                <RadioGroup
                  row
                  value={formData.essaiConcluant}
                  onChange={(e) => updateTextValue('essaiConcluant', e.target.value)}
                >
                  <FormControlLabel value="oui" control={<Radio disabled={readOnly} size="small" />} label="Oui (Concluant)" />
                  <FormControlLabel value="non" control={<Radio disabled={readOnly} size="small" />} label="Non (Réserves)" />
                </RadioGroup>
              </FormControl>
            </Grid>
          </Grid>
        </CardContent>
      </Card>

      {/* SIGNATURE DIALOG MODAL */}
      <Dialog open={sigDialogOpen} onClose={() => setSigDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', bgcolor: '#0f172a', color: '#fff', py: 1.5 }}>
          <Typography variant="subtitle1" sx={{ fontWeight: 800 }}>
            Signature Numérique &mdash; {activeSigField}
          </Typography>
          <IconButton onClick={() => setSigDialogOpen(false)} sx={{ color: '#fff' }}>
            <CloseIcon />
          </IconButton>
        </DialogTitle>
        <DialogContent sx={{ p: 3 }}>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            Dessinez votre signature manuscrite à l'aide de votre souris ou écran tactile.
          </Typography>
          <Box sx={{ border: '2px dashed #00875A', borderRadius: 2, overflow: 'hidden', bgcolor: '#fafafa' }}>
            <SignaturePad onSave={handleSaveSignature} />
          </Box>
        </DialogContent>
      </Dialog>
    </Box>
  );
}