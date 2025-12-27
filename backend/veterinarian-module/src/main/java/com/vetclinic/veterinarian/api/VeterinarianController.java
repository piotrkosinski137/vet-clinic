package com.vetclinic.veterinarian.api;

import static com.vetclinic.common.security.Roles.CAN_MANAGE_VETS;
import static com.vetclinic.common.security.Roles.HAS_ANY_ROLE;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vetclinic.veterinarian.api.dto.VeterinarianRequest;
import com.vetclinic.veterinarian.api.dto.VeterinarianResponse;
import com.vetclinic.veterinarian.domain.VeterinarianService;
import com.vetclinic.veterinarian.domain.model.Veterinarian;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/veterinarians")
@RequiredArgsConstructor
public class VeterinarianController {

    private final VeterinarianService veterinarianService;
    private final VeterinarianMapper veterinarianMapper;

    @PostMapping
    @PreAuthorize(CAN_MANAGE_VETS)
    public ResponseEntity<VeterinarianResponse> createVeterinarian(
            @Valid @RequestBody VeterinarianRequest request) {
        Veterinarian veterinarian = veterinarianMapper.toEntity(request);
        Veterinarian saved = veterinarianService.createVeterinarian(veterinarian);
        return ResponseEntity.status(HttpStatus.CREATED).body(veterinarianMapper.toResponse(saved));
    }

    @GetMapping
    @PreAuthorize(HAS_ANY_ROLE)
    public ResponseEntity<List<VeterinarianResponse>> getAllVeterinarians(
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String specialization,
            @RequestParam(required = false) String name) {
        List<Veterinarian> veterinarians;

        if (name != null && !name.isBlank()) {
            veterinarians = veterinarianService.searchByName(name);
        } else if (specialization != null && !specialization.isBlank()) {
            veterinarians = veterinarianService.getVeterinariansBySpecialization(specialization);
        } else if (active != null) {
            veterinarians =
                    active
                            ? veterinarianService.getActiveVeterinarians()
                            : veterinarianService.getAllVeterinarians();
        } else {
            veterinarians = veterinarianService.getAllVeterinarians();
        }

        return ResponseEntity.ok(veterinarianMapper.toResponseList(veterinarians));
    }

    @GetMapping("/{id}")
    @PreAuthorize(HAS_ANY_ROLE)
    public ResponseEntity<VeterinarianResponse> getVeterinarian(@PathVariable UUID id) {
        Veterinarian veterinarian = veterinarianService.getVeterinarian(id);
        return ResponseEntity.ok(veterinarianMapper.toResponse(veterinarian));
    }

    @GetMapping("/email/{email}")
    @PreAuthorize(HAS_ANY_ROLE)
    public ResponseEntity<VeterinarianResponse> getVeterinarianByEmail(@PathVariable String email) {
        Veterinarian veterinarian = veterinarianService.getVeterinarianByEmail(email);
        return ResponseEntity.ok(veterinarianMapper.toResponse(veterinarian));
    }

    @PutMapping("/{id}")
    @PreAuthorize(CAN_MANAGE_VETS)
    public ResponseEntity<VeterinarianResponse> updateVeterinarian(
            @PathVariable UUID id, @Valid @RequestBody VeterinarianRequest request) {
        Veterinarian updated = veterinarianMapper.toEntity(request);
        Veterinarian saved = veterinarianService.updateVeterinarian(id, updated);
        return ResponseEntity.ok(veterinarianMapper.toResponse(saved));
    }

    @PatchMapping("/{id}/active")
    @PreAuthorize(CAN_MANAGE_VETS)
    public ResponseEntity<VeterinarianResponse> toggleActive(
            @PathVariable UUID id, @RequestParam Boolean active) {
        Veterinarian veterinarian = veterinarianService.toggleActive(id, active);
        return ResponseEntity.ok(veterinarianMapper.toResponse(veterinarian));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(CAN_MANAGE_VETS)
    public ResponseEntity<Void> deleteVeterinarian(@PathVariable UUID id) {
        veterinarianService.deleteVeterinarian(id);
        return ResponseEntity.noContent().build();
    }
}
