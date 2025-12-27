import { InputHTMLAttributes, forwardRef } from 'react';
import { colors, spacing, borderRadius, fontSize } from '../../theme';

export interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  error?: boolean;
  fullWidth?: boolean;
}

/**
 * Input component - wrapper for native input element.
 * Use this instead of raw <input> elements for consistent styling.
 */
export const Input = forwardRef<HTMLInputElement, InputProps>(
  ({ error = false, fullWidth = true, style, ...props }, ref) => {
    const baseStyle: React.CSSProperties = {
      width: fullWidth ? '100%' : undefined,
      padding: spacing.sm,
      border: `1px solid ${error ? colors.danger.main : colors.neutral.border}`,
      borderRadius: borderRadius.sm,
      fontSize: fontSize.sm,
      outline: 'none',
      transition: 'border-color 0.2s',
      ...style,
    };

    return (
      <input
        ref={ref}
        style={baseStyle}
        onFocus={(e) => {
          e.currentTarget.style.borderColor = error ? colors.danger.main : colors.primary.main;
        }}
        onBlur={(e) => {
          e.currentTarget.style.borderColor = error ? colors.danger.main : colors.neutral.border;
        }}
        {...props}
      />
    );
  }
);

Input.displayName = 'Input';
