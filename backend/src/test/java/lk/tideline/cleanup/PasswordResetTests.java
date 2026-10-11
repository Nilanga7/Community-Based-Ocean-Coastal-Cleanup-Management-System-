package lk.tideline.cleanup;

import lk.tideline.cleanup.dto.AuthDtos.AuthResponse;
import lk.tideline.cleanup.dto.AuthDtos.LoginRequest;
import lk.tideline.cleanup.model.Role;
import lk.tideline.cleanup.model.User;
import lk.tideline.cleanup.repository.UserRepository;
import lk.tideline.cleanup.service.AuthService;
import lk.tideline.cleanup.service.PasswordResetMailer;
import lk.tideline.cleanup.service.PasswordResetService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/** Forgot password and "Remember me". */
@SpringBootTest
@TestPropertySource(properties = {"tideline.seed-demo-data=false", "tideline.uploads.directory=target/test-uploads"})
class PasswordResetTests {

    @Autowired
    private PasswordResetService passwordReset;

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository users;

    @Autowired
    private PasswordEncoder encoder;

    @MockBean
    private PasswordResetMailer mailer;

    @Test
    void aResetLinkSetsANewPasswordOnce() {
        User member = user();
        clearInvocations(mailer);

        passwordReset.requestReset(member.getEmail().toUpperCase());
        String token = sentToken(member);

        passwordReset.resetPassword(token, "brand-new-pass");
        assertThat(authService.login(new LoginRequest(member.getEmail(), "brand-new-pass", null)).token()).isNotBlank();
        assertThatThrownBy(() -> authService.login(new LoginRequest(member.getEmail(), "password123", null)))
                .isInstanceOf(BadCredentialsException.class);

        assertThatThrownBy(() -> passwordReset.resetPassword(token, "another-pass-1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("invalid or has expired");
    }

    @Test
    void onlyTheNewestLinkWorks() {
        User member = user();
        passwordReset.requestReset(member.getEmail());
        String first = sentToken(member);
        clearInvocations(mailer);
        passwordReset.requestReset(member.getEmail());
        String second = sentToken(member);

        assertThatThrownBy(() -> passwordReset.resetPassword(first, "brand-new-pass"))
                .isInstanceOf(IllegalArgumentException.class);
        passwordReset.resetPassword(second, "brand-new-pass");
    }

    @Test
    void anUnknownEmailQuietlySendsNothing() {
        clearInvocations(mailer);
        passwordReset.requestReset("nobody-" + UUID.randomUUID() + "@test.lk");
        verify(mailer, never()).send(any(), any(), anyLong());
    }

    @Test
    void rememberMeGivesALongerSession() {
        User member = user();
        AuthResponse normal = authService.login(new LoginRequest(member.getEmail(), "password123", false));
        AuthResponse remembered = authService.login(new LoginRequest(member.getEmail(), "password123", true));

        assertThat(normal.expiresInSeconds()).isEqualTo(12 * 60 * 60);
        assertThat(remembered.expiresInSeconds()).isEqualTo(30L * 24 * 60 * 60);
    }

    private String sentToken(User member) {
        ArgumentCaptor<String> link = ArgumentCaptor.forClass(String.class);
        verify(mailer).send(any(User.class), link.capture(), eq(30L));
        assertThat(link.getValue()).startsWith("http://localhost:5173/reset-password?token=");
        return link.getValue().substring(link.getValue().indexOf("token=") + 6);
    }

    private User user() {
        User user = new User();
        user.setFullName("Reset tester");
        user.setEmail(UUID.randomUUID() + "@test.lk");
        user.setPasswordHash(encoder.encode("password123"));
        user.setRole(Role.CITIZEN);
        return users.save(user);
    }
}
