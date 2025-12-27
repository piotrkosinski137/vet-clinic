import { useState, useEffect, useMemo } from 'react';
import { api } from '../api';
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

function formatDate(dateStr?: string): string {
  if (!dateStr) return '-';
  return new Date(dateStr).toLocaleDateString('pl-PL');
}

function formatCurrency(amount: number): string {
  return new Intl.NumberFormat('pl-PL', { style: 'currency', currency: 'PLN' }).format(amount);
}

export function PaymentsPage() {
  const [invoices, setInvoices] = useState<InvoiceResponse[]>([]);
  const [clients, setClients] = useState<ClientResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [statusFilter, setStatusFilter] = useState<InvoiceStatus | ''>('');
  const [page, setPage] = useState(1);
  const [selectedInvoice, setSelectedInvoice] = useState<InvoiceResponse | null>(null);
  const [showPaymentModal, setShowPaymentModal] = useState(false);
  const [actionLoading, setActionLoading] = useState(false);
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
      setError(err instanceof Error ? err.message : 'Failed to load data');
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
      success('Invoice issued');
      fetchData();
    } catch (err) {
      showError(err instanceof Error ? err.message : 'Failed to issue invoice');
    } finally {
      setActionLoading(false);
    }
  };

  const handleCancel = async (invoice: InvoiceResponse) => {
    if (!window.confirm('Are you sure you want to cancel this invoice?')) return;
    setActionLoading(true);
    try {
      await api.cancelInvoice(invoice.id);
      success('Invoice cancelled');
      fetchData();
    } catch (err) {
      showError(err instanceof Error ? err.message : 'Failed to cancel invoice');
    } finally {
      setActionLoading(false);
    }
  };

  const handleDelete = async (invoice: InvoiceResponse) => {
    if (!window.confirm('Are you sure you want to delete this invoice?')) return;
    setActionLoading(true);
    try {
      await api.deleteInvoice(invoice.id);
      success('Invoice deleted');
      fetchData();
    } catch (err) {
      showError(err instanceof Error ? err.message : 'Failed to delete invoice');
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
      showError('Please enter a valid amount');
      return;
    }
    setActionLoading(true);
    try {
      await api.recordPayment(selectedInvoice.id, {
        amount: paymentForm.amount,
        paymentMethod: paymentForm.paymentMethod,
        notes: paymentForm.notes || undefined,
      });
      success('Payment recorded');
      setShowPaymentModal(false);
      fetchData();
    } catch (err) {
      showError(err instanceof Error ? err.message : 'Failed to record payment');
    } finally {
      setActionLoading(false);
    }
  };

  return (
    <div>
      <PageHeader title="Payments & Invoices" />

      {/* Stats Cards */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(200px, 1fr))', gap: spacing.lg, marginBottom: spacing.xl }}>
        <StatCard
          label="Total Invoiced"
          value={formatCurrency(stats.totalInvoiced)}
          icon="📄"
          color="primary"
        />
        <StatCard
          label="Total Paid"
          value={formatCurrency(stats.totalPaid)}
          icon="✅"
          color="success"
        />
        <StatCard
          label="Outstanding"
          value={formatCurrency(stats.totalOutstanding)}
          icon="⏳"
          color="warning"
        />
        <StatCard
          label="Unpaid Invoices"
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
              <label style={{ display: 'block', marginBottom: spacing.xs, fontSize: '14px' }}>Status</label>
              <Select
                value={statusFilter}
                onChange={(e) => { setStatusFilter(e.target.value as InvoiceStatus | ''); setPage(1); }}
              >
                <option value="">All Statuses</option>
                {INVOICE_STATUSES.map(s => (
                  <option key={s} value={s}>{s}</option>
                ))}
              </Select>
            </div>
            <Button variant="ghost" onClick={() => { setStatusFilter(''); setPage(1); }}>
              Clear
            </Button>
          </div>
        </CardContent>
      </Card>

      {/* Invoices Table */}
      <Card variant="default">
        {loading && (
          <div style={{ padding: spacing.xl, textAlign: 'center' }}>
            <Loading text="Loading invoices..." />
          </div>
        )}

        {error && (
          <div style={{ padding: spacing.xl, textAlign: 'center', color: colors.danger.main }}>
            <Text>{error}</Text>
            <Button variant="ghost" onClick={fetchData} style={{ marginTop: spacing.md }}>Retry</Button>
          </div>
        )}

        {!loading && !error && filteredInvoices.length === 0 && (
          <EmptyState
            icon="💰"
            title="No invoices found"
            description="Invoices will appear here when created from visits."
          />
        )}

        {!loading && !error && filteredInvoices.length > 0 && (
          <>
            <div style={{ overflowX: 'auto' }}>
              <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '14px' }}>
                <thead>
                  <tr style={{ backgroundColor: colors.neutral.background, borderBottom: `1px solid ${colors.neutral.border}` }}>
                    <th style={{ padding: spacing.md, textAlign: 'left' }}>Invoice #</th>
                    <th style={{ padding: spacing.md, textAlign: 'left' }}>Client</th>
                    <th style={{ padding: spacing.md, textAlign: 'left' }}>Status</th>
                    <th style={{ padding: spacing.md, textAlign: 'right' }}>Total</th>
                    <th style={{ padding: spacing.md, textAlign: 'right' }}>Paid</th>
                    <th style={{ padding: spacing.md, textAlign: 'right' }}>Outstanding</th>
                    <th style={{ padding: spacing.md, textAlign: 'left' }}>Date</th>
                    <th style={{ padding: spacing.md, textAlign: 'left' }}>Due</th>
                    <th style={{ padding: spacing.md, textAlign: 'right' }}>Actions</th>
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
                          <Badge variant={statusColors[invoice.status]}>{invoice.status}</Badge>
                        </td>
                        <td style={{ padding: spacing.md, textAlign: 'right', fontWeight: 500 }}>
                          {formatCurrency(invoice.totalAmount)}
                        </td>
                        <td style={{ padding: spacing.md, textAlign: 'right', color: colors.success.main }}>
                          {formatCurrency(invoice.paidAmount)}
                        </td>
                        <td style={{ padding: spacing.md, textAlign: 'right', color: outstanding > 0 ? colors.warning.main : colors.neutral.textMuted }}>
                          {formatCurrency(outstanding)}
                        </td>
                        <td style={{ padding: spacing.md }}>{formatDate(invoice.createdAt)}</td>
                        <td style={{ padding: spacing.md }}>{formatDate(invoice.dueDate)}</td>
                        <td style={{ padding: spacing.md, textAlign: 'right' }}>
                          <div style={{ display: 'flex', gap: spacing.xs, justifyContent: 'flex-end', flexWrap: 'wrap' }}>
                            {invoice.status === 'DRAFT' && (
                              <Button size="sm" variant="primary" onClick={() => handleIssue(invoice)} disabled={actionLoading}>
                                Issue
                              </Button>
                            )}
                            {(invoice.status === 'ISSUED' || invoice.status === 'OVERDUE') && outstanding > 0 && (
                              <Button size="sm" variant="success" onClick={() => openPaymentModal(invoice)} disabled={actionLoading}>
                                Pay
                              </Button>
                            )}
                            {invoice.status !== 'PAID' && invoice.status !== 'CANCELLED' && (
                              <Button size="sm" variant="secondary" onClick={() => handleCancel(invoice)} disabled={actionLoading}>
                                Cancel
                              </Button>
                            )}
                            {invoice.status === 'DRAFT' && (
                              <Button size="sm" variant="danger" onClick={() => handleDelete(invoice)} disabled={actionLoading}>
                                Delete
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
        <ModalTitle>Record Payment</ModalTitle>
        {selectedInvoice && (
          <div style={{ marginBottom: spacing.lg, padding: spacing.md, backgroundColor: colors.neutral.background, borderRadius: borderRadius.md }}>
            <Text variant="caption">Invoice: {selectedInvoice.invoiceNumber}</Text>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: spacing.sm }}>
              <Text>Total: {formatCurrency(selectedInvoice.totalAmount)}</Text>
              <Text>Outstanding: {formatCurrency(selectedInvoice.totalAmount - selectedInvoice.paidAmount)}</Text>
            </div>
          </div>
        )}
        <FormField label="Amount" required>
          <Input
            type="number"
            step="0.01"
            min="0.01"
            value={paymentForm.amount}
            onChange={(e) => setPaymentForm({ ...paymentForm, amount: parseFloat(e.target.value) || 0 })}
          />
        </FormField>
        <FormField label="Payment Method" required>
          <Select
            value={paymentForm.paymentMethod}
            onChange={(e) => setPaymentForm({ ...paymentForm, paymentMethod: e.target.value as PaymentMethod })}
          >
            {PAYMENT_METHODS.map(m => (
              <option key={m} value={m}>{m}</option>
            ))}
          </Select>
        </FormField>
        <FormField label="Notes">
          <Input
            value={paymentForm.notes}
            onChange={(e) => setPaymentForm({ ...paymentForm, notes: e.target.value })}
            placeholder="Optional notes..."
          />
        </FormField>
        <ModalActions>
          <Button variant="ghost" onClick={() => setShowPaymentModal(false)}>Cancel</Button>
          <Button variant="primary" onClick={handleRecordPayment} disabled={actionLoading}>
            {actionLoading ? 'Recording...' : 'Record Payment'}
          </Button>
        </ModalActions>
      </Modal>
    </div>
  );
}
