package com.carmineabk.parcella.domain.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Point;
import java.util.UUID;

@Entity 
@Table (name = "locations")
@Getter @Setter @NoArgsConstructor 
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;   

    @Column (nullable = false, length = 500)
    private String address;

    @JdbcTypeCode (SqlTypes.GEOGRAPHY)   // [verify] constant name on Hibernate 7
    @Column(nullable = false, columnDefinition = "geography(Point,4326)")
    private Point point;                // org.locationtech.jts.geom.Point
}
