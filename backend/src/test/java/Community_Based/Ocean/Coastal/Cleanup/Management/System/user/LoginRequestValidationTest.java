package Community_Based.Ocean.Coastal.Cleanup.Management.System.user;

import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.error.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Exercises LoginRequest's Bean Validation annotations end-to-end through the real
 * POST /auth/login (permitAll — no Authorization header needed). Doesn't touch the DB (no user
 * is ever registered here), so no @Transactional/cleanup is needed.
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class LoginRequestValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void blankEmail_failsValidationWith400AndFieldMessage() throws Exception {
        String payload = """
                {
                  "email": "",
                  "password": "supersecret"
                }
                """;

        mockMvc.perform(post("/auth/login")
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
                  "email": "not-an-email",
                  "password": "supersecret"
                }
                """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value(ErrorCode.VALIDATION_ERROR.name()))
                .andExpect(jsonPath("$.error.message").value(containsString("email")));
    }

    @Test
    void blankPassword_failsValidationWith400AndFieldMessage() throws Exception {
        String payload = """
                {
                  "email": "ada@example.com",
                  "password": ""
                }
                """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value(ErrorCode.VALIDATION_ERROR.name()))
                .andExpect(jsonPath("$.error.message").value(containsString("password")));
    }

    @Test
    void wellFormedPayload_passesValidation_thenFails401AtServiceLayerForNonExistentUser() throws Exception {
        // Well-formed email/password for a user that was never registered — proves validation
        // itself doesn't block this request (it would be 400 if it did); the 401 comes from
        // AuthService.login()'s BadCredentialsException, one layer further in. The genuine
        // successful-login happy path (a real pre-registered user) is covered by
        // AuthControllerTest instead, since it needs a registered user to be meaningful.
        String payload = """
                {
                  "email": "nobody-login-validation-test@example.com",
                  "password": "supersecret"
                }
                """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value(ErrorCode.UNAUTHORIZED.name()));
    }
}
