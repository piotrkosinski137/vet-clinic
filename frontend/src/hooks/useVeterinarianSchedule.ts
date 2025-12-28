import { useState, useEffect, useCallback, useRef } from 'react';
import { api } from '../api/client';
import type {
  VeterinarianScheduleResponse,
  VeterinarianDayOffResponse,
  VeterinarianDayOffRequest,
  WeeklyScheduleRequest,
  VeterinarianAvailabilityResponse,
} from '../api/types';

interface UseVeterinarianScheduleResult {
  schedule: VeterinarianScheduleResponse[];
  daysOff: VeterinarianDayOffResponse[];
  loading: boolean;
  error: string | null;
  refetch: () => Promise<void>;
  updateSchedule: (schedule: WeeklyScheduleRequest) => Promise<VeterinarianScheduleResponse[]>;
  addDayOff: (dayOff: VeterinarianDayOffRequest) => Promise<VeterinarianDayOffResponse>;
  updateDayOff: (dayOffId: string, dayOff: VeterinarianDayOffRequest) => Promise<VeterinarianDayOffResponse>;
  deleteDayOff: (dayOffId: string) => Promise<void>;
  approveDayOff: (dayOffId: string, approved: boolean) => Promise<VeterinarianDayOffResponse>;
}

export function useVeterinarianSchedule(veterinarianId: string | null): UseVeterinarianScheduleResult {
  const [schedule, setSchedule] = useState<VeterinarianScheduleResponse[]>([]);
  const [daysOff, setDaysOff] = useState<VeterinarianDayOffResponse[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const abortControllerRef = useRef<AbortController | null>(null);
  const previousVetIdRef = useRef<string | null>(null);

  const fetchData = useCallback(async () => {
    if (!veterinarianId) {
      setSchedule([]);
      setDaysOff([]);
      return;
    }

    // Abort any pending request
    if (abortControllerRef.current) {
      abortControllerRef.current.abort();
    }
    abortControllerRef.current = new AbortController();

    setLoading(true);
    setError(null);

    try {
      const [scheduleData, daysOffData] = await Promise.all([
        api.getVeterinarianSchedule(veterinarianId),
        api.getVeterinarianDaysOff(veterinarianId),
      ]);
      setSchedule(scheduleData);
      setDaysOff(daysOffData);
    } catch (err) {
      if (err instanceof Error && err.name !== 'AbortError') {
        setError(err.message);
      }
    } finally {
      setLoading(false);
    }
  }, [veterinarianId]);

  // Fetch when veterinarianId changes
  useEffect(() => {
    if (veterinarianId !== previousVetIdRef.current) {
      previousVetIdRef.current = veterinarianId;
      fetchData();
    }

    return () => {
      if (abortControllerRef.current) {
        abortControllerRef.current.abort();
      }
    };
  }, [veterinarianId, fetchData]);

  const updateSchedule = useCallback(
    async (scheduleRequest: WeeklyScheduleRequest): Promise<VeterinarianScheduleResponse[]> => {
      if (!veterinarianId) throw new Error('No veterinarian selected');
      const updated = await api.updateVeterinarianSchedule(veterinarianId, scheduleRequest);
      setSchedule(updated);
      return updated;
    },
    [veterinarianId]
  );

  const addDayOff = useCallback(
    async (dayOff: VeterinarianDayOffRequest): Promise<VeterinarianDayOffResponse> => {
      if (!veterinarianId) throw new Error('No veterinarian selected');
      const created = await api.addVeterinarianDayOff(veterinarianId, dayOff);
      setDaysOff((prev) => [...prev, created]);
      return created;
    },
    [veterinarianId]
  );

  const updateDayOff = useCallback(
    async (dayOffId: string, dayOff: VeterinarianDayOffRequest): Promise<VeterinarianDayOffResponse> => {
      if (!veterinarianId) throw new Error('No veterinarian selected');
      const updated = await api.updateVeterinarianDayOff(veterinarianId, dayOffId, dayOff);
      setDaysOff((prev) => prev.map((d) => (d.id === dayOffId ? updated : d)));
      return updated;
    },
    [veterinarianId]
  );

  const deleteDayOff = useCallback(
    async (dayOffId: string): Promise<void> => {
      if (!veterinarianId) throw new Error('No veterinarian selected');
      await api.deleteVeterinarianDayOff(veterinarianId, dayOffId);
      setDaysOff((prev) => prev.filter((d) => d.id !== dayOffId));
    },
    [veterinarianId]
  );

  const approveDayOff = useCallback(
    async (dayOffId: string, approved: boolean): Promise<VeterinarianDayOffResponse> => {
      if (!veterinarianId) throw new Error('No veterinarian selected');
      const updated = await api.approveVeterinarianDayOff(veterinarianId, dayOffId, approved);
      setDaysOff((prev) => prev.map((d) => (d.id === dayOffId ? updated : d)));
      return updated;
    },
    [veterinarianId]
  );

  return {
    schedule,
    daysOff,
    loading,
    error,
    refetch: fetchData,
    updateSchedule,
    addDayOff,
    updateDayOff,
    deleteDayOff,
    approveDayOff,
  };
}

// Hook for fetching availability for all veterinarians on a specific date
interface UseVeterinariansAvailabilityResult {
  availability: VeterinarianAvailabilityResponse[];
  loading: boolean;
  error: string | null;
  refetch: () => Promise<void>;
}

export function useVeterinariansAvailability(date: string | null): UseVeterinariansAvailabilityResult {
  const [availability, setAvailability] = useState<VeterinarianAvailabilityResponse[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const abortControllerRef = useRef<AbortController | null>(null);
  const previousDateRef = useRef<string | null>(null);

  const fetchData = useCallback(async () => {
    if (!date) {
      setAvailability([]);
      return;
    }

    // Abort any pending request
    if (abortControllerRef.current) {
      abortControllerRef.current.abort();
    }
    abortControllerRef.current = new AbortController();

    setLoading(true);
    setError(null);

    try {
      const data = await api.getAllVeterinariansAvailability(date);
      setAvailability(data);
    } catch (err) {
      if (err instanceof Error && err.name !== 'AbortError') {
        setError(err.message);
      }
    } finally {
      setLoading(false);
    }
  }, [date]);

  // Fetch when date changes
  useEffect(() => {
    if (date !== previousDateRef.current) {
      previousDateRef.current = date;
      fetchData();
    }

    return () => {
      if (abortControllerRef.current) {
        abortControllerRef.current.abort();
      }
    };
  }, [date, fetchData]);

  return {
    availability,
    loading,
    error,
    refetch: fetchData,
  };
}
