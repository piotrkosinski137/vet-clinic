/**
 * React Query key factory for consistent cache management.
 *
 * Usage:
 * - queryKeys.visits.all() -> ['visits']
 * - queryKeys.visits.list(filters) -> ['visits', 'list', filters]
 * - queryKeys.visits.detail(id) -> ['visits', 'detail', id]
 *
 * Benefits:
 * - Type-safe query keys
 * - Easy cache invalidation: queryClient.invalidateQueries({ queryKey: queryKeys.visits.all() })
 * - Hierarchical invalidation: invalidating 'visits' invalidates all visit-related queries
 */

export const queryKeys = {
  // Visits
  visits: {
    all: () => ['visits'] as const,
    lists: () => [...queryKeys.visits.all(), 'list'] as const,
    list: (filters?: Record<string, unknown>) =>
      [...queryKeys.visits.lists(), filters] as const,
    details: () => [...queryKeys.visits.all(), 'detail'] as const,
    detail: (id: string) => [...queryKeys.visits.details(), id] as const,
    forDate: (date: string) => [...queryKeys.visits.all(), 'date', date] as const,
    forVet: (vetId: string, date: string) =>
      [...queryKeys.visits.all(), 'vet', vetId, date] as const,
    forVetRange: (vetId: string, dateFrom: string, dateTo: string) =>
      [...queryKeys.visits.all(), 'vet-range', vetId, dateFrom, dateTo] as const,
  },

  // Patients
  patients: {
    all: () => ['patients'] as const,
    lists: () => [...queryKeys.patients.all(), 'list'] as const,
    list: (filters?: Record<string, unknown>) =>
      [...queryKeys.patients.lists(), filters] as const,
    details: () => [...queryKeys.patients.all(), 'detail'] as const,
    detail: (id: string) => [...queryKeys.patients.details(), id] as const,
    forClient: (clientId: string) =>
      [...queryKeys.patients.all(), 'client', clientId] as const,
  },

  // Clients
  clients: {
    all: () => ['clients'] as const,
    lists: () => [...queryKeys.clients.all(), 'list'] as const,
    list: (filters?: Record<string, unknown>) =>
      [...queryKeys.clients.lists(), filters] as const,
    details: () => [...queryKeys.clients.all(), 'detail'] as const,
    detail: (id: string) => [...queryKeys.clients.details(), id] as const,
  },

  // Veterinarians
  veterinarians: {
    all: () => ['veterinarians'] as const,
    lists: () => [...queryKeys.veterinarians.all(), 'list'] as const,
    list: (filters?: Record<string, unknown>) =>
      [...queryKeys.veterinarians.lists(), filters] as const,
    details: () => [...queryKeys.veterinarians.all(), 'detail'] as const,
    detail: (id: string) => [...queryKeys.veterinarians.details(), id] as const,
    schedule: (vetId: string) =>
      [...queryKeys.veterinarians.all(), 'schedule', vetId] as const,
  },

  // Audit logs
  auditLogs: {
    all: () => ['auditLogs'] as const,
    lists: () => [...queryKeys.auditLogs.all(), 'list'] as const,
    list: (filters?: Record<string, unknown>) =>
      [...queryKeys.auditLogs.lists(), filters] as const,
  },

  // Dashboard
  dashboard: {
    all: () => ['dashboard'] as const,
    stats: () => [...queryKeys.dashboard.all(), 'stats'] as const,
  },

  // Price list
  priceList: {
    all: () => ['priceList'] as const,
    lists: () => [...queryKeys.priceList.all(), 'list'] as const,
    categories: () => [...queryKeys.priceList.all(), 'categories'] as const,
  },

  // Inventory
  inventory: {
    all: () => ['inventory'] as const,
    lists: () => [...queryKeys.inventory.all(), 'list'] as const,
    detail: (id: string) => [...queryKeys.inventory.all(), 'detail', id] as const,
    lowStock: () => [...queryKeys.inventory.all(), 'lowStock'] as const,
  },

  // Waiting room
  waitingRoom: {
    all: () => ['waitingRoom'] as const,
    list: () => [...queryKeys.waitingRoom.all(), 'list'] as const,
  },
};
