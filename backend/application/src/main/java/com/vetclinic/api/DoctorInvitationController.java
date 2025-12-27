package com.vetclinic.api;

import static com.vetclinic.common.security.Roles.CAN_MANAGE_VETS;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vetclinic.api.dto.DoctorInvitationRequest;
import com.vetclinic.common.tenant.TenantContext;
import com.vetclinic.config.KeycloakAdminService;
import com.vetclinic.veterinarian.api.dto.VeterinarianResponse;
import com.vetclinic.veterinarian.domain.VeterinarianService;
import com.vetclinic.veterinarian.domain.model.Veterinarian;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller for managing doctor invitations. Allows existing doctors to invite new doctors to
 * their clinic.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/doctors")
@RequiredArgsConstructor
@Tag(name = "Doctor Invitations", description = "API for inviting new doctors to the clinic")
public class DoctorInvitationController {

    private final KeycloakAdminService keycloakAdminService;
    private final VeterinarianService veterinarianService;

    @PostMapping("/invite")
    @PreAuthorize(CAN_MANAGE_VETS)
    @Operation(
            summary = "Invite a new doctor",
            description =
                    "Sends an email invitation to a new doctor. The doctor will receive a link to set their password.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Doctor invited successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "409", description = "Doctor with this email already exists")
    })
    public ResponseEntity<VeterinarianResponse> inviteDoctor(
            @Valid @RequestBody DoctorInvitationRequest request) {

        UUID clinicId = TenantContext.requireCurrentClinicId();
        log.info(
                "Inviting new doctor {} {} ({}) to clinic {}",
                request.firstName(),
                request.lastName(),
                request.email(),
                clinicId);

        // Create user in Keycloak with email invitation
        String keycloakUserId =
                keycloakAdminService.createUserWithEmailInvitation(
                        request.email(), request.firstName(), request.lastName(), clinicId);

        log.info("Keycloak user created with ID: {}", keycloakUserId);

        // Create veterinarian record in database
        Veterinarian veterinarian =
                Veterinarian.builder()
                        .firstName(request.firstName())
                        .lastName(request.lastName())
                        .email(request.email())
                        .phone(request.phone())
                        .specialization(request.specialization())
                        .licenseNumber(request.licenseNumber())
                        .notes(request.notes())
                        .active(true)
                        .build();

        Veterinarian saved = veterinarianService.createVeterinarian(veterinarian);
        log.info("Veterinarian record created with ID: {}", saved.getId());

        VeterinarianResponse response =
                VeterinarianResponse.builder()
                        .id(saved.getId())
                        .firstName(saved.getFirstName())
                        .lastName(saved.getLastName())
                        .fullName(saved.getFullName())
                        .email(saved.getEmail())
                        .phone(saved.getPhone())
                        .specialization(saved.getSpecialization())
                        .licenseNumber(saved.getLicenseNumber())
                        .colorCode(saved.getColorCode())
                        .active(saved.getActive())
                        .notes(saved.getNotes())
                        .createdAt(saved.getCreatedAt())
                        .updatedAt(saved.getUpdatedAt())
                        .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
