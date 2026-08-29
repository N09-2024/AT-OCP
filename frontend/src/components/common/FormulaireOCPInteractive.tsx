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
  Checkbox,
  Tooltip,
} from '@mui/material';
import PictureAsPdfIcon from '@mui/icons-material/PictureAsPdf';
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
import SmartToyIcon from '@mui/icons-material/SmartToy';
import MenuBookIcon from '@mui/icons-material/MenuBook';

import SignaturePad from './SignaturePad';
import PermisUploadCard from './PermisUploadCard';
import { AIAssistantDrawer } from '../ia/AIAssistantDrawer';
import { apiClient } from '../../services/apiClient';
import { iaApi } from '../../services/iaApi';
import { autorisationTravailApi } from '../../services/autorisationTravailApi';
import {
  type PermisDocumentResponse,
  initialiserPermis,
  getPermisDocuments,
  uploadPermisDocument,
  relancerAnalyse,
} from '../../services/permisDocumentApi';
import { useAuthStore } from '../../store/authStore';

// ⚠️ Export Word supprimé : seul le PDF officiel (gating HC/HM serveur) est autorisé.
// Les imports 'jspdf' et 'docx' ne sont plus utilisés dans ce composant.

export interface FormulaireOCPInteractiveProps {
  initialData?: any;
  readOnly?: boolean;
  /** all = édition complète | ceee = CEEE vise seulement | none = lecture */
  signMode?: 'all' | 'ceep' | 'ceee' | 'none';
  onChange?: (formData: any) => void;
  onSave?: (formData: any) => Promise<void>;
  /** Sauvegarde automatique (cases cochées) sans bouton */
  onAutoSave?: (formData: any) => Promise<void>;
  onSubmitAT?: (formData: any, signatureBlob?: Blob) => Promise<void>;
  onVisaCeee?: (formData: any, signatureBlob: Blob) => Promise<void>;
  loading?: boolean;
}

export default function FormulaireOCPInteractive({
  initialData: rawInitialData = {},
  readOnly = false,
  signMode = 'all',
  onChange,
  onSave,
  onAutoSave,
  onSubmitAT,
  onVisaCeee,
  loading = false,
}: FormulaireOCPInteractiveProps) {
  // Guard: if parent passes null explicitly (AT not yet loaded), treat as empty object
  const initialData = rawInitialData ?? {};
  const currentUser = useAuthStore((s) => s.user);
  const containerRef = useRef<HTMLDivElement>(null);
  const atStatut = initialData?.statut || initialData?.statutWorkflow || '';
  const isSubmittedOrTransmitted = Boolean(atStatut && atStatut !== 'BROUILLON' && atStatut !== 'DEMANDE_CREEE' && atStatut !== 'CLASSIFICATION_EFFECTUEE');
  const fieldsLocked = readOnly || signMode === 'ceee' || signMode === 'none' || isSubmittedOrTransmitted;
  const canSignCeep = !readOnly && (signMode === 'all' || signMode === 'ceep') && !isSubmittedOrTransmitted;
  // canSignCeee est indépendant de readOnly : le CEEE signe après que les champs sont verrouillés
  const canSignCeee = signMode === 'ceee' || (!readOnly && signMode === 'all' && !isSubmittedOrTransmitted);

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

  // --- IA Multi-Agents & RAG ---
  const [iaLoading, setIaLoading] = useState(false);
  const [iaRapport, setIaRapport] = useState<string | null>(null);
  const [iaAlertes, setIaAlertes] = useState<string[]>([]);
  const [iaSources, setIaSources] = useState<string[]>([]);
  const [iaConfidence, setIaConfidence] = useState<string>('HIGH');
  const [aiDrawerOpen, setAiDrawerOpen] = useState(false);
  const [iaSuggestions, setIaSuggestions] = useState<{
    risques: string[]; mesures: string[]; epis: string[]; permis: string[];
  }>({ risques: [], mesures: [], epis: [], permis: [] });

  // Permis Documents - validation IA
  const [permisDocuments, setPermisDocuments] = useState<PermisDocumentResponse[]>([]);
  const [permisUploading, setPermisUploading] = useState(false);
  const debounceInitRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  // Form State
  const todayStr = new Date().toISOString().split('T')[0];
  const initialDocType = initialData.typeDocumentSource || initialData.documentSourceType || 'DI';
  const initialDocNum = initialData.documentSourceNumero || (initialData.numero ? `${initialDocType}-2026-${Math.floor(1000 + Math.random() * 9000)}` : `${initialDocType}-2026-4819`);

  const [formData, setFormData] = useState({
    numero: initialData.numero || '',
    site: initialData.zoneProprietaireNom || initialData.zoneProprietaire?.nomZone || currentUser?.service?.zone?.nomZone || '',
    zoneProprietaireId: initialData.zoneProprietaireId || initialData.zoneProprietaire?.id || currentUser?.service?.zone?.id || null,
    zoneProprietaireNom: initialData.zoneProprietaireNom || initialData.zoneProprietaire?.nomZone || currentUser?.service?.zone?.nomZone || '',
    entite: initialData.serviceDemandeur || initialData.entite || currentUser?.service?.nomService || '',
    serviceDemandeur: initialData.serviceDemandeur || initialData.entite || currentUser?.service?.nomService || '',
    serviceDemandeurId: initialData.serviceDemandeurId || null,
    documentSourceType: initialDocType,
    documentSourceId: initialData.documentSourceId || '',
    documentSourceNumero: initialDocNum,
    di: initialDocType === 'DI' ? initialDocNum : '',
    ot: initialDocType === 'OT' ? initialDocNum : '',
    bt: initialDocType === 'BT' ? initialDocNum : '',
    zoneExecutanteId: initialData.zoneExecutanteId || initialData.zoneExecutante?.id || null,
    zoneExecutanteNom: initialData.zoneExecutanteNom || initialData.zoneExecutante?.nomZone || '',
    zoneExecutante: initialData.zoneExecutanteNom || initialData.zoneExecutante?.nomZone || '',
    lieu: initialData.zoneExecutanteNom || initialData.zoneExecutante?.nomZone || initialData.lieu || '',
    servicesIntervenants: initialData.servicesIntervenants || initialData.serviceIntervenant || '',
    serviceIntervenant: initialData.servicesIntervenants || initialData.serviceIntervenant || '',
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
  });

  // State Visite Préalable (§8.2 Standard OCP)
  const [gpsCoords, setGpsCoords] = useState<{ lat: number; lng: number } | null>(
    initialData.latitude ? { lat: initialData.latitude, lng: initialData.longitude } : null
  );
  const [gpsLoading, setGpsLoading] = useState(false);
  const [visitePhotoName, setVisitePhotoName] = useState<string | null>(initialData.photoPath || null);
  const photoInputRef = useRef<HTMLInputElement>(null);
  const [visiteCommentaire, setVisiteCommentaire] = useState(initialData.visiteCommentaire || '');
  const [preventionEnPlace, setPreventionEnPlace] = useState<boolean>(initialData.visiteEffectuee ?? true);

  const handleGetGps = () => {
    setGpsLoading(true);
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        const coords = { lat: pos.coords.latitude, lng: pos.coords.longitude };
        setGpsCoords(coords);
        setGpsLoading(false);
      },
      () => {
        alert("Impossible d'accéder à la géolocalisation GPS. Veuillez autoriser l'accès GPS.");
        setGpsLoading(false);
      }
    );
  };

  const handlePhotoUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      setVisitePhotoName(file.name);
    }
  };

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
      onChange?.(updated);
      return updated;
    });
  };

  const hydratedKeyRef = useRef<string | null>(null);

  useEffect(() => {
    const idKey = initialData?.id
      ? String(initialData.id)
      : (initialData?._loaded ? '__brouillon_local__' : null);

    if (!idKey) return;
    if (hydratedKeyRef.current === idKey) return; // déjà hydraté pour cette AT
    hydratedKeyRef.current = idKey;

    setFormData((prev: any) => {
      const getArray = (initArr: any, objArr: any, prevArr: any) => {
        if (Array.isArray(initArr) && initArr.length > 0) return initArr.map(String);
        if (Array.isArray(objArr) && objArr.length > 0) return objArr.map((x: any) => String(x.id || x));
        if (Array.isArray(prevArr) && prevArr.length > 0) return prevArr.map(String);
        return [];
      };

      return {
        ...prev,
        numero: initialData.numero || prev.numero || '',
        site: initialData.zoneProprietaire?.nomZone || initialData.zoneProprietaireNom || initialData.site || prev.site || '',
        zoneProprietaireNom: initialData.zoneProprietaire?.nomZone || initialData.zoneProprietaireNom || initialData.site || prev.zoneProprietaireNom || '',
        zoneProprietaireId: initialData.zoneProprietaire?.id || initialData.zoneProprietaireId || prev.zoneProprietaireId || '',
        entite: initialData.serviceDemandeur || initialData.entite || prev.entite || '',
        serviceDemandeur: initialData.serviceDemandeur || initialData.entite || prev.serviceDemandeur || '',
        zoneExecutanteNom: initialData.zoneExecutante?.nomZone || initialData.zoneExecutanteNom || initialData.zoneExecutante || initialData.lieu || prev.zoneExecutanteNom || '',
        zoneExecutante: initialData.zoneExecutante?.nomZone || initialData.zoneExecutanteNom || initialData.zoneExecutante || initialData.lieu || prev.zoneExecutante || '',
        zoneExecutanteId: initialData.zoneExecutante?.id || initialData.zoneExecutanteId || prev.zoneExecutanteId || '',
        lieu: initialData.zoneExecutante?.nomZone || initialData.zoneExecutanteNom || initialData.zoneExecutante || initialData.lieu || prev.lieu || '',
        servicesIntervenants: initialData.servicesIntervenants || initialData.serviceIntervenant || prev.servicesIntervenants || '',
        serviceIntervenant: initialData.servicesIntervenants || initialData.serviceIntervenant || prev.serviceIntervenant || '',
        serviceIntervenantId: initialData.serviceIntervenantId || prev.serviceIntervenantId || null,
        entreprisesIntervenantes: initialData.entreprisesIntervenantes || prev.entreprisesIntervenantes || '',
        documentSourceType: initialData.typeDocumentSource || prev.documentSourceType || 'DI',
        documentSourceId: initialData.documentSourceId || prev.documentSourceId || '',
        documentSourceNumero: initialData.documentSourceNumero || prev.documentSourceNumero || '',
        di: initialData.typeDocumentSource === 'DI' ? (initialData.documentSourceNumero ?? prev.di) : prev.di,
        ot: initialData.typeDocumentSource === 'OT' ? (initialData.documentSourceNumero ?? prev.ot) : prev.ot,
        bt: initialData.typeDocumentSource === 'BT' ? (initialData.documentSourceNumero ?? prev.bt) : prev.bt,
        description: initialData.description || initialData.descriptionTravaux || initialData.objet || prev.description || '',
        dateIntervention: initialData.dateIntervention || initialData.dateDebut || prev.dateIntervention,
        heureDebut: initialData.heureDebut || prev.heureDebut || '08:00',
        heureFin: initialData.heureFin || prev.heureFin || '17:00',
        risquesIds: getArray(initialData.risquesIds, initialData.risques, prev.risquesIds),
        mesuresIds: getArray(initialData.mesuresIds, initialData.mesures, prev.mesuresIds),
        episIds: getArray(initialData.episIds, initialData.epis, prev.episIds),
        moyensAccesIds: getArray(initialData.moyensAccesIds, initialData.moyensAcces, prev.moyensAccesIds),
        permisIds: getArray(initialData.permisIds, initialData.permis, prev.permisIds),
        sectionF: initialData.sectionF || initialData.mesuresSecuriteExecutant || prev.sectionF || '',
        g1NomCeep: initialData.g1NomCeep || prev.g1NomCeep || '',
        g1NomCeee: initialData.g1NomCeee || prev.g1NomCeee || '',
        g1VisaCeep: initialData.g1VisaCeep || prev.g1VisaCeep || null,
        g1VisaCeee: initialData.g1VisaCeee || prev.g1VisaCeee || null,
      };
    });
  }, [initialData?.id, initialData?._loaded]);

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
    setFormData((prev: any) => {
      const updated = {
        ...prev,
        documentSourceType: docSourceType,
        documentSourceId: docId,
        documentSourceNumero: numDoc,
        di: docSourceType === 'DI' ? numDoc : prev.di,
        ot: docSourceType === 'OT' ? numDoc : prev.ot,
        bt: docSourceType === 'BT' ? numDoc : prev.bt,
        description: found.objet || found.description || prev.description,
        lieu: found.equipementNom || prev.lieu,
      };
      scheduleAutoSave(updated);
      onChange?.(updated);
      return updated;
    });
  };

  const handleSelectServiceIntervenant = async (serviceIdOrName: string) => {
    if (readOnly || signMode === 'ceee' || signMode === 'none') return;
    const found = servicesList.find((s) => s.id === serviceIdOrName || s.nomService === serviceIdOrName);
    const nomService = found?.nomService || serviceIdOrName;
    const userNomService = currentUser?.service?.nomService;
    const userServiceId = currentUser?.service?.id;

    if ((found?.id && userServiceId && found.id === userServiceId) || (nomService && userNomService && nomService.toLowerCase().trim() === userNomService.toLowerCase().trim())) {
      alert(`⚠️ Une Autorisation de Travail ne peut pas être établie au sein d'un même service. Le service demandeur/propriétaire (${userNomService || 'votre service'}) et le service exécutant doivent être différents.`);
      return;
    }

    let displayCeee = '';
    if (found?.id) {
      try {
        const res = await apiClient.get(`/services/${found.id}/chefs-equipe`);
        const chefs = Array.isArray(res.data) ? res.data : [];
        displayCeee = chefs.map((c: any) => c.displayName || `${c.prenom || ''} ${c.nom || ''}`.trim()).filter(Boolean).join(' / ');
      } catch { /* ignore */ }
    }

    setFormData((prev: any) => {
      const updated = {
        ...prev,
        servicesIntervenants: nomService,
        serviceIntervenantId: found?.id || null,
        g1NomCeee: displayCeee || prev.g1NomCeee || '',
      };
      scheduleAutoSave(updated);
      onChange?.(updated);
      return updated;
    });
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
      onChange?.(updated);
      // La synchronisation des PermisDocument (agent IA) se fait désormais côté backend,
      // dans la même transaction que l'autosave (voir AutorisationTravailServiceImpl.autoSave),
      // ce qui élimine la race condition entre l'écriture et la relecture de formPermisIds.
      // On se contente ici de rafraîchir l'affichage un court instant après l'autosave.
      if (field === 'permisIds' && initialData?.id) {
        if (debounceInitRef.current) clearTimeout(debounceInitRef.current);
        debounceInitRef.current = setTimeout(async () => {
          try {
            const docs = await getPermisDocuments(initialData.id);
            setPermisDocuments(docs);
          } catch (e) {
            console.error('Rafraîchissement permis IA échoué', e);
          }
        }, 1200);
      }
      return updated;
    });
  };


  // ------------------------------------------------------------------
  // IA MULTI-AGENTS (CrewAI + LangChain + RAG)
  // Analyse complète déclenchable manuellement ou en arrière-plan
  // ------------------------------------------------------------------
  const iaDebounceRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const lastAnalyzedDescRef = useRef<string>('');

  const lancerAnalyseIA = async () => {
    const desc = (formData.description || '').trim();
    if (!desc) {
      alert("Veuillez renseigner une description des travaux avant de lancer l'analyse IA.");
      return;
    }
    setIaLoading(true);
    try {
      const res = await iaApi.analyzeAt({
        atId: initialData?.id,
        description: desc,
        installation: formData.lieu,
        visiteFaite: preventionEnPlace,
        sectionFRenseignee: Boolean(formData.sectionF?.trim()),
        risques: formData.risquesIds,
        mesures: formData.mesuresIds,
        epi: formData.episIds,
        moyensAcces: formData.moyensAccesIds,
      });
      setIaRapport(res.summary || res.rapport || 'Analyse IA multi-agents effectuée.');
      setIaAlertes(res.warnings || res.alertes || []);
      setIaSources(res.sources || []);
      setIaConfidence(res.confidence || 'HIGH');
      setIaSuggestions({
        risques: res.identifiedRisks || res.risques || [],
        mesures: res.recommendedMeasures || res.mesures || [],
        epis: res.epis || [],
        permis: res.permis || [],
      });
    } catch (e) {
      console.error('Erreur analyse IA', e);
    } finally {
      setIaLoading(false);
    }
  };

  useEffect(() => {
    if (readOnly || fieldsLocked) return;
    const desc = (formData.description || '').trim();
    if (desc.length < 15) return;
    if (desc === lastAnalyzedDescRef.current) return;

    if (iaDebounceRef.current) clearTimeout(iaDebounceRef.current);
    iaDebounceRef.current = setTimeout(async () => {
      lastAnalyzedDescRef.current = desc;
      lancerAnalyseIA();
    }, 2500);

    return () => {
      if (iaDebounceRef.current) clearTimeout(iaDebounceRef.current);
    };
  }, [formData.description, readOnly, fieldsLocked]);

  // Applique une suggestion IA à un référentiel de cases à cocher (l'utilisateur
  // choisit lui-même ce qu'il accepte - l'IA ne coche jamais à sa place).
  const appliquerSuggestion = (
    field: 'risquesIds' | 'mesuresIds' | 'episIds' | 'permisIds',
    refList: any[],
    label: string
  ) => {
    const low = label.toLowerCase();
    const found = refList.find((x) =>
      (x.nom || x.nomRisque || x.nomMesure || x.nomEPI || x.libelle || x.nomType || '')
        .toLowerCase()
        .includes(low.split(' ')[0])
    );
    if (found?.id) toggleCheckbox(field, found.id);
  };

  // Contrôle de complétude implicite, exécuté juste avant transmission
  // (remplace l'ancien clic manuel "Assistant IA" / bouton de contrôle).
  const controlerAvantSoumission = async (): Promise<boolean> => {
    try {
      const res = await iaApi.controlerDossier({
        description: formData.description,
        visiteFaite: true,
        nbRisques: formData.risquesIds.length,
        nbMesures: formData.mesuresIds.length,
        nbEpis: formData.episIds.length,
        nbPermis: formData.permisIds.length,
        sectionFRenseignee: !!formData.sectionF?.trim(),
      });
      const data = (res as any).data ?? res;
      setIaRapport(data.rapport || null);
      setIaAlertes(data.alertes || []);
      return data.complet !== false;
    } catch {
      return true; // IA indisponible : on ne bloque jamais le workflow métier
    }
  };

  const handleOpenSignature = (fieldKey: string) => {
    const key = fieldKey.toLowerCase();
    const isCeeeField = key.includes('ceee');
    // En mode ceee : les champs sont verrouillés (readOnly) mais le CEEE peut signer son propre visa
    if (signMode === 'ceee' && isCeeeField) {
      setActiveSigField(fieldKey);
      setSigDialogOpen(true);
      return;
    }
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
    // Si le CEEE vient de signer son visa depuis le formulaire, notifier le parent
    const isCeeeVisa = activeSigField.toLowerCase().includes('ceee');
    if (isCeeeVisa && onVisaCeee) {
      onVisaCeee(formData, blob);
    }
  };

  // Export PDF Serveur - seul export disponible. Le backend refuse (400)
  // tant que les visas HM + HC ne sont pas positifs (voir
  // AutorisationTravailServiceImpl.verifierDroitExportPdf côté Spring).
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

  const handleSignerEtTransmettre = async () => {
    // §8.2 OCP S-HSE-SEC-31 - Visite Préalable Obligatoire
    if (!preventionEnPlace) {
      alert(
        '⛔ Visite Préalable obligatoire (§8.2)\n\n' +
        'Vous devez effectuer la visite conjointe de chantier, cocher la confirmation des mesures de prévention, ' +
        'puis signer avant de transmettre l\'Autorisation de Travail.'
      );
      return;
    }

    const complet = await controlerAvantSoumission();
    if (!complet) {
      const confirmer = window.confirm(
        "L'IA signale des éléments potentiellement manquants (voir le bandeau ci-dessus). Transmettre quand même ?"
      );
      if (!confirmer) return;
    }

    // Enrich formData with Visite Préalable fields before submission
    const enrichedData = {
      ...formData,
      latitude: gpsCoords?.lat ?? null,
      longitude: gpsCoords?.lng ?? null,
      visiteCommentaire,
      visiteEffectuee: preventionEnPlace,
      photoPath: visitePhotoName,
    };
    onSubmitAT?.(enrichedData, sigBlobs['g1VisaCeep']);
  };

  // ---- Agent IA Permis : chargement initial + polling ----
  useEffect(() => {
    if (!initialData?.id || !formData.permisIds?.length) return;
    // Chargement initial
    getPermisDocuments(initialData.id).then(setPermisDocuments).catch(() => {});
  }, [initialData?.id]);

  useEffect(() => {
    if (!initialData?.id) return;
    const hasAnalysing = permisDocuments.some((d) => d.statut === 'EN_ATTENTE_ANALYSE');
    if (!hasAnalysing) return;
    // Polling toutes les 3s tant qu'un document est en cours d'analyse
    const timer = setInterval(() => {
      getPermisDocuments(initialData.id).then(setPermisDocuments).catch(() => {});
    }, 3000);
    return () => clearInterval(timer);
  }, [permisDocuments, initialData?.id]);

  const handlePermisUpload = async (typePermis: string, file: File) => {
    if (!initialData?.id) return;
    setPermisUploading(true);
    try {
      const updated = await uploadPermisDocument(initialData.id, typePermis, file);
      setPermisDocuments((prev) =>
        prev.some((d) => d.id === updated.id)
          ? prev.map((d) => (d.id === updated.id ? updated : d))
          : [...prev, updated]
      );
    } catch (e: any) {
      alert('Erreur upload : ' + (e?.response?.data?.message || e?.message || 'Erreur inconnue'));
    } finally {
      setPermisUploading(false);
    }
  };

  const handleRelancerAnalyse = async (id: string) => {
    try {
      const updated = await relancerAnalyse(id);
      setPermisDocuments((prev) =>
        prev.map((d) => (d.id === updated.id ? updated : d))
      );
    } catch (e) {
      console.error('Relance analyse échouée', e);
    }
  };

  // Blocage soumission si des permis ne sont pas tous VALIDE
  const permisEnAttente = permisDocuments.filter((d) => d.statut !== 'VALIDE').length;
  const soumissionBloquee = !readOnly && permisDocuments.length > 0 && permisEnAttente > 0;
  // Le PDF officiel n'est affiché qu'après les visas HMEP et HMEE.
  // Les indicateurs sont calculés par FormulaireOCPViewer à partir des visas serveur.
  const hmepAndHmeeSigned = Boolean(initialData?.hmepVisaSigne && initialData?.hmeeVisaSigne);

  return (
    <Box sx={{ pb: 6, maxWidth: 1120, mx: 'auto' }}>
      {/* IA SUGGESTION BANNER - Recommandations enrichies CrewAI + LangChain + RAG */}
      {(iaRapport || iaAlertes.length > 0) && (
        <Alert
          icon={<AutoAwesomeIcon sx={{ color: 'primary.main' }} />}
          severity={iaAlertes.length ? 'warning' : 'info'}
          sx={{ mb: 3, borderRadius: 3, boxShadow: 3, bgcolor: '#F0FDF4', border: '1px solid #BBF7D0' }}
          onClose={() => {
            setIaRapport(null);
            setIaAlertes([]);
          }}
        >
          <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: 1, mb: 1 }}>
            <Stack direction="row" spacing={1} sx={{ alignItems: 'center' }}>
              <Chip
                label="Recommandation IA (CrewAI + LangChain)"
                size="small"
                color="primary"
                sx={{ fontWeight: 800, fontSize: '0.72rem' }}
              />
              {iaConfidence && (
                <Chip
                  label={`Confiance : ${iaConfidence}`}
                  size="small"
                  variant="outlined"
                  sx={{ fontSize: '0.68rem', height: 20 }}
                />
              )}
            </Stack>

            <Stack direction="row" spacing={1}>
              <Button
                size="small"
                variant="outlined"
                color="primary"
                startIcon={<SmartToyIcon />}
                onClick={() => setAiDrawerOpen(true)}
                sx={{ textTransform: 'none', py: 0.2, px: 1, fontSize: '0.75rem' }}
              >
                Discuter avec l'Assistant
              </Button>
              <Button
                size="small"
                variant="text"
                color="inherit"
                onClick={() => {
                  setIaRapport(null);
                  setIaAlertes([]);
                }}
                sx={{ textTransform: 'none', py: 0.2, px: 1, fontSize: '0.75rem' }}
              >
                Ignorer
              </Button>
            </Stack>
          </Box>

          <Typography variant="subtitle2" sx={{ fontWeight: 700, color: '#14532D', mt: 0.5 }}>
            {iaRapport}
          </Typography>

          {/* Sources officielles RAG */}
          {iaSources.length > 0 && (
            <Box sx={{ mt: 1, display: 'flex', alignItems: 'center', gap: 0.5, flexWrap: 'wrap' }}>
              <MenuBookIcon sx={{ fontSize: 13, color: '#15803D' }} />
              <Typography variant="caption" sx={{ fontWeight: 600, color: '#15803D' }}>
                Sources réglementaires :
              </Typography>
              {iaSources.map((s, idx) => (
                <Chip key={idx} label={s} size="small" variant="outlined" sx={{ fontSize: '0.65rem', height: 18, bgcolor: 'rgba(255,255,255,0.7)' }} />
              ))}
            </Box>
          )}

          {/* Alertes et informations manquantes */}
          {iaAlertes.length > 0 && (
            <Box sx={{ mt: 1, p: 1, bgcolor: '#FEF3C7', borderRadius: 1.5, border: '1px solid #FDE68A' }}>
              <Typography variant="caption" sx={{ fontWeight: 700, color: '#92400E' }}>
                Points d'attention / Informations manquantes :
              </Typography>
              <ul style={{ margin: '2px 0 0 16px', padding: 0, fontSize: '0.8rem', color: '#92400E' }}>
                {iaAlertes.map((a, i) => (
                  <li key={i}>{a}</li>
                ))}
              </ul>
            </Box>
          )}

          {/* Suggestions cliquables */}
          {!fieldsLocked && (iaSuggestions.risques.length + iaSuggestions.mesures.length + iaSuggestions.epis.length + iaSuggestions.permis.length > 0) && (
            <Box sx={{ mt: 1.5, pt: 1, borderTop: '1px dashed #86EFAC' }}>
              <Typography variant="caption" sx={{ fontWeight: 700, color: '#166534' }}>
                Suggestions détectées (cliquez pour cocher ou appliquer) :
              </Typography>
              <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5, mt: 0.5 }}>
                {iaSuggestions.risques
                  .filter((label) => !refRisques.some((r) => (r.nom || r.nomRisque || '').toLowerCase() === label.toLowerCase() && isChecked('risquesIds', r.id)))
                  .map((label) => (
                    <Chip key={`r-${label}`} label={`+ ${label}`} size="small" variant="filled" color="error"
                      onClick={() => appliquerSuggestion('risquesIds', refRisques, label)} sx={{ cursor: 'pointer' }} />
                  ))}
                {iaSuggestions.mesures
                  .filter((label) => !refMesures.some((m) => (m.nom || m.nomMesure || '').toLowerCase() === label.toLowerCase() && isChecked('mesuresIds', m.id)))
                  .map((label) => (
                    <Chip key={`m-${label}`} label={`+ ${label}`} size="small" variant="filled" color="success"
                      onClick={() => appliquerSuggestion('mesuresIds', refMesures, label)} sx={{ cursor: 'pointer' }} />
                  ))}
                {iaSuggestions.epis
                  .filter((label) => !refEpis.some((e) => (e.nom || e.nomEPI || '').toLowerCase() === label.toLowerCase() && isChecked('episIds', e.id)))
                  .map((label) => (
                    <Chip key={`e-${label}`} label={`+ ${label}`} size="small" variant="filled" color="secondary"
                      onClick={() => appliquerSuggestion('episIds', refEpis, label)} sx={{ cursor: 'pointer' }} />
                  ))}
                {iaSuggestions.permis
                  .filter((label) => !refPermis.some((p) => (p.nomType || p.nom || '').toLowerCase() === label.toLowerCase() && isChecked('permisIds', p.id)))
                  .map((label) => (
                    <Chip key={`p-${label}`} label={`+ ${label}`} size="small" variant="filled" color="warning"
                      onClick={() => appliquerSuggestion('permisIds', refPermis, label)} sx={{ cursor: 'pointer' }} />
                  ))}
              </Box>
            </Box>
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
          background: 'linear-gradient(135deg, #1F4D3E 0%, #163C30 100%)',
          color: '#FFFFFF',
          position: 'relative',
          overflow: 'hidden',
        }}
      >
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 2, position: 'relative', zIndex: 1 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
            <Box
              component="img"
              src="/OCP_Group.svg.webp"
              alt="Logo OCP"
              sx={{ width: 56, height: 56, objectFit: 'contain', borderRadius: 2.5, bgcolor: 'rgba(255,255,255,0.15)', backdropFilter: 'blur(10px)', p: 0.5 }}
            />
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
            <Button
              variant="contained"
              size="small"
              startIcon={iaLoading ? <CircularProgress size={16} color="inherit" /> : <AutoAwesomeIcon />}
              onClick={lancerAnalyseIA}
              disabled={iaLoading || fieldsLocked}
              sx={{
                bgcolor: 'rgba(255,255,255,0.2)',
                color: '#FFFFFF',
                fontWeight: 700,
                backdropFilter: 'blur(8px)',
                '&:hover': { bgcolor: 'rgba(255,255,255,0.35)' },
                border: '1px solid rgba(255,255,255,0.4)',
                textTransform: 'none',
              }}
            >
              {iaLoading ? 'Analyse CrewAI...' : 'Analyser avec l\'IA'}
            </Button>
            <Button
              variant="outlined"
              size="small"
              startIcon={<SmartToyIcon />}
              onClick={() => setAiDrawerOpen(true)}
              sx={{
                color: '#FFFFFF',
                borderColor: 'rgba(255,255,255,0.4)',
                fontWeight: 700,
                '&:hover': { borderColor: '#FFFFFF', bgcolor: 'rgba(255,255,255,0.1)' },
                textTransform: 'none',
              }}
            >
              Assistant IA
            </Button>
            <Chip
              label={formData.numero ? `AT N° ${formData.numero}` : 'Brouillon en cours'}
              sx={{ bgcolor: 'rgba(255,255,255,0.2)', color: '#FFFFFF', fontWeight: 800, fontSize: 13, border: '1px solid rgba(255,255,255,0.3)' }}
            />
          </Stack>
        </Box>
      </Paper>

      {/* SECTION A: NATURE DE L'AT & SERVICES */}
      <Card sx={{ mb: 3, borderRadius: 3, border: '1px solid #D6E3DC', boxShadow: '0 4px 12px rgba(0,0,0,0.03)' }}>
        <CardHeader
          avatar={<AssignmentIcon sx={{ color: '#1F4D3E' }} />}
          title={
            <Typography variant="h6" sx={{ fontWeight: 800, color: '#16241E' }}>
              Section A &bull; Nature de l'AT & Affectation des Services
            </Typography>
          }
          subheader="Identification du type d'AT et définition du service demandeur vs service exécutant"
          sx={{ bgcolor: '#F7FAF8', borderBottom: '1px solid #D6E3DC', py: 1.5 }}
        />
        <CardContent sx={{ p: { xs: 2, md: 3 } }}>
          {/* 1. Nature de l'AT */}
          <Box>
            <Typography
              variant="caption"
              sx={{
                fontWeight: 800,
                color: '#5C6E67',
                mb: 1,
                display: 'block',
                textTransform: 'uppercase',
                letterSpacing: 0.4,
              }}
            >
              1. Nature de l'Autorisation de Travail
            </Typography>

            <Grid container spacing={1.5}>
              {[
                { key: 'DI', label: "Demande d'Intervention (DI)" },
                { key: 'OT', label: "Ordre de Travail (OT)" },
                { key: 'BT', label: "Bon de Travail (BT)" },
              ].map((item) => {
                const selected = (formData.documentSourceType || docSourceType) === item.key;

                return (
                  <Grid key={item.key} size={{ xs: 12, sm: 4 }}>
                    <Paper
                      elevation={0}
                      onClick={() => {
                        if (fieldsLocked) return;
                        setDocSourceType(item.key as any);
                        const randomNum = `${item.key}-2026-${Math.floor(1000 + Math.random() * 9000)}`;
                        setFormData((prev: any) => {
                          const updated = {
                            ...prev,
                            documentSourceType: item.key,
                            documentSourceNumero: randomNum,
                            di: item.key === 'DI' ? randomNum : '',
                            ot: item.key === 'OT' ? randomNum : '',
                            bt: item.key === 'BT' ? randomNum : '',
                          };
                          scheduleAutoSave(updated);
                          onChange?.(updated);
                          return updated;
                        });
                      }}
                      sx={{
                        minHeight: 76,
                        p: 1.5,
                        textAlign: 'center',
                        borderRadius: 2,
                        cursor: fieldsLocked ? 'default' : 'pointer',
                        border: selected ? '2px solid #1F4D3E' : '1px solid #D6E3DC',
                        bgcolor: selected ? '#EDF2EE' : '#FFFFFF',
                        transition: 'all 0.15s ease-in-out',
                        display: 'flex',
                        flexDirection: 'column',
                        alignItems: 'center',
                        justifyContent: 'center',
                        '&:hover': fieldsLocked
                          ? {}
                          : { borderColor: '#1F4D3E', bgcolor: '#EDF2EE' },
                      }}
                    >
                      <Typography
                        variant="subtitle2"
                        sx={{
                          fontWeight: 800,
                          color: selected ? '#1F4D3E' : '#5C6E67',
                        }}
                      >
                        {item.key}
                      </Typography>
                      <Typography
                        variant="caption"
                        sx={{
                          color: selected ? '#1F4D3E' : '#5C6E67',
                          fontSize: 11,
                          display: 'block',
                        }}
                      >
                        {item.label}
                      </Typography>
                    </Paper>
                  </Grid>
                );
              })}
            </Grid>

            {/* Champ de saisie / affichage du Numéro de Document Source (DI / OT / BT) */}
            <Box sx={{ mt: 2 }}>
              <TextField
                fullWidth
                size="small"
                label={`N° du document source (${formData.documentSourceType || docSourceType}) - affecté automatiquement`}
                value={formData.documentSourceNumero || ''}
                disabled={fieldsLocked}
                onChange={(e) => {
                  const val = e.target.value;
                  const curType = formData.documentSourceType || docSourceType;
                  setFormData((prev: any) => {
                    const updated = {
                      ...prev,
                      documentSourceNumero: val,
                      di: curType === 'DI' ? val : prev.di,
                      ot: curType === 'OT' ? val : prev.ot,
                      bt: curType === 'BT' ? val : prev.bt,
                    };
                    scheduleAutoSave(updated);
                    onChange?.(updated);
                    return updated;
                  });
                }}
                helperText={`Ce numéro (${formData.documentSourceType || docSourceType}) sera automatiquement incrusté dans la case correspondante (N° DI / N° OT / N° BT) du formulaire et du PDF officiel.`}
              />
            </Box>
          </Box>

          <Divider sx={{ my: 2.5 }} />

          {/* 2. Affectation propriétaire */}
          <Box>
            <Typography
              variant="caption"
              sx={{
                fontWeight: 800,
                color: '#5C6E67',
                mb: 1,
                display: 'block',
                textTransform: 'uppercase',
                letterSpacing: 0.4,
              }}
            >
              2. Affectation propriétaire
            </Typography>

            <Grid container spacing={2}>
              {/* Site / Zone Propriétaire */}
              <Grid size={{ xs: 12, md: 6 }}>
                <Typography
                  variant="caption"
                  sx={{ fontWeight: 700, color: '#5C6E67', mb: 0.5, display: 'block' }}
                >
                  Site / Zone Propriétaire (P)
                </Typography>
                <FormControl fullWidth size="small" disabled={readOnly}>
                  <Select
                    value={formData.site || formData.zoneProprietaireNom || ''}
                    displayEmpty
                    onChange={(e) => {
                      const zoneNom = e.target.value;
                      const foundZ = zonesList.find(z => z.nomZone === zoneNom || z.id === zoneNom);
                      const zNom = foundZ?.nomZone || zoneNom;
                      const zId = foundZ?.id || zoneNom;
                      setFormData((prev: any) => {
                        const updated = {
                          ...prev,
                          site: zNom,
                          zoneProprietaireId: zId,
                          zoneProprietaireNom: zNom,
                          zoneProprietaire: zNom,
                        };
                        scheduleAutoSave(updated);
                        onChange?.(updated);
                        return updated;
                      });
                    }}
                  >
                    <MenuItem value="">
                      <em>-- Sélectionner zone propriétaire --</em>
                    </MenuItem>
                    {zonesList.map((z) => (
                      <MenuItem key={z.id} value={z.nomZone}>
                        {z.nomZone}
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>
              </Grid>

              {/* Service Demandeur */}
              <Grid size={{ xs: 12, md: 6 }}>
                <Typography
                  variant="caption"
                  sx={{ fontWeight: 700, color: '#5C6E67', mb: 0.5, display: 'block' }}
                >
                  Service Demandeur / Propriétaire (P)
                </Typography>
                <FormControl fullWidth size="small" disabled={readOnly}>
                  <Select
                    value={formData.entite || formData.serviceDemandeur || ''}
                    displayEmpty
                    onChange={(e) => {
                      const srvNom = e.target.value;
                      const foundS = servicesList.find(s => s.nomService === srvNom || s.id === srvNom);
                      const sNom = foundS?.nomService || srvNom;
                      const sId = foundS?.id || srvNom;
                      setFormData((prev: any) => {
                        const updated = {
                          ...prev,
                          entite: sNom,
                          serviceDemandeur: sNom,
                          serviceDemandeurId: sId,
                        };
                        scheduleAutoSave(updated);
                        onChange?.(updated);
                        return updated;
                      });
                    }}
                  >
                    <MenuItem value="">
                      <em>-- Sélectionner service demandeur --</em>
                    </MenuItem>
                    {servicesList.map((s) => (
                      <MenuItem key={s.id} value={s.nomService}>
                        {s.nomService}
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>
              </Grid>
            </Grid>
          </Box>

          <Divider sx={{ my: 2.5 }} />

          {/* 3. Exécution */}
          <Box>
            <Typography
              variant="caption"
              sx={{
                fontWeight: 800,
                color: '#5C6E67',
                mb: 1,
                display: 'block',
                textTransform: 'uppercase',
                letterSpacing: 0.4,
              }}
            >
              3. Exécution de l'intervention
            </Typography>

            <Grid container spacing={2}>
              {/* Zone Exécutante / Lieu d'intervention */}
              <Grid size={{ xs: 12, md: 4 }}>
                <Typography
                  variant="caption"
                  sx={{ fontWeight: 700, color: '#5C6E67', mb: 0.5, display: 'block' }}
                >
                  Zone Exécutante / Lieu d'intervention (E) *
                </Typography>
                <FormControl fullWidth size="small" disabled={fieldsLocked}>
                  <Select
                    value={formData.zoneExecutanteNom || formData.zoneExecutante || formData.lieu || ''}
                    displayEmpty
                    onChange={(e) => {
                      const val = e.target.value;
                      const foundZ = zonesList.find(z => z.nomZone === val || z.id === val);
                      const zNom = foundZ?.nomZone || val;
                      const zId = foundZ?.id || val;
                      setFormData((prev: any) => {
                        const updated = {
                          ...prev,
                          zoneExecutanteId: zId,
                          zoneExecutanteNom: zNom,
                          zoneExecutante: zNom,
                          lieu: zNom,
                        };
                        scheduleAutoSave(updated);
                        onChange?.(updated);
                        return updated;
                      });
                    }}
                  >
                    <MenuItem value="">
                      <em>-- Sélectionner zone exécutante --</em>
                    </MenuItem>
                    {zonesList.map((z) => (
                      <MenuItem key={z.id} value={z.nomZone}>
                        {z.nomZone}
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>
              </Grid>

              {/* Service Intervenant / Exécutant (CEEE) */}
              <Grid size={{ xs: 12, md: 4 }}>
                <Typography
                  variant="caption"
                  sx={{ fontWeight: 700, color: '#5C6E67', mb: 0.5, display: 'block' }}
                >
                  Service Intervenant / Exécutant (CEEE) *
                </Typography>
                <FormControl fullWidth size="small" disabled={fieldsLocked}>
                  <Select
                    value={formData.servicesIntervenants || formData.serviceIntervenant || ''}
                    displayEmpty
                    onChange={(e) => {
                      const val = e.target.value;
                      const foundS = servicesList.find(s => s.nomService === val || s.id === val);
                      const sNom = foundS?.nomService || val;
                      const sId = foundS?.id || val;
                      setFormData((prev: any) => {
                        const updated = {
                          ...prev,
                          serviceIntervenantId: sId,
                          servicesIntervenants: sNom,
                          serviceIntervenant: sNom,
                        };
                        scheduleAutoSave(updated);
                        onChange?.(updated);
                        return updated;
                      });
                    }}
                  >
                    <MenuItem value="">
                      <em>-- Sélectionner le service exécutant --</em>
                    </MenuItem>
                    {servicesList.map((s) => (
                      <MenuItem key={s.id} value={s.nomService}>
                        {s.nomService}
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>
              </Grid>

              {/* Entreprise extérieure */}
              <Grid size={{ xs: 12, md: 4 }}>
                <Typography
                  variant="caption"
                  sx={{ fontWeight: 700, color: '#5C6E67', mb: 0.5, display: 'block' }}
                >
                  Entreprise Extérieure (Tiers / Sous-traitant)
                </Typography>
                <FormControl fullWidth size="small" disabled={fieldsLocked}>
                  <Select
                    value={formData.entreprisesIntervenantes || ''}
                    displayEmpty
                    onChange={(e) => updateTextValue('entreprisesIntervenantes', e.target.value)}
                  >
                    <MenuItem value="">
                      <em>Aucune (Régie interne OCP)</em>
                    </MenuItem>
                    {entreprisesList.map((ee) => (
                      <MenuItem key={ee.id} value={ee.nomEntreprise}>
                        {ee.nomEntreprise}
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>
              </Grid>
            </Grid>
          </Box>
        </CardContent>
      </Card>

      {/* SECTION 1: OBJET & HORAIRES DE L'INTERVENTION */}
      <Card sx={{ mb: 3, borderRadius: 3, border: '1px solid #D6E3DC', boxShadow: '0 4px 12px rgba(0,0,0,0.03)' }}>
        <CardHeader
          avatar={<AccessTimeIcon sx={{ color: '#1F4D3E' }} />}
          title={<Typography variant="h6" sx={{ fontWeight: 800, color: '#16241E' }}>Description & Planning des Travaux</Typography>}
          subheader="Objet détaillé et fenêtre d'intervention programmée"
          sx={{ bgcolor: '#F7FAF8', borderBottom: '1px solid #D6E3DC', py: 1.5 }}
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
                helperText={!fieldsLocked ? "L'IA analyse automatiquement cette description pour suggérer risques, mesures, EPI et permis." : undefined}
              />
            </Grid>
          </Grid>
        </CardContent>
      </Card>

      {/* VISITE PRÉALABLE CONJOINTE DU CHANTIER (§8.2 STANDARD OCP) */}
      <Card sx={{ mb: 3, borderRadius: 3, border: '2px solid #1F4D3E', boxShadow: '0 4px 16px rgba(31,77,62,0.08)' }}>
        <CardHeader
          avatar={<BuildIcon sx={{ color: '#2E624A' }} />}
          title={
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
              <Typography variant="h6" sx={{ fontWeight: 800, color: '#2E624A' }}>
                Visite Préalable Conjointe du Chantier (§8.2 Standard OCP)
              </Typography>
              <Chip
                label={preventionEnPlace ? "Visite validée ✓" : "Visite obligatoire ⚠️"}
                color={preventionEnPlace ? "success" : "warning"}
                size="small"
                sx={{ fontWeight: 800 }}
              />
            </Box>
          }
          subheader="Vérification conjointe sur le terrain (CEEP + CEEE) avec géolocalisation GPS et photo d'inspection"
          sx={{ bgcolor: '#EDF2EE', borderBottom: '1px solid #7FC8A9', py: 1.5 }}
        />
        <CardContent sx={{ p: 3 }}>
          <Grid container spacing={2.5}>
            {/* Relevé GPS */}
            <Grid size={{ xs: 12, md: 6 }}>
              <Paper variant="outlined" sx={{ p: 2, textAlign: 'center', bgcolor: '#F7FAF8', borderRadius: 2 }}>
                <Typography variant="subtitle2" sx={{ fontWeight: 800, mb: 1, color: '#16241E' }}>
                  1. Géolocalisation GPS du Chantier (§8.2)
                </Typography>
                {gpsCoords ? (
                  <Chip
                    label={`GPS: Lat ${gpsCoords.lat.toFixed(5)}, Lng ${gpsCoords.lng.toFixed(5)}`}
                    color="success"
                    sx={{ fontSize: 12, py: 1.8, px: 1, fontWeight: 700 }}
                  />
                ) : (
                  <Button
                    variant="contained"
                    size="small"
                    color="primary"
                    disabled={fieldsLocked}
                    onClick={handleGetGps}
                    sx={{ fontWeight: 700 }}
                  >
                    {gpsLoading ? 'Acquisition GPS...' : 'Relever position GPS sur le chantier'}
                  </Button>
                )}
              </Paper>
            </Grid>

            {/* Photo Chantier */}
            <Grid size={{ xs: 12, md: 6 }}>
              <Paper variant="outlined" sx={{ p: 2, textAlign: 'center', bgcolor: '#F7FAF8', borderRadius: 2 }}>
                <Typography variant="subtitle2" sx={{ fontWeight: 800, mb: 1, color: '#16241E' }}>
                  2. Photo d'Inspection du Chantier (§8.2)
                </Typography>
                <input
                  type="file"
                  accept="image/*"
                  capture="environment"
                  ref={photoInputRef}
                  onChange={handlePhotoUpload}
                  style={{ display: 'none' }}
                />
                {visitePhotoName ? (
                  <Chip
                    label={`Photo : ${visitePhotoName}`}
                    color="success"
                    sx={{ fontSize: 12, py: 1.8, px: 1, fontWeight: 700 }}
                  />
                ) : (
                  <Button
                    variant="contained"
                    size="small"
                    color="secondary"
                    disabled={fieldsLocked}
                    onClick={() => photoInputRef.current?.click()}
                    sx={{ fontWeight: 700 }}
                  >
                    Prendre une photo du chantier
                  </Button>
                )}
              </Paper>
            </Grid>

            {/* Observations terrain */}
            <Grid size={12}>
              <TextField
                fullWidth
                size="small"
                multiline
                rows={2}
                label="Constats & Remarques de la Visite Terrain (Zone d'intervention, accès, risques spécifiques)"
                placeholder="Indiquer les observations de la visite conjointe CEEP + CEEE..."
                value={visiteCommentaire}
                disabled={fieldsLocked}
                onChange={(e) => setVisiteCommentaire(e.target.value)}
              />
            </Grid>

            {/* Point de contrôle obligatoire §8.2 */}
            <Grid size={12}>
              <Paper
                sx={{
                  p: 2,
                  bgcolor: preventionEnPlace ? '#EDF2EE' : '#F7FAF8',
                  border: `2px solid ${preventionEnPlace ? '#3C7A5C' : '#9A3D2F'}`,
                  borderRadius: 2,
                }}
              >
                <FormControlLabel
                  control={
                    <Checkbox
                      checked={preventionEnPlace}
                      disabled={fieldsLocked}
                      onChange={(e) => setPreventionEnPlace(e.target.checked)}
                      color="success"
                    />
                  }
                  label={
                    <Typography variant="subtitle2" sx={{ fontWeight: 900, color: preventionEnPlace ? '#2E624A' : '#7A2E1A' }}>
                      Point de contrôle obligatoire (§8.2) : « Les conditions de sécurité et les mesures de prévention ont été inspectées conjointement et sont effectivement mises en place sur le chantier »
                    </Typography>
                  }
                />
                {!preventionEnPlace && (
                  <Typography variant="caption" sx={{ color: '#7A2E1A', display: 'block', mt: 0.5, ml: 4, fontStyle: 'italic' }}>
                    ⚠️ La validation de cette visite préalable est obligatoire avant la soumission et signature de l'AT.
                  </Typography>
                )}
              </Paper>
            </Grid>
          </Grid>
        </CardContent>
      </Card>

      {/* SECTION A: RISQUES LIÉS AUX TRAVAUX */}
      <Card sx={{ mb: 3, borderRadius: 3, border: '1px solid #FBEAE3', boxShadow: '0 4px 12px rgba(154,61,47,0.05)' }}>
        <CardHeader
          avatar={<WarningAmberIcon sx={{ color: '#9A3D2F' }} />}
          title={
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
              <Typography variant="h6" sx={{ fontWeight: 800, color: '#7A2E1A' }}>
                A. Risques liés aux travaux
              </Typography>
              <Chip label={`${formData.risquesIds.length} sélectionné(s)`} color="error" size="small" sx={{ fontWeight: 800 }} />
            </Box>
          }
          subheader="Identification des risques majeurs HSE sur le chantier"
          sx={{ bgcolor: '#F7FAF8', borderBottom: '1px solid #FBEAE3', py: 1.5 }}
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
                      border: active ? '2px solid #9A3D2F' : '1px solid #D6E3DC',
                      bgcolor: active ? '#FBEAE3' : '#FFFFFF',
                      transition: 'all 0.15s ease-in-out',
                      display: 'flex',
                      alignItems: 'center',
                      gap: 1.5,
                      '&:hover': fieldsLocked ? {} : { borderColor: '#9A3D2F', bgcolor: '#F7FAF8' },
                    }}
                  >
                    {active ? <CheckBoxIcon sx={{ color: '#9A3D2F' }} /> : <CheckBoxOutlineBlankIcon sx={{ color: '#5C6E67' }} />}
                    <Typography variant="body2" sx={{ fontWeight: active ? 700 : 500, color: active ? '#7A2E1A' : '#16241E' }}>
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
      <Card sx={{ mb: 3, borderRadius: 3, border: '1px solid #E2F0E8', boxShadow: '0 4px 12px rgba(60,122,92,0.05)' }}>
        <CardHeader
          avatar={<ShieldIcon sx={{ color: '#1F4D3E' }} />}
          title={
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
              <Typography variant="h6" sx={{ fontWeight: 800, color: '#1F4D3E' }}>
                B. Mesures de sécurité à prendre
              </Typography>
              <Chip label={`${formData.mesuresIds.length} active(s)`} color="success" size="small" sx={{ fontWeight: 800, bgcolor: '#1F4D3E' }} />
            </Box>
          }
          subheader="Consignes obligatoires de consignation, isolation et préparation"
          sx={{ bgcolor: '#EDF2EE', borderBottom: '1px solid #E2F0E8', py: 1.5 }}
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
                      border: active ? '2px solid #1F4D3E' : '1px solid #D6E3DC',
                      bgcolor: active ? '#E2F0E8' : '#FFFFFF',
                      transition: 'all 0.15s ease-in-out',
                      display: 'flex',
                      alignItems: 'center',
                      gap: 1.5,
                      '&:hover': fieldsLocked ? {} : { borderColor: '#7FC8A9', bgcolor: '#EDF2EE' },
                    }}
                  >
                    {active ? <CheckBoxIcon sx={{ color: '#1F4D3E' }} /> : <CheckBoxOutlineBlankIcon sx={{ color: '#5C6E67' }} />}
                    <Typography variant="body2" sx={{ fontWeight: active ? 700 : 500, color: active ? '#1F4D3E' : '#16241E' }}>
                      {m.nom || m.nomMesure || m.libelle}
                    </Typography>
                  </Paper>
                </Grid>
              );
            })}
          </Grid>
        </CardContent>
      </Card>

      {/* SECTION C: MOYENS D'ACCÈS */}
      <Card sx={{ mb: 3, borderRadius: 3, border: '1px solid #D6E3DC', boxShadow: '0 4px 12px rgba(0,0,0,0.03)' }}>
        <CardHeader
          avatar={<BuildIcon sx={{ color: '#3C7A5C' }} />}
          title={
            <Typography variant="h6" sx={{ fontWeight: 800, color: '#2E624A' }}>
              C. Moyens d'accès
            </Typography>
          }
          subheader="Échafaudages, nacelles, échelles"
          sx={{ bgcolor: '#EDF2EE', borderBottom: '1px solid #E2F0E8', py: 1.5 }}
        />
        <CardContent sx={{ p: { xs: 2, md: 2.5 } }}>
          <Grid container spacing={1.5}>
            {refMoyens.map((ma) => {
              const active = isChecked('moyensAccesIds', ma.id);

              return (
                <Grid key={ma.id} size={{ xs: 12, sm: 6, md: 3 }}>
                  <Paper
                    elevation={0}
                    onClick={() => toggleCheckbox('moyensAccesIds', ma.id)}
                    sx={{
                      minHeight: 52,
                      p: 1.25,
                      borderRadius: 2,
                      cursor: fieldsLocked ? 'default' : 'pointer',
                      border: active ? '2px solid #3C7A5C' : '1px solid #D6E3DC',
                      bgcolor: active ? '#EDF2EE' : '#FFFFFF',
                      display: 'flex',
                      alignItems: 'center',
                      gap: 1.25,
                      transition: 'all 0.15s ease-in-out',
                      '&:hover': fieldsLocked
                        ? {}
                        : { borderColor: '#3C7A5C', bgcolor: '#EDF2EE' },
                    }}
                  >
                    {active ? (
                      <CheckBoxIcon sx={{ color: '#3C7A5C', flexShrink: 0 }} />
                    ) : (
                      <CheckBoxOutlineBlankIcon sx={{ color: '#5C6E67', flexShrink: 0 }} />
                    )}
                    <Typography
                      variant="body2"
                      sx={{
                        fontWeight: active ? 700 : 500,
                        color: active ? '#2E624A' : '#16241E',
                      }}
                    >
                      {ma.nomMoyen || ma.nom || ma.libelle || ma.descriptionMoyen}
                    </Typography>
                  </Paper>
                </Grid>
              );
            })}
          </Grid>
        </CardContent>
      </Card>

      {/* SECTION D: ÉQUIPEMENTS DE PROTECTION (EPI) */}
      <Card sx={{ mb: 3, borderRadius: 3, border: '1px solid #D6E3DC', boxShadow: '0 4px 12px rgba(0,0,0,0.03)' }}>
        <CardHeader
          avatar={<RuleIcon sx={{ color: '#1F4D3E' }} />}
          title={
            <Typography variant="h6" sx={{ fontWeight: 800, color: '#2E624A' }}>
              D. Équipements de Protection (EPI)
            </Typography>
          }
          subheader="Protections individuelles requises"
          sx={{ bgcolor: '#EDF2EE', borderBottom: '1px solid #E3ECE7', py: 1.5 }}
        />
        <CardContent sx={{ p: { xs: 2, md: 2.5 } }}>
          <Grid container spacing={1.5}>
            {refEpis.map((e) => {
              const active = isChecked('episIds', e.id);

              return (
                <Grid key={e.id} size={{ xs: 12, sm: 6, md: 4 }}>
                  <Paper
                    elevation={0}
                    onClick={() => toggleCheckbox('episIds', e.id)}
                    sx={{
                      minHeight: 52,
                      p: 1.25,
                      borderRadius: 2,
                      cursor: fieldsLocked ? 'default' : 'pointer',
                      border: active ? '2px solid #1F4D3E' : '1px solid #D6E3DC',
                      bgcolor: active ? '#EDF2EE' : '#FFFFFF',
                      display: 'flex',
                      alignItems: 'center',
                      gap: 1.25,
                      transition: 'all 0.15s ease-in-out',
                      '&:hover': fieldsLocked
                        ? {}
                        : { borderColor: '#1F4D3E', bgcolor: '#EDF2EE' },
                    }}
                  >
                    {active ? (
                      <CheckBoxIcon sx={{ color: '#1F4D3E', flexShrink: 0 }} />
                    ) : (
                      <CheckBoxOutlineBlankIcon sx={{ color: '#5C6E67', flexShrink: 0 }} />
                    )}
                    <Typography
                      variant="body2"
                      sx={{
                        fontWeight: active ? 700 : 500,
                        color: active ? '#2E624A' : '#16241E',
                      }}
                    >
                      {e.nomEPI || e.nomepi || e.nomEpi || e.nom || e.libelle || e.descriptionEPI}
                    </Typography>
                  </Paper>
                </Grid>
              );
            })}
          </Grid>
        </CardContent>
      </Card>

      {/* SECTION E: PERMIS COMPLÉMENTAIRES OBLIGATOIRES */}
      <Card sx={{ mb: 3, borderRadius: 3, border: '1px solid #D6B56A', boxShadow: '0 4px 12px rgba(60,122,92,0.08)' }}>
        <CardHeader
          avatar={<WarningAmberIcon sx={{ color: '#A87532' }} />}
          title={
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
              <Typography variant="h6" sx={{ fontWeight: 800, color: '#7A5A27' }}>
                E. Permis complémentaires obligatoires
              </Typography>
              <Chip label={`${formData.permisIds.length} requis`} color="warning" size="small" sx={{ fontWeight: 800 }} />
            </Box>
          }
          subheader="Tout permis coché doit être annexé et validé CONFORME pour valider l'export PDF"
          sx={{ bgcolor: '#F6EEDC', borderBottom: '1px solid #D6B56A', py: 1.5 }}
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
                      border: active ? '2px solid #A87532' : '1px solid #D6E3DC',
                      bgcolor: active ? '#F6EEDC' : '#FFFFFF',
                      display: 'flex',
                      alignItems: 'center',
                      gap: 1.5,
                      '&:hover': fieldsLocked ? {} : { borderColor: '#A87532', bgcolor: '#F6EEDC' },
                    }}
                  >
                    {active ? <CheckBoxIcon sx={{ color: '#A87532' }} /> : <CheckBoxOutlineBlankIcon sx={{ color: '#5C6E67' }} />}
                    <Typography variant="body2" sx={{ fontWeight: active ? 700 : 500, color: active ? '#7A5A27' : '#16241E' }}>
                      {p.nomType || p.nom || p.libelle}
                    </Typography>
                  </Paper>
                </Grid>
              );
            })}
          </Grid>

          {/* Zone d upload et validation IA - visible si permis cochés et mode édition */}
          {!fieldsLocked && permisDocuments.length > 0 && (
            <Box sx={{ mt: 3 }}>
              <Divider sx={{ mb: 2 }} />
              <Typography variant="subtitle2" sx={{ fontWeight: 700, color: '#7A5A27', mb: 1.5 }}>
                📋 Documents de permis - Validation IA requise avant soumission
              </Typography>
              {permisDocuments.map((doc) => (
                <PermisUploadCard
                  key={doc.id}
                  doc={doc}
                  onUpload={handlePermisUpload}
                  onRelancer={handleRelancerAnalyse}
                  uploading={permisUploading}
                />
              ))}
              {soumissionBloquee && (
                <Alert severity="warning" sx={{ mt: 1, fontWeight: 600 }}>
                  ⚠ {permisEnAttente} permis en attente de validation - soumission bloquée jusqu'à validation complète
                </Alert>
              )}
            </Box>
          )}
        </CardContent>
      </Card>

      {/* SECTION F: MESURES SPÉCIFIQUES EXÉCUTANT */}

      <Card sx={{ mb: 3, borderRadius: 3, border: '1px solid #D6E3DC' }}>
        <CardHeader
          avatar={<RuleIcon sx={{ color: '#1F4D3E' }} />}
          title={<Typography variant="h6" sx={{ fontWeight: 800, color: '#16241E' }}>F. Mesures particulières de l'Exécutant</Typography>}
          subheader="Consignes spécifiques ajoutées par le Chef d'Équipe ou l'Entreprise Extérieure"
          sx={{ bgcolor: '#F7FAF8', borderBottom: '1px solid #D6E3DC', py: 1.5 }}
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

      {/* SECTION G: SIGNATURE CEEP */}
      <Box sx={{ width: '100%', mb: 3 }}>
        <Card sx={{ borderRadius: 3, border: '1px solid #D6E3DC', overflow: 'hidden' }}>
          <CardHeader
            avatar={<DrawIcon sx={{ color: '#1F4D3E' }} />}
            title={<Typography variant="h6" sx={{ fontWeight: 800, color: '#16241E' }}>G. Validation & Signature</Typography>}
            subheader="Signature du Chef d'Équipe Émetteur (CEEP) pour validation de l'AT"
            sx={{ bgcolor: '#F7FAF8', borderBottom: '1px solid #D6E3DC', py: 1.5 }}
          />
          <CardContent sx={{ p: { xs: 2, md: 2.5 } }}>
            <Stack spacing={1.5} sx={{ width: '100%', maxWidth: 520 }}>
              <TextField
                fullWidth
                size="small"
                label="Chef d'Équipe Émetteur (CEEP)"
                value={formData.g1NomCeep}
                disabled={fieldsLocked}
                onChange={(e) => updateTextValue('g1NomCeep', e.target.value)}
              />
              <Button
                variant={formData.g1VisaCeep ? 'contained' : 'outlined'}
                size="medium"
                color="primary"
                startIcon={<DrawIcon />}
                disabled={!canSignCeep}
                onClick={() => handleOpenSignature('g1VisaCeep')}
                sx={{
                  width: '100%',
                  justifyContent: 'flex-start',
                  textTransform: 'none',
                  fontWeight: 700,
                  py: 1.1,
                  boxShadow: canSignCeep && !formData.g1VisaCeep ? '0 0 0 2px #2E624A40' : 'none',
                }}
              >
                {formData.g1VisaCeep ? '✅ Visa CEEP signé - cliquer pour modifier' : '✍️ Signer l\'AT (Visa CEEP)'}
              </Button>
            </Stack>
          </CardContent>
        </Card>
      </Box>

      {/* BARRE D'ACTIONS INFÉRIEURE */}
      {(!readOnly || signMode === 'ceee') && !isSubmittedOrTransmitted && (
        <Box sx={{ width: '100%', mb: 3 }}>
          <Paper
            elevation={3}
            sx={{
              p: { xs: 2, md: 2.5 },
              borderRadius: 3,
              bgcolor: '#FFFFFF',
              border: '2px solid #1F4D3E',
            }}
          >
            <Typography variant="subtitle1" sx={{ fontWeight: 800, color: '#1F4D3E', mb: 0.5 }}>
              Validation & Transmission du Formulaire F-HSE-SEC-31-04
            </Typography>
            <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mb: 2 }}>
              Conforme au Standard OCP S-HSE-SEC-31. Assurez-vous que les informations et signatures sont saisies.
            </Typography>

            <Stack
              direction="row"
              spacing={1.5}
              sx={{
                flexWrap: 'wrap',
                alignItems: 'center',
                justifyContent: 'flex-start',
                rowGap: 1.5,
              }}
            >
              {onSave && (
                <Button
                  variant="outlined"
                  color="primary"
                  onClick={() => onSave(formData)}
                  disabled={loading}
                  sx={{ fontWeight: 700, borderRadius: 2, px: 2.5, py: 1 }}
                >
                  Enregistrer Brouillon
                </Button>
              )}
              {onSubmitAT && signMode !== 'ceee' && (
                <Button
                  variant="contained"
                  onClick={handleSignerEtTransmettre}
                  disabled={loading}
                  startIcon={<CheckCircleIcon />}
                  sx={{ bgcolor: '#1F4D3E', '&:hover': { bgcolor: '#1F4D3E' }, fontWeight: 800, borderRadius: 2, px: 3, py: 1 }}
                >
                  {loading ? <CircularProgress size={24} color="inherit" /> : 'Signer & Transmettre au CEEE'}
                </Button>
              )}
              {hmepAndHmeeSigned && (
                <Button
                  variant="outlined"
                  color="primary"
                  startIcon={<PictureAsPdfIcon />}
                  onClick={exportPDFServer}
                  disabled={loading}
                  sx={{ fontWeight: 700, borderRadius: 2, px: 2.5, py: 1 }}
                >
                  PDF Officiel
                </Button>
              )}
            </Stack>
          </Paper>
        </Box>
      )}

      {/* SIGNATURE DIALOG MODAL */}
      <Dialog open={sigDialogOpen} onClose={() => setSigDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle component="div" sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', bgcolor: '#0E2A21', color: '#FFFFFF', py: 1.5 }}>
          <Typography component="span" variant="subtitle1" sx={{ fontWeight: 800 }}>
            Signature Numérique &mdash; {activeSigField}
          </Typography>
          <IconButton onClick={() => setSigDialogOpen(false)} sx={{ color: '#FFFFFF' }}>
            <CloseIcon />
          </IconButton>
        </DialogTitle>
        <DialogContent sx={{ p: 3 }}>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            Dessinez votre signature manuscrite à l'aide de votre souris ou écran tactile.
          </Typography>
          <Box sx={{ border: '2px dashed #1F4D3E', borderRadius: 2, overflow: 'hidden', bgcolor: '#F7FAF8' }}>
            <SignaturePad onSave={handleSaveSignature} />
          </Box>
        </DialogContent>
      </Dialog>

      {/* TIROIR ASSISTANT IA (CREWAI + RAG OCP) */}
      <AIAssistantDrawer
        open={aiDrawerOpen}
        onClose={() => setAiDrawerOpen(false)}
        atContext={{
          atId: initialData?.id || formData.numero,
          description: formData.description,
          installation: formData.lieu,
          statut: atStatut,
        }}
      />
    </Box>
  );
}