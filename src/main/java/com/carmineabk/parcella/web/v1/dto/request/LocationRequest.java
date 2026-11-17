package com.carmineabk.parcella.web.v1.dto.request;

import jakarta.validation.constraints.*;

public record LocationRequest(
        @NotNull @DecimalMin("-90") @DecimalMax("90") Double lat,
        @NotNull @DecimalMin("-180") @DecimalMax("180") Double lng,
        @NotBlank @Size(max = 500) String address) {}