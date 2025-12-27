import { ClientResponse } from '../../api';
import { Button, Card, Text } from '../ui';
import { spacing, fontSize, fontWeight } from '../../theme';
import { useI18n } from '../../i18n';

interface ClientDetailsHeaderProps {
  client: ClientResponse;
  onEdit: () => void;
  onDelete: () => void;
}

export function ClientDetailsHeader({ client, onEdit, onDelete }: ClientDetailsHeaderProps) {
  const { t } = useI18n();

  return (
    <Card style={{ padding: spacing.md }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
        <div>
          <Text style={{ fontSize: fontSize.xl, fontWeight: fontWeight.bold }}>
            {client.firstName} {client.lastName}
          </Text>
          <div style={{ display: 'flex', gap: spacing.md, marginTop: spacing.xs }}>
            {client.email && (
              <Text variant="muted" size="sm">
                📧 {client.email}
              </Text>
            )}
            {client.phone && (
              <Text variant="muted" size="sm">
                📱 {client.phone}
              </Text>
            )}
          </div>
          {(client.address || client.city) && (
            <Text variant="muted" size="sm" style={{ marginTop: spacing.xs }}>
              📍{' '}
              {[client.address, client.city, client.postalCode].filter(Boolean).join(', ')}
            </Text>
          )}
        </div>
        <div style={{ display: 'flex', gap: spacing.xs }}>
          <Button variant="ghost" size="sm" onClick={onEdit}>
            {t('common.edit')}
          </Button>
          <Button variant="danger" size="sm" onClick={onDelete}>
            {t('common.delete')}
          </Button>
        </div>
      </div>
    </Card>
  );
}
