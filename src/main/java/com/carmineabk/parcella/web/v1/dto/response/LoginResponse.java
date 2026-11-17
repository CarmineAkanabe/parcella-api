package com.carmineabk.parcella.web.v1.dto.response;

public record LoginResponse(String accessToken, String tokenType, long expiresIn, UserResponse user) {}