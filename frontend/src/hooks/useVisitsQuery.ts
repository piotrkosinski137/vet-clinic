/**
 * React Query version of useVisits hook.
 *
 * This demonstrates the migration pattern from custom hooks to React Query.
 * Benefits:
 * - Automatic caching and deduplication
 * - Background refetching (stale-while-revalidate)
 * - Built-in loading and error states
 * - Optimistic updates support
 * - DevTools integration
 *
 * Migration guide:
 * 1. Replace useState + useEffect with useQuery for fetching
 * 2. Replace mutation functions with useMutation
 * 3. Use queryClient.invalidateQueries for cache invalidation
 */

import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  api,
  VisitResponse,
  VisitRequest,
  VisitFilters,
  VisitStatus,
  CheckInRequest,
} from '../api';
import { queryKeys } from './queryKeys';

// ============================================
// QUERIES (Read operations)
// ============================================

/**
 * Fetch visits with optional filters
 */
export function useVisitsQuery(filters?: VisitFilters) {
  return useQuery({
    queryKey: queryKeys.visits.list(filters),
    queryFn: () => api.getVisits(filters),
  });
}

/**
 * Fetch visits for a specific date
 */
export function useVisitsForDateQuery(date: string, options?: { enabled?: boolean }) {
  return useQuery({
    queryKey: queryKeys.visits.forDate(date),
    queryFn: () => api.getVisitsForDate(date),
    enabled: options?.enabled ?? !!date,
  });
}

/**
 * Fetch visits for a specific veterinarian on a date
 */
export function useVisitsForVetQuery(
  vetId: string,
  date: string,
  options?: { enabled?: boolean }
) {
  return useQuery({
    queryKey: queryKeys.visits.forVet(vetId, date),
    queryFn: () => api.getVisitsForVeterinarian(vetId, date),
    enabled: options?.enabled ?? (!!vetId && !!date),
  });
}

/**
 * Fetch visits for a veterinarian in a date range
 */
export function useVisitsForVetRangeQuery(
  vetId: string,
  dateFrom: string,
  dateTo: string,
  options?: { enabled?: boolean }
) {
  return useQuery({
    queryKey: queryKeys.visits.forVetRange(vetId, dateFrom, dateTo),
    queryFn: () => api.getVisitsForVeterinarianRange(vetId, dateFrom, dateTo),
    enabled: options?.enabled ?? (!!vetId && !!dateFrom && !!dateTo),
  });
}

// ============================================
// MUTATIONS (Write operations)
// ============================================

/**
 * Create a new visit
 */
export function useCreateVisitMutation() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (visit: VisitRequest) => api.createVisit(visit),
    onSuccess: () => {
      // Invalidate all visit-related queries
      queryClient.invalidateQueries({ queryKey: queryKeys.visits.all() });
      queryClient.invalidateQueries({ queryKey: queryKeys.dashboard.stats() });
      queryClient.invalidateQueries({ queryKey: queryKeys.waitingRoom.all() });
    },
  });
}

/**
 * Update an existing visit
 */
export function useUpdateVisitMutation() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, visit }: { id: string; visit: VisitRequest }) =>
      api.updateVisit(id, visit),
    onSuccess: (updatedVisit) => {
      // Update the specific visit in cache
      queryClient.setQueryData(
        queryKeys.visits.detail(updatedVisit.id),
        updatedVisit
      );
      // Invalidate list queries
      queryClient.invalidateQueries({ queryKey: queryKeys.visits.lists() });
    },
  });
}

/**
 * Update visit status
 */
export function useUpdateVisitStatusMutation() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, status }: { id: string; status: VisitStatus }) =>
      api.updateVisitStatus(id, status),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.visits.all() });
      queryClient.invalidateQueries({ queryKey: queryKeys.waitingRoom.all() });
    },
  });
}

/**
 * Reassign visit to different veterinarian/date
 */
export function useReassignVisitMutation() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({
      id,
      veterinarianId,
      veterinarianName,
      visitDate,
    }: {
      id: string;
      veterinarianId: string | null;
      veterinarianName: string | null;
      visitDate: string;
    }) => api.reassignVisit(id, veterinarianId, veterinarianName, visitDate),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.visits.all() });
    },
  });
}

/**
 * Delete a visit
 */
export function useDeleteVisitMutation() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: string) => api.deleteVisit(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.visits.all() });
      queryClient.invalidateQueries({ queryKey: queryKeys.dashboard.stats() });
    },
  });
}

/**
 * Check in a visit
 */
export function useCheckInVisitMutation() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, request }: { id: string; request?: CheckInRequest }) =>
      api.checkIn(id, request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.visits.all() });
      queryClient.invalidateQueries({ queryKey: queryKeys.waitingRoom.all() });
    },
  });
}

// ============================================
// CONVENIENCE HOOK (combines queries + mutations)
// ============================================

/**
 * Combined hook that provides both query data and mutation functions.
 * This maintains backward compatibility with the existing useVisits interface.
 */
export function useVisitsWithQuery(filters?: VisitFilters) {
  const { data: visits = [], isLoading, error, refetch } = useVisitsQuery(filters);

  const createMutation = useCreateVisitMutation();
  const updateMutation = useUpdateVisitMutation();
  const updateStatusMutation = useUpdateVisitStatusMutation();
  const reassignMutation = useReassignVisitMutation();
  const deleteMutation = useDeleteVisitMutation();
  const checkInMutation = useCheckInVisitMutation();

  return {
    // Query state
    visits,
    loading: isLoading,
    error: error?.message ?? null,
    refresh: refetch,

    // Mutations (matching original interface)
    createVisit: async (visit: VisitRequest): Promise<VisitResponse> => {
      return createMutation.mutateAsync(visit);
    },
    updateVisit: async (id: string, visit: VisitRequest): Promise<VisitResponse> => {
      return updateMutation.mutateAsync({ id, visit });
    },
    updateVisitStatus: async (id: string, status: VisitStatus): Promise<VisitResponse> => {
      return updateStatusMutation.mutateAsync({ id, status });
    },
    reassignVisit: async (
      id: string,
      veterinarianId: string | null,
      veterinarianName: string | null,
      visitDate: string
    ): Promise<VisitResponse> => {
      return reassignMutation.mutateAsync({ id, veterinarianId, veterinarianName, visitDate });
    },
    deleteVisit: async (id: string): Promise<void> => {
      await deleteMutation.mutateAsync(id);
    },
    checkIn: async (id: string, request?: CheckInRequest): Promise<VisitResponse> => {
      return checkInMutation.mutateAsync({ id, request });
    },

    // Mutation states for UI feedback
    isCreating: createMutation.isPending,
    isUpdating: updateMutation.isPending,
    isDeleting: deleteMutation.isPending,
  };
}
