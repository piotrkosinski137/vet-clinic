import { useState, useEffect, useMemo } from 'react';
import { api } from '../api';
import { useI18n } from '../i18n';
import type { InvoiceResponse, InvoiceStatus, PaymentMethod, ClientResponse } from '../api/types';
import {
  PageHeader,
  Card,
  CardContent,
  Button,
  Badge,
  Text,
  Loading,
  EmptyState,
  Select,
  Input,
  Modal,
  ModalTitle,
  ModalActions,
  FormField,
  Pagination,
  StatCard,
  useToast,
  ConfirmDialog,
} from '../components/ui';
import { colors, spacing, borderRadius } from '../theme';
import type { BadgeVariant } from '../components/ui/Badge';

const INVOICE_STATUSES: InvoiceStatus[] = ['DRAFT', 'ISSUED', 'PAID', 'CANCELLED', 'OVERDUE'];
const PAYMENT_METHODS: PaymentMethod[] = ['CASH', 'CARD', 'TRANSFER', 'OTHER'];
const ITEMS_PER_PAGE = 15;

const statusColors: Record<InvoiceStatus, BadgeVariant> = {
  DRAFT: 'secondary',
  ISSUED: 'primary',
  PAID: 'success',
  CANCELLED: 'danger',
  OVERDUE: 'warning',
};

function formatDate(dateStr?: string, language?: string): string {
  if (!dateStr) return '-';
  return new Date(dateStr).toLocaleDateString(language === 'pl' ? 'pl-PL' : 'en-US');
}

function formatCurrency(amount: number, language?: string): string {
  return new Intl.NumberFormat(language === 'pl' ? 'pl-PL' : 'en-US', { style: 'currency', currency: 'PLN' }).format(amount);
}

export function PaymentsPage() {
  const { t, language } = useI18n();
  const [invoices, setInvoices] = useState<InvoiceResponse[]>([]);
  const [clients, setClients] = useState<ClientResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [statusFilter, setStatusFilter] = useState<InvoiceStatus | ''>('');
  const [page, setPage] = useState(1);
  const [selectedInvoice, setSelectedInvoice] = useState<InvoiceResponse | null>(null);
  const [showPaymentModal, setShowPaymentModal] = useState(false);
  const [actionLoading, setActionLoading] = useState(false);
  const [confirmDialog, setConfirmDialog] = useState<{
    open: boolean;
    type: 'cancel' | 'delete';
    invoice: InvoiceResponse | null;
  }>({ open: false, type: 'cancel', invoice: null });
  const { success, error: showError } = useToast();

  const [paymentForm, setPaymentForm] = useState({
    amount: 0,
    paymentMethod: 'CASH' as PaymentMethod,
    notes: '',
  });

  const fetchData = async () => {
    setLoading(true);
    setError(null);
    try {
      const [invoicesData, clientsData] = await Promise.all([
        api.getInvoices(),
        api.getClients(),
      ]);
      setInvoices(invoicesData);
      setClients(clientsData);
    } catch (err) {
      setError(err instanceof Error ? err.message : t('errors.failedToLoad'));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const stats = useMemo(() => {
    const totalInvoiced = invoices.reduce((sum, inv) => sum + inv.totalAmount, 0);
    const totalPaid = invoices.reduce((sum, inv) => sum + inv.paidAmount, 0);
    const totalOutstanding = totalInvoiced - totalPaid;
    const unpaidCount = invoices.filter(inv => inv.status === 'ISSUED' || inv.status === 'OVERDUE').length;
    const overdueCount = invoices.filter(inv => inv.status === 'OVERDUE').length;
    return { totalInvoiced, totalPaid, totalOutstanding, unpaidCount, overdueCount };
  }, [invoices]);

  const filteredInvoices = useMemo(() => {
    let result = invoices;
    if (statusFilter) {
      result = result.filter(inv => inv.status === statusFilter);
    }
    return result.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
  }, [invoices, statusFilter]);

  const paginatedInvoices = useMemo(() => {
    const start = (page - 1) * ITEMS_PER_PAGE;
    return filteredInvoices.slice(start, start + ITEMS_PER_PAGE);
  }, [filteredInvoices, page]);

  const getClientName = (clientId: string) => {
    const client = clients.find(c => c.id === clientId);
    return client ? `${client.firstName} ${client.lastName}` : clientId.slice(0, 8) + '...';
  };

  const handleIssue = async (invoice: InvoiceResponse) => {
    setActionLoading(true);
    try {
      await api.issueInvoice(invoice.id);
      success(t('success.statusUpdated'));
      fetchData();
    } catch (err) {
      showError(err instanceof Error ? err.message : t('errors.failedToSave'));
    } finally {
      setActionLoading(false);
    }
  };

  const openCancelDialog = (invoice: InvoiceResponse) => {
    setConfirmDialog({ open: true, type: 'cancel', invoice });
  };

  const openDeleteDialog = (invoice: InvoiceResponse) => {
    setConfirmDialog({ open: true, type: 'delete', invoice });
  };

  const closeConfirmDialog = () => {
    setConfirmDialog({ open: false, type: 'cancel', invoice: null });
  };

  const handleConfirmAction = async () => {
    if (!confirmDialog.invoice) return;
    setActionLoading(true);
    try {
      if (confirmDialog.type === 'cancel') {
        await api.cancelInvoice(confirmDialog.invoice.id);
        success(t('success.statusUpdated'));
      } else {
        await api.deleteInvoice(confirmDialog.invoice.id);
        success(t('success.deleted'));
      }
      closeConfirmDialog();
      fetchData();
    } catch (err) {
      showError(err instanceof Error ? err.message : t('errors.failedToSave'));
    } finally {
      setActionLoading(false);
    }
  };

  const openPaymentModal = (invoice: InvoiceResponse) => {
    setSelectedInvoice(invoice);
    setPaymentForm({
      amount: invoice.totalAmount - invoice.paidAmount,
      paymentMethod: 'CASH',
      notes: '',
    });
    setShowPaymentModal(true);
  };

  const handleRecordPayment = async () => {
    if (!selectedInvoice || paymentForm.amount <= 0) {
      showError(t('errors.somethingWentWrong'));
      return;
    }
    setActionLoading(true);
    try {
      await api.recordPayment(selectedInvoice.id, {
        amount: paymentForm.amount,
        paymentMethod: paymentForm.paymentMethod,
        notes: paymentForm.notes || undefined,
      });
      success(t('success.saved'));
      setShowPaymentModal(false);
      fetchData();
    } catch (err) {
      showError(err instanceof Error ? err.message : t('errors.failedToSave'));
    } finally {
      setActionLoading(false);
    }
  };

  return (
    <div>
      <PageHeader title={t('payments.title')} />

      {/* Stats Cards */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(200px, 1fr))', gap: spacing.lg, marginBottom: spacing.xl }}>
        <StatCard
          label={t('payments.totalInvoiced')}
          value={formatCurrency(stats.totalInvoiced, language)}
          icon="📄"
          color="primary"
        />
        <StatCard
          label={t('payments.totalPaid')}
          value={formatCurrency(stats.totalPaid, language)}
          icon="✅"
          color="success"
        />
        <StatCard
          label={t('payments.outstanding')}
          value={formatCurrency(stats.totalOutstanding, language)}
          icon="⏳"
          color="warning"
        />
        <StatCard
          label={t('payments.unpaidInvoices')}
          value={String(stats.unpaidCount)}
          icon="📋"
          color="info"
        />
      </div>

      {/* Filters */}
      <Card variant="default" style={{ marginBottom: spacing.lg }}>
        <CardContent>
          <div style={{ display: 'flex', gap: spacing.md, alignItems: 'flex-end' }}>
            <div style={{ minWidth: '200px' }}>
              <label style={{ display: 'block', marginBottom: spacing.xs, fontSize: '14px' }}>{t('common.status')}</label>
              <Select
                value={statusFilter}
                onChange={(e) => { setStatusFilter(e.target.value as InvoiceStatus | ''); setPage(1); }}
              >
                <option value="">{t('common.allStatuses')}</option>
                {INVOICE_STATUSES.map(s => (
                  <option key={s} value={s}>{t(`payments.statuses.${s}`)}</option>
                ))}
              </Select>
            </div>
            <Button variant="ghost" onClick={() => { setStatusFilter(''); setPage(1); }}>
              {t('common.clear')}
            </Button>
          </div>
        </CardContent>
      </Card>

      {/* Invoices Table */}
      <Card variant="default">
        {loading && (
          <div style={{ padding: spacing.xl, textAlign: 'center' }}>
            <Loading text={t('payments.loading')} />
          </div>
        )}

        {error && (
          <div style={{ padding: spacing.xl, textAlign: 'center', color: colors.danger.main }}>
            <Text>{error}</Text>
            <Button variant="ghost" onClick={fetchData} style={{ marginTop: spacing.md }}>{t('errors.retry')}</Button>
          </div>
        )}

        {!loading && !error && filteredInvoices.length === 0 && (
          <EmptyState
            icon="💰"
            title={t('payments.noInvoices')}
            description={t('payments.noInvoicesHint')}
          />
        )}

        {!loading && !error && filteredInvoices.length > 0 && (
          <>
            <div style={{ overflowX: 'auto' }}>
              <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '14px' }}>
                <thead>
                  <tr style={{ backgroundColor: colors.neutral.background, borderBottom: `1px solid ${colors.neutral.border}` }}>
                    <th style={{ padding: spacing.md, textAlign: 'left' }}>{t('payments.invoiceNumber')}</th>
                    <th style={{ padding: spacing.md, textAlign: 'left' }}>{t('payments.client')}</th>
                    <th style={{ padding: spacing.md, textAlign: 'left' }}>{t('common.status')}</th>
                    <th style={{ padding: spacing.md, textAlign: 'right' }}>{t('common.total')}</th>
                    <th style={{ padding: spacing.md, textAlign: 'right' }}>{t('payments.paid')}</th>
                    <th style={{ padding: spacing.md, textAlign: 'right' }}>{t('payments.outstanding')}</th>
                    <th style={{ padding: spacing.md, textAlign: 'left' }}>{t('common.date')}</th>
                    <th style={{ padding: spacing.md, textAlign: 'left' }}>{t('payments.dueDate')}</th>
                    <th style={{ padding: spacing.md, textAlign: 'right' }}>{t('common.actions')}</th>
                  </tr>
                </thead>
                <tbody>
                  {paginatedInvoices.map(invoice => {
                    const outstanding = invoice.totalAmount - invoice.paidAmount;
                    return (
                      <tr key={invoice.id} style={{ borderBottom: `1px solid ${colors.neutral.borderLight}` }}>
                        <td style={{ padding: spacing.md, fontFamily: 'monospace', fontSize: '12px' }}>
                          {invoice.invoiceNumber}
                        </td>
                        <td style={{ padding: spacing.md }}>{getClientName(invoice.clientId)}</td>
                        <td style={{ padding: spacing.md }}>
                          <Badge variant={statusColors[invoice.status]}>{t(`payments.statuses.${invoice.status}`)}</Badge>
                        </td>
                        <td style={{ padding: spacing.md, textAlign: 'right', fontWeight: 500 }}>
                          {formatCurrency(invoice.totalAmount, language)}
                        </td>
                        <td style={{ padding: spacing.md, textAlign: 'right', color: colors.success.main }}>
                          {formatCurrency(invoice.paidAmount, language)}
                        </td>
                        <td style={{ padding: spacing.md, textAlign: 'right', color: outstanding > 0 ? colors.warning.main : colors.neutral.textMuted }}>
                          {formatCurrency(outstanding, language)}
                        </td>
                        <td style={{ padding: spacing.md }}>{formatDate(invoice.createdAt, language)}</td>
                        <td style={{ padding: spacing.md }}>{formatDate(invoice.dueDate, language)}</td>
                        <td style={{ padding: spacing.md, textAlign: 'right' }}>
                          <div style={{ display: 'flex', gap: spacing.xs, justifyContent: 'flex-end', flexWrap: 'wrap' }}>
                            {invoice.status === 'DRAFT' && (
                              <Button size="sm" variant="primary" onClick={() => handleIssue(invoice)} disabled={actionLoading}>
                                {t('common.issue')}
                              </Button>
                            )}
                            {(invoice.status === 'ISSUED' || invoice.status === 'OVERDUE') && outstanding > 0 && (
                              <Button size="sm" variant="success" onClick={() => openPaymentModal(invoice)} disabled={actionLoading}>
                                {t('common.pay')}
                              </Button>
                            )}
                            {invoice.status !== 'PAID' && invoice.status !== 'CANCELLED' && (
                              <Button size="sm" variant="secondary" onClick={() => openCancelDialog(invoice)} disabled={actionLoading}>
                                {t('common.cancel')}
                              </Button>
                            )}
                            {invoice.status === 'DRAFT' && (
                              <Button size="sm" variant="danger" onClick={() => openDeleteDialog(invoice)} disabled={actionLoading}>
                                {t('common.delete')}
                              </Button>
                            )}
                          </div>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
            {filteredInvoices.length > ITEMS_PER_PAGE && (
              <div style={{ padding: spacing.md, display: 'flex', justifyContent: 'center' }}>
                <Pagination
                  currentPage={page - 1}
                  totalItems={filteredInvoices.length}
                  pageSize={ITEMS_PER_PAGE}
                  onPageChange={(p) => setPage(p + 1)}
                />
              </div>
            )}
          </>
        )}
      </Card>

      {/* Record Payment Modal */}
      <Modal open={showPaymentModal} onClose={() => setShowPaymentModal(false)}>
        <ModalTitle>{t('payments.recordPayment')}</ModalTitle>
        {selectedInvoice && (
          <div style={{ marginBottom: spacing.lg, padding: spacing.md, backgroundColor: colors.neutral.background, borderRadius: borderRadius.md }}>
            <Text variant="caption">{t('payments.invoice')}: {selectedInvoice.invoiceNumber}</Text>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: spacing.sm }}>
              <Text>{t('common.total')}: {formatCurrency(selectedInvoice.totalAmount, language)}</Text>
              <Text>{t('payments.outstanding')}: {formatCurrency(selectedInvoice.totalAmount - selectedInvoice.paidAmount, language)}</Text>
            </div>
          </div>
        )}
        <FormField label={t('common.amount')} required>
          <Input
            type="number"
            step="0.01"
            min="0.01"
            value={paymentForm.amount}
            onChange={(e) => setPaymentForm({ ...paymentForm, amount: parseFloat(e.target.value) || 0 })}
          />
        </FormField>
        <FormField label={t('payments.paymentMethod')} required>
          <Select
            value={paymentForm.paymentMethod}
            onChange={(e) => setPaymentForm({ ...paymentForm, paymentMethod: e.target.value as PaymentMethod })}
          >
            {PAYMENT_METHODS.map(m => (
              <option key={m} value={m}>{t(`payments.methods.${m}`)}</option>
            ))}
          </Select>
        </FormField>
        <FormField label={t('common.notes')}>
          <Input
            value={paymentForm.notes}
            onChange={(e) => setPaymentForm({ ...paymentForm, notes: e.target.value })}
            placeholder={t('payments.optionalNotes')}
          />
        </FormField>
        <ModalActions>
          <Button variant="ghost" onClick={() => setShowPaymentModal(false)}>{t('common.cancel')}</Button>
          <Button variant="primary" onClick={handleRecordPayment} disabled={actionLoading}>
            {actionLoading ? t('common.recording') : t('payments.recordPayment')}
          </Button>
        </ModalActions>
      </Modal>

      {/* Confirm Dialog */}
      <ConfirmDialog
        open={confirmDialog.open}
        onClose={closeConfirmDialog}
        onConfirm={handleConfirmAction}
        title={confirmDialog.type === 'cancel' ? t('common.cancel') : t('common.delete')}
        message={
          confirmDialog.type === 'cancel'
            ? t('payments.confirmCancel')
            : t('payments.confirmDelete')
        }
        confirmLabel={confirmDialog.type === 'cancel' ? t('common.cancel') : t('common.delete')}
        variant="danger"
        loading={actionLoading}
      />
    </div>
  );
}
