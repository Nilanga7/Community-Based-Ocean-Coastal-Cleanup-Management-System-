package Community_Based.Ocean.Coastal.Cleanup.Management.System.common.storage;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * Stores files under {@code {verification-docs.dir}/{ownerId}/{uuid}-{originalFilename}}.
 * Every path derived from caller-supplied input (ownerId, original filename) is normalized and
 * checked to stay inside the configured root before touching the filesystem, since both are
 * ultimately traceable to request input.
 */
@Service
public class LocalDiskFileStorageService implements FileStorageService {

    private final Path rootDir;

    public LocalDiskFileStorageService(@Value("${verification-docs.dir}") String configuredDir) {
        this.rootDir = Path.of(configuredDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(rootDir);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not create file storage directory: " + rootDir, e);
        }
    }

    @Override
    public String store(Integer ownerId, MultipartFile file) {
        String safeFilename = safeOriginalFilename(file.getOriginalFilename());
        String storedFilename = UUID.randomUUID() + "-" + safeFilename;
        String relativePath = ownerId + "/" + storedFilename;

        Path target = resolveWithinRoot(relativePath);

        try {
            Files.createDirectories(target.getParent());
            file.transferTo(target);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to store uploaded file for owner " + ownerId, e);
        }

        return relativePath;
    }

    @Override
    public Resource load(String relativePath) {
        Path file = resolveWithinRoot(relativePath);
        if (!Files.exists(file) || !Files.isRegularFile(file)) {
            throw new EntityNotFoundException("Stored file not found: " + relativePath);
        }
        return new FileSystemResource(file);
    }

    private Path resolveWithinRoot(String relativePath) {
        Path resolved = rootDir.resolve(relativePath).normalize();
        if (!resolved.startsWith(rootDir)) {
            throw new IllegalArgumentException("Resolved path escapes the storage root: " + relativePath);
        }
        return resolved;
    }

    // Discards any directory components from the original filename (path traversal via
    // "../../etc/passwd"-style filenames) — only the base name is ever used.
    private String safeOriginalFilename(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            return "file";
        }
        String cleaned = StringUtils.cleanPath(originalFilename);
        String baseName = Path.of(cleaned).getFileName().toString();
        return baseName.isBlank() ? "file" : baseName;
    }
}
