package com.carmineabk.parcella.service;

import com.carmineabk.parcella.domain.entity.Location;
import com.carmineabk.parcella.domain.entity.User;
import com.carmineabk.parcella.repository.LocationRepository;
import com.carmineabk.parcella.repository.UserRepository;
import com.carmineabk.parcella.util.Geo;
import com.carmineabk.parcella.web.v1.dto.request.UpdateProfileRequest;
import com.carmineabk.parcella.web.v1.dto.response.UserResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository users;
    private final LocationRepository locations;

    @Transactional(readOnly = true)
    public UserResponse getMe(UUID id) {
        return UserResponse.from(find(id));
    }

    @Transactional
    public UserResponse updateMe(UUID id, UpdateProfileRequest req) {
        User user = find(id);
        if (req.fullName() != null) user.setFullName(req.fullName().trim());
        if (req.paymentPhoneNumber() != null) user.setPaymentPhoneNumber(req.paymentPhoneNumber());
        if (req.preferredContactNumber() != null) user.setPreferredContactNumber(req.preferredContactNumber());
        if (req.registeredLocation() != null) {
            Location loc = user.getRegisteredLocation();
            if (loc == null) loc = new Location();      // admins have none yet
            loc.setAddress(req.registeredLocation().address());
            loc.setPoint(Geo.point(req.registeredLocation().lat(), req.registeredLocation().lng()));
            user.setRegisteredLocation(locations.save(loc));
        }
        return UserResponse.from(user);   // no save(user): a managed entity is flushed on commit
    }

    private User find(UUID id) {
        return users.findById(id).orElseThrow(() -> new EntityNotFoundException("User not found"));
    }
}