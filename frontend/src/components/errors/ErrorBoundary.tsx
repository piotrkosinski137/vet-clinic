import { Component, ErrorInfo, ReactNode } from 'react';
import { colors, spacing, borderRadius } from '../../theme';
import pl from '../../i18n/locales/pl.json';
import en from '../../i18n/locales/en.json';

interface Props {
  children: ReactNode;
  fallback?: ReactNode;
}

interface State {
  hasError: boolean;
  error: Error | null;
}

function getTranslations() {
  const storedLang = typeof window !== 'undefined' ? localStorage.getItem('vetclinic-language') : null;
  return storedLang === 'en' ? en : pl;
}

/**
 * Error boundary component that catches JavaScript errors in child components.
 * Displays a fallback UI instead of crashing the entire application.
 */
export class ErrorBoundary extends Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = { hasError: false, error: null };
  }

  static getDerivedStateFromError(error: Error): State {
    return { hasError: true, error };
  }

  componentDidCatch(error: Error, errorInfo: ErrorInfo): void {
    // Log error in development only
    if (import.meta.env.DEV) {
      // eslint-disable-next-line no-console
      console.error('ErrorBoundary caught an error:', error, errorInfo);
    }
    // TODO: In production, send to error tracking service (e.g., Sentry)
  }

  handleRetry = (): void => {
    this.setState({ hasError: false, error: null });
  };

  render(): ReactNode {
    if (this.state.hasError) {
      if (this.props.fallback) {
        return this.props.fallback;
      }

      var translations = getTranslations();

      return (
        <div style={containerStyle}>
          <div style={cardStyle}>
            <div style={iconStyle}>!</div>
            <h2 style={titleStyle}>{translations.errors.somethingWentWrong}</h2>
            <p style={messageStyle}>
              {this.state.error?.message || translations.errors.unexpectedError}
            </p>
            <button style={buttonStyle} onClick={this.handleRetry}>
              {translations.errors.tryAgain}
            </button>
          </div>
        </div>
      );
    }

    return this.props.children;
  }
}

const containerStyle: React.CSSProperties = {
  display: 'flex',
  justifyContent: 'center',
  alignItems: 'center',
  minHeight: '400px',
  padding: spacing.xl,
};

const cardStyle: React.CSSProperties = {
  backgroundColor: colors.neutral.white,
  borderRadius: borderRadius.lg,
  padding: spacing.xxl,
  textAlign: 'center',
  maxWidth: '400px',
  boxShadow: '0 4px 6px rgba(0, 0, 0, 0.1)',
};

const iconStyle: React.CSSProperties = {
  width: '64px',
  height: '64px',
  borderRadius: '50%',
  backgroundColor: colors.danger.light,
  color: colors.danger.main,
  fontSize: '32px',
  fontWeight: 700,
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
  margin: '0 auto',
  marginBottom: spacing.lg,
};

const titleStyle: React.CSSProperties = {
  color: colors.neutral.text,
  fontSize: '20px',
  fontWeight: 600,
  marginBottom: spacing.md,
};

const messageStyle: React.CSSProperties = {
  color: colors.neutral.textLight,
  fontSize: '14px',
  marginBottom: spacing.lg,
  lineHeight: 1.5,
};

const buttonStyle: React.CSSProperties = {
  backgroundColor: colors.primary.main,
  color: colors.neutral.white,
  border: 'none',
  borderRadius: borderRadius.md,
  padding: `${spacing.sm} ${spacing.lg}`,
  fontSize: '14px',
  fontWeight: 500,
  cursor: 'pointer',
};
