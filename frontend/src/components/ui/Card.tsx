import { HTMLAttributes, forwardRef } from 'react';
import { colors, spacing, borderRadius, shadows, transitions } from '../../theme';

export interface CardProps extends HTMLAttributes<HTMLDivElement> {
  variant?: 'default' | 'outlined' | 'elevated' | 'gradient' | 'glass';
  padding?: 'none' | 'sm' | 'md' | 'lg';
  hoverable?: boolean;
  accentColor?: 'primary' | 'success' | 'warning' | 'danger' | 'info';
}

const paddingMap = {
  none: '0',
  sm: spacing.sm,
  md: spacing.md,
  lg: spacing.lg,
};

const accentColorMap = {
  primary: colors.primary.main,
  success: colors.success.main,
  warning: colors.warning.main,
  danger: colors.danger.main,
  info: colors.info.main,
};

const accentShadowMap = {
  primary: shadows.primary,
  success: shadows.success,
  warning: shadows.warning,
  danger: shadows.danger,
  info: shadows.primary,
};

/**
 * Card component - container for grouped content.
 * Use this for consistent card styling across the app.
 *
 * Variants:
 * - default: White card with subtle shadow
 * - outlined: Border-only card
 * - elevated: Higher shadow for emphasis
 * - gradient: Subtle gradient background
 * - glass: Frosted glass effect
 */
export const Card = forwardRef<HTMLDivElement, CardProps>(
  ({
    variant = 'default',
    padding = 'md',
    hoverable = false,
    accentColor,
    style,
    children,
    onMouseEnter,
    onMouseLeave,
    ...props
  }, ref) => {
    const getVariantStyles = (): React.CSSProperties => {
      switch (variant) {
        case 'outlined':
          return {
            backgroundColor: colors.neutral.surface,
            boxShadow: 'none',
            border: `1px solid ${colors.neutral.border}`,
          };
        case 'elevated':
          return {
            backgroundColor: colors.neutral.surface,
            boxShadow: shadows.lg,
            border: 'none',
          };
        case 'gradient':
          return {
            background: colors.gradients.cardHighlight,
            boxShadow: shadows.sm,
            border: `1px solid ${colors.neutral.borderLight}`,
          };
        case 'glass':
          return {
            backgroundColor: 'rgba(255, 255, 255, 0.8)',
            backdropFilter: 'blur(10px)',
            boxShadow: shadows.md,
            border: `1px solid rgba(255, 255, 255, 0.5)`,
          };
        default:
          return {
            backgroundColor: colors.neutral.surface,
            boxShadow: shadows.sm,
            border: `1px solid ${colors.neutral.borderLight}`,
          };
      }
    };

    const baseStyle: React.CSSProperties = {
      borderRadius: borderRadius.lg,
      padding: paddingMap[padding],
      transition: transitions.all,
      ...(accentColor && {
        borderLeft: `4px solid ${accentColorMap[accentColor]}`,
      }),
      ...getVariantStyles(),
      ...style,
    };

    const handleMouseEnter = (e: React.MouseEvent<HTMLDivElement>) => {
      if (hoverable) {
        e.currentTarget.style.transform = 'translateY(-2px)';
        e.currentTarget.style.boxShadow = accentColor
          ? accentShadowMap[accentColor]
          : shadows.lg;
      }
      onMouseEnter?.(e);
    };

    const handleMouseLeave = (e: React.MouseEvent<HTMLDivElement>) => {
      if (hoverable) {
        e.currentTarget.style.transform = 'translateY(0)';
        e.currentTarget.style.boxShadow = getVariantStyles().boxShadow as string || shadows.sm;
      }
      onMouseLeave?.(e);
    };

    return (
      <div
        ref={ref}
        style={baseStyle}
        onMouseEnter={handleMouseEnter}
        onMouseLeave={handleMouseLeave}
        {...props}
      >
        {children}
      </div>
    );
  }
);

Card.displayName = 'Card';

// Card sub-components for structured content
export interface CardHeaderProps extends HTMLAttributes<HTMLDivElement> {
  withBorder?: boolean;
}

export const CardHeader = forwardRef<HTMLDivElement, CardHeaderProps>(
  ({ style, children, withBorder = false, ...props }, ref) => (
    <div
      ref={ref}
      style={{
        marginBottom: spacing.md,
        paddingBottom: withBorder ? spacing.md : 0,
        borderBottom: withBorder ? `1px solid ${colors.neutral.border}` : 'none',
        ...style,
      }}
      {...props}
    >
      {children}
    </div>
  )
);

CardHeader.displayName = 'CardHeader';

export interface CardTitleProps extends HTMLAttributes<HTMLHeadingElement> {
  size?: 'sm' | 'md' | 'lg';
}

export const CardTitle = forwardRef<HTMLHeadingElement, CardTitleProps>(
  ({ style, children, size = 'md', ...props }, ref) => {
    const sizeStyles = {
      sm: { fontSize: '14px' },
      md: { fontSize: '16px' },
      lg: { fontSize: '18px' },
    };

    return (
      <h3
        ref={ref}
        style={{
          margin: 0,
          marginBottom: spacing.sm,
          color: colors.secondary.main,
          fontWeight: 600,
          ...sizeStyles[size],
          ...style,
        }}
        {...props}
      >
        {children}
      </h3>
    );
  }
);

CardTitle.displayName = 'CardTitle';

export interface CardContentProps extends HTMLAttributes<HTMLDivElement> {}

export const CardContent = forwardRef<HTMLDivElement, CardContentProps>(
  ({ style, children, ...props }, ref) => (
    <div ref={ref} style={style} {...props}>
      {children}
    </div>
  )
);

CardContent.displayName = 'CardContent';

export interface CardActionsProps extends HTMLAttributes<HTMLDivElement> {
  align?: 'left' | 'right' | 'center' | 'space-between';
}

export const CardActions = forwardRef<HTMLDivElement, CardActionsProps>(
  ({ style, children, align = 'left', ...props }, ref) => {
    const alignMap = {
      left: 'flex-start',
      right: 'flex-end',
      center: 'center',
      'space-between': 'space-between',
    };

    return (
      <div
        ref={ref}
        style={{
          display: 'flex',
          gap: spacing.sm,
          marginTop: spacing.md,
          paddingTop: spacing.md,
          borderTop: `1px solid ${colors.neutral.border}`,
          justifyContent: alignMap[align],
          ...style,
        }}
        {...props}
      >
        {children}
      </div>
    );
  }
);

CardActions.displayName = 'CardActions';

// Stat Card - for displaying statistics/metrics
export interface StatCardProps extends HTMLAttributes<HTMLDivElement> {
  label: string;
  value: string | number;
  icon?: string;
  trend?: { value: number; isPositive: boolean };
  color?: 'primary' | 'success' | 'warning' | 'danger' | 'info';
}

export const StatCard = forwardRef<HTMLDivElement, StatCardProps>(
  ({ label, value, icon, trend, color = 'primary', style, ...props }, ref) => {
    const colorMap = {
      primary: colors.primary,
      success: colors.success,
      warning: colors.warning,
      danger: colors.danger,
      info: colors.info,
    };

    const selectedColor = colorMap[color];

    return (
      <Card
        ref={ref}
        variant="default"
        hoverable
        style={{
          background: `linear-gradient(135deg, ${selectedColor.light} 0%, ${colors.neutral.surface} 100%)`,
          borderLeft: `4px solid ${selectedColor.main}`,
          ...style,
        }}
        {...props}
      >
        <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between' }}>
          <div>
            <div style={{
              fontSize: '12px',
              color: colors.neutral.textMuted,
              textTransform: 'uppercase',
              letterSpacing: '0.5px',
              marginBottom: spacing.xs,
            }}>
              {label}
            </div>
            <div style={{
              fontSize: '28px',
              fontWeight: 700,
              color: colors.secondary.main,
              lineHeight: 1.2,
            }}>
              {value}
            </div>
            {trend && (
              <div style={{
                fontSize: '12px',
                color: trend.isPositive ? colors.success.main : colors.danger.main,
                marginTop: spacing.xs,
              }}>
                {trend.isPositive ? '↑' : '↓'} {Math.abs(trend.value)}%
              </div>
            )}
          </div>
          {icon && (
            <div style={{
              width: '48px',
              height: '48px',
              borderRadius: borderRadius.lg,
              backgroundColor: selectedColor.light,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontSize: '24px',
            }}>
              {icon}
            </div>
          )}
        </div>
      </Card>
    );
  }
);

StatCard.displayName = 'StatCard';
