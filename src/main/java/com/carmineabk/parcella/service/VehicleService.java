package com.carmineabk.parcella.service;

import com.carmineabk.parcella.domain.entity.Driver;
import com.carmineabk.parcella.domain.entity.Vehicle;
import com.carmineabk.parcella.repository.VehicleRepository;
import com.carmineabk.parcella.web.v1.dto.request.VehicleCreateRequest;
import com.carmineabk.parcella.web.v1.dto.request.VehicleUpdateRequest;
import com.carmineabk.parcella.web.v1.dto.response.VehicleResponse;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicles;
    private final EntityManager em;

    @Transactional(readOnly = true)
    public List<VehicleResponse> list(UUID driverId) {
        return vehicles.findByDriverIdOrderByCreatedAtDesc(driverId).stream()
                .map(VehicleResponse::from).toList();
    }

    @Transactional
    public VehicleResponse create(UUID driverId, VehicleCreateRequest req) {
        String plate = normalize(req.plateNumber());
        if (vehicles.existsByPlateNumber(plate)) throw conflict();

        Vehicle v = new Vehicle();
        v.setDriver(em.getReference(Driver.class, driverId)); // FK only, no SELECT
        v.setType(req.type());
        v.setPlateNumber(plate);
        v.setMaxPackageSize(req.maxPackageSize());
        v.setActive(!vehicles.existsByDriverId(driverId));    // first vehicle is active
        return save(v);
    }

    @Transactional
    public VehicleResponse update(UUID driverId, UUID id, VehicleUpdateRequest req) {
        Vehicle v = owned(driverId, id);
        if (req.type() != null) v.setType(req.type());
        if (req.maxPackageSize() != null) v.setMaxPackageSize(req.maxPackageSize());
        if (req.plateNumber() != null) {
            String plate = normalize(req.plateNumber());
            if (vehicles.existsByPlateNumberAndIdNot(plate, id)) throw conflict();
            v.setPlateNumber(plate);
        }
        return save(v);
    }

    @Transactional
    public VehicleResponse activate(UUID driverId, UUID id) {
        vehicles.deactivateAllFor(driverId);  // runs now, then clears the persistence context
        Vehicle v = owned(driverId, id);      // loaded AFTER the clear, so its state is fresh
        v.setActive(true);
        return save(v);
    }

    private Vehicle owned(UUID driverId, UUID id) {
        return vehicles.findByIdAndDriverId(id, driverId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle not found"));
    }

    private VehicleResponse save(Vehicle v) {
        try {
            return VehicleResponse.from(vehicles.saveAndFlush(v));
        } catch (DataIntegrityViolationException e) {
            throw conflict(); // the unique indexes caught a race the exists-checks missed
        }
    }

    private static String normalize(String plate) {
        return plate.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
    }

    private static ResponseStatusException conflict() {
        return new ResponseStatusException(HttpStatus.CONFLICT, "Plate number already registered");
    }
}