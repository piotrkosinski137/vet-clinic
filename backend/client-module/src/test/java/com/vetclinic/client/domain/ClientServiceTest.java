package com.vetclinic.client.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vetclinic.client.domain.model.Client;
import com.vetclinic.client.domain.port.ClientRepository;
import com.vetclinic.common.event.DomainEventPublisher;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock private ClientRepository clientRepository;
    @Mock private DomainEventPublisher eventPublisher;

    private ClientService clientService;

    @BeforeEach
    void setUp() {
        clientService = new ClientService(clientRepository, eventPublisher);
    }

    @Nested
    class CreateClient {
        @Test
        void shouldCreateClient() {
            Client client = aClient("John", "Doe", "john@example.com");
            given(clientRepository.existsByEmail("john@example.com")).willReturn(false);
            given(clientRepository.save(any(Client.class))).willReturn(client);

            Client result = clientService.createClient(client);

            assertThat(result.getFirstName()).isEqualTo("John");
            verify(clientRepository).save(client);
        }

        @Test
        void shouldThrowWhenEmailExists() {
            Client client = aClient("John", "Doe", "john@example.com");
            given(clientRepository.existsByEmail("john@example.com")).willReturn(true);

            assertThatThrownBy(() -> clientService.createClient(client))
                    .isInstanceOf(EmailAlreadyExistsException.class);
        }
    }

    @Nested
    class GetClient {
        @Test
        void shouldGetClientById() {
            UUID id = UUID.randomUUID();
            Client client = aClient("John", "Doe", "john@example.com");
            given(clientRepository.findById(id)).willReturn(Optional.of(client));

            Client result = clientService.getClient(id);

            assertThat(result.getFirstName()).isEqualTo("John");
        }

        @Test
        void shouldThrowWhenClientNotFound() {
            UUID id = UUID.randomUUID();
            given(clientRepository.findById(id)).willReturn(Optional.empty());

            assertThatThrownBy(() -> clientService.getClient(id))
                    .isInstanceOf(ClientNotFoundException.class)
                    .hasMessageContaining(id.toString());
        }
    }

    @Nested
    class GetAllClients {
        @Test
        void shouldGetAllClients() {
            List<Client> clients =
                    List.of(
                            aClient("John", "Doe", "john@example.com"),
                            aClient("Jane", "Smith", "jane@example.com"));
            given(clientRepository.findAll()).willReturn(clients);

            List<Client> result = clientService.getAllClients();

            assertThat(result).hasSize(2);
        }
    }

    @Nested
    class DeleteClient {
        @Test
        void shouldDeleteClient() {
            UUID id = UUID.randomUUID();
            Client client = aClient("John", "Doe", "john@example.com");
            given(clientRepository.findById(id)).willReturn(Optional.of(client));

            clientService.deleteClient(id);

            verify(clientRepository).deleteById(id);
        }

        @Test
        void shouldThrowWhenDeletingNonExistentClient() {
            UUID id = UUID.randomUUID();
            given(clientRepository.findById(id)).willReturn(Optional.empty());

            assertThatThrownBy(() -> clientService.deleteClient(id))
                    .isInstanceOf(ClientNotFoundException.class);
        }
    }

    @Nested
    class SearchByName {
        @Test
        void shouldSearchClientsByName() {
            List<Client> clients =
                    List.of(
                            aClient("John", "Doe", "john@example.com"),
                            aClient("Johnny", "Smith", "johnny@example.com"));
            given(
                            clientRepository
                                    .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                                            "John", "John"))
                    .willReturn(clients);

            List<Client> result = clientService.searchByName("John");

            assertThat(result).hasSize(2);
        }
    }

    @Nested
    class SearchByPhone {
        @Test
        void shouldSearchClientsByPhone() {
            List<Client> clients = List.of(aClient("John", "Doe", "john@example.com"));
            given(clientRepository.findByPhoneContaining("123")).willReturn(clients);

            List<Client> result = clientService.searchByPhone("123");

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    class SearchByCity {
        @Test
        void shouldSearchClientsByCity() {
            List<Client> clients = List.of(aClient("John", "Doe", "john@example.com"));
            given(clientRepository.findByCityContainingIgnoreCase("Warsaw")).willReturn(clients);

            List<Client> result = clientService.searchByCity("Warsaw");

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    class GetClientByEmail {
        @Test
        void shouldGetClientByEmail() {
            Client client = aClient("John", "Doe", "john@example.com");
            given(clientRepository.findByEmail("john@example.com")).willReturn(Optional.of(client));

            Client result = clientService.getClientByEmail("john@example.com");

            assertThat(result.getEmail()).isEqualTo("john@example.com");
        }

        @Test
        void shouldThrowWhenEmailNotFound() {
            given(clientRepository.findByEmail("notfound@example.com"))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> clientService.getClientByEmail("notfound@example.com"))
                    .isInstanceOf(ClientNotFoundException.class)
                    .hasMessageContaining("email");
        }
    }

    @Nested
    class SearchClients {
        @Test
        void shouldSearchWithCriteria() {
            ClientSearchCriteria criteria =
                    new ClientSearchCriteria("John", null, null, null, null);
            List<Client> clients = List.of(aClient("John", "Doe", "john@example.com"));
            given(clientRepository.search("John", null, null, null, null)).willReturn(clients);

            List<Client> result = clientService.searchClients(criteria);

            assertThat(result).hasSize(1);
        }

        @Test
        void shouldReturnAllWhenNoCriteria() {
            ClientSearchCriteria criteria = new ClientSearchCriteria(null, null, null, null, null);
            List<Client> clients =
                    List.of(
                            aClient("John", "Doe", "john@example.com"),
                            aClient("Jane", "Smith", "jane@example.com"));
            given(clientRepository.findAll()).willReturn(clients);

            List<Client> result = clientService.searchClients(criteria);

            assertThat(result).hasSize(2);
        }
    }

    private static Client aClient(String firstName, String lastName, String email) {
        return Client.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .phone("+1234567890")
                .city("Warsaw")
                .build();
    }
}
