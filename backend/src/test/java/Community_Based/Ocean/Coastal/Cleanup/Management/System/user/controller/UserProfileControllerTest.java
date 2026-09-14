package Community_Based.Ocean.Coastal.Cleanup.Management.System.user.controller;

import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.config.JwtService;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.entity.User;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.entity.enums.UserRole;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.entity.enums.UserStatus;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class UserProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Value("${jwt.secret}")
    private String jwtSecret;

    private JwtService jwtService;
    private User self;
    private User otherUser;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(jwtSecret, 86_400_000L);
        self = userRepository.save(testUser(UserRole.VOLUNTEER_NON_DIVER));
        otherUser = userRepository.save(testUser(UserRole.VOLUNTEER_DIVER));
    }

    @Test
    void getProfile_bySelf_returnsOwnProfile() throws Exception {
        mockMvc.perform(get("/users/{id}/profile", self.getUserId())
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(self.getUserId(), self.getRole())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(self.getUserId()))
                .andExpect(jsonPath("$.email").value(self.getEmail()));
    }

    @Test
    void getProfile_withoutAuthorizationHeader_isUnauthorized() throws Exception {
        mockMvc.perform(get("/users/{id}/profile", self.getUserId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getProfile_byDifferentNonPrivilegedUser_isForbidden() throws Exception {
        mockMvc.perform(get("/users/{id}/profile", self.getUserId())
                        .header(HttpHeaders.AUTHORIZATION,
                                bearerTokenFor(otherUser.getUserId(), otherUser.getRole())))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateProfile_bySelf_persistsChangesToRealDatabase() throws Exception {
        String payload = """
                { "phone": "0771112222", "addressLine": "123 Ocean Ave" }
                """;

        mockMvc.perform(put("/users/{id}/profile", self.getUserId())
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(self.getUserId(), self.getRole()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phone").value("0771112222"))
                .andExpect(jsonPath("$.addressLine").value("123 Ocean Ave"));

        // Re-fetched independently from the repository, not trusting the response body alone.
        User reloaded = userRepository.findById(self.getUserId()).orElseThrow();
        assertThat(reloaded.getPhone()).isEqualTo("0771112222");
        assertThat(reloaded.getAddressLine()).isEqualTo("123 Ocean Ave");
    }

    @Test
    void updateProfile_withoutAuthorizationHeader_isUnauthorized() throws Exception {
        String payload = """
                { "phone": "0771112222" }
                """;

        mockMvc.perform(put("/users/{id}/profile", self.getUserId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateProfile_byDifferentUser_isForbiddenAndDoesNotPersist() throws Exception {
        String payload = """
                { "phone": "0779998888" }
                """;

        mockMvc.perform(put("/users/{id}/profile", self.getUserId())
                        .header(HttpHeaders.AUTHORIZATION,
                                bearerTokenFor(otherUser.getUserId(), otherUser.getRole()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden());

        User reloaded = userRepository.findById(self.getUserId()).orElseThrow();
        assertThat(reloaded.getPhone()).isNotEqualTo("0779998888");
    }

    private String bearerTokenFor(Integer userId, UserRole role) {
        return "Bearer " + jwtService.generateToken(userId, role);
    }

    private User testUser(UserRole role) {
        return User.builder()
                .firstName("Test")
                .lastName("User")
                .email("user-profile-controller-test-" + UUID.randomUUID() + "@example.com")
                .password("hashed-password")
                .role(role)
                .status(UserStatus.ACTIVE)
                .createdDate(LocalDateTime.now())
                .build();
    }
}
