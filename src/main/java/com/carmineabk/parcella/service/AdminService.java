package com.carmineabk.parcella.service;

import com.carmineabk.parcella.domain.entity.Driver;
import com.carmineabk.parcella.domain.entity.Admin;
import com.carmineabk.parcella.domain.entity.Customer;
import com.carmineabk.parcella.domain.entity.User;
import com.carmineabk.parcella.domain.enums.Role;
import com.carmineabk.parcella.domain.enums.VerificationStatus;
import com.carmineabk.parcella.repository.DriverRepository;
import com.carmineabk.parcella.repository.UserRepository;
import com.carmineabk.parcella.service.FileStorageService.StoredFile;
import com.carmineabk.parcella.web.v1.dto.response.AdminDriverResponse;
import com.carmineabk.parcella.web.v1.dto.response.PageResponse;
import com.carmineabk.parcella.web.v1.dto.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminService {

    private static final int MAX_PAGE_SIZE = 100;

    private final DriverRepository drivers;
    private final UserRepository users;
    private final FileStorageService storage;

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> listDrivers(VerificationStatus status, int page, int size) {
        Pageable pageable = pageable(page, size);
        Page<Driver> result = status == null
                ? drivers.findAll(pageable)
                : drivers.findByVerificationStatus(status, pageable);
        return PageResponse.from(result, UserResponse::from);
    }

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> listUsers(Role role, int page, int size) {
        Pageable pageable = pageable(page, size);
        Page<User> result = role == null
                ? users.findAll(pageable)
                : users.findByType(typeOf(role), pageable);
        return PageResponse.from(result, UserResponse::from);
    }

    /** Maps the API's role filter to the entity subclass, which is how SINGLE_TABLE is queried. */
    private static Class<? extends User> typeOf(Role role) {
        return switch (role) {
            case CUSTOMER -> Customer.class;
            case DRIVER -> Driver.class;
            case ADMIN -> Admin.class;
        };
    }

    @Transactional(readOnly = true)
    public AdminDriverResponse driverDetail(UUID id) {
        return AdminDriverResponse.from(loadDriver(id));
    }

    @Transactional
    public UserResponse decide(UUID id, VerificationStatus decision, String note) {
        if (decision == VerificationStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT,
                    "Decision must be APPROVED or REJECTED");
        }
        Driver driver = loadDriver(id);

        if (driver.getVerificationStatus() != VerificationStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This driver has already been decided");
        }
        if (decision == VerificationStatus.APPROVED && driver.getLicenseImage() == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "The driver has not uploaded a license");
        }
        boolean blankNote = note == null || note.isBlank();
        if (decision == VerificationStatus.REJECTED && blankNote) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT,
                    "A note is required when rejecting a driver");
        }

        driver.setVerificationStatus(decision);
        driver.setVerificationNote(blankNote ? null : note.trim());
        return UserResponse.from(drivers.saveAndFlush(driver));
    }

    @Transactional(readOnly = true)
    public StoredFile licenseOf(UUID id) {
        String path = drivers.findById(id)
                .map(Driver::getLicenseImage)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No license"));
        return storage.load(path);
    }

    private Driver loadDriver(UUID id) {
        return drivers.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Driver not found"));
    }

    /** Clamp paging input; sort is fixed server-side, never client-controlled. */
    private static Pageable pageable(int page, int size) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by("id"));
        return PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), MAX_PAGE_SIZE), sort);
    }
}