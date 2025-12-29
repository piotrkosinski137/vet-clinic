/**
 * Layout style constants and helpers for consistent UI patterns.
 * Eliminates magic numbers and provides reusable style objects.
 */

import { colors, spacing, borderRadius, fontSize, fontWeight } from './tokens';

// ============= Layout Dimensions =============
export const LAYOUT = {
  SIDEBAR_WIDTH: '350px',
  SIDEBAR_NARROW_WIDTH: '260px',
  CONTENT_MIN_HEIGHT: 'calc(100vh - 180px)',
  HEADER_HEIGHT: '64px',
  PAGE_PADDING: spacing.lg,
  MODAL_WIDTH: {
    sm: '400px',
    md: '500px',
    lg: '700px',
    xl: '900px',
  },
} as const;

// ============= Common Style Objects =============

/** Page container with sidebar + content grid */
export const pageGridStyles = {
  twoColumn: {
    display: 'grid' as const,
    gridTemplateColumns: `${LAYOUT.SIDEBAR_WIDTH} 1fr`,
    gap: spacing.md,
    height: LAYOUT.CONTENT_MIN_HEIGHT,
  },
  threeColumn: {
    display: 'grid' as const,
    gridTemplateColumns: `${LAYOUT.SIDEBAR_NARROW_WIDTH} 1fr 1fr`,
    gap: spacing.md,
    height: LAYOUT.CONTENT_MIN_HEIGHT,
  },
} as const;

/** Sidebar/panel container styles */
export const panelStyles = {
  container: {
    display: 'flex' as const,
    flexDirection: 'column' as const,
    backgroundColor: colors.neutral.white,
    borderRadius: borderRadius.md,
    border: `1px solid ${colors.neutral.border}`,
    overflow: 'hidden' as const,
  },
  header: {
    padding: spacing.md,
    borderBottom: `1px solid ${colors.neutral.border}`,
  },
  subHeader: {
    padding: `${spacing.xs} ${spacing.md}`,
    backgroundColor: colors.neutral.background,
    borderBottom: `1px solid ${colors.neutral.border}`,
  },
  content: {
    flex: 1,
    overflowY: 'auto' as const,
    display: 'flex' as const,
    flexDirection: 'column' as const,
  },
  scrollable: {
    flex: 1,
    overflowY: 'auto' as const,
  },
} as const;

/** List item styles for clickable items */
export const listItemStyles = {
  base: {
    padding: spacing.md,
    cursor: 'pointer' as const,
    borderBottom: `1px solid ${colors.neutral.borderLight}`,
    transition: 'background-color 0.15s ease',
  },
  selected: {
    backgroundColor: colors.primary.light,
  },
  hover: {
    backgroundColor: colors.neutral.surfaceHover,
  },
} as const;

/** Card content layouts */
export const cardStyles = {
  centered: {
    padding: spacing.xl,
    textAlign: 'center' as const,
  },
  withPadding: {
    padding: spacing.md,
  },
  flex: {
    flex: 1,
    display: 'flex' as const,
    alignItems: 'center' as const,
    justifyContent: 'center' as const,
    flexDirection: 'column' as const,
  },
  column: {
    display: 'flex' as const,
    flexDirection: 'column' as const,
    overflow: 'hidden' as const,
  },
} as const;

/** Flex row layouts */
export const flexStyles = {
  row: {
    display: 'flex' as const,
    alignItems: 'center' as const,
    gap: spacing.sm,
  },
  rowBetween: {
    display: 'flex' as const,
    justifyContent: 'space-between' as const,
    alignItems: 'center' as const,
  },
  rowEnd: {
    display: 'flex' as const,
    justifyContent: 'flex-end' as const,
    gap: spacing.xs,
  },
  column: {
    display: 'flex' as const,
    flexDirection: 'column' as const,
    gap: spacing.md,
  },
  wrap: {
    display: 'flex' as const,
    flexWrap: 'wrap' as const,
    gap: spacing.xs,
  },
} as const;

/** Text styles for truncation and overflow */
export const textStyles = {
  truncate: {
    overflow: 'hidden' as const,
    textOverflow: 'ellipsis' as const,
    whiteSpace: 'nowrap' as const,
  },
  bold: {
    fontWeight: fontWeight.bold,
  },
  semibold: {
    fontWeight: fontWeight.semibold,
  },
  medium: {
    fontWeight: fontWeight.medium,
  },
} as const;

/** Avatar/icon circle styles */
export const avatarStyles = {
  sm: {
    width: '32px',
    height: '32px',
    borderRadius: borderRadius.full,
    display: 'flex' as const,
    alignItems: 'center' as const,
    justifyContent: 'center' as const,
    fontSize: fontSize.sm,
    flexShrink: 0,
  },
  md: {
    width: '40px',
    height: '40px',
    borderRadius: borderRadius.full,
    display: 'flex' as const,
    alignItems: 'center' as const,
    justifyContent: 'center' as const,
    fontSize: fontSize.md,
    flexShrink: 0,
  },
  lg: {
    width: '48px',
    height: '48px',
    borderRadius: borderRadius.full,
    display: 'flex' as const,
    alignItems: 'center' as const,
    justifyContent: 'center' as const,
    fontSize: fontSize.lg,
    flexShrink: 0,
  },
} as const;

/** Pagination container styles */
export const paginationStyles = {
  container: {
    display: 'flex' as const,
    justifyContent: 'center' as const,
    alignItems: 'center' as const,
    gap: spacing.sm,
    padding: spacing.md,
    borderTop: `1px solid ${colors.neutral.border}`,
    backgroundColor: colors.neutral.background,
  },
} as const;

/** Table styles */
export const tableStyles = {
  header: {
    padding: spacing.sm,
    backgroundColor: colors.neutral.background,
    fontWeight: fontWeight.semibold,
    fontSize: fontSize.sm,
    borderBottom: `2px solid ${colors.neutral.border}`,
    textAlign: 'left' as const,
  },
  cell: {
    padding: spacing.sm,
    borderBottom: `1px solid ${colors.neutral.borderLight}`,
  },
  row: {
    cursor: 'pointer' as const,
    transition: 'background-color 0.15s ease',
  },
  rowHover: {
    backgroundColor: colors.neutral.surfaceHover,
  },
} as const;

/** Empty state styles */
export const emptyStateStyles = {
  container: {
    padding: spacing.xl,
    textAlign: 'center' as const,
    color: colors.neutral.textMuted,
  },
  icon: {
    fontSize: '48px',
    marginBottom: spacing.md,
  },
} as const;

/** RODO/consent badge colors */
export const consentBadgeColors = {
  granted: colors.success.main,
  pending: colors.warning.main,
  none: colors.neutral.textMuted,
} as const;

// ============= Helper Functions =============

/** Get background color based on selection state */
export function getListItemBackground(isSelected: boolean): string {
  return isSelected ? colors.primary.light : 'transparent';
}

/** Create a styled avatar with background color */
export function createAvatarStyle(bgColor: string, size: 'sm' | 'md' | 'lg' = 'sm') {
  return {
    ...avatarStyles[size],
    backgroundColor: bgColor,
  };
}

/** Grid template for 2-column detail view */
export const detailGridStyles = {
  twoColumn: {
    display: 'grid' as const,
    gridTemplateColumns: '1fr 1fr',
    gap: spacing.md,
    flex: 1,
    overflow: 'hidden' as const,
  },
} as const;
