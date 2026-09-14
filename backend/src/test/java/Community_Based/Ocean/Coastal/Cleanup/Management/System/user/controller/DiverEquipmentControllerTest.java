package Community_Based.Ocean.Coastal.Cleanup.Management.System.user.controller;

import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.config.JwtService;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.entity.User;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.entity.VolunteerDiver;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.entity.enums.UserRole;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.entity.enums.UserStatus;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.repository.UserRepository;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.repository.VolunteerDiverRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class DiverEquipmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VolunteerDiverRepository volunteerDiverRepository;

    @Value("${jwt.secret}")
    private String jwtSecret;

    private JwtService jwtService;
    private User diverUser;
    private User otherUser;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(jwtSecret, 86_400_000L);
        diverUser = userRepository.save(testUser(UserRole.VOLUNTEER_DIVER));
        volunteerDiverRepository.save(VolunteerDiver.builder().user(diverUser).build());
        otherUser = userRepository.save(testUser(UserRole.VOLUNTEER_NON_DIVER));
    }

    @Test
    void addEquipment_bySelf_createsRow() throws Exception {
        String payload = """
                { "equipmentName": "BCD Vest" }
                """;

        mockMvc.perform(post("/users/{id}/diver-equipment", diverUser.getUserId())
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(diverUser.getUserId(), diverUser.getRole()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.equipmentId").exists())
                .andExpect(jsonPath("$.equipmentName").value("BCD Vest"));
    }

    @Test
    void addEquipment_withoutAuthorizationHeader_isUnauthorized() throws Exception {
        String payload = """
                { "equipmentName": "BCD Vest" }
                """;

        mockMvc.perform(post("/users/{id}/diver-equipment", diverUser.getUserId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void addEquipment_byDifferentUser_isForbidden() throws Exception {
        String payload = """
                { "equipmentName": "BCD Vest" }
                """;

        mockMvc.perform(post("/users/{id}/diver-equipment", diverUser.getUserId())
                        .header(HttpHeaders.AUTHORIZATION,
                                bearerTokenFor(otherUser.getUserId(), otherUser.getRole()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden());
    }

    @Test
    void listEquipment_bySelf_returnsPreviouslyAddedItem() throws Exception {
        String addPayload = """
                { "equipmentName": "Regulator" }
                """;
        mockMvc.perform(post("/users/{id}/diver-equipment", diverUser.getUserId())
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(diverUser.getUserId(), diverUser.getRole()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addPayload))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/users/{id}/diver-equipment", diverUser.getUserId())
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(diverUser.getUserId(), diverUser.getRole())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].equipmentName").value("Regulator"));
    }

    @Test
    void listEquipment_withoutAuthorizationHeader_isUnauthorized() throws Exception {
        mockMvc.perform(get("/users/{id}/diver-equipment", diverUser.getUserId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listEquipment_byDifferentNonPrivilegedUser_isForbidden() throws Exception {
        mockMvc.perform(get("/users/{id}/diver-equipment", diverUser.getUserId())
                        .header(HttpHeaders.AUTHORIZATION,
                                bearerTokenFor(otherUser.getUserId(), otherUser.getRole())))
                .andExpect(status().isForbidden());
    }

    private String bearerTokenFor(Integer userId, UserRole role) {
        return "Bearer " + jwtService.generateToken(userId, role);
    }

    private User testUser(UserRole role) {
        return User.builder()
                .firstName("Test")
                .lastName("Diver")
                .email("diver-equipment-controller-test-" + UUID.randomUUID() + "@example.com")
                .password("hashed-password")
                .role(role)
                .status(UserStatus.ACTIVE)
                .createdDate(LocalDateTime.now())
                .build();
    }
}
