package com.securepg.scheduler;

import com.securepg.entity.MonthlyInvoice;
import com.securepg.entity.PaymentStatus;
import com.securepg.entity.SystemReminder;
import com.securepg.repository.MonthlyInvoiceRepository;
import com.securepg.repository.SystemReminderRepository;
import com.securepg.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class BillingScheduler {

    private final InvoiceService invoiceService;
    private final MonthlyInvoiceRepository monthlyInvoiceRepository;
    private final SystemReminderRepository systemReminderRepository;

    @Scheduled(cron = "0 0 6 1 * *", zone = "Asia/Kolkata")
    @Transactional
    public void generateMonthlyInvoices() {
        log.info("Running monthly invoice generation");
        invoiceService.generateMonthlyInvoicesForActiveTenants();
    }

    @Scheduled(cron = "0 30 6 * * *", zone = "Asia/Kolkata")
    @Transactional
    public void processRentReminders() {
        log.info("Running rent reminder scan");
        LocalDate today = LocalDate.now(java.time.Clock.system(java.time.ZoneId.of("Asia/Kolkata")));
        systemReminderRepository.deactivateExpiredReminders(today);

        for (int daysUntilDue = 1; daysUntilDue <= 3; daysUntilDue++) {
            final int reminderDays = daysUntilDue;
            LocalDate targetDueDate = today.plusDays(reminderDays);
            List<MonthlyInvoice> invoices = monthlyInvoiceRepository.findByPaymentStatusInAndDueDate(
                    List.of(PaymentStatus.PENDING, PaymentStatus.PARTIAL), targetDueDate);

            for (MonthlyInvoice invoice : invoices) {
                // Keep one active countdown per invoice; stale 3-day/2-day banners must not linger.
                systemReminderRepository.deactivateForInvoice(invoice);
                SystemReminder reminder = SystemReminder.builder()
                        .tenant(invoice.getTenant())
                        .invoice(invoice)
                        .daysUntilDue(reminderDays)
                        .active(true)
                        .build();
                systemReminderRepository.save(reminder);
            }
        }
    }
}
