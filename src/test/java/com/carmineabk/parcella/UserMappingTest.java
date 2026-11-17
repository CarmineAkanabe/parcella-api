package com.carmineabk.parcella;

import com.carmineabk.parcella.domain.entity.Driver;
import com.carmineabk.parcella.domain.entity.Location;
import com.carmineabk.parcella.domain.entity.User;
import com.carmineabk.parcella.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest 
@ActiveProfiles ("test")
@Transactional 
class UserMappingTest {

    @Autowired UserRepository users;
    @Autowired EntityManager em;

    @Test 
    void savesDriverWithLocation() {
        var gf = new GeometryFactory(new PrecisionModel(), 4326);
        Location loc = new Location();
        loc.setAddress("Test address, Douala");
        loc.setPoint(gf.createPoint(new Coordinate(9.70, 4.05))); // x = longitude, y = latitude
        em.persist(loc);

        Driver d = new Driver();
        d.setFullName("Test Driver");
        d.setEmail("driver@test.com");
        d.setPasswordHash("hash");
        d.setPaymentPhoneNumber("237677777777");
        d.setPreferredContactNumber("237677777777");
        d.setRegisteredLocation(loc);
        users.saveAndFlush(d);
        em.clear();

        User found = users.findById(d.getId()).orElseThrow();
        assertThat(found).isInstanceOf(Driver.class);
        assertThat(found.getRegisteredLocation().getPoint().getX()).isEqualTo(9.70);
    }
}
