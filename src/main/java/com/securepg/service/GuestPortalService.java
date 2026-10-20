package com.securepg.service;

import com.securepg.dto.*;
import com.securepg.entity.*;
import com.securepg.exception.ResourceNotFoundException;
import com.securepg.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GuestPortalService {

    private final TenantProfileRepository tenantProfileRepository;
    private final SystemReminderRepository systemReminderRepository;
    private final InvoiceService invoiceService;
    private final ConcernService concernService;

    @Transactional(readOnly = true)
    public GuestOverviewResponse getOverview(UUID tenantProfileId) {
        TenantProfile tenant = tenantProfileRepository.findByIdWithRoomAndUser(tenantProfileId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant profile not found"));

        List<SystemReminder> reminders = systemReminderRepository.findByTenantAndActiveTrue(tenant);
        boolean reminderActive = !reminders.isEmpty();
        String reminderMessage = reminderActive
                ? String.format("Rent due in %d day(s). Please settle your pending invoice.",
                        reminders.get(0).getDaysUntilDue())
                : null;

        return GuestOverviewResponse.builder()
                .roomNumber(tenant.getRoom().getRoomNumber())
                .checkInDate(tenant.getCheckInDate())
                .monthlyRentRate(tenant.getMonthlyRentRate())
                .rentReminderActive(reminderActive)
                .reminderMessage(reminderMessage)
                .build();
    }

    @Transactional(readOnly = true)
    public List<InvoiceLedgerItem> getPaymentLedger(UUID tenantProfileId) {
        TenantProfile tenant = tenantProfileRepository.findByIdWithRoomAndUser(tenantProfileId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant profile not found"));
        return invoiceService.getLedgerForTenant(tenant);
    }

    @Transactional(readOnly = true)
    public List<ConcernResponse> getConcerns(UUID tenantProfileId) {
        TenantProfile tenant = tenantProfileRepository.findByIdWithRoomAndUser(tenantProfileId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant profile not found"));
        return concernService.getConcernsForTenant(tenant);
    }
}
