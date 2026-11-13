package com.carmineabk.parcella.web.v1.controller;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class HealthController {
    private final JdbcClient jdbcClient;

    public HealthController(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        // Query PostGIS directly to prove the extension is active and accessible
        String postgisVersion = jdbcClient.sql("SELECT postgis_full_version()")
                .query(String.class)
                .single();

        return Map.of(
                "status", "UP",
                "postgis", postgisVersion
        );
    }
}
