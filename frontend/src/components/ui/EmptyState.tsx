import { forwardRef, HTMLAttributes, ReactNode } from 'react';
import { colors, spacing, fontSize, fontWeight } from '../../theme';

export interface EmptyStateProps extends HTMLAttributes<HTMLDivElement> {
  icon?: string;
  title: string;
  description?: string;
  action?: ReactNode;
}

/**
 * EmptyState component - displays when no data is available.
 * Provides a consistent, user-friendly empty state across the application.
 */
export const EmptyState = forwardRef<HTMLDivElement, EmptyStateProps>(
  ({ icon, title, description, action, style, ...props }, ref) => {
    return (
      <div
        ref={ref}
        style={{
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'center',
          padding: spacing.xl,
          textAlign: 'center',
          ...style,
        }}
        {...props}
      >
        {icon && (
          <span style={{ fontSize: '3rem', marginBottom: spacing.md }}>
            {icon}
          </span>
        )}
        <h3 style={{
          margin: 0,
          marginBottom: spacing.xs,
          fontSize: fontSize.lg,
          fontWeight: fontWeight.medium,
          color: colors.neutral.text,
        }}>
          {title}
        </h3>
        {description && (
          <p style={{
            margin: 0,
            marginBottom: action ? spacing.md : 0,
            fontSize: fontSize.sm,
            color: colors.neutral.textMuted,
          }}>
            {description}
          </p>
        )}
        {action}
      </div>
    );
  }
);

EmptyState.displayName = 'EmptyState';
