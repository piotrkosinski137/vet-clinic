import { createContext, useContext, useState, useCallback, ReactNode } from 'react';
import { colors, spacing, borderRadius, shadows, fontSize, fontWeight, zIndex } from '../../theme';

export type ToastVariant = 'success' | 'error' | 'warning' | 'info';

export interface Toast {
  id: string;
  message: string;
  variant: ToastVariant;
  duration?: number;
}

interface ToastContextType {
  toasts: Toast[];
  showToast: (message: string, variant?: ToastVariant, duration?: number) => void;
  success: (message: string, duration?: number) => void;
  error: (message: string, duration?: number) => void;
  warning: (message: string, duration?: number) => void;
  info: (message: string, duration?: number) => void;
  removeToast: (id: string) => void;
}

const ToastContext = createContext<ToastContextType | null>(null);

const DEFAULT_DURATION = 4000;

const variantStyles: Record<ToastVariant, React.CSSProperties> = {
  success: {
    backgroundColor: colors.success.main,
    color: colors.success.contrast,
  },
  error: {
    backgroundColor: colors.danger.main,
    color: colors.danger.contrast,
  },
  warning: {
    backgroundColor: colors.warning.main,
    color: colors.warning.contrast,
  },
  info: {
    backgroundColor: colors.primary.main,
    color: colors.primary.contrast,
  },
};

const variantIcons: Record<ToastVariant, string> = {
  success: '✓',
  error: '✕',
  warning: '⚠',
  info: 'ℹ',
};

export function ToastProvider({ children }: { children: ReactNode }) {
  const [toasts, setToasts] = useState<Toast[]>([]);

  const removeToast = useCallback((id: string) => {
    setToasts((prev) => prev.filter((toast) => toast.id !== id));
  }, []);

  const showToast = useCallback(
    (message: string, variant: ToastVariant = 'info', duration: number = DEFAULT_DURATION) => {
      const id = `toast-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
      const toast: Toast = { id, message, variant, duration };

      setToasts((prev) => [...prev, toast]);

      if (duration > 0) {
        setTimeout(() => removeToast(id), duration);
      }
    },
    [removeToast]
  );

  const success = useCallback(
    (message: string, duration?: number) => showToast(message, 'success', duration),
    [showToast]
  );
  const error = useCallback(
    (message: string, duration?: number) => showToast(message, 'error', duration),
    [showToast]
  );
  const warning = useCallback(
    (message: string, duration?: number) => showToast(message, 'warning', duration),
    [showToast]
  );
  const info = useCallback(
    (message: string, duration?: number) => showToast(message, 'info', duration),
    [showToast]
  );

  return (
    <ToastContext.Provider value={{ toasts, showToast, success, error, warning, info, removeToast }}>
      {children}
      <ToastContainer toasts={toasts} onRemove={removeToast} />
    </ToastContext.Provider>
  );
}

export function useToast(): ToastContextType {
  const context = useContext(ToastContext);
  if (!context) {
    throw new Error('useToast must be used within a ToastProvider');
  }
  return context;
}

interface ToastContainerProps {
  toasts: Toast[];
  onRemove: (id: string) => void;
}

function ToastContainer({ toasts, onRemove }: ToastContainerProps) {
  if (toasts.length === 0) return null;

  const containerStyle: React.CSSProperties = {
    position: 'fixed',
    top: spacing.lg,
    right: spacing.lg,
    zIndex: zIndex.tooltip,
    display: 'flex',
    flexDirection: 'column',
    gap: spacing.sm,
    maxWidth: '400px',
  };

  return (
    <div style={containerStyle}>
      {toasts.map((toast) => (
        <ToastItem key={toast.id} toast={toast} onRemove={onRemove} />
      ))}
    </div>
  );
}

interface ToastItemProps {
  toast: Toast;
  onRemove: (id: string) => void;
}

function ToastItem({ toast, onRemove }: ToastItemProps) {
  const baseStyle: React.CSSProperties = {
    display: 'flex',
    alignItems: 'center',
    gap: spacing.sm,
    padding: `${spacing.sm} ${spacing.md}`,
    borderRadius: borderRadius.md,
    boxShadow: shadows.lg,
    fontSize: fontSize.sm,
    fontWeight: fontWeight.medium,
    animation: 'slideIn 0.3s ease',
    ...variantStyles[toast.variant],
  };

  const iconStyle: React.CSSProperties = {
    fontSize: fontSize.md,
    flexShrink: 0,
  };

  const messageStyle: React.CSSProperties = {
    flex: 1,
  };

  const closeButtonStyle: React.CSSProperties = {
    background: 'none',
    border: 'none',
    color: 'inherit',
    cursor: 'pointer',
    padding: spacing.xs,
    fontSize: fontSize.md,
    opacity: 0.8,
    flexShrink: 0,
  };

  return (
    <div style={baseStyle}>
      <span style={iconStyle}>{variantIcons[toast.variant]}</span>
      <span style={messageStyle}>{toast.message}</span>
      <button
        style={closeButtonStyle}
        onClick={() => onRemove(toast.id)}
        onMouseEnter={(e) => (e.currentTarget.style.opacity = '1')}
        onMouseLeave={(e) => (e.currentTarget.style.opacity = '0.8')}
      >
        ✕
      </button>
    </div>
  );
}
