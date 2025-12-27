import { VisitStatus } from '../api';
import { colors } from '../theme';

/**
 * Visit status display configuration.
 * Provides consistent styling for visit status badges across the application.
 */
export const VISIT_STATUS_CONFIG: Record<
  VisitStatus,
  { label: string; color: string; bg: string }
> = {
  SCHEDULED: {
    label: 'Scheduled',
    color: colors.primary.main,
    bg: colors.primary.light,
  },
  IN_PROGRESS: {
    label: 'In Progress',
    color: colors.warning.main,
    bg: colors.warning.light,
  },
  COMPLETED: {
    label: 'Completed',
    color: colors.success.main,
    bg: colors.success.light,
  },
  CANCELLED: {
    label: 'Cancelled',
    color: colors.neutral.textMuted,
    bg: colors.neutral.border,
  },
};

/**
 * Get visit status display info by status value.
 */
export function getVisitStatusInfo(status: VisitStatus) {
  return (
    VISIT_STATUS_CONFIG[status] || {
      label: status,
      color: colors.neutral.textMuted,
      bg: colors.neutral.background,
    }
  );
}
