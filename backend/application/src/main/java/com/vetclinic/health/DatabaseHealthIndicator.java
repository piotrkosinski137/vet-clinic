package com.vetclinic.health;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/** Custom health indicator that checks database connectivity and basic query execution. */
@Component
public class DatabaseHealthIndicator implements HealthIndicator {

    private final DataSource dataSource;

    public DatabaseHealthIndicator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(2)) {
                // Execute a simple query to verify database is operational
                try (Statement statement = connection.createStatement();
                        ResultSet resultSet = statement.executeQuery("SELECT 1")) {
                    if (resultSet.next()) {
                        String databaseProduct = connection.getMetaData().getDatabaseProductName();
                        String databaseVersion =
                                connection.getMetaData().getDatabaseProductVersion();

                        return Health.up()
                                .withDetail("database", databaseProduct)
                                .withDetail("version", databaseVersion)
                                .withDetail("validationQuery", "SELECT 1")
                                .build();
                    }
                }
            }
            return Health.down().withDetail("reason", "Database connection is not valid").build();
        } catch (SQLException e) {
            return Health.down()
                    .withDetail("error", e.getClass().getName())
                    .withDetail("message", e.getMessage())
                    .build();
        }
    }
}
