package com.vetclinic.api.dto;

import lombok.Builder;

@Builder
public record DashboardStatsResponse(
        long totalPatients,
        long visitsToday,
        long pendingInvoices,
        long lowStockItems,
        long totalClients,
        long completedVisitsToday,
        long scheduledVisitsToday) {}
