package Community_Based.Ocean.Coastal.Cleanup.Management.System.user.service;

import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.entity.RegistrationVerification;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.entity.User;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.entity.enums.ReviewStatus;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.repository.RegistrationVerificationRepository;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Minimal stub: only creates the initial PENDING row (see interface Javadoc) — does not write
 * currentStage/stageStatus/communityResult on CleanupRequestPost or anything else. The Admin
 * Review module owner builds the actual review workflow (approve/reject/clarify, notifications)
 * on top of this row once that module lands.
 */
@Service
public class RegistrationVerificationServiceImpl implements RegistrationVerificationService {

    private final RegistrationVerificationRepository registrationVerificationRepository;
    private final UserRepository userRepository;

    public RegistrationVerificationServiceImpl(
            RegistrationVerificationRepository registrationVerificationRepository,
            UserRepository userRepository
    ) {
        this.registrationVerificationRepository = registrationVerificationRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void initiate(Integer userId) {
        if (registrationVerificationRepository.existsByUserUserId(userId)) {
            return;
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("No such user: " + userId));

        RegistrationVerification verification = RegistrationVerification.builder()
                .user(user)
                .status(ReviewStatus.PENDING)
                .submittedDate(LocalDateTime.now())
                .build();

        registrationVerificationRepository.save(verification);
    }
}
