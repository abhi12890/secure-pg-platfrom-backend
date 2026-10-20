package com.securepg.dto;

import com.securepg.entity.ConcernCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RaiseConcernRequest {

    @NotNull
    private ConcernCategory category;

    @NotBlank
    @Size(min = 10, max = 2000)
    private String description;
}
