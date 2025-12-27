import { HTMLAttributes, forwardRef } from 'react';
import { colors, fontSize as fontSizes, fontWeight as fontWeights } from '../../theme';

export type TextVariant = 'body' | 'caption' | 'muted';
export type TextSize = 'xs' | 'sm' | 'md' | 'lg' | 'xl';

export interface TextProps extends HTMLAttributes<HTMLParagraphElement> {
  variant?: TextVariant;
  size?: TextSize;
  weight?: keyof typeof fontWeights;
  as?: 'p' | 'span' | 'div';
}

const variantStyles: Record<TextVariant, React.CSSProperties> = {
  body: {
    color: colors.neutral.text,
  },
  caption: {
    color: colors.neutral.textLight,
  },
  muted: {
    color: colors.neutral.textMuted,
  },
};

/**
 * Text component - typography wrapper.
 * Use this for consistent text styling across the app.
 */
export const Text = forwardRef<HTMLParagraphElement, TextProps>(
  (
    { variant = 'body', size = 'md', weight = 'normal', as: Component = 'p', style, children, ...props },
    ref
  ) => {
    const baseStyle: React.CSSProperties = {
      margin: 0,
      fontSize: fontSizes[size],
      fontWeight: fontWeights[weight],
      lineHeight: 1.6,
      ...variantStyles[variant],
      ...style,
    };

    // Only pass ref when Component is 'p' (default), otherwise omit it
    // This is type-safe because the ref is typed for HTMLParagraphElement
    if (Component === 'p') {
      return (
        <Component ref={ref} style={baseStyle} {...props}>
          {children}
        </Component>
      );
    }

    return (
      <Component style={baseStyle} {...props}>
        {children}
      </Component>
    );
  }
);

Text.displayName = 'Text';
