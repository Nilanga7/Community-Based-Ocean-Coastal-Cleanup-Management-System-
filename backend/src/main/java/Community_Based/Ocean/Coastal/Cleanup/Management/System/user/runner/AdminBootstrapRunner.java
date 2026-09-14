package Community_Based.Ocean.Coastal.Cleanup.Management.System.user.runner;

import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.entity.Admin;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.entity.User;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.entity.enums.UserRole;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.entity.enums.UserStatus;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.repository.AdminRepository;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Creates the very first ADMIN account on startup, since ADMIN/GOVERNMENT_OFFICER accounts are
 * never self-registerable (see RegisterRequest's Javadoc) and, before this runs, there would be
 * no admin able to use POST /admin/users to create one. Runs once per startup, before the app
 * starts serving requests.
 */
@Component
public class AdminBootstrapRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrapRunner.class);

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final String initialAdminEmail;
    private final String initialAdminPassword;

    public AdminBootstrapRunner(
            UserRepository userRepository,
            AdminRepository adminRepository,
            PasswordEncoder passwordEncoder,
            @Value("${initial-admin.email}") String initialAdminEmail,
            @Value("${initial-admin.password}") String initialAdminPassword
    ) {
        this.userRepository = userRepository;
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
        this.initialAdminEmail = initialAdminEmail;
        this.initialAdminPassword = initialAdminPassword;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.existsByRole(UserRole.ADMIN)) {
            log.info("Admin bootstrap skipped — an ADMIN account already exists.");
            return;
        }

        if (isBlank(initialAdminEmail) || isBlank(initialAdminPassword)) {
            // Fail fast rather than starting with zero admins (nobody could ever create another
            // account) or letting a blank email/password NPE its way into a confusing failure
            // somewhere downstream.
            throw new IllegalStateException(
                    "No ADMIN account exists yet, and INITIAL_ADMIN_EMAIL/INITIAL_ADMIN_PASSWORD "
                            + "are not both set. Set both environment variables (see .env.example) "
                            + "and restart the backend, or create an admin account manually."
            );
        }

        User admin = User.builder()
                .firstName("System")
                .lastName("Administrator")
                .email(initialAdminEmail)
                .password(passwordEncoder.encode(initialAdminPassword))
                .role(UserRole.ADMIN)
                .status(UserStatus.ACTIVE)
                .createdDate(LocalDateTime.now())
                .build();
        admin = userRepository.save(admin);

        adminRepository.save(Admin.builder().user(admin).build());

        log.info("Bootstrapped initial ADMIN account ({}). Rotate its password after first login.",
                initialAdminEmail);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
