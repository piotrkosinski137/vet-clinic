import { useState, useEffect, useCallback, useRef } from 'react';
import { api } from '../api/client';
import type { PriceListItemResponse, PriceListFilters, ItemCategory } from '../api/types';

interface UsePriceListResult {
  items: PriceListItemResponse[];
  loading: boolean;
  error: string | null;
  refetch: () => Promise<void>;
  searchItems: (query: string, category?: ItemCategory) => PriceListItemResponse[];
}

export function usePriceList(filters?: PriceListFilters): UsePriceListResult {
  const [items, setItems] = useState<PriceListItemResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Store filters in ref to avoid dependency issues with objects
  const filtersRef = useRef(filters);
  filtersRef.current = filters;

  // Track if initial fetch has been done
  const hasFetched = useRef(false);

  // AbortController for request cancellation
  const abortControllerRef = useRef<AbortController | null>(null);

  const fetchItems = useCallback(async () => {
    // Abort any pending request
    if (abortControllerRef.current) {
      abortControllerRef.current.abort();
    }
    abortControllerRef.current = new AbortController();

    setLoading(true);
    setError(null);
    try {
      const data = await api.getPriceListItems(filtersRef.current);
      setItems(data);
    } catch (err) {
      if (err instanceof Error && err.name !== 'AbortError') {
        setError(err.message);
      }
    } finally {
      setLoading(false);
    }
  }, []);

  // Fetch only once on mount
  useEffect(() => {
    if (hasFetched.current) return;
    hasFetched.current = true;
    fetchItems();

    // Cleanup function to abort on unmount
    return () => {
      if (abortControllerRef.current) {
        abortControllerRef.current.abort();
      }
    };
  }, [fetchItems]);

  const searchItems = useCallback(
    (query: string, category?: ItemCategory): PriceListItemResponse[] => {
      const lowerQuery = query.toLowerCase();
      return items.filter((item) => {
        const matchesQuery =
          item.name.toLowerCase().includes(lowerQuery) ||
          (item.description?.toLowerCase().includes(lowerQuery) ?? false);
        const matchesCategory = !category || item.category === category;
        return matchesQuery && matchesCategory;
      });
    },
    [items]
  );

  return {
    items,
    loading,
    error,
    refetch: fetchItems,
    searchItems,
  };
}

export const CATEGORY_LABELS: Record<ItemCategory, { label: string; icon: string }> = {
  SERVICE: { label: 'Service', icon: '🔧' },
  MEDICATION: { label: 'Medication', icon: '💊' },
  PRODUCT: { label: 'Product', icon: '📦' },
  PROCEDURE: { label: 'Procedure', icon: '🏥' },
  CONSULTATION: { label: 'Consultation', icon: '👨‍⚕️' },
  LAB_TEST: { label: 'Lab Test', icon: '🔬' },
  VACCINATION: { label: 'Vaccination', icon: '💉' },
  OTHER: { label: 'Other', icon: '📋' },
};

export function formatCurrency(amount: number): string {
  return new Intl.NumberFormat('pl-PL', {
    style: 'currency',
    currency: 'PLN',
  }).format(amount);
}

export function calculateTotalCost(materials: { quantity: number; costPrice: number }[]): number {
  return materials.reduce((sum, item) => sum + item.quantity * item.costPrice, 0);
}

export function calculateTotalSell(materials: { quantity: number; sellPrice: number }[]): number {
  return materials.reduce((sum, item) => sum + item.quantity * item.sellPrice, 0);
}

export function calculateProfit(
  materials: { quantity: number; costPrice: number; sellPrice: number }[]
): number {
  return calculateTotalSell(materials) - calculateTotalCost(materials);
}
