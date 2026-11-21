package com.carmineabk.parcella.repository;

import com.carmineabk.parcella.domain.entity.Driver;
import com.carmineabk.parcella.domain.enums.VerificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DriverRepository extends JpaRepository<Driver, UUID> {

    @Override
    @EntityGraph(attributePaths = "registeredLocation")
    Page<Driver> findAll(Pageable pageable);

    @EntityGraph(attributePaths = "registeredLocation")
    Page<Driver> findByVerificationStatus(VerificationStatus status, Pageable pageable);
}