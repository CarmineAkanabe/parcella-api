package com.carmineabk.parcella.web.v1.dto.response;

import com.carmineabk.parcella.domain.entity.Driver;
import com.carmineabk.parcella.domain.entity.Location;
import com.carmineabk.parcella.domain.entity.User;
import com.carmineabk.parcella.domain.enums.AvailabilityStatus;
import com.carmineabk.parcella.domain.enums.VerificationStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id, String role, String fullName, String email,
        String paymentPhoneNumber, String preferredContactNumber,
        LocationResponse registeredLocation, Instant createdAt, DriverInfo driver) {

    public record DriverInfo(VerificationStatus verificationStatus, AvailabilityStatus availabilityStatus,
                             BigDecimal averageRating, BigDecimal trustScore) {}

    public static UserResponse from(User u) {
        Location loc = u.getRegisteredLocation();
        LocationResponse locationResponse = loc == null ? null
                : new LocationResponse(loc.getPoint().getY(), loc.getPoint().getX(), loc.getAddress());
        DriverInfo driverInfo = null;
        if (u instanceof Driver d) {
            driverInfo = new DriverInfo(d.getVerificationStatus(), d.getAvailabilityStatus(),
                    d.getAverageRating(), d.getTrustScore());
        }
        return new UserResponse(u.getId(), u.getRole().name(), u.getFullName(), u.getEmail(),
                u.getPaymentPhoneNumber(), u.getPreferredContactNumber(),
                locationResponse, u.getCreatedAt(), driverInfo);
    }
}