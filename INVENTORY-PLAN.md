# VetClinic Inventory Management System - Implementation Plan

## Executive Summary

Transform the current price-list based system into a full inventory management system with:
- Stock quantity tracking
- Invoice import (supplier invoices populate inventory)
- Automatic stock depletion when items used in visits
- Manual stock adjustments
- Low stock alerts

---

## Current State Analysis

### What Exists:
- `price_list_items` table - items with cost/sell prices
- `visit_used_materials` table - consumption history
- `MaterialsSelector` - dropdown to add items to visits
- `InventoryUsagePage` - daily consumption reporting

### What's Missing:
- Stock quantity tracking (`quantity_on_hand`)
- Stock transactions (receipts, adjustments)
- Invoice import/parsing
- Expiration date tracking
- Low stock alerts

---

## Invoice Format Analysis

The supplier invoice is a **fixed-width text file** with this structure:

```
Line 12+ contains item data:
Position  Field              Example Value
0-10      KodTowaru          8596
11-60     NazwaTowaru        ONSIOR KOTY 6MG - 30 TAB
61-75     Ilosc              1
76-82     JM (unit)          OP.
83-95     CenaTransBU        86.49
96-100    Upust (discount)   1
101-113   CenaTrans          85.63
...
150-160   DataWaznosci       2029-02-28 (expiry date)
161-175   Seria (batch)      LE249G
176-195   KodKreskowy        5420036930266 (barcode)
```

Key fields to extract:
- **KodTowaru** - Product code (for matching)
- **NazwaTowaru** - Product name
- **Ilosc** - Quantity
- **JM** - Unit (OP., SZT., etc.)
- **CenaTrans** - Net price after discount
- **BCenaTrans** - Gross price (with VAT)
- **DataWaznosci** - Expiration date
- **Seria** - Batch/series number
- **KodKreskowy** - Barcode (EAN)

---

## Real-Life Use Case Workflow

### 1. Supplier Invoice Arrival
```
Vet receives invoice from supplier (BAYLEG, etc.)
   ↓
Upload invoice file via UI
   ↓
System parses invoice, shows preview
   ↓
Vet confirms items to add
   ↓
Stock increased for each item
   ↓
Transaction recorded (RECEIPT type)
```

### 2. During Visit
```
Vet opens visit, goes to Billing tab
   ↓
MaterialsSelector shows ONLY items with stock > 0
   ↓
Vet selects items and quantities
   ↓
On save, stock is decreased
   ↓
Transaction recorded (USAGE type)
```

### 3. Manual Adjustment
```
Vet notices discrepancy (expired, damaged, miscounted)
   ↓
Opens Inventory page
   ↓
Adjusts quantity manually
   ↓
Transaction recorded (ADJUSTMENT type) with reason
```

### 4. Stock Check
```
Vet opens Inventory page
   ↓
Sees all products with current stock
   ↓
Low stock items highlighted (below reorder point)
   ↓
Can export list for reordering
```

---

## Database Schema Design

### 1. Extend `price_list_items` table
```sql
ALTER TABLE price_list_items ADD COLUMN stock_quantity INTEGER DEFAULT 0;
ALTER TABLE price_list_items ADD COLUMN reorder_point INTEGER DEFAULT 5;
ALTER TABLE price_list_items ADD COLUMN barcode VARCHAR(50);
ALTER TABLE price_list_items ADD COLUMN supplier_code VARCHAR(50);
```

### 2. New `inventory_transactions` table
```sql
CREATE TABLE inventory_transactions (
  id UUID PRIMARY KEY,
  clinic_id UUID NOT NULL,
  item_id UUID NOT NULL REFERENCES price_list_items(id),
  transaction_type VARCHAR(20) NOT NULL, -- RECEIPT, USAGE, ADJUSTMENT, EXPIRED, RETURN
  quantity INTEGER NOT NULL, -- positive for IN, negative for OUT
  reference_id UUID, -- visit_id for USAGE, invoice_id for RECEIPT
  reference_type VARCHAR(20), -- VISIT, INVOICE, MANUAL
  batch_number VARCHAR(50),
  expiration_date DATE,
  unit_cost DECIMAL(10,2),
  notes TEXT,
  created_by VARCHAR(255),
  created_at TIMESTAMP DEFAULT NOW()
);
```

### 3. New `supplier_invoices` table
```sql
CREATE TABLE supplier_invoices (
  id UUID PRIMARY KEY,
  clinic_id UUID NOT NULL,
  invoice_number VARCHAR(100) NOT NULL,
  supplier_name VARCHAR(255),
  supplier_nip VARCHAR(20),
  invoice_date DATE,
  total_amount DECIMAL(10,2),
  file_name VARCHAR(255),
  raw_content TEXT,
  status VARCHAR(20) DEFAULT 'PENDING', -- PENDING, PROCESSED, ERROR
  processed_at TIMESTAMP,
  created_at TIMESTAMP DEFAULT NOW()
);
```

### 4. New `supplier_invoice_items` table
```sql
CREATE TABLE supplier_invoice_items (
  id UUID PRIMARY KEY,
  invoice_id UUID NOT NULL REFERENCES supplier_invoices(id),
  item_id UUID REFERENCES price_list_items(id), -- null if new item
  product_code VARCHAR(50),
  product_name VARCHAR(255) NOT NULL,
  quantity INTEGER NOT NULL,
  unit VARCHAR(20),
  unit_price DECIMAL(10,2),
  total_price DECIMAL(10,2),
  vat_rate INTEGER,
  batch_number VARCHAR(50),
  expiration_date DATE,
  barcode VARCHAR(50),
  matched BOOLEAN DEFAULT FALSE -- true if matched to existing item
);
```

---

## Backend Implementation

### 1. New Entities

**InventoryTransaction.java**
```java
@Entity
@Table(name = "inventory_transactions")
public class InventoryTransaction extends TenantAwareEntity {
    private UUID itemId;

    @Enumerated(EnumType.STRING)
    private TransactionType transactionType; // RECEIPT, USAGE, ADJUSTMENT, EXPIRED, RETURN

    private Integer quantity; // positive = IN, negative = OUT
    private UUID referenceId;
    private String referenceType; // VISIT, INVOICE, MANUAL
    private String batchNumber;
    private LocalDate expirationDate;
    private BigDecimal unitCost;
    private String notes;
    private String createdBy;
}
```

**SupplierInvoice.java**
```java
@Entity
@Table(name = "supplier_invoices")
public class SupplierInvoice extends TenantAwareEntity {
    private String invoiceNumber;
    private String supplierName;
    private String supplierNip;
    private LocalDate invoiceDate;
    private BigDecimal totalAmount;
    private String fileName;

    @Lob
    private String rawContent;

    @Enumerated(EnumType.STRING)
    private InvoiceStatus status; // PENDING, PROCESSED, ERROR

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL)
    private List<SupplierInvoiceItem> items;
}
```

### 2. Invoice Parser Service

**InvoiceParserService.java**
```java
@Service
public class InvoiceParserService {

    public ParsedInvoice parseInvoice(String content) {
        // Detect format (fixed-width, CSV, etc.)
        // Parse header (supplier, dates, invoice number)
        // Parse line items
        // Return structured data
    }

    private List<ParsedItem> parseFixedWidthItems(String[] lines) {
        // Parse KAMSOFT format (as seen in example)
        // Extract: code, name, qty, unit, price, expiry, batch, barcode
    }
}
```

### 3. Inventory Service

**InventoryService.java**
```java
@Service
public class InventoryService {

    // Import invoice items to stock
    public void processInvoice(UUID invoiceId) {
        // For each invoice item:
        // 1. Match to existing price_list_item or create new
        // 2. Increase stock_quantity
        // 3. Create RECEIPT transaction
    }

    // Decrease stock when visit uses materials
    public void recordUsage(UUID visitId, List<UsedMaterial> materials) {
        // For each material:
        // 1. Decrease stock_quantity
        // 2. Create USAGE transaction
    }

    // Manual adjustment
    public void adjustStock(UUID itemId, int newQuantity, String reason) {
        // Calculate difference
        // Update stock_quantity
        // Create ADJUSTMENT transaction
    }

    // Get low stock items
    public List<PriceListItem> getLowStockItems() {
        // Return items where stock_quantity < reorder_point
    }
}
```

### 4. New API Endpoints

```
POST   /api/v1/inventory/invoices/upload     - Upload invoice file
GET    /api/v1/inventory/invoices            - List all invoices
GET    /api/v1/inventory/invoices/{id}       - Get invoice with items
POST   /api/v1/inventory/invoices/{id}/process - Process invoice (add to stock)

GET    /api/v1/inventory/stock               - Get all items with stock levels
PATCH  /api/v1/inventory/stock/{itemId}      - Adjust stock manually
GET    /api/v1/inventory/stock/low           - Get low stock items

GET    /api/v1/inventory/transactions        - Transaction history
GET    /api/v1/inventory/transactions/{itemId} - Transaction history for item
```

---

## Frontend Implementation

### 1. Inventory Page (Refactor existing InventoryUsagePage)

**Tabs:**
- **Stock Overview** - Current stock levels, search, filter
- **Invoice Import** - Upload and process invoices
- **Transactions** - History of all stock movements
- **Low Stock Alerts** - Items below reorder point

### 2. Invoice Import Flow

```
[Upload Zone] - Drag & drop or click to upload
      ↓
[Preview Table] - Show parsed items with:
  - Product name
  - Quantity
  - Unit
  - Price
  - Expiry date
  - Match status (existing/new)
      ↓
[Confirm Button] - Process and add to stock
```

### 3. MaterialsSelector Enhancement

```typescript
// Only show items with stock > 0
const availableItems = priceListItems.filter(item => item.stockQuantity > 0);

// Show stock level in dropdown
<option>
  {item.name} - {item.stockQuantity} {item.unit} in stock
</option>

// Warn if quantity exceeds stock
if (selectedQuantity > item.stockQuantity) {
  showWarning("Not enough stock!");
}
```

### 4. Stock Adjustment Modal

```
[Item Name]
Current Stock: 25 OP.
New Stock: [input field]
Reason: [dropdown: Expired, Damaged, Miscounted, Other]
Notes: [text area]
[Save] [Cancel]
```

---

## Implementation Phases

### Phase 1: Database & Backend Foundation
1. Create migration for schema changes
2. Add stockQuantity field to PriceListItem
3. Create InventoryTransaction entity
4. Create SupplierInvoice/SupplierInvoiceItem entities
5. Create InvoiceParserService
6. Create InventoryService
7. Create InventoryController

### Phase 2: Invoice Import
1. Create file upload endpoint
2. Implement KAMSOFT format parser
3. Create invoice preview API
4. Implement invoice processing (add to stock)
5. Frontend: Invoice upload UI

### Phase 3: Stock Management
1. Integrate with VisitService (decrease stock on save)
2. Update MaterialsSelector to check stock
3. Create stock adjustment UI
4. Create low stock alerts

### Phase 4: Reporting
1. Transaction history page
2. Stock valuation report
3. Usage by date range report
4. Expiring items report

---

## Technical Considerations

### Stock Depletion Timing
- Decrease stock when visit is SAVED (not when materials selected)
- If visit updated, recalculate difference
- If visit deleted, return stock

### Negative Stock Prevention
- Option 1: Hard prevent (reject if not enough stock)
- Option 2: Soft warning (allow but warn)
- Recommend: Soft warning for flexibility

### Multi-Unit Handling
- Items may come in OP. (packages) but used as SZT. (pieces)
- May need unit conversion factor

### Invoice Format Support
- Start with KAMSOFT fixed-width format
- Design parser to be extensible for other formats
- Consider CSV, Excel support later

### Audit Trail
- All stock changes recorded in transactions
- Include who made the change
- Include reason for adjustments

---

## API Types (Frontend)

```typescript
interface InventoryItem extends PriceListItemResponse {
  stockQuantity: number;
  reorderPoint: number;
  barcode?: string;
  supplierCode?: string;
}

interface InventoryTransaction {
  id: string;
  itemId: string;
  itemName: string;
  transactionType: 'RECEIPT' | 'USAGE' | 'ADJUSTMENT' | 'EXPIRED' | 'RETURN';
  quantity: number;
  referenceId?: string;
  referenceType?: 'VISIT' | 'INVOICE' | 'MANUAL';
  batchNumber?: string;
  expirationDate?: string;
  unitCost?: number;
  notes?: string;
  createdBy?: string;
  createdAt: string;
}

interface SupplierInvoice {
  id: string;
  invoiceNumber: string;
  supplierName: string;
  invoiceDate: string;
  totalAmount: number;
  status: 'PENDING' | 'PROCESSED' | 'ERROR';
  items: SupplierInvoiceItem[];
}

interface SupplierInvoiceItem {
  id: string;
  productCode: string;
  productName: string;
  quantity: number;
  unit: string;
  unitPrice: number;
  batchNumber?: string;
  expirationDate?: string;
  barcode?: string;
  matchedItemId?: string;
  matched: boolean;
}
```

---

## Success Metrics

1. **Accuracy**: Stock levels match physical count
2. **Efficiency**: Invoice import < 30 seconds
3. **Visibility**: Low stock items clearly visible
4. **Traceability**: Full transaction history available
5. **Usability**: Vets can manage inventory without training

---

## Questions to Clarify

1. Should we prevent using items with 0 stock, or just warn?
2. Do we need to track multiple batches/expiry dates per item?
3. Should we support multiple invoice formats?
4. Do we need supplier management (contacts, history)?
5. Should expired items be automatically removed from available stock?
