package Community_Based.Ocean.Coastal.Cleanup.Management.System.common.repository;

import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.entity.User;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.entity.enums.UserRole;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.entity.enums.UserStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Runs against the real dev MySQL (docker-compose), not an embedded database — this schema uses
 * MySQL-specific migration syntax (V1__init_schema.sql), so swapping in an embedded database
 * would defeat the point of validating against it. Each test still runs in its own transaction
 * that @DataJpaTest rolls back afterwards, so tests don't see each other's data.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void saveAndFindByEmail_roundTrips() {
        userRepository.saveAndFlush(validUser("roundtrip@example.com", UserRole.VOLUNTEER_NON_DIVER));

        Optional<User> found = userRepository.findByEmail("roundtrip@example.com");

        assertThat(found).isPresent();
        assertThat(found.get().getFirstName()).isEqualTo("Test");
        assertThat(found.get().getRole()).isEqualTo(UserRole.VOLUNTEER_NON_DIVER);
    }

    @Test
    void findByEmail_returnsEmpty_whenNoSuchUser() {
        assertThat(userRepository.findByEmail("nobody@example.com")).isEmpty();
    }

    @Test
    void existsByRole_reflectsWhetherThatRoleHasAnyUser() {
        // Deliberately NOT UserRole.ADMIN: AdminBootstrapRunner (Step 7c) is a real
        // ApplicationRunner that creates a real, permanently-committed ADMIN row the first time
        // any full @SpringBootTest context starts (that happens outside this test's own
        // transaction, so it isn't rolled back) — asserting "no ADMIN exists yet" would be order-
        // dependent on which test class happens to trigger that context first. GOVERNMENT_OFFICER
        // is never auto-created by anything, so it stays a reliable "definitely no rows yet" role.
        assertThat(userRepository.existsByRole(UserRole.GOVERNMENT_OFFICER)).isFalse();

        userRepository.saveAndFlush(validUser("first-gov-officer@example.com", UserRole.GOVERNMENT_OFFICER));

        assertThat(userRepository.existsByRole(UserRole.GOVERNMENT_OFFICER)).isTrue();
    }

    @Test
    void duplicateEmail_violatesUniqueConstraint() {
        userRepository.saveAndFlush(validUser("dup@example.com", UserRole.VOLUNTEER_DIVER));

        User duplicate = validUser("dup@example.com", UserRole.ORGANIZATION);

        assertThatThrownBy(() -> userRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private User validUser(String email, UserRole role) {
        return User.builder()
                .firstName("Test")
                .lastName("User")
                .email(email)
                .password("hashed-password")
                .role(role)
                .status(UserStatus.ACTIVE)
                .createdDate(LocalDateTime.now())
                .build();
    }
}
