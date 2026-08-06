// Enums
export type StatutAT = 
  | 'BROUILLON'
  | 'SOUMISE'
  | 'VALIDEE'
  | 'REJETEE'
  | 'RENOUVELEE'
  | 'CLOTUREE'
  | 'ARCHIVEE'
  | 'ANNULEE'
  | 'CLASSIFICATION_EFFECTUEE'
  | 'DEMANDE_CREEE'
  | 'VISITE_REALISEE'
  | 'AT_REDIGEE'
  | 'INTERVENTION_EN_COURS'
  | 'AT_RECONDUITE'
  | 'FIN_TRAVAUX_DECLAREE'
  | 'TRAVAUX_RECEPTIONES';

export type StatutWorkflowAT = StatutAT;
export type RoleNom = 
  | 'ADMIN' 
  | 'CEEP' | 'CEEE' 
  | 'HCEP' | 'HCEE' 
  | 'HMEP' | 'HMEE' 
  | 'RESPONSABLE_ENTREPRISE';
export type PositionAT = 'PROPRIETAIRE' | 'EXECUTANT' | 'AUCUNE';
export type StatutVisa = 'EN_ATTENTE' | 'VALIDE' | 'REFUSE' | 'VALIDATION' | 'SIGNATURE' | 'REFUS';
export type StatutPermis = 'A_VERIFIER' | 'CONFORME' | 'NON_CONFORME' | 'EXPIRE' | 'INVALIDE';
export type StatutDocument = 'BROUILLON' | 'SOUMIS' | 'EN_COURS' | 'EN_ATTENTE_VISITE' | 'EN_ATTENTE_ANALYSE' | 'AT_GENEREE' | 'CLOS' | 'ANNULE';
export type EtatVerrou = 'LIBRE' | 'EN_COURS_EDITION' | 'TRANSFERE';
export type TypeDocument = 'DI' | 'OT' | 'BT';

// User & Role & Permission
export interface Permission {
  id: string;
  nom: string;
  description?: string;
}

export interface Role {
  id: string;
  nom: string;
  description?: string;
  permissions?: Permission[];
}

export interface Utilisateur {
  id: string;
  matricule: string;
  nom: string;
  prenom: string;
  email: string;
  telephone?: string;
  photo?: string;
  actif: boolean;
  compteVerrouille: boolean;
  motDePasseExpire: boolean;
  enAttenteValidation?: boolean;
  dateCreation: string;
  dateModification?: string;
  derniereConnexion?: string;
  service?: Service;
  roles: Role[];
}

// Referentiels
export interface Zone {
  id: string;
  codeZone: string;
  nomZone: string;
  descriptionZone?: string;
}

export interface Service {
  id: string;
  nomService: string;
  codeService: string;
  descriptionService?: string;
  zone?: Zone;
}

export interface Installation {
  id: string;
  nomInstallation: string;
  codeInstallation: string;
  descriptionInstallation?: string;
  zone?: Zone;
}

export interface Equipement {
  id: string;
  nomEquipement: string;
  codeEquipement: string;
  descriptionEquipement?: string;
  installation?: Installation;
}

export interface EntrepriseExterne {
  id: string;
  nomEntreprise: string;
  responsable?: string;
  telephone?: string;
  adresse?: string;
}

export interface Risque {
  id: string;
  nomRisque: string;
  descriptionRisque?: string;
  niveau?: string;
}

export interface MesurePreparation {
  id: string;
  nomMesure: string;
  descriptionMesure?: string;
}

export interface MoyenAcces {
  id: string;
  nomMoyen: string;
  descriptionMoyen?: string;
}

export interface EPI {
  id: string;
  nomEPI: string;
  descriptionEPI?: string;
}

export interface TypePermis {
  id: string;
  nom: string;
  description?: string;
}

// Source Documents
export interface DemandeIntervention {
  id: string;
  numero: string;
  objet: string;
  description?: string;
  statut: StatutDocument;
  dateCreation: string;
  equipement?: Equipement;
  installation?: Installation;
  service?: Service;
  visitePrealable?: VisitePrealable;
  analyseRisque?: AnalyseRisque;
}

export interface OrdreTravail {
  id: string;
  numero: string;
  objet: string;
  description?: string;
  statut: StatutDocument;
  dateCreation: string;
  equipement?: Equipement;
  installation?: Installation;
  visitePrealable?: VisitePrealable;
  analyseRisque?: AnalyseRisque;
}

export interface BonTravail {
  id: string;
  numero: string;
  objet: string;
  description?: string;
  statut: StatutDocument;
  dateCreation: string;
  equipement?: Equipement;
  installation?: Installation;
  visitePrealable?: VisitePrealable;
  analyseRisque?: AnalyseRisque;
}

// Visite Préalable & Photos
export interface PhotoVisite {
  id: string;
  urlPhoto: string;
  datePrise: string;
}

export interface VisitePrealable {
  id: string;
  latitude?: number;
  longitude?: number;
  commentaire?: string;
  dateVisite: string;
  visiteurNomComplet?: string;
  photos?: PhotoVisite[];
}

// Analyse de Risques
export interface AnalyseRisque {
  id: string;
  dateAnalyse: string;
  valide: boolean;
  risques?: Risque[];
  mesures?: MesurePreparation[];
  epis?: EPI[];
  moyensAcces?: MoyenAcces[];
}

// Permis & IA Analysis
export interface AnalyseIA {
  id: string;
  dateAnalyse: string;
  resultat: string;
  confiance: number;
  explications?: string;
  motsClesTrouves?: string[];
}

export interface Permis {
  id: string;
  typePermis: TypePermis;
  estObligatoire: boolean;
  statutVerification: StatutPermis;
  dateExpiration?: string;
  fichierUrl?: string;
  fichierNom?: string;
  analyseIA?: AnalyseIA;
}

// Visa
export interface Visa {
  id: string;
  dateVisa: string;
  dateSignature?: string;
  statut: StatutVisa;
  commentaire?: string;
  ordre?: number;
  signaturePresente: boolean;
  adresseIP?: string;
  utilisateurId?: string;
  utilisateurNomComplet?: string;
  autorisationTravailId?: string;
}

// Historique
export interface HistoriqueAT {
  id: string;
  dateAction: string;
  action: string;
  ancienStatut?: StatutAT;
  nouveauStatut?: StatutAT;
  commentaire?: string;
  utilisateurNomComplet?: string;
}

// Photo Réception & Réception des Travaux
export interface PhotoReception {
  id: string;
  urlPhoto: string;
  description?: string;
  datePhoto?: string;
}

export interface ReceptionTravaux {
  id: string;
  dateReception: string;
  dateReelleDebut?: string;
  dateReelleFin?: string;
  travauxConformes: boolean;
  zoneNettoyee: boolean;
  consignationRetiree: boolean;
  equipementRemisEnService: boolean;
  installationRemiseEnEtat: boolean;
  essaisEffectues: boolean;
  essaisConformes: boolean;
  travauxRealises?: string;
  commentaires?: string;
  validee: boolean;
  signaturePath?: string;
  signaturePresente?: boolean;
  photos?: PhotoReception[];
  autorisationTravailId?: string;
}

// Autorisation de Travail (AT)
export interface AutorisationTravail {
  id: string;
  numero: string;
  objet: string;
  descriptionTravaux?: string;
  dateDebut?: string;
  dateFin?: string;
  heureDebut?: string;
  heureFin?: string;
  statut: StatutAT;
  statutWorkflow?: StatutWorkflowAT;
  positionUtilisateurCourant?: PositionAT;
  version: number;
  etatVerrou: EtatVerrou;
  proprietaireBrouillon?: Utilisateur;
  typeDocumentSource?: TypeDocument;
  documentSourceId?: string;
  documentSourceNumero?: string;
  zoneProprietaire?: Zone;
  zoneExecutante?: Zone;
  servicesIntervenants?: string;
  entreprisesIntervenantes?: string;
  mesuresSecuriteExecutant?: string;
  risquesIds?: string[];
  mesuresIds?: string[];
  episIds?: string[];
  moyensAccesIds?: string[];
  permisIds?: string[];
  risques?: Risque[];
  mesures?: MesurePreparation[];
  epis?: EPI[];
  moyensAcces?: MoyenAcces[];
  permis?: Permis[];
  visas?: Visa[];
  receptionTravaux?: ReceptionTravaux;
  exportPdfAutorise?: boolean;
  exportPdfMotifsRefus?: string[];
  dateCreation: string;
  dateModification?: string;
}

// Archives & PDF
export interface Archive {
  id: string;
  numeroArchive: string;
  numeroAT: string;
  version: number;
  dateArchivage: string;
  hashSHA256: string;
  qrCodeUrl?: string;
  pdfUrl?: string;
  archiveStatus: string;
  createdBy?: string;
}

// Dashboard Data
export interface KpiStats {
  autorisationsEnCours: number;
  visasEnAttente: number;
  permisActifs: number;
  receptionsEnAttente: number;
  totalArchives: number;
}

export interface MonthlyStat {
  mois: string;
  count: number;
}

export interface AtSummary {
  id: string;
  titre: string;
  installation: string;
  statut: string;
  echeance: string;
}

export interface DashboardData {
  kpis: KpiStats;
  statusDistribution: Record<string, number>;
  monthlyStats: MonthlyStat[];
  recentAutorisations: AtSummary[];
}

// Notifications
export interface Notification {
  id: string;
  titre: string;
  message: string;
  type: string; // INFO, ACTION, SUCCESS, ERROR
  lien?: string;
  lue: boolean;
  dateCreation: string;
}
