package com.vetclinic.client.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vetclinic.client.domain.model.Client;

interface JpaClientRepository extends JpaRepository<Client, UUID> {

    Optional<Client> findByEmail(String email);

    boolean existsByEmail(String email);
}
