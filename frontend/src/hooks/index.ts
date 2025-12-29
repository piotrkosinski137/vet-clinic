export { useCrud, type CrudConfig, type UseCrudResult } from './useCrud';
export { usePatients } from './usePatients';
export { useClients } from './useClients';
export { useVisits } from './useVisits';
export { useWaitingRoom } from './useWaitingRoom';
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
export { usePagination, type UsePaginationOptions, type UsePaginationResult } from './usePagination';
export { useConfirmDialog, type ConfirmDialogState, type UseConfirmDialogResult } from './useConfirmDialog';
export { useVisitDraft } from './useVisitDraft';
