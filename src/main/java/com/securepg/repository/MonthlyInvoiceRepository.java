package com.securepg.repository;

import com.securepg.entity.MonthlyInvoice;
import com.securepg.entity.PaymentStatus;
import com.securepg.entity.TenantProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MonthlyInvoiceRepository extends JpaRepository<MonthlyInvoice, UUID> {

    List<MonthlyInvoice> findByTenantOrderByBillingYearDescBillingMonthDesc(TenantProfile tenant);

    Optional<MonthlyInvoice> findByIdAndTenant(UUID id, TenantProfile tenant);

    boolean existsByTenantAndBillingMonthAndBillingYear(TenantProfile tenant, Integer billingMonth, Integer billingYear);

    @Query("SELECT i FROM MonthlyInvoice i JOIN FETCH i.tenant t JOIN FETCH t.room WHERE i.paymentStatus IN :statuses AND i.dueDate = :dueDate")
    List<MonthlyInvoice> findByPaymentStatusInAndDueDate(List<PaymentStatus> statuses, LocalDate dueDate);

    @Query("SELECT i FROM MonthlyInvoice i JOIN FETCH i.tenant WHERE i.billingMonth = :month AND i.billingYear = :year")
    List<MonthlyInvoice> findByBillingMonthAndBillingYear(Integer month, Integer year);

    List<MonthlyInvoice> findByPaymentStatusIn(List<PaymentStatus> statuses);

    List<MonthlyInvoice> findByLastPaidDateGreaterThanEqualAndLastPaidDateLessThanAndPaymentStatus(
            Instant fromInclusive, Instant toExclusive, PaymentStatus paymentStatus);
}
