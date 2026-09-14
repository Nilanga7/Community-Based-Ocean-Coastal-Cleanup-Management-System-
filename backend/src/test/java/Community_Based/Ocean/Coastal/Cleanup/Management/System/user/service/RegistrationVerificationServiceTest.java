package Community_Based.Ocean.Coastal.Cleanup.Management.System.user.service;

import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.entity.RegistrationVerification;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.entity.User;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.entity.enums.ReviewStatus;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.entity.enums.UserRole;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.entity.enums.UserStatus;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.repository.RegistrationVerificationRepository;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Real repositories against the real dev MySQL (not mocked) — the point of these tests is
 * proving initiate()'s existsByUserUserId-based idempotency actually respects the unique
 * constraint on registration_verification.user_id, which a mocked repository can't demonstrate.
 * @Transactional rolls back everything written here.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RegistrationVerificationServiceTest {

    @Autowired
    private RegistrationVerificationService registrationVerificationService;

    @Autowired
    private RegistrationVerificationRepository registrationVerificationRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = userRepository.save(User.builder()
                .firstName("Test")
                .lastName("User")
                .email("registration-verification-test-" + UUID.randomUUID() + "@example.com")
                .password("hashed-password")
                .role(UserRole.VOLUNTEER_DIVER)
                .status(UserStatus.ACTIVE)
                .createdDate(LocalDateTime.now())
                .build());
    }

    @Test
    void initiate_noExistingRow_createsPendingRowForThatUser() {
        registrationVerificationService.initiate(user.getUserId());

        List<RegistrationVerification> rowsForUser = rowsForUser(user.getUserId());

        assertThat(rowsForUser).hasSize(1);
        assertThat(rowsForUser.get(0).getStatus()).isEqualTo(ReviewStatus.PENDING);
        assertThat(rowsForUser.get(0).getUser().getUserId()).isEqualTo(user.getUserId());
    }

    @Test
    void initiate_secondCallForSameUser_isNoOp_doesNotDuplicateOrViolateUniqueConstraint() {
        registrationVerificationService.initiate(user.getUserId());

        // Simulates a second verification-document upload by the same user — must not throw
        // (would violate uk_regver_user if it attempted a second insert) and must not create a
        // second row.
        registrationVerificationService.initiate(user.getUserId());

        assertThat(rowsForUser(user.getUserId())).hasSize(1);
    }

    private List<RegistrationVerification> rowsForUser(Integer userId) {
        return registrationVerificationRepository.findAll().stream()
                .filter(row -> row.getUser().getUserId().equals(userId))
                .toList();
    }
}
