package com.securepg.dto;

import com.securepg.entity.ConcernCategory;
import com.securepg.entity.ConcernStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class ConcernResponse {

    private UUID id;
    private ConcernCategory category;
    private String description;
    private ConcernStatus status;
    private Instant createdAt;
    private Instant updatedAt;
}
