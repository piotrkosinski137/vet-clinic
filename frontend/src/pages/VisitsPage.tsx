import { useState, useEffect, useCallback, useRef } from "react";
import { useSearchParams } from "react-router-dom";
import { useVisits, useVeterinarians, useConfirmDialog } from "../hooks";
import { api } from "../api";
import { VisitResponse, VisitStatus, VisitRequest, CheckInRequest, VisitType } from "../api/types";
import {
  Button,
  Card,
  Text,
  Loading,
  Spinner,
  PageHeader,
  useToast,
  Tabs,
  TabList,
  Tab,
  TabPanel,
  ConfirmDialog,
} from "../components/ui";
import { Calendar, CalendarView } from "../components/calendar";
import { BookAppointmentModal, VisitDetailsModal } from "../components/visits";
import { DoctorsManagementModal } from "../components/doctors";
import { DoctorSchedulesPanel } from "../components/schedules";
import { colors, spacing, borderRadius, fontWeight, fontSize } from "../theme";
import { useI18n } from "../i18n";

function formatDateForApi(date: Date): string {
  return date.toISOString().split("T")[0];
}

export function VisitsPage() {
  const { t } = useI18n();
  const { visits, loading, error, fetchVisits, updateVisitStatus, updateVisit, reassignVisit, deleteVisit, checkIn, refresh } =
    useVisits();
  const { veterinarians, refetch: refetchVeterinarians } = useVeterinarians({ active: true });
  const { success, error: showError } = useToast();
  const { dialogState, showConfirm, closeDialog, handleConfirm } = useConfirmDialog();

  const [view, setView] = useState<CalendarView>("day");
  const [currentDate, setCurrentDate] = useState<Date>(new Date());
  const [selectedVisit, setSelectedVisit] = useState<VisitResponse | null>(null);
  const [showBookModal, setShowBookModal] = useState(false);
  const [bookingDate, setBookingDate] = useState<Date | null>(null);
  const [bookingHour, setBookingHour] = useState<number | null>(null);
  const [bookingMinute, setBookingMinute] = useState<number | null>(null);
  const [bookingVeterinarianId, setBookingVeterinarianId] = useState<string | null>(null);
  // Follow-up booking state
  const [followUpPatientId, setFollowUpPatientId] = useState<string | undefined>();
  const [followUpPatientName, setFollowUpPatientName] = useState<string | undefined>();
  const [followUpClientId, setFollowUpClientId] = useState<string | undefined>();
  const [followUpClientName, setFollowUpClientName] = useState<string | undefined>();
  const [followUpVisitType, setFollowUpVisitType] = useState<VisitType | undefined>();
  const [followUpReason, setFollowUpReason] = useState<string | undefined>();
  const [followUpPreviousVisitId, setFollowUpPreviousVisitId] = useState<string | undefined>();
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

  const handleSlotClick = useCallback((date: Date, hour: number, minute: number, veterinarianId?: string) => {
    setBookingDate(date);
    setBookingHour(hour);
    setBookingMinute(minute);
    setBookingVeterinarianId(veterinarianId || null);
    setShowBookModal(true);
  }, []);

  const handleVisitDrop = useCallback(
    async (
      visit: VisitResponse,
      newDate: Date,
      newHour: number,
      newMinute: number,
      newVeterinarianId?: string,
      newVeterinarianName?: string
    ) => {
      const newDateTime = new Date(newDate);
      newDateTime.setHours(newHour, newMinute, 0, 0);

      // Format as local datetime without UTC conversion (backend uses LocalDateTime)
      const year = newDateTime.getFullYear();
      const month = String(newDateTime.getMonth() + 1).padStart(2, "0");
      const day = String(newDateTime.getDate()).padStart(2, "0");
      const hours = String(newDateTime.getHours()).padStart(2, "0");
      const minutes = String(newDateTime.getMinutes()).padStart(2, "0");
      const localDateTimeStr = `${year}-${month}-${day}T${hours}:${minutes}:00`;

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
    [reassignVisit, success, showError, t]
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
      const updatedVisit = await updateVisitStatus(selectedVisit.id, status);
      success(t('visits.statusUpdated'));
      // If completed or cancelled, close the modal and refresh visits
      if (status === 'COMPLETED' || status === 'CANCELLED') {
        setSelectedVisit(null);
        refresh();
      } else {
        // Update selected visit with new status to reflect changes in modal
        setSelectedVisit(updatedVisit);
        refresh();
      }
    } catch (err) {
      showError(t('visits.failedToUpdateStatus'));
      throw err;
    }
  };

  const handleVisitDelete = () => {
    if (!selectedVisit) return;
    showConfirm(
      t('common.confirm'),
      t('visits.confirmDeleteVisit'),
      async () => {
        try {
          await deleteVisit(selectedVisit.id);
          setSelectedVisit(null);
          success(t('visits.appointmentDeleted'));
        } catch (err) {
          showError(t('visits.failedToDelete'));
          throw err;
        }
      }
    );
  };

  const handleCheckIn = async (request?: CheckInRequest) => {
    if (!selectedVisit) return;
    try {
      await checkIn(selectedVisit.id, request);
      setSelectedVisit(null);
      success(t('visits.checkedInSuccessfully'));
    } catch (err) {
      showError(err instanceof Error ? err.message : t('errors.failedToSave'));
      throw err;
    }
  };

  const handleBookFollowUp = (data: {
    patientId: string;
    patientName?: string;
    clientId?: string;
    clientName?: string;
    veterinarianId?: string;
    veterinarianName?: string;
    visitDate: string;
    visitType: VisitType;
    reason: string;
    previousVisitId: string;
  }) => {
    const visitDateTime = new Date(data.visitDate);
    setBookingDate(visitDateTime);
    setBookingHour(visitDateTime.getHours());
    setBookingMinute(visitDateTime.getMinutes());
    setBookingVeterinarianId(data.veterinarianId || null);
    setFollowUpPatientId(data.patientId);
    setFollowUpPatientName(data.patientName);
    setFollowUpClientId(data.clientId);
    setFollowUpClientName(data.clientName);
    setFollowUpVisitType(data.visitType);
    setFollowUpReason(data.reason);
    setFollowUpPreviousVisitId(data.previousVisitId);
    setSelectedVisit(null); // Close the visit details modal
    setShowBookModal(true); // Open the booking modal
  };

  const clearBookingState = () => {
    setBookingDate(null);
    setBookingHour(null);
    setBookingMinute(null);
    setBookingVeterinarianId(null);
    setFollowUpPatientId(undefined);
    setFollowUpPatientName(undefined);
    setFollowUpClientId(undefined);
    setFollowUpClientName(undefined);
    setFollowUpVisitType(undefined);
    setFollowUpReason(undefined);
    setFollowUpPreviousVisitId(undefined);
  };

  const handleBookingComplete = async (returnToVisitId?: string) => {
    setShowBookModal(false);
    clearBookingState();
    refresh();
    success(t('visits.appointmentBooked'));

    // If this was a follow-up booking, re-open the previous visit
    if (returnToVisitId) {
      try {
        // Try to find in current list first, otherwise fetch from API
        let previousVisit = visits.find(v => v.id === returnToVisitId);
        if (!previousVisit) {
          previousVisit = await api.getVisit(returnToVisitId);
        }
        if (previousVisit) {
          setSelectedVisit(previousVisit);
        }
      } catch {
        // If we can't find the visit, just don't re-open it
      }
    }
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

      <Tabs defaultTab="calendar">
        <TabList>
          <Tab value="calendar" icon="📅">{t('visits.calendar')}</Tab>
          <Tab value="schedules" icon="🕐">{t('schedule.doctorSchedules')}</Tab>
        </TabList>

        <TabPanel value="calendar">
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

          {/* Show loading indicator during refetch */}
          {loading && visits.length > 0 && (
            <div
              style={{
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                gap: spacing.sm,
                padding: spacing.sm,
                marginBottom: spacing.sm,
                backgroundColor: colors.primary.light,
                borderRadius: borderRadius.md,
              }}
            >
              <Spinner size="sm" />
              <Text size="sm" style={{ color: colors.primary.main }}>
                {t('visits.loading')}
              </Text>
            </div>
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
            style={{ height: "calc(100vh - 250px)" }}
          />
        </TabPanel>

        <TabPanel value="schedules">
          <DoctorSchedulesPanel />
        </TabPanel>
      </Tabs>

      {/* Visit Details Modal with Tabs */}
      <VisitDetailsModal
        visit={selectedVisit}
        open={!!selectedVisit}
        onClose={() => setSelectedVisit(null)}
        onSave={handleVisitSave}
        onStatusChange={handleStatusChange}
        onDelete={handleVisitDelete}
        onCheckIn={handleCheckIn}
        onBookFollowUp={handleBookFollowUp}
      />

      <BookAppointmentModal
        open={showBookModal}
        onClose={() => {
          setShowBookModal(false);
          clearBookingState();
        }}
        onSuccess={handleBookingComplete}
        initialDate={bookingDate}
        initialHour={bookingHour}
        initialMinute={bookingMinute}
        initialVeterinarianId={bookingVeterinarianId}
        initialPatientId={followUpPatientId}
        initialPatientName={followUpPatientName}
        initialClientId={followUpClientId}
        initialClientName={followUpClientName}
        initialVisitType={followUpVisitType}
        initialReason={followUpReason}
        previousVisitId={followUpPreviousVisitId}
      />

      <DoctorsManagementModal
        open={showDoctorsModal}
        onClose={() => setShowDoctorsModal(false)}
        onDoctorsChange={refetchVeterinarians}
      />

      <ConfirmDialog
        open={dialogState.open}
        onClose={closeDialog}
        onConfirm={handleConfirm}
        title={dialogState.title}
        message={dialogState.message}
        confirmLabel={t('common.delete')}
        cancelLabel={t('common.cancel')}
        variant="danger"
      />
    </div>
  );
}
