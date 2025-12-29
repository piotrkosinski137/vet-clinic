package com.vetclinic.visit.domain.model;

/**
 * Status of a veterinary visit.
 *
 * <p>Flow: SCHEDULED → CHECKED_IN → IN_PROGRESS → COMPLETED
 *
 * <p>Alternative paths:
 *
 * <ul>
 *   <li>SCHEDULED → CANCELLED (cancelled before arrival)
 *   <li>SCHEDULED → NO_SHOW (patient didn't show up)
 *   <li>CHECKED_IN → NO_SHOW (patient left before being seen)
 *   <li>CHECKED_IN → CANCELLED (cancelled while waiting)
 * </ul>
 */
public enum VisitStatus {
    /** Visit is scheduled but patient has not arrived yet */
    SCHEDULED,
    /** Patient has checked in and is waiting in the waiting room */
    CHECKED_IN,
    /** Visit is currently in progress with the veterinarian */
    IN_PROGRESS,
    /** Visit has been completed */
    COMPLETED,
    /** Visit was cancelled */
    CANCELLED,
    /** Patient did not show up for the appointment */
    NO_SHOW
}
