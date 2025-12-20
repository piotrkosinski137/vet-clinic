package com.vetclinic.fixtures;

import java.util.concurrent.atomic.AtomicInteger;

import com.vetclinic.client.domain.model.Client;

/**
 * Test fixture builder for Client entities. Provides sensible defaults and fluent API for creating
 * test data.
 */
public final class ClientFixture {

    private static final AtomicInteger EMAIL_COUNTER = new AtomicInteger(0);

    private String firstName = "John";
    private String lastName = "Doe";
    private String email;
    private String phone = "+1234567890";
    private String address = "123 Main St";
    private String city = "Springfield";
    private String postalCode = "12345";
    private String notes = null;

    private ClientFixture() {
        // Generate unique email by default to avoid constraint violations
        this.email = "john.doe" + EMAIL_COUNTER.incrementAndGet() + "@example.com";
    }

    public static ClientFixture aClient() {
        return new ClientFixture();
    }

    public static ClientFixture aClientWithPets() {
        return new ClientFixture().withNotes("Has multiple pets");
    }

    public ClientFixture withFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public ClientFixture withLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public ClientFixture withFullName(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
        return this;
    }

    public ClientFixture withEmail(String email) {
        this.email = email;
        return this;
    }

    public ClientFixture withPhone(String phone) {
        this.phone = phone;
        return this;
    }

    public ClientFixture withAddress(String address) {
        this.address = address;
        return this;
    }

    public ClientFixture withCity(String city) {
        this.city = city;
        return this;
    }

    public ClientFixture withPostalCode(String postalCode) {
        this.postalCode = postalCode;
        return this;
    }

    public ClientFixture withFullAddress(String address, String city, String postalCode) {
        this.address = address;
        this.city = city;
        this.postalCode = postalCode;
        return this;
    }

    public ClientFixture withNotes(String notes) {
        this.notes = notes;
        return this;
    }

    public Client build() {
        return Client.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .phone(phone)
                .address(address)
                .city(city)
                .postalCode(postalCode)
                .notes(notes)
                .build();
    }

    /** Returns JSON representation for API testing. */
    public String toJson() {
        StringBuilder json = new StringBuilder("{");
        json.append("\"firstName\":\"").append(firstName).append("\"");
        json.append(",\"lastName\":\"").append(lastName).append("\"");
        json.append(",\"email\":\"").append(email).append("\"");

        if (phone != null) {
            json.append(",\"phone\":\"").append(phone).append("\"");
        }
        if (address != null) {
            json.append(",\"address\":\"").append(address).append("\"");
        }
        if (city != null) {
            json.append(",\"city\":\"").append(city).append("\"");
        }
        if (postalCode != null) {
            json.append(",\"postalCode\":\"").append(postalCode).append("\"");
        }
        if (notes != null) {
            json.append(",\"notes\":\"").append(notes).append("\"");
        }

        json.append("}");
        return json.toString();
    }
}
