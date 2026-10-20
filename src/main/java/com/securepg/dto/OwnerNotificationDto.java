package com.securepg.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class OwnerNotificationDto {

    private UUID id;
    private String message;
    private String eventType;
    private Boolean readFlag;
    private Instant createdAt;
}
