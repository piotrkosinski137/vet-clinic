import { HTMLAttributes, forwardRef } from 'react';
import { colors } from '../../theme';

export type SpinnerSize = 'sm' | 'md' | 'lg';

export interface SpinnerProps extends HTMLAttributes<HTMLDivElement> {
  size?: SpinnerSize;
  color?: string;
}

const sizeMap: Record<SpinnerSize, string> = {
  sm: '16px',
  md: '24px',
  lg: '40px',
};

/**
 * Spinner component - loading indicator.
 * Use this for consistent loading states across the app.
 */
export const Spinner = forwardRef<HTMLDivElement, SpinnerProps>(
  ({ size = 'md', color = colors.primary.main, style, ...props }, ref) => {
    const spinnerSize = sizeMap[size];

    const spinnerStyle: React.CSSProperties = {
      width: spinnerSize,
      height: spinnerSize,
      border: `2px solid ${colors.neutral.border}`,
      borderTopColor: color,
      borderRadius: '50%',
      animation: 'spin 0.8s linear infinite',
      ...style,
    };

    return (
      <>
        <style>
          {`@keyframes spin { to { transform: rotate(360deg); } }`}
        </style>
        <div ref={ref} style={spinnerStyle} {...props} />
      </>
    );
  }
);

Spinner.displayName = 'Spinner';

// Loading wrapper for centered spinner with text
export interface LoadingProps extends HTMLAttributes<HTMLDivElement> {
  text?: string;
  size?: SpinnerSize;
}

export const Loading = forwardRef<HTMLDivElement, LoadingProps>(
  ({ text = 'Loading...', size = 'md', style, ...props }, ref) => (
    <div
      ref={ref}
      style={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        padding: '40px',
        color: colors.neutral.textLight,
        gap: '12px',
        ...style,
      }}
      {...props}
    >
      <Spinner size={size} />
      {text && <span>{text}</span>}
    </div>
  )
);

Loading.displayName = 'Loading';
