package com.vetclinic.billing.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vetclinic.billing.domain.model.Invoice;
import com.vetclinic.billing.domain.model.InvoiceStatus;
import com.vetclinic.billing.domain.port.InvoiceRepository;

@ExtendWith(MockitoExtension.class)
class BillingServiceDebtTest {

    @Mock private InvoiceRepository invoiceRepository;

    private ClientDebtService clientDebtService;

    @BeforeEach
    void setUp() {
        clientDebtService = new ClientDebtService(invoiceRepository);
    }

    @Test
    void shouldCalculateZeroDebtForClientWithNoInvoices() {
        // given
        UUID clientId = UUID.randomUUID();
        given(invoiceRepository.findByClientId(clientId)).willReturn(List.of());

        // when
        ClientDebtService.ClientDebtSummary summary =
                clientDebtService.calculateClientDebt(clientId);

        // then
        assertThat(summary.clientId()).isEqualTo(clientId);
        assertThat(summary.totalOutstanding()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(summary.totalInvoiced()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(summary.totalPaid()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(summary.unpaidInvoiceCount()).isZero();
        assertThat(summary.overdueInvoiceCount()).isZero();
    }

    @Test
    void shouldCalculateDebtFromUnpaidInvoices() {
        // given
        UUID clientId = UUID.randomUUID();
        Invoice unpaidInvoice =
                Invoice.builder()
                        .clientId(clientId)
                        .invoiceNumber("INV-001")
                        .issueDate(LocalDate.now())
                        .status(InvoiceStatus.ISSUED)
                        .totalAmount(new BigDecimal("150.00"))
                        .paidAmount(BigDecimal.ZERO)
                        .build();

        given(invoiceRepository.findByClientId(clientId)).willReturn(List.of(unpaidInvoice));

        // when
        ClientDebtService.ClientDebtSummary summary =
                clientDebtService.calculateClientDebt(clientId);

        // then
        assertThat(summary.totalOutstanding()).isEqualByComparingTo(new BigDecimal("150.00"));
        assertThat(summary.totalInvoiced()).isEqualByComparingTo(new BigDecimal("150.00"));
        assertThat(summary.totalPaid()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(summary.unpaidInvoiceCount()).isEqualTo(1);
    }

    @Test
    void shouldCalculateDebtFromPartiallyPaidInvoices() {
        // given
        UUID clientId = UUID.randomUUID();
        Invoice partiallyPaidInvoice =
                Invoice.builder()
                        .clientId(clientId)
                        .invoiceNumber("INV-001")
                        .issueDate(LocalDate.now())
                        .status(InvoiceStatus.PARTIALLY_PAID)
                        .totalAmount(new BigDecimal("200.00"))
                        .paidAmount(new BigDecimal("75.00"))
                        .build();

        given(invoiceRepository.findByClientId(clientId)).willReturn(List.of(partiallyPaidInvoice));

        // when
        ClientDebtService.ClientDebtSummary summary =
                clientDebtService.calculateClientDebt(clientId);

        // then
        assertThat(summary.totalOutstanding()).isEqualByComparingTo(new BigDecimal("125.00"));
        assertThat(summary.totalInvoiced()).isEqualByComparingTo(new BigDecimal("200.00"));
        assertThat(summary.totalPaid()).isEqualByComparingTo(new BigDecimal("75.00"));
        assertThat(summary.unpaidInvoiceCount()).isEqualTo(1);
    }

    @Test
    void shouldExcludeCancelledInvoicesFromDebtCalculation() {
        // given
        UUID clientId = UUID.randomUUID();
        Invoice cancelledInvoice =
                Invoice.builder()
                        .clientId(clientId)
                        .invoiceNumber("INV-001")
                        .issueDate(LocalDate.now())
                        .status(InvoiceStatus.CANCELLED)
                        .totalAmount(new BigDecimal("500.00"))
                        .paidAmount(BigDecimal.ZERO)
                        .build();

        given(invoiceRepository.findByClientId(clientId)).willReturn(List.of(cancelledInvoice));

        // when
        ClientDebtService.ClientDebtSummary summary =
                clientDebtService.calculateClientDebt(clientId);

        // then
        assertThat(summary.totalOutstanding()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(summary.totalInvoiced()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(summary.unpaidInvoiceCount()).isZero();
    }

    @Test
    void shouldCountFullyPaidInvoicesAsZeroDebt() {
        // given
        UUID clientId = UUID.randomUUID();
        Invoice paidInvoice =
                Invoice.builder()
                        .clientId(clientId)
                        .invoiceNumber("INV-001")
                        .issueDate(LocalDate.now())
                        .status(InvoiceStatus.PAID)
                        .totalAmount(new BigDecimal("100.00"))
                        .paidAmount(new BigDecimal("100.00"))
                        .build();

        given(invoiceRepository.findByClientId(clientId)).willReturn(List.of(paidInvoice));

        // when
        ClientDebtService.ClientDebtSummary summary =
                clientDebtService.calculateClientDebt(clientId);

        // then
        assertThat(summary.totalOutstanding()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(summary.totalInvoiced()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(summary.totalPaid()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(summary.unpaidInvoiceCount()).isZero();
    }

    @Test
    void shouldCountOverdueInvoicesByDueDate() {
        // given
        UUID clientId = UUID.randomUUID();
        Invoice overdueInvoice =
                Invoice.builder()
                        .clientId(clientId)
                        .invoiceNumber("INV-001")
                        .issueDate(LocalDate.now().minusDays(30))
                        .dueDate(LocalDate.now().minusDays(15))
                        .status(InvoiceStatus.ISSUED)
                        .totalAmount(new BigDecimal("100.00"))
                        .paidAmount(BigDecimal.ZERO)
                        .build();

        given(invoiceRepository.findByClientId(clientId)).willReturn(List.of(overdueInvoice));

        // when
        ClientDebtService.ClientDebtSummary summary =
                clientDebtService.calculateClientDebt(clientId);

        // then
        assertThat(summary.overdueInvoiceCount()).isEqualTo(1);
        assertThat(summary.unpaidInvoiceCount()).isEqualTo(1);
    }

    @Test
    void shouldCountOverdueInvoicesByStatus() {
        // given
        UUID clientId = UUID.randomUUID();
        Invoice overdueInvoice =
                Invoice.builder()
                        .clientId(clientId)
                        .invoiceNumber("INV-001")
                        .issueDate(LocalDate.now())
                        .status(InvoiceStatus.OVERDUE)
                        .totalAmount(new BigDecimal("100.00"))
                        .paidAmount(BigDecimal.ZERO)
                        .build();

        given(invoiceRepository.findByClientId(clientId)).willReturn(List.of(overdueInvoice));

        // when
        ClientDebtService.ClientDebtSummary summary =
                clientDebtService.calculateClientDebt(clientId);

        // then
        assertThat(summary.overdueInvoiceCount()).isEqualTo(1);
    }

    @Test
    void shouldAggregateDebtFromMultipleInvoices() {
        // given
        UUID clientId = UUID.randomUUID();
        Invoice invoice1 =
                Invoice.builder()
                        .clientId(clientId)
                        .invoiceNumber("INV-001")
                        .issueDate(LocalDate.now())
                        .status(InvoiceStatus.ISSUED)
                        .totalAmount(new BigDecimal("100.00"))
                        .paidAmount(BigDecimal.ZERO)
                        .build();

        Invoice invoice2 =
                Invoice.builder()
                        .clientId(clientId)
                        .invoiceNumber("INV-002")
                        .issueDate(LocalDate.now())
                        .status(InvoiceStatus.PARTIALLY_PAID)
                        .totalAmount(new BigDecimal("200.00"))
                        .paidAmount(new BigDecimal("50.00"))
                        .build();

        Invoice invoice3 =
                Invoice.builder()
                        .clientId(clientId)
                        .invoiceNumber("INV-003")
                        .issueDate(LocalDate.now())
                        .status(InvoiceStatus.PAID)
                        .totalAmount(new BigDecimal("75.00"))
                        .paidAmount(new BigDecimal("75.00"))
                        .build();

        Invoice invoice4 =
                Invoice.builder()
                        .clientId(clientId)
                        .invoiceNumber("INV-004")
                        .issueDate(LocalDate.now())
                        .status(InvoiceStatus.CANCELLED)
                        .totalAmount(new BigDecimal("300.00"))
                        .paidAmount(BigDecimal.ZERO)
                        .build();

        given(invoiceRepository.findByClientId(clientId))
                .willReturn(List.of(invoice1, invoice2, invoice3, invoice4));

        // when
        ClientDebtService.ClientDebtSummary summary =
                clientDebtService.calculateClientDebt(clientId);

        // then
        // Total invoiced: 100 + 200 + 75 = 375 (excluding cancelled)
        assertThat(summary.totalInvoiced()).isEqualByComparingTo(new BigDecimal("375.00"));
        // Total paid: 0 + 50 + 75 = 125
        assertThat(summary.totalPaid()).isEqualByComparingTo(new BigDecimal("125.00"));
        // Outstanding: 375 - 125 = 250
        assertThat(summary.totalOutstanding()).isEqualByComparingTo(new BigDecimal("250.00"));
        // Unpaid count: invoice1 and invoice2 (invoice3 is fully paid, invoice4 is cancelled)
        assertThat(summary.unpaidInvoiceCount()).isEqualTo(2);
    }
}
