package com.vetclinic;

import static com.vetclinic.fixtures.ClientFixture.aClient;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Integration tests for Client Debt API endpoints. Tests the aggregated debt calculation for
 * clients based on their invoices.
 */
@Transactional
class ClientDebtApiIntegrationTest extends AbstractIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Test
    void shouldReturnZeroDebtForClientWithNoInvoices() throws Exception {
        // Create a client
        String clientJson = aClient().withFirstName("NoDebt").withLastName("Client").toJson();

        MvcResult createResult =
                mockMvc.perform(
                                post("/api/v1/clients")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(clientJson))
                        .andExpect(status().isCreated())
                        .andReturn();

        JsonNode responseJson =
                objectMapper.readTree(createResult.getResponse().getContentAsString());
        String clientId = responseJson.get("id").asText();

        // Get debt for client
        mockMvc.perform(get("/api/v1/invoices/client/{clientId}/debt", clientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientId").value(clientId))
                .andExpect(jsonPath("$.totalOutstanding").value(0))
                .andExpect(jsonPath("$.totalInvoiced").value(0))
                .andExpect(jsonPath("$.totalPaid").value(0))
                .andExpect(jsonPath("$.unpaidInvoiceCount").value(0))
                .andExpect(jsonPath("$.overdueInvoiceCount").value(0));
    }

    @Test
    void shouldCalculateDebtFromUnpaidInvoices() throws Exception {
        // Create a client
        String clientJson = aClient().withFirstName("HasDebt").withLastName("Client").toJson();

        MvcResult clientResult =
                mockMvc.perform(
                                post("/api/v1/clients")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(clientJson))
                        .andExpect(status().isCreated())
                        .andReturn();

        JsonNode clientResponseJson =
                objectMapper.readTree(clientResult.getResponse().getContentAsString());
        String clientId = clientResponseJson.get("id").asText();

        // Create an invoice for this client
        String invoiceJson =
                String.format(
                        """
                {
                    "clientId": "%s",
                    "items": [
                        {
                            "name": "Consultation",
                            "quantity": 1,
                            "unitPrice": 100.00
                        }
                    ],
                    "taxRate": 0
                }
                """,
                        clientId);

        MvcResult invoiceResult =
                mockMvc.perform(
                                post("/api/v1/invoices")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(invoiceJson))
                        .andExpect(status().isCreated())
                        .andReturn();

        JsonNode invoiceResponseJson =
                objectMapper.readTree(invoiceResult.getResponse().getContentAsString());
        String invoiceId = invoiceResponseJson.get("id").asText();

        // Issue the invoice
        mockMvc.perform(post("/api/v1/invoices/{id}/issue", invoiceId)).andExpect(status().isOk());

        // Get debt for client
        mockMvc.perform(get("/api/v1/invoices/client/{clientId}/debt", clientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientId").value(clientId))
                .andExpect(jsonPath("$.totalOutstanding").value(100.00))
                .andExpect(jsonPath("$.totalInvoiced").value(100.00))
                .andExpect(jsonPath("$.totalPaid").value(0))
                .andExpect(jsonPath("$.unpaidInvoiceCount").value(1));
    }

    @Test
    void shouldReduceDebtAfterPartialPayment() throws Exception {
        // Create a client
        String clientJson =
                aClient().withFirstName("PartialPayment").withLastName("Client").toJson();

        MvcResult clientResult =
                mockMvc.perform(
                                post("/api/v1/clients")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(clientJson))
                        .andExpect(status().isCreated())
                        .andReturn();

        JsonNode clientResponseJson =
                objectMapper.readTree(clientResult.getResponse().getContentAsString());
        String clientId = clientResponseJson.get("id").asText();

        // Create an invoice
        String invoiceJson =
                String.format(
                        """
                {
                    "clientId": "%s",
                    "items": [
                        {
                            "name": "Treatment",
                            "quantity": 1,
                            "unitPrice": 200.00
                        }
                    ],
                    "taxRate": 0
                }
                """,
                        clientId);

        MvcResult invoiceResult =
                mockMvc.perform(
                                post("/api/v1/invoices")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(invoiceJson))
                        .andExpect(status().isCreated())
                        .andReturn();

        JsonNode invoiceResponseJson =
                objectMapper.readTree(invoiceResult.getResponse().getContentAsString());
        String invoiceId = invoiceResponseJson.get("id").asText();

        // Issue the invoice
        mockMvc.perform(post("/api/v1/invoices/{id}/issue", invoiceId)).andExpect(status().isOk());

        // Make a partial payment
        String paymentJson =
                """
                {
                    "amount": 75.00,
                    "paymentMethod": "CASH",
                    "notes": "Partial payment"
                }
                """;

        mockMvc.perform(
                        post("/api/v1/invoices/{id}/payments", invoiceId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(paymentJson))
                .andExpect(status().isCreated());

        // Get debt for client - should show remaining amount
        mockMvc.perform(get("/api/v1/invoices/client/{clientId}/debt", clientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientId").value(clientId))
                .andExpect(jsonPath("$.totalOutstanding").value(125.00))
                .andExpect(jsonPath("$.totalInvoiced").value(200.00))
                .andExpect(jsonPath("$.totalPaid").value(75.00))
                .andExpect(jsonPath("$.unpaidInvoiceCount").value(1));
    }

    @Test
    void shouldShowZeroDebtAfterFullPayment() throws Exception {
        // Create a client
        String clientJson = aClient().withFirstName("FullPayment").withLastName("Client").toJson();

        MvcResult clientResult =
                mockMvc.perform(
                                post("/api/v1/clients")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(clientJson))
                        .andExpect(status().isCreated())
                        .andReturn();

        JsonNode clientResponseJson =
                objectMapper.readTree(clientResult.getResponse().getContentAsString());
        String clientId = clientResponseJson.get("id").asText();

        // Create an invoice
        String invoiceJson =
                String.format(
                        """
                {
                    "clientId": "%s",
                    "items": [
                        {
                            "name": "Checkup",
                            "quantity": 1,
                            "unitPrice": 50.00
                        }
                    ],
                    "taxRate": 0
                }
                """,
                        clientId);

        MvcResult invoiceResult =
                mockMvc.perform(
                                post("/api/v1/invoices")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(invoiceJson))
                        .andExpect(status().isCreated())
                        .andReturn();

        JsonNode invoiceResponseJson =
                objectMapper.readTree(invoiceResult.getResponse().getContentAsString());
        String invoiceId = invoiceResponseJson.get("id").asText();

        // Issue the invoice
        mockMvc.perform(post("/api/v1/invoices/{id}/issue", invoiceId)).andExpect(status().isOk());

        // Make full payment
        String paymentJson =
                """
                {
                    "amount": 50.00,
                    "paymentMethod": "CARD",
                    "notes": "Full payment"
                }
                """;

        mockMvc.perform(
                        post("/api/v1/invoices/{id}/payments", invoiceId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(paymentJson))
                .andExpect(status().isCreated());

        // Get debt for client - should show zero outstanding
        mockMvc.perform(get("/api/v1/invoices/client/{clientId}/debt", clientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientId").value(clientId))
                .andExpect(jsonPath("$.totalOutstanding").value(0))
                .andExpect(jsonPath("$.totalInvoiced").value(50.00))
                .andExpect(jsonPath("$.totalPaid").value(50.00))
                .andExpect(jsonPath("$.unpaidInvoiceCount").value(0));
    }
}
