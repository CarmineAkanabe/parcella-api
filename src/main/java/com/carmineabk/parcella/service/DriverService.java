package com.carmineabk.parcella.service;

import com.carmineabk.parcella.domain.entity.Driver;
import com.carmineabk.parcella.domain.enums.AvailabilityStatus;
import com.carmineabk.parcella.domain.enums.VerificationStatus;
import com.carmineabk.parcella.repository.DriverRepository;
import com.carmineabk.parcella.web.v1.dto.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DriverService {

    private final DriverRepository drivers;
    private final FileStorageService storage;

    @Transactional
    public UserResponse updateAvailability(UUID driverId, AvailabilityStatus requested) {
        if (requested == AvailabilityStatus.BUSY) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT,
                    "BUSY is set automatically during a delivery");
        }
        Driver driver = load(driverId);

        if (driver.getAvailabilityStatus() == AvailabilityStatus.BUSY) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "You cannot change availability during an active delivery");
        }
        if (requested == AvailabilityStatus.AVAILABLE
                && driver.getVerificationStatus() != VerificationStatus.APPROVED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Your account must be approved before you can go available");
        }
        driver.setAvailabilityStatus(requested);
        return UserResponse.from(drivers.saveAndFlush(driver)); // mapped inside the transaction
    }

    @Transactional
    public UserResponse uploadLicense(UUID driverId, MultipartFile file) {
        Driver driver = load(driverId);
        String previous = driver.getLicenseImage();

        // Pass the appropriate FileStorageService.Kind enum constant as the 1st argument
        driver.setLicenseImage(storage.store(FileStorageService.Kind.LICENSE, driverId, file));
    
        if (driver.getVerificationStatus() == VerificationStatus.REJECTED) {
            driver.setVerificationStatus(VerificationStatus.PENDING); // resubmission
            driver.setVerificationNote(null);
        }
        UserResponse response = UserResponse.from(drivers.saveAndFlush(driver));

        storage.deleteQuietly(previous);
        return response;
    }


    private Driver load(UUID driverId) {
        return drivers.findById(driverId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Driver not found"));
    }
}