package com.carmineabk.parcella.web.v1.dto.request;

import com.carmineabk.parcella.domain.enums.PackageSize;
import com.carmineabk.parcella.domain.enums.VehicleType;

import jakarta.validation.constraints.Pattern;

public record VehicleUpdateRequest(
        VehicleType type,
        @Pattern(regexp = "^[A-Za-z0-9 -]{4,20}$",
                message = "4-20 characters: letters, digits, spaces, hyphens") String plateNumber,
        PackageSize maxPackageSize) {}
