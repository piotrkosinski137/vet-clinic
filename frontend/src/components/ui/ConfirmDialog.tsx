import { useState, useEffect } from 'react';
import { Modal, ModalTitle, ModalActions } from './Modal';
import { Button, type ButtonVariant } from './Button';
import { Input } from './Input';
import { FormField } from './FormField';
import { Text } from './Text';
import { spacing } from '../../theme';

export interface ConfirmDialogProps {
  open: boolean;
  onClose: () => void;
  onConfirm: (inputValue?: string) => void;
  title: string;
  message: string;
  confirmLabel?: string;
  cancelLabel?: string;
  variant?: ButtonVariant;
  /** If provided, shows an input field */
  inputLabel?: string;
  inputPlaceholder?: string;
  inputRequired?: boolean;
  loading?: boolean;
}

/**
 * Reusable confirmation dialog component.
 * Replaces window.confirm and window.prompt with proper UI.
 */
export function ConfirmDialog({
  open,
  onClose,
  onConfirm,
  title,
  message,
  confirmLabel = 'Confirm',
  cancelLabel = 'Cancel',
  variant = 'primary',
  inputLabel,
  inputPlaceholder,
  inputRequired = false,
  loading = false,
}: ConfirmDialogProps) {
  const [inputValue, setInputValue] = useState('');

  // Reset input when dialog opens/closes
  useEffect(() => {
    if (!open) {
      setInputValue('');
    }
  }, [open]);

  const handleConfirm = () => {
    if (inputLabel && inputRequired && !inputValue.trim()) {
      return;
    }
    onConfirm(inputLabel ? inputValue : undefined);
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !loading) {
      handleConfirm();
    }
  };

  const isConfirmDisabled = loading || Boolean(inputLabel && inputRequired && !inputValue.trim());

  return (
    <Modal open={open} onClose={onClose} maxWidth="400px">
      <ModalTitle>{title}</ModalTitle>
      <Text style={{ marginTop: spacing.sm, marginBottom: spacing.md }}>
        {message}
      </Text>
      {inputLabel && (
        <FormField label={inputLabel} required={inputRequired}>
          <Input
            value={inputValue}
            onChange={(e) => setInputValue(e.target.value)}
            placeholder={inputPlaceholder}
            onKeyDown={handleKeyDown}
            autoFocus
          />
        </FormField>
      )}
      <ModalActions>
        <Button variant="ghost" onClick={onClose} disabled={loading}>
          {cancelLabel}
        </Button>
        <Button
          variant={variant}
          onClick={handleConfirm}
          disabled={isConfirmDisabled}
        >
          {loading ? 'Processing...' : confirmLabel}
        </Button>
      </ModalActions>
    </Modal>
  );
}
