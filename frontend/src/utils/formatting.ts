import { DEFAULTS } from '../constants/defaults';
import { CURRENCY } from '../constants/locale';

/**
 * Truncates text to a maximum length with ellipsis.
 */
export const truncateText = (
  text: string | null | undefined,
  maxLength: number = DEFAULTS.TRUNCATION_LENGTH
): string => {
  if (!text) return '';
  if (text.length <= maxLength) return text;
  return text.substring(0, maxLength) + '...';
};

/**
 * Formats a number as Polish currency (PLN).
 */
export const formatCurrency = (amount: number | null | undefined): string => {
  if (amount == null) return '0.00 PLN';
  return new Intl.NumberFormat(CURRENCY.LOCALE, {
    style: 'currency',
    currency: CURRENCY.CODE,
  }).format(amount);
};

/**
 * Formats a number with specified decimal places.
 */
export const formatNumber = (
  value: number | null | undefined,
  decimals: number = 2
): string => {
  if (value == null) return '0';
  return value.toFixed(decimals);
};

/**
 * Calculates profit margin percentage.
 */
export const calculateMargin = (
  costPrice: number,
  sellPrice: number
): number => {
  if (sellPrice === 0) return 0;
  return ((sellPrice - costPrice) / sellPrice) * 100;
};

/**
 * Formats weight with unit.
 */
export const formatWeight = (weight: number | null | undefined): string => {
  if (weight == null) return '';
  return weight.toFixed(1) + ' kg';
};

/**
 * Formats temperature with unit (1 decimal place).
 */
export const formatTemperature = (temp: number | null | undefined): string => {
  if (temp == null) return '';
  return temp.toFixed(1) + '°C';
};

/**
 * Formats weight for display (2 decimal places).
 */
export const formatWeightDisplay = (weight: number | null | undefined): string => {
  if (weight == null) return '-';
  return weight.toFixed(2) + ' kg';
};

/**
 * Formats temperature for display (1 decimal place).
 */
export const formatTemperatureDisplay = (temp: number | null | undefined): string => {
  if (temp == null) return '-';
  return temp.toFixed(1) + '°C';
};
