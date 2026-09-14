package Community_Based.Ocean.Coastal.Cleanup.Management.System.user;

import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.config.JwtService;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.entity.enums.UserRole;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.error.ErrorCode;
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

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Exercises LoginRequest's Bean Validation annotations end-to-end through the test-only
 * LoginRequestTestController, reusing GlobalExceptionHandler's MethodArgumentNotValidException
 * handling from Step 4 — the same 400/ErrorResponse pipeline a real /auth/login controller will
 * get for free once it exists. Mirrors RegisterRequestValidationTest's pattern.
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class LoginRequestValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Value("${jwt.secret}")
    private String jwtSecret;

    private String bearerToken;

    @BeforeEach
    void issueToken() {
        JwtService jwtService = new JwtService(jwtSecret, 86_400_000L);
        bearerToken = "Bearer " + jwtService.generateToken(1, UserRole.VOLUNTEER_NON_DIVER);
    }

    @Test
    void blankEmail_failsValidationWith400AndFieldMessage() throws Exception {
        String payload = """
                {
                  "email": "",
                  "password": "supersecret"
                }
                """;

        mockMvc.perform(post("/test-only/login")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken)
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

        mockMvc.perform(post("/test-only/login")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken)
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

        mockMvc.perform(post("/test-only/login")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value(ErrorCode.VALIDATION_ERROR.name()))
                .andExpect(jsonPath("$.error.message").value(containsString("password")));
    }

    @Test
    void validPayload_passesValidation() throws Exception {
        String payload = """
                {
                  "email": "ada@example.com",
                  "password": "supersecret"
                }
                """;

        mockMvc.perform(post("/test-only/login")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isNoContent());
    }
}
