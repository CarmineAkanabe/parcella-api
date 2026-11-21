package com.carmineabk.parcella.web.v1.controller;

import com.carmineabk.parcella.service.DriverService;
import com.carmineabk.parcella.web.v1.dto.request.AvailabilityRequest;
import com.carmineabk.parcella.web.v1.dto.response.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/drivers/me")
@PreAuthorize("hasRole('DRIVER')")
@RequiredArgsConstructor
public class DriverProfileController {

    private final DriverService service;

    @PatchMapping("/availability")
    public UserResponse availability(@AuthenticationPrincipal Jwt jwt,
                                     @Valid @RequestBody AvailabilityRequest req) {
        return service.updateAvailability(driverId(jwt), req.availabilityStatus());
    }

    @PostMapping(path = "/license", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UserResponse license(@AuthenticationPrincipal Jwt jwt,
                                @RequestParam("file") MultipartFile file) {
        return service.uploadLicense(driverId(jwt), file);
    }

    private static UUID driverId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }
}