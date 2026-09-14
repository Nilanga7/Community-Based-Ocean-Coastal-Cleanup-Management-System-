package Community_Based.Ocean.Coastal.Cleanup.Management.System.common.config;

import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.entity.enums.UserRole;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Pure unit test — no Spring context. JwtService's constructor takes the secret/expiry as plain
 * args (Spring only resolves the @Value annotations when the container creates the bean), so it
 * can be instantiated directly here with fixture values.
 */
class JwtServiceTest {

    // Fixture only — decodes to 32 bytes, satisfying HS256's minimum key length. Never reuse as
    // a real deployment secret.
    private static final String TEST_SECRET = "cVZ1GJpbKUo/nJa6W9waGsZ8RmVHXI+dj39WYS/pltY=";

    private final JwtService jwtService = new JwtService(TEST_SECRET, 86_400_000L);

    @Test
    void generateAndValidateToken_roundTripsUserIdAndRole() {
        String token = jwtService.generateToken(42, UserRole.VOLUNTEER_DIVER);

        assertThat(jwtService.validateToken(token)).isTrue();
        assertThat(jwtService.getUserIdFromToken(token)).isEqualTo(42L);
        assertThat(jwtService.getRoleFromToken(token)).isEqualTo(UserRole.VOLUNTEER_DIVER);
    }

    @Test
    void validateToken_returnsFalseForGarbageToken() {
        assertThat(jwtService.validateToken("not-a-real-token")).isFalse();
    }

    @Test
    void validateToken_returnsFalseForExpiredToken() {
        JwtService alreadyExpiredIssuer = new JwtService(TEST_SECRET, -1_000L);
        String expiredToken = alreadyExpiredIssuer.generateToken(7, UserRole.ADMIN);

        assertThat(jwtService.validateToken(expiredToken)).isFalse();
    }

    @Test
    void getUserIdFromToken_throwsForExpiredToken() {
        JwtService alreadyExpiredIssuer = new JwtService(TEST_SECRET, -1_000L);
        String expiredToken = alreadyExpiredIssuer.generateToken(7, UserRole.ADMIN);

        assertThatThrownBy(() -> jwtService.getUserIdFromToken(expiredToken))
                .isInstanceOf(io.jsonwebtoken.ExpiredJwtException.class);
    }
}
