package com.vetclinic.common.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a veterinary clinic (tenant) in the system.
 *
 * <p>All business entities (patients, clients, appointments, etc.) belong to a specific clinic.
 * This enables multi-tenancy with row-level data isolation.
 */
@Entity
@Table(name = "veterinary_clinics")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VeterinaryClinic {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    /** Unique slug for URL-friendly identification (e.g., "happy-paws-clinic"). */
    @Column(nullable = false, unique = true)
    private String slug;

    /** Contact email for the clinic. */
    private String email;

    private String phone;

    private String address;

    private String city;

    @Column(name = "postal_code")
    private String postalCode;

    /** Whether the clinic account is active. */
    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
