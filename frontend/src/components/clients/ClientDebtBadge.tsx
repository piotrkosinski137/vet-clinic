import { useState, useEffect } from 'react';
import { api, ClientDebtResponse } from '../../api';
import { Badge, Text } from '../ui';
import { colors, spacing, borderRadius, fontWeight, fontSize } from '../../theme';

interface ClientDebtBadgeProps {
  clientId: string;
}

export function ClientDebtBadge({ clientId }: ClientDebtBadgeProps) {
  const [debt, setDebt] = useState<ClientDebtResponse | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    api
      .getClientDebt(clientId)
      .then((data) => {
        if (!cancelled) setDebt(data);
      })
      .catch(() => {
        if (!cancelled) setDebt(null);
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });

    return () => {
      cancelled = true;
    };
  }, [clientId]);

  if (loading) {
    return (
      <div
        style={{
          padding: spacing.sm,
          backgroundColor: colors.neutral.background,
          borderRadius: borderRadius.sm,
          marginTop: spacing.sm,
        }}
      >
        <Text variant="muted" size="sm">Loading billing info...</Text>
      </div>
    );
  }

  if (!debt) {
    return null;
  }

  const hasDebt = debt.totalOutstanding > 0;
  const hasOverdue = debt.overdueInvoiceCount > 0;

  if (!hasDebt) {
    return (
      <div
        style={{
          display: 'flex',
          alignItems: 'center',
          gap: spacing.sm,
          padding: spacing.sm,
          backgroundColor: colors.success.light,
          borderRadius: borderRadius.sm,
          marginTop: spacing.sm,
          borderLeft: `3px solid ${colors.success.main}`,
        }}
      >
        <span style={{ fontSize: fontSize.md }}>✅</span>
        <div>
          <Text size="sm" style={{ fontWeight: fontWeight.medium, color: colors.success.main }}>
            No Outstanding Debt
          </Text>
          {debt.totalPaid > 0 && (
            <Text variant="muted" size="sm">
              ${debt.totalPaid.toFixed(2)} paid to date
            </Text>
          )}
        </div>
      </div>
    );
  }

  return (
    <div
      style={{
        padding: spacing.sm,
        backgroundColor: hasOverdue ? colors.danger.light : colors.warning.light,
        borderRadius: borderRadius.sm,
        marginTop: spacing.sm,
        borderLeft: `3px solid ${hasOverdue ? colors.danger.main : colors.warning.main}`,
      }}
    >
      {/* Debt Header */}
      <div style={{ display: 'flex', alignItems: 'center', gap: spacing.sm, marginBottom: spacing.xs }}>
        <span style={{ fontSize: fontSize.md }}>{hasOverdue ? '⚠️' : '💰'}</span>
        <Text size="sm" style={{ fontWeight: fontWeight.semibold }}>
          Billing Status
        </Text>
      </div>

      {/* Outstanding Amount */}
      <div
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          backgroundColor: colors.neutral.white,
          padding: spacing.sm,
          borderRadius: borderRadius.sm,
          marginBottom: spacing.xs,
        }}
      >
        <Text size="sm">Outstanding</Text>
        <Text
          size="sm"
          style={{
            fontWeight: fontWeight.bold,
            color: hasOverdue ? colors.danger.main : colors.warning.main,
          }}
        >
          ${debt.totalOutstanding.toFixed(2)}
        </Text>
      </div>

      {/* Invoice Stats */}
      <div style={{ display: 'flex', gap: spacing.xs, flexWrap: 'wrap' }}>
        <Badge variant={hasOverdue ? 'danger' : 'warning'}>
          {debt.unpaidInvoiceCount} unpaid
        </Badge>
        {hasOverdue && (
          <Badge variant="danger">
            {debt.overdueInvoiceCount} overdue
          </Badge>
        )}
      </div>

      {/* Summary */}
      <div style={{ marginTop: spacing.sm, paddingTop: spacing.sm, borderTop: `1px solid ${colors.neutral.border}` }}>
        <div style={{ display: 'flex', justifyContent: 'space-between' }}>
          <Text variant="muted" size="sm">Total Invoiced</Text>
          <Text size="sm">${debt.totalInvoiced.toFixed(2)}</Text>
        </div>
        <div style={{ display: 'flex', justifyContent: 'space-between' }}>
          <Text variant="muted" size="sm">Total Paid</Text>
          <Text size="sm" style={{ color: colors.success.main }}>${debt.totalPaid.toFixed(2)}</Text>
        </div>
      </div>
    </div>
  );
}
