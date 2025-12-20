package com.vetclinic.client.domain;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vetclinic.client.domain.model.Client;
import com.vetclinic.client.domain.port.ClientRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClientService {

    private final ClientRepository clientRepository;

    @Transactional
    public Client createClient(Client client) {
        if (clientRepository.existsByEmail(client.getEmail())) {
            throw new EmailAlreadyExistsException(client.getEmail());
        }
        return clientRepository.save(client);
    }

    public Client getClient(UUID id) {
        return clientRepository.findById(id).orElseThrow(() -> new ClientNotFoundException(id));
    }

    public List<Client> getAllClients() {
        return clientRepository.findAll();
    }

    @Transactional
    public Client updateClient(UUID id, Client updated) {
        Client existing = getClient(id);
        existing.setFirstName(updated.getFirstName());
        existing.setLastName(updated.getLastName());
        existing.setPhone(updated.getPhone());
        existing.setAddress(updated.getAddress());
        existing.setCity(updated.getCity());
        existing.setPostalCode(updated.getPostalCode());
        existing.setNotes(updated.getNotes());
        return clientRepository.save(existing);
    }

    @Transactional
    public void deleteClient(UUID id) {
        if (!clientRepository.existsById(id)) {
            throw new ClientNotFoundException(id);
        }
        clientRepository.deleteById(id);
    }
}
