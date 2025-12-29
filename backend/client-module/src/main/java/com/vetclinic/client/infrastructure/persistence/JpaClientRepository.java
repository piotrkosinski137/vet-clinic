package com.vetclinic.client.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.vetclinic.client.domain.model.Client;

public interface JpaClientRepository extends JpaRepository<Client, UUID> {

    Optional<Client> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Client> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            String firstName, String lastName);

    List<Client> findByPhoneContaining(String phone);

    List<Client> findByCityContainingIgnoreCase(String city);

    @Query(
            """
            SELECT c FROM Client c
            WHERE (:firstName IS NULL OR LOWER(c.firstName) LIKE LOWER(CONCAT('%', CAST(:firstName AS string), '%')))
            AND (:lastName IS NULL OR LOWER(c.lastName) LIKE LOWER(CONCAT('%', CAST(:lastName AS string), '%')))
            AND (:email IS NULL OR LOWER(c.email) LIKE LOWER(CONCAT('%', CAST(:email AS string), '%')))
            AND (:phone IS NULL OR c.phone LIKE CONCAT('%', CAST(:phone AS string), '%'))
            AND (:city IS NULL OR LOWER(c.city) LIKE LOWER(CONCAT('%', CAST(:city AS string), '%')))
            """)
    List<Client> search(
            @Param("firstName") String firstName,
            @Param("lastName") String lastName,
            @Param("email") String email,
            @Param("phone") String phone,
            @Param("city") String city);

    /**
     * Full-text search across client name, email, phone. Uses unaccent for diacritic-insensitive
     * search (e.g., "Wozniak" finds "Woźniak").
     */
    @Query(
            value =
                    """
            SELECT c.* FROM clients c
            WHERE unaccent(lower(c.first_name)) LIKE unaccent(lower(concat('%', :query, '%')))
               OR unaccent(lower(c.last_name)) LIKE unaccent(lower(concat('%', :query, '%')))
               OR unaccent(lower(concat(c.first_name, ' ', c.last_name))) LIKE unaccent(lower(concat('%', :query, '%')))
               OR unaccent(lower(COALESCE(c.email, ''))) LIKE unaccent(lower(concat('%', :query, '%')))
               OR COALESCE(c.phone, '') LIKE concat('%', :query, '%')
            ORDER BY c.last_name, c.first_name
            """,
            nativeQuery = true)
    List<Client> searchByQuery(@Param("query") String query);
}
