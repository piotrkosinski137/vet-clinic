import { HTMLAttributes, forwardRef } from 'react';
import { spacing } from '../../theme';

export interface GridProps extends HTMLAttributes<HTMLDivElement> {
  columns?: number | string;
  gap?: keyof typeof spacing;
  minWidth?: string;
}

/**
 * Grid component - responsive grid layout.
 * Use this for consistent grid layouts across the app.
 */
export const Grid = forwardRef<HTMLDivElement, GridProps>(
  ({ columns, gap = 'md', minWidth = '300px', style, children, ...props }, ref) => {
    const gridTemplateColumns = columns
      ? typeof columns === 'number'
        ? `repeat(${columns}, 1fr)`
        : columns
      : `repeat(auto-fill, minmax(${minWidth}, 1fr))`;

    return (
      <div
        ref={ref}
        style={{
          display: 'grid',
          gridTemplateColumns,
          gap: spacing[gap],
          ...style,
        }}
        {...props}
      >
        {children}
      </div>
    );
  }
);

Grid.displayName = 'Grid';
