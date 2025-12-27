/**
 * Design tokens - single source of truth for all UI values.
 * Change these values to update the entire application appearance.
 *
 * Color palette inspired by modern veterinary/medical applications
 * with warm, professional tones.
 */

export const colors = {
  // Brand colors - Teal/Ocean inspired for veterinary care
  primary: {
    main: '#0891b2',      // Cyan-600 - calming, professional
    hover: '#0e7490',     // Cyan-700
    light: '#ecfeff',     // Cyan-50
    lighter: '#cffafe',   // Cyan-100
    contrast: '#ffffff',
    gradient: 'linear-gradient(135deg, #0891b2 0%, #06b6d4 100%)',
  },

  // Secondary - Deep slate for text and accents
  secondary: {
    main: '#1e293b',      // Slate-800
    hover: '#334155',     // Slate-700
    light: '#f1f5f9',     // Slate-100
    lighter: '#e2e8f0',   // Slate-200
    contrast: '#ffffff',
    gradient: 'linear-gradient(180deg, #1e293b 0%, #0f172a 100%)',
  },

  // Status colors
  danger: {
    main: '#dc2626',      // Red-600
    hover: '#b91c1c',     // Red-700
    light: '#fef2f2',     // Red-50
    lighter: '#fee2e2',   // Red-100
    contrast: '#ffffff',
  },
  success: {
    main: '#16a34a',      // Green-600
    hover: '#15803d',     // Green-700
    light: '#f0fdf4',     // Green-50
    lighter: '#dcfce7',   // Green-100
    contrast: '#ffffff',
  },
  warning: {
    main: '#d97706',      // Amber-600
    hover: '#b45309',     // Amber-700
    light: '#fffbeb',     // Amber-50
    lighter: '#fef3c7',   // Amber-100
    contrast: '#ffffff',
  },
  info: {
    main: '#2563eb',      // Blue-600
    hover: '#1d4ed8',     // Blue-700
    light: '#eff6ff',     // Blue-50
    lighter: '#dbeafe',   // Blue-100
    contrast: '#ffffff',
  },

  // Neutral colors - Warm gray tones
  neutral: {
    white: '#ffffff',
    background: '#f8fafc',      // Slate-50 - subtle warm background
    backgroundAlt: '#f1f5f9',   // Slate-100 - alternate sections
    surface: '#ffffff',         // Card backgrounds
    surfaceHover: '#f8fafc',    // Card hover state
    border: '#e2e8f0',          // Slate-200
    borderLight: '#f1f5f9',     // Slate-100
    text: '#1e293b',            // Slate-800
    textLight: '#475569',       // Slate-600
    textMuted: '#94a3b8',       // Slate-400
    overlay: 'rgba(15, 23, 42, 0.5)', // Slate-900 with opacity
  },

  // Accent colors for visual interest
  accent: {
    purple: '#8b5cf6',    // Violet-500
    pink: '#ec4899',      // Pink-500
    orange: '#f97316',    // Orange-500
    teal: '#14b8a6',      // Teal-500
  },

  // Gradients for backgrounds and highlights
  gradients: {
    // Main page background - subtle gradient
    pageBackground: 'linear-gradient(180deg, #f8fafc 0%, #f1f5f9 100%)',

    // Sidebar gradient - professional dark
    sidebar: 'linear-gradient(180deg, #1e293b 0%, #0f172a 100%)',

    // Card highlight gradient
    cardHighlight: 'linear-gradient(135deg, #ecfeff 0%, #f0fdf4 100%)',

    // Header accent gradient
    headerAccent: 'linear-gradient(90deg, #0891b2 0%, #14b8a6 100%)',

    // Success gradient
    success: 'linear-gradient(135deg, #16a34a 0%, #22c55e 100%)',

    // Warning gradient
    warning: 'linear-gradient(135deg, #d97706 0%, #f59e0b 100%)',

    // Danger gradient
    danger: 'linear-gradient(135deg, #dc2626 0%, #ef4444 100%)',
  },
} as const;

export const spacing = {
  xs: '4px',
  sm: '8px',
  md: '16px',
  lg: '24px',
  xl: '32px',
  xxl: '48px',
  xxxl: '64px',
} as const;

export const borderRadius = {
  xs: '2px',
  sm: '4px',
  md: '8px',
  lg: '12px',
  xl: '16px',
  xxl: '24px',
  full: '9999px',
} as const;

export const fontSize = {
  xs: '12px',
  sm: '14px',
  md: '16px',
  lg: '18px',
  xl: '24px',
  xxl: '32px',
  xxxl: '40px',
} as const;

export const fontWeight = {
  normal: 400,
  medium: 500,
  semibold: 600,
  bold: 700,
} as const;

export const fontFamily = {
  // System font stack for best performance and native feel
  sans: "-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif",
  mono: "ui-monospace, SFMono-Regular, 'SF Mono', Menlo, Consolas, monospace",
} as const;

export const lineHeight = {
  tight: '1.25',
  normal: '1.5',
  relaxed: '1.75',
} as const;

export const shadows = {
  none: 'none',
  xs: '0 1px 2px rgba(0, 0, 0, 0.04)',
  sm: '0 1px 3px rgba(0, 0, 0, 0.06), 0 1px 2px rgba(0, 0, 0, 0.04)',
  md: '0 4px 6px -1px rgba(0, 0, 0, 0.07), 0 2px 4px -1px rgba(0, 0, 0, 0.04)',
  lg: '0 10px 15px -3px rgba(0, 0, 0, 0.08), 0 4px 6px -2px rgba(0, 0, 0, 0.04)',
  xl: '0 20px 25px -5px rgba(0, 0, 0, 0.08), 0 10px 10px -5px rgba(0, 0, 0, 0.03)',
  // Colored shadows for cards
  primary: '0 4px 14px rgba(8, 145, 178, 0.15)',
  success: '0 4px 14px rgba(22, 163, 74, 0.15)',
  warning: '0 4px 14px rgba(217, 119, 6, 0.15)',
  danger: '0 4px 14px rgba(220, 38, 38, 0.15)',
  // Inner shadow for inputs
  inner: 'inset 0 2px 4px rgba(0, 0, 0, 0.04)',
} as const;

export const transitions = {
  fast: '0.1s ease',
  normal: '0.2s ease',
  slow: '0.3s ease',
  // Specific transitions
  colors: 'color 0.2s ease, background-color 0.2s ease, border-color 0.2s ease',
  transform: 'transform 0.2s ease',
  all: 'all 0.2s ease',
} as const;

export const breakpoints = {
  sm: '640px',
  md: '768px',
  lg: '1024px',
  xl: '1280px',
  xxl: '1536px',
} as const;

export const zIndex = {
  base: 0,
  dropdown: 100,
  sticky: 200,
  fixed: 300,
  modalBackdrop: 400,
  modal: 500,
  popover: 600,
  tooltip: 700,
  toast: 800,
} as const;

// Layout constants
export const layout = {
  sidebarWidth: '260px',
  sidebarCollapsedWidth: '72px',
  topBarHeight: '64px',
  maxContentWidth: '1400px',
  cardMaxWidth: '400px',
} as const;

// Theme object for convenience
export const theme = {
  colors,
  spacing,
  borderRadius,
  fontSize,
  fontWeight,
  fontFamily,
  lineHeight,
  shadows,
  transitions,
  breakpoints,
  zIndex,
  layout,
} as const;

export type Theme = typeof theme;
export type Colors = typeof colors;
export type Spacing = keyof typeof spacing;
export type BorderRadius = keyof typeof borderRadius;
export type FontSize = keyof typeof fontSize;
export type FontWeight = keyof typeof fontWeight;
export type Shadow = keyof typeof shadows;
