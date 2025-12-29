package com.vetclinic.client.domain;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vetclinic.client.domain.model.Client;
import com.vetclinic.client.domain.port.ClientRepository;
import com.vetclinic.common.event.DomainEventPublisher;
import com.vetclinic.common.util.ChangeDetector;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClientService {

    private static final String ENTITY_TYPE = "Client";

    private final ClientRepository clientRepository;
    private final DomainEventPublisher eventPublisher;

    @Transactional
    public Client createClient(Client client) {
        if (clientRepository.existsByEmail(client.getEmail())) {
            throw new EmailAlreadyExistsException(client.getEmail());
        }
        var saved = clientRepository.save(client);
        eventPublisher.publishCreated(ENTITY_TYPE, saved.getId(), ClientSnapshot.from(saved));
        return saved;
    }

    public Client getClient(UUID id) {
        return clientRepository.findById(id).orElseThrow(() -> new ClientNotFoundException(id));
    }

    public List<Client> getAllClients() {
        return clientRepository.findAll();
    }

    public long countClients() {
        return clientRepository.count();
    }

    @Transactional
    public Client updateClient(UUID id, Client updated) {
        var existing = getClient(id);
        var oldSnapshot = ClientSnapshot.from(existing);

        var changedFields =
                ChangeDetector.comparing(existing, updated)
                        .check("firstName", Client::getFirstName)
                        .check("lastName", Client::getLastName)
                        .check("phone", Client::getPhone)
                        .check("address", Client::getAddress)
                        .check("city", Client::getCity)
                        .check("postalCode", Client::getPostalCode)
                        .check("notes", Client::getNotes)
                        .getChangedFields();

        applyClientUpdates(existing, updated);

        var saved = clientRepository.save(existing);
        if (!changedFields.isEmpty()) {
            eventPublisher.publishUpdated(
                    ENTITY_TYPE, id, oldSnapshot, ClientSnapshot.from(saved), changedFields);
        }
        return saved;
    }

    private void applyClientUpdates(Client existing, Client updated) {
        existing.setFirstName(updated.getFirstName());
        existing.setLastName(updated.getLastName());
        existing.setPhone(updated.getPhone());
        existing.setAddress(updated.getAddress());
        existing.setCity(updated.getCity());
        existing.setPostalCode(updated.getPostalCode());
        existing.setNotes(updated.getNotes());
    }

    @Transactional
    public void deleteClient(UUID id) {
        var client =
                clientRepository.findById(id).orElseThrow(() -> new ClientNotFoundException(id));
        clientRepository.deleteById(id);
        eventPublisher.publishDeleted(ENTITY_TYPE, id, ClientSnapshot.from(client));
    }

    // Search methods

    /** Search clients by name (first or last name, case-insensitive partial match) */
    public List<Client> searchByName(String name) {
        return clientRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                name, name);
    }

    /** Search clients by phone number (partial match) */
    public List<Client> searchByPhone(String phone) {
        return clientRepository.findByPhoneContaining(phone);
    }

    /** Search clients by city (case-insensitive partial match) */
    public List<Client> searchByCity(String city) {
        return clientRepository.findByCityContainingIgnoreCase(city);
    }

    /** Get client by email */
    public Client getClientByEmail(String email) {
        return clientRepository
                .findByEmail(email)
                .orElseThrow(() -> new ClientNotFoundException("email", email));
    }

    /** Search clients by multiple criteria */
    public List<Client> searchClients(ClientSearchCriteria criteria) {
        if (!criteria.hasAnyCriteria()) {
            return clientRepository.findAll();
        }
        return clientRepository.search(
                criteria.firstName(),
                criteria.lastName(),
                criteria.email(),
                criteria.phone(),
                criteria.city());
    }

    /**
     * Full-text search across client name, email, phone. Uses unaccent for diacritic-insensitive
     * search (e.g., "Wozniak" finds "Woźniak").
     */
    public List<Client> searchByQuery(String query) {
        if (query == null || query.isBlank()) {
            return clientRepository.findAll();
        }
        return clientRepository.searchByQuery(query.trim());
    }
}
