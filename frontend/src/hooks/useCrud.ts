import { useState, useEffect, useCallback, useRef } from 'react';

/**
 * Configuration for the useCrud hook.
 */
export interface CrudConfig<TResponse, TRequest> {
  /** Function to fetch all items */
  fetchAll: () => Promise<TResponse[]>;
  /** Function to create a new item */
  create: (data: TRequest) => Promise<TResponse>;
  /** Function to update an existing item */
  update: (id: string, data: TRequest) => Promise<TResponse>;
  /** Function to delete an item */
  remove: (id: string) => Promise<void>;
  /** Error message for fetch failures */
  fetchErrorMessage?: string;
}

/**
 * Return type for the useCrud hook.
 */
export interface UseCrudResult<TResponse, TRequest> {
  /** Array of items */
  items: TResponse[];
  /** Loading state */
  loading: boolean;
  /** Error message or null */
  error: string | null;
  /** Refresh the data */
  refresh: () => void;
  /** Create a new item and add to state */
  create: (data: TRequest) => Promise<TResponse>;
  /** Update an existing item in state */
  update: (id: string, data: TRequest) => Promise<TResponse>;
  /** Remove an item from state */
  remove: (id: string) => Promise<void>;
}

/**
 * Generic CRUD hook for managing entity state.
 * Eliminates duplicate code across entity-specific hooks.
 *
 * @example
 * ```tsx
 * const crud = useCrud<PatientResponse, PatientRequest>({
 *   fetchAll: api.getPatients,
 *   create: api.createPatient,
 *   update: api.updatePatient,
 *   remove: api.deletePatient,
 *   fetchErrorMessage: 'Failed to fetch patients',
 * });
 * ```
 */
export function useCrud<TResponse extends { id: string }, TRequest>(
  config: CrudConfig<TResponse, TRequest>
): UseCrudResult<TResponse, TRequest> {
  const [items, setItems] = useState<TResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Store config in ref to avoid dependency issues with objects
  const configRef = useRef(config);
  configRef.current = config;

  // Track if initial fetch has been done
  const hasFetched = useRef(false);

  const fetchItems = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await configRef.current.fetchAll();
      setItems(data);
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : configRef.current.fetchErrorMessage || 'Failed to fetch data'
      );
    } finally {
      setLoading(false);
    }
  }, []);

  // Fetch only once on mount
  useEffect(() => {
    if (hasFetched.current) return;
    hasFetched.current = true;
    fetchItems();
  }, [fetchItems]);

  const create = useCallback(
    async (data: TRequest): Promise<TResponse> => {
      const created = await configRef.current.create(data);
      setItems((prev) => [...prev, created]);
      return created;
    },
    []
  );

  const update = useCallback(
    async (id: string, data: TRequest): Promise<TResponse> => {
      const updated = await configRef.current.update(id, data);
      setItems((prev) => prev.map((item) => (item.id === id ? updated : item)));
      return updated;
    },
    []
  );

  const remove = useCallback(
    async (id: string): Promise<void> => {
      await configRef.current.remove(id);
      setItems((prev) => prev.filter((item) => item.id !== id));
    },
    []
  );

  return {
    items,
    loading,
    error,
    refresh: fetchItems,
    create,
    update,
    remove,
  };
}
