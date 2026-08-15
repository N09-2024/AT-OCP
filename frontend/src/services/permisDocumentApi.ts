import { apiClient } from './apiClient';

export interface PermisDocumentResponse {
  id: string;
  atId: string;
  typePermisAttendu: string;
  fileOriginalName: string | null;
  fileContentType: string | null;
  statut: 'EN_ATTENTE_UPLOAD' | 'EN_ATTENTE_ANALYSE' | 'VALIDE' | 'REJETE';
  dateUpload: string | null;
  dateAnalyse: string | null;
  typeExtrait: string | null;
  dateDebutExtrait: string | null;
  dateFinExtrait: string | null;
  responsablesExtraits: string | null;
  motifRejet: string | null;
  scoreConfiance: number | null;
  commentaireIA: string | null;
}

/** Initialise / synchronise les PermisDocument avec les permis cochés en section E */
export const initialiserPermis = async (atId: string): Promise<PermisDocumentResponse[]> => {
  const { data } = await apiClient.post<PermisDocumentResponse[]>(
    `/permis-documents/at/${atId}/initialiser`
  );
  return data;
};

/** Récupère la liste des PermisDocument d une AT */
export const getPermisDocuments = async (atId: string): Promise<PermisDocumentResponse[]> => {
  const { data } = await apiClient.get<PermisDocumentResponse[]>(
    `/permis-documents/at/${atId}`
  );
  return data;
};

/** Upload un fichier de permis et déclenche l analyse IA */
export const uploadPermisDocument = async (
  atId: string,
  typePermis: string,
  file: File
): Promise<PermisDocumentResponse> => {
  const form = new FormData();
  form.append('typePermis', typePermis);
  form.append('file', file);
  const { data } = await apiClient.post<PermisDocumentResponse>(
    `/permis-documents/at/${atId}/upload`,
    form,
    { headers: { 'Content-Type': 'multipart/form-data' } }
  );
  return data;
};

/** Relance l analyse IA sur un document déjà uploadé */
export const relancerAnalyse = async (id: string): Promise<PermisDocumentResponse> => {
  const { data } = await apiClient.post<PermisDocumentResponse>(
    `/permis-documents/${id}/relancer-analyse`
  );
  return data;
};
