package com.carmineabk.parcella.config;

import com.carmineabk.parcella.domain.entity.Admin;
import com.carmineabk.parcella.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevDataSeeder implements ApplicationRunner {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;

    @Value("${parcella.seed.admin-email}")
    private String adminEmail;

    @Value("${parcella.seed.admin-password}")
    private String adminPassword;

    @Override
    public void run(ApplicationArguments args) {
        if (users.existsByEmailIgnoreCase(adminEmail)) {
            return;   // safe to restart: seeding is idempotent
        }
        Admin admin = new Admin();
        admin.setFullName("Platform Admin");
        admin.setEmail(adminEmail.trim().toLowerCase());
        admin.setPasswordHash(passwordEncoder.encode(adminPassword));
        admin.setStaffId("ADMIN-001");
        users.save(admin);
        log.info("Seeded dev admin {}", adminEmail);
    }
}