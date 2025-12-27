import { useMemo } from 'react';
import { api, PatientResponse, PatientRequest } from '../api';
import { useCrud, UseCrudResult } from './useCrud';

/**
 * Hook for managing patients with CRUD operations.
 * Built on the generic useCrud hook for consistency.
 */
export interface UsePatients {
  patients: PatientResponse[];
  loading: boolean;
  error: string | null;
  refresh: () => void;
  createPatient: (patient: PatientRequest) => Promise<PatientResponse>;
  updatePatient: (id: string, patient: PatientRequest) => Promise<PatientResponse>;
  deletePatient: (id: string) => Promise<void>;
}

export function usePatients(): UsePatients {
  const config = useMemo(
    () => ({
      fetchAll: () => api.getPatients(),
      create: (data: PatientRequest) => api.createPatient(data),
      update: (id: string, data: PatientRequest) => api.updatePatient(id, data),
      remove: (id: string) => api.deletePatient(id),
      fetchErrorMessage: 'Failed to fetch patients',
    }),
    []
  );

  const crud: UseCrudResult<PatientResponse, PatientRequest> = useCrud(config);

  return {
    patients: crud.items,
    loading: crud.loading,
    error: crud.error,
    refresh: crud.refresh,
    createPatient: crud.create,
    updatePatient: crud.update,
    deletePatient: crud.remove,
  };
}
