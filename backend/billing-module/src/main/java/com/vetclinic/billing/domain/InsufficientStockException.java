package com.vetclinic.billing.domain;

import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;

import com.vetclinic.common.exception.BusinessException;
import com.vetclinic.common.exception.ErrorCode;

/** Exception thrown when there is insufficient stock for an operation. */
public class InsufficientStockException extends BusinessException {

    public InsufficientStockException(UUID itemId, String itemName, int requested, int available) {
        super(
                ErrorCode.INSUFFICIENT_STOCK,
                HttpStatus.CONFLICT,
                String.format(
                        "Insufficient stock for '%s': requested %d, available %d",
                        itemName, requested, available),
                Map.of(
                        "itemId", itemId,
                        "itemName", itemName,
                        "requested", requested,
                        "available", available));
    }
}
