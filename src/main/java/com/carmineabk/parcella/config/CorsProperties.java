package com.carmineabk.parcella.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "parcella.cors")
public record CorsProperties(String frontendUrl) {}