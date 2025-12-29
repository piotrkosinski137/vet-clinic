import { useState, useRef, useEffect, useCallback, useLayoutEffect } from "react";
import { createPortal } from "react-dom";
import {
  usePriceList,
  CATEGORY_LABELS,
  calculateTotalCost,
  calculateTotalSell,
  calculateProfit,
  formatCurrency,
} from "../../hooks";
import type { PriceListItemResponse, ItemCategory, UsedMaterialDto } from "../../api/types";
import { Text, Badge } from "../ui";
import { useI18n } from "../../i18n";
import { colors, spacing, borderRadius, fontSize, fontWeight, zIndex } from "../../theme";

interface MaterialsSelectorProps {
  value: UsedMaterialDto[];
  onChange: (materials: UsedMaterialDto[]) => void;
  readOnly?: boolean;
}

export function MaterialsSelector({ value, onChange, readOnly = false }: MaterialsSelectorProps) {
  const { t } = useI18n();
  const [searchQuery, setSearchQuery] = useState("");
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);
  const [selectedCategory, setSelectedCategory] = useState<ItemCategory | "">("");
  const [showOutOfStock, setShowOutOfStock] = useState(false);
  const [highlightedIndex, setHighlightedIndex] = useState(0);
  const [dropdownPosition, setDropdownPosition] = useState({ top: 0, left: 0, width: 0 });
  const dropdownRef = useRef<HTMLDivElement>(null);
  const portalRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<HTMLInputElement>(null);

  // Calculate dropdown position when it opens
  useLayoutEffect(() => {
    if (isDropdownOpen && inputRef.current) {
      const rect = inputRef.current.getBoundingClientRect();
      setDropdownPosition({
        top: rect.bottom + window.scrollY,
        left: rect.left + window.scrollX,
        width: rect.width,
      });
    }
  }, [isDropdownOpen]);

  const { items: allMaterials, loading, searchItems } = usePriceList({ active: true });
  const filteredMaterials = searchQuery || selectedCategory
    ? searchItems(searchQuery, selectedCategory || undefined)
    : allMaterials;

  // Filter out already selected materials and check stock
  const availableMaterials = filteredMaterials.filter(m => {
    // Already selected? Skip
    if (value.some(v => v.materialId === m.id)) return false;
    // Services (null stock) are always available - they don't need stock tracking
    if (m.stockQuantity === null || m.stockQuantity === undefined) return true;
    // Out of stock products? Only show if toggle is on
    if (m.stockQuantity <= 0 && !showOutOfStock) return false;
    return true;
  });

  const totalSell = calculateTotalSell(value);
  const totalCost = calculateTotalCost(value);
  const profit = calculateProfit(value);

  // Close dropdown when clicking outside
  useEffect(() => {
    function handleClickOutside(event: MouseEvent) {
      const target = event.target as Node;
      const isOutsideInput = dropdownRef.current && !dropdownRef.current.contains(target);
      const isOutsidePortal = portalRef.current && !portalRef.current.contains(target);
      if (isOutsideInput && isOutsidePortal) {
        setIsDropdownOpen(false);
      }
    }
    if (isDropdownOpen) {
      document.addEventListener("mousedown", handleClickOutside);
      return () => document.removeEventListener("mousedown", handleClickOutside);
    }
  }, [isDropdownOpen]);

  const handleSelectMaterial = useCallback((material: PriceListItemResponse) => {
    const newItem: UsedMaterialDto = {
      materialId: material.id,
      name: material.name,
      quantity: 1,
      costPrice: material.costPrice,
      sellPrice: material.sellPrice,
      unit: material.unit || "unit",
    };
    onChange([...value, newItem]);
    setSearchQuery("");
    setIsDropdownOpen(false);
    inputRef.current?.focus();
  }, [value, onChange]);

  const handleRemoveMaterial = useCallback((materialId: string) => {
    onChange(value.filter((m) => m.materialId !== materialId));
  }, [value, onChange]);

  const handleQuantityChange = useCallback((materialId: string, delta: number) => {
    onChange(
      value.map((m) => {
        if (m.materialId === materialId) {
          const newQuantity = Math.max(1, m.quantity + delta);
          return { ...m, quantity: newQuantity };
        }
        return m;
      })
    );
  }, [value, onChange]);

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (!isDropdownOpen) {
      if (e.key === "ArrowDown" || e.key === "Enter") {
        setIsDropdownOpen(true);
      }
      return;
    }

    switch (e.key) {
      case "ArrowDown":
        e.preventDefault();
        setHighlightedIndex((prev) => Math.min(prev + 1, availableMaterials.length - 1));
        break;
      case "ArrowUp":
        e.preventDefault();
        setHighlightedIndex((prev) => Math.max(prev - 1, 0));
        break;
      case "Enter":
        e.preventDefault();
        if (availableMaterials[highlightedIndex]) {
          handleSelectMaterial(availableMaterials[highlightedIndex]);
        }
        break;
      case "Escape":
        setIsDropdownOpen(false);
        break;
    }
  };

  const categories = Object.entries(CATEGORY_LABELS) as [ItemCategory, { label: string; icon: string }][];

  if (readOnly) {
    return (
      <div>
        {value.length === 0 ? (
          <Text variant="muted" size="sm">{t('materials.noMaterialsUsed')}</Text>
        ) : (
          <>
            <div style={{ display: "flex", flexDirection: "column", gap: spacing.xs }}>
              {value.map((item) => (
                <div
                  key={item.materialId}
                  style={{
                    display: "flex",
                    justifyContent: "space-between",
                    alignItems: "center",
                    padding: spacing.xs,
                    backgroundColor: colors.neutral.background,
                    borderRadius: borderRadius.sm,
                  }}
                >
                  <div>
                    <Text size="sm">{item.name}</Text>
                    <Text variant="muted" size="sm">
                      {item.quantity} {item.unit}(s) x {formatCurrency(item.sellPrice)}
                    </Text>
                  </div>
                  <Text style={{ fontWeight: fontWeight.medium }}>
                    {formatCurrency(item.quantity * item.sellPrice)}
                  </Text>
                </div>
              ))}
            </div>
            {/* Summary */}
            <div
              style={{
                marginTop: spacing.sm,
                paddingTop: spacing.sm,
                borderTop: `2px solid ${colors.neutral.border}`,
              }}
            >
              <div style={{ display: "flex", justifyContent: "space-between", marginBottom: spacing.xs }}>
                <Text variant="muted" size="sm">{t('materials.clientTotal')}</Text>
                <Text style={{ fontWeight: fontWeight.bold, color: colors.primary.main }}>
                  {formatCurrency(totalSell)}
                </Text>
              </div>
              <div style={{ display: "flex", justifyContent: "space-between", marginBottom: spacing.xs }}>
                <Text variant="muted" size="sm">{t('common.cost')}</Text>
                <Text variant="muted" size="sm">{formatCurrency(totalCost)}</Text>
              </div>
              <div style={{ display: "flex", justifyContent: "space-between" }}>
                <Text size="sm" style={{ fontWeight: fontWeight.medium, color: colors.success.main }}>
                  {t('common.profit')}
                </Text>
                <Text style={{ fontWeight: fontWeight.bold, color: colors.success.main }}>
                  {formatCurrency(profit)}
                </Text>
              </div>
            </div>
          </>
        )}
      </div>
    );
  }

  return (
    <div>
      {/* Category Filter */}
      <div style={{ display: "flex", gap: spacing.xs, marginBottom: spacing.sm, flexWrap: "wrap", alignItems: "center" }}>
        <button
          onClick={() => setSelectedCategory("")}
          style={{
            padding: `${spacing.xs} ${spacing.sm}`,
            borderRadius: borderRadius.full,
            border: "none",
            cursor: "pointer",
            fontSize: fontSize.xs,
            backgroundColor: selectedCategory === "" ? colors.primary.main : colors.neutral.background,
            color: selectedCategory === "" ? colors.neutral.white : colors.neutral.text,
          }}
        >
          {t('common.all')}
        </button>
        {categories.map(([cat, { label, icon }]) => (
          <button
            key={cat}
            onClick={() => setSelectedCategory(cat)}
            style={{
              padding: `${spacing.xs} ${spacing.sm}`,
              borderRadius: borderRadius.full,
              border: "none",
              cursor: "pointer",
              fontSize: fontSize.xs,
              backgroundColor: selectedCategory === cat ? colors.primary.main : colors.neutral.background,
              color: selectedCategory === cat ? colors.neutral.white : colors.neutral.text,
            }}
          >
            {icon} {label}
          </button>
        ))}
        <label style={{ display: 'flex', alignItems: 'center', gap: spacing.xs, marginLeft: 'auto' }}>
          <input
            type="checkbox"
            checked={showOutOfStock}
            onChange={(e) => setShowOutOfStock(e.target.checked)}
          />
          <Text size="sm">Show out of stock</Text>
        </label>
      </div>

      {/* Search Input with Dropdown */}
      <div ref={dropdownRef} style={{ position: "relative", marginBottom: spacing.md }}>
        <input
          ref={inputRef}
          type="text"
          value={searchQuery}
          onChange={(e) => {
            setSearchQuery(e.target.value);
            setIsDropdownOpen(true);
            setHighlightedIndex(0);
          }}
          onFocus={() => setIsDropdownOpen(true)}
          onKeyDown={handleKeyDown}
          placeholder={t('materials.searchPlaceholder')}
          style={{
            width: "100%",
            padding: spacing.sm,
            borderRadius: borderRadius.sm,
            border: `1px solid ${colors.neutral.border}`,
            fontSize: fontSize.sm,
            outline: "none",
          }}
        />

        {/* Dropdown - rendered via portal to escape modal overflow */}
        {isDropdownOpen && createPortal(
          <div
            ref={portalRef}
            style={{
              position: "fixed",
              top: dropdownPosition.top,
              left: dropdownPosition.left,
              width: dropdownPosition.width,
              backgroundColor: colors.neutral.white,
              border: `1px solid ${colors.neutral.border}`,
              borderRadius: borderRadius.sm,
              boxShadow: "0 4px 12px rgba(0,0,0,0.15)",
              maxHeight: "300px",
              overflowY: "auto",
              zIndex: zIndex.popover,
            }}
          >
            {loading ? (
              <div style={{ padding: spacing.md, textAlign: "center" }}>
                <Text variant="muted" size="sm">{t('common.loading')}</Text>
              </div>
            ) : availableMaterials.length === 0 ? (
              <div style={{ padding: spacing.md, textAlign: "center" }}>
                <Text variant="muted" size="sm">{t('common.noData')}</Text>
              </div>
            ) : (
              availableMaterials.map((material, index) => {
                const catInfo = CATEGORY_LABELS[material.category];
                const margin = material.sellPrice - material.costPrice;
                const marginPercent = ((margin / material.costPrice) * 100).toFixed(0);
                // Services have null stock - treat as unlimited
                const isService = material.stockQuantity === null || material.stockQuantity === undefined;
                const stock = material.stockQuantity ?? 0;
                const isLowStock = !isService && stock > 0 && stock <= (material.reorderPoint ?? 5);
                const isOutOfStock = !isService && stock <= 0;

                return (
                  <div
                    key={material.id}
                    onClick={() => !isOutOfStock && handleSelectMaterial(material)}
                    style={{
                      padding: spacing.sm,
                      cursor: isOutOfStock ? 'not-allowed' : 'pointer',
                      backgroundColor: index === highlightedIndex ? colors.primary.light :
                                      isOutOfStock ? colors.danger.light :
                                      isLowStock ? colors.warning.light : 'transparent',
                      borderBottom: `1px solid ${colors.neutral.border}`,
                      opacity: isOutOfStock ? 0.6 : 1,
                    }}
                    onMouseEnter={() => setHighlightedIndex(index)}
                  >
                    <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start" }}>
                      <div style={{ flex: 1 }}>
                        <div style={{ display: "flex", alignItems: "center", gap: spacing.xs }}>
                          <span>{catInfo.icon}</span>
                          <Text style={{ fontWeight: fontWeight.medium }}>{material.name}</Text>
                          {isOutOfStock && <Badge variant="danger" style={{ fontSize: '10px' }}>OUT</Badge>}
                          {isLowStock && <Badge variant="warning" style={{ fontSize: '10px' }}>LOW</Badge>}
                        </div>
                        <Text variant="muted" size="sm">
                          {material.description && `${material.description} - `}
                          {t('materials.perUnit')} {material.unit}
                        </Text>
                        <Text size="sm" style={{
                          color: isService ? colors.neutral.textMuted : isOutOfStock ? colors.danger.main : isLowStock ? colors.warning.main : colors.success.main,
                          fontWeight: fontWeight.medium,
                        }}>
                          {isService ? 'Service (unlimited)' : `Stock: ${stock} ${material.unit}`}
                        </Text>
                      </div>
                      <div style={{ textAlign: "right" }}>
                        <Text style={{ fontWeight: fontWeight.bold, color: colors.primary.main }}>
                          {formatCurrency(material.sellPrice)}
                        </Text>
                        <Text variant="muted" size="sm">
                          cost: {formatCurrency(material.costPrice)} (+{marginPercent}%)
                        </Text>
                      </div>
                    </div>
                  </div>
                );
              })
            )}
          </div>,
          document.body
        )}
      </div>

      {/* Selected Materials List */}
      {value.length > 0 && (
        <div style={{ marginBottom: spacing.md }}>
          <Text size="sm" style={{ fontWeight: fontWeight.medium, marginBottom: spacing.xs }}>
            {t('materials.selectedItems')} ({value.length})
          </Text>
          <div style={{ display: "flex", flexDirection: "column", gap: spacing.xs }}>
            {value.map((item) => {
              const itemProfit = (item.sellPrice - item.costPrice) * item.quantity;
              const originalItem = allMaterials.find(m => m.id === item.materialId);
              const availableStock = originalItem?.stockQuantity ?? Infinity;
              const exceedsStock = item.quantity > availableStock;

              return (
                <div
                  key={item.materialId}
                  style={{
                    display: "flex",
                    alignItems: "center",
                    gap: spacing.sm,
                    padding: spacing.sm,
                    backgroundColor: colors.neutral.background,
                    borderRadius: borderRadius.sm,
                  }}
                >
                  <div style={{ flex: 1 }}>
                    <Text size="sm" style={{ fontWeight: fontWeight.medium }}>{item.name}</Text>
                    <div style={{ display: "flex", gap: spacing.md }}>
                      <Text variant="muted" size="sm">
                        Sell: {formatCurrency(item.sellPrice)}
                      </Text>
                      <Text variant="muted" size="sm">
                        Cost: {formatCurrency(item.costPrice)}
                      </Text>
                    </div>
                    {exceedsStock && (
                      <Text size="sm" style={{ color: colors.danger.main }}>
                        ⚠️ Exceeds stock ({availableStock} available)
                      </Text>
                    )}
                  </div>

                  {/* Quantity Controls */}
                  <div style={{ display: "flex", alignItems: "center", gap: spacing.xs }}>
                    <button
                      onClick={() => handleQuantityChange(item.materialId, -1)}
                      disabled={item.quantity <= 1}
                      style={{
                        width: "28px",
                        height: "28px",
                        borderRadius: borderRadius.full,
                        border: "none",
                        backgroundColor: colors.neutral.border,
                        cursor: item.quantity <= 1 ? "not-allowed" : "pointer",
                        opacity: item.quantity <= 1 ? 0.5 : 1,
                        fontSize: fontSize.lg,
                        fontWeight: fontWeight.bold,
                      }}
                    >
                      -
                    </button>
                    <span
                      style={{
                        minWidth: "40px",
                        textAlign: "center",
                        fontWeight: fontWeight.medium,
                      }}
                    >
                      {item.quantity}
                    </span>
                    <button
                      onClick={() => handleQuantityChange(item.materialId, 1)}
                      style={{
                        width: "28px",
                        height: "28px",
                        borderRadius: borderRadius.full,
                        border: "none",
                        backgroundColor: colors.primary.main,
                        color: colors.neutral.white,
                        cursor: "pointer",
                        fontSize: fontSize.lg,
                        fontWeight: fontWeight.bold,
                      }}
                    >
                      +
                    </button>
                  </div>

                  {/* Item Totals */}
                  <div style={{ textAlign: "right", minWidth: "80px" }}>
                    <Text style={{ fontWeight: fontWeight.medium }}>
                      {formatCurrency(item.quantity * item.sellPrice)}
                    </Text>
                    <Text variant="muted" size="sm" style={{ color: colors.success.main }}>
                      +{formatCurrency(itemProfit)}
                    </Text>
                  </div>

                  {/* Remove Button */}
                  <button
                    onClick={() => handleRemoveMaterial(item.materialId)}
                    style={{
                      background: "none",
                      border: "none",
                      cursor: "pointer",
                      color: colors.danger.main,
                      fontSize: fontSize.lg,
                      padding: spacing.xs,
                    }}
                  >
                    ×
                  </button>
                </div>
              );
            })}
          </div>
        </div>
      )}

      {/* Total Summary */}
      {value.length > 0 && (
        <div
          style={{
            padding: spacing.md,
            backgroundColor: colors.neutral.white,
            borderRadius: borderRadius.md,
            border: `2px solid ${colors.neutral.border}`,
          }}
        >
          {/* Client Total */}
          <div
            style={{
              display: "flex",
              justifyContent: "space-between",
              alignItems: "center",
              paddingBottom: spacing.sm,
              marginBottom: spacing.sm,
              borderBottom: `1px solid ${colors.neutral.border}`,
            }}
          >
            <Text style={{ fontWeight: fontWeight.semibold }}>
              {t('materials.clientTotal')}
            </Text>
            <Text
              style={{
                fontWeight: fontWeight.bold,
                fontSize: fontSize.xl,
                color: colors.primary.main,
              }}
            >
              {formatCurrency(totalSell)}
            </Text>
          </div>

          {/* Cost & Profit Row */}
          <div style={{ display: "flex", justifyContent: "space-between" }}>
            <div>
              <Text variant="muted" size="sm">{t('common.cost')}</Text>
              <Text style={{ fontWeight: fontWeight.medium }}>{formatCurrency(totalCost)}</Text>
            </div>
            <div style={{ textAlign: "right" }}>
              <Text size="sm" style={{ color: colors.success.main }}>{t('common.profit')}</Text>
              <Text
                style={{
                  fontWeight: fontWeight.bold,
                  fontSize: fontSize.lg,
                  color: colors.success.main,
                }}
              >
                +{formatCurrency(profit)}
              </Text>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
