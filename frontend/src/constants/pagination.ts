/**
 * Pagination constants to eliminate magic numbers and ensure consistency.
 */
export const PAGINATION = {
  /** Default page size for main lists */
  DEFAULT_PAGE_SIZE: 6,

  /** Page size for side panels */
  PANEL_PAGE_SIZE: 5,

  /** Alias for PANEL_PAGE_SIZE (legacy) */
  PANEL: 5,

  /** Maximum items to show before "show more" pattern */
  MAX_VISIBLE_ITEMS: 3,

  /** Maximum materials to show inline */
  MAX_VISIBLE_MATERIALS: 4,
} as const;

export type PaginationKey = keyof typeof PAGINATION;
