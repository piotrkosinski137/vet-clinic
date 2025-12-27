import { useState, useRef, useEffect, useCallback, useMemo, useLayoutEffect } from "react";
import { createPortal } from "react-dom";
import { usePriceList, formatCurrency } from "../../hooks";
import type { PriceListItemResponse, UsedMaterialDto } from "../../api/types";
import { Text } from "../ui";
import { useI18n } from "../../i18n";
import { colors, spacing, borderRadius, fontSize, fontWeight, zIndex } from "../../theme";

interface ProceduresSelectorProps {
  value: UsedMaterialDto[];
  onChange: (procedures: UsedMaterialDto[]) => void;
  readOnly?: boolean;
}

/**
 * ProceduresSelector - Select procedures/services with 100% clinic earnings (costPrice = 0)
 * These are labor-only services where the full price goes to the clinic as profit.
 */
export function ProceduresSelector({ value, onChange, readOnly = false }: ProceduresSelectorProps) {
  const { t } = useI18n();
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);
  const [highlightedIndex, setHighlightedIndex] = useState(0);
  const [dropdownPosition, setDropdownPosition] = useState({ top: 0, left: 0, width: 0 });
  const dropdownRef = useRef<HTMLDivElement>(null);
  const portalRef = useRef<HTMLDivElement>(null);
  const buttonRef = useRef<HTMLButtonElement>(null);

  // Calculate dropdown position when it opens
  useLayoutEffect(() => {
    if (isDropdownOpen && buttonRef.current) {
      const rect = buttonRef.current.getBoundingClientRect();
      setDropdownPosition({
        top: rect.bottom + window.scrollY,
        left: rect.left + window.scrollX,
        width: rect.width,
      });
    }
  }, [isDropdownOpen]);

  const { items: allItems, loading } = usePriceList({ active: true });

  // Filter for procedures/services with 0 cost (100% profit)
  const pureServiceItems = useMemo(() => {
    return allItems.filter(
      (item) =>
        item.costPrice === 0 &&
        (item.category === "PROCEDURE" || item.category === "SERVICE" || item.category === "CONSULTATION")
    );
  }, [allItems]);

  // Filter out already selected items
  const availableItems = pureServiceItems.filter(
    (item) => !value.some((v) => v.materialId === item.id)
  );

  const totalEarnings = value.reduce((sum, p) => sum + p.quantity * p.sellPrice, 0);

  // Close dropdown when clicking outside
  useEffect(() => {
    function handleClickOutside(event: MouseEvent) {
      const target = event.target as Node;
      const isOutsideButton = dropdownRef.current && !dropdownRef.current.contains(target);
      const isOutsidePortal = portalRef.current && !portalRef.current.contains(target);
      if (isOutsideButton && isOutsidePortal) {
        setIsDropdownOpen(false);
      }
    }
    if (isDropdownOpen) {
      document.addEventListener("mousedown", handleClickOutside);
      return () => document.removeEventListener("mousedown", handleClickOutside);
    }
  }, [isDropdownOpen]);

  const handleSelectItem = useCallback(
    (item: PriceListItemResponse) => {
      const newProcedure: UsedMaterialDto = {
        materialId: item.id,
        name: item.name,
        quantity: 1,
        costPrice: 0, // Always 0 for pure services
        sellPrice: item.sellPrice,
        unit: item.unit || "zabieg",
      };
      onChange([...value, newProcedure]);
      setIsDropdownOpen(false);
    },
    [value, onChange]
  );

  const handleRemoveItem = useCallback(
    (itemId: string) => {
      onChange(value.filter((p) => p.materialId !== itemId));
    },
    [value, onChange]
  );

  const handleQuantityChange = useCallback(
    (itemId: string, delta: number) => {
      onChange(
        value.map((p) => {
          if (p.materialId === itemId) {
            const newQuantity = Math.max(1, p.quantity + delta);
            return { ...p, quantity: newQuantity };
          }
          return p;
        })
      );
    },
    [value, onChange]
  );

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
        setHighlightedIndex((prev) => Math.min(prev + 1, availableItems.length - 1));
        break;
      case "ArrowUp":
        e.preventDefault();
        setHighlightedIndex((prev) => Math.max(prev - 1, 0));
        break;
      case "Enter":
        e.preventDefault();
        if (availableItems[highlightedIndex]) {
          handleSelectItem(availableItems[highlightedIndex]);
        }
        break;
      case "Escape":
        setIsDropdownOpen(false);
        break;
    }
  };

  const getCategoryIcon = (category: string): string => {
    switch (category) {
      case "PROCEDURE":
        return "🏥";
      case "CONSULTATION":
        return "👨‍⚕️";
      case "SERVICE":
        return "✂️";
      default:
        return "📋";
    }
  };

  if (readOnly) {
    return (
      <div>
        {value.length === 0 ? (
          <Text variant="muted" size="sm">
            {t('procedures.noProcedures')}
          </Text>
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
                    backgroundColor: colors.success.light,
                    borderRadius: borderRadius.sm,
                    borderLeft: `3px solid ${colors.success.main}`,
                  }}
                >
                  <div>
                    <Text size="sm">{item.name}</Text>
                    <Text variant="muted" size="sm">
                      {item.quantity} x {formatCurrency(item.sellPrice)}
                    </Text>
                  </div>
                  <Text style={{ fontWeight: fontWeight.bold, color: colors.success.main }}>
                    +{formatCurrency(item.quantity * item.sellPrice)}
                  </Text>
                </div>
              ))}
            </div>
            {/* Total */}
            <div
              style={{
                marginTop: spacing.sm,
                paddingTop: spacing.sm,
                borderTop: `2px solid ${colors.success.main}`,
                display: "flex",
                justifyContent: "space-between",
              }}
            >
              <Text style={{ fontWeight: fontWeight.semibold }}>{t('procedures.totalProcedures')}</Text>
              <Text style={{ fontWeight: fontWeight.bold, color: colors.success.main }}>
                +{formatCurrency(totalEarnings)}
              </Text>
            </div>
          </>
        )}
      </div>
    );
  }

  return (
    <div>
      {/* Dropdown Selector */}
      <div ref={dropdownRef} style={{ position: "relative", marginBottom: spacing.md }}>
        <button
          ref={buttonRef}
          type="button"
          onClick={() => setIsDropdownOpen(!isDropdownOpen)}
          onKeyDown={handleKeyDown}
          style={{
            width: "100%",
            padding: spacing.sm,
            borderRadius: borderRadius.sm,
            border: `1px solid ${colors.success.main}`,
            backgroundColor: colors.success.light,
            fontSize: fontSize.sm,
            textAlign: "left",
            cursor: "pointer",
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
          }}
        >
          <span>+ {t('procedures.addProcedure')}</span>
          <span style={{ fontSize: fontSize.lg }}>{isDropdownOpen ? "▲" : "▼"}</span>
        </button>

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
                <Text variant="muted" size="sm">
                  {t('procedures.loading')}
                </Text>
              </div>
            ) : availableItems.length === 0 ? (
              <div style={{ padding: spacing.md, textAlign: "center" }}>
                <Text variant="muted" size="sm">
                  {t('procedures.noAvailable')}
                </Text>
              </div>
            ) : (
              availableItems.map((item, index) => (
                <div
                  key={item.id}
                  onClick={() => handleSelectItem(item)}
                  style={{
                    padding: spacing.sm,
                    cursor: "pointer",
                    backgroundColor: index === highlightedIndex ? colors.success.light : "transparent",
                    borderBottom: `1px solid ${colors.neutral.border}`,
                  }}
                  onMouseEnter={() => setHighlightedIndex(index)}
                >
                  <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                    <div style={{ display: "flex", alignItems: "center", gap: spacing.sm }}>
                      <span style={{ fontSize: fontSize.lg }}>{getCategoryIcon(item.category)}</span>
                      <div>
                        <Text style={{ fontWeight: fontWeight.medium }}>{item.name}</Text>
                        {item.description && (
                          <Text variant="muted" size="sm">
                            {item.description}
                          </Text>
                        )}
                      </div>
                    </div>
                    <div style={{ textAlign: "right" }}>
                      <Text style={{ fontWeight: fontWeight.bold, color: colors.success.main }}>
                        {formatCurrency(item.sellPrice)}
                      </Text>
                      <Text variant="muted" size="sm" style={{ color: colors.success.main }}>
                        {t('procedures.subtitle')}
                      </Text>
                    </div>
                  </div>
                </div>
              ))
            )}
          </div>,
          document.body
        )}
      </div>

      {/* Selected Procedures List */}
      {value.length > 0 && (
        <div style={{ marginBottom: spacing.md }}>
          <Text size="sm" style={{ fontWeight: fontWeight.medium, marginBottom: spacing.xs }}>
            {t('procedures.selectedProcedures')} ({value.length})
          </Text>
          <div style={{ display: "flex", flexDirection: "column", gap: spacing.xs }}>
            {value.map((item) => (
              <div
                key={item.materialId}
                style={{
                  display: "flex",
                  alignItems: "center",
                  gap: spacing.sm,
                  padding: spacing.sm,
                  backgroundColor: colors.success.light,
                  borderRadius: borderRadius.sm,
                  borderLeft: `3px solid ${colors.success.main}`,
                }}
              >
                <div style={{ flex: 1 }}>
                  <Text size="sm" style={{ fontWeight: fontWeight.medium }}>
                    {item.name}
                  </Text>
                  <Text variant="muted" size="sm">
                    {formatCurrency(item.sellPrice)} / {item.unit}
                  </Text>
                </div>

                {/* Quantity Controls */}
                <div style={{ display: "flex", alignItems: "center", gap: spacing.xs }}>
                  <button
                    type="button"
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
                    type="button"
                    onClick={() => handleQuantityChange(item.materialId, 1)}
                    style={{
                      width: "28px",
                      height: "28px",
                      borderRadius: borderRadius.full,
                      border: "none",
                      backgroundColor: colors.success.main,
                      color: colors.neutral.white,
                      cursor: "pointer",
                      fontSize: fontSize.lg,
                      fontWeight: fontWeight.bold,
                    }}
                  >
                    +
                  </button>
                </div>

                {/* Item Total (100% profit) */}
                <div style={{ textAlign: "right", minWidth: "100px" }}>
                  <Text style={{ fontWeight: fontWeight.bold, color: colors.success.main }}>
                    +{formatCurrency(item.quantity * item.sellPrice)}
                  </Text>
                </div>

                {/* Remove Button */}
                <button
                  type="button"
                  onClick={() => handleRemoveItem(item.materialId)}
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
            ))}
          </div>
        </div>
      )}

      {/* Total Summary */}
      {value.length > 0 && (
        <div
          style={{
            padding: spacing.md,
            backgroundColor: colors.success.light,
            borderRadius: borderRadius.md,
            border: `2px solid ${colors.success.main}`,
          }}
        >
          <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
            <div>
              <Text style={{ fontWeight: fontWeight.semibold }}>{t('procedures.totalProcedures')}</Text>
              <Text variant="muted" size="sm">
                {t('procedures.fullProfit')}
              </Text>
            </div>
            <Text
              style={{
                fontWeight: fontWeight.bold,
                fontSize: fontSize.xl,
                color: colors.success.main,
              }}
            >
              +{formatCurrency(totalEarnings)}
            </Text>
          </div>
        </div>
      )}
    </div>
  );
}
