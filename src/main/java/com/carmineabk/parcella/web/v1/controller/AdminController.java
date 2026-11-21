package com.carmineabk.parcella.web.v1.controller;

import com.carmineabk.parcella.domain.enums.Role;
import com.carmineabk.parcella.domain.enums.VerificationStatus;
import com.carmineabk.parcella.service.AdminService;
import com.carmineabk.parcella.service.FileStorageService.StoredFile;
import com.carmineabk.parcella.web.v1.dto.request.VerificationDecisionRequest;
import com.carmineabk.parcella.web.v1.dto.response.AdminDriverResponse;
import com.carmineabk.parcella.web.v1.dto.response.PageResponse;
import com.carmineabk.parcella.web.v1.dto.response.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService service;

    @GetMapping("/api/v1/admin/drivers")
    public PageResponse<UserResponse> drivers(
            @RequestParam(required = false) VerificationStatus verificationStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return service.listDrivers(verificationStatus, page, size);
    }

    @GetMapping("/api/v1/admin/drivers/{id}")
    public AdminDriverResponse driver(@PathVariable UUID id) {
        return service.driverDetail(id);
    }

    @GetMapping("/api/v1/admin/drivers/{id}/license")
    public ResponseEntity<Resource> license(@PathVariable UUID id) {
        StoredFile file = service.licenseOf(id);
        return ResponseEntity.ok()
                .contentType(file.mediaType())
                .cacheControl(CacheControl.noStore())
                .body(file.resource());
    }

    @PostMapping("/api/v1/admin/drivers/{id}/verification")
    public UserResponse decide(@PathVariable UUID id, @Valid @RequestBody VerificationDecisionRequest req) {
        return service.decide(id, req.decision(), req.note());
    }

    @GetMapping("/api/v1/admin/users")
    public PageResponse<UserResponse> users(
            @RequestParam(required = false) Role role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return service.listUsers(role, page, size);
    }
}