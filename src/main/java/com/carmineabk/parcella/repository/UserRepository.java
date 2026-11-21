package com.carmineabk.parcella.repository;

import com.carmineabk.parcella.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);

    @Override
    @EntityGraph(attributePaths = "registeredLocation")
    Page<User> findAll(Pageable pageable);

    @EntityGraph(attributePaths = "registeredLocation")
    @Query(value = "select u from User u where type(u) = :type",
        countQuery = "select count(u) from User u where type(u) = :type")
    Page<User> findByType(@Param("type") Class<? extends User> type, Pageable pageable);
}
