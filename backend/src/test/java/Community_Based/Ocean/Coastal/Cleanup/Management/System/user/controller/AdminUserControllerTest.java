package Community_Based.Ocean.Coastal.Cleanup.Management.System.user.controller;

import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.config.JwtService;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.entity.GovernmentOfficer;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.entity.User;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.entity.enums.UserRole;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.error.ErrorCode;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.repository.GovernmentOfficerRepository;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end through the real POST /admin/users controller — real SecurityFilterChain,
 * @PreAuthorize, AuthService, and repositories against the dev MySQL. @Transactional rolls back
 * everything each test persists (the controller's own @Transactional on AuthService joins this
 * same transaction — default REQUIRED propagation — so nothing needs manual cleanup).
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GovernmentOfficerRepository governmentOfficerRepository;

    @Value("${jwt.secret}")
    private String jwtSecret;

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(jwtSecret, 86_400_000L);
    }

    private String bearerTokenFor(UserRole role) {
        return "Bearer " + jwtService.generateToken(1, role);
    }

    @Test
    void adminCreatesGovernmentOfficerAccount_succeeds_andBothRowsExist() throws Exception {
        String payload = """
                {
                  "firstName": "Grace",
                  "lastName": "Hopper",
                  "email": "grace.officer@example.com",
                  "password": "supersecret",
                  "role": "GOVERNMENT_OFFICER",
                  "roleDetails": { "department": "Marine Affairs", "designation": "Senior Officer" }
                }
                """;

        MvcResult result = mockMvc.perform(post("/admin/users")
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(UserRole.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("GOVERNMENT_OFFICER"))
                .andExpect(jsonPath("$.email").value("grace.officer@example.com"))
                .andReturn();

        // Must NOT auto-log-in the new account: a UserProfileResponse has no "token" field.
        assertThat(result.getResponse().getContentAsString()).doesNotContain("\"token\"");

        User savedUser = userRepository.findByEmail("grace.officer@example.com").orElseThrow();
        assertThat(savedUser.getRole()).isEqualTo(UserRole.GOVERNMENT_OFFICER);

        GovernmentOfficer officer = governmentOfficerRepository.findById(savedUser.getUserId()).orElseThrow();
        assertThat(officer.getDepartment()).isEqualTo("Marine Affairs");
        assertThat(officer.getDesignation()).isEqualTo("Senior Officer");
    }

    @Test
    void nonAdminToken_isForbidden() throws Exception {
        String payload = """
                {
                  "firstName": "Grace",
                  "lastName": "Hopper",
                  "email": "should-not-be-created@example.com",
                  "password": "supersecret",
                  "role": "GOVERNMENT_OFFICER"
                }
                """;

        mockMvc.perform(post("/admin/users")
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(UserRole.VOLUNTEER_NON_DIVER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden());

        assertThat(userRepository.findByEmail("should-not-be-created@example.com")).isEmpty();
    }

    @Test
    void unauthenticatedRequest_isUnauthorized() throws Exception {
        String payload = """
                {
                  "firstName": "Grace",
                  "lastName": "Hopper",
                  "email": "unauth@example.com",
                  "password": "supersecret",
                  "role": "GOVERNMENT_OFFICER"
                }
                """;

        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminAttemptingVolunteerDiverRole_isRejected() throws Exception {
        String payload = """
                {
                  "firstName": "Grace",
                  "lastName": "Hopper",
                  "email": "should-not-exist@example.com",
                  "password": "supersecret",
                  "role": "VOLUNTEER_DIVER"
                }
                """;

        mockMvc.perform(post("/admin/users")
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(UserRole.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.message")
                        .value("This endpoint only creates ADMIN or GOVERNMENT_OFFICER accounts"));

        assertThat(userRepository.findByEmail("should-not-exist@example.com")).isEmpty();
    }

    @Test
    void malformedBody_failsValidationWith400_andPersistsNothing() throws Exception {
        // Blank email — same VALIDATION_ERROR shape RegisterRequestValidationTest proved for the
        // public registration endpoint in Step 6. Proves @Valid is actually wired on this
        // controller's @RequestBody parameter, not just present on the DTO itself.
        String payload = """
                {
                  "firstName": "Grace",
                  "lastName": "Hopper",
                  "email": "",
                  "password": "supersecret",
                  "role": "GOVERNMENT_OFFICER"
                }
                """;

        mockMvc.perform(post("/admin/users")
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(UserRole.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value(ErrorCode.VALIDATION_ERROR.name()));

        assertThat(userRepository.findByEmail(""))
                .as("no User row should be persisted for a request that failed validation")
                .isEmpty();
    }
}
