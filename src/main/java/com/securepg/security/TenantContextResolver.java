package com.securepg.security;

import com.securepg.entity.Role;
import com.securepg.exception.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves tenant context exclusively from verified JWT principal — never from request input.
 */
@Component
public class TenantContextResolver {

    public UUID requireTenantProfileId(JwtUserPrincipal principal) {
        if (principal == null || principal.getRole() != Role.GUEST) {
            throw new AccessDeniedException("Guest authentication required");
        }
        if (principal.getTenantProfileId() == null) {
            throw new AccessDeniedException("Tenant profile not bound to authenticated guest");
        }
        return principal.getTenantProfileId();
    }

    public UUID requireUserId(JwtUserPrincipal principal) {
        if (principal == null) {
            throw new AccessDeniedException("Authentication required");
        }
        return principal.getUserId();
    }
}
