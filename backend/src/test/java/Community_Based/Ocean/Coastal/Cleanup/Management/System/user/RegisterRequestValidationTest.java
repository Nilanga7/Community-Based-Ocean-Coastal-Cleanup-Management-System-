package Community_Based.Ocean.Coastal.Cleanup.Management.System.user;

import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.error.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Exercises RegisterRequest's Bean Validation annotations end-to-end through the real
 * POST /auth/register (permitAll — no Authorization header needed). @Transactional rolls back
 * the real User/VolunteerNonDiver rows the "valid payload" case actually creates.
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class RegisterRequestValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void blankEmail_failsValidationWith400AndFieldMessage() throws Exception {
        String payload = """
                {
                  "firstName": "Ada",
                  "lastName": "Lovelace",
                  "email": "",
                  "password": "supersecret",
                  "role": "VOLUNTEER_NON_DIVER"
                }
                """;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value(ErrorCode.VALIDATION_ERROR.name()))
                .andExpect(jsonPath("$.error.message").value(containsString("email")));
    }

    @Test
    void malformedEmail_failsValidationWith400AndFieldMessage() throws Exception {
        String payload = """
                {
                  "firstName": "Ada",
                  "lastName": "Lovelace",
                  "email": "not-an-email",
                  "password": "supersecret",
                  "role": "VOLUNTEER_NON_DIVER"
                }
                """;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value(ErrorCode.VALIDATION_ERROR.name()))
                .andExpect(jsonPath("$.error.message").value(containsString("email")));
    }

    @Test
    void validPayload_passesValidationAndRegistersSuccessfully() throws Exception {
        String payload = """
                {
                  "firstName": "Ada",
                  "lastName": "Lovelace",
                  "email": "ada-validation-test@example.com",
                  "password": "supersecret",
                  "role": "VOLUNTEER_NON_DIVER"
                }
                """;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.role").value("VOLUNTEER_NON_DIVER"));
    }
}
