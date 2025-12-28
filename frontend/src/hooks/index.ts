export { useCrud, type CrudConfig, type UseCrudResult } from './useCrud';
export { usePatients } from './usePatients';
export { useClients } from './useClients';
export { useVisits } from './useVisits';
export { useVeterinarians } from './useVeterinarians';
export { useVeterinarianSchedule, useVeterinariansAvailability } from './useVeterinarianSchedule';
export {
  usePriceList,
  CATEGORY_LABELS,
  formatCurrency,
  calculateTotalCost,
  calculateTotalSell,
  calculateProfit,
} from './usePriceList';
export { useAsyncState, type AsyncState } from './useAsyncState';
export { useAuditLogs, useAuditLogsForEntity } from './useAuditLogs';
export { useAuthErrorHandler, dispatchAuthError, AUTH_ERROR_EVENT } from './useAuthErrorHandler';
export { useDashboardStats, type UseDashboardStatsResult } from './useDashboardStats';
