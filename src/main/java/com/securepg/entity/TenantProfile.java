package com.securepg.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "tenant_profiles", indexes = {
        @Index(name = "idx_tenant_user_id", columnList = "user_id", unique = true),
        @Index(name = "idx_tenant_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "encrypted_name", nullable = false, columnDefinition = "TEXT")
    private String encryptedName;

    @Column(name = "encrypted_phone", nullable = false, columnDefinition = "TEXT")
    private String encryptedPhone;

    @Column(name = "encrypted_id_proof", nullable = false, columnDefinition = "TEXT")
    private String encryptedIdProof;

    @Column(name = "check_in_date", nullable = false)
    private LocalDate checkInDate;

    @Column(name = "check_out_date")
    private LocalDate checkOutDate;

    @Column(name = "monthly_rent_rate", nullable = false, precision = 12, scale = 2)
    private BigDecimal monthlyRentRate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    @Builder.Default
    private TenantStatus status = TenantStatus.ACTIVE;
}
