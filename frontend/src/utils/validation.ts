/**
 * Validation utilities for form inputs.
 * Provides reusable validators following DRY principle.
 */

export type ValidationResult = true | string;
export type Validator<T = string> = (value: T) => ValidationResult;

/**
 * Combines multiple validators into one.
 * Returns the first error message or true if all pass.
 */
export const combineValidators = <T>(...validators: Validator<T>[]): Validator<T> =>
  (value: T) => {
    for (const validator of validators) {
      const result = validator(value);
      if (result !== true) return result;
    }
    return true;
  };

/**
 * Creates an optional validator that only runs if value is not empty.
 */
export const optional = <T>(validator: Validator<T>): Validator<T | undefined | null> =>
  (value) => {
    if (value === undefined || value === null || value === '') return true;
    return validator(value as T);
  };

// Basic validators
export const validators = {
  /**
   * Validates that a value is not empty.
   */
  required: (message = 'This field is required'): Validator<unknown> =>
    (value) => {
      if (value === undefined || value === null) return message;
      if (typeof value === 'string' && value.trim() === '') return message;
      if (Array.isArray(value) && value.length === 0) return message;
      return true;
    },

  /**
   * Validates minimum string length.
   */
  minLength: (min: number, message?: string): Validator<string> =>
    (value) =>
      value.length >= min || (message ?? `Must be at least ${min} characters`),

  /**
   * Validates maximum string length.
   */
  maxLength: (max: number, message?: string): Validator<string> =>
    (value) =>
      value.length <= max || (message ?? `Must be at most ${max} characters`),

  /**
   * Validates string length is within range.
   */
  lengthBetween: (min: number, max: number, message?: string): Validator<string> =>
    (value) =>
      (value.length >= min && value.length <= max) ||
      (message ?? `Must be between ${min} and ${max} characters`),

  /**
   * Validates email format.
   */
  email: (message = 'Invalid email address'): Validator<string> =>
    (value) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value) || message,

  /**
   * Validates phone number format (flexible international format).
   */
  phone: (message = 'Invalid phone number'): Validator<string> =>
    (value) => /^\+?[\d\s\-()]{9,20}$/.test(value.replace(/\s/g, '')) || message,

  /**
   * Validates Polish postal code format (XX-XXX).
   */
  polishPostalCode: (message = 'Invalid postal code (format: XX-XXX)'): Validator<string> =>
    (value) => /^\d{2}-\d{3}$/.test(value) || message,

  /**
   * Validates that a number is positive.
   */
  positive: (message = 'Must be a positive number'): Validator<number> =>
    (value) => value > 0 || message,

  /**
   * Validates that a number is non-negative.
   */
  nonNegative: (message = 'Must be zero or positive'): Validator<number> =>
    (value) => value >= 0 || message,

  /**
   * Validates minimum numeric value.
   */
  min: (minValue: number, message?: string): Validator<number> =>
    (value) => value >= minValue || (message ?? `Must be at least ${minValue}`),

  /**
   * Validates maximum numeric value.
   */
  max: (maxValue: number, message?: string): Validator<number> =>
    (value) => value <= maxValue || (message ?? `Must be at most ${maxValue}`),

  /**
   * Validates number is within range.
   */
  between: (minValue: number, maxValue: number, message?: string): Validator<number> =>
    (value) =>
      (value >= minValue && value <= maxValue) ||
      (message ?? `Must be between ${minValue} and ${maxValue}`),

  /**
   * Validates against a regex pattern.
   */
  pattern: (regex: RegExp, message = 'Invalid format'): Validator<string> =>
    (value) => regex.test(value) || message,

  /**
   * Validates URL format.
   */
  url: (message = 'Invalid URL'): Validator<string> =>
    (value) => {
      try {
        new URL(value);
        return true;
      } catch {
        return message;
      }
    },

  /**
   * Validates date is not in the past.
   */
  futureDate: (message = 'Date must be in the future'): Validator<string | Date> =>
    (value) => {
      const date = typeof value === 'string' ? new Date(value) : value;
      return date > new Date() || message;
    },

  /**
   * Validates date is not in the future.
   */
  pastDate: (message = 'Date must be in the past'): Validator<string | Date> =>
    (value) => {
      const date = typeof value === 'string' ? new Date(value) : value;
      return date < new Date() || message;
    },

  /**
   * Validates date format (YYYY-MM-DD).
   */
  dateFormat: (message = 'Invalid date format (YYYY-MM-DD)'): Validator<string> =>
    (value) => /^\d{4}-\d{2}-\d{2}$/.test(value) || message,

  /**
   * Validates that value matches another value (e.g., password confirmation).
   */
  matches: <T>(otherValue: T, message = 'Values do not match'): Validator<T> =>
    (value) => value === otherValue || message,

  /**
   * Validates value is one of allowed options.
   */
  oneOf: <T>(options: readonly T[], message?: string): Validator<T> =>
    (value) =>
      options.includes(value) ||
      (message ?? `Must be one of: ${options.join(', ')}`),

  /**
   * Custom validator with a predicate function.
   */
  custom: <T>(predicate: (value: T) => boolean, message: string): Validator<T> =>
    (value) => predicate(value) || message,
} as const;

/**
 * Validates a form data object against a schema of validators.
 * Returns an object with field names as keys and error messages as values.
 */
export const validateForm = <T extends Record<string, unknown>>(
  data: T,
  schema: Partial<Record<keyof T, Validator<unknown>>>
): Partial<Record<keyof T, string>> => {
  const errors: Partial<Record<keyof T, string>> = {};

  for (const [field, validator] of Object.entries(schema)) {
    if (validator) {
      const result = (validator as Validator<unknown>)(data[field as keyof T]);
      if (result !== true) {
        errors[field as keyof T] = result;
      }
    }
  }

  return errors;
};

/**
 * Checks if form has any validation errors.
 */
export const hasErrors = <T extends Record<string, unknown>>(
  errors: Partial<Record<keyof T, string>>
): boolean => Object.keys(errors).length > 0;
