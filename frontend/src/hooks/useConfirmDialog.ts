import { useState, useCallback } from 'react';

export interface ConfirmDialogState {
  open: boolean;
  title: string;
  message: string;
  onConfirm: () => void;
}

export interface UseConfirmDialogResult {
  /** Current dialog state */
  dialogState: ConfirmDialogState;
  /** Show the confirm dialog */
  showConfirm: (title: string, message: string, onConfirm: () => void) => void;
  /** Close the dialog */
  closeDialog: () => void;
  /** Handle confirm action (calls onConfirm and closes) */
  handleConfirm: () => void;
}

const INITIAL_STATE: ConfirmDialogState = {
  open: false,
  title: '',
  message: '',
  onConfirm: () => {},
};

/**
 * Hook for managing ConfirmDialog state.
 * Replaces window.confirm with a proper UI dialog.
 *
 * @example
 * const { dialogState, showConfirm, closeDialog, handleConfirm } = useConfirmDialog();
 *
 * // In handler:
 * const handleDelete = () => {
 *   showConfirm(
 *     t('common.confirm'),
 *     t('patients.confirmDelete'),
 *     () => deletePatient(id)
 *   );
 * };
 *
 * // In JSX:
 * <ConfirmDialog
 *   open={dialogState.open}
 *   onClose={closeDialog}
 *   onConfirm={handleConfirm}
 *   title={dialogState.title}
 *   message={dialogState.message}
 *   variant="danger"
 * />
 */
export function useConfirmDialog(): UseConfirmDialogResult {
  const [dialogState, setDialogState] = useState<ConfirmDialogState>(INITIAL_STATE);

  const showConfirm = useCallback((title: string, message: string, onConfirm: () => void) => {
    setDialogState({
      open: true,
      title,
      message,
      onConfirm,
    });
  }, []);

  const closeDialog = useCallback(() => {
    setDialogState(INITIAL_STATE);
  }, []);

  const handleConfirm = useCallback(() => {
    dialogState.onConfirm();
    closeDialog();
  }, [dialogState.onConfirm, closeDialog]);

  return {
    dialogState,
    showConfirm,
    closeDialog,
    handleConfirm,
  };
}
