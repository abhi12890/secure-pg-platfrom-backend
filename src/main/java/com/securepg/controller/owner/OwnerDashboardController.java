package com.securepg.controller.owner;

import com.securepg.dto.*;
import com.securepg.entity.ConcernStatus;
import com.securepg.entity.TenantStatus;
import com.securepg.service.ConcernService;
import com.securepg.service.OwnerDashboardService;
import com.securepg.service.OwnerNotificationStreamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/owner")
@RequiredArgsConstructor
public class OwnerDashboardController {

    private final OwnerDashboardService ownerDashboardService;
    private final ConcernService concernService;
    private final OwnerNotificationStreamService ownerNotificationStreamService;

    @GetMapping("/dashboard/metrics")
    public ResponseEntity<OwnerDashboardMetrics> getMetrics() {
        return ResponseEntity.ok(ownerDashboardService.getMetrics());
    }

    @GetMapping("/residents/active")
    public ResponseEntity<List<ResidentSummary>> getActiveResidents() {
        return ResponseEntity.ok(ownerDashboardService.getResidentsByStatus(TenantStatus.ACTIVE));
    }

    @GetMapping("/residents/inactive")
    public ResponseEntity<List<ResidentSummary>> getInactiveResidents() {
        return ResponseEntity.ok(ownerDashboardService.getResidentsByStatus(TenantStatus.INACTIVE));
    }

    @PostMapping("/residents/{tenantProfileId}/deactivate")
    public ResponseEntity<ResidentSummary> deactivateResident(@PathVariable UUID tenantProfileId) {
        return ResponseEntity.ok(ownerDashboardService.deactivateTenant(tenantProfileId));
    }

    @GetMapping("/concerns")
    public ResponseEntity<List<OwnerConcernItem>> getConcerns() {
        return ResponseEntity.ok(concernService.getAllConcernsForOwner());
    }

    @PatchMapping("/concerns/{concernId}/status")
    public ResponseEntity<OwnerConcernItem> updateConcernStatus(
            @PathVariable UUID concernId,
            @Valid @RequestBody UpdateConcernStatusRequest request) {
        return ResponseEntity.ok(concernService.updateConcernStatus(concernId, request.getStatus()));
    }

    @GetMapping(value = "/notifications/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamNotifications() {
        return ownerNotificationStreamService.subscribe();
    }

    @GetMapping("/notifications")
    public ResponseEntity<List<OwnerNotificationDto>> getNotifications() {
        return ResponseEntity.ok(ownerDashboardService.getNotifications());
    }
}
