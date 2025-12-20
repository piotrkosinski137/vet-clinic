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
}
