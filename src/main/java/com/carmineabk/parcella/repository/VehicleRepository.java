package com.carmineabk.parcella.repository;

import com.carmineabk.parcella.domain.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VehicleRepository extends JpaRepository<Vehicle, UUID> {

    List<Vehicle> findByDriverIdOrderByCreatedAtDesc(UUID driverId);

    Optional<Vehicle> findByIdAndDriverId(UUID id, UUID driverId);

    boolean existsByPlateNumber(String plateNumber);

    boolean existsByPlateNumberAndIdNot(String plateNumber, UUID id);

    boolean existsByDriverId(UUID driverId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Vehicle v set v.active = false where v.driver.id = :driverId and v.active = true")
    int deactivateAllFor(@Param("driverId") UUID driverId);
}