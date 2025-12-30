package com.vetclinic.billing.domain.model;

public enum BatchStatus {
    PENDING, // LOT/expiration not yet assigned
    COMPLETE, // Ready for use
    DEPLETED // quantity = 0
}
