import { TextareaHTMLAttributes, forwardRef } from 'react';
import { colors, spacing, borderRadius, fontSize } from '../../theme';

export interface TextAreaProps extends TextareaHTMLAttributes<HTMLTextAreaElement> {
  error?: boolean;
  fullWidth?: boolean;
}

/**
 * TextArea component - wrapper for native textarea element.
 * Use this instead of raw <textarea> elements for consistent styling.
 */
export const TextArea = forwardRef<HTMLTextAreaElement, TextAreaProps>(
  ({ error = false, fullWidth = true, style, ...props }, ref) => {
    const baseStyle: React.CSSProperties = {
      width: fullWidth ? '100%' : undefined,
      padding: spacing.sm,
      border: `1px solid ${error ? colors.danger.main : colors.neutral.border}`,
      borderRadius: borderRadius.sm,
      fontSize: fontSize.sm,
      outline: 'none',
      resize: 'vertical',
      fontFamily: 'inherit',
      ...style,
    };

    return (
      <textarea
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

TextArea.displayName = 'TextArea';
