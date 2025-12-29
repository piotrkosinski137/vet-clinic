package com.vetclinic.visit.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vetclinic.common.event.DomainEventPublisher;
import com.vetclinic.visit.domain.model.Visit;
import com.vetclinic.visit.domain.model.VisitPriority;
import com.vetclinic.visit.domain.model.VisitStatus;
import com.vetclinic.visit.domain.port.VeterinarianAvailabilityChecker;
import com.vetclinic.visit.domain.port.VisitRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("VisitService - Waiting Room Operations")
class VisitServiceWaitingRoomTest {

    @Mock private VisitRepository visitRepository;
    @Mock private DomainEventPublisher eventPublisher;
    @Mock private VeterinarianAvailabilityChecker availabilityChecker;

    @Captor private ArgumentCaptor<Visit> visitCaptor;

    private VisitService visitService;

    @BeforeEach
    void setUp() {
        visitService = new VisitService(visitRepository, eventPublisher, availabilityChecker);
    }

    @Nested
    @DisplayName("Check In to Waiting Room")
    class CheckInToWaitingRoom {

        @Test
        @DisplayName("should check in a scheduled visit to waiting room")
        void shouldCheckInScheduledVisit() {
            var visitId = UUID.randomUUID();
            var visit = aScheduledVisit(visitId);
            given(visitRepository.findById(visitId)).willReturn(Optional.of(visit));
            given(visitRepository.save(any(Visit.class))).willAnswer(inv -> inv.getArgument(0));

            var result =
                    visitService.checkInToWaitingRoom(
                            visitId, "Patient is anxious", VisitPriority.HIGH);

            assertThat(result.getStatus()).isEqualTo(VisitStatus.CHECKED_IN);
            assertThat(result.getCheckedInAt()).isNotNull();
            assertThat(result.getWaitingRoomNotes()).isEqualTo("Patient is anxious");
            assertThat(result.getPriority()).isEqualTo(VisitPriority.HIGH);
            verify(visitRepository).save(visitCaptor.capture());
            verify(eventPublisher).publishUpdated(eq("Visit"), eq(visitId), any(), any(), anySet());
        }

        @Test
        @DisplayName("should check in without notes and priority")
        void shouldCheckInWithoutNotesAndPriority() {
            var visitId = UUID.randomUUID();
            var visit = aScheduledVisit(visitId);
            given(visitRepository.findById(visitId)).willReturn(Optional.of(visit));
            given(visitRepository.save(any(Visit.class))).willAnswer(inv -> inv.getArgument(0));

            var result = visitService.checkInToWaitingRoom(visitId, null, null);

            assertThat(result.getStatus()).isEqualTo(VisitStatus.CHECKED_IN);
            assertThat(result.getCheckedInAt()).isNotNull();
            assertThat(result.getWaitingRoomNotes()).isNull();
            assertThat(result.getPriority()).isEqualTo(VisitPriority.NORMAL);
        }

        @Test
        @DisplayName("should throw when visit is already checked in")
        void shouldThrowWhenAlreadyCheckedIn() {
            var visitId = UUID.randomUUID();
            var visit = aCheckedInVisit(visitId);
            given(visitRepository.findById(visitId)).willReturn(Optional.of(visit));

            assertThatThrownBy(() -> visitService.checkInToWaitingRoom(visitId, null, null))
                    .isInstanceOf(InvalidVisitStateException.class)
                    .hasMessageContaining("CHECKED_IN");
        }

        @Test
        @DisplayName("should throw when visit is in progress")
        void shouldThrowWhenInProgress() {
            var visitId = UUID.randomUUID();
            var visit = aVisitWithStatus(visitId, VisitStatus.IN_PROGRESS);
            given(visitRepository.findById(visitId)).willReturn(Optional.of(visit));

            assertThatThrownBy(() -> visitService.checkInToWaitingRoom(visitId, null, null))
                    .isInstanceOf(InvalidVisitStateException.class)
                    .hasMessageContaining("IN_PROGRESS");
        }

        @Test
        @DisplayName("should throw when visit is completed")
        void shouldThrowWhenCompleted() {
            var visitId = UUID.randomUUID();
            var visit = aVisitWithStatus(visitId, VisitStatus.COMPLETED);
            given(visitRepository.findById(visitId)).willReturn(Optional.of(visit));

            assertThatThrownBy(() -> visitService.checkInToWaitingRoom(visitId, null, null))
                    .isInstanceOf(InvalidVisitStateException.class)
                    .hasMessageContaining("COMPLETED");
        }

        @Test
        @DisplayName("should throw when visit not found")
        void shouldThrowWhenNotFound() {
            var visitId = UUID.randomUUID();
            given(visitRepository.findById(visitId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> visitService.checkInToWaitingRoom(visitId, null, null))
                    .isInstanceOf(VisitNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Get Waiting Room Visits")
    class GetWaitingRoomVisits {

        @Test
        @DisplayName("should return visits in waiting room for today")
        void shouldReturnWaitingRoomVisits() {
            var today = LocalDate.now();
            var visits =
                    List.of(aCheckedInVisit(UUID.randomUUID()), aCheckedInVisit(UUID.randomUUID()));
            given(
                            visitRepository.findWaitingRoomVisits(
                                    today.atStartOfDay(), today.plusDays(1).atStartOfDay()))
                    .willReturn(visits);

            var result = visitService.getWaitingRoomVisits();

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("should return empty list when no visits in waiting room")
        void shouldReturnEmptyList() {
            var today = LocalDate.now();
            given(
                            visitRepository.findWaitingRoomVisits(
                                    today.atStartOfDay(), today.plusDays(1).atStartOfDay()))
                    .willReturn(List.of());

            var result = visitService.getWaitingRoomVisits();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Start Visit from Waiting Room")
    class StartVisitFromWaitingRoom {

        @Test
        @DisplayName("should start visit from waiting room")
        void shouldStartVisitFromWaitingRoom() {
            var visitId = UUID.randomUUID();
            var visit = aCheckedInVisit(visitId);
            given(visitRepository.findById(visitId)).willReturn(Optional.of(visit));
            given(visitRepository.save(any(Visit.class))).willAnswer(inv -> inv.getArgument(0));

            var result = visitService.startVisitFromWaitingRoom(visitId);

            assertThat(result.getStatus()).isEqualTo(VisitStatus.IN_PROGRESS);
            verify(visitRepository).save(any(Visit.class));
            verify(eventPublisher).publishUpdated(eq("Visit"), eq(visitId), any(), any(), anySet());
        }

        @Test
        @DisplayName("should throw when visit is not checked in")
        void shouldThrowWhenNotCheckedIn() {
            var visitId = UUID.randomUUID();
            var visit = aScheduledVisit(visitId);
            given(visitRepository.findById(visitId)).willReturn(Optional.of(visit));

            assertThatThrownBy(() -> visitService.startVisitFromWaitingRoom(visitId))
                    .isInstanceOf(InvalidVisitStateException.class)
                    .hasMessageContaining("CHECKED_IN");
        }

        @Test
        @DisplayName("should throw when visit is already in progress")
        void shouldThrowWhenAlreadyInProgress() {
            var visitId = UUID.randomUUID();
            var visit = aVisitWithStatus(visitId, VisitStatus.IN_PROGRESS);
            given(visitRepository.findById(visitId)).willReturn(Optional.of(visit));

            assertThatThrownBy(() -> visitService.startVisitFromWaitingRoom(visitId))
                    .isInstanceOf(InvalidVisitStateException.class);
        }
    }

    @Nested
    @DisplayName("Mark as No Show")
    class MarkAsNoShow {

        @Test
        @DisplayName("should mark scheduled visit as no show")
        void shouldMarkScheduledAsNoShow() {
            var visitId = UUID.randomUUID();
            var visit = aScheduledVisit(visitId);
            given(visitRepository.findById(visitId)).willReturn(Optional.of(visit));
            given(visitRepository.save(any(Visit.class))).willAnswer(inv -> inv.getArgument(0));

            var result = visitService.markAsNoShow(visitId);

            assertThat(result.getStatus()).isEqualTo(VisitStatus.NO_SHOW);
            verify(visitRepository).save(any(Visit.class));
        }

        @Test
        @DisplayName("should mark checked in visit as no show")
        void shouldMarkCheckedInAsNoShow() {
            var visitId = UUID.randomUUID();
            var visit = aCheckedInVisit(visitId);
            given(visitRepository.findById(visitId)).willReturn(Optional.of(visit));
            given(visitRepository.save(any(Visit.class))).willAnswer(inv -> inv.getArgument(0));

            var result = visitService.markAsNoShow(visitId);

            assertThat(result.getStatus()).isEqualTo(VisitStatus.NO_SHOW);
        }

        @Test
        @DisplayName("should throw when visit is in progress")
        void shouldThrowWhenInProgress() {
            var visitId = UUID.randomUUID();
            var visit = aVisitWithStatus(visitId, VisitStatus.IN_PROGRESS);
            given(visitRepository.findById(visitId)).willReturn(Optional.of(visit));

            assertThatThrownBy(() -> visitService.markAsNoShow(visitId))
                    .isInstanceOf(InvalidVisitStateException.class)
                    .hasMessageContaining("NO_SHOW");
        }

        @Test
        @DisplayName("should throw when visit is completed")
        void shouldThrowWhenCompleted() {
            var visitId = UUID.randomUUID();
            var visit = aVisitWithStatus(visitId, VisitStatus.COMPLETED);
            given(visitRepository.findById(visitId)).willReturn(Optional.of(visit));

            assertThatThrownBy(() -> visitService.markAsNoShow(visitId))
                    .isInstanceOf(InvalidVisitStateException.class);
        }
    }

    @Nested
    @DisplayName("Update Waiting Room Info")
    class UpdateWaitingRoomInfo {

        @Test
        @DisplayName("should update waiting room notes and priority")
        void shouldUpdateNotesAndPriority() {
            var visitId = UUID.randomUUID();
            var visit = aCheckedInVisit(visitId);
            given(visitRepository.findById(visitId)).willReturn(Optional.of(visit));
            given(visitRepository.save(any(Visit.class))).willAnswer(inv -> inv.getArgument(0));

            var result =
                    visitService.updateWaitingRoomInfo(
                            visitId, "Updated notes", VisitPriority.URGENT);

            assertThat(result.getWaitingRoomNotes()).isEqualTo("Updated notes");
            assertThat(result.getPriority()).isEqualTo(VisitPriority.URGENT);
            verify(visitRepository).save(any(Visit.class));
        }

        @Test
        @DisplayName("should update only notes when priority is null")
        void shouldUpdateOnlyNotes() {
            var visitId = UUID.randomUUID();
            var visit = aCheckedInVisit(visitId);
            visit.setPriority(VisitPriority.HIGH);
            given(visitRepository.findById(visitId)).willReturn(Optional.of(visit));
            given(visitRepository.save(any(Visit.class))).willAnswer(inv -> inv.getArgument(0));

            var result = visitService.updateWaitingRoomInfo(visitId, "New notes", null);

            assertThat(result.getWaitingRoomNotes()).isEqualTo("New notes");
            assertThat(result.getPriority()).isEqualTo(VisitPriority.HIGH);
        }

        @Test
        @DisplayName("should not save when nothing changed")
        void shouldNotSaveWhenNothingChanged() {
            var visitId = UUID.randomUUID();
            var visit = aCheckedInVisit(visitId);
            visit.setWaitingRoomNotes("Same notes");
            visit.setPriority(VisitPriority.NORMAL);
            given(visitRepository.findById(visitId)).willReturn(Optional.of(visit));

            var result =
                    visitService.updateWaitingRoomInfo(visitId, "Same notes", VisitPriority.NORMAL);

            assertThat(result.getWaitingRoomNotes()).isEqualTo("Same notes");
            verify(visitRepository, never()).save(any(Visit.class));
        }

        @Test
        @DisplayName("should throw when visit is not checked in")
        void shouldThrowWhenNotCheckedIn() {
            var visitId = UUID.randomUUID();
            var visit = aScheduledVisit(visitId);
            given(visitRepository.findById(visitId)).willReturn(Optional.of(visit));

            assertThatThrownBy(() -> visitService.updateWaitingRoomInfo(visitId, "notes", null))
                    .isInstanceOf(InvalidVisitStateException.class)
                    .hasMessageContaining("CHECKED_IN");
        }
    }

    // Test fixtures

    private static Visit aScheduledVisit(UUID id) {
        var visit =
                Visit.builder()
                        .patientId(UUID.randomUUID())
                        .visitDate(LocalDateTime.now())
                        .status(VisitStatus.SCHEDULED)
                        .priority(VisitPriority.NORMAL)
                        .build();
        visit.setId(id);
        return visit;
    }

    private static Visit aCheckedInVisit(UUID id) {
        var visit =
                Visit.builder()
                        .patientId(UUID.randomUUID())
                        .visitDate(LocalDateTime.now())
                        .status(VisitStatus.CHECKED_IN)
                        .checkedInAt(LocalDateTime.now().minusMinutes(10))
                        .priority(VisitPriority.NORMAL)
                        .build();
        visit.setId(id);
        return visit;
    }

    private static Visit aVisitWithStatus(UUID id, VisitStatus status) {
        var visit =
                Visit.builder()
                        .patientId(UUID.randomUUID())
                        .visitDate(LocalDateTime.now())
                        .status(status)
                        .priority(VisitPriority.NORMAL)
                        .build();
        visit.setId(id);
        return visit;
    }
}
