package com.securepg.dto;

import com.securepg.entity.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class InvoiceLedgerItem {

    private UUID id;
    private Integer billingMonth;
    private Integer billingYear;
    private BigDecimal totalAmountDue;
    private BigDecimal amountPaid;
    private BigDecimal pendingBalance;
    private PaymentStatus paymentStatus;
    private LocalDate dueDate;
    private Instant lastPaidDate;
}
