import { useState, useCallback } from 'react';

export interface AsyncState<T> {
  data: T;
  loading: boolean;
  error: string | null;
  execute: () => Promise<T | undefined>;
  setData: React.Dispatch<React.SetStateAction<T>>;
}

export function useAsyncState<T>(
  fetchFn: () => Promise<T>,
  initialValue: T
): AsyncState<T> {
  const [data, setData] = useState<T>(initialValue);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const execute = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const result = await fetchFn();
      setData(result);
      return result;
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Operation failed';
      setError(message);
      return undefined;
    } finally {
      setLoading(false);
    }
  }, [fetchFn]);

  return { data, loading, error, execute, setData };
}
