package Community_Based.Ocean.Coastal.Cleanup.Management.System.user.service;

import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.entity.User;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.entity.VerificationDocument;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.entity.enums.UserRole;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.entity.enums.UserStatus;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.error.ForbiddenOperationException;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.repository.UserRepository;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.repository.VerificationDocumentRepository;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.user.dto.VerificationDocumentResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @Transactional rolls back every User/VerificationDocument row this test writes to the real dev
 * MySQL. It does NOT touch the filesystem, though — file writes aren't part of a JDBC transaction
 * — so uploaded files are cleaned up explicitly in @AfterEach.
 * RegistrationVerificationService is mocked so this test proves the call site fires exactly once,
 * independent of that (admin-review-owned) table's own write succeeding.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class VerificationDocumentServiceTest {

    @Autowired
    private VerificationDocumentService verificationDocumentService;

    @Autowired
    private VerificationDocumentRepository verificationDocumentRepository;

    @Autowired
    private UserRepository userRepository;

    @Value("${verification-docs.dir}")
    private String verificationDocsDir;

    @MockitoBean
    private RegistrationVerificationService registrationVerificationService;

    private User owner;
    private User otherUser;

    @BeforeEach
    void setUp() {
        owner = userRepository.save(testUser(UserRole.VOLUNTEER_DIVER));
        otherUser = userRepository.save(testUser(UserRole.VOLUNTEER_NON_DIVER));
    }

    @AfterEach
    void deleteUploadedFiles() throws IOException {
        deleteDirectoryIfExists(Path.of(verificationDocsDir, String.valueOf(owner.getUserId())));
        deleteDirectoryIfExists(Path.of(verificationDocsDir, String.valueOf(otherUser.getUserId())));
    }

    @Test
    void upload_createsVerificationDocumentRow_writesFileToDisk_andInitiatesVerificationOnce() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "id-card.pdf", "application/pdf", "dummy verification document".getBytes()
        );

        VerificationDocumentResponse response = verificationDocumentService.upload(
                owner.getUserId(), "ID_CARD", file
        );

        assertThat(response.documentId()).isNotNull();
        assertThat(response.documentType()).isEqualTo("ID_CARD");
        assertThat(response.fileUrl())
                .as("file_url must be {userId}/{uuid}-{originalFilename}")
                .matches(owner.getUserId() + "/[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-"
                        + "[0-9a-fA-F]{4}-[0-9a-fA-F]{12}-id-card\\.pdf");

        Optional<VerificationDocument> saved = verificationDocumentRepository.findById(response.documentId());
        assertThat(saved).isPresent();
        assertThat(saved.get().getUser().getUserId()).isEqualTo(owner.getUserId());
        assertThat(saved.get().getFileUrl()).isEqualTo(response.fileUrl());

        Path fileOnDisk = Path.of(verificationDocsDir, response.fileUrl());
        assertThat(Files.exists(fileOnDisk)).as("uploaded file must actually land on disk").isTrue();

        verify(registrationVerificationService, times(1)).initiate(eq(owner.getUserId()));
    }

    @Test
    void download_byOwner_succeedsAndReturnsUploadedContent() throws IOException {
        byte[] uploadedContent = "owner's own verification document content".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file", "owner-doc.pdf", "application/pdf", uploadedContent
        );
        VerificationDocumentResponse uploaded = verificationDocumentService.upload(
                owner.getUserId(), "ID_CARD", file
        );

        Resource resource = verificationDocumentService.loadForDownload(
                uploaded.documentId(), owner.getUserId(), owner.getUserId(), false
        );

        assertThat(resource.getInputStream().readAllBytes())
                .as("downloaded bytes must match what was actually uploaded, not just \"no exception\"")
                .isEqualTo(uploadedContent);
    }

    @Test
    void download_byPrivilegedNonOwner_succeedsAndReturnsUploadedContent() throws IOException {
        byte[] uploadedContent = "content only an admin/gov-officer should also be able to fetch".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file", "owner-doc-2.pdf", "application/pdf", uploadedContent
        );
        VerificationDocumentResponse uploaded = verificationDocumentService.upload(
                owner.getUserId(), "PASSPORT", file
        );

        // The controller resolves ADMIN/GOVERNMENT_OFFICER JWT authorities into this boolean (see
        // VerificationDocumentController.hasElevatedRole) before calling loadForDownload — at the
        // service level, "a request carrying an ADMIN/GOVERNMENT_OFFICER token" and "privileged
        // requester" are the same thing, so this simulates that without needing a real token.
        Resource resource = verificationDocumentService.loadForDownload(
                uploaded.documentId(), owner.getUserId(), otherUser.getUserId(), true
        );

        assertThat(resource.getInputStream().readAllBytes()).isEqualTo(uploadedContent);
    }

    @Test
    void download_requestedByDifferentNonPrivilegedUser_isForbidden() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "passport.pdf", "application/pdf", "dummy verification document".getBytes()
        );
        VerificationDocumentResponse uploaded = verificationDocumentService.upload(
                owner.getUserId(), "PASSPORT", file
        );

        assertThatThrownBy(() ->
                verificationDocumentService.loadForDownload(
                        uploaded.documentId(), owner.getUserId(), otherUser.getUserId(), false
                )
        )
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage("You do not have permission to access this document");
    }

    @Test
    void download_withPathOwnerIdNotMatchingActualOwner_isNotFound() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "passport.pdf", "application/pdf", "dummy verification document".getBytes()
        );
        VerificationDocumentResponse uploaded = verificationDocumentService.upload(
                owner.getUserId(), "PASSPORT", file
        );

        // Even the real owner, requesting via the wrong /users/{id}/... path, must not succeed —
        // the path's claimed owner is checked against the document's actual owner.
        assertThatThrownBy(() ->
                verificationDocumentService.loadForDownload(
                        uploaded.documentId(), otherUser.getUserId(), owner.getUserId(), false
                )
        ).isInstanceOf(jakarta.persistence.EntityNotFoundException.class);
    }

    private User testUser(UserRole role) {
        return User.builder()
                .firstName("Test")
                .lastName("User")
                .email("verification-doc-test-" + UUID.randomUUID() + "@example.com")
                .password("hashed-password")
                .role(role)
                .status(UserStatus.ACTIVE)
                .createdDate(LocalDateTime.now())
                .build();
    }

    private void deleteDirectoryIfExists(Path dir) throws IOException {
        if (!Files.exists(dir)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(dir)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.delete(path);
                } catch (IOException e) {
                    throw new java.io.UncheckedIOException(e);
                }
            });
        }
    }
}
