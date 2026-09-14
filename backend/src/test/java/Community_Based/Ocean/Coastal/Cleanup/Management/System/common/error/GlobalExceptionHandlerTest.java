package Community_Based.Ocean.Coastal.Cleanup.Management.System.common.error;

import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.config.JwtService;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.entity.enums.UserRole;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Exercises GlobalExceptionHandler end-to-end via the test-only ErrorHandlingTestController,
 * since no production controllers exist yet at this step. Every assertion checks against
 * ErrorCode's actual values rather than re-typed literals, so a future change to a code/message
 * in ErrorCode is reflected here automatically instead of silently drifting out of sync.
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class GlobalExceptionHandlerTest {

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
    void validationFailure_returns400WithSharedErrorShape() throws Exception {
        mockMvc.perform(post("/test-only/validate")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value(ErrorCode.VALIDATION_ERROR.name()))
                .andExpect(jsonPath("$.error.message").value(containsString("requiredField")));
    }

    @Test
    void entityNotFound_returns404WithSharedErrorShape() throws Exception {
        mockMvc.perform(get("/test-only/not-found")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value(ErrorCode.NOT_FOUND.name()))
                .andExpect(jsonPath("$.error.message").value(ErrorCode.NOT_FOUND.defaultMessage()));
    }

    @Test
    void unexpectedException_returns500WithSharedErrorShape() throws Exception {
        mockMvc.perform(get("/test-only/boom")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error.code").value(ErrorCode.INTERNAL_ERROR.name()))
                .andExpect(jsonPath("$.error.message").value(ErrorCode.INTERNAL_ERROR.defaultMessage()));
    }
}
