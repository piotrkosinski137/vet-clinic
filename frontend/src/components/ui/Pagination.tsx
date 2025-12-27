import { forwardRef, HTMLAttributes } from 'react';
import { colors, spacing, fontSize } from '../../theme';
import { Button } from './Button';
import { PAGINATION } from '../../constants';

export interface PaginationProps extends Omit<HTMLAttributes<HTMLDivElement>, 'onChange'> {
  currentPage: number;
  totalItems: number;
  pageSize?: number;
  onPageChange: (page: number) => void;
  showInfo?: boolean;
}

/**
 * Pagination component - provides page navigation controls.
 * Automatically hides when there's only one page or less.
 */
export const Pagination = forwardRef<HTMLDivElement, PaginationProps>(
  ({ currentPage, totalItems, pageSize = PAGINATION.DEFAULT_PAGE_SIZE, onPageChange, showInfo = true, style, ...props }, ref) => {
    const totalPages = Math.ceil(totalItems / pageSize);
    const startItem = currentPage * pageSize + 1;
    const endItem = Math.min((currentPage + 1) * pageSize, totalItems);

    if (totalPages <= 1) return null;

    return (
      <div
        ref={ref}
        style={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          padding: spacing.sm,
          borderTop: `1px solid ${colors.neutral.border}`,
          ...style,
        }}
        {...props}
      >
        {showInfo && (
          <span style={{ fontSize: fontSize.sm, color: colors.neutral.textMuted }}>
            {startItem}-{endItem} of {totalItems}
          </span>
        )}
        <div style={{ display: 'flex', gap: spacing.xs }}>
          <Button
            variant="ghost"
            size="sm"
            onClick={() => onPageChange(currentPage - 1)}
            disabled={currentPage === 0}
          >
            ←
          </Button>
          <span style={{
            display: 'flex',
            alignItems: 'center',
            padding: `0 ${spacing.sm}`,
            fontSize: fontSize.sm
          }}>
            {currentPage + 1} / {totalPages}
          </span>
          <Button
            variant="ghost"
            size="sm"
            onClick={() => onPageChange(currentPage + 1)}
            disabled={currentPage >= totalPages - 1}
          >
            →
          </Button>
        </div>
      </div>
    );
  }
);

Pagination.displayName = 'Pagination';
