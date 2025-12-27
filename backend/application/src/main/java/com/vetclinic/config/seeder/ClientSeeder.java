package com.vetclinic.config.seeder;

import jakarta.persistence.EntityManager;

import org.springframework.stereotype.Component;

import com.vetclinic.client.domain.model.Client;

import lombok.extern.slf4j.Slf4j;

/** Seeds client entities. */
@Component
@Slf4j
public class ClientSeeder implements DataSeeder {

    private static final double NOTES_PROBABILITY = 0.3;

    private static final String[][] CLIENT_DATA = {
        {
            "Adam",
            "Malinowski",
            "adam.malinowski@email.pl",
            "+48 601 234 567",
            "ul. Kwiatowa 5",
            "Warszawa",
            "00-100"
        },
        {
            "Barbara",
            "Kaczmarek",
            "b.kaczmarek@gmail.com",
            "+48 602 345 678",
            "ul. Słoneczna 12",
            "Kraków",
            "30-001"
        },
        {
            "Cezary",
            "Wójcik",
            "cezary.wojcik@wp.pl",
            "+48 603 456 789",
            "ul. Leśna 8",
            "Gdańsk",
            "80-001"
        },
        {
            "Dorota",
            "Kamińska",
            "dorota.k@onet.pl",
            "+48 604 567 890",
            "ul. Morska 23",
            "Sopot",
            "81-701"
        },
        {
            "Edward",
            "Szymański",
            "e.szymanski@firma.pl",
            "+48 605 678 901",
            "ul. Górska 45",
            "Zakopane",
            "34-500"
        },
        {
            "Franciszka",
            "Woźniak",
            "fwoźniak@email.com",
            "+48 606 789 012",
            "ul. Polna 67",
            "Poznań",
            "60-001"
        },
        {
            "Grzegorz",
            "Dąbrowski",
            "g.dabrowski@mail.pl",
            "+48 607 890 123",
            "ul. Łąkowa 89",
            "Wrocław",
            "50-001"
        },
        {
            "Helena",
            "Kozłowska",
            "helena.kozl@interia.pl",
            "+48 608 901 234",
            "ul. Rzeczna 12",
            "Łódź",
            "90-001"
        },
        {
            "Igor",
            "Jankowski",
            "igor.jan@gmail.com",
            "+48 609 012 345",
            "ul. Parkowa 34",
            "Katowice",
            "40-001"
        },
        {
            "Joanna",
            "Mazur",
            "j.mazur@outlook.com",
            "+48 610 123 456",
            "ul. Ogrodowa 56",
            "Lublin",
            "20-001"
        },
        {
            "Krzysztof",
            "Krawczyk",
            "k.krawczyk@email.pl",
            "+48 611 234 567",
            "ul. Zielona 78",
            "Szczecin",
            "70-001"
        },
        {
            "Lucyna",
            "Piotrowska",
            "lucyna.p@wp.pl",
            "+48 612 345 678",
            "ul. Wesoła 90",
            "Bydgoszcz",
            "85-001"
        },
        {
            "Marek",
            "Grabowski",
            "m.grabowski@firma.com",
            "+48 613 456 789",
            "ul. Cicha 11",
            "Białystok",
            "15-001"
        },
        {
            "Natalia",
            "Pawlak",
            "n.pawlak@gmail.com",
            "+48 614 567 890",
            "ul. Spokojna 22",
            "Gdynia",
            "81-001"
        },
        {
            "Olga",
            "Michalska",
            "olga.m@onet.pl",
            "+48 615 678 901",
            "ul. Radosna 33",
            "Częstochowa",
            "42-200"
        },
        {
            "Paweł",
            "Zając",
            "pawel.zajac@mail.com",
            "+48 616 789 012",
            "ul. Słowicza 44",
            "Radom",
            "26-600"
        },
        {
            "Renata",
            "Król",
            "r.krol@interia.pl",
            "+48 617 890 123",
            "ul. Jaskółcza 55",
            "Toruń",
            "87-100"
        },
        {
            "Stefan",
            "Wieczorek",
            "s.wieczorek@email.pl",
            "+48 618 901 234",
            "ul. Ptasia 66",
            "Kielce",
            "25-001"
        }
    };

    @Override
    public int getOrder() {
        return 3;
    }

    @Override
    public void seed(EntityManager entityManager, SeedContext context) {
        for (var data : CLIENT_DATA) {
            var client = createClient(data, context);
            entityManager.persist(client);
            context.addClient(client);
        }
        log.info("Created {} clients", context.getClientIds().size());
    }

    private Client createClient(String[] data, SeedContext context) {
        var notes =
                context.getRandom().nextDouble() > (1 - NOTES_PROBABILITY)
                        ? "Regular customer, always on time"
                        : null;
        var client =
                Client.builder()
                        .firstName(data[0])
                        .lastName(data[1])
                        .email(data[2])
                        .phone(data[3])
                        .address(data[4])
                        .city(data[5])
                        .postalCode(data[6])
                        .notes(notes)
                        .build();
        client.setClinicId(context.getClinicId());
        return client;
    }
}
