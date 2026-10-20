package com.securepg.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "monthly_invoices", indexes = {
        @Index(name = "idx_invoice_tenant", columnList = "tenant_id"),
        @Index(name = "idx_invoice_billing_period", columnList = "billing_year, billing_month"),
        @Index(name = "idx_invoice_payment_status", columnList = "payment_status")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_tenant_billing_period", columnNames = {"tenant_id", "billing_year", "billing_month"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyInvoice {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private TenantProfile tenant;

    @Column(name = "billing_month", nullable = false)
    private Integer billingMonth;

    @Column(name = "billing_year", nullable = false)
    private Integer billingYear;

    @Column(name = "base_rent", nullable = false, precision = 12, scale = 2)
    private BigDecimal baseRent;

    @Column(name = "total_amount_due", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmountDue;

    @Column(name = "amount_paid", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal amountPaid = BigDecimal.ZERO;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 16)
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Column(name = "last_paid_date")
    private Instant lastPaidDate;

    @Column(name = "razorpay_order_id", length = 128)
    private String razorpayOrderId;
}
