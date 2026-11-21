package com.carmineabk.parcella.web.v1.dto.response;

import com.carmineabk.parcella.domain.entity.Driver;

public record AdminDriverResponse(UserResponse user, String licenseImageUrl) {

    public static AdminDriverResponse from(Driver d) {
        String path = d.getLicenseImage();
        // relative to the API base URL; the endpoint needs a token, so fetch it as a blob
        String url = path == null ? null
                : "/admin/drivers/" + d.getId() + "/license?v=" + Integer.toHexString(path.hashCode());
        return new AdminDriverResponse(UserResponse.from(d), url);
    }
}