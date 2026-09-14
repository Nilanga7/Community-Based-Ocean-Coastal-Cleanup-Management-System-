package Community_Based.Ocean.Coastal.Cleanup.Management.System.common.repository;

import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.entity.RegistrationVerification;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Owned by the Admin Review module (see CLAUDE.md's Module scope table). Exists here already so
 * RegistrationVerificationService.initiate(userId) (called from the User Registration module's
 * document-upload flow) can create the initial PENDING row — see that class for the one write
 * this module is allowed to make to this table.
 */
public interface RegistrationVerificationRepository extends JpaRepository<RegistrationVerification, Integer> {

    boolean existsByUserUserId(Integer userId);
}
