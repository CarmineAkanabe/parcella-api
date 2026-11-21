package com.carmineabk.parcella.web.v1.dto.request;

import com.carmineabk.parcella.domain.enums.AvailabilityStatus;
import jakarta.validation.constraints.NotNull;

public record AvailabilityRequest(@NotNull AvailabilityStatus availabilityStatus) {}