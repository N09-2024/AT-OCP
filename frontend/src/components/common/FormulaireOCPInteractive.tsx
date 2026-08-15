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

import SignaturePad from './SignaturePad';
import PermisUploadCard from './PermisUploadCard';
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
  const canSignCeep = !readOnly && (signMode === 'all' || signMode === 'ceep');
  // canSignCeee est indépendant de readOnly : le CEEE signe après que les champs sont verrouillés
  const canSignCeee = signMode === 'ceee' || (!readOnly && signMode === 'all');

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

  // --- IA implicite (plus de bouton "Assistant IA") ---
  const [iaLoading, setIaLoading] = useState(false);
  const [iaRapport, setIaRapport] = useState<string | null>(null);
  const [iaAlertes, setIaAlertes] = useState<string[]>([]);
  const [iaSuggestions, setIaSuggestions] = useState<{
    risques: string[]; mesures: string[]; epis: string[]; permis: string[];
  }>({ risques: [], mesures: [], epis: [], permis: [] });

  // Permis Documents — validation IA
  const [permisDocuments, setPermisDocuments] = useState<PermisDocumentResponse[]>([]);
  const [permisUploading, setPermisUploading] = useState(false);
  const debounceInitRef = useRef<ReturnType<typeof setTimeout> | null>(null);

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
        site: initialData.zoneProprietaire?.nomZone || initialData.site || prev.site || '',
        entite: initialData.servicesIntervenants || prev.entite || '',
        documentSourceType: initialData.typeDocumentSource || prev.documentSourceType || 'DI',
        documentSourceId: initialData.documentSourceId || prev.documentSourceId || '',
        documentSourceNumero: initialData.documentSourceNumero || prev.documentSourceNumero || '',
        di: initialData.typeDocumentSource === 'DI' ? (initialData.documentSourceNumero ?? prev.di) : prev.di,
        ot: initialData.typeDocumentSource === 'OT' ? (initialData.documentSourceNumero ?? prev.ot) : prev.ot,
        bt: initialData.typeDocumentSource === 'BT' ? (initialData.documentSourceNumero ?? prev.bt) : prev.bt,
        lieu: initialData.zoneProprietaire?.nomZone || prev.lieu || '',
        servicesIntervenants: initialData.servicesIntervenants || prev.servicesIntervenants || '',
        serviceIntervenantId: initialData.serviceIntervenantId || prev.serviceIntervenantId || null,
        entreprisesIntervenantes: initialData.entreprisesIntervenantes || prev.entreprisesIntervenantes || '',
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
        lieu: found.equipement?.installation?.zone?.nomZone || found.installation?.zone?.nomZone || prev.lieu,
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
      // Si permisIds, déclencher l initialisation des PermisDocument (debounce 1s)
      if (field === 'permisIds' && initialData?.id) {
        if (debounceInitRef.current) clearTimeout(debounceInitRef.current);
        debounceInitRef.current = setTimeout(async () => {
          try {
            const docs = await initialiserPermis(initialData.id);
            setPermisDocuments(docs);
          } catch (e) {
            console.error('Initialisation permis IA échouée', e);
          }
        }, 1000);
      }
      return updated;
    });
  };

  // Toggle Remise en place checkbox
  const toggleRemiseEnPlace = (itemKey: string) => {
    if (readOnly) return;
    setFormData((prev) => {
      const list = prev.remiseEnPlace;
      const updated = list.includes(itemKey)
        ? { ...prev, remiseEnPlace: list.filter((i: string) => i !== itemKey) }
        : { ...prev, remiseEnPlace: [...list, itemKey] };
      onChange?.(updated);
      return updated;
    });
  };

  // ------------------------------------------------------------------
  // IA IMPLICITE — plus de bouton "Assistant IA". L'analyse se déclenche
  // automatiquement, en arrière-plan, dès que la description est assez
  // longue et n'a pas déjà été analysée. Découplée de l'autosave (800ms)
  // par un débounce plus long (2s) pour ne pas multiplier les appels au
  // microservice IA à chaque frappe.
  // ------------------------------------------------------------------
  const iaDebounceRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const lastAnalyzedDescRef = useRef<string>('');

  useEffect(() => {
    if (readOnly || fieldsLocked) return;
    const desc = (formData.description || '').trim();
    if (desc.length < 15) return; // pas assez de contexte pour être utile
    if (desc === lastAnalyzedDescRef.current) return;

    if (iaDebounceRef.current) clearTimeout(iaDebounceRef.current);
    iaDebounceRef.current = setTimeout(async () => {
      lastAnalyzedDescRef.current = desc;
      setIaLoading(true);
      try {
        const res = await iaApi.analyserIntervention(desc);
        const data = (res as any).data ?? res; // tolère les deux formats de retour du service
        setIaRapport(data.rapport || null);
        setIaAlertes(data.alertes || []);
        setIaSuggestions({
          risques: data.risques || [],
          mesures: data.mesures || [],
          epis: data.epis || [],
          permis: data.permis || [],
        });
      } catch {
        // IA best-effort : un échec ne doit jamais bloquer la saisie du formulaire.
      } finally {
        setIaLoading(false);
      }
    }, 2000);

    return () => {
      if (iaDebounceRef.current) clearTimeout(iaDebounceRef.current);
    };
  }, [formData.description, readOnly, fieldsLocked]);

  // Applique une suggestion IA à un référentiel de cases à cocher (l'utilisateur
  // choisit lui-même ce qu'il accepte — l'IA ne coche jamais à sa place).
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

  // Export PDF Serveur — seul export disponible. Le backend refuse (400)
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
    // §8.2 OCP S-HSE-SEC-31 — Visite Préalable Obligatoire
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

          {/* Indicateur IA discret — remplace le bouton "Assistant IA" */}
          {iaLoading && (
            <Chip
              label="Analyse IA…"
              size="small"
              icon={<CircularProgress size={12} color="inherit" />}
              sx={{ fontWeight: 700, bgcolor: 'rgba(124,58,237,0.2)', color: '#c4b5fd' }}
            />
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
            {onSubmitAT && signMode !== 'ceee' && (
              <Tooltip
                title={soumissionBloquee ? `${permisEnAttente} permis en attente de validation IA` : ''}
                arrow
              >
                <span>
                  <Button
                    variant="contained"
                    sx={{ bgcolor: '#00875A', '&:hover': { bgcolor: '#006c48' }, fontWeight: 700, borderRadius: 2 }}
                    startIcon={<CheckCircleIcon />}
                    onClick={handleSignerEtTransmettre}
                    disabled={loading || soumissionBloquee}
                    size="small"
                  >
                    Signer & Transmettre
                  </Button>
                </span>
              </Tooltip>
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

      {/* IA SUGGESTION BANNER — implicite, avec suggestions cliquables */}
      {(iaRapport || iaAlertes.length > 0) && (
        <Alert
          icon={<AutoAwesomeIcon />}
          severity={iaAlertes.length ? 'warning' : 'info'}
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
            <ul style={{ margin: '4px 0 8px 16px', padding: 0 }}>
              {iaAlertes.map((a, i) => (
                <li key={i}>{a}</li>
              ))}
            </ul>
          )}
          {!fieldsLocked && (iaSuggestions.risques.length + iaSuggestions.mesures.length + iaSuggestions.epis.length + iaSuggestions.permis.length > 0) && (
            <Box sx={{ mt: 1 }}>
              <Typography variant="caption" sx={{ fontWeight: 700 }}>
                Suggestions (cliquez pour cocher) :
              </Typography>
              <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5, mt: 0.5 }}>
                {iaSuggestions.risques
                  .filter((label) => !refRisques.some((r) => (r.nom || r.nomRisque || '').toLowerCase() === label.toLowerCase() && isChecked('risquesIds', r.id)))
                  .map((label) => (
                    <Chip key={`r-${label}`} label={label} size="small" variant="outlined" color="error"
                      onClick={() => appliquerSuggestion('risquesIds', refRisques, label)} />
                  ))}
                {iaSuggestions.mesures
                  .filter((label) => !refMesures.some((m) => (m.nom || m.nomMesure || '').toLowerCase() === label.toLowerCase() && isChecked('mesuresIds', m.id)))
                  .map((label) => (
                    <Chip key={`m-${label}`} label={label} size="small" variant="outlined" color="success"
                      onClick={() => appliquerSuggestion('mesuresIds', refMesures, label)} />
                  ))}
                {iaSuggestions.epis
                  .filter((label) => !refEpis.some((e) => (e.nom || e.nomEPI || '').toLowerCase() === label.toLowerCase() && isChecked('episIds', e.id)))
                  .map((label) => (
                    <Chip key={`e-${label}`} label={label} size="small" variant="outlined" color="secondary"
                      onClick={() => appliquerSuggestion('episIds', refEpis, label)} />
                  ))}
                {iaSuggestions.permis
                  .filter((label) => !refPermis.some((p) => (p.nomType || p.nom || '').toLowerCase() === label.toLowerCase() && isChecked('permisIds', p.id)))
                  .map((label) => (
                    <Chip key={`p-${label}`} label={label} size="small" variant="outlined" color="warning"
                      onClick={() => appliquerSuggestion('permisIds', refPermis, label)} />
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

      {/* SECTION A: NATURE DE L'AT & SERVICES */}
      <Card sx={{ mb: 3, borderRadius: 3, border: '1px solid #e2e8f0', boxShadow: '0 4px 12px rgba(0,0,0,0.03)' }}>
        <CardHeader
          avatar={<AssignmentIcon sx={{ color: '#00875A' }} />}
          title={<Typography variant="h6" sx={{ fontWeight: 800, color: '#1e293b' }}>Section A &bull; Nature de l'AT & Affectation des Services</Typography>}
          subheader="Identification du type d'AT et définition du service demandeur vs service exécutant"
          sx={{ bgcolor: '#f8fafc', borderBottom: '1px solid #e2e8f0', py: 1.5 }}
        />
        <CardContent sx={{ p: 3 }}>
          <Grid container spacing={2.5}>
            {/* Nature / Type de l'AT (DI, OT, BT) */}
            <Grid size={{ xs: 12, md: 6 }}>
              <Typography variant="caption" sx={{ fontWeight: 700, color: '#475569', mb: 1, display: 'block' }}>
                Nature de l'Autorisation de Travail :
              </Typography>
              <Stack direction="row" spacing={1.5}>
                {[
                  { key: 'DI', label: "Demande d'Intervention (DI)" },
                  { key: 'OT', label: "Ordre de Travail (OT)" },
                  { key: 'BT', label: "Bon de Travail (BT)" },
                ].map((item) => {
                  const selected = docSourceType === item.key;
                  return (
                    <Paper
                      key={item.key}
                      elevation={0}
                      onClick={() => {
                        if (fieldsLocked) return;
                        setDocSourceType(item.key as any);
                        updateTextValue('documentSourceType', item.key);
                      }}
                      sx={{
                        flex: 1,
                        p: 1.5,
                        textAlign: 'center',
                        borderRadius: 2,
                        cursor: fieldsLocked ? 'default' : 'pointer',
                        border: selected ? '2px solid #00875A' : '1px solid #cbd5e1',
                        bgcolor: selected ? '#f0fdf4' : '#ffffff',
                        transition: 'all 0.15s ease-in-out',
                        '&:hover': fieldsLocked ? {} : { borderColor: '#00875A', bgcolor: '#f0fdf4' },
                      }}
                    >
                      <Typography variant="subtitle2" sx={{ fontWeight: 800, color: selected ? '#00875A' : '#475569' }}>
                        {item.key}
                      </Typography>
                      <Typography variant="caption" sx={{ color: selected ? '#065f46' : '#64748b', fontSize: 11, display: 'block' }}>
                        {item.label}
                      </Typography>
                    </Paper>
                  );
                })}
              </Stack>
            </Grid>

            {/* Site / Zone Propriétaire */}
            <Grid size={{ xs: 12, sm: 6, md: 3 }}>
              <Typography variant="caption" sx={{ fontWeight: 700, color: '#475569', mb: 0.5, display: 'block' }}>
                Site / Zone Propriétaire (P)
              </Typography>
              <FormControl fullWidth size="small" disabled={readOnly}>
                <Select
                  value={formData.site}
                  displayEmpty
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

            {/* Service Demandeur (P) */}
            <Grid size={{ xs: 12, sm: 6, md: 3 }}>
              <Typography variant="caption" sx={{ fontWeight: 700, color: '#475569', mb: 0.5, display: 'block' }}>
                Service Demandeur / Propriétaire (P)
              </Typography>
              <FormControl fullWidth size="small" disabled={readOnly}>
                <Select
                  value={formData.entite}
                  displayEmpty
                  onChange={(e) => updateTextValue('entite', e.target.value)}
                >
                  <MenuItem value=""><em>Sélectionner service...</em></MenuItem>
                  {servicesList.map((s) => (
                    <MenuItem key={s.id} value={s.nomService}>
                      {s.nomService}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>

            <Grid size={12}>
              <Divider sx={{ my: 0.5 }} />
            </Grid>

            {/* Service Intervenant (CEEE) */}
            <Grid size={{ xs: 12, sm: 6 }}>
              <Typography variant="caption" sx={{ fontWeight: 700, color: '#475569', mb: 0.5, display: 'block' }}>
                Service Intervenant / Exécutant (CEEE) *
              </Typography>
              <FormControl fullWidth size="small" disabled={fieldsLocked}>
                <Select
                  value={formData.serviceIntervenantId || formData.servicesIntervenants}
                  displayEmpty
                  onChange={(e) => handleSelectServiceIntervenant(e.target.value)}
                >
                  <MenuItem value=""><em>-- Sélectionner le service exécutant (différent de P) --</em></MenuItem>
                  {servicesList.map((s) => (
                    <MenuItem key={s.id} value={s.id}>
                      {s.nomService}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>

            {/* Entreprises Intervenantes Sous-traitantes */}
            <Grid size={{ xs: 12, sm: 6 }}>
              <Typography variant="caption" sx={{ fontWeight: 700, color: '#475569', mb: 0.5, display: 'block' }}>
                Entreprise Extérieure (Tiers / Sous-traitant)
              </Typography>
              <FormControl fullWidth size="small" disabled={fieldsLocked}>
                <Select
                  value={formData.entreprisesIntervenantes}
                  displayEmpty
                  onChange={(e) => updateTextValue('entreprisesIntervenantes', e.target.value)}
                >
                  <MenuItem value=""><em>Aucune (Régie interne OCP)</em></MenuItem>
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
                helperText={!fieldsLocked ? "L'IA analyse automatiquement cette description pour suggérer risques, mesures, EPI et permis." : undefined}
              />
            </Grid>
          </Grid>
        </CardContent>
      </Card>

      {/* VISITE PRÉALABLE CONJOINTE DU CHANTIER (§8.2 STANDARD OCP) */}
      <Card sx={{ mb: 3, borderRadius: 3, border: '2px solid #3b82f6', boxShadow: '0 4px 16px rgba(59,130,246,0.08)' }}>
        <CardHeader
          avatar={<BuildIcon sx={{ color: '#2563eb' }} />}
          title={
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
              <Typography variant="h6" sx={{ fontWeight: 800, color: '#1e40af' }}>
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
          sx={{ bgcolor: '#eff6ff', borderBottom: '1px solid #bfdbfe', py: 1.5 }}
        />
        <CardContent sx={{ p: 3 }}>
          <Grid container spacing={2.5}>
            {/* Relevé GPS */}
            <Grid size={{ xs: 12, md: 6 }}>
              <Paper variant="outlined" sx={{ p: 2, textAlign: 'center', bgcolor: '#f8fafc', borderRadius: 2 }}>
                <Typography variant="subtitle2" sx={{ fontWeight: 800, mb: 1, color: '#1e293b' }}>
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
              <Paper variant="outlined" sx={{ p: 2, textAlign: 'center', bgcolor: '#f8fafc', borderRadius: 2 }}>
                <Typography variant="subtitle2" sx={{ fontWeight: 800, mb: 1, color: '#1e293b' }}>
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
                  bgcolor: preventionEnPlace ? '#f0fdf4' : '#fff1f2',
                  border: `2px solid ${preventionEnPlace ? '#16a34a' : '#e11d48'}`,
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
                    <Typography variant="subtitle2" sx={{ fontWeight: 900, color: preventionEnPlace ? '#15803d' : '#be123c' }}>
                      Point de contrôle obligatoire (§8.2) : « Les conditions de sécurité et les mesures de prévention ont été inspectées conjointement et sont effectivement mises en place sur le chantier »
                    </Typography>
                  }
                />
                {!preventionEnPlace && (
                  <Typography variant="caption" sx={{ color: '#be123c', display: 'block', mt: 0.5, ml: 4, fontStyle: 'italic' }}>
                    ⚠️ La validation de cette visite préalable est obligatoire avant la soumission et signature de l'AT.
                  </Typography>
                )}
              </Paper>
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
                          {ma.nomMoyen || ma.nom || ma.libelle || ma.descriptionMoyen}
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
                          {e.nomEPI || e.nomepi || e.nomEpi || e.nom || e.libelle || e.descriptionEPI}
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

          {/* Zone d upload et validation IA — visible si permis cochés et mode édition */}
          {!fieldsLocked && permisDocuments.length > 0 && (
            <Box sx={{ mt: 3 }}>
              <Divider sx={{ mb: 2 }} />
              <Typography variant="subtitle2" sx={{ fontWeight: 700, color: '#b45309', mb: 1.5 }}>
                📋 Documents de permis — Validation IA requise avant soumission
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
                  ⚠ {permisEnAttente} permis en attente de validation — soumission bloquée jusqu'à validation complète
                </Alert>
              )}
            </Box>
          )}
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
                    variant={canSignCeee ? 'contained' : 'outlined'}
                    size="small"
                    color="success"
                    startIcon={<DrawIcon />}
                    disabled={!canSignCeee}
                    onClick={() => handleOpenSignature('g1VisaCeee')}
                    sx={{
                      justifyContent: 'flex-start',
                      textTransform: 'none',
                      fontWeight: canSignCeee ? 800 : 500,
                      boxShadow: canSignCeee ? '0 0 0 2px #16a34a40' : 'none',
                    }}
                  >
                    {formData.g1VisaCeee
                      ? '✅ Visa CEEE signé (cliquer pour modifier)'
                      : '✍️ Signer le Visa CEEE'}
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

      {/* BARRE D'ACTIONS INFERIEURE (BOTTOM ACTION BAR) */}
      {(!readOnly || signMode === 'ceee') && (
        <Paper
          elevation={4}
          sx={{
            p: 2.5,
            mb: 3,
            borderRadius: 3,
            bgcolor: '#ffffff',
            border: '2px solid #00875A',
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            flexWrap: 'wrap',
            gap: 2,
          }}
        >
          <Box>
            <Typography variant="subtitle1" sx={{ fontWeight: 800, color: '#00875A' }}>
              Validation & Transmission du Formulaire F-HSE-SEC-31-04
            </Typography>
            <Typography variant="caption" color="text.secondary">
              Conforme au Standard OCP S-HSE-SEC-31. Assurez-vous que les informations et signatures sont saisies.
            </Typography>
          </Box>
          <Stack direction="row" spacing={2}>
            {onSave && (
              <Button
                variant="outlined"
                color="primary"
                onClick={() => onSave(formData)}
                disabled={loading}
                sx={{ fontWeight: 700, borderRadius: 2, px: 3, py: 1 }}
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
                sx={{ bgcolor: '#00875A', '&:hover': { bgcolor: '#006c48' }, fontWeight: 800, borderRadius: 2, px: 4, py: 1 }}
              >
                {loading ? <CircularProgress size={24} color="inherit" /> : 'Signer & Transmettre au CEEE'}
              </Button>
            )}
          </Stack>
        </Paper>
      )}

      {/* SIGNATURE DIALOG MODAL */}
      <Dialog open={sigDialogOpen} onClose={() => setSigDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle component="div" sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', bgcolor: '#0f172a', color: '#fff', py: 1.5 }}>
          <Typography component="span" variant="subtitle1" sx={{ fontWeight: 800 }}>
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