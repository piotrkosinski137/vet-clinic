package com.vetclinic.client.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.vetclinic.client.domain.model.Client;
import com.vetclinic.client.domain.port.ClientRepository;

import lombok.RequiredArgsConstructor;

/**
 * Implementation of the Client module public API.
 *
 * <p>This class wraps the internal ClientService and ClientRepository to provide a clean,
 * module-level API for other modules to use.
 */
@Component
@RequiredArgsConstructor
public class ClientModuleApiImpl implements ClientModuleApi {

    private final ClientService clientService;
    private final ClientRepository clientRepository;

    @Override
    public ClientBasicInfo getClientBasicInfo(UUID clientId) {
        var client = clientService.getClient(clientId);
        return toBasicInfo(client);
    }

    @Override
    public boolean existsById(UUID clientId) {
        return clientRepository.existsById(clientId);
    }

    @Override
    public Optional<ClientBasicInfo> findByEmail(String email) {
        return clientRepository.findByEmail(email).map(this::toBasicInfo);
    }

    @Override
    public long countClients() {
        return clientService.countClients();
    }

    @Override
    public List<ClientBasicInfo> searchByName(String query) {
        return clientService.searchByName(query).stream().map(this::toBasicInfo).toList();
    }

    private ClientBasicInfo toBasicInfo(Client client) {
        return new ClientBasicInfo(
                client.getId(),
                client.getFirstName(),
                client.getLastName(),
                client.getEmail(),
                client.getPhone());
    }
}
