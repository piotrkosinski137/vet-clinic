import { useState, useMemo } from 'react';
import { useAuditLogs } from '../hooks';
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

function formatTimestamp(timestamp: string): string {
  const date = new Date(timestamp);
  return date.toLocaleString('pl-PL', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
  });
}

function DiffViewer({ oldValue, newValue, changedFields }: { oldValue?: string; newValue?: string; changedFields?: string }) {
  const [expanded, setExpanded] = useState(false);

  if (!oldValue && !newValue) return null;

  let oldObj: Record<string, unknown> = {};
  let newObj: Record<string, unknown> = {};

  try {
    if (oldValue) oldObj = JSON.parse(oldValue);
    if (newValue) newObj = JSON.parse(newValue);
  } catch {
    // If not valid JSON, show as text
    return (
      <div style={{ marginTop: spacing.sm }}>
        <Button variant="ghost" size="sm" onClick={() => setExpanded(!expanded)}>
          {expanded ? 'Hide Details' : 'Show Details'}
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
        {expanded ? 'Hide Changes' : `Show Changes (${fields.length} field${fields.length !== 1 ? 's' : ''})`}
      </Button>
      {expanded && (
        <div style={{ marginTop: spacing.sm, padding: spacing.sm, backgroundColor: colors.neutral.background, borderRadius: borderRadius.md }}>
          <table style={{ width: '100%', fontSize: '12px', borderCollapse: 'collapse' }}>
            <thead>
              <tr style={{ borderBottom: `1px solid ${colors.neutral.border}` }}>
                <th style={{ textAlign: 'left', padding: spacing.xs }}>Field</th>
                <th style={{ textAlign: 'left', padding: spacing.xs }}>Old Value</th>
                <th style={{ textAlign: 'left', padding: spacing.xs }}>New Value</th>
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

function AuditLogRow({ log }: { log: AuditLogResponse }) {
  return (
    <div style={{
      padding: spacing.md,
      borderBottom: `1px solid ${colors.neutral.borderLight}`,
      transition: 'background-color 0.2s',
    }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: spacing.sm }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: spacing.sm }}>
          <Badge variant={actionColors[log.action]}>{log.action}</Badge>
          <Badge variant="secondary">{log.entityType}</Badge>
          <Text variant="caption" style={{ color: colors.neutral.textMuted }}>
            {log.entityId.substring(0, 8)}...
          </Text>
        </div>
        <Text variant="caption" style={{ color: colors.neutral.textMuted }}>
          {formatTimestamp(log.timestamp)}
        </Text>
      </div>
      <div style={{ display: 'flex', alignItems: 'center', gap: spacing.sm, marginBottom: spacing.xs }}>
        <Text variant="body" style={{ fontWeight: 500 }}>{log.userName}</Text>
        {log.ipAddress && (
          <Text variant="caption" style={{ color: colors.neutral.textMuted }}>
            from {log.ipAddress}
          </Text>
        )}
      </div>
      {log.description && (
        <Text variant="caption" style={{ color: colors.neutral.textLight }}>
          {log.description}
        </Text>
      )}
      {log.action === 'UPDATE' && (
        <DiffViewer oldValue={log.oldValue} newValue={log.newValue} changedFields={log.changedFields} />
      )}
      {log.action === 'DELETE' && log.oldValue && (
        <DiffViewer oldValue={log.oldValue} changedFields="" />
      )}
    </div>
  );
}

export function AuditPage() {
  const [filters, setFilters] = useState<AuditLogFilters>({});
  const [page, setPage] = useState(1);

  const { logs, loading, error, refetch } = useAuditLogs(filters);

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

  return (
    <div>
      <PageHeader title="Audit Trail" />

      <Card variant="default" style={{ marginBottom: spacing.lg }}>
        <CardContent>
          <div style={filterRowStyle}>
            <div style={filterItemStyle}>
              <label style={{ display: 'block', marginBottom: spacing.xs, fontSize: '14px', color: colors.neutral.text }}>
                Entity Type
              </label>
              <Select
                value={filters.entityType || ''}
                onChange={(e) => handleFilterChange('entityType', e.target.value)}
              >
                <option value="">All Entities</option>
                {ENTITY_TYPES.map(type => (
                  <option key={type} value={type}>{type}</option>
                ))}
              </Select>
            </div>
            <div style={filterItemStyle}>
              <label style={{ display: 'block', marginBottom: spacing.xs, fontSize: '14px', color: colors.neutral.text }}>
                Action
              </label>
              <Select
                value={filters.action || ''}
                onChange={(e) => handleFilterChange('action', e.target.value as AuditAction)}
              >
                <option value="">All Actions</option>
                {ACTIONS.map(action => (
                  <option key={action} value={action}>{action}</option>
                ))}
              </Select>
            </div>
            <div style={filterItemStyle}>
              <label style={{ display: 'block', marginBottom: spacing.xs, fontSize: '14px', color: colors.neutral.text }}>
                From Date
              </label>
              <Input
                type="date"
                value={filters.dateFrom || ''}
                onChange={(e) => handleFilterChange('dateFrom', e.target.value)}
              />
            </div>
            <div style={filterItemStyle}>
              <label style={{ display: 'block', marginBottom: spacing.xs, fontSize: '14px', color: colors.neutral.text }}>
                To Date
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
              Search
            </Button>
            <Button variant="ghost" onClick={handleClearFilters}>
              Clear Filters
            </Button>
          </div>
        </CardContent>
      </Card>

      <Card variant="default">
        {loading && (
          <div style={{ padding: spacing.xl, textAlign: 'center' }}>
            <Loading text="Loading audit logs..." />
          </div>
        )}

        {error && (
          <div style={{ padding: spacing.xl, textAlign: 'center', color: colors.danger.main }}>
            <Text variant="body">{error}</Text>
            <Button variant="ghost" onClick={refetch} style={{ marginTop: spacing.md }}>
              Retry
            </Button>
          </div>
        )}

        {!loading && !error && logs.length === 0 && (
          <EmptyState
            icon="📋"
            title="No audit logs found"
            description="No actions match your current filters. Try adjusting your search criteria."
          />
        )}

        {!loading && !error && logs.length > 0 && (
          <>
            <div style={{ padding: spacing.md, borderBottom: `1px solid ${colors.neutral.border}`, backgroundColor: colors.neutral.background }}>
              <Text variant="caption" style={{ color: colors.neutral.textMuted }}>
                Showing {paginatedLogs.length} of {logs.length} entries
              </Text>
            </div>
            {paginatedLogs.map(log => (
              <AuditLogRow key={log.id} log={log} />
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
