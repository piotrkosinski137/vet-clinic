/**
 * Visit status configuration for consistent styling across the application.
 */
export const VISIT_STATUS_CONFIG = {
  SCHEDULED: {
    label: 'Scheduled',
    color: '#2196F3',
    bg: '#E3F2FD',
    bgColor: '#E3F2FD',
    icon: 'S',
  },
  CHECKED_IN: {
    label: 'Checked In',
    color: '#F57C00',
    bg: '#FFF8E1',
    bgColor: '#FFF8E1',
    icon: 'W',
  },
  IN_PROGRESS: {
    label: 'In Progress',
    color: '#FF9800',
    bg: '#FFF3E0',
    bgColor: '#FFF3E0',
    icon: 'P',
  },
  COMPLETED: {
    label: 'Completed',
    color: '#4CAF50',
    bg: '#E8F5E9',
    bgColor: '#E8F5E9',
    icon: 'C',
  },
  CANCELLED: {
    label: 'Cancelled',
    color: '#9E9E9E',
    bg: '#F5F5F5',
    bgColor: '#F5F5F5',
    icon: 'X',
  },
  NO_SHOW: {
    label: 'No Show',
    color: '#F44336',
    bg: '#FFEBEE',
    bgColor: '#FFEBEE',
    icon: 'N',
  },
} as const;

export type VisitStatusKey = keyof typeof VISIT_STATUS_CONFIG;

export const getVisitStatusConfig = (status: string) => {
  return VISIT_STATUS_CONFIG[status as VisitStatusKey] || VISIT_STATUS_CONFIG.SCHEDULED;
};
