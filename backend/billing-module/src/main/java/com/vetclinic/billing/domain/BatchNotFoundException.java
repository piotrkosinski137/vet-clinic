package com.vetclinic.billing.domain;

import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;

import com.vetclinic.common.exception.BusinessException;
import com.vetclinic.common.exception.ErrorCode;

/** Exception thrown when a batch is not found. */
public class BatchNotFoundException extends BusinessException {

    public BatchNotFoundException(UUID batchId) {
        super(
                ErrorCode.RESOURCE_NOT_FOUND,
                HttpStatus.NOT_FOUND,
                String.format("Inventory batch not found: %s", batchId),
                Map.of("batchId", batchId));
    }
}
