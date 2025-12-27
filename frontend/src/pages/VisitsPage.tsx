import { useState, useEffect, useCallback, useRef } from "react";
import { useSearchParams } from "react-router-dom";
import { useVisits, useVeterinarians } from "../hooks";
import { VisitResponse, VisitStatus, VisitRequest } from "../api/types";
import {
  Button,
  Card,
  Text,
  Loading,
  PageHeader,
  useToast,
} from "../components/ui";
import { Calendar, CalendarView } from "../components/calendar";
import { BookAppointmentModal, VisitDetailsModal } from "../components/visits";
import { DoctorsManagementModal } from "../components/doctors";
import { colors, spacing, borderRadius, fontWeight, fontSize } from "../theme";
import { useI18n } from "../i18n";

function formatDateForApi(date: Date): string {
  return date.toISOString().split("T")[0];
}

export function VisitsPage() {
  const { t } = useI18n();
  const { visits, loading, error, fetchVisits, updateVisitStatus, updateVisit, reassignVisit, deleteVisit, refresh } =
    useVisits();
  const { veterinarians, refetch: refetchVeterinarians } = useVeterinarians({ active: true });
  const { success, error: showError } = useToast();

  const [view, setView] = useState<CalendarView>("day");
  const [currentDate, setCurrentDate] = useState<Date>(new Date());
  const [selectedVisit, setSelectedVisit] = useState<VisitResponse | null>(null);
  const [showBookModal, setShowBookModal] = useState(false);
  const [bookingDate, setBookingDate] = useState<Date | null>(null);
  const [bookingHour, setBookingHour] = useState<number | null>(null);
  const [bookingVeterinarianId, setBookingVeterinarianId] = useState<string | null>(null);
  const [searchParams, setSearchParams] = useSearchParams();
  const [selectedDoctor, setSelectedDoctor] = useState<string>(() => searchParams.get("doctor") || "");
  const [showDoctorsModal, setShowDoctorsModal] = useState(false);

  // Sync URL param with selected doctor
  const handleDoctorSelect = useCallback((doctorName: string) => {
    setSelectedDoctor(doctorName);
    if (doctorName) {
      setSearchParams({ doctor: doctorName });
    } else {
      setSearchParams({});
    }
  }, [setSearchParams]);

  // Read doctor from URL on mount/change
  useEffect(() => {
    const doctorFromUrl = searchParams.get("doctor");
    if (doctorFromUrl !== null && doctorFromUrl !== selectedDoctor) {
      setSelectedDoctor(doctorFromUrl);
    }
  }, [searchParams, selectedDoctor]);

  // Filter out cancelled visits (doctor filtering is handled by showing/hiding columns in day view)
  const visibleVisits = visits.filter((v) => {
    if (v.status === "CANCELLED") return false;
    // In week view, still filter by doctor
    if (view === "week" && selectedDoctor && v.veterinarianName !== selectedDoctor) return false;
    return true;
  });

  // Filter veterinarians to show only selected doctor's column (or all if none selected)
  const visibleVeterinarians = selectedDoctor
    ? veterinarians.filter((v) => v.fullName === selectedDoctor)
    : veterinarians;

  // Track last fetched params to avoid duplicate fetches
  const lastFetchParams = useRef<string>("");

  useEffect(() => {
    let dateFrom: string;
    let dateTo: string;

    if (view === "day") {
      dateFrom = formatDateForApi(currentDate);
      dateTo = formatDateForApi(currentDate);
    } else {
      const startOfWeek = new Date(currentDate);
      const dayOfWeek = currentDate.getDay();
      startOfWeek.setDate(currentDate.getDate() - (dayOfWeek === 0 ? 6 : dayOfWeek - 1));
      const endOfWeek = new Date(startOfWeek);
      endOfWeek.setDate(startOfWeek.getDate() + 6);
      dateFrom = formatDateForApi(startOfWeek);
      dateTo = formatDateForApi(endOfWeek);
    }

    // Only fetch if params have changed
    const fetchKey = `${dateFrom}-${dateTo}`;
    if (fetchKey === lastFetchParams.current) return;
    lastFetchParams.current = fetchKey;

    fetchVisits({ dateFrom, dateTo });
  }, [currentDate, view, fetchVisits]);

  const handleVisitSelect = useCallback((visit: VisitResponse) => {
    setSelectedVisit(visit);
  }, []);

  const handleSlotClick = useCallback((date: Date, hour: number, veterinarianId?: string) => {
    setBookingDate(date);
    setBookingHour(hour);
    setBookingVeterinarianId(veterinarianId || null);
    setShowBookModal(true);
  }, []);

  const handleVisitDrop = useCallback(
    async (
      visit: VisitResponse,
      newDate: Date,
      newHour: number,
      newVeterinarianId?: string,
      newVeterinarianName?: string
    ) => {
      const newDateTime = new Date(newDate);
      newDateTime.setHours(newHour, 0, 0, 0);

      // Format as local datetime without UTC conversion (backend uses LocalDateTime)
      const year = newDateTime.getFullYear();
      const month = String(newDateTime.getMonth() + 1).padStart(2, "0");
      const day = String(newDateTime.getDate()).padStart(2, "0");
      const hours = String(newDateTime.getHours()).padStart(2, "0");
      const localDateTimeStr = `${year}-${month}-${day}T${hours}:00:00`;

      try {
        await reassignVisit(
          visit.id,
          newVeterinarianId || null,
          newVeterinarianName || null,
          localDateTimeStr
        );
        success(t('visits.visitReassigned'));
      } catch (err) {
        showError(err instanceof Error ? err.message : t('visits.failedToReassign'));
      }
    },
    [reassignVisit, success, showError]
  );

  const handleVisitSave = async (data: Partial<VisitRequest>) => {
    if (!selectedVisit) return;
    try {
      await updateVisit(selectedVisit.id, {
        ...selectedVisit,
        ...data,
      });
      setSelectedVisit({ ...selectedVisit, ...data } as VisitResponse);
      success(t('visits.visitUpdated'));
    } catch (err) {
      showError(t('visits.failedToSave'));
      throw err;
    }
  };

  const handleStatusChange = async (status: VisitStatus) => {
    if (!selectedVisit) return;
    try {
      const updated = await updateVisitStatus(selectedVisit.id, status);
      setSelectedVisit(updated);
      success(t('visits.statusUpdated'));
    } catch (err) {
      showError(t('visits.failedToUpdateStatus'));
      throw err;
    }
  };

  const handleVisitDelete = async () => {
    if (!selectedVisit) return;
    if (window.confirm(t('visits.confirmDeleteVisit'))) {
      try {
        await deleteVisit(selectedVisit.id);
        setSelectedVisit(null);
        success(t('visits.appointmentDeleted'));
      } catch (err) {
        showError(t('visits.failedToDelete'));
        throw err;
      }
    }
  };

  const handleBookingComplete = () => {
    setShowBookModal(false);
    setBookingDate(null);
    setBookingHour(null);
    setBookingVeterinarianId(null);
    refresh();
    success(t('visits.appointmentBooked'));
  };

  if (loading && visits.length === 0) {
    return <Loading text={t('visits.loading')} />;
  }

  return (
    <div>
      <PageHeader
        title={t('visits.schedule')}
        actions={
          <Button variant="primary" onClick={() => setShowBookModal(true)}>
            {t('visits.bookAppointment')}
          </Button>
        }
      />

      {/* Doctor Filter Bar */}
      <div
        style={{
          display: "flex",
          alignItems: "center",
          gap: spacing.sm,
          marginBottom: spacing.md,
          padding: spacing.sm,
          backgroundColor: colors.neutral.background,
          borderRadius: borderRadius.md,
          overflowX: "auto",
        }}
      >
        <Text size="sm" style={{ fontWeight: fontWeight.medium, marginRight: spacing.xs }}>
          {t('visits.filterByDoctor')}
        </Text>
        <button
          onClick={() => handleDoctorSelect("")}
          style={{
            padding: `${spacing.xs} ${spacing.md}`,
            borderRadius: borderRadius.full,
            border: "none",
            cursor: "pointer",
            fontWeight: fontWeight.medium,
            fontSize: fontSize.sm,
            backgroundColor: selectedDoctor === "" ? colors.primary.main : colors.neutral.white,
            color: selectedDoctor === "" ? colors.neutral.white : colors.neutral.text,
            boxShadow: selectedDoctor === "" ? "none" : `0 1px 3px ${colors.neutral.border}`,
            transition: "all 0.2s ease",
          }}
        >
          {t('visits.all')}
        </button>
        {veterinarians.map((vet) => (
          <button
            key={vet.id}
            onClick={() => handleDoctorSelect(vet.fullName)}
            style={{
              padding: `${spacing.xs} ${spacing.md}`,
              borderRadius: borderRadius.full,
              border: "none",
              cursor: "pointer",
              fontWeight: fontWeight.medium,
              fontSize: fontSize.sm,
              backgroundColor: selectedDoctor === vet.fullName ? (vet.colorCode || colors.primary.main) : colors.neutral.white,
              color: selectedDoctor === vet.fullName ? colors.neutral.white : colors.neutral.text,
              boxShadow: selectedDoctor === vet.fullName ? "none" : `0 1px 3px ${colors.neutral.border}`,
              transition: "all 0.2s ease",
              whiteSpace: "nowrap",
            }}
          >
            Dr. {vet.fullName}
          </button>
        ))}
        <div style={{ marginLeft: "auto" }}>
          <Button variant="ghost" size="sm" onClick={() => setShowDoctorsModal(true)}>
            {t('visits.manageDoctors')}
          </Button>
        </div>
      </div>

      {error && (
        <Card
          style={{
            padding: spacing.md,
            backgroundColor: colors.danger.light,
            marginBottom: spacing.md,
            display: "flex",
            alignItems: "center",
            gap: spacing.md,
          }}
        >
          <span style={{ fontSize: fontSize.lg }}>⚠️</span>
          <div style={{ flex: 1 }}>
            <Text style={{ color: colors.danger.main, fontWeight: fontWeight.medium }}>
              {t('visits.errorLoadingAppointments')}
            </Text>
            <Text variant="muted" size="sm">{error}</Text>
          </div>
          <Button variant="ghost" size="sm" onClick={refresh}>
            {t('errors.retry')}
          </Button>
        </Card>
      )}

      <Calendar
        visits={visibleVisits}
        view={view}
        onViewChange={setView}
        date={currentDate}
        onDateChange={setCurrentDate}
        onVisitSelect={handleVisitSelect}
        onSlotClick={handleSlotClick}
        onVisitDrop={handleVisitDrop}
        veterinarians={visibleVeterinarians}
        style={{ height: "calc(100vh - 200px)" }}
      />

      {/* Visit Details Modal with Tabs */}
      <VisitDetailsModal
        visit={selectedVisit}
        open={!!selectedVisit}
        onClose={() => setSelectedVisit(null)}
        onSave={handleVisitSave}
        onStatusChange={handleStatusChange}
        onDelete={handleVisitDelete}
      />

      <BookAppointmentModal
        open={showBookModal}
        onClose={() => {
          setShowBookModal(false);
          setBookingDate(null);
          setBookingHour(null);
          setBookingVeterinarianId(null);
        }}
        onSuccess={handleBookingComplete}
        initialDate={bookingDate}
        initialHour={bookingHour}
        initialVeterinarianId={bookingVeterinarianId}
      />

      <DoctorsManagementModal
        open={showDoctorsModal}
        onClose={() => setShowDoctorsModal(false)}
        onDoctorsChange={refetchVeterinarians}
      />
    </div>
  );
}
