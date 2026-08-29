import { apiClient } from './apiClient';
import type {
  Risque,
  MesurePreparation,
  MoyenAcces,
  EPI,
  TypePermis,
  Zone,
  Equipement,
  EntrepriseExterne,
  Service
} from '../types';

export const referentielApi = {
  getRisques: async (): Promise<Risque[]> => {
    const res = await apiClient.get('/risques');
    return Array.isArray(res.data) ? res.data : (res.data?.content || []);
  },

  getMesuresPreparation: async (): Promise<MesurePreparation[]> => {
    const res = await apiClient.get('/mesures-preparation');
    return Array.isArray(res.data) ? res.data : (res.data?.content || []);
  },

  getEpis: async (): Promise<EPI[]> => {
    const res = await apiClient.get('/epis');
    return Array.isArray(res.data) ? res.data : (res.data?.content || []);
  },

  getMoyensAcces: async (): Promise<MoyenAcces[]> => {
    const res = await apiClient.get('/moyens-acces');
    return Array.isArray(res.data) ? res.data : (res.data?.content || []);
  },

  getTypesPermis: async (): Promise<TypePermis[]> => {
    const res = await apiClient.get('/types-permis');
    return Array.isArray(res.data) ? res.data : (res.data?.content || []);
  },

  getZones: async (): Promise<Zone[]> => {
    const res = await apiClient.get('/zones');
    return Array.isArray(res.data) ? res.data : (res.data?.content || []);
  },

  getEquipements: async (): Promise<Equipement[]> => {
    const res = await apiClient.get('/equipements');
    return Array.isArray(res.data) ? res.data : (res.data?.content || []);
  },

  getEntreprises: async (): Promise<EntrepriseExterne[]> => {
    const res = await apiClient.get('/entreprises-externes');
    return Array.isArray(res.data) ? res.data : (res.data?.content || []);
  },

  getServices: async (): Promise<Service[]> => {
    const res = await apiClient.get('/services');
    return Array.isArray(res.data) ? res.data : (res.data?.content || []);
  },
};
