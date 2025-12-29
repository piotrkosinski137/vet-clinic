import { useState, useCallback, useEffect, useRef } from 'react';
import { api, VisitResponse, WaitingRoomVisitResponse, CheckInRequest, VisitPriority } from '../api';

interface UseWaitingRoom {
  visits: WaitingRoomVisitResponse[];
  loading: boolean;
  error: string | null;
  fetchWaitingRoom: () => Promise<void>;
  checkIn: (visitId: string, request?: CheckInRequest) => Promise<VisitResponse>;
  startVisit: (visitId: string) => Promise<VisitResponse>;
  markNoShow: (visitId: string) => Promise<VisitResponse>;
  updateWaitingRoomInfo: (visitId: string, notes?: string, priority?: VisitPriority) => Promise<VisitResponse>;
  refresh: () => void;
}

const AUTO_REFRESH_INTERVAL_MS = 30000; // 30 seconds

export function useWaitingRoom(autoRefresh = true): UseWaitingRoom {
  const [visits, setVisits] = useState<WaitingRoomVisitResponse[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const intervalRef = useRef<NodeJS.Timeout | null>(null);

  const fetchWaitingRoom = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      // Use enriched endpoint with patient, client and financial data
      const data = await api.getEnrichedWaitingRoom();
      setVisits(data);
    } catch (err) {
      if (err instanceof Error) {
        setError(err.message);
      }
    } finally {
      setLoading(false);
    }
  }, []);

  const checkIn = useCallback(async (visitId: string, request?: CheckInRequest): Promise<VisitResponse> => {
    const updated = await api.checkIn(visitId, request);
    // Re-fetch to get enriched data
    await fetchWaitingRoom();
    return updated;
  }, [fetchWaitingRoom]);

  const startVisit = useCallback(async (visitId: string): Promise<VisitResponse> => {
    const updated = await api.startFromWaitingRoom(visitId);
    // Remove from waiting room list
    setVisits((prev) => prev.filter((v) => v.id !== visitId));
    return updated;
  }, []);

  const markNoShow = useCallback(async (visitId: string): Promise<VisitResponse> => {
    const updated = await api.markNoShow(visitId);
    // Remove from waiting room list
    setVisits((prev) => prev.filter((v) => v.id !== visitId));
    return updated;
  }, []);

  const updateWaitingRoomInfo = useCallback(
    async (visitId: string, notes?: string, priority?: VisitPriority): Promise<VisitResponse> => {
      const updated = await api.updateWaitingRoomInfo(visitId, {
        waitingRoomNotes: notes,
        priority,
      });
      // Re-fetch to get updated enriched data
      await fetchWaitingRoom();
      return updated;
    },
    [fetchWaitingRoom]
  );

  const refresh = useCallback(() => {
    fetchWaitingRoom();
  }, [fetchWaitingRoom]);

  // Initial fetch and auto-refresh setup
  useEffect(() => {
    fetchWaitingRoom();

    if (autoRefresh) {
      intervalRef.current = setInterval(() => {
        fetchWaitingRoom();
      }, AUTO_REFRESH_INTERVAL_MS);
    }

    return () => {
      if (intervalRef.current) {
        clearInterval(intervalRef.current);
      }
    };
  }, [fetchWaitingRoom, autoRefresh]);

  return {
    visits,
    loading,
    error,
    fetchWaitingRoom,
    checkIn,
    startVisit,
    markNoShow,
    updateWaitingRoomInfo,
    refresh,
  };
}
