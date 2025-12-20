package com.vetclinic.client.api;

import com.vetclinic.client.api.dto.ClientRequest;
import com.vetclinic.client.api.dto.ClientResponse;
import com.vetclinic.client.domain.ClientService;
import com.vetclinic.client.domain.model.Client;

import jakarta.validation.Valid;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;
    private final ClientMapper clientMapper;

    @PostMapping
    public ResponseEntity<ClientResponse> createClient(
            @Valid @RequestBody ClientRequest request) {
        Client client = clientMapper.toEntity(request);
        Client created = clientService.createClient(client);
        ClientResponse response = clientMapper.toResponse(created);
        return ResponseEntity
                .created(URI.create("/api/v1/clients/" + created.getId()))
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientResponse> getClient(@PathVariable UUID id) {
        Client client = clientService.getClient(id);
        return ResponseEntity.ok(clientMapper.toResponse(client));
    }

    @GetMapping
    public ResponseEntity<List<ClientResponse>> getAllClients() {
        List<ClientResponse> responses = clientService.getAllClients().stream()
                .map(clientMapper::toResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClientResponse> updateClient(
            @PathVariable UUID id,
            @Valid @RequestBody ClientRequest request) {
        Client client = clientMapper.toEntity(request);
        Client updated = clientService.updateClient(id, client);
        return ResponseEntity.ok(clientMapper.toResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable UUID id) {
        clientService.deleteClient(id);
        return ResponseEntity.noContent().build();
    }
}
