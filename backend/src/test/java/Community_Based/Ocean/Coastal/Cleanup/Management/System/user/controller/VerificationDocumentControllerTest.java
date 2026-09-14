package Community_Based.Ocean.Coastal.Cleanup.Management.System.user.controller;

import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.config.JwtService;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.entity.User;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.entity.VerificationDocument;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.entity.enums.UserRole;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.entity.enums.UserStatus;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.error.ErrorCode;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.repository.UserRepository;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.repository.VerificationDocumentRepository;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.user.dto.VerificationDocumentResponse;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.user.service.VerificationDocumentService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end through the real POST/GET /users/{id}/verification-documents controller — real
 * SecurityFilterChain, real multipart handling, real repositories/filesystem against the dev
 * MySQL. Same pattern as AdminUserControllerTest: @Transactional rolls back DB rows;
 * @AfterEach cleans up files (not part of the JDBC transaction).
 * <p>
 * Everything in VerificationDocumentServiceTest calls the service directly with an
 * already-resolved `requesterIsPrivileged` boolean — it proves the service's authorization
 * logic but never exercises VerificationDocumentController.hasElevatedRole() itself, which is
 * what actually reads ROLE_ADMIN/ROLE_GOVERNMENT_OFFICER off the JWT's granted authorities in
 * production. These tests go through real HTTP specifically to close that gap.
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class VerificationDocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VerificationDocumentRepository verificationDocumentRepository;

    @Autowired
    private VerificationDocumentService verificationDocumentService;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${verification-docs.dir}")
    private String verificationDocsDir;

    private JwtService jwtService;
    private User owner;
    private User otherUser;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(jwtSecret, 86_400_000L);
        owner = userRepository.save(testUser(UserRole.VOLUNTEER_DIVER));
        otherUser = userRepository.save(testUser(UserRole.VOLUNTEER_NON_DIVER));
    }

    @AfterEach
    void deleteUploadedFiles() throws IOException {
        deleteDirectoryIfExists(Path.of(verificationDocsDir, String.valueOf(owner.getUserId())));
        deleteDirectoryIfExists(Path.of(verificationDocsDir, String.valueOf(otherUser.getUserId())));
    }

    @Test
    void upload_viaRealHttp_persistsRowAndFile_verifiedIndependentlyOfResponseBody() throws Exception {
        byte[] content = "http upload test content".getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "id-card.pdf", "application/pdf", content);

        mockMvc.perform(multipart("/users/{id}/verification-documents", owner.getUserId())
                        .file(file)
                        .param("documentType", "ID_CARD")
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(owner.getUserId(), owner.getRole())))
                .andExpect(status().isCreated());

        // Deliberately re-derived from the repository/filesystem, not from the response body —
        // proves the request actually persisted real state, not just that the controller
        // returned a plausible-looking JSON body.
        List<VerificationDocument> rows = documentsForUser(owner.getUserId());
        assertThat(rows).hasSize(1);
        VerificationDocument saved = rows.get(0);
        assertThat(saved.getDocumentType()).isEqualTo("ID_CARD");

        Path fileOnDisk = Path.of(verificationDocsDir, saved.getFileUrl());
        assertThat(Files.exists(fileOnDisk)).as("file must actually be written to disk").isTrue();
        assertThat(Files.readAllBytes(fileOnDisk))
                .as("bytes on disk must match what was uploaded")
                .isEqualTo(content);
    }

    @Test
    void upload_forDifferentPathUserId_isForbidden() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "id-card.pdf", "application/pdf", "irrelevant content".getBytes()
        );

        // owner's own valid token, but the path claims to be uploading for otherUser.
        mockMvc.perform(multipart("/users/{id}/verification-documents", otherUser.getUserId())
                        .file(file)
                        .param("documentType", "ID_CARD")
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(owner.getUserId(), owner.getRole())))
                .andExpect(status().isForbidden());

        assertThat(documentsForUser(otherUser.getUserId())).isEmpty();
    }

    @Test
    void download_byOwner_viaRealHttp_returnsCorrectContentAndHeaders() throws Exception {
        byte[] content = "owner's document content via http".getBytes();
        VerificationDocumentResponse uploaded = uploadDocumentForOwner(content);

        MvcResult result = mockMvc.perform(
                        get("/users/{id}/verification-documents/{docId}", owner.getUserId(), uploaded.documentId())
                                .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(owner.getUserId(), owner.getRole())))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("attachment")))
                .andReturn();

        assertThat(result.getResponse().getContentAsByteArray())
                .as("response body bytes must match the originally uploaded content")
                .isEqualTo(content);
    }

    @Test
    void download_byAdminToken_forDifferentUsersDocument_viaRealHttp_succeedsWithCorrectContent() throws Exception {
        byte[] content = "content fetched by an admin reviewer via http".getBytes();
        VerificationDocumentResponse uploaded = uploadDocumentForOwner(content);

        // otherUser's own backing role (VOLUNTEER_NON_DIVER) is irrelevant — this token claims
        // ADMIN regardless, same synthetic-token pattern used throughout this suite
        // (JwtAuthenticationFilter never re-validates the claimed role against a real row). The
        // point is proving hasElevatedRole() reads ROLE_ADMIN off this token's authorities and
        // resolves to true — every other test bypasses that method entirely. The path {id} is
        // owner's (the document's real owner), since the path and the requester are independent.
        String adminToken = bearerTokenFor(otherUser.getUserId(), UserRole.ADMIN);

        MvcResult result = mockMvc.perform(
                        get("/users/{id}/verification-documents/{docId}", owner.getUserId(), uploaded.documentId())
                                .header(HttpHeaders.AUTHORIZATION, adminToken))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(result.getResponse().getContentAsByteArray()).isEqualTo(content);
    }

    @Test
    void download_byNonPrivilegedNonOwner_viaRealHttp_isForbidden() throws Exception {
        byte[] content = "should not be downloadable by a stranger".getBytes();
        VerificationDocumentResponse uploaded = uploadDocumentForOwner(content);

        mockMvc.perform(
                        get("/users/{id}/verification-documents/{docId}", owner.getUserId(), uploaded.documentId())
                                .header(HttpHeaders.AUTHORIZATION,
                                        bearerTokenFor(otherUser.getUserId(), UserRole.VOLUNTEER_NON_DIVER)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value(ErrorCode.FORBIDDEN.name()));
    }

    @Test
    void upload_withoutAuthorizationHeader_isUnauthorized() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "doc.pdf", "application/pdf", "irrelevant content".getBytes()
        );

        mockMvc.perform(multipart("/users/{id}/verification-documents", owner.getUserId())
                        .file(file)
                        .param("documentType", "ID_CARD"))
                .andExpect(status().isUnauthorized());

        assertThat(documentsForUser(owner.getUserId()))
                .as("nothing should be persisted for an unauthenticated upload attempt")
                .isEmpty();
    }

    @Test
    void download_withoutAuthorizationHeader_isUnauthorized() throws Exception {
        VerificationDocumentResponse uploaded = uploadDocumentForOwner("irrelevant content".getBytes());

        mockMvc.perform(get("/users/{id}/verification-documents/{docId}", owner.getUserId(), uploaded.documentId()))
                .andExpect(status().isUnauthorized());
    }

    private VerificationDocumentResponse uploadDocumentForOwner(byte[] content) {
        MockMultipartFile file = new MockMultipartFile("file", "doc.pdf", "application/pdf", content);
        return verificationDocumentService.upload(owner.getUserId(), "PASSPORT", file);
    }

    private String bearerTokenFor(Integer userId, UserRole role) {
        return "Bearer " + jwtService.generateToken(userId, role);
    }

    private List<VerificationDocument> documentsForUser(Integer userId) {
        return verificationDocumentRepository.findAll().stream()
                .filter(document -> document.getUser().getUserId().equals(userId))
                .toList();
    }

    private User testUser(UserRole role) {
        return User.builder()
                .firstName("Test")
                .lastName("User")
                .email("verification-doc-controller-test-" + UUID.randomUUID() + "@example.com")
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
