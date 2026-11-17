package com.carmineabk.parcella.web.v1.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Size(max = 150) @Pattern(regexp = ".*\\S.*", message = "must not be blank") String fullName,
        @Pattern(regexp = "^237\\d{9}$", message = "must be 237 followed by 9 digits") String paymentPhoneNumber,
        @Pattern(regexp = "^237\\d{9}$", message = "must be 237 followed by 9 digits") String preferredContactNumber,
        @Valid LocationRequest registeredLocation) {}