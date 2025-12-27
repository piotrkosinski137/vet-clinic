package com.vetclinic.billing.domain.model;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsedMaterial {

    private UUID materialId;
    private Integer quantity;
    private String notes;
}
