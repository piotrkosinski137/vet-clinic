import { useState, useEffect, useCallback } from 'react';
import { api, PatientResponse, PatientRequest } from '../api';

interface UsePatients {
  patients: PatientResponse[];
  loading: boolean;
  error: string | null;
  refresh: () => void;
  createPatient: (patient: PatientRequest) => Promise<PatientResponse>;
  deletePatient: (id: string) => Promise<void>;
}

export function usePatients(): UsePatients {
  const [patients, setPatients] = useState<PatientResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchPatients = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await api.getPatients();
      setPatients(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to fetch patients');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchPatients();
  }, [fetchPatients]);

  const createPatient = async (patient: PatientRequest): Promise<PatientResponse> => {
    const created = await api.createPatient(patient);
    setPatients((prev) => [...prev, created]);
    return created;
  };

  const deletePatient = async (id: string): Promise<void> => {
    await api.deletePatient(id);
    setPatients((prev) => prev.filter((p) => p.id !== id));
  };

  return {
    patients,
    loading,
    error,
    refresh: fetchPatients,
    createPatient,
    deletePatient,
  };
}
