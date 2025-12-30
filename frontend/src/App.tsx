import { BrowserRouter, Routes, Route, NavLink, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './auth';
import { I18nProvider, useI18n, LanguageSelector } from './i18n';
import { QueryProvider } from './providers';
import { useAuthErrorHandler, useDashboardStats } from './hooks';
import { PatientsPage, PatientDetailsPage, ClientsPage, VisitsPage, WaitingRoomPage, StatisticsPage, InventoryUsagePage, PriceListPage, LoginPage, DoctorsPage, InventoryPage, AuditPage, ConsentsPage, CertificatesPage, PaymentsPage, VisitDetailsPage } from './pages';
import { Button, Card, CardTitle, Text, Loading, ToastProvider, StatCard } from './components/ui';
import { ProtectedRoute } from './components/auth';
import { ErrorBoundary } from './components/errors';
import { Sidebar } from './components/layout';
import { colors, spacing, borderRadius, shadows, layout, fontSize, fontWeight } from './theme';

function App() {
  return (
    <ErrorBoundary>
      <QueryProvider>
        <I18nProvider>
          <ToastProvider>
            <AuthProvider>
              <BrowserRouter>
                <AppContent />
              </BrowserRouter>
            </AuthProvider>
          </ToastProvider>
        </I18nProvider>
      </QueryProvider>
    </ErrorBoundary>
  );
}

const layoutStyles: React.CSSProperties = {
  display: 'flex',
  minHeight: '100vh',
};

const mainContentStyles: React.CSSProperties = {
  marginLeft: layout.sidebarWidth,
  flex: 1,
  background: colors.gradients.pageBackground,
  minHeight: '100vh',
  display: 'flex',
  flexDirection: 'column',
};

const topBarStyles: React.CSSProperties = {
  position: 'sticky',
  top: 0,
  zIndex: 50,
  display: 'flex',
  justifyContent: 'space-between',
  alignItems: 'center',
  padding: `${spacing.md} ${spacing.lg}`,
  background: colors.gradients.sidebar,
  boxShadow: shadows.lg,
};

const contentWrapperStyles: React.CSSProperties = {
  flex: 1,
  padding: spacing.lg,
};

const brandStyles: React.CSSProperties = {
  display: 'flex',
  alignItems: 'center',
  gap: spacing.md,
};

const brandIconStyles: React.CSSProperties = {
  fontSize: '24px',
  width: '40px',
  height: '40px',
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
  backgroundColor: 'rgba(255, 255, 255, 0.1)',
};

const brandTextStyles: React.CSSProperties = {
  fontSize: fontSize.lg,
  fontWeight: fontWeight.semibold,
  background: colors.gradients.headerAccent,
  WebkitBackgroundClip: 'text',
  WebkitTextFillColor: 'transparent',
  backgroundClip: 'text',
};

function AppContent() {
  const { isAuthenticated, isLoading } = useAuth();
  const { t } = useI18n();

  // Listen for 401 errors and redirect to login
  useAuthErrorHandler();

  if (isLoading) {
    return (
      <div
        className="app"
        style={{
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center',
          minHeight: '100vh',
          background: colors.gradients.pageBackground,
        }}
      >
        <Card variant="elevated" style={{ padding: spacing.xxl, textAlign: 'center' }}>
          <Loading text={t('common.loading')} />
        </Card>
      </div>
    );
  }

  return (
    <Routes>
      {/* Public routes */}
      <Route
        path="/login"
        element={
          isAuthenticated ? (
            <Navigate to="/" replace />
          ) : (
            <div
              className="app"
              style={{
                minHeight: '100vh',
                background: colors.gradients.pageBackground,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
              }}
            >
              <LoginPage />
            </div>
          )
        }
      />

      {/* Protected routes with authenticated layout */}
      <Route
        path="/*"
        element={
          <ProtectedRoute>
            <AuthenticatedLayout />
          </ProtectedRoute>
        }
      />
    </Routes>
  );
}

/**
 * Layout for authenticated users with sidebar and main content area.
 */
function AuthenticatedLayout() {
  return (
    <div className="app" style={layoutStyles}>
      <Sidebar />
      <main style={mainContentStyles}>
        <header style={topBarStyles}>
          <div style={brandStyles}>
            <span style={brandIconStyles}>🏥</span>
            <span style={brandTextStyles}>VetClinic Dashboard</span>
          </div>
          <LanguageSelector />
        </header>
        <div style={contentWrapperStyles}>
          <Routes>
            <Route path="/" element={<HomePage />} />
            <Route path="/patients" element={<PatientsPage />} />
            <Route path="/patients/:id" element={<PatientDetailsPage />} />
            <Route path="/clients" element={<ClientsPage />} />
            <Route path="/schedule" element={<VisitsPage />} />
            <Route path="/visit/:id" element={<VisitDetailsPage />} />
            <Route path="/visits/:id" element={<VisitDetailsPage />} />
            <Route path="/waiting-room" element={<WaitingRoomPage />} />
            <Route path="/statistics" element={<StatisticsPage />} />
            <Route path="/inventory" element={<InventoryPage />} />
            <Route path="/inventory-usage" element={<InventoryUsagePage />} />
            <Route path="/price-list" element={<PriceListPage />} />
            <Route path="/doctors" element={<DoctorsPage />} />
            <Route path="/payments" element={<PaymentsPage />} />
            <Route path="/certificates" element={<CertificatesPage />} />
            <Route path="/consents" element={<ConsentsPage />} />
            <Route path="/audit" element={<AuditPage />} />
          </Routes>
        </div>
      </main>
    </div>
  );
}

function HomePage() {
  const { t } = useI18n();
  const { stats, loading } = useDashboardStats();

  const headerStyles: React.CSSProperties = {
    marginBottom: spacing.xl,
  };

  const titleStyles: React.CSSProperties = {
    fontSize: '28px',
    fontWeight: 700,
    color: colors.secondary.main,
    marginBottom: spacing.xs,
  };

  const subtitleStyles: React.CSSProperties = {
    color: colors.neutral.textLight,
    fontSize: '16px',
  };

  const statsGridStyles: React.CSSProperties = {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fill, minmax(240px, 1fr))',
    gap: spacing.lg,
    marginBottom: spacing.xl,
  };

  const cardStyles: React.CSSProperties = {
    display: 'flex',
    flexDirection: 'column',
    height: '100%',
    padding: spacing.lg,
  };

  const gridStyles: React.CSSProperties = {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))',
    gap: spacing.lg,
  };

  const cardIconStyles: React.CSSProperties = {
    width: '48px',
    height: '48px',
    borderRadius: borderRadius.lg,
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    fontSize: '24px',
    marginBottom: spacing.md,
  };

  const navigationCards = [
    {
      path: '/patients',
      labelKey: 'nav.patients',
      descKey: 'home.patientsDesc',
      btnKey: 'home.viewPatients',
      icon: '🐾',
      bgColor: colors.primary.light,
      gradient: colors.primary.gradient,
    },
    {
      path: '/clients',
      labelKey: 'nav.clients',
      descKey: 'home.clientsDesc',
      btnKey: 'home.viewClients',
      icon: '👥',
      bgColor: colors.info.light,
      gradient: colors.gradients.headerAccent,
    },
    {
      path: '/schedule',
      labelKey: 'nav.schedule',
      descKey: 'home.scheduleDesc',
      btnKey: 'home.viewSchedule',
      icon: '📅',
      bgColor: colors.success.light,
      gradient: colors.gradients.success,
    },
    {
      path: '/statistics',
      labelKey: 'nav.statistics',
      descKey: 'home.statisticsDesc',
      btnKey: 'home.viewStatistics',
      icon: '📊',
      bgColor: colors.warning.light,
      gradient: colors.gradients.warning,
    },
    {
      path: '/inventory',
      labelKey: 'nav.inventory',
      descKey: 'home.inventoryDesc',
      btnKey: 'home.viewInventory',
      icon: '📦',
      bgColor: colors.primary.lighter,
      gradient: colors.primary.gradient,
    },
    {
      path: '/price-list',
      labelKey: 'nav.priceList',
      descKey: 'home.priceListDesc',
      btnKey: 'home.viewPriceList',
      icon: '🏷️',
      bgColor: colors.danger.light,
      gradient: colors.gradients.danger,
    },
  ];

  return (
    <div>
      {/* Header Section */}
      <div style={headerStyles}>
        <h1 style={titleStyles}>{t('home.title')}</h1>
        <p style={subtitleStyles}>{t('home.welcome')}</p>
      </div>

      {/* Quick Stats */}
      <div style={statsGridStyles}>
        <StatCard
          label="Today's Appointments"
          value={loading ? '-' : String(stats?.visitsToday ?? 0)}
          icon="📅"
          color="primary"
        />
        <StatCard
          label="Active Patients"
          value={loading ? '-' : String(stats?.totalPatients ?? 0)}
          icon="🐾"
          color="success"
        />
        <StatCard
          label="Pending Invoices"
          value={loading ? '-' : String(stats?.pendingInvoices ?? 0)}
          icon="📄"
          color="warning"
        />
        <StatCard
          label="Low Stock Items"
          value={loading ? '-' : String(stats?.lowStockItems ?? 0)}
          icon="⚠️"
          color="danger"
        />
      </div>

      {/* Navigation Cards */}
      <h2 style={{ marginBottom: spacing.lg, color: colors.secondary.main }}>
        Quick Access
      </h2>
      <div style={gridStyles}>
        {navigationCards.map((card) => (
          <Card
            key={card.path}
            variant="default"
            hoverable
            style={cardStyles}
          >
            <div
              style={{
                ...cardIconStyles,
                backgroundColor: card.bgColor,
              }}
            >
              {card.icon}
            </div>
            <CardTitle size="lg">{t(card.labelKey)}</CardTitle>
            <Text
              variant="caption"
              style={{ flex: 1, marginBottom: spacing.md }}
            >
              {t(card.descKey)}
            </Text>
            <NavLink to={card.path} style={{ textDecoration: 'none' }}>
              <Button
                variant="primary"
                style={{
                  width: '100%',
                  background: card.gradient,
                }}
              >
                {t(card.btnKey)}
              </Button>
            </NavLink>
          </Card>
        ))}
      </div>
    </div>
  );
}

export default App;
