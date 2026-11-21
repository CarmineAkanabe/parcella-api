package com.carmineabk.parcella.web.v1.controller;

import com.carmineabk.parcella.service.AvatarService;
import com.carmineabk.parcella.service.FileStorageService.StoredFile;
import com.carmineabk.parcella.web.v1.dto.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class AvatarController {

    private final AvatarService service;

    @PostMapping(path = "/api/v1/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UserResponse upload(@AuthenticationPrincipal Jwt jwt,
                               @RequestParam("file") MultipartFile file) {
        return service.upload(UUID.fromString(jwt.getSubject()), file);
    }

    @DeleteMapping("/api/v1/me/avatar")
    public UserResponse remove(@AuthenticationPrincipal Jwt jwt) {
        return service.remove(UUID.fromString(jwt.getSubject()));
    }

    /** Public on purpose: an <img src> cannot send a bearer token. */
    @GetMapping("/api/v1/users/{id}/avatar")
    public ResponseEntity<Resource> get(@PathVariable UUID id) {
        StoredFile avatar = service.avatarOf(id);
        return ResponseEntity.ok()
                .contentType(avatar.mediaType())
                .cacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePublic())
                .body(avatar.resource());
    }
}