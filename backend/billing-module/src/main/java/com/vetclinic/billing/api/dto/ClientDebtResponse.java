package com.vetclinic.billing.api.dto;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.Builder;

@Builder
public record ClientDebtResponse(
        UUID clientId,
        BigDecimal totalOutstanding,
        BigDecimal totalInvoiced,
        BigDecimal totalPaid,
        int unpaidInvoiceCount,
        int overdueInvoiceCount) {}
