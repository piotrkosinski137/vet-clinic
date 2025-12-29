package com.vetclinic.client.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.vetclinic.client.domain.model.Client;
import com.vetclinic.client.domain.port.ClientRepository;

import lombok.RequiredArgsConstructor;

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

    @Override
    public long count() {
        return jpaRepository.count();
    }

    @Override
    public List<Client> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            String firstName, String lastName) {
        return jpaRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                firstName, lastName);
    }

    @Override
    public List<Client> findByPhoneContaining(String phone) {
        return jpaRepository.findByPhoneContaining(phone);
    }

    @Override
    public List<Client> findByCityContainingIgnoreCase(String city) {
        return jpaRepository.findByCityContainingIgnoreCase(city);
    }

    @Override
    public List<Client> search(
            String firstName, String lastName, String email, String phone, String city) {
        return jpaRepository.search(firstName, lastName, email, phone, city);
    }

    @Override
    public List<Client> searchByQuery(String query) {
        return jpaRepository.searchByQuery(query);
    }
}
