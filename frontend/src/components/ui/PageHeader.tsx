import { HTMLAttributes, forwardRef, ReactNode } from 'react';
import { colors, spacing } from '../../theme';

export interface PageHeaderProps extends Omit<HTMLAttributes<HTMLDivElement>, 'title'> {
  title: ReactNode;
  subtitle?: ReactNode;
  actions?: ReactNode;
}

/**
 * PageHeader component - consistent page title with optional actions.
 * Use this at the top of each page for consistent layout.
 */
export const PageHeader = forwardRef<HTMLDivElement, PageHeaderProps>(
  ({ title, subtitle, actions, style, ...props }, ref) => {
    return (
      <div
        ref={ref}
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          marginBottom: spacing.lg,
          ...style,
        }}
        {...props}
      >
        <div>
          <h1 style={{ margin: 0, color: colors.secondary.main }}>{title}</h1>
          {subtitle && <p style={{ margin: `${spacing.xs} 0 0 0`, color: colors.neutral.textMuted }}>{subtitle}</p>}
        </div>
        {actions && <div style={{ display: 'flex', gap: spacing.sm }}>{actions}</div>}
      </div>
    );
  }
);

PageHeader.displayName = 'PageHeader';
