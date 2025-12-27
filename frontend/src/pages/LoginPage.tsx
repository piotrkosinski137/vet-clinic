import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../auth";
import { useI18n } from "../i18n";
import { Button, Card, Input, FormField, Text } from "../components/ui";
import { colors, spacing, borderRadius, fontSize, fontWeight, shadows } from "../theme";

export function LoginPage() {
  const { t } = useI18n();
  const navigate = useNavigate();
  const { isAuthenticated, loginWithCredentials, loginError, clearLoginError } = useAuth();

  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Redirect if already authenticated
  useEffect(() => {
    if (isAuthenticated) {
      navigate("/", { replace: true });
    }
  }, [isAuthenticated, navigate]);

  // Clear error when inputs change
  useEffect(() => {
    if (loginError) {
      clearLoginError();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [username, password]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!username || !password) return;

    setIsSubmitting(true);
    try {
      await loginWithCredentials(username, password);
      // Navigation happens via the useEffect when isAuthenticated changes
    } catch {
      // Error is handled by context
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div
      style={{
        minHeight: "100vh",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        background: `linear-gradient(135deg, ${colors.primary.light} 0%, ${colors.secondary.light} 100%)`,
        padding: spacing.lg,
      }}
    >
      <Card
        style={{
          width: "100%",
          maxWidth: "420px",
          padding: spacing.xl,
          boxShadow: shadows.lg,
        }}
      >
        {/* Logo / Header */}
        <div style={{ textAlign: "center", marginBottom: spacing.xl }}>
          <div
            style={{
              width: "80px",
              height: "80px",
              margin: "0 auto",
              marginBottom: spacing.md,
              borderRadius: borderRadius.full,
              background: `linear-gradient(135deg, ${colors.primary.main} 0%, ${colors.secondary.main} 100%)`,
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              fontSize: "40px",
            }}
          >
            🏥
          </div>
          <h1
            style={{
              margin: 0,
              marginBottom: spacing.xs,
              color: colors.secondary.main,
              fontSize: fontSize.xl,
              fontWeight: fontWeight.bold,
            }}
          >
            {t("auth.welcomeBack")}
          </h1>
          <Text variant="muted">{t("auth.loginSubtitle")}</Text>
        </div>

        {/* Login Form */}
        <form onSubmit={handleSubmit}>
          <FormField label={t("auth.username")}>
            <Input
              type="text"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              placeholder={t("auth.username")}
              autoComplete="username"
              autoFocus
              disabled={isSubmitting}
              style={{
                padding: spacing.md,
                fontSize: fontSize.md,
              }}
            />
          </FormField>

          <FormField label={t("auth.password")}>
            <Input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder={t("auth.password")}
              autoComplete="current-password"
              disabled={isSubmitting}
              style={{
                padding: spacing.md,
                fontSize: fontSize.md,
              }}
            />
          </FormField>

          {/* Error Message */}
          {loginError && (
            <div
              style={{
                padding: spacing.sm,
                marginBottom: spacing.md,
                backgroundColor: colors.danger.light,
                borderRadius: borderRadius.sm,
                borderLeft: `3px solid ${colors.danger.main}`,
              }}
            >
              <Text size="sm" style={{ color: colors.danger.main }}>
                {loginError}
              </Text>
            </div>
          )}

          <Button
            type="submit"
            variant="primary"
            disabled={isSubmitting || !username || !password}
            style={{
              width: "100%",
              padding: spacing.md,
              fontSize: fontSize.md,
              fontWeight: fontWeight.semibold,
              marginTop: spacing.sm,
            }}
          >
            {isSubmitting ? t("auth.loggingIn") : t("auth.login")}
          </Button>
        </form>

        {/* Test Credentials Hint */}
        <div
          style={{
            marginTop: spacing.lg,
            padding: spacing.md,
            backgroundColor: colors.neutral.background,
            borderRadius: borderRadius.sm,
            textAlign: "center",
          }}
        >
          <Text variant="muted" size="sm">
            {t("auth.testCredentials")}
          </Text>
        </div>

        {/* Forgot Password Link */}
        <div style={{ marginTop: spacing.md, textAlign: "center" }}>
          <Text variant="muted" size="sm">
            {t("auth.forgotPassword")}{" "}
            <span style={{ color: colors.primary.main }}>{t("auth.contactAdmin")}</span>
          </Text>
        </div>
      </Card>
    </div>
  );
}
