package Community_Based.Ocean.Coastal.Cleanup.Management.System.common.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pure unit test — no Spring context needed. LocalDiskFileStorageService only needs a directory
 * string in its constructor, so a JUnit-managed @TempDir stands in for verification-docs.dir.
 */
class LocalDiskFileStorageServiceTest {

    @TempDir
    Path storageRoot;

    private LocalDiskFileStorageService fileStorageService;

    @BeforeEach
    void setUp() {
        fileStorageService = new LocalDiskFileStorageService(storageRoot.toString());
    }

    @Test
    void store_withPathTraversalFilename_staysInsideConfiguredRoot() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "../../../evil.txt", "text/plain", "malicious content".getBytes()
        );

        String relativePath = fileStorageService.store(1, file);

        assertThat(relativePath)
                .as("stored relative path must not carry the traversal segments through")
                .doesNotContain("..");
        assertThat(relativePath).endsWith("-evil.txt");

        Path resolved = storageRoot.resolve(relativePath).normalize();
        assertThat(resolved.startsWith(storageRoot.toAbsolutePath().normalize()))
                .as("resolved stored path must stay inside the configured storage root")
                .isTrue();
        assertThat(Files.exists(resolved)).as("file must actually be written where claimed").isTrue();

        // Prove the traversal genuinely didn't escape: nothing landed three levels above the root.
        Path wouldBeEscapedTarget = storageRoot.resolve("../../../evil.txt").normalize();
        assertThat(Files.exists(wouldBeEscapedTarget))
                .as("no file should exist at the path the traversal attempt targeted")
                .isFalse();
    }
}
