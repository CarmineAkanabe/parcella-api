package com.carmineabk.parcella.web.v1.controller;

import com.carmineabk.parcella.service.VehicleService;
import com.carmineabk.parcella.web.v1.dto.request.VehicleCreateRequest;
import com.carmineabk.parcella.web.v1.dto.request.VehicleUpdateRequest;
import com.carmineabk.parcella.web.v1.dto.response.VehicleResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/drivers/me/vehicles")
@PreAuthorize("hasRole('DRIVER')")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService service;

    @GetMapping
    public List<VehicleResponse> list(@AuthenticationPrincipal Jwt jwt) {
        return service.list(driverId(jwt));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VehicleResponse create(@AuthenticationPrincipal Jwt jwt,
                                  @Valid @RequestBody VehicleCreateRequest req) {
        return service.create(driverId(jwt), req);
    }

    @PatchMapping("/{id}")
    public VehicleResponse update(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
                                  @Valid @RequestBody VehicleUpdateRequest req) {
        return service.update(driverId(jwt), id, req);
    }

    @PostMapping("/{id}/activate")
    public VehicleResponse activate(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return service.activate(driverId(jwt), id);
    }

    private static UUID driverId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }
}