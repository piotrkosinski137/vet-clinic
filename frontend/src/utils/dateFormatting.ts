import { LOCALE, DATE_FORMAT_OPTIONS } from '../constants/locale';

/**
 * Formats a date/time for display.
 */
export const formatDateTime = (date: string | Date | null | undefined): string => {
  if (!date) return '';
  return new Date(date).toLocaleString(LOCALE.PL);
};

/**
 * Formats a date for display (without time).
 */
export const formatDate = (date: string | Date | null | undefined): string => {
  if (!date) return '';
  return new Date(date).toLocaleDateString(LOCALE.PL);
};

/**
 * Formats a time for display.
 */
export const formatTime = (date: string | Date | null | undefined): string => {
  if (!date) return '';
  return new Date(date).toLocaleTimeString(LOCALE.PL, DATE_FORMAT_OPTIONS.TIME_SHORT);
};

/**
 * Formats a date for API requests (YYYY-MM-DD).
 */
export const formatDateForApi = (date: Date): string => {
  return date.toISOString().split('T')[0];
};

/**
 * Gets a date key string for grouping/lookup (YYYY-MM-DD).
 */
export const getDateKey = (date: Date): string => {
  return date.toISOString().split('T')[0];
};

/**
 * Formats a relative date (e.g., "2 days ago", "in 3 hours").
 */
export const formatRelativeDate = (date: string | Date | null | undefined): string => {
  if (!date) return '';
  const now = new Date();
  const target = new Date(date);
  const diffMs = target.getTime() - now.getTime();
  const diffDays = Math.round(diffMs / (1000 * 60 * 60 * 24));

  if (diffDays === 0) return 'Today';
  if (diffDays === 1) return 'Tomorrow';
  if (diffDays === -1) return 'Yesterday';
  if (diffDays > 0) return 'in ' + diffDays + ' days';
  return Math.abs(diffDays) + ' days ago';
};

/**
 * Gets start of week (Monday) for a given date.
 */
export const getStartOfWeek = (date: Date): Date => {
  const d = new Date(date);
  const day = d.getDay();
  const diff = d.getDate() - day + (day === 0 ? -6 : 1);
  d.setDate(diff);
  d.setHours(0, 0, 0, 0);
  return d;
};

/**
 * Gets all dates in a week starting from the given date.
 */
export const getWeekDates = (date: Date): Date[] => {
  const monday = getStartOfWeek(date);
  const dates: Date[] = [];
  for (let i = 0; i < 7; i++) {
    const d = new Date(monday);
    d.setDate(monday.getDate() + i);
    dates.push(d);
  }
  return dates;
};
