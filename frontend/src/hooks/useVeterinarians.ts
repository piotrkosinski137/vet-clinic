import { useState, useEffect, useCallback, useRef } from 'react';
import { api } from '../api/client';
import type {
  VeterinarianResponse,
  VeterinarianRequest,
  VeterinarianFilters,
} from '../api/types';

interface UseVeterinariansResult {
  veterinarians: VeterinarianResponse[];
  loading: boolean;
  error: string | null;
  refetch: () => Promise<void>;
  createVeterinarian: (veterinarian: VeterinarianRequest) => Promise<VeterinarianResponse>;
  updateVeterinarian: (id: string, veterinarian: VeterinarianRequest) => Promise<VeterinarianResponse>;
  deleteVeterinarian: (id: string) => Promise<void>;
  toggleActive: (id: string, active: boolean) => Promise<VeterinarianResponse>;
}

export function useVeterinarians(filters?: VeterinarianFilters): UseVeterinariansResult {
  const [veterinarians, setVeterinarians] = useState<VeterinarianResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Store filters in ref to avoid dependency issues with objects
  const filtersRef = useRef(filters);
  filtersRef.current = filters;

  // Track if initial fetch has been done
  const hasFetched = useRef(false);

  // AbortController for request cancellation
  const abortControllerRef = useRef<AbortController | null>(null);

  const fetchVeterinarians = useCallback(async () => {
    // Abort any pending request
    if (abortControllerRef.current) {
      abortControllerRef.current.abort();
    }
    abortControllerRef.current = new AbortController();

    setLoading(true);
    setError(null);
    try {
      const data = await api.getVeterinarians(filtersRef.current);
      setVeterinarians(data);
    } catch (err) {
      if (err instanceof Error && err.name !== 'AbortError') {
        setError(err.message);
      }
    } finally {
      setLoading(false);
    }
  }, []);

  // Fetch only once on mount
  useEffect(() => {
    if (hasFetched.current) return;
    hasFetched.current = true;
    fetchVeterinarians();

    // Cleanup function to abort on unmount
    return () => {
      if (abortControllerRef.current) {
        abortControllerRef.current.abort();
      }
    };
  }, [fetchVeterinarians]);

  const createVeterinarian = useCallback(
    async (veterinarian: VeterinarianRequest): Promise<VeterinarianResponse> => {
      const created = await api.createVeterinarian(veterinarian);
      setVeterinarians((prev) => [...prev, created]);
      return created;
    },
    []
  );

  const updateVeterinarian = useCallback(
    async (id: string, veterinarian: VeterinarianRequest): Promise<VeterinarianResponse> => {
      const updated = await api.updateVeterinarian(id, veterinarian);
      setVeterinarians((prev) => prev.map((v) => (v.id === id ? updated : v)));
      return updated;
    },
    []
  );

  const deleteVeterinarian = useCallback(async (id: string): Promise<void> => {
    await api.deleteVeterinarian(id);
    setVeterinarians((prev) => prev.filter((v) => v.id !== id));
  }, []);

  const toggleActive = useCallback(
    async (id: string, active: boolean): Promise<VeterinarianResponse> => {
      const updated = await api.toggleVeterinarianActive(id, active);
      setVeterinarians((prev) => prev.map((v) => (v.id === id ? updated : v)));
      return updated;
    },
    []
  );

  return {
    veterinarians,
    loading,
    error,
    refetch: fetchVeterinarians,
    createVeterinarian,
    updateVeterinarian,
    deleteVeterinarian,
    toggleActive,
  };
}
