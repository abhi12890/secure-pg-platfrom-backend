package com.securepg.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "guest_concerns", indexes = {
        @Index(name = "idx_concern_tenant", columnList = "tenant_id"),
        @Index(name = "idx_concern_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GuestConcern {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private TenantProfile tenant;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ConcernCategory category;

    @Column(name = "encrypted_description", nullable = false, columnDefinition = "TEXT")
    private String encryptedDescription;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    @Builder.Default
    private ConcernStatus status = ConcernStatus.OPEN;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private Instant updatedAt = Instant.now();

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
