import { useState, useEffect } from 'react';
import { DEBOUNCE_DELAY_MS } from '../constants/timings';

/**
 * Hook that debounces a value by a specified delay.
 * Useful for search inputs to avoid excessive API calls.
 *
 * @param value - The value to debounce
 * @param delay - The delay in milliseconds (defaults to DEBOUNCE_DELAY_MS)
 * @returns The debounced value
 */
export function useDebounce<T>(value: T, delay: number = DEBOUNCE_DELAY_MS): T {
  const [debouncedValue, setDebouncedValue] = useState<T>(value);

  useEffect(() => {
    const handler = setTimeout(() => setDebouncedValue(value), delay);
    return () => clearTimeout(handler);
  }, [value, delay]);

  return debouncedValue;
}
