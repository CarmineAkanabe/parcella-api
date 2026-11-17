package com.carmineabk.parcella.service;

import com.carmineabk.parcella.domain.entity.*;
import com.carmineabk.parcella.domain.enums.Role;
import com.carmineabk.parcella.exception.ConflictException;
import com.carmineabk.parcella.repository.LocationRepository;
import com.carmineabk.parcella.repository.UserRepository;
import com.carmineabk.parcella.security.JwtProperties;
import com.carmineabk.parcella.security.TokenService;
import com.carmineabk.parcella.util.Geo;
import com.carmineabk.parcella.web.v1.dto.request.*;
import com.carmineabk.parcella.web.v1.dto.response.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository users;
    private final LocationRepository locations;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokens;
    private final JwtProperties jwtProps;

    @Transactional
    public UserResponse register(RegisterRequest req) {

        String email = req.email().trim().toLowerCase();
        if (users.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("Email already registered");
        }

        Location location = new Location();
        location.setAddress(req.registeredLocation().address());
        location.setPoint(Geo.point(req.registeredLocation().lat(), req.registeredLocation().lng()));
        locations.save(location);

        User user = switch (Role.valueOf(req.role())) {
            case CUSTOMER -> new Customer();
            case DRIVER -> new Driver();
            default -> throw new IllegalStateException("Role not allowed at registration");
        };
        user.setFullName(req.fullName().trim());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(req.password()));
        user.setPaymentPhoneNumber(req.paymentPhoneNumber());
        user.setPreferredContactNumber(req.preferredContactNumber());
        user.setRegisteredLocation(location);

        return UserResponse.from(users.saveAndFlush(user));
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest req) {
        User user = users.findByEmailIgnoreCase(req.email().trim())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }
        String token = tokens.issue(user.getId(), user.getRole());
        return new LoginResponse(token, "Bearer", jwtProps.accessTokenTtl().toSeconds(),
                UserResponse.from(user));
    }
}