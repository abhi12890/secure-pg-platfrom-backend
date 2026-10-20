package com.securepg.service;

import com.securepg.dto.InvoiceLedgerItem;
import com.securepg.entity.MonthlyInvoice;
import com.securepg.entity.PaymentStatus;
import com.securepg.entity.TenantProfile;
import com.securepg.entity.TenantStatus;
import com.securepg.exception.BusinessException;
import com.securepg.repository.MonthlyInvoiceRepository;
import com.securepg.repository.SystemReminderRepository;
import com.securepg.repository.TenantProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final MonthlyInvoiceRepository monthlyInvoiceRepository;
    private final TenantProfileRepository tenantProfileRepository;
    private final SystemReminderRepository systemReminderRepository;

    @Transactional(readOnly = true)
    public List<InvoiceLedgerItem> getLedgerForTenant(TenantProfile tenant) {
        return monthlyInvoiceRepository.findByTenantOrderByBillingYearDescBillingMonthDesc(tenant)
                .stream()
                .map(this::toLedgerItem)
                .toList();
    }

    @Transactional(readOnly = true)
    public MonthlyInvoice requireOwnedInvoice(UUID invoiceId, TenantProfile tenant) {
        return monthlyInvoiceRepository.findByIdAndTenant(invoiceId, tenant)
                .orElseThrow(() -> new BusinessException("Invoice not found or access denied"));
    }

    @Transactional
    public void generateMonthlyInvoicesForActiveTenants() {
        YearMonth billingPeriod = YearMonth.now();
        List<TenantProfile> activeTenants = tenantProfileRepository.findByStatus(TenantStatus.ACTIVE);

        for (TenantProfile tenant : activeTenants) {
            if (monthlyInvoiceRepository.existsByTenantAndBillingMonthAndBillingYear(
                    tenant, billingPeriod.getMonthValue(), billingPeriod.getYear())) {
                continue;
            }

            LocalDate dueDate = billingPeriod.atEndOfMonth().plusDays(5);
            MonthlyInvoice invoice = MonthlyInvoice.builder()
                    .tenant(tenant)
                    .billingMonth(billingPeriod.getMonthValue())
                    .billingYear(billingPeriod.getYear())
                    .baseRent(tenant.getMonthlyRentRate())
                    .totalAmountDue(tenant.getMonthlyRentRate())
                    .amountPaid(BigDecimal.ZERO)
                    .dueDate(dueDate)
                    .paymentStatus(PaymentStatus.PENDING)
                    .build();

            monthlyInvoiceRepository.save(invoice);
        }
    }

    @Transactional
    public void markInvoicePaid(MonthlyInvoice invoice) {
        invoice.setAmountPaid(invoice.getTotalAmountDue());
        invoice.setPaymentStatus(PaymentStatus.PAID);
        invoice.setLastPaidDate(java.time.Instant.now());
        monthlyInvoiceRepository.save(invoice);
        systemReminderRepository.deactivateForInvoice(invoice);
    }

    private InvoiceLedgerItem toLedgerItem(MonthlyInvoice invoice) {
        BigDecimal pending = invoice.getTotalAmountDue().subtract(invoice.getAmountPaid());
        return InvoiceLedgerItem.builder()
                .id(invoice.getId())
                .billingMonth(invoice.getBillingMonth())
                .billingYear(invoice.getBillingYear())
                .totalAmountDue(invoice.getTotalAmountDue())
                .amountPaid(invoice.getAmountPaid())
                .pendingBalance(pending.max(BigDecimal.ZERO))
                .paymentStatus(invoice.getPaymentStatus())
                .dueDate(invoice.getDueDate())
                .lastPaidDate(invoice.getLastPaidDate())
                .build();
    }
}
