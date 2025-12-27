# Klinika XP - Modules & Features (Priority Order)

---

# MVP (Phase 1) - Core Operations

## 1. Core CRM (Kartoteki)

| Feature | Priority | Description |
|---------|----------|-------------|
| Owner Registry | MVP | Basic client data, contact info, address |
| Animal Registry | MVP | Patient data, species, breed, medical history link |
| Basic Search | MVP | By name, phone, microchip |
| Owner-Animal Linking | MVP | Multiple animals per owner |

## 2. Visits & Basic Documentation

| Feature | Priority | Description |
|---------|----------|-------------|
| Visit Registration | MVP | Interview, examination, diagnosis, medications |
| Visit History | MVP | View past visits per patient |
| Basic Notes | MVP | Free-text medical notes |
| Print Visit Summary | MVP | Basic printout for client |

## 3. Basic Scheduling

| Feature | Priority | Description |
|---------|----------|-------------|
| Appointment Calendar | MVP | Daily/weekly view per doctor |
| Book Appointment | MVP | Assign patient to time slot |
| Appointment Status | MVP | Scheduled, in progress, completed, cancelled |

## 4. Basic Billing

| Feature | Priority | Description |
|---------|----------|-------------|
| Price List | MVP | Services and products with prices |
| Invoice Generation | MVP | Create invoice from visit |
| Payment Recording | MVP | Mark as paid/unpaid |
| Basic Invoice Print | MVP | PDF/print invoice |

---

# Phase 2 - Essential Extensions

## 5. Basic Inventory

| Feature | Priority | Description |
|---------|----------|-------------|
| Product Catalog | P2 | Medications, supplies with stock levels |
| Manual Stock Updates | P2 | Add/remove stock manually |
| Low Stock Alerts | P2 | Notification when below threshold |
| Expiry Date Tracking | P2 | Basic expiry alerts |

## 6. Core Compliance

| Feature | Priority | Description |
|---------|----------|-------------|
| Audit Log | P2 | Track who changed what and when |
| Document Versioning | P2 | No deletion, only corrections |
| GDPR Consents | P2 | Generate and store consent forms |
| Vaccination Certificates | P2 | Basic certificate generation |

## 7. Enhanced CRM

| Feature | Priority | Description |
|---------|----------|-------------|
| Advanced Search | P2 | By characteristics, labels, debt status |
| Patient Labels | P2 | Aggressive, allergic, VIP markers |
| Debt Tracking | P2 | Outstanding balance per client |
| Change History | P2 | Full edit history on records |

---

# Phase 3 - Full Operations

## 8. Full Inventory & Logistics

| Feature | Priority | Description |
|---------|----------|-------------|
| Auto Stock Deduction | P3 | Deduct from visit usage |
| Batch Management | P3 | Track batches separately |
| Unit Conversion | P3 | Buy in bulk, sell in units |
| Invoice Import | P3 | XML/CSV from wholesalers |
| Multi-level Pricing | P3 | Different prices per client group |
| Inventory Stocktaking | P3 | Periodic stock audits |

## 9. Full Finance & Billing

| Feature | Priority | Description |
|---------|----------|-------------|
| Fiscal Printer Integration | P3 | Posnet, Thermal, Elzab |
| JPK Generation | P3 | JPK_VAT, JPK_FA compliance |
| Cash Register (KP/KW) | P3 | Cash reports, multi-register |
| Doctor Commissions | P3 | Calculate earnings per doctor |
| Payment Terminal | P3 | Auto-send amount to terminal |

## 10. Templates & Efficiency

| Feature | Priority | Description |
|---------|----------|-------------|
| Autotext & Templates | P3 | Reusable text blocks |
| Treatment Schemes | P3 | Predefined bundles (e.g., deworming set) |
| Drug Vademecum | P3 | Dosing database per species |
| Keyboard Shortcuts | P3 | Speed up data entry |

---

# Phase 4 - Advanced Features

## 11. Hospital Module

| Feature | Priority | Description |
|---------|----------|-------------|
| Cage Management | P4 | Assign/release hospital slots |
| Monitoring Card | P4 | Hourly logging (meds, feeding, walks) |
| Anesthesia Card | P4 | Sedation monitoring form |
| Dose Calculator | P4 | Weight-based with safety limits |
| Post-op Orders | P4 | Task reminders for technicians |
| Discharge Instructions | P4 | Auto-generate home care docs |

## 12. Advanced Scheduling & CRM

| Feature | Priority | Description |
|---------|----------|-------------|
| SMS/Email Reminders | P4 | Auto-notifications |
| Resource Booking | P4 | OR, equipment reservation |
| Tasks & Notes | P4 | Internal staff communication |
| Queue System | P4 | Waiting room display |

## 13. Basic Diagnostics

| Feature | Priority | Description |
|---------|----------|-------------|
| Lab Results Entry | P4 | Manual entry of test results |
| Result History | P4 | View past lab results |
| Attach Files | P4 | Upload images, PDFs to visit |

## 14. Extended Compliance

| Feature | Priority | Description |
|---------|----------|-------------|
| Treatment Book | P4 | Veterinary Inspection documents |
| Passport Registry | P4 | KIF integration |
| Controlled Substances | P4 | Special ledger for narcotics |
| KSeF Integration | P4 | e-Invoice system |

---

# Phase 5 - Premium Features

## 15. Full Diagnostics Integration

| Feature | Priority | Description |
|---------|----------|-------------|
| DICOM Viewer | P5 | RTG, USG, MRI viewing |
| DICOM Worklist | P5 | Send patient to RTG console |
| Analyzer Integration | P5 | Auto-import from Idexx, Mindray |
| External Labs | P5 | Online orders to VetLab, Laboklin |
| HL7/ASTM Support | P5 | Standard analyzer protocols |

## 16. Marketing & Loyalty

| Feature | Priority | Description |
|---------|----------|-------------|
| Targeted SMS Campaigns | P5 | Filter by breed, age for marketing |
| Loyalty Programs | P5 | Points, discounts |
| Satisfaction Surveys | P5 | NPS after visits |
| VoIP Integration | P5 | Caller ID popup |

## 17. Specialized Modules

| Feature | Priority | Description |
|---------|----------|-------------|
| Dental Chart | P5 | Interactive tooth diagram |
| Glucose Curves | P5 | Charts from glucometer |
| Specialized Exam Forms | P5 | Ortho, cardio, derma templates |
| Shelter Module | P5 | Intake, quarantine, adoption |
| Insemination Module | P5 | Breeding documentation |
| Farm Animals | P5 | Herd management, withdrawal periods |

## 18. Client-facing & AI

| Feature | Priority | Description |
|---------|----------|-------------|
| Client Mobile App | P5 | View results, health booklet |
| Smart Assistant | P5 | AI diagnosis suggestions |
| Self-service Kiosk | P5 | Tablet check-in |
| TV Display | P5 | Waiting room queue |

## 19. Enterprise Features

| Feature | Priority | Description |
|---------|----------|-------------|
| Multisite Support | P5 | Shared DB across locations |
| Multi-entity Billing | P5 | Separate fiscal per entity |
| B2B Billing | P5 | Wholesale clients, batch invoicing |
| Custom Reports | P5 | SQL-based report builder |
| Offline Mode | P5 | Local DB with cloud sync |

---

# Summary

| Phase | Focus | Modules |
|-------|-------|---------|
| **MVP** | Can operate clinic | CRM, Visits, Calendar, Billing |
| **P2** | Daily essentials | Inventory basics, Compliance, Enhanced CRM |
| **P3** | Full operations | Full Inventory, Full Finance, Templates |
| **P4** | Advanced care | Hospital, Advanced CRM, Basic Diagnostics |
| **P5** | Premium | Full Diagnostics, Marketing, Specialized, AI, Enterprise |
