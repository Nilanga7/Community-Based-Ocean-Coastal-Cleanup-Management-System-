package Community_Based.Ocean.Coastal.Cleanup.Management.System.common.config;

import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.entity.enums.UserRole;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.error.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end through the real SecurityFilterChain/JwtAuthenticationFilter. No controllers exist
 * for /auth/register or /auth/login yet (later steps), so the permitAll assertion checks that
 * security itself doesn't block the request (404 from the missing handler, not 401/403) rather
 * than checking for a 200 that no controller can produce yet.
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class SecurityConfigTest {

    private static final String PROTECTED_PATH = "/users/1/profile";

    @Autowired
    private MockMvc mockMvc;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Test
    void permitAllPath_isNotBlockedBySecurity() throws Exception {
        mockMvc.perform(get("/auth/login"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertThat(status)
                            .as("permitAll path must not be blocked by security (401/403)")
                            .isNotIn(401, 403);
                });
    }

    @Test
    void protectedPath_withoutToken_returns401() throws Exception {
        mockMvc.perform(get(PROTECTED_PATH))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value(ErrorCode.UNAUTHORIZED.name()))
                .andExpect(jsonPath("$.error.message").value(ErrorCode.UNAUTHORIZED.defaultMessage()));
    }

    @Test
    void protectedPath_withGarbageToken_returns401() throws Exception {
        mockMvc.perform(get(PROTECTED_PATH)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer not-a-real-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value(ErrorCode.UNAUTHORIZED.name()))
                .andExpect(jsonPath("$.error.message").value(ErrorCode.UNAUTHORIZED.defaultMessage()));
    }

    @Test
    void protectedPath_withExpiredToken_returns401() throws Exception {
        JwtService alreadyExpiredIssuer = new JwtService(jwtSecret, -1_000L);
        String expiredToken = alreadyExpiredIssuer.generateToken(1, UserRole.VOLUNTEER_NON_DIVER);

        mockMvc.perform(get(PROTECTED_PATH)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value(ErrorCode.UNAUTHORIZED.name()))
                .andExpect(jsonPath("$.error.message").value(ErrorCode.UNAUTHORIZED.defaultMessage()));
    }

    @Test
    void authenticatedButWrongRole_returns403() throws Exception {
        JwtService jwtService = new JwtService(jwtSecret, 86_400_000L);
        String nonAdminToken = jwtService.generateToken(1, UserRole.VOLUNTEER_NON_DIVER);

        mockMvc.perform(get("/test-only/admin-only")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + nonAdminToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value(ErrorCode.FORBIDDEN.name()))
                .andExpect(jsonPath("$.error.message").value(ErrorCode.FORBIDDEN.defaultMessage()));
    }

    // A browser never sends an Authorization header on a CORS preflight — if the security filter
    // chain doesn't let OPTIONS through before authorization runs, the preflight itself gets
    // 401/403, and the browser aborts the real request with a CORS error before it's ever sent.
    // These headers (Origin + Access-Control-Request-Method) are what make Spring's CorsFilter
    // recognize this as an actual preflight (CorsUtils.isPreFlightRequest) rather than a plain
    // OPTIONS request — a bare OPTIONS with neither header is not what a browser sends and isn't
    // the scenario being protected against here.
    @Test
    void optionsPreflight_toProtectedGetPath_isNotBlockedBySecurity() throws Exception {
        mockMvc.perform(options(PROTECTED_PATH)
                        .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertThat(status)
                            .as("CORS preflight must not be blocked by security (401/403)")
                            .isNotIn(401, 403);
                });
    }

    // Same as above but for a path whose real method is POST with a JSON body (diver-equipment
    // creation) — this is the shape that actually triggers a browser preflight in the first place
    // (application/json is not a CORS-safelisted content type), unlike the GET case above.
    @Test
    void optionsPreflight_toProtectedPostOnlyPath_isNotBlockedBySecurity() throws Exception {
        mockMvc.perform(options("/users/1/diver-equipment")
                        .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "authorization,content-type"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertThat(status)
                            .as("CORS preflight must not be blocked by security (401/403)")
                            .isNotIn(401, 403);
                });
    }

    // Control case for the two tests above: DefaultCorsProcessor itself rejects a preflight with
    // 403 when Origin isn't in CorsConfigurationSource's allowed list (see corsConfigurationSource
    // in SecurityConfig, backed by the cors.allowed-origin property — http://localhost:5173 in
    // this test profile, since no override exists in application-test.properties or the
    // environment). Without this negative case, the two tests above passing would be equally
    // consistent with "the endpoint accepts every preflight unconditionally" as with "origin
    // matching actually works" — this proves it's the latter: a disallowed origin on the exact
    // same request shape is rejected, so an allowed origin passing is real, not incidental.
    @Test
    void optionsPreflight_withDisallowedOrigin_isRejected() throws Exception {
        mockMvc.perform(options(PROTECTED_PATH)
                        .header(HttpHeaders.ORIGIN, "http://evil.example")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isForbidden());
    }
}
