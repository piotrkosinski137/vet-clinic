import { useState } from 'react';
import { Modal, ModalHeader, ModalTitle, ModalContent, ModalActions, Button, Input, Text } from '../ui';
import type { InventoryBatchResponse } from '../../api/types';
import { useI18n } from '../../i18n';
import { spacing, colors, borderRadius } from '../../theme';

interface Props {
  batch: InventoryBatchResponse;
  onClose: () => void;
  onComplete: (lotNumber: string, expirationDate?: string) => Promise<void>;
}

export function CompleteBatchModal({ batch, onClose, onComplete }: Props) {
  const { t } = useI18n();
  const [lotNumber, setLotNumber] = useState('');
  const [expirationDate, setExpirationDate] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [showConfirmation, setShowConfirmation] = useState(false);

  const handleSubmit = async () => {
    if (!lotNumber.trim()) {
      setError(t('common.required'));
      return;
    }

    if (!showConfirmation) {
      setShowConfirmation(true);
      return;
    }

    setLoading(true);
    setError(null);

    try {
      await onComplete(lotNumber.trim(), expirationDate || undefined);
    } catch (err) {
      setError(t('errors.failedToSave'));
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal open={true} onClose={onClose}>
      <ModalHeader>
        <ModalTitle>{t('stock.completeBatchTitle')}</ModalTitle>
      </ModalHeader>
      <ModalContent>
        <Text variant="muted" style={{ marginBottom: spacing.md }}>
          {t('stock.completeBatchDescription')}
        </Text>

        <div style={{ marginBottom: spacing.md }}>
          <Text style={{ fontWeight: 500, marginBottom: spacing.xs }}>
            {t('stock.item')}: {batch.itemName}
          </Text>
          <Text variant="muted">
            {t('stock.quantity')}: {batch.quantity}
          </Text>
        </div>

        <div style={{ marginBottom: spacing.md }}>
          <label style={{ display: 'block', marginBottom: spacing.xs }}>
            {t('stock.lotNumber')} *
          </label>
          <Input
            value={lotNumber}
            onChange={(e) => setLotNumber(e.target.value)}
            placeholder="LOT-2024-001"
            autoFocus
          />
        </div>

        <div style={{ marginBottom: spacing.md }}>
          <label style={{ display: 'block', marginBottom: spacing.xs }}>
            {t('stock.expirationDate')}
          </label>
          <Input
            type="date"
            value={expirationDate}
            onChange={(e) => setExpirationDate(e.target.value)}
          />
        </div>

        {/* Confirmation Warning */}
        {showConfirmation && (
          <div style={{
            marginBottom: spacing.md,
            padding: spacing.md,
            backgroundColor: colors.warning.light,
            borderRadius: borderRadius.sm,
            border: `1px solid ${colors.warning.main}`,
          }}>
            <Text style={{ fontWeight: 500, color: colors.warning.main, marginBottom: spacing.xs }}>
              {t('stock.confirmComplete')}
            </Text>
            <Text variant="muted" size="sm">
              {t('stock.completeWarning')}
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
        <Button variant="primary" onClick={handleSubmit} disabled={loading || !lotNumber.trim()}>
          {loading ? t('common.saving') : showConfirmation ? t('stock.confirmAction') : t('common.save')}
        </Button>
      </ModalActions>
    </Modal>
  );
}
