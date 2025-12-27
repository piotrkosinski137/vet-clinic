/**
 * UI-related constants to avoid magic numbers in components.
 */
export const UI = {
  /** Text truncation lengths */
  TRUNCATE: {
    SHORT: 80,
    MEDIUM: 100,
    LONG: 200,
  },
  /** Preview limits for lists */
  PREVIEW: {
    MATERIALS: 4,
    PETS: 3,
  },
  /** Grid column configurations for responsive layouts */
  GRID_COLUMNS: {
    SM: 1,
    MD: 2,
    LG: 3,
    XL: 4,
  },
  /** Modal width presets in pixels */
  MODAL_WIDTH: {
    SM: 400,
    MD: 600,
    LG: 800,
    XL: 1000,
  },
  /** Debounce delays in milliseconds */
  DEBOUNCE: {
    SEARCH: 300,
    RESIZE: 150,
    INPUT: 200,
  },
  /** Table dimensions */
  TABLE: {
    ROW_HEIGHT: 48,
    HEADER_HEIGHT: 56,
    MIN_COLUMN_WIDTH: 100,
  },
  /** Animation durations in milliseconds */
  ANIMATION: {
    FAST: 150,
    NORMAL: 300,
    SLOW: 500,
  },
} as const;
