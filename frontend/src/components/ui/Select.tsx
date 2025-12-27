import { SelectHTMLAttributes, forwardRef } from 'react';
import { colors, spacing, borderRadius, fontSize } from '../../theme';

export interface SelectProps extends SelectHTMLAttributes<HTMLSelectElement> {
  error?: boolean;
  fullWidth?: boolean;
}

/**
 * Select component - wrapper for native select element.
 * Use this instead of raw <select> elements for consistent styling.
 */
export const Select = forwardRef<HTMLSelectElement, SelectProps>(
  ({ error = false, fullWidth = true, style, children, ...props }, ref) => {
    const baseStyle: React.CSSProperties = {
      width: fullWidth ? '100%' : undefined,
      padding: spacing.sm,
      border: `1px solid ${error ? colors.danger.main : colors.neutral.border}`,
      borderRadius: borderRadius.sm,
      fontSize: fontSize.sm,
      outline: 'none',
      backgroundColor: colors.neutral.white,
      cursor: 'pointer',
      ...style,
    };

    return (
      <select
        ref={ref}
        style={baseStyle}
        onFocus={(e) => {
          e.currentTarget.style.borderColor = error ? colors.danger.main : colors.primary.main;
        }}
        onBlur={(e) => {
          e.currentTarget.style.borderColor = error ? colors.danger.main : colors.neutral.border;
        }}
        {...props}
      >
        {children}
      </select>
    );
  }
);

Select.displayName = 'Select';
