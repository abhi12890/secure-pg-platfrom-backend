package com.securepg.dto;

import com.securepg.entity.ConcernCategory;
import com.securepg.entity.ConcernPriority;
import com.securepg.entity.ConcernStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class OwnerConcernItem {

    private UUID id;
    private String residentName;
    private String roomNumber;
    private ConcernCategory category;
    private ConcernPriority priority;
    private ConcernStatus status;
    private Instant createdAt;
}
