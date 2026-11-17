package com.carmineabk.parcella.domain.entity;

import com.carmineabk.parcella.domain.enums.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "role", discriminatorType = DiscriminatorType.STRING, length = 20)
@Getter @Setter @NoArgsConstructor
public abstract class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;   

    @Column(nullable = false, length = 150)
    private String fullName;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, length = 100)
    private String passwordHash;

    @Column(length = 12)
    private String paymentPhoneNumber;

    @Column(length = 12)
    private String preferredContactNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registered_location_id")
    private Location registeredLocation;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public abstract Role getRole();
}
