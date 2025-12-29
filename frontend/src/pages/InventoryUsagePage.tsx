import { useState, useEffect, useMemo, useRef } from "react";
import { api } from "../api";
import type { VisitResponse } from "../api/types";
import {
  Button,
  Card,
  CardTitle,
  Text,
  Loading,
  Input,
  Badge,
} from "../components/ui";
import { formatCurrency } from "../hooks";
import { useI18n } from "../i18n";
import { colors, spacing, borderRadius, fontSize, fontWeight } from "../theme";
import { getLocaleForLanguage } from "../constants/locale";

interface AggregatedMaterial {
  materialId: string;
  name: string;
  unit: string;
  totalQuantity: number;
  costPrice: number;
  sellPrice: number;
  totalCost: number;
  totalSell: number;
  visitCount: number;
}

function formatDateForInput(date: Date): string {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}

function formatDisplayDate(dateString: string, language: string): string {
  const date = new Date(dateString);
  return date.toLocaleDateString(getLocaleForLanguage(language), {
    weekday: "long",
    year: "numeric",
    month: "long",
    day: "numeric",
  });
}

function aggregateMaterials(visits: VisitResponse[]): AggregatedMaterial[] {
  const materialMap = new Map<string, AggregatedMaterial>();

  visits.forEach((visit) => {
    if (visit.status === "CANCELLED") return;

    const materials = visit.usedMaterials ?? [];
    materials.forEach((m) => {
      const existing = materialMap.get(m.materialId);
      if (existing) {
        existing.totalQuantity += m.quantity;
        existing.totalCost += m.quantity * m.costPrice;
        existing.totalSell += m.quantity * m.sellPrice;
        existing.visitCount += 1;
      } else {
        materialMap.set(m.materialId, {
          materialId: m.materialId,
          name: m.name,
          unit: m.unit,
          totalQuantity: m.quantity,
          costPrice: m.costPrice,
          sellPrice: m.sellPrice,
          totalCost: m.quantity * m.costPrice,
          totalSell: m.quantity * m.sellPrice,
          visitCount: 1,
        });
      }
    });
  });

  // Sort by total quantity descending
  return Array.from(materialMap.values()).sort((a, b) => b.totalQuantity - a.totalQuantity);
}

export function InventoryUsagePage() {
  const { t, language } = useI18n();
  const [loading, setLoading] = useState(true);
  const [selectedDate, setSelectedDate] = useState<string>(formatDateForInput(new Date()));
  const [visits, setVisits] = useState<VisitResponse[]>([]);

  // Track last fetched date to avoid duplicate fetches
  const lastFetchedDate = useRef<string>("");

  useEffect(() => {
    // Only fetch if date has changed
    if (selectedDate === lastFetchedDate.current) return;
    lastFetchedDate.current = selectedDate;

    async function fetchData() {
      setLoading(true);
      try {
        const dayVisits = await api.getVisits({
          dateFrom: selectedDate,
          dateTo: selectedDate,
        });
        setVisits(dayVisits);
      } catch {
        setVisits([]);
      } finally {
        setLoading(false);
      }
    }

    fetchData();
  }, [selectedDate]);

  const aggregatedMaterials = useMemo(() => aggregateMaterials(visits), [visits]);

  const totals = useMemo(() => {
    return aggregatedMaterials.reduce(
      (acc, m) => ({
        totalCost: acc.totalCost + m.totalCost,
        totalSell: acc.totalSell + m.totalSell,
        totalItems: acc.totalItems + m.totalQuantity,
      }),
      { totalCost: 0, totalSell: 0, totalItems: 0 }
    );
  }, [aggregatedMaterials]);

  const completedVisits = visits.filter((v) => v.status === "COMPLETED" || v.status === "IN_PROGRESS");

  const goToDate = (offset: number) => {
    const current = new Date(selectedDate);
    current.setDate(current.getDate() + offset);
    setSelectedDate(formatDateForInput(current));
  };

  return (
    <div>
      <div
        style={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
          marginBottom: spacing.lg,
        }}
      >
        <h1 style={{ color: colors.secondary.main, margin: 0 }}>{t('inventory.title')}</h1>

        <div style={{ display: "flex", alignItems: "center", gap: spacing.sm }}>
          <Button variant="ghost" onClick={() => goToDate(-1)}>
            {t('common.previousDay')}
          </Button>
          <Input
            type="date"
            value={selectedDate}
            onChange={(e) => setSelectedDate(e.target.value)}
            style={{ width: "160px" }}
          />
          <Button variant="ghost" onClick={() => goToDate(1)}>
            {t('common.nextDay')}
          </Button>
          <Button
            variant="secondary"
            onClick={() => setSelectedDate(formatDateForInput(new Date()))}
          >
            {t('common.today')}
          </Button>
        </div>
      </div>

      {/* Date Header */}
      <Card
        style={{
          marginBottom: spacing.lg,
          backgroundColor: colors.primary.light,
          borderLeft: `4px solid ${colors.primary.main}`,
        }}
      >
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
          <div>
            <Text style={{ fontSize: fontSize.lg, fontWeight: fontWeight.semibold }}>
              {formatDisplayDate(selectedDate, language)}
            </Text>
            <Text variant="muted" size="sm">
              {completedVisits.length} {t('inventory.visitsWithMaterials')}
            </Text>
          </div>
          <div style={{ display: "flex", gap: spacing.lg }}>
            <div style={{ textAlign: "center" }}>
              <Text style={{ fontSize: fontSize.xl, fontWeight: fontWeight.bold, color: colors.primary.main }}>
                {totals.totalItems}
              </Text>
              <Text variant="muted" size="sm">{t('inventory.itemsUsed')}</Text>
            </div>
            <div style={{ textAlign: "center" }}>
              <Text style={{ fontSize: fontSize.xl, fontWeight: fontWeight.bold, color: colors.danger.main }}>
                {formatCurrency(totals.totalCost)}
              </Text>
              <Text variant="muted" size="sm">{t('inventory.totalCost')}</Text>
            </div>
            <div style={{ textAlign: "center" }}>
              <Text style={{ fontSize: fontSize.xl, fontWeight: fontWeight.bold, color: colors.success.main }}>
                {formatCurrency(totals.totalSell)}
              </Text>
              <Text variant="muted" size="sm">{t('inventory.totalRevenue')}</Text>
            </div>
            <div style={{ textAlign: "center" }}>
              <Text
                style={{
                  fontSize: fontSize.xl,
                  fontWeight: fontWeight.bold,
                  color: totals.totalSell - totals.totalCost >= 0 ? colors.success.main : colors.danger.main,
                }}
              >
                {formatCurrency(totals.totalSell - totals.totalCost)}
              </Text>
              <Text variant="muted" size="sm">{t('common.profit')}</Text>
            </div>
          </div>
        </div>
      </Card>

      {loading ? (
        <div style={{ padding: spacing.xl, textAlign: "center" }}>
          <Loading />
          <Text variant="muted">{t('common.loading')}</Text>
        </div>
      ) : aggregatedMaterials.length === 0 ? (
        <Card style={{ textAlign: "center", padding: spacing.xxl }}>
          <Text variant="muted" style={{ fontSize: fontSize.lg }}>
            {t('inventory.noMaterialsUsedOnDay')}
          </Text>
          <Text variant="muted" size="sm" style={{ marginTop: spacing.sm }}>
            {t('inventory.noMaterialsHint')}
          </Text>
        </Card>
      ) : (
        <Card>
          <CardTitle>{t('inventory.materialsUsed')}</CardTitle>

          {/* Table Header */}
          <div
            style={{
              display: "grid",
              gridTemplateColumns: "2fr 80px 100px 100px 100px 100px 80px",
              gap: spacing.sm,
              padding: spacing.sm,
              backgroundColor: colors.secondary.main,
              color: colors.neutral.white,
              borderRadius: `${borderRadius.sm} ${borderRadius.sm} 0 0`,
              fontSize: fontSize.sm,
              fontWeight: fontWeight.semibold,
              marginTop: spacing.md,
            }}
          >
            <div>{t('inventory.material')}</div>
            <div style={{ textAlign: "center" }}>{t('inventory.qtyUsed')}</div>
            <div style={{ textAlign: "right" }}>{t('inventory.unitCost')}</div>
            <div style={{ textAlign: "right" }}>{t('inventory.unitPrice')}</div>
            <div style={{ textAlign: "right" }}>{t('inventory.totalCost')}</div>
            <div style={{ textAlign: "right" }}>{t('inventory.totalRevenue')}</div>
            <div style={{ textAlign: "center" }}>{t('inventory.visitsCount')}</div>
          </div>

          {/* Table Rows */}
          {aggregatedMaterials.map((material) => (
            <div
              key={material.materialId}
              style={{
                display: "grid",
                gridTemplateColumns: "2fr 80px 100px 100px 100px 100px 80px",
                gap: spacing.sm,
                padding: spacing.sm,
                backgroundColor: colors.neutral.background,
                borderBottom: `1px solid ${colors.neutral.border}`,
                fontSize: fontSize.sm,
                alignItems: "center",
              }}
            >
              <div>
                <Text style={{ fontWeight: fontWeight.medium }}>{material.name}</Text>
                <Text variant="muted" size="sm">
                  {material.unit}
                </Text>
              </div>
              <div
                style={{
                  textAlign: "center",
                  fontWeight: fontWeight.bold,
                  color: colors.primary.main,
                }}
              >
                {material.totalQuantity}
              </div>
              <div style={{ textAlign: "right" }}>{formatCurrency(material.costPrice)}</div>
              <div style={{ textAlign: "right" }}>{formatCurrency(material.sellPrice)}</div>
              <div style={{ textAlign: "right", color: colors.danger.main }}>
                {formatCurrency(material.totalCost)}
              </div>
              <div style={{ textAlign: "right", color: colors.success.main }}>
                {formatCurrency(material.totalSell)}
              </div>
              <div style={{ textAlign: "center" }}>
                <Badge variant="secondary">{material.visitCount}</Badge>
              </div>
            </div>
          ))}

          {/* Totals Row */}
          <div
            style={{
              display: "grid",
              gridTemplateColumns: "2fr 80px 100px 100px 100px 100px 80px",
              gap: spacing.sm,
              padding: spacing.sm,
              backgroundColor: colors.primary.light,
              borderRadius: `0 0 ${borderRadius.sm} ${borderRadius.sm}`,
              fontSize: fontSize.sm,
              fontWeight: fontWeight.bold,
            }}
          >
            <div>{t('common.total')} ({aggregatedMaterials.length})</div>
            <div style={{ textAlign: "center", color: colors.primary.main }}>
              {totals.totalItems}
            </div>
            <div></div>
            <div></div>
            <div style={{ textAlign: "right", color: colors.danger.main }}>
              {formatCurrency(totals.totalCost)}
            </div>
            <div style={{ textAlign: "right", color: colors.success.main }}>
              {formatCurrency(totals.totalSell)}
            </div>
            <div></div>
          </div>
        </Card>
      )}

      {/* Profit Summary */}
      {aggregatedMaterials.length > 0 && (
        <Card style={{ marginTop: spacing.lg }}>
          <div
            style={{
              display: "flex",
              justifyContent: "space-around",
              textAlign: "center",
            }}
          >
            <div>
              <Text variant="muted" size="sm">
                {t('inventory.totalRevenue')}
              </Text>
              <Text
                style={{
                  fontSize: fontSize.xl,
                  fontWeight: fontWeight.bold,
                  color: colors.primary.main,
                }}
              >
                {formatCurrency(totals.totalSell)}
              </Text>
            </div>
            <div style={{ fontSize: fontSize.xl, color: colors.neutral.textMuted }}>-</div>
            <div>
              <Text variant="muted" size="sm">
                {t('inventory.totalCost')}
              </Text>
              <Text
                style={{
                  fontSize: fontSize.xl,
                  fontWeight: fontWeight.bold,
                  color: colors.danger.main,
                }}
              >
                {formatCurrency(totals.totalCost)}
              </Text>
            </div>
            <div style={{ fontSize: fontSize.xl, color: colors.neutral.textMuted }}>=</div>
            <div>
              <Text variant="muted" size="sm">
                {t('inventory.dailyProfit')}
              </Text>
              <Text
                style={{
                  fontSize: fontSize.xl,
                  fontWeight: fontWeight.bold,
                  color:
                    totals.totalSell - totals.totalCost >= 0
                      ? colors.success.main
                      : colors.danger.main,
                }}
              >
                {formatCurrency(totals.totalSell - totals.totalCost)}
              </Text>
            </div>
          </div>
        </Card>
      )}
    </div>
  );
}
