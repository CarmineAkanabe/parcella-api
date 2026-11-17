package com.carmineabk.parcella.web.v1.controller;

import com.carmineabk.parcella.service.UserService;
import com.carmineabk.parcella.web.v1.dto.request.UpdateProfileRequest;
import com.carmineabk.parcella.web.v1.dto.response.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/me")
@RequiredArgsConstructor
public class MeController {

    private final UserService userService;

    @GetMapping
    public UserResponse me(@AuthenticationPrincipal Jwt jwt) {
        return userService.getMe(UUID.fromString(jwt.getSubject()));
    }

    @PatchMapping
    public UserResponse update(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody UpdateProfileRequest req) {
        return userService.updateMe(UUID.fromString(jwt.getSubject()), req);
    }
}