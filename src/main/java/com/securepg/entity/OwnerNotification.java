package com.securepg.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "owner_notifications", indexes = {
        @Index(name = "idx_owner_notification_created", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OwnerNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 512)
    private String message;

    @Column(name = "event_type", nullable = false, length = 64)
    private String eventType;

    @Column(name = "read_flag", nullable = false)
    @Builder.Default
    private Boolean readFlag = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
