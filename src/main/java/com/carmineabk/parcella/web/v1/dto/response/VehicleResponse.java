package com.carmineabk.parcella.web.v1.dto.response;

import java.util.UUID;

import com.carmineabk.parcella.domain.entity.Vehicle;
import com.carmineabk.parcella.domain.enums.PackageSize;
import com.carmineabk.parcella.domain.enums.VehicleType;

public record VehicleResponse(UUID id, VehicleType type, String plateNumber,
                              PackageSize maxPackageSize, boolean active) {

    public static VehicleResponse from(Vehicle v) {
        return new VehicleResponse(v.getId(), v.getType(), v.getPlateNumber(),
                v.getMaxPackageSize(), v.isActive());
    }
}
