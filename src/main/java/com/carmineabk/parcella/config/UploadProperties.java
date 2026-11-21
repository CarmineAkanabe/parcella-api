package com.carmineabk.parcella.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "parcella.upload")
public record UploadProperties(String dir) {

    public UploadProperties {
        if (dir == null || dir.isBlank() || dir.contains("${")) {
            throw new IllegalStateException("parcella.upload.dir (env UPLOAD_DIR) is not set");
        }
    }
}