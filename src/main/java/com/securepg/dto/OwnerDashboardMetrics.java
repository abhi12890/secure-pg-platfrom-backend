package com.securepg.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class OwnerDashboardMetrics {

    private double occupancyRate;
    private int totalRooms;
    private int occupiedRooms;
    private BigDecimal revenueCollectedThisMonth;
    private BigDecimal totalPendingDues;
    private long openConcernsCount;
}
