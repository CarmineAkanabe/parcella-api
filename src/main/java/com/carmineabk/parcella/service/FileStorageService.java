package com.carmineabk.parcella.service;

import com.carmineabk.parcella.config.UploadProperties;

import org.springframework.core.io.FileSystemResource;
// import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
public class FileStorageService {

    public enum Kind {
        LICENSE("licenses", 5),
        AVATAR("avatars", 2);

        private final String folder;
        private final int maxMegabytes;

        Kind(String folder, int maxMegabytes) {
            this.folder = folder;
            this.maxMegabytes = maxMegabytes;
        }
    }

    public record StoredFile(Resource resource, MediaType mediaType) {}

    private final Path root;

    public FileStorageService(UploadProperties props) {
        this.root = Path.of(props.dir()).toAbsolutePath().normalize();
    }

    /** Validates and stores an image; returns the path RELATIVE to the upload root. */
    public String store(Kind kind, UUID ownerId, MultipartFile file) {
        if (file == null || file.isEmpty()) throw invalid("A file is required");
        if (file.getSize() > kind.maxMegabytes * 1024L * 1024L) {
            throw invalid("File too large (max " + kind.maxMegabytes + " MB)");
        }
        try {
            byte[] bytes = file.getBytes();
            String ext = detectExtension(bytes);
            if (ext == null) throw invalid("Only JPEG or PNG images are accepted");

            String relative = kind.folder + "/" + ownerId + "/" + UUID.randomUUID() + "." + ext;
            Path target = resolve(relative);
            Files.createDirectories(target.getParent());
            Files.write(target, bytes);
            return relative;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public StoredFile load(String relative) {
        Path p = resolve(relative);
        if (!Files.isReadable(p)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found");
        }
        MediaType type = relative.endsWith(".png") ? MediaType.IMAGE_PNG : MediaType.IMAGE_JPEG;
        return new StoredFile(new FileSystemResource(p), type);
    }

    public void deleteQuietly(String relative) {
        if (relative == null) return;
        try {
            Files.deleteIfExists(resolve(relative));
        } catch (IOException | RuntimeException ignored) {
            // an orphaned file is harmless; failing the request over it is not worth it
        }
    }

    private Path resolve(String relative) {
        Path p = root.resolve(relative).normalize();
        if (!p.startsWith(root)) throw new IllegalArgumentException("Path escapes upload root");
        return p;
    }

    /** Trust the bytes, not the filename or the Content-Type header. */
    private static String detectExtension(byte[] b) {
        if (b.length >= 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) {
            return "jpg";
        }
        if (b.length >= 8 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G'
                && b[4] == 0x0D && b[5] == 0x0A && b[6] == 0x1A && b[7] == 0x0A) {
            return "png";
        }
        return null;
    }

    private static ResponseStatusException invalid(String message) {
        return new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT, message);
    }
}