package com.securepg.service;

import com.securepg.dto.*;
import com.securepg.entity.*;
import com.securepg.exception.ResourceNotFoundException;
import com.securepg.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OwnerDashboardService {

    private final RoomRepository roomRepository;
    private final TenantProfileRepository tenantProfileRepository;
    private final MonthlyInvoiceRepository monthlyInvoiceRepository;
    private final GuestConcernRepository guestConcernRepository;
    private final OwnerNotificationRepository ownerNotificationRepository;
    private final TenantService tenantService;

    @Transactional(readOnly = true)
    public OwnerDashboardMetrics getMetrics() {
        List<Room> rooms = roomRepository.findAll();
        int totalCapacity = rooms.stream().mapToInt(Room::getCapacity).sum();
        int occupiedBeds = rooms.stream().mapToInt(Room::getCurrentOccupantsCount).sum();
        int occupiedRooms = (int) rooms.stream().filter(room -> room.getCurrentOccupantsCount() > 0).count();
        double occupancyRate = totalCapacity == 0 ? 0.0
                : (occupiedBeds * 100.0) / totalCapacity;

        ZoneId businessZone = ZoneId.of("Asia/Kolkata");
        YearMonth current = YearMonth.now(businessZone);
        Instant monthStart = current.atDay(1).atStartOfDay(businessZone).toInstant();
        Instant nextMonthStart = current.plusMonths(1).atDay(1).atStartOfDay(businessZone).toInstant();
        List<MonthlyInvoice> paymentsCollectedThisMonth = monthlyInvoiceRepository
                .findByLastPaidDateGreaterThanEqualAndLastPaidDateLessThanAndPaymentStatus(
                        monthStart, nextMonthStart, PaymentStatus.PAID);

        BigDecimal revenue = paymentsCollectedThisMonth.stream()
                .map(MonthlyInvoice::getAmountPaid)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal pending = monthlyInvoiceRepository
                .findByPaymentStatusIn(List.of(PaymentStatus.PENDING, PaymentStatus.PARTIAL))
                .stream()
                .map(i -> i.getTotalAmountDue().subtract(i.getAmountPaid()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long openConcerns = guestConcernRepository.countByStatus(ConcernStatus.OPEN);

        return OwnerDashboardMetrics.builder()
                .occupancyRate(Math.round(occupancyRate * 100.0) / 100.0)
                .totalRooms(rooms.size())
                .occupiedRooms(occupiedRooms)
                .revenueCollectedThisMonth(revenue)
                .totalPendingDues(pending)
                .openConcernsCount(openConcerns)
                .build();
    }

    @Transactional(readOnly = true)
    public List<ResidentSummary> getResidentsByStatus(TenantStatus status) {
        return tenantProfileRepository.findByStatusWithRoom(status)
                .stream()
                .map(this::toResidentSummary)
                .toList();
    }

    @Transactional
    public ResidentSummary deactivateTenant(UUID tenantProfileId) {
        TenantProfile tenant = tenantProfileRepository.findByIdWithRoomAndUser(tenantProfileId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));

        if (tenant.getStatus() == TenantStatus.INACTIVE) {
            return toResidentSummary(tenant);
        }

        tenant.setStatus(TenantStatus.INACTIVE);
        tenant.setCheckOutDate(LocalDate.now());

        Room room = tenant.getRoom();
        room.setCurrentOccupantsCount(Math.max(0, room.getCurrentOccupantsCount() - 1));

        tenantProfileRepository.save(tenant);
        roomRepository.save(room);

        return toResidentSummary(tenant);
    }

    @Transactional(readOnly = true)
    public List<OwnerNotificationDto> getNotifications() {
        return ownerNotificationRepository.findTop50ByOrderByCreatedAtDesc()
                .stream()
                .map(n -> OwnerNotificationDto.builder()
                        .id(n.getId())
                        .message(n.getMessage())
                        .eventType(n.getEventType())
                        .readFlag(n.getReadFlag())
                        .createdAt(n.getCreatedAt())
                        .build())
                .toList();
    }

    private ResidentSummary toResidentSummary(TenantProfile tenant) {
        return ResidentSummary.builder()
                .tenantProfileId(tenant.getId())
                .name(tenantService.decryptName(tenant))
                .roomNumber(tenant.getRoom().getRoomNumber())
                .checkInDate(tenant.getCheckInDate())
                .checkOutDate(tenant.getCheckOutDate())
                .monthlyRentRate(tenant.getMonthlyRentRate())
                .status(tenant.getStatus())
                .build();
    }
}
