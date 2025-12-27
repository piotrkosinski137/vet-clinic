package com.vetclinic.client.domain;

/**
 * Search criteria for finding clients. All fields are optional - only non-null values will be used
 * in the search.
 */
public record ClientSearchCriteria(
        /** Search by first name (case-insensitive, partial match) */
        String firstName,
        /** Search by last name (case-insensitive, partial match) */
        String lastName,
        /** Search by email (case-insensitive, partial match) */
        String email,
        /** Search by phone number (partial match) */
        String phone,
        /** Search by city (case-insensitive, partial match) */
        String city) {

    /** Create empty search criteria (returns all clients) */
    public static ClientSearchCriteria empty() {
        return new ClientSearchCriteria(null, null, null, null, null);
    }

    /** Check if any search criteria is specified */
    public boolean hasAnyCriteria() {
        return firstName != null
                || lastName != null
                || email != null
                || phone != null
                || city != null;
    }

    /** Convenience: search by any name field (first or last) */
    public static ClientSearchCriteria byName(String name) {
        return new ClientSearchCriteria(name, name, null, null, null);
    }
}
