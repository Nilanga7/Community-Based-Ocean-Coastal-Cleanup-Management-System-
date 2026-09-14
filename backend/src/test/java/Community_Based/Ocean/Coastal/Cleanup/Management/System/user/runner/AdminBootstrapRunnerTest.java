package Community_Based.Ocean.Coastal.Cleanup.Management.System.user.runner;

import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.entity.Admin;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.entity.User;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.entity.enums.UserRole;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.repository.AdminRepository;
import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminBootstrapRunnerTest {

    private static final String RAW_PASSWORD = "supersecret-admin-password";

    @Mock
    private UserRepository userRepository;
    @Mock
    private AdminRepository adminRepository;

    // Real encoder, not a mock — the test needs to prove the persisted value is genuinely
    // bcrypt-hashed (and thus not equal to the plaintext), not just that encode() was called.
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private AdminBootstrapRunner runner(String email, String password) {
        return new AdminBootstrapRunner(userRepository, adminRepository, passwordEncoder, email, password);
    }

    @Test
    void noAdminExists_bothEnvVarsSet_savesAdminWithBcryptHashedPassword() {
        when(userRepository.existsByRole(UserRole.ADMIN)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setUserId(1);
            return user;
        });

        runner("admin@example.com", RAW_PASSWORD).run(null);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getRole()).isEqualTo(UserRole.ADMIN);
        assertThat(savedUser.getEmail()).isEqualTo("admin@example.com");
        assertThat(savedUser.getPassword())
                .as("password must be hashed, not stored as plaintext")
                .isNotEqualTo(RAW_PASSWORD);
        assertThat(passwordEncoder.matches(RAW_PASSWORD, savedUser.getPassword()))
                .as("stored value must actually be a valid bcrypt hash of the raw password")
                .isTrue();

        ArgumentCaptor<Admin> adminCaptor = ArgumentCaptor.forClass(Admin.class);
        verify(adminRepository).save(adminCaptor.capture());
        assertThat(adminCaptor.getValue().getUser().getUserId()).isEqualTo(1);
    }

    @Test
    void adminAlreadyExists_saveIsNeverCalled() {
        when(userRepository.existsByRole(UserRole.ADMIN)).thenReturn(true);

        runner("admin@example.com", RAW_PASSWORD).run(null);

        verify(userRepository, never()).save(any());
        verify(adminRepository, never()).save(any());
    }

    @Test
    void noAdminExists_emailMissing_throwsAndDoesNotSave() {
        when(userRepository.existsByRole(UserRole.ADMIN)).thenReturn(false);

        assertThatThrownBy(() -> runner(null, RAW_PASSWORD).run(null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("INITIAL_ADMIN_EMAIL");

        verify(userRepository, never()).save(any());
        verify(adminRepository, never()).save(any());
    }

    @Test
    void noAdminExists_passwordBlank_throwsAndDoesNotSave() {
        when(userRepository.existsByRole(UserRole.ADMIN)).thenReturn(false);

        assertThatThrownBy(() -> runner("admin@example.com", "   ").run(null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("INITIAL_ADMIN_PASSWORD");

        verify(userRepository, never()).save(any());
        verify(adminRepository, never()).save(any());
    }
}
