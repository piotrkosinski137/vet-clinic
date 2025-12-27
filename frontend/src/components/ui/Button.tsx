import { ButtonHTMLAttributes, forwardRef } from 'react';
import { colors, spacing, borderRadius, fontSize, fontWeight, transitions } from '../../theme';

export type ButtonVariant = 'primary' | 'secondary' | 'danger' | 'success' | 'ghost';
export type ButtonSize = 'sm' | 'md' | 'lg';

export interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: ButtonVariant;
  size?: ButtonSize;
  fullWidth?: boolean;
}

const variantStyles: Record<ButtonVariant, React.CSSProperties> = {
  primary: {
    backgroundColor: colors.primary.main,
    color: colors.primary.contrast,
  },
  secondary: {
    backgroundColor: colors.secondary.main,
    color: colors.secondary.contrast,
  },
  danger: {
    backgroundColor: colors.danger.main,
    color: colors.danger.contrast,
  },
  success: {
    backgroundColor: colors.success.main,
    color: colors.success.contrast,
  },
  ghost: {
    backgroundColor: 'transparent',
    color: colors.neutral.text,
    border: `1px solid ${colors.neutral.border}`,
  },
};

const hoverColors: Record<ButtonVariant, string> = {
  primary: colors.primary.hover,
  secondary: colors.secondary.hover,
  danger: colors.danger.hover,
  success: colors.success.hover,
  ghost: colors.neutral.background,
};

const sizeStyles: Record<ButtonSize, React.CSSProperties> = {
  sm: {
    padding: `${spacing.xs} ${spacing.sm}`,
    fontSize: fontSize.xs,
  },
  md: {
    padding: `${spacing.sm} ${spacing.md}`,
    fontSize: fontSize.sm,
  },
  lg: {
    padding: `${spacing.md} ${spacing.lg}`,
    fontSize: fontSize.md,
  },
};

/**
 * Button component - wrapper for native button element.
 * Use this instead of raw <button> elements for consistent styling.
 */
export const Button = forwardRef<HTMLButtonElement, ButtonProps>(
  ({ variant = 'ghost', size = 'md', fullWidth = false, style, children, ...props }, ref) => {
    const baseStyle: React.CSSProperties = {
      display: 'inline-flex',
      alignItems: 'center',
      justifyContent: 'center',
      gap: spacing.sm,
      border: 'none',
      borderRadius: borderRadius.sm,
      cursor: props.disabled ? 'not-allowed' : 'pointer',
      fontWeight: fontWeight.medium,
      transition: `background ${transitions.normal}`,
      opacity: props.disabled ? 0.6 : 1,
      width: fullWidth ? '100%' : undefined,
      ...variantStyles[variant],
      ...sizeStyles[size],
      ...style,
    };

    return (
      <button
        ref={ref}
        style={baseStyle}
        onMouseEnter={(e) => {
          if (!props.disabled) {
            e.currentTarget.style.backgroundColor = hoverColors[variant];
          }
        }}
        onMouseLeave={(e) => {
          if (!props.disabled) {
            e.currentTarget.style.backgroundColor =
              variantStyles[variant].backgroundColor as string;
          }
        }}
        {...props}
      >
        {children}
      </button>
    );
  }
);

Button.displayName = 'Button';
