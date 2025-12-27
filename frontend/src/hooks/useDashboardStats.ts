import { useState, useCallback, useEffect } from 'react';
import { api } from '../api/client';
import type { DashboardStatsResponse } from '../api/types';

export interface UseDashboardStatsResult {
  stats: DashboardStatsResponse | null;
  loading: boolean;
  error: string | null;
  refresh: () => Promise<void>;
}

const DEFAULT_STATS: DashboardStatsResponse = {
  totalPatients: 0,
  visitsToday: 0,
  pendingInvoices: 0,
  lowStockItems: 0,
  totalClients: 0,
  completedVisitsToday: 0,
  scheduledVisitsToday: 0,
};

export function useDashboardStats(): UseDashboardStatsResult {
  const [stats, setStats] = useState<DashboardStatsResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchStats = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await api.getDashboardStats();
      setStats(data);
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Failed to load dashboard stats';
      setError(message);
      setStats(DEFAULT_STATS);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchStats();
  }, [fetchStats]);

  return { stats, loading, error, refresh: fetchStats };
}
