package com.securepg.service;

import com.securepg.entity.TenantProfile;
import com.securepg.exception.ResourceNotFoundException;
import com.securepg.repository.TenantProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TenantService {

    private final TenantProfileRepository tenantProfileRepository;
    private final FieldEncryptionService fieldEncryptionService;

    @Transactional(readOnly = true)
    public TenantProfile requireTenantByProfileId(UUID tenantProfileId) {
        return tenantProfileRepository.findByIdWithRoomAndUser(tenantProfileId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant profile not found"));
    }

    public String decryptName(TenantProfile tenant) {
        return fieldEncryptionService.decrypt(tenant.getEncryptedName());
    }

    public String decryptPhone(TenantProfile tenant) {
        return fieldEncryptionService.decrypt(tenant.getEncryptedPhone());
    }
}
