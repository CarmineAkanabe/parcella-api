package com.carmineabk.parcella.domain.entity;

import com.carmineabk.parcella.domain.enums.Role;
import jakarta.persistence.*;

@Entity
@DiscriminatorValue("CUSTOMER")
public class Customer extends User {
    @Override public Role getRole() { return Role.CUSTOMER; }
}
