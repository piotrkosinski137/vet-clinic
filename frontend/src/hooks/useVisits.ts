import { useState, useCallback, useRef } from 'react';
import { api, VisitResponse, VisitRequest, VisitFilters, VisitStatus, CheckInRequest } from '../api';

interface UseVisits {
  visits: VisitResponse[];
  loading: boolean;
  error: string | null;
  fetchVisits: (filters?: VisitFilters) => Promise<void>;
  fetchVisitsForDate: (date: string) => Promise<void>;
  fetchVisitsForVeterinarian: (vetId: string, date: string) => Promise<void>;
  fetchVisitsForVeterinarianRange: (vetId: string, dateFrom: string, dateTo: string) => Promise<void>;
  createVisit: (visit: VisitRequest) => Promise<VisitResponse>;
  updateVisit: (id: string, visit: VisitRequest) => Promise<VisitResponse>;
  updateVisitStatus: (id: string, status: VisitStatus) => Promise<VisitResponse>;
  reassignVisit: (
    id: string,
    veterinarianId: string | null,
    veterinarianName: string | null,
    visitDate: string
  ) => Promise<VisitResponse>;
  deleteVisit: (id: string) => Promise<void>;
  checkIn: (id: string, request?: CheckInRequest) => Promise<VisitResponse>;
  refresh: () => void;
}

export function useVisits(initialFilters?: VisitFilters): UseVisits {
  const [visits, setVisits] = useState<VisitResponse[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [currentFilters, setCurrentFilters] = useState<VisitFilters | undefined>(initialFilters);
  const abortControllerRef = useRef<AbortController | null>(null);

  const fetchVisits = useCallback(async (filters?: VisitFilters) => {
    // Abort any pending request
    if (abortControllerRef.current) {
      abortControllerRef.current.abort();
    }
    abortControllerRef.current = new AbortController();

    try {
      setLoading(true);
      setError(null);
      setCurrentFilters(filters);
      const data = await api.getVisits(filters);
      setVisits(data);
    } catch (err) {
      if (err instanceof Error && err.name !== 'AbortError') {
        setError(err.message);
      }
    } finally {
      setLoading(false);
    }
  }, []);

  const fetchVisitsForDate = useCallback(async (date: string) => {
    // Abort any pending request
    if (abortControllerRef.current) {
      abortControllerRef.current.abort();
    }
    abortControllerRef.current = new AbortController();

    try {
      setLoading(true);
      setError(null);
      const data = await api.getVisitsForDate(date);
      setVisits(data);
    } catch (err) {
      if (err instanceof Error && err.name !== 'AbortError') {
        setError(err.message);
      }
    } finally {
      setLoading(false);
    }
  }, []);

  const fetchVisitsForVeterinarian = useCallback(async (vetId: string, date: string) => {
    // Abort any pending request
    if (abortControllerRef.current) {
      abortControllerRef.current.abort();
    }
    abortControllerRef.current = new AbortController();

    try {
      setLoading(true);
      setError(null);
      const data = await api.getVisitsForVeterinarian(vetId, date);
      setVisits(data);
    } catch (err) {
      if (err instanceof Error && err.name !== 'AbortError') {
        setError(err.message);
      }
    } finally {
      setLoading(false);
    }
  }, []);

  const fetchVisitsForVeterinarianRange = useCallback(
    async (vetId: string, dateFrom: string, dateTo: string) => {
      // Abort any pending request
      if (abortControllerRef.current) {
        abortControllerRef.current.abort();
      }
      abortControllerRef.current = new AbortController();

      try {
        setLoading(true);
        setError(null);
        const data = await api.getVisitsForVeterinarianRange(vetId, dateFrom, dateTo);
        setVisits(data);
      } catch (err) {
        if (err instanceof Error && err.name !== 'AbortError') {
          setError(err.message);
        }
      } finally {
        setLoading(false);
      }
    },
    []
  );

  const createVisit = async (visit: VisitRequest): Promise<VisitResponse> => {
    const created = await api.createVisit(visit);
    setVisits((prev) => [...prev, created]);
    return created;
  };

  const updateVisit = async (id: string, visit: VisitRequest): Promise<VisitResponse> => {
    const updated = await api.updateVisit(id, visit);
    setVisits((prev) => prev.map((v) => (v.id === id ? updated : v)));
    return updated;
  };

  const updateVisitStatus = async (id: string, status: VisitStatus): Promise<VisitResponse> => {
    const updated = await api.updateVisitStatus(id, status);
    setVisits((prev) => prev.map((v) => (v.id === id ? updated : v)));
    return updated;
  };

  const reassignVisit = async (
    id: string,
    veterinarianId: string | null,
    veterinarianName: string | null,
    visitDate: string
  ): Promise<VisitResponse> => {
    const updated = await api.reassignVisit(id, veterinarianId, veterinarianName, visitDate);
    setVisits((prev) => prev.map((v) => (v.id === id ? updated : v)));
    return updated;
  };

  const deleteVisit = async (id: string): Promise<void> => {
    await api.deleteVisit(id);
    setVisits((prev) => prev.filter((v) => v.id !== id));
  };

  const checkIn = async (id: string, request?: CheckInRequest): Promise<VisitResponse> => {
    const updated = await api.checkIn(id, request);
    setVisits((prev) => prev.map((v) => (v.id === id ? updated : v)));
    return updated;
  };

  const refresh = useCallback(() => {
    fetchVisits(currentFilters);
  }, [fetchVisits, currentFilters]);

  return {
    visits,
    loading,
    error,
    fetchVisits,
    fetchVisitsForDate,
    fetchVisitsForVeterinarian,
    fetchVisitsForVeterinarianRange,
    createVisit,
    updateVisit,
    updateVisitStatus,
    reassignVisit,
    deleteVisit,
    checkIn,
    refresh,
  };
}
