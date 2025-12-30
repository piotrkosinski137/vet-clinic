import { useState, useEffect, useMemo } from "react";
import { api } from "../api";
import type {
  PriceListItemResponse,
  SupplierInvoice,
  InventoryTransaction,
  InventoryBatchResponse,
  BatchStatus,
} from "../api/types";
import {
  Button, Card, CardTitle, Text, Loading, Input, Badge, useToast
} from "../components/ui";
import { formatCurrency } from "../hooks";
import { useI18n } from "../i18n";
import { colors, spacing, borderRadius, fontSize, fontWeight } from "../theme";
import { CompleteBatchModal } from "../components/inventory/CompleteBatchModal";
import { DisposeBatchModal } from "../components/inventory/DisposeBatchModal";

type TabId = 'stock' | 'batches' | 'invoices' | 'transactions' | 'alerts' | 'expiringSoon';

export function InventoryPage() {
  const { t } = useI18n();
  const { success } = useToast();
  const [activeTab, setActiveTab] = useState<TabId>('stock');
  const [loading, setLoading] = useState(false);
  const [_error, setError] = useState<string | null>(null);

  // Stock tab state
  const [items, setItems] = useState<PriceListItemResponse[]>([]);
  const [searchQuery, setSearchQuery] = useState("");

  // Invoice tab state
  const [invoices, setInvoices] = useState<SupplierInvoice[]>([]);
  const [uploadFile, setUploadFile] = useState<File | null>(null);
  const [uploading, setUploading] = useState(false);

  // Transactions tab state
  const [transactions, setTransactions] = useState<InventoryTransaction[]>([]);
  const [transactionTypeFilter, setTransactionTypeFilter] = useState<string>('ALL');

  // Low stock tab state
  const [lowStockItems, setLowStockItems] = useState<PriceListItemResponse[]>([]);

  // Batches tab state
  const [batches, setBatches] = useState<InventoryBatchResponse[]>([]);
  const [batchStatusFilter, setBatchStatusFilter] = useState<BatchStatus | 'ALL'>('ALL');
  const [pendingCount, setPendingCount] = useState<number>(0);
  const [selectedBatchForComplete, setSelectedBatchForComplete] = useState<InventoryBatchResponse | null>(null);
  const [selectedBatchForDispose, setSelectedBatchForDispose] = useState<InventoryBatchResponse | null>(null);
  const [batchSortBy, setBatchSortBy] = useState<'expiration' | 'quantity' | 'value'>('expiration');

  // Expiring soon tab state
  const [expiringBatches, setExpiringBatches] = useState<InventoryBatchResponse[]>([]);
  const [expiringDaysFilter, setExpiringDaysFilter] = useState<number>(30);
  const [expiringCount, setExpiringCount] = useState<number>(0);

  // Tab definitions
  const tabs = [
    { id: 'stock' as TabId, label: t('stock.stockOverview'), icon: '📦' },
    { id: 'batches' as TabId, label: t('stock.batchesTab'), icon: '🏷️', badge: pendingCount > 0 ? pendingCount : undefined },
    { id: 'invoices' as TabId, label: t('stock.invoiceImport'), icon: '📄' },
    { id: 'transactions' as TabId, label: t('stock.transactions'), icon: '📋' },
    { id: 'alerts' as TabId, label: t('stock.lowStock'), icon: '⚠️' },
    { id: 'expiringSoon' as TabId, label: t('stock.expiringSoonTab'), icon: '⏰', badge: expiringCount > 0 ? expiringCount : undefined },
  ];

  // Fetch data based on active tab
  useEffect(() => {
    async function fetchData() {
      setLoading(true);
      try {
        switch (activeTab) {
          case 'stock':
            const stockItems = await api.getInventoryItems();
            setItems(stockItems);
            break;
          case 'invoices':
            const invs = await api.getSupplierInvoices();
            setInvoices(invs);
            break;
          case 'transactions':
            const txns = await api.getInventoryTransactions();
            setTransactions(txns);
            break;
          case 'alerts':
            const lowStock = await api.getLowStockItems();
            setLowStockItems(lowStock);
            break;
          case 'batches':
            const batchFilter = batchStatusFilter === 'ALL' ? undefined : { status: batchStatusFilter };
            const batchList = await api.getInventoryBatches(batchFilter);
            setBatches(batchList);
            break;
          case 'expiringSoon':
            const expiringData = await api.getExpiringBatches(expiringDaysFilter);
            setExpiringBatches(expiringData);
            break;
        }
      } catch {
        setError(t('errors.failedToLoad'));
      } finally {
        setLoading(false);
      }
    }
    fetchData();
  }, [activeTab, t, batchStatusFilter, expiringDaysFilter]);

  // Fetch pending batch count on mount
  useEffect(() => {
    async function fetchPendingCount() {
      try {
        const count = await api.getPendingBatchCount();
        setPendingCount(count);
      } catch {
        // Ignore - badge just won't show
      }
    }
    fetchPendingCount();
  }, [batches]); // Re-fetch when batches change

  // Fetch expiring batch count for badge
  useEffect(() => {
    async function fetchExpiringCount() {
      try {
        const expiring = await api.getExpiringBatches(30);
        setExpiringCount(expiring.length);
      } catch {
        // Ignore - badge just won't show
      }
    }
    fetchExpiringCount();
  }, []);

  // File upload handler
  const handleFileUpload = async () => {
    if (!uploadFile) return;
    setUploading(true);
    try {
      const invoice = await api.uploadSupplierInvoice(uploadFile);
      setInvoices(prev => [invoice, ...prev]);
      setUploadFile(null);
    } catch {
      setError(t('errors.failedToSave'));
    } finally {
      setUploading(false);
    }
  };

  // Process invoice handler
  const handleProcessInvoice = async (invoiceId: string) => {
    try {
      const processed = await api.processSupplierInvoice(invoiceId);
      setInvoices(prev => prev.map(inv =>
        inv.id === invoiceId ? processed : inv
      ));
    } catch {
      setError(t('errors.failedToSave'));
    }
  };

  // Filter items by search query
  const filteredItems = items.filter(item =>
    item.name.toLowerCase().includes(searchQuery.toLowerCase())
  );

  // Render tab content based on active tab
  const renderTabContent = () => {
    if (loading) {
      return (
        <div style={{ padding: spacing.xl, textAlign: 'center' }}>
          <Loading />
          <Text variant="muted">{t('common.loading')}</Text>
        </div>
      );
    }

    switch (activeTab) {
      case 'stock':
        return renderStockTab();
      case 'batches':
        return renderBatchesTab();
      case 'invoices':
        return renderInvoicesTab();
      case 'transactions':
        return renderTransactionsTab();
      case 'alerts':
        return renderAlertsTab();
      case 'expiringSoon':
        return renderExpiringSoonTab();
    }
  };

  // Stock tab renderer
  const renderStockTab = () => (
    <div>
      <Input
        placeholder={t('stock.searchItems')}
        value={searchQuery}
        onChange={(e) => setSearchQuery(e.target.value)}
        style={{ marginBottom: spacing.md, maxWidth: '300px' }}
      />

      {/* Stock Table */}
      <div style={{
        display: 'grid',
        gridTemplateColumns: '2fr 100px 100px 100px 100px 120px',
        gap: spacing.sm,
        padding: spacing.sm,
        backgroundColor: colors.secondary.main,
        color: colors.neutral.white,
        borderRadius: `${borderRadius.sm} ${borderRadius.sm} 0 0`,
        fontSize: fontSize.sm,
        fontWeight: fontWeight.semibold,
      }}>
        <div>{t('stock.item')}</div>
        <div style={{ textAlign: 'center' }}>{t('stock.currentStock')}</div>
        <div style={{ textAlign: 'center' }}>{t('stock.reorderLevel')}</div>
        <div style={{ textAlign: 'right' }}>{t('stock.costPrice')}</div>
        <div style={{ textAlign: 'right' }}>{t('stock.sellPrice')}</div>
        <div style={{ textAlign: 'center' }}>{t('common.status')}</div>
      </div>

      {filteredItems.length === 0 ? (
        <Card><Text variant="muted">{t('stock.noItems')}</Text></Card>
      ) : (
        filteredItems.map(item => {
          const stock = item.stockQuantity ?? 0;
          const reorder = item.reorderPoint ?? 5;
          const isLow = stock <= reorder;
          const isOut = stock === 0;

          return (
            <div
              key={item.id}
              style={{
                display: 'grid',
                gridTemplateColumns: '2fr 100px 100px 100px 100px 120px',
                gap: spacing.sm,
                padding: spacing.sm,
                backgroundColor: isOut ? colors.danger.light : isLow ? colors.warning.light : colors.neutral.background,
                borderBottom: `1px solid ${colors.neutral.border}`,
                alignItems: 'center',
              }}
            >
              <div>
                <Text style={{ fontWeight: fontWeight.medium }}>{item.name}</Text>
                <Text variant="muted" size="sm">{item.unit}</Text>
              </div>
              <div style={{ textAlign: 'center', fontWeight: fontWeight.bold, color: isOut ? colors.danger.main : isLow ? colors.warning.main : colors.success.main }}>
                {stock}
              </div>
              <div style={{ textAlign: 'center' }}>{reorder}</div>
              <div style={{ textAlign: 'right' }}>{formatCurrency(item.costPrice)}</div>
              <div style={{ textAlign: 'right' }}>{formatCurrency(item.sellPrice)}</div>
              <div style={{ textAlign: 'center' }}>
                <Badge variant={isOut ? 'danger' : isLow ? 'warning' : 'success'}>
                  {isOut ? t('stock.outOfStock') : isLow ? t('stock.lowStockStatus') : t('stock.inStock')}
                </Badge>
              </div>
            </div>
          );
        })
      )}
    </div>
  );

  // Invoices tab renderer
  const renderInvoicesTab = () => (
    <div>
      {/* Upload Section */}
      <Card style={{ marginBottom: spacing.lg }}>
        <CardTitle>{t('stock.uploadInvoice')}</CardTitle>
        <div style={{ display: 'flex', gap: spacing.md, alignItems: 'center' }}>
          <input
            type="file"
            accept=".txt,.csv"
            onChange={(e) => setUploadFile(e.target.files?.[0] || null)}
            style={{ flex: 1 }}
          />
          <Button
            onClick={handleFileUpload}
            disabled={!uploadFile || uploading}
            variant="primary"
          >
            {uploading ? t('common.uploading') : t('stock.uploadPreview')}
          </Button>
        </div>
      </Card>

      {/* Invoices List */}
      <Card>
        <CardTitle>{t('stock.supplierInvoices')}</CardTitle>
        {invoices.length === 0 ? (
          <Text variant="muted">{t('stock.noInvoices')}</Text>
        ) : (
          invoices.map(invoice => (
            <div
              key={invoice.id}
              style={{
                padding: spacing.md,
                borderBottom: `1px solid ${colors.neutral.border}`,
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
              }}
            >
              <div>
                <Text style={{ fontWeight: fontWeight.medium }}>
                  {invoice.invoiceNumber}
                </Text>
                <Text variant="muted" size="sm">
                  {invoice.supplierName} | {invoice.invoiceDate}
                </Text>
                <Text size="sm">
                  {invoice.items?.length || 0} items | {t('common.total')}: {formatCurrency(invoice.totalGross || 0)}
                </Text>
              </div>
              <div style={{ display: 'flex', gap: spacing.sm, alignItems: 'center' }}>
                <Badge variant={
                  invoice.status === 'PROCESSED' ? 'success' :
                  invoice.status === 'ERROR' ? 'danger' : 'warning'
                }>
                  {invoice.status}
                </Badge>
                {invoice.status === 'PENDING' && (
                  <Button
                    size="sm"
                    onClick={() => handleProcessInvoice(invoice.id)}
                  >
                    {t('common.process')}
                  </Button>
                )}
              </div>
            </div>
          ))
        )}
      </Card>
    </div>
  );

  // Disposal statistics for transactions tab
  const disposalStats = useMemo(() => {
    const expired = transactions.filter(t => t.transactionType === 'EXPIRED');
    return {
      count: expired.length,
      totalQty: expired.reduce((s, t) => s + Math.abs(t.quantity), 0),
      totalValue: expired.reduce((s, t) => s + Math.abs(t.quantity) * (t.unitCost || 0), 0)
    };
  }, [transactions]);

  // Filter transactions based on type filter
  const filteredTransactions = transactionTypeFilter === 'ALL'
    ? transactions
    : transactions.filter(txn => txn.transactionType === transactionTypeFilter);

  // Transactions tab renderer
  const renderTransactionsTab = () => (
    <Card>
      <CardTitle>{t('stock.transactionHistory')}</CardTitle>

      {/* Transaction Type Filter */}
      <div style={{ marginBottom: spacing.md, display: 'flex', gap: spacing.md, alignItems: 'center' }}>
        <Text>{t('common.type')}:</Text>
        <select
          value={transactionTypeFilter}
          onChange={(e) => setTransactionTypeFilter(e.target.value)}
          style={{ padding: spacing.sm, borderRadius: borderRadius.sm, border: `1px solid ${colors.neutral.border}` }}
        >
          <option value="ALL">{t('common.all')}</option>
          <option value="RECEIPT">{t('stock.transactionTypes.RECEIPT')}</option>
          <option value="USAGE">{t('stock.transactionTypes.USAGE')}</option>
          <option value="EXPIRED">{t('stock.transactionTypes.EXPIRED')}</option>
          <option value="ADJUSTMENT">{t('stock.transactionTypes.ADJUSTMENT')}</option>
        </select>
      </div>

      {/* Disposal Statistics */}
      {disposalStats.count > 0 && (
        <div style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(3, 1fr)',
          gap: spacing.md,
          marginBottom: spacing.lg,
        }}>
          <Card style={{ textAlign: 'center', backgroundColor: colors.danger.light }}>
            <Text variant="muted" size="sm">{t('stock.disposalTransactions')}</Text>
            <Text style={{ fontSize: fontSize.xl, fontWeight: fontWeight.bold, color: colors.danger.main }}>
              {disposalStats.count}
            </Text>
          </Card>
          <Card style={{ textAlign: 'center', backgroundColor: colors.warning.light }}>
            <Text variant="muted" size="sm">{t('stock.totalDisposedQty')}</Text>
            <Text style={{ fontSize: fontSize.xl, fontWeight: fontWeight.bold, color: colors.warning.main }}>
              {disposalStats.totalQty}
            </Text>
          </Card>
          <Card style={{ textAlign: 'center' }}>
            <Text variant="muted" size="sm">{t('stock.totalDisposedValue')}</Text>
            <Text style={{ fontSize: fontSize.xl, fontWeight: fontWeight.bold, color: colors.danger.main }}>
              {formatCurrency(disposalStats.totalValue)}
            </Text>
          </Card>
        </div>
      )}

      {filteredTransactions.length === 0 ? (
        <Text variant="muted">{t('stock.noTransactions')}</Text>
      ) : (
        <div>
          {/* Table Header */}
          <div style={{
            display: 'grid',
            gridTemplateColumns: '150px 100px 100px 100px 2fr 120px',
            gap: spacing.sm,
            padding: spacing.sm,
            backgroundColor: colors.secondary.main,
            color: colors.neutral.white,
            borderRadius: borderRadius.sm,
            fontSize: fontSize.sm,
            fontWeight: fontWeight.semibold,
          }}>
            <div>{t('common.date')}</div>
            <div>{t('common.type')}</div>
            <div style={{ textAlign: 'center' }}>{t('stock.qty')}</div>
            <div style={{ textAlign: 'center' }}>{t('stock.currentStock')}</div>
            <div>{t('common.notes')}</div>
            <div>{t('stock.reference')}</div>
          </div>

          {filteredTransactions.map(txn => (
            <div
              key={txn.id}
              style={{
                display: 'grid',
                gridTemplateColumns: '150px 100px 100px 100px 2fr 120px',
                gap: spacing.sm,
                padding: spacing.sm,
                borderBottom: `1px solid ${colors.neutral.border}`,
                alignItems: 'center',
              }}
            >
              <div>{new Date(txn.createdAt).toLocaleString('pl-PL', { hour12: false })}</div>
              <div>
                <Badge variant={
                  txn.transactionType === 'RECEIPT' ? 'success' :
                  txn.transactionType === 'USAGE' ? 'warning' :
                  txn.transactionType === 'ADJUSTMENT' ? 'secondary' : 'danger'
                }>
                  {t(`stock.transactionTypes.${txn.transactionType}`)}
                </Badge>
              </div>
              <div style={{
                textAlign: 'center',
                fontWeight: fontWeight.bold,
                color: txn.quantity > 0 ? colors.success.main : colors.danger.main
              }}>
                {txn.quantity > 0 ? '+' : ''}{txn.quantity}
              </div>
              <div style={{ textAlign: 'center' }}>
                {txn.quantityBefore} → {txn.quantityAfter}
              </div>
              <div><Text size="sm">{txn.notes || '-'}</Text></div>
              <div><Text variant="muted" size="sm">{txn.referenceType || '-'}</Text></div>
            </div>
          ))}
        </div>
      )}
    </Card>
  );

  // Low stock alerts tab renderer
  const renderAlertsTab = () => (
    <Card>
      <CardTitle style={{ color: colors.danger.main }}>⚠️ {t('stock.lowStockAlerts')}</CardTitle>
      {lowStockItems.length === 0 ? (
        <div style={{ textAlign: 'center', padding: spacing.xl }}>
          <Text style={{ fontSize: fontSize.xl }}>✓</Text>
          <Text variant="muted">{t('stock.allWellStocked')}</Text>
        </div>
      ) : (
        <div>
          {lowStockItems.map(item => {
            const stock = item.stockQuantity ?? 0;
            const reorder = item.reorderPoint ?? 5;
            const deficit = reorder - stock;

            return (
              <div
                key={item.id}
                style={{
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center',
                  padding: spacing.md,
                  backgroundColor: stock === 0 ? colors.danger.light : colors.warning.light,
                  borderRadius: borderRadius.sm,
                  marginBottom: spacing.sm,
                }}
              >
                <div>
                  <Text style={{ fontWeight: fontWeight.medium }}>{item.name}</Text>
                  <Text variant="muted" size="sm">{item.unit}</Text>
                </div>
                <div style={{ textAlign: 'right' }}>
                  <Text style={{ fontWeight: fontWeight.bold, color: stock === 0 ? colors.danger.main : colors.warning.main }}>
                    {stock} / {reorder}
                  </Text>
                  <Text size="sm" variant="muted">
                    {t('stock.orderMore', { count: deficit })}
                  </Text>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </Card>
  );

  // Sort batches based on selected criteria
  const sortedBatches = [...batches].sort((a, b) => {
    switch (batchSortBy) {
      case 'expiration':
        // Null expiration dates go to the end
        if (!a.expirationDate && !b.expirationDate) return 0;
        if (!a.expirationDate) return 1;
        if (!b.expirationDate) return -1;
        return new Date(a.expirationDate).getTime() - new Date(b.expirationDate).getTime();
      case 'quantity':
        return (b.quantity ?? 0) - (a.quantity ?? 0);
      case 'value':
        return (b.totalValue ?? 0) - (a.totalValue ?? 0);
      default:
        return 0;
    }
  });

  // Calculate total batch value
  const totalBatchValue = batches.reduce((sum, batch) => sum + (batch.totalValue ?? 0), 0);

  // Batches tab renderer
  const renderBatchesTab = () => (
    <div>
      {/* Summary Stats */}
      <div style={{
        display: 'flex',
        gap: spacing.lg,
        marginBottom: spacing.md,
        padding: spacing.md,
        backgroundColor: colors.neutral.background,
        borderRadius: borderRadius.sm,
        border: `1px solid ${colors.neutral.border}`,
      }}>
        <div>
          <Text variant="muted" size="sm">{t('stock.batchesFound', { count: batches.length })}</Text>
        </div>
        <div style={{ marginLeft: 'auto' }}>
          <Text style={{ fontWeight: fontWeight.semibold }}>
            {t('stock.totalBatchValue')}: {formatCurrency(totalBatchValue)}
          </Text>
        </div>
      </div>

      {/* Filter and Sort Controls */}
      <div style={{
        display: 'flex',
        gap: spacing.md,
        alignItems: 'center',
        marginBottom: spacing.md,
        flexWrap: 'wrap',
      }}>
        <Text>{t('common.status')}:</Text>
        <select
          value={batchStatusFilter}
          onChange={(e) => setBatchStatusFilter(e.target.value as BatchStatus | 'ALL')}
          style={{
            padding: spacing.sm,
            borderRadius: borderRadius.sm,
            border: `1px solid ${colors.neutral.border}`,
          }}
        >
          <option value="ALL">{t('common.all')}</option>
          <option value="PENDING">{t('stock.batchStatus.PENDING')}</option>
          <option value="COMPLETE">{t('stock.batchStatus.COMPLETE')}</option>
          <option value="DEPLETED">{t('stock.batchStatus.DEPLETED')}</option>
        </select>

        <div style={{ marginLeft: spacing.lg, display: 'flex', gap: spacing.sm, alignItems: 'center' }}>
          <select
            value={batchSortBy}
            onChange={(e) => setBatchSortBy(e.target.value as 'expiration' | 'quantity' | 'value')}
            style={{
              padding: spacing.sm,
              borderRadius: borderRadius.sm,
              border: `1px solid ${colors.neutral.border}`,
            }}
          >
            <option value="expiration">{t('stock.sortByExpiration')}</option>
            <option value="quantity">{t('stock.sortByQuantity')}</option>
            <option value="value">{t('stock.sortByValue')}</option>
          </select>
        </div>

        <Text variant="muted" size="sm" style={{ marginLeft: 'auto' }}>
          {t('stock.fifoInfo')}
        </Text>
      </div>

      {/* Batches Table */}
      <div style={{
        display: 'grid',
        gridTemplateColumns: '2fr 120px 120px 80px 100px 100px 100px 150px',
        gap: spacing.sm,
        padding: spacing.sm,
        backgroundColor: colors.secondary.main,
        color: colors.neutral.white,
        borderRadius: `${borderRadius.sm} ${borderRadius.sm} 0 0`,
        fontSize: fontSize.sm,
        fontWeight: fontWeight.semibold,
      }}>
        <div>{t('stock.item')}</div>
        <div>{t('stock.lotNumber')}</div>
        <div>{t('stock.expirationDate')}</div>
        <div style={{ textAlign: 'center' }}>{t('stock.quantity')}</div>
        <div style={{ textAlign: 'right' }}>{t('stock.unitCost')}</div>
        <div style={{ textAlign: 'right' }}>{t('stock.totalValue')}</div>
        <div style={{ textAlign: 'center' }}>{t('common.status')}</div>
        <div style={{ textAlign: 'center' }}>{t('common.actions')}</div>
      </div>

      {sortedBatches.length === 0 ? (
        <Card style={{ textAlign: 'center', padding: spacing.xl }}>
          <Text variant="muted" style={{ marginBottom: spacing.sm }}>
            {batchStatusFilter !== 'ALL'
              ? t('stock.noBatchesForStatus', { status: t(`stock.batchStatus.${batchStatusFilter}`) })
              : t('stock.noBatches')
            }
          </Text>
        </Card>
      ) : (
        sortedBatches.map(batch => {
          const isExpired = batch.isExpired;
          const isExpiringSoon = batch.isExpiringSoon;

          return (
            <div
              key={batch.id}
              style={{
                display: 'grid',
                gridTemplateColumns: '2fr 120px 120px 80px 100px 100px 100px 150px',
                gap: spacing.sm,
                padding: spacing.sm,
                backgroundColor: isExpired ? colors.danger.light : isExpiringSoon ? colors.warning.light : colors.neutral.background,
                borderBottom: `1px solid ${colors.neutral.border}`,
                alignItems: 'center',
              }}
            >
              <div>
                <Text style={{ fontWeight: fontWeight.medium }}>{batch.itemName}</Text>
              </div>
              <div>
                <Text size="sm">{batch.lotNumber || '-'}</Text>
              </div>
              <div>
                {batch.expirationDate ? (
                  <div>
                    <Text size="sm">{new Date(batch.expirationDate).toLocaleDateString()}</Text>
                    {batch.daysUntilExpiration !== undefined && batch.daysUntilExpiration !== null && (
                      <Text size="sm" variant="muted" style={{
                        color: isExpired ? colors.danger.main : isExpiringSoon ? colors.warning.main : undefined
                      }}>
                        {isExpired ? t('stock.expired') : `${batch.daysUntilExpiration} ${t('stock.daysUntilExpiration')}`}
                      </Text>
                    )}
                  </div>
                ) : (
                  <Text variant="muted">-</Text>
                )}
              </div>
              <div style={{ textAlign: 'center', fontWeight: fontWeight.bold }}>
                {batch.quantity}
              </div>
              <div style={{ textAlign: 'right' }}>
                {batch.unitCost ? formatCurrency(batch.unitCost) : '-'}
              </div>
              <div style={{ textAlign: 'right' }}>
                {batch.totalValue ? formatCurrency(batch.totalValue) : '-'}
              </div>
              <div style={{ textAlign: 'center' }}>
                <Badge variant={
                  batch.status === 'COMPLETE' ? 'success' :
                  batch.status === 'PENDING' ? 'warning' : 'secondary'
                }>
                  {t(`stock.batchStatus.${batch.status}`)}
                </Badge>
              </div>
              <div style={{ textAlign: 'center', display: 'flex', gap: spacing.xs, justifyContent: 'center' }}>
                {batch.status === 'PENDING' && (
                  <Button size="sm" variant="primary" onClick={() => setSelectedBatchForComplete(batch)}>
                    {t('stock.completeBatch')}
                  </Button>
                )}
                {batch.status === 'COMPLETE' && batch.quantity > 0 && (
                  <Button size="sm" variant="danger" onClick={() => setSelectedBatchForDispose(batch)}>
                    {t('stock.disposeBatch')}
                  </Button>
                )}
              </div>
            </div>
          );
        })
      )}

      {/* Complete Batch Modal */}
      {selectedBatchForComplete && (
        <CompleteBatchModal
          batch={selectedBatchForComplete}
          onClose={() => setSelectedBatchForComplete(null)}
          onComplete={async (lotNumber, expirationDate) => {
            await api.completeBatch(selectedBatchForComplete.id, { lotNumber, expirationDate });
            setSelectedBatchForComplete(null);
            // Refresh batches
            const batchFilter = batchStatusFilter === 'ALL' ? undefined : { status: batchStatusFilter };
            const batchList = await api.getInventoryBatches(batchFilter);
            setBatches(batchList);
          }}
        />
      )}

      {/* Dispose Batch Modal */}
      {selectedBatchForDispose && (
        <DisposeBatchModal
          batch={selectedBatchForDispose}
          onClose={() => setSelectedBatchForDispose(null)}
          onDispose={async (quantity, reason) => {
            await api.disposeBatch(selectedBatchForDispose.id, { quantity, reason });
            setSelectedBatchForDispose(null);
            success(t('stock.batchDisposed'));
            // Refresh batches
            const batchFilter = batchStatusFilter === 'ALL' ? undefined : { status: batchStatusFilter };
            const batchList = await api.getInventoryBatches(batchFilter);
            setBatches(batchList);
          }}
        />
      )}
    </div>
  );

  // Helper functions for expiring soon tab
  const getExpirationUrgency = (daysUntilExpiration: number | undefined, isExpired: boolean): 'critical' | 'warning' | 'normal' => {
    if (isExpired || (daysUntilExpiration !== undefined && daysUntilExpiration <= 0)) return 'critical';
    if (daysUntilExpiration !== undefined && daysUntilExpiration <= 7) return 'critical';
    if (daysUntilExpiration !== undefined && daysUntilExpiration <= 30) return 'warning';
    return 'normal';
  };

  const getUrgencyColors = (urgency: 'critical' | 'warning' | 'normal') => {
    switch (urgency) {
      case 'critical': return { bg: colors.danger.light, text: colors.danger.main, badge: 'danger' as const };
      case 'warning': return { bg: colors.warning.light, text: colors.warning.main, badge: 'warning' as const };
      case 'normal': return { bg: colors.info.light, text: colors.info.main, badge: 'primary' as const };
    }
  };

  // Expiring soon tab renderer
  const renderExpiringSoonTab = () => {
    const criticalCount = expiringBatches.filter(b => {
      const urgency = getExpirationUrgency(b.daysUntilExpiration, b.isExpired ?? false);
      return urgency === 'critical';
    }).length;

    const totalValueAtRisk = expiringBatches.reduce((sum, b) => sum + (b.totalValue ?? 0), 0);

    return (
      <div>
        {/* Summary Stats */}
        <div style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(3, 1fr)',
          gap: spacing.md,
          marginBottom: spacing.lg,
        }}>
          <Card style={{ textAlign: 'center', backgroundColor: colors.warning.light }}>
            <Text variant="muted" size="sm">{t('stock.expiringSoon.itemsExpiring')}</Text>
            <Text style={{ fontSize: fontSize.xl, fontWeight: fontWeight.bold, color: colors.warning.main }}>
              {expiringBatches.length}
            </Text>
          </Card>
          <Card style={{ textAlign: 'center', backgroundColor: colors.danger.light }}>
            <Text variant="muted" size="sm">{t('stock.expiringSoon.criticalItems')}</Text>
            <Text style={{ fontSize: fontSize.xl, fontWeight: fontWeight.bold, color: colors.danger.main }}>
              {criticalCount}
            </Text>
          </Card>
          <Card style={{ textAlign: 'center' }}>
            <Text variant="muted" size="sm">{t('stock.expiringSoon.valueAtRisk')}</Text>
            <Text style={{ fontSize: fontSize.xl, fontWeight: fontWeight.bold, color: colors.danger.main }}>
              {formatCurrency(totalValueAtRisk)}
            </Text>
          </Card>
        </div>

        {/* Days Filter Row */}
        <div style={{
          display: 'flex',
          gap: spacing.md,
          alignItems: 'center',
          marginBottom: spacing.md,
          flexWrap: 'wrap',
        }}>
          <Text>{t('stock.expiringSoon.filterByDays')}:</Text>
          <select
            value={expiringDaysFilter}
            onChange={(e) => setExpiringDaysFilter(Number(e.target.value))}
            style={{
              padding: spacing.sm,
              borderRadius: borderRadius.sm,
              border: `1px solid ${colors.neutral.border}`,
            }}
          >
            <option value={7}>7 {t('stock.expiringSoon.days')}</option>
            <option value={14}>14 {t('stock.expiringSoon.days')}</option>
            <option value={30}>30 {t('stock.expiringSoon.days')}</option>
            <option value={60}>60 {t('stock.expiringSoon.days')}</option>
            <option value={90}>90 {t('stock.expiringSoon.days')}</option>
          </select>

          {/* Color legend */}
          <div style={{ marginLeft: 'auto', display: 'flex', gap: spacing.md, alignItems: 'center' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: spacing.xs }}>
              <div style={{ width: '12px', height: '12px', borderRadius: '50%', backgroundColor: colors.danger.main }} />
              <Text size="sm">{t('stock.expiringSoon.critical')}</Text>
            </div>
            <div style={{ display: 'flex', alignItems: 'center', gap: spacing.xs }}>
              <div style={{ width: '12px', height: '12px', borderRadius: '50%', backgroundColor: colors.warning.main }} />
              <Text size="sm">{t('stock.expiringSoon.warning')}</Text>
            </div>
            <div style={{ display: 'flex', alignItems: 'center', gap: spacing.xs }}>
              <div style={{ width: '12px', height: '12px', borderRadius: '50%', backgroundColor: colors.info.main }} />
              <Text size="sm">{t('stock.expiringSoon.normal')}</Text>
            </div>
          </div>
        </div>

        {/* Table Header */}
        <div style={{
          display: 'grid',
          gridTemplateColumns: '2fr 120px 120px 80px 80px 100px 100px 120px',
          gap: spacing.sm,
          padding: spacing.sm,
          backgroundColor: colors.secondary.main,
          color: colors.neutral.white,
          borderRadius: `${borderRadius.sm} ${borderRadius.sm} 0 0`,
          fontSize: fontSize.sm,
          fontWeight: fontWeight.semibold,
        }}>
          <div>{t('stock.item')}</div>
          <div>{t('stock.lotNumber')}</div>
          <div>{t('stock.expirationDate')}</div>
          <div style={{ textAlign: 'center' }}>{t('stock.expiringSoon.daysLeft')}</div>
          <div style={{ textAlign: 'center' }}>{t('stock.qty')}</div>
          <div style={{ textAlign: 'right' }}>{t('stock.unitCost')}</div>
          <div style={{ textAlign: 'right' }}>{t('stock.totalValue')}</div>
          <div style={{ textAlign: 'center' }}>{t('common.actions')}</div>
        </div>

        {/* Empty State */}
        {expiringBatches.length === 0 ? (
          <Card style={{ textAlign: 'center', padding: spacing.xl }}>
            <Text style={{ fontSize: fontSize.xl }}>✓</Text>
            <Text variant="muted">{t('stock.expiringSoon.noExpiring')}</Text>
          </Card>
        ) : (
          expiringBatches.map(batch => {
            const urgency = getExpirationUrgency(batch.daysUntilExpiration, batch.isExpired ?? false);
            const urgencyColors = getUrgencyColors(urgency);

            return (
              <div
                key={batch.id}
                style={{
                  display: 'grid',
                  gridTemplateColumns: '2fr 120px 120px 80px 80px 100px 100px 120px',
                  gap: spacing.sm,
                  padding: spacing.sm,
                  backgroundColor: urgencyColors.bg,
                  borderBottom: `1px solid ${colors.neutral.border}`,
                  alignItems: 'center',
                }}
              >
                <div>
                  <Text style={{ fontWeight: fontWeight.medium }}>{batch.itemName}</Text>
                </div>
                <div>
                  <Text size="sm">{batch.lotNumber || '-'}</Text>
                </div>
                <div>
                  <Text size="sm">
                    {batch.expirationDate ? new Date(batch.expirationDate).toLocaleDateString() : '-'}
                  </Text>
                </div>
                <div style={{ textAlign: 'center' }}>
                  <Badge variant={urgencyColors.badge}>
                    {batch.isExpired ? t('stock.expired') : `${batch.daysUntilExpiration} ${t('stock.expiringSoon.days')}`}
                  </Badge>
                </div>
                <div style={{ textAlign: 'center', fontWeight: fontWeight.bold }}>
                  {batch.quantity}
                </div>
                <div style={{ textAlign: 'right' }}>
                  {batch.unitCost ? formatCurrency(batch.unitCost) : '-'}
                </div>
                <div style={{ textAlign: 'right' }}>
                  {batch.totalValue ? formatCurrency(batch.totalValue) : '-'}
                </div>
                <div style={{ textAlign: 'center' }}>
                  {batch.status === 'COMPLETE' && batch.quantity > 0 && (
                    <Button size="sm" variant="danger" onClick={() => setSelectedBatchForDispose(batch)}>
                      {t('stock.disposeBatch')}
                    </Button>
                  )}
                </div>
              </div>
            );
          })
        )}

        {/* Dispose Batch Modal */}
        {selectedBatchForDispose && (
          <DisposeBatchModal
            batch={selectedBatchForDispose}
            onClose={() => setSelectedBatchForDispose(null)}
            onDispose={async (quantity, reason) => {
              await api.disposeBatch(selectedBatchForDispose.id, { quantity, reason });
              setSelectedBatchForDispose(null);
              success(t('stock.batchDisposed'));
              // Refresh expiring batches
              const expiringData = await api.getExpiringBatches(expiringDaysFilter);
              setExpiringBatches(expiringData);
            }}
          />
        )}
      </div>
    );
  };

  return (
    <div>
      {/* Header */}
      <h1 style={{ color: colors.secondary.main, marginBottom: spacing.lg }}>
        {t('stock.title')}
      </h1>

      {/* Tab Navigation */}
      <div style={{
        display: 'flex',
        gap: spacing.sm,
        marginBottom: spacing.lg,
        borderBottom: `2px solid ${colors.neutral.border}`,
        paddingBottom: spacing.sm,
      }}>
        {tabs.map(tab => (
          <button
            key={tab.id}
            onClick={() => setActiveTab(tab.id)}
            style={{
              padding: `${spacing.sm} ${spacing.md}`,
              border: 'none',
              borderRadius: borderRadius.sm,
              cursor: 'pointer',
              backgroundColor: activeTab === tab.id ? colors.primary.main : 'transparent',
              color: activeTab === tab.id ? colors.neutral.white : colors.neutral.text,
              fontWeight: activeTab === tab.id ? fontWeight.semibold : fontWeight.normal,
              transition: 'all 0.2s',
              display: 'flex',
              alignItems: 'center',
              gap: spacing.xs,
            }}
          >
            {tab.icon} {tab.label}
            {tab.badge && (
              <span style={{
                backgroundColor: colors.danger.main,
                color: colors.neutral.white,
                borderRadius: '50%',
                padding: '2px 6px',
                fontSize: fontSize.xs,
                marginLeft: spacing.xs,
              }}>
                {tab.badge}
              </span>
            )}
          </button>
        ))}
      </div>

      {/* Tab Content */}
      {renderTabContent()}
    </div>
  );
}
