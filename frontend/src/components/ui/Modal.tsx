import { HTMLAttributes, forwardRef, useEffect } from 'react';
import { colors, spacing, borderRadius, zIndex } from '../../theme';

export interface ModalProps extends HTMLAttributes<HTMLDivElement> {
  open: boolean;
  onClose: () => void;
  maxWidth?: string;
}

/**
 * Modal component - dialog overlay.
 * Use this for consistent modal styling across the app.
 */
export const Modal = forwardRef<HTMLDivElement, ModalProps>(
  ({ open, onClose, maxWidth = '500px', style, children, ...props }, ref) => {
    // Close on escape key
    useEffect(() => {
      const handleEscape = (e: KeyboardEvent) => {
        if (e.key === 'Escape' && open) {
          onClose();
        }
      };
      document.addEventListener('keydown', handleEscape);
      return () => document.removeEventListener('keydown', handleEscape);
    }, [open, onClose]);

    // Prevent body scroll when modal is open
    useEffect(() => {
      if (open) {
        document.body.style.overflow = 'hidden';
      } else {
        document.body.style.overflow = '';
      }
      return () => {
        document.body.style.overflow = '';
      };
    }, [open]);

    if (!open) return null;

    const overlayStyle: React.CSSProperties = {
      position: 'fixed',
      top: 0,
      left: 0,
      right: 0,
      bottom: 0,
      backgroundColor: colors.neutral.overlay,
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      zIndex: zIndex.modal,
      padding: spacing.md,
    };

    const contentStyle: React.CSSProperties = {
      backgroundColor: colors.neutral.white,
      borderRadius: borderRadius.md,
      padding: spacing.lg,
      width: '100%',
      maxWidth,
      maxHeight: '90vh',
      overflowY: 'auto',
      ...style,
    };

    return (
      <div style={overlayStyle} onClick={onClose}>
        <div ref={ref} style={contentStyle} onClick={(e) => e.stopPropagation()} {...props}>
          {children}
        </div>
      </div>
    );
  }
);

Modal.displayName = 'Modal';

// Modal sub-components
export interface ModalHeaderProps extends HTMLAttributes<HTMLDivElement> {}

export const ModalHeader = forwardRef<HTMLDivElement, ModalHeaderProps>(
  ({ style, children, ...props }, ref) => (
    <div
      ref={ref}
      style={{
        marginBottom: spacing.md,
        ...style,
      }}
      {...props}
    >
      {children}
    </div>
  )
);

ModalHeader.displayName = 'ModalHeader';

export interface ModalTitleProps extends HTMLAttributes<HTMLHeadingElement> {}

export const ModalTitle = forwardRef<HTMLHeadingElement, ModalTitleProps>(
  ({ style, children, ...props }, ref) => (
    <h2
      ref={ref}
      style={{
        margin: 0,
        color: colors.secondary.main,
        ...style,
      }}
      {...props}
    >
      {children}
    </h2>
  )
);

ModalTitle.displayName = 'ModalTitle';

export interface ModalContentProps extends HTMLAttributes<HTMLDivElement> {}

export const ModalContent = forwardRef<HTMLDivElement, ModalContentProps>(
  ({ style, children, ...props }, ref) => (
    <div ref={ref} style={style} {...props}>
      {children}
    </div>
  )
);

ModalContent.displayName = 'ModalContent';

export interface ModalActionsProps extends HTMLAttributes<HTMLDivElement> {}

export const ModalActions = forwardRef<HTMLDivElement, ModalActionsProps>(
  ({ style, children, ...props }, ref) => (
    <div
      ref={ref}
      style={{
        display: 'flex',
        gap: spacing.sm,
        justifyContent: 'flex-end',
        marginTop: spacing.lg,
        ...style,
      }}
      {...props}
    >
      {children}
    </div>
  )
);

ModalActions.displayName = 'ModalActions';
