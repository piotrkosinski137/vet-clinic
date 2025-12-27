import { HTMLAttributes, forwardRef, ReactNode } from 'react';
import { colors, spacing } from '../../theme';

export interface PageHeaderProps extends HTMLAttributes<HTMLDivElement> {
  title: string;
  actions?: ReactNode;
}

/**
 * PageHeader component - consistent page title with optional actions.
 * Use this at the top of each page for consistent layout.
 */
export const PageHeader = forwardRef<HTMLDivElement, PageHeaderProps>(
  ({ title, actions, style, ...props }, ref) => {
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
        <h1 style={{ margin: 0, color: colors.secondary.main }}>{title}</h1>
        {actions && <div style={{ display: 'flex', gap: spacing.sm }}>{actions}</div>}
      </div>
    );
  }
);

PageHeader.displayName = 'PageHeader';
