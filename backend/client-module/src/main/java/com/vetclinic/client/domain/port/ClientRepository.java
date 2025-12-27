package com.vetclinic.client.domain.port;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.vetclinic.client.domain.model.Client;

/** Port for client persistence operations. */
public interface ClientRepository {

    Client save(Client client);

    Optional<Client> findById(UUID id);

    Optional<Client> findByEmail(String email);

    List<Client> findAll();

    void deleteById(UUID id);

    boolean existsById(UUID id);

    boolean existsByEmail(String email);

    // Search methods

    /** Search by name (first or last, case-insensitive partial match) */
    List<Client> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            String firstName, String lastName);

    /** Search by phone number (partial match) */
    List<Client> findByPhoneContaining(String phone);

    /** Search by city (case-insensitive partial match) */
    List<Client> findByCityContainingIgnoreCase(String city);

    /** Search clients by multiple criteria */
    List<Client> search(String firstName, String lastName, String email, String phone, String city);
}
