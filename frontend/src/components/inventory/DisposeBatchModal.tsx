import { useState } from 'react';
import { Modal, ModalHeader, ModalTitle, ModalContent, ModalActions, Button, Input, Text } from '../ui';
import type { InventoryBatchResponse } from '../../api/types';
import { useI18n } from '../../i18n';
import { formatCurrency } from '../../hooks';
import { spacing, colors, borderRadius } from '../../theme';

interface Props {
  batch: InventoryBatchResponse;
  onClose: () => void;
  onDispose: (quantity: number, reason: string) => Promise<void>;
}

export function DisposeBatchModal({ batch, onClose, onDispose }: Props) {
  const { t } = useI18n();
  const [quantity, setQuantity] = useState<number>(batch.quantity);
  const [reason, setReason] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [showConfirmation, setShowConfirmation] = useState(false);

  const disposeValue = (batch.unitCost ?? 0) * quantity;

  const handleSubmit = async () => {
    if (!reason.trim()) {
      setError(t('common.required'));
      return;
    }

    if (quantity <= 0 || quantity > batch.quantity) {
      setError(t('stock.disposeQuantityValidation', { min: 0.01, max: batch.quantity }));
      return;
    }

    if (!showConfirmation) {
      setShowConfirmation(true);
      return;
    }

    setLoading(true);
    setError(null);

    try {
      await onDispose(quantity, reason.trim());
    } catch (err) {
      setError(t('errors.failedToSave'));
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal open={true} onClose={onClose}>
      <ModalHeader>
        <ModalTitle>{t('stock.disposeBatchTitle')}</ModalTitle>
      </ModalHeader>
      <ModalContent>
        <Text variant="muted" style={{ marginBottom: spacing.md }}>
          {t('stock.disposeBatchDescription')}
        </Text>

        <div style={{ marginBottom: spacing.md, padding: spacing.sm, backgroundColor: colors.danger.light, borderRadius: '4px' }}>
          <Text style={{ fontWeight: 500 }}>
            {t('stock.item')}: {batch.itemName}
          </Text>
          <Text variant="muted">
            {t('stock.lotNumber')}: {batch.lotNumber || '-'}
          </Text>
          <Text variant="muted">
            {t('stock.quantity')}: {batch.quantity}
          </Text>
          {batch.expirationDate && (
            <Text variant="muted" style={{ color: batch.isExpired ? colors.danger.main : undefined }}>
              {t('stock.expirationDate')}: {new Date(batch.expirationDate).toLocaleDateString()}
              {batch.isExpired && ` (${t('stock.expired')})`}
            </Text>
          )}
        </div>

        <div style={{ marginBottom: spacing.md }}>
          <label style={{ display: 'block', marginBottom: spacing.xs }}>
            {t('stock.disposeQuantity')} *
          </label>
          <Input
            type="number"
            min={0.01}
            max={batch.quantity}
            step="0.01"
            value={quantity}
            onChange={(e) => setQuantity(parseFloat(e.target.value) || 0)}
          />
          <Text variant="muted" size="sm">
            Max: {batch.quantity}
          </Text>
        </div>

        <div style={{ marginBottom: spacing.md }}>
          <label style={{ display: 'block', marginBottom: spacing.xs }}>
            {t('stock.disposeReason')} *
          </label>
          <Input
            value={reason}
            onChange={(e) => setReason(e.target.value)}
            placeholder={t('stock.disposeReasonPlaceholder')}
          />
        </div>

        {/* Total Value Being Disposed */}
        <div style={{
          marginBottom: spacing.md,
          padding: spacing.sm,
          backgroundColor: colors.neutral.background,
          borderRadius: borderRadius.sm,
          border: `1px solid ${colors.neutral.border}`,
        }}>
          <Text style={{ fontWeight: 500 }}>
            {t('stock.totalValue')}: {formatCurrency(disposeValue)}
          </Text>
        </div>

        {/* Confirmation Warning */}
        {showConfirmation && (
          <div style={{
            marginBottom: spacing.md,
            padding: spacing.md,
            backgroundColor: colors.danger.light,
            borderRadius: borderRadius.sm,
            border: `1px solid ${colors.danger.main}`,
          }}>
            <Text style={{ fontWeight: 500, color: colors.danger.main, marginBottom: spacing.xs }}>
              {t('stock.confirmDispose')}
            </Text>
            <Text variant="muted" size="sm">
              {t('stock.disposeWarning')}
            </Text>
          </div>
        )}

        {error && (
          <Text style={{ color: 'red', marginTop: spacing.sm }}>{error}</Text>
        )}
      </ModalContent>
      <ModalActions>
        <Button variant="secondary" onClick={onClose} disabled={loading}>
          {t('common.cancel')}
        </Button>
        <Button variant="danger" onClick={handleSubmit} disabled={loading || !reason.trim() || quantity <= 0}>
          {loading ? t('common.saving') : showConfirmation ? t('stock.confirmAction') : t('stock.disposeBatch')}
        </Button>
      </ModalActions>
    </Modal>
  );
}
