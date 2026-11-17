package com.carmineabk.parcella.domain.entity;

import com.carmineabk.parcella.domain.enums.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@DiscriminatorValue("ADMIN")
@Getter @Setter
public class Admin extends User {
    private String staffId;
    @Override public Role getRole() { return Role.ADMIN; }
}
