package com.vetclinic.client.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Public API for the Client module.
 *
 * <p>This interface defines the methods that other modules can use to interact with the Client
 * module. It uses simple DTOs instead of entity types to maintain loose coupling between modules.
 *
 * <p>Usage example:
 *
 * <pre>{@code
 * @Autowired
 * private ClientModuleApi clientModule;
 *
 * ClientBasicInfo client = clientModule.getClientBasicInfo(clientId);
 * }</pre>
 */
public interface ClientModuleApi {

    /**
     * Get basic client information by ID.
     *
     * @param clientId the client ID
     * @return basic client information
     * @throws com.vetclinic.client.domain.ClientNotFoundException if client not found
     */
    ClientBasicInfo getClientBasicInfo(UUID clientId);

    /**
     * Check if a client exists by ID.
     *
     * @param clientId the client ID
     * @return true if client exists
     */
    boolean existsById(UUID clientId);

    /**
     * Find client by email.
     *
     * @param email the email address
     * @return optional containing client info if found
     */
    Optional<ClientBasicInfo> findByEmail(String email);

    /**
     * Get total count of clients.
     *
     * @return client count
     */
    long countClients();

    /**
     * Search clients by name query.
     *
     * @param query search query (searches first name and last name)
     * @return list of matching clients
     */
    List<ClientBasicInfo> searchByName(String query);

    /** Basic client information DTO for inter-module communication. */
    record ClientBasicInfo(UUID id, String firstName, String lastName, String email, String phone) {

        public String getFullName() {
            return firstName + " " + lastName;
        }
    }
}
