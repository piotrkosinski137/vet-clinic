/**
 * UI-related constants to avoid magic numbers in components.
 */
export const UI = {
  /** Text truncation lengths */
  TRUNCATE: {
    SHORT: 80,
    MEDIUM: 100,
  },
  /** Preview limits for lists */
  PREVIEW: {
    MATERIALS: 4,
    PETS: 3,
  },
} as const;
