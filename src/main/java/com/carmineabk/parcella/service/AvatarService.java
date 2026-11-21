package com.carmineabk.parcella.service;

import com.carmineabk.parcella.domain.entity.User;
import com.carmineabk.parcella.repository.UserRepository;
import com.carmineabk.parcella.service.FileStorageService.StoredFile;
import com.carmineabk.parcella.web.v1.dto.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AvatarService {

    private final UserRepository users;
    private final FileStorageService storage;

    @Transactional
    public UserResponse upload(UUID userId, MultipartFile file) {
        User user = load(userId);
        String previous = user.getAvatarImage();

        user.setAvatarImage(storage.store(FileStorageService.Kind.AVATAR, userId, file));
        UserResponse response = UserResponse.from(users.saveAndFlush(user));

        storage.deleteQuietly(previous);
        return response;
    }

    @Transactional
    public UserResponse remove(UUID userId) {
        User user = load(userId);
        String previous = user.getAvatarImage();

        user.setAvatarImage(null);
        UserResponse response = UserResponse.from(users.saveAndFlush(user));

        storage.deleteQuietly(previous);
        return response;
    }

    /** Unknown user and "no avatar" are both a 404: the caller learns nothing extra. */
    @Transactional(readOnly = true)
    public StoredFile avatarOf(UUID userId) {
        String path = users.findById(userId)
                .map(User::getAvatarImage)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No avatar"));
        return storage.load(path);
    }

    private User load(UUID userId) {
        return users.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
}