package com.vetclinic.patient.domain.model;

/**
 * Labels/tags that can be assigned to patients for quick identification and special handling
 * requirements.
 */
public enum PatientLabel {
    /** Patient may be aggressive and requires careful handling */
    AGGRESSIVE,

    /** Patient has known allergies - check medical records before treatment */
    ALLERGIC,

    /** VIP patient - requires priority attention */
    VIP,

    /** Patient has chronic conditions requiring ongoing treatment */
    CHRONIC,

    /** Senior patient (age 7+ for dogs/cats) - may need geriatric care */
    SENIOR,

    /** Patient requires special diet */
    SPECIAL_DIET,

    /** Patient is currently under ongoing treatment plan */
    UNDER_TREATMENT,

    /** Patient has history of escape attempts */
    FLIGHT_RISK
}
