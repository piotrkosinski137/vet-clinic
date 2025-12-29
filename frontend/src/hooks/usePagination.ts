import { useState, useMemo, useCallback } from 'react';
import { PAGINATION } from '../constants';

export interface UsePaginationOptions {
  /** Items per page. Defaults to PAGINATION.DEFAULT_PAGE_SIZE */
  pageSize?: number;
  /** Initial page (0-indexed). Defaults to 0 */
  initialPage?: number;
}

export interface UsePaginationResult<T> {
  /** Items for the current page */
  paginatedItems: T[];
  /** Current page (0-indexed) */
  currentPage: number;
  /** Total number of pages */
  totalPages: number;
  /** Total number of items */
  totalItems: number;
  /** Whether there's a previous page */
  hasPreviousPage: boolean;
  /** Whether there's a next page */
  hasNextPage: boolean;
  /** Go to a specific page */
  setPage: (page: number) => void;
  /** Go to the next page */
  nextPage: () => void;
  /** Go to the previous page */
  previousPage: () => void;
  /** Reset to the first page */
  reset: () => void;
  /** Page size being used */
  pageSize: number;
}

/**
 * Hook for client-side pagination of arrays.
 *
 * @example
 * const { paginatedItems, currentPage, totalPages, setPage, reset } = usePagination(items);
 *
 * // With custom page size
 * const { paginatedItems } = usePagination(items, { pageSize: 10 });
 *
 * // Reset when filter changes
 * useEffect(() => { reset(); }, [searchQuery]);
 */
export function usePagination<T>(
  items: T[],
  options: UsePaginationOptions = {}
): UsePaginationResult<T> {
  const {
    pageSize = PAGINATION.DEFAULT_PAGE_SIZE,
    initialPage = 0
  } = options;

  const [currentPage, setCurrentPage] = useState(initialPage);

  const totalItems = items.length;
  const totalPages = Math.max(1, Math.ceil(totalItems / pageSize));

  // Ensure current page is within valid range when items change
  const validatedPage = useMemo(() => {
    if (currentPage >= totalPages) {
      return Math.max(0, totalPages - 1);
    }
    return currentPage;
  }, [currentPage, totalPages]);

  // Update page if it became invalid
  if (validatedPage !== currentPage) {
    setCurrentPage(validatedPage);
  }

  const paginatedItems = useMemo(() => {
    const start = validatedPage * pageSize;
    const end = start + pageSize;
    return items.slice(start, end);
  }, [items, validatedPage, pageSize]);

  const setPage = useCallback((page: number) => {
    const clampedPage = Math.max(0, Math.min(page, totalPages - 1));
    setCurrentPage(clampedPage);
  }, [totalPages]);

  const nextPage = useCallback(() => {
    setCurrentPage((prev) => Math.min(prev + 1, totalPages - 1));
  }, [totalPages]);

  const previousPage = useCallback(() => {
    setCurrentPage((prev) => Math.max(prev - 1, 0));
  }, []);

  const reset = useCallback(() => {
    setCurrentPage(0);
  }, []);

  return {
    paginatedItems,
    currentPage: validatedPage,
    totalPages,
    totalItems,
    hasPreviousPage: validatedPage > 0,
    hasNextPage: validatedPage < totalPages - 1,
    setPage,
    nextPage,
    previousPage,
    reset,
    pageSize,
  };
}
