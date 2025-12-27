package com.vetclinic.config.seeder;

import jakarta.persistence.EntityManager;

/**
 * Interface for modular data seeders. Each seeder is responsible for initializing a specific type
 * of entity.
 */
public interface DataSeeder {

    /** Returns the order in which this seeder should run. Lower values run first. */
    int getOrder();

    /** Seeds data using the provided entity manager and context. */
    void seed(EntityManager entityManager, SeedContext context);
}
