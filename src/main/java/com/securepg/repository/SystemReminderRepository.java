package com.securepg.repository;

import com.securepg.entity.MonthlyInvoice;
import com.securepg.entity.SystemReminder;
import com.securepg.entity.TenantProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface SystemReminderRepository extends JpaRepository<SystemReminder, UUID> {

    List<SystemReminder> findByTenantAndActiveTrue(TenantProfile tenant);

    Optional<SystemReminder> findByTenantAndInvoiceIdAndDaysUntilDueAndActiveTrue(
            TenantProfile tenant, UUID invoiceId, Integer daysUntilDue);

    @Modifying
    @Query("UPDATE SystemReminder r SET r.active = false WHERE r.invoice = :invoice AND r.active = true")
    int deactivateForInvoice(MonthlyInvoice invoice);

    @Modifying
    @Query("UPDATE SystemReminder r SET r.active = false WHERE r.active = true AND r.invoice.dueDate <= :today")
    int deactivateExpiredReminders(LocalDate today);
}
