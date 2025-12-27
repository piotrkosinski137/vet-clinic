import { HTMLAttributes, forwardRef, ReactNode, useId } from 'react';
import { colors, spacing, fontSize, fontWeight } from '../../theme';

export interface FormFieldProps extends HTMLAttributes<HTMLDivElement> {
  /** Label text for the form field */
  label?: string;
  /** Error message to display */
  error?: string;
  /** Whether the field is required */
  required?: boolean;
  /** ID for the input element (for label association). Auto-generated if not provided. */
  inputId?: string;
  children: ReactNode;
}

/**
 * FormField component - wrapper for form inputs with label and error.
 * Provides proper accessibility with label-input association via htmlFor/id.
 *
 * Usage: Pass inputId to both FormField and the child input for proper accessibility.
 * If no inputId is provided, an auto-generated ID is available via the aria-describedby pattern.
 */
export const FormField = forwardRef<HTMLDivElement, FormFieldProps>(
  ({ label, error, required, inputId, children, style, ...props }, ref) => {
    const autoId = useId();
    const fieldId = inputId || autoId;
    const errorId = `${fieldId}-error`;

    return (
      <div
        ref={ref}
        style={{
          marginBottom: spacing.md,
          ...style,
        }}
        {...props}
      >
        {label && (
          <label
            htmlFor={fieldId}
            style={{
              display: 'block',
              marginBottom: spacing.xs,
              fontWeight: fontWeight.medium,
              color: colors.neutral.text,
              fontSize: fontSize.sm,
            }}
          >
            {label}
            {required && (
              <span style={{ color: colors.danger.main, marginLeft: spacing.xs }} aria-hidden="true">*</span>
            )}
          </label>
        )}
        {children}
        {error && (
          <span
            id={errorId}
            role="alert"
            style={{
              display: 'block',
              marginTop: spacing.xs,
              color: colors.danger.main,
              fontSize: fontSize.xs,
            }}
          >
            {error}
          </span>
        )}
      </div>
    );
  }
);

FormField.displayName = 'FormField';
