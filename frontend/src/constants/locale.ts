/**
 * Locale and currency constants for the application.
 * Centralized to avoid magic strings and enable easy internationalization.
 */

export const LOCALE = {
  PL: 'pl-PL',
  EN: 'en-US',
} as const;

export const CURRENCY = {
  CODE: 'PLN',
  LOCALE: LOCALE.PL,
} as const;

export const DATE_FORMAT_OPTIONS = {
  TIME_SHORT: { hour: '2-digit', minute: '2-digit', hour12: false } as const,
  DATE_FULL: { dateStyle: 'short' } as const,
  DATETIME_SHORT: { dateStyle: 'short', timeStyle: 'short', hour12: false } as const,
} as const;

/**
 * Get locale string based on language code.
 * @param language - Language code ('pl' or 'en')
 * @returns The full locale string (e.g., 'pl-PL' or 'en-US')
 */
export function getLocaleForLanguage(language: string): string {
  return language === 'pl' ? LOCALE.PL : LOCALE.EN;
}
