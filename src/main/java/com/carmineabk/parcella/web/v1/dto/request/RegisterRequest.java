package com.carmineabk.parcella.web.v1.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

public record RegisterRequest(
        @NotBlank @Pattern(regexp = "CUSTOMER|DRIVER", message = "must be CUSTOMER or DRIVER") String role,
        @NotBlank @Size(max = 150) String fullName,
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(min = 8, max = 72) String password,
        @NotBlank @Pattern(regexp = "^237\\d{9}$", message = "must be 237 followed by 9 digits") String paymentPhoneNumber,
        @NotBlank @Pattern(regexp = "^237\\d{9}$", message = "must be 237 followed by 9 digits") String preferredContactNumber,
        @NotNull @Valid LocationRequest registeredLocation) {}