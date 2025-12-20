package com.vetclinic.client.infrastructure.persistence;

import com.vetclinic.client.domain.model.Client;
import com.vetclinic.client.domain.port.ClientRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
class ClientRepositoryAdapter implements ClientRepository {

    private final JpaClientRepository jpaRepository;

    @Override
    public Client save(Client client) {
        return jpaRepository.save(client);
    }

    @Override
    public Optional<Client> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<Client> findByEmail(String email) {
        return jpaRepository.findByEmail(email);
    }

    @Override
    public List<Client> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }
}
