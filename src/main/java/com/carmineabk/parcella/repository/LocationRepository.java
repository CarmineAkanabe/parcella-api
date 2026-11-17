package com.carmineabk.parcella.repository;

import com.carmineabk.parcella.domain.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface LocationRepository extends JpaRepository<Location, UUID> {}