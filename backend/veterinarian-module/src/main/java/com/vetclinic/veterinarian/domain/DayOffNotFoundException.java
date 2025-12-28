package com.vetclinic.veterinarian.domain;

import java.util.UUID;

/** Exception thrown when a day off record is not found. */
public class DayOffNotFoundException extends RuntimeException {

    public DayOffNotFoundException(UUID id) {
        super("Day off not found with id: " + id);
    }
}
