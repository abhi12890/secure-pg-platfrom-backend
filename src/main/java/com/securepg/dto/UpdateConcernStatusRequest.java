package com.securepg.dto;

import com.securepg.entity.ConcernStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateConcernStatusRequest {

    @NotNull
    private ConcernStatus status;
}
