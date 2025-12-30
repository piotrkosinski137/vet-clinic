import { useState, useMemo } from "react";
import { api } from "../api";
import type { PriceListItemResponse, PriceListItemRequest, ItemCategory } from "../api/types";
import { usePriceList, formatCurrency } from "../hooks";
import {
  Button,
  Card,
  Text,
  Loading,
  Spinner,
  Input,
  Select,
  FormField,
  Modal,
  ModalTitle,
  ModalActions,
  Badge,
  useToast,
} from "../components/ui";
import { useI18n } from "../i18n";
import { colors, spacing, borderRadius, fontSize, fontWeight } from "../theme";

type TabType = "materials" | "procedures";

// Categories for materials (with cost)
const MATERIAL_CATEGORIES: ItemCategory[] = ["MEDICATION", "PRODUCT", "VACCINATION", "LAB_TEST", "OTHER"];

// Categories for procedures/services (100% profit, cost = 0)
const PROCEDURE_CATEGORIES: ItemCategory[] = ["PROCEDURE", "SERVICE", "CONSULTATION"];

const ITEMS_PER_PAGE = 10;

// Type guard for ItemCategory
const isItemCategory = (value: string): value is ItemCategory => {
  const validCategories: ItemCategory[] = [
    'SERVICE',
    'MEDICATION',
    'PRODUCT',
    'PROCEDURE',
    'CONSULTATION',
    'LAB_TEST',
    'VACCINATION',
    'OTHER'
  ];
  return validCategories.includes(value as ItemCategory);
};

export function PriceListPage() {
  const { t } = useI18n();
  const { success, error: showError } = useToast();
  const { items, loading, refetch } = usePriceList({ active: true });

  const [activeTab, setActiveTab] = useState<TabType>("materials");
  const [showAddModal, setShowAddModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [editingItem, setEditingItem] = useState<PriceListItemResponse | null>(null);
  const [searchQuery, setSearchQuery] = useState("");
  const [currentPage, setCurrentPage] = useState(0);
  const [submitting, setSubmitting] = useState(false);

  // Form state
  const [formData, setFormData] = useState<PriceListItemRequest>({
    name: "",
    description: "",
    category: "MEDICATION",
    costPrice: 0,
    sellPrice: 0,
    unit: "szt.",
    active: true,
    code: "",
  });

  // Filter items based on tab
  const materials = useMemo(() => {
    return items.filter((item) => MATERIAL_CATEGORIES.includes(item.category));
  }, [items]);

  const procedures = useMemo(() => {
    return items.filter((item) => PROCEDURE_CATEGORIES.includes(item.category));
  }, [items]);

  const currentItems = activeTab === "materials" ? materials : procedures;

  // Filter by search
  const filteredItems = useMemo(() => {
    if (!searchQuery) return currentItems;
    const query = searchQuery.toLowerCase();
    return currentItems.filter(
      (item) =>
        item.name.toLowerCase().includes(query) ||
        item.description?.toLowerCase().includes(query) ||
        item.code?.toLowerCase().includes(query)
    );
  }, [currentItems, searchQuery]);

  // Pagination
  const paginatedItems = filteredItems.slice(
    currentPage * ITEMS_PER_PAGE,
    (currentPage + 1) * ITEMS_PER_PAGE
  );
  const totalPages = Math.ceil(filteredItems.length / ITEMS_PER_PAGE);

  const handleTabChange = (tab: TabType) => {
    setActiveTab(tab);
    setSearchQuery("");
    setCurrentPage(0);
  };

  const handleSearchChange = (value: string) => {
    setSearchQuery(value);
    setCurrentPage(0);
  };

  const resetForm = () => {
    const defaultCategory = activeTab === "materials" ? "MEDICATION" : "PROCEDURE";
    setFormData({
      name: "",
      description: "",
      category: defaultCategory,
      costPrice: activeTab === "procedures" ? 0 : 0,
      sellPrice: 0,
      unit: activeTab === "procedures" ? "zabieg" : "szt.",
      active: true,
      code: "",
    });
  };

  const openAddModal = () => {
    resetForm();
    setShowAddModal(true);
  };

  const openEditModal = (item: PriceListItemResponse) => {
    setEditingItem(item);
    setFormData({
      name: item.name,
      description: item.description || "",
      category: item.category,
      costPrice: item.costPrice,
      sellPrice: item.sellPrice,
      unit: item.unit || "szt.",
      active: item.active ?? true,
      code: item.code || "",
    });
    setShowEditModal(true);
  };

  const closeModals = () => {
    setShowAddModal(false);
    setShowEditModal(false);
    setEditingItem(null);
    resetForm();
  };

  const handleAdd = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      // For procedures, cost is always 0
      const dataToSend = {
        ...formData,
        costPrice: activeTab === "procedures" ? 0 : formData.costPrice,
      };
      await api.createPriceListItem(dataToSend);
      success(t("priceList.itemAdded"));
      closeModals();
      refetch();
    } catch (err) {
      showError(t("errors.failedToSave"));
    } finally {
      setSubmitting(false);
    }
  };

  const handleEdit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!editingItem) return;
    setSubmitting(true);
    try {
      const dataToSend = {
        ...formData,
        costPrice: PROCEDURE_CATEGORIES.includes(formData.category) ? 0 : formData.costPrice,
      };
      await api.updatePriceListItem(editingItem.id, dataToSend);
      success(t("priceList.itemUpdated"));
      closeModals();
      refetch();
    } catch (err) {
      showError(t("errors.failedToSave"));
    } finally {
      setSubmitting(false);
    }
  };

  const calculateMargin = (cost: number, sell: number): string => {
    if (cost === 0) return "100%";
    const margin = ((sell - cost) / cost) * 100;
    return `${margin.toFixed(0)}%`;
  };

  const getCategoryLabel = (category: ItemCategory): string => {
    return t(`priceList.categories.${category}`);
  };

  const currentCategories = activeTab === "materials" ? MATERIAL_CATEGORIES : PROCEDURE_CATEGORIES;

  if (loading) {
    return (
      <div style={{ padding: spacing.xl, textAlign: "center" }}>
        <Loading />
        <Text variant="muted">{t("common.loading")}</Text>
      </div>
    );
  }

  return (
    <div>
      {/* Header */}
      <div
        style={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
          marginBottom: spacing.lg,
        }}
      >
        <h1 style={{ color: colors.secondary.main, margin: 0 }}>{t("priceList.title")}</h1>
      </div>

      {/* Tabs */}
      <div
        style={{
          display: "flex",
          gap: spacing.xs,
          marginBottom: spacing.lg,
          borderBottom: `2px solid ${colors.neutral.border}`,
        }}
      >
        <button
          onClick={() => handleTabChange("materials")}
          style={{
            padding: `${spacing.sm} ${spacing.lg}`,
            border: "none",
            backgroundColor: "transparent",
            cursor: "pointer",
            fontSize: fontSize.md,
            fontWeight: activeTab === "materials" ? fontWeight.semibold : fontWeight.normal,
            color: activeTab === "materials" ? colors.primary.main : colors.neutral.textMuted,
            borderBottom: activeTab === "materials" ? `3px solid ${colors.primary.main}` : "3px solid transparent",
            marginBottom: "-2px",
            transition: "all 0.2s",
          }}
        >
          💊 {t("priceList.materialsTab")} ({materials.length})
        </button>
        <button
          onClick={() => handleTabChange("procedures")}
          style={{
            padding: `${spacing.sm} ${spacing.lg}`,
            border: "none",
            backgroundColor: "transparent",
            cursor: "pointer",
            fontSize: fontSize.md,
            fontWeight: activeTab === "procedures" ? fontWeight.semibold : fontWeight.normal,
            color: activeTab === "procedures" ? colors.success.main : colors.neutral.textMuted,
            borderBottom: activeTab === "procedures" ? `3px solid ${colors.success.main}` : "3px solid transparent",
            marginBottom: "-2px",
            transition: "all 0.2s",
          }}
        >
          🏥 {t("priceList.proceduresTab")} ({procedures.length})
        </button>
      </div>

      {/* Search and Add */}
      <div
        style={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
          marginBottom: spacing.md,
          gap: spacing.md,
        }}
      >
        <Input
          type="text"
          value={searchQuery}
          onChange={(e) => handleSearchChange(e.target.value)}
          placeholder={t("common.search") + "..."}
          style={{ maxWidth: "300px" }}
        />
        <Button variant="primary" onClick={openAddModal}>
          + {activeTab === "materials" ? t("priceList.addMaterial") : t("priceList.addProcedure")}
        </Button>
      </div>

      {/* Table */}
      {filteredItems.length === 0 ? (
        <Card style={{ textAlign: "center", padding: spacing.xxl }}>
          <Text variant="muted" style={{ fontSize: fontSize.lg }}>
            {activeTab === "materials" ? t("priceList.noMaterials") : t("priceList.noProcedures")}
          </Text>
        </Card>
      ) : (
        <Card>
          {/* Table Header */}
          <div
            style={{
              display: "grid",
              gridTemplateColumns: activeTab === "materials"
                ? "2fr 120px 100px 100px 80px 80px"
                : "2fr 120px 100px 80px 80px",
              gap: spacing.sm,
              padding: spacing.sm,
              backgroundColor: colors.secondary.main,
              color: colors.neutral.white,
              borderRadius: `${borderRadius.sm} ${borderRadius.sm} 0 0`,
              fontSize: fontSize.sm,
              fontWeight: fontWeight.semibold,
            }}
          >
            <div>{t("priceList.name")}</div>
            <div>{t("priceList.category")}</div>
            {activeTab === "materials" && <div style={{ textAlign: "right" }}>{t("priceList.costPrice")}</div>}
            <div style={{ textAlign: "right" }}>{t("priceList.sellPrice")}</div>
            <div style={{ textAlign: "center" }}>{t("priceList.margin")}</div>
            <div style={{ textAlign: "center" }}>{t("common.edit")}</div>
          </div>

          {/* Table Rows */}
          {paginatedItems.map((item) => (
            <div
              key={item.id}
              style={{
                display: "grid",
                gridTemplateColumns: activeTab === "materials"
                  ? "2fr 120px 100px 100px 80px 80px"
                  : "2fr 120px 100px 80px 80px",
                gap: spacing.sm,
                padding: spacing.sm,
                backgroundColor: colors.neutral.background,
                borderBottom: `1px solid ${colors.neutral.border}`,
                fontSize: fontSize.sm,
                alignItems: "center",
              }}
            >
              <div>
                <Text style={{ fontWeight: fontWeight.medium }}>{item.name}</Text>
                {item.description && (
                  <Text variant="muted" size="sm">
                    {item.description}
                  </Text>
                )}
                {item.unit && (
                  <Text variant="muted" size="sm">
                    {t("priceList.unit")}: {item.unit}
                  </Text>
                )}
              </div>
              <div>
                <Badge variant="secondary">{getCategoryLabel(item.category)}</Badge>
              </div>
              {activeTab === "materials" && (
                <div style={{ textAlign: "right", color: colors.danger.main }}>
                  {formatCurrency(item.costPrice)}
                </div>
              )}
              <div style={{ textAlign: "right", fontWeight: fontWeight.medium, color: colors.primary.main }}>
                {formatCurrency(item.sellPrice)}
              </div>
              <div style={{ textAlign: "center" }}>
                <Badge variant={item.costPrice === 0 ? "success" : "warning"}>
                  {calculateMargin(item.costPrice, item.sellPrice)}
                </Badge>
              </div>
              <div style={{ textAlign: "center" }}>
                <Button variant="ghost" size="sm" onClick={() => openEditModal(item)}>
                  {t("common.edit")}
                </Button>
              </div>
            </div>
          ))}

          {/* Summary Row */}
          <div
            style={{
              display: "grid",
              gridTemplateColumns: activeTab === "materials"
                ? "2fr 120px 100px 100px 80px 80px"
                : "2fr 120px 100px 80px 80px",
              gap: spacing.sm,
              padding: spacing.sm,
              backgroundColor: colors.primary.light,
              borderRadius: `0 0 ${borderRadius.sm} ${borderRadius.sm}`,
              fontSize: fontSize.sm,
              fontWeight: fontWeight.bold,
            }}
          >
            <div>{t("common.total")}: {filteredItems.length}</div>
            <div></div>
            {activeTab === "materials" && <div></div>}
            <div></div>
            <div></div>
            <div></div>
          </div>
        </Card>
      )}

      {/* Pagination */}
      {totalPages > 1 && (
        <div
          style={{
            display: "flex",
            justifyContent: "center",
            alignItems: "center",
            gap: spacing.md,
            marginTop: spacing.lg,
          }}
        >
          <Button
            variant="ghost"
            size="sm"
            onClick={() => setCurrentPage((p) => Math.max(0, p - 1))}
            disabled={currentPage === 0}
          >
            ← Previous
          </Button>
          <Text size="sm">
            Page {currentPage + 1} of {totalPages}
          </Text>
          <Button
            variant="ghost"
            size="sm"
            onClick={() => setCurrentPage((p) => Math.min(totalPages - 1, p + 1))}
            disabled={currentPage >= totalPages - 1}
          >
            Next →
          </Button>
        </div>
      )}

      {/* Add Modal */}
      <Modal open={showAddModal} onClose={closeModals}>
        <ModalTitle>
          {activeTab === "materials" ? t("priceList.addMaterial") : t("priceList.addProcedure")}
        </ModalTitle>
        <form onSubmit={handleAdd}>
          <FormField label={t("priceList.name")} required>
            <Input
              type="text"
              value={formData.name}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
              placeholder={t("priceList.name")}
              required
            />
          </FormField>

          <FormField label={t("priceList.description")}>
            <Input
              type="text"
              value={formData.description || ""}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
              placeholder={t("priceList.description")}
            />
          </FormField>

          <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: spacing.md }}>
            <FormField label={t("priceList.category")} required>
              <Select
                value={formData.category}
                onChange={(e) => {
                  const value = e.target.value;
                  if (isItemCategory(value)) {
                    setFormData({ ...formData, category: value });
                  }
                }}
              >
                {currentCategories.map((cat) => (
                  <option key={cat} value={cat}>
                    {getCategoryLabel(cat)}
                  </option>
                ))}
              </Select>
            </FormField>

            <FormField label={t("priceList.unit")}>
              <Input
                type="text"
                value={formData.unit || ""}
                onChange={(e) => setFormData({ ...formData, unit: e.target.value })}
                placeholder={activeTab === "procedures" ? "zabieg" : "szt."}
              />
            </FormField>
          </div>

          <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: spacing.md }}>
            {activeTab === "materials" && (
              <FormField label={t("priceList.costPrice")} required>
                <Input
                  type="number"
                  step="0.01"
                  min="0"
                  value={formData.costPrice}
                  onChange={(e) => setFormData({ ...formData, costPrice: parseFloat(e.target.value) || 0 })}
                  required
                />
              </FormField>
            )}

            <FormField label={t("priceList.sellPrice")} required>
              <Input
                type="number"
                step="0.01"
                min="0"
                value={formData.sellPrice}
                onChange={(e) => setFormData({ ...formData, sellPrice: parseFloat(e.target.value) || 0 })}
                required
              />
            </FormField>
          </div>

          {activeTab === "materials" && formData.costPrice > 0 && formData.sellPrice > 0 && (
            <div
              style={{
                padding: spacing.sm,
                backgroundColor: colors.success.light,
                borderRadius: borderRadius.sm,
                marginBottom: spacing.md,
              }}
            >
              <Text size="sm">
                {t("priceList.margin")}: <strong>{calculateMargin(formData.costPrice, formData.sellPrice)}</strong>
                {" | "}
                {t("common.profit")}: <strong style={{ color: colors.success.main }}>
                  {formatCurrency(formData.sellPrice - formData.costPrice)}
                </strong>
              </Text>
            </div>
          )}

          {activeTab === "procedures" && (
            <div
              style={{
                padding: spacing.sm,
                backgroundColor: colors.success.light,
                borderRadius: borderRadius.sm,
                marginBottom: spacing.md,
                borderLeft: `3px solid ${colors.success.main}`,
              }}
            >
              <Text size="sm" style={{ color: colors.success.main }}>
                100% {t("common.profit")} - {t("procedures.fullProfit")}
              </Text>
            </div>
          )}

          <ModalActions>
            <Button type="button" variant="ghost" onClick={closeModals}>
              {t("common.cancel")}
            </Button>
            <Button type="submit" variant="primary" disabled={submitting}>
              {submitting && <Spinner size="sm" style={{ marginRight: spacing.xs }} />}
              {submitting ? t("priceList.adding") : t("common.add")}
            </Button>
          </ModalActions>
        </form>
      </Modal>

      {/* Edit Modal */}
      <Modal open={showEditModal} onClose={closeModals}>
        <ModalTitle>{t("common.edit")}</ModalTitle>
        <form onSubmit={handleEdit}>
          <FormField label={t("priceList.name")} required>
            <Input
              type="text"
              value={formData.name}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
              placeholder={t("priceList.name")}
              required
            />
          </FormField>

          <FormField label={t("priceList.description")}>
            <Input
              type="text"
              value={formData.description || ""}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
              placeholder={t("priceList.description")}
            />
          </FormField>

          <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: spacing.md }}>
            <FormField label={t("priceList.category")} required>
              <Select
                value={formData.category}
                onChange={(e) => {
                  const value = e.target.value;
                  if (isItemCategory(value)) {
                    setFormData({ ...formData, category: value });
                  }
                }}
              >
                {[...MATERIAL_CATEGORIES, ...PROCEDURE_CATEGORIES].map((cat) => (
                  <option key={cat} value={cat}>
                    {getCategoryLabel(cat)}
                  </option>
                ))}
              </Select>
            </FormField>

            <FormField label={t("priceList.unit")}>
              <Input
                type="text"
                value={formData.unit || ""}
                onChange={(e) => setFormData({ ...formData, unit: e.target.value })}
              />
            </FormField>
          </div>

          <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: spacing.md }}>
            {!PROCEDURE_CATEGORIES.includes(formData.category) && (
              <FormField label={t("priceList.costPrice")} required>
                <Input
                  type="number"
                  step="0.01"
                  min="0"
                  value={formData.costPrice}
                  onChange={(e) => setFormData({ ...formData, costPrice: parseFloat(e.target.value) || 0 })}
                  required
                />
              </FormField>
            )}

            <FormField label={t("priceList.sellPrice")} required>
              <Input
                type="number"
                step="0.01"
                min="0"
                value={formData.sellPrice}
                onChange={(e) => setFormData({ ...formData, sellPrice: parseFloat(e.target.value) || 0 })}
                required
              />
            </FormField>
          </div>

          {!PROCEDURE_CATEGORIES.includes(formData.category) && formData.costPrice > 0 && formData.sellPrice > 0 && (
            <div
              style={{
                padding: spacing.sm,
                backgroundColor: colors.success.light,
                borderRadius: borderRadius.sm,
                marginBottom: spacing.md,
              }}
            >
              <Text size="sm">
                {t("priceList.margin")}: <strong>{calculateMargin(formData.costPrice, formData.sellPrice)}</strong>
                {" | "}
                {t("common.profit")}: <strong style={{ color: colors.success.main }}>
                  {formatCurrency(formData.sellPrice - formData.costPrice)}
                </strong>
              </Text>
            </div>
          )}

          {PROCEDURE_CATEGORIES.includes(formData.category) && (
            <div
              style={{
                padding: spacing.sm,
                backgroundColor: colors.success.light,
                borderRadius: borderRadius.sm,
                marginBottom: spacing.md,
                borderLeft: `3px solid ${colors.success.main}`,
              }}
            >
              <Text size="sm" style={{ color: colors.success.main }}>
                100% {t("common.profit")} - {t("procedures.fullProfit")}
              </Text>
            </div>
          )}

          <ModalActions>
            <Button type="button" variant="ghost" onClick={closeModals}>
              {t("common.cancel")}
            </Button>
            <Button type="submit" variant="primary" disabled={submitting}>
              {submitting && <Spinner size="sm" style={{ marginRight: spacing.xs }} />}
              {submitting ? t("common.saving") : t("common.save")}
            </Button>
          </ModalActions>
        </form>
      </Modal>
    </div>
  );
}
