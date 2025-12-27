import { HTMLAttributes, forwardRef } from 'react';
import { colors, spacing, borderRadius, fontSize, fontWeight } from '../../theme';

export type BadgeVariant = 'primary' | 'secondary' | 'success' | 'danger' | 'warning';

export interface BadgeProps extends HTMLAttributes<HTMLSpanElement> {
  variant?: BadgeVariant;
}

const variantStyles: Record<BadgeVariant, React.CSSProperties> = {
  primary: {
    backgroundColor: colors.primary.light,
    color: colors.primary.hover,
  },
  secondary: {
    backgroundColor: colors.secondary.light,
    color: colors.secondary.main,
  },
  success: {
    backgroundColor: colors.success.light,
    color: colors.success.main,
  },
  danger: {
    backgroundColor: colors.danger.light,
    color: colors.danger.main,
  },
  warning: {
    backgroundColor: colors.warning.light,
    color: colors.warning.main,
  },
};

/**
 * Badge component - small label for status/category display.
 * Use this for consistent badge styling across the app.
 */
export const Badge = forwardRef<HTMLSpanElement, BadgeProps>(
  ({ variant = 'primary', style, children, ...props }, ref) => {
    const baseStyle: React.CSSProperties = {
      display: 'inline-block',
      padding: `${spacing.xs} ${spacing.sm}`,
      borderRadius: borderRadius.lg,
      fontSize: fontSize.xs,
      fontWeight: fontWeight.medium,
      ...variantStyles[variant],
      ...style,
    };

    return (
      <span ref={ref} style={baseStyle} {...props}>
        {children}
      </span>
    );
  }
);

Badge.displayName = 'Badge';
