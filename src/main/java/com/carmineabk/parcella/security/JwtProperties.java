package com.carmineabk.parcella.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.time.Duration;

@ConfigurationProperties(prefix = "parcella.jwt")
public record JwtProperties(String secret, Duration accessTokenTtl) {}