package com.securepg.dto;

import com.securepg.entity.TenantStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class ResidentSummary {

    private UUID tenantProfileId;
    private String name;
    private String roomNumber;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private BigDecimal monthlyRentRate;
    private TenantStatus status;
}
