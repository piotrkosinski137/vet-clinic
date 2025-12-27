import { NavLink } from 'react-router-dom';
import { useAuth } from '../../auth/AuthContext';
import { useI18n } from '../../i18n';
import { colors, spacing, fontSize, fontWeight, shadows, borderRadius, layout, transitions } from '../../theme';

interface NavItem {
  path: string;
  labelKey: string;
  icon: string;
}

const navItems: NavItem[] = [
  { path: '/', labelKey: 'nav.home', icon: '🏠' },
  { path: '/patients', labelKey: 'nav.patients', icon: '🐾' },
  { path: '/clients', labelKey: 'nav.clients', icon: '👥' },
  { path: '/schedule', labelKey: 'nav.schedule', icon: '📅' },
  { path: '/statistics', labelKey: 'nav.statistics', icon: '📊' },
  { path: '/inventory', labelKey: 'nav.inventory', icon: '📦' },
  { path: '/price-list', labelKey: 'nav.priceList', icon: '🏷️' },
  { path: '/doctors', labelKey: 'nav.doctors', icon: '🩺' },
  { path: '/payments', labelKey: 'nav.payments', icon: '💰' },
  { path: '/certificates', labelKey: 'nav.certificates', icon: '💉' },
  { path: '/consents', labelKey: 'nav.consents', icon: '🔒' },
  { path: '/audit', labelKey: 'nav.audit', icon: '📋' },
];

const sidebarStyles: React.CSSProperties = {
  position: 'fixed',
  left: 0,
  top: 0,
  bottom: 0,
  width: layout.sidebarWidth,
  background: colors.gradients.sidebar,
  color: colors.neutral.white,
  display: 'flex',
  flexDirection: 'column',
  boxShadow: shadows.xl,
  zIndex: 100,
};

const logoContainerStyles: React.CSSProperties = {
  padding: spacing.lg,
  borderBottom: `1px solid rgba(255, 255, 255, 0.1)`,
};

const logoStyles: React.CSSProperties = {
  display: 'flex',
  alignItems: 'center',
  gap: spacing.md,
  fontSize: fontSize.xl,
  fontWeight: fontWeight.bold,
  background: colors.gradients.headerAccent,
  WebkitBackgroundClip: 'text',
  WebkitTextFillColor: 'transparent',
  backgroundClip: 'text',
};

const logoIconStyles: React.CSSProperties = {
  fontSize: fontSize.xxl,
  WebkitTextFillColor: 'initial',
};

const navListStyles: React.CSSProperties = {
  flex: 1,
  padding: spacing.md,
  overflowY: 'auto',
  display: 'flex',
  flexDirection: 'column',
  gap: spacing.xs,
};

const getNavLinkStyles = (isActive: boolean): React.CSSProperties => ({
  display: 'flex',
  alignItems: 'center',
  gap: spacing.md,
  padding: `${spacing.md} ${spacing.md}`,
  borderRadius: borderRadius.lg,
  textDecoration: 'none',
  color: isActive ? colors.neutral.white : 'rgba(255, 255, 255, 0.7)',
  fontSize: fontSize.sm,
  fontWeight: isActive ? fontWeight.semibold : fontWeight.normal,
  background: isActive ? colors.primary.gradient : 'transparent',
  boxShadow: isActive ? shadows.primary : 'none',
  transition: transitions.all,
  cursor: 'pointer',
});

const navIconStyles: React.CSSProperties = {
  fontSize: fontSize.lg,
  width: '28px',
  height: '28px',
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
  borderRadius: borderRadius.md,
  backgroundColor: 'rgba(255, 255, 255, 0.1)',
};

const userSectionStyles: React.CSSProperties = {
  borderTop: `1px solid rgba(255, 255, 255, 0.1)`,
  padding: spacing.md,
  background: 'rgba(0, 0, 0, 0.2)',
};

const userInfoStyles: React.CSSProperties = {
  padding: spacing.md,
  marginBottom: spacing.sm,
  backgroundColor: 'rgba(255, 255, 255, 0.05)',
  borderRadius: borderRadius.lg,
  fontSize: fontSize.sm,
  border: '1px solid rgba(255, 255, 255, 0.1)',
};

const userAvatarStyles: React.CSSProperties = {
  width: '36px',
  height: '36px',
  borderRadius: borderRadius.full,
  background: colors.primary.gradient,
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
  fontWeight: fontWeight.bold,
  fontSize: fontSize.sm,
};

const usernameStyles: React.CSSProperties = {
  fontWeight: fontWeight.semibold,
  color: colors.neutral.white,
};

const userRoleStyles: React.CSSProperties = {
  fontSize: fontSize.xs,
  color: 'rgba(255, 255, 255, 0.5)',
};

const logoutButtonStyles: React.CSSProperties = {
  width: '100%',
  padding: spacing.md,
  background: colors.gradients.danger,
  color: colors.neutral.white,
  border: 'none',
  borderRadius: borderRadius.lg,
  fontSize: fontSize.sm,
  fontWeight: fontWeight.medium,
  cursor: 'pointer',
  transition: transitions.all,
  boxShadow: shadows.sm,
};

export function Sidebar() {
  const { t } = useI18n();
  const { username, logout } = useAuth();

  const initials = username ? username.slice(0, 2).toUpperCase() : 'U';

  return (
    <aside style={sidebarStyles}>
      {/* Logo/Brand */}
      <div style={logoContainerStyles}>
        <div style={logoStyles}>
          <span style={logoIconStyles}>🐕</span>
          <span>VetClinic</span>
        </div>
      </div>

      {/* Navigation */}
      <nav style={navListStyles}>
        {navItems.map((item) => (
          <NavLink
            key={item.path}
            to={item.path}
            style={({ isActive }) => getNavLinkStyles(isActive)}
            end={item.path === '/'}
          >
            <span style={navIconStyles}>{item.icon}</span>
            <span>{t(item.labelKey)}</span>
          </NavLink>
        ))}
      </nav>

      {/* User Section */}
      <div style={userSectionStyles}>
        <div style={userInfoStyles}>
          <div style={{ display: 'flex', alignItems: 'center', gap: spacing.sm }}>
            <div style={userAvatarStyles}>{initials}</div>
            <div>
              <div style={usernameStyles}>{username || t('auth.user')}</div>
              <div style={userRoleStyles}>{t('auth.veterinarian')}</div>
            </div>
          </div>
        </div>
        <button
          style={logoutButtonStyles}
          onClick={logout}
          onMouseEnter={(e) => {
            e.currentTarget.style.transform = 'translateY(-1px)';
            e.currentTarget.style.boxShadow = shadows.danger;
          }}
          onMouseLeave={(e) => {
            e.currentTarget.style.transform = 'translateY(0)';
            e.currentTarget.style.boxShadow = shadows.sm;
          }}
        >
          {t('auth.logout')}
        </button>
      </div>
    </aside>
  );
}
