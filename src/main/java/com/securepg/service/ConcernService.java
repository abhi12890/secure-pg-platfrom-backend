package com.securepg.service;

import com.securepg.dto.ConcernResponse;
import com.securepg.dto.OwnerConcernItem;
import com.securepg.dto.RaiseConcernRequest;
import com.securepg.entity.ConcernStatus;
import com.securepg.entity.GuestConcern;
import com.securepg.entity.TenantProfile;
import com.securepg.exception.ResourceNotFoundException;
import com.securepg.repository.GuestConcernRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConcernService {

    private final GuestConcernRepository guestConcernRepository;
    private final FieldEncryptionService fieldEncryptionService;
    private final TenantService tenantService;
    private final NotificationService notificationService;

    @Transactional
    public ConcernResponse raiseConcern(TenantProfile tenant, RaiseConcernRequest request) {
        GuestConcern concern = GuestConcern.builder()
                .tenant(tenant)
                .category(request.getCategory())
                .encryptedDescription(fieldEncryptionService.encrypt(request.getDescription()))
                .status(ConcernStatus.OPEN)
                .build();

        concern = guestConcernRepository.save(concern);

        String residentName = tenantService.decryptName(tenant);
        notificationService.notifyOwner(
                String.format("New %s concern from %s (Room %s)",
                        request.getCategory().name(), residentName, tenant.getRoom().getRoomNumber()),
                "CONCERN_RAISED");

        return toResponse(concern);
    }

    @Transactional(readOnly = true)
    public List<ConcernResponse> getConcernsForTenant(TenantProfile tenant) {
        return guestConcernRepository.findByTenantOrderByCreatedAtDesc(tenant)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OwnerConcernItem> getAllConcernsForOwner() {
        return guestConcernRepository.findAllWithTenantAndRoom()
                .stream()
                .map(this::toOwnerItem)
                .toList();
    }

    @Transactional
    public OwnerConcernItem updateConcernStatus(UUID concernId, ConcernStatus status) {
        GuestConcern concern = guestConcernRepository.findByIdWithTenantAndRoom(concernId)
                .orElseThrow(() -> new ResourceNotFoundException("Concern not found"));

        if (status != ConcernStatus.IN_PROGRESS && status != ConcernStatus.RESOLVED) {
            throw new com.securepg.exception.BusinessException("Invalid status transition");
        }

        concern.setStatus(status);
        concern = guestConcernRepository.save(concern);
        return toOwnerItem(concern);
    }

    private OwnerConcernItem toOwnerItem(GuestConcern concern) {
        return OwnerConcernItem.builder()
                .id(concern.getId())
                .residentName(tenantService.decryptName(concern.getTenant()))
                .roomNumber(concern.getTenant().getRoom().getRoomNumber())
                .category(concern.getCategory())
                .priority(concern.getCategory().priority())
                .status(concern.getStatus())
                .createdAt(concern.getCreatedAt())
                .build();
    }

    private ConcernResponse toResponse(GuestConcern concern) {
        return ConcernResponse.builder()
                .id(concern.getId())
                .category(concern.getCategory())
                .description(fieldEncryptionService.decrypt(concern.getEncryptedDescription()))
                .status(concern.getStatus())
                .createdAt(concern.getCreatedAt())
                .updatedAt(concern.getUpdatedAt())
                .build();
    }
}
