package Community_Based.Ocean.Coastal.Cleanup.Management.System.user.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Both /auth/register and /auth/login are permitAll — no auth-failure case applies to them
 * (there's no way to be "unauthorized" for a public endpoint), so this covers happy paths only,
 * per Step 9's TEST note that auth-failure coverage is for protected endpoints.
 * @Transactional rolls back the real User/VolunteerNonDiver rows registration actually creates.
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void register_happyPath_returns201WithToken() throws Exception {
        String payload = """
                {
                  "firstName": "Grace",
                  "lastName": "Hopper",
                  "email": "auth-controller-register-test@example.com",
                  "password": "supersecret",
                  "role": "VOLUNTEER_NON_DIVER"
                }
                """;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.role").value("VOLUNTEER_NON_DIVER"));
    }

    @Test
    void login_happyPath_returns200WithValidTokenForRegisteredUser() throws Exception {
        String email = "auth-controller-login-test@example.com";
        String registerPayload = """
                {
                  "firstName": "Grace",
                  "lastName": "Hopper",
                  "email": "%s",
                  "password": "supersecret",
                  "role": "VOLUNTEER_NON_DIVER"
                }
                """.formatted(email);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerPayload))
                .andExpect(status().isCreated());

        String loginPayload = """
                {
                  "email": "%s",
                  "password": "supersecret"
                }
                """.formatted(email);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.role").value("VOLUNTEER_NON_DIVER"));
    }
}
