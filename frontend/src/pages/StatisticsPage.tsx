import { useState, useEffect, useMemo } from "react";
import { api } from "../api";
import type { VisitResponse, VeterinarianResponse } from "../api/types";
import {
  Card,
  CardTitle,
  Text,
  Loading,
  Select,
  Badge,
} from "../components/ui";
import { formatCurrency } from "../hooks";
import { useI18n } from "../i18n";
import { colors, spacing, borderRadius, fontSize, fontWeight, shadows } from "../theme";

type TimePeriod = "day" | "week" | "month";

interface PeriodStats {
  visitCount: number;
  completedCount: number;
  cancelledCount: number;
  totalRevenue: number;
  totalCost: number;
  totalProfit: number;
}

function getDateRange(period: TimePeriod): { from: Date; to: Date } {
  const now = new Date();
  const to = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 23, 59, 59);
  let from: Date;

  switch (period) {
    case "day":
      from = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 0, 0, 0);
      break;
    case "week":
      const dayOfWeek = now.getDay();
      const daysToMonday = dayOfWeek === 0 ? 6 : dayOfWeek - 1;
      from = new Date(now.getFullYear(), now.getMonth(), now.getDate() - daysToMonday, 0, 0, 0);
      break;
    case "month":
      from = new Date(now.getFullYear(), now.getMonth(), 1, 0, 0, 0);
      break;
  }

  return { from, to };
}

function formatDateForApi(date: Date): string {
  return date.toISOString().split("T")[0];
}

function calculateVisitRevenue(visit: VisitResponse): number {
  const materials = visit.usedMaterials ?? [];
  return materials.reduce((sum, m) => sum + m.quantity * m.sellPrice, 0);
}

function calculateVisitCost(visit: VisitResponse): number {
  const materials = visit.usedMaterials ?? [];
  return materials.reduce((sum, m) => sum + m.quantity * m.costPrice, 0);
}

function calculateStats(visits: VisitResponse[]): PeriodStats {
  const completedVisits = visits.filter((v) => v.status === "COMPLETED");
  const cancelledVisits = visits.filter((v) => v.status === "CANCELLED");

  const totalRevenue = completedVisits.reduce((sum, v) => sum + calculateVisitRevenue(v), 0);
  const totalCost = completedVisits.reduce((sum, v) => sum + calculateVisitCost(v), 0);

  return {
    visitCount: visits.length,
    completedCount: completedVisits.length,
    cancelledCount: cancelledVisits.length,
    totalRevenue,
    totalCost,
    totalProfit: totalRevenue - totalCost,
  };
}

const statCardStyle: React.CSSProperties = {
  backgroundColor: colors.neutral.white,
  borderRadius: borderRadius.md,
  padding: spacing.lg,
  boxShadow: shadows.md,
  textAlign: "center",
};

const statValueStyle: React.CSSProperties = {
  fontSize: fontSize.xxl,
  fontWeight: fontWeight.bold,
  marginBottom: spacing.xs,
};

const statLabelStyle: React.CSSProperties = {
  fontSize: fontSize.sm,
  color: colors.neutral.textMuted,
};

export function StatisticsPage() {
  const { t } = useI18n();
  const [loading, setLoading] = useState(true);
  const [_error, setError] = useState<string | null>(null);
  const [veterinarians, setVeterinarians] = useState<VeterinarianResponse[]>([]);
  const [selectedVetId, setSelectedVetId] = useState<string>("all");
  const [allVisits, setAllVisits] = useState<{
    day: VisitResponse[];
    week: VisitResponse[];
    month: VisitResponse[];
  }>({ day: [], week: [], month: [] });

  useEffect(() => {
    async function fetchData() {
      setLoading(true);
      try {
        const [vets, dayVisits, weekVisits, monthVisits] = await Promise.all([
          api.getVeterinarians({ active: true }),
          api.getVisits({
            dateFrom: formatDateForApi(getDateRange("day").from),
            dateTo: formatDateForApi(getDateRange("day").to),
          }),
          api.getVisits({
            dateFrom: formatDateForApi(getDateRange("week").from),
            dateTo: formatDateForApi(getDateRange("week").to),
          }),
          api.getVisits({
            dateFrom: formatDateForApi(getDateRange("month").from),
            dateTo: formatDateForApi(getDateRange("month").to),
          }),
        ]);

        setVeterinarians(vets);
        setAllVisits({
          day: dayVisits,
          week: weekVisits,
          month: monthVisits,
        });
      } catch {
        setError("Failed to fetch statistics data. Please try again.");
      } finally {
        setLoading(false);
      }
    }

    fetchData();
  }, []);

  const filteredVisits = useMemo(() => {
    if (selectedVetId === "all") {
      return allVisits;
    }

    const selectedVet = veterinarians.find((v) => v.id === selectedVetId);
    if (!selectedVet) return allVisits;

    return {
      day: allVisits.day.filter((v) => v.veterinarianName === selectedVet.fullName),
      week: allVisits.week.filter((v) => v.veterinarianName === selectedVet.fullName),
      month: allVisits.month.filter((v) => v.veterinarianName === selectedVet.fullName),
    };
  }, [allVisits, selectedVetId, veterinarians]);

  const stats = useMemo(
    () => ({
      day: calculateStats(filteredVisits.day),
      week: calculateStats(filteredVisits.week),
      month: calculateStats(filteredVisits.month),
    }),
    [filteredVisits]
  );

  if (loading) {
    return (
      <div style={{ padding: spacing.xl, textAlign: "center" }}>
        <Loading />
        <Text variant="muted">{t('common.loading')}</Text>
      </div>
    );
  }

  const periods: { key: TimePeriod; label: string }[] = [
    { key: "day", label: t('statistics.thisDay') },
    { key: "week", label: t('statistics.thisWeek') },
    { key: "month", label: t('statistics.thisMonth') },
  ];

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
        <h1 style={{ color: colors.secondary.main, margin: 0 }}>{t('statistics.title')}</h1>

        <div style={{ display: "flex", alignItems: "center", gap: spacing.sm }}>
          <Text variant="muted">{t('statistics.filterByDoctor')}:</Text>
          <Select
            value={selectedVetId}
            onChange={(e) => setSelectedVetId(e.target.value)}
            style={{ minWidth: "200px" }}
          >
            <option value="all">{t('statistics.allDoctors')}</option>
            {veterinarians.map((vet) => (
              <option key={vet.id} value={vet.id}>
                Dr. {vet.fullName}
              </option>
            ))}
          </Select>
        </div>
      </div>

      {/* Visit Counts */}
      <Card style={{ marginBottom: spacing.lg }}>
        <CardTitle>{t('statistics.visitStatistics')}</CardTitle>
        <div
          style={{
            display: "grid",
            gridTemplateColumns: "repeat(3, 1fr)",
            gap: spacing.lg,
            marginTop: spacing.md,
          }}
        >
          {periods.map(({ key, label }) => (
            <div key={key} style={statCardStyle}>
              <div style={{ ...statValueStyle, color: colors.primary.main }}>
                {stats[key].visitCount}
              </div>
              <div style={statLabelStyle}>{label}</div>
              <div
                style={{
                  display: "flex",
                  justifyContent: "center",
                  gap: spacing.sm,
                  marginTop: spacing.sm,
                }}
              >
                <Badge variant="success">{stats[key].completedCount} {t('statistics.completed')}</Badge>
                {stats[key].cancelledCount > 0 && (
                  <Badge variant="danger">{stats[key].cancelledCount} {t('statistics.cancelled')}</Badge>
                )}
              </div>
            </div>
          ))}
        </div>
      </Card>

      {/* Revenue & Profit */}
      <Card style={{ marginBottom: spacing.lg }}>
        <CardTitle>{t('statistics.revenueProfit')}</CardTitle>
        <div
          style={{
            display: "grid",
            gridTemplateColumns: "repeat(3, 1fr)",
            gap: spacing.lg,
            marginTop: spacing.md,
          }}
        >
          {periods.map(({ key, label }) => (
            <div
              key={key}
              style={{
                ...statCardStyle,
                borderTop: `4px solid ${colors.success.main}`,
              }}
            >
              <Text variant="muted" size="sm" style={{ marginBottom: spacing.md }}>
                {label}
              </Text>

              <div style={{ marginBottom: spacing.md }}>
                <div style={{ ...statValueStyle, color: colors.primary.main, fontSize: fontSize.xl }}>
                  {formatCurrency(stats[key].totalRevenue)}
                </div>
                <div style={statLabelStyle}>{t('common.revenue')}</div>
              </div>

              <div style={{ marginBottom: spacing.md }}>
                <div style={{ ...statValueStyle, color: colors.danger.main, fontSize: fontSize.lg }}>
                  {formatCurrency(stats[key].totalCost)}
                </div>
                <div style={statLabelStyle}>{t('common.cost')}</div>
              </div>

              <div
                style={{
                  borderTop: `1px solid ${colors.neutral.border}`,
                  paddingTop: spacing.md,
                }}
              >
                <div
                  style={{
                    ...statValueStyle,
                    color: stats[key].totalProfit >= 0 ? colors.success.main : colors.danger.main,
                    fontSize: fontSize.xl,
                  }}
                >
                  {formatCurrency(stats[key].totalProfit)}
                </div>
                <div style={statLabelStyle}>{t('common.profit')}</div>
              </div>
            </div>
          ))}
        </div>
      </Card>

      {/* Per-Doctor Breakdown (only when "All Doctors" is selected) */}
      {selectedVetId === "all" && veterinarians.length > 0 && (
        <Card>
          <CardTitle>{t('statistics.todayPerformance')}</CardTitle>
          <div style={{ marginTop: spacing.md }}>
            {/* Table Header */}
            <div
              style={{
                display: "grid",
                gridTemplateColumns: "1fr 80px 100px 100px 100px",
                gap: spacing.sm,
                padding: spacing.sm,
                backgroundColor: colors.secondary.main,
                color: colors.neutral.white,
                borderRadius: `${borderRadius.sm} ${borderRadius.sm} 0 0`,
                fontSize: fontSize.sm,
                fontWeight: fontWeight.semibold,
              }}
            >
              <div>{t('statistics.doctor')}</div>
              <div style={{ textAlign: "center" }}>{t('statistics.visits')}</div>
              <div style={{ textAlign: "right" }}>{t('common.revenue')}</div>
              <div style={{ textAlign: "right" }}>{t('common.cost')}</div>
              <div style={{ textAlign: "right" }}>{t('common.profit')}</div>
            </div>

            {/* Table Rows */}
            {veterinarians.map((vet) => {
              const vetVisits = allVisits.day.filter(
                (v) => v.veterinarianName === vet.fullName
              );
              const vetStats = calculateStats(vetVisits);

              return (
                <div
                  key={vet.id}
                  style={{
                    display: "grid",
                    gridTemplateColumns: "1fr 80px 100px 100px 100px",
                    gap: spacing.sm,
                    padding: spacing.sm,
                    backgroundColor: colors.neutral.background,
                    borderBottom: `1px solid ${colors.neutral.border}`,
                    fontSize: fontSize.sm,
                    alignItems: "center",
                  }}
                >
                  <div style={{ display: "flex", alignItems: "center", gap: spacing.sm }}>
                    <div
                      style={{
                        width: "8px",
                        height: "8px",
                        borderRadius: borderRadius.full,
                        backgroundColor: vet.colorCode || colors.primary.main,
                      }}
                    />
                    <Text style={{ fontWeight: fontWeight.medium }}>Dr. {vet.fullName}</Text>
                  </div>
                  <div style={{ textAlign: "center", fontWeight: fontWeight.medium }}>
                    {vetStats.completedCount}/{vetStats.visitCount}
                  </div>
                  <div style={{ textAlign: "right" }}>{formatCurrency(vetStats.totalRevenue)}</div>
                  <div style={{ textAlign: "right", color: colors.danger.main }}>
                    {formatCurrency(vetStats.totalCost)}
                  </div>
                  <div
                    style={{
                      textAlign: "right",
                      fontWeight: fontWeight.semibold,
                      color: vetStats.totalProfit >= 0 ? colors.success.main : colors.danger.main,
                    }}
                  >
                    {formatCurrency(vetStats.totalProfit)}
                  </div>
                </div>
              );
            })}

            {/* Totals Row */}
            <div
              style={{
                display: "grid",
                gridTemplateColumns: "1fr 80px 100px 100px 100px",
                gap: spacing.sm,
                padding: spacing.sm,
                backgroundColor: colors.primary.light,
                borderRadius: `0 0 ${borderRadius.sm} ${borderRadius.sm}`,
                fontSize: fontSize.sm,
                fontWeight: fontWeight.bold,
              }}
            >
              <div>{t('common.total')}</div>
              <div style={{ textAlign: "center" }}>
                {stats.day.completedCount}/{stats.day.visitCount}
              </div>
              <div style={{ textAlign: "right" }}>{formatCurrency(stats.day.totalRevenue)}</div>
              <div style={{ textAlign: "right", color: colors.danger.main }}>
                {formatCurrency(stats.day.totalCost)}
              </div>
              <div
                style={{
                  textAlign: "right",
                  color: stats.day.totalProfit >= 0 ? colors.success.main : colors.danger.main,
                }}
              >
                {formatCurrency(stats.day.totalProfit)}
              </div>
            </div>
          </div>
        </Card>
      )}
    </div>
  );
}
