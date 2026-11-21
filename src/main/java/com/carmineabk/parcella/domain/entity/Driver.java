package com.carmineabk.parcella.domain.entity;

import com.carmineabk.parcella.domain.enums.AvailabilityStatus;
import com.carmineabk.parcella.domain.enums.Role;
import com.carmineabk.parcella.domain.enums.VerificationStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@DiscriminatorValue("DRIVER")
@Getter @Setter
public class Driver extends User {

    @Enumerated(EnumType.STRING)
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    @Enumerated(EnumType.STRING)
    private AvailabilityStatus availabilityStatus = AvailabilityStatus.OFFLINE;

    private String licenseImage;

    @Column(precision = 3, scale = 2)
    private BigDecimal averageRating = BigDecimal.ZERO;

    @Column(precision = 5, scale = 2)
    private BigDecimal trustScore = BigDecimal.ZERO;

    @Column(name = "verification_note", length = 500)
    private String verificationNote;

    @Override public Role getRole() { return Role.DRIVER; }
}