import { useState, useMemo } from 'react';
import { useAuditLogs } from '../hooks';
import { useI18n } from '../i18n';
import { getLocaleForLanguage } from '../constants/locale';
import {
  PageHeader,
  Card,
  CardContent,
  Input,
  Select,
  Button,
  Badge,
  Text,
  Loading,
  EmptyState,
  Pagination,
} from '../components/ui';
import { colors, spacing, borderRadius } from '../theme';
import type { AuditLogFilters, AuditAction, AuditLogResponse } from '../api/types';
import type { BadgeVariant } from '../components/ui/Badge';

const ENTITY_TYPES = ['Patient', 'Client', 'Visit', 'Invoice', 'Veterinarian'];
const ACTIONS: AuditAction[] = ['CREATE', 'UPDATE', 'DELETE', 'VIEW', 'EXPORT', 'PRINT', 'LOGIN', 'LOGOUT'];
const ITEMS_PER_PAGE = 20;

const actionColors: Record<AuditAction, BadgeVariant> = {
  CREATE: 'success',
  UPDATE: 'primary',
  DELETE: 'danger',
  VIEW: 'secondary',
  EXPORT: 'warning',
  PRINT: 'warning',
  LOGIN: 'success',
  LOGOUT: 'warning',
};

function formatTimestamp(timestamp: string, language: string): string {
  const date = new Date(timestamp);
  return date.toLocaleString(getLocaleForLanguage(language), {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false,
  });
}

interface DiffViewerProps {
  oldValue?: string;
  newValue?: string;
  changedFields?: string;
  t: (key: string, params?: Record<string, string | number>) => string;
}

function DiffViewer({ oldValue, newValue, changedFields, t }: DiffViewerProps) {
  const [expanded, setExpanded] = useState(false);

  if (!oldValue && !newValue) return null;

  let oldObj: Record<string, unknown> = {};
  let newObj: Record<string, unknown> = {};

  try {
    if (oldValue) oldObj = JSON.parse(oldValue);
    if (newValue) newObj = JSON.parse(newValue);
  } catch {
    return (
      <div style={{ marginTop: spacing.sm }}>
        <Button variant="ghost" size="sm" onClick={() => setExpanded(!expanded)}>
          {expanded ? t('audit.hideDetails') : t('audit.showDetails')}
        </Button>
        {expanded && (
          <div style={{ marginTop: spacing.sm, padding: spacing.sm, backgroundColor: colors.neutral.background, borderRadius: borderRadius.md, fontSize: '12px', fontFamily: 'monospace' }}>
            {oldValue && <div style={{ color: colors.danger.main }}>- {oldValue}</div>}
            {newValue && <div style={{ color: colors.success.main }}>+ {newValue}</div>}
          </div>
        )}
      </div>
    );
  }

  const fields = changedFields ? changedFields.split(',') : Object.keys({ ...oldObj, ...newObj });

  return (
    <div style={{ marginTop: spacing.sm }}>
      <Button variant="ghost" size="sm" onClick={() => setExpanded(!expanded)}>
        {expanded
          ? t('audit.hideChanges')
          : fields.length === 1
            ? t('audit.showChanges', { count: fields.length })
            : t('audit.showChangesPlural', { count: fields.length })}
      </Button>
      {expanded && (
        <div style={{ marginTop: spacing.sm, padding: spacing.sm, backgroundColor: colors.neutral.background, borderRadius: borderRadius.md }}>
          <table style={{ width: '100%', fontSize: '12px', borderCollapse: 'collapse' }}>
            <thead>
              <tr style={{ borderBottom: `1px solid ${colors.neutral.border}` }}>
                <th style={{ textAlign: 'left', padding: spacing.xs }}>{t('audit.field')}</th>
                <th style={{ textAlign: 'left', padding: spacing.xs }}>{t('audit.oldValue')}</th>
                <th style={{ textAlign: 'left', padding: spacing.xs }}>{t('audit.newValue')}</th>
              </tr>
            </thead>
            <tbody>
              {fields.map(field => (
                <tr key={field} style={{ borderBottom: `1px solid ${colors.neutral.borderLight}` }}>
                  <td style={{ padding: spacing.xs, fontWeight: 500 }}>{field}</td>
                  <td style={{ padding: spacing.xs, color: colors.danger.main }}>
                    {oldObj[field] !== undefined ? String(oldObj[field]) : '-'}
                  </td>
                  <td style={{ padding: spacing.xs, color: colors.success.main }}>
                    {newObj[field] !== undefined ? String(newObj[field]) : '-'}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}

interface AuditLogRowProps {
  log: AuditLogResponse;
  t: (key: string, params?: Record<string, string | number>) => string;
  language: string;
}

function AuditLogRow({ log, t, language }: AuditLogRowProps) {
  const actionLabel = t(`audit.actions.${log.action}`);
  const entityLabel = t(`audit.entities.${log.entityType}`) || log.entityType;

  const getActionDescription = (): string => {
    switch (log.action) {
      case 'CREATE':
        return t('audit.descriptions.createRecord', { action: actionLabel, entity: entityLabel });
      case 'UPDATE':
        return t('audit.descriptions.updateRecord', { action: actionLabel, entity: entityLabel });
      case 'DELETE':
        return t('audit.descriptions.deleteRecord', { action: actionLabel, entity: entityLabel });
      case 'VIEW':
        return t('audit.descriptions.viewRecord', { action: actionLabel, entity: entityLabel });
      case 'EXPORT':
        return t('audit.descriptions.exportRecord', { action: actionLabel, entity: entityLabel });
      case 'PRINT':
        return t('audit.descriptions.printRecord', { action: actionLabel, entity: entityLabel });
      case 'LOGIN':
        return t('audit.descriptions.loginSystem');
      case 'LOGOUT':
        return t('audit.descriptions.logoutSystem');
      default:
        return `${log.action} - ${log.entityType}`;
    }
  };

  return (
    <div style={{
      padding: spacing.lg,
      borderBottom: `1px solid ${colors.neutral.borderLight}`,
      transition: 'background-color 0.2s',
    }}>
      {/* Main action description - bold and prominent */}
      <div style={{
        display: 'flex',
        alignItems: 'center',
        gap: spacing.md,
        marginBottom: spacing.md,
      }}>
        <Badge
          variant={actionColors[log.action]}
          style={{
            fontSize: '14px',
            fontWeight: 700,
            padding: '6px 14px',
            textTransform: 'uppercase',
            letterSpacing: '0.5px',
          }}
        >
          {actionLabel}
        </Badge>
        <Text variant="body" style={{
          fontSize: '18px',
          fontWeight: 700,
          color: colors.neutral.text,
        }}>
          {getActionDescription()}
        </Text>
      </div>

      {/* User and IP - large and prominent */}
      <div style={{
        display: 'flex',
        alignItems: 'center',
        gap: spacing.lg,
        marginBottom: spacing.md,
        padding: spacing.md,
        backgroundColor: colors.neutral.background,
        borderRadius: borderRadius.md,
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: spacing.sm }}>
          <span style={{ fontSize: '20px' }}>👤</span>
          <div>
            <Text variant="caption" style={{ color: colors.neutral.textMuted, display: 'block', marginBottom: '2px' }}>
              {t('audit.user')}
            </Text>
            <Text variant="body" style={{
              fontSize: '18px',
              fontWeight: 700,
              color: colors.primary.main,
            }}>
              {log.userName}
            </Text>
          </div>
        </div>
        {log.ipAddress && (
          <div style={{ display: 'flex', alignItems: 'center', gap: spacing.sm }}>
            <span style={{ fontSize: '20px' }}>🌐</span>
            <div>
              <Text variant="caption" style={{ color: colors.neutral.textMuted, display: 'block', marginBottom: '2px' }}>
                {t('audit.ipAddress')}
              </Text>
              <Text variant="body" style={{
                fontSize: '18px',
                fontWeight: 700,
                color: colors.warning.main,
                fontFamily: 'monospace',
              }}>
                {log.ipAddress}
              </Text>
            </div>
          </div>
        )}
        <div style={{ marginLeft: 'auto', display: 'flex', alignItems: 'center', gap: spacing.sm }}>
          <span style={{ fontSize: '16px' }}>🕐</span>
          <div>
            <Text variant="caption" style={{ color: colors.neutral.textMuted, display: 'block', marginBottom: '2px' }}>
              {t('audit.dateTime')}
            </Text>
            <Text variant="body" style={{ fontWeight: 500 }}>
              {formatTimestamp(log.timestamp, language)}
            </Text>
          </div>
        </div>
      </div>

      {/* Entity details */}
      <div style={{
        display: 'flex',
        alignItems: 'center',
        gap: spacing.md,
        marginBottom: spacing.sm,
      }}>
        <Badge variant="secondary" style={{ fontSize: '12px' }}>
          {entityLabel}
        </Badge>
        <Text variant="caption" style={{ color: colors.neutral.textMuted, fontFamily: 'monospace' }}>
          ID: {log.entityId.substring(0, 8)}...
        </Text>
        {log.userAgent && (
          <Text variant="caption" style={{ color: colors.neutral.textMuted, marginLeft: 'auto' }}>
            {log.userAgent.length > 50 ? `${log.userAgent.substring(0, 50)}...` : log.userAgent}
          </Text>
        )}
      </div>

      {/* Description if available */}
      {log.description && (
        <div style={{
          padding: spacing.sm,
          backgroundColor: colors.neutral.backgroundAlt,
          borderRadius: borderRadius.sm,
          marginBottom: spacing.sm,
        }}>
          <Text variant="body" style={{ color: colors.neutral.text, fontStyle: 'italic' }}>
            {log.description}
          </Text>
        </div>
      )}

      {/* Diff viewer for UPDATE and DELETE */}
      {log.action === 'UPDATE' && (
        <DiffViewer oldValue={log.oldValue} newValue={log.newValue} changedFields={log.changedFields} t={t} />
      )}
      {log.action === 'DELETE' && log.oldValue && (
        <DiffViewer oldValue={log.oldValue} changedFields="" t={t} />
      )}
    </div>
  );
}

export function AuditPage() {
  const { t, language } = useI18n();
  const [filters, setFilters] = useState<AuditLogFilters>({});
  const [page, setPage] = useState(1);

  const { logs, loading, error, refetch } = useAuditLogs(filters);

  // All hooks must be called before any conditional returns
  const paginatedLogs = useMemo(() => {
    const start = (page - 1) * ITEMS_PER_PAGE;
    return logs.slice(start, start + ITEMS_PER_PAGE);
  }, [logs, page]);

  const handleFilterChange = (key: keyof AuditLogFilters, value: string) => {
    setFilters(prev => ({ ...prev, [key]: value || undefined }));
    setPage(1);
  };

  const handleClearFilters = () => {
    setFilters({});
    setPage(1);
  };

  const filterRowStyle: React.CSSProperties = {
    display: 'flex',
    gap: spacing.md,
    flexWrap: 'wrap',
    marginBottom: spacing.lg,
  };

  const filterItemStyle: React.CSSProperties = {
    flex: '1 1 200px',
    maxWidth: '250px',
  };

  // Show full-page loader on initial load (after all hooks)
  if (loading && logs.length === 0) {
    return <Loading text={t('audit.loading')} />;
  }

  return (
    <div>
      <PageHeader title={t('audit.title')} />

      <Card variant="default" style={{ marginBottom: spacing.lg }}>
        <CardContent>
          <div style={filterRowStyle}>
            <div style={filterItemStyle}>
              <label style={{ display: 'block', marginBottom: spacing.xs, fontSize: '14px', color: colors.neutral.text }}>
                {t('audit.entityType')}
              </label>
              <Select
                value={filters.entityType || ''}
                onChange={(e) => handleFilterChange('entityType', e.target.value)}
              >
                <option value="">{t('audit.allEntities')}</option>
                {ENTITY_TYPES.map(type => (
                  <option key={type} value={type}>{t(`audit.entities.${type}`) || type}</option>
                ))}
              </Select>
            </div>
            <div style={filterItemStyle}>
              <label style={{ display: 'block', marginBottom: spacing.xs, fontSize: '14px', color: colors.neutral.text }}>
                {t('audit.action')}
              </label>
              <Select
                value={filters.action || ''}
                onChange={(e) => handleFilterChange('action', e.target.value as AuditAction)}
              >
                <option value="">{t('audit.allActions')}</option>
                {ACTIONS.map(action => (
                  <option key={action} value={action}>{t(`audit.actions.${action}`) || action}</option>
                ))}
              </Select>
            </div>
            <div style={filterItemStyle}>
              <label style={{ display: 'block', marginBottom: spacing.xs, fontSize: '14px', color: colors.neutral.text }}>
                {t('audit.fromDate')}
              </label>
              <Input
                type="date"
                value={filters.dateFrom || ''}
                onChange={(e) => handleFilterChange('dateFrom', e.target.value)}
              />
            </div>
            <div style={filterItemStyle}>
              <label style={{ display: 'block', marginBottom: spacing.xs, fontSize: '14px', color: colors.neutral.text }}>
                {t('audit.toDate')}
              </label>
              <Input
                type="date"
                value={filters.dateTo || ''}
                onChange={(e) => handleFilterChange('dateTo', e.target.value)}
              />
            </div>
          </div>
          <div style={{ display: 'flex', gap: spacing.sm }}>
            <Button variant="primary" onClick={refetch}>
              {t('common.search')}
            </Button>
            <Button variant="ghost" onClick={handleClearFilters}>
              {t('audit.clearFilters')}
            </Button>
          </div>
        </CardContent>
      </Card>

      <Card variant="default">
        {/* Show inline loader during refetch (when we already have data) */}
        {loading && logs.length > 0 && (
          <div style={{ padding: spacing.md, textAlign: 'center', borderBottom: `1px solid ${colors.neutral.border}` }}>
            <Loading text={t('audit.loading')} />
          </div>
        )}

        {error && (
          <div style={{ padding: spacing.xl, textAlign: 'center', color: colors.danger.main }}>
            <Text variant="body">{error}</Text>
            <Button variant="ghost" onClick={refetch} style={{ marginTop: spacing.md }}>
              {t('audit.retry')}
            </Button>
          </div>
        )}

        {!loading && !error && logs.length === 0 && (
          <EmptyState
            icon="📋"
            title={t('audit.noLogsTitle')}
            description={t('audit.noLogsDescription')}
          />
        )}

        {!loading && !error && logs.length > 0 && (
          <>
            <div style={{ padding: spacing.md, borderBottom: `1px solid ${colors.neutral.border}`, backgroundColor: colors.neutral.background }}>
              <Text variant="caption" style={{ color: colors.neutral.textMuted }}>
                {t('audit.showingEntries', { current: paginatedLogs.length, total: logs.length })}
              </Text>
            </div>
            {paginatedLogs.map(log => (
              <AuditLogRow key={log.id} log={log} t={t} language={language} />
            ))}
            {logs.length > ITEMS_PER_PAGE && (
              <div style={{ padding: spacing.md, display: 'flex', justifyContent: 'center' }}>
                <Pagination
                  currentPage={page - 1}
                  totalItems={logs.length}
                  pageSize={ITEMS_PER_PAGE}
                  onPageChange={(p) => setPage(p + 1)}
                />
              </div>
            )}
          </>
        )}
      </Card>
    </div>
  );
}
