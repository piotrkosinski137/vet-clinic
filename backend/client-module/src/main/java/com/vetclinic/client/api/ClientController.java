package com.vetclinic.client.api;

import static com.vetclinic.common.security.Roles.CAN_MANAGE_CLIENTS;
import static com.vetclinic.common.security.Roles.HAS_ANY_ROLE;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vetclinic.client.api.dto.ClientRequest;
import com.vetclinic.client.api.dto.ClientResponse;
import com.vetclinic.client.domain.ClientSearchCriteria;
import com.vetclinic.client.domain.ClientService;
import com.vetclinic.client.domain.model.Client;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;
    private final ClientMapper clientMapper;

    @PostMapping
    @PreAuthorize(CAN_MANAGE_CLIENTS)
    public ResponseEntity<ClientResponse> createClient(@Valid @RequestBody ClientRequest request) {
        Client client = clientMapper.toEntity(request);
        Client created = clientService.createClient(client);
        ClientResponse response = clientMapper.toResponse(created);
        return ResponseEntity.created(URI.create("/api/v1/clients/" + created.getId()))
                .body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize(HAS_ANY_ROLE)
    public ResponseEntity<ClientResponse> getClient(@PathVariable UUID id) {
        Client client = clientService.getClient(id);
        return ResponseEntity.ok(clientMapper.toResponse(client));
    }

    /**
     * Get all clients with optional filtering.
     *
     * @param firstName Search by first name (partial, case-insensitive)
     * @param lastName Search by last name (partial, case-insensitive)
     * @param email Search by email (partial, case-insensitive)
     * @param phone Search by phone number (partial match)
     * @param city Search by city (partial, case-insensitive)
     */
    @GetMapping
    @PreAuthorize(HAS_ANY_ROLE)
    public ResponseEntity<List<ClientResponse>> getAllClients(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String city) {

        ClientSearchCriteria criteria =
                new ClientSearchCriteria(firstName, lastName, email, phone, city);

        List<Client> clients;
        if (criteria.hasAnyCriteria()) {
            clients = clientService.searchClients(criteria);
        } else {
            clients = clientService.getAllClients();
        }

        List<ClientResponse> responses = clients.stream().map(clientMapper::toResponse).toList();
        return ResponseEntity.ok(responses);
    }

    /** Search clients by email. */
    @GetMapping("/email/{email}")
    @PreAuthorize(HAS_ANY_ROLE)
    public ResponseEntity<ClientResponse> getByEmail(@PathVariable String email) {
        Client client = clientService.getClientByEmail(email);
        return ResponseEntity.ok(clientMapper.toResponse(client));
    }

    @PutMapping("/{id}")
    @PreAuthorize(CAN_MANAGE_CLIENTS)
    public ResponseEntity<ClientResponse> updateClient(
            @PathVariable UUID id, @Valid @RequestBody ClientRequest request) {
        Client client = clientMapper.toEntity(request);
        Client updated = clientService.updateClient(id, client);
        return ResponseEntity.ok(clientMapper.toResponse(updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(CAN_MANAGE_CLIENTS)
    public ResponseEntity<Void> deleteClient(@PathVariable UUID id) {
        clientService.deleteClient(id);
        return ResponseEntity.noContent().build();
    }
}
