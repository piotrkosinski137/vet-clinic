package com.vetclinic.client.api.dto;

import java.time.Instant;
import java.util.UUID;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ClientResponse {

    UUID id;
    String firstName;
    String lastName;
    String email;
    String phone;
    String address;
    String city;
    String postalCode;
    String notes;
    Instant createdAt;
    Instant updatedAt;
}
