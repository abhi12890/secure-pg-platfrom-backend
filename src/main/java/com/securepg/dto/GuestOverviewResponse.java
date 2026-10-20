package com.securepg.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class GuestOverviewResponse {

    private String roomNumber;
    private LocalDate checkInDate;
    private BigDecimal monthlyRentRate;
    private boolean rentReminderActive;
    private String reminderMessage;
}
