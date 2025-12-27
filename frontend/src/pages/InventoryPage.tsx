import { useState, useEffect } from "react";
import { api } from "../api";
import type {
  PriceListItemResponse,
  SupplierInvoice,
  InventoryTransaction
} from "../api/types";
import {
  Button, Card, CardTitle, Text, Loading, Input, Badge
} from "../components/ui";
import { formatCurrency } from "../hooks";
import { useI18n } from "../i18n";
import { colors, spacing, borderRadius, fontSize, fontWeight } from "../theme";

type TabId = 'stock' | 'invoices' | 'transactions' | 'alerts';

export function InventoryPage() {
  const { t: _t } = useI18n(); // Reserved for i18n
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

  // Low stock tab state
  const [lowStockItems, setLowStockItems] = useState<PriceListItemResponse[]>([]);

  // Tab definitions
  const tabs = [
    { id: 'stock' as TabId, label: 'Stock Overview', icon: '📦' },
    { id: 'invoices' as TabId, label: 'Invoice Import', icon: '📄' },
    { id: 'transactions' as TabId, label: 'Transactions', icon: '📋' },
    { id: 'alerts' as TabId, label: 'Low Stock', icon: '⚠️' },
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
        }
      } catch {
        setError('Failed to fetch data. Please try again.');
      } finally {
        setLoading(false);
      }
    }
    fetchData();
  }, [activeTab]);

  // File upload handler
  const handleFileUpload = async () => {
    if (!uploadFile) return;
    setUploading(true);
    try {
      const invoice = await api.uploadSupplierInvoice(uploadFile);
      setInvoices(prev => [invoice, ...prev]);
      setUploadFile(null);
    } catch {
      setError('Upload failed. Please try again.');
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
      setError('Failed to process invoice. Please try again.');
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
          <Text variant="muted">Loading...</Text>
        </div>
      );
    }

    switch (activeTab) {
      case 'stock':
        return renderStockTab();
      case 'invoices':
        return renderInvoicesTab();
      case 'transactions':
        return renderTransactionsTab();
      case 'alerts':
        return renderAlertsTab();
    }
  };

  // Stock tab renderer
  const renderStockTab = () => (
    <div>
      <Input
        placeholder="Search items..."
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
        <div>Item</div>
        <div style={{ textAlign: 'center' }}>Stock</div>
        <div style={{ textAlign: 'center' }}>Reorder</div>
        <div style={{ textAlign: 'right' }}>Cost</div>
        <div style={{ textAlign: 'right' }}>Sell</div>
        <div style={{ textAlign: 'center' }}>Status</div>
      </div>

      {filteredItems.length === 0 ? (
        <Card><Text variant="muted">No items found</Text></Card>
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
                  {isOut ? 'Out of Stock' : isLow ? 'Low Stock' : 'In Stock'}
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
        <CardTitle>Upload Supplier Invoice</CardTitle>
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
            {uploading ? 'Uploading...' : 'Upload & Preview'}
          </Button>
        </div>
      </Card>

      {/* Invoices List */}
      <Card>
        <CardTitle>Supplier Invoices</CardTitle>
        {invoices.length === 0 ? (
          <Text variant="muted">No invoices uploaded yet</Text>
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
                  {invoice.items?.length || 0} items | Total: {formatCurrency(invoice.totalGross || 0)}
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
                    Process
                  </Button>
                )}
              </div>
            </div>
          ))
        )}
      </Card>
    </div>
  );

  // Transactions tab renderer
  const renderTransactionsTab = () => (
    <Card>
      <CardTitle>Transaction History</CardTitle>
      {transactions.length === 0 ? (
        <Text variant="muted">No transactions recorded yet</Text>
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
            <div>Date</div>
            <div>Type</div>
            <div style={{ textAlign: 'center' }}>Qty</div>
            <div style={{ textAlign: 'center' }}>Stock</div>
            <div>Notes</div>
            <div>Reference</div>
          </div>

          {transactions.map(txn => (
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
              <div>{new Date(txn.createdAt).toLocaleString()}</div>
              <div>
                <Badge variant={
                  txn.transactionType === 'RECEIPT' ? 'success' :
                  txn.transactionType === 'USAGE' ? 'warning' :
                  txn.transactionType === 'ADJUSTMENT' ? 'secondary' : 'danger'
                }>
                  {txn.transactionType}
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
      <CardTitle style={{ color: colors.danger.main }}>⚠️ Low Stock Alerts</CardTitle>
      {lowStockItems.length === 0 ? (
        <div style={{ textAlign: 'center', padding: spacing.xl }}>
          <Text style={{ fontSize: fontSize.xl }}>✓</Text>
          <Text variant="muted">All items are well stocked!</Text>
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
                    Order {deficit} more
                  </Text>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </Card>
  );

  return (
    <div>
      {/* Header */}
      <h1 style={{ color: colors.secondary.main, marginBottom: spacing.lg }}>
        Inventory Management
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
            }}
          >
            {tab.icon} {tab.label}
          </button>
        ))}
      </div>

      {/* Tab Content */}
      {renderTabContent()}
    </div>
  );
}
