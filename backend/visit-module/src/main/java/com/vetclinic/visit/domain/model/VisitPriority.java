package com.vetclinic.visit.domain.model;

/**
 * Priority level for visits in the waiting room. Used to determine the order in which patients
 * should be seen.
 */
public enum VisitPriority {
    /** Low priority - can wait longer */
    LOW,
    /** Normal priority - standard waiting order */
    NORMAL,
    /** High priority - should be seen soon */
    HIGH,
    /** Urgent priority - emergency cases, seen immediately */
    URGENT
}
