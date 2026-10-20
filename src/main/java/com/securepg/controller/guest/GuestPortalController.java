package com.securepg.controller.guest;

import com.securepg.dto.*;
import com.securepg.entity.TenantProfile;
import com.securepg.security.JwtUserPrincipal;
import com.securepg.security.TenantContextResolver;
import com.securepg.service.ConcernService;
import com.securepg.service.GuestPortalService;
import com.securepg.service.PaymentService;
import com.securepg.service.TenantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/guest")
@RequiredArgsConstructor
public class GuestPortalController {

    private final TenantContextResolver tenantContextResolver;
    private final TenantService tenantService;
    private final GuestPortalService guestPortalService;
    private final PaymentService paymentService;
    private final ConcernService concernService;

    @GetMapping("/overview")
    public ResponseEntity<GuestOverviewResponse> getOverview(
            @AuthenticationPrincipal JwtUserPrincipal principal) {
        UUID tenantProfileId = tenantContextResolver.requireTenantProfileId(principal);
        return ResponseEntity.ok(guestPortalService.getOverview(tenantProfileId));
    }

    @GetMapping("/payments/ledger")
    public ResponseEntity<List<InvoiceLedgerItem>> getPaymentLedger(
            @AuthenticationPrincipal JwtUserPrincipal principal) {
        UUID tenantProfileId = tenantContextResolver.requireTenantProfileId(principal);
        return ResponseEntity.ok(guestPortalService.getPaymentLedger(tenantProfileId));
    }

    @PostMapping("/payments/initiate/{invoiceId}")
    public ResponseEntity<PaymentInitiateResponse> initiatePayment(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable UUID invoiceId) {
        UUID tenantProfileId = tenantContextResolver.requireTenantProfileId(principal);
        TenantProfile tenant = tenantService.requireTenantByProfileId(tenantProfileId);
        return ResponseEntity.ok(paymentService.initiatePayment(invoiceId, tenant));
    }

    @PostMapping("/concerns/raise")
    public ResponseEntity<ConcernResponse> raiseConcern(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @Valid @RequestBody RaiseConcernRequest request) {
        UUID tenantProfileId = tenantContextResolver.requireTenantProfileId(principal);
        TenantProfile tenant = tenantService.requireTenantByProfileId(tenantProfileId);
        return ResponseEntity.ok(concernService.raiseConcern(tenant, request));
    }

    @GetMapping("/concerns")
    public ResponseEntity<List<ConcernResponse>> getConcerns(
            @AuthenticationPrincipal JwtUserPrincipal principal) {
        UUID tenantProfileId = tenantContextResolver.requireTenantProfileId(principal);
        return ResponseEntity.ok(guestPortalService.getConcerns(tenantProfileId));
    }
}
