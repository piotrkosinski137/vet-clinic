/**
 * Default values for forms and data initialization.
 */
export const DEFAULTS = {
  /** Default species for new patients */
  SPECIES: 'DOG' as const,

  /** Default visit duration in minutes */
  VISIT_DURATION_MINUTES: 30,

  /** Default truncation length for text */
  TRUNCATION_LENGTH: 80,

  /** Longer truncation for descriptions */
  TRUNCATION_LENGTH_LONG: 100,

  /** Working hours start */
  WORK_START_HOUR: 8,

  /** Working hours end */
  WORK_END_HOUR: 18,
} as const;

export type DefaultsKey = keyof typeof DEFAULTS;
