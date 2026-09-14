package Community_Based.Ocean.Coastal.Cleanup.Management.System;

import Community_Based.Ocean.Coastal.Cleanup.Management.System.user.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Boots the full application context (not a @WebMvcTest/@DataJpaTest slice) to prove real
 * component scanning actually finds AuthService as a Spring bean under the user.service package —
 * every other AuthService-adjacent test so far either constructs it directly with mocks
 * (AuthServiceTest) or doesn't touch it at all, so none of them would catch a package/scanning
 * misconfiguration. Requires the dev MySQL container running (docker-compose up), same as
 * UserRepositoryTest.
 */
@SpringBootTest
@ActiveProfiles("test")
class ApplicationContextSmokeTest {

    @Autowired
    private AuthService authService;

    @Test
    void contextLoadsAndAuthServiceIsRegistered() {
        assertThat(authService).isNotNull();
    }
}
