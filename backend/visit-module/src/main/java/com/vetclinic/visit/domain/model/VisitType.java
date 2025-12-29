package com.vetclinic.visit.domain.model;

/**
 * Types of veterinary visits.
 *
 * <p>Each type represents a different category of veterinary service that can be provided during a
 * visit.
 */
public enum VisitType {
    /** General consultation. */
    CONSULTATION,

    /** Vaccination (needle icon). */
    VACCINATION,

    /** Deworming treatment. */
    DEWORMING,

    /** Blood tests, lab work (flask icon). */
    LAB_WORK,

    /** USG examination. */
    ULTRASOUND,

    /** Heart/cardiology (heart icon). */
    CARDIOLOGY,

    /** Surgical procedure. */
    SURGERY,

    /** Dental procedure. */
    DENTAL,

    /** Grooming service. */
    GROOMING,

    /** Emergency visit. */
    EMERGENCY,

    /** Follow-up visit. */
    FOLLOW_UP,

    /** Regular checkup. */
    CHECKUP
}
