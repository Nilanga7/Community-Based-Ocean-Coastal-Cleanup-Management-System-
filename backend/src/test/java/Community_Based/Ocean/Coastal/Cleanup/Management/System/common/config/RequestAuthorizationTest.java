package Community_Based.Ocean.Coastal.Cleanup.Management.System.common.config;

import Community_Based.Ocean.Coastal.Cleanup.Management.System.common.error.ForbiddenOperationException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Pure unit test — no Spring context, no database. RequestAuthorization is a stateless static
 * utility read only through an Authentication, so its Authentication/GrantedAuthority collaborator
 * is mocked directly rather than standing up a real security context.
 * <p>
 * UserProfileControllerTest, DiverEquipmentControllerTest and VerificationDocumentControllerTest
 * already exercise this class indirectly (missing token -> 401, wrong user -> 403), but those
 * 401s are produced by Spring Security's filter chain rejecting the request before the controller
 * (and therefore RequestAuthorization) ever runs — none of them actually calls these methods with
 * a null Authentication. The "unauthenticated" cases here instead lock in RequestAuthorization's
 * own contract in isolation: principalUserId (the shared helper both public methods call first)
 * null-checks its Authentication argument and throws ForbiddenOperationException, the same
 * exception every other denial in this class throws, rather than letting a caller outside the
 * usual filter chain see a raw NullPointerException/500.
 */
class RequestAuthorizationTest {

    private static final String MESSAGE = "not allowed";

    @Test
    void requireSelf_withMatchingPrincipalId_passes() {
        Authentication authentication = authenticationOf(1, "ROLE_VOLUNTEER_NON_DIVER");

        RequestAuthorization.requireSelf(1, authentication, MESSAGE);
    }

    @Test
    void requireSelf_withDifferentIdEvenWhenElevated_isDenied() {
        Authentication authentication = authenticationOf(2, "ROLE_ADMIN");

        assertThatThrownBy(() -> RequestAuthorization.requireSelf(1, authentication, MESSAGE))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage(MESSAGE);
    }

    @Test
    void requireSelf_withNullAuthentication_isDenied() {
        assertThatThrownBy(() -> RequestAuthorization.requireSelf(1, null, MESSAGE))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void requireSelfOrElevated_withMatchingPrincipalId_passes() {
        Authentication authentication = authenticationOf(1, "ROLE_VOLUNTEER_NON_DIVER");

        RequestAuthorization.requireSelfOrElevated(1, authentication, MESSAGE);
    }

    @Test
    void requireSelfOrElevated_withDifferentIdAndAdminRole_passes() {
        Authentication authentication = authenticationOf(2, "ROLE_ADMIN");

        RequestAuthorization.requireSelfOrElevated(1, authentication, MESSAGE);
    }

    @Test
    void requireSelfOrElevated_withDifferentIdAndGovernmentOfficerRole_passes() {
        Authentication authentication = authenticationOf(2, "ROLE_GOVERNMENT_OFFICER");

        RequestAuthorization.requireSelfOrElevated(1, authentication, MESSAGE);
    }

    @Test
    void requireSelfOrElevated_withDifferentIdAndNoElevatedRole_isDenied() {
        Authentication authentication = authenticationOf(2, "ROLE_VOLUNTEER_DIVER");

        assertThatThrownBy(() -> RequestAuthorization.requireSelfOrElevated(1, authentication, MESSAGE))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage(MESSAGE);
    }

    @Test
    void requireSelfOrElevated_withNullAuthentication_isDenied() {
        assertThatThrownBy(() -> RequestAuthorization.requireSelfOrElevated(1, null, MESSAGE))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void principalUserId_returnsAuthenticationPrincipal() {
        Authentication authentication = authenticationOf(7, "ROLE_ADMIN");

        assertThat(RequestAuthorization.principalUserId(authentication)).isEqualTo(7);
    }

    @Test
    void principalUserId_withNullAuthentication_throwsForbiddenOperationException() {
        assertThatThrownBy(() -> RequestAuthorization.principalUserId(null))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void hasElevatedRole_returnsFalseForNonElevatedRole() {
        Authentication authentication = authenticationOf(1, "ROLE_VOLUNTEER_DIVER");

        assertThat(RequestAuthorization.hasElevatedRole(authentication)).isFalse();
    }

    private Authentication authenticationOf(Integer principalUserId, String authority) {
        Authentication authentication = Mockito.mock(Authentication.class);
        Mockito.when(authentication.getPrincipal()).thenReturn(principalUserId);
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(authority));
        Mockito.doReturn(authorities).when(authentication).getAuthorities();
        return authentication;
    }
}
