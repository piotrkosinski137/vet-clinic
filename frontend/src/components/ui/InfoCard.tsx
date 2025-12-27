import { forwardRef, HTMLAttributes, ReactNode } from 'react';
import { colors, spacing, fontSize, fontWeight } from '../../theme';

export interface InfoCardProps extends HTMLAttributes<HTMLDivElement> {
  label: string;
  value: ReactNode;
  icon?: string;
  variant?: 'default' | 'compact';
}

/**
 * InfoCard component - displays key-value information.
 * Use this for showing structured data like client details, stats, etc.
 */
export const InfoCard = forwardRef<HTMLDivElement, InfoCardProps>(
  ({ label, value, icon, variant = 'default', style, ...props }, ref) => {
    const isCompact = variant === 'compact';

    return (
      <div
        ref={ref}
        style={{
          display: 'flex',
          flexDirection: isCompact ? 'row' : 'column',
          gap: isCompact ? spacing.sm : spacing.xs,
          padding: isCompact ? spacing.xs : spacing.sm,
          ...style,
        }}
        {...props}
      >
        <span style={{
          fontSize: fontSize.sm,
          color: colors.neutral.textMuted,
          fontWeight: fontWeight.medium,
        }}>
          {icon && <span style={{ marginRight: spacing.xs }}>{icon}</span>}
          {label}
        </span>
        <span style={{
          fontSize: isCompact ? fontSize.sm : fontSize.md,
          color: colors.neutral.text,
        }}>
          {value || '-'}
        </span>
      </div>
    );
  }
);

InfoCard.displayName = 'InfoCard';
