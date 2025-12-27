package com.vetclinic.config.seeder;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import com.vetclinic.billing.domain.model.PriceListItem;
import com.vetclinic.client.domain.model.Client;
import com.vetclinic.patient.domain.model.Patient;
import com.vetclinic.visit.domain.model.Visit;

import lombok.Getter;
import lombok.Setter;

/**
 * Shared context for data initialization. Holds references between entities created by different
 * initializers.
 */
@Getter
public class SeedContext {

    private static final UUID DEFAULT_CLINIC_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000001");

    private static final int RANDOM_SEED = 42;

    @Setter private UUID clinicId = DEFAULT_CLINIC_ID;

    private final Random random = new Random(RANDOM_SEED);

    private final List<UUID> clientIds = new ArrayList<>();
    private final List<Client> clients = new ArrayList<>();
    private final List<UUID> patientIds = new ArrayList<>();
    private final List<Patient> patients = new ArrayList<>();
    private final List<UUID> veterinarianIds = new ArrayList<>();
    private final List<String> veterinarianNames = new ArrayList<>();
    private final List<Visit> completedVisits = new ArrayList<>();
    private final List<PriceListItem> priceListItems = new ArrayList<>();

    public void addClient(Client client) {
        clients.add(client);
        clientIds.add(client.getId());
    }

    public void addPatient(Patient patient) {
        patients.add(patient);
        patientIds.add(patient.getId());
    }

    public void addVeterinarian(UUID id, String fullName) {
        veterinarianIds.add(id);
        veterinarianNames.add(fullName);
    }

    public void addPriceListItem(PriceListItem item) {
        priceListItems.add(item);
    }

    public void addCompletedVisit(Visit visit) {
        completedVisits.add(visit);
    }

    public UUID getDefaultClinicId() {
        return DEFAULT_CLINIC_ID;
    }
}
