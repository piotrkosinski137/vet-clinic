package com.vetclinic.common.constants;

/** Application-wide constants to eliminate magic numbers and strings. */
public final class AppConstants {

    private AppConstants() {
        // Utility class - prevent instantiation
    }

    // Pagination
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;

    // Visit duration (minutes)
    public static final int DEFAULT_VISIT_DURATION_MINUTES = 30;
    public static final int MIN_VISIT_DURATION_MINUTES = 5;
    public static final int MAX_VISIT_DURATION_MINUTES = 480;

    // Animal temperature (Celsius) - normal range for most animals
    public static final double MIN_ANIMAL_TEMPERATURE = 30.0;
    public static final double MAX_ANIMAL_TEMPERATURE = 45.0;

    // Invoice
    public static final int MAX_TAX_RATE_PERCENT = 100;
    public static final int DEFAULT_TAX_RATE_PERCENT = 23; // Polish VAT

    // API paths
    public static final String API_V1_PREFIX = "/api/v1";

    // Error handling
    public static final String PROBLEM_JSON_TYPE = "application/problem+json";
    public static final String ERROR_BASE_URL = "https://api.vetclinic.com/errors/";

    // Working hours
    public static final int DEFAULT_WORK_START_HOUR = 8;
    public static final int DEFAULT_WORK_END_HOUR = 18;

    // Keycloak
    public static final String KEYCLOAK_CLINIC_ID_ATTRIBUTE = "clinic_id";

    // Inventory
    public static final int DEFAULT_REORDER_POINT = 5;
    public static final int INITIAL_STOCK_QUANTITY = 0;
    public static final int LOW_STOCK_THRESHOLD = 10;
}
