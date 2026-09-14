package Community_Based.Ocean.Coastal.Cleanup.Management.System.common.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 * Shared file-storage abstraction (see CLAUDE.md "Open technical decisions" / File/media storage
 * — local disk for this pass). Not module-specific: any module needing to store an uploaded file
 * (verification documents now; report media later) can depend on this without depending on the
 * User Registration module.
 */
public interface FileStorageService {

    /**
     * Stores the file under a per-owner subdirectory and returns a path relative to the
     * configured storage root — never an absolute filesystem path (that would leak local
     * filesystem structure into a DB column that's meant to be portable across environments).
     * Pass the returned value back into {@link #load(String)} to retrieve the file later.
     */
    String store(Integer ownerId, MultipartFile file);

    /**
     * Resolves a previously-stored relative path back to a readable Resource. Throws if the path
     * escapes the configured storage root or the file doesn't exist.
     */
    Resource load(String relativePath);
}
